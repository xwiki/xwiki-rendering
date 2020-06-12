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

import java.io.StringReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.renderer.reference.link.LinkLabelGenerator;

/**
 * Used to filter plain text blocks.
 *
 * @version $Id$
 * @since 1.9M1
 */
public class PlainTextBlockFilter implements BlockFilter
{
    /**
     * The set of valid Block classes as plain text content.
     */
    private static final Set<Class<? extends Block>> VALID_PLAINTEXT_BLOCKS = new HashSet<Class<? extends Block>>();

    static {
        VALID_PLAINTEXT_BLOCKS.add(WordBlock.class);
        VALID_PLAINTEXT_BLOCKS.add(SpaceBlock.class);
        VALID_PLAINTEXT_BLOCKS.add(SpecialSymbolBlock.class);
        VALID_PLAINTEXT_BLOCKS.add(NewLineBlock.class);
    }

    /**
     * A parser that knows how to parse plain text; this is used to transform link labels into plain text.
     */
    private Parser plainTextParser;

    /**
     * Generate link label.
     */
    private LinkLabelGenerator linkLabelGenerator;

    /**
     * @param plainTextParser a plain text parser used to transform link labels into plain text
     * @param linkLabelGenerator generate link label.
     * @since 2.0M3
     */
    public PlainTextBlockFilter(Parser plainTextParser, LinkLabelGenerator linkLabelGenerator)
    {
        this.plainTextParser = plainTextParser;
        this.linkLabelGenerator = linkLabelGenerator;
    }

    @Override
    public List<Block> filter(Block block)
    {
        if (VALID_PLAINTEXT_BLOCKS.contains(block.getClass())) {
            return Collections.singletonList(block);
        }

        if (LinkBlock.class.isAssignableFrom(block.getClass()) && block.getChildren().isEmpty()) {
            ResourceReference reference = ((LinkBlock) block).getReference();
            return filterLinkBlock(reference);
        }

        return Collections.emptyList();
    }

    private List<Block> filterLinkBlock(ResourceReference reference)
    {
        try {
            String label;
            ResourceType resourceType = reference.getType();

            if (ResourceType.DOCUMENT.equals(resourceType) || ResourceType.SPACE.equals(resourceType)
                || ResourceType.PAGE.equals(resourceType)) {
                label = this.linkLabelGenerator.generate(reference);
            } else {
                label = reference.getReference();
            }

            List<Block> labelBlocks = this.plainTextParser.parse(new StringReader(label)).getChildren();
            return labelBlocks.isEmpty() ? labelBlocks : labelBlocks.get(0).getChildren();
        } catch (ParseException e) {
            // This shouldn't happen since the parser cannot throw an exception since the source is a memory
            // String.
            throw new RuntimeException("Failed to parse link label as plain text", e);
        }
    }
}
