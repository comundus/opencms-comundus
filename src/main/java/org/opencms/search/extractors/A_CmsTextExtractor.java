/*
 * File   : $Source: /usr/local/cvs/opencms/src/org/opencms/search/extractors/A_CmsTextExtractor.java,v $
 * Date   : $Date: 2008-02-27 12:05:31 $
 * Version: $Revision: 1.11 $
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

import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Base utility class that allows extraction of the indexable "plain" text from a given document format.<p>
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.11 $ 
 * 
 * @since 6.0.0 
 */
public abstract class A_CmsTextExtractor implements I_CmsTextExtractor {

    /** A buffer in case the input stream must be read more then once. */
    protected byte[] m_inputBuffer;

    /**
     * @see org.opencms.search.extractors.I_CmsTextExtractor#extractText(byte[])
     */
    public I_CmsExtractionResult extractText(byte[] content) throws Exception {

        // encoding is null        
        return extractText(content, null);
    }

    /**
     * @see org.opencms.search.extractors.I_CmsTextExtractor#extractText(byte[], java.lang.String)
     */
    public I_CmsExtractionResult extractText(byte[] content, String encoding) throws Exception {

        // call stream based method of extraction
        m_inputBuffer = content;
        return extractText(new ByteArrayInputStream(content), encoding);
    }

    /**
     * @see org.opencms.search.extractors.I_CmsTextExtractor#extractText(java.io.InputStream)
     */
    public I_CmsExtractionResult extractText(InputStream in) throws Exception {

        // encoding is null        
        return extractText(in, null);
    }

    /**
     * @see org.opencms.search.extractors.I_CmsTextExtractor#extractText(java.io.InputStream, java.lang.String)
     */
    public I_CmsExtractionResult extractText(InputStream in, String encoding) throws Exception {

        // read the byte content
        byte[] text = CmsFileUtil.readFully(in);
        // call byte array based method of extraction
        return extractText(text, encoding);
    }

    /**
     * Creates a copy of the original input stream, which allows to read the input stream more then 
     * once, required for certain document types.<p>
     * 
     * @param in the inpur stram to copy
     * @return a copy of the original input stream
     * @throws IOException in case of read errors from the original input stream
     */
    public InputStream getStreamCopy(InputStream in) throws IOException {

        if (m_inputBuffer != null) {
            return new ByteArrayInputStream(m_inputBuffer);
        }

        // read the input stream fully and copy it to a byte array
        m_inputBuffer = CmsFileUtil.readFully(in);

        // now return a reader from the byte array
        return new ByteArrayInputStream(m_inputBuffer);
    }

    /**
     * Combines a meta information item extracted from the document with the main content buffer and 
     * also stores the individual information as item in the Map of content items.<p>
     * 
     * @param itemValue the value of the item to store
     * @param itemKey the key in the Map of content items
     * @param content a buffer where to append the content item
     * @param contentItems the Map of individual content items
     */
    protected void combineContentItem(String itemValue, String itemKey, StringBuffer content, Map contentItems) {

        if (CmsStringUtil.isNotEmpty(itemValue)) {
            contentItems.put(itemKey, itemValue);
            content.append('\n');
            content.append(itemValue);
        }
    }

    /**
     * Removes "unwanted" control chars from the given content.<p>
     * 
     * @param content the content to remove the unwanted control chars from
     * 
     * @return the content with the unwanted control chars removed
     */
    protected String removeControlChars(String content) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(content)) {
            // to avoid later null pointer exceptions an empty String is returned
            return "";
        }

        char[] chars = content.toCharArray();
        StringBuffer result = new StringBuffer(chars.length);
        boolean wasUnwanted = false;
        for (int i = 0; i < chars.length; i++) {
            char ch = chars[i];

            int type = Character.getType(ch);
            switch (type) {

                // punctuation
                case Character.CURRENCY_SYMBOL:
                case Character.CONNECTOR_PUNCTUATION:
                case Character.FINAL_QUOTE_PUNCTUATION:
                case Character.INITIAL_QUOTE_PUNCTUATION:
                case Character.DASH_PUNCTUATION:
                case Character.START_PUNCTUATION:
                case Character.END_PUNCTUATION:
                case Character.OTHER_PUNCTUATION:
                    // letters
                case Character.OTHER_LETTER:
                case Character.MODIFIER_LETTER:
                case Character.UPPERCASE_LETTER:
                case Character.TITLECASE_LETTER:
                case Character.LOWERCASE_LETTER:
                    // digits
                case Character.DECIMAL_DIGIT_NUMBER:
                    // spaces
                case Character.SPACE_SEPARATOR:
                    result.append(ch);
                    wasUnwanted = false;
                    break;

                // line separators
                case Character.LINE_SEPARATOR:
                    result.append('\n');
                    wasUnwanted = true;
                    break;

                // symbols
                case Character.MATH_SYMBOL:
                case Character.OTHER_SYMBOL:
                    // other stuff:
                case Character.CONTROL:
                case Character.COMBINING_SPACING_MARK:
                case Character.ENCLOSING_MARK:
                case Character.FORMAT:
                case Character.LETTER_NUMBER:
                case Character.MODIFIER_SYMBOL:
                case Character.NON_SPACING_MARK:
                case Character.PARAGRAPH_SEPARATOR:
                case Character.PRIVATE_USE:
                case Character.SURROGATE:
                case Character.UNASSIGNED:
                case Character.OTHER_NUMBER:
                default:
                    if (!wasUnwanted) {
                        result.append('\n');
                        wasUnwanted = true;
                    }
            }
        }

        return result.toString();
    }
}