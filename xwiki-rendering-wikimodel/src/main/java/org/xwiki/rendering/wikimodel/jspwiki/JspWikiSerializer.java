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
package org.xwiki.rendering.wikimodel.jspwiki;

import org.xwiki.rendering.wikimodel.IWemListener;
import org.xwiki.rendering.wikimodel.WikiFormat;
import org.xwiki.rendering.wikimodel.WikiParameters;
import org.xwiki.rendering.wikimodel.WikiReference;

/**
 * Not ready yet.
 *
 * @version $Id$
 * @since 4.0M1
 */
public class JspWikiSerializer implements IWemListener
{
    private StringBuffer fBuffer;

    public JspWikiSerializer()
    {

    }

    public JspWikiSerializer(StringBuffer buf)
    {
        fBuffer = buf;
    }

    public void beginDefinitionDescription()
    {
        // TODO Auto-generated method stub

    }

    public void beginDefinitionList(WikiParameters params)
    {
        // TODO Auto-generated method stub

    }

    public void beginDefinitionTerm()
    {
        // TODO Auto-generated method stub

    }

    public void beginDocument()
    {
        // TODO Auto-generated method stub

    }

    public void beginDocument(WikiParameters params)
    {
        // TODO Auto-generated method stub

    }

    public void beginFormat(WikiFormat format)
    {
        // TODO Auto-generated method stub

    }

    public void beginHeader(int headerLevel, WikiParameters params)
    {
        println(getEol());
        for (int i = 0; i < headerLevel; i++) {
            print("!");
        }
        print(" ");
    }

    public void beginInfoBlock(String infoType, WikiParameters params)
    {
        // TODO Auto-generated method stub

    }

    public void beginList(WikiParameters params, boolean ordered)
    {
        // TODO Auto-generated method stub

    }

    public void beginListItem()
    {
        print("* ");
    }

    @Override
    public void beginListItem(WikiParameters params)
    {
        beginListItem();
    }

    public void beginParagraph(WikiParameters params)
    {
        // TODO Auto-generated method stub

    }

    public void beginPropertyBlock(String propertyUri, boolean doc)
    {
        // TODO Auto-generated method stub

    }

    public void beginPropertyInline(String str)
    {
        // TODO Auto-generated method stub

    }

    public void beginQuotation(WikiParameters params)
    {
        // TODO Auto-generated method stub

    }

    public void beginQuotationLine()
    {
        // TODO Auto-generated method stub

    }

    public void beginSection(
        int docLevel,
        int headerLevel,
        WikiParameters params)
    {
        // TODO Auto-generated method stub

    }

    public void beginSectionContent(
        int docLevel,
        int headerLevel,
        WikiParameters params)
    {
        // TODO Auto-generated method stub

    }

    public void beginTable(WikiParameters params)
    {
    }

    public void beginTableCell(boolean tableHead, WikiParameters params)
    {
        if (tableHead) {
            print("||");
        } else {
            print("|");
        }
    }

    public void beginTableRow(WikiParameters params)
    {
        println("");
    }

    public void endDefinitionDescription()
    {
        // TODO Auto-generated method stub

    }

    public void endDefinitionList(WikiParameters params)
    {
        // TODO Auto-generated method stub

    }

    public void endDefinitionTerm()
    {
        // TODO Auto-generated method stub

    }

    public void endDocument()
    {
        // TODO Auto-generated method stub

    }

    public void endDocument(WikiParameters params)
    {
        // TODO Auto-generated method stub

    }

    public void endFormat(WikiFormat format)
    {
        // TODO Auto-generated method stub

    }

    public void endHeader(int headerLevel, WikiParameters params)
    {
        println("");
        println("");
    }

    public void endInfoBlock(String infoType, WikiParameters params)
    {
        // TODO Auto-generated method stub

    }

    public void endList(WikiParameters params, boolean ordered)
    {
        // TODO Auto-generated method stub

    }

    public void endListItem()
    {
        println("");
    }

    @Override
    public void endListItem(WikiParameters params)
    {
        println("");
    }

    public void endParagraph(WikiParameters params)
    {
        // TODO Auto-generated method stub

    }

    public void endPropertyBlock(String propertyUri, boolean doc)
    {
        // TODO Auto-generated method stub

    }

    public void endPropertyInline(String inlineProperty)
    {
        // TODO Auto-generated method stub

    }

    public void endQuotation(WikiParameters params)
    {
        // TODO Auto-generated method stub

    }

    public void endQuotationLine()
    {
        // TODO Auto-generated method stub

    }

    public void endSection(int docLevel, int headerLevel, WikiParameters params)
    {
        // TODO Auto-generated method stub

    }

    public void endSectionContent(
        int docLevel,
        int headerLevel,
        WikiParameters params)
    {
        // TODO Auto-generated method stub

    }

    public void endTable(WikiParameters params)
    {
    }

    public void endTableCell(boolean tableHead, WikiParameters params)
    {
        if (tableHead) {
            print("||");
        } else {
            print("|");
        }
    }

    public void endTableRow(WikiParameters params)
    {
        println("");
    }

    protected String getEol()
    {
        return "\n";
    }

    public void onEmptyLines(int count)
    {
        // TODO Auto-generated method stub

    }

    public void onEscape(String str)
    {
        // TODO Auto-generated method stub

    }

    public void onExtensionBlock(String extensionName, WikiParameters params)
    {
        // TODO Auto-generated method stub

    }

    public void onExtensionInline(String extensionName, WikiParameters params)
    {
        // TODO Auto-generated method stub

    }

    public void onHorizontalLine(WikiParameters params)
    {
        println("----");
    }

    public void onImage(String ref)
    {
        // TODO Auto-generated method stub

    }

    public void onImage(WikiReference ref)
    {
        // TODO Auto-generated method stub

    }

    public void onLineBreak()
    {
        println("");
        println("");
    }

    public void onMacro(String macroName, WikiParameters params, String content)
    {
        // TODO Auto-generated method stub

    }

    public void onMacroBlock(
        String macroName,
        WikiParameters params,
        String content)
    {
        // TODO Auto-generated method stub

    }

    public void onMacroInline(
        String macroName,
        WikiParameters params,
        String content)
    {
        // TODO Auto-generated method stub

    }

    public void onNewLine()
    {
        println("");
    }

    public void onReference(String ref)
    {
        onReference(ref, null);
    }

    private void onReference(String link, String label)
    {
        link = link.replaceAll(" ", "_");
        if (link.indexOf("Image") == 0) {
            print("{image" + link.substring(5) + "}");
        } else {
            if (label != null) {
                print("[" + label + ">" + link + "]");
            } else {
                print("[" + link + "]");
            }
        }
    }

    public void onReference(WikiReference ref)
    {
        onReference(ref.getLink(), ref.getLabel());
    }

    public void onSpace(String str)
    {
        print(str);
    }

    public void onSpecialSymbol(String str)
    {
        print(str);
    }

    public void onTableCaption(String str)
    {
        println(str);
    }

    public void onVerbatimBlock(String str, WikiParameters params)
    {
        print("{{{" + str + "}}}");
    }

    public void onVerbatimInline(String str, WikiParameters params)
    {
        println("{{{" + str + "}}}");
    }

    public void onWord(String str)
    {
        print(str);
    }

    protected void print(String str)
    {
        if (fBuffer != null) {
            fBuffer.append(str);
        } else {
            System.out.print(str);
        }
    }

    protected void println(String str)
    {
        if (fBuffer != null) {
            fBuffer.append(str);
            String eol = getEol();
            fBuffer.append(eol);
        } else {
            System.out.println(str);
        }
    }
}
