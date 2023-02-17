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

import java.io.StringReader;

import org.xwiki.rendering.listener.InlineFilterListener;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.listener.WrappingListener;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.StreamParser;
import org.xwiki.rendering.util.IdGenerator;
import org.xwiki.rendering.util.ParserUtils;

/**
 * Methods for helping in parsing.
 *
 * @version $Id$
 * @since 1.8M1
 */
public class WikiModelParserUtils extends ParserUtils
{
    public void parseInline(StreamParser parser, String content, Listener listener) throws ParseException
    {
        parseInline(parser, content, listener, null, false);
    }

    private final class PrefixIgnoredInlineFilterListener extends InlineFilterListener
    {
        private boolean foundWord;

        private boolean foundSpace;

        @Override
        public void onWord(String word)
        {
            if (this.foundWord) {
                super.onWord(word);
            } else {
                this.foundWord = true;
            }
        }

        @Override
        public void onSpace()
        {
            if (this.foundSpace) {
                super.onSpace();
            } else {
                this.foundSpace = true;
            }
        }
    }

    /**
     * @param parser the parser for parsing the content
     * @param content the content to parse
     * @param listener the listener to call on events
     * @param idGenerator the id generator to automatically generate ids
     * @param prefix if a prefix shall be added and ignored at the start of the content
     * @since 14.2RC1
     */
    public void parseInline(StreamParser parser, String content, Listener listener, IdGenerator idGenerator,
        boolean prefix) throws ParseException
    {
        String contentToParse;
        WrappingListener inlineFilterListener;

        if (prefix) {
            inlineFilterListener = new PrefixIgnoredInlineFilterListener();
            contentToParse = "wikimarker " + content;
        } else {
            inlineFilterListener = new InlineFilterListener();
            contentToParse = content;
        }

        inlineFilterListener.setWrappedListener(listener);

        if (idGenerator != null) {
            parser.parse(new StringReader(contentToParse), inlineFilterListener, idGenerator);
        } else {
            parser.parse(new StringReader(contentToParse), inlineFilterListener);
        }
    }
}
