<?xml version="1.0" encoding="UTF-8" ?>
<jsp:root version="1.2" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:c="http://java.sun.com/jsp/jstl/core">
    <jsp:directive.page contentType="text/html; charset=UTF-8" session="false" />

    <jsp:directive.page import="info.magnolia.cms.gui.dialog.DialogControlImpl" />
    <jsp:directive.page import="info.magnolia.cms.gui.misc.CssConstants" />
    <jsp:directive.page import="info.magnolia.cms.gui.control.Edit" />

    <table cellpadding="0" cellspacing="2" border="0" height="60">
        <tr>
            <jsp:scriptlet>
    DialogControlImpl control=(DialogControlImpl) pageContext.getRequest().getAttribute("dialogObject");

    //access all the values of the dialog definition with getConfigValue
    String colorStart=control.getConfigValue("colorStart");
    String colorOffset=control.getConfigValue("colorOffset");

    int start=Integer.parseInt(colorStart,16);
    int offset=Integer.parseInt(colorOffset,16);

    for (int i=start;i>0;i-=offset) {

        String color=Integer.toHexString(i);
        while(6>color.length()) {
            color="0"+color;
        }
        pageContext.setAttribute("color", color);
        </jsp:scriptlet>

            <td style="background-color:#${color};width:60px;"
                onclick="document.getElementById('${dialogObject.name}').value='${color}'">&amp;nbsp;</td>
            <jsp:scriptlet>
    }
    </jsp:scriptlet>
        </tr>
    </table>
    <br />
    Selected value:
    <br />

    <jsp:scriptlet>


    //for form elements use the magnolia control package
    // toggle alt to get two different outputs

    Edit editControl=new Edit(control.getName(),control.getWebsiteNode());
    editControl.setCssClass(CssConstants.CSSCLASS_EDIT);
    editControl.setCssStyles("width","60");
    out.println(editControl.getHtml());

</jsp:scriptlet>
</jsp:root>
