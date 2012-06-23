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
package org.xwiki.rendering.internal.macro.jira.displayer.field;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.jdom2.Element;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.VerbatimBlock;
import org.xwiki.rendering.macro.jira.JIRAFieldDisplayer;

/**
 * Common Field Displayer for Dates. Parses field in the JIRA date format and generates date the format
 * {@code dd-MMM-yyyy}.
 *
 * @version $Id$
 * @since 4.2M1
 */
public abstract class AbstractDateJIRAFieldDisplayer implements JIRAFieldDisplayer
{
    /**
     * JIRA Date format.
     */
    private DateFormat jiraDateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");

    /**
     * Date format for displaying.
     */
    private DateFormat displayDateFormat = new SimpleDateFormat("dd-MMM-yyyy");

    @Override
    public List<Block> displayField(String fieldName, Element issue)
    {
        List<Block> result;
        String date = issue.getChildText(fieldName);
        if (date != null) {
            try {
                Date parsedDate = this.jiraDateFormat.parse(date);
                result = Arrays.<Block>asList(new VerbatimBlock(this.displayDateFormat.format(parsedDate), true));
            } catch (ParseException e) {
                result = Arrays.<Block>asList(new VerbatimBlock(date, true));
            }
        } else {
            result = Collections.emptyList();
        }
        return result;
    }
}
