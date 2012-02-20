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

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xwiki.rendering.wikimodel.IWikiParser;
import org.xwiki.rendering.wikimodel.confluence.ConfluenceExtendedWikiParser;
import org.xwiki.rendering.wikimodel.xml.sax.WemReader;

import junit.framework.TestCase;

/**
 * @version $Id$
 * @since 4.0M1
 */
public class WemToSaxTest extends TestCase
{
    /**
     * @param name
     */
    public WemToSaxTest(String name)
    {
        super(name);
    }

    protected IWikiParser newWikiParser()
    {
        return new ConfluenceExtendedWikiParser();
        // return new ConfluenceWikiParser();
        // return new CommonWikiParser();
    }

    public Document parseDocument(Reader reader, IWikiParser parser)
        throws Exception
    {
        try {
            Document document = XmlUtil.newDocument();
            DOMResult result = new DOMResult(document);
            WemReader xmlReader = new WemReader(parser);
            XmlUtil.write(reader, xmlReader, result);
            return document;
        } finally {
            reader.close();
        }
    }

    public Document parseDocument(String str, IWikiParser parser)
        throws Exception
    {
        StringReader reader = new StringReader(str);
        return parseDocument(reader, parser);
    }

    public void test() throws Exception
    {
        test("abc", "<w:p><w:format>abc</w:format></w:p>");
        test(
            "h1.abc",
            ""
                + "<w:section w:absLevel=\"2\" w:docLevel=\"1\" w:headerLevel=\"1\">"
                + "<w:header w:level=\"1\">"
                + "<w:format>abc</w:format>"
                + "</w:header>"
                + "<w:content w:absLevel=\"2\" w:docLevel=\"1\" w:headerLevel=\"1\"/>"
                + "</w:section>");
        test(
            "h1.abc\npara",
            ""
                + "<w:section w:absLevel=\"2\" w:docLevel=\"1\" w:headerLevel=\"1\">"
                + "<w:header w:level=\"1\">"
                + "<w:format>abc</w:format>"
                + "</w:header>"
                + "<w:content w:absLevel=\"2\" w:docLevel=\"1\" w:headerLevel=\"1\">"
                + "<w:p><w:format>para</w:format></w:p>"
                + "</w:content>"
                + "</w:section>");
        test("* item one", ""
            + "<w:ul>"
            + "<w:li><w:format>item one</w:format></w:li>"
            + "</w:ul>");
        test("* item one\n* item two", ""
            + "<w:ul>"
            + "<w:li><w:format>item one</w:format></w:li>"
            + "<w:li><w:format>item two</w:format></w:li>"
            + "</w:ul>");
    }

    private void test(String wiki, String xml) throws Exception
    {
        IWikiParser parser = newWikiParser();
        Document doc = parseDocument(wiki, parser);
        StringWriter writer = new StringWriter();
        Source input = new DOMSource(doc.getDocumentElement());
        Result output = new StreamResult(writer);
        XmlUtil.write(input, output, false);
        String result = writer.toString();
        String fullXml = ""
            +
            "<w:document xmlns:w=\"http://www.wikimodel.org/ns/wem#\" xmlns:u=\"http://www.wikimodel.org/ns/user-defined-params#\">"
            + "<w:section w:absLevel=\"1\" w:docLevel=\"1\" w:headerLevel=\"0\">"
            + "<w:content w:absLevel=\"1\" w:docLevel=\"1\" w:headerLevel=\"0\">"
            + xml
            + "</w:content>"
            + "</w:section>"
            + "</w:document>";
        assertEquals(fullXml, result);
    }
}
