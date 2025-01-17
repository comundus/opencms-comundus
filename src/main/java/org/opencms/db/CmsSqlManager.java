/*
 * File   : $Source: /usr/local/cvs/opencms/src/org/opencms/db/CmsSqlManager.java,v $
 * Date   : $Date: 2008-02-27 12:05:43 $
 * Version: $Revision: 1.13 $
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

package org.opencms.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

/**
 * Superclass for all SQL manager implementations.<p>
 * 
 * @author Carsten Weinholz 
 * 
 * @version $Revision: 1.13 $
 * 
 * @since 6.0.0
 */
public class CmsSqlManager {

    /** the driver manager. */
    private CmsDriverManager m_driverManager;

    /**
     * Protected constructor to allow only subclassing.<p> 
     */
    protected CmsSqlManager() {

        // hides the public constructor
    }

    /**
     * Creates a new SQL manager from the provided driver manager.<p> 
     * 
     * @param driverManager the low level database driver manager 
     */
    protected CmsSqlManager(CmsDriverManager driverManager) {

        m_driverManager = driverManager;
    }

    /** 
     * Returns the number of active connections managed by a pool.<p> 
     * 
     * @param dbPoolUrl the url of a pool 
     * @return the number of active connections 
     * @throws CmsDbException if something goes wrong 
     */
    public int getActiveConnections(String dbPoolUrl) throws CmsDbException {

        return m_driverManager.getActiveConnections(dbPoolUrl);
    }

    /**
     * Returns a connection to the database using the given pool identified by its name.<p>
     * 
     * @param dbPoolName the pool name
     * @return a database connection
     * @throws SQLException if something goes wrong
     */
    public Connection getConnection(String dbPoolName) throws SQLException {

        return getConnectionByUrl(CmsDbPool.DBCP_JDBC_URL_PREFIX + CmsDbPool.OPENCMS_URL_PREFIX + dbPoolName);
    }

    /**
     * Returns a connection to the database using the given pool identified by its full url.<p>
     * 
     * @param dbPoolUrl the pool url
     * @return a database connection
     * @throws SQLException if something goes wrong
     */
    public synchronized Connection getConnectionByUrl(String dbPoolUrl) throws SQLException {

        return DriverManager.getConnection(dbPoolUrl);
    }

    /**
     * Returns a list of available database connection pool names.<p>
     * 
     * @return a list of database connection pool names
     */
    public List getDbPoolUrls() {

        return CmsDbPool.getDbPoolUrls(m_driverManager.getPropertyConfiguration());
    }

    /**
     * Returns the name of the default database connection pool.<p>
     * 
     * @return the name of the default database connection pool
     */
    public String getDefaultDbPoolName() {

        return CmsDbPool.getDefaultDbPoolName();
    }

    /** 
     * Returns the number of idle connections managed by a pool.<p> 
     * 
     * @param dbPoolUrl the url of a pool 
     * @return the number of idle connections 
     * @throws CmsDbException if something goes wrong 
     */
    public int getIdleConnections(String dbPoolUrl) throws CmsDbException {

        return m_driverManager.getIdleConnections(dbPoolUrl);
    }
}