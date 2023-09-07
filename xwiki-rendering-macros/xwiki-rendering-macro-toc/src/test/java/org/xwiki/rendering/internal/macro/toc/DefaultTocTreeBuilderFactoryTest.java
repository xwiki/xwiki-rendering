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

import javax.inject.Named;

import org.junit.jupiter.api.Test;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.macro.toc.TocEntriesResolver;
import org.xwiki.rendering.macro.toc.TocEntryExtension;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.renderer.reference.link.LinkLabelGenerator;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static org.mockito.Mockito.verify;

/**
 * Test of {@link DefaultTocTreeBuilderFactory}.
 *
 * @version $Id$
 */
@ComponentTest
class DefaultTocTreeBuilderFactoryTest
{
    @InjectMockComponents
    private DefaultTocTreeBuilderFactory factory;

    /**
     * A parser that knows how to parse plain text; this is used to transform link labels into plain text.
     */
    @MockComponent
    @Named("plain/1.0")
    private Parser plainTextParser;

    /**
     * Generate link label.
     */
    @MockComponent
    private LinkLabelGenerator linkLabelGenerator;

    @MockComponent
    private TocEntriesResolver tocEntriesResolver;

    @MockComponent
    @Named("context")
    private ComponentManager componentManager;

    @Test
    void build() throws Exception
    {
        this.factory.build(null);
        verify(this.componentManager).getInstance(TocEntriesResolver.class, null);
        verify(this.componentManager).getInstanceList(TocEntryExtension.class);
    }
}
