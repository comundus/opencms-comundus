/*
 * File   : $Source: /usr/local/cvs/opencms/src/org/opencms/configuration/CmsSearchConfiguration.java,v $
 * Date   : $Date: 2008-02-27 12:05:48 $
 * Version: $Revision: 1.20 $
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

package org.opencms.configuration;

import org.opencms.i18n.CmsLocaleComparator;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsSearchAnalyzer;
import org.opencms.search.CmsSearchDocumentType;
import org.opencms.search.CmsSearchIndex;
import org.opencms.search.CmsSearchIndexSource;
import org.opencms.search.CmsSearchManager;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.search.fields.CmsSearchFieldConfiguration;
import org.opencms.search.fields.CmsSearchFieldMapping;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.digester.Digester;

import org.dom4j.Element;

/**
 * Lucene search configuration class.<p>
 * 
 * @author Thomas Weckert 
 * 
 * @version $Revision: 1.20 $
 * 
 * @since 6.0.0
 */
public class CmsSearchConfiguration extends A_CmsXmlConfiguration implements I_CmsXmlConfiguration {

    /** The "boost" attribute. */
    public static final String A_BOOST = "boost";

    /** The "displayName" attribute. */
    public static final String A_DISPLAY = "display";

    /** The "excerpt" attribute. */
    public static final String A_EXCERPT = "excerpt";

    /** The "index" attribute. */
    public static final String A_INDEX = "index";

    /** The "store" attribute. */
    public static final String A_STORE = "store";

    /** The name of the DTD for this configuration. */
    public static final String CONFIGURATION_DTD_NAME = "opencms-search.dtd";

    /** The name of the default XML file for this configuration. */
    public static final String DEFAULT_XML_FILE_NAME = "opencms-search.xml";

    /** Node name constant. */
    public static final String N_ANALYZER = "analyzer";

    /** Node name constant. */
    public static final String N_ANALYZERS = "analyzers";

    /** Node name constant. */
    public static final String N_CLASS = "class";

    /** Node name constant. */
    public static final String N_CONFIGURATION = "configuration";

    /** Node name constant. */
    public static final String N_DESCRIPTION = "description";

    /** Node name constant. */
    public static final String N_DIRECTORY = "directory";

    /** Node name constant. */
    public static final String N_DOCUMENTTYPE = "documenttype";

    /** Node name constant. */
    public static final String N_DOCUMENTTYPES = "documenttypes";

    /** Node name constant. */
    public static final String N_DOCUMENTTYPES_INDEXED = "documenttypes-indexed";

    /** Node name constant. */
    public static final String N_EXCERPT = "excerpt";

    /** Node name constant. */
    public static final String N_EXTRACTION_CACHE_MAX_AGE = "extractionCacheMaxAge";

    /** Node name constant. */
    public static final String N_FIELD = "field";

    /** Node name constant. */
    public static final String N_FIELDCONFIGURATION = "fieldconfiguration";

    /** Node name constant. */
    public static final String N_FIELDCONFIGURATIONS = "fieldconfigurations";

    /** Node name constant. */
    public static final String N_FIELDS = "fields";

    /** Node name constant. */
    public static final String N_FORCEUNLOCK = "forceunlock";

    /** Node name constant. */
    public static final String N_HIGHLIGHTER = "highlighter";

    /** Node name constant. */
    public static final String N_INDEX = "index";

    /** Node name constant. */
    public static final String N_INDEXER = "indexer";

    /** Node name constant. */
    public static final String N_INDEXES = "indexes";

    /** Node name constant. */
    public static final String N_INDEXSOURCE = "indexsource";

    /** Node name constant. */
    public static final String N_INDEXSOURCES = "indexsources";

    /** Node name constant. */
    public static final String N_LOCALE = "locale";

    /** Node name constant. */
    public static final String N_MAPPING = "mapping";

    /** Node name constant. */
    public static final String N_MIMETYPE = "mimetype";

    /** Node name constant. */
    public static final String N_MIMETYPES = "mimetypes";

    /** Node name constant. */
    public static final String N_PROJECT = "project";

