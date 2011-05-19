[#-------------- VirtualURI Template --------------]

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

            <h1>${content.title!content.@name}</h1><br />

            <p>${content.text!""}</p><br />
            <p>Select a product:
                <a href="${ctx.contextPath}${content.@handle}/product1">Product 1</a>
                <a href="${ctx.contextPath}${content.@handle}/product2">Product 2</a>
                <a href="${ctx.contextPath}${content.@handle}/product3">Product 3</a>
            </p>
            <p>You selected: ${ctx.parameters.product!"none yet"}</p>
            <br />
            <div>
                <h3>Display Sources</h3><br />
                <ul>
                    <li><a href="${ctx.contextPath}/.sources/templates/virtualURI.ftl">VirtualURI Template</a></li>
                </ul>
            </div><br />
        </div>

    </body>
</html>