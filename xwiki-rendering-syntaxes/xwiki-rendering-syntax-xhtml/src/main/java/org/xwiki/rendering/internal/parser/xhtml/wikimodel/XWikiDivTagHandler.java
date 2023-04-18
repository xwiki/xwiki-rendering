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
import org.xwiki.rendering.wikimodel.xhtml.handler.DivisionTagHandler;
import org.xwiki.rendering.wikimodel.xhtml.impl.TagContext;

/**
 * The div might contain a non generated content metadata which needs a specific processing.
 *
 * @version $Id$
 * @since 10.10RC1
 */
public class XWikiDivTagHandler extends DivisionTagHandler implements XWikiWikiModelHandler
{
    private XWikiMacroHandler xWikiMacroHandler;

    /**
     * Default constructor of a {@link XWikiDivTagHandler}.
     *
     * @param documentClass used by {@link DivisionTagHandler}
     * @param componentManager is used to retrieved the proper parser component for serializing a non generated content
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
        boolean withNonGeneratedContent = this.xWikiMacroHandler.handleBegin(context);

        // we only go through the element if we're not in a macro, or we are in a potentially new content
        if (!withNonGeneratedContent) {
            super.begin(context);

            // in case of beginDocument we use a new stack of parameter, so we need to put in it the
            // NON_GENERATED_CONTENT_STACK value, as it will be popped in end()
            context.getTagStack().pushStackParameter(NON_GENERATED_CONTENT_STACK, false);
        }
    }


    @Override
    protected void end(TagContext context)
    {
        boolean nonGeneratedContent = this.xWikiMacroHandler.handleEnd(context);

        if (!nonGeneratedContent) {
            super.end(context);

            // we still have one non generated content value in the context,
            // we remove it to be consistent
            context.getTagStack().popStackParameter(NON_GENERATED_CONTENT_STACK);
        }
    }
}
