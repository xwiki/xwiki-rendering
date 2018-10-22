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
package org.xwiki.rendering.wikimodel.xhtml.impl;

import java.util.ArrayList;
import java.util.List;

import org.xwiki.rendering.wikimodel.WikiParameter;
import org.xwiki.rendering.wikimodel.WikiParameters;
import org.xwiki.rendering.wikimodel.impl.WikiScannerUtil;

/**
 * This class represents some generic information related to macro and their parsing / rendering.
 *
 * @version $Id$
 * @since 10.9
 */
public class MacroInfo
{
    private static final String MACRO_SEPARATOR = "|-|";

    public static final String MACRO_START = "startmacro:";

    public static final String MACRO_STOP = "stopmacro";

    private final String name;
    private final WikiParameters parameters;

    private String content;

    public MacroInfo(String content)
    {
        String macroString = content.substring(MACRO_START.length());
        int index = macroString.indexOf(MACRO_SEPARATOR);

        if (index != -1) {
            // Extract macro name
            name = macroString.substring(0, index);

            // Remove macro name part and continue parsing
            macroString = macroString.substring(index + MACRO_SEPARATOR.length());

            index = macroString.indexOf(MACRO_SEPARATOR);
            if (index != -1) {
                // Extract macro parameters
                List<WikiParameter> parameters = new ArrayList<WikiParameter>();
                index = WikiScannerUtil.splitToPairs(macroString, parameters, null, MACRO_SEPARATOR);
                this.parameters = new WikiParameters(parameters);

                // Extract macro content
                if (macroString.length() > index) {
                    this.content = macroString.substring(index + MACRO_SEPARATOR.length());
                } else {
                    this.content = null;
                }
            } else {
                // There is only parameters remaining in the string, the
                // macro does not have content
                // Extract macro parameters
                this.parameters = WikiParameters.newWikiParameters(macroString);
                this.content = null;
            }
        } else {
            // There is only macro name, the macro does not have
            // parameters
            // or content
            name = macroString;
            this.content = null;
            parameters = WikiParameters.EMPTY;
        }
    }

    public String getName()
    {
        return name;
    }

    public WikiParameters getParameters()
    {
        return parameters;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }
}