/*
 * File   : $Source: /usr/local/cvs/opencms/src/org/opencms/loader/CmsResourceManager.java,v $
 * Date   : $Date: 2008-02-27 12:05:32 $
 * Version: $Revision: 1.47 $
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

package org.opencms.loader;

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.configuration.CmsVfsConfiguration;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.collectors.I_CmsResourceCollector;
import org.opencms.file.types.CmsResourceTypeBinary;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.file.types.CmsResourceTypeUnknownFile;
import org.opencms.file.types.CmsResourceTypeUnknownFolder;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.module.CmsModuleManager;
import org.opencms.relations.CmsRelationType;
import org.opencms.security.CmsRole;
import org.opencms.security.CmsRoleViolationException;
import org.opencms.util.CmsResourceTranslator;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

/**
 * Collects all available resource loaders, resource types and resource collectors at startup and provides
 * methods to access them during OpenCms runtime.<p> 
 *
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.47 $ 
 * 
 * @since 6.0.0 
 */
public class CmsResourceManager {

    /**
     * Contains the part of the resource manager configuration that can be changed 
     * during runtime by the import / deletion of a module.<p>
     * 
     * A module can add resource types and extension mappings to resource types.<p>
     */
    static final class CmsResourceManagerConfiguration {

        /** The mappings of file extensions to resource types. */
        protected Map m_extensionMappings;

        /** A list that contains all initialized resource types. */
        protected List m_resourceTypeList;

        /** A list that contains all initialized resource types, plus configured types for "unknown" resources. */
        protected List m_resourceTypeListWithUnknown;

        /** A map that contains all initialized resource types mapped to their type id. */
        private Map m_resourceTypeIdMap;

        /** A map that contains all initialized resource types mapped to their type name. */
        private Map m_resourceTypeNameMap;

        /**
         * Creates a new resource manager data storage.<p>
         */
        protected CmsResourceManagerConfiguration() {

            m_resourceTypeIdMap = new HashMap(128);
            m_resourceTypeNameMap = new HashMap(128);
            m_extensionMappings = new HashMap(128);
            m_resourceTypeList = new ArrayList(32);
        }

        /**
         * Adds a resource type to the list of configured resource types.<p>
         * 
         * @param type the resource type to add
         */
        protected void addResourceType(I_CmsResourceType type) {

            m_resourceTypeIdMap.put(new Integer(type.getTypeId()), type);
            m_resourceTypeNameMap.put(type.getTypeName(), type);
            m_resourceTypeList.add(type);
        }

        /**
         * Freezes the current configuration by making all data structures unmodifiable
         * that can be accessed form outside this class.<p> 
         * 
         * @param restypeUnknownFolder the configured default resource type for unknown folders
         * @param restypeUnknownFile the configured default resource type for unknown files
         */
        protected void freeze(I_CmsResourceType restypeUnknownFolder, I_CmsResourceType restypeUnknownFile) {

            // generate the resource type list with unknown resource types
            m_resourceTypeListWithUnknown = new ArrayList(m_resourceTypeList.size() + 2);
            if (restypeUnknownFolder != null) {
                m_resourceTypeListWithUnknown.add(restypeUnknownFolder);
            }
            if (restypeUnknownFile != null) {
                m_resourceTypeListWithUnknown.add(restypeUnknownFile);
            }
            m_resourceTypeListWithUnknown.addAll(m_resourceTypeList);

            // freeze the current configuration
            m_resourceTypeListWithUnknown = Collections.unmodifiableList(m_resourceTypeListWithUnknown);
            m_resourceTypeList = Collections.unmodifiableList(m_resourceTypeList);
            m_extensionMappings = Collections.unmodifiableMap(m_extensionMappings);
        }

        /**
         * Returns the configured resource type with the matching type id, or <code>null</code>
         * if a resource type with that id is not configured.<p> 
         * 
         * @param typeId the type id to get the resource type for
         * 
         * @return the configured resource type with the matching type id, or <code>null</code>
         */
        protected I_CmsResourceType getResourceTypeById(int typeId) {

            return (I_CmsResourceType)m_resourceTypeIdMap.get(new Integer(typeId));
        }

        /**
         * Returns the configured resource type with the matching type name, or <code>null</code>
         * if a resource type with that name is not configured.<p> 
         * 
         * @param typeName the type name to get the resource type for
         * 
         * @return the configured resource type with the matching type name, or <code>null</code>
         */
        protected I_CmsResourceType getResourceTypeByName(String typeName) {

            return (I_CmsResourceType)m_resourceTypeNameMap.get(typeName);
        }
    }

    /** The path to the default template. */
    public static final String DEFAULT_TEMPLATE = CmsWorkplace.VFS_PATH_COMMONS + "template/default.jsp";

    /** The MIME type <code>"text/html"</code>. */
    public static final String MIMETYPE_HTML = "text/html";

