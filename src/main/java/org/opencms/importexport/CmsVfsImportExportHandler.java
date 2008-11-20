/*
 * File   : $Source: /usr/local/cvs/opencms/src/org/opencms/importexport/CmsVfsImportExportHandler.java,v $
 * Date   : $Date: 2007-08-13 16:30:11 $
 * Version: $Revision: 1.24 $
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
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.module.CmsModuleXmlHandler;
import org.opencms.report.I_CmsReport;
import org.opencms.security.CmsRoleViolationException;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.CmsXmlException;

import java.util.Collections;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;

/**
 * Import/export handler implementation for VFS data.<p>
 * 
 * @author Thomas Weckert  
 * 
 * @version $Revision: 1.24 $ 
 * 
 * @since 6.0.0 
 */
public class CmsVfsImportExportHandler implements I_CmsImportExportHandler {

    /** Timestamp to limit the resources to be exported by date.<p> */
    private long m_contentAge;

    /** The description of this import/export handler.<p> */
    private String m_description;

    /** The VFS paths to be exported.<p> */
    private List m_exportPaths;

    /** Boolean flag to decide whether user/group data should be exported or not.<p> */
    private boolean m_exportUserdata;

    /** The name of the export file in the real file system.<p> */
    private String m_fileName;

    /** Boolean flag to decide whether VFS resources under /system/ should be exported or not.<p> */
    private boolean m_includeSystem;

    /** Boolean flag to decide whether unchanged resources should be exported or not.<p> */
    private boolean m_includeUnchanged;

    /** Boolean flag to indicate that only the resources of the current project should be exported. */
    private boolean m_projectOnly;

    /** Boolean flag to indicate if the folders are exported recursively or not. */
    private boolean m_recursive;

    /**
     * Creates a new VFS import/export handler.<p>
     */
    public CmsVfsImportExportHandler() {

        super();
        m_description = Messages.get().getBundle().key(Messages.GUI_CMSIMPORTHANDLER_DEFAULT_DESC_0);
        m_includeSystem = false;
        m_includeUnchanged = true;
        m_exportUserdata = true;
        m_exportPaths = Collections.EMPTY_LIST;
        m_recursive = true;
    }

    /**
     * @see org.opencms.importexport.I_CmsImportExportHandler#exportData(org.opencms.file.CmsObject, org.opencms.report.I_CmsReport)
     */
    public void exportData(CmsObject cms, I_CmsReport report)
    throws CmsImportExportException, CmsRoleViolationException {

        report.println(Messages.get().container(Messages.RPT_EXPORT_DB_BEGIN_0), I_CmsReport.FORMAT_HEADLINE);
        new CmsExport(
            cms,
            getFileName(),
            getExportPaths(),
            isIncludeSystem(),
            isIncludeUnchanged(),
            null,
            isExportUserdata(),
            getContentAge(),
            report,
            isRecursive(),
            isProjectOnly());
        report.println(Messages.get().container(Messages.RPT_EXPORT_DB_END_0), I_CmsReport.FORMAT_HEADLINE);
    }

    /**
     * Returns the timestamp to limit the resources to be exported by date.<p>
     * 
     * Only resources that have been modified after this date will be exported.<p>
     * 
     * @return the timestamp to limit the resources to be exported by date
     */
    public long getContentAge() {

        return m_contentAge;
    }

    /**
     * @see org.opencms.importexport.I_CmsImportExportHandler#getDescription()
     */
    public String getDescription() {

        return m_description;
    }

    /**
     * Returns the list with VFS paths to be exported.<p>
     * 
     * @return the list with VFS paths to be exported
     */
    public List getExportPaths() {

        return m_exportPaths;
    }

    /**
     * Returns the name of the export file in the real file system.<p>
     * 
     * @return the name of the export file in the real file system
     */
    public String getFileName() {

        return m_fileName;
    }

    /**
     * @see org.opencms.importexport.I_CmsImportExportHandler#importData(org.opencms.file.CmsObject, java.lang.String, java.lang.String, org.opencms.report.I_CmsReport)
     */
    public synchronized void importData(CmsObject cms, String importFile, String importPath, I_CmsReport report)
    throws CmsImportExportException, CmsXmlException, CmsRoleViolationException {

        report.println(Messages.get().container(Messages.RPT_IMPORT_DB_BEGIN_0), I_CmsReport.FORMAT_HEADLINE);
        CmsImport vfsImport = new CmsImport(cms, importFile, importPath, report);
        vfsImport.importResources();
        report.println(Messages.get().container(Messages.RPT_IMPORT_DB_END_0), I_CmsReport.FORMAT_HEADLINE);
    }

