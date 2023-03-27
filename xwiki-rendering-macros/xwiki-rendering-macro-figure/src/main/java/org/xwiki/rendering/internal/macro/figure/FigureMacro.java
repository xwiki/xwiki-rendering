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

import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.block.FigureBlock;
import org.xwiki.rendering.macro.figure.AbstractFigureMacro;
import org.xwiki.rendering.macro.figure.FigureMacroParameters;

/**
 * Tag content as an illustration and with an optional caption.
 *
 * @version $Id$
 * @since 10.2
 */
@Component
@Named("figure")
@Singleton
public class FigureMacro extends AbstractFigureMacro<FigureMacroParameters>
{
    /**
     * Create and initialize the descriptor of the macro.
     */
    public FigureMacro()
    {
        super();
    }

    // TODO: move to platform
    private void updateStyleParameters(FigureMacroParameters parameters, FigureBlock figureBlock)
    {
        if (parameters.getStyle() != null) {
            figureBlock.setParameter("data-xwiki-image-style", parameters.getStyle());
        }

        if (parameters.getWidth() != null) {
            figureBlock.setParameter("width", String.valueOf(parameters.getWidth()));
        }

        if (parameters.isBorder()) {
            figureBlock.setParameter("data-xwiki-image-style-border", Boolean.TRUE.toString());
        }

        if (parameters.isTextWrap()) {
            figureBlock.setParameter("data-xwiki-image-style-text-wrap", Boolean.TRUE.toString());
        }

        if (parameters.getAlignment() != null) {
            figureBlock.setParameter("data-xwiki-image-style-alignment", parameters.getAlignment().getId());
        }
    }
}
