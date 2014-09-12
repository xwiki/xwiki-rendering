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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import org.jdom2.Element;
import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.TableBlock;
import org.xwiki.rendering.block.TableCellBlock;
import org.xwiki.rendering.block.TableHeadCellBlock;
import org.xwiki.rendering.block.TableRowBlock;
import org.xwiki.rendering.block.VerbatimBlock;
import org.xwiki.rendering.macro.jira.JIRAFields;
import org.xwiki.rendering.macro.jira.JIRAMacroParameters;

/**
 * Displays JIRA issues in a table.
 *
 * @version $Id$
 * @since 4.2M1
 */
@Component
@Named("table")
@Singleton
public class TableJIRADisplayer extends AbstractJIRADisplayer
{
    /**
     * Default list of JIRA fields to display.
     */
    private static final List<String> FIELDS =
        Arrays.asList(JIRAFields.TYPE, JIRAFields.KEY, JIRAFields.SUMMARY, JIRAFields.STATUS, JIRAFields.CREATED);

    @Override
    public List<Block> display(Collection<Element> issues, JIRAMacroParameters parameters)
    {
        List<Block> rowBlocks = new ArrayList<Block>();

        Map<String, String> fieldNames = getFieldNames(parameters);

        // Create the table headers for the specified fields
        List<Block> headerCellBlocks = new ArrayList<Block>();
        for (String field : getFields(parameters)) {
            headerCellBlocks.add(new TableHeadCellBlock(Arrays.<Block>asList(
                new VerbatimBlock(getFieldName(field, fieldNames), true))));
        }
        rowBlocks.add(new TableRowBlock(headerCellBlocks));

        // Construct the data rows, one row per issue
        for (Element issue : issues) {
            List<Block> dataCellBlocks = new ArrayList<Block>();
            for (String field : getFields(parameters)) {
                // Use the displayer for the field
                dataCellBlocks.add(new TableCellBlock(getFieldDisplayer(field).displayField(field, issue)));
            }
            rowBlocks.add(new TableRowBlock(dataCellBlocks));
        }

        return Arrays.<Block>asList(new TableBlock(rowBlocks));
    }

    @Override
    protected List<String> getDefaultFields()
    {
        return FIELDS;
    }

    /**
     * @param field the field id for which to find the name to display
     * @param fieldNames the map of all field names
     * @return the field name to display as table header for the passed field
     */
    private String getFieldName(String field, Map<String, String> fieldNames)
    {
        String result = fieldNames.get(field);
        if (result == null) {
            result = field;
        }
        return result;
    }

    /**
     * @param parameters the macro parameters containing optional field names defined by the user
     * @return the map of all field names combining default field names and field names defined by the user in the macro
     *         parameters
     */
    private Map<String, String> getFieldNames(JIRAMacroParameters parameters)
    {
        Map<String, String> fieldNames = new HashMap<String, String>();
        fieldNames.putAll(parameters.getDefaultFieldNames());

        if (parameters.getFieldNames() != null) {
            List<String> fields = getFields(parameters);
            for (int i = 0; i < fields.size(); i++) {
                fieldNames.put(fields.get(i), parameters.getFieldNames().get(i));
            }
        }

        return fieldNames;
    }
}
