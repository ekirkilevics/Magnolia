[#-------------- Sample FTL Template --------------]

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
        <title>${content.title!content.@name}</title>
    </head>

    <body style="background-color:#ccb">
        <cms:page content="website:/howTo-freemarker" dialog="samples:samplesFieldShowRoom" label="/howTo-freemarker - Sample using the NEW freemarker template"></cms:page>

        <h1>${content.title!content.@name}</h1>
        <h3>${content.@path} (${content.@id})</h3>
        <h3>From JCR NODE: ${cmsfn.asJCRNode(content).path} </h3>

        <div id="wrapper" style="padding:15px;">

            <h2>Singleton Area (stage)</h2>
            [@cms.area name="stage" /]


            <h2>List Area (main)</h2>
            [@cms.area name="main" /]

        </div><!-- end wrapper -->

  </body>
</html>
