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
				   info.magnolia.cms.gui.control.Button"%>
<%@ taglib uri="cms-taglib" prefix="cms" %>
<%@ taglib uri="cms-util-taglib" prefix="cmsu" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>


<%-- ################################################## --%>
<%-- main bar --%>
<%-- ################################################## --%>


<%-- sample 1: using the mainBar tag--%>
<%-- use the main bar tag to use the main bar as it is --%>
<%-- "paragraph" specifies the paragraph evoked by the "Properties" button--%>
<cms:mainBar paragraph="samplesPageProperties"/>


<%
	/*
	//	sample 2: linking only magnolia js and css
	// beside the visible bar with the buttons, mainBar draws also the links to javascript and css files used by magnolia
	// if you don't need a visible mainBar, use the following to link to js and css sources:

	new BarMain(request).drawHtmlLinks(out);
	*/

%>


<%
	/*
	// sample 3: customise the main bar	

	Content currentPage=Resource.getActivePage(request);

	BarMain bar=new BarMain(request);

	//set bar to an absolute position
	bar.setTop(20);
	bar.setLeft(100);

	//set width of bar to any css compatible value
	bar.setWidth("800px");

	// overlay mode; true (default): main bar overlays content - false: main bar moves content downwards
	// note: top, left and width do not apply if overlay is set to false
	// note: since all content divs of samples are positioned absolut, overlay mode "false" does only affect the header image
	//bar.setOverlay(false);

	//path is needed for the links of the buttons
	bar.setPath(currentPage.getHandle());

	//"paragraph" specifies the paragraph evoked by the "Properties" button
	bar.setParagraph("samplesPageProperties");

	// initialize the default buttons (preview, site admin, properties)
	// note: buttons are not placed through init (see below)
	bar.setDefaultButtons();

	// to overwrite single properties of the default buttons, use getButtonXXX() methods:
	bar.getButtonProperties().setLabel("*P*R*O*P*S*");
	bar.getButtonPreview().setLabel("&laquo; #P#R#E#V#I#E#W#");
	bar.getButtonSiteAdmin().setLabel("s i t e a d m i n");


	//add customized buttons to the main bar
	Button cb1=new Button();
	cb1.setLabel("Custom I");
	cb1.setOnclick("alert('Whatever you want.');");
	bar.setButtonsLeft(cb1);

	Button cb2=new Button();
	cb2.setLabel("Custom II");
	cb2.setOnclick("alert('Whatever you want.');");
	bar.setButtonsRight(cb2);

	// places the preview and the site admin button to the very left and the properties button to the very right
	bar.placeDefaultButtons();


	// right buttons added after calling placeDefaultButtons() will be placed on the right of the properties button
	Button cb3=new Button();
	cb3.setLabel("Custom III");
	cb3.setOnclick("alert('Whatever you want.');");
	bar.setButtonsRight(cb3);

	//draw the main bar
	bar.drawHtml(out);
	*/

%>




