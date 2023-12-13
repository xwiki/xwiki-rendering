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
package org.xwiki.rendering.internal.parser.html5;

import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.internal.parser.xhtml5.XHTML5Parser;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.util.IdGenerator;
import org.xwiki.xml.html.HTMLCleaner;
import org.xwiki.xml.html.HTMLCleanerConfiguration;
import org.xwiki.xml.html.HTMLUtils;

import static org.xwiki.rendering.internal.html5.HTML5SyntaxProvider.HTML_5_0;

/**
 * Parses a subset of HTML5 and generates a {@code XDOM} object.
 *
 * @version $Id$
 * @since 14.1RC1
 */
@Component
@Named("html/5.0")
public class HTML5Parser extends XHTML5Parser
{
    /**
     * Used to clean the HTML into valid XHTML.
     */
    @Inject
    private HTMLCleaner htmlCleaner;

    @Override
    public XDOM parse(Reader source) throws ParseException
    {
        return super.parse(
            new StringReader(HTMLUtils.toString(this.htmlCleaner.clean(source, getHTMLCleanerConfiguration()))));
    }

    @Override
    public XDOM parse(Reader source, IdGenerator idGenerator) throws ParseException
    {
        return super.parse(
            new StringReader(HTMLUtils.toString(this.htmlCleaner.clean(source, getHTMLCleanerConfiguration()))),
            idGenerator);
    }

    @Override
    public void parse(Reader source, Listener listener) throws ParseException
    {
        super.parse(new StringReader(HTMLUtils.toString(this.htmlCleaner.clean(source, getHTMLCleanerConfiguration()))),
            listener);
    }

    @Override
    public void parse(Reader source, Listener listener, IdGenerator idGenerator) throws ParseException
    {
        super.parse(new StringReader(HTMLUtils.toString(this.htmlCleaner.clean(source, getHTMLCleanerConfiguration()))),
            listener, idGenerator);
    }

    @Override
    public Syntax getSyntax()
    {
        return HTML_5_0;
    }

    private HTMLCleanerConfiguration getHTMLCleanerConfiguration()
    {
        HTMLCleanerConfiguration configuration = this.htmlCleaner.getDefaultConfiguration();
        Map<String, String> parameters = new HashMap<>(configuration.getParameters());
        parameters.put(HTMLCleanerConfiguration.HTML_VERSION, "5");
        configuration.setParameters(parameters);
        return configuration;
    }
}
