<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
[#assign cms=JspTaglibs["cms-taglib"]]
[#assign cmsu=JspTaglibs["cms-util-taglib"]]
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>${content.title}</title>
    <link rel="stylesheet" type="text/css" href="${contextPath}/docroot/samples/samples.css"/>
    [@cms.links /]
</head>

<body>
[@cms.mainBar dialog="sampleProperties" /]

<div id="header">
    <h1>Magnolia Samples</h1>
</div>

<div id="navigation">[@cmsu.simpleNavigation /]</div>

<div id="main">
    <h${def.titleSize}>${content.title}</h${def.titleSize}>
    <p>${content.text!""}</p>

    [@cms.contentNodeIterator contentNodeCollectionName="main"]
        [@cms.includeTemplate /]
    [/@cms.contentNodeIterator]

    [@cms.newBar contentNodeCollectionName="main" paragraph="sampleFreemarker, sampleFreemarkerSearch, sampleFTLControlsShowRoom" /]
</div>

<div id="footer">
    <p>This page was last edited by <span class="author">${content.metaData.authorId}</span>
        on <span class="modificationdate">${content.metaData.modificationDate}</span>
    </p>
</div>
</body>
</html>