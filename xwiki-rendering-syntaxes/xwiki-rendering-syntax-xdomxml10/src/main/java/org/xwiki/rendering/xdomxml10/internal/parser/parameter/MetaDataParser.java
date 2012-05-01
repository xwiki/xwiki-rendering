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
package org.xwiki.rendering.xdomxml10.internal.parser.parameter;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xwiki.properties.ConverterManager;
import org.xwiki.rendering.listener.MetaData;

public class MetaDataParser extends DefaultHandler implements ValueParser<MetaData>
{
    private Map<String, Type> typeMapping = new HashMap<String, Type>();

    private Map<String, ValueParser< ? >> handlers = new HashMap<String, ValueParser< ? >>();

    private MetaData metaData = new MetaData();

    private StringBuffer stringValue = new StringBuffer();

    private int level = 0;

    private String currentEntry;

    private Type currentType = String.class;

    private ValueParser< ? > currentParser;

    private final ConverterManager converter;

    public MetaDataParser(ConverterManager converter)
    {
        this.converter = converter;

        this.typeMapping.put(Integer.class.getSimpleName().toLowerCase(), Integer.class);
        this.typeMapping.put(Long.class.getSimpleName().toLowerCase(), Long.class);
        this.typeMapping.put(Boolean.class.getSimpleName().toLowerCase(), Boolean.class);
        this.typeMapping.put(Double.class.getSimpleName().toLowerCase(), Double.class);
        this.typeMapping.put(Float.class.getSimpleName().toLowerCase(), Float.class);
        this.typeMapping.put(Character.class.getSimpleName().toLowerCase(), Character.class);

        this.handlers.put("stringmap", new CustomParametersParser());
        this.handlers.put("resourcereference", new ResourceReferenceParser());
    }

    public MetaDataParser(MetaDataParser metaDataParser)
    {
        this.converter = metaDataParser.converter;

        this.typeMapping = metaDataParser.typeMapping;
        this.handlers = metaDataParser.handlers;
    }

    public void putHandler(String handlerId, ValueParser< ? > handler)
    {
        this.handlers.put(handlerId.toLowerCase(), handler);
    }

    @Override
    public MetaData getValue()
    {
        return this.metaData;
    }

    // ContentHandler

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        if (this.currentParser != null) {
            this.currentParser.characters(ch, start, length);
        } else {
            this.stringValue.append(ch, start, length);
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
        if (this.level > 0) {
            if (this.currentParser != null) {
                this.currentParser.startElement(uri, localName, qName, attributes);
            } else {
                this.currentEntry = qName;
                String name = attributes.getValue("name");
                if (name != null) {
                    this.currentEntry = name;
                }
                String type = attributes.getValue("type");
                if (type != null) {
                    this.currentType = this.typeMapping.get(type.toLowerCase());
                    if (this.currentType == null) {
                        this.currentParser = this.handlers.get(type.toLowerCase());
                        if (this.currentParser == null && "metadata".equalsIgnoreCase(type)) {
                            this.currentParser = createMetaDataParser();
                        }

                        if (this.currentParser != null) {
                            this.currentParser.startElement(uri, localName, qName, attributes);
                        }
                    }
                }
            }
        }

        ++this.level;
    }

    protected MetaDataParser createMetaDataParser()
    {
        return new MetaDataParser(this);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        --this.level;

        if (this.level > 0) {
            if (this.currentParser != null) {
                if (this.level > 1) {
                    this.currentParser.endElement(uri, localName, qName);
                } else {
                    this.metaData.addMetaData(this.currentEntry, this.currentParser.getValue());
                    this.currentType = null;
                    this.currentParser = null;
                }
            } else {
                Object value;
                if (this.currentType != null) {
                    try {
                        value = this.converter.convert(this.currentType, this.stringValue.toString());
                    } catch (Exception e) {
                        value = this.stringValue.toString();
                    }
                } else {
                    value = this.stringValue.toString();
                }

                this.metaData.addMetaData(this.currentEntry, value);

                this.stringValue.setLength(0);
                this.currentType = null;
                this.currentParser = null;
            }
        }
    }
}
