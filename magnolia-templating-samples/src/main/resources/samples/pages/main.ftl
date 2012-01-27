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

        [#-- ****** main page bar ****** --]

        <div id="wrapper">
            <div id="header">

                [#-- ****** navigation area ****** --]
                [@cms.area name="navigation" /]

                [#-- ****** stage area ****** --]
                [@cms.area name="stage"/]

            </div><!-- end header -->

            [#-- ****** page content ****** --]
            <h1>${content.title!content.@name}</h1>
            [#if content.text?has_content]
                <p>${content.text}</p>
            [/#if]

            <div id="wrapper-2">

                [#-- ****** main area ****** --]
                [@cms.area name="main" /]

                [#-- ****** extras area ****** --]
                [@cms.area name="extras"/]

            </div><!-- end wrapper-2 -->

            [#-- ****** footer area ****** --]
            [@cms.area name="footer" /]


        </div><!-- end wrapper -->
    </body>
</html>
