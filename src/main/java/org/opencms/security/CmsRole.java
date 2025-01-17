/*
 * File   : $Source: /usr/local/cvs/opencms/src/org/opencms/security/CmsRole.java,v $
 * Date   : $Date: 2008-02-27 12:05:29 $
 * Version: $Revision: 1.19 $
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

package org.opencms.security;

import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * A role is used in the OpenCms security system to check if a user has access to a certain system function.<p>
 * 
 * Roles are used to ensure access permissions to system function that are not file based. 
 * For example, roles are used to check permissions to functions like "the user can schedule a 
 * job in the <code>{@link org.opencms.scheduler.CmsScheduleManager}</code>" or "the user can export (or import) 
 * the OpenCms database".<p>
 * 
 * All roles are based on <code>{@link org.opencms.file.CmsGroup}</code>. This means to have access to a role,
 * the user has to be a member in a certain predefined system group. Each role has exactly one group that
 * contains all "direct" members of this role.<p>
 * 
 * All roles have (optional) parent roles. If a user not a member of the role group of a role, but he is
 * a member of at last one of the parent role groups, he/she also has full access to this role. This is called 
 * "indirect" membership to the role.<p>
 * 
 * Please note that "indirect" membership does grant the user the same full access to a role that "direct" 
 * membership does. For example, the <code>{@link #ROOT_ADMIN}</code> role is a parent group of all other roles. 
 * So all users that are members of <code>{@link #ROOT_ADMIN}</code> have access to the functions of all other roles.<p>
 * 
 * Please do not perform automated sorting of members on this compilation unit. That leads 
 * to NPE's<p>
 * 
 * @author  Alexander Kandzior 
 *
 * @version $Revision: 1.19 $ 
 * 
 * @since 6.0.0 
 */
public final class CmsRole {

    /** The "ACCOUNT_MANAGER" role. */
    public static final CmsRole ACCOUNT_MANAGER;

    /** The "ADMINISTRATOR" role, which is a parent to all organizational unit roles. */
    public static final CmsRole ADMINISTRATOR;

    /** The "EXPORT_DATABASE" role. */
    public static final CmsRole DATABASE_MANAGER;

    /** The "DEVELOPER" role. */
    public static final CmsRole DEVELOPER;

    /** Identifier for role principals. */
    public static final String PRINCIPAL_ROLE = "ROLE";

    /** The "DIRECT_EDIT_USER" role. */
    // public static final CmsRole DIRECT_EDIT_USER;
    /** The "PROJECT_MANAGER" role. */
    public static final CmsRole PROJECT_MANAGER;

    /** The "ROOT_ADMIN" role, which is a parent to all other roles. */
    public static final CmsRole ROOT_ADMIN;

    /** The "VFS_MANAGER" role. */
    public static final CmsRole VFS_MANAGER;

    /** The "WORKPLACE_MANAGER" role. */
    public static final CmsRole WORKPLACE_MANAGER;

    /** The "WORKPLACE_USER" role. */
    public static final CmsRole WORKPLACE_USER;

    /** The list of system roles. */
    private static final List SYSTEM_ROLES;

    /** The child roles of this role. */
    private final List m_children = new ArrayList();

    /** The distinct group names of this role. */
    private List m_distictGroupNames = new ArrayList();

    /** The name of the group this role is mapped to in the OpenCms database.*/
    private final String m_groupName;

    /** The id of the role, does not differentiate for organizational units. */
    private final CmsUUID m_id;

    /** Indicates if this role is organizational unit dependent. */
    private boolean m_ouDependent;

    /** The organizational unit this role applies to. */
    private String m_ouFqn;

    /** The parent role of this role. */
    private final CmsRole m_parentRole;

    /** The name of this role. */
    private final String m_roleName;

    /** Indicates if this role is a system role or a user defined role. */
    private boolean m_systemRole;

