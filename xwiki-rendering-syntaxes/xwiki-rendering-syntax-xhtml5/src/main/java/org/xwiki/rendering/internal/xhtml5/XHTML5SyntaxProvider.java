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
package org.xwiki.rendering.internal.xhtml5;

import java.util.Collections;
import java.util.List;

import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.syntax.SyntaxType;
import org.xwiki.stability.Unstable;

/**
 * Register the {@code xhtml/5} Syntax supported by this module.
 *
 * @version $Id$
 * @since 14.0RC1
 */
@Component
@Named("xhtml/5")
@Singleton
@Unstable
public class XHTML5SyntaxProvider implements Provider<List<Syntax>>
{
    /**
     * XHTML syntax type.
     */
    public static final SyntaxType XHTML = new SyntaxType("xhtml", "XHTML");

    /**
     * XHTML 5 syntax.
     */
    public static final Syntax XHTML_5 = new Syntax(XHTML, "5");

    @Override public List<Syntax> get()
    {
        return Collections.singletonList(XHTML_5);
    }
}
