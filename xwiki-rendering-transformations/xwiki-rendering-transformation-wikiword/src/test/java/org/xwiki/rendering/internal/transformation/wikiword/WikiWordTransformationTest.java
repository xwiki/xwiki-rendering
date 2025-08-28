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
package org.xwiki.rendering.internal.transformation.wikiword;

import java.io.StringReader;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.jupiter.api.Test;
import org.xwiki.rendering.block.MacroMarkerBlock;
import org.xwiki.rendering.block.WordBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.renderer.BlockRenderer;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.rendering.transformation.Transformation;
import org.xwiki.rendering.transformation.TransformationContext;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.junit5.mockito.ComponentTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link org.xwiki.rendering.internal.transformation.wikiword.WikiWordTransformation}.
 *
 * @version $Id$
 * @since 2.6RC1
 */
@ComponentTest
@AllComponents
class WikiWordTransformationTest
{
    @Inject
    @Named("wikiword")
    private Transformation wikiWordTransformation;

    @Inject
    @Named("xwiki/2.1")
    private Parser parser;

    @Inject
    @Named("xwiki/2.1")
    private BlockRenderer xwikiBlockRenderer;

    @Inject
    @Named("event/1.0")
    private BlockRenderer eventBlockRenderer;

    @Test
    void wikiWordTransformation() throws Exception
    {
        // Tests the following at once:
        // - that a wiki word is recognized
        // - that several wiki words in a row are recognized
        // - that wiki words with non ASCII chars are recognized (accented chars)
        // - that two uppercase letters following each other (as in "XWiki") are not considered a wiki word
        // - that several uppercases chars followed by lowercases and then one uppercase and lowercase chars is
        //   recognized as a wiki word (eg "XWikiEnterprise")
        String testInput = "This is a WikiWord, AnotherÙne, XWikiEnterprise, not one: XWiki";

        XDOM xdom = this.parser.parse(new StringReader(testInput));
        this.wikiWordTransformation.transform(xdom, new TransformationContext());
        WikiPrinter printer = new DefaultWikiPrinter();
        this.xwikiBlockRenderer.render(xdom, printer);
        assertEquals("This is a [[doc:WikiWord]], [[doc:AnotherÙne]], [[doc:XWikiEnterprise]], not one: XWiki",
            printer.toString());
    }

    @Test
    void wikiWordTransformationIgnoresProtectedContent() throws Exception
    {
        String expected = """
            beginDocument
            beginMacroMarkerStandalone [code] []
            onWord [WikiWord]
            endMacroMarkerStandalone [code] []
            endDocument""";

        XDOM xdom = new XDOM(List.of(new MacroMarkerBlock("code", Collections.emptyMap(),
            List.of(new WordBlock("WikiWord")), false)));
        this.wikiWordTransformation.transform(xdom, new TransformationContext());

        WikiPrinter printer = new DefaultWikiPrinter();
        this.eventBlockRenderer.render(xdom, printer);
        assertEquals(expected, printer.toString());
    }
}
