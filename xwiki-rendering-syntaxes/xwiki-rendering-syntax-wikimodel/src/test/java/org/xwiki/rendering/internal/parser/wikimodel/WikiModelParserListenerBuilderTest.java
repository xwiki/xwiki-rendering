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
package org.xwiki.rendering.internal.parser.wikimodel;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.xwiki.rendering.internal.listener.ListenerRegistry;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.listener.chaining.ChainingListener;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.syntax.SyntaxRegistry;
import org.xwiki.test.LogLevel;
import org.xwiki.test.junit5.LogCaptureExtension;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import ch.qos.logback.classic.Level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.xwiki.rendering.listener.ListenerProvider.PARSE_ACTION;
import static org.xwiki.rendering.syntax.Syntax.XWIKI_2_1;

/**
 * Test of {@link WikiModelParserListenerBuilder}.
 *
 * @version $Id$
 */
@ComponentTest
class WikiModelParserListenerBuilderTest
{
    @RegisterExtension
    private LogCaptureExtension logCapture = new LogCaptureExtension(LogLevel.WARN);

    @InjectMockComponents
    private WikiModelParserListenerBuilder listenerBuilder;

    @MockComponent
    private ListenerRegistry listenerRegistry;

    @MockComponent
    private SyntaxRegistry syntaxRegistry;

    private Listener listener;

    @Test
    void buildListenerNoNewListener() throws Exception
    {
        when(this.syntaxRegistry.resolveSyntax(XWIKI_2_1.toIdString())).thenReturn(XWIKI_2_1);
        when(this.listenerRegistry.getListeners(any(), anyString(), any())).thenReturn(List.of());

        Listener newListener = this.listenerBuilder.buildListener(XWIKI_2_1.toIdString(), this.listener);
        assertSame(this.listener, newListener);

        verify(this.listenerRegistry).getListeners(any(), eq(PARSE_ACTION), eq(XWIKI_2_1));
    }

    @Test
    void buildListener() throws Exception
    {
        ChainingListener firstChainingListener = mock(ChainingListener.class);

        when(this.syntaxRegistry.resolveSyntax(XWIKI_2_1.toIdString())).thenReturn(XWIKI_2_1);
        when(this.listenerRegistry.getListeners(any(), anyString(), any())).thenReturn(List.of(
            firstChainingListener, mock(ChainingListener.class)
        ));

        Listener newListener = this.listenerBuilder.buildListener(XWIKI_2_1.toIdString(), this.listener);
        assertSame(firstChainingListener, newListener);

        verify(this.listenerRegistry).getListeners(any(), eq(PARSE_ACTION), eq(XWIKI_2_1));
    }

    @Test
    void buildListenerInvalidSyntax() throws Exception
    {
        ChainingListener firstChainingListener = mock(ChainingListener.class);

        when(this.syntaxRegistry.resolveSyntax(XWIKI_2_1.toIdString())).thenThrow(ParseException.class);

        when(this.listenerRegistry.getListeners(any(), anyString(), any())).thenReturn(List.of(
            firstChainingListener, mock(ChainingListener.class)
        ));

        Listener newListener = this.listenerBuilder.buildListener(XWIKI_2_1.toIdString(), this.listener);
        assertSame(firstChainingListener, newListener);

        verify(this.listenerRegistry).getListeners(any(), eq(PARSE_ACTION), isNull());

        assertEquals(
            "Failed to find syntax [xwiki/2.1] in the registry during parser initialization. Cause: [ParseException: ]",
            this.logCapture.getMessage(0));
        assertEquals(Level.WARN, this.logCapture.getLogEvent(0).getLevel());
    }
}
