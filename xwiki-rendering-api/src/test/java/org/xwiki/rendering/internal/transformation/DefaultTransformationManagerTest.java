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

import java.util.Arrays;

import javax.inject.Named;
import javax.inject.Provider;

import org.junit.jupiter.api.Test;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.configuration.RenderingConfiguration;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.transformation.AbstractTransformation;
import org.xwiki.rendering.transformation.RenderingContext;
import org.xwiki.rendering.transformation.Transformation;
import org.xwiki.rendering.transformation.TransformationContext;
import org.xwiki.rendering.transformation.TransformationException;
import org.xwiki.test.annotation.BeforeComponent;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;
import org.xwiki.test.mockito.MockitoComponentManager;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DefaultTransformationManager}.
 *
 * @version $Id$
 */
@ComponentTest
class DefaultTransformationManagerTest
{
    @InjectMockComponents
    private DefaultTransformationManager transformationManager;

    @MockComponent
    private RenderingConfiguration renderingConfiguration;

    @InjectComponentManager
    private MockitoComponentManager componentManager;

    @MockComponent
    @Named("context")
    private Provider<ComponentManager> componentManagerProvider;

    public class Transformation1 extends AbstractTransformation
    {
        @Override
        public void transform(Block block, TransformationContext context)
        {
            // No need to do anything for the test
        }
    }

    public class Transformation2 extends AbstractTransformation
    {
        @Override
        public void transform(Block block, TransformationContext context)
        {
            // No need to do anything for the test
        }
    }

    @Test
    void performTransformationsWhenNoTransformation() throws Exception
    {
        this.transformationManager.performTransformations(XDOM.EMPTY,
            new TransformationContext(XDOM.EMPTY, Syntax.XWIKI_2_0));
    }

    @BeforeComponent("performTransformationsWhenErrorsInTransformations")
    void beforePerformTransformationsWhenErrorsInTransformations() throws Exception
    {
        MutableRenderingContext mrc = mock(MutableRenderingContext.class);
        this.componentManager.registerComponent(RenderingContext.class, mrc);
        doThrow(new TransformationException("error")).when(mrc).transformInContext(any(Transformation.class),
            any(TransformationContext.class), any(Block.class));
    }

    @Test
    void performTransformationsWhenErrorsInTransformations() throws Exception
    {
        when(this.renderingConfiguration.getTransformationNames()).thenReturn(Arrays.asList("tx1", "tx2"));
        Transformation tx1 = new Transformation1();
        this.componentManager.registerComponent(Transformation.class, "tx1", tx1);
        Transformation tx2 = new Transformation2();
        this.componentManager.registerComponent(Transformation.class, "tx2", tx2);
        when(this.componentManagerProvider.get()).thenReturn(this.componentManager);

        Throwable exception = assertThrows(TransformationException.class, () -> {
            this.transformationManager.performTransformations(XDOM.EMPTY,
                new TransformationContext(XDOM.EMPTY, Syntax.XWIKI_2_0));
        });

        String expected = "\\QThe following transformations failed to execute properly: [\\E\n"
            + "\\Q- Transformation: "
                + "[org.xwiki.rendering.internal.transformation.DefaultTransformationManagerTest$Transformation2]\\E\n"
            + "\\Qorg.xwiki.rendering.transformation.TransformationException: error\\E\n"
            + "(.*\n)+"
            + "\\Q- Transformation: "
                + "[org.xwiki.rendering.internal.transformation.DefaultTransformationManagerTest$Transformation1]\\E\n"
            + "\\Qorg.xwiki.rendering.transformation.TransformationException: error\\E\n"
            + "(.*\n)+"
            + "]";
        assertThat(exception.getMessage(), matchesPattern(expected));
    }
}
