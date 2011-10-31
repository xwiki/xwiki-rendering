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
package org.xwiki.rendering.xdomxml.internal.version10.renderer;

import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;
import org.xwiki.rendering.internal.renderer.xml.AbstractChainingContentHandlerStreamRenderer;
import org.xwiki.rendering.listener.Format;
import org.xwiki.rendering.listener.HeaderLevel;
import org.xwiki.rendering.listener.ListType;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.listener.chaining.ListenerChain;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.xdomxml.internal.XDOMXMLConstants;
import org.xwiki.rendering.xdomxml.internal.current.parameter.ParameterManager;
import org.xwiki.rendering.xdomxml.internal.renderer.parameters.DefaultSerializer;
import org.xwiki.rendering.xdomxml.internal.version10.renderer.parameter.FormatConverter;
import org.xwiki.rendering.xdomxml.internal.version10.renderer.parameter.HeaderLevelConverter;
import org.xwiki.rendering.xdomxml.internal.version10.renderer.parameter.ListTypeConverter;
import org.xwiki.rendering.xdomxml.internal.version10.renderer.parameter.ResourceReferenceSerializer;

/**
 * Current version of the XDOM+XML stream based renderer.
 * 
 * @version $Id$
 */
public class XDOMXMLChainingStreamRenderer extends AbstractChainingContentHandlerStreamRenderer
{
    private static final DefaultSerializer SERIALIZER = new DefaultSerializer();

    private static final String VERSION = "1.0";

    private FormatConverter formatConverter = new FormatConverter();

    private HeaderLevelConverter headerLevelConverter = new HeaderLevelConverter();

    private ListTypeConverter listTypeConverter = new ListTypeConverter();

    private ResourceReferenceSerializer linkSerializer = new ResourceReferenceSerializer();

    private boolean versionSerialized = false;

    public XDOMXMLChainingStreamRenderer(ListenerChain listenerChain, ParameterManager parameterManager)
    {
        setListenerChain(listenerChain);
    }

    @Override
    public Syntax getSyntax()
    {
        return Syntax.XDOMXML_1_0;
    }

    // Events

    @Override
    public void beginDocument(MetaData metaData)
    {
        startBlock("document");

        serializeParameter("metaData", metaData.getMetaData());
    }

    @Override
    public void beginGroup(Map<String, String> parameters)
    {
        startBlock("group", parameters);
    }

    @Override
    public void beginFormat(Format format, Map<String, String> parameters)
    {
        startBlock("format", parameters);

        serializeParameter("format", this.formatConverter.toString(format));
    }

    @Override
    public void beginParagraph(Map<String, String> parameters)
    {
        startBlock("paragraph", parameters);
    }

    @Override
    public void beginLink(ResourceReference reference, boolean isFreeStandingURI, Map<String, String> parameters)
    {
        startBlock("link", parameters);

        this.linkSerializer.serialize(reference, getContentHandler());
        if (isFreeStandingURI) {
            serializeParameter("freestanding", isFreeStandingURI);
        }
    }

    @Override
    public void beginSection(Map<String, String> parameters)
    {
        startBlock("section", parameters);
    }

    @Override
    public void beginHeader(HeaderLevel level, String id, Map<String, String> parameters)
    {
        startBlock("header", parameters);

        serializeParameter("level", this.headerLevelConverter.toString(level));
        serializeParameter("id", id);
    }

    @Override
    public void beginList(ListType listType, Map<String, String> parameters)
    {
        startBlock("list", parameters);

        serializeParameter("type", this.listTypeConverter.toString(listType));
    }

    @Override
    public void beginListItem()
    {
        startBlock("listItem");
    }

    @Override
    public void beginDefinitionTerm()
    {
        startBlock("definitionTerm");
    }

    @Override
    public void beginDefinitionDescription()
    {
        startBlock("definitionDescription");
    }

    @Override
    public void beginTable(Map<String, String> parameters)
    {
        startBlock("table", parameters);
    }

    @Override
    public void beginTableCell(Map<String, String> parameters)
    {
        startBlock("tableCell", parameters);
    }

    @Override
    public void beginTableHeadCell(Map<String, String> parameters)
    {
        startBlock("tableHeadCell", parameters);
    }

    @Override
    public void beginTableRow(Map<String, String> parameters)
    {
        startBlock("tableRow", parameters);
    }

