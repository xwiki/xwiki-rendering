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
package org.xwiki.rendering.wikimodel.test;

import java.io.StringReader;

import org.xwiki.rendering.wikimodel.IWemListener;
import org.xwiki.rendering.wikimodel.IWikiParser;
import org.xwiki.rendering.wikimodel.IWikiPrinter;
import org.xwiki.rendering.wikimodel.WikiParameters;
import org.xwiki.rendering.wikimodel.WikiParserException;
import org.xwiki.rendering.wikimodel.xhtml.PrintListener;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @version $Id$
 * @since 4.0M1
 */
public abstract class AbstractWikiParserTest
{
    private boolean fShowSections;

    protected boolean supportImage;

    protected boolean supportDownload;

    protected void checkResults(String control, String test)
    {
        if (control != null) {
            control = "<div class='wikimodel-document'>\n" + control + "\n</div>\n";
            assertEquals(control, test);
        }
    }

    protected IWemListener newParserListener(final StringBuffer buf)
    {
        IWikiPrinter printer = newPrinter(buf);
        IWemListener listener;
        if (!fShowSections) {
            listener = new PrintListener(printer, this.supportImage, this.supportDownload);
        } else {
            listener = new PrintListener(printer, this.supportImage, this.supportDownload)
            {
                @Override
                public void beginSection(int docLevel, int headerLevel, WikiParameters params)
                {
                    println("<section-" + docLevel + "-" + headerLevel + params + ">");
                }

                @Override
                public void beginSectionContent(int docLevel, int headerLevel, WikiParameters params)
                {
                    println("<sectionContent-" + docLevel + "-" + headerLevel + params + ">");
                }

                @Override
                public void endSection(int docLevel, int headerLevel, WikiParameters params)
                {
                    println("</section-" + docLevel + "-" + headerLevel + ">");
                }

                @Override
                public void endSectionContent(int docLevel, int headerLevel, WikiParameters params)
                {
                    println("</sectionContent-" + docLevel + "-" + headerLevel + ">");
                }
            };
        }
        return listener;
    }

    protected IWikiPrinter newPrinter(final StringBuffer buf)
    {
        return new IWikiPrinter()
        {
            public void print(String str)
            {
                buf.append(str);
            }

            public void println(String str)
            {
                buf.append(str);
                buf.append("\n");
            }
        };
    }

    protected abstract IWikiParser newWikiParser();

    protected void showSections(boolean b)
    {
        fShowSections = b;
    }

    protected void test(String string) throws WikiParserException
    {
        test(string, null);
    }

    protected void test(String string, String control) throws WikiParserException
    {
        StringReader reader = new StringReader(string);
        IWikiParser parser = newWikiParser();
        final StringBuffer buf = new StringBuffer();
        IWemListener listener = newParserListener(buf);
        parser.parse(reader, listener);
        String test = buf.toString();
        checkResults(control, test);
    }
}