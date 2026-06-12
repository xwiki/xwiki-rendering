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

import java.util.Map;

import jakarta.inject.Named;
import jakarta.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.listener.Listener;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Quote block parser.
 *
 * @version $Id$
 * @since 18.5.0RC1
 */
@Component
@Named(QuoteBlockParser.QUOTE)
@Singleton
public class QuoteBlockParser extends AbstractListBlockParser
{
    /**
     * This component's role hint. Also the type of blocks handled by this parser.
     */
    public static final String QUOTE = "quote";

    @Override
    protected void beginList(Listener listener, Map<String, String> parameters)
    {
        listener.beginQuotation(parameters);
    }

    @Override
    protected void beginListItem(ObjectNode listItemBlock, Listener listener)
    {
        listener.beginQuotationLine();
    }

    @Override
    protected void endListItem(ObjectNode listItemBlock, Listener listener)
    {
        listener.endQuotationLine();
    }

    @Override
    protected void endList(Listener listener, Map<String, String> parameters)
    {
        listener.endQuotation(parameters);
    }
}
