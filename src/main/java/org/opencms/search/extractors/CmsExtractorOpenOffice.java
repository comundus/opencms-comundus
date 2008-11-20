/*
 * File   : $Source: /usr/local/cvs/opencms/src/org/opencms/search/extractors/CmsExtractorOpenOffice.java,v $
 * Date   : $Date: 2008-02-27 12:05:31 $
 * Version: $Revision: 1.2 $
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

package org.opencms.search.extractors;

import java.io.InputStream;

import java.util.zip.*;

import java.util.ListIterator;
import org.dom4j.io.SAXReader;
import org.dom4j.Node;

/**
 * Extracts the text from OpenOffice documents (.ods, .odf).<p>
 *
 * @author Dirk Oelkers
 * 
 * @version $Revision: 1.2 $ 
 * 
 * @since 7.0.4  
 */
public final class CmsExtractorOpenOffice extends A_CmsTextExtractor {

    /** Static member instance of the extractor. */
    private static final CmsExtractorOpenOffice INSTANCE = new CmsExtractorOpenOffice();

    /**
     * Hide the public constructor.<p> 
     */
    private CmsExtractorOpenOffice() {

        // noop
    }

    /**
     * Returns an instance of this text extractor.<p> 
     * 
     * @return an instance of this text extractor
     */
    public static I_CmsTextExtractor getExtractor() {

        return INSTANCE;
    }

    /**
     * @see org.opencms.search.extractors.A_CmsTextExtractor#extractText(java.io.InputStream, java.lang.String)
     */
    public I_CmsExtractionResult extractText(InputStream in, String encoding) throws Exception {

        ZipInputStream zin = new ZipInputStream(in);
        ZipEntry ze;
        boolean FOUND_CONTENT = false;
        String result = "";
        while (!FOUND_CONTENT) {
            ze = zin.getNextEntry();
            FOUND_CONTENT = ze.getName().equalsIgnoreCase("content.xml");
            if (FOUND_CONTENT) {
                result = readContent(zin);
                try {
                    zin.close();
                } catch (Exception e) {
                    e.printStackTrace(System.err);
                }
            }
        }
        result = removeControlChars(result);
        return new CmsExtractionResult(result);
    }

    /**
     * Internal routine that parses the specific content.xml part of 
     * an odf document.<p>
     * 
     * @param in the input stream spooled to the start of the content.xml part 
     * 
     * @return the extracted content 
     * 
     * @throws Exception if sth goes wrong
     */
    private String readContent(java.io.InputStream in) throws Exception {

        StringBuffer resultBuffer = new StringBuffer();
        SAXReader reader = new SAXReader();
        org.dom4j.Document doc = reader.read(in);
        java.util.List textlist = doc.selectNodes("//text:p[@*] | //text:span[@*]");
        ListIterator li = textlist.listIterator();
        Node textNode;
        String text;
        while (li.hasNext()) {
            textNode = (Node)li.next();
            text = textNode.getText();
            if (text.length() > 1) {
                text = " " + text + " ";
                resultBuffer.append(text);
            }
        }
        return resultBuffer.toString();
    }

}
