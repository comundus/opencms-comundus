/*
 * File   : $Source: /usr/local/cvs/opencms/src/org/opencms/xml/A_CmsXmlDocument.java,v $
 * Date   : $Date: 2008-04-14 13:51:37 $
 * Version: $Revision: 1.37 $
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

package org.opencms.xml;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.xml.types.I_CmsXmlContentValue;
import org.opencms.xml.types.I_CmsXmlSchemaType;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.Element;
import org.xml.sax.EntityResolver;

/**
 * Provides basic XML document handling functions useful when dealing
 * with XML documents that are stored in the OpenCms VFS.<p>
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.37 $ 
 * 
 * @since 6.0.0 
 */
public abstract class A_CmsXmlDocument implements I_CmsXmlDocument {

    /** The content conversion to use for this XML document. */
    protected String m_conversion;

    /** The document object of the document. */
    protected Document m_document;

    /** Maps element names to available locales. */
    protected Map m_elementLocales;

    /** Maps locales to avaliable element names. */
    protected Map m_elementNames;

    /** The encoding to use for this xml document. */
    protected String m_encoding;

    /** The file that contains the document data (note: is not set when creating an empty or document based document). */
    protected CmsFile m_file;

    /** Set of locales contained in this document. */
    protected Set m_locales;

    /** Reference for named elements in the document. */
    private Map m_bookmarks;

    /**
     * Default constructor for a XML document
     * that initializes some internal values.<p> 
     */
    protected A_CmsXmlDocument() {

        m_bookmarks = new HashMap();
        m_locales = new HashSet();
    }

    /**
     * Creates the bookmark name for a localized element to be used in the bookmark lookup table.<p>
     * 
     * @param name the element name
     * @param locale the element locale 
     * @return the bookmark name for a localized element
     */
    protected static final String getBookmarkName(String name, Locale locale) {

        StringBuffer result = new StringBuffer(64);
        result.append('/');
        result.append(locale.toString());
        result.append('/');
        result.append(name);
        return result.toString();
    }

    /**
     * @see org.opencms.xml.I_CmsXmlDocument#copyLocale(java.util.List, java.util.Locale)
     */
    public void copyLocale(List possibleSources, Locale destination) throws CmsXmlException {

        if (hasLocale(destination)) {
            throw new CmsXmlException(Messages.get().container(Messages.ERR_LOCALE_ALREADY_EXISTS_1, destination));
        }
        Iterator i = possibleSources.iterator();
        Locale source = null;
        while (i.hasNext() && (source == null)) {
            // check all locales and try to find the first match
            Locale candidate = (Locale)i.next();
            if (hasLocale(candidate)) {
                // locale has been found
                source = candidate;
            }
        }
        if (source != null) {
            // found a locale, copy this to the destination
            copyLocale(source, destination);
        } else {
            // no matching locale has been found
            throw new CmsXmlException(Messages.get().container(
                Messages.ERR_LOCALE_NOT_AVAILABLE_1,
                CmsLocaleManager.getLocaleNames(possibleSources)));
        }
    }

    /**
     * @see org.opencms.xml.I_CmsXmlDocument#copyLocale(java.util.Locale, java.util.Locale)
     */
    public void copyLocale(Locale source, Locale destination) throws CmsXmlException {

        if (!hasLocale(source)) {
            throw new CmsXmlException(Messages.get().container(Messages.ERR_LOCALE_NOT_AVAILABLE_1, source));
        }
        if (hasLocale(destination)) {
            throw new CmsXmlException(Messages.get().container(Messages.ERR_LOCALE_ALREADY_EXISTS_1, destination));
        }

        Element sourceElement = null;
        Element rootNode = m_document.getRootElement();
        Iterator i = rootNode.elementIterator();
        String localeStr = source.toString();
        while (i.hasNext()) {
            Element element = (Element)i.next();
            String language = element.attributeValue(CmsXmlContentDefinition.XSD_ATTRIBUTE_VALUE_LANGUAGE, null);
            if ((language != null) && (localeStr.equals(language))) {
                // detach node with the locale
                sourceElement = element.createCopy();
                // there can be only one node for the locale
                break;
            }
        }

        if (sourceElement == null) {
            // should not happen since this was checked already, just to make sure...
            throw new CmsXmlException(Messages.get().container(Messages.ERR_LOCALE_NOT_AVAILABLE_1, source));
        }

        // switch locale value in attribute of copied node
        sourceElement.addAttribute(CmsXmlContentDefinition.XSD_ATTRIBUTE_VALUE_LANGUAGE, destination.toString());
        // attach the copied node to the root node
        rootNode.add(sourceElement);

        // re-initialize the document bookmarks
        initDocument(m_document, m_encoding, getContentDefinition());
    }

