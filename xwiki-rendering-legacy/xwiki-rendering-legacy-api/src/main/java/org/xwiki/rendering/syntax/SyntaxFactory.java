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

import org.xwiki.component.annotation.Role;
import org.xwiki.rendering.parser.ParseException;

/**
 * @version $Id$
 * @since 2.0RC1
 * @deprecated since 9.8RC1, use {@link Syntax#valueOf(String)} instead
 */
@Role
@Deprecated
public interface SyntaxFactory
{
    /**
     * @param syntaxAsIdString the syntax as a string (eg "xwiki/2.0", "plain/1.0")
     * @return the parsed syntax as a Syntax object
     * @throws ParseException in case the string doesn't represent a valid syntax
     * @deprecated since 9.8RC1, use {@link Syntax#valueOf(String)} instead
     */
    @Deprecated
    Syntax createSyntaxFromIdString(String syntaxAsIdString) throws ParseException;
}
