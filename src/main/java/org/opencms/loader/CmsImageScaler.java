/*
 * File   : $Source: /usr/local/cvs/opencms/src/org/opencms/loader/CmsImageScaler.java,v $
 * Date   : $Date: 2008-06-17 09:59:03 $
 * Version: $Revision: 1.9 $
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
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.loader;

import com.alkacon.simapi.RenderSettings;
import com.alkacon.simapi.Simapi;
import com.alkacon.simapi.filter.GrayscaleFilter;
import com.alkacon.simapi.filter.ShadowFilter;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;

/**
 * Creates scaled images, acting as it's own parameter container.<p>
 * 
 * @author  Alexander Kandzior 
 * 
 * @version $Revision: 1.9 $
 * 
 * @since 6.2.0
 */
public class CmsImageScaler {

    /** The name of the transparent color (for the backgound image). */
    public static final String COLOR_TRANSPARENT = "transparent";

    /** The name of the grayscale image filter. */
    public static final String FILTER_GRAYSCALE = "grayscale";

    /** The name of the shadow image filter. */
    public static final String FILTER_SHADOW = "shadow";

    /** The supported image filter names. */
    public static final List FILTERS = Arrays.asList(new String[] {FILTER_GRAYSCALE, FILTER_SHADOW});

    /** The (optional) parameter used for sending the scale information of an image in the http request. */
    public static final String PARAM_SCALE = "__scale";

    /** The default maximum image size (width * height) to apply image blurring when downscaling (setting this to high may case "out of memory" errors). */
    public static final int SCALE_DEFAULT_MAX_BLUR_SIZE = 2500 * 2500;

    /** The default maximum image size (width or height) to allow when updowscaling an image using request parameters. */
    public static final int SCALE_DEFAULT_MAX_SIZE = 1500;

    /** The scaler parameter to indicate the requested image background color (if required). */
    public static final String SCALE_PARAM_COLOR = "c";

    /** The scaler parameter to indicate the requested image filter. */
    public static final String SCALE_PARAM_FILTER = "f";

    /** The scaler parameter to indicate the requested image height. */
    public static final String SCALE_PARAM_HEIGHT = "h";

    /** The scaler parameter to indicate the requested image position (if required). */
    public static final String SCALE_PARAM_POS = "p";

    /** The scaler parameter to indicate to requested image save quality in percent (if applicable, for example used with JPEG images). */
    public static final String SCALE_PARAM_QUALITY = "q";

    /** The scaler parameter to indicate to requested <code>{@link java.awt.RenderingHints}</code> settings. */
    public static final String SCALE_PARAM_RENDERMODE = "r";

    /** The scaler parameter to indicate the requested scale type. */
    public static final String SCALE_PARAM_TYPE = "t";

    /** The scaler parameter to indicate the requested image width. */
    public static final String SCALE_PARAM_WIDTH = "w";

    /** The log object for this class. */
    protected static final Log LOG = CmsLog.getLog(CmsImageScaler.class);

    /** The target background color (optional). */
    private Color m_color;

    /** The list of image filter names (Strings) to apply. */
    private List m_filters;

    /** The target height (required). */
    private int m_height;

    /** The maximum image size (width * height) to apply image blurring when downscaling (setting this to high may case "out of memory" errors). */
    private int m_maxBlurSize;

    /** The target position (optional). */
    private int m_position;

    /** The target image save quality (if applicable, for example used with JPEG images) (optional). */
    private int m_quality;

    /** The image processing renderings hints constant mode indicator (optional). */
    private int m_renderMode;

    /** The final (parsed and corrected) scale parameters. */
    private String m_scaleParameters;

    /** The target scale type (optional). */
    private int m_type;

    /** The target width (required). */
    private int m_width;

    /**
     * Creates a new, empty image scaler object.<p>
     */
    public CmsImageScaler() {

        init();
    }

