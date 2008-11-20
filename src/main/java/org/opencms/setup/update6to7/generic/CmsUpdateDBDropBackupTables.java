/*
 * File   : $Source: /usr/local/cvs/opencms/src/org/opencms/setup/update6to7/generic/CmsUpdateDBDropBackupTables.java,v $
 * Date   : $Date: 2007-07-04 16:56:40 $
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

package org.opencms.setup.update6to7.generic;

import org.opencms.setup.CmsSetupDb;
import org.opencms.setup.update6to7.A_CmsUpdateDBPart;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

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
 * @author Roland Metzler
 * 
 * @version $Revision: 1.2 $ 
 * 
 * @since 7.0.0
 */
public class CmsUpdateDBDropBackupTables extends A_CmsUpdateDBPart {

    /** Array of the BACKUP tables that are to be dropped.<p> */
    protected static final String[] BACKUP_TABLES = {
        "CMS_BACKUP_PROJECTRESOURCES",
        "CMS_BACKUP_PROJECTS",
        "CMS_BACKUP_PROPERTIES",
        "CMS_BACKUP_PROPERTYDEF",
        "CMS_BACKUP_RESOURCES",
        "CMS_BACKUP_STRUCTURE"};

    /** Constant ArrayList of the BACKUP_TABLES that are to be dropped.<p> */
    protected static final List BACKUP_TABLES_LIST = Collections.unmodifiableList(Arrays.asList(BACKUP_TABLES));

    /** Constant for the replacement of the tablename in the sql query.<p> */
    protected static final String REPLACEMENT_TABLENAME = "${tablename}";

    /** Constant for the sql query to drop a table.<p> */
    private static final String QUERY_DROP_TABLE = "Q_DROP_TABLE";

    /** Constant for the SQL query properties.<p> */
    private static final String QUERY_PROPERTY_FILE = "generic/cms_drop_backup_tables_queries.properties";

    /**
     * Constructor.<p>
     * 
     * @throws IOException if the query properties cannot be read
     */
    public CmsUpdateDBDropBackupTables()
    throws IOException {

        super();
        loadQueryProperties(QUERY_PROPERTIES_PREFIX + QUERY_PROPERTY_FILE);
    }

    /**
     * @see org.opencms.setup.update6to7.A_CmsUpdateDBPart#internalExecute(org.opencms.setup.CmsSetupDb)
     */
    protected void internalExecute(CmsSetupDb dbCon) {

        System.out.println(new Exception().getStackTrace()[0].toString());
        String dropQuery = readQuery(QUERY_DROP_TABLE);
        for (Iterator it = BACKUP_TABLES_LIST.iterator(); it.hasNext();) {
            String table = (String)it.next();
            HashMap replacer = new HashMap();
            replacer.put(REPLACEMENT_TABLENAME, table);
            try {
                dbCon.updateSqlStatement(dropQuery, replacer, null);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
