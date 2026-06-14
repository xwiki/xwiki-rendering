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
package org.xwiki.rendering.xdomxml10.internal.renderer;

import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;
import org.xwiki.rendering.listener.Format;
import org.xwiki.rendering.listener.HeaderLevel;
import org.xwiki.rendering.listener.ListType;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.listener.chaining.ListenerChain;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.xdomxml10.internal.XDOMXMLConstants;
import org.xwiki.rendering.xdomxml10.internal.renderer.parameter.DefaultSerializer;
import org.xwiki.rendering.xdomxml10.internal.renderer.parameter.FormatConverter;
import org.xwiki.rendering.xdomxml10.internal.renderer.parameter.HeaderLevelConverter;
import org.xwiki.rendering.xdomxml10.internal.renderer.parameter.ListTypeConverter;
import org.xwiki.rendering.xdomxml10.internal.renderer.parameter.ResourceReferenceSerializer;
import org.xwiki.rendering.xml.internal.renderer.AbstractChainingContentHandlerStreamRenderer;

import static org.xwiki.rendering.xdomxml10.internal.XDOMXML10SyntaxProvider.XDOMXML_1_0;

/**
 * Current version of the XDOM+XML stream based renderer.
 * 
 * @version $Id$
 */
public class XDOMXMLChainingStreamRenderer extends AbstractChainingContentHandlerStreamRenderer
{
    private static final DefaultSerializer SERIALIZER = new DefaultSerializer();

    private static final String VERSION = "1.0";

    private static final String METADATA = "metaData";

    private static final String FORMAT = "format";

    private static final String FREESTANDING = "freestanding";

    private static final String ID = "id";

    private static final String TYPE = "type";

    private static final String LISTITEM = "listItem";

    private static final String CONTENT = "content";

    private static final String INLINE = "inline";

    private static final String WORD = "word";

    private static final String IMAGE = "image";

    private FormatConverter formatConverter = new FormatConverter();

    private HeaderLevelConverter headerLevelConverter = new HeaderLevelConverter();

    private ListTypeConverter listTypeConverter = new ListTypeConverter();

    private ResourceReferenceSerializer linkSerializer = new ResourceReferenceSerializer();

    private boolean versionSerialized;

    /**
     * @param listenerChain the chain of listeners this renderer is part of
     */
    public XDOMXMLChainingStreamRenderer(ListenerChain listenerChain)
    {
        setListenerChain(listenerChain);
    }

    @Override
    public Syntax getSyntax()
    {
        return XDOMXML_1_0;
    }

    // Events

    @Override
    public void beginDocument(MetaData metadata)
    {
        startBlock("document");

        if (!metadata.getMetaData().isEmpty()) {
            serializeParameter(METADATA, metadata, false);
        }
    }

    @Override
    public void beginGroup(Map<String, String> parameters)
    {
        startBlock("group", parameters);
    }

    @Override
    public void beginFormat(Format format, Map<String, String> parameters)
    {
        startBlock(FORMAT, parameters);

        serializeParameter(FORMAT, this.formatConverter.toString(format), false);
    }

    @Override
    public void beginParagraph(Map<String, String> parameters)
    {
        startBlock("paragraph", parameters);
    }

