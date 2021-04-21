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
package org.xwiki.rendering.internal.parser.html;

import java.io.Reader;
import java.io.StringReader;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.internal.parser.xhtml.XHTMLParser;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.xml.html.HTMLCleaner;
import org.xwiki.xml.html.HTMLUtils;

import static org.xwiki.rendering.internal.html.HTML401SyntaxProvider.HTML_4_01;

/**
 * Parses HTML and generate a {@link XDOM} object.
 *
 * @version $Id$
 * @since 1.5M2
 */
@Component
@Named("html/4.01")
@Singleton
public class HTMLParser extends XHTMLParser
{
    /**
     * Used to clean the HTML into valid XHTML. Injected by the Component Manager.
     */
    @Inject
    private HTMLCleaner htmlCleaner;

    @Override
    public Syntax getSyntax()
    {
        return HTML_4_01;
    }

    @Override
    public XDOM parse(Reader source) throws ParseException
    {
        return super.parse(new StringReader(HTMLUtils.toString(this.htmlCleaner.clean(source))));
    }

    @Override
    public void parse(Reader source, Listener listener) throws ParseException
    {
        super.parse(new StringReader(HTMLUtils.toString(this.htmlCleaner.clean(source))), listener);
    }
}
