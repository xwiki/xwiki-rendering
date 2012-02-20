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
package org.xwiki.rendering.wikimodel.xml;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/**
 * This class contains various utility methods used to manipulate with DOM
 * nodes.
 *
 * @version $Id$
 * @since 4.0M1
 */
public class XmlUtil
{
    public static void formatXML(
        Document xml,
        Document xsl,
        URIResolver resolver,
        Writer output) throws Exception
    {
        try {
            DOMSource xslSource = new DOMSource(xsl);
            DOMSource xmlSource = new DOMSource(xml);
            Result result = new StreamResult(output);
            formatXML(xmlSource, xslSource, resolver, result);
        } finally {
            output.close();
        }
    }

    public static void formatXML(Document xml, Document xsl, Writer output)
        throws Exception
    {
        formatXML(xml, xsl, null, output);
    }

    public static void formatXML(
        Reader xml,
        Reader xsl,
        URIResolver resolver,
        Writer output) throws Exception
    {
        try {
            try {
                try {
                    Source xmlSource = new SAXSource(new InputSource(xml));
                    Source xslSource = new SAXSource(new InputSource(xsl));
                    Result result = new StreamResult(output);
                    formatXML(xmlSource, xslSource, resolver, result);
                } finally {
                    output.close();
                }
            } finally {
                xsl.close();
            }
        } finally {
            xml.close();
        }
    }

    public static void formatXML(Reader xml, Reader xsl, Writer output)
        throws Exception
    {
        formatXML(xml, xsl, null, output);
    }

    /**
     * @param xmlSource
     * @param xslSource
     * @param resolver
     * @param output
     * @throws TransformerFactoryConfigurationError
     * @throws TransformerConfigurationException
     * @throws TransformerException
     */
    public static void formatXML(
        Source xmlSource,
        Source xslSource,
        URIResolver resolver,
        Result result)
        throws TransformerFactoryConfigurationError,
        TransformerConfigurationException,
        TransformerException
    {
        TransformerFactory factory = TransformerFactory.newInstance();
        Templates t = factory.newTemplates(xslSource);
        Transformer transformer = t.newTransformer();
        if (resolver != null) {
            transformer.setURIResolver(resolver);
        }
        transformer.transform(xmlSource, result);
    }

