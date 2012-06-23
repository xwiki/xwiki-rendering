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
package org.xwiki.rendering.internal.macro.jira.displayer;

import java.util.List;

import javax.inject.Inject;

import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.macro.jira.JIRADisplayer;
import org.xwiki.rendering.macro.jira.JIRAFieldDisplayer;
import org.xwiki.rendering.macro.jira.JIRAMacroParameters;

/**
 * Common issue Displayer that Displayers can extend and that provides common methods.
 *
 * @version $Id$
 * @since 4.2M1
 */
public abstract class AbstractJIRADisplayer implements JIRADisplayer
{
    /**
     * Used to find specific Field displayers.
     */
    @Inject
    protected ComponentManager componentManager;

    /**
     * Default field displayer to use when there's no specific field displayer for a field.
     */
    @Inject
    protected JIRAFieldDisplayer defaultDisplayer;

    /**
     * @param fieldName the field to display
     * @return the field displayer to use to display the passed field name
     */
    protected JIRAFieldDisplayer getFieldDisplayer(String fieldName)
    {
        JIRAFieldDisplayer displayer;
        try {
            displayer = this.componentManager.getInstance(JIRAFieldDisplayer.class, fieldName);
        } catch (ComponentLookupException e) {
            // Use the default displayer
            displayer = this.defaultDisplayer;
        }
        return displayer;
    }

    /**
     * @param parameters the macro parameters from which to get an optional list of JIRA field names to display (if
     *        not defined by the user then use default field names)
     * @return the list of JIRA field names to be displayed
     */
    protected List<String> getFields(JIRAMacroParameters parameters)
    {
        List<String> fields = parameters.getFields();
        if (fields == null) {
            fields = getDefaultFields();
        }
        return fields;
    }

    /**
     * @return the default list of fields to display if not overriden by the user
     */
    protected abstract List<String> getDefaultFields();
}
