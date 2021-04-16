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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.component.event.ComponentDescriptorAddedEvent;
import org.xwiki.component.event.ComponentDescriptorRemovedEvent;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.util.DefaultParameterizedType;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.ApplicationStartedEvent;
import org.xwiki.observation.event.Event;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.syntax.SyntaxRegistry;

/**
 * Listen to any component implementing {@code Provider<List<Syntax>>} and register the provided syntaxes in the
 * Syntax Registry.
 *
 * @version $Id$
 * @since 13.3RC1
 */
@Component
@Singleton
@Named("syntaxregistry")
public class SyntaxRegistryListener implements EventListener
{
    private static final Type SYNTAX_PROVIDER_TYPE = new DefaultParameterizedType(null, Provider.class,
        new DefaultParameterizedType(null, List.class, Syntax.class));

    @Inject
    private SyntaxRegistry syntaxRegistry;

    @Inject
    @Named("context")
    private Provider<ComponentManager> componentManagerProvider;

    /**
     * Used to remember which components have registered which syntaxes. Used when removing
     */
    private Map<ComponentDescriptor<?>, Syntax[]> componentSyntaxes = new HashMap<>();

    @Override
    public String getName()
    {
        return "syntaxregistry";
    }

    @Override
    public List<Event> getEvents()
    {
        return Arrays.asList(
            new ApplicationStartedEvent(),
            new ComponentDescriptorAddedEvent(SYNTAX_PROVIDER_TYPE),
            new ComponentDescriptorRemovedEvent(SYNTAX_PROVIDER_TYPE));
    }

    @Override
    public void onEvent(Event event, Object source, Object data)
    {
        if (event instanceof ApplicationStartedEvent) {
            // Listeners are initialized after components located in the current classloader and thus we need to look
            // for any syntax provides already registered in the component manager since no
            // ComponentDescriptorAddedEvent will be sent for them.
            // TODO: In the future we'll probably want to fix XWiki's initialization,
            //  see https://jira.xwiki.org/browse/XWIKI-18563. When this is fixed, we won't need anymore to listen to
            // ApplicationStartedEvent.
            registerSyntaxesProviders();
        } else {
            ComponentManager componentManager = (ComponentManager) source;
            ComponentDescriptor<?> descriptor = (ComponentDescriptor<?>) data;

            if (event instanceof ComponentDescriptorAddedEvent) {
                Provider<List<Syntax>> syntaxesProvider = getSyntaxesProvider(componentManager, descriptor);
                List<Syntax> syntaxes = syntaxesProvider.get();
                Syntax[] syntaxArray = new Syntax[syntaxes.size()];
                this.syntaxRegistry.registerSyntaxes(syntaxes.toArray(syntaxArray));
                this.componentSyntaxes.put(descriptor, syntaxArray);
            } else {
                // If the descriptor is not found then this means that the syntaxes were registered manually against the
                // Syntax Registry. Thus they'll also need to be unregistered manually.
                if (this.componentSyntaxes.containsKey(descriptor)) {
                    this.syntaxRegistry.unregisterSyntaxes(this.componentSyntaxes.get(descriptor));
                }
            }
        }
    }

    private void registerSyntaxesProviders()
    {
        ComponentManager cm = this.componentManagerProvider.get();
        List<ComponentDescriptor<Provider<List<Syntax>>>> componentDescriptors =
            cm.getComponentDescriptorList(SYNTAX_PROVIDER_TYPE);
        for (ComponentDescriptor<Provider<List<Syntax>>> cd : componentDescriptors) {
            Provider<List<Syntax>> syntaxesProvider;
            try {
                syntaxesProvider = cm.getInstance(cd.getRoleType(), cd.getRoleHint());
            } catch (ComponentLookupException e) {
                // Some Syntax Provider failed to instantiate properly. Since these are core Syntax Providers in the
                // current ClassLoader they're supposed to work fine. Thus if it fails we consider it's a fatal error
                // and we raise an exception. This should normally not happen.
                throw new RuntimeException(String.format("Failed to instantiate Syntax Provider component [{}]",
                    cd), e);
            }
            registerSyntaxesProvider(syntaxesProvider, cd);
        }
    }

    private void registerSyntaxesProvider(Provider<List<Syntax>> syntaxesProvider, ComponentDescriptor<?> descriptor)
    {
        List<Syntax> syntaxes = syntaxesProvider.get();
        Syntax[] syntaxArray = new Syntax[syntaxes.size()];
        this.syntaxRegistry.registerSyntaxes(syntaxes.toArray(syntaxArray));
        this.componentSyntaxes.put(descriptor, syntaxArray);
    }

    private Provider<List<Syntax>> getSyntaxesProvider(ComponentManager componentManager,
        ComponentDescriptor<?> descriptor)
    {
        try {
            return componentManager.getInstance(SYNTAX_PROVIDER_TYPE, descriptor.getRoleHint());
        } catch (ComponentLookupException e) {
            // This shouldn't happen since the component has just been registered in the CM!
            throw new RuntimeException(String.format("Failed to lookup Syntaxes Provider for role [%s]",
                descriptor.getRoleHint()), e);
        }
    }
}
