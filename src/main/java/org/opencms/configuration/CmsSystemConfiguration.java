/*
 * File   : $Source: /usr/local/cvs/opencms/src/org/opencms/configuration/CmsSystemConfiguration.java,v $
 * Date   : $Date: 2008-07-01 13:17:16 $
 * Version: $Revision: 1.48 $
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

package org.opencms.configuration;

import org.opencms.db.CmsCacheSettings;
import org.opencms.db.CmsDefaultUsers;
import org.opencms.db.CmsLoginManager;
import org.opencms.db.CmsLoginMessage;
import org.opencms.db.I_CmsDbContextFactory;
import org.opencms.flex.CmsFlexCacheConfiguration;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.mail.CmsMailHost;
import org.opencms.mail.CmsMailSettings;
import org.opencms.main.CmsContextInfo;
import org.opencms.main.CmsDefaultSessionStorageProvider;
import org.opencms.main.CmsEventManager;
import org.opencms.main.CmsHttpAuthenticationSettings;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsServletContainerSettings;
import org.opencms.main.I_CmsSessionStorageProvider;
import org.opencms.main.I_CmsRequestHandler;
import org.opencms.main.I_CmsResourceInit;
import org.opencms.main.OpenCms;
import org.opencms.monitor.CmsMemoryMonitorConfiguration;
import org.opencms.publish.CmsPublishManager;
import org.opencms.scheduler.CmsScheduleManager;
import org.opencms.scheduler.CmsScheduledJobInfo;
import org.opencms.security.CmsDefaultAuthorizationHandler;
import org.opencms.security.CmsDefaultValidationHandler;
import org.opencms.security.CmsRoleViolationException;
import org.opencms.security.I_CmsAuthorizationHandler;
import org.opencms.security.I_CmsPasswordHandler;
import org.opencms.security.I_CmsValidationHandler;
import org.opencms.site.CmsSite;
import org.opencms.site.CmsSiteManagerImpl;
import org.opencms.site.CmsSiteMatcher;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.digester.Digester;
import org.apache.commons.logging.Log;

import org.dom4j.Element;

/**
 * System master configuration class.<p>
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.48 $
 * 
 * @since 6.0.0
 */
public class CmsSystemConfiguration extends A_CmsXmlConfiguration implements I_CmsXmlConfiguration {

    /** The "error" attribute. */
    public static final String A_ERROR = "error";

    /** The "exclusive" attribute. */
    public static final String A_EXCLUSIVE = "exclusive";

    /** The "mode" attribute. */
    public static final String A_MODE = "mode";

    /** The attribute name for the alias offset. */
    public static final String A_OFFSET = "offset";

    /** The "server" attribute. */
    public static final String A_SERVER = "server";

    /** The name of the DTD for this configuration. */
    public static final String CONFIGURATION_DTD_NAME = "opencms-system.dtd";

    /** The name of the default XML file for this configuration. */
    public static final String DEFAULT_XML_FILE_NAME = "opencms-system.xml";

    /** The node name for the job "active" value. */
    public static final String N_ACTIVE = "active";

    /** The node name for the alias node. */
    public static final String N_ALIAS = "alias";

    /** The node name for the authorization handler. */
    public static final String N_AUTHORIZATIONHANDLER = "authorizationhandler";

    /** The node name for the avgcachebytes node. */
    public static final String N_AVGCACHEBYTES = "avgcachebytes";

    /** The node name for the browser-based node. */
    public static final String N_BROWSER_BASED = "browser-based";

    /** the result cache node. */
    public static final String N_CACHE = "resultcache";

    /** The node name for the cache-enabled node. */
    public static final String N_CACHE_ENABLED = "cache-enabled";

    /** The node name for the cache-offline node. */
    public static final String N_CACHE_OFFLINE = "cache-offline";

    /** The node name for a job class. */
    public static final String N_CLASS = "class";

    /** The duration after which responsibles will be notified about out-dated content. */
    public static final String N_CONTENT_NOTIFICATION = "content-notification";

    /** The node name for the job context. */
    public static final String N_CONTEXT = "context";

    /** The node name for the job cron expression. */
    public static final String N_CRONEXPRESSION = "cronexpression";

    /** The node name for the defaultcontentencoding node. */
    public static final String N_DEFAULT_CONTENT_ENCODING = "defaultcontentencoding";

    /** The node name for the default-uri node. */
    public static final String N_DEFAULT_URI = "default-uri";

    /** The node name for the defaultusers expression. */
    public static final String N_DEFAULTUSERS = "defaultusers";

    /** The node name for the digest type. */
    public static final String N_DIGESTTYPE = "digest-type";

    /** The node name for the login account lock minutes.  */
    public static final String N_DISABLEMINUTES = "disableMinutes";

    /** The node name for the email-interval node. */
    public static final String N_EMAIL_INTERVAL = "email-interval";

    /** The node name for the email-receiver node. */
    public static final String N_EMAIL_RECEIVER = "email-receiver";

    /** The node name for the email-sender node. */
    public static final String N_EMAIL_SENDER = "email-sender";

    /** The node name for the login message enabled flag. */
    public static final String N_ENABLED = "enabled";

    /** The node name for the context encoding. */
    public static final String N_ENCODING = "encoding";

    /** The node name for the request handler classes. */
    public static final String N_EVENTMANAGER = "eventmanager";

    /** The node name for the events node. */
    public static final String N_EVENTS = "events";

    /** The node name for the flexcache node. */
    public static final String N_FLEXCACHE = "flexcache";

    /** The node name for the form-based node. */
    public static final String N_FORM_BASED = "form-based";

    /** The node name for the group-administrators node. */
    public static final String N_GROUP_ADMINISTRATORS = "group-administrators";

    /** The node name for the group-guests node. */
    public static final String N_GROUP_GUESTS = "group-guests";

    /** The node name for the group-projectmanagers node. */
    public static final String N_GROUP_PROJECTMANAGERS = "group-projectmanagers";

    /** The node name for the group-users node. */
    public static final String N_GROUP_USERS = "group-users";

    /** The node name for the publish "history-size" value. */
    public static final String N_HISTORYSIZE = "history-size";

    /** The node name for the http-authentication node. */
    public static final String N_HTTP_AUTHENTICATION = "http-authentication";

    /** The node name for the internationalization node. */
    public static final String N_I18N = "internationalization";

    /** The node name for a job. */
    public static final String N_JOB = "job";

    /** The name of the class to generate cache keys. */
    public static final String N_KEYGENERATOR = "keygenerator";

    /** The node name for individual locales. */
    public static final String N_LOCALE = "locale";

    /** The node name for the locale handler. */
    public static final String N_LOCALEHANDLER = "localehandler";

    /** The node name for the configured locales. */
    public static final String N_LOCALESCONFIGURED = "localesconfigured";

    /** The node name for the default locale(s). */
    public static final String N_LOCALESDEFAULT = "localesdefault";

    /** The node name for the log-interval node. */
    public static final String N_LOG_INTERVAL = "log-interval";

    /** The node name for the login message login forbidden flag. */
    public static final String N_LOGINFORBIDDEN = "loginForbidden";

    /** The node name for the login manager. */
    public static final String N_LOGINMANAGER = "loginmanager";

    /** The node name for the login message. */
    public static final String N_LOGINMESSAGE = "loginmessage";

    /** The node name for the mail configuration. */
    public static final String N_MAIL = "mail";

    /** The node name for the "mail from" node. */
    public static final String N_MAILFROM = "mailfrom";

    /** The node name for the "mail host" node. */
    public static final String N_MAILHOST = "mailhost";

    /** The node name for the login manager bad attempt count. */
    public static final String N_MAXBADATTEMPTS = "maxBadAttempts";

    /** The node name for the maxcachebytes node. */
    public static final String N_MAXCACHEBYTES = "maxcachebytes";

    /** The node name for the maxentrybytes node. */
    public static final String N_MAXENTRYBYTES = "maxentrybytes";

    /** The node name for the maxkeys node. */
    public static final String N_MAXKEYS = "maxkeys";

    /** The node name for the maxusagepercent node. */
    public static final String N_MAXUSAGE_PERCENT = "maxusagepercent";

    /** The node name for the memorymonitor node. */
    public static final String N_MEMORYMONITOR = "memorymonitor";

    /** The node name for the login message text. */
    public static final String N_MESSAGE = "message";

    /** The duration after which responsibles will be notified about out-dated content. */
    public static final String N_NOTIFICATION_PROJECT = "notification-project";

    /** The duration after which responsibles will be notified about out-dated content. */
    public static final String N_NOTIFICATION_TIME = "notification-time";

    /** The node name for the job parameters. */
    public static final String N_PARAMETERS = "parameters";

    /** The node name for the password encoding. */
    public static final String N_PASSWORDENCODING = "encoding";

    /** The node name for the password handler. */
    public static final String N_PASSWORDHANDLER = "passwordhandler";

    /** The node name for the permission handler. */
    public static final String N_PERMISSIONHANDLER = "permissionhandler";

    /** The node name for the prevent-response-flush node. */
    public static final String N_PREVENTRESPONSEFLUSH = "prevent-response-flush";

    /** The node name for the context project name. */
    public static final String N_PROJECT = "project";

    /** The node name for the "publishhistory" section. */
    public static final String N_PUBLISHMANAGER = "publishmanager";

    /** The node name for the "publishhistory" section. */
    public static final String N_QUEUEPERSISTANCE = "queue-persistance";

    /** The node name for the "publishhistory" section. */
    public static final String N_QUEUESHUTDOWNTIME = "queue-shutdowntime";

    /** The node name for the memory email receiver. */
    public static final String N_RECEIVER = "receiver";

    /** The node name for the release-tags-after-end node. */
    public static final String N_RELEASETAGSAFTEREND = "release-tags-after-end";

    /** The node name for the context remote addr. */
    public static final String N_REMOTEADDR = "remoteaddr";

    /** The node name for the context requested uri. */
    public static final String N_REQUESTEDURI = "requesteduri";

    /** The node name for the request-error-page-attribute node. */
    public static final String N_REQUESTERRORPAGEATTRIBUTE = "request-error-page-attribute";

    /** The node name for the request handler classes. */
    public static final String N_REQUESTHANDLER = "requesthandler";

    /** The node name for the request handlers. */
    public static final String N_REQUESTHANDLERS = "requesthandlers";

    /** The node name for the resource init classes. */
    public static final String N_RESOURCEINIT = "resourceinit";

    /** The node name for the resource init classes. */
    public static final String N_RESOURCEINITHANDLER = "resourceinithandler";

    /** The node name for the job "reuseinstance" value. */
    public static final String N_REUSEINSTANCE = "reuseinstance";

    /** The node name for the runtime info. */
    public static final String N_RUNTIMECLASSES = "runtimeclasses";

    /** The node name for the runtime info factory. */
    public static final String N_RUNTIMEINFO = "runtimeinfo";

    /** The node name for the runtime properties node. */
    public static final String N_RUNTIMEPROPERTIES = "runtimeproperties";

