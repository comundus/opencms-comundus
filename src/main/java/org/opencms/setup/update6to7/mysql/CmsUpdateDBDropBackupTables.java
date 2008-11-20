/*
 * File   : $Source: /usr/local/cvs/opencms/src/org/opencms/setup/update6to7/mysql/CmsUpdateDBDropBackupTables.java,v $
 * Date   : $Date: 2007-07-04 16:56:57 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.setup.update6to7.mysql;

import java.io.IOException;

/** 
 * This class drops the CMS_BACKUP tables that are no longer used after all the transfers are finished.<p> 
 * 
 * The tables to drop are
 * <ul>
 * <li>CMS_BACKUP_PROJECTRESOURCES</li>
 * <li>CMS_BACKUP_PROJECTS</li>
 * <li>CMS_BACKUP_PROPERTIES</li>
 * <li>CMS_BACKUP_PROPERTYDEF</li>
 * <li>CMS_BACKUP_RESOURCES</li>
 * <li>CMS_BACKUP_STRUCTURE</li>
 * </ul>
 * 
 * @author metzler
 */
public class CmsUpdateDBDropBackupTables extends org.opencms.setup.update6to7.generic.CmsUpdateDBDropBackupTables {

    /**
     * Constructor.<p>
     * 
     * @throws IOException if the query properties cannot be read
     */
    public CmsUpdateDBDropBackupTables()
    throws IOException {

        super();
    }
}
