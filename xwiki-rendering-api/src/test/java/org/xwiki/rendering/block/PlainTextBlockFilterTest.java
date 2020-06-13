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
package org.xwiki.rendering.block;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.rendering.listener.HeaderLevel;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.syntax.Syntax;

/**
 * Unit tests for plain text extraction, testing {@link PlainTextBlockFilter}.
 *
 * @version $Id$
 */
public class PlainTextBlockFilterTest
{
    private PlainTextBlockFilter filter;

    @BeforeEach
    public void setUp()
    {
        Parser plainTextParser = new Parser()
        {
            /**
             * simplistic implementation that returns an empty XDOM for empty input
             * or an XDOM with a single WordBlock otherwise
             * 
             * @throws ParseException not really
             */
            @Override
            public XDOM parse(Reader source) throws ParseException
            {
                String content;
                try {
                    content = IOUtils.toString(source);
                } catch (IOException ioe) {
                    throw new ParseException("this should not happen", ioe);
                }
                List<Block> childrenBlocks = new ArrayList<Block>();
                if (!"".equals(content)) {
                    childrenBlocks.add(new ParagraphBlock(Collections.singletonList(new WordBlock(content))));
                }
                XDOM xdom = new XDOM(childrenBlocks);
                return xdom;
            }

            @Override
            public Syntax getSyntax()
            {
                // unused when testing
                throw new UnsupportedOperationException("getSyntax");
            }
        };

        filter = new PlainTextBlockFilter(plainTextParser, null);
    }

    @Test
    public void filterWord()
    {
        WordBlock wordBlock = new WordBlock("text");

        List<Block> result = filter.filter(wordBlock);

        assertIterableEquals(Collections.singletonList(wordBlock), result);
    }

    @Test
    public void filterHeader()
    {
        List<Block> headerContent = new ArrayList<Block>();
        headerContent.add(new WordBlock("header"));
        HeaderBlock headerBlock = new HeaderBlock(headerContent, HeaderLevel.LEVEL1);

        List<Block> result = filter.filter(headerBlock);

        assertIterableEquals(Collections.emptyList(), result);
    }

    @Test
    public void filterLinkWithLabel()
    {
        Block labelBlock = new WordBlock("label");
        ResourceReference reference = new ResourceReference("file name.txt", ResourceType.ATTACHMENT);
        LinkBlock labeledLink = new LinkBlock(Collections.singletonList(labelBlock), reference, false);

        List<Block> result = filter.filter(labeledLink);
        // in this case the result is empty, as recursive application of the filter to all blocks
        // will return the label block later
        assertIterableEquals(Collections.emptyList(), result);
    }

    @Test
    public void filterLinkWithoutLabel()
    {
        ResourceReference reference = new ResourceReference("file name.txt", ResourceType.ATTACHMENT);
        LinkBlock emptyLink = new LinkBlock(Collections.emptyList(), reference, false);

        List<Block> result = filter.filter(emptyLink);
        assertIterableEquals(Collections.singletonList(new WordBlock("file name.txt")), result);
    }

    @Test
    public void filterEmptyLink()
    {
        ResourceReference reference = new ResourceReference("", ResourceType.ATTACHMENT);
        LinkBlock emptyLink = new LinkBlock(Collections.emptyList(), reference, false);

        List<Block> result = filter.filter(emptyLink);
        // here the result is empty due to lack of contents
        assertIterableEquals(Collections.emptyList(), result);
    }

    @Test
    public void simpleTextHeader()
    {
        List<Block> headerContent = new ArrayList<Block>();
        headerContent.add(new WordBlock("some"));
        headerContent.add(new SpaceBlock());
        headerContent.add(new WordBlock("text"));
        HeaderBlock headerBlock = new HeaderBlock(headerContent, HeaderLevel.LEVEL1);

        List<Block> result = headerBlock.clone(filter).getChildren();

        assertIterableEquals(headerContent, result);
    }

}
