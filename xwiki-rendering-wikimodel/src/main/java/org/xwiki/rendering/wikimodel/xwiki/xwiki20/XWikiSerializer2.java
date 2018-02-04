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
package org.xwiki.rendering.wikimodel.xwiki.xwiki20;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Stack;
import java.util.logging.Logger;

import org.xwiki.rendering.wikimodel.IWemConstants;
import org.xwiki.rendering.wikimodel.IWikiPrinter;
import org.xwiki.rendering.wikimodel.PrintTextListener;
import org.xwiki.rendering.wikimodel.ReferenceHandler;
import org.xwiki.rendering.wikimodel.WikiFormat;
import org.xwiki.rendering.wikimodel.WikiPageUtil;
import org.xwiki.rendering.wikimodel.WikiParameter;
import org.xwiki.rendering.wikimodel.WikiParameters;
import org.xwiki.rendering.wikimodel.WikiReference;

/**
 * Serializing XWiki 2.0 syntax ... <br>
 * <br>
 * Implementation hints:
 * <ul>
 * <li>This serializer is only tested with transformed MediaWiki sources</li>
 * <li>To support nested tables, we're using a stack of tables.</li>
 * <li>To get the table caption in front of a table definition, we're using a
 * simple model for, see nested Table class</li>
 * <li>Nested definition lists are NOT supported</li>
 * </ul>
 *
 * @version $Id$
 * @since 4.0M1
 * @see org.xwiki.rendering.wikimodel.xwiki.xwiki10.XWikiSerializer
 */
public class XWikiSerializer2 extends PrintTextListener
{
    private static final String[] HEADERS = {"", "=", "==", "===", "====", "=====", "======"};

    private Deque<ListOrdering> listOrdering = new ArrayDeque<ListOrdering>();

    private Stack<Table> tables = new Stack<Table>();

    private boolean listItemWritten = false;

    private boolean firstTableRow = false;

    private boolean withinTableCell = false;

    private Logger logger;

    public XWikiSerializer2(IWikiPrinter printer)
    {
        super(printer);
        logger = Logger.getLogger(this.getClass().getCanonicalName());
    }

    // ------------------------------------------------------------------------

    @Override
    public void beginHeader(int headerLevel, WikiParameters params)
    {
        println();
        print(HEADERS[headerLevel] + " ");
    }

    @Override
    public void endHeader(int headerLevel, WikiParameters params)
    {
        print(" " + HEADERS[headerLevel]);
        println();
    }

    // ------------------------------------------------------------------------

    @Override
    public void beginList(WikiParameters params, boolean ordered)
    {
        if (ordered) {
            listOrdering.push(ListOrdering.ORDERED);
        } else {
            listOrdering.push(ListOrdering.UNORDERED);
        }
        if (listItemWritten) { // new line for each new list
            println();
        }
    }

    @Override
    public void endList(WikiParameters params, boolean ordered)
    {
        listOrdering.pop();
        listItemWritten = false;
    }

    @Override
    public void beginListItem()
    {
        final ListOrdering[] orders = listOrdering.toArray(new ListOrdering[listOrdering.size()]);
        StringBuilder sb = new StringBuilder();
        boolean isordered = false;
        for (int i = 0; i < orders.length; i++) {
            if (orders[i] == ListOrdering.ORDERED) {
                sb.append('1');
                isordered = true;
            } else {
                sb.append('*');
            }
        }
        if (isordered) {
            sb.append('.');
        }
        sb.append(' ');
        print(sb.toString());
        listItemWritten = true;
    }

    @Override
    public void beginListItem(WikiParameters params)
    {
        beginListItem();
    }

    @Override
    public void endListItem()
    {
        if (listItemWritten) { // don't split related lists
            println();
        }
    }

    @Override
    public void endListItem(WikiParameters params)
    {
        endListItem();
    }

    // ------------------------------------------------------------------------

