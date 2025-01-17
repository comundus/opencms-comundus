/*
 * File   : $Source: /usr/local/cvs/opencms/src/org/opencms/flex/CmsFlexCacheEntry.java,v $
 * Date   : $Date: 2008-02-27 12:05:47 $
 * Version: $Revision: 1.34 $
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

package org.opencms.flex;

import org.opencms.cache.I_CmsLruCacheObject;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsLog;
import org.opencms.monitor.CmsMemoryMonitor;
import org.opencms.monitor.I_CmsMemoryMonitorable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

import org.apache.commons.logging.Log;

/**
 * Contains the contents of a cached resource.<p>
 * 
 * It is basically a list of pre-generated output,
 * include() calls to other resources (with request parameters) and http headers that this 
 * resource requires to be set.<p>
 *
 * A CmsFlexCacheEntry might also describe a redirect-call, but in this case
 * nothing else will be cached.<p>
 *
 * The pre-generated output is saved in <code>byte[]</code> arrays.
 * The include() calls are saved as Strings of the included resource name, 
 * the parameters for the calls are saved in a HashMap.
 * The headers are saved in a HashMap.
 * In case of a redirect, the redirect target is cached in a String.<p>
 *
 * The CmsFlexCacheEntry can also have an expire date value, which indicates the time 
 * that his entry will become invalid and should thus be cleared from the cache.<p>
 *
 * @author  Alexander Kandzior 
 * @author Thomas Weckert  
 * 
 * @version $Revision: 1.34 $ 
 * 
 * @since 6.0.0 
 * 
 * @see org.opencms.cache.I_CmsLruCacheObject
 */
public class CmsFlexCacheEntry extends Object implements I_CmsLruCacheObject, I_CmsMemoryMonitorable {

