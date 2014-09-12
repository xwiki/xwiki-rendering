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
import java.io.Writer;

/**
 * Printer using a {@link Writer} as the underlying output target.
 *
 * @version $Id$
 * @since 6.2M1
 */
public class WriterWikiPrinter implements WikiPrinter
{
    /**
     * The buffer where to put the provided {@link String}s.
     */
    private Writer writer;

    /**
     * @param writer the writer
     */
    public WriterWikiPrinter(Writer writer)
    {
        this.writer = writer;
    }

    /**
     * @return the writer
     */
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
            this.writer.write(text);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write", e);
        }
    }

    @Override
    public void println(String text)
    {
        print(text);
        print(getEOL());
    }

    @Override
    public String toString()
    {
        return this.writer.toString();
    }
}
