/*
 * File   : $Source: /usr/local/cvs/opencms/src/org/opencms/db/CmsUserSettings.java,v $
 * Date   : $Date: 2008-04-10 15:47:29 $
 * Version: $Revision: 1.50 $
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

package org.opencms.db;

import org.opencms.configuration.CmsDefaultUserSettings;
import org.opencms.configuration.CmsWorkplaceConfiguration;
import org.opencms.configuration.I_CmsXmlConfiguration;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.file.CmsResource.CmsResourceCopyMode;
import org.opencms.file.CmsResource.CmsResourceDeleteMode;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsContextInfo;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.report.I_CmsReport;
import org.opencms.synchronize.CmsSynchronizeSettings;
import org.opencms.util.A_CmsModeStringEnumeration;
import org.opencms.util.CmsStringUtil;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;

/**
 * Object to conveniently access and modify the users workplace settings.<p>
 *
 * @author  Andreas Zahner 
 * @author  Michael Emmerich 
 * 
 * @version $Revision: 1.50 $
 * 
 * @since 6.0.0
 */
public class CmsUserSettings {

    /**
     *  Enumeration class for workplace search result styles.<p>
     */
    public static final class CmsSearchResultStyle extends A_CmsModeStringEnumeration {

        /** Workplace search result style explorer view. */
        public static final CmsSearchResultStyle STYLE_EXPLORER = new CmsSearchResultStyle(
            "explorer",
            Messages.GUI_WORKPLACE_SEARCH_STYLE_EXPLORER_0);

        /** Workplace search result style list view with excerpts. */
        public static final CmsSearchResultStyle STYLE_LIST_WITH_EXCERPTS = new CmsSearchResultStyle(
            "list-with-excerpts",
            Messages.GUI_WORKPLACE_SEARCH_STYLE_LIST_WITH_EXCERPTS_0);

        /** Workplace search result style list view without excerpts. */
        public static final CmsSearchResultStyle STYLE_LIST_WITHOUT_EXCERPTS = new CmsSearchResultStyle(
            "list-without-excerpts",
            Messages.GUI_WORKPLACE_SEARCH_STYLE_LIST_WITHOUT_EXCERPTS_0);

        /** Serializable version id. */
        private static final long serialVersionUID = 6611568161885127011L;

        /** The localization key for this style. */
        private final String m_key;

        /**
         * Private constructor.<p>
         * 
         * @param style the workplace search result style string representation
         * @param key the localization key for this style
         */
        private CmsSearchResultStyle(String style, String key) {

            super(style);
            m_key = key;
        }

        /**
         * Returns the copy mode object from the old copy mode integer.<p>
         * 
         * @param mode the old copy mode integer
         * 
         * @return the copy mode object
         */
        public static CmsSearchResultStyle valueOf(String mode) {

            if (STYLE_LIST_WITHOUT_EXCERPTS.getMode().equals(mode)) {
                return STYLE_LIST_WITHOUT_EXCERPTS;
            } else if (STYLE_LIST_WITH_EXCERPTS.getMode().equals(mode)) {
                return STYLE_LIST_WITH_EXCERPTS;
            } else {
                return STYLE_EXPLORER;
            }
        }

        /**
         * Returns the localization key for this style.<p>
         * 
         * @return the localization key for this style
         */
        public String getKey() {

            return m_key;
        }
    }

    /** Key for additional info address. */
    public static final String ADDITIONAL_INFO_ADDRESS = "USER_ADDRESS";

    /** Key for additional info city. */
    public static final String ADDITIONAL_INFO_CITY = "USER_TOWN"; // Value must unfortunately still be "USER_TOWN" or existing serialized user information will be lost

    /** Key for additional info of resources that were confirmed by the user. */
    public static final String ADDITIONAL_INFO_CONFIRMED_RESOURCES = "ADDITIONAL_INFO_CONFIRMED_RESOURCES";

    /** Key for additional info address. */
    public static final String ADDITIONAL_INFO_COUNTRY = "USER_COUNTRY";

    /** Key for additional info default group. */
    public static final String ADDITIONAL_INFO_DEFAULTGROUP = "USER_DEFAULTGROUP";

    /** Key for additional info address. */
    public static final String ADDITIONAL_INFO_DESCRIPTION = "USER_DESCRIPTION";

    /** Key for additional info explorer settings. */
    public static final String ADDITIONAL_INFO_EXPLORERSETTINGS = "USER_EXPLORERSETTINGS";

    /** Key for additional info flags. */
    public static final String ADDITIONAL_INFO_PREFERENCES = "USER_PREFERENCES";

    /** Key for additional info start settings. */
    public static final String ADDITIONAL_INFO_STARTSETTINGS = "USER_STARTSETTINGS";

    /** Key for additional info time warp. */
    public static final String ADDITIONAL_INFO_TIMEWARP = "USER_TIMEWARP";

    /**
     *  Key for additional info city.
     *  
     *  @deprecated use {@link #ADDITIONAL_INFO_CITY} instead
     */
    public static final String ADDITIONAL_INFO_TOWN = "USER_TOWN";

    /** Key for additional info upload applet client folder path. */
    public static final String ADDITIONAL_INFO_UPLOADAPPLET_CLIENTFOLDER = "USER_UPLOADAPPLET_CLIENTFOLDER";

    /** Key for additional info address. */
    public static final String ADDITIONAL_INFO_ZIPCODE = "USER_ZIPCODE";

    /** Flag for displaying the date created column. */
    public static final int FILELIST_DATE_CREATED = 1024;

    /** Flag for displaying the date expired column. */
    public static final int FILELIST_DATE_EXPIRED = 8192;

    /** Flag for displaying the changed column. */
    public static final int FILELIST_DATE_LASTMODIFIED = 4;

    /** Flag for displaying the date released column. */
    public static final int FILELIST_DATE_RELEASED = 4096;

    /** Flag for displaying the locked column. */
    public static final int FILELIST_LOCKEDBY = 256;

    /** Flag for displaying the name column. */
    public static final int FILELIST_NAME = 512;

    /** Flag for displaying the navigation text column. */
    public static final int FILELIST_NAVTEXT = 64;

    /** Flag for displaying the access column. */
    public static final int FILELIST_PERMISSIONS = 128;

    /** Flag for displaying the size column. */
    public static final int FILELIST_SIZE = 8;

    /** Flag for displaying the state column. */
    public static final int FILELIST_STATE = 16;

    /** Flag for displaying the title column. */
    public static final int FILELIST_TITLE = 1;

    /** Flag for displaying the file type column. */
    public static final int FILELIST_TYPE = 2;

    /** Flag for displaying the owner column. */
    public static final int FILELIST_USER_CREATED = 32;

    /** Flag for displaying the user who last modified column. */
    public static final int FILELIST_USER_LASTMODIFIED = 2048;

    /** Identifier prefix for all keys in the user additional info table. */
    public static final String PREFERENCES = "USERPREFERENCES_";

    /** Identifier for the synchronize setting key. */
    public static final String SYNC_DESTINATION = "DESTINATION";

    /** Identifier for the synchronize setting key. */
    public static final String SYNC_ENABLED = "ENABLED";

    /** Identifier for the synchronize setting key. */
    public static final String SYNC_SETTINGS = "SYNC_SETTINGS_";

    /** Identifier for the synchronize setting key. */
    public static final String SYNC_VFS_LIST = "VFS_LIST";

    /** The default button style. */
    private static final int BUTTONSTYLE_DEFAULT = 1;

