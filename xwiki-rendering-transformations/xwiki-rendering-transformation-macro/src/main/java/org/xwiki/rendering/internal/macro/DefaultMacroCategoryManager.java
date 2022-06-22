/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.rendering.internal.macro;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.macro.MacroCategoryManager;
import org.xwiki.rendering.macro.MacroId;
import org.xwiki.rendering.macro.MacroLookupException;
import org.xwiki.rendering.macro.MacroManager;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.transformation.macro.MacroTransformationConfiguration;

/**
 * Default implementation of {@link org.xwiki.rendering.macro.MacroCategoryManager}.
 *
 * @version $Id$
 * @since 2.0M3
 */
@Component
@Singleton
public class DefaultMacroCategoryManager implements MacroCategoryManager
{
    /**
     * Used to get macro categories defined by the user (if any).
     */
    @Inject
    private MacroTransformationConfiguration configuration;

    /**
     * Macro manager component used to check the existence of macros.
     */
    @Inject
    private MacroManager macroManager;

    /**
     * Internal help class to be able to search Macros matching a Macro Id.
     */
    private interface MacroMatcher
    {
        /**
         * @param macroId the macro Id to match
         * @return true if the concerned macro matches the macro Id
         */
        boolean match(MacroId macroId);
    }

    @Override
    public Set<String> getMacroCategories() throws MacroLookupException
    {
        return getMacroCategories(null);
    }

    @Override
    public Set<String> getMacroCategories(final Syntax syntax) throws MacroLookupException
    {
        return Collections.unmodifiableSet(getMacrosGroupedByCategories(syntax).keySet());
    }

    @Override
    public Set<MacroId> getMacroIds(String category) throws MacroLookupException
    {
        return getMacroIds(category, null);
    }

    @Override
    public Set<MacroId> getMacroIds(String category, final Syntax syntax) throws MacroLookupException
    {
        Set<MacroId> macros = getMacrosGroupedByCategories(syntax).get(category);
        return (null != macros) ? Collections.unmodifiableSet(macros) : Collections.<MacroId>emptySet();
    }

    /**
     * Returns a map of macros, grouped by their categories. Note that a macro can have more than one category and can
     * be returned under multiple keys.
     *
     * @param matcher a macro name matcher to be able to filter macros, used to filter macros for a given syntax
     * @return macro names grouped by category, including the 'null' macro category
     * @throws MacroLookupException if any error occurs when getting macros ids by category
     */
    private Map<String, Set<MacroId>> getMacroIdsByCategory(MacroMatcher matcher) throws MacroLookupException
    {
        Map<String, Set<MacroId>> result = new HashMap<>();

        // Find all registered macro ids
        Set<MacroId> macroIds = this.macroManager.getMacroIds();

        // Loop through all the macro ids and categorize them.
        // TODO: test this part with Option 1 (fully override, several categories).
        // TODO: also test in the current configuration to see how it's behaving currently.
        Properties categories = this.configuration.getCategories();
        for (MacroId macroId : macroIds) {
            if (matcher.match(macroId)) {
                // Check if this macro's category has been overwritten.
                String macroCategoriesProperty = categories.getProperty(macroId.toString());

                // If not, use the default category set by macro author.
                Set<String> macroCategories;
                if (macroCategoriesProperty == null) {
                    macroCategories = this.macroManager.getMacro(macroId).getDescriptor().getDefaultCategories();
                } else {
                    macroCategories = Arrays.stream(macroCategoriesProperty.split("\\s+,\\s+"))
                        .collect(Collectors.toSet());
                }

                if (macroCategories != null) {
                    for (String macroCategory : macroCategories) {
                        addToCategory(result, macroId, macroCategory);
                    }
                } else {
                    // If no categories are found, the macro is added to a single "null" category.
                    addToCategory(result, macroId, null);
                }
            }
        }

        return result;
    }

    private Map<String, Set<MacroId>> getMacrosGroupedByCategories(Syntax syntax) throws MacroLookupException
    {
        return getMacroIdsByCategory(macroId -> {
            // True if the macroId has no syntax or if it has one it has to match the passed syntax
            return syntax == null || macroId.getSyntax() == null || macroId.getSyntax().equals(syntax);
        });
    }

    private void addToCategory(Map<String, Set<MacroId>> result, MacroId macroId, String macroCategory)
    {
        // Add to category. Note the category can also be null.
        Set<MacroId> ids = result.get(macroCategory);
        if (ids == null) {
            ids = new HashSet<>();
        }
        ids.add(macroId);
        result.put(macroCategory, ids);
    }
}