    /** Node name constant. */
    public static final String N_REBUILD = "rebuild";

    /** Node name constant. */
    public static final String N_RESOURCES = "resources";

    /** Node name constant. */
    public static final String N_RESOURCETYPE = "resourcetype";

    /** Node name constant. */
    public static final String N_RESOURCETYPES = "resourcetypes";

    /** Node name constant. */
    public static final String N_SEARCH = "search";

    /** Node name constant. */
    public static final String N_SOURCE = "source";

    /** Node name constant. */
    public static final String N_SOURCES = "sources";

    /** Node name constant. */
    public static final String N_STEMMER = "stemmer";

    /** Node name constant. */
    public static final String N_TIMEOUT = "timeout";

    /** Node name constant. */
    private static final String XPATH_SEARCH = "*/" + N_SEARCH;

    /** The configured search manager. */
    private CmsSearchManager m_searchManager;

    /**
     * Public constructor, will be called by configuration manager.<p> 
     */
    public CmsSearchConfiguration() {

        setXmlFileName(DEFAULT_XML_FILE_NAME);
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_SEARCH_CONFIG_INIT_0));
        }
    }

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#addXmlDigesterRules(org.apache.commons.digester.Digester)
     */
    public void addXmlDigesterRules(Digester digester) {

        String xPath = null;

        // add finish rule
        digester.addCallMethod(XPATH_SEARCH, "initializeFinished");

        // creation of the search manager        
        digester.addObjectCreate(XPATH_SEARCH, CmsSearchManager.class);

        // search manager finished
        digester.addSetNext(XPATH_SEARCH, "setSearchManager");

        // directory rule
        digester.addCallMethod(XPATH_SEARCH + "/" + N_DIRECTORY, "setDirectory", 0);

        // timeout rule
        digester.addCallMethod(XPATH_SEARCH + "/" + N_TIMEOUT, "setTimeout", 0);

        // forceunlock rule
        digester.addCallMethod(XPATH_SEARCH + "/" + N_FORCEUNLOCK, "setForceunlock", 0);

        // rule for the max. char. lenght of the search result excerpt
        digester.addCallMethod(XPATH_SEARCH + "/" + N_EXCERPT, "setMaxExcerptLength", 0);

        // rule for the max. age of entries in the extraction cache
        digester.addCallMethod(XPATH_SEARCH + "/" + N_EXTRACTION_CACHE_MAX_AGE, "setExtractionCacheMaxAge", 0);

        // rule for the highlighter to highlight the search terms in the excerpt of the search result
        digester.addCallMethod(XPATH_SEARCH + "/" + N_HIGHLIGHTER, "setHighlighter", 0);

        // document type rule
        xPath = XPATH_SEARCH + "/" + N_DOCUMENTTYPES + "/" + N_DOCUMENTTYPE;
        digester.addObjectCreate(xPath, CmsSearchDocumentType.class);
        digester.addCallMethod(xPath + "/" + N_NAME, "setName", 0);
        digester.addCallMethod(xPath + "/" + N_CLASS, "setClassName", 0);
        digester.addCallMethod(xPath + "/" + N_MIMETYPES + "/" + N_MIMETYPE, "addMimeType", 0);
        digester.addCallMethod(xPath + "/" + N_RESOURCETYPES + "/" + N_RESOURCETYPE, "addResourceType", 0);
        digester.addSetNext(xPath, "addDocumentTypeConfig");

        // analyzer rule
        xPath = XPATH_SEARCH + "/" + N_ANALYZERS + "/" + N_ANALYZER;
        digester.addObjectCreate(xPath, CmsSearchAnalyzer.class);
        digester.addCallMethod(xPath + "/" + N_CLASS, "setClassName", 0);
        digester.addCallMethod(xPath + "/" + N_STEMMER, "setStemmerAlgorithm", 0);
        digester.addCallMethod(xPath + "/" + N_LOCALE, "setLocaleString", 0);
        digester.addSetNext(xPath, "addAnalyzer");

        // search index rule
        xPath = XPATH_SEARCH + "/" + N_INDEXES + "/" + N_INDEX;
        digester.addObjectCreate(xPath, CmsSearchIndex.class);
        digester.addCallMethod(xPath + "/" + N_NAME, "setName", 0);
        digester.addCallMethod(xPath + "/" + N_REBUILD, "setRebuildMode", 0);
        digester.addCallMethod(xPath + "/" + N_PROJECT, "setProjectName", 0);
        digester.addCallMethod(xPath + "/" + N_LOCALE, "setLocaleString", 0);
        digester.addCallMethod(xPath + "/" + N_CONFIGURATION, "setFieldConfigurationName", 0);
        digester.addCallMethod(xPath + "/" + N_SOURCES + "/" + N_SOURCE, "addSourceName", 0);
        digester.addSetNext(xPath, "addSearchIndex");

        // search index source rule
        xPath = XPATH_SEARCH + "/" + N_INDEXSOURCES + "/" + N_INDEXSOURCE;
        digester.addObjectCreate(xPath, CmsSearchIndexSource.class);
        digester.addCallMethod(xPath + "/" + N_NAME, "setName", 0);
        digester.addCallMethod(xPath + "/" + N_INDEXER, "setIndexerClassName", 1);
        digester.addCallParam(xPath + "/" + N_INDEXER, 0, N_CLASS);
        digester.addCallMethod(xPath + "/" + N_RESOURCES + "/" + N_RESOURCE, "addResourceName", 0);
        digester.addCallMethod(xPath + "/" + N_DOCUMENTTYPES_INDEXED + "/" + N_NAME, "addDocumentType", 0);
        digester.addSetNext(xPath, "addSearchIndexSource");

        // field configuration rules
        xPath = XPATH_SEARCH + "/" + N_FIELDCONFIGURATIONS + "/" + N_FIELDCONFIGURATION;
        digester.addObjectCreate(xPath, CmsSearchFieldConfiguration.class);
        digester.addCallMethod(xPath + "/" + N_NAME, "setName", 0);
        digester.addCallMethod(xPath + "/" + N_DESCRIPTION, "setDescription", 0);
        digester.addSetNext(xPath, "addFieldConfiguration");

        xPath = xPath + "/" + N_FIELDS + "/" + N_FIELD;
        digester.addObjectCreate(xPath, CmsSearchField.class);
        digester.addCallMethod(xPath, "setName", 1);
        digester.addCallParam(xPath, 0, I_CmsXmlConfiguration.A_NAME);
        digester.addCallMethod(xPath, "setDisplayNameForConfiguration", 1);
        digester.addCallParam(xPath, 0, A_DISPLAY);
        digester.addCallMethod(xPath, "setStored", 1);
        digester.addCallParam(xPath, 0, A_STORE);
        digester.addCallMethod(xPath, "setIndexed", 1);
        digester.addCallParam(xPath, 0, A_INDEX);
        digester.addCallMethod(xPath, "setBoost", 1);
        digester.addCallParam(xPath, 0, A_BOOST);
        digester.addCallMethod(xPath, "setInExcerpt", 1);
        digester.addCallParam(xPath, 0, A_EXCERPT);
        digester.addCallMethod(xPath, "setDefaultValue", 1);
        digester.addCallParam(xPath, 0, A_DEFAULT);
        digester.addSetNext(xPath, "addField");

        xPath = xPath + "/" + N_MAPPING;
        digester.addObjectCreate(xPath, CmsSearchFieldMapping.class);
        digester.addCallMethod(xPath, "setDefaultValue", 1);
        digester.addCallParam(xPath, 0, A_DEFAULT);
        digester.addCallMethod(xPath, "setType", 1);
        digester.addCallParam(xPath, 0, A_TYPE);
        digester.addCallMethod(xPath, "setParam", 0);
        digester.addSetNext(xPath, "addMapping");

        // generic <param> parameter rules
        digester.addCallMethod(
            "*/" + I_CmsXmlConfiguration.N_PARAM,
            I_CmsConfigurationParameterHandler.ADD_PARAMETER_METHOD,
            2);
        digester.addCallParam("*/" + I_CmsXmlConfiguration.N_PARAM, 0, I_CmsXmlConfiguration.A_NAME);
        digester.addCallParam("*/" + I_CmsXmlConfiguration.N_PARAM, 1);
    }

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#generateXml(org.dom4j.Element)
     */
    public Element generateXml(Element parent) {

        // add <search> node
        Element searchElement = parent.addElement(N_SEARCH);
        if (OpenCms.getRunLevel() >= OpenCms.RUNLEVEL_3_SHELL_ACCESS) {
            // initialized OpenCms instance is available, use latest values
            m_searchManager = OpenCms.getSearchManager();
        }

        // add <directory> element
        searchElement.addElement(N_DIRECTORY).addText(m_searchManager.getDirectory());
        // add <timeout> element
        searchElement.addElement(N_TIMEOUT).addText(String.valueOf(m_searchManager.getTimeout()));
        // add <forceunlock> element
        if (m_searchManager.getForceunlock() != null) {
            searchElement.addElement(N_FORCEUNLOCK).addText(m_searchManager.getForceunlock().toString());
        }
        // add <exerpt> element
        searchElement.addElement(N_EXCERPT).addText(String.valueOf(m_searchManager.getMaxExcerptLength()));
        // add <extractionCacheMaxAge> element
        searchElement.addElement(N_EXTRACTION_CACHE_MAX_AGE).addText(
            String.valueOf(m_searchManager.getExtractionCacheMaxAge()));
        // add <highlighter> element
        searchElement.addElement(N_HIGHLIGHTER).addText(m_searchManager.getHighlighter().getClass().getName());

        // <documenttypes> 
        Element documenttypesElement = searchElement.addElement(N_DOCUMENTTYPES);
        List docTypeKeyList = m_searchManager.getDocumentTypeConfigs();
        Iterator docTypeIterator = docTypeKeyList.iterator();
        while (docTypeIterator.hasNext()) {
            CmsSearchDocumentType currSearchDocType = (CmsSearchDocumentType)docTypeIterator.next();
            // add the next <documenttype> element
            Element documenttypeElement = documenttypesElement.addElement(N_DOCUMENTTYPE);
            // add <name> element
            documenttypeElement.addElement(N_NAME).addText(currSearchDocType.getName());
            // add <class> element
            documenttypeElement.addElement(N_CLASS).addText(currSearchDocType.getClassName());
            // add <mimetypes> element
            Element mimetypesElement = documenttypeElement.addElement(N_MIMETYPES);
            // get the list of mimetypes to trigger the document factory class 
            Iterator mimeTypesIterator = currSearchDocType.getMimeTypes().iterator();
            while (mimeTypesIterator.hasNext()) {
                // add <mimetype> element(s)
                mimetypesElement.addElement(N_MIMETYPE).addText((String)mimeTypesIterator.next());
            }
            // add <resourcetypes> element
            Element restypesElement = documenttypeElement.addElement(N_RESOURCETYPES);
            // get the list of Cms resource types to trigger the document factory
            Iterator resTypesIterator = currSearchDocType.getResourceTypes().iterator();
            while (resTypesIterator.hasNext()) {
                // add <resourcetype> element(s)
                restypesElement.addElement(N_RESOURCETYPE).addText((String)resTypesIterator.next());
            }
        }
        // </documenttypes> 

        // <analyzers> 
        Element analyzersElement = searchElement.addElement(N_ANALYZERS);
        List analyzerLocaleList = new ArrayList(m_searchManager.getAnalyzers().keySet());
        // sort Analyzers in ascending order
        Collections.sort(analyzerLocaleList, CmsLocaleComparator.getComparator());
        Iterator analyzersLocaleInterator = analyzerLocaleList.iterator();
        while (analyzersLocaleInterator.hasNext()) {
            CmsSearchAnalyzer searchAnalyzer = m_searchManager.getCmsSearchAnalyzer((Locale)analyzersLocaleInterator.next());
            // add the next <analyzer> element
            Element analyzerElement = analyzersElement.addElement(N_ANALYZER);
            // add <class> element
            analyzerElement.addElement(N_CLASS).addText(searchAnalyzer.getClassName());
            if (searchAnalyzer.getStemmerAlgorithm() != null) {
                // add <stemmer> element
                analyzerElement.addElement(N_STEMMER).addText(searchAnalyzer.getStemmerAlgorithm());
            }
            // add <locale> element
            analyzerElement.addElement(N_LOCALE).addText(searchAnalyzer.getLocale().toString());
        }
        // </analyzers>

        // <indexes>
        Element indexesElement = searchElement.addElement(N_INDEXES);
        Iterator indexIterator = m_searchManager.getSearchIndexes().iterator();
        while (indexIterator.hasNext()) {
            CmsSearchIndex searchIndex = (CmsSearchIndex)indexIterator.next();
            // add the next <index> element
            Element indexElement = indexesElement.addElement(N_INDEX);
            // add <name> element
            indexElement.addElement(N_NAME).addText(searchIndex.getName());
            // add <rebuild> element
            indexElement.addElement(N_REBUILD).addText(searchIndex.getRebuildMode());
            // add <project> element
            indexElement.addElement(N_PROJECT).addText(searchIndex.getProject());
            // add <locale> element
            indexElement.addElement(N_LOCALE).addText(searchIndex.getLocale().toString());
            // add <configuration> element
            String fieldConfigurationName = searchIndex.getFieldConfigurationName();
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(fieldConfigurationName)) {
                indexElement.addElement(N_CONFIGURATION).addText(fieldConfigurationName);
            }
            // add <sources> element
            Element sourcesElement = indexElement.addElement(N_SOURCES);
            // iterate above sourcenames
            Iterator sourcesIterator = searchIndex.getSourceNames().iterator();
            while (sourcesIterator.hasNext()) {
                // add <source> element
                sourcesElement.addElement(N_SOURCE).addText((String)sourcesIterator.next());
            }
            // iterate additional params
            Map indexConfiguration = searchIndex.getConfiguration();
            if (indexConfiguration != null) {
                Iterator it = indexConfiguration.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry entry = (Map.Entry)it.next();
                    String name = (String)entry.getKey();
                    String value = (String)entry.getValue();
                    Element paramNode = indexElement.addElement(N_PARAM);
                    paramNode.addAttribute(A_NAME, name);
                    paramNode.addText(value);
                }
            }
        }
        // </indexes>

        // <indexsources>
        Element indexsourcesElement = searchElement.addElement(N_INDEXSOURCES);
        List indexSources = new ArrayList(m_searchManager.getSearchIndexSources().values());
        Iterator indexsourceIterator = indexSources.iterator();
        while (indexsourceIterator.hasNext()) {
            CmsSearchIndexSource searchIndexSource = (CmsSearchIndexSource)indexsourceIterator.next();
            // add <indexsource> element(s)
            Element indexsourceElement = indexsourcesElement.addElement(N_INDEXSOURCE);
            // add <name> element
            indexsourceElement.addElement(N_NAME).addText(searchIndexSource.getName());
            // add <indexer class=""> element
            Element indexerElement = indexsourceElement.addElement(N_INDEXER).addAttribute(
                N_CLASS,
                searchIndexSource.getIndexerClassName());
            Map params = searchIndexSource.getParams();
            Iterator paramIterator = params.entrySet().iterator();
            while (paramIterator.hasNext()) {
                Map.Entry entry = (Map.Entry)paramIterator.next();
                String name = (String)entry.getKey();
                String value = (String)entry.getValue();
                // add <param name=""> element(s)                
                indexerElement.addElement(I_CmsXmlConfiguration.N_PARAM).addAttribute(
                    I_CmsXmlConfiguration.A_NAME,
                    name).addText(value);
            }
            // add <resources> element
            Element resourcesElement = indexsourceElement.addElement(N_RESOURCES);
            Iterator resourceIterator = searchIndexSource.getResourcesNames().iterator();
            while (resourceIterator.hasNext()) {
                // add <resource> element(s)
                resourcesElement.addElement(N_RESOURCE).addText((String)resourceIterator.next());
            }
            // add <documenttypes-indexed> element
            Element doctypes_indexedElement = indexsourceElement.addElement(N_DOCUMENTTYPES_INDEXED);
            Iterator doctypesIterator = searchIndexSource.getDocumentTypes().iterator();
            while (doctypesIterator.hasNext()) {
                // add <name> element(s)
                doctypes_indexedElement.addElement(N_NAME).addText((String)doctypesIterator.next());
            }
        }
        // </indexsources>

        // <fieldconfigurations>
        Element configurationsElement = searchElement.addElement(N_FIELDCONFIGURATIONS);
        Iterator configs = m_searchManager.getFieldConfigurations().iterator();
        while (configs.hasNext()) {
            CmsSearchFieldConfiguration config = (CmsSearchFieldConfiguration)configs.next();
            Element configElement = configurationsElement.addElement(N_FIELDCONFIGURATION);
            configElement.addElement(N_NAME).setText(config.getName());
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(config.getDescription())) {
                configElement.addElement(N_DESCRIPTION).setText(config.getDescription());
            }
            Element fieldsElement = configElement.addElement(N_FIELDS);
            Iterator fields = config.getFields().iterator();
            while (fields.hasNext()) {
                CmsSearchField field = (CmsSearchField)fields.next();
                Element fieldElement = fieldsElement.addElement(N_FIELD);
                fieldElement.addAttribute(A_NAME, field.getName());
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(field.getDisplayNameForConfiguration())) {
                    fieldElement.addAttribute(A_DISPLAY, field.getDisplayNameForConfiguration());
                }
                fieldElement.addAttribute(A_STORE, String.valueOf(field.isStored()));
                String index;
                if (field.isIndexed()) {
                    if (field.isTokenizedAndIndexed()) {
                        // index and tokenized
                        index = CmsStringUtil.TRUE;
                    } else {
                        // indexed but not tokenized
                        index = CmsSearchField.STR_UN_TOKENIZED;
                    }
                } else {
                    // not indexed at all
                    index = CmsStringUtil.FALSE;
                }
                fieldElement.addAttribute(A_INDEX, index);
                if (field.getBoost() != CmsSearchField.BOOST_DEFAULT) {
                    fieldElement.addAttribute(A_BOOST, String.valueOf(field.getBoost()));
                }
                if (field.isInExcerptAndStored()) {
                    fieldElement.addAttribute(A_EXCERPT, String.valueOf(true));
                }
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(field.getDefaultValue())) {
                    fieldElement.addAttribute(A_DEFAULT, field.getDefaultValue());
                }
                Iterator mappings = field.getMappings().iterator();
                while (mappings.hasNext()) {
                    CmsSearchFieldMapping mapping = (CmsSearchFieldMapping)mappings.next();
                    Element mappingElement = fieldElement.addElement(N_MAPPING);
                    mappingElement.addAttribute(A_TYPE, mapping.getType().toString());
                    if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(mapping.getDefaultValue())) {
                        mappingElement.addAttribute(A_DEFAULT, mapping.getDefaultValue());
                    }
                    if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(mapping.getParam())) {
                        mappingElement.setText(mapping.getParam());
                    }
                }
            }
        }
        // </fieldconfigurations>

        return searchElement;
    }

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#getDtdFilename()
     */
    public String getDtdFilename() {

        return CONFIGURATION_DTD_NAME;
    }

    /**
     * Returns the generated search manager.<p>
     * 
     * @return the generated search manager
     */
    public CmsSearchManager getSearchManager() {

        return m_searchManager;
    }

    /**
     * Will be called when configuration of this object is finished.<p> 
     */
    public void initializeFinished() {

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_SEARCH_CONFIG_FINISHED_0));
        }
    }

    /**
     * Sets the generated search manager.<p>
     * 
     * @param manager the search manager to set
     */
    public void setSearchManager(CmsSearchManager manager) {

        m_searchManager = manager;
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_SEARCH_MANAGER_FINISHED_0));
        }
    }
}