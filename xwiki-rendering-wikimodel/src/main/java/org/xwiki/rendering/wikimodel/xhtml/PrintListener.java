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
package org.xwiki.rendering.wikimodel.xhtml;

import org.xwiki.rendering.wikimodel.IWikiPrinter;
import org.xwiki.rendering.wikimodel.WikiPageUtil;
import org.xwiki.rendering.wikimodel.WikiParameters;

/**
 * @version $Id$
 * @since 4.0M1
 */
public class PrintListener extends PrintInlineListener
{
    /**
     * @param printer
     */
    public PrintListener(IWikiPrinter printer)
    {
        super(printer);
    }

    public PrintListener(IWikiPrinter printer, boolean supportImage, boolean supportDownload)
    {
        super(printer, supportImage, supportDownload);
    }

    @Override
    public void beginDefinitionDescription()
    {
        print("  <dd>");
    }

    @Override
    public void beginDefinitionList(WikiParameters parameters)
    {
        println("<dl>");
    }

    @Override
    public void beginDefinitionTerm()
    {
        print("  <dt>");
    }

    @Override
    public void beginDocument(WikiParameters params)
    {
        println("<div class='wikimodel-document'" + params + ">");
    }

    @Override
    public void beginHeader(int headerLevel, WikiParameters params)
    {
        print("<h" + headerLevel + params + ">");
    }

    @Override
    public void beginInfoBlock(String infoType, WikiParameters params)
    {
        print("<table" + params + "><tr><th>" + infoType + "</th><td>");
    }

    @Override
    public void beginList(WikiParameters parameters, boolean ordered)
    {
        if (ordered) {
            println("<ol" + parameters + ">");
        } else {
            println("<ul" + parameters + ">");
        }
    }

    @Override
    public void beginListItem()
    {
        print("  <li>");
    }

    @Override
    public void beginListItem(WikiParameters params)
    {
        print("  <li" + params + ">");
    }

    @Override
    public void beginParagraph(WikiParameters params)
    {
        print("<p" + params + ">");
    }

    @Override
    public void beginPropertyBlock(String propertyUri, boolean doc)
    {
        print("<div class='wikimodel-property' url='"
            + WikiPageUtil.escapeXmlAttribute(propertyUri)
            + "'>");
        if (doc) {
            println("");
        }
    }

    @Override
    public void beginQuotation(WikiParameters params)
    {
        println("<blockquote" + params + ">");
    }

    @Override
    public void beginQuotationLine()
    {
        // print("<p>");
    }

    @Override
    public void beginTable(WikiParameters params)
    {
        println("<table" + params + "><tbody>");
    }

    @Override
    public void beginTableCell(boolean tableHead, WikiParameters params)
    {
        String str = tableHead ? "<th" : "<td";
        print(str + params + ">");
    }

    @Override
    public void beginTableRow(WikiParameters params)
    {
        print("  <tr" + params + ">");
    }

    @Override
    public void endDefinitionDescription()
    {
        println("</dd>");
    }

    @Override
    public void endDefinitionList(WikiParameters parameters)
    {
        println("</dl>");
    }

    @Override
    public void endDefinitionTerm()
    {
        println("</dt>");
    }

    @Override
    public void endDocument(WikiParameters params)
    {
        println("</div>");
    }

    @Override
    public void endHeader(int headerLevel, WikiParameters params)
    {
        println("</h" + headerLevel + ">");
    }

    @Override
    public void endInfoBlock(String infoType, WikiParameters params)
    {
        println("</td></tr></table>");
    }

    @Override
    public void endList(WikiParameters parameters, boolean ordered)
    {
        if (ordered) {
            println("</ol>");
        } else {
            println("</ul>");
        }
    }

    @Override
    public void endListItem()
    {
        println("</li>");
    }

    @Override
    public void endListItem(WikiParameters params)
    {
        println("</li>");
    }

    @Override
    public void endParagraph(WikiParameters params)
    {
        println("</p>");
    }

    @Override
    public void endPropertyBlock(String propertyUri, boolean doc)
    {
        println("</div>");
    }

    @Override
    public void endQuotation(WikiParameters params)
    {
        println("</blockquote>");
    }

    @Override
    public void endQuotationLine()
    {
        println("");
    }

    @Override
    public void endTable(WikiParameters params)
    {
        println("</tbody></table>");
    }

    @Override
    public void endTableCell(boolean tableHead, WikiParameters params)
    {
        String str = tableHead ? "</th>" : "</td>";
        print(str);
    }

    @Override
    public void endTableRow(WikiParameters params)
    {
        println("</tr>");
    }

    @Override
    public void onEmptyLines(int count)
    {
        if (count > 1) {
            println("<div style='height:" + count + "em;'></div>");
        }
    }

    /**
     * @see PrintInlineListener#onExtensionBlock(java.lang.String,
     *      org.xwiki.rendering.wikimodel.WikiParameters)
     */
    @Override
    public void onExtensionBlock(String extensionName, WikiParameters params)
    {
        println("<div class='wikimodel-extension' extension='"
            + extensionName
            + "'"
            + params
            + "/>");
    }

    @Override
    public void onHorizontalLine(WikiParameters params)
    {
        println("<hr" + params + " />");
    }

    @Override
    public void onMacroBlock(
        String macroName,
        WikiParameters params,
        String content)
    {
        if (content == null) {
            println("<pre class='wikimodel-macro' macroName='"
                + macroName
                + "'"
                + params
                + "/>");
        } else {
            println("<pre class='wikimodel-macro' macroName='"
                + macroName
                + "'"
                + params
                + "><![CDATA["
                + content
                + "]]></pre>");
        }
    }

    @Override
    public void onMacroInline(
        String macroName,
        WikiParameters params,
        String content)
    {
        if (content == null) {
            print("<span class='wikimodel-macro' macroName='"
                + macroName
                + "'"
                + params
                + "/>");
        } else {
            print("<span class='wikimodel-macro' macroName='"
                + macroName
                + "'"
                + params
                + "><![CDATA["
                + content
                + "]]></span>");
        }
    }

    @Override
    public void onTableCaption(String str)
    {
    }

    @Override
    public void onVerbatimBlock(String str, WikiParameters params)
    {
        println("<pre"
            + params
            + ">"
            + WikiPageUtil.escapeXmlString(str)
            + "</pre>");
    }
}
