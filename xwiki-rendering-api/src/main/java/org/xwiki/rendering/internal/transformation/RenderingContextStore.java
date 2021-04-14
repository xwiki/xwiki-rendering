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
package org.xwiki.rendering.internal.transformation;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.context.internal.concurrent.AbstractContextStore;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.syntax.SyntaxRegistry;
import org.xwiki.rendering.transformation.RenderingContext;

/**
 * Save and restore well known {@link RenderingContext} entries.
 * 
 * @version $Id$
 * @since 11.8RC1
 */
@Component
@Singleton
@Named("rendering")
public class RenderingContextStore extends AbstractContextStore
{
    /**
     * The prefix of all the rendering context entries names.
     */
    public static final String PROP_PREFIX = "rendering.";

    /**
     * Name of the entry corresponding to {@link RenderingContext#getDefaultSyntax()}.
     */
    public static final String PROP_DEFAULTSYNTAX = PROP_PREFIX + "defaultsyntax";

    /**
     * Name of the entry corresponding to {@link RenderingContext#getTargetSyntax()}.
     */
    public static final String PROP_TARGETSYNTAX = PROP_PREFIX + "targetsyntax";

    /**
     * Name of the entry corresponding to {@link RenderingContext#isRestricted()}.
     */
    public static final String PROP_RESTRICTED = PROP_PREFIX + "restricted";

    // TODO: add support for XDOM and current lock

    @Inject
    private RenderingContext context;

    @Inject
    private Logger logger;

    @Inject
    private SyntaxRegistry syntaxRegistry;

    /**
     * Default constructor.
     */
    public RenderingContextStore()
    {
        super(PROP_DEFAULTSYNTAX, PROP_TARGETSYNTAX, PROP_RESTRICTED);
    }

    @Override
    public void save(Map<String, Serializable> contextStore, Collection<String> entries)
    {
        save(contextStore, PROP_DEFAULTSYNTAX, this.context.getDefaultSyntax(), entries);
        save(contextStore, PROP_TARGETSYNTAX, this.context.getTargetSyntax(), entries);
    }

    private void save(Map<String, Serializable> contextStore, String key, Syntax value, Collection<String> entries)
    {
        if (entries.contains(key) && value != null) {
            contextStore.put(key, value.toIdString());
        }
    }

    @Override
    public void restore(Map<String, Serializable> contextStore)
    {
        MutableRenderingContext mutableContext = (MutableRenderingContext) this.context;

        mutableContext.push(mutableContext.getTransformation(), mutableContext.getXDOM(),
            getSyntax(contextStore, PROP_DEFAULTSYNTAX), null, get(contextStore, PROP_RESTRICTED, false),
            getSyntax(contextStore, PROP_TARGETSYNTAX));
    }

    private Syntax getSyntax(Map<String, Serializable> contextStore, String key)
    {
        if (contextStore.containsKey(key)) {
            Serializable value = contextStore.get(key);

            if (value != null) {
                try {
                    return this.syntaxRegistry.resolveSyntax(value.toString());
                } catch (ParseException e) {
                    this.logger.warn("Failed to restore the Syntax for key [{}]", key, e);
                }
            }
        }

        return null;
    }
}
