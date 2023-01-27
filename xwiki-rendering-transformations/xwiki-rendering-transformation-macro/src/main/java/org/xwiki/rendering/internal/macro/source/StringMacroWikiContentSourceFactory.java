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
package org.xwiki.rendering.internal.macro.source;

import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.macro.source.MacroContentSourceReference;
import org.xwiki.rendering.macro.source.MacroContentWikiSource;
import org.xwiki.rendering.macro.source.MacroContentWikiSourceFactory;
import org.xwiki.rendering.transformation.MacroTransformationContext;

/**
 * @version $Id$
 * @since 15.1RC1
 * @since 14.10.5
 */
@Component
@Singleton
@Named(MacroContentSourceReference.TYPE_STRING)
public class StringMacroWikiContentSourceFactory implements MacroContentWikiSourceFactory
{
    @Override
    public MacroContentWikiSource getContent(MacroContentSourceReference reference, MacroTransformationContext context)
    {
        return new MacroContentWikiSource(reference, reference.getReference(), null);
    }
}
