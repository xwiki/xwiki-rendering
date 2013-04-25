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
package org.xwiki.rendering.internal.parser.reference;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;

/**
 * Parses the content of resource references. The format of a resource reference is the following:
 * {@code (type):(reference)} where {@code type} represents the type (see
 * {@link org.xwiki.rendering.listener.reference.ResourceType} of the resource pointed to (e.g. document, mailto,
 * attachment, image, document in another wiki, etc), and {@code reference} defines the target. The syntax of
 * {@code reference} depends on the Resource type and is documented in the javadoc of the various
 * {@link org.xwiki.rendering.parser.ResourceReferenceTypeParser} implementations. Note that the implementation is
 * pluggable and it's allowed plug new resource reference types by implementing
 * {@link org.xwiki.rendering.parser.ResourceReferenceTypeParser}s and registering the implementation as a component.
 * 
 * @version $Id$
 * @since 2.6M1
 * @deprecated starting with 5.1M1 each Syntax needs to have its own component extending
 *             AbstractDefaultResourceReferenceParser in order to control the list of ResourceReferenceTypeParser it
 *             supports
 */
@Component
@Singleton
@Deprecated
public class DefaultResourceReferenceParser extends AbstractDefaultResourceReferenceParser
{
    @Override
    protected boolean isTypeParserSupported(String typePrefix)
    {
        return true;
    }
}