    /**
     * Creates a user defined role.<p>
     * 
     * @param roleName the name of this role
     * @param groupName the name of the group the members of this role are stored in
     * @param parentRole the parent role of this role
     * @param ouDependent if the role is organizational unit dependent
     */
    public CmsRole(String roleName, CmsRole parentRole, String groupName, boolean ouDependent) {

        this(roleName, parentRole, groupName);
        m_ouDependent = ouDependent;
        m_systemRole = false;
        initialize();
    }

    /**
     * Copy constructor.<p>
     * 
     * @param role the role to copy
     */
    private CmsRole(CmsRole role) {

        m_roleName = role.m_roleName;
        m_id = CmsUUID.getConstantUUID(m_roleName);
        m_groupName = role.m_groupName;
        m_parentRole = role.m_parentRole;
        m_systemRole = role.m_systemRole;
        m_ouDependent = role.m_ouDependent;
        m_children.addAll(role.m_children);
        m_distictGroupNames.addAll(Collections.unmodifiableList(role.m_distictGroupNames));
    }

    /**
     * Creates a system role.<p>
     * 
     * @param roleName the name of this role
     * @param parentRole the parent role of this role
     * @param groupName the related group name
     */
    private CmsRole(String roleName, CmsRole parentRole, String groupName) {

        m_roleName = roleName;
        m_id = CmsUUID.getConstantUUID(m_roleName);
        m_ouDependent = !groupName.startsWith(CmsOrganizationalUnit.SEPARATOR);
        m_parentRole = parentRole;
        m_systemRole = true;
        if (!m_ouDependent) {
            m_groupName = groupName.substring(1);
        } else {
            m_groupName = groupName;
        }
        if (parentRole != null) {
            parentRole.m_children.add(this);
        }
    }

    /**
     * Initializes the system roles with the configured OpenCms system group names.<p>
     */
    static {

        ROOT_ADMIN = new CmsRole("ROOT_ADMIN", null, "/RoleRootAdmins");
        WORKPLACE_MANAGER = new CmsRole("WORKPLACE_MANAGER", CmsRole.ROOT_ADMIN, "/RoleWorkplaceManager");
        DATABASE_MANAGER = new CmsRole("DATABASE_MANAGER", CmsRole.ROOT_ADMIN, "/RoleDatabaseManager");

        ADMINISTRATOR = new CmsRole("ADMINISTRATOR", CmsRole.ROOT_ADMIN, "RoleAdministrators");
        PROJECT_MANAGER = new CmsRole("PROJECT_MANAGER", CmsRole.ADMINISTRATOR, "RoleProjectmanagers");
        ACCOUNT_MANAGER = new CmsRole("ACCOUNT_MANAGER", CmsRole.ADMINISTRATOR, "RoleAccountManagers");
        VFS_MANAGER = new CmsRole("VFS_MANAGER", CmsRole.ADMINISTRATOR, "RoleVfsManagers");
        DEVELOPER = new CmsRole("DEVELOPER", CmsRole.VFS_MANAGER, "RoleDevelopers");
        WORKPLACE_USER = new CmsRole("WORKPLACE_USER", CmsRole.ADMINISTRATOR, "RoleWorkplaceUsers");
        // DIRECT_EDIT_USER = new CmsRole("DIRECT_EDIT_USER", CmsRole.WORKPLACE_USER, "RoleDirectEditUsers");

        // create a lookup list for the system roles
        SYSTEM_ROLES = Collections.unmodifiableList(Arrays.asList(new CmsRole[] {
            ROOT_ADMIN,
            WORKPLACE_MANAGER,
            DATABASE_MANAGER,
            ADMINISTRATOR,
            PROJECT_MANAGER,
            ACCOUNT_MANAGER,
            VFS_MANAGER,
            DEVELOPER,
            WORKPLACE_USER
        // DIRECT_EDIT_USER
        }));

        // now initilaize all system roles
        for (int i = 0; i < SYSTEM_ROLES.size(); i++) {
            ((CmsRole)SYSTEM_ROLES.get(i)).initialize();
        }
    }

    /**
     * Returns the list of system defined roles (instances of <code>{@link CmsRole}</code>).<p> 
     * 
     * @return the list of system defined roles
     */
    public static List getSystemRoles() {

        return SYSTEM_ROLES;
    }

