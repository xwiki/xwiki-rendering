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
package org.xwiki.rendering.wikimodel.confluence;

import java.io.Reader;
import java.io.StringReader;
import java.util.Stack;

import org.xwiki.rendering.wikimodel.CompositeListener;
import org.xwiki.rendering.wikimodel.IWemListener;
import org.xwiki.rendering.wikimodel.WikiParameters;
import org.xwiki.rendering.wikimodel.WikiParserException;

/**
 * <pre>
 * http://confluence.atlassian.com/renderer/notationhelp.action?section=all
 * </pre>
 *
 * @author MikhailKotelnikov
 */
public class ConfluenceExtendedWikiParser extends ConfluenceWikiParser
{
    public static class EnhancedListener extends CompositeListener
    {
        private Stack<Boolean> fSkipDocument = new Stack<Boolean>();

        public EnhancedListener(IWemListener... listeners)
        {
            super(listeners);
        }

        @Override
        public void beginDocument(WikiParameters params)
        {
            if (!skipDocument()) {
                super.beginDocument(params);
            }
        }

        @Override
        public void beginSection(
            int docLevel,
            int headerLevel,
            WikiParameters params)
        {
            if (!skipDocument()) {
                super.beginSection(docLevel, headerLevel, params);
            }
        }

        @Override
        public void beginSectionContent(
            int docLevel,
            int headerLevel,
            WikiParameters params)
        {
            if (!skipDocument()) {
                super.beginSectionContent(docLevel, headerLevel, params);
            }
            push(false);
        }

        @Override
        public void endDocument(WikiParameters params)
        {
            if (!skipDocument()) {
                super.endDocument(params);
            }
        }

        @Override
        public void endSection(
            int docLevel,
            int headerLevel,
            WikiParameters params)
        {
            if (!skipDocument()) {
                super.endSection(docLevel, headerLevel, params);
            }
        }

        @Override
        public void endSectionContent(
            int docLevel,
            int headerLevel,
            WikiParameters params)
        {
            pop();
            if (!skipDocument()) {
                super.endSectionContent(docLevel, headerLevel, params);
            }
        }

        @Override
        public void onMacroBlock(
            String macroName,
            WikiParameters params,
            String content)
        {
            String type = null;
            if ("note".equals(macroName)) {
                type = "N";
            } else if ("tip".equals(macroName)) {
                type = "T";
            }
            if (type != null) {
                beginInfoBlock(type, params);
                parseContent(content);
                endInfoBlock(type, params);
            } else {
                super.onMacroBlock(macroName, params, content);
            }
        }

        /**
         * @param content
         * @throws WikiParserException
         */
        private void parseContent(String content)
        {
            try {
                push(true);
                StringReader reader = new StringReader(content);
                ConfluenceWikiParser parser = new ConfluenceWikiParser();
                parser.parse(reader, this);
                pop();
            } catch (WikiParserException e) {
                throw new RuntimeException(e);
            }
        }

        private void pop()
        {
            fSkipDocument.pop();
        }

        private void push(boolean b)
        {
            fSkipDocument.push(b);
        }

        private boolean skipDocument()
        {
            if (fSkipDocument.isEmpty()) {
                return false;
            }
            Boolean peek = fSkipDocument.peek();
            return peek;
        }
    }

    /**
     *
     */
    public ConfluenceExtendedWikiParser()
    {
        super();
    }

    /**
     * @see org.xwiki.rendering.wikimodel.IWikiParser#parse(java.io.Reader,
     *      org.xwiki.rendering.wikimodel.IWemListener)
     */
    @Override
    public void parse(Reader reader, IWemListener listener)
        throws WikiParserException
    {
        IWemListener composite = new EnhancedListener(listener);
        super.parse(reader, composite);
    }
}