    @Override
    public void beginQuotation(Map<String, String> parameters)
    {
        startBlock("quotation", parameters);
    }

    @Override
    public void beginMacroMarker(String id, Map<String, String> parameters, String content, boolean isInline)
    {
        startBlock("macroMarker", parameters);

        serializeParameter("id", id);
        if (content != null) {
            serializeParameter("content", content);
        }
        if (isInline) {
            serializeParameter("inline", isInline);
        }
    }

    @Override
    public void beginDefinitionList(Map<String, String> parameters)
    {
        startBlock("definitionList", parameters);
    }

    @Override
    public void beginQuotationLine()
    {
        startBlock("quotationLine");
    }

    @Override
    public void endDocument(MetaData metaData)
    {
        endBlock();
    }

    @Override
    public void endGroup(Map<String, String> parameters)
    {
        endBlock();
    }

    @Override
    public void endFormat(Format format, Map<String, String> parameters)
    {
        endBlock();
    }

    @Override
    public void endParagraph(Map<String, String> parameters)
    {
        endBlock();
    }

    @Override
    public void endLink(ResourceReference reference, boolean isFreeStandingURI, Map<String, String> parameters)
    {
        endBlock();
    }

    @Override
    public void endSection(Map<String, String> parameters)
    {
        endBlock();
    }

    @Override
    public void endHeader(HeaderLevel level, String id, Map<String, String> parameters)
    {
        endBlock();
    }

    @Override
    public void endList(ListType listType, Map<String, String> parameters)
    {
        endBlock();
    }

    @Override
    public void endListItem()
    {
        endBlock();
    }

    @Override
    public void endMacroMarker(String name, Map<String, String> parameters, String content, boolean isInline)
    {
        endBlock();
    }

    @Override
    public void endDefinitionList(Map<String, String> parameters)
    {
        endBlock();
    }

    @Override
    public void endDefinitionTerm()
    {
        endBlock();
    }

    @Override
    public void endDefinitionDescription()
    {
        endBlock();
    }

    @Override
    public void endQuotation(Map<String, String> parameters)
    {
        endBlock();
    }

    @Override
    public void endQuotationLine()
    {
        endBlock();
    }

    @Override
    public void endTable(Map<String, String> parameters)
    {
        endBlock();
    }

    @Override
    public void endTableCell(Map<String, String> parameters)
    {
        endBlock();
    }

    @Override
    public void endTableHeadCell(Map<String, String> parameters)
    {
        endBlock();
    }

    @Override
    public void endTableRow(Map<String, String> parameters)
    {
        endBlock();
    }

    @Override
    public void onNewLine()
    {
        emptyBlock("newLine");
    }

    @Override
    public void onMacro(String id, Map<String, String> parameters, String content, boolean isInline)
    {
        startBlock("macro", parameters);

        serializeParameter("id", id);
        if (content != null) {
            serializeParameter("content", content);
        }
        if (isInline) {
            serializeParameter("inline", isInline);
        }

        endBlock();
    }

    @Override
    public void onWord(String word)
    {
        startBlock("word");

        serializeParameter("word", word);

        endBlock();
    }

    @Override
    public void onSpace()
    {
        emptyBlock("space");
    }

    @Override
    public void onSpecialSymbol(char symbol)
    {
        startBlock("specialSymbol");

        serializeParameter("symbol", symbol);

        endBlock();
    }

    @Override
    public void onRawText(String text, Syntax syntax)
    {
        startBlock("rawText");

        serializeParameter("content", text);
        serializeParameter("syntax", syntax.toIdString());

        endBlock();
    }

    @Override
    public void onId(String name)
    {
        startBlock("id");

        serializeParameter("name", name);

        endBlock();
    }

    @Override
    public void onHorizontalLine(Map<String, String> parameters)
    {
        emptyBlock("horizontalLine", parameters);
    }

    @Override
    public void onEmptyLines(int count)
    {
        startBlock("emptyLines");

        if (count > 1) {
            serializeParameter("count", count);
        }

        endBlock();
    }

    @Override
    public void onVerbatim(String protectedString, boolean isInline, Map<String, String> parameters)
    {
        startBlock("verbatim");

        serializeParameter("content", protectedString);
        if (isInline) {
            serializeParameter("inline", isInline);
        }

        endBlock();
    }

