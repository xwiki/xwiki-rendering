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
package org.xwiki.rendering.internal.macro.jira;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.macro.AbstractMacro;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.macro.jira.JIRADataSource;
import org.xwiki.rendering.macro.jira.JIRADisplayer;
import org.xwiki.rendering.macro.jira.JIRAMacroParameters;
import org.xwiki.rendering.transformation.MacroTransformationContext;

/**
 * Fetches information from a JIRA server and displays them as a table, list or enumeration.
 *
 * @version $Id$
 * @since 4.2M1
 */
@Component
@Named("jira")
@Singleton
public class JIRAMacro extends AbstractMacro<JIRAMacroParameters>
{
    /**
     * The description of the macro.
     */
    private static final String DESCRIPTION =
        "Fetches information from a JIRA server and displays them as a table, list or enumeration.";

    /**
     * Used to get JIRA Data Source and JIRA Displayer matching what the user has asked for.
     */
    @Inject
    private ComponentManager componentManager;

    /**
     * Create and initialize the descriptor of the macro.
     */
    public JIRAMacro()
    {
        super("JIRA", DESCRIPTION, JIRAMacroParameters.class);
        setDefaultCategory(DEFAULT_CATEGORY_CONTENT);
    }

    @Override
    public boolean supportsInlineMode()
    {
        return true;
    }

    @Override
    public List<Block> execute(JIRAMacroParameters parameters, String content, MacroTransformationContext context)
        throws MacroExecutionException
    {
        return getDisplayer(parameters).display(getDataSource(parameters).getData(content, parameters), parameters);
    }

    /**
     * @param parameters the macro parameters specified by the user
     * @return the data source component asked by the user (defaults to the List Data Source if not specified)
     * @throws MacroExecutionException if the data source component doesn't exist
     */
    private JIRADataSource getDataSource(JIRAMacroParameters parameters) throws MacroExecutionException
    {
        JIRADataSource source;
        try {
            source = this.componentManager.getInstance(JIRADataSource.class, parameters.getSource());
        } catch (ComponentLookupException e) {
            throw new MacroExecutionException(String.format("Unknown JIRA source [%s]", parameters.getSource()), e);
        }

        return source;
    }

    /**
     * @param parameters the macro parameters specified by the user
     * @return the displayer component sked by the user (defaults to the Table Displayer if not specified)
     * @throws MacroExecutionException if the displayer component doesn't exist
     */
    private JIRADisplayer getDisplayer(JIRAMacroParameters parameters) throws MacroExecutionException
    {
        JIRADisplayer displayer;
        try {
            displayer = this.componentManager.getInstance(JIRADisplayer.class, parameters.getStyle());
        } catch (ComponentLookupException e) {
            throw new MacroExecutionException(String.format("Unknown JIRA style [%s]", parameters.getStyle()), e);
        }

        return displayer;
    }
}
