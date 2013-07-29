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

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

/**
 * @version $Id$
 */
public class WriterWikiPrinter implements WikiPrinter
{
    /**
     * The buffer where to put the provided {@link String}s.
     */
    private Writer writer;

    /**
     * The default constructor. It initialize a new empty {@link StringBuffer}.
     */
    public WriterWikiPrinter()
    {
        this(new StringWriter());
    }

    /**
     * @param writer the {@link Writer} to where to put the provided {@link String}s.
     */
    public WriterWikiPrinter(Writer writer)
    {
        this.writer = writer;
    }

    public Writer getWriter()
    {
        return this.writer;
    }

    /**
     * This method is protected to allow classes extending this one to override what a new line is.
     * 
     * @return a new line symbols
     */
    protected String getEOL()
    {
        return "\n";
    }

    @Override
    public void print(String text)
    {
        try {
            getWriter().append(text);
        } catch (IOException e) {
            // TODO runtime exception ?
        }
    }

    @Override
    public void println(String text)
    {
        try {
            getWriter().append(text).append(getEOL());
        } catch (IOException e) {
            // TODO runtime exception ?
        }
    }

    @Override
    public String toString()
    {
        return getWriter().toString();
    }
}
