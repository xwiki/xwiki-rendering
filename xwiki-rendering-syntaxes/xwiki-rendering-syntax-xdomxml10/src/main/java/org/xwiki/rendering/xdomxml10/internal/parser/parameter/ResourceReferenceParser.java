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
package org.xwiki.rendering.xdomxml10.internal.parser.parameter;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.rendering.xdomxml10.internal.parser.DefaultBlockParser;

// TODO: Work around the fact that DefaultBlockParser is a Component while ResourceReferenceParser is not supposed to
// be one. This is a bad design since a non-component should not extend a component (it's dangerous - @Inject-ed
// component will not be injected, and the extending class will inherit the @Component annotation, which is bad -
// imagine for example that in the future we auto-generate components.txt based on the @Component annotation).
/**
 * Parses a resource reference parameter.
 *
 * @version $Id$
 */
@Component(staticRegistration = false)
public class ResourceReferenceParser extends DefaultBlockParser implements ValueParser<ResourceReference>
{
    private static final String TYPE = "type";

    private static final String REFERENCE = "reference";

    private static final String TYPED = "typed";

    private static final Set<String> NAMES = Stream.of(TYPE, REFERENCE, TYPED).collect(Collectors.toSet());

    /**
     * The parsed resource reference.
     */
    public ResourceReference reference;

    /**
     * Default constructor.
     */
    public ResourceReferenceParser()
    {
        super(NAMES);
    }

    @Override
    public ResourceReference getValue()
    {
        return this.reference;
    }

    @Override
    protected void endBlock()
    {
        this.reference =
            new ResourceReference(getParameterAsString(REFERENCE, null), new ResourceType(getParameterAsString(
                TYPE, "path")));
        this.reference.setTyped(getParameterAsBoolean(TYPED, true));
        this.reference.setParameters(getCustomParameters());
    }
}
