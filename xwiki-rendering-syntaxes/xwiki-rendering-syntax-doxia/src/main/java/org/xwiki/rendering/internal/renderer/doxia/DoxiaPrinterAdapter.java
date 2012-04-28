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
package org.xwiki.rendering.internal.renderer.doxia;

import java.io.IOException;
import java.io.Writer;

import org.xwiki.rendering.renderer.printer.WikiPrinter;

/**
 * Bridge between {@link Writer} and {@link WikiPrinter}. This is needed since Doxia Sinks need to be passed a
 * {@link Writer}.
 *
 * @version $Id$
 * @since 3.2RC1
 */
public class DoxiaPrinterAdapter extends Writer
{
    /**
     * The wiki printer to print to.
     */
    private WikiPrinter printer;

    /**
     * @param printer the wiki printer to print to
     */
    public DoxiaPrinterAdapter(WikiPrinter printer)
    {
        this.printer = printer;
    }

    @Override
    public Writer append(char c) throws IOException
    {
        this.printer.print("" + c);
        return this;
    }

    @Override
    public Writer append(CharSequence charSequence, int i, int i1) throws IOException
    {
        return append(charSequence.subSequence(i, i1));
    }

    @Override
    public Writer append(CharSequence charSequence) throws IOException
    {
        this.printer.print(charSequence.toString());
        return this;
    }

    @Override
    public void close() throws IOException
    {
        // Do nothing
    }

    @Override
    public void flush() throws IOException
    {
        // Do nothing
    }

    @Override
    public void write(char[] chars) throws IOException
    {
        this.printer.print(new String(chars));
    }

    @Override
    public void write(char[] chars, int i, int i1) throws IOException
    {
        this.printer.print(new String(chars, i, i1));
    }

    @Override
    public void write(int i) throws IOException
    {
        this.printer.print("" + i);
    }

    @Override
    public void write(String s) throws IOException
    {
        this.printer.print(s);
    }

    @Override
    public void write(String s, int i, int i1) throws IOException
    {
        this.printer.print(s.substring(i, i1));
    }
}
