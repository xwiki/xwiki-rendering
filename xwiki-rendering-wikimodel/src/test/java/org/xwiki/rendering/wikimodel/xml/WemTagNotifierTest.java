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
package org.xwiki.rendering.wikimodel.xml;

import java.io.StringReader;
import java.io.StringWriter;

import org.xwiki.rendering.wikimodel.IWikiParser;
import org.xwiki.rendering.wikimodel.common.CommonWikiParser;
import org.xwiki.rendering.wikimodel.test.AbstractWikiParserTest;
import org.xwiki.rendering.wikimodel.xml.sax.WemReader;

/**
 * @version $Id$
 * @since 4.0M1
 */
public class WemTagNotifierTest extends AbstractWikiParserTest
{
    /**
     * @param name
     */
    public WemTagNotifierTest(String name)
    {
        super(name);
    }

    @Override
    protected IWikiParser newWikiParser()
    {
        return new CommonWikiParser();
    }

    public void test() throws Exception
    {
        showSections(true);
        String str = "{{a=b c=d}}\n=Header=\n* item __one__\n* item two";
        StringReader reader = new StringReader(str);

        IWikiParser parser = newWikiParser();
        WemReader xmlReader = new WemReader(parser);
        StringWriter writer = new StringWriter();
        XmlUtil.write(reader, xmlReader, writer);
        String control =
            "<w:document xmlns:w=\"http://www.wikimodel.org/ns/wem#\" xmlns:u=\"http://www.wikimodel.org/ns/user-defined-params#\">\n"
                + "    <w:section w:absLevel=\"1\" w:docLevel=\"1\" w:headerLevel=\"0\">\n"
                + "        <w:content w:absLevel=\"1\" w:docLevel=\"1\" w:headerLevel=\"0\">\n"
                + "            <w:section w:absLevel=\"2\" w:docLevel=\"1\" w:headerLevel=\"1\" u:a=\"b\" u:c=\"d\">\n"
                + "                <w:header w:level=\"1\" u:a=\"b\" u:c=\"d\">\n"
                + "                    <w:format>Header</w:format>\n"
                + "                </w:header>\n"
                +
                "                <w:content w:absLevel=\"2\" w:docLevel=\"1\" w:headerLevel=\"1\" u:a=\"b\" u:c=\"d\">\n"
                + "                    <w:ul>\n"
                + "                        <w:li>\n"
                + "                            <w:format>item </w:format>\n"
                + "                            <w:format w:styles=\"em\">one</w:format>\n"
                + "                        </w:li>\n"
                + "                        <w:li>\n"
                + "                            <w:format>item two</w:format>\n"
                + "                        </w:li>\n"
                + "                    </w:ul>\n"
                + "                </w:content>\n"
                + "            </w:section>\n"
                + "        </w:content>\n"
                + "    </w:section>\n"
                + "</w:document>\n"
                + "";
        String result = writer.toString();
        assertEquals(control, result);
        System.out.println(writer);
    }
}