    /** The MIME type <code>"text/plain"</code>. */
    public static final String MIMETYPE_TEXT = "text/plain";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsResourceManager.class);

    /** The map for all configured collector names, mapped to their collector class. */
    private Map m_collectorNameMappings;

    /** The list of all currently configured content collector instances. */
    private List m_collectors;

    /** The current resource manager configuration. */
    private CmsResourceManagerConfiguration m_configuration;

    /** The list of all configured MIME types. */
    private List m_configuredMimeTypes;

    /** The list of all configured relation types. */
    private List m_configuredRelationTypes;

    /** Filename translator, used only for the creation of new files. */
    private CmsResourceTranslator m_fileTranslator;

    /** Folder translator, used to translate all accesses to resources. */
    private CmsResourceTranslator m_folderTranslator;

    /** Indicates if the configuration is finalized (frozen). */
    private boolean m_frozen;

    /** Contains all loader extensions to the include process. */
    private List m_includeExtensions;

    /** A list that contains all initialized resource loaders. */
    private List m_loaderList;

    /** All initialized resource loaders, mapped to their id. */
    private I_CmsResourceLoader[] m_loaders;

    /** The OpenCms map of configured MIME types. */
    private Map m_mimeTypes;

    /** A list that contains all resource types added from the XML configuration. */
    private List m_resourceTypesFromXml;

    /** The configured default type for files when the resource type is missing. */
    private I_CmsResourceType m_restypeUnknownFile;

    /** The configured default type for folders when the resource type is missing. */
    private I_CmsResourceType m_restypeUnknownFolder;

    /**
     * Creates a new instance for the resource manager, 
     * will be called by the VFS configuration manager.<p>
     */
    public CmsResourceManager() {

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_STARTING_LOADER_CONFIG_0));
        }

        m_resourceTypesFromXml = new ArrayList();
        m_loaders = new I_CmsResourceLoader[16];
        m_loaderList = new ArrayList();
        m_includeExtensions = new ArrayList();
        m_configuredMimeTypes = new ArrayList();
        m_configuredRelationTypes = new ArrayList();
    }

    /**
     * Adds a given content collector class to the type manager.<p> 
     * 
     * @param className the name of the class to add
     * @param order the order number for this collector
     * 
     * @return the created content collector instance
     * 
     * @throws CmsConfigurationException in case the collector could not be properly initialized
     */
    public synchronized I_CmsResourceCollector addContentCollector(String className, String order)
    throws CmsConfigurationException {

        Class classClazz;
        // init class for content collector
        try {
            classClazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            LOG.error(Messages.get().getBundle().key(Messages.LOG_CONTENT_COLLECTOR_CLASS_NOT_FOUND_1, className), e);
            return null;
        }

        I_CmsResourceCollector collector;
        try {
            collector = (I_CmsResourceCollector)classClazz.newInstance();
        } catch (InstantiationException e) {
            throw new CmsConfigurationException(Messages.get().container(
                Messages.ERR_INVALID_COLLECTOR_NAME_1,
                className));
        } catch (IllegalAccessException e) {
            throw new CmsConfigurationException(Messages.get().container(
                Messages.ERR_INVALID_COLLECTOR_NAME_1,
                className));
        } catch (ClassCastException e) {
            throw new CmsConfigurationException(Messages.get().container(
                Messages.ERR_INVALID_COLLECTOR_NAME_1,
                className));
        }

        // set the configured order for the collector
        int ord = 0;
        try {
            ord = Integer.valueOf(order).intValue();
        } catch (NumberFormatException e) {
            LOG.error(Messages.get().getBundle().key(Messages.LOG_COLLECTOR_BAD_ORDER_NUMBER_1, className), e);
        }
        collector.setOrder(ord);

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_ADD_COLLECTOR_CLASS_2, className, order));
        }

        // extend or init the current list of configured collectors
        if (m_collectors != null) {
            m_collectors = new ArrayList(m_collectors);
            m_collectorNameMappings = new HashMap(m_collectorNameMappings);
        } else {
            m_collectors = new ArrayList();
            m_collectorNameMappings = new HashMap();
        }

        if (!m_collectors.contains(collector)) {
            // this is a collector not currently configured
            m_collectors.add(collector);

            Iterator i = collector.getCollectorNames().iterator();
            while (i.hasNext()) {
                String name = (String)i.next();
                if (m_collectorNameMappings.containsKey(name)) {
                    // this name is already configured, check the order of the collector
                    I_CmsResourceCollector otherCollector = (I_CmsResourceCollector)m_collectorNameMappings.get(name);
                    if (collector.getOrder() > otherCollector.getOrder()) {
                        // new collector has a greater order than the old collector in the Map
                        m_collectorNameMappings.put(name, collector);
                        if (CmsLog.INIT.isInfoEnabled()) {
                            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_COLLECTOR_REPLACED_1, name));
                        }
                    } else {
                        if (CmsLog.INIT.isInfoEnabled()) {
                            CmsLog.INIT.info(Messages.get().getBundle().key(
                                Messages.INIT_DUPLICATE_COLLECTOR_SKIPPED_1,
                                name));
                        }
                    }
                } else {
                    m_collectorNameMappings.put(name, collector);
                    if (CmsLog.INIT.isInfoEnabled()) {
                        CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_ADD_COLLECTOR_1, name));
                    }
                }
            }
        }

        // ensure list is unmodifiable to avoid potential misuse or accidental changes
        Collections.sort(m_collectors);
        m_collectors = Collections.unmodifiableList(m_collectors);
        m_collectorNameMappings = Collections.unmodifiableMap(m_collectorNameMappings);

        // return the created collector instance
        return collector;
    }

    /**
     * Adds a new loader to the internal list of loaded loaders.<p>
     *
     * @param loader the loader to add
     * @throws CmsConfigurationException in case the resource manager configuration is already initialized
     */
    public void addLoader(I_CmsResourceLoader loader) throws CmsConfigurationException {

        // check if new loaders can still be added
        if (m_frozen) {
            throw new CmsConfigurationException(Messages.get().container(Messages.ERR_NO_CONFIG_AFTER_STARTUP_0));
        }

        // add the loader to the internal list of loaders
        int pos = loader.getLoaderId();
        if (pos >= m_loaders.length) {
            I_CmsResourceLoader[] buffer = new I_CmsResourceLoader[pos * 2];
            System.arraycopy(m_loaders, 0, buffer, 0, m_loaders.length);
            m_loaders = buffer;
        }
        m_loaders[pos] = loader;
        if (loader instanceof I_CmsLoaderIncludeExtension) {
            // this loader requires special processing during the include process
            m_includeExtensions.add(loader);
        }
        m_loaderList.add(loader);
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(
                Messages.INIT_ADD_LOADER_2,
                loader.getClass().getName(),
                new Integer(pos)));
        }
    }

    /**
     * Adds a new MIME type from the XML configuration to the internal list of MIME types.<p> 
     * 
     * @param extension the MIME type extension
     * @param type the MIME type description
     * 
     * @return the created MIME type instance
     * 
     * @throws CmsConfigurationException in case the resource manager configuration is already initialized
     */
    public CmsMimeType addMimeType(String extension, String type) throws CmsConfigurationException {

        // check if new mime types can still be added
        if (m_frozen) {
            throw new CmsConfigurationException(Messages.get().container(Messages.ERR_NO_CONFIG_AFTER_STARTUP_0));
        }

        CmsMimeType mimeType = new CmsMimeType(extension, type);
        m_configuredMimeTypes.add(mimeType);
        return mimeType;
    }

    /**
     * Adds a new relation type from the XML configuration to the list of user defined relation types.<p> 
     * 
     * @param name the name of the relation type
     * @param type the type of the relation type, weak or strong
     * 
     * @return the new created relation type instance
     * 
     * @throws CmsConfigurationException in case the resource manager configuration is already initialized
     */
    public CmsRelationType addRelationType(String name, String type) throws CmsConfigurationException {

        // check if new relation types can still be added
        if (m_frozen) {
            throw new CmsConfigurationException(Messages.get().container(Messages.ERR_NO_CONFIG_AFTER_STARTUP_0));
        }

        CmsRelationType relationType = new CmsRelationType(m_configuredRelationTypes.size(), name, type);
        m_configuredRelationTypes.add(relationType);
        return relationType;
    }

    /** ADDED BY COMUNDUS - OpenCms Sprint : change integrated into 7.0.5 code
     * Adds a new relation type from the XML configuration to the list of user defined relation types.
     * This signature adds the "defined in content" parameter<p> 
     * 
     * @param name the name of the relation type
     * @param type the type of the relation type, weak or strong
     * @param defincontent "true" or "false" wether the link relation is defined by a files content or not
     * 
     * @return the new created relation type instance
     * 
     * @throws CmsConfigurationException in case the resource manager configuration is already initialized
     */
    public CmsRelationType addRelationType(String name, String type, String defincontent) throws CmsConfigurationException {

        // check if new relation types can still be added
        if (m_frozen) {
            throw new CmsConfigurationException(Messages.get().container(Messages.ERR_NO_CONFIG_AFTER_STARTUP_0));
        }
        boolean isDefinedInContent = Boolean.valueOf(defincontent).booleanValue();
        CmsRelationType relationType = new CmsRelationType(m_configuredRelationTypes.size(), name, type, isDefinedInContent);
        m_configuredRelationTypes.add(relationType);
        return relationType;
    }

    /**
     * Adds a new resource type from the XML configuration to the internal list of loaded resource types.<p>
     * 
     * Resource types can also be added from a module.<p>
     *
     * @param resourceType the resource type to add
     * @throws CmsConfigurationException in case the resource manager configuration is already initialized
     */
    public void addResourceType(I_CmsResourceType resourceType) throws CmsConfigurationException {

        // check if new resource types can still be added
        if (m_frozen) {
            throw new CmsConfigurationException(Messages.get().container(Messages.ERR_NO_CONFIG_AFTER_STARTUP_0));
        }

        I_CmsResourceType conflictingType = null;
        if (resourceType.getTypeId() == CmsResourceTypeUnknownFile.RESOURCE_TYPE_ID) {
            // default unknown file resource type
            if (m_restypeUnknownFile != null) {
                // error: already set
                conflictingType = m_restypeUnknownFile;
            } else {
                m_restypeUnknownFile = resourceType;
                return;
            }
        } else if (resourceType.getTypeId() == CmsResourceTypeUnknownFolder.RESOURCE_TYPE_ID) {
            // default unknown folder resource type
            if (m_restypeUnknownFolder != null) {
                // error: already set
                conflictingType = m_restypeUnknownFolder;
            } else {
                m_restypeUnknownFolder = resourceType;
                return;
            }
        } else {
            // normal resource types
            int conflictIndex = m_resourceTypesFromXml.indexOf(resourceType);
            if (conflictIndex >= 0) {
                conflictingType = (I_CmsResourceType)m_resourceTypesFromXml.get(conflictIndex);
            }
        }
        if (conflictingType != null) {
            // configuration problem: the resource type (or at least the id or the name) is already configured
            throw new CmsConfigurationException(Messages.get().container(
                Messages.ERR_CONFLICTING_RESOURCE_TYPES_4,
                new Object[] {
                    resourceType.getTypeName(),
                    new Integer(resourceType.getTypeId()),
                    conflictingType.getTypeName(),
                    new Integer(conflictingType.getTypeId())}));
        }

        m_resourceTypesFromXml.add(resourceType);
    }

    /**
     * Returns the configured content collector with the given name, or <code>null</code> if 
     * no collector with this name is configured.<p>
     *  
     * @param collectorName the name of the collector to get
     * @return the configured content collector with the given name
     */
    public I_CmsResourceCollector getContentCollector(String collectorName) {

        return (I_CmsResourceCollector)m_collectorNameMappings.get(collectorName);
    }

    /**
     * Returns the default resource type for the given resource name, using the 
     * configured resource type file extensions.<p>
     * 
     * In case the given name does not map to a configured resource type,
     * {@link CmsResourceTypePlain} is returned.<p>
     * 
     * This is only required (and should <i>not</i> be used otherwise) when 
     * creating a new resource automatically during file upload or synchronization.
     * Only in this case, the file type for the new resource is determined using this method.
     * Otherwise the resource type is <i>always</i> stored as part of the resource, 
     * and is <i>not</i> related to the file name.<p>
     * 
     * @param resourcename the resource name to look up the resource type for
     * 
     * @return the default resource type for the given resource name
     * 
     * @throws CmsException if something goes wrong
     */
    public I_CmsResourceType getDefaultTypeForName(String resourcename) throws CmsException {

        String typeName = null;
        String suffix = null;
        if (CmsStringUtil.isNotEmpty(resourcename)) {
            int pos = resourcename.lastIndexOf('.');
            if (pos >= 0) {
                suffix = resourcename.substring(pos);
                if (CmsStringUtil.isNotEmpty(suffix)) {
                    suffix = suffix.toLowerCase();
                    typeName = (String)m_configuration.m_extensionMappings.get(suffix);

                }
            }
        }

        if (typeName == null) {
            // use default type "plain"
            typeName = CmsResourceTypePlain.getStaticTypeName();
        }

        if (CmsLog.INIT.isDebugEnabled()) {
            CmsLog.INIT.debug(Messages.get().getBundle().key(Messages.INIT_GET_RESTYPE_2, typeName, suffix));
        }
        // look up and return the resource type
        return getResourceType(typeName);
    }

    /**
     * Returns the file extensions (suffixes) mappings to resource types.<p>
     *
     * @return a Map with all known file extensions as keys and their resource types as values.
     */
    public Map getExtensionMapping() {

        return m_configuration.m_extensionMappings;
    }

    /**
     * Returns the file translator.<p>
     *
     * @return the file translator
     */
    public CmsResourceTranslator getFileTranslator() {

        return m_fileTranslator;
    }

    /**
     * Returns the folder translator.<p>
     *
     * @return the folder translator
     */
    public CmsResourceTranslator getFolderTranslator() {

        return m_folderTranslator;
    }

    /**
     * Returns the loader class instance for a given resource.<p>
     * 
     * @param resource the resource
     * @return the appropriate loader class instance
     * @throws CmsLoaderException if something goes wrong
     */
    public I_CmsResourceLoader getLoader(CmsResource resource) throws CmsLoaderException {

        return getLoader(getResourceType(resource.getTypeId()).getLoaderId());
    }

    /**
     * Returns the loader class instance for the given loader id.<p>
     * 
     * @param id the id of the loader to return
     * @return the loader class instance for the given loader id
     */
    public I_CmsResourceLoader getLoader(int id) {

        return m_loaders[id];
    }

    /**
     * Returns the (unmodifyable array) list with all initialized resource loaders.<p>
     * 
     * @return the (unmodifyable array) list with all initialized resource loaders
     */
    public List getLoaders() {

        return m_loaderList;
    }

    /**
     * Returns the MIME type for a specified file name.<p>
     * 
     * If an encoding parameter that is not <code>null</code> is provided,
     * the returned MIME type is extended with a <code>; charset={encoding}</code> setting.<p> 
     * 
     * If no MIME type for the given filename can be determined, the
     * default <code>{@link #MIMETYPE_HTML}</code> is used.<p>
     * 
     * @param filename the file name to check the MIME type for
     * @param encoding the default encoding (charset) in case of MIME types is of type "text"
     * 
     * @return the MIME type for a specified file
     */
    public String getMimeType(String filename, String encoding) {

        return getMimeType(filename, encoding, MIMETYPE_HTML);
    }

    /**
     * Returns the MIME type for a specified file name.<p>
     * 
     * If an encoding parameter that is not <code>null</code> is provided,
     * the returned MIME type is extended with a <code>; charset={encoding}</code> setting.<p> 
     * 
     * If no MIME type for the given filename can be determined, the
     * provided default is used.<p>
     * 
     * @param filename the file name to check the MIME type for
     * @param encoding the default encoding (charset) in case of MIME types is of type "text"
     * @param defaultMimeType the default MIME type to use if no matching type for the filename is found
     * 
     * @return the MIME type for a specified file
     */
    public String getMimeType(String filename, String encoding, String defaultMimeType) {

        String mimeType = null;
        int lastDot = filename.lastIndexOf('.');
        // check the MIME type for the file extension 
        if ((lastDot > 0) && (lastDot < (filename.length() - 1))) {
            mimeType = (String)m_mimeTypes.get(filename.substring(lastDot).toLowerCase(Locale.ENGLISH));
        }
        if (mimeType == null) {
            mimeType = defaultMimeType;
            if (mimeType == null) {
                // no default MIME type was provided
                return null;
            }
        }
        StringBuffer result = new StringBuffer(mimeType);
        if ((encoding != null) && mimeType.startsWith("text") && (mimeType.indexOf("charset") == -1)) {
            result.append("; charset=");
            result.append(encoding);
        }
        return result.toString();
    }

    /**
     * Returns an unmodifiable List of the configured {@link CmsMimeType} objects.<p>
     * 
     * @return an unmodifiable List of the configured {@link CmsMimeType} objects
     */
    public List getMimeTypes() {

        return m_configuredMimeTypes;
    }

    /**
     * Returns an (unmodifiable) list of class names of all currently registered content collectors 
     * ({@link I_CmsResourceCollector} objects).<p>
     *   
     * @return an (unmodifiable) list of class names of all currently registered content collectors
     *      ({@link I_CmsResourceCollector} objects)
     */
    public List getRegisteredContentCollectors() {

        return m_collectors;
    }

    /**
     * Returns an unmodifiable List of the configured {@link CmsRelationType} objects.<p>
     * 
     * @return an unmodifiable List of the configured {@link CmsRelationType} objects
     */
    public List getRelationTypes() {

        return m_configuredRelationTypes;
    }

    /**
     * Convenience method to get the initialized resource type instance for the given resource, 
     * with a fall back to special "unknown" resource types in case the resource type is not configured.<p>
     * 
     * @param resource the resource to get the type for
     * 
     * @return the initialized resource type instance for the given resource
     */
    public I_CmsResourceType getResourceType(CmsResource resource) {

        I_CmsResourceType result = m_configuration.getResourceTypeById(resource.getTypeId());
        if (result == null) {
            // this resource type is unknown, return the default files instead
            if (resource.isFolder()) {
                // resource is a folder
                if (m_restypeUnknownFolder != null) {
                    result = m_restypeUnknownFolder;
                } else {
                    result = m_configuration.getResourceTypeById(CmsResourceTypeFolder.getStaticTypeId());
                }
            } else {
                // resource is a file
                if (m_restypeUnknownFile != null) {
                    result = m_restypeUnknownFile;
                } else {
                    result = m_configuration.getResourceTypeById(CmsResourceTypeBinary.getStaticTypeId());
                }
            }
        }
        return result;
    }

    /**
     * Returns the initialized resource type instance for the given id.<p>
     * 
     * @param typeId the id of the resource type to get
     * 
     * @return the initialized resource type instance for the given id
     * 
     * @throws CmsLoaderException if no resource type is available for the given id
     */
    public I_CmsResourceType getResourceType(int typeId) throws CmsLoaderException {

        I_CmsResourceType result = m_configuration.getResourceTypeById(typeId);
        if (result == null) {
            throw new CmsLoaderException(Messages.get().container(
                Messages.ERR_UNKNOWN_RESTYPE_ID_REQ_1,
                new Integer(typeId)));
        }
        return result;
    }

    /**
     * Returns the initialized resource type instance for the given resource type name.<p>
     * 
     * @param typeName the name of the resource type to get
     * 
     * @return the initialized resource type instance for the given name
     * 
     * @throws CmsLoaderException if no resource type is available for the given name
     */
    public I_CmsResourceType getResourceType(String typeName) throws CmsLoaderException {

        I_CmsResourceType result = m_configuration.getResourceTypeByName(typeName);
        if (result != null) {
            return result;
        }
        throw new CmsLoaderException(Messages.get().container(Messages.ERR_UNKNOWN_RESTYPE_NAME_REQ_1, typeName));
    }

    /**
     * Returns the (unmodifiable) list with all initialized resource types.<p>
     * 
     * @return the (unmodifiable) list with all initialized resource types
     */
    public List getResourceTypes() {

        return m_configuration.m_resourceTypeList;
    }

    /**
     * Returns the (unmodifiable) list with all initialized resource types including unknown types.<p>
     * 
     * @return the (unmodifiable) list with all initialized resource types including unknown types
     */
    public List getResourceTypesWithUnknown() {

        return m_configuration.m_resourceTypeListWithUnknown;
    }

    /**
     * The configured default type for files when the resource type is missing.<p>
     * 
     * @return the configured default type for files
     */
    public I_CmsResourceType getResTypeUnknownFile() {

        return m_restypeUnknownFile;
    }

    /**
     * The configured default type for folders when the resource type is missing.<p>
     * 
     * @return The configured default type for folders
     */
    public I_CmsResourceType getResTypeUnknownFolder() {

        return m_restypeUnknownFolder;
    }

    /**
     * Returns a template loader facade for the given file.<p>
     * @param cms the current OpenCms user context
     * @param resource the requested file
     * @param templateProperty the property to read for the template
     * 
     * @return a resource loader facade for the given file
     * @throws CmsException if something goes wrong
     */
    public CmsTemplateLoaderFacade getTemplateLoaderFacade(CmsObject cms, CmsResource resource, String templateProperty)
    throws CmsException {

        String templateProp = cms.readPropertyObject(resource, templateProperty, true).getValue();

        if (templateProp == null) {

            // use default template, if template is not set
            templateProp = DEFAULT_TEMPLATE;

            if (!cms.existsResource(templateProp, CmsResourceFilter.IGNORE_EXPIRATION)) {
                // no template property defined, this is a must for facade loaders
                throw new CmsLoaderException(Messages.get().container(
                    Messages.ERR_NONDEF_PROP_2,
                    templateProperty,
                    cms.getSitePath(resource)));
            }
        } else if (!cms.existsResource(templateProp, CmsResourceFilter.IGNORE_EXPIRATION)) {

            // use default template, if template does not exist
            if (cms.existsResource(DEFAULT_TEMPLATE, CmsResourceFilter.IGNORE_EXPIRATION)) {
                templateProp = DEFAULT_TEMPLATE;
            }
        }

        CmsResource template = cms.readFile(templateProp, CmsResourceFilter.IGNORE_EXPIRATION);
        return new CmsTemplateLoaderFacade(getLoader(template), resource, template);
    }

    /**
     * Checks if an initialized resource type instance equal to the given resource type is available.<p>
     * 
     * @param type the resource type to check
     * @return <code>true</code> if such a resource type has been configured, <code>false</code> otherwise
     * 
     * @see #getResourceType(String)
     * @see #getResourceType(int)
     */
    public boolean hasResourceType(I_CmsResourceType type) {

        return hasResourceType(type.getTypeId());
    }

    /**
     * Checks if an initialized resource type instance for the given resource type is is available.<p>
     * 
     * @param typeId the id of the resource type to check
     * @return <code>true</code> if such a resource type has been configured, <code>false</code> otherwise
     * 
     * @see #getResourceType(int)
     */
    public boolean hasResourceType(int typeId) {

        return m_configuration.getResourceTypeById(typeId) != null;
    }

    /**
     * Checks if an initialized resource type instance for the given resource type name is available.<p>
     * 
     * @param typeName the name of the resource type to check
     * @return <code>true</code> if such a resource type has been configured, <code>false</code> otherwise
     * 
     * @see #getResourceType(String)
     */
    public boolean hasResourceType(String typeName) {

        return m_configuration.getResourceTypeByName(typeName) != null;
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#initConfiguration()
     * 
     * @throws CmsConfigurationException in case of duplicate resource types in the configuration
     */
    public void initConfiguration() throws CmsConfigurationException {

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_LOADER_CONFIG_FINISHED_0));
        }

        m_resourceTypesFromXml = Collections.unmodifiableList(m_resourceTypesFromXml);
        m_loaderList = Collections.unmodifiableList(m_loaderList);
        Collections.sort(m_configuredMimeTypes);
        m_configuredMimeTypes = Collections.unmodifiableList(m_configuredMimeTypes);
        m_configuredRelationTypes = Collections.unmodifiableList(m_configuredRelationTypes);

        // initialize the resource types
        initResourceTypes();
        // initialize the MIME types
        initMimeTypes();
    }

    /**
     * Initializes all additional resource types stored in the modules.<p>
     * 
     * @param cms an initialized OpenCms user context with "module manager" role permissions
     * 
     * @throws CmsRoleViolationException in case the provided OpenCms user context did not have "module manager" role permissions
     * @throws CmsConfigurationException in case of duplicate resource types in the configuration
     */
    public synchronized void initialize(CmsObject cms) throws CmsRoleViolationException, CmsConfigurationException {

        if (OpenCms.getRunLevel() > OpenCms.RUNLEVEL_1_CORE_OBJECT) {
            // some simple test cases don't require this check       
            OpenCms.getRoleManager().checkRole(cms, CmsRole.DATABASE_MANAGER);
        }

        // initialize the resource types
        initResourceTypes();

        // call initialize method on all resource types
        Iterator i = m_configuration.m_resourceTypeList.iterator();
        while (i.hasNext()) {
            I_CmsResourceType type = (I_CmsResourceType)i.next();
            type.initialize(cms);
        }

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_LOADER_CONFIG_FINISHED_0));
        }
    }

    /**    
     * Loads the requested resource and writes the contents to the response stream.<p>
     * 
     * @param req the current HTTP request
     * @param res the current HTTP response
     * @param cms the current OpenCms user context
     * @param resource the requested resource
     * @throws ServletException if something goes wrong
     * @throws IOException if something goes wrong
     * @throws CmsException if something goes wrong
     */
    public void loadResource(CmsObject cms, CmsResource resource, HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException, CmsException {

        res.setContentType(getMimeType(resource.getName(), cms.getRequestContext().getEncoding()));
        I_CmsResourceLoader loader = getLoader(resource);
        loader.load(cms, resource, req, res);
    }

    /**
     * Extension method for handling special, loader depended actions during the include process.<p>
     * 
     * Note: If you have multiple loaders configured that require include extensions, 
     * all loaders are called in the order they are configured in.<p> 
     * 
     * @param target the target for the include, might be <code>null</code>
     * @param element the element to select form the target might be <code>null</code>
     * @param editable the flag to indicate if the target is is enabled for direct edit
     * @param paramMap a map of parameters for the include, can be modified, might be <code>null</code>
     * @param req the current request
     * @param res the current response
     * @throws CmsException in case something goes wrong
     * @return the modified target URI
     */
    public String resolveIncludeExtensions(
        String target,
        String element,
        boolean editable,
        Map paramMap,
        ServletRequest req,
        ServletResponse res) throws CmsException {

        if (m_includeExtensions == null) {
            return target;
        }
        String result = target;
        for (int i = 0; i < m_includeExtensions.size(); i++) {
            // offer the element to every include extension
            I_CmsLoaderIncludeExtension loader = (I_CmsLoaderIncludeExtension)m_includeExtensions.get(i);
            result = loader.includeExtension(target, element, editable, paramMap, req, res);
        }
        return result;
    }

    /**
     * Sets the folder and the file translator.<p>
     * 
     * @param folderTranslator the folder translator to set
     * @param fileTranslator the file translator to set
     */
    public void setTranslators(CmsResourceTranslator folderTranslator, CmsResourceTranslator fileTranslator) {

        m_folderTranslator = folderTranslator;
        m_fileTranslator = fileTranslator;
    }

    /**
     * Shuts down this resource manage instance.<p>
     * 
     * @throws Exception in case of errors during shutdown
     */
    public synchronized void shutDown() throws Exception {

        Iterator it = m_loaderList.iterator();
        while (it.hasNext()) {
            // destroy all resource loaders
            I_CmsResourceLoader loader = (I_CmsResourceLoader)it.next();
            loader.destroy();
        }

        m_loaderList = null;
        m_loaders = null;
        m_collectorNameMappings = null;
        m_includeExtensions = null;
        m_mimeTypes = null;
        m_configuredMimeTypes = null;
        m_configuredRelationTypes = null;

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_SHUTDOWN_1, this.getClass().getName()));
        }
    }

    /**
     * Initialize the MIME types.<p>
     * 
     * MIME types are configured in the OpenCms <code>opencms-vfs.xml</code> configuration file.<p>
     * 
     * For legacy reasons, the MIME types are also read from a file <code>"mimetypes.properties"</code>
     * that must be located in the default <code>"classes"</code> folder of the web application.<p>
     */
    private void initMimeTypes() {

        // legacy MIME type initialization: try to read properties file
        Properties mimeTypes = new Properties();
        try {
            // first try: read MIME types from default package
            mimeTypes.load(getClass().getClassLoader().getResourceAsStream("mimetypes.properties"));
        } catch (Throwable t) {
            try {
                // second try: read MIME types from loader package (legacy reasons, there are no types by default)
                mimeTypes.load(getClass().getClassLoader().getResourceAsStream(
                    "org/opencms/loader/mimetypes.properties"));
            } catch (Throwable t2) {
                if (LOG.isInfoEnabled()) {
                    LOG.info(Messages.get().getBundle().key(
                        Messages.LOG_READ_MIMETYPES_FAILED_2,
                        "mimetypes.properties",
                        "org/opencms/loader/mimetypes.properties"));
                }
            }
        }

        // initialize the Map with all available MIME types
        List combinedMimeTypes = new ArrayList(mimeTypes.size() + m_configuredMimeTypes.size());
        // first add all MIME types from the configuration
        combinedMimeTypes.addAll(m_configuredMimeTypes);
        // now add the MIME types from the properties        
        Iterator i = mimeTypes.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry entry = (Map.Entry)i.next();
            CmsMimeType mimeType = new CmsMimeType((String)entry.getKey(), (String)entry.getValue(), false);
            if (!combinedMimeTypes.contains(mimeType)) {
                // make sure no MIME types from the XML configuration are overwritten
                combinedMimeTypes.add(mimeType);
            }
        }

        // create a lookup Map for the MIME types
        m_mimeTypes = new HashMap(mimeTypes.size());
        Iterator j = combinedMimeTypes.iterator();
        while (j.hasNext()) {
            CmsMimeType mimeType = (CmsMimeType)j.next();
            m_mimeTypes.put(mimeType.getExtension(), mimeType.getType());
        }

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(
                Messages.INIT_NUM_MIMETYPES_1,
                new Integer(m_mimeTypes.size())));
        }
    }

    /**
     * Adds a new resource type to the internal list of loaded resource types and initializes 
     * options for the resource type.<p>
     *
     * @param resourceType the resource type to add
     * @param configuration the resource configuration
     */
    private synchronized void initResourceType(
        I_CmsResourceType resourceType,
        CmsResourceManagerConfiguration configuration) {

        // add the loader to the internal list of loaders
        configuration.addResourceType(resourceType);
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(
                Messages.INIT_ADD_RESTYPE_3,
                resourceType.getTypeName(),
                new Integer(resourceType.getTypeId()),
                resourceType.getClass().getName()));
        }

        // add the mappings
        List mappings = resourceType.getConfiguredMappings();
        Iterator i = mappings.iterator();
        while (i.hasNext()) {
            String mapping = (String)i.next();
            // only add this mapping if a mapping with this file extension does not
            // exist already
            if (!configuration.m_extensionMappings.containsKey(mapping)) {
                configuration.m_extensionMappings.put(mapping, resourceType.getTypeName());
                if (CmsLog.INIT.isInfoEnabled()) {
                    CmsLog.INIT.info(Messages.get().getBundle().key(
                        Messages.INIT_MAP_RESTYPE_2,
                        mapping,
                        resourceType.getTypeName()));
                }
            }
        }
    }

    /**
     * Initializes member variables required for storing the resource types.<p>
     *
     * @throws CmsConfigurationException in case of duplicate resource types in the configuration
     */
    private synchronized void initResourceTypes() throws CmsConfigurationException {

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_STARTING_LOADER_CONFIG_0));
        }

        CmsResourceManagerConfiguration newConfiguration = new CmsResourceManagerConfiguration();

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(
                Messages.INIT_ADD_RESTYPE_FROM_FILE_2,
                new Integer(m_resourceTypesFromXml.size()),
                CmsVfsConfiguration.DEFAULT_XML_FILE_NAME));
        }

        // build a new resource type list from the resource types of the XML configuration
        Iterator i;
        i = m_resourceTypesFromXml.iterator();
        while (i.hasNext()) {
            I_CmsResourceType resourceType = (I_CmsResourceType)i.next();
            initResourceType(resourceType, newConfiguration);
        }

        // add all resource types declared in the modules
        CmsModuleManager moduleManager = OpenCms.getModuleManager();
        if (moduleManager != null) {
            i = moduleManager.getModuleNames().iterator();
            while (i.hasNext()) {
                CmsModule module = moduleManager.getModule((String)i.next());
                if ((module != null) && (module.getResourceTypes().size() > 0)) {
                    // module contains resource types                
                    if (CmsLog.INIT.isInfoEnabled()) {
                        CmsLog.INIT.info(Messages.get().getBundle().key(
                            Messages.INIT_ADD_NUM_RESTYPES_FROM_MOD_2,
                            new Integer(module.getResourceTypes().size()),
                            module.getName()));
                    }

                    Iterator j = module.getResourceTypes().iterator();
                    while (j.hasNext()) {
                        I_CmsResourceType resourceType = (I_CmsResourceType)j.next();
                        I_CmsResourceType conflictingType = null;
                        if (resourceType.getTypeId() == CmsResourceTypeUnknownFile.RESOURCE_TYPE_ID) {
                            // default unknown file resource type
                            if (m_restypeUnknownFile != null) {
                                // error: already set
                                conflictingType = m_restypeUnknownFile;
                            } else {
                                m_restypeUnknownFile = resourceType;
                                continue;
                            }
                        } else if (resourceType.getTypeId() == CmsResourceTypeUnknownFolder.RESOURCE_TYPE_ID) {
                            // default unknown folder resource type
                            if (m_restypeUnknownFolder != null) {
                                // error: already set
                                conflictingType = m_restypeUnknownFolder;
                            } else {
                                m_restypeUnknownFile = resourceType;
                                continue;
                            }
                        } else {
                            // normal resource types
                            conflictingType = newConfiguration.getResourceTypeById(resourceType.getTypeId());
                        }
                        if (conflictingType != null) {
                            throw new CmsConfigurationException(Messages.get().container(
                                Messages.ERR_CONFLICTING_MODULE_RESOURCE_TYPES_5,
                                new Object[] {
                                    resourceType.getTypeName(),
                                    new Integer(resourceType.getTypeId()),
                                    module.getName(),
                                    conflictingType.getTypeName(),
                                    new Integer(conflictingType.getTypeId())}));
                        }
                        initResourceType(resourceType, newConfiguration);
                    }
                }
            }
        }

        // freeze the current configuration
        newConfiguration.freeze(m_restypeUnknownFile, m_restypeUnknownFile);
        m_configuration = newConfiguration;
        m_frozen = true;

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_RESOURCE_TYPE_INITIALIZED_0));
        }
    }
}