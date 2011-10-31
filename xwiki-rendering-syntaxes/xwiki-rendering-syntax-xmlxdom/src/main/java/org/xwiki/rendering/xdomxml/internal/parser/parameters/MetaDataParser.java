package org.xwiki.rendering.xdomxml.internal.parser.parameters;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xwiki.rendering.listener.MetaData;

//FIXME: support any kind of data and not just String
public class MetaDataParser extends DefaultHandler
{
    private MetaData metaData = new MetaData();

    private StringBuffer value = new StringBuffer();

    private int level = 0;

    private String currentEntry;

    public MetaData getMetaData()
    {
        return this.metaData;
    }

    // ContentHandler

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        this.value.append(ch, start, length);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
        if (this.level > 0) {
            this.currentEntry = qName;
            String name = attributes.getValue("name");
            if (name != null) {
                this.currentEntry = name;
            }
        }

        ++this.level;
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        --this.level;

        if (this.level > 0) {
            this.metaData.addMetaData(this.currentEntry, this.value.toString());
            this.value.setLength(0);
        }        
    }
}