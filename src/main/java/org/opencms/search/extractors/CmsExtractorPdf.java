/*
 * File   : $Source: /usr/local/cvs/opencms/src/org/opencms/search/extractors/CmsExtractorPdf.java,v $
 * Date   : $Date: 2008-02-27 12:05:30 $
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

package org.opencms.search.extractors;

import org.opencms.util.CmsStringUtil;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.pdfbox.encryption.DocumentEncryption;
import org.pdfbox.pdfparser.PDFParser;
import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.pdmodel.PDDocumentInformation;
import org.pdfbox.util.PDFTextStripper;

/**
 * Extracts the text from a PDF document.<p>
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.13 $ 
 * 
 * @since 6.0.0 
 */
public final class CmsExtractorPdf extends A_CmsTextExtractor {

    /** Static member instance of the extractor. */
    private static final CmsExtractorPdf INSTANCE = new CmsExtractorPdf();

    /**
     * Hide the public constructor.<p> 
     */
    private CmsExtractorPdf() {

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
     * @see org.opencms.search.extractors.I_CmsTextExtractor#extractText(java.io.InputStream, java.lang.String)
     */
    public I_CmsExtractionResult extractText(InputStream in, String encoding) throws Exception {

        PDDocument pdfDocument = null;

        try {
            PDFParser parser = new PDFParser(in);
            parser.parse();

            pdfDocument = parser.getPDDocument();

            // check for encryption
            if (pdfDocument.isEncrypted()) {
                DocumentEncryption decryptor = new DocumentEncryption(pdfDocument);
                // try using the default password
                decryptor.decryptDocument("");
            }

            // create PDF stripper
            PDFTextStripper stripper = new PDFTextStripper();
            PDDocumentInformation info = pdfDocument.getDocumentInformation();
            Map contentItems = new HashMap();

            // add the main document text
            String result = stripper.getText(pdfDocument);
            StringBuffer content = new StringBuffer(result);
            if (CmsStringUtil.isNotEmpty(result)) {
                contentItems.put(I_CmsExtractionResult.ITEM_RAW, result);
            }

            // append document meta data as content items
            combineContentItem(info.getTitle(), I_CmsExtractionResult.ITEM_TITLE, content, contentItems);
            combineContentItem(info.getKeywords(), I_CmsExtractionResult.ITEM_KEYWORDS, content, contentItems);
            combineContentItem(info.getSubject(), I_CmsExtractionResult.ITEM_SUBJECT, content, contentItems);
            combineContentItem(info.getAuthor(), I_CmsExtractionResult.ITEM_AUTHOR, content, contentItems);
            combineContentItem(info.getCreator(), I_CmsExtractionResult.ITEM_CREATOR, content, contentItems);
            combineContentItem(info.getProducer(), I_CmsExtractionResult.ITEM_PRODUCER, content, contentItems);

            // free some memory
            stripper = null;
            info = null;

            // return the final result
            return new CmsExtractionResult(content.toString(), contentItems);

        } finally {
            if (pdfDocument != null) {
                pdfDocument.close();
            }
        }
    }
}