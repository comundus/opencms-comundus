/*
 * File   : $Source: /usr/local/cvs/opencms/src/org/opencms/search/extractors/CmsExtractorHtml.java,v $
 * Date   : $Date: 2008-02-27 12:05:31 $
 * Version: $Revision: 1.10 $
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

import org.opencms.main.OpenCms;
import org.opencms.util.CmsHtmlExtractor;
import org.opencms.util.CmsStringUtil;

import java.io.InputStream;

/**
 * Extracts the text from an HTML document.<p>
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.10 $ 
 * 
 * @since 6.0.0 
 */
public final class CmsExtractorHtml extends A_CmsTextExtractor {

    /** Static member instance of the extractor. */
    private static final CmsExtractorHtml INSTANCE = new CmsExtractorHtml();

    /**
     * Hide the public constructor.<p> 
     */
    private CmsExtractorHtml() {

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

        if (CmsStringUtil.isEmpty(encoding)) {
            encoding = OpenCms.getSystemInfo().getDefaultEncoding();
        }

        String result = CmsHtmlExtractor.extractText(in, encoding);
        result = removeControlChars(result);
        return new CmsExtractionResult(result);
    }
}