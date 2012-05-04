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
package org.xwiki.rendering.xdomxmlcurrent.internal.renderer;

import java.lang.reflect.Proxy;

import javax.inject.Inject;

import org.xwiki.component.annotation.Component;
import org.xwiki.properties.ConverterManager;
import org.xwiki.rendering.internal.renderer.xml.AbstractRenderer;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.listener.descriptor.ListenerDescriptorManager;
import org.xwiki.rendering.renderer.xml.ContentHandlerStreamRenderer;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.xdomxmlcurrent.internal.parameter.ParameterManager;

@Component("xdom+xml/current")
public class XDOMXMLRenderer extends AbstractRenderer
{
    @Inject
    private ParameterManager parameterManager;

    @Inject
    private ListenerDescriptorManager descriptorManager;

    @Inject
    private ConverterManager converter;

    @Override
    public Syntax getSyntax()
    {
        return Syntax.XDOMXML_CURRENT;
    }

    @Override
    protected ContentHandlerStreamRenderer createContentHandlerStreamRenderer()
    {
        XDOMXMLChainingStreamRenderer handler =
            new XDOMXMLChainingStreamRenderer(this.parameterManager,
                this.descriptorManager.getListenerDescriptor(Listener.class), this.converter);
        ContentHandlerStreamRenderer instance =
            (ContentHandlerStreamRenderer) Proxy.newProxyInstance(ContentHandlerStreamRenderer.class.getClassLoader(),
                new Class[] {ContentHandlerStreamRenderer.class}, handler);

        return instance;
    }
}
