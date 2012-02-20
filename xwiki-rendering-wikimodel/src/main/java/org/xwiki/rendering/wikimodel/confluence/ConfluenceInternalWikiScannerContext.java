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
package org.xwiki.rendering.wikimodel.confluence;

import java.util.Stack;

import org.xwiki.rendering.wikimodel.IWemListener;
import org.xwiki.rendering.wikimodel.WikiParameters;
import org.xwiki.rendering.wikimodel.impl.InternalWikiScannerContext;
import org.xwiki.rendering.wikimodel.util.IListListener;
import org.xwiki.rendering.wikimodel.util.ListBuilder;
import org.xwiki.rendering.wikimodel.util.SectionBuilder;

public class ConfluenceInternalWikiScannerContext extends
    InternalWikiScannerContext
{
    private int fPreBlockType = IBlockTypes.NONE;

    public ConfluenceInternalWikiScannerContext(
        SectionBuilder<WikiParameters> sectionBuilder, IWemListener listener)
    {
        super(sectionBuilder, listener);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Modified to make sure last empty cell of a row is not taken into account.
     *
     * @see org.xwiki.rendering.wikimodel.impl.InternalWikiScannerContext#onTableCell(boolean,
     *      org.xwiki.rendering.wikimodel.WikiParameters)
     */
    @Override
    public void onTableCell(boolean head, WikiParameters params)
    {
        checkStyleOpened();
        checkListOpened();
        endTableCell();
        fTableHead = head;
        fTableCellParams = params != null ? params : WikiParameters.EMPTY;

        // Commented because we don't want last empty cell of a row to be taken
        // into account
        // beginTableCell(head, params);
    }

    @Override
    public void beginList()
    {
        if ((fBlockType & IBlockTypes.LIST) != IBlockTypes.LIST) {
            if (!isInTable()) {
                closeBlock();
            } else {
                checkStyleOpened();
                fPreBlockType = fBlockType;
            }
            if (fListParams == null) {
                fListParams = WikiParameters.EMPTY;
            }
            IListListener listener = new IListListener()
            {
                private Stack<WikiParameters> fListParamsStack = new Stack<WikiParameters>();

                public void beginRow(char treeType, char rowType)
                {
                    if (rowType == ':') {
                        fBlockType = IBlockTypes.LIST_DL_DD;
                        fListener.beginDefinitionDescription();
                    } else if (rowType == ';') {
                        fBlockType = IBlockTypes.LIST_DL_DT;
                        fListener.beginDefinitionTerm();
                    } else {
                        fBlockType = IBlockTypes.LIST_LI;
                        fListener.beginListItem();
                    }
                    beginStyleContainer();
                }

                public void beginTree(char type)
                {
                    fListParamsStack.push(fListParams);

                    closeFormat();
                    switch (type) {
                        case '#':
                            fListener.beginList(fListParams, true);
                            fBlockType = IBlockTypes.LIST;
                            break;
                        case 'd':
                            fListener.beginDefinitionList(fListParams);
                            fBlockType = IBlockTypes.LIST_DL;
                            break;
                        default:
                            fListener.beginList(fListParams, false);
                            fBlockType = IBlockTypes.LIST;
                            break;
                    }

                    fListParams = WikiParameters.EMPTY;
                }

                public void endRow(char treeType, char rowType)
                {
                    closeFormat();
                    endStyleContainer();
                    if (rowType == ':') {
                        fListener.endDefinitionDescription();
                        fBlockType = IBlockTypes.LIST_DL;
                    } else if (rowType == ';') {
                        if ((fBlockType & IBlockTypes.LIST_DL_DT) == IBlockTypes.LIST_DL_DT) {
                            fListener.endDefinitionTerm();
                        } else {
                            fListener.endDefinitionDescription();
                        }
                        fBlockType = IBlockTypes.LIST_DL;
                    } else {
                        fListener.endListItem();
                        fBlockType = IBlockTypes.LIST;
                    }
                }

                public void endTree(char type)
                {
                    switch (type) {
                        case '#':
                            fListener.endList(fListParamsStack.peek(), true);
                            fBlockType = IBlockTypes.LIST;
                            break;
                        case 'd':
                            fListener
                                .endDefinitionList(fListParamsStack.peek());
                            fBlockType = IBlockTypes.LIST;
                            break;
                        default:
                            fListener.endList(fListParamsStack.peek(), false);
                            fBlockType = IBlockTypes.LIST;
                            break;
                    }

                    fListParamsStack.pop();
                }
            };
            fListBuilder = new ListBuilder(listener)
            {
                @Override
                protected char getTreeType(char rowType)
                {
                    if (rowType == ';' || rowType == ':') {
                        return 'd';
                    }
                    return rowType;
                }
            };
            fBlockType = IBlockTypes.LIST;
        }
    }

    @Override
    public void endList()
    {
        if ((fBlockType & IBlockTypes.LIST) != 0) {
            fListBuilder.alignContext("");
            fListBuilder = null;

            fBlockType = fPreBlockType;
            fPreBlockType = IBlockTypes.NONE;
        }
    }

    public boolean isExplicitInTable()
    {
        return super.isInTable()
            || (((fPreBlockType & IBlockTypes.TABLE) == IBlockTypes.TABLE) &&
            (fBlockType & IBlockTypes.LIST) == IBlockTypes.LIST);
    }

    private void checkListOpened()
    {
        if (isExplicitInTable()) {
            endList();
        }
    }
}
