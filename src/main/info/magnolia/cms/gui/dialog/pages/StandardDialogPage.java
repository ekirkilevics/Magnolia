package info.magnolia.cms.gui.dialog.pages;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.config.Paragraph;
import info.magnolia.cms.beans.runtime.MultipartForm;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.gui.control.Button;
import info.magnolia.cms.gui.control.ControlSuper;
import info.magnolia.cms.gui.control.Save;
import info.magnolia.cms.gui.dialog.DialogBox;
import info.magnolia.cms.gui.dialog.DialogButtonSet;
import info.magnolia.cms.gui.dialog.DialogDialog;
import info.magnolia.cms.gui.dialog.DialogFactory;
import info.magnolia.cms.gui.dialog.DialogHidden;
import info.magnolia.cms.gui.dialog.DialogStatic;
import info.magnolia.cms.gui.dialog.DialogTab;
import info.magnolia.cms.gui.misc.Sources;
import info.magnolia.cms.i18n.ContextMessages;
import info.magnolia.cms.security.SessionAccessControl;
import info.magnolia.cms.servlets.BasePageServlet;
import info.magnolia.cms.util.Resource;

import java.io.IOException;
import java.io.PrintWriter;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;


/**
 * @author Fabrizio Giustina
 * @version $Id: $
 */
public class StandardDialogPage extends BasePageServlet {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(StandardDialogPage.class);

    /**
     * @see info.magnolia.cms.servlets.BasePageServlet#draw(HttpServletRequest, HttpServletResponse)
     */
    public void draw(HttpServletRequest request, HttpServletResponse response) throws IOException, RepositoryException {
        info.magnolia.cms.i18n.Messages msgs = ContextMessages.getInstanceSavely(request);

        PrintWriter out = response.getWriter();

        MultipartForm form = Resource.getPostedForm(request);
        boolean drawDialog = true;

        String path = "";
        String nodeCollectionName = "";
        String nodeName = "";
        String paragraph = "";
        String richE = "";
        String richEPaste = "";
        String repository = ContentRepository.WEBSITE;

        if (form != null) {
            if (form.getParameter("mgnlParagraphSelected") == null) {
                Save control = new Save(form, request);
                control.save();
                out.println("<html>");
                out.println(new Sources(request.getContextPath()).getHtmlJs());
                out.println("<script type=\"text/javascript\">");
                out.println("mgnlDialogReloadOpener();");
                out.println("window.close();");
                out.println("</script></html>");
                drawDialog = false;
            }
            else {
                path = form.getParameter("mgnlPath");
                nodeCollectionName = form.getParameter("mgnlNodeCollection");
                nodeName = form.getParameter("mgnlNode");
                paragraph = form.getParameter("mgnlParagraph");
                richE = form.getParameter("mgnlRichE");
                richEPaste = form.getParameter("mgnlRichEPaste");
                repository = form.getParameter("mgnlRepository");
            }
        }
        else {
            path = request.getParameter("mgnlPath");
            nodeCollectionName = request.getParameter("mgnlNodeCollection");
            nodeName = request.getParameter("mgnlNode");
            paragraph = request.getParameter("mgnlParagraph");
            richE = request.getParameter("mgnlRichE");
            richEPaste = request.getParameter("mgnlRichEPaste");
            repository = request.getParameter("mgnlRepository");
        }

        if (drawDialog) {
            HierarchyManager hm = null;
            try {
                hm = SessionAccessControl.getHierarchyManager(request, repository);
            }
            catch (Exception e) {
                log.info("Exception caught: " + e.getMessage(), e);
            }
            if (paragraph.indexOf(",") == -1) {
                Content configNode = null;
                Content websiteNode = null;
                Paragraph para = Paragraph.getInfo(paragraph);

                if (para == null) {
                    out.println(msgs.get("dialog.paragraph.paragraphNotAvailable", paragraph));
                    return;
                }

                configNode = para.getDialogContent();

                try {
                    Content websiteContent = hm.getPage(path);
                    if (nodeName == null || nodeName.equals("")) {
                        websiteNode = websiteContent;
                    }
                    else {
                        if (nodeCollectionName == null || nodeCollectionName.equals("")) {
                            websiteNode = websiteContent.getContentNode(nodeName);

                        }
                        else {
                            websiteNode = websiteContent.getContentNode(nodeCollectionName).getContentNode(nodeName);

                        }
                    }
                }
                catch (RepositoryException re) {
                    // content does not exist yet

                }
                DialogDialog dialog = DialogFactory.getDialogDialogInstance(request, response, websiteNode, configNode);

                dialog.setConfig("path", path);
                dialog.setConfig("nodeCollection", nodeCollectionName);
                dialog.setConfig("node", nodeName);
                dialog.setConfig("paragraph", paragraph);
                dialog.setConfig("richE", richE);
                dialog.setConfig("richEPaste", richEPaste);
                dialog.setConfig("repository", repository);
                dialog.drawHtml(out);

            }
            else {
                // multiple paragraphs: show selection dialog
                DialogDialog dialog = DialogFactory.getDialogDialogInstance(request, response, null, null);

                dialog.setConfig("path", path);
                dialog.setConfig("nodeCollection", nodeCollectionName);
                dialog.setConfig("node", nodeName);
                dialog.setConfig("paragraph", paragraph);
                dialog.setConfig("richE", richE);
                dialog.setConfig("richEPaste", richEPaste);
                dialog.setConfig("repository", repository);

                dialog.setLabel(msgs.get("dialog.paragraph.createNew"));
                dialog.setConfig("saveLabel", msgs.get("buttons.ok"));

                DialogHidden h1 = DialogFactory.getDialogHiddenInstance(request, response, null, null);
                h1.setName("mgnlParagraphSelected");
                h1.setValue("true");
                h1.setConfig("saveInfo", "false");
                dialog.addSub(h1);

                DialogTab tab = dialog.addTab();

                DialogStatic c0 = DialogFactory.getDialogStaticInstance(request, response, null, null);

                c0.setConfig("line", "false");
                c0.setValue(msgs.get("dialog.paragraph.select"));
                c0.setBoxType((DialogBox.BOXTYPE_1COL));
                tab.addSub(c0);

                DialogButtonSet c1 = DialogFactory.getDialogButtonSetInstance(request, response, null, null);
                c1.setName("mgnlParagraph");
                c1.setButtonType(ControlSuper.BUTTONTYPE_RADIO);
                c1.setBoxType(DialogBox.BOXTYPE_1COL);
                c1.setConfig("saveInfo", "false");
                c1.setConfig("width", "100%");

                String[] pars = paragraph.split(",");
                for (int i = 0; i < pars.length; i++) {
                    try {
                        Paragraph paragraphInfo = Paragraph.getInfo(pars[i]);
                        Button button = new Button(c1.getName(), paragraphInfo.getName());
                        StringBuffer label = new StringBuffer();
                        label.append("<strong>" + msgs.getWithDefault(paragraphInfo.getTitle(),paragraphInfo.getTitle()) + "</strong><br />");
                        label.append(msgs.getWithDefault(paragraphInfo.getDescription(),paragraphInfo.getDescription()));
                        label.append("<br /><br />");
                        button.setLabel(label.toString());
                        button.setOnclick("document.mgnlFormMain.submit();");
                        c1.addOption(button);
                    }
                    catch (Exception e) {
                        // paragraph definition does not exist
                        log.warn("Exception caught: " + e.getMessage(), e);
                    }
                }

                tab.addSub(c1);
                dialog.drawHtml(out);
            }
        }

    }

}