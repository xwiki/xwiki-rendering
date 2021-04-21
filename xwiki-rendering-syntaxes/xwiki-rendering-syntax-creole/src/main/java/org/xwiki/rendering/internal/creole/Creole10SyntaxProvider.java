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
package org.xwiki.rendering.internal.creole;

import java.util.Collections;
import java.util.List;

import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.syntax.SyntaxType;

/**
 * Register the {@code creole/1.0} Syntax supported by this module.
 *
 * @version $Id$
 * @since 13.4RC1
 */
@Component
@Named("creole/1.0")
@Singleton
public class Creole10SyntaxProvider implements Provider<List<Syntax>>
{
    /**
     * Creole syntax type.
     */
    public static final SyntaxType CREOLE = new SyntaxType("creole", "Creole");

    /**
     * Creole 1.0 syntax.
     */
    public static final Syntax CREOLE_1_0 = new Syntax(CREOLE, "1.0");

    @Override
    public List<Syntax> get()
    {
        return Collections.singletonList(CREOLE_1_0);
    }
}
