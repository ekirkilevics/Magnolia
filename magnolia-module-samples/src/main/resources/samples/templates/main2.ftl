[#-------------- Sample FTL Template --------------]

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
        <title>${content.title!content.@name}</title>
    </head>

    <body style="background-color:#ccb">

	<h1>This page is: ${cmsfn.asJCRNode(content).path} (${cmsfn.asJCRNode(content).identifier})</h1>

	<div id="main" style="padding:15px;">

	[#list (content.main)?children as paragraph]
		[@cms.render content=paragraph /]
	[/#list]

	<h2>Area</h2>
	[@cms.area name="stage" dialog="mainProperties" paragraphs="samplesHowToFTL,samplesFreemarkerParagraph"/]

	[@cms.context name="shoeSize" value="213" /]

	</div>

	</body>
</html>