<%
/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2004 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 * */
%><%@ page import="info.magnolia.cms.core.HierarchyManager,
				 info.magnolia.cms.util.Resource,
				 info.magnolia.cms.core.ContentNode,
				 javax.jcr.SimpleCredentials,
				 info.magnolia.cms.core.Content,
				 java.util.*,
				 javax.jcr.RepositoryException,
				 org.apache.webdav.lib.WebdavResource,
				 info.magnolia.cms.security.SessionAccessControl,
                 info.magnolia.cms.beans.config.Server,
                 info.magnolia.cms.beans.runtime.MultipartForm,
                 info.magnolia.cms.gui.control.Save,
                 info.magnolia.cms.beans.config.Paragraph,
                 info.magnolia.cms.gui.dialog.*,
                 info.magnolia.cms.gui.control.ButtonSet,
                 info.magnolia.cms.gui.control.Button,
				   info.magnolia.cms.beans.config.ContentRepository,
				   org.apache.log4j.Logger,
				   info.magnolia.cms.gui.misc.Sources"%>

<%if (!Server.isAdmin())
            response.sendRedirect("/");

        %>



<%
MultipartForm form = Resource.getPostedForm(request);
        boolean drawDialog = true;

        Logger log = Logger.getLogger("standard.jsp");

        String path = "";
        String nodeCollectionName = "";
        String nodeName = "";
        String paragraph = "";
        String richE = "";
        String richEPaste = "";
        String repository = ContentRepository.WEBSITE;

        if (form != null)
        {
            if (form.getParameter("mgnlParagraphSelected") == null)
            {
                Save control = new Save(form, request);
                control.save();
                out.println("<html>");
                out.println(new Sources(request.getContextPath()).getHtmlJs());
                out.println("<script>");
                out.println("mgnlDialogReloadOpener();");
                out.println("window.close();");
                out.println("</script></html>");
                drawDialog = false;
            }
            else
            {
                path = form.getParameter("mgnlPath");
                nodeCollectionName = form.getParameter("mgnlNodeCollection");
                nodeName = form.getParameter("mgnlNode");
                paragraph = form.getParameter("mgnlParagraph");
                richE = form.getParameter("mgnlRichE");
                richEPaste = form.getParameter("mgnlRichEPaste");
                repository = form.getParameter("mgnlRepository");
            }
        }
        else
        {
            path = request.getParameter("mgnlPath");
            nodeCollectionName = request.getParameter("mgnlNodeCollection");
            nodeName = request.getParameter("mgnlNode");
            paragraph = request.getParameter("mgnlParagraph");
            richE = request.getParameter("mgnlRichE");
            richEPaste = request.getParameter("mgnlRichEPaste");
            repository = request.getParameter("mgnlRepository");
        }

        if (drawDialog)
        {
            HierarchyManager hm = null;
            try
            {
                hm = SessionAccessControl.getHierarchyManager(request, repository);
            }
            catch (Exception e)
            {
            }
            if (paragraph.indexOf(",") == -1)
            {
                Content configNode = null;
                Content websiteNode = null;
                try
                {
                    configNode = Paragraph.getInfo(paragraph).getDialogContent();
                    try
                    {
                        Content websiteContent = hm.getPage(path);
                        if (nodeName == null || nodeName.equals(""))
                        {
                            websiteNode = websiteContent;
                        }
                        else
                        {
                            if (nodeCollectionName == null || nodeCollectionName.equals(""))
                            {
                                websiteNode = websiteContent.getContentNode(nodeName);
                            }
                            else
                            {
                                websiteNode = websiteContent
                                    .getContentNode(nodeCollectionName)
                                    .getContentNode(nodeName);
                            }
                        }
                    }
                    catch (RepositoryException re)
                    {
                        //content does not exist yet
                    }
                    DialogDialog dialog = new DialogDialog();
                    dialog.init(configNode, websiteNode, pageContext);
                    dialog.setConfig("path", path);
                    dialog.setConfig("nodeCollection", nodeCollectionName);
                    dialog.setConfig("node", nodeName);
                    dialog.setConfig("paragraph", paragraph);
                    dialog.setConfig("richE", richE);
                    dialog.setConfig("richEPaste", richEPaste);
                    dialog.setConfig("repository", repository);
                    dialog.drawHtml(out);
                }
                catch (Exception e)
                {
                    log.warn("Lack of configuration - not able to render dialog control", e);
                }
            }
            else
            {
                //multiple paragraphs: show selection dialog
                DialogDialog dialog = new DialogDialog();
                dialog.setRequest(request);
                dialog.setConfig("path", path);
                dialog.setConfig("nodeCollection", nodeCollectionName);
                dialog.setConfig("node", nodeName);
                dialog.setConfig("paragraph", paragraph);
                dialog.setConfig("richE", richE);
                dialog.setConfig("richEPaste", richEPaste);
                dialog.setConfig("repository", repository);

                dialog.setLabel("Create new paragraph");
                dialog.setConfig("saveLabel", "OK");

                DialogHidden h1 = new DialogHidden();
                h1.setName("mgnlParagraphSelected");
                h1.setValue("true");
                h1.setConfig("saveInfo", "false");
                dialog.addSub(h1);

                DialogTab tab = dialog.addTab();

                DialogStatic c0 = new DialogStatic();
                c0.setConfig("line", "false");
                c0.setValue("Select a type for the new paragraph");
                c0.setBoxType((DialogStatic.BOXTYPE_1COL));
                tab.addSub(c0);

                DialogButtonSet c1 = new DialogButtonSet();
                c1.setName("mgnlParagraph");
                c1.setButtonType(ButtonSet.BUTTONTYPE_RADIO);
                c1.setBoxType(DialogButtonSet.BOXTYPE_1COL);
                c1.setConfig("saveInfo", "false");
                c1.setConfig("width", "100%");
                //c1.setConfig("cols","2");

                String[] pars = paragraph.split(",");
                for (int i = 0; i < pars.length; i++)
                {
                    try
                    {
                        Paragraph paragraphInfo = Paragraph.getInfo(pars[i]);
                        Button button = new Button(c1.getName(), paragraphInfo.getName());
                        StringBuffer label = new StringBuffer();
                        label.append("<b>" + paragraphInfo.getTitle() + "</b><br>");
                        label.append(paragraphInfo.getDescription());
                        label.append("<br><br>");
                        button.setLabel(label.toString());
                        button.setOnclick("document.mgnlFormMain.submit();");
                        c1.addOption(button);
                    }
                    catch (Exception e)
                    {
                        //paragraph definition does not exist
                    }
                }

                tab.addSub(c1);

                dialog.drawHtml(out);
            }

        }

    %>

