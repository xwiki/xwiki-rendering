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
package org.xwiki.rendering.internal.parser.xwiki21.reference;

import java.util.Arrays;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.internal.parser.reference.AbstractDefaultResourceReferenceParser;

/**
 * Configures {@link AbstractDefaultResourceReferenceParser} to specify which type parsers are supported by the
 * XWiki Syntax 2.1. Namely:
 * <ul>
 *   <li>url</li>
 *   <li>unc</li>
 *   <li>path</li>
 *   <li>mailto</li>
 *   <li>interwiki</li>
 *   <li>icon</li>
 *   <li>doc</li>
 *   <li>attach</li>
 * </ul>
 *
 * @version $Id$
 * @since 5.1M1
 */
@Component
@Named("default/2.1")
@Singleton
public class XWiki21DefaultResourceReferenceParser extends AbstractDefaultResourceReferenceParser
{
    /**
     * The list of type parsers supported by the XWiki Syntax 2.1.
     */
    private static final List<String> SUPPORTED_TYPE = Arrays.asList(
        "url", "unc", "path", "mailto", "interwiki", "icon", "doc", "attach"
    );

    @Override
    protected boolean isTypeParserSupported(String typePrefix)
    {
        return SUPPORTED_TYPE.contains(typePrefix);
    }
}
