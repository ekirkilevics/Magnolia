<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
    xmlns:c="http://java.sun.com/jsp/jstl/core"
    xmlns:cms="cms-taglib">
    <jsp:directive.page language="java"
        contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" />

    <jsp:text>
        <![CDATA[ <?xml version="1.0" encoding="UTF-8" ?> ]]>
    </jsp:text>
    <jsp:text>
        <![CDATA[ <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"> ]]>
    </jsp:text>

<jsp:useBean id="zipUpload" class="info.magnolia.module.dms.gui.ZipUploadView"/>

<jsp:scriptlet>
    zipUpload.process(request);
</jsp:scriptlet>

<c:if test="${zipUpload.success}">
    <script type="text/javascript">
        window.close();
        opener.mgnl.dms.DMS.showInTree('${zipUpload.path}');
    </script>
</c:if>

<c:if test="${not zipUpload.success}">
        <jsp:scriptlet>
            zipUpload.render(request, response);
        </jsp:scriptlet>

        <c:if test="${not empty zipUpload.msg}">
            <script type="text/javascript">
                alert('${zipUpload.msg}');
            </script>
        </c:if>
</c:if>
</jsp:root>