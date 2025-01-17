/*
 * File   : $Source: /usr/local/cvs/opencms/src/org/opencms/workplace/commons/CmsResourceCategoriesList.java,v $
 * Date   : $Date: 2008-02-27 12:05:24 $
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

import org.opencms.i18n.CmsMessageContainer;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.relations.CmsCategory;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListMetadata;

import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Resource categories view.<p>
 * 
 * @author Raphael Schnuck  
 * 
 * @version $Revision: 1.5 $ 
 * 
 * @since 6.9.2
 */
public class CmsResourceCategoriesList extends A_CmsResourceCategoriesList {

    /** list action id constant. */
    public static final String LIST_ACTION_REMOVE = "ar";

    /** list id constant. */
    public static final String LIST_ID = "lrc";

    /** list action id constant. */
    public static final String LIST_MACTION_REMOVE = "mr";

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsResourceCategoriesList(CmsJspActionElement jsp) {

        this(jsp, LIST_ID);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsResourceCategoriesList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Protected constructor.<p>
     * @param jsp an initialized JSP action element
     * @param listId the id of the specialized list
     */
    protected CmsResourceCategoriesList(CmsJspActionElement jsp, String listId) {

        super(jsp, listId, Messages.get().container(Messages.GUI_RESOURCECATEGORIES_LIST_NAME_0), true);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListSingleActions()
     */
    public void executeListSingleActions() throws CmsRuntimeException {

        if (getParamListAction().equals(LIST_ACTION_REMOVE)) {
            try {
                // lock resource if autolock is enabled
                checkLock(getParamResource());

                CmsListItem listItem = getSelectedItem();
                String categoryPath = listItem.getId();
                getCategoryService().removeResourceFromCategory(getCms(), getParamResource(), categoryPath);
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

        return getResourceCategories();
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
        CmsListDirectAction stateAction = new CmsListDirectAction(LIST_ACTION_REMOVE) {

            /**
             * @see org.opencms.workplace.list.A_CmsListAction#getConfirmationMessage()
             */
            public CmsMessageContainer getConfirmationMessage() {

                Iterator itCategories;
                try {
                    itCategories = ((A_CmsResourceCategoriesList)getWp()).getResourceCategories().iterator();
                } catch (CmsException e) {
                    e.printStackTrace();
                    return super.getConfirmationMessage();
                }
                while (itCategories.hasNext()) {
                    CmsCategory category = (CmsCategory)itCategories.next();
                    try {
                        if (((A_CmsResourceCategoriesList)getWp()).getResourceCategories().containsAll(
                            ((A_CmsResourceCategoriesList)getWp()).getCategoryService().readSubCategories(
                                getWp().getCms(),
                                category.getPath(),
                                true))) {
                            return Messages.get().container(Messages.GUI_CATEGORIES_LIST_DEFACTION_REMOVE_CONF_MORE_0);
                        }
                    } catch (CmsException e) {
                        return super.getConfirmationMessage();
                    }
                }
                return super.getConfirmationMessage();
            }
        };
        stateAction.setName(Messages.get().container(Messages.GUI_CATEGORIES_LIST_DEFACTION_REMOVE_NAME_0));
        stateAction.setHelpText(Messages.get().container(Messages.GUI_CATEGORIES_LIST_DEFACTION_REMOVE_HELP_0));
        stateAction.setIconPath(ICON_MINUS);
        stateAction.setConfirmationMessage(Messages.get().container(
            Messages.GUI_CATEGORIES_LIST_DEFACTION_REMOVE_CONF_SINGLE_0));
        stateCol.addDirectAction(stateAction);
        // add it to the list definition
        metadata.addColumn(stateCol);
    }
}
