/*
 * File   : $Source: /usr/local/cvs/opencms/src/org/opencms/staticexport/I_CmsStaticExportHandler.java,v $
 * Date   : $Date: 2008-02-27 12:05:46 $
 * Version: $Revision: 1.11 $
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

package org.opencms.staticexport;

import org.opencms.report.I_CmsReport;
import org.opencms.util.CmsUUID;

/**
 * Provides a method 
 * for scrubbing files from the export folder that might have been changed,
 * so that the export is newly created after the next request to the resource.<p>
 * 
 * @author Michael Moossen  
 * 
 * @version $Revision: 1.11 $ 
 * 
 * @since 6.0.0 
 */
public interface I_CmsStaticExportHandler {

    /**
     * Returns <code>true</code> if this static export handler is currently 
     * performing a static export operation.<p> 
     * 
     * @return <code>true</code> if this static export handler is currently 
     *              performing a static export operation
     */
    boolean isBusy();

    /**
     * Scrubs files from the export folder that might have been changed.
     * 
     * @param publishHistoryId the <code>{@link CmsUUID}</code> of the published project
     * @param report an <code>{@link I_CmsReport}</code> instance to print output message, 
     *              or <code>null</code> to write messages to the log file
     */
    void performEventPublishProject(CmsUUID publishHistoryId, I_CmsReport report);
}