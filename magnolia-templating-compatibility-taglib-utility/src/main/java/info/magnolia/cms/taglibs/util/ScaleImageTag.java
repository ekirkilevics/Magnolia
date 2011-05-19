/**
 * This file Copyright (c) 2003-2011 Magnolia International
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
package info.magnolia.cms.taglibs.util;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.cms.taglibs.Resource;
import info.magnolia.context.MgnlContext;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

import javax.imageio.ImageIO;
import javax.jcr.AccessDeniedException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


/**
 * Creates a scaled copy of an image. The maximum width and height of the images can be specified via the
 * attributes. <br />
 * <br />
 * If the scaled image with the specified name does not exist in the repository, then this tag will create it and save
 * it. If the scaled image already exists, then it will not be recreated. <br />
 * <br />
 * The name of the node that contains the original image is set by the attribute 'parentContentNode', and the name of
 * the nodeData for the image is set by the attribute 'parentNodeDataName'. If 'parentContentNode' is null, the local
 * content node is used. <br />
 * <br />
 * The name of the content node that contains the new scaled image is set by the attribute 'imageContentNodeName'. This
 * node is created under the original image node. This ensures that, if the original images is deleted, so are all the
 * scaled versions. <br />
 * <br />
 * This tag writes out the handle of the content node that contains the image. <br />
 * <br />
 *
 * @jsp.tag name="scaleImage" body-content="empty"
 *
 * @author Patrick Janssen
 * @author Fabrizio Giustina
 * @version 1.0
 */
public class ScaleImageTag extends BaseImageTag {
    private static final Logger log = LoggerFactory.getLogger(ScaleImageTag.class);

    /**
     * Location for folder for temporary image creation.
     */
    private static final String TEMP_IMAGE_NAME = "tmp-img";

    /**
     * The value of the extension nodeData in the properties node.
     */
    private static final String PROPERTIES_EXTENSION_VALUE = "PNG";

    /**
     * Attribute: Image maximum height.
     */
    private int maxHeight = 0;

    /**
     * Attribute: Image maximum width.
     */
    private int maxWidth = 0;

    /**
     * Attribute: Allow resizing images beyond their original dimensions.
     * Enabled by default for backwards compatibility but keep in mind this can
     * result in images of very poor quality.
     */
    private boolean allowOversize = true;

    /**
     * Attribute: The name of the new content node to create.
     */
    private String imageContentNodeName;

    /**
     * Attribute: The name of the data node that contains the existing image.
     */
    private String parentNodeDataName;

