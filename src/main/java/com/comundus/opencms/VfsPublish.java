package com.comundus.opencms;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsRequestContext;

import org.opencms.main.CmOpenCmsShell;
import org.opencms.main.OpenCms;

import org.opencms.report.CmsShellReport;
import org.opencms.report.I_CmsReport;

import java.io.File;

import java.util.Iterator;
import java.util.List;


/**
 * Publishes resources in VFS.
 *
 */

//(C) comundus GmbH, D-71332 WAIBLINGEN, www.comundus.com
public class VfsPublish {
    /** The CmsObject. */
    private CmsObject cms;

    /** The report to write the output to. */
    private I_CmsReport report;

    /**
     * Publishes the given List of VFS paths.
     *
     * @param webappDirectory
     *            path to WEB-INF of the OpenCms installation
     * @param syncVFSPaths
     *            List of VFS paths to publish
     * @param adminPassword
     *            password of user "Admin" performing the operation
     * @throws Exception
     *             if anything goes wrong
     */
    public final void execute(final String webappDirectory,
        final List syncVFSPaths, final String adminPassword)
        throws Exception {
        final String webinfdir = webappDirectory + File.separatorChar +
            "WEB-INF";
        final CmOpenCmsShell cmsshell = CmOpenCmsShell.getInstance(webinfdir,
                "Admin", adminPassword);
        this.cms = cmsshell.getCmsObject();

        final CmsRequestContext requestcontext = this.cms.getRequestContext();
        this.report = new CmsShellReport(requestcontext.getLocale());
        requestcontext.setCurrentProject(this.cms.readProject("Offline"));

        final Iterator i = syncVFSPaths.iterator();

        while (i.hasNext()) {
            final String sourcePathInVfs = (String) i.next();
            OpenCms.getPublishManager()
                   .publishResource(this.cms, sourcePathInVfs, true, this.report);
        }

        OpenCms.getPublishManager().waitWhileRunning();
    }
}
