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
import org.xwiki.rendering.wikimodel.PrintTextListener;
import org.xwiki.rendering.wikimodel.ReferenceHandler;
import org.xwiki.rendering.wikimodel.WikiFormat;
import org.xwiki.rendering.wikimodel.WikiPageUtil;
import org.xwiki.rendering.wikimodel.WikiParameters;
import org.xwiki.rendering.wikimodel.util.WikiEntityUtil;

/**
 * @version $Id$
 * @since 4.0M1
 */
public class PrintInlineListener extends PrintTextListener
{
    /**
     *
     */
    public PrintInlineListener(IWikiPrinter printer)
    {
        super(printer);
    }

    public PrintInlineListener(IWikiPrinter printer, boolean supportImage, boolean supportDownload)
    {
        super(printer, supportImage, supportDownload);
    }

    /**
     * @see org.xwiki.rendering.wikimodel.IWemListener#beginFormat(org.xwiki.rendering.wikimodel.WikiFormat)
     */
    @Override
    public void beginFormat(WikiFormat format)
    {
        print(format.getTags(true));
        if (format.getParams().size() > 0) {
            print("<span class='wikimodel-parameters'" + format.getParams() + ">");
        }
    }

    /**
     * @see org.xwiki.rendering.wikimodel.IWemListener#beginPropertyInline(java.lang.String)
     */
    @Override
    public void beginPropertyInline(String str)
    {
        print("<span class='wikimodel-property' url='"
            + WikiPageUtil.escapeXmlAttribute(str)
            + "'>");
    }

    /**
     * @see org.xwiki.rendering.wikimodel.IWemListener#endFormat(org.xwiki.rendering.wikimodel.WikiFormat)
     */
    @Override
    public void endFormat(WikiFormat format)
    {
        if (format.getParams().size() > 0) {
            print("</span>");
        }
        print(format.getTags(false));
    }

    /**
     * @see org.xwiki.rendering.wikimodel.IWemListener#endPropertyInline(java.lang.String)
     */
    @Override
    public void endPropertyInline(String inlineProperty)
    {
        print("</span>");
    }

    /**
     * Returns an HTML/XML entity corresponding to the specified special symbol.
     * Depending on implementation it can be real entities (like &amp;amp;
     * &amp;lt; &amp;gt; or the corresponding digital codes (like &amp;#38;,
     * &amp;#&amp;#38; or &amp;#8250;). Digital entity representation is better
     * for generation of XML files.
     *
     * @param str the special string to convert to an HTML/XML entity
     * @return an HTML/XML entity corresponding to the specified special symbol.
     */
    protected String getSymbolEntity(String str)
    {
        String entity = null;
        if (isHtmlEntities()) {
            entity = WikiEntityUtil.getHtmlSymbol(str);
        } else {
            int code = WikiEntityUtil.getHtmlCodeByWikiSymbol(str);
            if (code > 0) {
                entity = "#" + Integer.toString(code);
            }
        }
        if (entity != null) {
            entity = "&" + entity + ";";
            if (str.startsWith(" --")) {
                entity = "&#160;" + entity + " ";
            }
        }
        return entity;
    }

    /**
     * Returns <code>true</code> if special Wiki entities should be represented
     * as the corresponding HTML entities or they should be visualized using the
     * corresponding XHTML codes (like &amp;amp; and so on). This method can be
     * overloaded in subclasses to re-define the visualization style.
     *
     * @return <code>true</code> if special Wiki entities should be represented
     *         as the corresponding HTML entities or they should be visualized
     *         using the corresponding XHTML codes (like &amp;amp; and so on).
     */
    protected boolean isHtmlEntities()
    {
        return true;
    }

    @Override
    protected ReferenceHandler newReferenceHandler()
    {
        return new ReferenceHandler(isSupportImage(), isSupportDownload())
        {
            @Override
            protected void handleImage(
                String ref,
                String label,
                WikiParameters params)
            {
                print("<img src='"
                    + WikiPageUtil.escapeXmlAttribute(ref)
                    + "'"
                    + params
                    + "/>");
            }

            @Override
            protected void handleReference(
                String ref,
                String label,
                WikiParameters params)
            {
                print("<a href='"
                    + WikiPageUtil.escapeXmlAttribute(ref)
                    + "'"
                    + params
                    + ">"
                    + WikiPageUtil.escapeXmlString(label)
                    + "</a>");
            }
        };
    }

    /**
     * @see org.xwiki.rendering.wikimodel.IWemListener#onEscape(java.lang.String)
     */
    @Override
    public void onEscape(String str)
    {
        print("<span class='wikimodel-escaped'>"
            + WikiPageUtil.escapeXmlString(str)
            + "</span>");
    }

    @Override
    public void onExtensionInline(String extensionName, WikiParameters params)
    {
        print("<span class='wikimodel-extension' extension='"
            + extensionName
            + "'"
            + params
            + "/>");
    }

    /**
     * @see org.xwiki.rendering.wikimodel.IWemListener#onLineBreak()
     */
    @Override
    public void onLineBreak()
    {
        print("<br />");
    }

    /**
     * @see org.xwiki.rendering.wikimodel.IWemListener#onSpecialSymbol(java.lang.String)
     */
    @Override
    public void onSpecialSymbol(String str)
    {
        String entity = getSymbolEntity(str);
        if (entity == null) {
            entity = WikiPageUtil.escapeXmlString(str);
        }
        print(entity);
    }

    /**
     * @see org.xwiki.rendering.wikimodel.IWemListener#onVerbatimInline(java.lang.String,
     *      WikiParameters)
     */
    @Override
    public void onVerbatimInline(String str, WikiParameters params)
    {
        print("<tt class=\"wikimodel-verbatim\""
            + params
            + ">"
            + WikiPageUtil.escapeXmlString(str)
            + "</tt>");
    }
}
