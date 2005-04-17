<jsp:root version="1.2" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:cms="urn:jsptld:cms-taglib"
    xmlns:cmsu="urn:jsptld:cms-util-taglib" xmlns:c="urn:jsptld:http://java.sun.com/jsp/jstl/core">


    <!-- content title -->
    <h1><cms:ifNotEmpty nodeDataName="contentTitle" actpage="true">
        <cms:out nodeDataName="contentTitle" />
    </cms:ifNotEmpty> <cms:ifEmpty nodeDataName="contentTitle" actpage="true">
        <cms:out nodeDataName="title" />
    </cms:ifEmpty></h1>


    <cms:contentNodeIterator contentNodeCollectionName="mainColumnParagraphsDev">
        <div style="clear:both;"><cms:adminOnly>
            <cms:editBar />
        </cms:adminOnly> <cms:includeTemplate /> <br />
        <br />
        </div>
    </cms:contentNodeIterator>

    <cms:adminOnly>
        <div style="clear:both;"><cms:newBar contentNodeCollectionName="mainColumnParagraphsDev"
            paragraph="samplesDevShowRichEdit,samplesDevShowDate,samplesDevShowFile,samplesDevShowAllControls,samplesDevShowInclude" />
        </div>
    </cms:adminOnly>
</jsp:root>
