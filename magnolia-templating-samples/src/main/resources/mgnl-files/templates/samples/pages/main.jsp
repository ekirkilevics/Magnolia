<%@ page language="java" contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="cms" uri="http://magnolia-cms.com/taglib/templating-components/cms"%>
<%@ taglib prefix="cmsfn" uri="http://magnolia-cms.com/taglib/templating-components/cmsfn"%>


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
      <cms:area name="navigation" />

      <!-- ****** stage area ****** -->
      <cms:area name="stage" />

    </div>
    <!-- end header -->

    <!-- ****** page content ****** -->
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

    <c:if test="${not empty content.text}">
      <p>${cmsfn:decode(content).text}</p>
    </c:if>

    <div id="wrapper-2">
           <!-- ****** main area ****** -->
            <cms:area name="main" />

            <!-- ****** extras area ****** -->
            <cms:area name="extras"/>


    </div>
    <!-- end wrapper-2 -->

        <!-- ****** footer area ****** -->
        <cms:area name="footer" />


  </div>
  <!-- end wrapper -->
 </body>
</html>