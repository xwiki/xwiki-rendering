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
    public static final int BEGIN = inc();

    public static final int BEGIN_FORMAT = inc();

    public static final int ESCAPE = inc();

    public static final int EXTENSION = inc();

    private static int fCounter;

    public static final int IMAGE = inc();

    public static final int LINE_BREAK = inc();

    public static final int MACRO = inc();

    public static final int NEW_LINE = inc();

    public static final int REFERENCE = inc();

    public static final int SPACE = inc();

    public static final int SPECIAL_SYMBOL = inc();

    public static final int VERBATIM = inc();

    public static final int WORD = inc();

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
