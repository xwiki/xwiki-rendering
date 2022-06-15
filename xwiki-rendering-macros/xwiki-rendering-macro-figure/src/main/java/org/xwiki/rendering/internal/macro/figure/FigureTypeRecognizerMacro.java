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
package org.xwiki.rendering.internal.macro.figure;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.FigureBlock;
import org.xwiki.rendering.macro.AbstractNoParameterMacro;
import org.xwiki.rendering.macro.figure.FigureTypeRecognizer;
import org.xwiki.rendering.transformation.MacroTransformationContext;

/**
 * This macro is expected to be put as the previous sibling of a {@link FigureBlock}. When in this position, it will
 * insert a {@code data-xwiki-rendering-figure-type} property with its type in the {@link FigureBlock}.
 *
 * @version $Id$
 * @since 14.5RC1
 */
@Component
@Named("figureTypeRecognizer")
@Singleton
public class FigureTypeRecognizerMacro extends AbstractNoParameterMacro
{
    @Inject
    private FigureTypeRecognizer figureTypeRecognizer;

    /**
     * Default constructor.
     */
    public FigureTypeRecognizerMacro()
    {
        super("Figure Type Recognizer", "Internal macro used to recognizer the type of a figure.");
        setDefaultCategory(DEFAULT_CATEGORY_HIDDEN);
        // 1 more than the Figure macro priority (so executed after the Figure macro).
        setPriority(1001);
    }

    @Override
    public boolean supportsInlineMode()
    {
        return false;
    }

    @Override
    public List<Block> execute(Object parameters, String content, MacroTransformationContext context)
    {
        Block nextSibling = context.getCurrentMacroBlock().getNextSibling();
        if (nextSibling instanceof FigureBlock) {
            String type = this.figureTypeRecognizer.isTable((FigureBlock) nextSibling) ? "table" : "figure";
            nextSibling.setParameter("data-xwiki-rendering-figure-type", type);
        }
        return List.of();
    }
}
