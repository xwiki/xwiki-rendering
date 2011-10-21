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
package org.xwiki.rendering.internal.renderer.pdf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.StringReader;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.Assert;
import org.junit.Test;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.renderer.BlockRenderer;
import org.xwiki.rendering.renderer.printer.OutputStreamWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.test.AbstractComponentTestCase;

/**
 * Unit tests for {@link PDFRenderer}.
 * 
 * @version $Id$
 * @since 3.3M1
 */
public class PDFRendererTest extends AbstractComponentTestCase
{
    @Test
    public void testFormats() throws Exception
    {
        String input = "//one// **two** --three-- __four__!";
        
        assertPDF("formats.pdf", input);
    }

    @Test
    public void testHeaders() throws Exception
    {
        String input =
              "= Title level 1\n"
            + "== Title level 2\n"
            + "=== Title level 3\n"
            + "==== Title level 4\n"
            + "===== Title level 5\n"
            + "====== Title level 6";

        assertPDF("sections.pdf", input);
    }

    @Test
    public void testLists() throws Exception
    {
        String input =
              "1. Item 1\n"
            + "11. Item 2\n"
            + "11*. Item 3\n"
            + "11. Item 4\n"
            + "1. Item 5\n"
            + "* Item 1\n"
            + "** Item 2\n"
            + "*** Item 3\n"
            + "***1. Item 4\n"
            + "** Item 5\n"
            + "* Item 6\n"
            + "* Item 7";

        assertPDF("lists.pdf", input);
    }

    private void assertPDF(String expectedResourceName, String input) throws Exception
    {
        XDOM xdom = getComponentManager().lookup(Parser.class, "xwiki/2.0").parse(new StringReader(input));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        WikiPrinter printer = new OutputStreamWikiPrinter(baos);

        // Render first in a memory stream for the automated test
        BlockRenderer renderer = getComponentManager().lookup(BlockRenderer.class, "pdf/1.0");
        renderer.render(xdom, printer);

        // Read the generated PDF data using PDFBox (since iText is good at generating but less good at reading)
        // in order to get the PDF content stream.
        PDDocument pdfDocument = PDDocument.load(new ByteArrayInputStream(baos.toByteArray()));
        PDPage page = (PDPage) pdfDocument.getDocumentCatalog().getAllPages().get(0);
        String data = new String(page.getContents().getByteArray());

        // Read the expected PDF from the file system and get the PDF content stream for comparison with the generated
        // PDF data.
        PDDocument expectedPdfDocument =
            PDDocument.load(getClass().getClassLoader().getResourceAsStream(expectedResourceName));
        PDPage expectedPage = (PDPage) expectedPdfDocument.getDocumentCatalog().getAllPages().get(0);

        Assert.assertEquals(new String(expectedPage.getContents().getByteArray()), data);
    }
}
