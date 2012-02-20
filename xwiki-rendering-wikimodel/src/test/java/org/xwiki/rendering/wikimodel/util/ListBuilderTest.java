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
package org.xwiki.rendering.wikimodel.util;

import junit.framework.TestCase;

/**
 * @version $Id$
 * @since 4.0M1
 */
public class ListBuilderTest extends TestCase
{
    /**
     * @param name
     */
    public ListBuilderTest(String name)
    {
        super(name);
    }

    public void testTwo() throws Exception
    {
        testTwo("a", "<A><a></a></A>");
        testTwo("a\na", "<A><a></a><a></a></A>");
        testTwo("a\n a", "<A><a><A><a></a></A></a></A>");
        testTwo("a\n a\n a", "<A><a><A><a></a><a></a></A></a></A>");
        testTwo("a\n a\n a\n a", "<A><a>"
            + "<A>"
            + "<a></a>"
            + "<a></a>"
            + "<a></a>"
            + "</A>"
            + "</a></A>");
        testTwo("a\n b\n b\n b", "<A><a>"
            + "<B>"
            + "<b></b>"
            + "<b></b>"
            + "<b></b>"
            + "</B>"
            + "</a></A>");
        testTwo("a\n b\n b\n b\na", "<A>"
            + "<a>"
            + "<B>"
            + "<b></b>"
            + "<b></b>"
            + "<b></b>"
            + "</B>"
            + "</a>"
            + "<a></a>"
            + "</A>");
        testTwo("a\nab\nabc\nabcd", "<A><a>"
            + "<B><b>"
            + "<C><c>"
            + "<D><d></d></D>"
            + "</c></C>"
            + "</b></B>"
            + "</a></A>");
        testTwo("a\nab\nabc", "<A><a>"
            + "<B><b>"
            + "<C><c></c></C>"
            + "</b></B>"
            + "</a></A>");
        testTwo(""
            + "            a\n"
            + "       a   b\n"
            + "  a c\n"
            + " a c  d\n"
            + " e"
            + ""
            + ""
            + ""
            + ""
            + "", "<A><a>"
            + "<B><b></b></B>"
            + "<C><c>"
            + "<D><d></d></D>"
            + "</c></C>"
            + "</a></A>"
            + "<E><e></e></E>");

        testTwo("a\n b", "<A><a><B><b></b></B></a></A>");
        testTwo("a\nab", "<A><a><B><b></b></B></a></A>");
        testTwo("a\n         ", "<A><a></a></A>");
        testTwo(" ", "");
        testTwo("", "");
        testTwo("              ", "");
        testTwo("         a", "<A><a></a></A>");
        testTwo("     a     \n         ", "<A><a></a></A>");
        testTwo(""
            + " a\n"
            + "  b\n"
            + "  c\n"
            + "   d\n"
            + " e"
            + ""
            + ""
            + ""
            + ""
            + "", "<A><a>"
            + "<B><b></b></B>"
            + "<C><c>"
            + "<D><d></d></D>"
            + "</c></C>"
            + "</a></A>"
            + "<E><e></e></E>");
        testTwo(""
            + "a\n"
            + "ab\n"
            + "ac\n"
            + "acd\n"
            + "e"
            + ""
            + ""
            + ""
            + ""
            + "", "<A><a>"
            + "<B><b></b></B>"
            + "<C><c>"
            + "<D><d></d></D>"
            + "</c></C>"
            + "</a></A>"
            + "<E><e></e></E>");
        testTwo(""
            + "            a\n"
            + "       a   b\n"
            + "  a c\n"
            + " a c  d\n"
            + " e"
            + ""
            + ""
            + ""
            + ""
            + "", "<A><a>"
            + "<B><b></b></B>"
            + "<C><c>"
            + "<D><d></d></D>"
            + "</c></C>"
            + "</a></A>"
            + "<E><e></e></E>");

        testTwo("" + "    a\n" + "  b\n" + " c\n" + "cd\n" + "", ""
            + "<A><a></a></A>"
            + "<B><b></b></B>"
            + "<C><c>"
            + "<D><d></d></D>"
            + "</c></C>");
    }

    private void testTwo(String string, String control)
    {
        final StringBuffer buf = new StringBuffer();
        IListListener listener = new IListListener()
        {
            public void beginRow(char treeType, char rowType)
            {
                openTag(rowType);
            }

            public void beginTree(char type)
            {
                openTag(Character.toUpperCase(type));
            }

            private void closeTag(char ch)
            {
                buf.append("</").append(ch).append(">");
            }

            public void endRow(char treeType, char rowType)
            {
                closeTag(rowType);
            }

            public void endTree(char type)
            {
                closeTag(Character.toUpperCase(type));
            }

            private void openTag(char str)
            {
                buf.append("<").append(str).append(">");
            }
        };
        ListBuilder builder = new ListBuilder(listener);
        String[] lines = string.split("\n");
        for (String s : lines) {
            builder.alignContext(s);
        }
        builder.alignContext("");
        // builder.finish();
        assertEquals(control, buf.toString());
    }
}
