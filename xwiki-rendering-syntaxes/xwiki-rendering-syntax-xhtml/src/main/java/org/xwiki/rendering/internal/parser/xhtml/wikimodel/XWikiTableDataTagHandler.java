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
package org.xwiki.rendering.internal.parser.xhtml.wikimodel;

import org.xwiki.rendering.wikimodel.WikiParameter;
import org.xwiki.rendering.wikimodel.WikiParameters;
import org.xwiki.rendering.wikimodel.xhtml.handler.TableDataTagHandler;
import org.xwiki.rendering.wikimodel.xhtml.impl.TagContext;

/**
 * Override the default implementation of hte WikiModel XHTML parser for handling HTML table cells. We need to do this
 * in order to handle clean auto generated scope attributes.
 *
 * @version $Id$
 * @since 2.4M1
 */
public class XWikiTableDataTagHandler extends TableDataTagHandler implements XWikiWikiModelHandler
{
    /**
     * Name of the th scope attribute.
     */
    private static final String TH_SCOPE = "scope";

    /**
     * Column value of the th scope attribute.
     */
    private static final String TH_SCOPE_COL = "col";

    /**
     * Row value of the th scope attribute.
     */
    private static final String TH_SCOPE_ROW = "row";

    @Override
    protected void begin(TagContext context)
    {
        WikiParameters parameters = context.getParams();

        // clean useless scope attributes
        WikiParameter scopeParameter = parameters.getParameter(TH_SCOPE);

        if (scopeParameter != null) {
            if (context.getScannerContext().getTableRowCounter() == 0) {
                if (scopeParameter.getValue().equals(TH_SCOPE_COL)) {
                    parameters = parameters.remove(TH_SCOPE);
                }
            } else if (context.getScannerContext().getTableCellCounter() == 0) {
                if (scopeParameter.getValue().equals(TH_SCOPE_ROW)) {
                    parameters = parameters.remove(TH_SCOPE);
                }
            } else {
                if (scopeParameter.getValue().equals(TH_SCOPE_COL)) {
                    parameters = parameters.remove(TH_SCOPE);
                }
            }
        }

        context.getScannerContext().beginTableCell(true, parameters);
    }
}
