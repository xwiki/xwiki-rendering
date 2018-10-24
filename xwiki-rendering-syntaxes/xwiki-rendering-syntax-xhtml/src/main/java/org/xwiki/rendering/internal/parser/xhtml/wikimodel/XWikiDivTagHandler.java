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
import org.xwiki.rendering.wikimodel.WikiParameters;
import org.xwiki.rendering.wikimodel.xhtml.handler.DivisionTagHandler;
import org.xwiki.rendering.wikimodel.xhtml.impl.TagContext;
import org.xwiki.stability.Unstable;

/**
 * The div might contain an unchanged content metadata which needs a specific processing.
 *
 * @version $Id$
 * @since 10.9
 */
@Unstable
public class XWikiDivTagHandler extends DivisionTagHandler implements XWikiWikiModelHandler
{
    private XWikiMacroHandler xWikiMacroHandler;

    /**
     * Default constructor of a {@link XWikiDivTagHandler}.
     *
     * @param documentClass used by {@link DivisionTagHandler}
     * @param componentManager is used to retrieved the proper parser component for serializing an unchanged content
     * @param parser the current parser is actually used to simplify the build of other parsers.
     */
    public XWikiDivTagHandler(String documentClass, ComponentManager componentManager, XHTMLParser parser)
    {
        super(documentClass);
        this.xWikiMacroHandler = new XWikiMacroHandler(componentManager, parser);
    }

    @Override
    protected void begin(TagContext context)
    {
        WikiParameters params = context.getParams();
        boolean withUnchangedContent = this.xWikiMacroHandler.handleBegin(context);

        // we only go through the element if we're not in a macro, or we are in a potentially new content
        if (!withUnchangedContent) {
            super.begin(context);
        }

        context.getTagStack().pushStackParameter(UNCHANGED_CONTENT_STACK, withUnchangedContent);
    }


    @Override
    protected void end(TagContext context)
    {
        boolean unchangedContent = this.xWikiMacroHandler.handleEnd(context);

        if (!unchangedContent) {
            super.end(context);
        }
    }
}
