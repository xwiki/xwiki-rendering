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
package org.xwiki.rendering.macro.toc;

import java.util.List;

import org.xwiki.component.annotation.Role;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.HeaderBlock;
import org.xwiki.stability.Unstable;

/**
 * Table of content entry extension, allowing to intercept and improve the default rendering of the table of content
 * entries.
 * <p>
 * For instance, this component can be used by the numbered content application to prefix each entry with an
 * auto-generated number, matching the number used as a prefix for the corresponding header.
 *
 * @version $Id$
 * @since 15.8RC1
 */
@Role
@Unstable
public interface TocEntryExtension
{
    /**
     * Allow to decorate a table of content entry.
     * <p>
     * For instance, the numbered content extension can decorate the table of content entries by prefixing each entry
     * with an automatically generated number.
     * <p>
     * The {@code decorate} methods from all {@link TocEntryExtension} components are expected to be called one after
     * the other on each table of content entry. The call order is the same for all entries but is not guaranteed.
     *
     * @param headerBlock the header block to render as an entry
     * @param blocks the blocks
     * @param rootBlock the root block containing the header
     * @param tocEntriesResolver a table of content resolver, allowing to help resolving content surrounding the
     *     entry
     * @return a new list of blocks to user to display the entry
     */
    List<Block> decorate(HeaderBlock headerBlock, List<Block> blocks, Block rootBlock,
        TocEntriesResolver tocEntriesResolver);
}