    /** Initial size for lists. */
    public static final int INITIAL_CAPACITY_LISTS = 10;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsFlexCacheEntry.class);

    /** The CacheEntry's size in bytes. */
    private int m_byteSize;

    /** Indicates if this cache entry is completed. */
    private boolean m_completed;

    /** The "expires" date for this Flex cache entry. */
    private long m_dateExpires;

    /** The "last modified" date for this Flex cache entry. */
    private long m_dateLastModified;

    /** The list of items for this resource. */
    private List m_elements;

    /** A Map of cached headers for this resource. */
    private Map m_headers;

    /** Pointer to the next cache entry in the LRU cache. */
    private I_CmsLruCacheObject m_next;

    /** Pointer to the previous cache entry in the LRU cache. */
    private I_CmsLruCacheObject m_previous;

    /** A redirection target (if redirection is set). */
    private String m_redirectTarget;

    /** The key under which this cache entry is stored in the variation map. */
    private String m_variationKey;

    /** The variation map where this cache entry is stored. */
    private Map m_variationMap;

    /** 
     * Constructor for class CmsFlexCacheEntry.<p>
     * 
     * The way to use this class is to first use this empty constructor 
     * and later add data with the various add methods.
     */
    public CmsFlexCacheEntry() {

        m_elements = new ArrayList(INITIAL_CAPACITY_LISTS);
        m_dateExpires = CmsResource.DATE_EXPIRED_DEFAULT;
        m_dateLastModified = -1;
        // base memory footprint of this object with all referenced objects
        m_byteSize = 1024;

        setNextLruObject(null);
        setPreviousLruObject(null);
    }

    /** 
     * Adds an array of bytes to this cache entry,
     * this will usually be the result of some kind of output - stream.<p>
     *
     * @param bytes the output to save in the cache
     */
    public void add(byte[] bytes) {

        if (m_completed) {
            return;
        }
        if (m_redirectTarget == null) {
            // Add only if not already redirected
            m_elements.add(bytes);
            m_byteSize += CmsMemoryMonitor.getMemorySize(bytes);
        }
    }

    /** 
     * Add an include - call target resource to this cache entry.<p>
     *
     * @param resource a name of a resource in the OpenCms VFS
     * @param parameters a map of parameters specific to this include call
     */
    public void add(String resource, Map parameters) {

        if (m_completed) {
            return;
        }
        if (m_redirectTarget == null) {
            // Add only if not already redirected
            m_elements.add(resource);
            if (parameters == null) {
                parameters = Collections.EMPTY_MAP;
            }
            m_elements.add(parameters);
            m_byteSize += CmsMemoryMonitor.getMemorySize(resource);
        }
    }

    /** 
     * Add a map of headers to this cache entry,
     * which are usually collected in the class CmsFlexResponse first.<p>
     *
     * @param headers the map of headers to add to the entry 
     */
    public void addHeaders(Map headers) {

        if (m_completed) {
            return;
        }
        m_headers = headers;

        Iterator allHeaders = m_headers.keySet().iterator();
        while (allHeaders.hasNext()) {
            m_byteSize += CmsMemoryMonitor.getMemorySize(allHeaders.next());
        }
    }

    /**
     * @see org.opencms.cache.I_CmsLruCacheObject#addToLruCache()
     */
    public void addToLruCache() {

        // do nothing here...
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_FLEXCACHEENTRY_ADDED_ENTRY_1, this));
        }
    }

    /**
     * Completes this cache entry.<p>
     * 
     * A completed cache entry is made "unmodifiable",
     * so that no further data can be added and existing data can not be changed.<p>
     * 
     * This is to prevent the (unlikely) case that some user-written class 
     * tries to make changes to a cache entry.<p>
     */
    public void complete() {

        m_completed = true;
        // Prevent changing of the cached lists
        if (m_headers != null) {
            m_headers = Collections.unmodifiableMap(m_headers);
        }
        if (m_elements != null) {
            m_elements = Collections.unmodifiableList(m_elements);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_FLEXCACHEENTRY_ENTRY_COMPLETED_1, toString()));
        }
    }

    /**
     * Returns the list of data entries of this cache entry.<p>
     * 
     * Data entries are byte arrays representing some kind of output
     * or Strings representing include calls to other resources.<p>
     *
     * @return the list of data elements of this cache entry
     */
    public List elements() {

        return m_elements;
    }

    /** 
     * Returns the expiration date of this cache entry,
     * this is set to the time when the entry becomes invalid.<p>
     * 
     * @return the expiration date value for this resource
     */
    public long getDateExpires() {

        return m_dateExpires;
    }

    /**
     * Returns the "last modified" date for this Flex cache entry.<p>
     * 
     * @return the "last modified" date for this Flex cache entry
     */
    public long getDateLastModified() {

        return m_dateLastModified;
    }

    /**
     * @see org.opencms.cache.I_CmsLruCacheObject#getLruCacheCosts()
     */
    public int getLruCacheCosts() {

        return m_byteSize;
    }

    /**
     * @see org.opencms.monitor.I_CmsMemoryMonitorable#getMemorySize()
     */
    public int getMemorySize() {

        return getLruCacheCosts();
    }

    /**
     * @see org.opencms.cache.I_CmsLruCacheObject#getNextLruObject()
     */
    public I_CmsLruCacheObject getNextLruObject() {

        return m_next;
    }

    /**
     * @see org.opencms.cache.I_CmsLruCacheObject#getPreviousLruObject()
     */
    public I_CmsLruCacheObject getPreviousLruObject() {

        return m_previous;
    }

    /**
     * @see org.opencms.cache.I_CmsLruCacheObject#getValue()
     */
    public Object getValue() {

        return m_elements;
    }

    /**
     * @see org.opencms.cache.I_CmsLruCacheObject#removeFromLruCache()
     */
    public void removeFromLruCache() {

        if ((m_variationMap != null) && (m_variationKey != null)) {
            m_variationMap.remove(m_variationKey);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(
                Messages.LOG_FLEXCACHEENTRY_REMOVED_ENTRY_FOR_VARIATION_1,
                m_variationKey));
        }
    }

    /** 
     * Processing method for this cached entry.<p>
     *
     * If this method is called, it delivers the contents of
     * the cached entry to the given request / response.
     * This includes calls to all included resources.<p>
     *
     * @param req the request from the client
     * @param res the server response
     * 
     * @throws CmsFlexCacheException is thrown when problems writing to the response output-stream occur
     * @throws ServletException might be thrown from call to RequestDispatcher.include()
     * @throws IOException might be thrown from call to RequestDispatcher.include() or from Response.sendRedirect()
     */
    public void service(CmsFlexRequest req, CmsFlexResponse res)
    throws CmsFlexCacheException, ServletException, IOException {

        if (!m_completed) {
            return;
        }

        if (m_redirectTarget != null) {
            res.setOnlyBuffering(false);
            // redirect the response, no further output required
            res.sendRedirect(m_redirectTarget);
        } else {
            // process cached headers first
            CmsFlexResponse.processHeaders(m_headers, res);
            // check if this cache entry is a "leaf" (i.e. no further includes)            
            boolean hasNoSubElements = (m_elements.size() == 1);
            // write output to stream and process all included elements
            for (int i = 0; i < m_elements.size(); i++) {
                Object o = m_elements.get(i);
                if (o instanceof String) {
                    // handle cached parameters
                    i++;
                    Map map = (Map)m_elements.get(i);
                    Map oldMap = null;
                    if (map.size() > 0) {
                        oldMap = req.getParameterMap();
                        req.addParameterMap(map);
                    }
                    // do the include call
                    req.getRequestDispatcher((String)o).include(req, res);
                    // reset parameters if necessary
                    if (oldMap != null) {
                        req.setParameterMap(oldMap);
                    }
                } else {
                    try {
                        res.writeToOutputStream((byte[])o, hasNoSubElements);
                    } catch (IOException e) {
                        CmsMessageContainer message = Messages.get().container(
                            Messages.LOG_FLEXCACHEKEY_NOT_FOUND_1,
                            getClass().getName());
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(message.key());
                        }

                        throw new CmsFlexCacheException(message, e);
                    }
                }
            }
        }
    }

    /**
     * Sets the expiration date of this Flex cache entry exactly to the 
     * given time.<p>
     * 
     * @param dateExpires the time to expire this cache entry
     */
    public void setDateExpires(long dateExpires) {

        m_dateExpires = dateExpires;
        if (LOG.isDebugEnabled()) {
            long now = System.currentTimeMillis();
            LOG.debug(Messages.get().getBundle().key(
                Messages.LOG_FLEXCACHEENTRY_SET_EXPIRATION_DATE_3,
                new Long(m_dateExpires),
                new Long(now),
                new Long(m_dateExpires - now)));
        }
    }

    /**
     * Sets an expiration date for this cache entry to the next timeout,
     * which indicates the time this entry becomes invalid.<p>
     *
     * The timeout parameter represents the minute - interval in which the cache entry
     * is to be cleared. 
     * The interval always starts at 0.00h. 
     * A value of 60 would indicate that this entry will reach it's expiration date at the beginning of the next 
     * full hour, a timeout of 20 would indicate that the entry is invalidated at x.00, x.20 and x.40 of every hour etc.<p>
     *
     * @param timeout the timeout value to be set
     */
    public void setDateExpiresToNextTimeout(long timeout) {

        if ((timeout < 0) || !m_completed) {
            return;
        }

        long now = System.currentTimeMillis();
        long daytime = now % 86400000;
        long timeoutMinutes = timeout * 60000;
        setDateExpires(now - (daytime % timeoutMinutes) + timeoutMinutes);
    }

    /**
     * Sets the "last modified" date for this Flex cache entry with the given value.<p>
     * 
     * @param dateLastModified the value to set for the "last modified" date
     */
    public void setDateLastModified(long dateLastModified) {

        m_dateLastModified = dateLastModified;
    }

    /**
     * Sets the "last modified" date for this Flex cache entry by using the last passed timeout value.<p>
     * 
     * If a cache entry uses the timeout feature, it becomes invalid every time the timeout interval
     * passes. Thus the "last modified" date is the time the last timeout passed.<p>
     * 
     * @param timeout the timeout value to use to calculate the date last modified
     */
    public void setDateLastModifiedToPreviousTimeout(long timeout) {

        long now = System.currentTimeMillis();
        long daytime = now % 86400000;
        long timeoutMinutes = timeout * 60000;
        setDateLastModified(now - (daytime % timeoutMinutes));
    }

    /**
     * @see org.opencms.cache.I_CmsLruCacheObject#setNextLruObject(org.opencms.cache.I_CmsLruCacheObject)
     */
    public void setNextLruObject(I_CmsLruCacheObject theNextEntry) {

        m_next = theNextEntry;
    }

    /**
     * @see org.opencms.cache.I_CmsLruCacheObject#setPreviousLruObject(org.opencms.cache.I_CmsLruCacheObject)
     */
    public void setPreviousLruObject(I_CmsLruCacheObject thePreviousEntry) {

        m_previous = thePreviousEntry;
    }

    /** 
     * Set a redirect target for this cache entry.<p>
     *
     * <b>Important:</b>
     * When a redirect target is set, all saved data is thrown away,
     * and new data will not be saved in the cache entry.
     * This is so since with a redirect nothing will be displayed
     * in the browser anyway, so there is no point in saving the data.<p>
     * 
     * @param target The redirect target (must be a valid URL).
     */
    public void setRedirect(String target) {

        if (m_completed || (target == null)) {
            return;
        }
        m_redirectTarget = target;
        m_byteSize = 512 + CmsMemoryMonitor.getMemorySize(target);
        // If we have a redirect we don't need any other output or headers
        m_elements = null;
        m_headers = null;
    }

    /**
     * Stores a backward reference to the map and key where this cache entry is stored.<p>
     * 
     * This is required for the FlexCache.<p>
     *
     * @param theVariationKey the variation key
     * @param theVariationMap the variation map
     */
    public void setVariationData(String theVariationKey, Map theVariationMap) {

        m_variationKey = theVariationKey;
        m_variationMap = theVariationMap;
    }

    /** 
     * @see java.lang.Object#toString()
     *
     * @return a basic String representation of this CmsFlexCache entry
     */
    public String toString() {

        String str = null;
        if (m_redirectTarget == null) {
            str = "CmsFlexCacheEntry [" + m_elements.size() + " Elements/" + getLruCacheCosts() + " bytes]\n";
            Iterator i = m_elements.iterator();
            int count = 0;
            while (i.hasNext()) {
                count++;
                Object o = i.next();
                if (o instanceof String) {
                    str += "" + count + " - <cms:include target=" + o + ">\n";
                } else if (o instanceof byte[]) {
                    str += "" + count + " - <![CDATA[" + new String((byte[])o) + "]]>\n";
                } else {
                    str += "<!--[" + o.toString() + "]-->";
                }
            }
        } else {
            str = "CmsFlexCacheEntry [Redirect to target=" + m_redirectTarget + "]";
        }
        return str;
    }
}
