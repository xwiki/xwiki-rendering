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
package org.xwiki.rendering.internal.parser.confluencexhtml.wikimodel;

import org.xwiki.rendering.wikimodel.WikiParameter;
import org.xwiki.rendering.wikimodel.xhtml.handler.TagHandler;
import org.xwiki.rendering.wikimodel.xhtml.impl.TagContext;

/**
 * Handles users.
 * <p>
 * Example:
 * <p>
 * {@code
 * <ri:user ri:username="admin" />
 * }
 *
 * @version $Id$
 * @since 5.3M2
 */
public class UserTagHandler extends TagHandler
{
    public UserTagHandler()
    {
        super(false, false, false);
    }

    @Override
    protected void begin(TagContext context)
    {
        Object container = context.getTagStack().getStackParameter("confluence-container");

        WikiParameter usernameParameter = context.getParams().getParameter("ri:username");
        if (usernameParameter != null && container instanceof UserContainer) {
            ((UserContainer) container).setUser(usernameParameter.getValue());
        }
    }
}
