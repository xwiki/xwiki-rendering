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
import java.io.OutputStream;

/**
 * Wiki Printer that sends all its data to an {@link OutputStream}.
 *
 * @version $Id$
 * @since 3.3M1
 */
public class OutputStreamWikiPrinter extends OutputStream implements WikiPrinter
{
    /**
     * @see #OutputStreamWikiPrinter(java.io.OutputStream)
     */
    private OutputStream outputStream;

    /**
     * @param outputStream the stream to forward data to
     */
    public OutputStreamWikiPrinter(OutputStream outputStream)
    {
        this.outputStream = outputStream;
    }
    
    @Override
    public void close() throws IOException
    {
        this.outputStream.close();
    }

    @Override
    public void flush() throws IOException
    {
        this.outputStream.flush();
    }

    @Override
    public void write(byte[] bytes) throws IOException
    {
        this.outputStream.write(bytes);
    }

    @Override
    public void write(byte[] bytes, int i, int i1) throws IOException
    {
        this.outputStream.write(bytes, i, i1);
    }

    @Override
    public void write(int i) throws IOException
    {
        this.outputStream.write(i);
    }

    @Override
    public void print(String text)
    {
        try {
            write(text.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to write [" + text + "] to output stream", e);
        }
    }

    @Override
    public void println(String text)
    {
        print(text + '\n');
    }
}