    /** The node name for the scheduler. */
    public static final String N_SCHEDULER = "scheduler";

    /** The node name for the secure site. */
    public static final String N_SECURE = "secure";

    /** The node name for the servlet container settings. */
    public static final String N_SERVLETCONTAINERSETTINGS = "servletcontainer-settings";

    /** The node name for the session-storageprovider node. */
    public static final String N_SESSION_STORAGEPROVIDER = "session-storageprovider";

    /** The node name for the context site root. */
    public static final String N_SITEROOT = "siteroot";

    /** The node name for the sites node. */
    public static final String N_SITES = "sites";

    /** The size of the driver manager's cache for ACLS. */
    public static final String N_SIZE_ACLS = "size-accesscontrollists";

    /** The size of the driver manager's cache for groups. */
    public static final String N_SIZE_GROUPS = "size-groups";

    /** The size of the driver manager's cache for organizational units. */
    public static final String N_SIZE_ORGUNITS = "size-orgunits";

    /** The size of the security manager's cache for permission checks. */
    public static final String N_SIZE_PERMISSIONS = "size-permissions";

    /** The size of the driver manager's cache for project resources. */
    public static final String N_SIZE_PROJECTRESOURCES = "size-projectresources";

    /** The size of the driver manager's cache for projects. */
    public static final String N_SIZE_PROJECTS = "size-projects";

    /** The size of the driver manager's cache for properties. */
    public static final String N_SIZE_PROPERTIES = "size-properties";

    /** The size of the driver manager's cache for property lists. */
    public static final String N_SIZE_PROPERTYLISTS = "size-propertylists";

    /** The size of the driver manager's cache for lists of resources. */
    public static final String N_SIZE_RESOURCELISTS = "size-resourcelists";

    /** The size of the driver manager's cache for resources. */
    public static final String N_SIZE_RESOURCES = "size-resources";

    /** The size of the driver manager's cache for roles. */
    public static final String N_SIZE_ROLES = "size-roles";

    /** The size of the driver manager's cache for user/group relations. */
    public static final String N_SIZE_USERGROUPS = "size-usergroups";

    /** The size of the driver manager's cache for users. */
    public static final String N_SIZE_USERS = "size-users";

    /** The main system configuration node name. */
    public static final String N_SYSTEM = "system";

    /** The node name for the login message end time. */
    public static final String N_TIMEEND = "timeEnd";

    /** The node name for the login message start time. */
    public static final String N_TIMESTART = "timeStart";

    /** The node name for the user-admin node. */
    public static final String N_USER_ADMIN = "user-admin";

    /** The node name for the user-deletedresource node. */
    public static final String N_USER_DELETEDRESOURCE = "user-deletedresource";

    /** The node name for the user-export node. */
    public static final String N_USER_EXPORT = "user-export";

    /** The node name for the user-guest node. */
    public static final String N_USER_GUEST = "user-guest";

    /** The node name for the context user name. */
    public static final String N_USERNAME = "user";

    /** The node name for the validation handler. */
    public static final String N_VALIDATIONHANDLER = "validationhandler";

    /** The node name for the version history. */
    public static final String N_VERSIONHISTORY = "versionhistory";

    /** The node name for the warning-interval node. */
    public static final String N_WARNING_INTERVAL = "warning-interval";

    /** The node name for the workplace-server node. */
    public static final String N_WORKPLACE_SERVER = "workplace-server";

