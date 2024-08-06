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
package org.xwiki.rendering.internal.macro.message;

import java.io.StringReader;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.CompositeBlock;
import org.xwiki.rendering.block.FormatBlock;
import org.xwiki.rendering.block.GroupBlock;
import org.xwiki.rendering.block.ImageBlock;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.block.MetaDataBlock;
import org.xwiki.rendering.listener.Format;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.macro.MacroPreparationException;
import org.xwiki.rendering.macro.box.AbstractBoxMacro;
import org.xwiki.rendering.macro.box.BoxMacroParameters;
import org.xwiki.rendering.macro.descriptor.DefaultContentDescriptor;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.rendering.util.IconProvider;

/**
 * Common implementation for message macros (e.g. info, error, warning, success, etc).
 *
 * @version $Id$
 * @since 2.0M3
 */
public abstract class AbstractMessageMacro extends AbstractBoxMacro<BoxMacroParameters>
{
    protected String iconName;

    /**
     * Used to get the icon representations.
     */
    @Inject
    private IconProvider iconProvider;

    @Inject
    private MacroIconPrettyNameProvider iconPrettyNameProvider;

    @Inject
    @Named("plain/1.0")
    private Parser plainTextParser;

    /**
     * Create and initialize the descriptor of the macro.
     *
     * @param macroName the macro name (eg "Error", "Info", etc)
     * @param macroDescription the macro description
     */
    public AbstractMessageMacro(String macroName, String macroDescription)
    {
        super(macroName, macroDescription,
            new DefaultContentDescriptor("Content of the message", true, Block.LIST_BLOCK_TYPE),
            BoxMacroParameters.class);
    }

    @Override
    protected List<Block> parseContent(BoxMacroParameters parameters, String content,
        MacroTransformationContext context) throws MacroExecutionException
    {
        List<Block> macroContent = getMacroContentParser().parse(content, context, false, context.isInline())
            .getChildren();
        return Collections.singletonList(new MetaDataBlock(macroContent, this.getNonGeneratedContentMetaData()));
    }

    @Override
    protected String getClassProperty()
    {
        return super.getClassProperty() + ' ' + this.getDescriptor().getId().getId() + "message";
    }

    @Override
    public void prepare(MacroBlock macroBlock) throws MacroPreparationException
    {
        this.contentParser.prepareContentWiki(macroBlock);
    }

    @Override
    public List<Block> execute(BoxMacroParameters parameters, String content, MacroTransformationContext context) 
        throws MacroExecutionException 
    {
        List<Block> boxFoundation = super.execute(parameters, content, context);
        if (!boxFoundation.isEmpty() && this.iconName != null) {
            Block defaultBox = boxFoundation.get(0);
            // For an easier styling, we wrap the content and title together if they are non-empty and visible
            if (defaultBox.getChildren().size() > 1) {
                Block boxTextContent = new GroupBlock(defaultBox.getChildren());
                defaultBox.setChildren(List.of(boxTextContent));
            }
            // Enhance the default box with an icon as the first element.
            Block iconBlock = this.iconProvider.get(this.iconName);
            // Provide an accessible name besides this icon
            // This is the responsibility of the message macro and not the iconProvider which should only provide
            // icons without any semantics
            String iconPrettyName = iconPrettyNameProvider.getIconPrettyName(this.iconName);
            if (iconBlock.getClass() == ImageBlock.class) {
                iconBlock.setAttribute("alt", iconPrettyName);
            } else if (!iconPrettyName.isEmpty()) {
                try {
                    Block iconAlternative = new FormatBlock(
                        this.plainTextParser.parse(new StringReader(iconPrettyName)).getChildren(), 
                        Format.NONE);
                    iconBlock = new CompositeBlock(List.of(iconBlock, iconAlternative));
                } catch (ParseException e) {
                    // This shouldn't happen since the parser cannot throw an exception since the source is a memory
                    // String.
                    throw new RuntimeException("Failed to parse text alternative for the icon", e);
                }
            }
            
            // Add the icon block at the start of the box block.
            defaultBox.insertChildBefore(iconBlock, defaultBox.getChildren().get(0));
        }
        return boxFoundation;
    }
}
