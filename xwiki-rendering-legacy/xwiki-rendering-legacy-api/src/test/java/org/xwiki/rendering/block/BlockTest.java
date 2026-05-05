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

import java.util.List;

import org.junit.jupiter.api.Test;
import org.xwiki.rendering.listener.HeaderLevel;
import org.xwiki.rendering.listener.reference.DocumentResourceReference;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Unit tests for Block manipulation, testing {@link AbstractBlock}.
 *
 * @version $Id$
 * @since 4.1M1
 */
class BlockTest
{
    @Test
    void getPreviousBlockByType()
    {
        WordBlock lw = new WordBlock("linkword");
        SpecialSymbolBlock ls = new SpecialSymbolBlock('$');

        DocumentResourceReference linkReference = new DocumentResourceReference("reference");
        LinkBlock pl = new LinkBlock(List.of(lw, ls), linkReference, false);

        ImageBlock pi = new ImageBlock(new ResourceReference("document@attachment", ResourceType.ATTACHMENT), true);

        ParagraphBlock rootBlock = new ParagraphBlock(List.of(pi, pl));

        assertSame(lw, ls.getPreviousBlockByType(WordBlock.class, false));
        assertNull(ls.getPreviousBlockByType(ImageBlock.class, false));
        assertSame(pl, ls.getPreviousBlockByType(LinkBlock.class, true));
        assertSame(pi, ls.getPreviousBlockByType(ImageBlock.class, true));
        assertSame(rootBlock, ls.getPreviousBlockByType(ParagraphBlock.class, true));
    }

    @Test
    void getChildrenByType()
    {
        ParagraphBlock pb1 =
            new ParagraphBlock(List.of(new HeaderBlock(List.of(new WordBlock("title1")),
                    HeaderLevel.LEVEL1)));
        ParagraphBlock pb2 =
            new ParagraphBlock(List.of(new HeaderBlock(List.of(new WordBlock("title2")),
                    HeaderLevel.LEVEL2)));
        ParagraphBlock pb3 =
            new ParagraphBlock(List.of(pb1, pb2, new HeaderBlock(List.of(),
                    HeaderLevel.LEVEL1)));

        List<HeaderBlock> results = pb1.getChildrenByType(HeaderBlock.class, true);
        assertEquals(1, results.size());

        results = pb1.getChildrenByType(HeaderBlock.class, false);
        assertEquals(1, results.size());

        results = pb3.getChildrenByType(HeaderBlock.class, true);
        assertEquals(3, results.size());

        results = pb3.getChildrenByType(HeaderBlock.class, false);
        assertEquals(1, results.size());
    }
}
