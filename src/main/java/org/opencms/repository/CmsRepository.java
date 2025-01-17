/*
 * File   : $Source: /usr/local/cvs/opencms/src/org/opencms/repository/CmsRepository.java,v $
 * Date   : $Date: 2008-02-27 12:05:47 $
 * Version: $Revision: 1.6 $
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

package org.opencms.repository;

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsObject;
import org.opencms.file.wrapper.CmsObjectWrapper;
import org.opencms.file.wrapper.I_CmsResourceWrapper;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;

/**
 * Creates a repository session to access OpenCms.<p>
 * 
 * The configuration of the used {@link I_CmsResourceWrapper} is done here.
 * This is the main class to get access to the resources in the VFS of 
 * OpenCms. The method {@link #login(String, String)} logs in to OpenCms
 * and returns a {@link CmsRepositorySession} to use for basic file and
 * folder operations.<p>
 * 
 * The project and the site to use for the access to OpenCms is read out
 * of the user settings.<p>
 * 
 * @see CmsObjectWrapper
 * 
 * @author Peter Bonrad
 * 
 * @version $Revision: 1.6 $
 * 
 * @since 6.5.6
 */
public class CmsRepository extends A_CmsRepository {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsRepository.class);

    /** The name of the parameter of the configuration. */
    private static final String PARAM_WRAPPER = "wrapper";

    /** The list of configured wrappers of the repository. */
    private List m_wrappers;

    /**
     * Empty default constructor.<p>
     */
    public CmsRepository() {

        super();
        m_wrappers = new ArrayList();
    }

    /**
     * @see org.opencms.repository.A_CmsRepository#initConfiguration()
     */
    public void initConfiguration() throws CmsConfigurationException {

        if (getConfiguration().containsKey(PARAM_WRAPPER)) {
            String[] wrappers = (String[])getConfiguration().get(PARAM_WRAPPER);

            for (int i = 0; i < wrappers.length; i++) {

                String classname = wrappers[i].trim();
                Class nameClazz;

                // init class for wrapper
                try {
                    nameClazz = Class.forName(classname);
                } catch (ClassNotFoundException e) {
                    LOG.error(Messages.get().getBundle().key(Messages.LOG_WRAPPER_CLASS_NOT_FOUND_1, classname), e);
                    return;
                }

                I_CmsResourceWrapper wrapper;
                try {
                    wrapper = (I_CmsResourceWrapper)nameClazz.newInstance();
                } catch (InstantiationException e) {
                    throw new CmsConfigurationException(Messages.get().container(
                        Messages.ERR_INVALID_WRAPPER_NAME_1,
                        classname));
                } catch (IllegalAccessException e) {
                    throw new CmsConfigurationException(Messages.get().container(
                        Messages.ERR_INVALID_WRAPPER_NAME_1,
                        classname));
                } catch (ClassCastException e) {
                    throw new CmsConfigurationException(Messages.get().container(
                        Messages.ERR_INVALID_WRAPPER_NAME_1,
                        classname));
                }

                m_wrappers.add(wrapper);

                if (CmsLog.INIT.isInfoEnabled()) {
                    CmsLog.INIT.info(Messages.get().getBundle().key(
                        Messages.INIT_ADD_WRAPPER_1,
                        wrapper.getClass().getName()));
                }
            }
        }

        m_wrappers = Collections.unmodifiableList(m_wrappers);

        super.initConfiguration();
    }

    /**
     * @see org.opencms.repository.I_CmsRepository#login(java.lang.String, java.lang.String)
     */
    public I_CmsRepositorySession login(String userName, String password) throws CmsException {

        CmsObject cms;
        cms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserGuest());
        cms.loginUser(userName, password);

        CmsUserSettings settings = new CmsUserSettings(cms);

        cms.getRequestContext().setSiteRoot(settings.getStartSite());
        cms.getRequestContext().setCurrentProject(cms.readProject(settings.getStartProject()));

        // set the object wrapper as an attribute in the request context, so that it can be
        // used everywhere a CmsObject is accessible.
        CmsObjectWrapper objWrapper = new CmsObjectWrapper(cms, m_wrappers);
        cms.getRequestContext().setAttribute(CmsObjectWrapper.ATTRIBUTE_NAME, objWrapper);

        return new CmsRepositorySession(objWrapper, getFilter());
    }

}
