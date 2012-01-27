<%@ page language="java" contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="cms" uri="http://magnolia-cms.com/taglib/templating-components/cms"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
 <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
  <link media="screen" href="${pageContext.request.contextPath}/.resources/samples/css/samples.css" type="text/css" rel="stylesheet">

  <cms:init />

  <title>
    <c:choose>
      <c:when test="${not empty content.title}">
        ${content.title}
      </c:when>
      <c:otherwise>
        ${content['@name']}
      </c:otherwise>
    </c:choose>
    </title>
 </head>
 <body>
  <!-- ****** main page bar ****** -->

    <div id="wrapper">


        <div id="header">
        <!-- ****** navigation ****** -->
        <jsp:include page="/templates/samples/areas/navigation.jsp" />
            </div><!-- end header -->


      <h1>
          <c:choose>
        <c:when test="${not empty content.title}">
            ${content.title}
          </c:when>
          <c:otherwise>
            ${content['@name']}
          </c:otherwise>
        </c:choose>
      </h1>

            <div id="main">
                <p>Select a product:
                    <a href="${pageContext.request.contextPath}${content['@path']}/product1">Product 1</a>
                    <a href="${pageContext.request.contextPath}${content['@path']}/product2">Product 2</a>
                    <a href="${pageContext.request.contextPath}${content['@path']}/product3">Product 3</a>
                </p>
                <p>You selected:
                  <b>
                    <c:choose>
            <c:when test="${not empty param.product}">
              ${param.product}
            </c:when>
            <c:otherwise>
              none yet
            </c:otherwise>
          </c:choose>
                  </b>
                </p>
                <br />
                <div>
                    <h3>Display Sources</h3><br />
                    <ul>
                        <li><a href="${pageContext.request.contextPath}/.sources/pages/virtualURI.jsp">VirtualURI Template</a></li>
                    </ul>
                </div>
                <br />

            </div><!-- end main -->

        <!-- ****** footer area ****** -->
        <cms:area name="footer" />

        </div><!-- end wrapper -->
 </body>
</html>