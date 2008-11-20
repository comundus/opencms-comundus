/*
 * File   : $Source: /usr/local/cvs/opencms/src/org/opencms/setup/comptest/CmsSetupTestWarFileUnpacked.java,v $
 * Date   : $Date: 2007-07-04 16:57:38 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.setup.comptest;

import org.opencms.main.CmsSystemInfo;
import org.opencms.setup.CmsSetupBean;

import java.io.File;

/**
 * Tests if the OpenCms WAR file is unpacked.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.3 $ 
 * 
 * @since 6.1.8 
 */
public class CmsSetupTestWarFileUnpacked implements I_CmsSetupTest {

    /** The test name. */
    public static final String TEST_NAME = "Unpacked WAR File";

    /**
     * @see org.opencms.setup.comptest.I_CmsSetupTest#getName()
     */
    public String getName() {

        return TEST_NAME;
    }

    /**
     * @see org.opencms.setup.comptest.I_CmsSetupTest#execute(org.opencms.setup.CmsSetupBean)
     */
    public CmsSetupTestResult execute(CmsSetupBean setupBean) {

        CmsSetupTestResult testResult = new CmsSetupTestResult(this);

        String basePath = setupBean.getWebAppRfsPath();
        if (!basePath.endsWith(File.separator)) {
            basePath += File.separator;
        }
        File file = new File(basePath
            + CmsSystemInfo.FOLDER_WEBINF
            + CmsSystemInfo.FOLDER_CONFIG
            + CmsSystemInfo.FILE_PROPERTIES);
        if (file.exists() && file.canRead() && file.canWrite()) {
            testResult.setGreen();
            testResult.setResult(RESULT_PASSED);
        } else {
            testResult.setRed();
            testResult.setInfo("OpenCms cannot be installed unless the OpenCms WAR file is unpacked! "
                + "Please check the settings of your servlet container or unpack the WAR file manually.");
            testResult.setHelp("WAR file NOT unpacked");
            testResult.setResult(RESULT_FAILED);
        }
        return testResult;
    }
}
