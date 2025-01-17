/*
 * File   : $Source: /usr/local/cvs/opencms/src/org/opencms/workplace/commons/CmsResourceLinkRelationList.java,v $
 * Date   : $Date: 2008-02-27 12:05:23 $
 * Version: $Revision: 1.7 $
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

import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelationType;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.explorer.CmsResourceUtil;
import org.opencms.workplace.list.A_CmsListExplorerDialog;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListExplorerColumn;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListResourceProjStateAction;
import org.opencms.workplace.list.I_CmsListResourceCollector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * List for resources with relations to a given resource.<p>
 * 
 * @author Raphael Schnuck
 * 
 * @version $Revision: 1.7 $ 
 * 
 * @since 6.9.1 
 */
public class CmsResourceLinkRelationList extends A_CmsListExplorerDialog {

    /** List column id constant. */
    protected static final String LIST_COLUMN_RELATION_TYPE = "crt";

    /** The log object for this class. */
    protected static final Log LOG = CmsLog.getLog(CmsResourceLinkRelationList.class);

    /** The list id for this class. */
    private static final String LIST_ID = "lrlr";

    /** The list holds the broken links list item ids. */
    private List m_brokenLinks;

    /** The resource collector for this class. */
    private I_CmsListResourceCollector m_collector;

    /** Indicates if the current request shows the source resources for the relations are shown. */
    private boolean m_isSource;

    /** The map to map resources to relation types. */
    private Map m_relationTypes;

