<jsp:root version="1.2" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:cms="urn:jsptld:cms-taglib"
    xmlns:cmsu="urn:jsptld:cms-util-taglib" xmlns:c="urn:jsptld:http://java.sun.com/jsp/jstl/core">
    <jsp:directive.page import="info.magnolia.cms.core.Content" />
    <jsp:directive.page import="info.magnolia.cms.util.Resource" />
    <jsp:directive.page import="info.magnolia.cms.gui.inline.BarMain" />
    <jsp:directive.page import="info.magnolia.cms.gui.control.Button" />
    <jsp:directive.page import="info.magnolia.cms.beans.config.ContentRepository" />

    <!-- main bar of form template -->
    <jsp:scriptlet>

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
	fProps.setOnclick("mgnlOpenDialog('"+Resource.getActivePage(request).getHandle()+"','','','samplesFormProperties','"+ContentRepository.WEBSITE+"','"+ request.getContextPath() + "');");
	bar.setButtonsRight(fProps);


	// places the preview and the site admin button to the very left and the properties button to the very right
	bar.placeDefaultButtons();


	//draw the main bar
	bar.drawHtml(out);

</jsp:scriptlet>
</jsp:root>


