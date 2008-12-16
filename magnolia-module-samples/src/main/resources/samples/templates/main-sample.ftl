<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
[#assign cms=JspTaglibs["cms-taglib"]]
[#assign cmsu=JspTaglibs["cms-util-taglib"]]

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
   [#include "include/header.ftl"]
</head>

<body >
[@cms.mainBar dialog="sampleProperties" /]
    <h${def.titleSize}>${content.title}</h${def.titleSize}>
    <h3>${content.title}</h3>

    <p>${content.text!""}</p>
    <div id="main">

        [@cms.contentNodeIterator contentNodeCollectionName="main"]
            [@cms.includeTemplate /]
        [/@cms.contentNodeIterator]
        [@cms.newBar contentNodeCollectionName="main" paragraph="sampleFreemarker, sampleFreemarkerSearch, sampleFTLControlsShowRoom" /]

        [#include "include/footer.ftl"]
    </div><!-- end main -->
    [@cmsu.simpleNavigation /]
</body>
</html>