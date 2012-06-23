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
package org.xwiki.rendering.macro.jira;

import java.util.Collection;
import java.util.List;

import org.jdom2.Element;
import org.xwiki.component.annotation.Role;
import org.xwiki.rendering.block.Block;

/**
 * A JIRA Displayer is used to display the JIRA issues visually. Example of Displayers: Table displayer which displays
 * data in a table, Lsit displayer to display data in a list, etc.
 *
 * @version $Id$
 * @since 4.2M1
 */
@Role
public interface JIRADisplayer
{
    /**
     * Displays the passed JIRA issues.
     *
     * @param issues the issues to display
     * @param parameters the macro parameters specified by the user; can be used to specify displayer-specific
     *        configuration data (for example displayers allows the user to specify the list of JIRA issue fields to
     *        display
     * @return the list of Blocks rerpesenting what to display
     */
    List<Block> display(Collection<Element> issues, JIRAMacroParameters parameters);
}
