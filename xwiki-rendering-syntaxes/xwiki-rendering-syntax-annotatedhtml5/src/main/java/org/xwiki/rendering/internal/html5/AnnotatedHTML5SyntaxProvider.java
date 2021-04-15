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
package org.xwiki.rendering.internal.html5;

import java.util.Collections;
import java.util.List;

import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.syntax.SyntaxType;

/**
 * Register the {@code annotatedhtml/5.0} Syntax supported by this module.
 *
 * @version $Id$
 * @since 13.3RC1
 */
@Component
@Named("annotatedhtml/5.0")
@Singleton
public class AnnotatedHTML5SyntaxProvider implements Provider<List<Syntax>>
{
    /**
     * Annotated HTML syntax type.
     */
    public static final SyntaxType ANNOTATED_HTML = new SyntaxType("annotatedhtml", "Annotated HTML");

    /**
     * Annotated HTML 5.0 syntax.
     */
    public static final Syntax ANNOTATED_HTML_5_0 = new Syntax(ANNOTATED_HTML, "5.0");

    @Override
    public List<Syntax> get()
    {
        return Collections.singletonList(ANNOTATED_HTML_5_0);
    }
}
