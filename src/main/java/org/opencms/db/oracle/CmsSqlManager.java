/*
 * File   : $Source: /usr/local/cvs/opencms/src/org/opencms/db/oracle/CmsSqlManager.java,v $
 * Date   : $Date: 2008-02-27 12:05:43 $
 * Version: $Revision: 1.26 $
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

package org.opencms.db.oracle;

import org.opencms.db.CmsDbContext;
import org.opencms.db.generic.Messages;
import org.opencms.main.CmsLog;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;

/**
 * Oracle implementation of the SQL manager.<p>
 * 
 * @author Thomas Weckert  
 * 
 * @version $Revision: 1.26 $
 * 
 * @since 6.0.0 
 */
public class CmsSqlManager extends org.opencms.db.generic.CmsSqlManager {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(org.opencms.db.oracle.CmsSqlManager.class);

    /** The filename/path of the SQL query properties. */
    private static final String QUERY_PROPERTIES = "org/opencms/db/oracle/query.properties";

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = -6823428215978645371L;

    /**
     * @see org.opencms.db.generic.CmsSqlManager#CmsSqlManager()
     */
    public CmsSqlManager() {

        super();
        loadQueryProperties(QUERY_PROPERTIES);
    }

    /**
     * Attempts to close the connection, statement and result set after a statement has been executed.<p>
     * 
     * @param sqlManager the sql manager to use
     * @param dbc the current database context
     * @param con the JDBC connection
     * @param stmnt the statement
     * @param res the result set
     * @param commit the additional statement for the 'commit' command
     * @param wasInTransaction if using transactions
     */
    public static synchronized void closeAllInTransaction(
        org.opencms.db.generic.CmsSqlManager sqlManager,
        CmsDbContext dbc,
        Connection con,
        PreparedStatement stmnt,
        ResultSet res,
        PreparedStatement commit,
        boolean wasInTransaction) {

        if (dbc == null) {
            LOG.error(Messages.get().getBundle().key(Messages.LOG_NULL_DB_CONTEXT_0));
        }

        if (res != null) {
            try {
                res.close();
            } catch (SQLException exc) {
                // ignore
                if (LOG.isDebugEnabled()) {
                    LOG.debug(exc.getLocalizedMessage(), exc);
                }
            }
        }
        if (commit != null) {
            try {
                commit.close();
            } catch (SQLException exc) {
                // ignore
                if (LOG.isDebugEnabled()) {
                    LOG.debug(exc.getLocalizedMessage(), exc);
                }
            }
        }
        if (!wasInTransaction) {
            if (stmnt != null) {
                try {
                    PreparedStatement rollback = sqlManager.getPreparedStatement(con, "C_ROLLBACK");
                    rollback.execute();
                    rollback.close();
                } catch (SQLException se) {
                    // ignore
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(se.getLocalizedMessage(), se);
                    }
                }
                try {
                    stmnt.close();
                } catch (SQLException exc) {
                    // ignore
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(exc.getLocalizedMessage(), exc);
                    }
                }
            }
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException se) {
                    // ignore
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(se.getLocalizedMessage(), se);
                    }
                }
            }
        }
    }

    /**
     * @see org.opencms.db.generic.CmsSqlManager#getBytes(java.sql.ResultSet, java.lang.String)
     */
    public byte[] getBytes(ResultSet res, String attributeName) throws SQLException {

        Blob blob = res.getBlob(attributeName);
        return blob.getBytes(1, (int)blob.length());
    }
}