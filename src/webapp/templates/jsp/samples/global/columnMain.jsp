<jsp:root version="1.2" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:cms="urn:jsptld:cms-taglib"
    xmlns:cmsu="urn:jsptld:cms-util-taglib" xmlns:c="urn:jsptld:http://java.sun.com/jsp/jstl/core">


<!-- content title -->
<h1>
<cms:ifNotEmpty nodeDataName="contentTitle" actpage="true">
	<cms:out nodeDataName="contentTitle"/>
</cms:ifNotEmpty>
<cms:ifEmpty nodeDataName="contentTitle" actpage="true">
	<cms:out nodeDataName="title"/>
</cms:ifEmpty>
</h1>


<cms:contentNodeIterator contentNodeCollectionName="mainColumnParagraphs">
    <div style="clear:both;">
        <cms:adminOnly>
            <cms:editBar/>
        </cms:adminOnly>
        <!-- line -->
        <c:set var="lineAbove"><cms:out nodeDataName="lineAbove"/></c:set>
        <c:if test="${lineAbove=='true'}">
            <div class="line">
                <br/>
            </div>
        </c:if>
        <cms:includeTemplate/>
    </div>

	<!-- spacer -->
	<div style="clear:both;">
		<c:set var="spacer"><cms:out nodeDataName="spacer"/></c:set>
		<c:if test="${spacer=='1'}">
			<br/>
		</c:if>
		<c:if test="${spacer=='2'}">
			<br/><br/>
		</c:if>
	</div>
</cms:contentNodeIterator>
</jsp:root>
