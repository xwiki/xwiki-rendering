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
package org.xwiki.rendering.internal.parser.xhtml.wikimodel;

import java.util.Objects;

import org.xwiki.rendering.wikimodel.xhtml.handler.TagHandler;
import org.xwiki.rendering.wikimodel.xhtml.impl.IgnoreElementRule;
import org.xwiki.rendering.wikimodel.xhtml.impl.TagContext;
import org.xwiki.rendering.wikimodel.xhtml.impl.TagStack;

import static org.xwiki.xml.html.HTMLConstants.TAG_HEAD;

/**
 * Handler for the head tag. Ignore all the content of the element.
 *
 * @version $Id$
 * @since 15.6RC1
 * @since 15.5.1
 * @since 14.10.14
 */
public class XWikiHeadTagHandler extends TagHandler
{
    /**
     * Default constructor.
     */
    public XWikiHeadTagHandler()
    {
        super(false);
    }

    @Override
    protected void begin(TagContext context)
    {
        TagStack tagStack = context.getTagStack();
        tagStack.pushIgnoreElementRule(new IgnoreElementRule(ignoreElementRule -> {
            TagContext tagContext = ignoreElementRule.getTagContext();
            // Pops itself off the stack when the head element ends.
            if (Objects.equals(tagContext.getName(), TAG_HEAD)) {
                tagContext.getTagStack().popIgnoreElementRule();
            }
            return false;
        }, true));
    }
}
