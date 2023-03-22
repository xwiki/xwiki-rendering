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
package org.xwiki.rendering.macro.figure;

import org.xwiki.properties.annotation.PropertyDescription;
import org.xwiki.properties.annotation.PropertyName;
import org.xwiki.rendering.internal.macro.figure.FigureMacro;
import org.xwiki.stability.Unstable;

/**
 * Parameters for the {@link FigureMacro}.
 *
 * @version $Id$
 * @since 14.9RC1
 */
@Unstable
public class FigureMacroParameters
{
    /**
     * By default, the figure type is set to {@link FigureType#AUTOMATIC}.
     */
    private FigureType type = FigureType.AUTOMATIC;

    private String style;

    private String width;

    private boolean border;

    private AlignmentType alignment;

    private boolean textWrap;

    /**
     * @return the type of the figure (i.e., {@link FigureType#FIGURE} or {@link FigureType#TABLE}}), if
     *     {@link FigureType#AUTOMATIC} the type will be automatically defined based on the macro content
     */
    public FigureType getType()
    {
        return this.type;
    }

    /**
     * @param type the type of the figure (i.e., {@link FigureType#FIGURE} or {@link FigureType#TABLE}}), if
     *     {@link FigureType#AUTOMATIC} the type will be automatically defined based on the macro content
     */
    @PropertyDescription("The type of the figure (i.e., \"figure\" or \"table\"). When automatic, the type will be "
        + "defined based on the macro content.")
    @PropertyName("Type")
    public void setType(FigureType type)
    {
        this.type = type;
    }

    /**
     * Define the figure style.
     *
     * @param style the style of the image
     * @since 14.10.8
     * @since 15.3RC1
     */
    @Unstable
    @PropertyDescription("TODO Style description")
    @PropertyName("TODO Style name")
    public void setStyle(String style)
    {
        this.style = style;
    }

    /**
     * @return the type of the image
     * @since 14.10.8
     * @since 15.3RC1
     */
    @Unstable
    public String getStyle()
    {
        return this.style;
    }

    /**
     * @return the width of the image (a css dimension e.g., in pixel or percent, or "automatic", or "")
     * @since 14.10.8
     * @since 15.3RC1
     */
    @Unstable
    public String getWidth()
    {
        return this.width;
    }

    /**
     * @param width a css dimension e.g., in pixel or percent, or "automatic", or ""
     * @since 14.10.8
     * @since 15.3RC1
     */
    @Unstable
    @PropertyDescription("TODO width description")
    @PropertyName("TODO width name")
    public void setWidth(String width)
    {
        this.width = width;
    }

    /**
     * @return {@code true} if the figure must have a border
     * @since 14.10.8
     * @since 15.3RC1
     */
    @Unstable
    public boolean isBorder()
    {
        return this.border;
    }

    /**
     * @param border {@code true} of the figure must have a border
     * @since 14.10.8
     * @since 15.3RC1
     */
    @Unstable
    @PropertyDescription("TODO border description")
    @PropertyName("TODO border name")
    public void setBorder(boolean border)
    {
        this.border = border;
    }

    /**
     * @return the alignment type of the figure (none, start, center, or end)
     * @since 14.10.8
     * @since 15.3RC1
     */
    @Unstable
    public AlignmentType getAlignment()
    {
        return this.alignment;
    }

    /**
     * @param alignment the alignment type of the figure (none, start, center, or end)
     * @since 14.10.8
     * @since 15.3RC1
     */
    @Unstable
    @PropertyDescription("TODO alignment description")
    @PropertyName("TODO alignment name")
    public void setAlignment(AlignmentType alignment)
    {
        this.alignment = alignment;
    }

    /**
     * @return {@code true} if figure must wrap the text
     * @since 14.10.8
     * @since 15.3RC1
     */
    @Unstable
    public boolean isTextWrap()
    {
        return this.textWrap;
    }

    /**
     * @param textWrap {@code true} if the fuigure must wrap the text
     */
    @Unstable
    @PropertyDescription("TODO textWrap description")
    @PropertyName("TODO textWrap name")
    public void setTextWrap(boolean textWrap)
    {
        this.textWrap = textWrap;
    }
}
