/*
 * File   : $Source: /usr/local/cvs/opencms/src/org/opencms/importexport/CmsImportExportException.java,v $
 * Date   : $Date: 2008-02-27 12:05:48 $
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

package org.opencms.importexport;

import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsException;

/**
 * Signals an error during import or export of OpenCms data.<p>
 * 
 * @author Thomas Weckert  
 * 
 * @version $Revision: 1.10 $ 
 * 
 * @since 6.0.0 
 */
public class CmsImportExportException extends CmsException {

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = -5010535524516515412L;

    /**
     * @see CmsException#CmsException(CmsMessageContainer)
     */
    public CmsImportExportException(CmsMessageContainer container) {

        super(container);
    }

    /**
     * @see CmsException#CmsException(CmsMessageContainer, Throwable)
     */
    public CmsImportExportException(CmsMessageContainer container, Throwable cause) {

        super(container, cause);
    }

    /**
     * @see org.opencms.main.CmsException#createException(org.opencms.i18n.CmsMessageContainer, java.lang.Throwable)
     */
    public CmsException createException(CmsMessageContainer container, Throwable cause) {

        return new CmsImportExportException(container, cause);
    }

}