    /**
     * Corrects the structure of this XML document.<p>
     * 
     * @param cms the current OpenCms user context
     * 
     * @return the file that contains the corrected XML structure
     * 
     * @throws CmsXmlException if something goes wrong
     */
    public CmsFile correctXmlStructure(CmsObject cms) throws CmsXmlException {

        // iterate over all locales
        Iterator i = m_locales.iterator();
        while (i.hasNext()) {
            Locale locale = (Locale)i.next();
            List names = getNames(locale);
            List validValues = new ArrayList();

            // iterate over all nodes per language
            Iterator j = names.iterator();
            while (j.hasNext()) {

                // this step is required for values that need a processing of their content
                // an example for this is the HTML value that does link replacement                
                String name = (String)j.next();
                I_CmsXmlContentValue value = getValue(name, locale);
                if (value.isSimpleType()) {
                    String content = value.getStringValue(cms);
                    value.setStringValue(cms, content);
                }

                // save valid elements for later check
                validValues.add(value);
            }

            if (isAutoCorrectionEnabled()) {
                // full correction of XML

                ArrayList roots = new ArrayList();
                ArrayList rootCds = new ArrayList();
                ArrayList validElements = new ArrayList();

                // gather all XML content definitions and their parent nodes                                
                Iterator it = validValues.iterator();
                while (it.hasNext()) {
                    // collect all root elements, also for the nested content definitions
                    I_CmsXmlContentValue value = (I_CmsXmlContentValue)it.next();
                    Element element = value.getElement();
                    validElements.add(element);
                    if (element.supportsParent()) {
                        // get the parent XML node
                        Element root = element.getParent();
                        if ((root != null) && !roots.contains(root)) {
                            // this is a parent node we do not have already in our storage
                            CmsXmlContentDefinition rcd = value.getContentDefinition();
                            if (rcd != null) {
                                // this value has a valid XML content definition
                                roots.add(root);
                                rootCds.add(rcd);
                            } else {
                                // no valid content definition for the XML value
                                throw new CmsXmlException(Messages.get().container(
                                    Messages.ERR_CORRECT_NO_CONTENT_DEF_3,
                                    value.getName(),
                                    value.getTypeName(),
                                    value.getPath()));
                            }
                        }
                    }
                }

                for (int le = 0; le < roots.size(); le++) {
                    // iterate all XML content root nodes and correct each XML subtree

                    Element root = (Element)roots.get(le);
                    CmsXmlContentDefinition cd = (CmsXmlContentDefinition)rootCds.get(le);

                    // step 1: first sort the nodes according to the schema, this takes care of re-ordered elements
                    List nodeLists = new ArrayList();
                    Iterator is = cd.getTypeSequence().iterator();
                    while (is.hasNext()) {
                        I_CmsXmlSchemaType type = (I_CmsXmlSchemaType)is.next();
                        String name = type.getName();
                        List elements = root.elements(name);
                        if (elements.size() > type.getMaxOccurs()) {
                            // to many nodes of this type appear according to the current schema definition
                            for (int lo = (elements.size() - 1); lo >= type.getMaxOccurs(); lo--) {
                                elements.remove(lo);
                            }
                        }
                        nodeLists.add(elements);
                    }

                    // step 2: clear the list of nodes (this will remove all invalid nodes)
                    List nodeList = root.elements();
                    nodeList.clear();
                    Iterator in = nodeLists.iterator();
                    while (in.hasNext()) {
                        // now add all valid nodes in the right order
                        List elements = (List)in.next();
                        nodeList.addAll(elements);
                    }

                    // step 3: now append the missing elements according to the XML content definition
                    cd.addDefaultXml(cms, this, root, locale);
                }
            }
        }

        // write the modified XML back to the VFS file 
        if (m_file != null) {
            // make sure the file object is available
            m_file.setContents(marshal());
        }
        return m_file;
    }

