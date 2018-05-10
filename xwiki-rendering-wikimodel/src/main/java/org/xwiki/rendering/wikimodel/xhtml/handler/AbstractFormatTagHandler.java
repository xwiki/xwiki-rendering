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

import java.io.StringReader;

import org.w3c.css.sac.InputSource;
import org.xwiki.rendering.wikimodel.WikiParameter;
import org.xwiki.rendering.wikimodel.WikiParameters;
import org.xwiki.rendering.wikimodel.WikiStyle;
import org.xwiki.rendering.wikimodel.xhtml.impl.TagContext;

import com.steadystate.css.dom.CSSStyleDeclarationImpl;
import com.steadystate.css.parser.CSSOMParser;
import com.steadystate.css.parser.SACParserCSS21;

/**
 * @version $Id$
 * @since 4.0M1
 */
public abstract class AbstractFormatTagHandler extends TagHandler
{
    public static final String FORMATWIKISTYLE = "formatWikiStyle";

    public static final String FORMATPARAMETERS = "formatParameters";

    public static final String FORMATSTYLEPARAMETER = "formatStyleParameter";

    private final WikiStyle style;

    /**
     * The object used to parse the style attribute. Explicitly specify the
     * parser to use, since otherwise cssparser overrides the default parser
     * used in the JVM, breaking css4j.
     */
    private final CSSOMParser cssParser = new CSSOMParser(new SACParserCSS21());

    public AbstractFormatTagHandler()
    {
        this(null);
    }

    protected AbstractFormatTagHandler(WikiStyle style)
    {
        super(true);

        this.style = style;
    }

    @Override
    protected void begin(TagContext context)
    {
        // parameters
        WikiParameters currentParameters = (WikiParameters) context
            .getTagStack().getStackParameter(FORMATPARAMETERS);
        CSSStyleDeclarationImpl currentStyle = (CSSStyleDeclarationImpl) context
            .getTagStack().getStackParameter(FORMATSTYLEPARAMETER);

        if (currentParameters != null) {
            for (WikiParameter parameter : context.getParams()) {
                WikiParameter currentParameter = currentParameters
                    .getParameter(parameter.getKey());

                String value = parameter.getValue();

                if (currentParameter != null) {
                    if ("style".equals(parameter.getKey())) {
                        CSSStyleDeclarationImpl mergedStyle = mergeStyle(
                            currentStyle, currentParameter.getValue(),
                            parameter.getValue());

                        if (mergedStyle != currentStyle) {
                            value = mergedStyle.getCssText();
                            currentStyle = mergedStyle;
                        }
                    } else if ("class".equals(parameter.getKey())) {
                        value = mergeClass(currentParameter.getValue(),
                            parameter.getValue());
                    }
                }

                currentParameters = currentParameters.setParameter(
                    parameter.getKey(), value);
            }
        } else {
            currentParameters = new WikiParameters(context.getParams());
        }
        context.getTagStack().pushStackParameter(FORMATPARAMETERS,
            currentParameters);
        context.getTagStack().pushStackParameter(FORMATSTYLEPARAMETER,
            currentStyle);

        if (currentParameters.getSize() > 0) {
            context.getScannerContext().beginFormat(currentParameters);
        }

        // style
        if (this.style != null) {
            context.getScannerContext().beginFormat(this.style);
            context.getTagStack().pushStackParameter(FORMATWIKISTYLE,
                this.style);
        }
    }

    private CSSStyleDeclarationImpl mergeStyle(
        CSSStyleDeclarationImpl parentStyle, String parentStyleValue,
        String styleValue)
    {
        CSSStyleDeclarationImpl currentStyle = new CSSStyleDeclarationImpl();

        if (parentStyle == null) {
            try {
                this.cssParser.parseStyleDeclaration(currentStyle,
                    new InputSource(new StringReader(parentStyleValue)));
            } catch (Exception e) {
                return parentStyle;
            }
        } else {
            currentStyle.setProperties(parentStyle.getProperties());
        }

        try {
            this.cssParser.parseStyleDeclaration(currentStyle, new InputSource(
                new StringReader(styleValue)));
        } catch (Exception e) {
            return parentStyle;
        }

        return currentStyle;
    }

    private String mergeClass(String value1, String value2)
    {
        return value1 + " " + value2;
    }

    @Override
    protected void end(TagContext context)
    {
        // style
        if (this.style != null) {
            context.getScannerContext().endFormat(this.style);
            context.getTagStack().popStackParameter(FORMATWIKISTYLE);
        }

        // parameters
        if (context.getParams().getSize() > 0) {
            context.getScannerContext().endFormat(WikiParameters.EMPTY);
        }

        context.getTagStack().popStackParameter(FORMATPARAMETERS);
        context.getTagStack().popStackParameter(FORMATSTYLEPARAMETER);

        // reopen

        WikiStyle currentWikiStyle = (WikiStyle) context.getTagStack()
            .getStackParameter(FORMATWIKISTYLE);
        WikiParameters currentParameters = (WikiParameters) context
            .getTagStack().getStackParameter(FORMATPARAMETERS);

        if (currentParameters != null && currentParameters.getSize() > 0) {
            context.getScannerContext().beginFormat(currentParameters);
            if (currentWikiStyle != null) {
                context.getScannerContext().beginFormat(currentWikiStyle);
            }
        }
    }
}
