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
package org.xwiki.rendering.wikimodel;

/**
 * @version $Id$
 * @since 4.0M1
 */
public class WikiPrinter implements IWikiPrinter
{
    private StringBuilder fBuffer;

    public WikiPrinter()
    {
        this(new StringBuilder());
    }

    /**
     * @param buffer
     */
    public WikiPrinter(StringBuilder buffer)
    {
        fBuffer = buffer;
    }

    public StringBuilder getBuffer()
    {
        return fBuffer;
    }

    /**
     * @return a new line symbols
     */
    protected String getEol()
    {
        return "\n";
    }

    /**
     * @see IWikiPrinter#print(java.lang.String)
     */
    public void print(String str)
    {
        fBuffer.append(str);
    }

    /**
     * @see IWikiPrinter#println(java.lang.String)
     */
    public void println(String str)
    {
        fBuffer.append(str);
        fBuffer.append(getEol());
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return fBuffer.toString();
    }
}
