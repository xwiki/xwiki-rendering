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

import java.util.List;

import org.jdom2.Element;
import org.xwiki.component.annotation.Role;
import org.xwiki.rendering.block.Block;

/**
 * Defines how to display a JIRA issue field (for example the "Key" field can be displayed with a link to the JIRA
 * issue, date fields can have their dates formatted in a special way, "status" field can be displayed with an icon,
 * etc).
 *
 * @version $Id$
 * @since 4.2M1
 */
@Role
public interface JIRAFieldDisplayer
{
    /**
     * Generate Blocks to display the passed field.
     *
     * @param fieldName the name of the field to display
     * @param issue the JIRA issue as an XML element, can be used to extract information useful to generate the display
     * @return the list of Blocks to display the passed field
     */
    List<Block> displayField(String fieldName, Element issue);
}
