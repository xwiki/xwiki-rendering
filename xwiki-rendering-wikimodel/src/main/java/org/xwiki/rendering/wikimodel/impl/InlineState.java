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
package org.xwiki.rendering.wikimodel.impl;

/**
 * @version $Id$
 * @since 4.0M1
 */
public class InlineState
{
    public final static int BEGIN = inc();

    public final static int BEGIN_FORMAT = inc();

    public final static int ESCAPE = inc();

    public final static int EXTENSION = inc();

    private static int fCounter;

    public final static int IMAGE = inc();

    public final static int LINE_BREAK = inc();

    public final static int MACRO = inc();

    public final static int NEW_LINE = inc();

    public final static int REFERENCE = inc();

    public final static int SPACE = inc();

    public final static int SPECIAL_SYMBOL = inc();

    public final static int VERBATIM = inc();

    public final static int WORD = inc();

    private static int inc()
    {
        fCounter++;
        return 1 << fCounter;
    }

    private int fState = BEGIN_FORMAT;

    public boolean check(int mask)
    {
        return (fState & mask) == mask;
    }

    public int get()
    {
        return fState;
    }

    public void set(int state)
    {
        fState = state;
    }
}
