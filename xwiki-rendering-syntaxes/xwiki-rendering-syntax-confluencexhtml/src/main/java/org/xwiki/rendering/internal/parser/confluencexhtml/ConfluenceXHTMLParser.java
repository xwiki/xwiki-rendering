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
package org.xwiki.rendering.internal.parser.confluencexhtml;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.io.IOUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.internal.parser.confluencexhtml.wikimodel.AttachmentTagHandler;
import org.xwiki.rendering.internal.parser.confluencexhtml.wikimodel.ConfluenceXWikiGeneratorListener;
import org.xwiki.rendering.internal.parser.confluencexhtml.wikimodel.ImageTagHandler;
import org.xwiki.rendering.internal.parser.confluencexhtml.wikimodel.LinkTagHandler;
import org.xwiki.rendering.internal.parser.confluencexhtml.wikimodel.MacroTagHandler;
import org.xwiki.rendering.internal.parser.confluencexhtml.wikimodel.PageTagHandler;
import org.xwiki.rendering.internal.parser.confluencexhtml.wikimodel.ParameterTagHandler;
import org.xwiki.rendering.internal.parser.confluencexhtml.wikimodel.PlainTextBodyTagHandler;
import org.xwiki.rendering.internal.parser.confluencexhtml.wikimodel.PlainTextLinkBodyTagHandler;
import org.xwiki.rendering.internal.parser.confluencexhtml.wikimodel.SpaceTagHandler;
import org.xwiki.rendering.internal.parser.confluencexhtml.wikimodel.URLTagHandler;
import org.xwiki.rendering.internal.parser.confluencexhtml.wikimodel.UserTagHandler;
import org.xwiki.rendering.internal.parser.wikimodel.AbstractWikiModelParser;
import org.xwiki.rendering.internal.parser.wikimodel.XWikiGeneratorListener;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.ResourceReferenceParser;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.util.IdGenerator;
import org.xwiki.rendering.wikimodel.IWikiParser;
import org.xwiki.rendering.wikimodel.xhtml.XhtmlParser;
import org.xwiki.rendering.wikimodel.xhtml.handler.TagHandler;

/**
 * Parses Confluence XHTML and generate rendering events.
 * 
 * @version $Id$
 * @since 5.3M2
 */
@Component
@Named("confluence+xhtml/1.0")
@Singleton
public class ConfluenceXHTMLParser extends AbstractWikiModelParser
{
    /**
     * @see #getLinkReferenceParser()
     */
    @Inject
    @Named("link")
    private ResourceReferenceParser linkReferenceParser;

    /**
     * @see #getImageReferenceParser()
     */
    @Inject
    @Named("image")
    private ResourceReferenceParser imageReferenceParser;

    @Override
    public Syntax getSyntax()
    {
        return Syntax.CONFLUENCEXHTML_1_0;
    }

    @Override
    public IWikiParser createWikiModelParser() throws ParseException
    {
        XhtmlParser parser = new XhtmlParser();

        parser.setNamespacesEnabled(false);

        // Override some of the WikiModel XHTML parser tag handlers to introduce our own logic.
        Map<String, TagHandler> handlers = new HashMap<String, TagHandler>();

        handlers.put("ac:macro", new MacroTagHandler());
        handlers.put("ac:parameter", new ParameterTagHandler());
        handlers.put("ac:plain-text-body", new PlainTextBodyTagHandler());

        handlers.put("ac:image", new ImageTagHandler());
        handlers.put("ri:url", new URLTagHandler());

        handlers.put("ac:link", new LinkTagHandler());
        handlers.put("ri:page", new PageTagHandler());
        handlers.put("ri:space", new SpaceTagHandler());
        handlers.put("ri:user", new UserTagHandler());
        handlers.put("ac:plain-text-link-body", new PlainTextLinkBodyTagHandler());

        handlers.put("ri:attachment", new AttachmentTagHandler());

        parser.setExtraHandlers(handlers);

        return parser;
    }

    @Override
    protected void parse(final Reader source, Listener listener, IdGenerator idGenerator) throws ParseException
    {
        String content;
        try {
            content = IOUtils.toString(source);
        } catch (IOException e) {
            throw new ParseException("Failed to read source", e);
        }

        // Confluence generate invalid CDATA (nice touch...)
        content = content.replaceAll("(<!\\[CDATA\\[[^\\]]*\\]\\]) (>)", "$1$2");

        // Add <void> element around the content to make sure to have valid xml
        content = "<void>" + content + "</void>";

        // Add XHTML entities
        content =
            "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\""
                + " \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">" + content;

        super.parse(new StringReader(content), listener, idGenerator);
    }

    @Override
    public ResourceReferenceParser getLinkReferenceParser()
    {
        return this.linkReferenceParser;
    }

    @Override
    public ResourceReferenceParser getImageReferenceParser()
    {
        return this.imageReferenceParser;
    }

    @Override
    public XWikiGeneratorListener createXWikiGeneratorListener(Listener listener, IdGenerator idGenerator)
    {
        return new ConfluenceXWikiGeneratorListener(getLinkLabelParser(), listener, getLinkReferenceParser(),
            getImageReferenceParser(), this.plainRendererFactory, idGenerator, getSyntax());
    }
}