    /**
     * Creates a new image scaler for the given image contained in the byte array.<p>
     * 
     * <b>Please note:</b>The image itself is not stored in the scaler, only the width and
     * height dimensions of the image. To actually scale an image, you need to use
     * <code>{@link #scaleImage(CmsFile)}</code>. This constructor is commonly used only 
     * to extract the image dimensions, for example when creating a String value for
     * the <code>{@link CmsPropertyDefinition#PROPERTY_IMAGE_SIZE}</code> property.<p>
     * 
     * In case the byte array can not be decoded to an image, or in case of other errors,
     * <code>{@link #isValid()}</code> will return <code>false</code>.<p>
     * 
     * @param content the image to calculate the dimensions for
     * @param rootPath the root path of the resource (for error logging)
     */
    public CmsImageScaler(byte[] content, String rootPath) {

        init();
        try {
            // read the scaled image
            BufferedImage image = Simapi.read(content);
            m_height = image.getHeight();
            m_width = image.getWidth();
        } catch (Exception e) {
            // nothing we can do about this, keep the original properties            
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.ERR_UNABLE_TO_EXTRACT_IMAGE_SIZE_1, rootPath), e);
            }
            // set height / width to default of -1
            init();
        }
    }

    /**
     * Creates a new image scaler that is a rescale from the original size to the given scaler.<p> 
     * 
     * @param original the scaler that holds the original image dimensions
     * @param target the image scaler to be used for rescaling this image scaler
     * 
     * @deprecated use {@link #getReScaler(CmsImageScaler)} on the <code>original</code> scaler instead
     */
    public CmsImageScaler(CmsImageScaler original, CmsImageScaler target) {

        CmsImageScaler scaler = original.getReScaler(target);
        initValuesFrom(scaler);
    }

    /**
     * Creates a new image scaler by reading the property <code>{@link CmsPropertyDefinition#PROPERTY_IMAGE_SIZE}</code>
     * from the given resource.<p>
     * 
     * In case of any errors reading or parsing the property,
     * <code>{@link #isValid()}</code> will return <code>false</code>.<p>
     * 
     * @param cms the OpenCms user context to use when reading the property
     * @param res the resource to read the property from
     */
    public CmsImageScaler(CmsObject cms, CmsResource res) {

        init();
        String sizeValue = null;
        if ((cms != null) && (res != null)) {
            try {
                CmsProperty sizeProp = cms.readPropertyObject(res, CmsPropertyDefinition.PROPERTY_IMAGE_SIZE, false);
                if (!sizeProp.isNullProperty()) {
                    // parse property value using standard procedures
                    sizeValue = sizeProp.getValue();
                }
            } catch (Exception e) {
                // ignore
            }
        }
        if (CmsStringUtil.isNotEmpty(sizeValue)) {
            parseParameters(sizeValue);
        }
    }

    /**
     * Creates a new image scaler based on the given http request.<p>
     * 
     * @param request the http request to read the parameters from
     * @param maxScaleSize the maximum scale size (width or height) for the image
     * @param maxBlurSize the maximum size of the image (width * height) to apply blur (may cause "out of memory" for large images)
     */
    public CmsImageScaler(HttpServletRequest request, int maxScaleSize, int maxBlurSize) {

        init();
        m_maxBlurSize = maxBlurSize;
        String parameters = request.getParameter(CmsImageScaler.PARAM_SCALE);
        if (CmsStringUtil.isNotEmpty(parameters)) {
            parseParameters(parameters);
            if (isValid()) {
                // valid parameters, check if scale size is not too big
                if ((getWidth() > maxScaleSize) || (getHeight() > maxScaleSize)) {
                    // scale size is too big, reset scaler
                    init();
                }
            }
        }
    }

    /**
     * Creates a new image scaler based on the given parameter String.<p>
     * 
     * @param parameters the scale parameters to use
     */
    public CmsImageScaler(String parameters) {

        init();
        if (CmsStringUtil.isNotEmpty(parameters)) {
            parseParameters(parameters);
        }
    }

    /**
     * Creates a new image scaler based on the given base scaler and the given width and height.<p>
     * 
     * @param base the base scaler to initialize the values with
     * @param width the width to set for this scaler
     * @param height the height to set for this scaler
     */
    protected CmsImageScaler(CmsImageScaler base, int width, int height) {

        initValuesFrom(base);
        setWidth(width);
        setHeight(height);
    }

    /**
     * Adds a filter name to the list of filters that should be applied to the image.<p>
     * 
     * @param filter the filter name to add
     */
    public void addFilter(String filter) {

        if (CmsStringUtil.isNotEmpty(filter)) {
            filter = filter.trim().toLowerCase();
            if (FILTERS.contains(filter)) {
                m_filters.add(filter);
            }
        }
    }

    /**
     * @see java.lang.Object#clone()
     */
    public Object clone() {

        CmsImageScaler clone = new CmsImageScaler();
        clone.initValuesFrom(this);
        return clone;
    }

    /**
     * Returns the color.<p>
     *
     * @return the color
     */
    public Color getColor() {

        return m_color;
    }

    /**
     * Returns the color as a String.<p>
     *
     * @return the color as a String
     */
    public String getColorString() {

        StringBuffer result = new StringBuffer();
        if (m_color == Simapi.COLOR_TRANSPARENT) {
            result.append(COLOR_TRANSPARENT);
        } else {
            if (m_color.getRed() < 16) {
                result.append('0');
            }
            result.append(Integer.toString(m_color.getRed(), 16));
            if (m_color.getGreen() < 16) {
                result.append('0');
            }
            result.append(Integer.toString(m_color.getGreen(), 16));
            if (m_color.getBlue() < 16) {
                result.append('0');
            }
            result.append(Integer.toString(m_color.getBlue(), 16));
        }
        return result.toString();
    }

    /**
     * Returns a new image scaler that is a downscale from the size of <code>this</code> scaler 
     * to the given scaler size.<p>
     * 
     * If no downscale from this to the given scaler is required according to
     * {@link #isDownScaleRequired(CmsImageScaler)}, then <code>null</code> is returned.<p> 
     * 
     * @param downScaler the image scaler that holds the downscaled target image dimensions
     * 
     * @return a new image scaler that is a downscale from the size of <code>this</code> scaler 
     *      to the given target scaler size, or <code>null</code>
     */
    public CmsImageScaler getDownScaler(CmsImageScaler downScaler) {

        if (!isDownScaleRequired(downScaler)) {
            // no downscaling is required
            return null;
        }

        int downHeight = downScaler.getHeight();
        int downWidth = downScaler.getWidth();

        int height = getHeight();
        int width = getWidth();

        if (((height > width) && (downHeight < downWidth)) || ((width > height) && (downWidth < downHeight))) {
            // adjust orientation
            downHeight = downWidth;
            downWidth = downScaler.getHeight();
        }

        if (width > downWidth) {
            // width is too large, re-calculate height
            float scale = (float)downWidth / (float)width;
            downHeight = Math.round(height * scale);
        } else if (height > downHeight) {
            // height is too large, re-calculate width
            float scale = (float)downHeight / (float)height;
            downWidth = Math.round(width * scale);
        } else {
            // something is wrong, don't downscale
            return null;
        }

        // now create and initialize the result scaler
        return new CmsImageScaler(downScaler, downWidth, downHeight);
    }

    /** 
     * Returns the list of image filter names (Strings) to be applied to the image.<p> 
     * 
     * @return the list of image filter names (Strings) to be applied to the image
     */
    public List getFilters() {

        return m_filters;
    }

    /** 
     * Returns the list of image filter names (Strings) to be applied to the image as a String.<p> 
     * 
     * @return the list of image filter names (Strings) to be applied to the image as a String
     */
    public String getFiltersString() {

        StringBuffer result = new StringBuffer();
        Iterator i = m_filters.iterator();
        while (i.hasNext()) {
            String filter = (String)i.next();
            result.append(filter);
            if (i.hasNext()) {
                result.append(':');
            }
        }
        return result.toString();
    }

    /**
     * Returns the height.<p>
     *
     * @return the height
     */
    public int getHeight() {

        return m_height;
    }

    /**
     * Returns the image type from the given file name based on the file suffix (extension)
     * and the available image writers.<p>
     * 
     * For example, for the file name "opencms.gif" the type is GIF, for 
     * "opencms.jpg" is is "JPEG" etc.<p> 
     * 
     * In case the input filename has no suffix, or there is no known image writer for the format defined
     * by the suffix, <code>null</code> is returned.<p>
     * 
     * Any non-null result can be used if an image type input value is required.<p>
     * 
     * @param filename the file name to get the type for
     *  
     * @return the image type from the given file name based on the suffix and the available image writers, 
     *      or null if no image writer is available for the format 
     */
    public String getImageType(String filename) {

        return Simapi.getImageType(filename);
    }

    /**
     * Returns the maximum image size (width * height) to apply image blurring when downscaling images.<p>
     * 
     * Image blurring is required to achieve the best results for downscale operations when the target image size 
     * is 2 times or more smaller then the original image size. This parameter controls the maximum size (width * height) of an 
     * image that is blurred before it is downscaled. If the image is larger, no blurring is done. 
     * However, image blurring is an expensive operation in both CPU usage and memory consumption. 
     * Setting the blur size to large may case "out of memory" errors.<p>
     * 
     * @return the maximum image size (width * height) to apply image blurring when downscaling images
     */
    public int getMaxBlurSize() {

        return m_maxBlurSize;
    }

    /**
     * Returns the image pixel count, that is the image with multiplied by the image height.<p>
     * 
     * If this scalier is not valid (see {@link #isValid()}) the result is undefined.<p>
     * 
     * @return the image pixel count, that is the image with multiplied by the image height
     */
    public int getPixelCount() {

        return m_width * m_height;
    }

    /**
     * Returns the position.<p>
     *
     * @return the position
     */
    public int getPosition() {

        return m_position;
    }

    /**
     * Returns the image saving quality in percent (0 - 100).<p>
     * 
     * This is used only if applicable, for example when saving JPEG images.<p>
     *
     * @return the image saving quality in percent
     */
    public int getQuality() {

        return m_quality;
    }

    /**
     * Returns the image rendering mode constant.<p>
     *
     * Possible values are:<dl>
     * <dt>{@link Simapi#RENDER_QUALITY} (default)</dt>
     * <dd>Use best possible image processing - this may be slow sometimes.</dd>
     * 
     * <dt>{@link Simapi#RENDER_SPEED}</dt>
     * <dd>Fastest image processing but worse results - use this for thumbnails or where speed is more important then quality.</dd>
     * 
     * <dt>{@link Simapi#RENDER_MEDIUM}</dt>
     * <dd>Use default rendering hints from JVM - not recommended since it's almost as slow as the {@link Simapi#RENDER_QUALITY} mode.</dd></dl>
     *
     * @return the image rendering mode constant
     */
    public int getRenderMode() {

        return m_renderMode;
    }

    /**
     * Returns a new image scaler that is a rescaler from the <code>this</code> scaler 
     * size to the given target scaler size.<p>
     * 
     * The height of the target image is calculated in proportion 
     * to the original image width. If the width of the the original image is not known, 
     * the target image width is calculated in proportion to the original image height.<p>
     * 
     * @param target the image scaler that holds the target image dimensions
     * 
     * @return a new image scaler that is a rescale from the <code>this</code> scaler 
     *      size to the given target scaler size.<p>
     */
    public CmsImageScaler getReScaler(CmsImageScaler target) {

        int height = target.getHeight();
        int width = target.getWidth();

        if ((width > 0) && (getWidth() > 0)) {
            // width is known, calculate height
            float scale = (float)width / (float)getWidth();
            height = Math.round(getHeight() * scale);
        } else if ((height > 0) && (getHeight() > 0)) {
            // height is known, calculate width
            float scale = (float)height / (float)getHeight();
            width = Math.round(getWidth() * scale);
        } else if (isValid() && !target.isValid()) {
            // scaler is not valid but original is, so use original size of image
            width = getWidth();
            height = getHeight();
        }

        if ((target.getType() == 1) && (!target.isValid())) {
            // "no upscale" has been requested, only one target dimension was given
            if ((target.getWidth() > 0) && (getWidth() < width)) {
                // target width was given, target image should have this width 
                height = getHeight();
            } else if ((target.getHeight() > 0) && (getHeight() < height)) {
                // target height was given, target image should have this height
                width = getWidth();
            }
        }

        // now create and initialize the result scaler
        return new CmsImageScaler(target, width, height);
    }

    /**
     * Returns the type.<p>
     * 
     * Possible values are:<dl>
     * 
     * <dt>0 (default): Scale to exact target size with background padding</dt><dd><ul>
     * <li>enlarge image to fit in target size (if required)
     * <li>reduce image to fit in target size (if required)
     * <li>keep image aspect ratio / proportions intact
     * <li>fill up with bgcolor to reach exact target size
     * <li>fit full image inside target size (only applies if reduced)</ul></dd>
     *
     * <dt>1: Thumbnail generation mode (like 0 but no image enlargement)</dt><dd><ul>
     * <li>dont't enlarge image
     * <li>reduce image to fit in target size (if required)
     * <li>keep image aspect ratio / proportions intact
     * <li>fill up with bgcolor to reach exact target size
     * <li>fit full image inside target size (only applies if reduced)</ul></dd>
     *
     * <dt>2: Scale to exact target size, crop what does not fit</dt><dd><ul>
     * <li>enlarge image to fit in target size (if required)
     * <li>reduce image to fit in target size (if required)
     * <li>keep image aspect ratio / proportions intact
     * <li>fit full image inside target size (crop what does not fit)</ul></dd>
     *
     * <dt>3: Scale and keep image proportions, target size variable</dt><dd><ul>
     * <li>enlarge image to fit in target size (if required)
     * <li>reduce image to fit in target size (if required)
     * <li>keep image aspect ratio / proportions intact
     * <li>scaled image will not be padded or cropped, so target size is likely not the exact requested size</ul></dd>
     *
     * <dt>4: Don't keep image proportions, use exact target size</dt><dd><ul>
     * <li>enlarge image to fit in target size (if required)
     * <li>reduce image to fit in target size (if required)
     * <li>don't keep image aspect ratio / proportions intact
     * <li>the image will be scaled exactly to the given target size and likely will be loose proportions</ul></dd>
     * </dl>
     * 
     * @return the type
     */
    public int getType() {

        return m_type;
    }

    /**
     * Returns the width.<p>
     *
     * @return the width
     */
    public int getWidth() {

        return m_width;
    }

    /**
     * Returns a new image scaler that is a width based downscale from the size of <code>this</code> scaler 
     * to the given scaler size.<p>
     * 
     * If no downscale from this to the given scaler is required because the width of <code>this</code>
     * scaler is not larger than the target width, then the image dimensions of <code>this</code> scaler 
     * are unchanged in the result scaler. No upscaling is done!<p>
     * 
     * @param downScaler the image scaler that holds the downscaled target image dimensions
     * 
     * @return a new image scaler that is a downscale from the size of <code>this</code> scaler 
     *      to the given target scaler size
     */
    public CmsImageScaler getWidthScaler(CmsImageScaler downScaler) {

        int width = downScaler.getWidth();
        int height;

        if (getWidth() > width) {
            // width is too large, re-calculate height
            float scale = (float)width / (float)getWidth();
            height = Math.round(getHeight() * scale);
        } else {
            // width is ok
            width = getWidth();
            height = getHeight();
        }

        // now create and initialize the result scaler
        return new CmsImageScaler(downScaler, width, height);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {

        return toString().hashCode();
    }

    /**
     * Returns <code>true</code> if this image scaler must be downscaled when compared to the
     * given "downscale" image scaler.<p>
     *
     * If either <code>this</code> scaler or the given <code>downScaler</code> is invalid according to
     * {@link #isValid()}, then <code>false</code> is returned.<p>
     * 
     * The use case: <code>this</code> scaler represents an image (that is contains width and height of 
     * an image). The <code>downScaler</code> represents the maximum wanted image. The scalers
     * are compared and if the image represented by <code>this</code> scaler is too large,
     * <code>true</code> is returned. Image orientation is ignored, so for example an image with 600x800 pixel 
     * will NOT be downscaled if the target size is 800x600 but kept unchanged.<p>
     * 
     * @param downScaler the downscaler to compare this image scaler with
     * 
     * @return <code>true</code> if this image scaler must be downscaled when compared to the
     *      given "downscale" image scaler
     */
    public boolean isDownScaleRequired(CmsImageScaler downScaler) {

        if ((downScaler == null) || !isValid() || !downScaler.isValid()) {
            // one of the scalers is invalid
            return false;
        }

        if (getPixelCount() < (downScaler.getPixelCount() / 2)) {
            // the image has much less pixels then the target, so don't downscale
            return false;
        }

        int downWidth = downScaler.getWidth();
        int downHeight = downScaler.getHeight();
        if (downHeight > downWidth) {
            // normalize image orientation - the width should always be the large side
            downWidth = downHeight;
            downHeight = downScaler.getWidth();
        }
        int height = getHeight();
        int width = getWidth();
        if (height > width) {
            // normalize image orientation - the width should always be the large side
            width = height;
            height = getWidth();
        }

        return (width > downWidth) || (height > downHeight);
    }

    /**
     * Returns <code>true</code> if all required parameters are available.<p>
     * 
     * Required parameters are "h" (height), and "w" (width).<p>
     * 
     * @return <code>true</code> if all required parameters are available
     */
    public boolean isValid() {

        return (m_width > 0) && (m_height > 0);
    }

    /**
     * Parses the given parameters and sets the internal scaler variables accordingly.<p>
     * 
     * The parameter String must have the format <code>"h:100,w:200,t:1"</code>,
     * that is a comma separated list of attributes followed by a colon ":", followed by a value.
     * As possible attributes, use the constants from this class that start with <code>SCALE_PARAM</Code>
     * for example {@link #SCALE_PARAM_HEIGHT} or {@link #SCALE_PARAM_WIDTH}.<p>
     * 
     * @param parameters the parameters to parse
     */
    public void parseParameters(String parameters) {

        m_width = -1;
        m_height = -1;
        m_position = 0;
        m_type = 0;
        m_color = Color.WHITE;

        List tokens = CmsStringUtil.splitAsList(parameters, ',');
        Iterator it = tokens.iterator();
        String k;
        String v;
        while (it.hasNext()) {
            String t = (String)it.next();
            // extract key and value
            k = null;
            v = null;
            int idx = t.indexOf(':');
            if (idx >= 0) {
                k = t.substring(0, idx).trim();
                if (t.length() > idx) {
                    v = t.substring(idx + 1).trim();
                }
            }
            if (CmsStringUtil.isNotEmpty(k) && CmsStringUtil.isNotEmpty(v)) {
                // key and value are available
                if (SCALE_PARAM_HEIGHT.equals(k)) {
                    // image height
                    m_height = CmsStringUtil.getIntValue(v, Integer.MIN_VALUE, k);
                } else if (SCALE_PARAM_WIDTH.equals(k)) {
                    // image width
                    m_width = CmsStringUtil.getIntValue(v, Integer.MIN_VALUE, k);
                } else if (SCALE_PARAM_TYPE.equals(k)) {
                    // scaling type
                    setType(CmsStringUtil.getIntValue(v, -1, CmsImageScaler.SCALE_PARAM_TYPE));
                } else if (SCALE_PARAM_COLOR.equals(k)) {
                    // image background color
                    setColor(v);
                } else if (SCALE_PARAM_POS.equals(k)) {
                    // image position (depends on scale type)
                    setPosition(CmsStringUtil.getIntValue(v, -1, CmsImageScaler.SCALE_PARAM_POS));
                } else if (SCALE_PARAM_QUALITY.equals(k)) {
                    // image position (depends on scale type)
                    setQuality(CmsStringUtil.getIntValue(v, 0, k));
                } else if (SCALE_PARAM_RENDERMODE.equals(k)) {
                    // image position (depends on scale type)
                    setRenderMode(CmsStringUtil.getIntValue(v, 0, k));
                } else if (SCALE_PARAM_FILTER.equals(k)) {
                    // image filters to apply
                    setFilters(v);
                } else {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(Messages.get().getBundle().key(Messages.ERR_INVALID_IMAGE_SCALE_PARAMS_2, k, v));
                    }
                }
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().getBundle().key(Messages.ERR_INVALID_IMAGE_SCALE_PARAMS_2, k, v));
                }
            }
        }
    }

    /**
     * Returns a scaled version of the given image byte content according this image scalers parameters.<p>
     *  
     * @param content the image byte content to scale
     * @param rootPath the root path of the image file in the VFS
     * 
     * @return a scaled version of the given image byte content according to the provided scaler parameters
     */
    public byte[] scaleImage(byte[] content, String rootPath) {

        byte[] result = content;

        RenderSettings renderSettings;
        if ((m_renderMode == 0) && (m_quality == 0)) {
            // use default render mode and quality
            renderSettings = new RenderSettings(Simapi.RENDER_QUALITY);
        } else {
            // use special render mode and/or quality
            renderSettings = new RenderSettings(m_renderMode);
            if (m_quality != 0) {
                renderSettings.setCompressionQuality(m_quality / 100f);
            }
        }
        // set max blur siuze
        renderSettings.setMaximumBlurSize(m_maxBlurSize);
        // new create the scaler
        Simapi scaler = new Simapi(renderSettings);
        // calculate a valid image type supported by the imaging libary (e.g. "JPEG", "GIF")
        String imageType = Simapi.getImageType(rootPath);
        if (imageType == null) {
            // no type given, maybe the name got mixed up
            String mimeType = OpenCms.getResourceManager().getMimeType(rootPath, null, null);
            // check if this is another known mime type, if so DONT use it (images should not be named *.pdf)
            if (mimeType == null) {
                // no mime type found, use JPEG format to write images to the cache         
                imageType = Simapi.TYPE_JPEG;
            }
        }
        if (imageType == null) {
            // unknown type, unable to scale the image
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.ERR_UNABLE_TO_SCALE_IMAGE_2, rootPath, toString()));
            }
            return result;
        }
        try {
            BufferedImage image = Simapi.read(content);

            Color color = getColor();

            if (!m_filters.isEmpty()) {
                Iterator i = m_filters.iterator();
                while (i.hasNext()) {
                    String filter = (String)i.next();
                    if (FILTER_GRAYSCALE.equals(filter)) {
                        // add a grayscale filter
                        GrayscaleFilter grayscaleFilter = new GrayscaleFilter();
                        renderSettings.addImageFilter(grayscaleFilter);
                    } else if (FILTER_SHADOW.equals(filter)) {
                        // add a drop shadow filter
                        ShadowFilter shadowFilter = new ShadowFilter();
                        shadowFilter.setXOffset(5);
                        shadowFilter.setYOffset(5);
                        shadowFilter.setOpacity(192);
                        shadowFilter.setBackgroundColor(color.getRGB());
                        color = Simapi.COLOR_TRANSPARENT;
                        renderSettings.setTransparentReplaceColor(Simapi.COLOR_TRANSPARENT);
                        renderSettings.addImageFilter(shadowFilter);
                    }
                }
            }

            switch (getType()) {
                // select the "right" method of scaling according to the "t" parameter
                case 1:
                    // thumbnail generation mode (like 0 but no image enlargement)
                    image = scaler.resize(image, getWidth(), getHeight(), color, getPosition(), false);
                    break;
                case 2:
                    // scale to exact target size, crop what does not fit
                    image = scaler.resize(image, getWidth(), getHeight(), getPosition());
                    break;
                case 3:
                    // scale and keep image proportions, target size variable
                    image = scaler.resize(image, getWidth(), getHeight(), true);
                    break;
                case 4:
                    // don't keep image proportions, use exact target size
                    image = scaler.resize(image, getWidth(), getHeight(), false);
                    break;
                default:
                    // scale to exact target size with background padding
                    image = scaler.resize(image, getWidth(), getHeight(), color, getPosition(), true);
            }

            if (!m_filters.isEmpty()) {
                Rectangle targetSize = scaler.applyFilterDimensions(getWidth(), getHeight());
                image = scaler.resize(
                    image,
                    (int)targetSize.getWidth(),
                    (int)targetSize.getHeight(),
                    Simapi.COLOR_TRANSPARENT,
                    Simapi.POS_CENTER);
                image = scaler.applyFilters(image);
            }

            // get the byte result for the scaled image
            result = scaler.getBytes(image, imageType);
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.ERR_UNABLE_TO_SCALE_IMAGE_2, rootPath, toString()), e);
            }
        }
        return result;
    }

    /**
     * Returns a scaled version of the given image file according this image scalers parameters.<p>
     *  
     * @param file the image file to scale
     * 
     * @return a scaled version of the given image file according to the provided scaler parameters
     */
    public byte[] scaleImage(CmsFile file) {

        return scaleImage(file.getContents(), file.getRootPath());
    }

    /**
     * Sets the color.<p>
     *
     * @param color the color to set
     */
    public void setColor(Color color) {

        m_color = color;
    }

    /**
     * Sets the color as a String.<p>
     *
     * @param value the color to set
     */
    public void setColor(String value) {

        if (COLOR_TRANSPARENT.indexOf(value) == 0) {
            setColor(Simapi.COLOR_TRANSPARENT);
        } else {
            setColor(CmsStringUtil.getColorValue(value, Color.WHITE, SCALE_PARAM_COLOR));
        }
    }

    /**
     * Sets the list of filters as a String.<p>
     * 
     * @param value the list of filters to set
     */
    public void setFilters(String value) {

        m_filters = new ArrayList();
        List filters = CmsStringUtil.splitAsList(value, ':');
        Iterator i = filters.iterator();
        while (i.hasNext()) {
            String filter = (String)i.next();
            filter = filter.trim().toLowerCase();
            Iterator j = FILTERS.iterator();
            while (j.hasNext()) {
                String candidate = (String)j.next();
                if (candidate.startsWith(filter)) {
                    // found a matching filter
                    addFilter(candidate);
                    break;
                }
            }
        }
    }

    /**
     * Sets the height.<p>
     *
     * @param height the height to set
     */
    public void setHeight(int height) {

        m_height = height;
    }

    /**
     * Sets the maximum image size (width * height) to apply image blurring when downscaling images.<p> 
     * 
     * @param maxBlurSize the maximum image blur size to set
     * 
     * @see #getMaxBlurSize() for a more detailed description about this parameter
     */
    public void setMaxBlurSize(int maxBlurSize) {

        m_maxBlurSize = maxBlurSize;
    }

    /**
     * Sets the scale position.<p>
     *
     * @param position the position to set
     */
    public void setPosition(int position) {

        switch (position) {
            case Simapi.POS_DOWN_LEFT:
            case Simapi.POS_DOWN_RIGHT:
            case Simapi.POS_STRAIGHT_DOWN:
            case Simapi.POS_STRAIGHT_LEFT:
            case Simapi.POS_STRAIGHT_RIGHT:
            case Simapi.POS_STRAIGHT_UP:
            case Simapi.POS_UP_LEFT:
            case Simapi.POS_UP_RIGHT:
                // pos is fine
                m_position = position;
                break;
            default:
                m_position = Simapi.POS_CENTER;
        }
    }

    /**
     * Sets the image saving quality in percent.<p>
     *
     * @param quality the image saving quality (in percent) to set
     */
    public void setQuality(int quality) {

        if (quality < 0) {
            m_quality = 0;
        } else if (quality > 100) {
            m_quality = 100;
        } else {
            m_quality = quality;
        }
    }

    /**
     * Sets the image rendering mode constant.<p>
     *
     * @param renderMode the image rendering mode to set
     * 
     * @see #getRenderMode() for a list of allowed values for the rendering mode
     */
    public void setRenderMode(int renderMode) {

        if ((renderMode < Simapi.RENDER_QUALITY) || (renderMode > Simapi.RENDER_SPEED)) {
            renderMode = Simapi.RENDER_QUALITY;
        }
        m_renderMode = renderMode;
    }

    /**
     * Sets the scale type.<p>
     *
     * @param type the scale type to set
     * 
     * @see #getType() for a detailed description of the possible values for the type
     */
    public void setType(int type) {

        if ((type < 0) || (type > 4)) {
            // invalid type, use 0
            m_type = 0;
        } else {
            m_type = type;
        }
    }

    /**
     * Sets the width.<p>
     *
     * @param width the width to set
     */
    public void setWidth(int width) {

        m_width = width;
    }

    /**
     * Creates a request parameter configured with the values from this image scaler, also
     * appends a <code>'?'</code> char as a prefix so that this may be directly appended to an image URL.<p>
     * 
     * This can be appended to an image request in order to apply image scaling parameters.<p>
     * 
     * @return a request parameter configured with the values from this image scaler
     */
    public String toRequestParam() {

        StringBuffer result = new StringBuffer(128);
        result.append('?');
        result.append(PARAM_SCALE);
        result.append('=');
        result.append(toString());

        return result.toString();
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {

        if (m_scaleParameters != null) {
            return m_scaleParameters;
        }

        StringBuffer result = new StringBuffer(64);
        result.append(CmsImageScaler.SCALE_PARAM_WIDTH);
        result.append(':');
        result.append(m_width);
        result.append(',');
        result.append(CmsImageScaler.SCALE_PARAM_HEIGHT);
        result.append(':');
        result.append(m_height);
        if (m_type > 0) {
            result.append(',');
            result.append(CmsImageScaler.SCALE_PARAM_TYPE);
            result.append(':');
            result.append(m_type);
        }
        if (m_position > 0) {
            result.append(',');
            result.append(CmsImageScaler.SCALE_PARAM_POS);
            result.append(':');
            result.append(m_position);
        }
        if (m_color != Color.WHITE) {
            result.append(',');
            result.append(CmsImageScaler.SCALE_PARAM_COLOR);
            result.append(':');
            result.append(getColorString());
        }
        if (m_quality > 0) {
            result.append(',');
            result.append(CmsImageScaler.SCALE_PARAM_QUALITY);
            result.append(':');
            result.append(m_quality);
        }
        if (m_renderMode > 0) {
            result.append(',');
            result.append(CmsImageScaler.SCALE_PARAM_RENDERMODE);
            result.append(':');
            result.append(m_renderMode);
        }
        if (!m_filters.isEmpty()) {
            result.append(',');
            result.append(CmsImageScaler.SCALE_PARAM_FILTER);
            result.append(':');
            result.append(getFiltersString());
        }
        m_scaleParameters = result.toString();
        return m_scaleParameters;
    }

    /**
     * Initializes the members with the default values.<p>
     */
    private void init() {

        m_height = -1;
        m_width = -1;
        m_type = 0;
        m_position = 0;
        m_renderMode = 0;
        m_quality = 0;
        m_color = Color.WHITE;
        m_filters = new ArrayList();
        m_maxBlurSize = CmsImageLoader.getMaxBlurSize();
    }

    /**
     * Copies all values from the given scaler into this scaler.<p>
     * 
     * @param source the source scaler
     */
    private void initValuesFrom(CmsImageScaler source) {

        m_width = source.m_width;
        m_height = source.m_height;
        m_type = source.m_type;
        m_position = source.m_position;
        m_renderMode = source.m_renderMode;
        m_quality = source.m_quality;
        m_color = source.m_color;
        m_filters = new ArrayList(source.m_filters);
        m_maxBlurSize = source.m_maxBlurSize;
    }
}