    /**
     * @see org.opencms.xml.I_CmsXmlDocument#getConversion()
     */
    public String getConversion() {

        return m_conversion;
    }

    /**
     * @see org.opencms.xml.I_CmsXmlDocument#getEncoding()
     */
    public String getEncoding() {

        return m_encoding;
    }

    /**
     * @see org.opencms.xml.I_CmsXmlDocument#getFile()
     */
    public CmsFile getFile() {

        return m_file;
    }

    /**
     * @see org.opencms.xml.I_CmsXmlDocument#getIndexCount(java.lang.String, java.util.Locale)
     */
    public int getIndexCount(String path, Locale locale) {

        List elements = getValues(path, locale);
        if (elements == null) {
            return 0;
        } else {
            return elements.size();
        }
    }

    /**
     * @see org.opencms.xml.I_CmsXmlDocument#getLocales()
     */
    public List getLocales() {

        return new ArrayList(m_locales);
    }

    /**
     * Returns a List of all locales that have the named element set in this document.<p>
     * 
     * If no locale for the given element name is available, an empty list is returned.<p>
     * 
     * @param path the element to look up the locale List for
     * @return a List of all Locales that have the named element set in this document
     */
    public List getLocales(String path) {

        Object result = m_elementLocales.get(CmsXmlUtils.createXpath(path, 1));
        if (result == null) {
            return Collections.EMPTY_LIST;
        }
        return new ArrayList((Set)result);
    }

    /**
     * @see org.opencms.xml.I_CmsXmlDocument#getNames(java.util.Locale)
     */
    public List getNames(Locale locale) {

        Object o = m_elementNames.get(locale);
        if (o != null) {
            return new ArrayList((Set)o);
        }
        return Collections.EMPTY_LIST;
    }

    /**
     * @see org.opencms.xml.I_CmsXmlDocument#getStringValue(org.opencms.file.CmsObject, java.lang.String, java.util.Locale)
     */
    public String getStringValue(CmsObject cms, String path, Locale locale) {

        I_CmsXmlContentValue value = getValueInternal(CmsXmlUtils.createXpath(path, 1), locale);
        if (value != null) {
            return value.getStringValue(cms);
        }
        return null;
    }

    /**
     * @see org.opencms.xml.I_CmsXmlDocument#getStringValue(CmsObject, java.lang.String, Locale, int)
     */
    public String getStringValue(CmsObject cms, String path, Locale locale, int index) {

        // directly calling getValueInternal() is more efficient then calling getStringValue(CmsObject, String, Locale)
        // since the most costs are generated in resolving the xpath name
        I_CmsXmlContentValue value = getValueInternal(CmsXmlUtils.createXpath(path, index + 1), locale);
        if (value != null) {
            return value.getStringValue(cms);
        }
        return null;
    }

    /**
     * @see org.opencms.xml.I_CmsXmlDocument#getValue(java.lang.String, java.util.Locale)
     */
    public I_CmsXmlContentValue getValue(String path, Locale locale) {

        return getValueInternal(CmsXmlUtils.createXpath(path, 1), locale);
    }

    /**
     * @see org.opencms.xml.I_CmsXmlDocument#getValue(java.lang.String, java.util.Locale, int)
     */
    public I_CmsXmlContentValue getValue(String path, Locale locale, int index) {

        return getValueInternal(CmsXmlUtils.createXpath(path, index + 1), locale);
    }

    /**
     * @see org.opencms.xml.I_CmsXmlDocument#getValues(java.util.Locale)
     */
    public List getValues(Locale locale) {

        List result = new ArrayList();

        // bookmarks are stored with the locale as first prefix
        String prefix = '/' + locale.toString() + '/';

        // it's better for performance to iterate through the list of bookmarks directly
        Iterator i = m_bookmarks.keySet().iterator();
        while (i.hasNext()) {
            String key = (String)i.next();
            if (key.startsWith(prefix)) {
                result.add(m_bookmarks.get(key));
            }
        }

        // sort the result
        Collections.sort(result);

        return result;
    }

