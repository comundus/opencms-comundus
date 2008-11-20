/*
 * File   : $Source: /usr/local/cvs/opencms/src/org/opencms/workplace/list/I_CmsSearchMethod.java,v $
 * Date   : $Date: 2008-02-27 12:05:28 $
 * Version: $Revision: 1.7 $
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

package org.opencms.workplace.list;

import java.util.List;

/**
 * Defines a search action, that is a list action with an additional method for filtering.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.7 $ 
 * 
 * @since 6.0.0 
 */
public interface I_CmsSearchMethod extends I_CmsListAction {

    /**
     * Returns a sublist of the given items, that match the filter string.<p>
     * 
     * @param items the items to filter
     * @param filter the filter string
     * 
     * @return the filtered sublist
     */
    List filter(List items, String filter);
}
