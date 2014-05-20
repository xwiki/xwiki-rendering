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

import org.xwiki.rendering.wikimodel.IWemListener;
import org.xwiki.rendering.wikimodel.IWikiParser;
import org.xwiki.rendering.wikimodel.WikiParserException;
import org.xwiki.rendering.wikimodel.internal.confluence.javacc.ConfluenceWikiScanner;
import org.xwiki.rendering.wikimodel.internal.confluence.javacc.ParseException;

/**
 * <pre>
 * http://confluence.atlassian.com/renderer/notationhelp.action?section=all
 * </pre>
 *
 * @author MikhailKotelnikov
 */
public class ConfluenceWikiParser implements IWikiParser
{
    /**
     * Indicate if {noformat} macro should be seen as a macro or a verbatim
     * block.
     */
    private boolean fNoformatAsMacro = true;

    public ConfluenceWikiParser()
    {
    }

    /**
     *
     */
    public ConfluenceWikiParser(boolean noformatAsMacro)
    {
        fNoformatAsMacro = noformatAsMacro;
    }

    /**
     * @see org.xwiki.rendering.wikimodel.IWikiParser#parse(java.io.Reader,
     *      org.xwiki.rendering.wikimodel.IWemListener)
     */
    public void parse(Reader reader, IWemListener listener) throws WikiParserException
    {
        try {
            ConfluenceWikiScanner scanner = new ConfluenceWikiScanner(reader);
            scanner.setNoformatAsMacro(fNoformatAsMacro);
            ConfluenceWikiScannerContext context = new ConfluenceWikiScannerContext(listener);
            scanner.parse(context);
        } catch (ParseException e) {
            throw new WikiParserException(e);
        }
    }
}
