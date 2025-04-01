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

import javax.inject.Named;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ComponentTest
class IsolatedExecutionConfigurationTest
{
    @MockComponent
    @Named("restricted")
    private ConfigurationSource configurationSource;

    @InjectMockComponents
    private IsolatedExecutionConfiguration isolatedExecutionConfiguration;

    @ParameterizedTest
    @CsvSource({
        "false, false, false",
        "true, false, false",
        "true, true, true",
        "false, true, true",
        "true, , true",
        "false, , false"
    })
    void isExecutionIsolated(boolean macroIsolated, Boolean configurationIsolated, boolean expected)
    {
        when(this.configurationSource.getProperty(anyString(), any(), isNull())).thenReturn(configurationIsolated);

        String macroId = "test";
        assertEquals(expected,
            this.isolatedExecutionConfiguration.isExecutionIsolated(macroId, macroIsolated));
        verify(this.configurationSource).getProperty("rendering.macro.test.executionIsolated", Boolean.class, null);

        // Verify that on the second load the value is cached.
        assertEquals(expected, this.isolatedExecutionConfiguration.isExecutionIsolated(macroId, macroIsolated));

        verifyNoMoreInteractions(this.configurationSource);

        // Verify that despite the cache if the macro value changes, the change is reflected if the configuration
        // isn't set.
        if (configurationIsolated == null) {
            assertEquals(!expected, this.isolatedExecutionConfiguration.isExecutionIsolated(macroId, !macroIsolated));
        } else {
            assertEquals(expected, this.isolatedExecutionConfiguration.isExecutionIsolated(macroId, !macroIsolated));
        }

        // Verify that the cached value doesn't affect a second macro ID.
        String secondMacroId = "second";
        when(this.configurationSource.getProperty("rendering.macro.second.executionIsolated", Boolean.class, null))
            .thenReturn(!expected);
        assertEquals(!expected, this.isolatedExecutionConfiguration.isExecutionIsolated(secondMacroId, macroIsolated));
    }
}
