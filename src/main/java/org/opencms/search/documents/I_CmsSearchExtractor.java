/*
 * File   : $Source: /usr/local/cvs/opencms/src/org/opencms/search/documents/I_CmsSearchExtractor.java,v $
 * Date   : $Date: 2008-02-27 12:05:21 $
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

package org.opencms.search.documents;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.search.CmsSearchIndex;
import org.opencms.search.extractors.I_CmsExtractionResult;

/**
 * Defines a text extractor for the integrated search engine.<p>
 * 
 * The job of a search extractor is to extract indexable plain text from 
 * a resource in the OpenCms VFS. This may be from the resource content, for example 
 * from a PDF file, or from the resource properties, for example the Title, Keywords and 
 * Description properties.<p>
 * 
 * @author Carsten Weinholz 
 * 
 * @version $Revision: 1.8 $ 
 * 
 * @since 6.0.0 
 */
public interface I_CmsSearchExtractor {

    /**
     * Extractes the content of a given index resource according to the resource file type and the 
     * configuration of the given index.<p>
     * 
     * @param cms the cms object
     * @param resource the resource to extract the content from
     * @param index the index to extract the content for
     * 
     * @return the extracted content of the resource
     * 
     * @throws CmsException if somethin goes wrong
     */
    I_CmsExtractionResult extractContent(CmsObject cms, CmsResource resource, CmsSearchIndex index) throws CmsException;

}