[#-------------- Sample FTL Template --------------]

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">

    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
        <link media="screen" href="${contextPath}/.resources/samples/css/samples.css" type="text/css" rel="stylesheet">
        <title>${content.title!content.@name}</title>
    </head>

    <body>
        <div id="wrapper">

            <cms:page content="website:${content.@path}" dialog="samples:samplesFieldShowRoom" label="/howTo-freemarker - Sample using the NEW freemarker template"></cms:page>

            <div id="header">

                [#-- ****** navigation ****** --]
                <div id="navigation">
                    [#include "/samples/areas/navigation.ftl" ]
                    [#-- should be used as an area so it can be inherited. Inheritance of areas not implemented yet.
                    [@cms.area name="stage" /]
                    --]
                </div><!-- end navigation -->


                [#-- ****** stage ****** --]
                <div id="stage">
                    <h3>Single-Area Stage</h3>
                    [@cms.area name="stage" /]
                </div><!-- end stage -->

            </div><!-- end header -->

            <h1>${content.title!content.@name}</h1>
            <p>${content.@path} (${content.@id})</p>
            <p>From JCR NODE: ${cmsfn.asJCRNode(content).path} </p>


            <div id="wrapper-2">

                [#-- ****** main ****** --]
                <div id="main">
                  <h3>List-Area Main</h3>
                    [@cms.area name="main" /]
                </div><!-- end main -->
                [#-- [@cms.area name="main" dialog="samples:mainProperties" components="samples:paragraphs/howToFTL,samples:paragraphs/text"/] --]


                [#-- ****** extras ****** --]
                <div id="extras">
                <h3>List-Area Extras</h3>
                    [@cms.area name="extras" /]
                </div><!-- end extras -->

            </div><!-- end wrapper-2 -->

            [#-- ****** footer  ****** --]
            <div id="footer">
              <h3>List-Area Footer</h3>
                [@cms.area name="footer" /]
            </div><!-- end footer -->


        </div><!-- end wrapper -->
    </body>
</html>
