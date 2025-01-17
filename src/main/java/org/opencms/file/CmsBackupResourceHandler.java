/*
 * File   : $Source: /usr/local/cvs/opencms/src/org/opencms/file/CmsBackupResourceHandler.java,v $
 * Date   : $Date: 2008-02-27 12:05:38 $
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
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

package org.opencms.file;

import org.opencms.file.history.CmsHistoryResourceHandler;
import org.opencms.file.history.I_CmsHistoryResource;

import javax.servlet.ServletRequest;

/**
 * Resource init handler that loads backup versions of resources.<p>
 *
 * @author Michael Emmerich 
 * 
 * @version $Revision: 1.6 $
 * 
 * @since 6.0.1
 * 
 * @deprecated use {@link CmsHistoryResourceHandler} instead
 */
public class CmsBackupResourceHandler extends CmsHistoryResourceHandler {

    /**
     * The historical version handler path.<p> 
     * 
     * @deprecated Use {@link #HISTORY_HANDLER} instead
     */
    public static final String BACKUP_HANDLER = HISTORY_HANDLER;

    /**
     * Returns the historical version of a resource, 
     * if the given request is displaying a history version.<p> 
     * 
     * @param req the request to check
     * 
     * @return the backup resource if the given request is displaying a history backup version
     * 
     * @deprecated use {@link CmsHistoryResourceHandler#getHistoryResource(ServletRequest)} instead
     */
    public static I_CmsHistoryResource getBackupResouce(ServletRequest req) {

        return getHistoryResource(req);
    }

    /**
     * Returns <code>true</code> if the given request is displaying a history backup version.<p> 
     * 
     * @param req the request to check
     * 
     * @return <code>true</code> if the given request is displaying a history backup version
     * 
     * @deprecated Use {@link #isHistoryRequest(ServletRequest)} instead
     */
    public static boolean isBackupRequest(ServletRequest req) {

        return isHistoryRequest(req);
    }
}
