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
package org.xwiki.rendering.internal.transformation.macro;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.block.RawBlock;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.syntax.SyntaxType;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.rendering.transformation.macro.RawBlockFilter;
import org.xwiki.rendering.transformation.macro.RawBlockFilterParameters;
import org.xwiki.xml.html.HTMLCleaner;
import org.xwiki.xml.html.HTMLCleanerConfiguration;
import org.xwiki.xml.html.HTMLConstants;
import org.xwiki.xml.html.HTMLUtils;

/**
 * HTML filter for raw blocks.
 *
 * @version $Id$
 * @since 14.6RC1
 */
@Component
@Singleton
@Named("html")
public class HTMLRawBlockFilter implements RawBlockFilter
{
    /**
     * To clean the passed HTML.
     */
    @Inject
    private HTMLCleaner htmlCleaner;

    @Override
    public RawBlock filter(RawBlock block, RawBlockFilterParameters parameters) throws MacroExecutionException
    {
        RawBlock result = block;

        boolean restricted =
            parameters.isRestricted() || parameters.getMacroTransformationContext().getTransformationContext()
                .isRestricted();

        if (SyntaxType.HTML_FAMILY_TYPES.contains(block.getSyntax().getType())
            && (parameters.isClean()) || restricted)
        {
            String cleanedContent =
                cleanHTML(block.getRawContent(), parameters.getMacroTransformationContext(), restricted,
                    block.getSyntax());
            result = new RawBlock(cleanedContent, block.getSyntax());
        }

        return result;
    }

    /**
     * Clean the HTML entered by the user, transforming it into valid XHTML.
     *
     * @param content the content to clean
     * @param context the macro transformation context
     * @param restricted if the allowed HTML shall be restricted
     * @param targetSyntax the target syntax
     * @return the cleaned HTML as a string representing valid XHTML
     * @throws MacroExecutionException if the macro is inline and the content is not inline HTML
     */
    private String cleanHTML(String content, MacroTransformationContext context, boolean restricted,
        Syntax targetSyntax)
        throws MacroExecutionException
    {
        String cleanedContent = content;

        HTMLCleanerConfiguration cleanerConfiguration = getCleanerConfiguration(restricted, targetSyntax);

        // Note that we trim the content since we want to be lenient with the user in case he has entered
        // some spaces/newlines before a XML declaration (prolog). Otherwise the XML parser would fail to parse.
        Document document = this.htmlCleaner.clean(new StringReader(cleanedContent), cleanerConfiguration);

        // Since XML can only have a single root node and since we want to allow users to put
        // content such as the following, we need to wrap the content in a root node:
        // <tag1>
        // ..
        // </tag1>
        // <tag2>
        // </tag2>
        // In addition we also need to ensure the XHTML DTD is defined so that valid XHTML entities can be
        // specified.

        // Remove the HTML envelope since this macro is only a fragment of a page which will already have an
        // HTML envelope when rendered. We remove it so that the HTML <head> tag isn't output.
        HTMLUtils.stripHTMLEnvelope(document);

        // If in inline mode verify we have inline HTML content and remove the top level paragraph if there's one
        if (context.isInline()) {
            // TODO: Improve this since when're inside a table cell or a list item we can allow non inline items too
            Element root = document.getDocumentElement();
            if (root.getChildNodes().getLength() == 1 && root.getFirstChild().getNodeType() == Node.ELEMENT_NODE
                && root.getFirstChild().getNodeName().equalsIgnoreCase("p"))
            {
                HTMLUtils.stripFirstElementInside(document, HTMLConstants.TAG_HTML, HTMLConstants.TAG_P);
            } else {
                throw new MacroExecutionException(
                    "When using the HTML macro inline, you can only use inline HTML content."
                        + " Block HTML content (such as tables) cannot be displayed."
                        + " Try leaving an empty line before and after the HTML macro.");
            }
        }

        // Don't print the XML declaration nor the XHTML DocType.
        cleanedContent = HTMLUtils.toString(document, true, true);

        // Don't print the top level html element (which is always present and at the same location
        // since it's been normalized by the HTML cleaner)
        // Note: we trim the first 7 characters since they correspond to a leading new line (generated by
        // XMLUtils.toString() since the doctype is printed on a line by itself followed by a new line) +
        // the 6 chars from "<html>".
        cleanedContent = cleanedContent.substring(7, cleanedContent.length() - 8);

        return cleanedContent;
    }

    /**
     * @param restricted if the allowed HTML shall be restricted
     * @param targetSyntax the target syntax
     * @return the appropriate cleaner configuration.
     */
    private HTMLCleanerConfiguration getCleanerConfiguration(boolean restricted, Syntax targetSyntax)
    {
        HTMLCleanerConfiguration cleanerConfiguration = this.htmlCleaner.getDefaultConfiguration();
        Map<String, String> parameters = new HashMap<>(cleanerConfiguration.getParameters());

        if (Syntax.HTML_5_0.equals(targetSyntax) || Syntax.ANNOTATED_HTML_5_0.equals(targetSyntax)) {
            parameters.put(HTMLCleanerConfiguration.HTML_VERSION, "5");
        }

        if (restricted) {
            parameters.put(HTMLCleanerConfiguration.RESTRICTED, "true");
        }

        cleanerConfiguration.setParameters(parameters);

        return cleanerConfiguration;
    }
}
