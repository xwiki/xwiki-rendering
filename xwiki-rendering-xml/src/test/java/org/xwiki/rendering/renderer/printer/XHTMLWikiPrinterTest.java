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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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
}
