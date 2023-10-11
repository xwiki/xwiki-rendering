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
package org.xwiki.rendering.internal.transformation.macro;

import java.util.List;

import org.xwiki.rendering.macro.AbstractMacro;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.transformation.MacroTransformationContext;

/**
 * A macro that does nothing.
 * 
 * @version $Id$
 * @since 15.9RC1
 */
public final class VoidMacro extends AbstractMacro
{
    /**
     * The unique instance to use for a macro doing nothing.
     */
    public static final VoidMacro INSTANCE = new VoidMacro();

    /**
     * The default constructor.
     */
    private VoidMacro()
    {
        super(null);
    }

    @Override
    public boolean supportsInlineMode()
    {
        return true;
    }

    @Override
    public List execute(Object parameters, String content, MacroTransformationContext context)
        throws MacroExecutionException
    {
        return List.of();
    }
}
