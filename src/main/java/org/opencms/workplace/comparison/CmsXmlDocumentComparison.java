/*
 * File   : $Source: /usr/local/cvs/opencms/src/org/opencms/workplace/comparison/CmsXmlDocumentComparison.java,v $
 * Date   : $Date: 2008-02-27 12:05:55 $
 * Version: $Revision: 1.8 $
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

package org.opencms.workplace.comparison;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.types.CmsResourceTypeXmlPage;
import org.opencms.main.CmsException;
import org.opencms.xml.I_CmsXmlDocument;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.content.I_CmsXmlContentValueVisitor;
import org.opencms.xml.page.CmsXmlPageFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * A comparison of properties, attributes and elements of xml documents.<p>
 * 
 * @author Jan Baudisch
 */
public class CmsXmlDocumentComparison extends CmsResourceComparison {

    /**
     * Visitor that collects the xpath expressions of xml contents.<p>
     */
    static class CmsXmlContentElementPathExtractor implements I_CmsXmlContentValueVisitor {

        /** The paths to the elements in the xml content. */
        private List m_elementPaths;

        /**
         * Creates a CmsXmlContentElementPathExtractor.<p>
         */
        CmsXmlContentElementPathExtractor() {

            m_elementPaths = new ArrayList();
        }

        /**
         * 
         * @see org.opencms.xml.content.I_CmsXmlContentValueVisitor#visit(org.opencms.xml.types.I_CmsXmlContentValue)
         */
        public void visit(I_CmsXmlContentValue value) {

            // only add simple types
            if (value.isSimpleType()) {
                m_elementPaths.add(new CmsXmlContentElementComparison(
                    value.getLocale(),
                    value.getPath(),
                    value.getTypeName()));
            }
        }

        /**
         * Returns the elementPaths.<p>
         *
         * @return the elementPaths
         */
        List getElementPaths() {

            return m_elementPaths;
        }
    }

    /** The compared elements.<p> */
    private List m_elements;

    /**
     * Creates a new xml document comparison.<p>
     * 
     * @param cms the CmsObject to use
     * @param res1 the first file to compare
     * @param res2 the second file to compare
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsXmlDocumentComparison(CmsObject cms, CmsFile res1, CmsFile res2)
    throws CmsException {

        I_CmsXmlDocument resource1;
        I_CmsXmlDocument resource2;

        List elements1 = null;
        List elements2 = null;

        if (CmsResourceTypeXmlPage.isXmlPage(res1) && CmsResourceTypeXmlPage.isXmlPage(res2)) {
            resource1 = CmsXmlPageFactory.unmarshal(cms, res1);
            resource2 = CmsXmlPageFactory.unmarshal(cms, res2);
            elements1 = getElements(resource1);
            elements2 = getElements(resource2);
        } else {
            resource1 = CmsXmlContentFactory.unmarshal(cms, res1);
            CmsXmlContentElementPathExtractor visitor = new CmsXmlContentElementPathExtractor();
            ((CmsXmlContent)resource1).visitAllValuesWith(visitor);
            elements1 = visitor.getElementPaths();
            resource2 = CmsXmlContentFactory.unmarshal(cms, res2);
            visitor = new CmsXmlContentElementPathExtractor();
            ((CmsXmlContent)resource2).visitAllValuesWith(visitor);
            elements2 = visitor.getElementPaths();
        }

        List removed = new ArrayList(elements1);
        removed.removeAll(elements2);
        Iterator i = removed.iterator();
        while (i.hasNext()) {
            CmsElementComparison elem = (CmsElementComparison)i.next();
            elem.setStatus(CmsResourceComparison.TYPE_REMOVED);
            String value = resource1.getValue(elem.getName(), elem.getLocale()).getStringValue(cms);
            elem.setVersion1(value);
            elem.setVersion2("");
        }
        List added = new ArrayList(elements2);
        added.removeAll(elements1);
        i = added.iterator();
        while (i.hasNext()) {
            CmsElementComparison elem = (CmsElementComparison)i.next();
            elem.setStatus(CmsResourceComparison.TYPE_ADDED);
            elem.setVersion1("");
            I_CmsXmlContentValue contentValue = resource2.getValue(elem.getName(), elem.getLocale());
            String value = contentValue.getStringValue(cms);
            elem.setVersion2(value);
        }
        List union = new ArrayList(elements1);
        union.retainAll(elements2);

        // find out, which elements were changed
        i = new ArrayList(union).iterator();
        while (i.hasNext()) {
            CmsElementComparison elem = (CmsElementComparison)i.next();
            String value1 = resource1.getValue(elem.getName(), elem.getLocale()).getStringValue(cms);
            String value2 = resource2.getValue(elem.getName(), elem.getLocale()).getStringValue(cms);
            if (value1 == null) {
                value1 = "";
            }
            if (value2 == null) {
                value2 = "";
            }
            elem.setVersion1(value1);
            elem.setVersion2(value2);
            if (!value1.equals(value2)) {
                elem.setStatus(CmsResourceComparison.TYPE_CHANGED);
            } else {
                elem.setStatus(CmsResourceComparison.TYPE_UNCHANGED);
            }
        }
        m_elements = new ArrayList(removed);
        m_elements.addAll(added);
        m_elements.addAll(union);
        Collections.sort(m_elements);
    }

    /**
     * Returns the elements.<p>
     *
     * @return the elements
     */
    public List getElements() {

        if (m_elements == null) {
            return Collections.EMPTY_LIST;
        }
        return Collections.unmodifiableList(m_elements);
    }

    /** Returs a list of all element names of a xml page.<p>
     * 
     * @param xmlPage the xml page to read the element names from
     * @return a list of all element names of a xml page
     */
    private List getElements(I_CmsXmlDocument xmlPage) {

        List elements = new ArrayList();
        Iterator locales = xmlPage.getLocales().iterator();
        while (locales.hasNext()) {
            Locale locale = (Locale)locales.next();
            Iterator elementNames = xmlPage.getNames(locale).iterator();
            while (elementNames.hasNext()) {
                String elementName = (String)elementNames.next();
                elements.add(new CmsElementComparison(locale, elementName));
            }
        }
        return elements;
    }
}