    @Override
    public void onImage(ResourceReference reference, boolean isFreeStandingURI, Map<String, String> parameters)
    {
        startBlock("image", parameters);

        serializeParameter("freestanding", isFreeStandingURI);
        this.linkSerializer.serialize(reference, getContentHandler());

        endBlock();
    }

    // Tools

    private void startBlock(String blockName)
    {
        startBlock(blockName, null);
    }

    private void startBlock(String blockName, Map<String, String> customParameters)
    {
        AttributesImpl attributes = new AttributesImpl();
        attributes.addAttribute(null, null, XDOMXMLConstants.ATT_BLOCK_NAME, null, blockName);
        if (!this.versionSerialized) {
            attributes.addAttribute(null, null, XDOMXMLConstants.ATT_BLOCK_VERSION, null, VERSION);
            this.versionSerialized = true;
        }

        startElement(XDOMXMLConstants.ELEM_BLOCK, attributes);

        if (customParameters != null) {
            serializeCustomParameters(customParameters);
        }
    }

    private void endBlock()
    {
        endElement(XDOMXMLConstants.ELEM_BLOCK);
    }

    private void emptyBlock(String blockName)
    {
        startBlock(blockName);
        endBlock();
    }

    private void emptyBlock(String blockName, Map<String, String> customParameters)
    {
        startBlock(blockName, customParameters);
        endBlock();
    }

    private void serializeCustomParameters(Map<String, String> parameters)
    {
        if (parameters.size() > 0) {
            serializeParameter(XDOMXMLConstants.ELEM_PARAMETERS, parameters);
        }
    }

    public void serializeParameter(String name, Map<String, String> map)
    {
        SERIALIZER.serializeParameter(name, map, getContentHandler());
    }

    public void serializeParameter(String name, MetaData metaData)
    {
        startElement(name, DefaultSerializer.EMPTY_ATTRIBUTES);
        for (Map.Entry<String, Object> entry : metaData.getMetaData().entrySet()) {
            serializeParameter(entry.getKey(), entry.getValue());
        }
        endElement(name);
    }

    public void serializeParameter(String name, boolean value)
    {
        SERIALIZER.serializeParameter(name, value, getContentHandler());
    }

    public void serializeParameter(String name, char value)
    {
        SERIALIZER.serializeParameter(name, value, getContentHandler());
    }

    public void serializeParameter(String name, int value)
    {
        SERIALIZER.serializeParameter(name, value, getContentHandler());
    }

    public void serializeParameter(String name, String value)
    {
        SERIALIZER.serializeParameter(name, value, getContentHandler());
    }

    public void serializeParameter(String name, Format value)
    {
        SERIALIZER.serializeParameter(name, this.formatConverter.toString(value), getContentHandler());
    }

    public void serializeParameter(String name, HeaderLevel value)
    {
        SERIALIZER.serializeParameter(name, this.headerLevelConverter.toString(value), getContentHandler());
    }

    public void serializeParameter(String name, ListType value)
    {
        SERIALIZER.serializeParameter(name, this.listTypeConverter.toString(value), getContentHandler());
    }

    public void serializeParameter(String name, ResourceReference value)
    {
        startElement(name, DefaultSerializer.EMPTY_ATTRIBUTES);
        this.linkSerializer.serialize(value, getContentHandler());
        endElement(name);
    }

    public void serializeParameter(String name, Number value)
    {
        SERIALIZER.serializeParameter(name, value.toString(), getContentHandler());
    }

    public void serializeParameter(String name, Object value)
    {
        if (value instanceof String) {
            serializeParameter(name, (String) value);
        } else if (value instanceof Number) {
            serializeParameter(name, (Number) value);
        } else if (value instanceof Format) {
            serializeParameter(name, (Format) value);
        } else if (value instanceof HeaderLevel) {
            serializeParameter(name, (HeaderLevel) value);
        } else if (value instanceof ListType) {
            serializeParameter(name, (ListType) value);
        } else if (value instanceof ResourceReference) {
            serializeParameter(name, (ResourceReference) value);
        } else if (value instanceof  MetaData) {
            serializeParameter(name, (MetaData) value);
        }
    }

    private void startElement(String elementName, Attributes attributes)
    {
        SERIALIZER.startElement(elementName, attributes, getContentHandler());
    }

    private void endElement(String elementName)
    {
        SERIALIZER.endElement(elementName, getContentHandler());
    }
}
