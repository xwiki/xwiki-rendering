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

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.xwiki.rendering.listener.reference.DocumentResourceReference;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;

/**
 * Unit tests for Block manipulation, testing {@link AbstractBlock}.
 * 
 * @version $Id$
 * @since 4.1M1
 */
public class BlockTest
{
    @Test
    public void testGetPreviousBlockByType()
    {
        WordBlock lw = new WordBlock("linkword");
        SpecialSymbolBlock ls = new SpecialSymbolBlock('$');

        DocumentResourceReference linkReference = new DocumentResourceReference("reference");
        LinkBlock pl = new LinkBlock(Arrays.<Block> asList(lw, ls), linkReference, false);

        ImageBlock pi = new ImageBlock(new ResourceReference("document@attachment", ResourceType.ATTACHMENT), true);

        ParagraphBlock rootBlock = new ParagraphBlock(Arrays.<Block> asList(pi, pl));

        Assert.assertSame(lw, ls.getPreviousBlockByType(WordBlock.class, false));
        Assert.assertNull(ls.getPreviousBlockByType(ImageBlock.class, false));
        Assert.assertSame(pl, ls.getPreviousBlockByType(LinkBlock.class, true));
        Assert.assertSame(pi, ls.getPreviousBlockByType(ImageBlock.class, true));
        Assert.assertSame(rootBlock, ls.getPreviousBlockByType(ParagraphBlock.class, true));
    }
}