    @Override
    public void onImage(String ref)
    {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @Override
    public void onImage(WikiReference ref)
    {
        WikiParameters params = ref.getParameters();
        final WikiParameter format = params.getParameter("format");
        if (format != null && "thumb".equals(format.getValue())) {
            onImageThumbnail(ref);
        } else {
            print("[[image:");
            String link = ref.getLink();
            final int dotidx = link.indexOf(':');
            if (dotidx > 0) {
                link = link.substring(dotidx + 1);
            }
            link = clearName(link);
            print(link);
            // add title attribute - the easy way ;-)
            if (ref.getLabel() != null) {
                params = params.addParameter("title", ref.getLabel());
            }
            // put the parameters
            if (params != null && params.getSize() > 0) {
                // print parameters ...
                print("||");
                for (int i = 0, len = params.getSize(); i < len; i++) {
                    print(" ");
                    final WikiParameter param = params.getParameter(i);
                    String val = WikiPageUtil.escapeXmlAttribute(param.getValue());
                    print(param.getKey() + "=\"" + val + "\"");
                }
            }
            print("]]");
        }
    }

    /**
     * Special case when detected, that there are image thumbnail. Thus it's
     * possible to insert something like XWiki "LightBox" macro. (Default
     * implementation will set "width:200px")
     */
    protected void onImageThumbnail(WikiReference ref)
    {
        WikiParameters params = ref.getParameters();
        final WikiParameter oldstyle = params.getParameter("style");
        if (oldstyle != null) {
            // remove existing
            params.remove("style");
            params = params.addParameter("style", oldstyle.getValue() + ";" + "float: right; width:"
                + getImageThumbwidth());
        } else {
            params = params.addParameter("style", "float: right; width:" + getImageThumbwidth());
        }
        params = params.remove("format");
        WikiReference newimgref = new WikiReference(ref.getLink(), ref.getLabel(), params);
        onImage(newimgref);
    }

    // ------------------------------------------------------------------------

    @Override
    public void onVerbatimBlock(String str, WikiParameters params)
    {
        print("{{code language=none}}");
        print(str);
        print("{{/code}}");
    }

    @Override
    public void onVerbatimInline(String str, WikiParameters params)
    {
        println("{{code language=none}}");
        println(str);
        println("{{/code}}");
    }

    // ------------------------------------------------------------------------

    @Override
    public void onTableCaption(String str)
    {
        tables.peek().setCaption(str);
    }

    @Override
    public void beginTable(WikiParameters params)
    {
        Table t = new Table();
        if (params != null && params.getSize() > 0) {
            t.appendText("(% style=\"");
            for (WikiParameter param : params) {
                t.appendText(param.getKey() + ":" + param.getValue() + ";");
            }
            t.appendText("\" %)");
            t.appendText(getEol());
        }
        tables.push(t);
        firstTableRow = true;
    }

    @Override
    public void endTable(WikiParameters params)
    {
        final Table table = tables.pop();
        if (table.getCaption() != null) {
            print("**");
            print(table.getCaption());
            print("**");
            println();
        }
        println(table.getText());
    }

    @Override
    public void beginTableCell(boolean tableHead, WikiParameters params)
    {
        final Table t = tables.peek();
        if (firstTableRow) {
            t.appendText("|= ");
        } else {
            t.appendText("| ");
        }
        withinTableCell = true;
    }

    @Override
    public void endTableCell(boolean tableHead, WikiParameters params)
    {
        final Table t = tables.peek();
        t.appendText(" ");
        withinTableCell = false;
    }

    @Override
    public void beginTableRow(WikiParameters params)
    {
        // nothing
    }

    @Override
    public void endTableRow(WikiParameters params)
    {
        final Table t = tables.peek();
        t.appendText(getEol());
        firstTableRow = false;
    }

    // ------------------------------------------------------------------------

    @Override
    public void beginFormat(WikiFormat format)
    {
        if (format.hasStyle(IWemConstants.STRONG)) {
            print("**");
        } else if (format.hasStyle(IWemConstants.EM)) {
            print("//");
        } else if (format.hasStyle(IWemConstants.MONO)) {
            print("##");
        } else if (format.hasStyle(IWemConstants.TT)) {
            print("##");
        } else {
            super.beginFormat(format);
        }
    }

    @Override
    public void endFormat(WikiFormat format)
    {
        if (format.hasStyle(IWemConstants.TT)) {
            print("##");
        } else if (format.hasStyle(IWemConstants.MONO)) {
            print("##");
        } else if (format.hasStyle(IWemConstants.EM)) {
            print("//");
        } else if (format.hasStyle(IWemConstants.STRONG)) {
            print("**");
        } else {
            super.beginFormat(format);
        }
    }

    // ------------------------------------------------------------------------

    @Override
    public void endParagraph(WikiParameters params)
    {
        println();
        println();
    }

    @Override
    protected void endBlock()
    {
        println();
    }

    protected String getEol()
    {
        return "\n";
    }

    @Override
    public void onLineBreak()
    {
        print("\\\\");
    }

    @Override
    public void onNewLine()
    {
        super.onNewLine();
    }

    @Override
    public void onSpecialSymbol(String str)
    {
        if (withinTableCell && "|".equals(str)) {
            print("~");
        }
        print(str);
    }

    // ------------------------------------------------------------------------

    @Override
    public void beginDefinitionTerm()
    {
        print("; ");
    }

    @Override
    public void endDefinitionTerm()
    {
        println();
    }

    @Override
    public void beginDefinitionDescription()
    {
        print(": ");
    }

    @Override
    public void endDefinitionDescription()
    {
        println();
    }

    /*
     * ========================================================================
     */

    /**
     * @return The wisth information for a thumbnail image
     */
    protected String getImageThumbwidth()
    {
        return "200px";
    }

    @Override
    protected void print(String str)
    {
        if (!tables.isEmpty()) {
            tables.peek().appendText(str);
        } else {
            super.print(str);
        }
    }

    @Override
    protected void println()
    {
        if (!tables.isEmpty()) {
            tables.peek().appendText(getEol());
        } else {
            super.println();
        }
    }

    @Override
    protected void println(String str)
    {
        if (!tables.isEmpty()) {
            tables.peek().appendText(str + getEol());
        } else {
            super.println(str);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see PrintTextListener#newReferenceHandler()
     */
    @Override
    protected ReferenceHandler newReferenceHandler()
    {
        return new XWiki2ReferenzeHandler();
    }

    /*
     * ========================================================================
     */

    /**
     * @return the logger
     */
    public Logger getLogger()
    {
        return logger;
    }

    /**
     * @param logger the logger to set
     */
    public void setLogger(Logger logger)
    {
        this.logger = logger;
    }

    /**
     * @see #clearName(String)
     */
    public final static String clearName(String name, boolean stripDots, boolean ascii)
    {
        String temp = name;
        temp = temp.replaceAll(
            "[\u00c0\u00c1\u00c2\u00c3\u00c4\u00c5\u0100\u0102\u0104\u01cd\u01de\u01e0\u01fa\u0200\u0202\u0226]", "A");
        temp = temp.replaceAll(
            "[\u00e0\u00e1\u00e2\u00e3\u00e4\u00e5\u0101\u0103\u0105\u01ce\u01df\u01e1\u01fb\u0201\u0203\u0227]", "a");
        temp = temp.replaceAll("[\u00c6\u01e2\u01fc]", "AE");
        temp = temp.replaceAll("[\u00e6\u01e3\u01fd]", "ae");
        temp = temp.replaceAll("[\u008c\u0152]", "OE");
        temp = temp.replaceAll("[\u009c\u0153]", "oe");
        temp = temp.replaceAll("[\u00c7\u0106\u0108\u010a\u010c]", "C");
        temp = temp.replaceAll("[\u00e7\u0107\u0109\u010b\u010d]", "c");
        temp = temp.replaceAll("[\u00d0\u010e\u0110]", "D");
        temp = temp.replaceAll("[\u00f0\u010f\u0111]", "d");
        temp = temp.replaceAll("[\u00c8\u00c9\u00ca\u00cb\u0112\u0114\u0116\u0118\u011a\u0204\u0206\u0228]", "E");
        temp = temp.replaceAll("[\u00e8\u00e9\u00ea\u00eb\u0113\u0115\u0117\u0119\u011b\u01dd\u0205\u0207\u0229]", "e");
        temp = temp.replaceAll("[\u011c\u011e\u0120\u0122\u01e4\u01e6\u01f4]", "G");
        temp = temp.replaceAll("[\u011d\u011f\u0121\u0123\u01e5\u01e7\u01f5]", "g");
        temp = temp.replaceAll("[\u0124\u0126\u021e]", "H");
        temp = temp.replaceAll("[\u0125\u0127\u021f]", "h");
        temp = temp.replaceAll("[\u00cc\u00cd\u00ce\u00cf\u0128\u012a\u012c\u012e\u0130\u01cf\u0208\u020a]", "I");
        temp = temp.replaceAll("[\u00ec\u00ed\u00ee\u00ef\u0129\u012b\u012d\u012f\u0131\u01d0\u0209\u020b]", "i");
        temp = temp.replaceAll("[\u0132]", "IJ");
        temp = temp.replaceAll("[\u0133]", "ij");
        temp = temp.replaceAll("[\u0134]", "J");
        temp = temp.replaceAll("[\u0135]", "j");
        temp = temp.replaceAll("[\u0136\u01e8]", "K");
        temp = temp.replaceAll("[\u0137\u0138\u01e9]", "k");
        temp = temp.replaceAll("[\u0139\u013b\u013d\u013f\u0141]", "L");
        temp = temp.replaceAll("[\u013a\u013c\u013e\u0140\u0142\u0234]", "l");
        temp = temp.replaceAll("[\u00d1\u0143\u0145\u0147\u014a\u01f8]", "N");
        temp = temp.replaceAll("[\u00f1\u0144\u0146\u0148\u0149\u014b\u01f9\u0235]", "n");
        temp = temp.replaceAll(
            "[\u00d2\u00d3\u00d4\u00d5\u00d6\u00d8\u014c\u014e\u0150\u01d1\u01ea\u01ec\u01fe\u020c\u020e\u022a\u022c"
                + "\u022e\u0230]", "O");
        temp = temp.replaceAll(
            "[\u00f2\u00f3\u00f4\u00f5\u00f6\u00f8\u014d\u014f\u0151\u01d2\u01eb\u01ed\u01ff\u020d\u020f\u022b\u022d"
                + "\u022f\u0231]", "o");
        temp = temp.replaceAll("[\u0156\u0158\u0210\u0212]", "R");
        temp = temp.replaceAll("[\u0157\u0159\u0211\u0213]", "r");
        temp = temp.replaceAll("[\u015a\u015c\u015e\u0160\u0218]", "S");
        temp = temp.replaceAll("[\u015b\u015d\u015f\u0161\u0219]", "s");
        temp = temp.replaceAll("[\u00de\u0162\u0164\u0166\u021a]", "T");
        temp = temp.replaceAll("[\u00fe\u0163\u0165\u0167\u021b\u0236]", "t");
        temp = temp.replaceAll(
            "[\u00d9\u00da\u00db\u00dc\u0168\u016a\u016c\u016e\u0170\u0172\u01d3\u01d5\u01d7\u01d9\u01db\u0214\u0216]",
            "U");
        temp = temp.replaceAll(
            "[\u00f9\u00fa\u00fb\u00fc\u0169\u016b\u016d\u016f\u0171\u0173\u01d4\u01d6\u01d8\u01da\u01dc\u0215\u0217]",
            "u");
        temp = temp.replaceAll("[\u0174]", "W");
        temp = temp.replaceAll("[\u0175]", "w");
        temp = temp.replaceAll("[\u00dd\u0176\u0178\u0232]", "Y");
        temp = temp.replaceAll("[\u00fd\u00ff\u0177\u0233]", "y");
        temp = temp.replaceAll("[\u0179\u017b\u017d]", "Z");
        temp = temp.replaceAll("[\u017a\u017c\u017e]", "z");
        temp = temp.replaceAll("[\u00df]", "SS");
        temp = temp.replaceAll("[_':,;\\\\/]", " ");
        name = temp;
        name = name.replaceAll("\\s+", "");
        name = name.replaceAll("[\\(\\)]", " ");

        if (stripDots) {
            name = name.replaceAll("[\\.]", "");
        }

        if (ascii) {
            name = name.replaceAll("[^a-zA-Z0-9\\-_\\.]", "");
        }

        if (name.length() > 250) {
            name = name.substring(0, 250);
        }

        return name;
    }

    /**
     * Clears the name of files; used while uploading attachments within XWiki
     *
     * RECOMMENDED FOR NAMES OF UPLOADED FILES. (boolean stripDots = false;
     * boolean ascii = true;)
     */
    public final static String clearName(String name)
    {
        boolean stripDots = false;
        boolean ascii = true;
        return clearName(name, stripDots, ascii);
    }

    /*
     * ========================================================================
     */
    private enum ListOrdering
    {
        ORDERED, UNORDERED
    }

    /*
     * ========================================================================
     */

    private class XWiki2ReferenzeHandler extends ReferenceHandler
    {
        XWiki2ReferenzeHandler()
        {
            this(false, false);
        }

        protected XWiki2ReferenzeHandler(boolean supportImage, boolean supportDownload)
        {
            super(supportImage, supportDownload);
        }

        @Override
        protected void handleImage(String ref, String label, WikiParameters params)
        {
            handleReference("image:" + ref, label, params); // TODO: testing ...
        }

        @Override
        protected void handleReference(String ref, String label, WikiParameters params)
        {
            print("[[");
            if (label != null) {
                print(label + ">>");
            }
            print(ref);
            print("]]");
        }
    }

    /*
     * ========================================================================
     */

    /**
     * Workaround to put a caption in front of a table.
     */
    private static class Table
    {
        private StringBuilder text = new StringBuilder();

        private String caption;

        /**
         * @param caption the caption to set
         */
        public void setCaption(String caption)
        {
            this.caption = caption;
        }

        /**
         * @see java.lang.StringBuilder#append(java.lang.CharSequence)
         */
        public StringBuilder appendText(CharSequence s)
        {
            return text.append(s);
        }

        /**
         * @see java.lang.StringBuilder#toString()
         */
        public String getText()
        {
            return text.toString();
        }

        /**
         * @return the caption
         */
        public String getCaption()
        {
            return caption;
        }
    }
}
