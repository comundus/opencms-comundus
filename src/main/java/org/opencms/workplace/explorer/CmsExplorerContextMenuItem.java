/*
 * File   : $Source: /usr/local/cvs/opencms/src/org/opencms/workplace/explorer/CmsExplorerContextMenuItem.java,v $
 * Date   : $Date: 2008-02-27 12:05:21 $
 * Version: $Revision: 1.12 $
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

package org.opencms.workplace.explorer;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides information about a single context menu item for a resource type in the OpenCms explorer view.<p>
 * 
 * An item can be a context menu entry or a separator line.<p>
 * 
 * @author Andreas Zahner 
 * 
 * @version $Revision: 1.12 $ 
 * 
 * @since 6.0.0 
 */
public class CmsExplorerContextMenuItem {

    /** The name for an entry type. */
    public static final String TYPE_ENTRY = "entry";

    /** The name for a separator type. */
    public static final String TYPE_SEPARATOR = "sep";

    private String m_key;
    private CmsExplorerContextMenuItem m_parent;
    private String m_rule;
    private String m_rules;
    private List m_subItems;
    private String m_target;
    private String m_type;
    private String m_uri;

    /**
     * Empty constructor that creates a single context menu entry.<p>
     */
    public CmsExplorerContextMenuItem() {

        // noop
    }

    /**
     * Adds a menu sub entry to this context menu item.<p>
     * 
     * @param item the entry item to add to this context menu item
     */
    public void addContextMenuEntry(CmsExplorerContextMenuItem item) {

        addSubItem(item, TYPE_ENTRY);
    }

    /**
     * Adds a menu separator to this context menu item.<p>
     * 
     * @param item the separator item to add to this context menu item
     */
    public void addContextMenuSeparator(CmsExplorerContextMenuItem item) {

        addSubItem(item, TYPE_SEPARATOR);
    }

    /**
     * Returns the key for localization.<p>
     * 
     * @return the key for localization
     */
    public String getKey() {

        return m_key;
    }

    /**
     * Returns the parent context menu entry item of a sub menu item.<p>
     * 
     * @return the parent context menu entry item
     */
    public CmsExplorerContextMenuItem getParent() {

        return m_parent;
    }

    /**
     * Returns the name of the menu rule set.<p>
     * 
     * @return the name of the menu rule set
     */
    public String getRule() {

        return m_rule;
    }

    /**
     * Returns the set of display rules.<p>
     * 
     * @return the set of display rules
     */
    public String getRules() {

        return m_rules;
    }

    /**
     * Returns the sub item entries of this context menu item.<p>
     * 
     * @return the sub item entries of this context menu item
     */
    public List getSubItems() {

        return m_subItems;
    }

    /**
     * Returns the frame target of the current item.<p>
     * 
     * @return the frame target of the current item
     */
    public String getTarget() {

        return m_target;
    }

    /**
     * Returns the type of the current item.<p>
     * 
     * @return the type of the current item
     */
    public String getType() {

        return m_type;
    }

    /**
     * Returns the dialog URI of the current item.<p>
     * 
     * @return the dialog URI of the current item
     */
    public String getUri() {

        return m_uri;
    }

    /**
     * Returns if the item is a main item with configured sub items.<p>
     * 
     * @return true if the item is a main entry item with configured sub items, otherwise false
     */
    public boolean isParentItem() {

        return m_subItems != null;
    }

    /**
     * Returns if the item is a sub item.<p>
     * 
     * @return true if the item is a sub entry item, otherwise false
     */
    public boolean isSubItem() {

        return m_parent != null;
    }

    /**
     * Sets the key for localization.<p>
     * 
     * @param key the key for localization
     */
    public void setKey(String key) {

        m_key = key;
    }

    /**
     * Sets the name of the menu rule set.<p>
     * 
     * @param rule the name of the menu rule set
     */
    public void setRule(String rule) {

        m_rule = rule;
    }

    /**
     * Sets the set of display rules.<p>
     * 
     * @param rules the set of display rules
     */
    public void setRules(String rules) {

        m_rules = rules;
    }

    /**
     * Sets the frame target of the current item.<p>
     * 
     * @param target the frame target of the current item
     */
    public void setTarget(String target) {

        m_target = target;
    }

    /**
     * Sets the type of the current item.<p>
     * 
     * @param type the type of the current item
     */
    public void setType(String type) {

        m_type = type;
    }

    /**
     * Sets the dialog URI of the current item.<p>
     * 
     * @param uri the dialog URI of the current item
     */
    public void setUri(String uri) {

        m_uri = uri;
    }

    /**
     * Adds a sub item entry to this context menu item.<p>
     * 
     * @param item the item to add to this context menu item
     * @param type the item type to add
     */
    protected void addSubItem(CmsExplorerContextMenuItem item, String type) {

        if (m_subItems == null) {
            m_subItems = new ArrayList();
        }
        item.setType(type);
        m_subItems.add(item);
        item.setParent(this);
    }

    /**
     * Sets the parent context menu item for sub menu items.<p>
     * 
     * @param parent the parent context menu item
     */
    protected void setParent(CmsExplorerContextMenuItem parent) {

        m_parent = parent;
    }
}