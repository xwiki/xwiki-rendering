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
package org.xwiki.rendering.xdomxml.internal.version10.parser.parameter;

import java.util.HashSet;
import java.util.Set;

import org.xml.sax.SAXException;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.rendering.xdomxml.internal.parser.DefaultBlockParser;
import org.xwiki.rendering.xdomxml.internal.parser.parameters.ValueParser;

public class ResourceReference10Parser extends DefaultBlockParser implements ValueParser<ResourceReference>
{
    private static final Set<String> NAMES = new HashSet<String>()
    {
        {
            add("type");
            add("reference");
            add("typed");
        }
    };

    public ResourceReference reference;

    public ResourceReference10Parser()
    {
        super(NAMES);
    }

    @Override
    public ResourceReference getValue()
    {
        return this.reference;
    }

    @Override
    protected void endBlock() throws SAXException
    {
        this.reference =
            new ResourceReference(getParameterAsString("reference", null), new ResourceType(getParameterAsString(
                "type", "path")));
        this.reference.setTyped(getParameterAsBoolean("typed", true));
        this.reference.setParameters(getCustomParameters());
    }
}