    @Override
    public void beginLink(ResourceReference reference, boolean freestanding, Map<String, String> parameters)
    {
        startBlock("link", parameters);

        this.linkSerializer.serialize(reference, getContentHandler());
        if (freestanding) {
            serializeParameter(FREESTANDING, freestanding, false);
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

        serializeParameter("level", this.headerLevelConverter.toString(level), false);
        serializeParameter(ID, id, false);
    }

    @Override
    public void beginList(ListType type, Map<String, String> parameters)
    {
        startBlock("list", parameters);

        serializeParameter(TYPE, this.listTypeConverter.toString(type), false);
    }

    @Override
    public void beginListItem()
    {
        startBlock(LISTITEM);
    }

    @Override
    public void beginListItem(Map<String, String> parameters)
    {
        startBlock(LISTITEM, parameters);
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

        serializeParameter(ID, id, false);
        if (content != null) {
            serializeParameter(CONTENT, content, false);
        }
        if (isInline) {
            serializeParameter(INLINE, isInline, false);
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
    public void beginMetaData(MetaData metadata)
    {
        startBlock(METADATA);

        if (!metadata.getMetaData().isEmpty()) {
            serializeParameter(METADATA, metadata, false);
        }
    }

    @Override
    public void endDocument(MetaData metadata)
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
    public void endLink(ResourceReference reference, boolean freestanding, Map<String, String> parameters)
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
    public void endList(ListType type, Map<String, String> parameters)
    {
        endBlock();
    }

    @Override
    public void endListItem()
    {
        endBlock();
    }

    @Override
    public void endListItem(Map<String, String> parameters)
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
    public void endMetaData(MetaData metadata)
    {
        endBlock();
    }

    @Override
    public void onNewLine()
    {
        emptyBlock("newLine");
    }

    @Override
    public void onMacro(String id, Map<String, String> parameters, String content, boolean inline)
    {
        startBlock("macro", parameters);

        serializeParameter(ID, id, false);
        if (content != null) {
            serializeParameter(CONTENT, content, false);
        }
        if (inline) {
            serializeParameter(INLINE, inline, false);
        }

        endBlock();
    }

    @Override
    public void onWord(String word)
    {
        startBlock(WORD);

        serializeParameter(WORD, word, false);

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

        serializeParameter("symbol", symbol, false);

        endBlock();
    }

    @Override
    public void onRawText(String text, Syntax syntax)
    {
        startBlock("rawText");

        serializeParameter(CONTENT, text, false);
        serializeParameter("syntax", syntax.toIdString(), false);

        endBlock();
    }

    @Override
    public void onId(String name)
    {
        startBlock(ID);

        serializeParameter("name", name, false);

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
            serializeParameter("count", count, false);
        }

        endBlock();
    }

    @Override
    public void onVerbatim(String content, boolean inline, Map<String, String> parameters)
    {
        startBlock("verbatim");

        serializeParameter(CONTENT, content, false);
        if (inline) {
            serializeParameter(INLINE, inline, false);
        }

        endBlock();
    }

    @Override
    public void onImage(ResourceReference reference, boolean freestanding, Map<String, String> parameters)
    {
        startBlock(IMAGE, parameters);

        serializeParameter(FREESTANDING, freestanding, false);
        this.linkSerializer.serialize(reference, getContentHandler());

        endBlock();
    }

    @Override
    public void onImage(ResourceReference reference, boolean freestanding, String id, Map<String, String> parameters)
    {
        startBlock(IMAGE, parameters);

        serializeParameter(FREESTANDING, freestanding, false);
        serializeParameter(ID, id, false);
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
            serializeParameter(XDOMXMLConstants.ELEM_PARAMETERS, parameters, false);
        }
    }

    /**
     * @param name the name of the parameter
     * @param map the map value to serialize
     * @param type {@code true} to serialize the type information, {@code false} otherwise
     */
    public void serializeParameter(String name, Map<?, ?> map, boolean type)
    {
        SERIALIZER.serializeParameter(name, map, type, getContentHandler());
    }

    /**
     * @param name the name of the parameter
     * @param metadata the metadata value to serialize
     * @param type {@code true} to serialize the type information, {@code false} otherwise
     */
    public void serializeParameter(String name, MetaData metadata, boolean type)
    {
        Attributes attributes;

        if (type) {
            AttributesImpl attributesImpl = new AttributesImpl();
            attributesImpl.addAttribute(null, null, TYPE, null, "MetaData");
            attributes = attributesImpl;
        } else {
            attributes = DefaultSerializer.EMPTY_ATTRIBUTES;
        }

        startElement(name, attributes);
        for (Map.Entry<String, Object> entry : metadata.getMetaData().entrySet()) {
            serializeParameter(entry.getKey(), entry.getValue(), true);
        }
        endElement(name);
    }

    /**
     * @param name the name of the parameter
     * @param value the boolean value to serialize
     * @param type {@code true} to serialize the type information, {@code false} otherwise
     */
    public void serializeParameter(String name, boolean value, boolean type)
    {
        SERIALIZER.serializeParameter(name, value, type, getContentHandler());
    }

    /**
     * @param name the name of the parameter
     * @param value the char value to serialize
     * @param type {@code true} to serialize the type information, {@code false} otherwise
     */
    public void serializeParameter(String name, char value, boolean type)
    {
        SERIALIZER.serializeParameter(name, value, type, getContentHandler());
    }

    /**
     * @param name the name of the parameter
     * @param value the int value to serialize
     * @param type {@code true} to serialize the type information, {@code false} otherwise
     */
    public void serializeParameter(String name, int value, boolean type)
    {
        SERIALIZER.serializeParameter(name, value, type, getContentHandler());
    }

    /**
     * @param name the name of the parameter
     * @param value the String value to serialize
     * @param type {@code true} to serialize the type information, {@code false} otherwise
     */
    public void serializeParameter(String name, String value, boolean type)
    {
        SERIALIZER.serializeParameter(name, value, null, getContentHandler());
    }

    /**
     * @param name the name of the parameter
     * @param value the format value to serialize
     * @param type {@code true} to serialize the type information, {@code false} otherwise
     */
    public void serializeParameter(String name, Format value, boolean type)
    {
        SERIALIZER.serializeParameter(name, this.formatConverter.toString(value), type ? "Format" : null,
            getContentHandler());
    }

    /**
     * @param name the name of the parameter
     * @param value the header level value to serialize
     * @param type {@code true} to serialize the type information, {@code false} otherwise
     */
    public void serializeParameter(String name, HeaderLevel value, boolean type)
    {
        SERIALIZER.serializeParameter(name, this.headerLevelConverter.toString(value), type ? "HeaderLevel" : null,
            getContentHandler());
    }

    /**
     * @param name the name of the parameter
     * @param value the list type value to serialize
     * @param type {@code true} to serialize the type information, {@code false} otherwise
     */
    public void serializeParameter(String name, ListType value, boolean type)
    {
        SERIALIZER.serializeParameter(name, this.listTypeConverter.toString(value), type ? "ListType" : null,
            getContentHandler());
    }

    /**
     * @param name the name of the parameter
     * @param value the resource reference value to serialize
     * @param type {@code true} to serialize the type information, {@code false} otherwise
     */
    public void serializeParameter(String name, ResourceReference value, boolean type)
    {
        Attributes attributes;

        if (type) {
            AttributesImpl attributesImpl = new AttributesImpl();
            attributesImpl.addAttribute(null, null, TYPE, null, "ResourceReference");
            attributes = attributesImpl;
        } else {
            attributes = DefaultSerializer.EMPTY_ATTRIBUTES;
        }

        startElement(name, attributes);
        this.linkSerializer.serialize(value, getContentHandler());
        endElement(name);
    }

    /**
     * @param name the name of the parameter
     * @param value the number value to serialize
     * @param type {@code true} to serialize the type information, {@code false} otherwise
     */
    public void serializeParameter(String name, Number value, boolean type)
    {
        SERIALIZER.serializeParameter(name, value.toString(), type ? value.getClass().getSimpleName() : null,
            getContentHandler());
    }

    /**
     * @param name the name of the parameter
     * @param value the value to serialize, dispatched to the matching typed overload
     * @param type {@code true} to serialize the type information, {@code false} otherwise
     */
    public void serializeParameter(String name, Object value, boolean type)
    {
        if (value instanceof String) {
            serializeParameter(name, (String) value, type);
        } else if (value instanceof Number) {
            serializeParameter(name, (Number) value, type);
        } else if (value instanceof Format) {
            serializeParameter(name, (Format) value, type);
        } else if (value instanceof HeaderLevel) {
            serializeParameter(name, (HeaderLevel) value, type);
        } else if (value instanceof ListType) {
            serializeParameter(name, (ListType) value, type);
        } else if (value instanceof ResourceReference) {
            serializeParameter(name, (ResourceReference) value, type);
        } else if (value instanceof MetaData) {
            serializeParameter(name, (MetaData) value, type);
        } else if (value instanceof Map) {
            serializeParameter(name, (Map<?, ?>) value, type);
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
