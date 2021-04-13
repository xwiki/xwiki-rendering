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
package org.xwiki.rendering.syntax;

import java.util.Map;
import java.util.Optional;

import org.xwiki.component.annotation.Role;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.stability.Unstable;

/**
 * Register, unregister and list syntaxes available in the wiki. Syntaxes can be automatically registered by
 * implementing the {@code javax.inject.Provider<List<Syntax>>} component role (and they are automatically unregistered
 * when the component is unregistered from the Component Manager). If a syntax is registered manually by calling a
 * register method from this class, then it also needs to be unregistered manually when the extension bringing it is
 * uninstalled.
 *
 * @version $Id$
 * @since 13.3RC1
 */
@Unstable
@Role
public interface SyntaxRegistry
{
    /**
     * Adds one or several Syntaxes to the Syntax Registry.
     *
     * @param syntaxes the syntaxes to add
     */
    void registerSyntaxes(Syntax... syntaxes);

    /**
     * Removes one or several Syntaxes from the Syntax Registry.
     *
     * @param syntaxes the syntaxes to remove
     */
    void unregisterSyntaxes(Syntax... syntaxes);

    /**
     * @return the list of available Syntaxes
     */
    Map<String, Syntax> getSyntaxes();

    /**
     * @param syntaxId the syntax represented as a string (e.g. {@code xwiki/2.1})
     * @return the {@code Syntax} object taken from the list of already registered Syntaxes and
     *         {@link Optional#empty()} if not found
     */
    Optional<Syntax> getSyntax(String syntaxId);

    /**
     * Parse the provided string representation of a Syntax and return a typed {@code Syntax} object.
     *
     * @param syntaxId the syntax represented as a string (e.g. {@code xwiki/2.1})
     * @return the Syntax from the registry if found and otherwise construct a Syntax object based on the string
     *         representation (but with a syntax type name equal to the syntax type id since the information is not
     *         present in the string representation).
     * @throws ParseException when the passed syntax is invalid
     */
    Syntax resolveSyntax(String syntaxId) throws ParseException;
}
