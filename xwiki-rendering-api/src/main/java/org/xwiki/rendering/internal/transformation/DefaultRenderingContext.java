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

import java.util.ArrayDeque;
import java.util.Deque;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.transformation.Transformation;
import org.xwiki.rendering.transformation.TransformationContext;
import org.xwiki.rendering.transformation.TransformationException;

/**
 * The complete context of the transformation process.
 *
 * @version $Id$
 * @since 6.0
 */
@Component
@Singleton
public class DefaultRenderingContext implements MutableRenderingContext
{
    /**
     * Key of the this context in the execution context.
     */
    private static final String EXECUTION_CONTEXT_KEY = "rendering.context";

    /**
     * A null context to avoid special cases.
     */
    private static final Context NULL_CONTEXT = new Context();

    /**
     * Used to access the rendering context stack from the execution context.
     */
    @Inject
    private Execution execution;

    protected static final class Context implements Cloneable
    {
        /**
         * The complete {@link org.xwiki.rendering.block.XDOM} of the content currently being transformed.
         */
        private final XDOM xdom;

        /**
         * The block current block processed by the transformation (ie the macro block).
         */
        private Block currentBlock;

        /**
         * The current syntax of transformation.
         */
        private final Syntax syntax;

        /**
         * In restricted mode, only transformations that are deemed safe for execution by untrusted users will be
         * performed.
         */
        private final boolean restricted;

        /**
         * The current Transformation instance being executed.
         */
        private final Transformation transformation;

        /**
         * An id representing the transformation being evaluated.
         */
        private final String transformationId;

        /**
         * The syntax of the renderer.
         */
        private Syntax targetSyntax;

        /**
         * Create a null context.
         */
        private Context()
        {
            this(null, null, null, null, false, null);
        }

        /**
         * Initialize a new rendering context.
         *
         * @param transformation the transformation being performed.
         * @param xdom the complete XDOM being processed.
         * @param syntax the current syntax.
         * @param transformationId the id of the transformation.
         * @param restricted true if the transformation is restricted.
         */
        private Context(Transformation transformation, XDOM xdom, Syntax syntax, String transformationId,
            boolean restricted, Syntax targetSyntax)
        {
            this.transformationId = transformationId;
            this.xdom = xdom;
            this.syntax = syntax;
            this.restricted = restricted;
            this.transformation = transformation;
            this.targetSyntax = targetSyntax;
        }

        public String getTransformationId()
        {
            return this.transformationId;
        }

        @Override
        public Context clone()
        {
            Context newContext;
            try {
                newContext = (Context) super.clone();
            } catch (CloneNotSupportedException e) {
                // Should never happen
                throw new RuntimeException("Failed to clone object", e);
            }

            return newContext;
        }
    }

    @Override
    public void push(Transformation transformation, TransformationContext context)
    {
        push(transformation, context.getXDOM(), context.getSyntax(), context.getId(), context.isRestricted(),
            context.getTargetSyntax());
    }

    @Override
    public void push(Transformation transformation, XDOM xdom, Syntax syntax, String id, boolean restricted,
        Syntax targetSyntax)
    {
        Deque<Context> stack = getContextStack(true);
        if (stack != null) {
            stack.push(new Context(transformation, xdom, syntax, id, restricted, targetSyntax));
        }
    }

    @Override
    public void pop()
    {
        Deque<Context> stack = getContextStack(false);
        if (stack != null) {
            stack.pop();
        }
    }

    @Override
    public void transformInContext(Transformation transformation, TransformationContext context, Block block)
        throws TransformationException
    {
        try {
            push(transformation, context);
            transformation.transform(block, context);
        } finally {
            pop();
        }
    }

    @SuppressWarnings("unchecked")
    private Deque<Context> getContextStack(boolean create)
    {
        ExecutionContext context = this.execution.getContext();

        if (context != null) {
            Deque<Context> stack = (Deque<Context>) context.getProperty(EXECUTION_CONTEXT_KEY);

            if (stack == null && create) {
                stack = new ArrayDeque<>();
                context.setProperty(EXECUTION_CONTEXT_KEY, stack);
            }

            return stack;
        }

        return null;
    }

    protected Context peek()
    {
        Deque<Context> stack = getContextStack(false);
        return (stack != null && !stack.isEmpty()) ? stack.peek() : NULL_CONTEXT;
    }

    @Override
    public XDOM getXDOM()
    {
        return peek().xdom;
    }

    @Override
    public Block getCurrentBlock()
    {
        return peek().currentBlock;
    }

    @Override
    public void setCurrentBlock(Block block)
    {
        Context context = peek();
        if (context != null && context != NULL_CONTEXT) {
            context.currentBlock = block;
        }
    }

    @Override
    public Syntax getDefaultSyntax()
    {
        return peek().syntax;
    }

    @Override
    public boolean isRestricted()
    {
        return peek().restricted;
    }

    @Override
    public Transformation getTransformation()
    {
        return peek().transformation;
    }

    @Override
    public String getTransformationId()
    {
        return peek().transformationId;
    }

    @Override
    public Syntax getTargetSyntax()
    {
        return peek().targetSyntax;
    }

    @Override
    public void setTargetSyntax(Syntax targetSyntax)
    {
        Context context = peek();
        if (context != null && context != NULL_CONTEXT) {
            context.targetSyntax = targetSyntax;
        }
    }
}
