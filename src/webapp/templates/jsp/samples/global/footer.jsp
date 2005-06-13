<jsp:root version="1.2" xmlns:jsp="http://java.sun.com/JSP/Page" 
	xmlns:cms="urn:jsptld:cms-taglib"
    xmlns:cmsu="urn:jsptld:cms-util-taglib" 
    xmlns:c="urn:jsptld:http://java.sun.com/jsp/jstl/core"
    xmlns:fmt="urn:jsptld:http://java.sun.com/jsp/jstl/fmt">
    <br />
    <br />
    <br />
    <div class="line"><br />
    </div>
    <cms:adminOnly>
    	<fmt:message key="buttons.editfooter" var="label"/>
        <cms:editButton label="${label}" paragraph="samplesPageFooter" contentNodeName="footerPar" />
        <br />
    </cms:adminOnly>
    <cms:ifNotEmpty nodeDataName="footerText" contentNodeName="footerPar">
        <cms:out nodeDataName="footerText" contentNodeName="footerPar" />
        <br />
        <br />
    </cms:ifNotEmpty>Powered by magnolia &amp;ndash; <a href="http://www.magnolia.info" target="_blank">www.magnolia.info</a>
    <br /> J2EE Content Management Suite based on JSR-170<br />
    <br />
</jsp:root>
