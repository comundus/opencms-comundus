/*
 * File   : $Source: /usr/local/cvs/opencms/src/org/opencms/file/wrapper/CmsResourceExtensionWrapperXmlPage.java,v $
 * Date   : $Date: 2008-02-27 12:05:30 $
 * Version: $Revision: 1.6 $
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

package org.opencms.file.wrapper;

import org.opencms.file.types.CmsResourceTypeXmlPage;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;

/**
 * A resource type wrapper for xml pages, which adds the correct file extension "xml"
 * to the resources.<p>
 *
 * @author Peter Bonrad
 * 
 * @version $Revision: 1.6 $
 * 
 * @since 6.5.6
 */
public class CmsResourceExtensionWrapperXmlPage extends A_CmsResourceExtensionWrapper {

    /** The extension used for this resource type wrapper. */
    private static final String RESOURCE_TYPE_EXTENSION = "xml";

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceExtensionWrapper#checkTypeId(int)
     */
    protected boolean checkTypeId(int typeId) {

        try {
            I_CmsResourceType resType = OpenCms.getResourceManager().getResourceType(typeId);
            if (resType instanceof CmsResourceTypeXmlPage) {
                return true;
            }
        } catch (CmsException ex) {
            // noop
        }

        return false;
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceExtensionWrapper#getExtension()
     */
    protected String getExtension() {

        return RESOURCE_TYPE_EXTENSION;
    }

}
