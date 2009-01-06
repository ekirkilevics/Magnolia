/**
 * This file Copyright (c) 2003-2009 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.module.admininterface.pages;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.admininterface.PageMVCHandler;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;


/**
 * @author Fabrizio Giustina
 * @version $Id$
 */
public class FileThumbnailDialogPage extends PageMVCHandler {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(FileThumbnailDialogPage.class);

    private String src;

    private String size;

    /**
     * @param name
     * @param request
     * @param response
     */
    public FileThumbnailDialogPage(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
    }

    /**
     * Setter for <code>size</code>.
     * @param size The size to set.
     */
    public void setSize(String size) {
        this.size = size;
    }

    /**
     * Setter for <code>src</code>.
     * @param src The src to set.
     */
    public void setSrc(String src) {
        this.src = src;
    }

    /**
     * @see info.magnolia.cms.servlets.MVCServletHandler#renderHtml(java.lang.String)
     */
    public void renderHtml(String view) throws IOException {

        if (src == null) {
            return;
        }

        response.setContentType("image/jpeg"); //$NON-NLS-1$

        HierarchyManager hm = MgnlContext.getHierarchyManager(ContentRepository.WEBSITE);

        InputStream in = null;

        NodeData data;
        try {
            data = hm.getNodeData(src);
            in = data.getValue().getStream();
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
            return;
        }

        Image image = ImageIO.read(in);

        int thumbHeight;
        int thumbWidth;
        if (size != null) {
            thumbHeight = image.getHeight(null);
            thumbWidth = image.getWidth(null);
        }
        else {
            thumbWidth = 150;
            int w = image.getWidth(null);
            int h = image.getHeight(null);
            if (w == 0) {
                w = 1;
            }
            if (h == 0) {
                h = 1;
            }

            if (w > thumbWidth) {
                thumbHeight = thumbWidth * h / w;
            }
            else {
                thumbWidth = w;
                thumbHeight = h;
            }

            if (thumbHeight > 120) {
                thumbHeight = 100;
                thumbWidth = thumbHeight * w / h;
            }

        }

        // draw original image to thumbnail image object and scale it to the new size on-the-fly
        BufferedImage thumbImage = new BufferedImage(thumbWidth, thumbHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = thumbImage.createGraphics();
        // graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.drawImage(image, 0, 0, thumbWidth, thumbHeight, null);

        ServletOutputStream sout = response.getOutputStream();
        BufferedOutputStream output = new BufferedOutputStream(sout);
        JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(output);
        JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(thumbImage);
        param.setQuality(0.8f, false);

        encoder.setJPEGEncodeParam(param);
        encoder.encode(thumbImage);
    }

}
