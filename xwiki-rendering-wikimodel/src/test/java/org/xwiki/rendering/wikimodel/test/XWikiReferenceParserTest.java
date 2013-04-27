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
package org.xwiki.rendering.wikimodel.test;

import org.xwiki.rendering.wikimodel.WikiParameters;
import org.xwiki.rendering.wikimodel.WikiReference;
import org.xwiki.rendering.wikimodel.xwiki.xwiki20.XWikiReferenceParser;

import org.junit.Assert;
import junit.framework.TestCase;

/**
 * @version $Id$
 * @since 4.0M1
 */
public class XWikiReferenceParserTest extends TestCase
{
    public void testParseReferenceWhenReferenceOnly()
    {
        XWikiReferenceParser parser = new XWikiReferenceParser();
        WikiReference reference = parser.parse("reference");
        assertNull(reference.getLabel());
        Assert.assertEquals(WikiParameters.EMPTY, reference.getParameters());
        assertEquals("reference", reference.getLink());
    }

    public void testParseReferenceWhenLabelSpecified()
    {
        XWikiReferenceParser parser = new XWikiReferenceParser();
        WikiReference reference = parser.parse("label>>reference");
        assertEquals("label", reference.getLabel());
        assertEquals(WikiParameters.EMPTY, reference.getParameters());
        assertEquals("reference", reference.getLink());
    }

    public void testParseReferenceWhenParametersSpecified()
    {

        XWikiReferenceParser parser = new XWikiReferenceParser();
        WikiReference reference = parser
            .parse("reference||param1=value1 param2=value2");
        assertNull(reference.getLabel());
        assertEquals(2, reference.getParameters().getSize());
        assertEquals("value1", reference
            .getParameters()
            .getParameter("param1")
            .getValue());
        assertEquals("value2", reference
            .getParameters()
            .getParameter("param2")
            .getValue());
        assertEquals("reference", reference.getLink());
    }

    public void testParseReferenceWhenLabelAndParametersSpecified()
    {
        XWikiReferenceParser parser = new XWikiReferenceParser();
        WikiReference reference = parser.parse("label>>reference||param=value");
        assertEquals("label", reference.getLabel());
        assertEquals("value", reference
            .getParameters()
            .getParameter("param")
            .getValue());
        assertEquals("reference", reference.getLink());
    }

    public void testParseReferenceWithGreaterThanSymbolInLabel()
    {
        XWikiReferenceParser parser = new XWikiReferenceParser();
        WikiReference reference = parser
            .parse("<strong>hello</strong>>>reference");
        assertEquals("<strong>hello</strong>", reference.getLabel());
        assertEquals("reference", reference.getLink());
    }

    public void testParseReferenceWithPipeSymbolInLink()
    {
        XWikiReferenceParser parser = new XWikiReferenceParser();
        WikiReference reference = parser.parse("reference|||param=value");
        assertEquals("reference|", reference.getLink());
        assertEquals("value", reference
            .getParameters()
            .getParameter("param")
            .getValue());
    }

    public void testParseReferenceWhenLabelAndParametersSpecifiedWithSomeEscaping()
    {
        XWikiReferenceParser parser = new XWikiReferenceParser();
        WikiReference reference = parser
            .parse("la~>>bel>>refe~||rence||param=value");
        assertEquals("la>>bel", reference.getLabel());
        assertEquals("value", reference
            .getParameters()
            .getParameter("param")
            .getValue());
        assertEquals("refe||rence", reference.getLink());
    }

    public void testParseReferenceWhenLabelAndParametersSpecifiedWithSomeEscapingAndInternalLink()
    {
        XWikiReferenceParser parser = new XWikiReferenceParser();
        WikiReference reference = parser
            .parse("[[sub~>>label>>subre~||ference||subparam=subvalue]]la~>>bel>>refe~||rence||param=value");
        assertEquals(
            "[[sub~>>label>>subre~||ference||subparam=subvalue]]la>>bel",
            reference.getLabel());
        assertEquals("value", reference
            .getParameters()
            .getParameter("param")
            .getValue());
        assertEquals("refe||rence", reference.getLink());
    }
}
