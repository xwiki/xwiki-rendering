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
package org.xwiki.rendering.listener;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ListenerElement
{
    private String name;

    private List<Class< ? >> parameters = new ArrayList<Class< ? >>();

    public Method beginMethod;

    public Method endMethod;

    public Method onMethod;

    public ListenerElement(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return this.name;
    }

    public List<Class< ? >> getParameters()
    {
        return this.parameters;
    }

    public Method getBeginMethod()
    {
        return this.beginMethod;
    }

    public void setBeginMethod(Method beginMethod)
    {
        this.beginMethod = beginMethod;
    }

    public Method getEndMethod()
    {
        return this.endMethod;
    }

    public void setEndMethod(Method endMethod)
    {
        this.endMethod = endMethod;
    }

    public Method getOnMethod()
    {
        return this.onMethod;
    }

    public void setOnMethod(Method onMethod)
    {
        this.onMethod = onMethod;
    }
}
