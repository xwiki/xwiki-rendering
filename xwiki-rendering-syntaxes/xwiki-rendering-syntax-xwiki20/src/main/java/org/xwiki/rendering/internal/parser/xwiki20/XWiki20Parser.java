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
package org.xwiki.rendering.internal.parser.xwiki20;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.internal.parser.wikimodel.AbstractWikiModelParser;
import org.xwiki.rendering.internal.xwiki20.XWiki20SyntaxProvider;
import org.xwiki.rendering.parser.ResourceReferenceParser;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.wikimodel.IWikiParser;
import org.xwiki.rendering.wikimodel.xwiki.xwiki20.XWikiParser;

/**
 * @version $Id$
 * @since 2.1M1
 */
@Component
@Named("xwiki/2.0")
@Singleton
public class XWiki20Parser extends AbstractWikiModelParser
{
    /**
     * @see #getLinkReferenceParser()
     */
    @Inject
    @Named("xwiki/2.0/link")
    private ResourceReferenceParser linkReferenceParser;

    /**
     * @see #getImageReferenceParser()
     */
    @Inject
    @Named("xwiki/2.0/image")
    private ResourceReferenceParser imageReferenceParser;

    @Override
    public Syntax getSyntax()
    {
        return XWiki20SyntaxProvider.XWIKI_2_0;
    }

    @Override
    public IWikiParser createWikiModelParser()
    {
        return new XWikiParser();
    }

    @Override
    public ResourceReferenceParser getLinkReferenceParser()
    {
        return this.linkReferenceParser;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.rendering.internal.parser.wikimodel.AbstractWikiModelParser#getImageReferenceParser()
     * @since 2.5RC1
     */
    @Override
    public ResourceReferenceParser getImageReferenceParser()
    {
        return this.imageReferenceParser;
    }
}
