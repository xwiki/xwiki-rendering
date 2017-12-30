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
package org.xwiki.rendering.internal.renderer;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generates syntax for a parameters group like macros and links.
 *
 * @version $Id$
 * @since 1.9RC2
 */
public class ParametersPrinter
{
    /**
     * Quote character.
     */
    private static final String QUOTE = "\"";

    private final String escapedStrings;

    private char escapeChar;

    private Pattern escaped;

    private String replacement;

    /**
     * Default constructor.
     * 
     * @deprecated since 7.4.5 and 8.2RC1, use {@link #ParametersPrinter(char, String...) instead
     */
    @Deprecated
    public ParametersPrinter()
    {
        this.escapedStrings = Pattern.quote(QUOTE);
    }

    /**
     * @param escapeChar the character used to escape a meaningful string
     * @param escapedStrings the meaningful strings to escape
     * @since 7.4.5
     * @since 8.2RC1
     */
    public ParametersPrinter(char escapeChar, String... escapedStrings)
    {
        StringBuilder builder = new StringBuilder();

        builder.append(Pattern.quote(QUOTE));
        for (String str : escapedStrings) {
            builder.append('|');
            builder.append(Pattern.quote(str));
        }

        this.escapedStrings = builder.toString();

        setEscapeChar(escapeChar);
    }

    private void setEscapeChar(char escapeChar)
    {
        this.escapeChar = escapeChar;

        StringBuilder replacementBuilder = new StringBuilder();
        replacementBuilder.append(Matcher.quoteReplacement(String.valueOf(escapeChar)));
        replacementBuilder.append("$0");
        this.replacement = replacementBuilder.toString();

        this.escaped = Pattern.compile(Pattern.quote(String.valueOf(this.escapeChar)) + '|' + this.escapedStrings);
    }

    /**
     * Print the parameters as a String.
     *
     * @param parameters the parameters to print
     * @param escapeChar the character used in front of a special character when need to escape it
     * @return the printed parameters
     * @deprecated since 7.4.5 and 8.2RC1, use {@link #print(Map)} instead
     */
    @Deprecated
    public String print(Map<String, String> parameters, char escapeChar)
    {
        setEscapeChar(escapeChar);

        return print(parameters);
    }

    /**
     * Print the parameters as a String.
     *
     * @param parameters the parameters to print
     * @return the printed parameters
     * @since 7.4.5
     * @since 8.2RC1
     */
    public String print(Map<String, String> parameters)
    {
        StringBuilder builder = new StringBuilder();

        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            String value = entry.getValue();
            String key = entry.getKey();

            if (key != null && value != null) {
                if (builder.length() > 0) {
                    builder.append(' ');
                }
                builder.append(print(key, value));
            }
        }

        return builder.toString();
    }

    /**
     * Print a parameter as a String.
     *
     * @param parameterName the name of the parameter to print
     * @param parameterValue the value of the parameter to print
     * @param escapeChar the character used in front of a special character when need to escape it
     * @return the printed parameter
     * @deprecated since 7.4.5 and 8.2RC1, use {@link #print(String, String)} instead
     */
    @Deprecated
    public String print(String parameterName, String parameterValue, char escapeChar)
    {
        setEscapeChar(escapeChar);

        return print(parameterName, parameterValue);
    }

    /**
     * Print a parameter as a String.
     *
     * @param parameterName the name of the parameter to print
     * @param parameterValue the value of the parameter to print
     * @return the printed parameter
     * @since 7.4.5
     * @since 8.2RC1
     */
    public String print(String parameterName, String parameterValue)
    {
        // escape meaningfull strings
        String value = this.escaped.matcher(parameterValue).replaceAll(this.replacement);

        return parameterName + "=" + QUOTE + value + QUOTE;
    }
}
