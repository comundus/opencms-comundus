/*
 * File   : $Source: /usr/local/cvs/opencms/src/org/opencms/workplace/editors/directedit/I_CmsDirectEditProvider.java,v $
 * Date   : $Date: 2008-02-27 12:05:54 $
 * Version: $Revision: 1.5 $
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
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.editors.directedit;

import org.opencms.configuration.I_CmsConfigurationParameterHandler;
import org.opencms.file.CmsObject;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * Provides the methods to generate the "direct edit" HTML fragments that are inserted 
 * in the generated pages in offline mode.<p>
 * 
 * In case you want to implement this, it's a good idea to extend from {@link A_CmsDirectEditProvider}
 * or {@link CmsDirectEditDefaultProvider} as these already contain the required low level logic.<p>
 * 
 * The default direct edit provider used can be configured in <code>opencms-workplace.xml</code> in the 
 * <code>&lt;directeditprovider class="..." /&gt;</code> node. The standard provider is the
 * {@link CmsDirectEditDefaultProvider}.<p>
 * 
 * @author Alexander Kandzior
 * 
 * @version $Revision: 1.5 $ 
 * 
 * @since 6.2.3 
 * 
 * @see CmsDirectEditDefaultProvider
 * @see CmsDirectEditTextButtonProvider
 * @see CmsDirectEditJspIncludeProvider
 * @see org.opencms.jsp.CmsJspTagEditable
 */
public interface I_CmsDirectEditProvider extends I_CmsConfigurationParameterHandler, Cloneable {

    /** Key to identify the direct edit provider instance. */
    String ATTRIBUTE_DIRECT_EDIT_PROVIDER = "org.opencms.workplace.editors.directedit.__directEditProvider";

    /** Key to identify the direct edit provider parameteres. */
    String ATTRIBUTE_DIRECT_EDIT_PROVIDER_PARAMS = "org.opencms.workplace.editors.directedit.__directEditProviderParams";

    /**
     * Initialize method for a new instance of the direct edit provider.<p>
     * 
     * @param cms the current users OpenCms context
     * @param mode the direct edit mode to use
     * @param fileName link to a file that contains the direct edit HTML elements (optional)
     */
    void init(CmsObject cms, CmsDirectEditMode mode, String fileName);

    /**
     * Inserts the "end direct edit" HTML in the provided JSP page context.<p>
     * 
     * @param context the JSP page context to insert the HTML to
     * 
     * @throws JspException in case something goes wrong
     */
    void insertDirectEditEnd(PageContext context) throws JspException;

    /**
     * Inserts the "direct edit header" HTML in the provided JSP page context.<p>
     * 
     * @param context the JSP page context to insert the HTML to
     * @param params the parameters for the direct edit includes
     * 
     * @throws JspException in case something goes wrong
     */
    void insertDirectEditIncludes(PageContext context, CmsDirectEditParams params) throws JspException;

    /**
     * Inserts the "start direct edit" HTML in the provided JSP page context.<p>
     * 
     * @param context the JSP page context to insert the HTML to
     * @param params the parameters for the direct edit call
     * 
     * @return <code>true</code> in case a direct edit element was opened, <code>false</code> otherwise
     * 
     * @throws JspException in case something goes wrong
     */
    boolean insertDirectEditStart(PageContext context, CmsDirectEditParams params) throws JspException;

    /**
     * Returns <code>true</code> if this provider (currently) operates in manual mode.<p>
     * 
     * In manual mode the direct edit HTML is inserted with <code>&lt;cms:enditable mode="manual" /&gt</code>
     * tags. Otherwise the direct edit HTML is automatically inserted in the current page.<p>
     * 
     * Some providers may not be able to operate in manual mode. These will always return <code>false</code>.<p>
     * 
     * @param mode the mode of the current direct edit element
     * 
     * @return <code>true</code> if this provider (currently) operates in manual mode
     */
    boolean isManual(CmsDirectEditMode mode);

    /**
     * Creates a new instance of this direct edit provider with the same basic configuration.<p>
     * 
     * @return a new instance of this direct edit provider with the same basic configuration
     */
    I_CmsDirectEditProvider newInstance();
}