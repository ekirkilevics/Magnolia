<jsp:root version="1.2" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:cms="urn:jsptld:cms-taglib"
    xmlns:cmsu="urn:jsptld:cms-util-taglib" xmlns:c="urn:jsptld:http://java.sun.com/jsp/jstl/core">
    <cms:contentNodeIterator contentNodeCollectionName="rightColumnParagraphs">
        <div style="clear:both;">
            <cms:adminOnly>
                <cms:editBar />
            </cms:adminOnly>
            <cms:includeTemplate />
        </div>
    </cms:contentNodeIterator>

    <cms:adminOnly>
        <div style="clear:both;">
            <cms:newBar contentNodeCollectionName="rightColumnParagraphs" paragraph="samplesRightColumn" />
        </div>
    </cms:adminOnly>
</jsp:root>
