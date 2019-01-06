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
package org.xwiki.rendering.xdomxml10.internal.parser;

import java.util.Collections;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.properties.ConverterManager;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.xdomxml10.internal.parser.parameter.MetaDataParser;

@Component
@Named("metadata")
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class MetaDataBlockParser extends DefaultBlockParser
{
    private static final Set<String> NAMES = Collections.singleton("metaData");

    @Inject
    protected ConverterManager converter;

    protected MetaDataParser metaDataParser;

    public MetaDataBlockParser()
    {
        super(NAMES);
    }

    @Override
    protected void startElementInternal(String uri, String localName, String qName, Attributes attributes)
        throws SAXException
    {
        if ("metaData".equals(qName)) {
            this.metaDataParser = new MetaDataParser(this.converter);
            setCurrentHandler(this.metaDataParser);
        } else {
            super.startElementInternal(uri, localName, qName, attributes);
        }
    }

    @Override
    protected void beginBlock() throws SAXException
    {
        getListener().beginMetaData(this.metaDataParser != null ? this.metaDataParser.getValue() : MetaData.EMPTY);
    }

    @Override
    protected void endBlock() throws SAXException
    {
        getListener().endMetaData(this.metaDataParser != null ? this.metaDataParser.getValue() : MetaData.EMPTY);
    }
}
