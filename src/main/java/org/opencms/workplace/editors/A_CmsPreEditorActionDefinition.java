/*
 * File   : $Source: /usr/local/cvs/opencms/src/org/opencms/workplace/editors/A_CmsPreEditorActionDefinition.java,v $
 * Date   : $Date: 2008-02-27 12:05:23 $
 * Version: $Revision: 1.4 $
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

package org.opencms.workplace.editors;

import org.opencms.file.CmsResource;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.OpenCms;
import org.opencms.workplace.CmsDialog;

/**
 * Defines an action to be performed before the workplace editor is opened for the first time.<p>
 * 
 * Implements the basic methods to handle the resource type.<p>
 * 
 * @author Andreas Zahner 
 * 
 * @version $Revision: 1.4 $ 
 * 
 * @since 6.5.4 
 */
public abstract class A_CmsPreEditorActionDefinition implements I_CmsPreEditorActionDefinition {

    /** The resource type for which the action should be performed. */
    private I_CmsResourceType m_resourceType;

    /** The resource type name for which the action should be performed. */
    private String m_resourceTypeName;

    /**
     * Constructor, without parameters.<p>
     */
    public A_CmsPreEditorActionDefinition() {

        // empty constructor, needed for initialization
    }

    /**
     * @see org.opencms.workplace.editors.I_CmsPreEditorActionDefinition#doPreAction(org.opencms.file.CmsResource, org.opencms.workplace.CmsDialog, java.lang.String)
     */
    public abstract boolean doPreAction(CmsResource resource, CmsDialog dialog, String originalParams) throws Exception;

    /**
     * @see org.opencms.workplace.editors.I_CmsPreEditorActionDefinition#getResourceType()
     */
    public I_CmsResourceType getResourceType() {

        if (m_resourceType == null) {
            try {
                m_resourceType = OpenCms.getResourceManager().getResourceType(m_resourceTypeName);
            } catch (CmsLoaderException e) {
                // should not happen, ignore
            }
        }
        return m_resourceType;
    }

    /**
     * @see org.opencms.workplace.editors.I_CmsPreEditorActionDefinition#getResourceTypeName()
     */
    public String getResourceTypeName() {

        return m_resourceTypeName;
    }

    /**
     * @see org.opencms.workplace.editors.I_CmsPreEditorActionDefinition#setResourceTypeName(java.lang.String)
     */
    public void setResourceTypeName(String resourceTypeName) {

        m_resourceTypeName = resourceTypeName;
    }

}
