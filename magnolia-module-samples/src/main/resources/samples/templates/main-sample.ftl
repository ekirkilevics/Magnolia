<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
[#assign cms=JspTaglibs["cms-taglib"]]
[#assign cmsu=JspTaglibs["cms-util-taglib"]]

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
   [#include "include/header.ftl"]
</head>

<body >
[@cms.mainBar dialog="sampleProperties" /]
    <h${templateDef.titleSize}>${content.title}</h${templateDef.titleSize}>
    <h3>${content.title}</h3>

    <p>${content.text!""}</p>
    <div id="main">

        [@cms.contentNodeIterator contentNodeCollectionName="main"]
            [@cms.includeTemplate /]
        [/@cms.contentNodeIterator]
        [@cms.newBar contentNodeCollectionName="main" paragraph="sampleFreemarker, sampleFreemarkerSearch, sampleFTLControlsShowRoom" /]

        [#include "include/footer.ftl"]
    </div><!-- end main -->
    <div id="search" >
        <form name="mgnlsearch" action="" method="post">
          <input type="hidden" id="resultPage" name="resultPage" value="searchResultFTL" />
          <input id="query" name="query" value="${query!}" />
          <input type="submit" name="search" value="search" />
        </form>
    </div><!-- end search -->
    [@cmsu.simpleNavigation /]
</body>
</html>