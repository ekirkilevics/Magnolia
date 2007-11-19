<jsp:root version="1.2" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:cms="cms-taglib"
    xmlns:cmsu="cms-util-taglib" xmlns:c="http://java.sun.com/jsp/jstl/core">
  <jsp:directive.page import="info.magnolia.module.ModuleRegistry"/>
  <jsp:directive.page contentType="text/html; charset=UTF-8" session="false" />

  <!-- exposes the current node for use with jstl -->
  <cms:setNode var="pageProperties" />

  <title>Magnolia 3.0 Samples | ${pageProperties.title}</title>

  <!--  add magnolia css and js links -->
  <cms:links />
  <jsp:scriptlet>
    request.setAttribute("module", ModuleRegistry.Factory.getInstance().getModuleInstance("samples"));
  </jsp:scriptlet>

  <c:forEach items="${module.cssFiles}" var="cssFile">
      <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}${cssFile}"/>
  </c:forEach>

	<c:forEach items="${module.jsFiles}" var="jsFile">
	  <script type="text/javascript" src="${pageContext.request.contextPath}${jsFile}">
        <jsp:text><![CDATA[<!--  -->]]></jsp:text>
      </script>
	</c:forEach>

  <meta name="description" content="${pageProperties.metaDescription}" />
  <meta name="keywords" content="${pageProperties.metaKeywords}" />
</jsp:root>
