/*
 * File   : $Source: /usr/local/cvs/opencms/src/org/opencms/workplace/list/CmsListSearchAction.java,v $
 * Date   : $Date: 2008-02-27 12:05:28 $
 * Version: $Revision: 1.15 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2008 Alkacon Software GmbH (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.list;

import org.opencms.i18n.CmsMessageContainer;
import org.opencms.workplace.CmsWorkplace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Default implementation for a seach action in an html list.<p>
 * 
 * It allows to search in several columns, including item details.<p>
 * 
 * @author Michael Moossen  
 * 
 * @version $Revision: 1.15 $ 
 * 
 * @since 6.0.0 
 */
public class CmsListSearchAction extends A_CmsListSearchAction implements I_CmsSearchMethod {

    /** Ids of Columns to search into. */
    private final List m_columns = new ArrayList();

    /**
     * Default Constructor.<p>
     * 
     * @param column the column to search into
     */
    public CmsListSearchAction(CmsListColumnDefinition column) {

        super();
        useDefaultShowAllAction();
        m_columns.add(column);
    }

    /**
     * Adds a column to search into.<p>
     * 
     * @param column the additional column to search into
     */
    public void addColumn(CmsListColumnDefinition column) {

        m_columns.add(column);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListSearchAction#buttonHtml(org.opencms.workplace.CmsWorkplace)
     */
    public String buttonHtml(CmsWorkplace wp) {

        // delay the composition of the help text as much as possible
        if (getHelpText() == EMPTY_MESSAGE) {
            String columns = "";
            Iterator it = m_columns.iterator();
            while (it.hasNext()) {
                CmsListColumnDefinition col = (CmsListColumnDefinition)it.next();
                columns += "${key." + col.getName().getKey() + "}";
                if (it.hasNext()) {
                    columns += ", ";
                }
            }
            if (columns.lastIndexOf(", ") > 0) {
                columns = columns.substring(0, columns.lastIndexOf(", "))
                    + " and "
                    + columns.substring(columns.lastIndexOf(", ") + 2);
            }
            setHelpText(new CmsMessageContainer(
                Messages.get(),
                Messages.GUI_LIST_ACTION_SEARCH_HELP_1,
                new Object[] {columns}));
        }
        return super.buttonHtml(wp);
    }

    /**
     * @see org.opencms.workplace.list.I_CmsSearchMethod#filter(java.util.List, java.lang.String)
     */
    public List filter(List items, String filter) {

        List res = new ArrayList();
        Iterator itItems = items.iterator();
        while (itItems.hasNext()) {
            CmsListItem item = (CmsListItem)itItems.next();
            if (res.contains(item)) {
                continue;
            }
            Iterator itCols = m_columns.iterator();
            while (itCols.hasNext()) {
                CmsListColumnDefinition col = (CmsListColumnDefinition)itCols.next();
                if (item.get(col.getId()) == null) {
                    continue;
                }
                if (item.get(col.getId()).toString().indexOf(filter) > -1) {
                    res.add(item);
                    break;
                }
            }
        }
        return res;
    }

    /**
     * Returns the list of columns to be searched.<p>
     * 
     * @return a list of {@link CmsListColumnDefinition} objects
     */
    public List getColumns() {

        return Collections.unmodifiableList(m_columns);
    }

    /**
     * @see org.opencms.workplace.list.I_CmsListAction#setWp(org.opencms.workplace.list.A_CmsListDialog)
     */
    public void setWp(A_CmsListDialog wp) {

        super.setWp(wp);
        if (getShowAllAction() != null) {
            getShowAllAction().setWp(wp);
        }
    }
}