    /**
     * The maximum height of the image in pixels.
     * @jsp.attribute required="false" rtexprvalue="true" type="int"
     */
    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }

    /**
     * The maximum width of the image in pixels.
     * @jsp.attribute required="false" rtexprvalue="true" type="int"
     */
    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
    }

    /**
     * Allow resizing images beyond their original dimensions?
     * @jsp.attribute required="false" rtexprvalue="true" type="boolean"
     */
    public void setAllowOversize(boolean allowOversize) {
        this.allowOversize = allowOversize;
    }

    /**
     * The name of the content node that contains the image to be copied and scaled.
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    @Override
    public void setParentContentNodeName(String parentContentNodeName) {
        this.parentContentNodeName = parentContentNodeName;
    }

    /**
     * The name of the data node that contains the image data to be copied and scaled.
     * @jsp.attribute required="true" rtexprvalue="true"
     */
    public void setParentNodeDataName(String parentNodeDataName) {
        this.parentNodeDataName = parentNodeDataName;
    }

    /**
     * The name of the new contentNode that will contain the scaled version of the image.
     * @jsp.attribute required="true" rtexprvalue="true"
     */
    @Override
    public void setImageContentNodeName(String imageContentNodeName) {
        this.imageContentNodeName = imageContentNodeName;
    }

    @Override
    public void doTag() throws JspException {
        // initialize everything
        Content parentContentNode;
        Content imageContentNode;
        JspWriter out = this.getJspContext().getOut();

        try {

            // set the parent node that contains the original image
            if ((this.parentContentNodeName == null) || (this.parentContentNodeName.equals(""))) {
                parentContentNode = Resource.getLocalContentNode();
            }
            else {
                HierarchyManager hm = MgnlContext.getHierarchyManager(ContentRepository.WEBSITE);
                // if this name starts with a '/', then assume it is a node handle
                // otherwise assume that its is a path relative to the local content node
                if (this.parentContentNodeName.startsWith("/")) {
                    parentContentNode = hm.getContent(this.parentContentNodeName);
                }
                else {
                    String handle = Resource.getLocalContentNode().getHandle();
                    parentContentNode = hm.getContent(handle + "/" + this.parentContentNodeName);
                }
            }

            // check if the new image node exists, if not then create it
            if (parentContentNode.hasContent(this.imageContentNodeName)) {
                imageContentNode = parentContentNode.getContent(this.imageContentNodeName);
            }
            else {
                // create the node
                imageContentNode = parentContentNode.createContent(this.imageContentNodeName, ItemType.CONTENTNODE);
                parentContentNode.save();
            }
            // if the node does not have the image data or should be rescaled (i.e., something has c
            // then create the image data
            if (!imageContentNode.hasNodeData(this.parentNodeDataName) || rescale(parentContentNode, imageContentNode)) {
                this.createImageNodeData(parentContentNode, imageContentNode);
            }
            // write out the handle for the new image and exit
            StringBuffer handle = new StringBuffer(imageContentNode.getHandle());
            handle.append("/");
            handle.append(getFilename());
            out.write(handle.toString());
        }
        catch (PathNotFoundException e) {
            log.error("PathNotFoundException occured in ScaleImage tag: " + e.getMessage(), e);
        }
        catch (AccessDeniedException e) {
            log.error("AccessDeniedException occured in ScaleImage tag: " + e.getMessage(), e);
        }
        catch (RepositoryException e) {
            log.error("RepositoryException occured in ScaleImage tag: " + e.getMessage(), e);
        }
        catch (FileNotFoundException e) {
            log.error("FileNotFoundException occured in ScaleImage tag: " + e.getMessage(), e);
        }
        catch (IOException e) {
            log.error("IOException occured in ScaleImage tag: " + e.getMessage(), e);
        }
        this.cleanUp();
    }

    /**
     * Checks to see if the previously scaled image needs to be rescaled. This is true when the parent content node has
     * been updated or the height or width parameters have changed.
     * @param parentContentNode The node containing the scaled image node
     * @param imageContentNode The scaled image node
     * @return
     */
    protected boolean rescale(Content parentContentNode, Content imageContentNode) {
        Calendar parentModified = parentContentNode.getMetaData().getModificationDate() != null ? parentContentNode
            .getMetaData()
            .getModificationDate() : parentContentNode.getMetaData().getCreationDate();

        Calendar imageModified = imageContentNode.getMetaData().getModificationDate() != null ? imageContentNode
            .getMetaData()
            .getModificationDate() : imageContentNode.getMetaData().getCreationDate();

        if (parentModified.after(imageModified)) {
            return true;
        }
        else {
            int originalHeight = (int) imageContentNode.getNodeData("maxHeight").getLong();
            int originalWidth = (int) imageContentNode.getNodeData("maxWidth").getLong();

            return originalHeight != maxHeight || originalWidth != maxWidth;
        }
    }

    /**
     * Set objects to null.
     */
    public void cleanUp() {
        this.parentNodeDataName = null;
        this.imageContentNodeName = null;
        this.maxWidth = 0;
        this.maxHeight = 0;
    }

    /**
     * Create an image file that is a scaled version of the original image.
     * @param image node
     */
    private void createImageNodeData(Content parentContentNode, Content imageContentNode) throws PathNotFoundException,
        RepositoryException, IOException {

        // get the original image, as a buffered image
        InputStream oriImgStr = parentContentNode.getNodeData(this.parentNodeDataName).getStream();
        BufferedImage oriImgBuff = ImageIO.read(oriImgStr);
        oriImgStr.close();
        // create the new image file
        File newImgFile = this.scaleImage(oriImgBuff);

        NodeDataUtil.getOrCreate(imageContentNode, "maxHeight").setValue(maxHeight);
        NodeDataUtil.getOrCreate(imageContentNode, "maxWidth").setValue(maxWidth);

        createImageNode(newImgFile, imageContentNode);

        newImgFile.delete();
    }

    /**
     * Create an image file that is a scaled version of the original image.
     * @param the original image file
     * @return the new image file
     */
    private File scaleImage(BufferedImage oriImgBuff) throws IOException {
        // get the dimesnions of the original image
        int oriWidth = oriImgBuff.getWidth();
        int oriHeight = oriImgBuff.getHeight();
        // get scale factor for the new image
        double scaleFactor = this.scaleFactor(oriWidth, oriHeight);
        // get the width and height of the new image
        int newWidth = new Double(oriWidth * scaleFactor).intValue();
        int newHeight = new Double(oriHeight * scaleFactor).intValue();
        // create the thumbnail as a buffered image
        Image newImg = oriImgBuff.getScaledInstance(newWidth, newHeight, Image.SCALE_AREA_AVERAGING);
        BufferedImage newImgBuff = new BufferedImage(
            newImg.getWidth(null),
            newImg.getHeight(null),
            BufferedImage.TYPE_INT_RGB);
        Graphics2D g = newImgBuff.createGraphics();
        g.drawImage(newImg, 0, 0, null);
        g.dispose();
        // create the new image file in the temporary dir
        File newImgFile = File.createTempFile(TEMP_IMAGE_NAME, PROPERTIES_EXTENSION_VALUE);

        ImageIO.write(newImgBuff, PROPERTIES_EXTENSION_VALUE, newImgFile);
        // return the file
        return newImgFile;
    }

    /**
     * Calculate the scale factor for the image.
     * @param originalWidth the image width
     * @param originalHeight the image height
     * @return the scale factor
     */
    private double scaleFactor(int originalWidth, int originalHeight) {
        double scaleFactor;

        int scaleWidth = this.maxWidth;
        int scaleHeight = this.maxHeight;

        if (!this.allowOversize) {
            scaleWidth = Math.min(this.maxWidth, originalWidth);
            scaleHeight = Math.min(this.maxHeight, originalHeight);
        }

        if (scaleWidth <= 0 && scaleHeight <= 0) {
            // may a copy at the same size
            scaleFactor = 1;
        }
        else if (scaleWidth <= 0) {
            // use height
            scaleFactor = (double) scaleHeight / (double) originalHeight;
        }
        else if (scaleHeight <= 0) {
            // use width
            scaleFactor = (double) scaleWidth / (double) originalWidth;
        }
        else {
            // create two scale factors, and see which is smaller
            double scaleFactorWidth = (double) scaleWidth / (double) originalWidth;
            double scaleFactorHeight = (double) scaleHeight / (double) originalHeight;
            scaleFactor = Math.min(scaleFactorWidth, scaleFactorHeight);
        }
        return scaleFactor;
    }

    @Override
    protected String getFilename() {
        return this.parentNodeDataName;
    }
}
