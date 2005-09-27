<jsp:root version="1.2" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:cms="urn:jsptld:cms-taglib"
    xmlns:cmsu="urn:jsptld:cms-util-taglib" xmlns:c="urn:jsptld:http://java.sun.com/jsp/jstl/core">
    <jsp:directive.page import="info.magnolia.cms.core.Content" />
    <jsp:directive.page import="info.magnolia.cms.util.Resource" />
    <jsp:directive.page import="info.magnolia.cms.gui.misc.Sources" />
    <jsp:directive.page import="info.magnolia.cms.gui.inline.BarMain" />
    <jsp:directive.page import="info.magnolia.cms.gui.misc.FileProperties" />
    <jsp:directive.page import="info.magnolia.cms.gui.control.Button" />


<!--
     header image
-->

<jsp:scriptlet> <![CDATA[
    Content activePage=Resource.getActivePage(request);
	String dataName="headerImage";
	String imagePath=null;
	String alt="";

	if (activePage.getNodeData(dataName).isExist()) {
		// if existing, show the image of the page itself
		FileProperties props=new FileProperties(activePage,dataName);
		imagePath=props.getProperty(FileProperties.PATH);
		alt=activePage.getNodeData(dataName+"Alt").getString();
	}
	else {
		// else find the nearest image
		for (int i=activePage.getAncestors().size()-1;i>=0;i--) {
			Content c=activePage.getAncestor(i);
			if (c.getNodeData(dataName).isExist()) {
				FileProperties props=new FileProperties(c,dataName);
				imagePath=props.getProperty(FileProperties.PATH);
				alt=c.getNodeData(dataName+"Alt").getString();
				break;
			}
		}
	}
	if (imagePath==null) {
		//no image found: use default
		imagePath="/docroot/samples/imgs/header.jpg";
		alt="magnolia - for content management";
	}
	pageContext.setAttribute("imagePath", imagePath);
	pageContext.setAttribute("alt", alt);
	]]> </jsp:scriptlet>
<div style="position:absolute;left:0px;top:0px;">
    <img src="${pageContext.request.contextPath}${imagePath}" alt="${alt}" />
    <br/>
</div>
</jsp:root>
