/*
 * File   : $Source: /usr/local/cvs/opencms/src/org/opencms/file/history/CmsHistoryProject.java,v $
 * Date   : $Date: 2008-02-27 12:05:25 $
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

package org.opencms.file.history;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.main.CmsException;
import org.opencms.security.CmsPrincipal;
import org.opencms.util.CmsUUID;

import java.util.List;

/**
 * Describes an OpenCms historical project entry.<p>
 *
 * @author Alexander Kandzior 
 * @author Michael Moossen
 *
 * @version $Revision: 1.4 $
 * 
 * @since 6.9.1
 */
public class CmsHistoryProject extends CmsProject implements Cloneable {

    /** The publishing date of this project. */
    private long m_datePublished;

    /** The resources belonging to the project. */
    private List m_projectResources;

    /** The publish tag of the published project. */
    private int m_publishTag;

    /** The user id of the publisher. */
    private CmsUUID m_userPublished;

    /**
     * Creates a new CmsHistoryProject.<p>
     * 
     * @param publishTag the version id for this historical project
     * @param projectId the id to use for this project
     * @param name the name for this project
     * @param description the description for this project
     * @param ownerId the owner id for this project
     * @param groupId the group id for this project
     * @param managerGroupId the manager group id for this project
     * @param dateCreated the creation date of this project
     * @param type the type of this project
     * @param datePublished the date this backup project was published
     * @param userPublished the id of the user who published
     * @param projectResources a list of resources that are the project "view"
     */
    public CmsHistoryProject(
        int publishTag,
        CmsUUID projectId,
        String name,
        String description,
        CmsUUID ownerId,
        CmsUUID groupId,
        CmsUUID managerGroupId,
        long dateCreated,
        CmsProjectType type,
        long datePublished,
        CmsUUID userPublished,
        List projectResources) {

        super(projectId, name, description, ownerId, groupId, managerGroupId, 0, dateCreated, type);

        m_publishTag = publishTag;
        m_datePublished = datePublished;
        m_userPublished = userPublished;
        m_projectResources = projectResources;
    }

    /**
     * @see org.opencms.file.CmsProject#clone()
     */
    public Object clone() {

        return new CmsHistoryProject(
            m_publishTag,
            getUuid(),
            getName(),
            getDescription(),
            getOwnerId(),
            getGroupId(),
            getManagerGroupId(),
            getDateCreated(),
            getType(),
            m_datePublished,
            m_userPublished,
            m_projectResources);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (obj instanceof CmsHistoryProject) {
            return ((CmsHistoryProject)obj).getUuid().equals(getUuid());
        }
        return false;
    }

    /**
     * Returns the project manager group name.<p>
     *
     * @param cms the current cms context 
     *
     * @return the projects manager group name
     */
    public String getGroupManagersName(CmsObject cms) {

        try {
            return CmsPrincipal.readPrincipalIncludingHistory(cms, getManagerGroupId()).getName();
        } catch (CmsException e) {
            return getManagerGroupId().toString();
        }
    }

    /**
     * Returns the projects user group name.<p>
     * 
     * @return the projects user group name
     * 
     * @deprecated use {@link #getGroupUsersName(CmsObject)} instead
     */
    public String getGroupName() {

        return getGroupId().toString();
    }

    /**
     * Returns the projects user group name.<p>
     * 
     * @param cms the current cms context 
     *
     * @return the projects user group name
     */
    public String getGroupUsersName(CmsObject cms) {

        try {
            return CmsPrincipal.readPrincipalIncludingHistory(cms, getGroupId()).getName();
        } catch (CmsException e) {
            return getGroupId().toString();
        }
    }

    /**
     * Returns the project manager group name.<p>
     * 
     * @return the projects manager group name
     * 
     * @deprecated use {@link #getGroupManagersName(CmsObject)} instead
     */
    public String getManagerGroupName() {

        return getManagerGroupId().toString();
    }

    /**
     * Gets the ownername.
     *
     * @return the ownername
     * 
     * @deprecated use {@link #getOwnerName(CmsObject)} instead
     */
    public String getOwnerName() {

        return getOwnerId().toString();
    }

    /**
     * Returns the owner name.<p>
     *
     * @param cms the current cms context 
     *
     * @return the owner name
     */
    public String getOwnerName(CmsObject cms) {

        try {
            return CmsPrincipal.readPrincipalIncludingHistory(cms, getOwnerId()).getName();
        } catch (CmsException e) {
            return getOwnerId().toString();
        }
    }

    /**
     * Returns the project resources (i.e. the "view" of the project).<p>
     * 
     * @return the project resources 
     */
    public List getProjectResources() {

        return m_projectResources;
    }

    /**
     * Returns the id of the user that published this project.<p>
     *
     * @return the id of the user that published this project
     */
    public CmsUUID getPublishedBy() {

        return m_userPublished;
    }

    /**
     * Gets the publishers name.
     *
     * @return the publishers name
     * 
     * @deprecated use {@link #getPublishedByName(CmsObject)} instead
     */
    public String getPublishedByName() {

        return getPublishedBy().toString();
    }

    /**
     * Returns the publishers name.<p>
     *
     * @param cms the current cms context 
     *
     * @return the publishers name
     */
    public String getPublishedByName(CmsObject cms) {

        try {
            return CmsPrincipal.readPrincipalIncludingHistory(cms, getPublishedBy()).getName();
        } catch (CmsException e) {
            return getPublishedBy().toString();
        }
    }

    /**
     * Returns the publishing date of this project.<p>
     *
     * @return the publishing date of this project
     */
    public long getPublishingDate() {

        return m_datePublished;
    }

    /**
     * Returns the publish tag.<p>
     *
     * @return the publish tag
     */
    public int getPublishTag() {

        return m_publishTag;
    }

    /**
     * Gets the versionId.
     *
     * @return the versionId
     * 
     * @deprecated Use {@link #getPublishTag()} instead
     */
    public int getVersionId() {

        return getPublishTag();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {

        return (new Long(m_datePublished)).hashCode();
    }
}