<jsp:root version="1.2" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:c="urn:jsptld:http://java.sun.com/jsp/jstl/core"
    xmlns:fmt="urn:jsptld:http://java.sun.com/jsp/jstl/fmt" xmlns:cms="urn:jsptld:cms-taglib">
    <jsp:directive.page contentType="text/html; charset=UTF-8" />

    <jsp:scriptlet>pageContext.setAttribute("license", info.magnolia.cms.license.LicenseFileExtractor.getInstance().getEntries());</jsp:scriptlet>
    <jsp:scriptlet>pageContext.setAttribute("isadmin", new Boolean(info.magnolia.cms.beans.config.Server.isAdmin()));</jsp:scriptlet>
    <fmt:message var="version" key="about.version" />
    <fmt:message var="build" key="about.build" />

    <jsp:text>
        <![CDATA[<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"> ]]>
    </jsp:text>
    <html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
    <head>
    <title><fmt:message>about.title</fmt:message></title>

    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />

    <!--  add magnolia css and js links -->
    <cms:links adminOnly="false" />

    <style type="text/css">
    <![CDATA[
      body {padding-left: 15px;}
      a {color: #000;}
      .framediv {
        position:absolute;
        top:15px;
        left:220px;
        width: 550px;
        border-style:solid;
        border-width:1px;
        border-top-color:#999;
        border-left-color:#999;
        border-bottom-color:#CCC;
        border-right-color:#CCC;
      }

      .framediv iframe {
        width: 100%;
        height: 450px;
        border: none;
      }
    ]]>
    </style>
    </head>

    <body class="mgnlBgLight">

    <div class="mgnlText">
    <h1>Magnolia CMS</h1>

    <pre><![CDATA[
${version}: ${license.ImplementationVersion}
${build}: ${license.BuildNumber}

<pre>
${license.ProviderAddress}
</pre>

<a href="http://${license.ProductDomain}${license.VersionPageHandle}" target="_blank">${license.ProductDomain}</a>

<a href="mailto:${license.ProviderEmail}">${license.ProviderEmail}</a>
]]></pre></div>



    <div class="framediv"><iframe
        src="http://www.magnolia.info/en/magnolia/admincentral/aboutmagnolia.html?magnoliaVersion=${license.VersionNumber}&amp;amp;magnoliaBuild=${license.ImplementationVersion}&amp;amp;serverOS=${license.OSName}&amp;amp;isAdmin=${isadmin}">
    </iframe></div>

    </body>
    </html>
</jsp:root>
