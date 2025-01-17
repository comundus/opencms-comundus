/*
 * File   : $Source: /usr/local/cvs/opencms/src/org/opencms/newsletter/CmsNewsletterContentType.java,v $
 * Date   : $Date: 2008-02-27 12:05:33 $
 * Version: $Revision: 1.4 $
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

package org.opencms.newsletter;

/**
 * Type definition class for email content types.<p>
 * 
 * @author Alexander Kandzior
 * 
 * @version $Revision: 1.4 $
 * 
 * @since 6.2.0
 */
public final class CmsNewsletterContentType {

    /** Content type definition for HTML. */
    public static final CmsNewsletterContentType TYPE_HTML = new CmsNewsletterContentType("html");

    /** Content type definition for plain text. */
    public static final CmsNewsletterContentType TYPE_TEXT = new CmsNewsletterContentType("text");

    /** The name of this type. */
    private String m_typeName;

    /**
     * Creates a new newsletter content type.<p>
     * 
     * @param typeName the type name to use
     */
    private CmsNewsletterContentType(String typeName) {

        m_typeName = typeName;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {

        return m_typeName;
    }
}