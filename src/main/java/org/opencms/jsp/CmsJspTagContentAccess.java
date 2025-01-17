/*
 * File   : $Source: /usr/local/cvs/opencms/src/org/opencms/jsp/CmsJspTagContentAccess.java,v $
 * Date   : $Date: 2008-07-01 12:00:39 $
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

package org.opencms.jsp;

import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.jsp.util.CmsJspContentAccessBean;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.I_CmsXmlDocument;

import java.util.Locale;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.Tag;

/**
 * Used to access XML content item information from the current open <code>&lt;cms:contentload&gt;</code> 
 * tag using JSP page context and the JSP EL.<p>
 * 
 * The tag will create an instance of a {@link CmsJspContentAccessBean} that is stored in the selected context.
 * Use the options provided by the bean to access the XML content directly.<p>
 * 
 * For example together with the JSTL, use this tag inside an open tag like this:<pre>
 * &lt;cms:contentload ... &gt;
 *     &lt;cms:contentaccess var="myVarName" scope="page" /&gt;
 *     ... other code ...
 * &lt;/cms:contentload&gt;</pre>
 * 
 * @author  Alexander Kandzior 
 * 
 * @version $Revision: 1.5 $ 
 * 
 * @since 7.0.2
 */
public class CmsJspTagContentAccess extends CmsJspScopedVarBodyTagSuport {

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = -9015874900596113856L;

    /** Locale of the content node element to show. */
    private Locale m_locale;

    /**
     * @see javax.servlet.jsp.tagext.Tag#doEndTag()
     */
    public int doEndTag() {

        if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
            // need to release manually, JSP container may not call release as required (happens with Tomcat)
            release();
        }
        return EVAL_PAGE;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    public int doStartTag() throws JspException {

        // get a reference to the parent "content container" class
        Tag ancestor = findAncestorWithClass(this, I_CmsXmlContentContainer.class);
        if (ancestor == null) {
            CmsMessageContainer errMsgContainer = Messages.get().container(
                Messages.ERR_PARENTLESS_TAG_1,
                "contentaccess");
            String msg = Messages.getLocalizedMessage(errMsgContainer, pageContext);
            throw new JspTagException(msg);
        }
        // get the currently open content container
        I_CmsXmlContentContainer contentContainer = (I_CmsXmlContentContainer)ancestor;

        // get loaded content from content container
        I_CmsXmlDocument xmlContent = contentContainer.getXmlDocument();

        // get the current users OpenCms context
        CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());

        // get the selected Locale or use the default from the OpenCms request context
        Locale locale = m_locale == null ? cms.getRequestContext().getLocale() : m_locale;

        // initialize a new instance of a content access bean
        CmsJspContentAccessBean bean = new CmsJspContentAccessBean(cms, locale, xmlContent);

        // store the content access bean in the selected page context scope
        storeAttribute(bean);

        return SKIP_BODY;
    }

    /**
     * Returns the locale.<p>
     *
     * @return the locale
     */
    public String getLocale() {

        return (m_locale != null) ? m_locale.toString() : "";
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#release()
     */
    public void release() {

        m_locale = null;
        super.release();
    }

    /**
     * Sets the locale.<p>
     *
     * @param locale the locale to set
     */
    public void setLocale(String locale) {

        if (CmsStringUtil.isEmpty(locale)) {
            m_locale = null;
        } else {
            m_locale = CmsLocaleManager.getLocale(locale);
        }
    }
}