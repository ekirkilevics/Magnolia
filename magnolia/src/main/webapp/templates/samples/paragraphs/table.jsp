<jsp:root version="1.2" xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:cms="urn:jsptld:cms-taglib"
    xmlns:cmsu="urn:jsptld:cms-util-taglib"
    xmlns:c="urn:jsptld:http://java.sun.com/jsp/jstl/core">
    <jsp:directive.page import="info.magnolia.cms.util.Resource" />
<jsp:scriptlet>
<![CDATA[

    String data=Resource.getLocalContentNode(request).getNodeData("tableData").getString();

    if (!data.equals("")) {
        boolean header=Resource.getLocalContentNode(request).getNodeData("tableHeader").getBoolean();
        boolean altBg=Resource.getLocalContentNode(request).getNodeData("tableAltBg").getBoolean();
        boolean linesH=Resource.getLocalContentNode(request).getNodeData("tableLinesHorizontal").getBoolean();
        boolean linesV=Resource.getLocalContentNode(request).getNodeData("tableLinesVertical").getBoolean();
        boolean small=Resource.getLocalContentNode(request).getNodeData("tableFontSmall").getBoolean();
        boolean alignRight=Resource.getLocalContentNode(request).getNodeData("tableAlignment").getBoolean();

        out.println("<table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">");
        boolean alt=true;

        String[] rows=data.split("\r\n");
        StringBuffer html=new StringBuffer();

        for (int i=0;i<rows.length;i++) {

            html.append("<tr");
            if (altBg && alt) html.append(" class=\"tableAlt\"");
            html.append(">");

            String[] cols=rows[i].split("\t");

            for (int ii=0;ii<cols.length;ii++) {
                String cssClass;
                if (i==0 && header) cssClass="tableHead";
                else cssClass="table";

                html.append("<td class=\""+cssClass+"\" style=\"");
                if (alignRight) {
                    if (i!=0) html.append("text-align:right;");
                    else  html.append("text-align:center;");
                }
                if (small) html.append("font-size:9px;");
                if (linesV) {
                    html.append("border-right-width:1px;");
                    if (ii==0) html.append("border-left-width:1px;");
                }
                if (linesH) {
                    html.append("border-bottom-width:1px;");
                    if (i==0) html.append("border-top-width:1px;");
                }
                html.append("\">");

                if (cols[ii].equals("")) html.append("&nbsp;");
                else html.append(cols[ii]);

                html.append("</td>");
            }
            html.append("</tr>");
            alt=!alt;
        }
        html.append("</table>");
        out.println(html);
    }
]]>
</jsp:scriptlet>
</jsp:root>
