/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.gui.dialog;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.gui.control.Button;
import info.magnolia.cms.gui.control.Hidden;
import info.magnolia.cms.gui.misc.CssConstants;
import info.magnolia.cms.gui.misc.Sources;
import info.magnolia.cms.gui.misc.Spacer;
import info.magnolia.cms.i18n.MessagesManager;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpURL;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.webdav.lib.Property;
import org.apache.webdav.lib.WebdavResource;
import org.apache.webdav.lib.methods.XMLResponseMethodBase;


/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class DialogWebDAV extends DialogBox {

    private static final int ICONS_HEIGHT = 16;

    private static final int ICONS_WIDTH = 23;

    public static final String NULLGIF = "/admindocroot/0.gif"; //$NON-NLS-1$

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(DialogWebDAV.class);

    // dev; remove values later (StringUtils.EMPTY; not null!)
    private String host = StringUtils.EMPTY;

    private int port;

    private String directory = StringUtils.EMPTY;

    private String subDirectory = StringUtils.EMPTY;

    private String user = StringUtils.EMPTY;

    private String password = StringUtils.EMPTY;

    private String protocol = "http"; //$NON-NLS-1$

    private WebdavResource davConnection;

    /**
     * Empty constructor should only be used by DialogFactory.
     */
    protected DialogWebDAV() {
    }

    /**
     * @see info.magnolia.cms.gui.dialog.DialogInterface#init(HttpServletRequest, HttpServletResponse, Content, Content)
     */
    public void init(HttpServletRequest request, HttpServletResponse response, Content websiteNode, Content configNode)
        throws RepositoryException {
        super.init(request, response, websiteNode, configNode);
        initIconExtensions();
    }

    public void setHost(String s) {
        this.host = s;
    }

    public String getHost() {
        return this.host;
    }

    public void setPort(String s) {
        try {
            this.port = (new Integer(s)).intValue();
        }
        catch (NumberFormatException nfe) {
            this.port = 0;
        }
    }

    public void setPort(int i) {
        this.port = i;
    }

    public int getPort() {
        if (this.port == 0) {
            return 80;
        }

        return this.port;
    }

    public void setDirectory(String s) {
        this.directory = s;
    }

    public String getDirectory() {
        return this.directory;
    }

    public void setSubDirectory(String s) {
        this.subDirectory = s;
    }

    public String getSubDirectory() {
        return this.subDirectory;
    }

    public void setUser(String s) {
        this.user = s;
    }

    public String getUser() {
        return this.user;
    }

    public void setPassword(String s) {
        this.password = s;
    }

    public String getPassword() {
        return this.password;
    }

    public void setProtocol(String s) {
        this.protocol = s;
    }

    public String getProtocol() {
        return this.protocol;
    }

    public String getHtmlDecodeURI(String s) {
        return "<script type=\"text/javascript\">document.write(decodeURI(\"" + s + "\"));</script>"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    public void setDAVConnection(WebdavResource w) {
        this.davConnection = w;
    }

    public WebdavResource getDAVConnection() {
        return this.davConnection;
    }

    private String getSizeValue() {
        if (this.getWebsiteNode() != null) {
            return this.getWebsiteNode().getNodeData(this.getName() + "_size").getString(); //$NON-NLS-1$
        }

        return StringUtils.EMPTY;
    }

    private String getModDateValue() {
        if (this.getWebsiteNode() != null) {
            return this.getWebsiteNode().getNodeData(this.getName() + "_lastModified").getString(); //$NON-NLS-1$
        }

        return StringUtils.EMPTY;
    }

    public void setDAVConnection() {
        WebdavResource wdr = null;
        try {

            wdr = new WebdavResource(new HttpURL(
                this.getUser(),
                this.getPassword(),
                this.getHost(),
                this.getPort(),
                this.getDirectory()));

        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        this.setDAVConnection(wdr);
    }

    /**
     * @see info.magnolia.cms.gui.dialog.DialogInterface#drawHtml(Writer)
     */
    public void drawHtml(Writer out) throws IOException {
        this.drawHtmlPre(out);
        this.setDAVConnection();
        this.setSessionAttribute();
        String showName = "&nbsp;"; //$NON-NLS-1$
        String showPath = StringUtils.EMPTY;
        String showIcon = StringUtils.EMPTY;
        if (StringUtils.isNotEmpty(this.getValue())) {
            String valueTmp = StringUtils.EMPTY;
            boolean isDirectory = false;
            if (this.getValue().lastIndexOf("/") == this.getValue().length() - 1) { //$NON-NLS-1$
                // value is directory
                valueTmp = this.getValue().substring(0, this.getValue().length() - 1);
                isDirectory = true;
                this.setSubDirectory(this.getValue());
                showIcon = ICONS_PATH + ICONS_FOLDER;
            }
            else {
                // value is file
                valueTmp = this.getValue();
                showIcon = this.getIconPath(this.getValue());
            }
            if (valueTmp.indexOf("/") != -1) { //$NON-NLS-1$
                showName = valueTmp.substring(valueTmp.lastIndexOf("/") + 1); //$NON-NLS-1$
                if (!isDirectory) {
                    this.setSubDirectory(valueTmp.substring(0, valueTmp.lastIndexOf("/") + 1)); //$NON-NLS-1$
                }
            }
            else {
                showName = valueTmp;
                if (!isDirectory) {
                    this.setSubDirectory(StringUtils.EMPTY);
                }
            }
            showPath = "/" + this.getSubDirectory().substring(0, this.getSubDirectory().lastIndexOf("/") + 1); //$NON-NLS-1$ //$NON-NLS-2$
            showPath = "<a href=\"javascript:mgnlDialogDAVBrowse('" //$NON-NLS-1$
                + this.getName() + "_iFrame','selectedValue');\">" //$NON-NLS-1$
                + this.getHtmlDecodeURI(showPath) + "</a>"; //$NON-NLS-1$
        }
        else {
            showPath = "<i>" + MessagesManager.get(this.getRequest(), "dialog.webdav.noSelection") + "</i>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            showIcon = NULLGIF;
        }

        this.setDescription(MessagesManager.get(this.getRequest(), "dialog.webdav.connectedTo") //$NON-NLS-1$
            + this.getProtocol() + "://" //$NON-NLS-1$
            + this.getHost() + ":" //$NON-NLS-1$
            + this.getPort() + this.getDirectory() + "<br />" //$NON-NLS-1$
            + this.getDescription());
        out.write(Spacer.getHtml(2, 2));
        out.write("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\">"); //$NON-NLS-1$
        out.write("<tr>"); //$NON-NLS-1$
        out.write("<td><img id=\"" //$NON-NLS-1$
            + this.getName() + "_showIcon\" src=\"" //$NON-NLS-1$
            + this.getRequest().getContextPath() + showIcon + "\" width=\"" //$NON-NLS-1$
            + ICONS_WIDTH + "\" height=\"" //$NON-NLS-1$
            + ICONS_HEIGHT + "\"></td>"); //$NON-NLS-1$
        out.write("<td id=\"" + this.getName() + "_showName\">" //$NON-NLS-1$ //$NON-NLS-2$
            + this.getHtmlDecodeURI(showName) + "</td>"); //$NON-NLS-1$
        out.write("</tr><tr height=\"4\"><td></td></tr><tr>"); //$NON-NLS-1$
        out.write("<td><img src=\"" //$NON-NLS-1$
            + this.getRequest().getContextPath() + ICONS_PATH + ICONS_FOLDER + "\"></td>"); //$NON-NLS-1$
        out.write("<td id=\"" + this.getName() + "_showPath\">" + showPath + "</td>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        out.write("</tr></table>"); //$NON-NLS-1$
        out.write(new Hidden(this.getName(), this.getValue()).getHtml());
        Hidden size = new Hidden(this.getName() + "_size", this.getSizeValue()); //$NON-NLS-1$
        size.setType(PropertyType.TYPENAME_LONG);
        out.write(size.getHtml());
        Hidden lastMod = new Hidden(this.getName() + "_lastModified", this.getModDateValue()); //$NON-NLS-1$
        lastMod.setType(PropertyType.TYPENAME_DATE);
        out.write(lastMod.getHtml());
        out.write(this.getHtmlSessionAttributeRemoveControl());
        out.write(Spacer.getHtml(12, 12));
        Button home = new Button();
        home.setSaveInfo(false);
        home.setLabel(MessagesManager.get(this.getRequest(), "dialog.webdav.home")); //$NON-NLS-1$
        home.setOnclick("mgnlDialogDAVBrowse('" //$NON-NLS-1$
            + this.getName() + "_iFrame','homeDirectory')"); //$NON-NLS-1$
        out.write(home.getHtml());
        Button refresh = new Button();
        refresh.setSaveInfo(false);
        refresh.setLabel(MessagesManager.get(this.getRequest(), "dialog.webdav.refresh")); //$NON-NLS-1$
        refresh.setOnclick("mgnlDialogDAVBrowse('" + this.getName() //$NON-NLS-1$
            + "_iFrame','refreshDirectory')"); //$NON-NLS-1$
        out.write(refresh.getHtml());
        Button up = new Button();
        up.setSaveInfo(false);
        up.setId(this.getName() + "_upDiv"); //$NON-NLS-1$
        up.setLabel(MessagesManager.get(this.getRequest(), "dialog.webdav.parentdirectory")); //$NON-NLS-1$
        up.setOnclick("mgnlDialogDAVBrowse('" //$NON-NLS-1$
            + this.getName() + "_iFrame','parentDirectory')"); //$NON-NLS-1$
        out.write(up.getHtml());
        out.write(Spacer.getHtml(3, 3));
        // #################
        // iFrame
        // #################
        out.write("<iframe"); //$NON-NLS-1$
        out.write(" id=\"" + this.getName() + "_iFrame\""); //$NON-NLS-1$ //$NON-NLS-2$
        out.write(" class=\"" + CssConstants.CSSCLASS_WEBDAVIFRAME + "\""); //$NON-NLS-1$ //$NON-NLS-2$
        if (this.getConfigValue("height", null) != null) { //$NON-NLS-1$
            out.write(" style=\"height:" + this.getConfigValue("height") + ";\")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        out.write(" frameborder=\"0\""); //$NON-NLS-1$
        out.write(" src=\"/.magnolia/dialogpages/webDAVIFrame.html?" //$NON-NLS-1$
            + SESSION_ATTRIBUTENAME_DIALOGOBJECT + "=" //$NON-NLS-1$
            + this.getConfigValue(SESSION_ATTRIBUTENAME_DIALOGOBJECT) + "&mgnlCK=" //$NON-NLS-1$
            + new Date().getTime() + "\""); //$NON-NLS-1$
        out.write(" reloadsrc=\"0\""); //$NON-NLS-1$
        out.write(" usecss=\"1\""); //$NON-NLS-1$
        out.write(" strict_output=\"1\""); //$NON-NLS-1$
        out.write(" content_type=\"application/xhtml+xml\""); //$NON-NLS-1$
        out.write(" scrolling=\"auto\""); //$NON-NLS-1$
        out.write("></iframe>"); //$NON-NLS-1$
        this.drawHtmlPost(out);
    }

    public void drawHtmlList(Writer out) {
        Enumeration fileList = null;
        String dir = this.getDirectory() + this.getSubDirectory();
        WebdavResource wdr = this.getDAVConnection();
        try {
            dir = URLDecoder.decode(dir, "UTF-8"); //$NON-NLS-1$
        }
        catch (UnsupportedEncodingException uee) {
            // should never happen
        }
        try {
            if (StringUtils.isEmpty(dir)) {
                fileList = wdr.propfindMethod(1);
            }
            else {
                try {
                    fileList = wdr.propfindMethod(dir, 1);
                }
                catch (Exception e) {
                    dir = this.getDirectory();
                    fileList = wdr.propfindMethod(dir, 1);
                }
            }
            out.write("<html><head>"); //$NON-NLS-1$
            out.write(new Sources(this.getRequest().getContextPath()).getHtmlCss());
            out.write(new Sources(this.getRequest().getContextPath()).getHtmlJs());
            String parentDirectory = StringUtils.EMPTY;
            if (!this.getDirectory().equals(dir)) {
                parentDirectory = this.getSubDirectory().substring(0, this.getSubDirectory().length() - 1); // get rid
                // of last /
                // (/dir/home/
                // ->
                // /dir/home)
                parentDirectory = parentDirectory.substring(0, parentDirectory.lastIndexOf("/") + 1); //$NON-NLS-1$
                out.write("<script type=\"text/javascript\">mgnlDialogDAVShow('" //$NON-NLS-1$
                    + this.getName() + "_upDiv',true);</script>"); //$NON-NLS-1$
            }
            else {
                // home
                out.write("<script type=\"text/javascript\">mgnlDialogDAVShow('" //$NON-NLS-1$
                    + this.getName() + "_upDiv',false);</script>"); //$NON-NLS-1$
            }
            out.write("</head>"); //$NON-NLS-1$
            out.write("<body>"); //$NON-NLS-1$
            out.write("<table cellpadding=\"3\" cellspacing=\"0\" border=\"0\" width=\"100%\">"); //$NON-NLS-1$
            out.write("<form name=\"mgnlDialogDAVBrowseForm\" method=\"post\">"); //$NON-NLS-1$
            out.write(new Hidden("subDirectory", StringUtils.EMPTY, false).getHtml()); //$NON-NLS-1$
            out.write(new Hidden("parentDirectory", parentDirectory, false).getHtml()); //$NON-NLS-1$
            out.write(new Hidden("homeDirectory", StringUtils.EMPTY, false).getHtml()); //$NON-NLS-1$
            out.write(new Hidden("refreshDirectory", this.getSubDirectory(), false).getHtml()); //$NON-NLS-1$
            out.write(new Hidden("selectedValue", this.getValue(), false).getHtml()); //$NON-NLS-1$
            out.write(new Hidden(SESSION_ATTRIBUTENAME_DIALOGOBJECT, this
                .getConfigValue(SESSION_ATTRIBUTENAME_DIALOGOBJECT), false).getHtml());
            out.write("</form>"); //$NON-NLS-1$
            List fileListAS = new ArrayList();
            List dirListAS = new ArrayList();
            List selfAS = new ArrayList();
            while (fileList.hasMoreElements()) {
                XMLResponseMethodBase.Response response = (XMLResponseMethodBase.Response) fileList.nextElement();
                Map properties = this.getDAVProperties(response);
                if (StringUtils.isEmpty((String) properties.get("name"))) { //$NON-NLS-1$
                    continue;
                }
                String name = (String) properties.get("name"); //$NON-NLS-1$
                if (this.getSubDirectory().equals(name) || this.getSubDirectory().equals(name + "/")) { //$NON-NLS-1$
                    // self directory
                    properties.put("isSelf", "true"); //$NON-NLS-1$ //$NON-NLS-2$
                }
                if (name.indexOf("/") != -1) { //$NON-NLS-1$
                    // when path contains spaces, the name contains the entire path -> get rid of path
                    name = name.substring(name.lastIndexOf("/") + 1); //$NON-NLS-1$
                }
                if (name.startsWith("._")) { //$NON-NLS-1$
                    continue;
                }
                if (name.startsWith(".") //$NON-NLS-1$
                    && !this.getConfigValue("showHiddenFiles", "false").equals("true")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    continue;
                }
                properties.put("name", name); //$NON-NLS-1$
                String displayType = (String) properties.get("displayType"); //$NON-NLS-1$
                if (properties.get("isSelf") != null) { //$NON-NLS-1$
                    selfAS.add(properties);
                }
                else if (displayType.equals("folder")) { //$NON-NLS-1$
                    dirListAS.add(properties);
                }
                else {
                    fileListAS.add(properties);
                }
            }
            int i = 0;
            List parentAS = new ArrayList();
            if (!this.getDirectory().equals(dir)) {
                Map parentProp = new Hashtable();
                String name = StringUtils.EMPTY;
                if (StringUtils.isEmpty(parentDirectory)) {
                    name = "/"; //$NON-NLS-1$
                }
                else {
                    name = parentDirectory.substring(0, parentDirectory.length() - 1);
                    if (name.indexOf("/") != -1) { //$NON-NLS-1$
                        name = name.substring(0, name.lastIndexOf("/")); //$NON-NLS-1$
                    }
                }
                parentProp.put("name", name); //$NON-NLS-1$
                parentProp.put("isParent", "true"); //$NON-NLS-1$ //$NON-NLS-2$
                parentProp.put("href", parentDirectory); //$NON-NLS-1$
                parentProp.put("displayType", "folder"); //$NON-NLS-1$ //$NON-NLS-2$
                parentProp.put("sizeStringValue", StringUtils.EMPTY); //$NON-NLS-1$
                parentProp.put("sizeStringUnit", StringUtils.EMPTY); //$NON-NLS-1$
                parentProp.put("lastModifiedString", StringUtils.EMPTY); //$NON-NLS-1$
                parentAS.add(parentProp);
                i = drawHtmlList(out, parentAS, i);
            }
            i = drawHtmlList(out, selfAS, i);
            i = drawHtmlList(out, dirListAS, i);
            i = drawHtmlList(out, fileListAS, i);
            if (i == 1) {
                out.write("<tr><td colspan=\"3\"></td><td colspan=\"3\"><em>" //$NON-NLS-1$
                    + MessagesManager.get(this.getRequest(), "dialog.webdav.directoryIsEmpty") //$NON-NLS-1$
                    + "</em></td></tr>"); //$NON-NLS-1$
            }
            out.write("</table>"); //$NON-NLS-1$
            out.write("</body></html>"); //$NON-NLS-1$
        }
        catch (Exception e) {
            log.debug("Exception caught: " + e.getMessage(), e); //$NON-NLS-1$
        }
    }

    public int drawHtmlList(Writer out, List as, int i) throws IOException {

        boolean alt = (i % 2 == 0);

        // todo: better sorting
        Collections.sort(as, new DialogWebDAVComparator());
        Iterator it = as.iterator();
        while (it.hasNext()) {
            Map properties = (Hashtable) it.next();
            String displayType = (String) properties.get("displayType"); //$NON-NLS-1$
            String name = (String) properties.get("name"); //$NON-NLS-1$
            if (!alt) {
                out.write("<tr>"); //$NON-NLS-1$
            }
            else {
                out.write("<tr class=" + CssConstants.CSSCLASS_BGALT + ">"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            alt = !alt;
            out.write("<td></td>"); //$NON-NLS-1$
            out.write("<td>"); //$NON-NLS-1$
            if (properties.get("isParent") == null //$NON-NLS-1$
                && (displayType.indexOf("folder") == -1 || this //$NON-NLS-1$
                    .getConfigValue("allowDirectorySelection") //$NON-NLS-1$
                    .equals("true"))) { //$NON-NLS-1$
                String lastModified = StringUtils.EMPTY;
                if (properties.get("lastModified") != null) { //$NON-NLS-1$
                    lastModified = ((String) properties.get("lastModified")) //$NON-NLS-1$
                        .replaceAll(" ", "%20"); //$NON-NLS-1$ //$NON-NLS-2$
                }
                out.write("<input type=\"radio\" name=\"" + this.getName() + "_radio\""); //$NON-NLS-1$ //$NON-NLS-2$
                out.write(" onclick=mgnlDialogDAVSelect(\"" //$NON-NLS-1$
                    + this.getName() + "\",\"" //$NON-NLS-1$
                    + name + "\",\"" //$NON-NLS-1$
                    + i + "\",\"" //$NON-NLS-1$
                    + (String) properties.get("size") //$NON-NLS-1$
                    + "\",\"" //$NON-NLS-1$
                    + lastModified + "\");"); //$NON-NLS-1$
                // if (this.getValue().equals(this.getSubDirectory()+name) ||
                // this.getValue().equals(this.getSubDirectory()+name+"/")) out.write(" checked");
                boolean checked = false;
                if (properties.get("isSelf") != null) { //$NON-NLS-1$
                    if (this.getValue().equals(this.getSubDirectory())) {
                        checked = true;
                    }
                }
                else {
                    if (this.getValue().equals(this.getSubDirectory() + name)
                        || this.getValue().equals(this.getSubDirectory() + name + "/")) { //$NON-NLS-1$
                        checked = true;
                    }
                }
                if (checked) {
                    out.write(MessagesManager.get(this.getRequest(), "dialog.webdav.checked")); //$NON-NLS-1$
                }
                out.write(" />"); //$NON-NLS-1$
            }
            out.write("</td>"); //$NON-NLS-1$
            // out.write("<td>"+this.getValue()+"..."+this.getSubDirectory()+"..."+name+"</td>");
            String idHidden = this.getName() + "_" + i + "_hidden"; //$NON-NLS-1$ //$NON-NLS-2$
            String idIcon = this.getName() + "_" + i + "_icon"; //$NON-NLS-1$ //$NON-NLS-2$
            i++;
            String iconPath;
            if (displayType.equals("folder")) { //$NON-NLS-1$
                iconPath = ICONS_PATH + ICONS_FOLDER;
            }
            else {
                iconPath = this.getIconPath(name);
            }
            out.write("<td>"); //$NON-NLS-1$
            out.write("<img src=\"" //$NON-NLS-1$
                + this.getRequest().getContextPath() + iconPath + "\" border=\"0\" id=\"" //$NON-NLS-1$
                + idIcon + "\">"); //$NON-NLS-1$
            out.write("</td>"); //$NON-NLS-1$
            out.write("<td width=\"100%\">"); //$NON-NLS-1$
            if (displayType.indexOf("folder") == 0) { //$NON-NLS-1$
                if (properties.get("isSelf") != null) { //$NON-NLS-1$
                    out.write(this.getHtmlDecodeURI("<b><i>.&nbsp;&nbsp;" + name + "</i></b>")); //$NON-NLS-1$ //$NON-NLS-2$
                }
                else {
                    if (properties.get("isParent") != null) { //$NON-NLS-1$
                        name = "<b><i>..&nbsp;&nbsp;" + name + "</i></b>"; //$NON-NLS-1$ //$NON-NLS-2$
                    }
                    out.write("<a href=\"javascript:mgnlDialogDAVBrowse('','" + idHidden + "');\">"); //$NON-NLS-1$ //$NON-NLS-2$
                    out.write(this.getHtmlDecodeURI(name));
                    out.write("</a>"); //$NON-NLS-1$
                }
            }
            else {
                out.write(this.getHtmlDecodeURI(name));
                out.write("[<a href=\"" //$NON-NLS-1$
                    + this.getProtocol() + "://" //$NON-NLS-1$
                    + this.getHost() + ":" //$NON-NLS-1$
                    + this.getPort() + this.getDirectory() + (String) properties.get("href") //$NON-NLS-1$
                    + "\" target=\"blank\">view</a>]"); //$NON-NLS-1$
            }
            out.write(new Hidden(idHidden, (String) properties.get("href"), false).getHtml()); //$NON-NLS-1$
            out.write("</td>"); //$NON-NLS-1$
            out.write("<td style=\"text-align:right;\">" + (String) properties.get("sizeStringValue") + "</td>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            out.write("<td>" + (String) properties.get("sizeStringUnit") + "</td>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            out.write("<td></td>"); //$NON-NLS-1$
            out.write("<td style='white-space:nowrap;'>" + (String) properties.get("lastModifiedString") + "</td>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            out.write("<td></td>"); //$NON-NLS-1$
            out.write("</tr>"); //$NON-NLS-1$
        }

        return i;
    }

    public Map getDAVProperties(XMLResponseMethodBase.Response response) {
        Map properties = new Hashtable();
        /* get the short name */
        Enumeration props = response.getProperties();
        String href = response.getHref();
        properties.put("href", href.replaceFirst(this.getDirectory(), StringUtils.EMPTY)); //$NON-NLS-1$
        while (props.hasMoreElements()) {
            Property property = (Property) props.nextElement();
            if (property.getLocalName().equalsIgnoreCase("getcontenttype")) { //$NON-NLS-1$
                if (property.getPropertyAsString().equalsIgnoreCase("httpd/unix-directory")) { //$NON-NLS-1$
                    properties.put("displayType", "folder"); //$NON-NLS-1$ //$NON-NLS-2$
                    if (href.length() > this.getDirectory().length()) {
                        String name = href.replaceFirst(this.getDirectory(), StringUtils.EMPTY);
                        name = name.substring(0, name.length() - 1);
                        name = name.substring(0, name.length());
                        properties.put("name", name); //$NON-NLS-1$
                    }
                }
            }
            else if (property.getLocalName().equalsIgnoreCase("getcontentlength")) { //$NON-NLS-1$
                properties.put("size", property.getPropertyAsString()); //$NON-NLS-1$
            }
            else if (property.getLocalName().equalsIgnoreCase("getlastmodified")) { //$NON-NLS-1$
                properties.put("lastModifiedString", //$NON-NLS-1$
                    this.getFormattedDate(property.getPropertyAsString(), "MMM dd yyyy")); //$NON-NLS-1$
                properties.put("lastModified", //$NON-NLS-1$
                    this.getFormattedDate(property.getPropertyAsString(), "yyyy-MM-dd, HH:mm:ss")); //$NON-NLS-1$
            }
        }
        if (properties.get("name") == null) { //$NON-NLS-1$
            if (href.length() > this.getDirectory().length()) {
                String name = href.replaceFirst(this.getDirectory(), StringUtils.EMPTY);
                properties.put("name", name); //$NON-NLS-1$
            }
            int index = href.lastIndexOf("."); //$NON-NLS-1$
            if (index > -1) {
                properties.put("displayType", (href.substring(index + 1)).toLowerCase()); //$NON-NLS-1$
            }
            else {
                properties.put("displayType", "general"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        if (properties.get("size") == null) { //$NON-NLS-1$
            properties.put("size", StringUtils.EMPTY); //$NON-NLS-1$
            properties.put("sizeStringValue", StringUtils.EMPTY); //$NON-NLS-1$
            properties.put("sizeStringUnit", StringUtils.EMPTY); //$NON-NLS-1$
        }
        else {
            String[] size = this.getFileSizeString((String) properties.get("size")).split(" "); //$NON-NLS-1$ //$NON-NLS-2$
            properties.put("sizeStringValue", size[0]); //$NON-NLS-1$
            properties.put("sizeStringUnit", size[1]); //$NON-NLS-1$
        }
        return properties;
    }

    public String getFileSizeString(String fileSize) {
        int bytes = (new Integer(fileSize)).intValue();
        int size = (bytes / 1024);
        if (size == 0) {
            return (bytes + " " + MessagesManager.get(this.getRequest(), "file.bytes")); //$NON-NLS-1$ //$NON-NLS-2$
        }
        else if (size >= 1024) {
            return ((size / 1024) + " " + MessagesManager.get(this.getRequest(), "file.mb")); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return (size + " " + MessagesManager.get(this.getRequest(), "file.kb")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public String getFormattedDate(String date, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);

        try {
            Date x = DateFormat.getDateTimeInstance().parse(date);
            return sdf.format(x);
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
            return date;
        }
    }

    private String getHtmlSessionAttributeRemoveControl() {
        return new Hidden(SESSION_ATTRIBUTENAME_DIALOGOBJECT_REMOVE, this
            .getConfigValue(SESSION_ATTRIBUTENAME_DIALOGOBJECT), false).getHtml();
    }
}
