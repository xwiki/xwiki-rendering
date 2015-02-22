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
package org.xwiki.rendering.internal.parser.markdown11;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.pegdown.ast.InlineHtmlNode;
import org.pegdown.ast.Node;
import org.pegdown.ast.SuperNode;
import org.xwiki.rendering.internal.parser.markdown.AbstractTablePegdownVisitor;
import org.xwiki.rendering.internal.parser.markdown11.ast.MacroNode;
import org.xwiki.rendering.internal.parser.markdown11.ast.MacroParameterNode;
import org.xwiki.rendering.internal.parser.markdown11.ast.SubscriptNode;
import org.xwiki.rendering.internal.parser.markdown11.ast.SuperscriptNode;
import org.xwiki.rendering.listener.Format;
import org.xwiki.rendering.syntax.Syntax;

/**
 * Implements Pegdown Visitor's {@link org.pegdown.ast.Node} for Pegdown's plugins.
 *
 * @version $Id $
 * @since 5.2RC1
 */
public abstract class AbstractPluginsPegdownVisitor extends AbstractTablePegdownVisitor
{
    @Override
    public void visit(Node node)
    {
        // this is kinda ugly but whole visitor's implementation should be refactored in future...
        if (node instanceof SuperscriptNode) {
            visit((SuperscriptNode) node);
        } else if (node instanceof SubscriptNode) {
            visit((SubscriptNode) node);
        } else if (node instanceof MacroNode) {
            visit((MacroNode) node);
        } else {
            throw new RuntimeException("Don't know how to handle node " + node);
        }
    }

    /**
     * @param node a superscript node
     */
    public void visit(SuperscriptNode node)
    {
        handleFormatted(node, Format.SUPERSCRIPT);
    }

    /**
     * @param node a subscript node
     */
    public void visit(SubscriptNode node)
    {
        handleFormatted(node, Format.SUBSCRIPT);
    }

    /**
     * @param node a macro node
     */
    public void visit(MacroNode node)
    {
        Map<String, String> parameters = new LinkedHashMap<String, String>(node.getParameters().size());
        for (MacroParameterNode param : node.getParameters()) {
            parameters.put(param.getName(), param.getValue());
        }
        String content = extractText(node);
        content = content.length() > 0 ? content : null;

        getListener().onMacro(node.getMacroId(), parameters, content, node.isInline());
    }

    /**
     * Handles formatted text node. This is common code for superscript and subscript visitor.
     *
     * @param node any text node
     * @param format formatting style
     */
    @SuppressWarnings("unchecked")
    private void handleFormatted(SuperNode node, Format format)
    {
        getListener().beginFormat(format, Collections.EMPTY_MAP);
        visitChildren(node);
        getListener().endFormat(format, Collections.EMPTY_MAP);
    }

    /**
     * {@inheritDoc}
     *
     * @see InlineXHtmlPegdownPluginParser
     */
    @Override
    public void visit(InlineHtmlNode inlineHtmlNode)
    {
        getListener().onRawText(inlineHtmlNode.getText(), Syntax.XHTML_1_0);
    }

    @Override
    public void visit(WikiLinkNode wikiLinkNode)
    {
        String label = "";
        String link = "";
        String parametersString= "";

        String[] separated = wikiLinkNode.getText().split("(?<!\\\\)(?:\\\\\\\\)*\\|");


        for(int i=0; i<separated.length; i++)
        {
            String s = separated[i];
            s = s.replace("\\|", "|");

            if(i == 0)
            {
                label = s;
            }
            else if(i == 1)
            {
                link = s;
            }
            else
            {
                parametersString = s;
                break;
            }
        }

        Map<String,String> parameters = this.parametersStringToMap(parametersString);

        ResourceReference reference = this.linkResourceReferenceParser.parse(link);
        getListener().beginLink(reference, false, parameters);

        try {
            // WrappingListener filterListener = new SomeCustomFilterListener();
            // filterListener.setWrappedListener(getListener());
            filterListener = getListener();
            this.markdownStreamParser.parse(new StringReader(label), filterListener);
        } catch (ParseException e) {
            throw new RuntimeException(String.format("Error parsing content [%s]", label), e);
        }

        getListener().endLink(reference, false, parameters);
    }

    private  Map<String,String> parametersStringToMap(String parametersString)
    {
        Map<String,String> parameters = new HashMap<String, String>();

        String key = "";
        String value = "";

        boolean beginValueByComa = false;
        boolean isKeyParsing = true;

        for(int i = 0; i<parametersString.length(); i++) {
            char c = parametersString.charAt(i);

            if(isKeyParsing) {
                if(c == '=') {
                    isKeyParsing = false;
                }
                else {
                    key += c;
                }
            }
            else {
                if(c == ' ' && beginValueByComa == false ) {
                    isKeyParsing = true;
                    if(value != "") {
                        value = value.trim();
                        parameters.put(key,value);
                        value = "";
                        key = "";
                    }
                }
                else {
                    if(c == '"' && value == "") {
                        beginValueByComa = true;
                    }
                    else if(c == '"' && beginValueByComa == true && value.endsWith("\\") == false) {
                        beginValueByComa = false;
                        if(i == (parametersString.length()-1)) {
                            value = value.trim();
                            parameters.put(key,value);
                            value = "";
                            key = "";
                        }
                    }
                    else {
                        value += c;
                    }
                }
            }
        }
        return parameters;
    }
}
