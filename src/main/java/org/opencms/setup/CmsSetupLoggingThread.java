/*
 * File   : $Source: /usr/local/cvs/opencms/src/org/opencms/setup/CmsSetupLoggingThread.java,v $
 * Date   : $Date: 2006-03-27 14:52:51 $
 * Version: $Revision: 1.13 $
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

package org.opencms.setup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Logging Thread which collects the output from CmsSetupThread and
 * stores it in a Vector that the OpenCms setup wizard can read via
 * the getMessages() method.<p>
 *
 * @author  Alexander Kandzior 
 * 
 * @version $Revision: 1.13 $ 
 * 
 * @since 6.0.0 
 */
public class CmsSetupLoggingThread extends Thread {

    private LineNumberReader m_lineReader;
    private FileWriter m_logWriter;
    private List m_messages;
    private PipedInputStream m_pipedIn;
    private PipedOutputStream m_pipedOut;
    private boolean m_stopThread;

    /** 
     * Constructor.<p>
     * 
     * @param pipedOut the output stream to write to 
     * @param log the file name to write the log to (if null, no log is written)
     */
    public CmsSetupLoggingThread(PipedOutputStream pipedOut, String log) {

        super("OpenCms: Setup logging");

        m_pipedOut = pipedOut;
        m_messages = new ArrayList();
        m_stopThread = false;

        if (log != null) {
            try {
                File logFile = new File(log);
                if (logFile.exists()) {
                    logFile.delete();
                }
                m_logWriter = new FileWriter(logFile);
            } catch (Throwable t) {
                m_logWriter = null;
            }
        } else {
            m_logWriter = null;
        }

        try {
            m_pipedIn = new PipedInputStream();
            m_pipedIn.connect(m_pipedOut);
            m_lineReader = new LineNumberReader(new BufferedReader(new InputStreamReader(m_pipedIn)));
        } catch (Exception e) {
            m_messages.add(e.toString());
        }
    }

    /** 
     * Returns a Vector with the last collected log messages.<p>
     * 
     * @return a Vector with the last collected log messages
     */
    public List getMessages() {

        return m_messages;
    }

    /**
     * Returns <code>"true"</code> if the logging is finished.<p>
     * 
     * @return <code>"true"</code> if the logging is finished
     */
    public boolean isFinished() {

        return m_stopThread;
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {

        int lineNr = 0;
        int lastLineNr = -1;
        String line = null;
        while (!m_stopThread) {
            lineNr = m_lineReader.getLineNumber();
            try {
                line = m_lineReader.readLine();
            } catch (IOException e) {
                // "Write end dead" IO exceptions can be ignored                
            }
            if (line != null) {
                if (lineNr > lastLineNr) {
                    // supress multiple output of the same line after "Write end dead" IO exception 
                    String content = (lineNr + 1) + ":\t" + line;
                    m_messages.add(content);
                    lastLineNr = lineNr;
                    if (m_logWriter != null) {
                        try {
                            m_logWriter.write(content + "\n");
                        } catch (IOException e) {
                            m_logWriter = null;
                        }
                    }
                }
            }
        }
        try {
            m_pipedIn.close();
        } catch (IOException e) {
            // ignore
        }
        if (m_logWriter != null) {
            try {
                m_logWriter.close();
            } catch (IOException e) {
                m_logWriter = null;
            }
        }
    }

    /** 
     * Used to break the loop in the run() method.<p> 
     */
    public void stopThread() {

        try {
            // give the logging thread a chance to read all remaining messages
            Thread.sleep(1000);
        } catch (Throwable t) {
            // ignore
        }
        m_stopThread = true;
    }

}