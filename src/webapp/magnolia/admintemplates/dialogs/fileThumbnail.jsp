<%
/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2004 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 * */
%><%@ page import="info.magnolia.cms.beans.runtime.File,
				 info.magnolia.cms.util.Resource,
				 java.io.InputStream,
				 java.awt.image.BufferedImage,
				 java.awt.*,
				 java.io.BufferedOutputStream,
				 com.sun.image.codec.jpeg.JPEGImageEncoder,
				 com.sun.image.codec.jpeg.JPEGCodec,
				 com.sun.image.codec.jpeg.JPEGEncodeParam,
				 javax.imageio.ImageIO,
				 java.io.IOException,
				 java.awt.image.PixelGrabber,
				 com.sun.image.codec.jpeg.JPEGImageDecoder,
				 info.magnolia.cms.core.HierarchyManager,
				 info.magnolia.cms.core.NodeData,
				   java.io.OutputStream,
				   info.magnolia.cms.security.SessionAccessControl"%>
<%

	response.setContentType("image/jpeg");

	String src = request.getParameter("src");
	String size = request.getParameter("size");
	//HierarchyManager hm=Resource.getHierarchyManager(request);
	HierarchyManager hm=SessionAccessControl.getHierarchyManager(request);

	NodeData data=hm.getNodeData(src);

	InputStream in=data.getValue().getStream();

	//File file = Resource.getFile(request);
	//InputStream in = file.getStream();

	//byte[] buffer = new byte[file.getSize()];
	//in.read(buffer,0,file.getSize());

	Image image = ImageIO.read(in);

	int thumbHeight;
	int thumbWidth;
	if (size!=null) {
		thumbHeight=image.getHeight(null);
		thumbWidth=image.getWidth(null);
	}
	else {
		/*
		thumbHeight=100;
		if (image.getHeight(null)!=0) thumbWidth=image.getWidth(null)*thumbHeight/image.getHeight(null);
		else thumbWidth=100;
		*/
		thumbWidth=150;
		int w=image.getWidth(null);
		int h=image.getHeight(null);
		if (w==0) w=1;
		if (h==0) h=1;

		if (w>thumbWidth) {
			thumbHeight=thumbWidth*h/w;
		}
		else {
			thumbWidth=w;
			thumbHeight=h;
		}

		if (thumbHeight>120) {
			thumbHeight=100;
			thumbWidth=thumbHeight*w/h;
		}


	}




	// draw original image to thumbnail image object and
	// scale it to the new size on-the-fly
	BufferedImage thumbImage = new BufferedImage(thumbWidth,
	thumbHeight, BufferedImage.TYPE_INT_RGB);
	Graphics2D graphics2D = thumbImage.createGraphics();
	//graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	graphics2D.drawImage(image, 0, 0, thumbWidth, thumbHeight, null);
	// save thumbnail image to OUTFILE

	//ImageIO.write(thumbImage,"gif",response.getOutputStream());
	//response.getOutputStream().write(buffer);
	//in.close();

	ServletOutputStream sout = response.getOutputStream();
	BufferedOutputStream output = new BufferedOutputStream(sout);
	JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(output);
	JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(thumbImage);
	//int quality = Integer.parseInt("80");
	//quality = Math.max(0, Math.min(quality, 100));
	//param.setQuality((float)quality / 100.0f, false);
	param.setQuality(0.8f, false);

	encoder.setJPEGEncodeParam(param);
	encoder.encode(thumbImage);

/*
File file = Resource.getFile(request);
InputStream in = file.getStream();
byte[] buffer = new byte[file.getSize()];
in.read(buffer,0,file.getSize());
OutputStream os=response.getOutputStream();
os.write(buffer);
os.flush();
in.close();
*/
%>