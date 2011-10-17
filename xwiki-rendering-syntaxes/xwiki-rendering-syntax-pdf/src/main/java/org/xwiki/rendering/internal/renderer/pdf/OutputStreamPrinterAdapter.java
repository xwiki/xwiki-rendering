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
package org.xwiki.rendering.internal.renderer.pdf;

import java.io.IOException;
import java.io.OutputStream;

import org.xwiki.rendering.renderer.printer.WikiPrinter;

/**
 * Sends everything printed to an {@link OutputStream} to a {@link WikiPrinter}.
 * 
 * @version $Id$
 * @since 3.3M1
 */
public class OutputStreamPrinterAdapter extends OutputStream
{
    /**
     * @see #OutputStreamPrinterAdapter(org.xwiki.rendering.renderer.printer.WikiPrinter)
     */
    private WikiPrinter printer;

    /**
     * @param printer the wiki printer to send data to
     */
    public OutputStreamPrinterAdapter(WikiPrinter printer)
    {
        this.printer = printer;
    }

    @Override
    public void close() throws IOException
    {
        super.close();
    }

    @Override
    public void flush() throws IOException
    {
        super.flush();
    }

    @Override
    public void write(byte[] bytes) throws IOException
    {
        this.printer.print(new String(bytes));
    }

    @Override
    public void write(byte[] bytes, int i, int i1) throws IOException
    {
        this.printer.print(new String(bytes, i, i1));
    }

    @Override
    public void write(int i) throws IOException
    {
        this.printer.print("" + i);
    }
}
