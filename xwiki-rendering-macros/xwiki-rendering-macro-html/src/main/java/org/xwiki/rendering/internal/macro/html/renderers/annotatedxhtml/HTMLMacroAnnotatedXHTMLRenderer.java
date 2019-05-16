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
package org.xwiki.rendering.internal.macro.html.renderers.annotatedxhtml;

import javax.inject.Inject;
import javax.inject.Named;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.rendering.internal.macro.html.HTMLMacroBlockStateChainingListener;
import org.xwiki.rendering.internal.macro.html.HTMLMacroChainingRenderer;
import org.xwiki.rendering.internal.renderer.xhtml.AnnotatedXHTMLChainingRenderer;
import org.xwiki.rendering.internal.renderer.xhtml.image.XHTMLImageRenderer;
import org.xwiki.rendering.internal.renderer.xhtml.link.XHTMLLinkRenderer;
import org.xwiki.rendering.listener.chaining.EmptyBlockChainingListener;
import org.xwiki.rendering.listener.chaining.ListenerChain;
import org.xwiki.rendering.listener.chaining.MetaDataStateChainingListener;
import org.xwiki.rendering.renderer.AbstractChainingPrintRenderer;
import org.xwiki.stability.Unstable;

/**
 * Renderer that generates Annotated XHTML from a XDOM resulting from the parsing of text containing HTML mixed with
 * wiki syntax.
 *
 * @version $Id$
 * @since 11.4RC1
 */
@Component
@Named("htmlmacro+annotatedxhtml/1.0")
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
@Unstable
public class HTMLMacroAnnotatedXHTMLRenderer extends AbstractChainingPrintRenderer implements Initializable
{
    /**
     * To render link events into XHTML. This is done so that it's pluggable because link rendering depends on how the
     * underlying system wants to handle it. For example for XWiki we check if the document exists, we get the document
     * URL, etc.
     */
    @Inject
    @Named("annotated")
    private XHTMLLinkRenderer linkRenderer;

    /**
     * To render image events into XHTML. This is done so that it's pluggable because image rendering depends on how the
     * underlying system wants to handle it. For example for XWiki we check if the image exists as a document
     * attachments, we get its URL, etc.
     */
    @Inject
    @Named("annotated")
    private XHTMLImageRenderer imageRenderer;

    @Override
    public void initialize() throws InitializationException
    {
        ListenerChain chain = new ListenerChain();
        setListenerChain(chain);

        // Construct the listener chain in the right order. Listeners early in the chain are called before listeners
        // placed later in the chain.
        chain.addListener(this);
        chain.addListener(new HTMLMacroBlockStateChainingListener(chain));
        chain.addListener(new EmptyBlockChainingListener(chain));
        chain.addListener(new MetaDataStateChainingListener(chain));
        chain.addListener(new HTMLMacroChainingRenderer(
            new AnnotatedXHTMLChainingRenderer(this.linkRenderer, this.imageRenderer, chain))
        );
    }
}
