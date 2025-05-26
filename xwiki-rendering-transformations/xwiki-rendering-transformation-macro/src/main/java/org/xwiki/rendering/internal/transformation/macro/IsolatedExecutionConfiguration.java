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
package org.xwiki.rendering.internal.transformation.macro;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.configuration.ConfigurationSource;

/**
 * A caching wrapper around the isolated execution configuration.
 *
 * @version $Id$
 * @since 17.3.0RC1
 * @since 16.10.9
 */
@Component(roles = IsolatedExecutionConfiguration.class)
@Singleton
public class IsolatedExecutionConfiguration
{
    @Inject
    @Named("restricted")
    private Provider<ConfigurationSource> configurationSourceProvider;

    // Cache configuration values as converting the configuration value to Boolean is kind of slow because it uses a
    // context component manager.
    private final Map<String, Optional<Boolean>> cache = new ConcurrentHashMap<>();

    /**
     * @param macroId the ID of the macro
     * @param defaultValue the default value to return if the configuration is not set
     * @return if the execution of the macro is isolated according to the configuration
     */
    public boolean isExecutionIsolated(String macroId, boolean defaultValue)
    {
        return this.cache.computeIfAbsent(macroId,
                k -> Optional.ofNullable(
                    this.configurationSourceProvider.get()
                        .getProperty("rendering.macro." + k + ".executionIsolated", Boolean.class, null)))
            .orElse(defaultValue);
    }
}
