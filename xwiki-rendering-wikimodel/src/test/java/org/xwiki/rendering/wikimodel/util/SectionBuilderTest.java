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
package org.xwiki.rendering.wikimodel.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @version $Id$
 * @since 4.0M1
 */
class SectionBuilderTest
{
    protected static class PrintListener implements ISectionListener<String>
    {
        final StringBuffer fBuf = new StringBuffer();

        public void beginDocument(IPos<String> pos)
        {
            this.fBuf.append("<doc" + n(pos) + ">");
        }

        public void beginSection(IPos<String> pos)
        {
            this.fBuf.append("<s" + n(pos) + ">");
        }

        public void beginSectionContent(IPos<String> pos)
        {
            this.fBuf.append("<c" + n(pos) + ">");
        }

        public void beginSectionHeader(IPos<String> pos)
        {
            this.fBuf.append("<h" + n(pos) + ">");
        }

        public void endDocument(IPos<String> pos)
        {
            this.fBuf.append("</doc" + n(pos) + ">");
        }

        public void endSection(IPos<String> pos)
        {
            this.fBuf.append("</s" + n(pos) + ">");
        }

        public void endSectionContent(IPos<String> pos)
        {
            this.fBuf.append("</c" + n(pos) + ">");
        }

        public void endSectionHeader(IPos<String> pos)
        {
            this.fBuf.append("</h" + n(pos) + ">");
        }

        private String n(IPos<String> pos)
        {
            return "[" + pos.getData() + "]" + "-" + pos.getDocumentLevel()
                + "-" + pos.getHeaderLevel();
        }

        @Override
        public String toString()
        {
            return this.fBuf.toString();
        }
    }

    SectionBuilder<String> fBuilder;

    private PrintListener fListener;

    private String check(String prev, String delta)
    {
        String test = this.fListener.toString();
        assertEquals(prev + delta, test);
        return test;
    }

    @BeforeEach
    void setUp()
    {
        this.fListener = new PrintListener();
        this.fBuilder = new SectionBuilder<>(this.fListener);
    }

    @Test
    void testDocumentLevels()
    {
        String prev = "";

        this.fBuilder.beginDocument("X");
        prev = check(prev, "<doc[X]-1-0>");
        this.fBuilder.beginDocument("Y");
        prev = check(prev, "<doc[Y]-2-0>");
        this.fBuilder.endDocument();
        prev = check(prev, "</doc[Y]-2-0>");
        this.fBuilder.endDocument();
        check(prev, "</doc[X]-1-0>");
    }

    @Test
    void testHeaderLevels()
    {
        this.fBuilder.beginDocument("X");
        String prev = "";
        prev = check(prev, "<doc[X]-1-0>");

        this.fBuilder.beginHeader(1, "A");
        prev = check(prev, "<s[A]-1-1><h[A]-1-1>");
        this.fBuilder.endHeader();
        prev = check(prev, "</h[A]-1-1><c[A]-1-1>");

        this.fBuilder.beginHeader(3, "B");
        prev = check(prev, "<s[B]-1-2><c[B]-1-2><s[B]-1-3><h[B]-1-3>");
        this.fBuilder.endHeader();
        prev = check(prev, "</h[B]-1-3><c[B]-1-3>");

        this.fBuilder.beginHeader(3, "C");
        prev = check(prev, "</c[B]-1-3></s[B]-1-3><s[C]-1-3><h[C]-1-3>");
        this.fBuilder.endHeader();
        prev = check(prev, "</h[C]-1-3><c[C]-1-3>");

        this.fBuilder.beginHeader(2, "D");
        prev = check(prev,
            "</c[C]-1-3></s[C]-1-3></c[B]-1-2></s[B]-1-2><s[D]-1-2><h[D]-1-2>");
        this.fBuilder.endHeader();
        prev = check(prev, "</h[D]-1-2><c[D]-1-2>");

        this.fBuilder.beginHeader(3, "E");
        prev = check(prev, "<s[E]-1-3><h[E]-1-3>");
        this.fBuilder.endHeader();
        prev = check(prev, "</h[E]-1-3><c[E]-1-3>");

        this.fBuilder.endDocument();
        check(prev, "</c[E]-1-3></s[E]-1-3>" + "</c[D]-1-2></s[D]-1-2>"
            + "</c[A]-1-1></s[A]-1-1>" + "</doc[X]-1-0>");
    }

    @Test
    void testInSubDocument()
    {
        String prev = "";
        this.fBuilder.beginDocument("X");
        prev = check(prev, "<doc[X]-1-0>");

        this.fBuilder.beginHeader(1, "A");
        prev = check(prev, "<s[A]-1-1><h[A]-1-1>");
        this. fBuilder.endHeader();
        prev = check(prev, "</h[A]-1-1><c[A]-1-1>");

        // BEGIN DOCUMENT Y [

        this. fBuilder.beginDocument("Y");
        prev = check(prev, "<doc[Y]-2-0>");

        this. fBuilder.beginHeader(1, "YA");
        prev = check(prev, "<s[YA]-2-1><h[YA]-2-1>");
        this. fBuilder.endHeader();
        prev = check(prev, "</h[YA]-2-1><c[YA]-2-1>");

        this. fBuilder.endDocument();
        prev = check(prev, "</c[YA]-2-1></s[YA]-2-1></doc[Y]-2-0>");

        // ] END DOCUMENT Y

        this.fBuilder.beginHeader(2, "B");
        prev = check(prev, "<s[B]-1-2><h[B]-1-2>");
        this.fBuilder.endHeader();
        prev = check(prev, "</h[B]-1-2><c[B]-1-2>");

        this.fBuilder.endDocument();
        check(prev, "</c[B]-1-2></s[B]-1-2>" + "</c[A]-1-1></s[A]-1-1>"
            + "</doc[X]-1-0>");
    }
}
