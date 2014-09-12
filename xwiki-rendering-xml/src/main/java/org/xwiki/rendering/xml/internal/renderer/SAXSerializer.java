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
package org.xwiki.rendering.xml.internal.renderer;

import java.io.IOException;
import java.io.Writer;

import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

/**
 * Fix various issues in {@link XMLWriter}.
 *
 * @version $Id$
 * @since 5.2M1
 */
public class SAXSerializer extends XMLWriter
{
    /**
     * Indicate if something has been written already (only used when formatting is enabled).
     */
    private boolean started;

    /**
     * @param writer the actual writer
     */
    public SAXSerializer(Writer writer)
    {
        super(writer);
    }

    /**
     * @param writer the actual writer
     * @param format the XML format to use
     * @since 5.2M1
     */
    public SAXSerializer(Writer writer, OutputFormat format)
    {
        super(writer, format);
    }

    @Override
    // FIXME: remove that when https://sourceforge.net/p/dom4j/bugs/202/ is fixed
    protected String escapeAttributeEntities(String text)
    {
        String escapedTest = super.escapeAttributeEntities(text);
        escapedTest = escapedTest.replace("\t", "&#9;");
        escapedTest = escapedTest.replace("\n", "&#10;");
        escapedTest = escapedTest.replace("\r", "&#13;");

        return escapedTest;
    }

    // Workaround a XMLWriter with the first new line
    @Override
    protected void writePrintln() throws IOException
    {
        if (getOutputFormat().isNewlines()) {
            if (this.started) {
                super.writePrintln();
            }

            this.started = true;
        }
    }
}
