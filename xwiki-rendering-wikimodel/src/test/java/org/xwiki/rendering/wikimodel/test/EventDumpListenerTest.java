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
package org.xwiki.rendering.wikimodel.test;

import org.xwiki.rendering.wikimodel.EventDumpListener;
import org.xwiki.rendering.wikimodel.IWemListener;
import org.xwiki.rendering.wikimodel.IWikiParser;
import org.xwiki.rendering.wikimodel.IWikiPrinter;
import org.xwiki.rendering.wikimodel.WikiParserException;
import org.xwiki.rendering.wikimodel.common.CommonWikiParser;

/**
 * @version $Id$
 * @since 4.0M1
 */
public class EventDumpListenerTest extends AbstractWikiParserTest
{
    public EventDumpListenerTest(String name)
    {
        super(name);
    }

    @Override
    protected IWikiParser newWikiParser()
    {
        return new CommonWikiParser();
    }

    /**
     * @param buf
     * @return
     */
    protected IWemListener newParserListener(final StringBuffer buf)
    {
        IWikiPrinter printer = newPrinter(buf);
        IWemListener listener = new EventDumpListener(printer);
        return listener;
    }

    public void test() throws WikiParserException
    {
        test("%worksIn (((\n"
            + "   %type [Company]\n"
            + "   %name Cognium *Systems*\n"
            + "   %address (((\n"
            + "   )))");
        /**
         * The trace, how it *should* be:
         *
         * <pre>       
         beginDocument()
         beginPropertyBlock('worksIn',doc=true)
         beginPropertyBlock('type',doc=false)
         beginParagraph()
         beginFormat([])
         onReference('Company')
         endFormat([])
         endParagraph()
         endPropertyBlock('type', doc=false)
         beginPropertyBlock('name',doc=false)
         beginParagraph()
         beginFormat([])
         onWord('Cognium')
         onSpace(' ')
         onWord('Systems')
         endFormat([])
         endParagraph()
         endPropertyBlock('name', doc=false)
         beginPropertyBlock('address',doc=true)
         endPropertyBlock('address', doc=true)
         endPropertyBlock('worksIn', doc=true)
         endDocument()
         </pre>
         */

    }
}