    /**
     * Returns the role for the given group.<p>
     * 
     * @param group a group to check for role representation
     * 
     * @return the role for the given group
     */
    public static CmsRole valueOf(CmsGroup group) {

        // check groups for internal representing the roles
        if (group.isRole()) {
            CmsRole role = valueOfGroupName(group.getName());
            if (role != null) {
                return role;
            }
        }
        // check virtual groups mapping a role
        if (group.isVirtual()) {
            int index = (group.getFlags() & (I_CmsPrincipal.FLAG_CORE_LIMIT - 1));
            index = index / (I_CmsPrincipal.FLAG_GROUP_VIRTUAL * 2);
            CmsRole role = (CmsRole)getSystemRoles().get(index);
            return role.forOrgUnit(group.getOuFqn());
        }
        return null;
    }

    /**
     * Returns the role for the given group name.<p>
     * 
     * @param groupName a group name to check for role representation
     * 
     * @return the role for the given group name
     */
    public static CmsRole valueOfGroupName(String groupName) {

        String groupOu = CmsOrganizationalUnit.getParentFqn(groupName);
        Iterator it = SYSTEM_ROLES.iterator();
        while (it.hasNext()) {
            CmsRole role = (CmsRole)it.next();
            // direct check
            if (groupName.equals(role.getGroupName())) {
                return role.forOrgUnit(groupOu);
            }
            if (!role.isOrganizationalUnitIndependent()) {
                // the role group name does not start with "/", but the given group fqn does 
                if (groupName.endsWith(CmsOrganizationalUnit.SEPARATOR + role.getGroupName())) {
                    return role.forOrgUnit(groupOu);
                }
            }
        }
        return null;
    }

    /**
     * Returns the role for the given id.<p>
     * 
     * @param roleId the id to check for role representation
     * 
     * @return the role for the given role id
     */
    public static CmsRole valueOfId(CmsUUID roleId) {

        Iterator it = SYSTEM_ROLES.iterator();
        while (it.hasNext()) {
            CmsRole role = (CmsRole)it.next();
            if (roleId.equals(role.getId())) {
                return role;
            }
        }
        return null;
    }

    /**
     * Returns the role for the given role name.<p>
     * 
     * @param roleName a role name to check for role representation
     * 
     * @return the role for the given role name
     */
    public static CmsRole valueOfRoleName(String roleName) {

        String roleOu = CmsOrganizationalUnit.getParentFqn(roleName);
        Iterator it = SYSTEM_ROLES.iterator();
        while (it.hasNext()) {
            CmsRole role = (CmsRole)it.next();
            // direct check
            if (roleName.equals(role.getRoleName())) {
                return role.forOrgUnit(roleOu);
            }
            if (!role.isOrganizationalUnitIndependent()) {
                // the role name does not start with "/", but the given role fqn does 
                if (roleName.endsWith(CmsOrganizationalUnit.SEPARATOR + role.getRoleName())) {
                    return role.forOrgUnit(roleOu);
                }
            }
        }
        return null;
    }

    /**
     * Returns a role violation exception configured with a localized, role specific message 
     * for this role.<p>
     * 
     * @param requestContext the current users OpenCms request context
     * 
     * @return a role violation exception configured with a localized, role specific message 
     *      for this role
     */
    public CmsRoleViolationException createRoleViolationException(CmsRequestContext requestContext) {

        return new CmsRoleViolationException(Messages.get().container(
            Messages.ERR_USER_NOT_IN_ROLE_2,
            requestContext.currentUser().getName(),
            getName(requestContext.getLocale())));
    }

    /**
     * Returns a role violation exception configured with a localized, role specific message 
     * for this role.<p>
     * 
     * @param requestContext the current users OpenCms request context
     * @param orgUnitFqn the organizational unit used for the role check, it may be <code>null</code>
     * 
     * @return a role violation exception configured with a localized, role specific message 
     *      for this role
     */
    public CmsRoleViolationException createRoleViolationExceptionForOrgUnit(
        CmsRequestContext requestContext,
        String orgUnitFqn) {

        return new CmsRoleViolationException(Messages.get().container(
            Messages.ERR_USER_NOT_IN_ROLE_FOR_ORGUNIT_3,
            requestContext.currentUser().getName(),
            getName(requestContext.getLocale()),
            orgUnitFqn));
    }

