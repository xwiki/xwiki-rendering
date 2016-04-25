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
 * An immutable object which holds information about a macro reference.
 * Macros are unique just by their name.
 * The content and parameters are optional.<br>
 * <br>
 * There are a few 'built in' macro names:
 * <ul>
 * <li>toc</li>
 * <li>notoc</li>
 * <li>forcetoc</li>
 * <li>footnotes</li>
 * </ul>
 *
 * @version $Id$
 * @since 4.0M1
 */
public class WikiMacro
{
    public final static String MACRO_TOC = "toc";

    public final static String MACRO_NOTOC = "notoc";

    public final static String MACRO_FORCETOC = "forcetoc";

    public final static String MACRO_FOOTNOTES = "footnotes";

    public final static String UNHANDLED_MACRO = "unhandled";

    private final String name;

    private final WikiParameters wikiParameters;

    private final String content;

    public WikiMacro(String name)
    {
        this(name, WikiParameters.EMPTY, null);
    }

    public WikiMacro(String name, WikiParameters wikiParameters)
    {
        this(name, wikiParameters, null);
    }

    public WikiMacro(String name, WikiParameters wikiParameters, String content)
    {
        super();
        this.name = name;
        this.content = content;
        this.wikiParameters = wikiParameters;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return the content
     */
    public String getContent()
    {
        return content;
    }

    /**
     * @return the wikiParameters
     */
    public WikiParameters getWikiParameters()
    {
        return wikiParameters;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        WikiMacro other = (WikiMacro) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "{{" + name + " | " + content + " | " + wikiParameters + "}}";
    }
}