    /**
     * Creates and returns an new document builder factory. This method tries to
     * configure the namespace support for the builder. If the underlying parser
     * does not support namespaces then this method returns a simple
     * DocumentBuilder object.
     *
     * @return a new document builder
     */
    private static DocumentBuilder getDocumentBuilder()
        throws ParserConfigurationException
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true); // never forget this!
        try {
            factory.setFeature("http://xml.org/sax/features/namespaces", true);
        } catch (Throwable t) {
            // Just skip it...
        }
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder;
    }

    public static String lookupNamespaceURI(Node root, String specifiedPrefix)
    {
        if (root == null) {
            return null;
        }
        if (root.hasAttributes()) {
            NamedNodeMap nnm = root.getAttributes();
            for (int i = 0; i < nnm.getLength(); i++) {
                Node n = nnm.item(i);
                if (("xmlns".equals(n.getPrefix()) && specifiedPrefix.equals(n
                    .getNodeName()))
                    || ("xmlns:" + specifiedPrefix).equals(n.getNodeName()))
                {
                    return n.getNodeValue();
                }
            }
        }
        return lookupNamespaceURI(root.getParentNode(), specifiedPrefix);
    }

    /**
     * Returns a new copy of the given node using the specified document as a
     * factory for new nodes.
     *
     * @param doc the document used as a factory for new nodes
     * @param node the node to copy
     * @return a new copy of the given node using the specified document as a
     *         factory for new nodes.
     */
    @SuppressWarnings("unchecked")
    public static <E extends Node> E newCopy(Document doc, E node)
    {
        E result = (E) node.cloneNode(true);
        return (E) doc.adoptNode(result);
    }

    /**
     * Creates and returns a new empty DOM document.
     *
     * @return a newly created DOM document
     */
    public static Document newDocument() throws ParserConfigurationException
    {
        DocumentBuilder builder = getDocumentBuilder();
        return builder.newDocument();
    }

    public static Element newTemplate(String xml) throws Exception
    {
        Document doc = readXML(xml);
        return doc.getDocumentElement();
    }

    /**
     * Parses the given input stream and returns the corresponding desirialized
     * XML document.
     *
     * @param input the input stream containing the serialized XML document
     * @return the deserialized DOM document
     */
    public static Document readXML(InputStream input) throws Exception
    {
        return readXML(new InputStreamReader(input, "UTF-8"));
    }

    /**
     * Parses the given input stream and returns the corresponding desirialized
     * XML document.
     *
     * @param reader the reader containing the serialized XML document
     * @return the deserialized DOM document
     */
    public static Document readXML(Reader reader) throws Exception
    {
        try {
            DocumentBuilder builder = getDocumentBuilder();
            InputSource source = new InputSource(reader);
            Document doc = builder.parse(source);
            return doc;
        } finally {
            reader.close();
        }
    }

    public static Document readXML(String str) throws Exception
    {
        if (str == null) {
            return null;
        }
        Reader reader = new StringReader(str);
        return readXML(reader);
    }

    public static String write(Document doc) throws Exception
    {
        return write(doc.getDocumentElement());
    }

    public static String write(Element e) throws Exception
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        write(e, baos);
        return new String(baos.toByteArray(), "UTF-8");
    }

    public static void write(Element root, OutputStream os) throws Exception
    {
        Writer writer = new OutputStreamWriter(os);
        write(root, writer);
    }

    /**
     * @param root
     * @param writer
     * @throws TransformerConfigurationException
     * @throws TransformerFactoryConfigurationError
     * @throws TransformerException
     */
    public static void write(Element root, Writer writer)
        throws TransformerConfigurationException,
        TransformerFactoryConfigurationError,
        TransformerException
    {
        Source input = new DOMSource(root);
        write(input, writer);
    }

    /**
     * @param reader
     * @param parser
     * @param output
     * @throws TransformerConfigurationException
     * @throws TransformerFactoryConfigurationError
     * @throws TransformerException
     */
    public static void write(Reader reader, XMLReader parser, Result output)
        throws TransformerConfigurationException,
        TransformerFactoryConfigurationError,
        TransformerException
    {
        SAXSource input = new SAXSource();
        InputSource inputSource = new InputSource();
        inputSource.setCharacterStream(reader);
        input.setInputSource(inputSource);
        input.setXMLReader(parser);
        write(input, output);
    }

    /**
     * @param parser
     * @param writer
     * @throws TransformerConfigurationException
     * @throws TransformerFactoryConfigurationError
     * @throws TransformerException
     */
    public static void write(Reader reader, XMLReader parser, Writer writer)
        throws TransformerConfigurationException,
        TransformerFactoryConfigurationError,
        TransformerException
    {
        Result output = new StreamResult(writer);
        write(reader, parser, output);
    }

    public static void write(Source input, Result output)
        throws TransformerConfigurationException,
        TransformerFactoryConfigurationError,
        TransformerException
    {
        write(input, output, true);
    }

    public static void write(Source input, Result output, boolean indent)
        throws TransformerConfigurationException,
        TransformerFactoryConfigurationError,
        TransformerException
    {
        boolean omitxmldeclaration = true;
        Transformer idTransform = TransformerFactory
            .newInstance()
            .newTransformer();
        if (omitxmldeclaration) {
            idTransform.setOutputProperty("omit-xml-declaration", "yes");
        }
        idTransform.setOutputProperty("encoding", "UTF-8");
        if (indent) {
            idTransform.setOutputProperty("indent", "yes");
            try {
                idTransform.setOutputProperty(
                    "{http://xml.apache.org/xslt}indent-amount",
                    "4");
            } catch (Exception e) {
                //
            }
        }
        idTransform.transform(input, output);
    }

    /**
     * @param input
     * @param os
     * @throws TransformerConfigurationException
     * @throws TransformerFactoryConfigurationError
     * @throws TransformerException
     */
    public static void write(Source input, Writer writer)
        throws TransformerConfigurationException,
        TransformerFactoryConfigurationError,
        TransformerException
    {
        Result output = new StreamResult(writer);
        write(input, output);
    }
}