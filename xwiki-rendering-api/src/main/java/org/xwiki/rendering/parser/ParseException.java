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
package org.xwiki.rendering.parser;

import org.xwiki.stability.Unstable;

/**
 * Encapsulate a parsing error.
 *
 * @version $Id$
 */
public class ParseException extends Exception
{
    /**
     * Class ID for serialization.
     */
    private static final long serialVersionUID = 442523704445037944L;

    /**
     * Construct a new ParseException with the specified detail message.
     *
     * @param message The detailed message. This can later be retrieved by the Throwable.getMessage() method.
     */
    public ParseException(String message)
    {
        super(message);
    }

    /**
     * Constructs a new parse exception with the specified cause .
     *
     * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method).)
     * @since 14.9RC1
     * @since 14.4.6
     * @since 13.10.10
     */
    @Unstable
    public ParseException(Throwable cause)
    {
        super(cause);
    }

    /**
     * Construct a new ParseException with the specified detail message and cause.
     *
     * @param message The detailed message. This can later be retrieved by the Throwable.getMessage() method.
     * @param throwable the cause. This can be retrieved later by the Throwable.getCause() method. (A null value is
     *            permitted, and indicates that the cause is nonexistent or unknown)
     */
    public ParseException(String message, Throwable throwable)
    {
        super(message, throwable);
    }
}
