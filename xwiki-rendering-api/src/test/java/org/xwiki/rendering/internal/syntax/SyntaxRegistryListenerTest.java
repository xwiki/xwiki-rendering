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
import java.util.Collections;
import java.util.List;

import javax.inject.Provider;

import org.junit.jupiter.api.Test;
import org.xwiki.component.internal.StackingComponentEventManager;
import org.xwiki.component.util.DefaultParameterizedType;
import org.xwiki.observation.ObservationManager;
import org.xwiki.observation.event.ApplicationStartedEvent;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.syntax.SyntaxRegistry;
import org.xwiki.rendering.syntax.SyntaxType;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;
import org.xwiki.test.mockito.MockitoComponentManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    private static final Type SYNTAX_PROVIDER_TYPE = new DefaultParameterizedType(null, Provider.class,
        new DefaultParameterizedType(null, List.class, Syntax.class));

    @InjectComponentManager
    private MockitoComponentManager componentManager;

    /**
     * Used to simulate a Syntax Provider brought by an extension, i.e registers in the CM after the initial
     * CM initialization. Note that the MySyntaxProvider component is registered in the CM during the initial CM
     * initialization and this allows us to test the 2 use cases and verify they both work.
     */
    private class MyExtensionSyntaxProvider implements Provider<List<Syntax>>
    {
        @Override
        public List<Syntax> get()
        {
            return Collections.singletonList(new Syntax(new SyntaxType("myextensionid", "My Extension Id"), "1.0"));
        }
    }

    @Test
    void registerAndUnregister() throws Exception
    {
        // Simulate XWiki initialization by sending the ApplicationStartedEvent event. This will, in turn, trigger
        // the registration of syntaxes defined in core SyntaxProvider components.
        ObservationManager observationManager = this.componentManager.getInstance(ObservationManager.class);
        observationManager.notify(new ApplicationStartedEvent(), null);

        SyntaxRegistry syntaxRegistry = this.componentManager.getInstance(SyntaxRegistry.class);

        // Step 1: Verify that the Syntax provided by MySyntaxProvider has been registered in the Syntax Registry.
        Provider<List<Syntax>> mySyntaxProvider =
            this.componentManager.getInstance(SYNTAX_PROVIDER_TYPE, MySyntaxProvider.MY_1_0.toIdString());
        Syntax expectedSyntax = mySyntaxProvider.get().get(0);
        assertEquals(1, mySyntaxProvider.get().size());
        assertEquals(expectedSyntax, syntaxRegistry.getSyntax(expectedSyntax.toIdString()).get());
        assertEquals(expectedSyntax, syntaxRegistry.getSyntaxes().get(expectedSyntax.toIdString()));

        // Step 2: Register a new Syntax dynamically to simulate an extension bringing a new Syntax. Verify it's need
        // added to the Syntax Registry

        // Note: We need to make sure that the CM has a ComponentEventManager set as otherwise the events won't be
        // sent by the Observation Manager. This is what happens at XWiki init, and that we need to simulate here.
        StackingComponentEventManager componentEventManager = new StackingComponentEventManager();
        componentEventManager.shouldStack(false);
        componentEventManager.setObservationManager(observationManager);
        this.componentManager.setComponentEventManager(componentEventManager);

        this.componentManager.registerComponent(SYNTAX_PROVIDER_TYPE, "myextension", new MyExtensionSyntaxProvider());
        Provider<List<Syntax>> myExtensionSyntaxProvider =
            this.componentManager.getInstance(SYNTAX_PROVIDER_TYPE, "myextension");
        Syntax expectedExtensionSyntax = myExtensionSyntaxProvider.get().get(0);
        assertEquals(1, myExtensionSyntaxProvider.get().size());
        assertEquals(expectedExtensionSyntax, syntaxRegistry.getSyntax(expectedExtensionSyntax.toIdString()).get());
        assertEquals(2, syntaxRegistry.getSyntaxes().size());
        assertEquals(expectedExtensionSyntax, syntaxRegistry.getSyntaxes().get(expectedExtensionSyntax.toIdString()));

        // Step 3: Unregister the 2 new Syntaxes and verify that the syntaxes are no longer in the Syntax Registry.
        this.componentManager.unregisterComponent(SYNTAX_PROVIDER_TYPE, "myextension");
        this.componentManager.unregisterComponent(SYNTAX_PROVIDER_TYPE, MySyntaxProvider.MY_1_0.toIdString());
        assertTrue(syntaxRegistry.getSyntaxes().isEmpty());
    }
}
