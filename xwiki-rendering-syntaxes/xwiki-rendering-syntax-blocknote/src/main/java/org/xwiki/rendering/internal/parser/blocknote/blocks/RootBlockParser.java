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
package org.xwiki.rendering.internal.parser.blocknote.blocks;

import java.util.Deque;

import jakarta.inject.Named;
import jakarta.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.internal.blocknote.BlockNote10SyntaxProvider;
import org.xwiki.rendering.internal.parser.blocknote.Context;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.parser.ParseException;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Parser for the root BlockNote block, which represents the entire document.
 *
 * @version $Id$
 * @since 18.5.0RC1
 */
@Component
@Named("root")
@Singleton
public class RootBlockParser extends AbstractBlockParser
{
    /**
     * This component's role hint. Also the type of blocks handled by this parser.
     */
    public static final String ROOT = "root";

    @Override
    public void parse(ObjectNode rootBlock, Deque<Context> contextStack) throws ParseException
    {
        MetaData metadata = new MetaData();
        metadata.addMetaData(MetaData.SYNTAX, BlockNote10SyntaxProvider.BLOCKNOTE_1_0);
        Context context = contextStack.peek();
        context.listener().beginDocument(metadata);

        visitChildBlocks(rootBlock, CHILDREN, contextStack);

        context.listener().endDocument(metadata);
    }
}