    /**
     * Returns the boolean flag to decide whether user/group data should be exported or not.<p>
     * 
     * @return true, if user/group data should be exported
     */
    public boolean isExportUserdata() {

        return m_exportUserdata;
    }

    /**
     * Returns the boolean flag to decide whether VFS resources under /system/ should be exported or not.<p>
     * 
     * @return true, if VFS resources under /system/ should not be exported
     */
    public boolean isIncludeSystem() {

        return m_includeSystem;
    }

    /**
     * Returns the boolean flag to decide whether unchanged resources should be exported or not.<p>
     * 
     * @return true, if unchanged resources should not be exported
     */
    public boolean isIncludeUnchanged() {

        return m_includeUnchanged;
    }

    /**
     * Returns the projectOnly.<p>
     *
     * @return the projectOnly
     */
    public boolean isProjectOnly() {

        return m_projectOnly;
    }

    /**
     * Returns the recursive flag.<p>
     *
     * @return the recursive flag
     */
    public boolean isRecursive() {

        return m_recursive;
    }

    /**
     * @see org.opencms.importexport.I_CmsImportExportHandler#matches(org.dom4j.Document)
     */
    public boolean matches(Document manifest) {

        Element rootElement = manifest.getRootElement();

        boolean hasModuleNode = (rootElement.selectNodes(
            "./" + CmsModuleXmlHandler.N_MODULE + "/" + CmsModuleXmlHandler.N_NAME).size() > 0);
        boolean hasFileNodes = (rootElement.selectNodes("./" + "files").size() == 1);
        boolean hasUserData = (rootElement.selectNodes("./" + CmsImportExportManager.N_USERGROUPDATA).size() == 1);

        return (!hasModuleNode && (hasFileNodes || hasUserData));
    }

    /**
     * Sets the timestamp to limit the resources to be exported by date.<p>
     * 
     * Only resources that have been modified after this date will be exported.<p>
     * 
     * @param contentAge the timestamp to limit the resources to be exported by date
     */
    public void setContentAge(long contentAge) {

        if (contentAge < 0) {
            String ageString = Long.toString(contentAge);
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_BAD_CONTENT_AGE_1, ageString));
        }
        m_contentAge = contentAge;
    }

    /**
     * @see org.opencms.importexport.I_CmsImportExportHandler#setDescription(java.lang.String)
     */
    public void setDescription(String description) {

        m_description = description;
    }

    /**
     * Sets the list with VFS paths to be exported.<p>
     * 
     * @param exportPaths the list with VFS paths to be exported
     */
    public void setExportPaths(List exportPaths) {

        m_exportPaths = exportPaths;
    }

    /**
     * Sets the boolean flag to decide whether user/group data should be exported or not.<p>
     * 
     * @param exportUserdata true, if user/group data should not be exported
     */
    public void setExportUserdata(boolean exportUserdata) {

        m_exportUserdata = exportUserdata;
    }

    /**
     * Sets the name of the export file in the real file system.<p>
     * 
     * @param fileName the name of the export file in the real file system
     */
    public void setFileName(String fileName) {

        if (CmsStringUtil.isEmpty(fileName) || !fileName.trim().equals(fileName)) {
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_BAD_FILE_NAME_1, fileName));
        }
        m_fileName = fileName;
    }

    /**
     * Sets the boolean flag to decide whether VFS resources under /system/ should be exported or not.<p>
     * 
     * @param excludeSystem true, if VFS resources under /system/ should not be exported
     */
    public void setIncludeSystem(boolean excludeSystem) {

        m_includeSystem = excludeSystem;
    }

    /**
     * Sets the boolean flag to decide whether unchanged resources should be exported or not.<p>
     * 
     * @param excludeUnchanged true, if unchanged resources should not be exported
     */
    public void setIncludeUnchanged(boolean excludeUnchanged) {

        m_includeUnchanged = excludeUnchanged;
    }

    /**
     * Sets the projectOnly.<p>
     *
     * @param projectOnly the projectOnly to set
     */
    public void setProjectOnly(boolean projectOnly) {

        m_projectOnly = projectOnly;
    }

    /**
     * Sets the recursive flag.<p>
     *
     * @param recursive the recursive flag to set
     */
    public void setRecursive(boolean recursive) {

        m_recursive = recursive;
    }

    /**
     * @see java.lang.Object#finalize()
     */
    protected void finalize() throws Throwable {

        try {
            if (m_exportPaths != null) {
                m_exportPaths.clear();
            }
        } catch (Throwable t) {
            // noop
        }
        super.finalize();
    }
}
