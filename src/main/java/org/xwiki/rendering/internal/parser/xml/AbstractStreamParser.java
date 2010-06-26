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
package org.xwiki.rendering.internal.parser.xml;

import java.io.Reader;

import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.StreamParser;
import org.xwiki.rendering.parser.xml.ContentHandlerStreamParser;
import org.xwiki.rendering.parser.xml.ContentHandlerStreamParserFactory;

/**
 * 
 * @version $Id$
 */
public abstract class AbstractStreamParser implements ContentHandlerStreamParserFactory, StreamParser
{
    @Requirement
    private ComponentManager componentManager;

    public ContentHandlerStreamParser createParser(Listener listener)
    {
        ContentHandlerStreamParser parser;
        try {
            parser = this.componentManager.lookup(ContentHandlerStreamParser.class, getSyntax().toIdString());
        } catch (ComponentLookupException e) {
            throw new RuntimeException(
                "Failed to create [" + getSyntax().toString() + "] ContentHandler stream parser", e);
        }

        parser.setListener(listener);

        return parser;
    }

    public void parse(Reader source, Listener listener) throws ParseException
    {
        // TODO Auto-generated method stub

    }
}