    /**
     * @see org.opencms.xml.I_CmsXmlDocument#getValues(java.lang.String, java.util.Locale)
     */
    public List getValues(String path, Locale locale) {

        List result = new ArrayList();
        int count = 1;
        Object o;
        String xpath = CmsXmlUtils.createXpath(path, 1);
        xpath = CmsXmlUtils.removeXpathIndex(xpath);
        do {
            String subpath = CmsXmlUtils.createXpathElement(xpath, count);
            o = getBookmark(subpath, locale);
            if (o != null) {
                result.add(o);
                count++;
            }
        } while (o != null);

        return result;
    }

    /**
     * @see org.opencms.xml.I_CmsXmlDocument#hasLocale(java.util.Locale)
     */
    public boolean hasLocale(Locale locale) {

        if (locale == null) {
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_NULL_LOCALE_0));
        }

        return m_locales.contains(locale);
    }

    /**
     * @see org.opencms.xml.I_CmsXmlDocument#hasValue(java.lang.String, java.util.Locale)
     */
    public boolean hasValue(String path, Locale locale) {

        return null != getBookmark(CmsXmlUtils.createXpath(path, 1), locale);
    }

    /**
     * @see org.opencms.xml.I_CmsXmlDocument#hasValue(java.lang.String, java.util.Locale, int)
     */
    public boolean hasValue(String path, Locale locale, int index) {

        return null != getBookmark(CmsXmlUtils.createXpath(path, index + 1), locale);
    }

    /**
     * @see org.opencms.xml.I_CmsXmlDocument#initDocument()
     */
    public void initDocument() {

        initDocument(m_document, m_encoding, getContentDefinition());
    }

    /**
     * @see org.opencms.xml.I_CmsXmlDocument#isEnabled(java.lang.String, java.util.Locale)
     */
    public boolean isEnabled(String path, Locale locale) {

        return hasValue(path, locale);
    }

    /**
     * @see org.opencms.xml.I_CmsXmlDocument#isEnabled(java.lang.String, java.util.Locale, int)
     */
    public boolean isEnabled(String path, Locale locale, int index) {

        return hasValue(path, locale, index);
    }

    /**
     * Marshals (writes) the content of the current XML document 
     * into a byte array using the selected encoding.<p>
     * 
     * @return the content of the current XML document written into a byte array
     * @throws CmsXmlException if something goes wrong
     */
    public byte[] marshal() throws CmsXmlException {

        return ((ByteArrayOutputStream)marshal(new ByteArrayOutputStream(), m_encoding)).toByteArray();
    }

    /**
     * @see org.opencms.xml.I_CmsXmlDocument#moveLocale(java.util.Locale, java.util.Locale)
     */
    public void moveLocale(Locale source, Locale destination) throws CmsXmlException {

        copyLocale(source, destination);
        removeLocale(source);
    }

    /**
     * @see org.opencms.xml.I_CmsXmlDocument#removeLocale(java.util.Locale)
     */
    public void removeLocale(Locale locale) throws CmsXmlException {

        if (!hasLocale(locale)) {
            throw new CmsXmlException(Messages.get().container(Messages.ERR_LOCALE_NOT_AVAILABLE_1, locale));
        }

        Element rootNode = m_document.getRootElement();
        Iterator i = rootNode.elementIterator();
        String localeStr = locale.toString();
        while (i.hasNext()) {
            Element element = (Element)i.next();
            String language = element.attributeValue(CmsXmlContentDefinition.XSD_ATTRIBUTE_VALUE_LANGUAGE, null);
            if ((language != null) && (localeStr.equals(language))) {
                // detach node with the locale
                element.detach();
                // there can be only one node for the locale
                break;
            }
        }

        // re-initialize the document bookmarks
        initDocument(m_document, m_encoding, getContentDefinition());
    }

    /**
     * Sets the content conversion mode for this document.<p>
     * 
     * @param conversion the conversion mode to set for this document
     */
    public void setConversion(String conversion) {

        m_conversion = conversion;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {

        try {
            return CmsXmlUtils.marshal(m_document, m_encoding);
        } catch (CmsXmlException e) {
            throw new CmsRuntimeException(Messages.get().container(Messages.ERR_WRITE_XML_DOC_TO_STRING_0), e);
        }
    }

    /**
     * Validates the XML structure of the document with the DTD or XML schema used by the document.<p>
     * 
     * This is required in case someone modifies the XML structure of a  
     * document using the "edit control code" option.<p>
     * 
     * @param resolver the XML entity resolver to use
     * @throws CmsXmlException if the validation fails
     */
    public void validateXmlStructure(EntityResolver resolver) throws CmsXmlException {

        if (m_file != null) {
            byte[] xmlData = null;
            // file is set, use bytes from file directly
            xmlData = m_file.getContents();
            CmsXmlUtils.validateXmlStructure(xmlData, resolver);
        } else {
            CmsXmlUtils.validateXmlStructure(m_document, m_encoding, resolver);
        }
    }

    /**
     * Adds a bookmark for the given value.<p>
     * 
     * @param path the lookup path to use for the bookmark
     * @param locale the locale to use for the bookmark
     * @param enabled if true, the value is enabled, if false it is disabled
     * @param value the value to bookmark
     */
    protected void addBookmark(String path, Locale locale, boolean enabled, Object value) {

        // add the locale (since the locales are a set adding them more then once does not matter)
        addLocale(locale);

        // add a bookmark to the provided value 
        m_bookmarks.put(getBookmarkName(path, locale), value);

        Object o;
        // update mapping of element name to locale
        if (enabled) {
            // only include enabled elements
            o = m_elementLocales.get(path);
            if (o != null) {
                ((Set)o).add(locale);
            } else {
                Set set = new HashSet();
                set.add(locale);
                m_elementLocales.put(path, set);
            }
        }
        // update mapping of locales to element names
        o = m_elementNames.get(locale);
        if (o != null) {
            ((Set)o).add(path);
        } else {
            Set set = new HashSet();
            set.add(path);
            m_elementNames.put(locale, set);
        }
    }

    /**
     * Adds a locale to the set of locales of the XML document.<p>
     * 
     * @param locale the locale to add
     */
    protected void addLocale(Locale locale) {

        // add the locale to all locales in this dcoument
        m_locales.add(locale);
    }

    /**
     * Clears the XML document bookmarks.<p>
     */
    protected void clearBookmarks() {

        m_bookmarks.clear();
    }

    /**
     * Creates a partial deep element copy according to the set of element paths.<p>
     * Only elements contained in that set will be copied.
     * 
     * @param element the element to copy
     * @param copyElements the set of paths for elements to copy
     * 
     * @return a partial deep copy of <code>element</code>
     * @see org.opencms.xml.A_CmsXmlDocument#createDeepElementCopyInternal(String, Element, Element, Set)
     */
    protected Element createDeepElementCopy(Element element, Set copyElements) {
        
        return createDeepElementCopyInternal(null, null, element, copyElements);
    } 
    
    /**
     * Returns the bookmarked value for the given bookmark,
     * which must be a valid bookmark name. 
     * 
     * Use {@link #getBookmarks()} to get the list of all valid bookmark names.<p>
     * 
     * @param bookmark the bookmark name to look up 
     * @return the bookmarked value for the given bookmark
     */
    protected Object getBookmark(String bookmark) {

        return m_bookmarks.get(bookmark);
    }

    /**
     * Returns the bookmarked value for the given name.<p>
     * 
     * @param path the lookup path to use for the bookmark
     * @param locale the locale to get the bookmark for
     * @return the bookmarked value
     */
    protected Object getBookmark(String path, Locale locale) {

        return m_bookmarks.get(getBookmarkName(path, locale));
    }

    /**
     * Returns the names of all bookmarked elements.<p>
     * 
     * @return the names of all bookmarked elements
     */
    protected Set getBookmarks() {

        return m_bookmarks.keySet();
    }

    /**
     * Initializes an XML document based on the provided document, encoding and content definition.<p>
     * 
     * @param document the base XML document to use for initializing
     * @param encoding the encoding to use when marshalling the document later
     * @param contentDefinition the content definition to use
     */
    protected abstract void initDocument(Document document, String encoding, CmsXmlContentDefinition contentDefinition);

    /**
     * Returns <code>true</code> if the auto correction feature is enabled for saving this XML content.<p>
     * 
     * @return <code>true</code> if the auto correction feature is enabled for saving this XML content
     */
    protected boolean isAutoCorrectionEnabled() {

        // by default, this method always returns false
        return false;
    }

    /**
     * Marshals (writes) the content of the current XML document 
     * into an output stream.<p>
     * 
     * @param out the output stream to write to
     * @param encoding the encoding to use
     * @return the output stream with the XML content
     * @throws CmsXmlException if something goes wrong
     */
    protected OutputStream marshal(OutputStream out, String encoding) throws CmsXmlException {

        return CmsXmlUtils.marshal(m_document, out, encoding);
    }

    /**
     * Removes the bookmark for an element with the given name and locale.<p>
     * 
     * @param path the lookup path to use for the bookmark
     * @param locale the locale of the element
     * @return the element removed from the bookmarks or null
     */
    protected I_CmsXmlContentValue removeBookmark(String path, Locale locale) {

        // remove mapping of element name to locale
        Object o;
        o = m_elementLocales.get(path);
        if (o != null) {
            ((Set)o).remove(locale);
        }
        // remove mapping of locale to element name
        o = m_elementNames.get(locale);
        if (o != null) {
            ((Set)o).remove(path);
        }
        // remove the bookmark and return the removed element
        return (I_CmsXmlContentValue)m_bookmarks.remove(getBookmarkName(path, locale));
    }

    /**
     * Creates a partial deep element copy according to the set of element paths.<p>
     * Only elements contained in that set will be copied.
     * 
     * @param parentPath the path of the parent element or <code>null</code>, initially
     * @param parent the parent element
     * @param element the element to copy
     * @param copyElements the set of paths for elements to copy
     * 
     * @return a partial deep copy of <code>element</code>
     */
    private Element createDeepElementCopyInternal(String parentPath, Element parent, Element element, Set copyElements) {
        
        String elName = element.getName();
        if (parentPath != null) {
            Element first = element.getParent().element(elName);
            int elIndex   = element.getParent().indexOf(element)-first.getParent().indexOf(first)+1;
            elName = parentPath + (parentPath.length() > 0 ? "/" : "") + elName.concat("["+elIndex+"]");
        }
        
        if (parentPath == null || copyElements.contains(elName)) {
            // this is a content element we want to copy
            Element copy = element.createCopy();
            // copy.detach();
            if (parentPath != null) {
                parent.add(copy);
            }
            
            // check if we need to copy subelements, too
            boolean copyNested = (parentPath == null);
            for (Iterator i = copyElements.iterator(); !copyNested && i.hasNext();) {
                String path = (String)i.next();
                copyNested = !elName.equals(path) && path.startsWith(elName);
            }
            
            if (copyNested) {
                copy.clearContent();
                for (Iterator i = element.elementIterator(); i.hasNext();) {
                    Element el = (Element)i.next();
                    createDeepElementCopyInternal((parentPath == null) ? "" : elName, copy, el, copyElements);
                }
            }
            
            return copy;
        } else {   
            return null;
        }
    } 
    
    /**
     * Internal method to look up a value, requires that the name already has been 
     * "normalized" for the bookmark lookup. 
     * 
     * This is required to find names like "title/subtitle" which are stored
     * internally as "title[0]/subtitle[0)" in the bookmarks. 
     * 
     * @param path the path to look up 
     * @param locale the locale to look up
     *  
     * @return the value found in the bookmarks 
     */
    private I_CmsXmlContentValue getValueInternal(String path, Locale locale) {

        Object value = getBookmark(path, locale);
        if (value != null) {
            return (I_CmsXmlContentValue)value;
        }
        return null;
    }
}