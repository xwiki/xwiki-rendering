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
package org.xwiki.rendering.renderer.printer;

import java.util.Map;

import org.dom4j.io.XMLWriter;
import org.xml.sax.Attributes;

/**
 * Base toolkit class for all XML-based printers.
 * 
 * @version $Id$
 * @since 1.9M1
 */
public interface XMLWikiPrinter
{   
    
    public XMLWriter getXMLWriter();

    public void setWikiPrinter(WikiPrinter printer);

    /**
     * Print provided text. Takes care of xml escaping.
     */
    public void printXML(String str);

    /**
     * Print the xml element. In the form <name/>.
     */
    public void printXMLElement(String name);

    /**
     * Print the xml element. In the form <name att1="value1" att2="value2"/>.
     */
    public void printXMLElement(String name, String[][] attributes);

    /**
     * Print the xml element. In the form <name att1="value1" att2="value2"/>.
     */
    public void printXMLElement(String name, Map<String, String> attributes);

    /**
     * Print the start tag of xml element. In the form &lt;name&gt;.
     */
    public void printXMLStartElement(String name);

    /**
     * Print the start tag of xml element. In the form &lt;name att1="value1" att2="value2"&gt;.
     */
    public void printXMLStartElement(String name, String[][] attributes);

    /**
     * Print the start tag of xml element. In the form &lt;name att1="value1" att2="value2"&gt;.
     */
    public void printXMLStartElement(String name, Map<String, String> attributes);

    /**
     * Print the start tag of xml element. In the form &lt;name att1="value1" att2="value2"&gt;.
     */
    public void printXMLStartElement(String name, Attributes attributes);

    /**
     * Print the end tag of xml element. In the form &lt;/name&gt;.
     */
    public void printXMLEndElement(String name);

    /**
     * Print a XML comment. Note that the content that you pass must be valid XML comment, ie not have <code>--</code>
     * characters (or <code>-</code> if it's the last character). If you're not sure what the comment content will be
     * use {@link #printXMLComment(String, boolean)} instead, passing true for the second parameter.
     * 
     * @param content the comment content
     */
    public void printXMLComment(String content);

    /**
     * Print a XML comment.
     * 
     * @param content the comment content
     * @param escape indicate if comment content has to be escaped. XML content does not support -- and - (when it's the
     *            last character). Escaping is based on backslash. "- --\ -" give "- \-\-\\ \-\ ".
     */
    public void printXMLComment(String content, boolean escape);

    /**
     * Start a CDATA section.
     */
    public void printXMLStartCData();

    /**
     * End a CDATA section.
     */
    public void printXMLEndCData();

    public void printEntity(String entity);

    /**
     * Print some text without escaping anything, it's supposed to be XML or at least contains only valid characters in
     * XML text node.
     * 
     * @param row the content
     */
    public void printRaw(String row);
    
}
