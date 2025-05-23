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

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.internal.parser.xhtml.XHTMLParser;
import org.xwiki.rendering.wikimodel.WikiParameter;
import org.xwiki.rendering.wikimodel.WikiParameters;
import org.xwiki.rendering.wikimodel.xhtml.handler.SpanTagHandler;
import org.xwiki.rendering.wikimodel.xhtml.impl.TagContext;

import static java.util.function.Predicate.not;

/**
 * Handle XWiki span elements (we need to ensure we skip link content when we generate them and also skip the spans for
 * the "?" for links pointing to non existing pages).
 *
 * @version $Id$
 * @since 1.7M1
 */
public class XWikiSpanTagHandler extends SpanTagHandler implements XWikiWikiModelHandler
{
    private static final Set<String> IGNORED_CLASSES = Set.of(
        "wikilink",
        "wikiinternallink",
        "wikicreatelink",
        "wikiexternallink",
        "wikiattachmentlink"
    );

    private static final String CLASS_ATTRIBUTE = "class";

    private static final String WIKI_GENERATED_LINK_CONTENT = "wikigeneratedlinkcontent";

    private static final String XWIKI_RENDERING_ERROR = "xwikirenderingerror";

    private XWikiMacroHandler xWikiMacroHandler;

    /**
     * Default constructor of a {@link XWikiSpanTagHandler}.
     *
     * @param componentManager is used to retrieved the proper parser component for serializing a non generated content
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
        boolean withNonGeneratedContent = this.xWikiMacroHandler.handleBegin(context);

        // we only go through the element if we're not in a macro, or we are in a potentially new content
        if (!withNonGeneratedContent) {
            // If we're on a span for unknown links then skip the event for its content.
            // Ex: <a href="..."><span class="wikicreatelinktext">...</span><span class="wikicreatelinkqm">?</span></a>
            WikiParameter classParam = params.getParameter(CLASS_ATTRIBUTE);
            if (classParam != null) {
                String classParamValue = classParam.getValue();

                String filteredClassNames = filterIgnoredClasses(classParamValue);
                if (classParamValue.contains(WIKI_GENERATED_LINK_CONTENT)
                    || XWIKI_RENDERING_ERROR.equals(classParamValue))
                {
                    setAccumulateContent(true);
                } else if (filteredClassNames.equals(classParamValue)) {
                    // Nothing removed means not an ignored class.
                    super.begin(context);
                } else if (!filteredClassNames.isEmpty()) {
                    // There was something removed, but the class still isn't empty - most likely due to some bug, an
                    // intended class was combined with an ignored class.
                    // Keep the filtered class.
                    super.begin(updateContext(context, filteredClassNames));
                }
            } else {
                super.begin(context);
            }
        }
    }

    @Override
    protected void end(TagContext context)
    {
        boolean nonGeneratedContent = this.xWikiMacroHandler.handleEnd(context);

        if (!nonGeneratedContent) {
            WikiParameter classParam = context.getParams().getParameter(CLASS_ATTRIBUTE);
            if (classParam != null) {
                String classParamValue = classParam.getValue();

                String filteredClassNames = filterIgnoredClasses(classParamValue);
                if (classParamValue.contains(WIKI_GENERATED_LINK_CONTENT)
                    || XWIKI_RENDERING_ERROR.equals(classParamValue))
                {
                    setAccumulateContent(false);
                } else if (filteredClassNames.equals(classParamValue)) {
                    // Nothing removed means not an ignored class.
                    super.end(context);
                } else if (!filteredClassNames.isEmpty()) {
                    // There was something removed, but the class still isn't empty - most likely due to some bug, an
                    // intended class was combined with an ignored class.
                    // Keep the filtered class.
                    super.end(updateContext(context, filteredClassNames));
                }
            } else {
                super.end(context);
            }
        }
    }

    private static TagContext updateContext(TagContext context, String filteredClassNames)
    {
        // We cannot update parameters or context, so create a new context.
        WikiParameters updatedParameters = context.getParams().setParameter(CLASS_ATTRIBUTE, filteredClassNames);
        return new TagContext(context.getParentContext(), context.getName(), updatedParameters, context.getTagStack());
    }

    private static String filterIgnoredClasses(String classParamValue)
    {
        String[] classes = StringUtils.split(classParamValue);
        String filteredClassNames;
        if (Arrays.stream(classes).anyMatch(IGNORED_CLASSES::contains)) {
            filteredClassNames = Arrays.stream(classes)
                .filter(not(IGNORED_CLASSES::contains))
                .collect(Collectors.joining(" "));
        } else {
            // Keep the exact original string such that it is easy to check if anything has been removed and to avoid
            // modifications that could, e.g., lead to non-empty diffs even though nothing has been changed.
            filteredClassNames = classParamValue;
        }
        return filteredClassNames;
    }
}
