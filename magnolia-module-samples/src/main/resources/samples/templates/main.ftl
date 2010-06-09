[#-------------- Sample FTL Template --------------]

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
[#assign cms=JspTaglibs["cms-taglib"]]
[#assign cmsu=JspTaglibs["cms-util-taglib"]]

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
        <title>${content.title!content.@name}</title>
        <link rel="stylesheet" type="text/css" href="${contextPath}/docroot/samples/samples.css"/>
        [@cms.links /]
    </head>

    <body>

    [#-- Page Dialog --]
    [@cms.mainBar dialog="mainProperties" /]

    <div id="header">
        <h1>Magnolia Samples</h1>
    </div>

    [#-- Simple Navigation Tag --]
    <div id="navigation">[@cmsu.simpleNavigation /]</div>

    <div id="main">

        [#-- Variable titleSize is set in the template definition, if empty sets it to 1 --]
        <h${def.titleSize!"1"}>${content.title!content.@name}</h${def.titleSize!"1"}><br />

        <p>${content.text!""}</p><br />

        [#-- Variable dummy is set in the template definition parameters content node --]
        <p>dummy: ${def.dummy!""}</p><br />

        <div>
            <h3>Display Sources</h3><br />
            <ul>
                <li><a href="${ctx.contextPath}/.sources/templates/main.ftl">Main template</a></li>
            </ul>
        </div><br />

        [@cms.contentNodeIterator contentNodeCollectionName="main"]
            [@cms.includeTemplate /]
        [/@cms.contentNodeIterator]

        [@cms.newBar contentNodeCollectionName="main" paragraph="samplesHowToFTL, samplesHowToJSP, samplesControlsShowRoom, samplesSearchResult" /]
    </div>

    <div id="footer">
        <p>This page was last edited by <span class="author">${content.metaData.authorId}</span>
            on <span class="modificationdate">${content.metaData.modificationDate}</span>
        </p>
    </div>
    </body>
</html>