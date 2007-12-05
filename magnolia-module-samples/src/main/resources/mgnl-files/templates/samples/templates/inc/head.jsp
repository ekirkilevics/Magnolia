<jsp:root version="1.2" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:cms="cms-taglib"
    xmlns:cmsu="cms-util-taglib" xmlns:c="http://java.sun.com/jsp/jstl/core">
  <jsp:directive.page contentType="text/html; charset=UTF-8" session="false" />

  <!-- exposes the current node for use with jstl -->
  <cms:setNode var="pageProperties" />

  <title>Magnolia 3.0 Samples | ${pageProperties.title}</title>

  <!--  add magnolia css and js links -->
  <cms:links />
  
  <!-- populate the Samples module configuration to JSP/JSTL -->
  <jsp:useBean id="module" class="info.magnolia.module.samples.jsp.SamplesModuleConfigHelper" scope="request"/>

  <!-- include css files that are configured in AdminCentral under config:/modules/samples/config/cssFiles -->
  <c:forEach items="${module.cssFiles}" var="cssFile">
      <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}${cssFile}"/>
  </c:forEach>

  <!-- include js files that are configured in AdminCentral under config:/modules/samples/config/jsFiles -->
	<c:forEach items="${module.jsFiles}" var="jsFile">
	  <script type="text/javascript" src="${pageContext.request.contextPath}${jsFile}">
        <jsp:text><![CDATA[<!--  -->]]></jsp:text>
      </script>
	</c:forEach>

  <meta name="description" content="${pageProperties.metaDescription}" />
  <meta name="keywords" content="${pageProperties.metaKeywords}" />
</jsp:root>
