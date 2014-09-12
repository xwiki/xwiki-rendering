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
package org.xwiki.rendering.listener.descriptor;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * An element of the listener.
 * <p>
 * An element is defined by either an <code>on</code> event of a combination of <code>begin</code> and <code>end</code>
 * events.
 *
 * @version $Id$
 * @since 3.3M1
 */
public class ListenerElement
{
    /**
     * @see #getName()
     */
    private String name;

    /**
     * @see #getParameters()
     */
    private List<Type> parameters = new ArrayList<Type>();

    /**
     * @see #getBeginMethod()
     */
    private Method beginMethod;

    /**
     * @see #getEndMethod()
     */
    private Method endMethod;

    /**
     * @see #getOnMethod()
     */
    private Method onMethod;

    /**
     * @param name the name of the element
     */
    public ListenerElement(String name)
    {
        this.name = name;
    }

    /**
     * @return the name of the element
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * @return the parameters of the element
     */
    public List<Type> getParameters()
    {
        return this.parameters;
    }

    /**
     * @return the begin method, null if it's a <code>on</code> event based element
     */
    public Method getBeginMethod()
    {
        return this.beginMethod;
    }

    /**
     * @param beginMethod the begin method, null if it's a <code>on</code> event based element
     */
    public void setBeginMethod(Method beginMethod)
    {
        this.beginMethod = beginMethod;
    }

    /**
     * @return the end method, null if it's a <code>on</code> event based element
     */
    public Method getEndMethod()
    {
        return this.endMethod;
    }

    /**
     * @param endMethod the end method, null if it's a <code>on</code> event based element
     */
    public void setEndMethod(Method endMethod)
    {
        this.endMethod = endMethod;
    }

    /**
     * @return the on method, null if it's a <code>begin/end</code> event based element
     */
    public Method getOnMethod()
    {
        return this.onMethod;
    }

    /**
     * @param onMethod the on method, null if it's a <code>begin/end</code> event based element
     */
    public void setOnMethod(Method onMethod)
    {
        this.onMethod = onMethod;
    }
}
