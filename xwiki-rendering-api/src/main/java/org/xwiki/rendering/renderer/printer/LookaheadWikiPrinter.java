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
package org.xwiki.rendering.renderer.printer;

/**
 * Wiki printer that allows deferring printing text and that instead can save it in some internal buffer. This allows
 * accumulating some content before it's flushed. This feature is used for example in the XWiki Syntax Renderer to
 * accumulate text so that it be reviewed and escaped before printed (indeed some text has some characters that need to
 * be escaped or they'd have a wiki meaning otherwise).
 *
 * @version $Id$
 * @since 1.7
 */
public class LookaheadWikiPrinter extends WrappingWikiPrinter
{
    private StringBuffer buffer = new StringBuffer();

    /**
     * @param printer the wiki printer to wrap
     */
    public LookaheadWikiPrinter(WikiPrinter printer)
    {
        super(printer);
    }

    protected void printInternal(String text)
    {
        super.print(text);
    }

    protected void printlnInternal(String text)
    {
        super.println(text);
    }

    @Override
    public void print(String text)
    {
        flush();
        printInternal(text);
    }

    @Override
    public void println(String text)
    {
        flush();
        printlnInternal(text);
    }

    /**
     * Save the passed text in the internal buffer so that it's printed only when the printer is flushed.
     *
     * @param text the text to print in a delayed manner
     */
    public void printDelayed(String text)
    {
        getBuffer().append(text);
    }

    /**
     * Save the passed text followed by an end of line in the internal buffer so that it's printed only when the printer
     * is flushed.
     *
     * @param text the text to print in a delayed manner
     */
    public void printlnDelayed(String text)
    {
        getBuffer().append(text).append(getEOL());
    }

    /**
     * @return the internal buffer holding the not-yet-flushed content
     */
    public StringBuffer getBuffer()
    {
        return this.buffer;
    }

    /**
     * Print the content currently held in the internal buffer and empty that buffer.
     */
    public void flush()
    {
        if (getBuffer().length() > 0) {
            printInternal(getBuffer().toString());
            getBuffer().setLength(0);
        }
    }

    /**
     * This method is protected to allow classes extending this one to define what a new line is.
     *
     * @return a new line symbols
     */
    protected String getEOL()
    {
        return "\n";
    }
}
