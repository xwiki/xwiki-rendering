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
package org.xwiki.rendering.macro.html;

import org.xwiki.properties.annotation.PropertyDescription;

/**
 * Parameters for the {@link org.xwiki.rendering.internal.macro.html.HTMLMacro} Macro.
 *
 * @version $Id$
 * @since 1.6M1
 */
public class HTMLMacroParameters
{
    /**
     * Indicate if the user has asked to interpret wiki syntax or not.
     */
    private boolean wiki;

    /**
     * Indicate if the HTML should be transformed into valid XHTML or not.
     */
    private boolean clean = true;

    /**
     * @param wiki indicate if the user has asked to interpret wiki syntax or not
     */
    @PropertyDescription("Indicate if the wiki syntax in the macro will be interpreted or not.")
    public void setWiki(boolean wiki)
    {
        this.wiki = wiki;
    }

    /**
     * @param clean indicate if the HTML should be transformed into valid XHTML or not
     */
    @PropertyDescription("Indicate if the HTML should be transformed into valid XHTML or not.")
    public void setClean(boolean clean)
    {
        this.clean = clean;
    }

    /**
     * @return if the user has asked to interpret wiki syntax or not
     */
    public boolean isWiki()
    {
        return this.wiki;
    }

    /**
     * @return if the HTML should be transformed into valid XHTML or not
     */
    public boolean isClean()
    {
        return this.clean;
    }
}
