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
%><%@ page import="info.magnolia.cms.core.Content,
				   info.magnolia.cms.util.Resource,
				   info.magnolia.cms.gui.misc.Sources,
				   info.magnolia.cms.gui.inline.BarMain,
				   info.magnolia.cms.gui.misc.FileProperties,
				   info.magnolia.cms.gui.control.Button,
				   info.magnolia.cms.beans.config.ContentRepository"%>
<%@ taglib uri="cms-taglib" prefix="cms" %>
<%@ taglib uri="cms-util-taglib" prefix="cmsu" %>
<%@ taglib uri="JSTL" prefix="c" %>

<%-- ################################################## --%>
<%-- main bar of form template --%>
<%-- ################################################## --%>




<%

	Content currentPage=Resource.getActivePage(request);

	BarMain bar=new BarMain(request);

	//path is needed for the links of the buttons
	bar.setPath(currentPage.getHandle());

	//"paragraph" specifies the paragraph evoked by the "Properties" button
	bar.setParagraph("samplesPageProperties");

	// initialize the default buttons (preview, site admin, properties)
	// note: buttons are not placed through init (see below)
	bar.setDefaultButtons();

	// to overwrite single properties of the default buttons, use getButtonXXX() methods:
	bar.getButtonProperties().setLabel("Page properties");


	//add customized buttons to the main bar
	Button fProps=new Button();
	fProps.setLabel("Form properties");
	fProps.setOnclick("mgnlOpenDialog('"+Resource.getActivePage(request).getHandle()+"','','','samplesFormProperties','"+ContentRepository.WEBSITE+"');");
	bar.setButtonsRight(fProps);


	// places the preview and the site admin button to the very left and the properties button to the very right
	bar.placeDefaultButtons();


	//draw the main bar
	bar.drawHtml(out);

%>




