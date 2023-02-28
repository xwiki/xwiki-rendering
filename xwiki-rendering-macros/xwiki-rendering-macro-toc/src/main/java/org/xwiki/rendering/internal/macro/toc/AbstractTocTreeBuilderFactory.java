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
package org.xwiki.rendering.internal.macro.toc;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.macro.toc.TocEntriesResolver;
import org.xwiki.rendering.macro.toc.TocEntryExtension;
import org.xwiki.rendering.macro.toc.TocTreeBuilderFactory;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.renderer.reference.link.LinkLabelGenerator;

/**
 * Provides a default {@link TocBlockFilter} with a {@code plain/1.0} parser and the default
 * {@link LinkLabelGenerator}.
 *
 * @version $Id$
 * @since 15.2RC1
 */
public abstract class AbstractTocTreeBuilderFactory implements TocTreeBuilderFactory
{
    @Inject
    private ComponentManager componentManager;

    /**
     * A parser that knows how to parse plain text; this is used to transform link labels into plain text.
     */
    @Inject
    @Named("plain/1.0")
    private Parser plainTextParser;

    /**
     * Generate link label.
     */
    @Inject
    private LinkLabelGenerator linkLabelGenerator;

    protected TocBlockFilter getTocBlockFilter()
    {
        return new TocBlockFilter(this.plainTextParser, this.linkLabelGenerator);
    }

    protected TocEntriesResolver getTocEntriesResolver(String resolverHint) throws ComponentLookupException
    {
        return this.componentManager.getInstance(TocEntriesResolver.class, resolverHint);
    }

    protected List<TocEntryExtension> getExtensions() throws ComponentLookupException
    {
        return this.componentManager.getInstanceList(TocEntryExtension.class);
    }
}
