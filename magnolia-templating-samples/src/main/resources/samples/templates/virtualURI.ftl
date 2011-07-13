[#-------------- VirtualURI Template --------------]

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
        <link media="screen" href="${contextPath}/.resources/samples/css/samples.css" type="text/css" rel="stylesheet">
        <title>${content.title!content.@name}</title>
    </head>

    <body>
        <div id="wrapper">

            <cms:page content="website:${content.@path}" dialog="samples:mainProperties" label="/samples virtual URI mappings"></cms:page>

            <div id="header">
                <div id="navigation">
                    [#include "/samples/areas/navigation.ftl" ]
                </div><!-- end navigation -->
            </div><!-- end navigation -->


            <h1>${content.title!content.@name}</h1><br />
            <p>${content.text!""}</p><br />

            <div id="main">
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
                </div>
                <br />

            </div><!-- end main -->

        </div><!-- end wrapper -->
    </body>
</html>