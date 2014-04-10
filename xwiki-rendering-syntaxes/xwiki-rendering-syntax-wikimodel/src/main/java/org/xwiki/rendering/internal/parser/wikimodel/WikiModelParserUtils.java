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
        parseInline(parser, content, listener, false);
    }

    /**
     * @since 6.0RC1, 5.4.5
     */
    public void parseInline(StreamParser parser, String content, Listener listener, boolean prefix)
        throws ParseException
    {
        if (prefix) {
            WrappingListener inlineFilterListener = new InlineFilterListener()
            {
                private boolean foundWord = false;

                private boolean foundSpace = false;

                @Override
                public void onWord(String word)
                {
                    if (foundWord) {
                        super.onWord(word);
                    } else {
                        foundWord = true;
                    }
                }

                @Override
                public void onSpace()
                {
                    if (foundSpace) {
                        super.onSpace();
                    } else {
                        foundSpace = true;
                    }
                }
            };
            inlineFilterListener.setWrappedListener(listener);

            parser.parse(new StringReader("wikimarker " + content), inlineFilterListener);
        } else {
            WrappingListener inlineFilterListener = new InlineFilterListener();
            inlineFilterListener.setWrappedListener(listener);
            parser.parse(new StringReader(content), inlineFilterListener);
        }
    }
}