    /**
     * Returns a role violation exception configured with a localized, role specific message 
     * for this role.<p>
     * 
     * @param requestContext the current users OpenCms request context
     * @param resource the resource used for the role check, it may be <code>null</code>
     * 
     * @return a role violation exception configured with a localized, role specific message 
     *      for this role
     */
    public CmsRoleViolationException createRoleViolationExceptionForResource(
        CmsRequestContext requestContext,
        CmsResource resource) {

        return new CmsRoleViolationException(Messages.get().container(
            Messages.ERR_USER_NOT_IN_ROLE_FOR_RESOURCE_3,
            requestContext.currentUser().getName(),
            getName(requestContext.getLocale()),
            requestContext.removeSiteRoot(resource.getRootPath())));
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (obj instanceof CmsRole) {
            CmsRole that = (CmsRole)obj;
            // first check name
            if (m_roleName.equals(that.m_roleName)) {
                if (isOrganizationalUnitIndependent()) {
                    // if ou independent ignore ou info
                    return true;
                }
                // then check the org unit
                if (m_ouFqn == null) {
                    // if org unit not set
                    return (that.m_ouFqn == null);
                } else {
                    // if org unit set
                    return (m_ouFqn.equals(that.m_ouFqn));
                }
            }
        }
        return false;
    }

