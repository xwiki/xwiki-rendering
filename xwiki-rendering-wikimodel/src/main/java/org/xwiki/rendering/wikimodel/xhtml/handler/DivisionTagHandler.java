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
package org.xwiki.rendering.wikimodel.xhtml.handler;

import java.util.Arrays;

import org.xwiki.rendering.wikimodel.WikiParameter;
import org.xwiki.rendering.wikimodel.xhtml.impl.TagContext;

/**
 * @version $Id$
 * @since 4.0M1
 */
public class DivisionTagHandler extends BlockTagHandler
{
    public DivisionTagHandler()
    {
        super();
    }

    public DivisionTagHandler(String documentClass)
    {
        super(documentClass);
    }

    @Override
    public boolean isBlockHandler(TagContext context)
    {
        WikiParameter param = context.getParams().getParameter("class");
        if (param != null) {
            if (Arrays.asList(param.getValue().split(" ")).contains("wikimodel-emptyline")) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected void begin(TagContext context)
    {
        if (!isBlockHandler(context)) {
            super.begin(context);
        } else {
            context.getTagStack().incrementEmptyLinesCount();
        }
    }

    @Override
    protected void end(TagContext context)
    {
        if (!isBlockHandler(context)) {
            super.end(context);
        }
    }
}
