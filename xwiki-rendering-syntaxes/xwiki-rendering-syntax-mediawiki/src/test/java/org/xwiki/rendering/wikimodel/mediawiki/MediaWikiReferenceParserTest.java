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
package org.xwiki.rendering.wikimodel.mediawiki;

import org.xwiki.rendering.wikimodel.WikiParameters;
import org.xwiki.rendering.wikimodel.WikiReference;

import junit.framework.TestCase;

/**
 * @version $Id$
 * @since 4.0M1
 */
public class MediaWikiReferenceParserTest extends TestCase
{
    private MediaWikiReferenceParser clazz;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        clazz = new MediaWikiReferenceParser();
    }

    public void testParseSimpleLinks()
    {
        WikiReference actual;

        actual = clazz.parse("Image:foo.png");
        assertEquals("foo.png", actual.getLink());

        actual = clazz.parse("image:foo.png");
        assertEquals("foo.png", actual.getLink());

        actual = clazz.parse("File:bar.png");
        assertEquals("bar.png", actual.getLink());

        actual = clazz.parse("file:bar.png");
        assertEquals("bar.png", actual.getLink());
    }

    public void testParseImageParams()
    {
        WikiReference actual;
        actual = clazz.parse("Image:image.png|thumb|250px|center|Image Caption");
        assertEquals("image.png", actual.getLink());
        assertEquals("Image Caption", actual.getLabel());

        final WikiParameters parameters = actual.getParameters();

        assertNotNull(parameters.getParameter("width"));
        assertEquals("250px", parameters.getParameter("width").getValue());

        assertNotNull(parameters.getParameter("alt"));
        assertEquals("Image Caption", parameters.getParameter("alt").getValue());

        assertNotNull(parameters.getParameter("align"));
        assertEquals("center", parameters.getParameter("align").getValue());

        assertNotNull(parameters.getParameter("format"));
        assertEquals("thumb", parameters.getParameter("format").getValue());
    }

    public void testParseExternalLinks()
    {
        WikiReference actual;

        actual = clazz.parse("http://code.google.com/p/wikimodel/");
        assertEquals("http://code.google.com/p/wikimodel/", actual.getLink());

        actual = clazz.parse("http://code.google.com/p/wikimodel/ Official site");
        assertEquals("http://code.google.com/p/wikimodel/", actual.getLink());
        assertEquals("Official site", actual.getLabel());
    }

    public void testParseInternalLinks()
    {
        WikiReference actual;

        actual = clazz.parse("Main Page");
        assertEquals("Main Page", actual.getLink());

        actual = clazz.parse("Main Page|Different Text");
        assertEquals("Main Page", actual.getLink());
        assertEquals("Different Text", actual.getLabel());

        actual = clazz.parse("Main Page|");
        assertEquals("Main Page", actual.getLink());
        assertEquals("", actual.getLabel());
    }

    /**
     * @see http://code.google.com/p/wikimodel/issues/attachmentText?id=184
     */
    public void testNamedParameters()
    {
        WikiReference actual;

        actual = clazz.parse("File:example.jpg|link=Main Page|caption");
        assertEquals("example.jpg", actual.getLink());
        assertEquals("Main Page", actual.getParameters().getParameter("link").getValue());

        actual = clazz.parse("File:example.jpg|link=|caption");
        assertEquals("example.jpg", actual.getLink());
        assertEquals("", actual.getParameters().getParameter("link").getValue());
    }
}