    /**
     * Default constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     * @param isSource indicates if the source resources of the relations are shown in the list
     */
    public CmsResourceLinkRelationList(CmsJspActionElement jsp, boolean isSource) {

        super(jsp, LIST_ID, Messages.get().container(Messages.GUI_LINK_RELATION_LIST_NAME_0));
        m_isSource = isSource;

        // set the right resource util parameters
        CmsResourceUtil resUtil = getResourceUtil();
        resUtil.setAbbrevLength(50);
        resUtil.setRelativeTo(CmsResource.getParentFolder(getParamResource()));
        resUtil.setSiteMode(CmsResourceUtil.SITE_MODE_MATCHING);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListMultiActions()
     */
    public void executeListMultiActions() {

        throwListUnsupportedActionException();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListSingleActions()
     */
    public void executeListSingleActions() {

        throwListUnsupportedActionException();
    }

    /**
     * Returns the list to identify the resources with broken links.<p>
     * 
     * @return the list to identify the resources with broken links
     */
    public List getBrokenLinks() {

        return m_brokenLinks;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListExplorerDialog#getCollector()
     */
    public I_CmsListResourceCollector getCollector() {

        if (m_collector == null) {
            m_collector = new CmsListResourceLinkRelationCollector(this, getParamResource(), isSource());
        }
        return m_collector;
    }

    /**
     * Returns the relationTypes.<p>
     *
     * @return the relationTypes
     */
    public Map getRelationTypes() {

        return m_relationTypes;
    }

    /**
     * Returns the isSource.<p>
     *
     * @return the isSource
     */
    public boolean isSource() {

        return m_isSource;
    }

    /**
     * Sets the list to identify the resources with broken links.<p>
     * 
     * @param brokenLinks the list to identify the resources with broken links
     */
    public void setBrokenLinks(List brokenLinks) {

        m_brokenLinks = brokenLinks;
    }

    /**
     * Sets the relationTypes.<p>
     *
     * @param relationTypes the relationTypes to set
     */
    public void setRelationTypes(Map relationTypes) {

        m_relationTypes = relationTypes;
    }

    /**
     * Sets the isSource.<p>
     *
     * @param isSource the isSource to set
     */
    public void setSource(boolean isSource) {

        m_isSource = isSource;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#defaultActionHtmlStart()
     */
    protected String defaultActionHtmlStart() {

        return getList().listJs() + CmsListExplorerColumn.getExplorerStyleDef() + dialogContentStart(getParamTitle());
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#fillDetails(java.lang.String)
     */
    protected void fillDetails(String detailId) {

        // empty
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListExplorerDialog#getListItems()
     */
    protected List getListItems() throws CmsException {

        List newItems = new ArrayList();
        List items = super.getListItems();
        Iterator itItems = items.iterator();
        while (itItems.hasNext()) {
            CmsListItem item = (CmsListItem)itItems.next();
            CmsResource resource = getResourceUtil(item).getResource();

            CmsRelationType relationType = (CmsRelationType)((List)getRelationTypes().get(resource)).remove(0);
            String localizedRelationType = relationType.getLocalizedName(getMessages());

            Map itemValues = item.getValues();
            CmsListItem newItem = getList().newItem(localizedRelationType + "_" + resource.getStructureId().toString());

            Iterator itItemValuesKeys = itemValues.entrySet().iterator();
            while (itItemValuesKeys.hasNext()) {
                Map.Entry e = (Map.Entry)itItemValuesKeys.next();
                String currentKey = (String)e.getKey();
                newItem.set(currentKey, e.getValue());
            }
            newItem.set(LIST_COLUMN_RELATION_TYPE, localizedRelationType);
            newItems.add(newItem);
        }
        return newItems;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListExplorerDialog#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setColumns(CmsListMetadata metadata) {

        super.setColumns(metadata);
        // position 3: project state icon, resource is inside or outside current project        
        CmsListColumnDefinition projStateIconCol = metadata.getColumnDefinition(LIST_COLUMN_PROJSTATEICON);
        projStateIconCol.removeDirectAction(LIST_ACTION_PROJSTATEICON);

        // add resource icon action
        CmsListDirectAction resourceProjStateAction = new CmsListResourceProjStateAction(LIST_ACTION_PROJSTATEICON) {

            /**
             * @see org.opencms.workplace.list.CmsListResourceProjStateAction#getIconPath()
             */
            public String getIconPath() {

                if (((CmsResourceLinkRelationList)getWp()).getBrokenLinks() != null) {
                    if (((CmsResourceLinkRelationList)getWp()).getBrokenLinks().contains(getItem().getId())) {
                        return "buttons/deletecontent.png";
                    }
                }
                return super.getIconPath();
            }

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#getName()
             */
            public CmsMessageContainer getName() {

                if (((CmsResourceLinkRelationList)getWp()).getBrokenLinks() != null) {
                    if (((CmsResourceLinkRelationList)getWp()).getBrokenLinks().contains(getItem().getId())) {
                        return Messages.get().container(Messages.GUI_RELATION_LIST_BROKEN_HELP_0);
                    }
                }
                return super.getName();
            }
        };
        resourceProjStateAction.setEnabled(false);
        projStateIconCol.addDirectAction(resourceProjStateAction);

        CmsListColumnDefinition relationTypeCol = new CmsListResourceLinkRelationExplorerColumn(
            LIST_COLUMN_RELATION_TYPE);
        relationTypeCol.setName(Messages.get().container(Messages.GUI_RELATION_LIST_TYPE_NAME_0));
        metadata.addColumn(relationTypeCol, 4);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListExplorerDialog#isColumnVisible(int)
     */
    protected boolean isColumnVisible(int colFlag) {

        boolean isVisible = (colFlag == CmsUserSettings.FILELIST_TITLE);
        isVisible = isVisible || (colFlag == LIST_COLUMN_TYPEICON.hashCode());
        isVisible = isVisible || (colFlag == LIST_COLUMN_LOCKICON.hashCode());
        isVisible = isVisible || (colFlag == LIST_COLUMN_PROJSTATEICON.hashCode());
        isVisible = isVisible || (colFlag == LIST_COLUMN_NAME.hashCode());
        isVisible = isVisible || (colFlag == CmsUserSettings.FILELIST_TYPE);
        isVisible = isVisible || (colFlag == CmsUserSettings.FILELIST_SIZE);
        isVisible = isVisible
            || ((colFlag == LIST_COLUMN_SITE.hashCode()) && (OpenCms.getSiteManager().getSites().size() > 1));
        return isVisible;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setMultiActions(CmsListMetadata metadata) {

        // empty
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#validateParamaters()
     */
    protected void validateParamaters() throws Exception {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(getParamResource())) {
            throw new Exception();
        }
    }
}
