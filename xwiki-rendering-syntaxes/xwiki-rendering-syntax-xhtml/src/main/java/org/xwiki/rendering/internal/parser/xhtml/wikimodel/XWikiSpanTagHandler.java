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

import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.internal.parser.xhtml.XHTMLParser;
import org.xwiki.rendering.wikimodel.WikiParameter;
import org.xwiki.rendering.wikimodel.WikiParameters;
import org.xwiki.rendering.wikimodel.xhtml.handler.SpanTagHandler;
import org.xwiki.rendering.wikimodel.xhtml.impl.TagContext;

/**
 * Handle XWiki span elements (we need to ensure we skip link content when we generate them and also skip the spans for
 * the "?" for links pointing to non existing pages).
 *
 * @version $Id$
 * @since 1.7M1
 */
public class XWikiSpanTagHandler extends SpanTagHandler implements XWikiWikiModelHandler
{

    private XWikiMacroHandler xWikiMacroHandler;

    /**
     * Default constructor of a {@link XWikiSpanTagHandler}.
     *
     * @param componentManager is used to retrieved the proper parser component for serializing an unchanged content
     * @param parser the current parser is actually used to simplify the build of other parsers.
     * @since 10.10RC1
     */
    public XWikiSpanTagHandler(ComponentManager componentManager, XHTMLParser parser)
    {
        this.xWikiMacroHandler = new XWikiMacroHandler(componentManager, parser);
    }

    @Override
    protected void begin(TagContext context)
    {
        WikiParameters params = context.getParams();
        boolean withUnchangedContent = this.xWikiMacroHandler.handleBegin(context);

        // we only go through the element if we're not in a macro, or we are in a potentially new content
        if (!withUnchangedContent) {
            // If we're on a span for unknown links then skip the event for its content.
            // Ex: <a href="..."><span class="wikicreatelinktext">...</span><span class="wikicreatelinkqm">?</span></a>
            WikiParameter classParam = params.getParameter("class");
            if (classParam != null) {
                if (classParam.getValue().contains("wikigeneratedlinkcontent"))
                {
                    setAccumulateContent(true);
                } else if ("wikilink".equals(classParam.getValue())
                    || "wikicreatelink".equals(classParam.getValue())
                    || "wikiexternallink".equals(classParam.getValue()))
                {
                    // Nothing to do
                } else if ("xwikirenderingerror".equals(classParam.getValue())) {
                    setAccumulateContent(true);
                } else {
                    super.begin(context);
                }
            } else {
                super.begin(context);
            }
        }
    }

    @Override
    protected void end(TagContext context)
    {
        boolean unchangedContent = this.xWikiMacroHandler.handleEnd(context);

        if (!unchangedContent) {
            WikiParameter classParam = context.getParams().getParameter("class");
            if (classParam != null) {
                if (classParam.getValue().contains("wikigeneratedlinkcontent"))
                {
                    setAccumulateContent(false);
                } else if ("wikilink".equals(classParam.getValue())
                    || "wikicreatelink".equals(classParam.getValue())
                    || "wikiexternallink".equals(classParam.getValue()))
                {
                    // Nothing to do
                } else if ("xwikirenderingerror".equals(classParam.getValue())) {
                    setAccumulateContent(false);
                } else {
                    super.end(context);
                }
            } else {
                super.end(context);
            }
        }
    }
}
