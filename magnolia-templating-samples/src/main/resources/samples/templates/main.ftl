[#-------------- Sample FTL Template --------------]

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">

    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
        <link media="screen" href="${contextPath}/.resources/samples/css/samples.css" type="text/css" rel="stylesheet">
        [@cms.init /]
        <title>${content.title!content.@name}</title>
    </head>

    <body>
        <div id="wrapper">
            [#-- ****** main page bar ****** --]
            [@cms.edit /]
            <div id="header">

                [#-- ****** navigation ****** --]
                <div id="navigation">
                    [@cms.area name="navigation" /]
                </div><!-- end navigation -->

                [#-- ****** stage ****** --]
                [@cms.area name="stage"/]

            </div><!-- end header -->

            <h1>${content.title!content.@name}</h1>
            <p>${content.@path} (${content.@id})</p>
            <p>From JCR NODE: ${cmsfn.asJCRNode(content).path} </p>


            <div id="wrapper-2">

                [#-- ****** main ****** --]
                [@cms.area name="main" /]

                [#-- ****** extras ****** --]
                [@cms.area name="extras"/]

            </div><!-- end wrapper-2 -->

            [#-- ****** footer  ****** --]
            [@cms.area name="footer" /]


        </div><!-- end wrapper -->
    </body>
</html>
