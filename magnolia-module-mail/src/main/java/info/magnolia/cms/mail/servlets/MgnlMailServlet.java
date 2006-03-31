package info.magnolia.cms.mail.servlets;

import info.magnolia.cms.beans.runtime.Document;
import info.magnolia.cms.mail.MailConstants;
import info.magnolia.cms.mail.MgnlMailFactory;
import info.magnolia.cms.mail.handlers.MgnlMailHandler;
import info.magnolia.cms.mail.templates.MgnlEmail;
import info.magnolia.cms.util.RequestFormUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringBufferInputStream;
import java.util.*;

/**
 * Servlet to send email using the email module.
 * Date: Mar 31, 2006
 * Time: 10:12:45 AM
 *
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public class MgnlMailServlet extends javax.servlet.http.HttpServlet {

    Logger log = LoggerFactory.getLogger(MgnlMailServlet.class);
    private static final String TYPE = "type";
    private static final String SUBJECT = "subject";
    private static final String FROM = "from";
    private static final String TEXT = "text";
    private static final String TORECIPIENTS = "torecipients";
    private static final String CCRECIPIENTS = "ccrecipients";
    private static final String PARAMETERS = "parameters";
    private static final String FILE = "file";
    private static final String TEMPLATE = "template";
    public static final String ATT_ID = "att";
    public static final String ACTION = "action";

    protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        RequestFormUtil request = new RequestFormUtil(httpServletRequest);
        if (request.getParameter(ACTION) == null) {
            this.doGet(httpServletRequest, httpServletResponse);
            return;
        }

        String type = request.getParameter(TYPE);
        String subject = request.getParameter(SUBJECT);
        String from = request.getParameter(FROM);
        String text = request.getParameter(TEXT);
        String to = request.getParameter(TORECIPIENTS);
        String cc = request.getParameter(CCRECIPIENTS);
        String parameters = request.getParameter(PARAMETERS);
        String template = request.getParameter(TEMPLATE);
        Document doc;
        Hashtable attachment = new Hashtable(1);
        try {
            doc = request.getDocument(FILE);
            attachment.put(ATT_ID, doc.getFile());
        } catch (Exception e) {
            log.info("No Attachment", e);
        }

        try {
            MgnlMailFactory factory = MgnlMailFactory.getInstance();
            MgnlMailHandler handler = factory.getEmailHandler();

            HashMap map = convertToMap(parameters);
            map.put(MailConstants.MAIL_ATTACHMENT, attachment);

            MgnlEmail email = factory.getEmailFromType(type);
            email.setFrom(from);
            email.setSubject(subject);
            email.setToList(factory.convertEmailList(to));
            email.setBody(text, map);
            email.setTemplate(template);
            email.setCcList(factory.convertEmailList(cc));
            log.info("Email:" + email.toString());
            handler.prepareAndSendMail(email);

            PrintWriter writer = httpServletResponse.getWriter();
            writer.write("<p class=\"success\">");
            writer.write("Mail was sent to " + from);
            writer.write("</p>");
        }
        catch (Exception e) {
            log.error("Error while sending email", e);
            PrintWriter writer = httpServletResponse.getWriter();
            writer.write("<p class=\"error\">");
            e.printStackTrace(writer);
            writer.write("</p>");
        }
        finally {
            doGet(httpServletRequest, httpServletResponse);
        }
    }

    private HashMap convertToMap(String parameters) throws IOException {
        HashMap map = new HashMap();
        StringBufferInputStream string = new StringBufferInputStream(parameters);
        Properties props = new Properties();
        props.load(string);
        Iterator iter = props.keySet().iterator();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            map.put(key, props.get(key));
        }
        return map;
    }

    private String getServletStyle() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("" +
                "<style type=\"text/css\">\n" +
                "<!--\n " +
                "body    { background-color: rgb(255,255,255); color: rgb(0,0,0); margin: 0px 0px; font-size: 10pt ; font-family: verdana}" +
                "p.error       { font-size: 8pt; color: red; }" +
                "p.success { font-size: 8pt; color: green; }" +
                "p.comments { font-size: 7pt; }" +
                "textArea {color: blue }" +
                "input {color:blue ; font-size: 10pt}" +
                "table {font-size: 10pt}" +
                "tr {font-size: 10pt}" +
                "td {font-size: 10pt}" +
                "h1 {font-family: verdana ; font-size : 12pt}" +
                "-->\n " +
                "</style>" +
                "");
        return buffer.toString();
    }

    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        displayForm(httpServletResponse);
    }

    private void displayForm(HttpServletResponse httpServletResponse) throws IOException {
        StringBuffer sb = new StringBuffer();
        sb.append("<html><body>");
        sb.append(getServletStyle());

        sb.append("<h1>Email Servlet</h1>");

        // Email edit area
        sb.append("<form method=\"post\">");
        sb.append("<input type=\"hidden\"  width=\"80%\" name=\"" + ACTION + "\" value=\"action\"/>");
        sb.append("<table>");

        sb.append("<tr>");
        sb.append("<td>From:</td>");
        sb.append("<td><input type=\"text\" width=\"80%\"cols=\"80\" name=\"" + FROM + "\"/></td>");
        sb.append("</tr>");

        sb.append("<tr>");
        sb.append("<td>Suject:</td>");
        sb.append("<td><input type=\"text\" width=\"80%\"cols=\"80\" name=\"" + SUBJECT + "\"/></td>");
        sb.append("</tr>");

        sb.append("<tr>");
        sb.append("<td>Text:</td>");
        sb.append("<td><textArea  cols=\"80\" width=\"80%\"rows=\"10\"  name=\"" + TEXT + "\"></textArea></td>");
        sb.append("</tr>");

        sb.append("<tr>");
        sb.append("<td>To:</td>");
        sb.append("<td><textArea  cols=\"80\" width=\"80%\"rows=\"2\"  name=\"" + TORECIPIENTS + "\"></textArea></td>");
        sb.append("</tr>");

        sb.append("<tr>");
        sb.append("<td>Cc:</td>");
        sb.append("<td><textArea  cols=\"80\" width=\"80%\"rows=\"2\"  name=\"" + CCRECIPIENTS + "\"></textArea></td>");
        sb.append("</tr>");

        sb.append("<tr>");
        sb.append("<td>Parameters:</td>");
        sb.append("<td><textArea  cols=\"80\" width=\"80%\"rows=\"2\"  name=\"" + PARAMETERS + "\"></textArea></td>");
        sb.append("</tr>");

        sb.append("<tr>");
        sb.append("<td>Attach:</td>");
        sb.append("<td><input cols=\"80\" width=\"80%\"type=\"file\" name=\"" + FILE + "\"><p class=\"comments\">The attachment has a default id of '" + ATT_ID + "'. This is only limited in the servlet</p>");
        sb.append("</tr>");

        sb.append("<tr>");
        sb.append("<td>Template:</td>");
        sb.append("<td>" + getTemplateSelectBox() + "</td>");
        sb.append("</tr>");

        sb.append("<tr>");
        sb.append("<td>Email Type:</td>");
        sb.append("<td>" + getMailTypeSelectBox() + "</td>");
        sb.append("</tr>");

        sb.append("</table>");

        sb.append("<center><input type=\"submit\" value=\"send\"/></center>");
        sb.append("</form>");

        sb.append("</body></html>");

        PrintWriter writer = httpServletResponse.getWriter();
        writer.write(sb.toString());
        writer.flush();
    }

    private StringBuffer getMailTypeSelectBox() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<select name=\"" + TYPE + "\">");
        addOption(MailConstants.MAIL_TEMPLATE_SIMPLE, buffer);
        addOption(MailConstants.MAIL_TEMPLATE_STATIC, buffer);
        addOption(MailConstants.MAIL_TEMPLATE_HTML, buffer);
        addOption(MailConstants.MAIL_TEMPLATE_VELOCITY, buffer);
        buffer.append("</select>");
        return buffer;
    }

    private void addOption(String option, StringBuffer buffer) {
        buffer.append("<option value=\"" + option + "\">" + option + "");
    }

    private StringBuffer getTemplateSelectBox() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<select ");
        try {
            ArrayList list = MgnlMailFactory.getInstance().listTemplatesFromRepository();

            // if no template, disable
            if (list.size() == 0)
                buffer.append(" disabled ");
            buffer.append(" name=\"" + TEMPLATE + "\">");

            for (int i = 0; i < list.size(); i++) {
                String item = (String) list.get(i);
                addOption(item, buffer);
            }
        }
        catch (Exception e) {
            log.error("Error while getting the templates");
        } finally {
            buffer.append("</select>");
            return buffer;
        }
    }

}
