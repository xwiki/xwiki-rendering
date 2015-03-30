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
package org.xwiki.rendering.wikimodel.xhtml.handler;

import org.xwiki.rendering.wikimodel.EmptyWemListener;
import org.xwiki.rendering.wikimodel.impl.WikiScannerContext;
import org.xwiki.rendering.wikimodel.xhtml.impl.TagContext;

/**
 * @version $Id$
 * @since 4.0M1
 */
public class PreserveTagHandler extends TagHandler
{
    public PreserveTagHandler()
    {
        super(true);
    }

    @Override
    protected void begin(TagContext context)
    {
        // filter content of the <pre> element
        context.getTagStack().pushScannerContext(
            new WikiScannerContext(new PreserverListener()));
        context.getScannerContext().beginDocument();
    }

    @Override
    protected void end(TagContext context)
    {
        context.getScannerContext().endDocument();
        PreserverListener preserverListener = (PreserverListener) context
            .getTagStack().popScannerContext().getfListener();
        sendEmptyLines(context);

        String preservedContent = preserverListener.toString();
        handlePreservedContent(context, preservedContent);
    }

    protected void handlePreservedContent(TagContext context, String preservedContent)
    {
        context.getScannerContext().onVerbatim(preservedContent,
            false, context.getParams());
    }
}

class PreserverListener extends EmptyWemListener
{
    StringBuffer buffer = new StringBuffer();

    @Override
    public String toString()
    {
        return this.buffer.toString();
    }

    @Override
    public void onWord(String str)
    {
        this.buffer.append(str);
    }

    @Override
    public void onSpecialSymbol(String str)
    {
        this.buffer.append(str);
    }

    @Override
    public void onSpace(String str)
    {
        this.buffer.append(str);
    }

    @Override
    public void onLineBreak()
    {
        this.buffer.append("\n");
    }

    @Override
    public void onNewLine()
    {
        this.buffer.append("\n");
    }
}
