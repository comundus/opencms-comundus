/*
 * File   : $Source: /usr/local/cvs/opencms/src/org/opencms/search/CmsSearchSimilarity.java,v $
 * Date   : $Date: 2007-08-13 16:29:59 $
 * Version: $Revision: 1.8 $
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

package org.opencms.search;

import org.opencms.search.fields.CmsSearchField;

import org.apache.lucene.search.DefaultSimilarity;

/**
 * Reduces the importance of the <code>{@link #lengthNorm(String, int)}</code> factor 
 * for the <code>{@link CmsSearchField#FIELD_CONTENT}</code> field, while 
 * keeping the Lucene default for all other fields.<p>
 * 
 * This implementation was added since apparently the default length norm is heavily biased 
 * for small documents. In the default, even if a term is found in 2 documents the same number of 
 * times, the smaller document (containing less terms) will have a score easily 3x as high as 
 * the longer document. Using this implementation the importance of the term number is reduced.<p>
 * 
 * Inspired by Chuck Williams WikipediaSimilarity.<p>
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.8 $ 
 * 
 * @since 6.0.0 
 */
public class CmsSearchSimilarity extends DefaultSimilarity {

    /** Logarithm base 10 used as factor in the score calculations. */
    private static final double LOG10 = Math.log(10.0);

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = 3598754228215079733L;

    /**
     * Creates a new instance of the OpenCms search similarity.<p>
     */
    public CmsSearchSimilarity() {

        super();
    }

    /**
     * Special implementation for "length norm" to reduce the significance of this factor 
     * for the <code>{@link CmsSearchField#FIELD_CONTENT}</code> field, while 
     * keeping the Lucene default for all other fields.<p>
     * 
     * @see org.apache.lucene.search.Similarity#lengthNorm(java.lang.String, int)
     */
    public float lengthNorm(String fieldName, int numTerms) {

        if (fieldName.equals(CmsSearchField.FIELD_CONTENT)) {
            // special length norm for content
            return (float)(3.0 / (Math.log(1000 + numTerms) / LOG10));
        }
        // all other fields use the default Lucene implementation
        return (float)(1.0 / Math.sqrt(numTerms));
    }
}