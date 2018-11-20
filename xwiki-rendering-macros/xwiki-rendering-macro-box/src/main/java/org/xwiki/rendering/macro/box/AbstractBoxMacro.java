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
package org.xwiki.rendering.macro.box;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.FormatBlock;
import org.xwiki.rendering.block.GroupBlock;
import org.xwiki.rendering.block.ImageBlock;
import org.xwiki.rendering.block.NewLineBlock;
import org.xwiki.rendering.listener.Format;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.rendering.macro.AbstractMacro;
import org.xwiki.rendering.macro.MacroContentParser;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.macro.descriptor.ContentDescriptor;
import org.xwiki.rendering.parser.ResourceReferenceParser;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.stability.Unstable;

/**
 * Draw a box around provided content.
 *
 * @param <P> the type of macro parameters bean.
 * @version $Id$
 * @since 1.7
 */
public abstract class AbstractBoxMacro<P extends BoxMacroParameters> extends AbstractMacro<P>
{
    /**
     * Predefined error message.
     */
    public static final String CONTENT_MISSING_ERROR = "The required content is missing.";

    /**
     * Parses untyped image references.
     */
    @Inject
    @Named("image/untyped")
    private ResourceReferenceParser untypedImageReferenceParser;

    /**
     * The parser used to parse box content and box title parameter.
     */
    @Inject
    private MacroContentParser contentParser;

    /**
     * Creates a new box macro.
     *
     * @param name the name of the macro
     * @param description string describing this macro.
     * @param contentDescriptor the {@link ContentDescriptor} describing the content of this macro.
     * @param parametersBeanClass class of the parameters bean.
     */
    protected AbstractBoxMacro(String name, String description, ContentDescriptor contentDescriptor,
        Class<?> parametersBeanClass)
    {
        super(name, description, contentDescriptor, parametersBeanClass);
    }

    @Override
    public boolean supportsInlineMode()
    {
        return true;
    }

    /**
     * @param parameters the parameters where to get the block title (see {@link BoxMacroParameters#getBlockTitle()}).
     * @return the title represented as a list of Blocks
     * @since 10.10RC1
     */
    @Unstable
    protected List<? extends Block> getBlockTitle(P parameters, String content, MacroTransformationContext context)
    {
        return parameters.getBlockTitle();
    }

    @Override
    public List<Block> execute(P parameters, String content, MacroTransformationContext context)
        throws MacroExecutionException
    {
        // TODO: Refactor this when it'll possible to have a specific converter associated to a macro parameter.
        ResourceReference imageReference = parameters.getImage();
        // If the image reference is unknown then resolve it with the untyped resource reference parser
        // (this happens when the user doesn't specify a type for the image reference).
        if (imageReference != null && imageReference.getType().equals(ResourceType.UNKNOWN)) {
            imageReference = this.untypedImageReferenceParser.parse(imageReference.getReference());
        }

        String titleParameter = parameters.getTitle();
        List<? extends Block> titleBlockList = this.getBlockTitle(parameters);

        // Use a linked hashmap to keep the parameters in the same order as we create them when they are retrieved
        // by renderers. This is useful for example in the Event renderer to control the order in which the params
        // are displayed.
        Map<String, String> boxParameters = new LinkedHashMap<String, String>();
        String classParameter = parameters.getCssClass();
        String cssClass =
            StringUtils.isEmpty(classParameter) ? getClassProperty() : getClassProperty() + " " + classParameter;
        boxParameters.put("class", cssClass);

        if (!StringUtils.isEmpty(parameters.getWidth())) {
            boxParameters.put("style", "width:" + parameters.getWidth());
        }

        Block boxBlock = new BoxBlockBuilder()
            .setParameters(parameters)
            .setContent(content)
            .setContext(context)
            .setBoxParameters(boxParameters)
            .setImageReference(imageReference)
            .setTitleParameter(titleParameter)
            .setTitleBlockList(titleBlockList)
            .build();

        if (boxBlock == null) {
            return Collections.emptyList();
        }

        return Collections.singletonList(boxBlock);
    }

    private final class BoxBlockBuilder
    {
        private P parameters;

        private String content;

        private MacroTransformationContext context;

        private Map<String, String> boxParameters;

        private ResourceReference imageReference;

        private String titleParameter;

        private List<? extends Block> titleBlockList;

        public BoxBlockBuilder setParameters(P parameters)
        {
            this.parameters = parameters;
            return this;
        }

        public BoxBlockBuilder setContent(String content)
        {
            this.content = content;
            return this;
        }

        public BoxBlockBuilder setContext(MacroTransformationContext context)
        {
            this.context = context;
            return this;
        }

        public BoxBlockBuilder setBoxParameters(Map<String, String> boxParameters)
        {
            this.boxParameters = boxParameters;
            return this;
        }

        public BoxBlockBuilder setImageReference(ResourceReference imageReference)
        {
            this.imageReference = imageReference;
            return this;
        }

        public BoxBlockBuilder setTitleParameter(String titleParameter)
        {
            this.titleParameter = titleParameter;
            return this;
        }

        public BoxBlockBuilder setTitleBlockList(List<? extends Block> titleBlockList)
        {
            this.titleBlockList = titleBlockList;
            return this;
        }

        public Block build() throws MacroExecutionException
        {
            Block ret = null;

            // if the content is empty but yet mandatory, we throw an exception
            if (StringUtils.isEmpty(content)
                && AbstractBoxMacro.this.getDescriptor().getContentDescriptor().isMandatory()) {
                throw new MacroExecutionException(CONTENT_MISSING_ERROR);
            }

            // if it's null but not mandatory we return null
            // if it's only empty we continue the processing
            if (content == null) {
                return ret;
            }

            if (context.isInline()) {
                List<Block> contentBlocks = parseContent(parameters, content, context);
                FormatBlock spanBlock = new FormatBlock(contentBlocks, Format.NONE);
                spanBlock.setParameters(boxParameters);
                ret = spanBlock;
            } else {
                ret = new GroupBlock(boxParameters);

                // we add the image, if there is one
                if (imageReference != null) {
                    Block imageBlock = new ImageBlock(imageReference, true);
                    ret.addChild(imageBlock);
                    ret.addChild(new NewLineBlock());
                }
                // we add the title, if there is one
                if (!StringUtils.isEmpty(titleParameter)) {
                    // Don't execute transformations explicitly. They'll be executed on the generated content later on.
                    ret.addChildren(AbstractBoxMacro.this.contentParser.parse(
                        titleParameter, context, false, true).getChildren());
                }
                if (titleBlockList != null) {
                    ret.addChildren(titleBlockList);
                }
                List<Block> contentBlocks = parseContent(parameters, content, context);
                ret.addChildren(contentBlocks);
            }

            return ret;
        }
    }

    /**
     * Execute macro content and return the result. This methods is separated form
     * {@link #execute(BoxMacroParameters, String, MacroTransformationContext)} to be able to overwrite it in macro
     * which need boxes.
     *
     * @param parameters the parameters of the macro.
     * @param content the content of the macro.
     * @param context the context if the macros transformation.
     * @return the result of the macro execution.
     * @throws MacroExecutionException error when executing the macro.
     */
    protected abstract List<Block> parseContent(P parameters, String content, MacroTransformationContext context)
        throws MacroExecutionException;

    /**
     * @return the name of the CSS class to use when rendering, in case no cssClass parameter is specified.
     */
    protected String getClassProperty()
    {
        return "box";
    }

    /**
     * @return the macro content parser to use to parse content in wiki syntax
     */
    protected MacroContentParser getMacroContentParser()
    {
        return this.contentParser;
    }
}