    /**
     * Creates a new role based on this one for the given organizational unit.<p>
     * 
     * @param ouFqn fully qualified name of the organizational unit
     * 
     * @return a new role based on this one for the given organizational unit
     */
    public CmsRole forOrgUnit(String ouFqn) {

        CmsRole newRole = new CmsRole(this);
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(ouFqn)) {
            if (!ouFqn.endsWith(CmsOrganizationalUnit.SEPARATOR)) {
                ouFqn += CmsOrganizationalUnit.SEPARATOR;
            }
        }
        newRole.m_ouFqn = ouFqn;
        return newRole;
    }

    /**
     * Returns a list of all sub roles.<p>
     * 
     * @param recursive if not set just direct children are returned
     * 
     * @return all sub roles as a list of {@link CmsRole} objects
     */
    public List getChildren(boolean recursive) {

        List children = new ArrayList();
        Iterator itChildren = m_children.iterator();
        while (itChildren.hasNext()) {
            CmsRole child = (CmsRole)itChildren.next();
            if (child.isOrganizationalUnitIndependent()) {
                child = child.forOrgUnit(null);
            } else {
                child = child.forOrgUnit(m_ouFqn);
            }
            children.add(child);
            if (recursive) {
                children.addAll(child.getChildren(true));
            }
        }
        return children;
    }

    /**
     * Returns a localized role description.<p>
     * 
     * @param locale the locale
     * 
     * @return the localized role description
     */
    public String getDescription(Locale locale) {

        if (m_systemRole) {
            // localize role names for system roles
            return Messages.get().getBundle(locale).key("GUI_ROLE_DESCRIPTION_" + m_roleName + "_0");
        } else {
            return getName(locale);
        }
    }

    /**
     * Returns the display name of this role including the organizational unit.<p>
     * 
     * @param cms the cms context
     * @param locale the locale
     * 
     * @return the display name of this role including the organizational unit
     * 
     * @throws CmsException if the organizational unit could not be read 
     */
    public String getDisplayName(CmsObject cms, Locale locale) throws CmsException {

        return Messages.get().getBundle(locale).key(
            Messages.GUI_PRINCIPAL_DISPLAY_NAME_2,
            getName(locale),
            OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, getOuFqn()).getDisplayName(locale));
    }

    /**
     * Returns the distinct group names of this role.<p>
     * 
     * This group names are not fully qualified (organizational unit dependent).<p>
     * 
     * @return the distinct group names of this role
     */
    public List getDistinctGroupNames() {

        return m_distictGroupNames;
    }

    /**
     * Returns the fully qualified name of this role.<p>
     * 
     * @return the fqn of this role
     */
    public String getFqn() {

        if (getOuFqn() == null) {
            return getRoleName();
        }
        return getOuFqn() + getRoleName();
    }

    /**
     * Returns the name of the group this role is mapped to in the OpenCms database.<p>
     * 
     * Here the fully qualified group name is returned.<p>
     * 
     * @return the name of the group this role is mapped to in the OpenCms database
     */
    public String getGroupName() {

        if ((m_ouFqn == null) || isOrganizationalUnitIndependent()) {
            return m_groupName;
        }
        return m_ouFqn + m_groupName;
    }

    /**
     * Returns the id of this role.<p>
     * 
     * Does not differentiate for organizational units.<p>
     * 
     * @return the id of this role
     */
    public CmsUUID getId() {

        return m_id;
    }

    /**
     * Returns a localized role name.<p>
     * 
     * @param locale the locale
     * 
     * @return the localized role name
     */
    public String getName(Locale locale) {

        if (m_systemRole) {
            // localize role names for system roles
            return Messages.get().getBundle(locale).key("GUI_ROLENAME_" + m_roleName + "_0");
        } else {
            return getRoleName();
        }
    }

    /**
     * Returns the fully qualified name of the organizational unit.<p>
     *
     * @return the fully qualified name of the organizational unit
     */
    public String getOuFqn() {

        return CmsOrganizationalUnit.removeLeadingSeparator(m_ouFqn);
    }

    /**
     * Returns the parent role of this role.<p>
     *
     * @return the parent role of this role
     */
    public CmsRole getParentRole() {

        if (m_parentRole == null) {
            return null;
        }
        return m_parentRole.forOrgUnit(m_ouFqn);
    }

    /**
     * Returns the name of the role.<p>
     * 
     * @return the name of the role
     */
    public String getRoleName() {

        return m_roleName;
    }

    /**
     * Returns the flags needed for a group to emulate this role.<p>
     * 
     * @return the flags needed for a group to emulate this role
     */
    public int getVirtualGroupFlags() {

        int flags = I_CmsPrincipal.FLAG_GROUP_VIRTUAL;
        flags += I_CmsPrincipal.FLAG_GROUP_VIRTUAL * 2 * getSystemRoles().indexOf(forOrgUnit(null));
        return flags;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {

        return m_roleName.hashCode()
            + (((m_ouFqn == null) || isOrganizationalUnitIndependent()) ? 13 : m_ouFqn.hashCode());
    }

    /**
     * Checks if this role is organizational unit independent.<p>
     * 
     * @return <code>true</code> if this role is organizational unit independent
     */
    public boolean isOrganizationalUnitIndependent() {

        return !m_ouDependent;
    }

    /**
     * Check if this role is a system role.<p>
     *
     * @return <code>true</code> if this role is a system role
     */
    public boolean isSystemRole() {

        return m_systemRole;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {

        StringBuffer result = new StringBuffer();

        result.append("[");
        result.append(this.getClass().getName());
        result.append(", role: ");
        result.append(getRoleName());
        result.append(", org unit: ");
        result.append(getOuFqn());
        result.append(", group: ");
        result.append(getGroupName());
        result.append("]");

        return result.toString();
    }

    /**
     * Returns a set of all roles group names.<p>
     * 
     * @return a set of all roles group names
     */
    private Set getAllGroupNames() {

        Set distinctGroups = new HashSet();
        // add role group name
        distinctGroups.add(getGroupName());
        if (getParentRole() != null) {
            // add parent roles group names
            distinctGroups.addAll(getParentRole().getAllGroupNames());
        }
        return distinctGroups;
    }

    /**
     * Initializes this role, creating an optimized data structure for 
     * the lookup of the role group names.<p>
     */
    private void initialize() {

        // calculate the distinct groups of this role
        Set distinctGroups = new HashSet();
        distinctGroups.addAll(getAllGroupNames());
        m_distictGroupNames = Collections.unmodifiableList(new ArrayList(distinctGroups));
    }
}
