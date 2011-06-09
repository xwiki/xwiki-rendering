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
package org.xwiki.rendering.internal.parser.xwiki20;

import java.util.Arrays;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.internal.parser.reference.GenericLinkReferenceParser;

/**
 * Parses the content of XWiki 2.0 resource references. The supported generic format is as follows:
 * <code>(link)(@interWikiAlias)?</code>, where:
 * <ul>
 * <li><code>link</code>: The full link reference using the following syntax:
 * <code>(reference)(#anchor)?(?queryString)?</code>, where:
 * <ul>
 * <li><code>reference</code>: The link reference. This can be either a URI in the form <code>protocol:path</code>
 * (example: "http://xwiki.org", "mailto:john@smith.com) or a wiki page name (example: "wiki:Space.WebHome"). Note that
 * in the case of a wiki page name the character "\" is used as the escape character (for example if you wish to have
 * "#" or "?" in your page name you'll need to write "\#" and "\?").</li>
 * <li><code>anchor</code>: An optional anchor name pointing to an anchor defined in the referenced link. Note that in
 * XWiki anchors are automatically created for titles. Example: "TableOfContentAnchor".</li>
 * <li><code>queryString</code>: An optional query string for specifying parameters that will be used in the rendered
 * URL. Example: "mydata1=5&mydata2=Hello".</li>
 * </ul>
 * The <code>link</code> element is mandatory.</li>
 * <li><code>interWikiAlias</code>: An optional <a href="http://en.wikipedia.org/wiki/InterWiki">Inter Wiki</a> alias as
 * defined in the InterWiki Map. Example: "wikipedia"</li>
 * </ul>
 * Examples of valid wiki links:
 * <ul>
 * <li>Hello World</li>
 * <li>http://myserver.com/HelloWorld</li>
 * <li>HelloWorld#Anchor</li>
 * <li>Hello World@Wikipedia</li>
 * <li>mywiki:HelloWorld</li>
 * <li>Hello World?param1=1&param2=2</li>
 * </ul>
 * Note that allowed URIs are URLs of the form {@code http://}, {@code mailto:}, {@code image:} and {@code attach:}.
 * 
 * @version $Id$
 * @since 2.5RC1
 */
@Component
@Named("xwiki/2.0/link")
@Singleton
public class XWiki20LinkReferenceParser extends GenericLinkReferenceParser
{
    /**
     * The list of recognized URL prefixes.
     */
    public static final List<String> URI_PREFIXES = Arrays.asList("mailto", "image", "attach");

    /**
     * @return the list of URI prefixes the link parser recognizes
     */
    protected List<String> getAllowedURIPrefixes()
    {
        return URI_PREFIXES;
    }
}
