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

/**
 * Handle any tag for which the content should be ignored during parsing.
 *
 * @version $Id$
 * @since 15.6RC1
 * @since 15.5.1
 * @since 14.10.14
 */
public class XWikiIgnoredTagHandler extends TagHandler
{
    private final String tag;

    /**
     * Default constructor.
     *
     * @param tag name of the tag to ignore
     */
    public XWikiIgnoredTagHandler(String tag)
    {
        super(false);
        this.tag = tag;
    }

    @Override
    protected void begin(TagContext context)
    {
        TagStack tagStack = context.getTagStack();
        // Push the ignored element rule for the outermost element.
        tagStack.pushIgnoreElementRule(getIgnoreElementRule());
    }

    private IgnoreElementRule getIgnoreElementRule()
    {
        return new IgnoreElementRule(ignoreElementRule -> {
            TagContext tagContext = ignoreElementRule.getTagContext();
            boolean isExpectedTag = Objects.equals(tagContext.getName(), this.tag);
            if (isExpectedTag) {
                TagStack tagStack = tagContext.getTagStack();
                if (ignoreElementRule.isBeginElement()) {
                    // Re-push the ignored element rule for nested opening tags of the expected tag. 
                    tagStack.pushIgnoreElementRule(getIgnoreElementRule());
                } else {
                    // Pop the ignored element rule for closing tags of the expected tag.
                    tagStack.popIgnoreElementRule();
                }
            }
            return false;
        }, true);
    }
}
