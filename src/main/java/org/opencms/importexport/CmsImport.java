/*
 * File   : $Source: /usr/local/cvs/opencms/src/org/opencms/importexport/CmsImport.java,v $
 * Date   : $Date: 2007-08-13 16:30:11 $
 * Version: $Revision: 1.45 $
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
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.report.I_CmsReport;
import org.opencms.security.CmsRole;
import org.opencms.security.CmsRoleViolationException;
import org.opencms.util.CmsFileUtil;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.CmsXmlUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.logging.Log;

import org.dom4j.Document;
import org.dom4j.Element;

/**
 * Holds the functionaility to import resources from the filesystem
 * or a zip file into the OpenCms VFS.<p>
 *
 * @author Andreas Zahner 
 * @author Alexander Kandzior 
 * @author Michael Emmerich 
 * @author Thomas Weckert  
 * 
 * @version $Revision: 1.45 $ 
 * 
 * @since 6.0.0 
 */
public class CmsImport {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsImport.class);

    /** The cms context to do the operations with. */
    protected CmsObject m_cms;

    /** The xml manifest-file. */
    protected Document m_docXml;

    /** The import-file to load resources from. */
    protected String m_importFile;

    /** Stores all import interface implementations .*/
    protected List m_importImplementations;

    /** The import-path to write resources into the cms. */
    protected String m_importPath;

    /** The import-resource (folder) to load resources from. */
    protected File m_importResource;

    /**
     * The version of this import, noted in the info tag of the manifest.xml.<p>
     * 
     * 0 indicates an export file without a version number, that is before version 4.3.23 of OpenCms.<br>
     * 1 indicates an export file of OpenCms with a version before 5.0.0
     * 2 indicates an export file of OpenCms with a version before 5.1.2
     * 3 indicates an export file of OpenCms with a version before 5.1.6
     * 4 indicates an export file of OpenCms with a version after 5.1.6
     */
    protected int m_importVersion;

    /**  The import-resource (zip) to load resources from. */
    protected ZipFile m_importZip;

    /** The object to report the log messages. */
    protected I_CmsReport m_report;

    /**
     * Constructs a new uninitialized import, required for special subclass data import.<p>
     */
    public CmsImport() {

        // empty
        super();
    }

    /**
     * Constructs a new import object which imports the resources from an OpenCms 
     * export zip file or a folder in the "real" file system.<p>
     *
     * @param cms the current cms object
     * @param importFile the file or folder to import from
     * @param importPath the path in the cms VFS to import into
     * @param report a report object to output the progress information to
     * 
     * @throws CmsRoleViolationException if the current user dies not have role permissions to import the database
     */
    public CmsImport(CmsObject cms, String importFile, String importPath, I_CmsReport report)
    throws CmsRoleViolationException {

        // check the role permissions
        OpenCms.getRoleManager().checkRole(cms, CmsRole.DATABASE_MANAGER);

        // set member variables
        m_cms = cms;
        m_importFile = importFile;
        m_importPath = importPath;
        m_report = report;
        m_importImplementations = OpenCms.getImportExportManager().getImportVersionClasses();
    }

    /**
     * Returns the value of a child element with a specified name for a given parent element.<p>
     *
     * @param parentElement the parent element
     * @param elementName the child element name
     * @return the value of the child node, or null if something went wrong
     */
    public static String getChildElementTextValue(Element parentElement, String elementName) {

        try {
            // get the first child element matching the specified name
            Element childElement = (Element)parentElement.selectNodes("./" + elementName).get(0);
            // return the value of the child element
            return childElement.getTextTrim();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns a list of files which are both in the import and in the virtual file system.<p>
     * 
     * @return a list of Strings, complete path of the files
     * @throws CmsImportExportException if the import file could not be opened
     * @throws CmsXmlException if the manifest of the import could not be unmarshalled
     */
    public List getConflictingFilenames() throws CmsXmlException, CmsImportExportException {

        List fileNodes;
        Element currentElement;
        String source, destination;
        List conflictNames = new ArrayList();
        //String xpathExpr = null;

        if (m_docXml == null) {
            openImportFile();
        }

        // get all file-nodes
        fileNodes = m_docXml.selectNodes("//" + CmsImportExportManager.N_FILE);

        // walk through all files in manifest
        for (int i = 0; i < fileNodes.size(); i++) {
            currentElement = (Element)fileNodes.get(i);
            source = CmsImport.getChildElementTextValue(currentElement, CmsImportExportManager.N_SOURCE);
            destination = CmsImport.getChildElementTextValue(currentElement, CmsImportExportManager.N_DESTINATION);
            if (source != null) {
                // only consider files
                boolean exists = true;
                try {
                    CmsResource res = m_cms.readResource(m_importPath + destination);
                    if (res.getState().isDeleted()) {
                        exists = false;
                    }
                } catch (CmsException e) {
                    exists = false;
                }
                if (exists) {
                    conflictNames.add(m_importPath + destination);
                }
            }
        }

        if (m_importZip != null) {
            try {
                m_importZip.close();
            } catch (IOException e) {

                CmsMessageContainer message = Messages.get().container(
                    Messages.ERR_IMPORTEXPORT_ERROR_CLOSING_ZIP_ARCHIVE_1,
                    m_importZip.getName());
                if (LOG.isDebugEnabled()) {
                    LOG.debug(message.key(), e);
                }

                throw new CmsImportExportException(message, e);
            }
        }
        return conflictNames;
    }

    /**
     * Returns a list of resource names that are needed to create a project for this import.<p>
     * 
     * It calls the method getConflictingFileNames if needed, to calculate these resources.
     * 
     * @return a list of resource names that are needed to create a project for this import
     * @throws CmsImportExportException if ZIP archive could not be closed
     */
    public List getResourcesForProject() throws CmsImportExportException {

        List resources = new ArrayList();

        // get all file-nodes
        List fileNodes = m_docXml.selectNodes("//" + CmsImportExportManager.N_FILE);

        // walk through all files in manifest
        for (int i = 0; i < fileNodes.size(); i++) {
            Element currentElement = (Element)fileNodes.get(i);
            String destination = CmsImport.getChildElementTextValue(
                currentElement,
                CmsImportExportManager.N_DESTINATION);

            // get the resources for a project
            try {
                String resource = destination.substring(0, destination.indexOf("/", 1) + 1);
                resource = m_importPath + resource;
                // add the resource, if it dosen't already exist
                if ((!resources.contains(resource)) && (!resource.equals(m_importPath))) {
                    try {
                        m_cms.readFolder(resource, CmsResourceFilter.IGNORE_EXPIRATION);
                        // this resource exists in the current project -> add it
                        resources.add(resource);
                    } catch (CmsException exc) {
                        // this resource is missing - we need the root-folder
                        resources.add("/");
                    }
                }
            } catch (StringIndexOutOfBoundsException exc) {
                // this is a resource in root-folder: ignore the excpetion
            }
        }
        closeImportFile();

        if (resources.contains("/")) {
            // we have to import root - forget the rest!
            resources.clear();
            resources.add("/");
        }

        return resources;
    }

    /**
     * Imports the resources and writes them to the cms VFS, even if there 
     * already exist files with the same name.<p>
     * 
     * @throws CmsImportExportException if something goes wrong
     * @throws CmsXmlException if the manifest of the import file could not be unmarshalled
     */
    public void importResources() throws CmsImportExportException, CmsXmlException {

        // initialize the import
        boolean run = false;
        openImportFile();
        m_report.println(
            Messages.get().container(Messages.RPT_IMPORT_VERSION_1, String.valueOf(m_importVersion)),
            I_CmsReport.FORMAT_NOTE);
        try {
            // now find the correct import implementation         
            Iterator i = m_importImplementations.iterator();
            while (i.hasNext()) {
                I_CmsImport imp = (I_CmsImport)i.next();
                if (imp.getVersion() == m_importVersion) {
                    // this is the correct import version, so call it for the import process
                    imp.importResources(m_cms, m_importPath, m_report, m_importResource, m_importZip, m_docXml);
                    run = true;
                    break;
                }
            }
            if (!run) {
                m_report.println(
                    Messages.get().container(Messages.RPT_IMPORT_DB_NO_CLASS_0),
                    I_CmsReport.FORMAT_WARNING);
            }
        } finally {
            // close the import file
            closeImportFile();
            OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_CLEAR_OFFLINE_CACHES, Collections.EMPTY_MAP));
        }
    }

    /**
     * Closes the import file.<p>
     * 
     * @throws CmsImportExportException if the ZIP archive could not be closed
     */
    protected void closeImportFile() throws CmsImportExportException {

        if (m_importZip != null) {
            try {
                m_importZip.close();
            } catch (IOException e) {
                m_report.println(e);

                CmsMessageContainer message = Messages.get().container(
                    Messages.ERR_IMPORTEXPORT_ERROR_CLOSING_ZIP_ARCHIVE_1,
                    m_importZip.getName());
                if (LOG.isDebugEnabled()) {
                    LOG.debug(message.key(), e);
                }

                throw new CmsImportExportException(message, e);
            }
        }
    }

    /**
     * Returns a byte array containing the content of the file.<p>
     *
     * @param filename the name of the file to read
     * @return a byte array containing the content of the file
     */
    protected byte[] getFileBytes(String filename) {

        try {
            // is this a zip-file?
            if (m_importZip != null) {
                // yes
                ZipEntry entry = m_importZip.getEntry(filename);
                InputStream stream = m_importZip.getInputStream(entry);
                int size = new Long(entry.getSize()).intValue();
                return CmsFileUtil.readFully(stream, size);
            } else {
                // no - use directory
                File file = new File(m_importResource, filename);
                return CmsFileUtil.readFile(file);
            }
        } catch (FileNotFoundException fnfe) {
            m_report.println(fnfe);
        } catch (IOException ioe) {
            m_report.println(ioe);
        }
        // this will only be returned in case there was an exception
        return "".getBytes();
    }

    /**
     * Gets the import resource and stores it in object-member.<p>
     * 
     * @throws CmsImportExportException if the import file could not be opened
     */
    protected void getImportResource() throws CmsImportExportException {

        try {
            // get the import resource
            m_importResource = new File(OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(m_importFile));

            // if it is a file it must be a zip-file
            if (m_importResource.isFile()) {
                m_importZip = new ZipFile(m_importResource);
            }
        } catch (IOException e) {
            m_report.println(e);

            CmsMessageContainer message = Messages.get().container(
                Messages.ERR_IMPORTEXPORT_ERROR_OPENING_ZIP_ARCHIVE_1,
                m_importFile);
            if (LOG.isDebugEnabled()) {
                LOG.debug(message.key(), e);
            }

            throw new CmsImportExportException(message, e);
        }
    }

    /**
     * Initalizes the import.<p>
     * 
     * @throws CmsImportExportException if the import file could not be opened
     * @throws CmsXmlException if the manifest of the import could not be unmarshalled
     */
    protected void openImportFile() throws CmsXmlException, CmsImportExportException {

        // open the import resource
        getImportResource();

        // read the xml-config file
        m_docXml = CmsXmlUtils.unmarshalHelper(getFileBytes(CmsImportExportManager.EXPORT_MANIFEST), null);

        // try to read the export version number
        try {
            m_importVersion = Integer.parseInt(((Element)m_docXml.selectNodes("//" + CmsImportExportManager.N_VERSION).get(
                0)).getTextTrim());
        } catch (Exception e) {
            // ignore the exception, the export file has no version nummber (version 0)
            // should never happen
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
    }
}