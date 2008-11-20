/*
 * File   : $Source: /usr/local/cvs/opencms/src/org/opencms/importexport/I_CmsImport.java,v $
 * Date   : $Date: 2007-08-13 16:30:11 $
 * Version: $Revision: 1.17 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2007 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.importexport;

import org.opencms.file.CmsObject;
import org.opencms.report.I_CmsReport;

import java.io.File;
import java.util.zip.ZipFile;

import org.dom4j.Document;

/**
 * This interface describes a import class which is used to import resources into the VFS.<p>
 * 
 * OpenCms supports different import versions, for each version a own import class must be implemented.
 * A group of common used methods can be found in <code>{@link org.opencms.importexport.A_CmsImport}</code>.<p>
 *
 * @author Michael Emmerich 
 * @author Thomas Weckert  
 * 
 * @version $Revision: 1.17 $ 
 * 
 * @since 6.0.0 
 */
public interface I_CmsImport {

    /**
     * Returns the version of the import implementation.<p>
     *  
     * <ul>
     * <li>0 indicates an export file without a version number, that is before version 4.3.23 of OpenCms</li>
     * <li>1 indicates an export file of OpenCms with a version before 5.0.0</li>
     * <li>2 indicates an export file of OpenCms with a version before 5.1.2</li>
     * <li>3 indicates an export file of OpenCms with a version before 5.1.6</li>
     * <li>4 indicates an export file of OpenCms with a version after 5.1.6</li>
     * </ul>
     * 
     * @return the version of the import implementation
     */
    int getVersion();

    /**
     * Imports the resources.<p>
     * 
     * @param cms the current users OpenCms context
     * @param importPath the path in the OpenCms VFS to import into
     * @param report a report object to output the progress information to
     * @param importResource the import-resource (folder) to load resources from
     * @param importZip the import-resource (zip) to load resources from
     * @param docXml the <code>manifest.xml</code> file which contains the meta information of the imported files
     * 
     * @throws CmsImportExportException if something goes wrong
     */
    void importResources(
        CmsObject cms,
        String importPath,
        I_CmsReport report,
        File importResource,
        ZipFile importZip,
        Document docXml) throws CmsImportExportException;
}