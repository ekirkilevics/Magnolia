package info.magnolia.module.adminInterface;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.config.ItemType;
import info.magnolia.cms.gui.control.Tree;
import info.magnolia.cms.gui.dialog.DialogSpacer;
import info.magnolia.cms.gui.misc.Sources;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;


/**
 * Main admin interface servlet. Generates the content for the main admincentral iframe.
 * @author Fabrizio Giustina
 * @version $Id: $
 */
public class AdminInterfaceServlet extends HttpServlet {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Map with repository name/handler class for admin tree. When this servlet will receive a call with a parameter
     * <code>repository</code>, the corresponding handler will be used top display the admin tree.
     */
    private final Map TREE_HANDLERS = new HashMap();

    /**
     * Loads TREE_HANDLERS with default values.
     * @see javax.servlet.Servlet#init(javax.servlet.ServletConfig)
     */
    public void init(ServletConfig servletConfig) throws ServletException {
        // @todo dinamically add new handlers for admin interface tree?
        TREE_HANDLERS.put(ContentRepository.WEBSITE, new AdminTreeWebsite());
        TREE_HANDLERS.put(ContentRepository.USERS, new AdminTreeUsers());
        TREE_HANDLERS.put(ContentRepository.USER_ROLES, new AdminTreeRoles());
        TREE_HANDLERS.put(ContentRepository.CONFIG, new AdminTreeConfig());
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest,HttpServletResponse)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
        IOException {
        doGet(request, response);
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest, HttpServletResponse)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // http://issues.apache.org/bugzilla/show_bug.cgi?id=22666
        //
        // 1. The Coyote HTTP/1.1 connector has a useBodyEncodingForURI attribute which
        // if set to true will use the request body encoding to decode the URI query
        // parameters.
        // - The default value is true for TC4 (breaks spec but gives consistent
        // behaviour across TC4 versions)
        // - The default value is false for TC5 (spec compliant but there may be
        // migration issues for some apps)
        // 2. The Coyote HTTP/1.1 connector has a URIEncoding attribute which defaults to
        // ISO-8859-1.
        // 3. The parameters class (o.a.t.u.http.Parameters) has a QueryStringEncoding
        // field which defaults to the URIEncoding. It must be set before the parameters
        // are parsed to have an effect.
        //
        // Things to note regarding the servlet API:
        // 1. HttpServletRequest.setCharacterEncoding() normally only applies to the
        // request body NOT the URI.
        // 2. HttpServletRequest.getPathInfo() is decoded by the web container.
        // 3. HttpServletRequest.getRequestURI() is not decoded by container.
        //
        // Other tips:
        // 1. Use POST with forms to return parameters as the parameters are then part of
        // the request body.

        StringBuffer html = new StringBuffer(500);
        boolean proceed = true;

        request.setCharacterEncoding("UTF-8");

        String repository = request.getParameter("repository");
        if (StringUtils.isEmpty(repository)) {
            repository = ContentRepository.WEBSITE;
        }

        String path = request.getParameter("path");
        if (StringUtils.isEmpty(path)) {
            path = "/";
        }

        String pathOpen = request.getParameter("pathOpen");
        String pathSelected = request.getParameter("pathSelected");

        boolean create = false;
        String createItemType = ItemType.NT_NODEDATA;
        if (request.getParameter("createItemType") != null) {
            create = true;
            createItemType = request.getParameter("createItemType");
        }

