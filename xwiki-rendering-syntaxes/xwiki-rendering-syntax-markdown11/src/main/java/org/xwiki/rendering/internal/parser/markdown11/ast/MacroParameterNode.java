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
package org.xwiki.rendering.internal.parser.markdown11.ast;

import java.util.List;

import org.parboiled.common.ImmutableList;
import org.pegdown.ast.AbstractNode;
import org.pegdown.ast.Node;
import org.pegdown.ast.Visitor;

/**
 * Represents macro's parameter node.
 *
 * @version $Id $
 * @since 5.2M1
 */
public class MacroParameterNode extends AbstractNode
{
    private final String name;

    private String value;

    /**
     * @param name a name of the parameter
     */
    public MacroParameterNode(String name)
    {
        this.name = name;
    }

    /**
     * @return name of the parameter
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return value of the parameter
     */
    public String getValue()
    {
        return value;
    }

    /**
     * @param value a value of the parameter
     * @return {@literal true}
     */
    public boolean setValue(String value)
    {
        this.value = value;
        return true;
    }

    @Override
    public List<Node> getChildren()
    {
        return ImmutableList.of();
    }

    @Override
    public void accept(Visitor visitor)
    {
        visitor.visit(this);
    }

    @Override
    public String toString()
    {
        return name + "=" + value;
    }
}
