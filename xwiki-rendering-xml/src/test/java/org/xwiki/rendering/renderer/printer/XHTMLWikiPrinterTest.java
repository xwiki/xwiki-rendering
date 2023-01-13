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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.xml.sax.helpers.AttributesImpl;
import org.xwiki.xml.html.HTMLElementSanitizer;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link XHTMLWikiPrinter}.
 *
 * @version $Id$
 */
class XHTMLWikiPrinterTest
{
    @ParameterizedTest
    @CsvSource({
        "Closing the {{/html}} macro., Closing the &#123;&#123;/html}} macro.",
        "Starting a macro {, Starting a macro &#123;",
        "Partial: {{/h, Partial: {&#123;/h",
        "{{html}}, {{html}}"
    })
    void testRawEscaping(String input, String expected)
    {
        WikiPrinter mockPrinter = mock(WikiPrinter.class);
        XHTMLWikiPrinter xhtmlWikiPrinter = new XHTMLWikiPrinter(mockPrinter);
        xhtmlWikiPrinter.printRaw(input);
        verify(mockPrinter).print(expected);
    }

    @ParameterizedTest
    @CsvSource({
        "invalid, value, data-xwiki-translated-attribute-invalid, value",
        "valid, test&, valid, test&amp;",
        "in/valid, value, data-xwiki-translated-attribute-invalid, value"
    })
    void testParameterCleaning(String parameterName, String parameterValue, String expectedName, String expectedValue)
    {
        HTMLElementSanitizer mockSanitizer = mock(HTMLElementSanitizer.class);
        when(mockSanitizer.isElementAllowed(anyString())).thenReturn(true);
        when(mockSanitizer.isAttributeAllowed(anyString(), anyString(), anyString())).then(invocation ->
        {
            String attributeName = invocation.getArgument(1, String.class);
            return "valid".equals(attributeName) || attributeName.startsWith("data-");
        });

        // Test all possibilities of invoking the printer (with different kinds of arguments).
        WikiPrinter mockPrinter = mock(WikiPrinter.class);
        XHTMLWikiPrinter xhtmlWikiPrinter = new XHTMLWikiPrinter(mockPrinter, mockSanitizer);
        Map<String, String> mapParameters = Map.of(parameterName, parameterValue);
        xhtmlWikiPrinter.printXMLStartElement("div", mapParameters);
        verify(mockSanitizer, atLeast(1)).isElementAllowed("div");
        verify(mockSanitizer, atLeast(1)).isAttributeAllowed("div", parameterName, parameterValue);
        verify(mockSanitizer, atLeast(1)).isAttributeAllowed("div", expectedName, parameterValue);
        verifyPrinting(mockPrinter, expectedName, expectedValue, true);

        mockPrinter = mock(WikiPrinter.class);
        xhtmlWikiPrinter = new XHTMLWikiPrinter(mockPrinter, mockSanitizer);
        xhtmlWikiPrinter.printXMLElement("div", mapParameters);
        verifyPrinting(mockPrinter, expectedName, expectedValue, false);

        mockPrinter = mock(WikiPrinter.class);
        xhtmlWikiPrinter = new XHTMLWikiPrinter(mockPrinter, mockSanitizer);
        String[][] arrayParameters = { { parameterName, parameterValue } };
        xhtmlWikiPrinter.printXMLStartElement("div", arrayParameters);
        verifyPrinting(mockPrinter, expectedName, expectedValue, true);

        mockPrinter = mock(WikiPrinter.class);
        xhtmlWikiPrinter = new XHTMLWikiPrinter(mockPrinter, mockSanitizer);
        xhtmlWikiPrinter.printXMLElement("div", arrayParameters);
        verifyPrinting(mockPrinter, expectedName, expectedValue, false);

        mockPrinter = mock(WikiPrinter.class);
        xhtmlWikiPrinter = new XHTMLWikiPrinter(mockPrinter, mockSanitizer);
        AttributesImpl attributes = new AttributesImpl();
        attributes.addAttribute(null, null, parameterName, null, parameterValue);
        xhtmlWikiPrinter.printXMLStartElement("div", attributes);
        verifyPrinting(mockPrinter, expectedName, expectedValue, true);
    }

    private void verifyPrinting(WikiPrinter mockPrinter, String attributeName,
        String attributeValue, boolean isStart)
    {
        InOrder inOrder = Mockito.inOrder(mockPrinter);
        inOrder.verify(mockPrinter).print("<");
        inOrder.verify(mockPrinter).print("div");
        inOrder.verify(mockPrinter).print(" ");
        inOrder.verify(mockPrinter).print(attributeName);
        inOrder.verify(mockPrinter).print("=");
        inOrder.verify(mockPrinter).print("\"");
        inOrder.verify(mockPrinter).print(attributeValue);
        inOrder.verify(mockPrinter).print("\"");
        if (isStart) {
            inOrder.verify(mockPrinter).print(">");
        } else {
            inOrder.verify(mockPrinter).print("/>");
        }
        verifyNoMoreInteractions(mockPrinter);

    }
}
