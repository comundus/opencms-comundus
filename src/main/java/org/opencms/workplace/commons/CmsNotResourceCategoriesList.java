/*
 * File   : $Source: /usr/local/cvs/opencms/src/org/opencms/workplace/commons/CmsNotResourceCategoriesList.java,v $
 * Date   : $Date: 2008-02-27 12:05:25 $
 * Version: $Revision: 1.5 $
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

package org.opencms.workplace.commons;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.relations.CmsCategory;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.I_CmsListFormatter;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Not resource categories view.<p>
 * 
 * @author Raphael Schnuck  
 * 
 * @version $Revision: 1.5 $ 
 * 
 * @since 6.9.2
 */
public class CmsNotResourceCategoriesList extends A_CmsResourceCategoriesList {

    /** list action id constant. */
    public static final String LIST_ACTION_ADD = "aa";

    /** list id constant. */
    public static final String LIST_ID = "lnrc";

    /** a set of action id's to use for adding. */
    protected static Set m_addActionIds = new HashSet();

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsNotResourceCategoriesList(CmsJspActionElement jsp) {

        this(jsp, LIST_ID);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsNotResourceCategoriesList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Protected constructor.<p>
     * @param jsp an initialized JSP action element
     * @param listId the id of the specialized list
     */
    protected CmsNotResourceCategoriesList(CmsJspActionElement jsp, String listId) {

        super(jsp, listId, Messages.get().container(Messages.GUI_NOTRESOURCECATEGORIES_LIST_NAME_0), true);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListSingleActions()
     */
    public void executeListSingleActions() throws CmsRuntimeException {

        if (getParamListAction().equals(LIST_ACTION_ADD)) {
            try {
                // lock resource if autolock is enabled
                checkLock(getParamResource());

                CmsListItem listItem = getSelectedItem();
                getCategoryService().addResourceToCategory(getCms(), getParamResource(), listItem.getId());
            } catch (CmsException e) {
                throw new CmsRuntimeException(e.getMessageContainer(), e);
            }
        } else {
            throwListUnsupportedActionException();
        }
        listSave();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#defaultActionHtmlStart()
     */
    protected String defaultActionHtmlStart() {

        return getList().listJs() + dialogContentStart(getParamTitle());
    }

    /**
     * @see org.opencms.workplace.commons.A_CmsResourceCategoriesList#getCategories()
     */
    protected List getCategories() throws CmsException {

        List resourceRelations = getResourceCategories();
        List result = getCategoryService().readAllCategories(getJsp().getCmsObject(), true);
        Iterator itResourceRelations = resourceRelations.iterator();
        while (itResourceRelations.hasNext()) {
            CmsCategory category = (CmsCategory)itResourceRelations.next();
            if (result.contains(category)
                && resourceRelations.containsAll(getCategoryService().readSubCategories(
                    getJsp().getCmsObject(),
                    category.getPath(),
                    true))) {
                result.remove(category);
            }
        }
        return result;
    }

    /**
     * @see org.opencms.workplace.commons.A_CmsResourceCategoriesList#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setColumns(CmsListMetadata metadata) {

        super.setColumns(metadata);

        metadata.getColumnDefinition(LIST_COLUMN_NAME).setFormatter(new I_CmsListFormatter() {

            /**
             * @see org.opencms.workplace.list.I_CmsListFormatter#format(java.lang.Object, java.util.Locale)
             */
            public String format(Object data, Locale locale) {

                CmsListColumnDefinition listColumnDefinition = getList().getMetadata().getColumnDefinition(
                    LIST_COLUMN_STATE);
                CmsListDirectAction stateAction = (CmsListDirectAction)listColumnDefinition.getDirectAction(LIST_ACTION_ADD);
                if (!stateAction.isEnabled()) {
                    StringBuffer ret = new StringBuffer();
                    ret.append("<span style=\"color: graytext;\">");
                    ret.append(data);
                    ret.append("</span>");
                    return ret.toString();
                }
                return (String)data;
            }

        });
    }

    /**
     * @see org.opencms.workplace.commons.A_CmsResourceCategoriesList#setStateActionCol(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setStateActionCol(CmsListMetadata metadata) {

        // create column for state change
        CmsListColumnDefinition stateCol = new CmsListColumnDefinition(LIST_COLUMN_STATE);
        stateCol.setName(Messages.get().container(Messages.GUI_CATEGORIES_LIST_COLS_STATE_0));
        stateCol.setHelpText(Messages.get().container(Messages.GUI_CATEGORIES_LIST_COLS_STATE_HELP_0));
        stateCol.setWidth("20");
        stateCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        stateCol.setSorteable(false);
        // add remove action
        CmsListDirectAction stateAction = new CmsListDirectAction(LIST_ACTION_ADD) {

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isEnabled()
             */
            public boolean isEnabled() {

                try {
                    if (((A_CmsResourceCategoriesList)getWp()).getResourceCategories().contains(
                        ((A_CmsResourceCategoriesList)getWp()).getCategoryService().readCategory(
                            getWp().getCms(),
                            getItem().getId()))) {
                        return false;
                    }
                } catch (CmsException e) {
                    // noop
                }
                return true;
            }
        };
        stateAction.setName(Messages.get().container(Messages.GUI_CATEGORIES_LIST_DEFACTION_ADD_NAME_0));
        stateAction.setHelpText(Messages.get().container(Messages.GUI_CATEGORIES_LIST_DEFACTION_ADD_HELP_0));
        stateAction.setIconPath(ICON_ADD);
        stateCol.addDirectAction(stateAction);
        // add it to the list definition
        metadata.addColumn(stateCol);
    }
}
