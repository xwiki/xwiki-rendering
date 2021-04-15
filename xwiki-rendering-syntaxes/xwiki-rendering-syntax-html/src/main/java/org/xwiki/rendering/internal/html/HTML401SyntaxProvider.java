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
package org.xwiki.rendering.internal.html;

import java.util.Collections;
import java.util.List;

import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.syntax.SyntaxType;

/**
 * Register the {@code html/4.01} Syntax supported by this module.
 *
 * @version $Id$
 * @since 13.3RC1
 */
@Component
@Named("html/4.01")
@Singleton
public class HTML401SyntaxProvider implements Provider<List<Syntax>>
{
    /**
     * HTML syntax type.
     */
    public static final SyntaxType HTML = new SyntaxType("html", "HTML");

    /**
     * HTML 4.01 syntax.
     */
    public static final Syntax HTML_4_01 = new Syntax(HTML, "4.01");

    @Override
    public List<Syntax> get()
    {
        return Collections.singletonList(HTML_4_01);
    }
}
