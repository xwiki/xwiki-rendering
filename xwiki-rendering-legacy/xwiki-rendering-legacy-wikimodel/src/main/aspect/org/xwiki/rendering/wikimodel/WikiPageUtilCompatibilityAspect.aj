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
package org.xwiki.rendering.wikimodel;

/**
 * Add a backward compatibility layer to the {@link WikiPageUtil} class.
 *
 * @version $Id$
 * @since 18.8.0RC1
 */
public privileged aspect WikiPageUtilCompatibilityAspect
{
    /**
     * @param ch the character to check
     * @param colonEnabled if this flag is <code>true</code> then this method accepts the ':' symbol.
     * @return <code>true</code> if the given value is a valid XML name character
     * @deprecated use {@link WikiPageUtil#isValidXmlNameChar(int, boolean)} instead
     */
    @Deprecated(since = "18.8.0RC1")
    public static boolean WikiPageUtil.isValidXmlNameChar(char ch, boolean colonEnabled)
    {
        return WikiPageUtil.isValidXmlNameChar((int) ch, colonEnabled);
    }

    /**
     * @param ch the character to check
     * @param colonEnabled if this flag is <code>true</code> then this method accepts the ':' symbol.
     * @return <code>true</code> if the given value is a valid first character for an XML name
     * @deprecated use {@link WikiPageUtil#isValidXmlNameStartChar(int, boolean)} instead
     */
    @Deprecated(since = "18.8.0RC1")
    public static boolean WikiPageUtil.isValidXmlNameStartChar(char ch, boolean colonEnabled)
    {
        return WikiPageUtil.isValidXmlNameStartChar((int) ch, colonEnabled);
    }
}
