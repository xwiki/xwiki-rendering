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
package org.xwiki.rendering.internal.syntax;

import java.lang.reflect.Type;
import java.util.List;

import javax.inject.Provider;

import org.junit.jupiter.api.Test;
import org.xwiki.component.internal.StackingComponentEventManager;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.util.DefaultParameterizedType;
import org.xwiki.observation.ObservationManager;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.syntax.SyntaxRegistry;
import org.xwiki.test.annotation.AfterComponent;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.annotation.BeforeComponent;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link SyntaxRegistryListener}.
 *
 * @version $Id$
 */
@AllComponents
@ComponentTest
class SyntaxRegistryListenerTest
{
    @InjectComponentManager
    private ComponentManager componentManager;

    private StackingComponentEventManager componentEventManager;

    @BeforeComponent
    void before()
    {
        // Set the component event manager in the component manager so that when a component descriptor is added,
        // the observation manager notifies the SyntaxRegistryListener. This is normally done in XWiki's
        // ServletContextListener but since we're in an integration test, we need to simulate that.
        this.componentEventManager = new StackingComponentEventManager();
        this.componentManager.setComponentEventManager(this.componentEventManager);
    }

    @AfterComponent
    void after() throws Exception
    {
        // When this code is called, all components (including the component under test) have been registered
        // against the ComponentManager. We now need to tell the StackingComponentEventManager to execute all the
        // component descriptor events it has stacked so that SyntaxRegistryListener receives them and registers
        // the syntax into the Syntax Registry.
        ObservationManager observationManager = this.componentManager.getInstance(ObservationManager.class);
        this.componentEventManager.setObservationManager(observationManager);
        this.componentEventManager.shouldStack(false);
        this.componentEventManager.flushEvents();
    }

    @Test
    void registerAndUnregister() throws Exception
    {
        // Verify that the new Syntax has been registered in the Syntax Registry.
        Type syntaxProviderType = new DefaultParameterizedType(null, Provider.class,
            new DefaultParameterizedType(null, List.class, Syntax.class));
        Provider<List<Syntax>> syntaxProvider =
            this.componentManager.getInstance(syntaxProviderType, MySyntaxProvider.MY_1_0.toIdString());
        SyntaxRegistry syntaxRegistry = this.componentManager.getInstance(SyntaxRegistry.class);
        assertEquals(1, syntaxProvider.get().size());
        assertEquals(syntaxProvider.get().get(0), syntaxRegistry.getSyntax(MySyntaxProvider.MY_1_0.toIdString()).get());
        assertEquals(syntaxProvider.get().get(0),
            syntaxRegistry.getSyntaxes().get(MySyntaxProvider.MY_1_0.toIdString()));

        // Unregister the new Syntax
        this.componentManager.unregisterComponent(syntaxProviderType, MySyntaxProvider.MY_1_0.toIdString());

        // Verify that the new syntax is no longer in the Syntax Registry.
        assertFalse(syntaxRegistry.getSyntax("myid/1.0").isPresent());
        assertTrue(syntaxRegistry.getSyntaxes().isEmpty());
    }
}