    /** The attribute name for the deleted node. */
    private static final String A_DELETED = "deleted";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSystemConfiguration.class);

    /** The authorization handler. */
    private String m_authorizationHandler;

    /** The settings of the driver manager. */
    private CmsCacheSettings m_cacheSettings;

    /** The configured OpenCms default users and groups. */
    private CmsDefaultUsers m_cmsDefaultUsers;

    /** The flex cache configuration object. */
    private CmsFlexCacheConfiguration m_cmsFlexCacheConfiguration;

    /** The memory monitor configuration. */
    private CmsMemoryMonitorConfiguration m_cmsMemoryMonitorConfiguration;

    /** The list of jobs for the scheduler. */
    private List m_configuredJobs;

    /** The default content encoding. */
    private String m_defaultContentEncoding;

    /** The configured OpenCms event manager. */
    private CmsEventManager m_eventManager;

    /** Indicates if the version history is enabled. */
    private boolean m_historyEnabled;

    /** The maximum number of historical versions per resource. */
    private int m_historyVersions;

    /** The maximum number of historical versions for deleted resources. */
    private int m_historyVersionsAfterDeletion;

    /** The HTTP basic authentication settings. */
    private CmsHttpAuthenticationSettings m_httpAuthenticationSettings;

    /** The configured locale manager for multi language support. */
    private CmsLocaleManager m_localeManager;

    /** The configured login manager. */
    private CmsLoginManager m_loginManager;

    /** The configured login message. */
    private CmsLoginMessage m_loginMessage;

    /** The mail settings. */
    private CmsMailSettings m_mailSettings;

    /** Notification project. */
    private String m_notificationProject;

    /** The duration after which responsibles will be notified about out-dated content (in days). */
    // It is an Integer object so that it can be distinguished if this optional element was set or not
    private Integer m_notificationTime;

    /** The password handler. */
    private I_CmsPasswordHandler m_passwordHandler;

    /** The permission handler. */
    private String m_permissionHandler;

    /** The configured publish manager. */
    private CmsPublishManager m_publishManager;

    /** A list of instantiated request handler classes. */
    private List m_requestHandlers;

    /** A list of instantiated resource init handler classes. */
    private List m_resourceInitHandlers;

    /** The runtime info factory. */
    private I_CmsDbContextFactory m_runtimeInfoFactory;

    /** The runtime properties. */
    private Map m_runtimeProperties;

    /** The configured schedule manager. */
    private CmsScheduleManager m_scheduleManager;

    /** The configured session storage provider class name. */
    private String m_sessionStorageProvider;

    /** The configured site manager. */
    private CmsSiteManagerImpl m_siteManager;

    /** The temporary file project id. */
    private int m_tempFileProjectId;

    /** The configured validation handler. */
    private String m_validationHandler;

    /**
     * Public constructor, will be called by configuration manager.<p> 
     */
    public CmsSystemConfiguration() {

        setXmlFileName(DEFAULT_XML_FILE_NAME);
        m_historyEnabled = true;
        m_historyVersions = 10;
        m_historyVersionsAfterDeletion = -1; // use m_historyVersions instead
        m_resourceInitHandlers = new ArrayList();
        m_requestHandlers = new ArrayList();
        m_configuredJobs = new ArrayList();
        m_runtimeProperties = new HashMap();
        m_eventManager = new CmsEventManager();
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_SYSTEM_CONFIG_INIT_0));
        }
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#addConfigurationParameter(java.lang.String, java.lang.String)
     */
    public void addConfigurationParameter(String paramName, String paramValue) {

        m_runtimeProperties.put(paramName, paramValue);
    }

    /**
     * Adds the event manager class.<p>
     * 
     * @param clazz the class name of event manager class  to instantiate and add
     */
    public void addEventManager(String clazz) {

        try {
            m_eventManager = (CmsEventManager)Class.forName(clazz).newInstance();
            if (CmsLog.INIT.isInfoEnabled()) {
                CmsLog.INIT.info(Messages.get().getBundle().key(
                    Messages.INIT_EVENTMANAGER_CLASS_SUCCESS_1,
                    m_eventManager));
            }
        } catch (Throwable t) {
            LOG.error(Messages.get().getBundle().key(Messages.INIT_EVENTMANAGER_CLASS_INVALID_1, clazz), t);
            return;
        }
    }

    /**
     * Adds a new job description for the scheduler.<p>
     * 
     * @param jobInfo the job description to add
     */
    public void addJobFromConfiguration(CmsScheduledJobInfo jobInfo) {

        m_configuredJobs.add(jobInfo);

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(
                Messages.INIT_SCHEDULER_CONFIG_JOB_3,
                jobInfo.getJobName(),
                jobInfo.getClassName(),
                jobInfo.getContextInfo().getUserName()));
        }
    }

    /**
     * Adds a new instance of a request handler class.<p>
     * 
     * @param clazz the class name of the request handler to instantiate and add
     */
    public void addRequestHandler(String clazz) {

        Object initClass;
        try {
            initClass = Class.forName(clazz).newInstance();
        } catch (Throwable t) {
            LOG.error(Messages.get().getBundle().key(Messages.LOG_INIT_REQUEST_HANDLER_FAILURE_1, clazz), t);
            return;
        }
        if (initClass instanceof I_CmsRequestHandler) {
            m_requestHandlers.add(initClass);
            if (CmsLog.INIT.isInfoEnabled()) {
                CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_REQUEST_HANDLER_SUCCESS_1, clazz));
            }
        } else {
            if (CmsLog.INIT.isErrorEnabled()) {
                CmsLog.INIT.error(Messages.get().getBundle().key(Messages.INIT_REQUEST_HANDLER_INVALID_1, clazz));
            }
        }
    }

    /**
     * Adds a new instance of a resource init handler class.<p>
     * 
     * @param clazz the class name of the resource init handler to instantiate and add
     */
    public void addResourceInitHandler(String clazz) {

        Object initClass;
        try {
            initClass = Class.forName(clazz).newInstance();
        } catch (Throwable t) {
            LOG.error(Messages.get().getBundle().key(Messages.LOG_RESOURCE_INIT_CLASS_INVALID_1, clazz), t);
            return;
        }
        if (initClass instanceof I_CmsResourceInit) {
            m_resourceInitHandlers.add(initClass);
            if (CmsLog.INIT.isInfoEnabled()) {
                CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_RESOURCE_INIT_SUCCESS_1, clazz));
            }
        } else {
            if (CmsLog.INIT.isErrorEnabled()) {
                CmsLog.INIT.error(Messages.get().getBundle().key(Messages.INIT_RESOURCE_INIT_INVALID_CLASS_1, clazz));
            }
        }
    }

    /**
     * Generates the schedule manager.<p>
     */
    public void addScheduleManager() {

        m_scheduleManager = new CmsScheduleManager(m_configuredJobs);
    }

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#addXmlDigesterRules(org.apache.commons.digester.Digester)
     */
    public void addXmlDigesterRules(Digester digester) {

        // add finish rule
        digester.addCallMethod("*/" + N_SYSTEM, "initializeFinished");

        // add rule for internationalization
        digester.addObjectCreate("*/" + N_SYSTEM + "/" + N_I18N, CmsLocaleManager.class);
        digester.addSetNext("*/" + N_SYSTEM + "/" + N_I18N, "setLocaleManager");

        // add locale handler creation rule
        digester.addObjectCreate(
            "*/" + N_SYSTEM + "/" + N_I18N + "/" + N_LOCALEHANDLER,
            A_CLASS,
            CmsConfigurationException.class);
        digester.addSetNext("*/" + N_SYSTEM + "/" + N_I18N + "/" + N_LOCALEHANDLER, "setLocaleHandler");

        // add locale rules
        digester.addCallMethod(
            "*/" + N_SYSTEM + "/" + N_I18N + "/" + N_LOCALESCONFIGURED + "/" + N_LOCALE,
            "addAvailableLocale",
            0);
        digester.addCallMethod(
            "*/" + N_SYSTEM + "/" + N_I18N + "/" + N_LOCALESDEFAULT + "/" + N_LOCALE,
            "addDefaultLocale",
            0);

        // add version history rules
        digester.addCallMethod("*/" + N_SYSTEM + "/" + N_VERSIONHISTORY, "setHistorySettings", 3);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_VERSIONHISTORY, 0, A_ENABLED);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_VERSIONHISTORY, 1, A_COUNT);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_VERSIONHISTORY, 2, A_DELETED);

        // add mail configuration rule
        digester.addObjectCreate("*/" + N_SYSTEM + "/" + N_MAIL, CmsMailSettings.class);
        digester.addCallMethod("*/" + N_SYSTEM + "/" + N_MAIL + "/" + N_MAILFROM, "setMailFromDefault", 0);
        digester.addSetNext("*/" + N_SYSTEM + "/" + N_MAIL, "setMailSettings");

        // add mail host configuration rule
        digester.addCallMethod("*/" + N_SYSTEM + "/" + N_MAIL + "/" + N_MAILHOST, "addMailHost", 5);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_MAIL + "/" + N_MAILHOST, 0, A_NAME);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_MAIL + "/" + N_MAILHOST, 1, A_ORDER);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_MAIL + "/" + N_MAILHOST, 2, A_PROTOCOL);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_MAIL + "/" + N_MAILHOST, 3, A_USER);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_MAIL + "/" + N_MAILHOST, 4, A_PASSWORD);

        // add scheduler creation rule
        digester.addCallMethod("*/" + N_SYSTEM + "/" + N_SCHEDULER, "addScheduleManager");

        // add scheduler job creation rule
        digester.addObjectCreate("*/" + N_SYSTEM + "/" + N_SCHEDULER + "/" + N_JOB, CmsScheduledJobInfo.class);
        digester.addBeanPropertySetter("*/" + N_SYSTEM + "/" + N_SCHEDULER + "/" + N_JOB + "/" + N_NAME, "jobName");
        digester.addBeanPropertySetter("*/" + N_SYSTEM + "/" + N_SCHEDULER + "/" + N_JOB + "/" + N_CLASS, "className");
        digester.addBeanPropertySetter(
            "*/" + N_SYSTEM + "/" + N_SCHEDULER + "/" + N_JOB + "/" + N_CRONEXPRESSION,
            "cronExpression");
        digester.addBeanPropertySetter(
            "*/" + N_SYSTEM + "/" + N_SCHEDULER + "/" + N_JOB + "/" + N_REUSEINSTANCE,
            "reuseInstance");
        digester.addBeanPropertySetter("*/" + N_SYSTEM + "/" + N_SCHEDULER + "/" + N_JOB + "/" + N_ACTIVE, "active");
        digester.addSetNext("*/" + N_SYSTEM + "/" + N_SCHEDULER + "/" + N_JOB, "addJobFromConfiguration");

        // add job context creation rule
        digester.addObjectCreate(
            "*/" + N_SYSTEM + "/" + N_SCHEDULER + "/" + N_JOB + "/" + N_CONTEXT,
            CmsContextInfo.class);
        digester.addBeanPropertySetter("*/"
            + N_SYSTEM
            + "/"
            + N_SCHEDULER
            + "/"
            + N_JOB
            + "/"
            + N_CONTEXT
            + "/"
            + N_USERNAME, "userName");
        digester.addBeanPropertySetter("*/"
            + N_SYSTEM
            + "/"
            + N_SCHEDULER
            + "/"
            + N_JOB
            + "/"
            + N_CONTEXT
            + "/"
            + N_PROJECT, "projectName");
        digester.addBeanPropertySetter("*/"
            + N_SYSTEM
            + "/"
            + N_SCHEDULER
            + "/"
            + N_JOB
            + "/"
            + N_CONTEXT
            + "/"
            + N_SITEROOT, "siteRoot");
        digester.addBeanPropertySetter("*/"
            + N_SYSTEM
            + "/"
            + N_SCHEDULER
            + "/"
            + N_JOB
            + "/"
            + N_CONTEXT
            + "/"
            + N_REQUESTEDURI, "requestedUri");
        digester.addBeanPropertySetter("*/"
            + N_SYSTEM
            + "/"
            + N_SCHEDULER
            + "/"
            + N_JOB
            + "/"
            + N_CONTEXT
            + "/"
            + N_LOCALE, "localeName");
        digester.addBeanPropertySetter("*/"
            + N_SYSTEM
            + "/"
            + N_SCHEDULER
            + "/"
            + N_JOB
            + "/"
            + N_CONTEXT
            + "/"
            + N_ENCODING);
        digester.addBeanPropertySetter("*/"
            + N_SYSTEM
            + "/"
            + N_SCHEDULER
            + "/"
            + N_JOB
            + "/"
            + N_CONTEXT
            + "/"
            + N_REMOTEADDR, "remoteAddr");
        digester.addSetNext("*/" + N_SYSTEM + "/" + N_SCHEDULER + "/" + N_JOB + "/" + N_CONTEXT, "setContextInfo");

        // add generic parameter rules (used for jobs, password handler)
        digester.addCallMethod(
            "*/" + I_CmsXmlConfiguration.N_PARAM,
            I_CmsConfigurationParameterHandler.ADD_PARAMETER_METHOD,
            2);
        digester.addCallParam("*/" + I_CmsXmlConfiguration.N_PARAM, 0, I_CmsXmlConfiguration.A_NAME);
        digester.addCallParam("*/" + I_CmsXmlConfiguration.N_PARAM, 1);

        // add event classes
        digester.addCallMethod("*/" + N_SYSTEM + "/" + N_EVENTS + "/" + N_EVENTMANAGER, "addEventManager", 1);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_EVENTS + "/" + N_EVENTMANAGER, 0, A_CLASS);

        // add resource init classes
        digester.addCallMethod(
            "*/" + N_SYSTEM + "/" + N_RESOURCEINIT + "/" + N_RESOURCEINITHANDLER,
            "addResourceInitHandler",
            1);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_RESOURCEINIT + "/" + N_RESOURCEINITHANDLER, 0, A_CLASS);

        // add request handler classes
        digester.addCallMethod(
            "*/" + N_SYSTEM + "/" + N_REQUESTHANDLERS + "/" + N_REQUESTHANDLER,
            "addRequestHandler",
            1);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_REQUESTHANDLERS + "/" + N_REQUESTHANDLER, 0, A_CLASS);

        // add password handler creation rule
        digester.addObjectCreate("*/" + N_SYSTEM + "/" + N_PASSWORDHANDLER, A_CLASS, CmsConfigurationException.class);
        digester.addCallMethod(
            "*/" + N_SYSTEM + "/" + N_PASSWORDHANDLER,
            I_CmsConfigurationParameterHandler.INIT_CONFIGURATION_METHOD);
        digester.addBeanPropertySetter(
            "*/" + N_SYSTEM + "/" + N_PASSWORDHANDLER + "/" + N_PASSWORDENCODING,
            "inputEncoding");
        digester.addBeanPropertySetter("*/" + N_SYSTEM + "/" + N_PASSWORDHANDLER + "/" + N_DIGESTTYPE, "digestType");
        digester.addSetNext("*/" + N_SYSTEM + "/" + N_PASSWORDHANDLER, "setPasswordHandler");

        // add validation handler creation rules
        digester.addCallMethod("*/" + N_SYSTEM + "/" + N_VALIDATIONHANDLER, "setValidationHandler", 1);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_VALIDATIONHANDLER, 0, A_CLASS);

        // add login manager creation rules
        digester.addCallMethod("*/" + N_LOGINMANAGER, "setLoginManager", 2);
        digester.addCallParam("*/" + N_LOGINMANAGER + "/" + N_DISABLEMINUTES, 0);
        digester.addCallParam("*/" + N_LOGINMANAGER + "/" + N_MAXBADATTEMPTS, 1);

        // add login message creation rules
        digester.addObjectCreate("*/" + N_LOGINMESSAGE, CmsLoginMessage.class);
        digester.addBeanPropertySetter("*/" + N_LOGINMESSAGE + "/" + N_ENABLED);
        digester.addBeanPropertySetter("*/" + N_LOGINMESSAGE + "/" + N_MESSAGE);
        digester.addBeanPropertySetter("*/" + N_LOGINMESSAGE + "/" + N_LOGINFORBIDDEN);
        digester.addBeanPropertySetter("*/" + N_LOGINMESSAGE + "/" + N_TIMESTART);
        digester.addBeanPropertySetter("*/" + N_LOGINMESSAGE + "/" + N_TIMEEND);
        digester.addSetNext("*/" + N_LOGINMESSAGE, "setLoginMessage");

        // add site configuration rule        
        digester.addObjectCreate("*/" + N_SYSTEM + "/" + N_SITES, CmsSiteManagerImpl.class);
        digester.addCallMethod("*/" + N_SYSTEM + "/" + N_SITES + "/" + N_WORKPLACE_SERVER, "setWorkplaceServer", 0);
        digester.addCallMethod("*/" + N_SYSTEM + "/" + N_SITES + "/" + N_DEFAULT_URI, "setDefaultUri", 0);
        digester.addSetNext("*/" + N_SYSTEM + "/" + N_SITES, "setSiteManager");

        // add site configuration rule
        digester.addCallMethod("*/" + N_SYSTEM + "/" + N_SITES + "/" + N_SITE, "addSite", 5);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_SITES + "/" + N_SITE, 0, A_SERVER);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_SITES + "/" + N_SITE, 1, A_URI);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_SITES + "/" + N_SITE + "/" + N_SECURE, 2, A_SERVER);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_SITES + "/" + N_SITE + "/" + N_SECURE, 3, A_EXCLUSIVE);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_SITES + "/" + N_SITE + "/" + N_SECURE, 4, A_ERROR);

        // add an alias to the currently configured site
        digester.addCallMethod(
            "*/" + N_SYSTEM + "/" + N_SITES + "/" + N_SITE + "/" + N_ALIAS,
            "addAliasToConfigSite",
            2);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_SITES + "/" + N_SITE + "/" + N_ALIAS, 0, A_SERVER);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_SITES + "/" + N_SITE + "/" + N_ALIAS, 1, A_OFFSET);

        // add compatibility parameter rules 
        digester.addCallMethod(
            "*/" + N_SYSTEM + "/" + N_RUNTIMEPROPERTIES + "/" + N_PARAM,
            I_CmsConfigurationParameterHandler.ADD_PARAMETER_METHOD,
            2);
        digester.addCallParam(
            "*/" + N_SYSTEM + "/" + N_RUNTIMEPROPERTIES + "/" + N_PARAM,
            0,
            I_CmsXmlConfiguration.A_NAME);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_RUNTIMEPROPERTIES + "/" + N_PARAM, 1);

        // add runtime classes configuration rules
        digester.addCallMethod(
            "*/" + N_SYSTEM + "/" + N_RUNTIMECLASSES + "/" + N_RUNTIMEINFO,
            "setRuntimeInfoFactory",
            1);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_RUNTIMECLASSES + "/" + N_RUNTIMEINFO, 0, A_CLASS);

        // add default users rule
        digester.addCallMethod("*/" + N_SYSTEM + "/" + N_DEFAULTUSERS, "setCmsDefaultUsers", 8);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_DEFAULTUSERS + "/" + N_USER_ADMIN, 0);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_DEFAULTUSERS + "/" + N_USER_GUEST, 1);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_DEFAULTUSERS + "/" + N_USER_EXPORT, 2);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_DEFAULTUSERS + "/" + N_USER_DELETEDRESOURCE, 3);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_DEFAULTUSERS + "/" + N_GROUP_ADMINISTRATORS, 4);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_DEFAULTUSERS + "/" + N_GROUP_PROJECTMANAGERS, 5);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_DEFAULTUSERS + "/" + N_GROUP_USERS, 6);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_DEFAULTUSERS + "/" + N_GROUP_GUESTS, 7);

        // add defaultContentEncoding rule
        digester.addCallMethod("*/" + N_SYSTEM + "/" + N_DEFAULT_CONTENT_ENCODING, "setDefaultContentEncoding", 1);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_DEFAULT_CONTENT_ENCODING, 0);

        // add memorymonitor configuration rule        
        digester.addObjectCreate("*/" + N_SYSTEM + "/" + N_MEMORYMONITOR, CmsMemoryMonitorConfiguration.class);
        digester.addCallMethod("*/" + N_SYSTEM + "/" + N_MEMORYMONITOR, "initialize", 5);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_MEMORYMONITOR, 0, A_CLASS);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_MEMORYMONITOR + "/" + N_MAXUSAGE_PERCENT, 1);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_MEMORYMONITOR + "/" + N_LOG_INTERVAL, 2);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_MEMORYMONITOR + "/" + N_EMAIL_INTERVAL, 3);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_MEMORYMONITOR + "/" + N_WARNING_INTERVAL, 4);
        digester.addCallMethod("*/" + N_SYSTEM + "/" + N_MEMORYMONITOR + "/" + N_EMAIL_SENDER, "setEmailSender", 0);
        digester.addCallMethod(
            "*/" + N_SYSTEM + "/" + N_MEMORYMONITOR + "/" + N_EMAIL_RECEIVER + "/" + N_RECEIVER,
            "addEmailReceiver",
            0);

        // set the MemoryMonitorConfiguration initialized once before
        digester.addSetNext("*/" + N_SYSTEM + "/" + N_MEMORYMONITOR, "setCmsMemoryMonitorConfiguration");

        // add flexcache configuration rule        
        digester.addObjectCreate("*/" + N_SYSTEM + "/" + N_FLEXCACHE, CmsFlexCacheConfiguration.class);
        digester.addCallMethod("*/" + N_SYSTEM + "/" + N_FLEXCACHE, "initialize", 6);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_FLEXCACHE + "/" + N_CACHE_ENABLED, 0);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_FLEXCACHE + "/" + N_CACHE_OFFLINE, 1);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_FLEXCACHE + "/" + N_MAXCACHEBYTES, 2);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_FLEXCACHE + "/" + N_AVGCACHEBYTES, 3);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_FLEXCACHE + "/" + N_MAXENTRYBYTES, 4);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_FLEXCACHE + "/" + N_MAXKEYS, 5);

        // set the FlexCacheConfiguration initialized once before
        digester.addSetNext("*/" + N_SYSTEM + "/" + N_FLEXCACHE, "setCmsFlexCacheConfiguration");

        // add http basic authentication rules
        digester.addObjectCreate("*/" + N_SYSTEM + "/" + N_HTTP_AUTHENTICATION, CmsHttpAuthenticationSettings.class);
        digester.addCallMethod(
            "*/" + N_SYSTEM + "/" + N_HTTP_AUTHENTICATION + "/" + N_BROWSER_BASED,
            "setUseBrowserBasedHttpAuthentication",
            0);
        digester.addCallMethod(
            "*/" + N_SYSTEM + "/" + N_HTTP_AUTHENTICATION + "/" + N_FORM_BASED,
            "setFormBasedHttpAuthenticationUri",
            0);
        digester.addSetNext("*/" + N_SYSTEM + "/" + N_HTTP_AUTHENTICATION, "setHttpAuthenticationSettings");

        // cache rules
        digester.addObjectCreate("*/" + N_SYSTEM + "/" + N_CACHE, CmsCacheSettings.class);
        digester.addCallMethod("*/" + N_SYSTEM + "/" + N_CACHE + "/" + N_KEYGENERATOR, "setCacheKeyGenerator", 0);
        digester.addCallMethod("*/" + N_SYSTEM + "/" + N_CACHE + "/" + N_SIZE_USERS, "setUserCacheSize", 0);
        digester.addCallMethod("*/" + N_SYSTEM + "/" + N_CACHE + "/" + N_SIZE_GROUPS, "setGroupCacheSize", 0);
        digester.addCallMethod("*/" + N_SYSTEM + "/" + N_CACHE + "/" + N_SIZE_ORGUNITS, "setOrgUnitCacheSize", 0);
        digester.addCallMethod("*/" + N_SYSTEM + "/" + N_CACHE + "/" + N_SIZE_USERGROUPS, "setUserGroupsCacheSize", 0);
        digester.addCallMethod("*/" + N_SYSTEM + "/" + N_CACHE + "/" + N_SIZE_PROJECTS, "setProjectCacheSize", 0);
        digester.addCallMethod(
            "*/" + N_SYSTEM + "/" + N_CACHE + "/" + N_SIZE_PROJECTRESOURCES,
            "setProjectResourcesCacheSize",
            0);
        digester.addCallMethod("*/" + N_SYSTEM + "/" + N_CACHE + "/" + N_SIZE_RESOURCES, "setResourceCacheSize", 0);
        digester.addCallMethod("*/" + N_SYSTEM + "/" + N_CACHE + "/" + N_SIZE_ROLES, "setRolesCacheSize", 0);
        digester.addCallMethod(
            "*/" + N_SYSTEM + "/" + N_CACHE + "/" + N_SIZE_RESOURCELISTS,
            "setResourcelistCacheSize",
            0);
        digester.addCallMethod("*/" + N_SYSTEM + "/" + N_CACHE + "/" + N_SIZE_PROPERTIES, "setPropertyCacheSize", 0);
        digester.addCallMethod(
            "*/" + N_SYSTEM + "/" + N_CACHE + "/" + N_SIZE_PROPERTYLISTS,
            "setPropertyListsCacheSize",
            0);
        digester.addCallMethod("*/" + N_SYSTEM + "/" + N_CACHE + "/" + N_SIZE_ACLS, "setAclCacheSize", 0);
        digester.addCallMethod("*/" + N_SYSTEM + "/" + N_CACHE + "/" + N_SIZE_PERMISSIONS, "setPermissionCacheSize", 0);
        digester.addSetNext("*/" + N_SYSTEM + "/" + N_CACHE, "setCacheSettings");

        // set the notification time
        digester.addCallMethod(
            "*/" + N_SYSTEM + "/" + N_CONTENT_NOTIFICATION + "/" + N_NOTIFICATION_TIME,
            "setNotificationTime",
            1);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_CONTENT_NOTIFICATION + "/" + N_NOTIFICATION_TIME, 0);

        // set the notification project
        digester.addCallMethod(
            "*/" + N_SYSTEM + "/" + N_CONTENT_NOTIFICATION + "/" + N_NOTIFICATION_PROJECT,
            "setNotificationProject",
            1);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_CONTENT_NOTIFICATION + "/" + N_NOTIFICATION_PROJECT, 0);

        // add authorization handler creation rules
        digester.addCallMethod("*/" + N_SYSTEM + "/" + N_AUTHORIZATIONHANDLER, "setAuthorizationHandler", 1);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_AUTHORIZATIONHANDLER, 0, A_CLASS);

        // add publish manager configuration rule        
        digester.addObjectCreate("*/" + N_SYSTEM + "/" + N_PUBLISHMANAGER, CmsPublishManager.class);
        digester.addCallMethod(
            "*/" + N_SYSTEM + "/" + N_PUBLISHMANAGER + "/" + N_HISTORYSIZE,
            "setPublishHistorySize",
            0);
        digester.addCallMethod(
            "*/" + N_SYSTEM + "/" + N_PUBLISHMANAGER + "/" + N_QUEUEPERSISTANCE,
            "setPublishQueuePersistance",
            0);
        digester.addCallMethod(
            "*/" + N_SYSTEM + "/" + N_PUBLISHMANAGER + "/" + N_QUEUESHUTDOWNTIME,
            "setPublishQueueShutdowntime",
            0);
        digester.addSetNext("*/" + N_SYSTEM + "/" + N_PUBLISHMANAGER, "setPublishManager");

        // add rule for session storage provider
        digester.addCallMethod("*/" + N_SYSTEM + "/" + N_SESSION_STORAGEPROVIDER, "setSessionStorageProvider", 1);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_SESSION_STORAGEPROVIDER, 0, A_CLASS);

        // add rule for permission handler
        digester.addCallMethod("*/" + N_SYSTEM + "/" + N_PERMISSIONHANDLER, "setPermissionHandler", 1);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_PERMISSIONHANDLER, 0, A_CLASS);

        // add rules for servlet container settings
        digester.addCallMethod(
            "*/" + N_SYSTEM + "/" + N_SERVLETCONTAINERSETTINGS + "/" + N_PREVENTRESPONSEFLUSH,
            "setPreventResponseFlush",
            0);
        digester.addCallMethod(
            "*/" + N_SYSTEM + "/" + N_SERVLETCONTAINERSETTINGS + "/" + N_RELEASETAGSAFTEREND,
            "setReleaseTagsAfterEnd",
            0);
        digester.addCallMethod(
            "*/" + N_SYSTEM + "/" + N_SERVLETCONTAINERSETTINGS + "/" + N_REQUESTERRORPAGEATTRIBUTE,
            "setRequestErrorPageAttribute",
            0);
        digester.addCallMethod("*/" + N_SYSTEM + "/" + N_SERVLETCONTAINERSETTINGS, "setServletContainerSettingsMode", 1);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_SERVLETCONTAINERSETTINGS, 0, A_MODE);
    }

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#generateXml(org.dom4j.Element)
     */
    public Element generateXml(Element parent) {

        // generate vfs node and subnodes
        Element systemElement = parent.addElement(N_SYSTEM);

        if (OpenCms.getRunLevel() >= OpenCms.RUNLEVEL_3_SHELL_ACCESS) {
            // initialized OpenCms instance is available, use latest values
            m_localeManager = OpenCms.getLocaleManager();
            m_mailSettings = OpenCms.getSystemInfo().getMailSettings();
            m_configuredJobs = OpenCms.getScheduleManager().getJobs();
            m_historyEnabled = OpenCms.getSystemInfo().isHistoryEnabled();
            m_historyVersions = OpenCms.getSystemInfo().getHistoryVersions();
            m_historyVersionsAfterDeletion = OpenCms.getSystemInfo().getHistoryVersionsAfterDeletion();
            // m_resourceInitHandlers instance must be the one from configuration
            // m_requestHandlers instance must be the one from configuration
            m_siteManager = OpenCms.getSiteManager();
            m_loginManager = OpenCms.getLoginManager();
            m_loginMessage = OpenCms.getLoginManager().getLoginMessage();
        }

        // i18n nodes
        Element i18nElement = systemElement.addElement(N_I18N);
        i18nElement.addElement(N_LOCALEHANDLER).addAttribute(
            A_CLASS,
            m_localeManager.getLocaleHandler().getClass().getName());
        Iterator i;
        Element localesElement;
        localesElement = i18nElement.addElement(N_LOCALESCONFIGURED);
        i = m_localeManager.getAvailableLocales().iterator();
        while (i.hasNext()) {
            Locale locale = (Locale)i.next();
            localesElement.addElement(N_LOCALE).addText(locale.toString());
        }
        localesElement = i18nElement.addElement(N_LOCALESDEFAULT);
        i = m_localeManager.getDefaultLocales().iterator();
        while (i.hasNext()) {
            Locale locale = (Locale)i.next();
            localesElement.addElement(N_LOCALE).setText(locale.toString());
        }

        // mail nodes
        Element mailElement = systemElement.addElement(N_MAIL);
        mailElement.addElement(N_MAILFROM).setText(m_mailSettings.getMailFromDefault());
        i = m_mailSettings.getMailHosts().iterator();
        while (i.hasNext()) {
            CmsMailHost host = (CmsMailHost)i.next();
            Element hostElement = mailElement.addElement(N_MAILHOST).addAttribute(A_NAME, host.getHostname()).addAttribute(
                A_ORDER,
                host.getOrder().toString()).addAttribute(A_PROTOCOL, host.getProtocol());
            if (host.isAuthenticating()) {
                hostElement.addAttribute(A_USER, host.getUsername()).addAttribute(A_PASSWORD, host.getPassword());
            }
        }

        // scheduler node
        Element schedulerElement = systemElement.addElement(N_SCHEDULER);
        i = m_configuredJobs.iterator();
        while (i.hasNext()) {
            CmsScheduledJobInfo jobInfo = (CmsScheduledJobInfo)i.next();
            Element jobElement = schedulerElement.addElement(N_JOB);
            jobElement.addElement(N_NAME).addText(jobInfo.getJobName());
            jobElement.addElement(N_CLASS).addText(jobInfo.getClassName());
            jobElement.addElement(N_REUSEINSTANCE).addText(String.valueOf(jobInfo.isReuseInstance()));
            jobElement.addElement(N_ACTIVE).addText(String.valueOf(jobInfo.isActive()));
            jobElement.addElement(N_CRONEXPRESSION).addCDATA(jobInfo.getCronExpression());
            Element contextElement = jobElement.addElement(N_CONTEXT);
            contextElement.addElement(N_USERNAME).setText(jobInfo.getContextInfo().getUserName());
            contextElement.addElement(N_PROJECT).setText(jobInfo.getContextInfo().getProjectName());
            contextElement.addElement(N_SITEROOT).setText(jobInfo.getContextInfo().getSiteRoot());
            contextElement.addElement(N_REQUESTEDURI).setText(jobInfo.getContextInfo().getRequestedUri());
            contextElement.addElement(N_LOCALE).setText(jobInfo.getContextInfo().getLocaleName());
            contextElement.addElement(N_ENCODING).setText(jobInfo.getContextInfo().getEncoding());
            contextElement.addElement(N_REMOTEADDR).setText(jobInfo.getContextInfo().getRemoteAddr());
            Map jobParameters = jobInfo.getConfiguration();
            if ((jobParameters != null) && (jobParameters.size() > 0)) {
                Element parameterElement = jobElement.addElement(N_PARAMETERS);
                Iterator it = jobParameters.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry entry = (Map.Entry)it.next();
                    String name = (String)entry.getKey();
                    String value = (String)entry.getValue();
                    Element paramNode = parameterElement.addElement(N_PARAM);
                    paramNode.addAttribute(A_NAME, name);
                    paramNode.addText(value);
                }
            }
        }

        // <events> node
        Element eventsElement = systemElement.addElement(N_EVENTS);
        Element eventManagerElement = eventsElement.addElement(N_EVENTMANAGER);
        eventManagerElement.addAttribute(A_CLASS, m_eventManager.getClass().getName());

        // version history
        Element historyElement = systemElement.addElement(N_VERSIONHISTORY);
        historyElement.addAttribute(A_ENABLED, String.valueOf(m_historyEnabled));
        historyElement.addAttribute(A_COUNT, new Integer(m_historyVersions).toString());
        historyElement.addAttribute(A_DELETED, new Integer(m_historyVersionsAfterDeletion).toString());

        // resourceinit
        Element resourceinitElement = systemElement.addElement(N_RESOURCEINIT);
        i = m_resourceInitHandlers.iterator();
        while (i.hasNext()) {
            I_CmsResourceInit clazz = (I_CmsResourceInit)i.next();
            Element handlerElement = resourceinitElement.addElement(N_RESOURCEINITHANDLER);
            handlerElement.addAttribute(A_CLASS, clazz.getClass().getName());
        }

        // request handlers
        Element requesthandlersElement = systemElement.addElement(N_REQUESTHANDLERS);
        i = m_requestHandlers.iterator();
        while (i.hasNext()) {
            I_CmsRequestHandler clazz = (I_CmsRequestHandler)i.next();
            Element handlerElement = requesthandlersElement.addElement(N_REQUESTHANDLER);
            handlerElement.addAttribute(A_CLASS, clazz.getClass().getName());
        }

        // password handler
        Element passwordhandlerElement = systemElement.addElement(N_PASSWORDHANDLER).addAttribute(
            A_CLASS,
            m_passwordHandler.getClass().getName());
        passwordhandlerElement.addElement(N_PASSWORDENCODING).addText(m_passwordHandler.getInputEncoding());
        passwordhandlerElement.addElement(N_DIGESTTYPE).addText(m_passwordHandler.getDigestType());
        Map handlerParameters = m_passwordHandler.getConfiguration();
        if (handlerParameters != null) {
            Iterator it = handlerParameters.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry)it.next();
                String name = (String)entry.getKey();
                String value = (String)entry.getValue();
                Element paramNode = passwordhandlerElement.addElement(N_PARAM);
                paramNode.addAttribute(A_NAME, name);
                paramNode.addText(value);
            }
        }

        // validation handler
        if (m_validationHandler != null) {
            Element valHandlerElem = systemElement.addElement(N_VALIDATIONHANDLER);
            valHandlerElem.addAttribute(A_CLASS, m_validationHandler);
        }

        // login manager
        if (m_loginManager != null) {
            Element managerElement = systemElement.addElement(N_LOGINMANAGER);
            managerElement.addElement(N_DISABLEMINUTES).addText(String.valueOf(m_loginManager.getDisableMinutes()));
            managerElement.addElement(N_MAXBADATTEMPTS).addText(String.valueOf(m_loginManager.getMaxBadAttempts()));
        }

        // login message
        if (m_loginMessage != null) {
            Element messageElement = systemElement.addElement(N_LOGINMESSAGE);
            messageElement.addElement(N_ENABLED).addText(String.valueOf(m_loginMessage.isEnabled()));
            messageElement.addElement(N_MESSAGE).addCDATA(m_loginMessage.getMessage());
            messageElement.addElement(N_LOGINFORBIDDEN).addText(String.valueOf(m_loginMessage.isLoginForbidden()));
            if (m_loginMessage.getTimeStart() != CmsLoginMessage.DEFAULT_TIME_START) {
                messageElement.addElement(N_TIMESTART).addText(String.valueOf(m_loginMessage.getTimeStart()));
            }
            if (m_loginMessage.getTimeEnd() != CmsLoginMessage.DEFAULT_TIME_END) {
                messageElement.addElement(N_TIMEEND).addText(String.valueOf(m_loginMessage.getTimeEnd()));
            }
        }

        // create <sites> node 
        Element sitesElement = systemElement.addElement(N_SITES);
        sitesElement.addElement(N_WORKPLACE_SERVER).addText(m_siteManager.getWorkplaceServer());
        sitesElement.addElement(N_DEFAULT_URI).addText(m_siteManager.getDefaultUri());
        Iterator siteIterator = new HashSet(m_siteManager.getSites().values()).iterator();
        while (siteIterator.hasNext()) {
            CmsSite site = (CmsSite)siteIterator.next();
            // create <site server="" uri=""/> subnode(s)
            Element siteElement = sitesElement.addElement(N_SITE);

            siteElement.addAttribute(A_SERVER, site.getSiteMatcher().toString());
            siteElement.addAttribute(A_URI, site.getSiteRoot().concat("/"));
            // create <secure server=""/> subnode            
            if (site.hasSecureServer()) {
                Element secureElem = siteElement.addElement(N_SECURE);
                secureElem.addAttribute(A_SERVER, site.getSecureUrl());
                secureElem.addAttribute(A_EXCLUSIVE, "" + site.isExclusiveUrl());
                secureElem.addAttribute(A_ERROR, "" + site.isExclusiveError());
            }
            // create <alias server=""/> subnode(s)            
            Iterator aliasIterator = site.getAliases().iterator();
            while (aliasIterator.hasNext()) {
                CmsSiteMatcher matcher = (CmsSiteMatcher)aliasIterator.next();
                Element aliasElement = siteElement.addElement(N_ALIAS);
                aliasElement.addAttribute(A_SERVER, matcher.getUrl());
                if (matcher.getTimeOffset() != 0) {
                    aliasElement.addAttribute(A_OFFSET, "" + matcher.getTimeOffset());
                }
            }
        }

        // create <runtimeproperties> node
        Element runtimepropertiesElement = systemElement.addElement(N_RUNTIMEPROPERTIES);
        if (m_runtimeProperties != null) {
            List sortedRuntimeProperties = new ArrayList(m_runtimeProperties.keySet());
            Collections.sort(sortedRuntimeProperties);
            Iterator it = sortedRuntimeProperties.iterator();
            while (it.hasNext()) {
                String key = (String)it.next();
                // create <param name="">value</param> subnodes
                runtimepropertiesElement.addElement(N_PARAM).addAttribute(A_NAME, key).addText(
                    (String)m_runtimeProperties.get(key));
            }
        }

        // create <runtimeinfo> node
        Element runtimeinfoElement = systemElement.addElement(N_RUNTIMECLASSES);
        Element runtimeinfofactoryElement = runtimeinfoElement.addElement(N_RUNTIMEINFO);
        runtimeinfofactoryElement.addAttribute(A_CLASS, getRuntimeInfoFactory().getClass().getName());

        // create <defaultusers> node
        Element defaultusersElement = systemElement.addElement(N_DEFAULTUSERS);
        // create <user-admin> subnode
        defaultusersElement.addElement(N_USER_ADMIN).addText(m_cmsDefaultUsers.getUserAdmin());
        // create <user-guest> subnode
        defaultusersElement.addElement(N_USER_GUEST).addText(m_cmsDefaultUsers.getUserGuest());
        // create <user-export> subnode
        defaultusersElement.addElement(N_USER_EXPORT).addText(m_cmsDefaultUsers.getUserExport());
        if (!m_cmsDefaultUsers.getUserDeletedResource().equals(m_cmsDefaultUsers.getUserAdmin())) {
            // create <user-deletedresource> subnode
            defaultusersElement.addElement(N_USER_DELETEDRESOURCE).addText(m_cmsDefaultUsers.getUserDeletedResource());
        }
        // create <group-administrators> subnode
        defaultusersElement.addElement(N_GROUP_ADMINISTRATORS).addText(m_cmsDefaultUsers.getGroupAdministrators());
        // create <group-projectmanagers> subnode
        defaultusersElement.addElement(N_GROUP_PROJECTMANAGERS).addText(m_cmsDefaultUsers.getGroupProjectmanagers());
        // create <group-users> subnode
        defaultusersElement.addElement(N_GROUP_USERS).addText(m_cmsDefaultUsers.getGroupUsers());
        // create <group-guests> subnode
        defaultusersElement.addElement(N_GROUP_GUESTS).addText(m_cmsDefaultUsers.getGroupGuests());

        // create <defaultcontentencoding> node
        systemElement.addElement(N_DEFAULT_CONTENT_ENCODING).addText(getDefaultContentEncoding());

        // create <memorymonitor> node
        if (m_cmsMemoryMonitorConfiguration != null) {
            Element memorymonitorElement = systemElement.addElement(N_MEMORYMONITOR);
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_cmsMemoryMonitorConfiguration.getClassName())) {
                memorymonitorElement.addAttribute(A_CLASS, m_cmsMemoryMonitorConfiguration.getClassName());
            }
            if (m_cmsMemoryMonitorConfiguration.getMaxUsagePercent() > 0) {
                memorymonitorElement.addElement(N_MAXUSAGE_PERCENT).addText(
                    String.valueOf(m_cmsMemoryMonitorConfiguration.getMaxUsagePercent()));
            }
            if (m_cmsMemoryMonitorConfiguration.getLogInterval() > 0) {
                memorymonitorElement.addElement(N_LOG_INTERVAL).addText(
                    String.valueOf(m_cmsMemoryMonitorConfiguration.getLogInterval()));
            }
            if (m_cmsMemoryMonitorConfiguration.getEmailInterval() > 0) {
                memorymonitorElement.addElement(N_EMAIL_INTERVAL).addText(
                    String.valueOf(m_cmsMemoryMonitorConfiguration.getEmailInterval()));
            }
            if (m_cmsMemoryMonitorConfiguration.getWarningInterval() > 0) {
                memorymonitorElement.addElement(N_WARNING_INTERVAL).addText(
                    String.valueOf(m_cmsMemoryMonitorConfiguration.getWarningInterval()));
            }
            if (m_cmsMemoryMonitorConfiguration.getEmailSender() != null) {
                memorymonitorElement.addElement(N_EMAIL_SENDER).addText(
                    m_cmsMemoryMonitorConfiguration.getEmailSender());
            }
            List emailReceiver = m_cmsMemoryMonitorConfiguration.getEmailReceiver();
            if (!emailReceiver.isEmpty()) {
                Element emailreceiverElement = memorymonitorElement.addElement(N_EMAIL_RECEIVER);
                Iterator iter = emailReceiver.iterator();
                while (iter.hasNext()) {
                    emailreceiverElement.addElement(N_RECEIVER).addText((String)iter.next());
                }
            }
        }

        // create <flexcache> node
        Element flexcacheElement = systemElement.addElement(N_FLEXCACHE);
        flexcacheElement.addElement(N_CACHE_ENABLED).addText(
            String.valueOf(m_cmsFlexCacheConfiguration.isCacheEnabled()));
        flexcacheElement.addElement(N_CACHE_OFFLINE).addText(
            String.valueOf(m_cmsFlexCacheConfiguration.isCacheOffline()));
        flexcacheElement.addElement(N_MAXCACHEBYTES).addText(
            String.valueOf(m_cmsFlexCacheConfiguration.getMaxCacheBytes()));
        flexcacheElement.addElement(N_AVGCACHEBYTES).addText(
            String.valueOf(m_cmsFlexCacheConfiguration.getAvgCacheBytes()));
        flexcacheElement.addElement(N_MAXENTRYBYTES).addText(
            String.valueOf(m_cmsFlexCacheConfiguration.getMaxEntryBytes()));
        flexcacheElement.addElement(N_MAXKEYS).addText(String.valueOf(m_cmsFlexCacheConfiguration.getMaxKeys()));

        // create <http-authentication> node
        Element httpAuthenticationElement = systemElement.addElement(N_HTTP_AUTHENTICATION);
        httpAuthenticationElement.addElement(N_BROWSER_BASED).setText(
            Boolean.toString(m_httpAuthenticationSettings.useBrowserBasedHttpAuthentication()));
        if (m_httpAuthenticationSettings.getFormBasedHttpAuthenticationUri() != null) {
            httpAuthenticationElement.addElement(N_FORM_BASED).setText(
                m_httpAuthenticationSettings.getFormBasedHttpAuthenticationUri());
        }

        // cache settings
        Element cacheElement = systemElement.addElement(N_CACHE);
        cacheElement.addElement(N_KEYGENERATOR).setText(m_cacheSettings.getCacheKeyGenerator());
        cacheElement.addElement(N_SIZE_USERS).setText(Integer.toString(m_cacheSettings.getUserCacheSize()));
        cacheElement.addElement(N_SIZE_GROUPS).setText(Integer.toString(m_cacheSettings.getGroupCacheSize()));
        if (m_cacheSettings.getConfiguredOrgUnitCacheSize() > -1) {
            cacheElement.addElement(N_SIZE_ORGUNITS).setText(
                Integer.toString(m_cacheSettings.getConfiguredOrgUnitCacheSize()));
        }
        cacheElement.addElement(N_SIZE_USERGROUPS).setText(Integer.toString(m_cacheSettings.getUserGroupsCacheSize()));
        cacheElement.addElement(N_SIZE_PROJECTS).setText(Integer.toString(m_cacheSettings.getProjectCacheSize()));
        if (m_cacheSettings.getConfiguredProjectResourcesCacheSize() > -1) {
            cacheElement.addElement(N_SIZE_PROJECTRESOURCES).setText(
                Integer.toString(m_cacheSettings.getConfiguredProjectResourcesCacheSize()));
        }
        cacheElement.addElement(N_SIZE_RESOURCES).setText(Integer.toString(m_cacheSettings.getResourceCacheSize()));
        if (m_cacheSettings.getConfiguredRolesCacheSize() > -1) {
            cacheElement.addElement(N_SIZE_ROLES).setText(
                Integer.toString(m_cacheSettings.getConfiguredRolesCacheSize()));
        }
        cacheElement.addElement(N_SIZE_RESOURCELISTS).setText(
            Integer.toString(m_cacheSettings.getResourcelistCacheSize()));
        cacheElement.addElement(N_SIZE_PROPERTIES).setText(Integer.toString(m_cacheSettings.getPropertyCacheSize()));
        if (m_cacheSettings.getConfiguredPropertyListsCacheSize() > -1) {
            cacheElement.addElement(N_SIZE_PROPERTYLISTS).setText(
                Integer.toString(m_cacheSettings.getConfiguredPropertyListsCacheSize()));
        }
        cacheElement.addElement(N_SIZE_ACLS).setText(Integer.toString(m_cacheSettings.getAclCacheSize()));
        cacheElement.addElement(N_SIZE_PERMISSIONS).setText(Integer.toString(m_cacheSettings.getPermissionCacheSize()));

        // content notification settings
        if ((m_notificationTime != null) || (m_notificationProject != null)) {
            Element notificationElement = systemElement.addElement(N_CONTENT_NOTIFICATION);
            if (m_notificationTime != null) {
                notificationElement.addElement(N_NOTIFICATION_TIME).setText(m_notificationTime.toString());
            }
            if (m_notificationProject != null) {
                notificationElement.addElement(N_NOTIFICATION_PROJECT).setText(m_notificationProject);
            }
        }

        // authorization handler
        if (m_authorizationHandler != null) {
            Element authorizationHandlerElem = systemElement.addElement(N_AUTHORIZATIONHANDLER);
            authorizationHandlerElem.addAttribute(A_CLASS, m_authorizationHandler);
        }

        // optional publish manager nodes
        if (m_publishManager != null) {
            Element pubHistElement = systemElement.addElement(N_PUBLISHMANAGER);
            pubHistElement.addElement(N_HISTORYSIZE).setText(String.valueOf(m_publishManager.getPublishHistorySize()));
            // optional nodes for publish queue
            pubHistElement.addElement(N_QUEUEPERSISTANCE).setText(
                String.valueOf(m_publishManager.isPublishQueuePersistanceEnabled()));
            pubHistElement.addElement(N_QUEUESHUTDOWNTIME).setText(
                String.valueOf(m_publishManager.getPublishQueueShutdowntime()));
        }

        // session storage provider
        if (m_sessionStorageProvider != null) {
            Element sessionStorageProviderElem = systemElement.addElement(N_SESSION_STORAGEPROVIDER);
            sessionStorageProviderElem.addAttribute(A_CLASS, m_sessionStorageProvider);
        }

        // permission handler
        if (m_permissionHandler != null) {
            Element permissionHandlerElem = systemElement.addElement(N_PERMISSIONHANDLER);
            permissionHandlerElem.addAttribute(A_CLASS, m_permissionHandler);
        }

        // servlet container settings
        CmsServletContainerSettings servletContainerSettings = OpenCms.getSystemInfo().getServletContainerSettings();
        if (!servletContainerSettings.getMode().isNone()) {
            Element servletContainerSettingsElem = systemElement.addElement(N_SERVLETCONTAINERSETTINGS);
            servletContainerSettingsElem.addAttribute(A_MODE, servletContainerSettings.getMode().getMode());
            if (!servletContainerSettings.getMode().isAuto()) {
                servletContainerSettingsElem.addElement(N_PREVENTRESPONSEFLUSH).addText(
                    "" + servletContainerSettings.isPreventResponseFlush());
                servletContainerSettingsElem.addElement(N_RELEASETAGSAFTEREND).addText(
                    "" + servletContainerSettings.isReleaseTagsAfterEnd());
                if (servletContainerSettings.getRequestErrorPageAttribute() != null) {
                    servletContainerSettingsElem.addElement(N_REQUESTERRORPAGEATTRIBUTE).addText(
                        servletContainerSettings.getRequestErrorPageAttribute());
                }
            }
        }

        // return the system node
        return systemElement;
    }

    /**
     * Returns an instance of the configured authorization handler.<p>
     * 
     * @return an instance of the configured authorization handler
     */
    public I_CmsAuthorizationHandler getAuthorizationHandler() {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_authorizationHandler)) {
            return new CmsDefaultAuthorizationHandler();
        }
        try {
            I_CmsAuthorizationHandler authorizationHandler = (I_CmsAuthorizationHandler)Class.forName(
                m_authorizationHandler).newInstance();
            if (LOG.isInfoEnabled()) {
                LOG.info(Messages.get().getBundle().key(
                    Messages.INIT_AUTHORIZATION_HANDLER_CLASS_SUCCESS_1,
                    m_authorizationHandler));
            }
            return authorizationHandler;
        } catch (Throwable t) {
            LOG.error(Messages.get().getBundle().key(
                Messages.INIT_AUTHORIZATION_HANDLER_CLASS_INVALID_1,
                m_authorizationHandler), t);
            return new CmsDefaultAuthorizationHandler();
        }
    }

    /**
     * Returns the settings of the driver manager.<p>
     *
     * @return the settings of the driver manager
     */
    public CmsCacheSettings getCacheSettings() {

        return m_cacheSettings;
    }

    /**
     * Returns the default users.<p>
     *
     * @return the default users
     */
    public CmsDefaultUsers getCmsDefaultUsers() {

        return m_cmsDefaultUsers;
    }

    /**
     * Returns the flexCacheConfiguration.<p>
     *
     * @return the flexCacheConfiguration
     */
    public CmsFlexCacheConfiguration getCmsFlexCacheConfiguration() {

        return m_cmsFlexCacheConfiguration;
    }

    /**
     * Returns the memory monitor configuration.<p>
     *
     * @return the memory monitor configuration
     */
    public CmsMemoryMonitorConfiguration getCmsMemoryMonitorConfiguration() {

        return m_cmsMemoryMonitorConfiguration;
    }

    /**
     * Returns the defaultContentEncoding.<p>
     *
     * @return the defaultContentEncoding
     */
    public String getDefaultContentEncoding() {

        return m_defaultContentEncoding;
    }

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#getDtdFilename()
     */
    public String getDtdFilename() {

        return CONFIGURATION_DTD_NAME;
    }

    /**
     * Returns the configured OpenCms event manager instance.<p>
     * 
     * @return the configured OpenCms event manager instance
     */
    public CmsEventManager getEventManager() {

        return m_eventManager;
    }

    /**
     * Returns the maximum number of versions that are kept per resource in the VFS version history.<p>
     * 
     * If the version history is disabled, this setting has no effect.<p>
     * 
     * @return the maximum number of versions that are kept per resource
     * 
     * @see #isHistoryEnabled()
     */
    public int getHistoryVersions() {

        return m_historyVersions;
    }

    /**
     * Returns the maximum number of versions that are kept in the VFS version history for deleted resources.<p>
     * 
     * If the version history is disabled, this setting has no effect.<p>
     * 
     * @return the maximum number of versions that are kept for deleted resources
     * 
     * @see #isHistoryEnabled()
     */
    public int getHistoryVersionsAfterDeletion() {

        return m_historyVersionsAfterDeletion;
    }

    /**
     * Returns the HTTP authentication settings.<p>
     *
     * @return the HTTP authentication settings
     */
    public CmsHttpAuthenticationSettings getHttpAuthenticationSettings() {

        return m_httpAuthenticationSettings;
    }

    /**
     * Returns the configured locale manager for multi language support.<p>
     * 
     * @return the configured locale manager for multi language support
     */
    public CmsLocaleManager getLocaleManager() {

        return m_localeManager;
    }

    /**
     * Returns the configured login manager.<p>
     *
     * @return the configured login manager
     */
    public CmsLoginManager getLoginManager() {

        if (m_loginManager == null) {
            // no login manager configured, create default
            m_loginManager = new CmsLoginManager(
                CmsLoginManager.DISABLE_MINUTES_DEFAULT,
                CmsLoginManager.MAX_BAD_ATTEMPTS_DEFAULT);
        }
        if (m_loginMessage != null) {
            // null OpenCms object is ok during configuration
            try {
                m_loginManager.setLoginMessage(null, m_loginMessage);
            } catch (CmsRoleViolationException e) {
                // this should never happen
            }
        }
        return m_loginManager;
    }

    /**
     * Returns the configured mail settings.<p>
     * 
     * @return the configured mail settings
     */
    public CmsMailSettings getMailSettings() {

        return m_mailSettings;
    }

    /**
     * Returns the project in which timestamps for the content notification are read.<p>
     * 
     * @return the project in which timestamps for the content notification are read
     */
    public String getNotificationProject() {

        return m_notificationProject;
    }

    /**
     * Returns the duration after which responsibles will be notified about out-dated content (in days).<p>
     * 
     * @return the duration after which responsibles will be notified about out-dated content
     */
    public int getNotificationTime() {

        if (m_notificationTime != null) {
            return m_notificationTime.intValue();
        } else {
            return -1;
        }
    }

    /**
     * Returns the configured password handler.<p>
     * 
     * @return the configured password handler
     */
    public I_CmsPasswordHandler getPasswordHandler() {

        return m_passwordHandler;
    }

    /**
     * Returns the permission Handler class name.<p>
     *
     * @return the permission Handler class name
     */
    public String getPermissionHandler() {

        return m_permissionHandler;
    }

    /**
     * Returns the configured publish manager.<p>
     * 
     * @return the configured publish manager
     */
    public CmsPublishManager getPublishManager() {

        if (m_publishManager == null) {
            // no publish manager configured, create default
            m_publishManager = new CmsPublishManager(
                CmsPublishManager.DEFAULT_HISTORY_SIZE,
                CmsPublishManager.DEFAULT_QUEUE_PERSISTANCE,
                CmsPublishManager.DEFAULT_QUEUE_SHUTDOWNTIME);
        }
        return m_publishManager;
    }

    /**
     * Returns the list of instantiated request handler classes.<p>
     * 
     * @return the list of instantiated request handler classes
     */
    public List getRequestHandlers() {

        return m_requestHandlers;
    }

    /**
     * Returns the list of instantiated resource init handler classes.<p>
     * 
     * @return the list of instantiated resource init handler classes
     */
    public List getResourceInitHandlers() {

        return m_resourceInitHandlers;
    }

    /**
     * Returns the runtime info factory instance.<p>
     * 
     * @return the runtime info factory instance
     */
    public I_CmsDbContextFactory getRuntimeInfoFactory() {

        return m_runtimeInfoFactory;
    }

    /**
     * Returns the runtime Properties.<p>
     *
     * @return the runtime Properties
     */
    public Map getRuntimeProperties() {

        return m_runtimeProperties;
    }

    /**
     * Returns the configured schedule manager.<p>
     *
     * @return the configured schedule manager
     */
    public CmsScheduleManager getScheduleManager() {

        return m_scheduleManager;
    }

    /**
     * Returns an instance of the configured session storage provider.<p>
     * 
     * @return an instance of the configured session storage provider
     */
    public I_CmsSessionStorageProvider getSessionStorageProvider() {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_sessionStorageProvider)) {
            return new CmsDefaultSessionStorageProvider();
        }
        try {
            I_CmsSessionStorageProvider sessionCacheProvider = (I_CmsSessionStorageProvider)Class.forName(
                m_sessionStorageProvider).newInstance();
            if (CmsLog.INIT.isInfoEnabled()) {
                CmsLog.INIT.info(Messages.get().getBundle().key(
                    Messages.INIT_SESSION_STORAGEPROVIDER_SUCCESS_1,
                    m_sessionStorageProvider));
            }
            return sessionCacheProvider;
        } catch (Throwable t) {
            LOG.error(Messages.get().getBundle().key(
                Messages.LOG_INIT_SESSION_STORAGEPROVIDER_FAILURE_1,
                m_sessionStorageProvider), t);
            return new CmsDefaultSessionStorageProvider();
        }
    }

    /**
     * Returns the site manager.<p>
     *
     * @return the site manager
     */
    public CmsSiteManagerImpl getSiteManager() {

        return m_siteManager;
    }

    /**
     * Returns temporary file project id.<p>
     * 
     * @return temporary file project id
     */
    public int getTempFileProjectId() {

        return m_tempFileProjectId;
    }

    /**
     * Returns an instance of the configured validation handler.<p>
     * 
     * @return an instance of the configured validation handler
     */
    public I_CmsValidationHandler getValidationHandler() {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_validationHandler)) {
            return new CmsDefaultValidationHandler();
        }
        try {
            I_CmsValidationHandler validationHandler = (I_CmsValidationHandler)Class.forName(m_validationHandler).newInstance();
            if (LOG.isInfoEnabled()) {
                LOG.info(Messages.get().getBundle().key(
                    Messages.INIT_VALIDATION_HANDLER_CLASS_SUCCESS_1,
                    m_validationHandler));
            }
            return validationHandler;
        } catch (Throwable t) {
            LOG.error(Messages.get().getBundle().key(
                Messages.INIT_VALIDATION_HANDLER_CLASS_INVALID_1,
                m_validationHandler), t);
            return new CmsDefaultValidationHandler();
        }
    }

    /**
     * Will be called when configuration of this object is finished.<p> 
     */
    public void initializeFinished() {

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_SYSTEM_CONFIG_FINISHED_0));
        }
    }

    /**
     * Returns if the VFS version history is enabled.<p> 
     * 
     * @return if the VFS version history is enabled
     */
    public boolean isHistoryEnabled() {

        return m_historyEnabled;
    }

    /**
     * Sets the authorization handler.<p>
     * 
     * @param authorizationHandlerClass the authorization handler class to set.
     */
    public void setAuthorizationHandler(String authorizationHandlerClass) {

        m_authorizationHandler = authorizationHandlerClass;
    }

    /**
     * Sets the settings of the driver manager.<p>
     *
     * @param settings the settings of the driver manager
     */
    public void setCacheSettings(CmsCacheSettings settings) {

        m_cacheSettings = settings;
    }

    /**
     * Sets the CmsDefaultUsers.<p>
     * 
     * @param userAdmin the name of the default admin user
     * @param userGuest the name of the guest user
     * @param userExport the name of the export user
     * @param userDeletedResource the name of the deleted resource user, can be <code>null</code>
     * @param groupAdministrators the name of the administrators group
     * @param groupProjectmanagers the name of the project managers group
     * @param groupUsers the name of the users group
     * @param groupGuests the name of the guests group
     */
    public void setCmsDefaultUsers(

        String userAdmin,
        String userGuest,
        String userExport,
        String userDeletedResource,
        String groupAdministrators,
        String groupProjectmanagers,
        String groupUsers,
        String groupGuests) {

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_CHECKING_DEFAULT_USER_NAMES_0));
        }
        m_cmsDefaultUsers = new CmsDefaultUsers(
            userAdmin,
            userGuest,
            userExport,
            userDeletedResource,
            groupAdministrators,
            groupProjectmanagers,
            groupUsers,
            groupGuests);

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(
                Messages.INIT_ADMIN_USER_1,
                m_cmsDefaultUsers.getUserAdmin()));
            CmsLog.INIT.info(Messages.get().getBundle().key(
                Messages.INIT_GUEST_USER_1,
                m_cmsDefaultUsers.getUserGuest()));
            CmsLog.INIT.info(Messages.get().getBundle().key(
                Messages.INIT_EXPORT_USER_1,
                m_cmsDefaultUsers.getUserExport()));
            CmsLog.INIT.info(Messages.get().getBundle().key(
                Messages.INIT_DELETED_RESOURCE_USER_1,
                m_cmsDefaultUsers.getUserDeletedResource()));
            CmsLog.INIT.info(Messages.get().getBundle().key(
                Messages.INIT_ADMIN_GROUP_1,
                m_cmsDefaultUsers.getGroupAdministrators()));
            CmsLog.INIT.info(Messages.get().getBundle().key(
                Messages.INIT_PROJECT_MANAGERS_GROUP_1,
                m_cmsDefaultUsers.getGroupProjectmanagers()));
            CmsLog.INIT.info(Messages.get().getBundle().key(
                Messages.INIT_USERS_GROUP_1,
                m_cmsDefaultUsers.getGroupUsers()));
            CmsLog.INIT.info(Messages.get().getBundle().key(
                Messages.INIT_GUESTS_GROUP_1,
                m_cmsDefaultUsers.getGroupGuests()));
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_DEFAULT_USER_NAMES_INITIALIZED_0));
        }
    }

    /**
     * Sets the flexCacheConfiguration.<p>
     *
     * @param flexCacheConfiguration the flexCacheConfiguration to set
     */
    public void setCmsFlexCacheConfiguration(CmsFlexCacheConfiguration flexCacheConfiguration) {

        m_cmsFlexCacheConfiguration = flexCacheConfiguration;
    }

    /**
     * Sets the cmsMemoryMonitorConfiguration.<p>
     *
     * @param cmsMemoryMonitorConfiguration the cmsMemoryMonitorConfiguration to set
     */
    public void setCmsMemoryMonitorConfiguration(CmsMemoryMonitorConfiguration cmsMemoryMonitorConfiguration) {

        m_cmsMemoryMonitorConfiguration = cmsMemoryMonitorConfiguration;
    }

    /**
     * Sets the defaultContentEncoding.<p>
     *
     * @param defaultContentEncoding the defaultContentEncoding to set
     */
    public void setDefaultContentEncoding(String defaultContentEncoding) {

        m_defaultContentEncoding = defaultContentEncoding;
    }

    /**
     * VFS version history settings are set here.<p>
     * 
     * @param historyEnabled if true the history is enabled
     * @param historyVersions the maximum number of versions that are kept per VFS resource
     * @param historyVersionsAfterDeletion the maximum number of versions for deleted resources
     */
    public void setHistorySettings(String historyEnabled, String historyVersions, String historyVersionsAfterDeletion) {

        m_historyEnabled = Boolean.valueOf(historyEnabled).booleanValue();
        m_historyVersions = Integer.valueOf(historyVersions).intValue();
        m_historyVersionsAfterDeletion = Integer.valueOf(historyVersionsAfterDeletion).intValue();
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(
                Messages.INIT_HISTORY_SETTINGS_3,
                Boolean.valueOf(m_historyEnabled),
                new Integer(m_historyVersions),
                new Integer(m_historyVersionsAfterDeletion)));
        }
    }

    /**
     * Sets the HTTP authentication settings.<p>
     *
     * @param httpAuthenticationSettings the HTTP authentication settings to set
     */
    public void setHttpAuthenticationSettings(CmsHttpAuthenticationSettings httpAuthenticationSettings) {

        m_httpAuthenticationSettings = httpAuthenticationSettings;
    }

    /**
     * Sets the locale manager for multi language support.<p>
     * 
     * @param localeManager the locale manager to set
     */
    public void setLocaleManager(CmsLocaleManager localeManager) {

        m_localeManager = localeManager;
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_CONFIG_I18N_FINISHED_0));
        }
    }

    /**
     * Sets the configured login manager.<p>
     *
     * @param maxBadAttemptsStr the number of allowed bad login attempts
     * @param disableMinutesStr the time an account gets locked if to many bad logins are attempted
     */
    public void setLoginManager(String disableMinutesStr, String maxBadAttemptsStr) {

        int disableMinutes;
        try {
            disableMinutes = Integer.valueOf(disableMinutesStr).intValue();
        } catch (NumberFormatException e) {
            disableMinutes = CmsLoginManager.DISABLE_MINUTES_DEFAULT;
        }
        int maxBadAttempts;
        try {
            maxBadAttempts = Integer.valueOf(maxBadAttemptsStr).intValue();
        } catch (NumberFormatException e) {
            maxBadAttempts = CmsLoginManager.MAX_BAD_ATTEMPTS_DEFAULT;
        }
        m_loginManager = new CmsLoginManager(disableMinutes, maxBadAttempts);
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(
                Messages.INIT_LOGINMANAGER_2,
                new Integer(disableMinutes),
                new Integer(maxBadAttempts)));
        }
    }

    /**
     * Adds the login message from the configuration.<p>
     * 
     * @param message the login message to add
     */
    public void setLoginMessage(CmsLoginMessage message) {

        m_loginMessage = message;
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(
                Messages.INIT_LOGINMESSAGE_3,
                Boolean.valueOf(message.isEnabled()),
                Boolean.valueOf(message.isLoginForbidden()),
                message.getMessage()));
        }
    }

    /**
     * Sets the mail settings.<p>
     * 
     * @param mailSettings the mail settings to set.
     */
    public void setMailSettings(CmsMailSettings mailSettings) {

        m_mailSettings = mailSettings;
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_MAIL_SETTINGS_1, mailSettings));
        }
    }

    /**
     * Sets the project in which timestamps for the content notification are read.<p>
     * 
     * @param notificationProject the project in which timestamps for the content notification are read
     */
    public void setNotificationProject(String notificationProject) {

        m_notificationProject = notificationProject;
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_NOTIFICATION_PROJECT_1, m_notificationProject));
        }
    }

    /**
     * Sets the duration after which responsibles will be notified about out-dated content (in days).<p>
     * 
     * @param notificationTime the duration after which responsibles will be notified about out-dated content
     */
    public void setNotificationTime(String notificationTime) {

        try {
            m_notificationTime = new Integer(notificationTime);
        } catch (Throwable t) {
            m_notificationTime = new Integer(-1);
        }
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_NOTIFICATION_TIME_1, m_notificationTime));
        }
    }

    /**
     * Sets the password handler class.<p>
     * 
     * @param passwordHandler the password handler to set
     */
    public void setPasswordHandler(I_CmsPasswordHandler passwordHandler) {

        m_passwordHandler = passwordHandler;
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(
                Messages.INIT_PWD_HANDLER_SUCCESS_1,
                passwordHandler.getClass().getName()));
        }
    }

    /**
     * Sets the permission Handler class name.<p>
     *
     * @param permissionHandler the class name to set
     */
    public void setPermissionHandler(String permissionHandler) {

        m_permissionHandler = permissionHandler;
    }

    /**
     * Sets the servlet container specific setting.<p>
     * 
     * @param configValue the configuration value
     */
    public void setPreventResponseFlush(String configValue) {

        OpenCms.getSystemInfo().getServletContainerSettings().setPreventResponseFlush(
            Boolean.valueOf(configValue).booleanValue());
    }

    /**
     * Sets the publish manager.<p>
     * 
     * @param publishManager the publish manager
     */
    public void setPublishManager(CmsPublishManager publishManager) {

        m_publishManager = publishManager;
    }

    /**
     * Sets the servlet container specific setting.<p>
     * 
     * @param configValue the configuration value
     */
    public void setReleaseTagsAfterEnd(String configValue) {

        OpenCms.getSystemInfo().getServletContainerSettings().setReleaseTagsAfterEnd(
            Boolean.valueOf(configValue).booleanValue());
    }

    /**
     * Sets the servlet container specific setting.<p>
     * 
     * @param configValue the configuration value
     */
    public void setRequestErrorPageAttribute(String configValue) {

        OpenCms.getSystemInfo().getServletContainerSettings().setRequestErrorPageAttribute(configValue);
    }

    /**
     * Sets the runtime info factory.<p>
     * 
     * @param className the class name of the configured runtime info factory
     */
    public void setRuntimeInfoFactory(String className) {

        Object objectInstance;

        try {
            objectInstance = Class.forName(className).newInstance();
        } catch (Throwable t) {
            LOG.error(Messages.get().getBundle().key(Messages.LOG_RESOURCE_INIT_FAILURE_1, className), t);
            return;
        }

        if (objectInstance instanceof I_CmsDbContextFactory) {
            m_runtimeInfoFactory = (I_CmsDbContextFactory)objectInstance;
            if (CmsLog.INIT.isInfoEnabled()) {
                CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_RUNTIME_INFO_FACTORY_SUCCESS_1, className));
            }
        } else {
            if (CmsLog.INIT.isFatalEnabled()) {
                CmsLog.INIT.fatal(Messages.get().getBundle().key(
                    Messages.INIT_RUNTIME_INFO_FACTORY_FAILURE_1,
                    className));
            }
        }

    }

    /**
     * Sets the servlet container settings configuration mode.<p>
     * 
     * @param configValue the value to set
     */
    public void setServletContainerSettingsMode(String configValue) {

        OpenCms.getSystemInfo().getServletContainerSettings().setMode(configValue);
    }

    /**
     * Sets the session storage provider.<p>
     * 
     * @param sessionStorageProviderClass the session storage provider class to set.
     */
    public void setSessionStorageProvider(String sessionStorageProviderClass) {

        m_sessionStorageProvider = sessionStorageProviderClass;
    }

    /**
     * Sets the site manager.<p>
     *
     * @param siteManager the site manager to set
     */
    public void setSiteManager(CmsSiteManagerImpl siteManager) {

        m_siteManager = siteManager;
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_SITE_CONFIG_FINISHED_0));
        }
    }

    /**
     * Sets the temporary file project id.<p>
     * 
     * @param tempFileProjectId the temporary file project id to set
     */
    public void setTempFileProjectId(String tempFileProjectId) {

        try {
            m_tempFileProjectId = Integer.valueOf(tempFileProjectId).intValue();
        } catch (Throwable t) {
            m_tempFileProjectId = -1;
        }
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(
                Messages.INIT_TEMPFILE_PROJECT_ID_1,
                new Integer(m_tempFileProjectId)));
        }
    }

    /**
     * Sets the validation handler.<p>
     * 
     * @param validationHandlerClass the validation handler class to set.
     */
    public void setValidationHandler(String validationHandlerClass) {

        m_validationHandler = validationHandlerClass;
    }
}