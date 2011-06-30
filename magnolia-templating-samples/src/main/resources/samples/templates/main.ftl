[#-------------- Sample FTL Template --------------]

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
        <title>${content.title!content.@name}</title>
    </head>

    <body style="background-color:#ccb">
        <div id="wrapper" style="padding:15px;">

            <cms:page content="website:/howTo-freemarker" dialog="samples:samplesFieldShowRoom" label="/howTo-freemarker - Sample using the NEW freemarker template"></cms:page>

            <h1>${content.title!content.@name}</h1>
            <p>${content.@path} (${content.@id})</p>
            <p>From JCR NODE: ${cmsfn.asJCRNode(content).path} </p>


            [#-- ****** stage ****** --]
            <div id="stage">
                <h3>Single Area Stage</h3>
                [@cms.area name="stage" /]
            </div><!-- end stage -->


            [#-- ****** main ****** --]
            <h3>List Area Main</h3>
            <div id="main">
                [@cms.area name="main" /]
            </div><!-- end main -->


        </div><!-- end wrapper -->

  </body>
</html>
