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
package org.xwiki.rendering.internal.parser.xhtml5.wikimodel;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.internal.parser.xhtml.XHTMLParser;
import org.xwiki.rendering.internal.parser.xhtml.wikimodel.XWikiSpanTagHandler;
import org.xwiki.rendering.wikimodel.WikiParameter;
import org.xwiki.rendering.wikimodel.WikiParameters;
import org.xwiki.rendering.wikimodel.xhtml.handler.TeletypeTagHandler;
import org.xwiki.rendering.wikimodel.xhtml.impl.TagContext;

/**
 * XWiki XHTML5 span tag handler. Also handles monospace text.
 *
 * @version $Id$
 * @since 14.0RC1
 */
public class XHTML5SpanTagHandler extends XWikiSpanTagHandler
{

    private static final String CLASS = "class";

    private static final String MONOSPACE = "monospace";

    private final TeletypeTagHandler ttHandler;

    /**
     * Default constructor of a {@link XWikiSpanTagHandler}.
     *
     * @param componentManager is used to retrieved the proper parser component for serializing a non generated content
     * @param parser the current parser is actually used to simplify the build of other parsers.
     * @since 10.10RC1
     */
    public XHTML5SpanTagHandler(ComponentManager componentManager, XHTMLParser parser)
    {
        super(componentManager, parser);
        this.ttHandler = new TeletypeTagHandler();
    }

    @Override
    protected void begin(TagContext context)
    {
        TagContext ttContext = getMonospaceContext(context);
        if (ttContext != null) {
            this.ttHandler.beginElement(ttContext);
        } else {
            super.begin(context);
        }
    }

    @Override
    protected void end(TagContext context)
    {
        TagContext ttContext = getMonospaceContext(context);
        if (ttContext != null) {
            this.ttHandler.endElement(ttContext);
        } else {
            super.end(context);
        }
    }

    private TagContext getMonospaceContext(TagContext context)
    {
        WikiParameter param = context.getParams().getParameter(CLASS);
        if (param != null) {
            List<String> classList = Arrays.asList(param.getValue().split(" "));
            if (classList.contains(MONOSPACE) || classList.contains("wikimodel-verbatim")) {
                WikiParameters ttParams;
                if (MONOSPACE.equals(param.getValue())) {
                    ttParams = context.getParams().remove(CLASS);
                } else {
                    ttParams = context.getParams().setParameter(CLASS,
                        classList.stream().filter(Predicate.isEqual(
                            MONOSPACE).negate()).collect(Collectors.joining(" ")));
                }
                return new TagContext(context.getParentContext(), "tt", ttParams, context.getTagStack());
            }
        }

        return null;
    }
}
