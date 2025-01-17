/*
 * File   : $Source: /usr/local/cvs/opencms/src/org/opencms/report/CmsLogReport.java,v $
 * Date   : $Date: 2008-02-27 12:05:41 $
 * Version: $Revision: 1.25 $
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

package org.opencms.report;

import org.opencms.main.CmsLog;

import java.util.Locale;

/**
 * Report class used for the logfile.<p>
 * 
 * This prints all messages in the logfile at INFO level.<p>
 * 
 * @author Alexander Kandzior 
 * @author Jan Baudisch 
 * @author Peter Bonrad
 * 
 * @version $Revision: 1.25 $ 
 * 
 * @since 6.0.0 
 */
public class CmsLogReport extends A_CmsReport {

    /** The buffer to write the log messages to. */
    private StringBuffer m_buffer;

    /** The class name to use for the logger. */
    private Class m_clazz;

    /**
     * Constructs a new report using the provided locale for the output language, 
     * using the provided Java class for the log channel.<p>
     * 
     * @param locale the locale to use for the report output messages
     * @param clazz the the class for the logger channel 
     */
    public CmsLogReport(Locale locale, Class clazz) {

        init(locale, null);
        m_buffer = new StringBuffer();
        if (clazz == null) {
            clazz = CmsLogReport.class;
        }
        m_clazz = clazz;
    }

    /**
     * @see org.opencms.report.I_CmsReport#getReportUpdate()
     */
    public String getReportUpdate() {

        return "";
    }

    /**
     * @see org.opencms.report.A_CmsReport#print(java.lang.String, int)
     */
    public synchronized void print(String value, int format) {

        switch (format) {
            case FORMAT_HEADLINE:
                m_buffer.append("[ ");
                m_buffer.append(value);
                m_buffer.append(" ]");
                break;
            case FORMAT_WARNING:
                m_buffer.append("!!! ");
                m_buffer.append(value);
                m_buffer.append(" !!!");
                addWarning(value);
                break;
            case FORMAT_ERROR:
                m_buffer.append("!!! ");
                m_buffer.append(value);
                m_buffer.append(" !!!");
                addError(value);
                break;
            case FORMAT_NOTE:
            case FORMAT_OK:
            case FORMAT_DEFAULT:
            default:
                m_buffer.append(value);
        }
    }

    /**
     * @see org.opencms.report.I_CmsReport#println()
     */
    public synchronized void println() {

        if (CmsLog.getLog(m_clazz).isInfoEnabled()) {
            CmsLog.getLog(m_clazz).info(m_buffer.toString());
        }
        m_buffer = new StringBuffer();
    }

    /**
     * @see org.opencms.report.I_CmsReport#println(java.lang.Throwable)
     */
    public synchronized void println(Throwable t) {

        if (CmsLog.getLog(m_clazz).isInfoEnabled()) {
            StringBuffer message = new StringBuffer();
            message.append(getMessages().key(Messages.RPT_EXCEPTION_0));
            message.append(t.getMessage());
            m_buffer.append(message);
            addError(message.toString());
            CmsLog.getLog(m_clazz).info(m_buffer.toString(), t);
        }
        m_buffer = new StringBuffer();
    }
}