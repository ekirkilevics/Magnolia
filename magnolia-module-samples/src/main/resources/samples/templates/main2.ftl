[#-------------- Sample FTL Template --------------]

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
        <title>${content.title!content.@name}</title>
        <link rel="stylesheet" type="text/css" href="${contextPath}/docroot/samples/samples.css"/>
    </head>

    <body>

	[@cms.edit /]

	[@cms.contextAttribute name="shoeSize" value="213" /]

	[@cms.area name="stage" /]

	[@cms.render /]

	${cmsfn.asJCRNode(content)}

	</body>
</html>