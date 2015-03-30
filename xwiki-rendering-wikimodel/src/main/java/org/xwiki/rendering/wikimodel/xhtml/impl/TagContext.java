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
package org.xwiki.rendering.wikimodel.xhtml.impl;

import org.xwiki.rendering.wikimodel.WikiPageUtil;
import org.xwiki.rendering.wikimodel.WikiParameters;
import org.xwiki.rendering.wikimodel.impl.WikiScannerContext;
import org.xwiki.rendering.wikimodel.xhtml.handler.TagHandler;

/**
 * Provides context for a given tag.
 *
 * @version $Id$
 * @since 7.0RC1
 */
public class TagContext
{
    private final TagContext fParent;

    private final String fName;

    private final WikiParameters fParameters;

    private final TagStack fTagStack;

    private StringBuffer fContent;

    private TagHandler fHandler;

    public TagContext(TagContext parent, String name, WikiParameters params, TagStack tagStack)
    {
        fName = (name != null) ? name.toLowerCase() : null;
        fParent = parent;
        fParameters = params;
        fTagStack = tagStack;
    }

    public boolean appendContent(String content)
    {
        if (fHandler == null || !fHandler.isAccumulateContent()) {
            return false;
        }
        if (fContent == null) {
            fContent = new StringBuffer();
        }
        fContent.append(content);
        return true;
    }

    public void beginElement(TagHandler handler)
    {
        if (fParent == null) {
            getScannerContext().beginDocument();
        }
        fHandler = handler;
        if (fHandler != null) {
            fHandler.beginElement(this);
        }
    }

    public void endElement()
    {
        if (fHandler != null) {
            fHandler.endElement(this);
        }
        if (fParent == null) {
            getScannerContext().endDocument();
        }
    }

    public String getContent()
    {
        return fContent != null ? WikiPageUtil.escapeXmlString(fContent.toString()) : "";
    }

    public String getName()
    {
        return fName;
    }

    public WikiParameters getParams()
    {
        return fParameters;
    }

    public TagContext getParentContext() {
        return fParent;
    }

    public TagContext getParent()
    {
        // If my parent is not handled, I want it to be fully ignored, so I will go up the tree until I found
        // a handled parent, however I should not reach the root.
        if (fParent.fHandler == null && fParent.fParent.fName != null) {
            return fParent.getParent();
        } else {
            return fParent;
        }
    }

    public TagStack getTagStack()
    {
        return fTagStack;
    }

    public WikiScannerContext getScannerContext()
    {
        return fTagStack.getScannerContext();
    }

    public boolean isContentContainer()
    {
        return fHandler == null || fHandler.isContentContainer();
    }

    public boolean isTag(String string)
    {
        return string.equals(fName);
    }
}
