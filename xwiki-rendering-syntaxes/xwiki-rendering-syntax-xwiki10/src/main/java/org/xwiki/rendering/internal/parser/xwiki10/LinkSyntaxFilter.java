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
package org.xwiki.rendering.internal.parser.xwiki10;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.rendering.parser.xwiki10.AbstractFilter;
import org.xwiki.rendering.parser.xwiki10.Filter;
import org.xwiki.rendering.parser.xwiki10.FilterContext;
import org.xwiki.rendering.parser.xwiki10.util.CleanUtil;

/**
 * @version $Id$
 * @since 1.8M1
 */
@Component
@Named("link")
@Singleton
public class LinkSyntaxFilter extends AbstractFilter implements Initializable
{
    private static final Pattern LINKSYNTAX_PATTERN = Pattern.compile("\\[(.+?)\\]");

    /**
     * URL matching pattern.
     */
    private static final Pattern URL_SCHEME_PATTERN = Pattern.compile("^[a-zA-Z0-9+.-]*://.*$");

    @Inject
    @Named("escape20")
    private Filter escape20Filter;

    @Override
    public void initialize() throws InitializationException
    {
        // make sure to match link before wiki syntax (which is 1000) since wiki syntax is not supported in links label
        // in xwiki/1.0 syntax
        setPriority(900);
    }

    @Override
    public String filter(String content, FilterContext filterContext)
    {
        StringBuffer result = new StringBuffer();

        Matcher matcher = LINKSYNTAX_PATTERN.matcher(content);
        int current = 0;
        for (; matcher.find(); current = matcher.end()) {
            String before = content.substring(current, matcher.start());

            // a standalone new line is not interpreted by XWiki 1.0 rendering
            result.append(CleanUtil.removeTrailingNewLines(before, 1, true));

            StringBuffer linkResult = new StringBuffer();
            linkResult.append("[[");

            String str = matcher.group(1);
            if (str != null) {
                str = str.trim();
                String text = null, href = null, target = null;

                // Is there an alias like [alias|link] ?
                int pipeIndex = str.indexOf('|');
                int pipeLength = 1;
                if (pipeIndex == -1) {
                    pipeIndex = str.indexOf('>');
                }
                if (pipeIndex == -1) {
                    pipeIndex = str.indexOf("&gt;");
                    pipeLength = 4;
                }
                if (-1 != pipeIndex) {
                    text = str.substring(0, pipeIndex).trim();
                    str = str.substring(pipeIndex + pipeLength);
                }

                // Is there a target like [alias|link|target] ?
                pipeIndex = str.indexOf('|');
                pipeLength = 1;
                if (pipeIndex == -1) {
                    pipeIndex = str.indexOf('>');
                }
                if (pipeIndex == -1) {
                    pipeIndex = str.indexOf("&gt;");
                    pipeLength = 4;
                }
                if (-1 != pipeIndex) {
                    target = str.substring(pipeIndex + pipeLength).trim();
                    str = str.substring(0, pipeIndex);
                }
                // Done splitting

                // Fill in missing components
                href = str.trim();

                // Done, now print the link
                if (text != null) {
                    linkResult.append(this.escape20Filter.filter(text, filterContext).replace("~", "~~")
                        .replace(">>", "~>~>").replace("||", "~|~|"));
                    linkResult.append(">>");
                }

                // xwiki/1.0 and xwiki/2.0 syntaxes are not using query string and anchor in the same order
                if (!URL_SCHEME_PATTERN.matcher(href).matches()) {
                    int anchorIndex = href.lastIndexOf('#');
                    if (anchorIndex == -1) {
                        anchorIndex = href.length();
                    }
                    int queryStringIndex = href.lastIndexOf('?', anchorIndex - 1);
                    if (queryStringIndex == -1) {
                        queryStringIndex = anchorIndex;
                    }
                    String anchor = href.substring(anchorIndex);
                    String queryString = href.substring(queryStringIndex, anchorIndex);
                    href = href.substring(0, queryStringIndex);

                    linkResult.append(href);
                    linkResult.append(anchor);
                    linkResult.append(queryString);
                } else {
                    linkResult.append(href);
                }

                // Target
                if (target != null) {
                    linkResult.append("||target=");
                    linkResult.append(target);
                }
            }

            linkResult.append("]]");

            result.append(CleanUtil.extractVelocity(linkResult, filterContext, true, true));
        }

        if (current == 0) {
            return content;
        }

        result.append(content.substring(current));

        return result.toString();
    }
}
