/*
 * File   : $Source: /usr/local/cvs/opencms/src/org/opencms/file/types/A_CmsResourceTypeLinkParseable.java,v $
 * Date   : $Date: 2008-02-27 12:05:45 $
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

package org.opencms.file.types;

import org.opencms.relations.I_CmsLinkParseable;

/**
 * Base implementation for resource types implementing the {@link I_CmsLinkParseable} interface.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.4 $ 
 * 
 * @since 6.5.0 
 */
public abstract class A_CmsResourceTypeLinkParseable extends A_CmsResourceType implements I_CmsLinkParseable {

    /**
     * Default constructor.<p>
     */
    public A_CmsResourceTypeLinkParseable() {

        super();
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#isDirectEditable()
     */
    public boolean isDirectEditable() {

        return true;
    }
}