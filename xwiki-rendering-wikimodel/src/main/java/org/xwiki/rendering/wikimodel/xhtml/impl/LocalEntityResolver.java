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
package org.xwiki.rendering.wikimodel.xhtml.impl;

import org.xwiki.component.annotation.Component;

/**
 * @version $Id$
 * @since 4.0M1
 * @deprecated use org.xwiki.xml.internal.LocalEntityResolver instead
 */
@Deprecated
// TODO: Work around the fact that LocalEntityResolver is a Component while org.xwiki.xml.internal.LocalEntityResolver
//  is not supposed to be one. This is a bad design since a non-component should not extend a component (it's dangerous
//  - @Inject-ed component will not be injected, and the extending class will inherit the @Component annotation, which
//  is bad - imagine for example that in the future we auto-generate components.txt based on the @Component annotation).
@Component(staticRegistration = false)
public class LocalEntityResolver extends org.xwiki.xml.internal.LocalEntityResolver
{

}
