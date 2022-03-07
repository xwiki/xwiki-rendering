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
package org.xwiki.rendering.listener.chaining;

import java.util.Map;

import org.xwiki.rendering.listener.Format;
import org.xwiki.rendering.listener.HeaderLevel;
import org.xwiki.rendering.listener.ListType;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.syntax.Syntax;

/**
 * Stores event types and offers a way to call a stored event.
 *
 * @version $Id$
 * @since 1.8RC1
 */
public enum EventType
{
    /**
     * @see Listener#beginDocument(org.xwiki.rendering.listener.MetaData)
     */
    BEGIN_DOCUMENT {
        @Override
        public void fireEvent(Listener listener, Object... eventParameters)
        {
            listener.beginDocument((MetaData) eventParameters[0]);
        }
    },
    /**
     * @see Listener#endDocument(org.xwiki.rendering.listener.MetaData)
     */
    END_DOCUMENT {
        @Override
        public void fireEvent(Listener listener, Object... eventParameters)
        {
            listener.endDocument((MetaData) eventParameters[0]);
        }
    },
    /**
     * @see Listener#beginGroup(java.util.Map)
     */
    BEGIN_GROUP {
        @Override
        public void fireEvent(Listener listener, Object... eventParameters)
        {
            listener.beginGroup((Map<String, String>) eventParameters[0]);
        }
    },
    /**
     * @see Listener#endGroup(java.util.Map)
     */
    END_GROUP {
        @Override
        public void fireEvent(Listener listener, Object... eventParameters)
        {
            listener.endGroup((Map<String, String>) eventParameters[0]);
        }

        @Override
        public boolean isInlineEnd()
        {
            return true;
        }
    },
    /**
     * @see Listener#beginParagraph(java.util.Map)
     */
    BEGIN_PARAGRAPH {
        @Override
        public void fireEvent(Listener listener, Object... eventParameters)
        {
            listener.beginParagraph((Map<String, String>) eventParameters[0]);
        }
    },
    /**
     * @see Listener#endParagraph(java.util.Map)
     */
    END_PARAGRAPH {
        @Override
        public void fireEvent(Listener listener, Object... eventParameters)
        {
            listener.endParagraph((Map<String, String>) eventParameters[0]);
        }

        @Override
        public boolean isInlineEnd()
        {
            return true;
        }
    },
    /**
     * @see Listener#beginDefinitionList(java.util.Map)
     */
    BEGIN_DEFINITION_LIST {
        @Override
        public void fireEvent(Listener listener, Object... eventParameters)
        {
            listener.beginDefinitionList((Map<String, String>) eventParameters[0]);
        }

        @Override
        public boolean isInlineEnd()
        {
            // This is because for nested definition lists, the event after a definition list item content is a new
            // definition list
            return true;
        }
    },
    /**
     * @see Listener#endDefinitionList(java.util.Map)
     */
    END_DEFINITION_LIST {
        @Override
        public void fireEvent(Listener listener, Object... eventParameters)
        {
            listener.endDefinitionList((Map<String, String>) eventParameters[0]);
        }
    },
    /**
     * @see Listener#beginDefinitionTerm()
     */
    BEGIN_DEFINITION_TERM {
        @Override
        public void fireEvent(Listener listener, Object... eventParameters)
        {
            listener.beginDefinitionTerm();
        }
    },
    /**
     * @see Listener#endDefinitionTerm()
     */
    END_DEFINITION_TERM {
        @Override
        public void fireEvent(Listener listener, Object... eventParameters)
        {
            listener.endDefinitionTerm();
        }

        @Override
        public boolean isInlineEnd()
        {
            return true;
        }
    },
    /**
     * @see Listener#beginDefinitionDescription()
     */
    BEGIN_DEFINITION_DESCRIPTION {
        @Override
        public void fireEvent(Listener listener, Object... eventParameters)
        {
            listener.beginDefinitionDescription();
        }
    },
    /**
     * @see Listener#endDefinitionDescription()
     */
    END_DEFINITION_DESCRIPTION {
        @Override
        public void fireEvent(Listener listener, Object... eventParameters)
        {
            listener.endDefinitionDescription();
        }

        @Override
        public boolean isInlineEnd()
        {
            return true;
        }
    },
    /**
     * @see Listener#beginFormat(org.xwiki.rendering.listener.Format, java.util.Map)
     */
    BEGIN_FORMAT {
        @Override
        public void fireEvent(Listener listener, Object... eventParameters)
        {
            listener.beginFormat((Format) eventParameters[0], (Map<String, String>) eventParameters[1]);
        }
    },
    /**
     * @see Listener#endFormat(org.xwiki.rendering.listener.Format, java.util.Map)
     */
    END_FORMAT {
        @Override
        public void fireEvent(Listener listener, Object... eventParameters)
        {
            listener.endFormat((Format) eventParameters[0], (Map<String, String>) eventParameters[1]);
        }
    },
    /**
     * @see Listener#beginHeader(org.xwiki.rendering.listener.HeaderLevel, String, java.util.Map)
     */
    BEGIN_HEADER {
        @Override
        public void fireEvent(Listener listener, Object... eventParameters)
        {
            listener.beginHeader((HeaderLevel) eventParameters[0], (String) eventParameters[1],
                (Map<String, String>) eventParameters[2]);
        }
    },
    /**
     * @see Listener#endHeader(org.xwiki.rendering.listener.HeaderLevel, String, java.util.Map)
     */
    END_HEADER {
        @Override
        public void fireEvent(Listener listener, Object... eventParameters)
        {
            listener.endHeader((HeaderLevel) eventParameters[0], (String) eventParameters[1],
                (Map<String, String>) eventParameters[2]);
        }
    },
    /**
     * @see Listener#beginLink(org.xwiki.rendering.listener.reference.ResourceReference, boolean, java.util.Map)
     */
    BEGIN_LINK {
        @Override
        public void fireEvent(Listener listener, Object... eventParameters)
        {
            listener.beginLink((ResourceReference) eventParameters[0], (Boolean) eventParameters[1],
                (Map<String, String>) eventParameters[2]);
        }
    },
    /**
     * @see Listener#endLink(org.xwiki.rendering.listener.reference.ResourceReference, boolean, java.util.Map)
     */
    END_LINK {
        @Override
        public void fireEvent(Listener listener, Object... eventParameters)
        {
            listener.endLink((ResourceReference) eventParameters[0], (Boolean) eventParameters[1],
                (Map<String, String>) eventParameters[2]);
        }
    },
    /**
     * @see Listener#beginList(org.xwiki.rendering.listener.ListType, java.util.Map)
     */
    BEGIN_LIST {
        @Override
        public void fireEvent(Listener listener, Object... eventParameters)
        {
            listener.beginList((ListType) eventParameters[0], (Map<String, String>) eventParameters[1]);
        }

        @Override
        public boolean isInlineEnd()
        {
            // This is because for nested lists, the event after list item content is a new list
            return true;
        }
    },
    /**
     * @see Listener#endList(org.xwiki.rendering.listener.ListType, java.util.Map)
     */
    END_LIST {
        @Override
        public void fireEvent(Listener listener, Object... eventParameters)
        {
            listener.endList((ListType) eventParameters[0], (Map<String, String>) eventParameters[1]);
        }
    },
    /**
     * @see org.xwiki.rendering.listener.Listener#beginListItem()
     */
    BEGIN_LIST_ITEM {
        @Override
        public void fireEvent(Listener listener, Object... eventParameters)
        {
            if (eventParameters.length > 0) {
                listener.beginListItem((Map<String, String>) eventParameters[0]);
            } else {
                listener.beginListItem();
            }
        }
    },
    /**
     * @see org.xwiki.rendering.listener.Listener#endListItem()
     */
    END_LIST_ITEM {
        @Override
        public void fireEvent(Listener listener, Object... eventParameters)
        {
            if (eventParameters.length > 0) {
                listener.endListItem((Map<String, String>) eventParameters[0]);
            } else {
                listener.endListItem();
            }
        }

        @Override
        public boolean isInlineEnd()
        {
            return true;
        }
    },
    /**
     * @see Listener#beginMacroMarker(String, java.util.Map, String, boolean)
     */
    BEGIN_MACRO_MARKER {
        @Override
        public void fireEvent(Listener listener, Object... eventParameters)
        {
            listener.beginMacroMarker((String) eventParameters[0], (Map<String, String>) eventParameters[1],
                (String) eventParameters[2], (Boolean) eventParameters[3]);
        }
    },
    /**
     * @see Listener#endMacroMarker(String, java.util.Map, String, boolean)
     */
    END_MACRO_MARKER {
        @Override
        public void fireEvent(Listener listener, Object... eventParameters)
        {
            listener.endMacroMarker((String) eventParameters[0], (Map<String, String>) eventParameters[1],
                (String) eventParameters[2], (Boolean) eventParameters[3]);
        }
    },
    /**
     * @see Listener#beginQuotation(java.util.Map)
     */
    BEGIN_QUOTATION {
        @Override
        public void fireEvent(Listener listener, Object... eventParameters)
        {
            listener.beginQuotation((Map<String, String>) eventParameters[0]);
        }

        @Override
        public boolean isInlineEnd()
        {
            // This is because for nested quotations, the event after a quotation line is a new quotation
            return true;
        }
    },
    /**
     * @see Listener#endQuotation(java.util.Map)
     */
    END_QUOTATION {
        @Override
        public void fireEvent(Listener listener, Object... eventParameters)
        {
            listener.endQuotation((Map<String, String>) eventParameters[0]);
        }
    },
    /**
     * @see org.xwiki.rendering.listener.Listener#beginQuotationLine()
     */
    BEGIN_QUOTATION_LINE {
        @Override
        public void fireEvent(Listener listener, Object... eventParameters)
        {
            listener.beginQuotationLine();
        }
    },
    /**
     * @see org.xwiki.rendering.listener.Listener#endQuotationLine()
     */
    END_QUOTATION_LINE {
        @Override
        public void fireEvent(Listener listener, Object... eventParameters)
        {
            listener.endQuotationLine();
        }

        @Override
        public boolean isInlineEnd()
        {
            return true;
        }
    },
    /**
     * @see Listener#beginSection(java.util.Map)
     */
    BEGIN_SECTION {
        @Override
        public void fireEvent(Listener listener, Object... eventParameters)
        {
            listener.beginSection((Map<String, String>) eventParameters[0]);
        }
    },
    /**
     * @see Listener#endSection(java.util.Map)
     */
    END_SECTION {
        @Override
        public void fireEvent(Listener listener, Object... eventParameters)
        {
            listener.endSection((Map<String, String>) eventParameters[0]);
        }
    },
    /**
     * @see Listener#beginTable(java.util.Map)
     */
    BEGIN_TABLE {
        @Override
        public void fireEvent(Listener listener, Object... eventParameters)
        {
            listener.beginTable((Map<String, String>) eventParameters[0]);
        }
    },
    /**
     * @see Listener#endTable(java.util.Map)
     */
    END_TABLE {
        @Override
        public void fireEvent(Listener listener, Object... eventParameters)
        {
            listener.endTable((Map<String, String>) eventParameters[0]);
        }
    },
    /**
     * @see Listener#beginTableCell(java.util.Map)
     */
    BEGIN_TABLE_CELL {
        @Override
        public void fireEvent(Listener listener, Object... eventParameters)
        {
            listener.beginTableCell((Map<String, String>) eventParameters[0]);
        }
    },
    /**
     * @see Listener#endTableCell(java.util.Map)
     */
    END_TABLE_CELL {
        @Override
        public void fireEvent(Listener listener, Object... eventParameters)
        {
            listener.endTableCell((Map<String, String>) eventParameters[0]);
        }

        @Override
        public boolean isInlineEnd()
        {
            return true;
        }
    },
    /**
     * @see Listener#beginTableHeadCell(java.util.Map)
     */
    BEGIN_TABLE_HEAD_CELL {
        @Override
        public void fireEvent(Listener listener, Object... eventParameters)
        {
            listener.beginTableHeadCell((Map<String, String>) eventParameters[0]);
        }
    },
    /**
     * @see Listener#endTableHeadCell(java.util.Map)
     */
    END_TABLE_HEAD_CELL {
        @Override
        public void fireEvent(Listener listener, Object... eventParameters)
        {
            listener.endTableHeadCell((Map<String, String>) eventParameters[0]);
        }

        @Override
        public boolean isInlineEnd()
        {
            return true;
        }
    },
    /**
     * @see Listener#beginTableRow(java.util.Map)
     */
    BEGIN_TABLE_ROW {
        @Override
        public void fireEvent(Listener listener, Object... eventParameters)
        {
            listener.beginTableRow((Map<String, String>) eventParameters[0]);
        }
    },
    /**
     * @see Listener#endTableRow(java.util.Map)
     */
    END_TABLE_ROW {
        @Override
        public void fireEvent(Listener listener, Object... eventParameters)
        {
            listener.endTableRow((Map<String, String>) eventParameters[0]);
        }
    },
    /**
     * @see Listener#beginMetaData(org.xwiki.rendering.listener.MetaData)
     */
    BEGIN_METADATA {
        @Override
        public void fireEvent(Listener listener, Object... eventParameters)
        {
            listener.beginMetaData((MetaData) eventParameters[0]);
        }
    },
    /**
     * @see Listener#endMetaData(org.xwiki.rendering.listener.MetaData)
     */
    END_METADATA {
        @Override
        public void fireEvent(Listener listener, Object... eventParameters)
        {
            listener.endMetaData((MetaData) eventParameters[0]);
        }
    },
    /**
     * @see Listener#onRawText(String, org.xwiki.rendering.syntax.Syntax)
     */
    ON_RAW_TEXT {
        @Override
        public void fireEvent(Listener listener, Object... eventParameters)
        {
            listener.onRawText((String) eventParameters[0], (Syntax) eventParameters[1]);
        }
    },
    /**
     * @see Listener#onEmptyLines(int)
     */
    ON_EMPTY_LINES {
        @Override
        public void fireEvent(Listener listener, Object... eventParameters)
        {
            listener.onEmptyLines((Integer) eventParameters[0]);
        }
    },
    /**
     * @see Listener#onHorizontalLine(java.util.Map)
     */
    ON_HORIZONTAL_LINE {
        @Override
        public void fireEvent(Listener listener, Object... eventParameters)
        {
            listener.onHorizontalLine((Map<String, String>) eventParameters[0]);
        }
    },
    /**
     * @see Listener#onId(String)
     */
    ON_ID {
        @Override
        public void fireEvent(Listener listener, Object... eventParameters)
        {
            listener.onId((String) eventParameters[0]);
        }
    },
    /**
     * @see Listener#onImage(org.xwiki.rendering.listener.reference.ResourceReference, boolean, java.util.Map)
     */
    ON_IMAGE {
        @Override
        public void fireEvent(Listener listener, Object... eventParameters)
        {
            if (eventParameters.length == 4) {
                listener.onImage((ResourceReference) eventParameters[0], (Boolean) eventParameters[1],
                    (String) eventParameters[2], (Map<String, String>) eventParameters[3]);
            } else {
                listener.onImage((ResourceReference) eventParameters[0], (Boolean) eventParameters[1],
                    (Map<String, String>) eventParameters[2]);
            }
        }
    },
    /**
     * @see Listener#onMacro(String, java.util.Map, String, boolean)
     */
    ON_MACRO {
        @Override
        public void fireEvent(Listener listener, Object... eventParameters)
        {
            listener.onMacro((String) eventParameters[0], (Map<String, String>) eventParameters[1],
                (String) eventParameters[2], (Boolean) eventParameters[3]);
        }
    },
    /**
     * @see org.xwiki.rendering.listener.Listener#onNewLine()
     */
    ON_NEW_LINE {
        @Override
        public void fireEvent(Listener listener, Object... eventParameters)
        {
            listener.onNewLine();
        }
    },
    /**
     * @see org.xwiki.rendering.listener.Listener#onSpace()
     */
    ON_SPACE {
        @Override
        public void fireEvent(Listener listener, Object... eventParameters)
        {
            listener.onSpace();
        }
    },
    /**
     * @see Listener#onSpecialSymbol(char)
     */
    ON_SPECIAL_SYMBOL {
        @Override
        public void fireEvent(Listener listener, Object... eventParameters)
        {
            listener.onSpecialSymbol((Character) eventParameters[0]);
        }
    },
    /**
     * @see Listener#onVerbatim(String, boolean, java.util.Map)
     */
    ON_VERBATIM {
        @Override
        public void fireEvent(Listener listener, Object... eventParameters)
        {
            listener.onVerbatim((String) eventParameters[0], (Boolean) eventParameters[1],
                (Map<String, String>) eventParameters[2]);
        }
    },
    /**
     * @see Listener#onWord(String)
     */
    ON_WORD {
        @Override
        public void fireEvent(Listener listener, Object... eventParameters)
        {
            listener.onWord((String) eventParameters[0]);
        }
    },
    /**
     * @see Listener#beginFigure(Map)
     */
    BEGIN_FIGURE {
        @Override
        public void fireEvent(Listener listener, Object... eventParameters)
        {
            listener.beginFigure((Map<String, String>) eventParameters[0]);
        }
    },
    /**
     * @see Listener#beginFigureCaption(Map)
     */
    BEGIN_FIGURE_CAPTION {
        @Override
        public void fireEvent(Listener listener, Object... eventParameters)
        {
            listener.beginFigureCaption((Map<String, String>) eventParameters[0]);
        }
    },
    /**
     * @see Listener#endFigure(Map)
     */
    END_FIGURE {
        @Override
        public void fireEvent(Listener listener, Object... eventParameters)
        {
            listener.endFigure((Map<String, String>) eventParameters[0]);
        }
    },
    /**
     * @see Listener#endFigureCaption(Map)
     */
    END_FIGURE_CAPTION {
        @Override
        public void fireEvent(Listener listener, Object... eventParameters)
        {
            listener.endFigureCaption((Map<String, String>) eventParameters[0]);
        }
    };

    /**
     * Calls an event method on the passed listener with the passed parameters.
     *
     * @param listener the listener to call
     * @param eventParameters the parameters to use when calling the event method on the passed listener
     */
    public abstract void fireEvent(Listener listener, Object... eventParameters);

    /**
     * @return true if the event stops the inline mode or false otherwise
     */
    public boolean isInlineEnd()
    {
        return false;
    }
}
