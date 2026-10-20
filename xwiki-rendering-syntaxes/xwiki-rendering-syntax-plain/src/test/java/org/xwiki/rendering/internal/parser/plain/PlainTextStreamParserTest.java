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
package org.xwiki.rendering.internal.parser.plain;

import java.io.FilterReader;
import java.io.Reader;
import java.io.StringReader;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.listener.MetaData;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link PlainTextStreamParser}.
 *
 * @version $Id$
 */
class PlainTextStreamParserTest
{
    private static final String TEXT = "a b\n!";

    private final PlainTextStreamParser parser = new PlainTextStreamParser();

    @Test
    void parseStringReaderWithoutBuffer() throws Exception
    {
        StringReader source = spy(new StringReader(TEXT));
        Listener listener = mock();

        this.parser.parse(source, listener);

        // A buffer would read the source by blocks.
        verify(source, never()).read(any(char[].class), anyInt(), anyInt());
        verifyEvents(listener);
    }

    @Test
    void parseUnbufferedReader() throws Exception
    {
        Reader source = new FilterReader(new StringReader(TEXT))
        {
        };
        Listener listener = mock();

        this.parser.parse(source, listener);

        verifyEvents(listener);
    }

    private void verifyEvents(Listener listener)
    {
        InOrder inOrder = inOrder(listener);
        inOrder.verify(listener).beginDocument(MetaData.EMPTY);
        inOrder.verify(listener).beginParagraph(Listener.EMPTY_PARAMETERS);
        inOrder.verify(listener).onWord("a");
        inOrder.verify(listener).onSpace();
        inOrder.verify(listener).onWord("b");
        inOrder.verify(listener).onNewLine();
        inOrder.verify(listener).onSpecialSymbol('!');
        inOrder.verify(listener).endParagraph(Listener.EMPTY_PARAMETERS);
        inOrder.verify(listener).endDocument(MetaData.EMPTY);
        inOrder.verifyNoMoreInteractions();
    }
}
