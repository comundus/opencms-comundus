/*
 * File   : $Source: /usr/local/cvs/opencms/src/org/opencms/setup/update6to7/oracle/CmsUpdateDBProjectId.java,v $
 * Date   : $Date: 2007-07-04 16:56:38 $
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

package org.opencms.setup.update6to7.oracle;

import org.opencms.setup.CmsSetupDb;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Oracle implementation to update the project ids to uuids.<p>
 * 
 * @author Roland Metzler
 * @author Peter Bonrad
 *
 * @version $Revision: 1.2 $
 * 
 * @since 7.0.0
 */
public class CmsUpdateDBProjectId extends org.opencms.setup.update6to7.generic.CmsUpdateDBProjectId {

    /** Constant for the sql primary key of the CMS_PROJECTRESOURCES table.<p> */
    private static final String COLUMN_PROJECT_ID_RESOURCE_PATH = "PROJECT_ID,RESOURCE_PATH";

    /** Constant for the SQL query properties.<p> */
    private static final String QUERY_PROPERTY_FILE = "oracle/cms_projectid_queries.properties";

    /** Constant for the replacement in the sql query. */
    private static final String REPLACEMENT_TABLEINDEX_SPACE = "${indexTablespace}";

    /**
     * Constructor.<p>
     * 
     * @throws IOException if the sql queries properties file could not be read
     */
    public CmsUpdateDBProjectId()
    throws IOException {

        super();
        loadQueryProperties(QUERY_PROPERTIES_PREFIX + QUERY_PROPERTY_FILE);
    }

    /**
     * @see org.opencms.setup.update6to7.generic.CmsUpdateDBProjectId#addPrimaryKey(org.opencms.setup.CmsSetupDb, java.lang.String, java.lang.String)
     */
    protected void addPrimaryKey(CmsSetupDb dbCon, String tablename, String primaryKey) throws SQLException {

        String indexTablespace = (String)m_poolData.get("indexTablespace");

        System.out.println(new Exception().getStackTrace()[0].toString());
        if (dbCon.hasTableOrColumn(tablename, null)) {
            String query = readQuery(QUERY_ADD_PRIMARY_KEY);
            Map replacer = new HashMap();
            replacer.put(REPLACEMENT_TABLENAME, tablename);
            replacer.put(REPLACEMENT_PRIMARY_KEY, primaryKey);
            replacer.put(REPLACEMENT_TABLEINDEX_SPACE, indexTablespace);
            dbCon.updateSqlStatement(query, replacer, null);
        } else {
            System.out.println("table " + tablename + " does not exists");
        }
    }

    /**
     * @see org.opencms.setup.update6to7.generic.CmsUpdateDBProjectId#addUUIDColumnToTable(org.opencms.setup.CmsSetupDb, java.lang.String, java.lang.String)
     */
    protected void addUUIDColumnToTable(CmsSetupDb dbCon, String tablename, String column) throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());
        if (!dbCon.hasTableOrColumn(tablename, column)) {
            String query = readQuery(QUERY_ADD_TEMP_UUID_COLUMN); // Get the query
            // if the table is not one of the ONLINE or OFFLINE resources add the new column in the first position
            if (!RESOURCES_TABLES_LIST.contains(tablename)) {
                //query += " FIRST";
            }
            Map replacer = new HashMap(); // Build the replacements
            replacer.put(REPLACEMENT_TABLENAME, tablename);
            replacer.put(REPLACEMENT_COLUMN, column);
            dbCon.updateSqlStatement(query, replacer, null); // execute the query
        } else {
            System.out.println("column " + column + " in table " + tablename + " already exists");
        }
    }

    /**
     * @see org.opencms.setup.update6to7.generic.CmsUpdateDBProjectId#checkColumnTypeProjectId(int)
     */
    protected boolean checkColumnTypeProjectId(int type) {

        return type == java.sql.Types.NUMERIC;
    }

    /**
     * @see org.opencms.setup.update6to7.generic.CmsUpdateDBProjectId#createHistProjectsTable(org.opencms.setup.CmsSetupDb)
     */
    protected void createHistProjectsTable(CmsSetupDb dbCon) throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());
        if (!dbCon.hasTableOrColumn(HISTORY_PROJECTS_TABLE, null)) {
            String createStatement = readQuery(QUERY_CREATE_HISTORY_PROJECTS_TABLE);

            String indexTablespace = (String)m_poolData.get("indexTablespace");
            HashMap replacer = new HashMap();
            replacer.put(REPLACEMENT_TABLEINDEX_SPACE, indexTablespace);

            dbCon.updateSqlStatement(createStatement, replacer, null);
            transferDataToHistoryTable(dbCon);
        } else {
            System.out.println("table " + HISTORY_PROJECTS_TABLE + " already exists");
        }
    }

    /**
     * @see org.opencms.setup.update6to7.generic.CmsUpdateDBProjectId#createTempTable(org.opencms.setup.CmsSetupDb)
     */
    protected void createTempTable(CmsSetupDb dbCon) throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());
        if (!dbCon.hasTableOrColumn(TEMPORARY_TABLE_NAME, null)) {
            String createStatement = readQuery(QUERY_CREATE_TEMP_TABLE_UUIDS);

            String indexTablespace = (String)m_poolData.get("indexTablespace");
            HashMap replacer = new HashMap();
            replacer.put(REPLACEMENT_TABLEINDEX_SPACE, indexTablespace);

            dbCon.updateSqlStatement(createStatement, replacer, null);
        } else {
            System.out.println("table " + TEMPORARY_TABLE_NAME + " already exists");
        }
    }

    /**
     * @see org.opencms.setup.update6to7.generic.CmsUpdateDBProjectId#getColumnProjectIdResourcePath()
     */
    protected String getColumnProjectIdResourcePath() {

        return COLUMN_PROJECT_ID_RESOURCE_PATH;
    }

    /**
     * @see org.opencms.setup.update6to7.generic.CmsUpdateDBProjectId#needsUpdating(org.opencms.setup.CmsSetupDb, java.lang.String)
     */
    protected boolean needsUpdating(CmsSetupDb dbCon, String tablename) throws SQLException {

        System.out.println(new Exception().getStackTrace()[0].toString());
        boolean result = true;

        String query = readQuery(QUERY_DESCRIBE_TABLE);
        Map replacer = new HashMap();
        replacer.put(REPLACEMENT_TABLENAME, tablename);
        ResultSet set = dbCon.executeSqlStatement(query, replacer);

        while (set.next()) {
            String fieldname = set.getString("COLUMN_NAME");
            if (fieldname.equals(COLUMN_PROJECT_ID) || fieldname.equals(COLUMN_PROJECT_LASTMODIFIED)) {
                try {
                    String fieldtype = set.getString("DATA_TYPE");
                    // If the type is varchar then no update needs to be done.
                    if (fieldtype.indexOf("VARCHAR") > -1) {
                        return false;
                    }
                } catch (SQLException e) {
                    result = true;
                }
            }
        }

        return result;
    }
}
