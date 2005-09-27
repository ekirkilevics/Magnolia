package info.magnolia.module.admininterface.dialogpages;

import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.security.SessionAccessControl;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;


/**
 * @author Fabrizio Giustina
 * @version $Id$
 */
public class FileThumbnailDialogPage extends HttpServlet {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(FileThumbnailDialogPage.class);

    /**
     * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest, HttpServletResponse)
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("image/jpeg"); //$NON-NLS-1$

        String src = request.getParameter("src"); //$NON-NLS-1$
        String size = request.getParameter("size"); //$NON-NLS-1$

        HierarchyManager hm = SessionAccessControl.getHierarchyManager(request);

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