    /** The default number of entries per page. */
    private static final int ENTRYS_PER_PAGE_DEFAULT = 50;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDriverManager.class);

    /** Default workplace search index name. */
    private static final String SEARCH_INDEX_DEFAULT = "Offline project (VFS)";

    private boolean m_dialogDirectpublish;

    private boolean m_dialogExpandInheritedPermissions;

    private boolean m_dialogExpandUserPermissions;

    private CmsResourceCopyMode m_dialogFileCopy;

    private CmsResourceDeleteMode m_dialogFileDelete;

    private CmsResourceCopyMode m_dialogFolderCopy;

    private boolean m_dialogPermissionsInheritOnFolder;

    private int m_directeditButtonStyle;

    private int m_editorButtonStyle;

    private TreeMap m_editorSettings;

    private int m_explorerButtonStyle;

    private int m_explorerFileEntries;

    private int m_explorerSettings;

    /** The list of numbers in the preferences dialog, how much entries shown on a page. */
    private String m_exporerFileEntryOptions;

    /** Flag to determine if all projects should be list. */
    private boolean m_listAllProjects;

    private Locale m_locale;

    /** Controls if the "create index page" check box in the new folder dialog should be initially be checked or not. */
    private Boolean m_newFolderCreateIndexPage;

    /** Controls if the "edit properties" check box in the new folder dialog should be initially be checked or not. */
    private Boolean m_newFolderEditProperties;

    private String m_project;

    /** Controls appearance of the publish button. */
    private String m_publishButtonAppearance;

    private boolean m_restrictExplorerView;

    private boolean m_showExportSettings;

    /** Flag that controls display of the file upload button. */
    private boolean m_showFileUploadButton;

    private boolean m_showLock;

    /** Flag to determine if the publish notifications should be shown. */
    private boolean m_showPublishNotification;

    /** Controls if the resource type dialog for uploaded resources (not the applet) is shown or not. */
    private Boolean m_showUploadTypeDialog;

    private String m_startFolder;

    private String m_startSite;

    private CmsSynchronizeSettings m_synchronizeSettings;

    /** The custom user surf time. */
    private long m_timeWarp;

    private boolean m_uploadApplet;

    /** The path of the preselected folder for the upload applet on the client machine. */
    private String m_uploadAppletClientFolder;

    private CmsUser m_user;

    private String m_view;

    private int m_workplaceButtonStyle;

    private String m_workplaceReportType;

    /** The name of the search index to use in the workplace. */
    private String m_workplaceSearchIndexName;

    /** Workplace search result list view style. */
    private CmsSearchResultStyle m_workplaceSearchViewStyle;

    /**
     * Creates an empty new user settings object.<p>
     */
    public CmsUserSettings() {

        m_workplaceButtonStyle = CmsUserSettings.BUTTONSTYLE_DEFAULT;
        m_workplaceReportType = I_CmsReport.REPORT_TYPE_SIMPLE;
        m_explorerButtonStyle = CmsUserSettings.BUTTONSTYLE_DEFAULT;
        m_explorerFileEntries = CmsUserSettings.ENTRYS_PER_PAGE_DEFAULT;
        m_explorerSettings = CmsUserSettings.FILELIST_NAME;
        m_editorSettings = new TreeMap();
        m_showFileUploadButton = true;
        m_showPublishNotification = false;
        m_listAllProjects = false;
        m_uploadApplet = true;
        m_publishButtonAppearance = CmsDefaultUserSettings.PUBLISHBUTTON_SHOW_ALWAYS;
        m_newFolderCreateIndexPage = Boolean.TRUE;
        m_newFolderEditProperties = Boolean.TRUE;
        m_showUploadTypeDialog = Boolean.TRUE;
        m_workplaceSearchIndexName = SEARCH_INDEX_DEFAULT;
        m_workplaceSearchViewStyle = CmsSearchResultStyle.STYLE_EXPLORER;
    }

    /**
     * Creates a user settings object with initialized settings of the current user.<p>
     * 
     * @param cms the OpenCms context
     */
    public CmsUserSettings(CmsObject cms) {

        this(cms.getRequestContext().currentUser());
    }

    /**
     * Creates a user settings object with initialized settings of the user.<p>
     * 
     * Some default settings will not be set, if no cms object is given.<p>
     *  
     * @param user the current CmsUser
     * 
     * @see #CmsUserSettings(CmsObject)
     */
    public CmsUserSettings(CmsUser user) {

        init(user);
    }

    /**
     * Gets the default copy mode when copying a file of the user.<p>
     * 
     * @return the default copy mode when copying a file of the user
     */
    public CmsResourceCopyMode getDialogCopyFileMode() {

        return m_dialogFileCopy;
    }

    /**
     * Gets the default copy mode when copying a folder of the user.<p>
     * 
     * @return the default copy mode when copying a folder of the user
     */
    public CmsResourceCopyMode getDialogCopyFolderMode() {

        return m_dialogFolderCopy;
    }

    /**
     * Returns the default setting for file deletion.<p>
     * 
     * @return the default setting for file deletion
     */
    public CmsResourceDeleteMode getDialogDeleteFileMode() {

        return m_dialogFileDelete;
    }

    /**
     * Returns the default setting for expanding inherited permissions in the dialog.<p>
     * 
     * @return true if inherited permissions should be expanded, otherwise false
     */
    public boolean getDialogExpandInheritedPermissions() {

        return m_dialogExpandInheritedPermissions;
    }

    /**
     * Returns the default setting for expanding the users permissions in the dialog.<p>
     * 
     * @return true if the users permissions should be expanded, otherwise false
     */
    public boolean getDialogExpandUserPermissions() {

        return m_dialogExpandUserPermissions;
    }

    /**
     * Returns the default setting for inheriting permissions on folders.<p>
     * 
     * @return true if permissions should be inherited on folders, otherwise false
     */
    public boolean getDialogPermissionsInheritOnFolder() {

        return m_dialogPermissionsInheritOnFolder;
    }

    /**
     * Returns the default setting for direct publishing.<p>
     * 
     * @return the default setting for direct publishing: true if siblings should be published, otherwise false
     */
    public boolean getDialogPublishSiblings() {

        return m_dialogDirectpublish;
    }

    /**
     * Determines if the export part of the secure/export dialog should be shown.<p>
     * 
     * @return true if the export dialog is shown, otherwise false
     */
    public boolean getDialogShowExportSettings() {

        return m_showExportSettings;
    }

    /**
     * Determines if the lock dialog should be shown.<p>
     * 
     * @return true if the lock dialog is shown, otherwise false
     */
    public boolean getDialogShowLock() {

        return m_showLock;
    }

    /**
     * Returns the style of the direct edit buttons of the user.<p>
     * 
     * @return the style of the direct edit buttons of the user
     */
    public int getDirectEditButtonStyle() {

        return m_directeditButtonStyle;
    }

    /**
     * Returns the style of the editor buttons of the user.<p>
     * 
     * @return the style of the editor buttons of the user
     */
    public int getEditorButtonStyle() {

        return m_editorButtonStyle;
    }

    /**
     * Returns the editor settings of the user.<p>
     * 
     * @return the editor settings of the user
     */
    public Map getEditorSettings() {

        return m_editorSettings;
    }

    /**
     * Returns the style of the explorer buttons of the user.<p>
     * 
     * @return the style of the explorer buttons of the user
     */
    public int getExplorerButtonStyle() {

        return m_explorerButtonStyle;
    }

    /**
     * Returns the number of displayed files per page of the user.<p>
     * 
     * @return the number of displayed files per page of the user
     */
    public int getExplorerFileEntries() {

        return m_explorerFileEntries;
    }

    /**
     * Returns the explorer start settings.<p>
     * 
     * @return the explorer start settings
     */
    public int getExplorerSettings() {

        return m_explorerSettings;
    }

    /**
     * Returns the exporerFileEntryOptions.<p>
     *
     * @return the exporerFileEntryOptions
     */
    public String getExporerFileEntryOptions() {

        return m_exporerFileEntryOptions;
    }

    /**
     * Returns if all projects should be listed or only the ones in the current ou.<p>
     * 
     * @return true if all projects should be listed, otherwise false
     */
    public boolean getListAllProjects() {

        return m_listAllProjects;
    }

    /** 
     * Returns the locale of the user.<p>
     * 
     * @return the locale of the user
     */
    public Locale getLocale() {

        return m_locale;
    }

    /**
     * Returns <code>{@link Boolean#TRUE}</code> if the "create index page" check box in the new folder 
     * dialog should be initially be checked. <p>
     * 
     * @return <code>{@link Boolean#TRUE}</code> if the "create index page" check box in the new folder 
     *      dialog should be initially be checked. 
     */
    public Boolean getNewFolderCreateIndexPage() {

        return m_newFolderCreateIndexPage;
    }

    /**
     * Returns <code>{@link Boolean#TRUE}</code> if the "edit properties" check box in the new folder 
     * dialog should be initially be checked. <p>
     * 
     * @return <code>{@link Boolean#TRUE}</code> if the "edit properties" check box in the new folder 
     *      dialog should be initially be checked. 
     */
    public Boolean getNewFolderEditProperties() {

        return m_newFolderEditProperties;
    }

    /**
     * Returns the preferred editor for the given resource type of the user.<p>
     * 
     * @param resourceType the resource type
     * @return the preferred editor for the resource type or null, if not specified
     */
    public String getPreferredEditor(String resourceType) {

        return (String)m_editorSettings.get(resourceType);
    }

    /**
     * Returns the appearance of the "publish project" button.<p>
     * 
     * This can be either {@link CmsDefaultUserSettings#PUBLISHBUTTON_SHOW_ALWAYS}, 
     * {@link CmsDefaultUserSettings#PUBLISHBUTTON_SHOW_AUTO} or 
     * {@link CmsDefaultUserSettings#PUBLISHBUTTON_SHOW_NEVER}.<p>
     * 
     * @return the appearance of the "publish project" button
     */
    public String getPublishButtonAppearance() {

        return m_publishButtonAppearance;
    }

    /**
     * Sets if the explorer view is restricted to the defined site and folder.<p>
     * 
     * @return true if the explorer view is restricted, otherwise false
     */
    public boolean getRestrictExplorerView() {

        return m_restrictExplorerView;
    }

    /**
     * Returns <code>true</code> if the file upload button should be shown or <code>false</code> otherwise.<p>
     *
     * @return the showFileUpload
     */
    public boolean getShowFileUploadButton() {

        return m_showFileUploadButton;
    }

    /**
     * Returns if the publish notifications should be shown or not.<p>
     * 
     * @return true if the publish notifications should be shown, otherwise false
     */
    public boolean getShowPublishNotification() {

        return m_showPublishNotification;
    }

    /**
     * Returns <code>{@link Boolean#TRUE}</code> if the resource type selection dialog should 
     * be shown in the file upload process (non - applet version). <p>
     * 
     * @return <code>{@link Boolean#TRUE}</code> if the resource type selection dialog should 
     *      be shown in the file upload process (non - applet version).
     */
    public Boolean getShowUploadTypeDialog() {

        return m_showUploadTypeDialog;
    }

    /**
     * Returns the start folder of the user.<p>
     * 
     * @return the start folder of the user
     */
    public String getStartFolder() {

        return m_startFolder;
    }

    /** 
     * Returns the start project of the user.<p>
     * 
     * @return the start project of the user
     */
    public String getStartProject() {

        return m_project;
    }

    /**
     * Returns the start site of the user.<p>
     * 
     * @return the start site of the user
     */
    public String getStartSite() {

        return m_startSite;
    }

    /**
     * Returns the current start view of the user.<p>
     * 
     * @return the current start view of the user
     */
    public String getStartView() {

        return m_view;
    }

    /**
     * Returns the (optional) workplace synchronize settings.<p>
     *
     * @return the (optional) workplace synchronize settings
     */
    public CmsSynchronizeSettings getSynchronizeSettings() {

        return m_synchronizeSettings;
    }

    /**
     * Returns the current users time warp time, or
     * {@link org.opencms.main.CmsContextInfo#CURRENT_TIME} if this feature is disabled and the current time
     * is used for each user request.<p>
     * 
     * @return the current users time warp time, or
     *      {@link org.opencms.main.CmsContextInfo#CURRENT_TIME} if this feature is disabled
     */
    public long getTimeWarp() {

        return m_timeWarp;
    }

    /**
     * Returns the folder path  of the upload applet on the client machine.<p>
     *
     * @return the folder path  of the upload applet on the client machine
     */
    public String getUploadAppletClientFolder() {

        return m_uploadAppletClientFolder;
    }

    /**
     * Returns the current user for the settings.<p>
     * 
     * @return the CmsUser
     */
    public CmsUser getUser() {

        return m_user;
    }

    /**
     * Returns the style of the workplace buttons of the user.<p>
     * 
     * @return the style of the workplace buttons of the user
     */
    public int getWorkplaceButtonStyle() {

        return m_workplaceButtonStyle;
    }

    /**
     * Returns the type of the report (simple or extended) of the user.<p>
     * 
     * @return the type of the report (simple or extended) of the user
     */
    public String getWorkplaceReportType() {

        return m_workplaceReportType;
    }

    /**
     * Returns the name of the search index to use in the workplace.<p>
     *
     * @return the name of the search index to use in the workplace
     */
    public String getWorkplaceSearchIndexName() {

        return m_workplaceSearchIndexName;
    }

    /**
     * Returns the workplace search result list view style.<p>
     *
     * @return the workplace search result list view style
     */
    public CmsSearchResultStyle getWorkplaceSearchViewStyle() {

        return m_workplaceSearchViewStyle;
    }

    /**
     * Initializes the user settings with the given users setting parameters.<p>
     * 
     * @param user the current CmsUser
     */
    public void init(CmsUser user) {

        m_user = user;

        // try to initialize the User Settings with the values stored in the user object.
        // if no values are found, the default user settings will be used.

        // workplace button style
        try {
            m_workplaceButtonStyle = ((Integer)m_user.getAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_WORKPLACEGENERALOPTIONS
                + CmsWorkplaceConfiguration.N_BUTTONSTYLE)).intValue();
        } catch (Throwable t) {
            m_workplaceButtonStyle = OpenCms.getWorkplaceManager().getDefaultUserSettings().getWorkplaceButtonStyle();
        }
        // workplace time warp setting
        Object timeWarpObj = m_user.getAdditionalInfo(ADDITIONAL_INFO_TIMEWARP);
        try {
            m_timeWarp = ((Long)timeWarpObj).longValue();
        } catch (ClassCastException e) {
            try {
                m_timeWarp = Long.parseLong((String)timeWarpObj);
            } catch (Throwable t) {
                m_timeWarp = CmsContextInfo.CURRENT_TIME;
            }
        } catch (Throwable t) {
            m_timeWarp = CmsContextInfo.CURRENT_TIME;
        }
        // workplace report type
        m_workplaceReportType = (String)m_user.getAdditionalInfo(PREFERENCES
            + CmsWorkplaceConfiguration.N_WORKPLACEGENERALOPTIONS
            + CmsWorkplaceConfiguration.N_REPORTTYPE);
        if (m_workplaceReportType == null) {
            m_workplaceReportType = OpenCms.getWorkplaceManager().getDefaultUserSettings().getWorkplaceReportType();
        }
        // workplace list all projects
        try {
            m_listAllProjects = ((Boolean)m_user.getAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_WORKPLACEGENERALOPTIONS
                + CmsWorkplaceConfiguration.N_LISTALLPROJECTS)).booleanValue();
        } catch (Throwable t) {
            m_listAllProjects = OpenCms.getWorkplaceManager().getDefaultUserSettings().getListAllProjects();
        }
        // workplace show publish notification
        try {
            m_showPublishNotification = ((Boolean)m_user.getAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_WORKPLACEGENERALOPTIONS
                + CmsWorkplaceConfiguration.N_PUBLISHNOTIFICATION)).booleanValue();
        } catch (Throwable t) {
            m_showPublishNotification = OpenCms.getWorkplaceManager().getDefaultUserSettings().getShowPublishNotification();
        }
        // workplace upload applet mode
        try {
            m_uploadApplet = ((Boolean)m_user.getAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_WORKPLACEGENERALOPTIONS
                + CmsWorkplaceConfiguration.N_UPLOADAPPLET)).booleanValue();
        } catch (Throwable t) {
            m_uploadApplet = OpenCms.getWorkplaceManager().getDefaultUserSettings().useUploadApplet();
        }
        // locale
        Object obj = m_user.getAdditionalInfo(PREFERENCES
            + CmsWorkplaceConfiguration.N_WORKPLACESTARTUPSETTINGS
            + CmsWorkplaceConfiguration.N_LOCALE);
        if (obj == null) {
            m_locale = null;
        } else {
            m_locale = CmsLocaleManager.getLocale(String.valueOf(obj));
        }
        if (m_locale == null) {
            m_locale = OpenCms.getWorkplaceManager().getDefaultUserSettings().getLocale();
        }
        // start project
        try {
            m_project = (String)m_user.getAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_WORKPLACESTARTUPSETTINGS
                + CmsWorkplaceConfiguration.N_PROJECT);
        } catch (Throwable t) {
            m_project = null;
        }
        if (m_project == null) {
            m_project = OpenCms.getWorkplaceManager().getDefaultUserSettings().getStartProject();
            String ou = user.getOuFqn();
            if (ou == null) {
                ou = "";
            }
            m_project = user.getOuFqn() + m_project;
        }
        // start view
        m_view = (String)m_user.getAdditionalInfo(PREFERENCES
            + CmsWorkplaceConfiguration.N_WORKPLACESTARTUPSETTINGS
            + CmsWorkplaceConfiguration.N_WORKPLACEVIEW);
        if (m_view == null) {
            m_view = OpenCms.getWorkplaceManager().getDefaultUserSettings().getStartView();
        }
        // explorer button style
        try {
            m_explorerButtonStyle = ((Integer)m_user.getAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_EXPLORERGENERALOPTIONS
                + CmsWorkplaceConfiguration.N_BUTTONSTYLE)).intValue();
        } catch (Throwable t) {
            m_explorerButtonStyle = OpenCms.getWorkplaceManager().getDefaultUserSettings().getExplorerButtonStyle();
        }
        // explorer file entries        
        try {
            m_explorerFileEntries = ((Integer)m_user.getAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_EXPLORERGENERALOPTIONS
                + CmsWorkplaceConfiguration.N_ENTRIES)).intValue();
        } catch (Throwable t) {
            m_explorerFileEntries = OpenCms.getWorkplaceManager().getDefaultUserSettings().getExplorerFileEntries();
        }
        // explorer settings
        try {
            m_explorerSettings = ((Integer)m_user.getAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_EXPLORERGENERALOPTIONS
                + CmsWorkplaceConfiguration.N_EXPLORERDISPLAYOPTIONS)).intValue();
        } catch (Throwable t) {
            m_explorerSettings = OpenCms.getWorkplaceManager().getDefaultUserSettings().getExplorerSettings();
        }
        // dialog file copy mode
        try {
            m_dialogFileCopy = CmsResourceCopyMode.valueOf(((Integer)m_user.getAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_DIALOGSDEFAULTSETTINGS
                + CmsWorkplaceConfiguration.N_FILECOPY)).intValue());
        } catch (Throwable t) {
            m_dialogFileCopy = OpenCms.getWorkplaceManager().getDefaultUserSettings().getDialogCopyFileMode();
        }
        // dialog folder copy mode
        try {
            m_dialogFolderCopy = CmsResourceCopyMode.valueOf(((Integer)m_user.getAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_DIALOGSDEFAULTSETTINGS
                + CmsWorkplaceConfiguration.N_FOLDERCOPY)).intValue());
        } catch (Throwable t) {
            m_dialogFolderCopy = OpenCms.getWorkplaceManager().getDefaultUserSettings().getDialogCopyFolderMode();
        }
        // dialog file delete mode
        try {
            m_dialogFileDelete = CmsResourceDeleteMode.valueOf(((Integer)m_user.getAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_DIALOGSDEFAULTSETTINGS
                + CmsWorkplaceConfiguration.N_FILEDELETION)).intValue());
        } catch (Throwable t) {
            m_dialogFileDelete = OpenCms.getWorkplaceManager().getDefaultUserSettings().getDialogDeleteFileMode();
        }
        // dialog direct publish mode
        try {
            m_dialogDirectpublish = ((Boolean)m_user.getAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_DIALOGSDEFAULTSETTINGS
                + CmsWorkplaceConfiguration.N_DIRECTPUBLISH)).booleanValue();
        } catch (Throwable t) {
            m_dialogDirectpublish = OpenCms.getWorkplaceManager().getDefaultUserSettings().getDialogPublishSiblings();
        }
        // dialog show lock mode
        try {
            m_showLock = ((Boolean)m_user.getAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_DIALOGSDEFAULTSETTINGS
                + CmsWorkplaceConfiguration.N_SHOWLOCK)).booleanValue();
        } catch (Throwable t) {
            m_showLock = OpenCms.getWorkplaceManager().getDefaultUserSettings().getDialogShowLock();
        }
        // dialog show export settings mode
        try {
            m_showExportSettings = ((Boolean)m_user.getAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_DIALOGSDEFAULTSETTINGS
                + CmsWorkplaceConfiguration.N_SHOWEXPORTSETTINGS)).booleanValue();
        } catch (Throwable t) {
            m_showExportSettings = OpenCms.getWorkplaceManager().getDefaultUserSettings().getDialogShowExportSettings();
        }
        // dialog permissions inheriting mode
        try {
            m_dialogPermissionsInheritOnFolder = ((Boolean)m_user.getAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_DIALOGSDEFAULTSETTINGS
                + CmsWorkplaceConfiguration.N_PERMISSIONSINHERITONFOLDER)).booleanValue();
        } catch (Throwable t) {
            m_dialogPermissionsInheritOnFolder = OpenCms.getWorkplaceManager().getDefaultUserSettings().getDialogPermissionsInheritOnFolder();
        }
        // dialog expand inherited permissions mode
        try {
            m_dialogExpandInheritedPermissions = ((Boolean)m_user.getAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_DIALOGSDEFAULTSETTINGS
                + CmsWorkplaceConfiguration.N_EXPANDPERMISSIONSINHERITED)).booleanValue();
        } catch (Throwable t) {
            m_dialogExpandInheritedPermissions = OpenCms.getWorkplaceManager().getDefaultUserSettings().getDialogExpandInheritedPermissions();
        }
        // dialog expand users permissions mode
        try {
            m_dialogExpandUserPermissions = ((Boolean)m_user.getAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_DIALOGSDEFAULTSETTINGS
                + CmsWorkplaceConfiguration.N_EXPANDPERMISSIONSUSER)).booleanValue();
        } catch (Throwable t) {
            m_dialogExpandUserPermissions = OpenCms.getWorkplaceManager().getDefaultUserSettings().getDialogExpandUserPermissions();
        }
        // editor button style
        try {
            m_editorButtonStyle = ((Integer)m_user.getAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_EDITORGENERALOPTIONS
                + CmsWorkplaceConfiguration.N_BUTTONSTYLE)).intValue();
        } catch (Throwable t) {
            m_editorButtonStyle = OpenCms.getWorkplaceManager().getDefaultUserSettings().getEditorButtonStyle();
        }
        // direct edit button style
        try {
            m_directeditButtonStyle = ((Integer)m_user.getAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_EDITORGENERALOPTIONS
                + CmsWorkplaceConfiguration.N_DIRECTEDITSTYLE)).intValue();
        } catch (Throwable t) {
            m_directeditButtonStyle = OpenCms.getWorkplaceManager().getDefaultUserSettings().getDirectEditButtonStyle();
        }
        // editor settings
        m_editorSettings = new TreeMap();
        Iterator itKeys = m_user.getAdditionalInfo().keySet().iterator();
        while (itKeys.hasNext()) {
            String key = (String)itKeys.next();
            if (key.startsWith(PREFERENCES + CmsWorkplaceConfiguration.N_EDITORPREFERREDEDITORS)) {
                String editKey = key.substring((PREFERENCES + CmsWorkplaceConfiguration.N_EDITORPREFERREDEDITORS).length());
                m_editorSettings.put(editKey, m_user.getAdditionalInfo(key));
            }
        }
        if (m_editorSettings.isEmpty()) {
            m_editorSettings = new TreeMap(OpenCms.getWorkplaceManager().getDefaultUserSettings().getEditorSettings());
        }
        // start site
        m_startSite = (String)m_user.getAdditionalInfo(PREFERENCES
            + CmsWorkplaceConfiguration.N_WORKPLACESTARTUPSETTINGS
            + I_CmsXmlConfiguration.N_SITE);
        if (m_startSite == null) {
            m_startSite = OpenCms.getWorkplaceManager().getDefaultUserSettings().getStartSite();
        }
        // start folder, we use the setter here for default logic in case of illegal folder string:
        String startFolder = (String)m_user.getAdditionalInfo(PREFERENCES
            + CmsWorkplaceConfiguration.N_WORKPLACESTARTUPSETTINGS
            + CmsWorkplaceConfiguration.N_FOLDER);
        if (startFolder == null) {
            startFolder = OpenCms.getWorkplaceManager().getDefaultUserSettings().getStartFolder();
        }
        setStartFolder(startFolder);

        // restrict explorer folder view
        try {
            m_restrictExplorerView = ((Boolean)m_user.getAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_WORKPLACESTARTUPSETTINGS
                + CmsWorkplaceConfiguration.N_RESTRICTEXPLORERVIEW)).booleanValue();
        } catch (Throwable t) {
            m_restrictExplorerView = OpenCms.getWorkplaceManager().getDefaultUserSettings().getRestrictExplorerView();
        }
        // workplace search
        m_workplaceSearchIndexName = OpenCms.getWorkplaceManager().getDefaultUserSettings().getWorkplaceSearchIndexName();

        m_workplaceSearchViewStyle = CmsSearchResultStyle.valueOf((String)m_user.getAdditionalInfo(PREFERENCES
            + CmsWorkplaceConfiguration.N_WORKPLACESEARCH
            + CmsWorkplaceConfiguration.N_SEARCHVIEWSTYLE));
        if (m_workplaceSearchViewStyle == null) {
            m_workplaceSearchViewStyle = OpenCms.getWorkplaceManager().getDefaultUserSettings().getWorkplaceSearchViewStyle();
        }
        // synchronize settings
        try {
            boolean enabled = ((Boolean)m_user.getAdditionalInfo(PREFERENCES + SYNC_SETTINGS + SYNC_ENABLED)).booleanValue();
            String destination = (String)m_user.getAdditionalInfo(PREFERENCES + SYNC_SETTINGS + SYNC_DESTINATION);
            List vfsList = CmsStringUtil.splitAsList((String)m_user.getAdditionalInfo(PREFERENCES
                + SYNC_SETTINGS
                + SYNC_VFS_LIST), '|');
            m_synchronizeSettings = new CmsSynchronizeSettings();
            m_synchronizeSettings.setEnabled(enabled);
            m_synchronizeSettings.setDestinationPathInRfs(destination);
            m_synchronizeSettings.setSourceListInVfs(vfsList);
        } catch (Throwable t) {
            // default is to disable the synchronize settings
            m_synchronizeSettings = null;
        }
        // upload applet client folder path
        m_uploadAppletClientFolder = (String)m_user.getAdditionalInfo(ADDITIONAL_INFO_UPLOADAPPLET_CLIENTFOLDER);

        try {
            save(null);
        } catch (CmsException e) {
            // ignore
            if (LOG.isWarnEnabled()) {
                LOG.warn(e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * Saves the changed settings of the user to the users {@link CmsUser#getAdditionalInfo()} map.<p>
     * 
     * If the given CmsObject is <code>null</code>, the additional user infos are only updated in memory
     * and not saved into the database.<p>
     * 
     * @param cms the CmsObject needed to write the user to the db
     * 
     * @throws CmsException if user cannot be written to the db
     */
    public void save(CmsObject cms) throws CmsException {

        // only set those values that are different than the default values
        // if the user info should be written to the database (if the CmsObject != null)
        // all values that are equal to the default values must be deleted from the additional info
        // user settings.

        // workplace button style
        if (getWorkplaceButtonStyle() != OpenCms.getWorkplaceManager().getDefaultUserSettings().getWorkplaceButtonStyle()) {
            m_user.setAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_WORKPLACEGENERALOPTIONS
                + CmsWorkplaceConfiguration.N_BUTTONSTYLE, new Integer(getWorkplaceButtonStyle()));
        } else if (cms != null) {
            m_user.deleteAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_WORKPLACEGENERALOPTIONS
                + CmsWorkplaceConfiguration.N_BUTTONSTYLE);
        }
        // workplace report type
        if (!getWorkplaceReportType().equals(
            OpenCms.getWorkplaceManager().getDefaultUserSettings().getWorkplaceReportType())) {
            m_user.setAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_WORKPLACEGENERALOPTIONS
                + CmsWorkplaceConfiguration.N_REPORTTYPE, getWorkplaceReportType());
        } else if (cms != null) {
            m_user.deleteAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_WORKPLACEGENERALOPTIONS
                + CmsWorkplaceConfiguration.N_REPORTTYPE);
        }
        // workplace upload applet
        if (useUploadApplet() != OpenCms.getWorkplaceManager().getDefaultUserSettings().useUploadApplet()) {
            m_user.setAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_WORKPLACEGENERALOPTIONS
                + CmsWorkplaceConfiguration.N_UPLOADAPPLET, Boolean.valueOf(useUploadApplet()));
        } else if (cms != null) {
            m_user.deleteAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_WORKPLACEGENERALOPTIONS
                + CmsWorkplaceConfiguration.N_UPLOADAPPLET);
        }
        // list all projects
        if (getListAllProjects() != OpenCms.getWorkplaceManager().getDefaultUserSettings().getListAllProjects()) {
            m_user.setAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_WORKPLACEGENERALOPTIONS
                + CmsWorkplaceConfiguration.N_LISTALLPROJECTS, Boolean.valueOf(getListAllProjects()));
        } else if (cms != null) {
            m_user.deleteAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_WORKPLACEGENERALOPTIONS
                + CmsWorkplaceConfiguration.N_LISTALLPROJECTS);
        }
        // publish notification
        if (getShowPublishNotification() != OpenCms.getWorkplaceManager().getDefaultUserSettings().getShowPublishNotification()) {
            m_user.setAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_WORKPLACEGENERALOPTIONS
                + CmsWorkplaceConfiguration.N_PUBLISHNOTIFICATION, Boolean.valueOf(getShowPublishNotification()));
        } else if (cms != null) {
            m_user.deleteAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_WORKPLACEGENERALOPTIONS
                + CmsWorkplaceConfiguration.N_PUBLISHNOTIFICATION);
        }
        // locale
        if (!getLocale().equals(OpenCms.getWorkplaceManager().getDefaultUserSettings().getLocale())) {
            m_user.setAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_WORKPLACESTARTUPSETTINGS
                + CmsWorkplaceConfiguration.N_LOCALE, getLocale().toString());
        } else if (cms != null) {
            m_user.deleteAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_WORKPLACESTARTUPSETTINGS
                + CmsWorkplaceConfiguration.N_LOCALE);
        }
        // start project       
        if (!getStartProject().equals(OpenCms.getWorkplaceManager().getDefaultUserSettings().getStartProject())) {
            try {
                // be sure the project is valid
                if (cms != null) {
                    cms.readProject(getStartProject());
                }
                m_user.setAdditionalInfo(PREFERENCES
                    + CmsWorkplaceConfiguration.N_WORKPLACESTARTUPSETTINGS
                    + CmsWorkplaceConfiguration.N_PROJECT, getStartProject());
            } catch (Exception e) {
                if (cms != null) {
                    m_user.deleteAdditionalInfo(PREFERENCES
                        + CmsWorkplaceConfiguration.N_WORKPLACESTARTUPSETTINGS
                        + CmsWorkplaceConfiguration.N_PROJECT);
                }
            }
        } else if (cms != null) {
            m_user.deleteAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_WORKPLACESTARTUPSETTINGS
                + CmsWorkplaceConfiguration.N_PROJECT);
        }
        // view
        if (!getStartView().equals(OpenCms.getWorkplaceManager().getDefaultUserSettings().getStartView())) {
            m_user.setAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_WORKPLACESTARTUPSETTINGS
                + CmsWorkplaceConfiguration.N_WORKPLACEVIEW, getStartView());
        } else if (cms != null) {
            m_user.deleteAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_WORKPLACESTARTUPSETTINGS
                + CmsWorkplaceConfiguration.N_WORKPLACEVIEW);
        }
        // start site
        if (!getStartSite().equals(OpenCms.getWorkplaceManager().getDefaultUserSettings().getStartSite())) {
            m_user.setAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_WORKPLACESTARTUPSETTINGS
                + I_CmsXmlConfiguration.N_SITE, getStartSite());
        } else if (cms != null) {
            m_user.deleteAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_WORKPLACESTARTUPSETTINGS
                + I_CmsXmlConfiguration.N_SITE);
        }
        // start folder
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(getStartFolder())
            && !getStartFolder().equals(OpenCms.getWorkplaceManager().getDefaultUserSettings().getStartFolder())) {
            m_user.setAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_WORKPLACESTARTUPSETTINGS
                + CmsWorkplaceConfiguration.N_FOLDER, getStartFolder());
        } else if (cms != null) {
            m_user.deleteAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_WORKPLACESTARTUPSETTINGS
                + CmsWorkplaceConfiguration.N_FOLDER);
        }
        // restrict explorer folder view
        if (getRestrictExplorerView() != OpenCms.getWorkplaceManager().getDefaultUserSettings().getRestrictExplorerView()) {
            m_user.setAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_WORKPLACESTARTUPSETTINGS
                + CmsWorkplaceConfiguration.N_RESTRICTEXPLORERVIEW, Boolean.valueOf(getRestrictExplorerView()));
        } else if (cms != null) {
            m_user.deleteAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_WORKPLACESTARTUPSETTINGS
                + CmsWorkplaceConfiguration.N_RESTRICTEXPLORERVIEW);
        }
        // explorer button style    
        if (getExplorerButtonStyle() != OpenCms.getWorkplaceManager().getDefaultUserSettings().getExplorerButtonStyle()) {
            m_user.setAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_EXPLORERGENERALOPTIONS
                + CmsWorkplaceConfiguration.N_BUTTONSTYLE, new Integer(getExplorerButtonStyle()));
        } else if (cms != null) {
            m_user.deleteAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_EXPLORERGENERALOPTIONS
                + CmsWorkplaceConfiguration.N_BUTTONSTYLE);
        }
        // explorer file entries
        if (getExplorerFileEntries() != OpenCms.getWorkplaceManager().getDefaultUserSettings().getExplorerFileEntries()) {
            m_user.setAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_EXPLORERGENERALOPTIONS
                + CmsWorkplaceConfiguration.N_ENTRIES, new Integer(getExplorerFileEntries()));
        } else if (cms != null) {
            m_user.deleteAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_EXPLORERGENERALOPTIONS
                + CmsWorkplaceConfiguration.N_ENTRIES);
        }
        // explorer settings
        if (getExplorerSettings() != OpenCms.getWorkplaceManager().getDefaultUserSettings().getExplorerSettings()) {
            m_user.setAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_EXPLORERGENERALOPTIONS
                + CmsWorkplaceConfiguration.N_EXPLORERDISPLAYOPTIONS, new Integer(getExplorerSettings()));
        } else if (cms != null) {
            m_user.deleteAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_EXPLORERGENERALOPTIONS
                + CmsWorkplaceConfiguration.N_EXPLORERDISPLAYOPTIONS);
        }
        // dialog file copy mode
        if (getDialogCopyFileMode() != OpenCms.getWorkplaceManager().getDefaultUserSettings().getDialogCopyFileMode()) {
            m_user.setAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_DIALOGSDEFAULTSETTINGS
                + CmsWorkplaceConfiguration.N_FILECOPY, new Integer(getDialogCopyFileMode().getMode()));
        } else if (cms != null) {
            m_user.deleteAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_DIALOGSDEFAULTSETTINGS
                + CmsWorkplaceConfiguration.N_FILECOPY);
        }
        // dialog folder copy mode
        if (getDialogCopyFolderMode() != OpenCms.getWorkplaceManager().getDefaultUserSettings().getDialogCopyFolderMode()) {
            m_user.setAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_DIALOGSDEFAULTSETTINGS
                + CmsWorkplaceConfiguration.N_FOLDERCOPY, new Integer(getDialogCopyFolderMode().getMode()));
        } else if (cms != null) {
            m_user.deleteAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_DIALOGSDEFAULTSETTINGS
                + CmsWorkplaceConfiguration.N_FOLDERCOPY);
        }
        // dialog file delete mode
        if (getDialogDeleteFileMode() != OpenCms.getWorkplaceManager().getDefaultUserSettings().getDialogDeleteFileMode()) {
            m_user.setAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_DIALOGSDEFAULTSETTINGS
                + CmsWorkplaceConfiguration.N_FILEDELETION, new Integer(getDialogDeleteFileMode().getMode()));
        } else if (cms != null) {
            m_user.deleteAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_DIALOGSDEFAULTSETTINGS
                + CmsWorkplaceConfiguration.N_FILEDELETION);
        }
        // dialog direct publish mode
        if (getDialogPublishSiblings() != OpenCms.getWorkplaceManager().getDefaultUserSettings().getDialogPublishSiblings()) {
            m_user.setAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_DIALOGSDEFAULTSETTINGS
                + CmsWorkplaceConfiguration.N_DIRECTPUBLISH, Boolean.valueOf(getDialogPublishSiblings()));
        } else if (cms != null) {
            m_user.deleteAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_DIALOGSDEFAULTSETTINGS
                + CmsWorkplaceConfiguration.N_DIRECTPUBLISH);
        }
        // dialog show lock mode
        if (getDialogShowLock() != OpenCms.getWorkplaceManager().getDefaultUserSettings().getDialogShowLock()) {
            m_user.setAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_DIALOGSDEFAULTSETTINGS
                + CmsWorkplaceConfiguration.N_SHOWLOCK, Boolean.valueOf(getDialogShowLock()));
        } else if (cms != null) {
            m_user.deleteAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_DIALOGSDEFAULTSETTINGS
                + CmsWorkplaceConfiguration.N_SHOWLOCK);
        }
        // dialog permissions inheritation mode
        if (getDialogPermissionsInheritOnFolder() != OpenCms.getWorkplaceManager().getDefaultUserSettings().getDialogPermissionsInheritOnFolder()) {
            m_user.setAdditionalInfo(
                PREFERENCES
                    + CmsWorkplaceConfiguration.N_DIALOGSDEFAULTSETTINGS
                    + CmsWorkplaceConfiguration.N_PERMISSIONSINHERITONFOLDER,
                Boolean.valueOf(getDialogPermissionsInheritOnFolder()));
        } else if (cms != null) {
            m_user.deleteAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_DIALOGSDEFAULTSETTINGS
                + CmsWorkplaceConfiguration.N_PERMISSIONSINHERITONFOLDER);
        }
        // dialog expand inherited permissions mode
        if (getDialogExpandInheritedPermissions() != OpenCms.getWorkplaceManager().getDefaultUserSettings().getDialogExpandInheritedPermissions()) {
            m_user.setAdditionalInfo(
                PREFERENCES
                    + CmsWorkplaceConfiguration.N_DIALOGSDEFAULTSETTINGS
                    + CmsWorkplaceConfiguration.N_EXPANDPERMISSIONSINHERITED,
                Boolean.valueOf(getDialogExpandInheritedPermissions()));
        } else if (cms != null) {
            m_user.deleteAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_DIALOGSDEFAULTSETTINGS
                + CmsWorkplaceConfiguration.N_EXPANDPERMISSIONSINHERITED);
        }
        // dialog expand users permissions mode
        if (getDialogExpandUserPermissions() != OpenCms.getWorkplaceManager().getDefaultUserSettings().getDialogExpandUserPermissions()) {
            m_user.setAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_DIALOGSDEFAULTSETTINGS
                + CmsWorkplaceConfiguration.N_EXPANDPERMISSIONSUSER, Boolean.valueOf(getDialogExpandUserPermissions()));
        } else if (cms != null) {
            m_user.deleteAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_DIALOGSDEFAULTSETTINGS
                + CmsWorkplaceConfiguration.N_EXPANDPERMISSIONSUSER);
        }
        // editor button style    
        if (getEditorButtonStyle() != OpenCms.getWorkplaceManager().getDefaultUserSettings().getEditorButtonStyle()) {
            m_user.setAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_EDITORGENERALOPTIONS
                + CmsWorkplaceConfiguration.N_BUTTONSTYLE, new Integer(getEditorButtonStyle()));
        } else if (cms != null) {
            m_user.deleteAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_EDITORGENERALOPTIONS
                + CmsWorkplaceConfiguration.N_BUTTONSTYLE);
        }
        // direct edit button style    
        if (getDirectEditButtonStyle() != OpenCms.getWorkplaceManager().getDefaultUserSettings().getDirectEditButtonStyle()) {
            m_user.setAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_EDITORGENERALOPTIONS
                + CmsWorkplaceConfiguration.N_DIRECTEDITSTYLE, new Integer(getDirectEditButtonStyle()));
        } else if (cms != null) {
            m_user.deleteAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_EDITORGENERALOPTIONS
                + CmsWorkplaceConfiguration.N_DIRECTEDITSTYLE);
        }
        // editor settings
        if (m_editorSettings.size() > 0) {
            Iterator itEntries = m_editorSettings.entrySet().iterator();
            while (itEntries.hasNext()) {
                Map.Entry entry = (Map.Entry)itEntries.next();
                if (entry.getValue() != null) {
                    m_user.setAdditionalInfo(PREFERENCES
                        + CmsWorkplaceConfiguration.N_EDITORPREFERREDEDITORS
                        + entry.getKey(), entry.getValue().toString());
                } else {
                    m_user.deleteAdditionalInfo(PREFERENCES
                        + CmsWorkplaceConfiguration.N_EDITORPREFERREDEDITORS
                        + entry.getKey());
                }
            }
        } else if (cms != null) {
            Iterator itKeys = m_user.getAdditionalInfo().keySet().iterator();
            while (itKeys.hasNext()) {
                String key = (String)itKeys.next();
                if (key.startsWith(PREFERENCES + CmsWorkplaceConfiguration.N_EDITORPREFERREDEDITORS)) {
                    m_user.deleteAdditionalInfo(key);
                }
            }
        }
        // workplace search
        if (getWorkplaceSearchViewStyle() != null) {
            m_user.setAdditionalInfo(PREFERENCES
                + CmsWorkplaceConfiguration.N_WORKPLACESEARCH
                + CmsWorkplaceConfiguration.N_SEARCHVIEWSTYLE, getWorkplaceSearchViewStyle().toString());
        }
        // synchronize settings        
        if (getSynchronizeSettings() != null) {
            m_user.setAdditionalInfo(
                PREFERENCES + SYNC_SETTINGS + SYNC_ENABLED,
                Boolean.valueOf(getSynchronizeSettings().isEnabled()));
            m_user.setAdditionalInfo(
                PREFERENCES + SYNC_SETTINGS + SYNC_DESTINATION,
                getSynchronizeSettings().getDestinationPathInRfs());
            m_user.setAdditionalInfo(PREFERENCES + SYNC_SETTINGS + SYNC_VFS_LIST, CmsStringUtil.collectionAsString(
                getSynchronizeSettings().getSourceListInVfs(),
                "|"));
        } else {
            m_user.deleteAdditionalInfo(PREFERENCES + SYNC_SETTINGS + SYNC_ENABLED);
            m_user.deleteAdditionalInfo(PREFERENCES + SYNC_SETTINGS + SYNC_DESTINATION);
            m_user.deleteAdditionalInfo(PREFERENCES + SYNC_SETTINGS + SYNC_VFS_LIST);
        }
        // upload applet client folder path
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_uploadAppletClientFolder)) {
            m_user.setAdditionalInfo(ADDITIONAL_INFO_UPLOADAPPLET_CLIENTFOLDER, m_uploadAppletClientFolder);
        } else {
            m_user.deleteAdditionalInfo(ADDITIONAL_INFO_UPLOADAPPLET_CLIENTFOLDER);
        }
        // workplace user surf time (time warp) 
        if (getTimeWarp() != CmsContextInfo.CURRENT_TIME) {
            m_user.setAdditionalInfo(ADDITIONAL_INFO_TIMEWARP, new Long(getTimeWarp()));
        } else if (cms != null) {
            m_user.deleteAdditionalInfo(ADDITIONAL_INFO_TIMEWARP);
        }

        // only write the updated user to the DB if we have the cms object
        if (cms != null) {
            cms.writeUser(m_user);
        }
    }

    /**
     * Sets the default copy mode when copying a file of the user.<p>
     * 
     * @param mode the default copy mode when copying a file of the user
     */
    public void setDialogCopyFileMode(CmsResourceCopyMode mode) {

        m_dialogFileCopy = mode;
    }

    /**
     * Sets the default copy mode when copying a folder of the user.<p>
     * 
     * @param mode the default copy mode when copying a folder of the user
     */
    public void setDialogCopyFolderMode(CmsResourceCopyMode mode) {

        m_dialogFolderCopy = mode;
    }

    /**
     * Sets the default setting for file deletion.<p>
     * 
     * @param mode the default setting for file deletion
     */
    public void setDialogDeleteFileMode(CmsResourceDeleteMode mode) {

        m_dialogFileDelete = mode;
    }

    /**
     * Sets the default setting for expanding inherited permissions in the dialog.<p>
     *
     * @param dialogShowInheritedPermissions the default setting for expanding inherited permissions in the dialog
     */
    public void setDialogExpandInheritedPermissions(boolean dialogShowInheritedPermissions) {

        m_dialogExpandInheritedPermissions = dialogShowInheritedPermissions;
    }

    /**
     * Sets the default setting for expanding the users permissions in the dialog.<p>
     *
     * @param dialogShowUserPermissions the default setting for expanding the users permissions in the dialog
     */
    public void setDialogExpandUserPermissions(boolean dialogShowUserPermissions) {

        m_dialogExpandUserPermissions = dialogShowUserPermissions;
    }

    /**
     * Sets the default setting for inheriting permissions on folders.<p>
     *
     * @param dialogPermissionsInheritOnFolder the default setting for inheriting permissions on folders
     */
    public void setDialogPermissionsInheritOnFolder(boolean dialogPermissionsInheritOnFolder) {

        m_dialogPermissionsInheritOnFolder = dialogPermissionsInheritOnFolder;
    }

    /**
     * Sets the default setting for direct publishing.<p>
     * 
     * @param publishSiblings the default setting for direct publishing: true if siblings should be published, otherwise false
     */
    public void setDialogPublishSiblings(boolean publishSiblings) {

        m_dialogDirectpublish = publishSiblings;
    }

    /**
     *  Sets if the export setting part of the secure/export dialog should be shown.<p>
     * 
     * @param show true if the export dialog should be shown, otherwise false
     */
    public void setDialogShowExportSettings(boolean show) {

        m_showExportSettings = show;
    }

    /**
     *  Sets if the lock dialog should be shown.<p>
     * 
     * @param show true if the lock dialog should be shown, otherwise false
     */
    public void setDialogShowLock(boolean show) {

        m_showLock = show;
    }

    /**
     * Sets the style of the direct edit buttons of the user.<p>
     * 
     * @param style the style of the direct edit buttons of the user
     */
    public void setDirectEditButtonStyle(int style) {

        m_directeditButtonStyle = style;
    }

    /**
     * Sets the style of the editor buttons of the user.<p>
     * 
     * @param style the style of the editor buttons of the user
     */
    public void setEditorButtonStyle(int style) {

        m_editorButtonStyle = style;
    }

    /**
     * Sets the editor settings of the user.<p>
     * 
     * @param settings the editor settings of the user
     */
    public void setEditorSettings(Map settings) {

        m_editorSettings = new TreeMap(settings);
    }

    /**
     * Sets the style of the explorer buttons of the user.<p>
     * 
     * @param style the style of the explorer buttons of the user
     */
    public void setExplorerButtonStyle(int style) {

        m_explorerButtonStyle = style;
    }

    /**
     * Sets the number of displayed files per page of the user.<p>
     * 
     * @param entries the number of displayed files per page of the user
     */
    public void setExplorerFileEntries(int entries) {

        m_explorerFileEntries = entries;
    }

    /**
     * Sets the explorer start settings.<p>
     * 
     * @param settings explorer start settings to use
     */
    public void setExplorerSettings(int settings) {

        m_explorerSettings = settings;
    }

    /**
     * Sets the exporerFileEntryOptions.<p>
     *
     * @param exporerFileEntryOptions the exporerFileEntryOptions to set
     */
    public void setExporerFileEntryOptions(String exporerFileEntryOptions) {

        m_exporerFileEntryOptions = exporerFileEntryOptions;
    }

    /**
     * Sets if all the projects should be shown or not.<p>
     * 
     * @param listAllProjects true if all the projects should be shown, otherwise false
     */
    public void setListAllProjects(boolean listAllProjects) {

        m_listAllProjects = listAllProjects;
    }

    /**
     * Sets the locale of the user.<p>
     * 
     * @param locale the locale of the user
     */
    public void setLocale(Locale locale) {

        m_locale = locale;
    }

    /**
     * Sets if the "create index page" check box in the new folder 
     * dialog should be initially be checked or not. <p>
     * 
     * @param setting if the "create index page" check box in the new folder 
     *      dialog should be initially be checked or not.
     */
    public void setNewFolderCreateIndexPage(Boolean setting) {

        m_newFolderCreateIndexPage = setting;
    }

    /**
     * Sets if the "edit properties" check box in the new folder 
     * dialog should be initially be checked or not. <p>
     * 
     * @param setting if the "edit properties" check box in the new folder 
     *      dialog should be initially be checked or not.
     */
    public void setNewFolderEditPropertes(Boolean setting) {

        m_newFolderEditProperties = setting;
    }

    /**
     * Sets the preferred editor for the given resource type of the user.<p>
     * 
     * @param resourceType the resource type
     * @param editorUri the editor URI
     */
    public void setPreferredEditor(String resourceType, String editorUri) {

        if (editorUri == null) {
            m_editorSettings.remove(resourceType);
        }
        m_editorSettings.put(resourceType, editorUri);
    }

    /**
     * Sets the appearance of the "publish project" button.<p>
     * 
     * Allowed values are either {@link CmsDefaultUserSettings#PUBLISHBUTTON_SHOW_ALWAYS}, 
     * {@link CmsDefaultUserSettings#PUBLISHBUTTON_SHOW_AUTO} or 
     * {@link CmsDefaultUserSettings#PUBLISHBUTTON_SHOW_NEVER}.<p>
     * 
     * @param publishButtonAppearance the appearance of the "publish project" button
     */
    public void setPublishButtonAppearance(String publishButtonAppearance) {

        String value = CmsDefaultUserSettings.PUBLISHBUTTON_SHOW_ALWAYS;
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(publishButtonAppearance)) {
            publishButtonAppearance = publishButtonAppearance.trim().toLowerCase();
            if (CmsDefaultUserSettings.PUBLISHBUTTON_SHOW_AUTO.equals(publishButtonAppearance)) {
                value = CmsDefaultUserSettings.PUBLISHBUTTON_SHOW_AUTO;
            } else if (CmsDefaultUserSettings.PUBLISHBUTTON_SHOW_NEVER.equals(publishButtonAppearance)) {
                value = CmsDefaultUserSettings.PUBLISHBUTTON_SHOW_NEVER;
            }
        }
        m_publishButtonAppearance = value;
    }

    /**
     * Sets if the explorer view is restricted to the defined site and folder.<p>
     * 
     * @param restrict true if the explorer view is restricted, otherwise false
     */
    public void setRestrictExplorerView(boolean restrict) {

        m_restrictExplorerView = restrict;
    }

    /**
     * Sets if the file creation date should be shown in explorer view.<p>
     * 
     * @param show true if the file creation date should be shown, otherwise false
     */
    public void setShowExplorerFileDateCreated(boolean show) {

        setExplorerSetting(show, CmsUserSettings.FILELIST_DATE_CREATED);
    }

    /**
     * Sets if the file expire date should be shown in explorer view.<p>
     * 
     * @param show true if the file expire date should be shown, otherwise false
     */
    public void setShowExplorerFileDateExpired(boolean show) {

        setExplorerSetting(show, CmsUserSettings.FILELIST_DATE_EXPIRED);
    }

    /**
     * Sets if the file last modified date state should be shown in explorer view.<p>
     * 
     * @param show true if the file last modified date should be shown, otherwise false
     */
    public void setShowExplorerFileDateLastModified(boolean show) {

        setExplorerSetting(show, CmsUserSettings.FILELIST_DATE_LASTMODIFIED);
    }

    /**
     * Sets if the file release date should be shown in explorer view.<p>
     * 
     * @param show true if the file release date should be shown, otherwise false
     */
    public void setShowExplorerFileDateReleased(boolean show) {

        setExplorerSetting(show, CmsUserSettings.FILELIST_DATE_RELEASED);
    }

    /**
     * Sets if the file locked by should be shown in explorer view.<p>
     * 
     * @param show true if the file locked by should be shown, otherwise false
     */
    public void setShowExplorerFileLockedBy(boolean show) {

        setExplorerSetting(show, CmsUserSettings.FILELIST_LOCKEDBY);
    }

    /**
     * Sets if the file navtext should be shown in explorer view.<p>
     * 
     * @param show true if the file navtext should be shown, otherwise false
     */
    public void setShowExplorerFileNavText(boolean show) {

        setExplorerSetting(show, CmsUserSettings.FILELIST_NAVTEXT);
    }

    /**
     * Sets if the file permissions should be shown in explorer view.<p>
     * 
     * @param show true if the file permissions should be shown, otherwise false
     */
    public void setShowExplorerFilePermissions(boolean show) {

        setExplorerSetting(show, CmsUserSettings.FILELIST_PERMISSIONS);
    }

    /**
     * Sets if the file size should be shown in explorer view.<p>
     * 
     * @param show true if the file size should be shown, otherwise false
     */
    public void setShowExplorerFileSize(boolean show) {

        setExplorerSetting(show, CmsUserSettings.FILELIST_SIZE);
    }

    /**
     * Sets if the file state should be shown in explorer view.<p>
     * 
     * @param show true if the state size should be shown, otherwise false
     */
    public void setShowExplorerFileState(boolean show) {

        setExplorerSetting(show, CmsUserSettings.FILELIST_STATE);
    }

    /**
     * Sets if the file title should be shown in explorer view.<p>
     * 
     * @param show true if the file title should be shown, otherwise false
     */
    public void setShowExplorerFileTitle(boolean show) {

        setExplorerSetting(show, CmsUserSettings.FILELIST_TITLE);
    }

    /**
     * Sets if the file type should be shown in explorer view.<p>
     * 
     * @param show true if the file type should be shown, otherwise false
     */
    public void setShowExplorerFileType(boolean show) {

        setExplorerSetting(show, CmsUserSettings.FILELIST_TYPE);
    }

    /**
     * Sets if the file creator should be shown in explorer view.<p>
     * 
     * @param show true if the file creator should be shown, otherwise false
     */
    public void setShowExplorerFileUserCreated(boolean show) {

        setExplorerSetting(show, CmsUserSettings.FILELIST_USER_CREATED);
    }

    /**
     * Sets if the file last modified by should be shown in explorer view.<p>
     * 
     * @param show true if the file last modified by should be shown, otherwise false
     */
    public void setShowExplorerFileUserLastModified(boolean show) {

        setExplorerSetting(show, CmsUserSettings.FILELIST_USER_LASTMODIFIED);
    }

    /**
     * Controls whether to display a file upload icon or not.<p>
     * 
     * @param flag <code>true</code> or <code>false</code> to flag the use of the file upload button
     */
    public void setShowFileUploadButton(boolean flag) {

        m_showFileUploadButton = flag;
    }

    /**
     * Sets if the publish notifications should be shown or not.<p>
     * 
     * @param showPublishNotification true if the publish notifications should be shown, otherwise false
     */
    public void setShowPublishNotification(boolean showPublishNotification) {

        m_showPublishNotification = showPublishNotification;
    }

    /**
     * Sets if the resource type selection dialog should 
     * be shown in the file upload process (non - applet version) or not. <p>
     * 
     * @param showUploadTypeDialog if the resource type selection dialog should 
     *      be shown in the file upload process (non - applet version)
     */
    public void setShowUploadTypeDialog(Boolean showUploadTypeDialog) {

        m_showUploadTypeDialog = showUploadTypeDialog;
    }

    /**
     * Sets the start folder of the user.<p>
     * 
     * @param folder the start folder of the user
     */
    public void setStartFolder(String folder) {

        if (!folder.startsWith("/")) {
            folder = "/" + folder;
        }
        if (!folder.endsWith("/")) {
            folder = folder + "/";
        }
        m_startFolder = folder;
    }

    /**
     * Sets the start project of the user.<p>
     * 
     * @param project the start project id of the user
     */
    public void setStartProject(String project) {

        m_project = project;
    }

    /**
     * Sets the start site of the user.<p>
     * 
     * @param site the start site of the user
     */
    public void setStartSite(String site) {

        m_startSite = site;
    }

    /**
     * Sets the current start view of the user.<p>
     * 
     * @param view the current start view of the user
     */
    public void setStartView(String view) {

        m_view = view;
    }

    /**
     * Sets the (optional) workplace synchronize settings.<p>
     *
     * @param synchronizeSettings the (optional) workplace synchronize settings to set
     */
    public void setSynchronizeSettings(CmsSynchronizeSettings synchronizeSettings) {

        m_synchronizeSettings = synchronizeSettings;
    }

    /**
     * Sets the user specific custom "time warp" time.<p>
     * 
     * Use {@link org.opencms.main.CmsContextInfo#CURRENT_TIME} to disable this feature, ie. enable the
     * current time for each new request.<p>
     * 
     * If this value is set, auto time warping will be disabled: Clicking on a resource that 
     * has not been released at the given time or is already expired at the given time will not 
     * be shown - an error message will pop up  ("out of time window").<p>
     * 
     * @param timewarp the time warp time to set
     */
    public void setTimeWarp(long timewarp) {

        m_timeWarp = timewarp;
    }

    /**
     * Sets the folder path  of the upload applet on the client machine.<p>
     *
     * @param uploadAppletClientFolder the folder path  of the upload applet on the client machine
     */
    public void setUploadAppletClientFolder(String uploadAppletClientFolder) {

        m_uploadAppletClientFolder = uploadAppletClientFolder;
    }

    /**
     * Sets the current user for the settings.<p>
     * 
     * @param user the CmsUser
     */
    public void setUser(CmsUser user) {

        m_user = user;
    }

    /**
     *  Sets if the upload applet should be used.<p>
     * 
     * @param use true if the upload applet should be used, otherwise false
     */
    public void setUseUploadApplet(boolean use) {

        m_uploadApplet = use;
    }

    /**
     * Sets the style of the workplace buttons of the user.<p>
     * 
     * @param style the style of the workplace buttons of the user
     */
    public void setWorkplaceButtonStyle(int style) {

        m_workplaceButtonStyle = style;
    }

    /**
     * Sets the type of the report (simple or extended) of the user.<p>
     * 
     * @param type the type of the report (simple or extended) of the user
     */
    public void setWorkplaceReportType(String type) {

        m_workplaceReportType = type;
    }

    /**
     * Sets the name of the search index to use in the workplace.<p>
     *
     * @param workplaceSearchIndexName the name of the search index to use in the workplace to set
     */
    public void setWorkplaceSearchIndexName(String workplaceSearchIndexName) {

        m_workplaceSearchIndexName = workplaceSearchIndexName;
    }

    /**
     * Sets the workplace search result list view style.<p>
     *
     * @param workplaceSearchViewStyle the workplace search result list view style to set
     */
    public void setWorkplaceSearchViewStyle(CmsSearchResultStyle workplaceSearchViewStyle) {

        m_workplaceSearchViewStyle = workplaceSearchViewStyle;
    }

    /**
     * Determines if the file creation date should be shown in explorer view.<p>
     * 
     * @return true if the file creation date should be shown, otherwise false
     */
    public boolean showExplorerFileDateCreated() {

        return ((m_explorerSettings & CmsUserSettings.FILELIST_DATE_CREATED) > 0);
    }

    /**
     * Determines if the file date expired should be shown in explorer view.<p>
     * 
     * @return true if the file date expired should be shown, otherwise false
     */
    public boolean showExplorerFileDateExpired() {

        return ((m_explorerSettings & CmsUserSettings.FILELIST_DATE_EXPIRED) > 0);
    }

    /**
     * Determines if the file last modified date should be shown in explorer view.<p>
     * 
     * @return true if the file last modified date should be shown, otherwise false
     */
    public boolean showExplorerFileDateLastModified() {

        return ((m_explorerSettings & CmsUserSettings.FILELIST_DATE_LASTMODIFIED) > 0);
    }

    /**
     * Determines if the file date released should be shown in explorer view.<p>
     * 
     * @return true if the file date released should be shown, otherwise false
     */
    public boolean showExplorerFileDateReleased() {

        return ((m_explorerSettings & CmsUserSettings.FILELIST_DATE_RELEASED) > 0);
    }

    /**
     * Determines if the file locked by should be shown in explorer view.<p>
     * 
     * @return true if the file locked by should be shown, otherwise false
     */
    public boolean showExplorerFileLockedBy() {

        return ((m_explorerSettings & CmsUserSettings.FILELIST_LOCKEDBY) > 0);
    }

    /**
     * Determines if the file navigation text should be shown in explorer view.<p>
     * 
     * @return true if the file navigation text should be shown, otherwise false
     */
    public boolean showExplorerFileNavText() {

        return ((m_explorerSettings & CmsUserSettings.FILELIST_NAVTEXT) > 0);
    }

    /**
     * Determines if the file permissions should be shown in explorer view.<p>
     * 
     * @return true if the file permissions should be shown, otherwise false
     */
    public boolean showExplorerFilePermissions() {

        return ((m_explorerSettings & CmsUserSettings.FILELIST_PERMISSIONS) > 0);
    }

    /**
     * Determines if the file size should be shown in explorer view.<p>
     * 
     * @return true if the file size should be shown, otherwise false
     */
    public boolean showExplorerFileSize() {

        return ((m_explorerSettings & CmsUserSettings.FILELIST_SIZE) > 0);
    }

    /**
     * Determines if the file state should be shown in explorer view.<p>
     * 
     * @return true if the file state should be shown, otherwise false
     */
    public boolean showExplorerFileState() {

        return ((m_explorerSettings & CmsUserSettings.FILELIST_STATE) > 0);
    }

    /**
     * Determines if the file title should be shown in explorer view.<p>
     * 
     * @return true if the file title should be shown, otherwise false
     */
    public boolean showExplorerFileTitle() {

        return ((m_explorerSettings & CmsUserSettings.FILELIST_TITLE) > 0);
    }

    /**
     * Determines if the file type should be shown in explorer view.<p>
     * 
     * @return true if the file type should be shown, otherwise false
     */
    public boolean showExplorerFileType() {

        return ((m_explorerSettings & CmsUserSettings.FILELIST_TYPE) > 0);
    }

    /**
     * Determines if the file creator should be shown in explorer view.<p>
     * 
     * @return true if the file creator should be shown, otherwise false
     */
    public boolean showExplorerFileUserCreated() {

        return ((m_explorerSettings & CmsUserSettings.FILELIST_USER_CREATED) > 0);
    }

    /**
     * Determines if the file last modified by should be shown in explorer view.<p>
     * 
     * @return true if the file last modified by should be shown, otherwise false
     */
    public boolean showExplorerFileUserLastModified() {

        return ((m_explorerSettings & CmsUserSettings.FILELIST_USER_LASTMODIFIED) > 0);
    }

    /**
     * Determines if the upload applet should be used.<p>
     * 
     * @return true if the if the upload applet should be used, otherwise false
     */
    public boolean useUploadApplet() {

        return m_uploadApplet;
    }

    /**
     * Sets a specific explorer setting depending on the set parameter.<p>
     * 
     * @param set true if the setting should be set, otherwise false
     * @param setting the settings constant value for the explorer settings
     */
    private void setExplorerSetting(boolean set, int setting) {

        if (set) {
            m_explorerSettings |= setting;
        } else {
            m_explorerSettings &= ~setting;
        }
    }
}