        String actionStr = request.getParameter("treeAction");
        if (actionStr != null) {
            int action = Integer.parseInt(actionStr);
            Tree tree = new Tree(repository, request);

            if (action == Tree.ACTION_COPY || action == Tree.ACTION_MOVE) {

                String pathClipboard = request.getParameter("pathClipboard");
                int pasteType = Integer.parseInt(request.getParameter("pasteType"));

                String newPath = tree.pasteNode(pathClipboard, pathSelected, pasteType, action);

                // pass new path to tree.js for selecting the newly created node
                // NOTE: tree.js checks for this pattern; adapt it there, if any changes are made here
                html.append("<input type=\"hidden\" id=\"mgnlSelectNode\" value=\"" + newPath + "\" />");

                if (pasteType == Tree.PASTETYPE_SUB) {
                    pathOpen = pathSelected;
                }
                else {
                    // open parent path of destination path
                    pathOpen = pathSelected.substring(0, pathSelected.lastIndexOf("/"));
                }

                pathSelected = null;
            }
            else if (action == Tree.ACTION_ACTIVATE) {
                boolean recursive = (request.getParameter("recursive") != null);
                tree.activateNode(pathSelected, recursive);
            }
            else if (action == Tree.ACTION_DEACTIVATE) {
                tree.deActivateNode(pathSelected);
            }

        }

        String deleteNode = request.getParameter("deleteNode");
        String saveName = request.getParameter("saveName");

        // value to save is a node data's value (config admin)
        boolean isNodeDataValue = "true".equals(request.getParameter("isNodeDataValue"));

        // value to save is a node data's type (config admin)
        boolean isNodeDataType = "true".equals(request.getParameter("isNodeDataType"));

        if (saveName != null || isNodeDataValue || isNodeDataType) {

            String value = StringUtils.defaultString(request.getParameter("saveValue"));

            // value to save is a content's meta information
            boolean isMeta = "true".equals(request.getParameter("isMeta"));
            // value to save is a label (name of page, content node or node data)
            boolean isLabel = "true".equals(request.getParameter("isLabel"));

            Tree tree = new Tree(repository, request);
            if (isNodeDataValue || isNodeDataType) {
                tree.setPath(StringUtils.substringAfterLast(path, "/"));
                saveName = StringUtils.substringAfterLast(path, "/");
            }
            else {
                tree.setPath(path);
            }

            if (isLabel) {
                html.append(tree.renameNode(value));
            }
            else if (isNodeDataType) {
                int type = Integer.valueOf(value).intValue();
                html.append(tree.saveNodeDataType(saveName, type));
            }
            else {
                html.append(tree.saveNodeData(saveName, value, isMeta));
            }
            proceed = false;
        }

        if (deleteNode != null) {
            Tree tree = new Tree(repository, request);
            tree.deleteNode(path, deleteNode);
        }

        if (proceed) {
            String mode = StringUtils.defaultString(request.getParameter("treeMode"));
            boolean snippetMode = mode.equals("snippet");

            if (!snippetMode) {
                html.append("<html><head>");
                html.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>");
                html.append(new Sources(request.getContextPath()).getHtmlJs());
                html.append(new Sources(request.getContextPath()).getHtmlCss());
                html.append("</head>");
                html.append("<body class=\"mgnlBgDark\" onload=\"mgnlTree.resizeOnload();\" >");
                html.append(new DialogSpacer().getHtml(20));
            }

            if (TREE_HANDLERS.containsKey(repository)) {

                Tree tree = new Tree(repository, request);
                tree.setJavascriptTree("mgnlTree");
                tree.setSnippetMode(snippetMode);
                tree.setHeight(50);

                ((AdminTree) TREE_HANDLERS.get(repository)).configureTree(
                    tree,
                    request,
                    path,
                    pathOpen,
                    pathSelected,
                    create,
                    createItemType);

                if (!snippetMode) {
                    html.append("<div id=" + tree.getJavascriptTree() + "_DivSuper style=\"display:block;\">");
                }
                html.append(tree.getHtml());
                if (!snippetMode) {
                    html.append("</div>");
                }

            }

            if (!snippetMode) {
                html.append("</body></html>");
            }
        }

        PrintWriter out = response.getWriter();
        response.setContentType("text/html; charset=UTF-8");

        String htmlString = html.toString();
        response.setContentLength(htmlString.getBytes().length);
        out.write(htmlString);

    }

}
