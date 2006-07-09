/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.module.dms.gui;

import info.magnolia.cms.beans.runtime.Document;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.gui.control.SelectOption;
import info.magnolia.cms.gui.dialog.Dialog;
import info.magnolia.cms.gui.dialog.DialogBox;
import info.magnolia.cms.gui.dialog.DialogFactory;
import info.magnolia.cms.gui.dialog.DialogHidden;
import info.magnolia.cms.gui.dialog.DialogSelect;
import info.magnolia.cms.gui.dialog.DialogTab;
import info.magnolia.cms.gui.misc.CssConstants;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.cms.util.RequestFormUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.dms.DMSModule;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Enumeration;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;

/**
 * Upload a zip file
 * @author philipp
 */
public class ZipUploadView {

    private static Logger log = Logger.getLogger(ZipUploadView.class);

    /**
     * Easy to handle file upload
     */
    private RequestFormUtil form;

    private boolean success = false;

    private String path;

    /**
     * Not every zip file is encoded the same way
     */
    private String encoding = "CP437";

    private HierarchyManager hm;

    /**
     * Error messages
     */
    private String msg = "";

    public ZipUploadView() {
    }

    /**
     * Process.
     */
    public void process(HttpServletRequest request) {
        MgnlContext.initAsWebContext(request);
        this.form = new RequestFormUtil(request);
        this.setPath(form.getParameter("path"));
        this.setEncoding(form.getParameter("encoding"));
        this.hm = MgnlContext.getHierarchyManager(DMSModule.getInstance().getRepository());
        success = _process();
    }

    private boolean _process() {
        if (form.getFrom() == null) {
            return false;
        }

        Document doc = form.getDocument("file");
        if (doc != null) {
            try {
                File tmpFile = File.createTempFile("dms_zipupload", ".zip");
                FileOutputStream tmpStream = new FileOutputStream(tmpFile);
                IOUtils.copy(doc.getStream(), tmpStream);
                ZipFile zip = new ZipFile(tmpFile, this.getEncoding());

                for (Enumeration em = zip.getEntries(); em.hasMoreElements();) {
                    ZipEntry entry = (ZipEntry) em.nextElement();
                    processEntry(zip, entry);
                }

                // save all those changes
                hm.save();

                tmpFile.delete();
            }
            catch (Exception e) {
                log.error("can't upload zip file", e);
                msg = e.toString();
                return false;
            }
            return true;
        }

        return false;
    }

    /**
     * Checks if this entry is processable
     * @param zip
     * @param entry
     * @throws AccessDeniedException
     * @throws PathNotFoundException
     * @throws RepositoryException
     * @throws IOException
     */
    private void processEntry(ZipFile zip, ZipEntry entry) throws AccessDeniedException, PathNotFoundException,
        RepositoryException, IOException {
        if (entry.getName().startsWith("__MACOSX")) {
            // ignore
        }

        else if (entry.getName().endsWith(".DS_Store")) {
            // ignore
        }

        else if (entry.isDirectory()) {
            // ignore
        }

        else {
            processFile(zip, entry);
        }
    }

    /**
     * Process the file
     * @param zip
     * @param entry
     * @throws AccessDeniedException
     * @throws PathNotFoundException
     * @throws RepositoryException
     * @throws IOException
     */
    private void processFile(ZipFile zip, ZipEntry entry) throws AccessDeniedException, PathNotFoundException,
        RepositoryException, IOException {

        String path = entry.getName();
        if (StringUtils.isNotEmpty(this.path) && !this.path.equals("/")) {
            path = this.path + "/" + path;
        }
        String label = path;
        if (StringUtils.contains(label, "/")) {
            label = StringUtils.substringAfterLast(label, "/");
        }

        String extension = StringUtils.substringAfterLast(label, ".");
        if (StringUtils.contains(path, "/")) {
            path = StringUtils.substringBeforeLast(path, "/");
        }
        else {
            path = "/";
        }

        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        String utf8Path = path;

        // make proper name, the path was already created
        path = StringUtils.replace(path, "/", "________backslash________");
        path = Path.getValidatedLabel(path);
        path = StringUtils.replace(path, "________backslash________", "/");

        createPath(hm, path, utf8Path, ItemType.CONTENT);

        label = StringUtils.substringBeforeLast(label, ".");
        String name = Path.getValidatedLabel(label);
        if (name.matches("^-*$")) {
            name = "file";
        }
        name = Path.getUniqueLabel(hm, path, name);

        Content node = hm.createContent(path, name, ItemType.CONTENTNODE.getSystemName());
        InputStream stream = zip.getInputStream(entry);
        long size = entry.getSize();

        log.info("import:" + node.getHandle() + " free memory: " + Runtime.getRuntime().freeMemory()/1024 + "k");

        try{
            info.magnolia.module.dms.beans.Document doc = new info.magnolia.module.dms.beans.Document(node);
            // set all the information
            doc.setFile(label, extension, stream, size);
            // save it
            node.save();
            // add version (first)
            doc.addVersion();
        }
        catch(Throwable e){
            // FIXME
            // this is done for debuging reasons: we continou the execution to see if it heals
            log.error("can't import file", e);
        }
    }

    /**
     * Create the path to the node
     * @param hm
     * @param path the url enabled path
     * @param utf8Path the nice looking path
     * @param type
     * @throws AccessDeniedException
     * @throws PathNotFoundException
     * @throws RepositoryException
     * @throws IOException
     */
    public static void createPath(HierarchyManager hm, String path, String utf8Path, ItemType type)
        throws AccessDeniedException, PathNotFoundException, RepositoryException, IOException {

        if (path.equals("/")) {
            return;
        }

        // remove leading /
        path = StringUtils.removeStart(path, "/");
        utf8Path = StringUtils.removeStart(utf8Path, "/");

        String[] names = path.split("/"); //$NON-NLS-1$
        String[] niceNames = utf8Path.split("/"); //$NON-NLS-1$

        Content node = hm.getRoot();
        for (int i = 0; i < names.length; i++) {
            String name = names[i];

            if (node.hasContent(name)) {
                node = node.getContent(name);
            }
            else {
                try {
                    node = node.createContent(name, type);
                }
                catch (Exception e) {
                    log.error("can't create path", e);
                }
                NodeDataUtil.getOrCreate(node, "title").setValue(niceNames[i]);
                NodeDataUtil.getOrCreate(node, "type").setValue("folder");
            }
        }
    }

    public boolean isSuccess() {
        return success;
    }

    /**
     * @return Returns the path.
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path The path to set.
     */
    private void setPath(String path) {
        this.path = path;
    }

    /**
     * @return Returns the msg.
     */
    public String getMsg() {
        return msg;
    }

    /**
     * @param msg The msg to set.
     */
    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getEncoding() {
        return encoding;
    }

    private void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public void render(HttpServletRequest request, HttpServletResponse response) throws RepositoryException,
        IOException {
        Dialog dialog = DialogFactory.getDialogInstance(request, response, null, null);
        dialog.setConfig("width", 500);
        dialog.setConfig("height", 300);

        DialogTab tab = dialog.addTab("Zip Upload");
        DialogBox file = new DialogBox() {

            public void drawHtml(Writer out) throws IOException {
                this.drawHtmlPre(out);
                out.write("<input type=\"file\" class=\""
                    + CssConstants.CSSCLASS_EDIT
                    + "\" name=\""
                    + this.getName()
                    + "\"/>");
                this.drawHtmlPost(out);
            }
        };
        file.setName("file");
        file.setLabel("Zip File");
        tab.addSub(file);

        DialogHidden path = DialogFactory.getDialogHiddenInstance(request, response, null, null);
        path.setName("path");
        path.setValue(this.getPath());
        tab.addSub(path);

        DialogSelect enc = DialogFactory.getDialogSelectInstance(request, response, null, null);
        enc.setLabel("Encoding");
        enc.setName("encoding");
        enc
            .setDescription("Not every zip tool uses the same encoding for the filenames. If you don't know the encoding of your zip file it is probably CP437.");
        enc.addOption(new SelectOption("Windows (CP437)", "CP437"));
        enc.addOption(new SelectOption("Mac (UTF-8)", "UTF-8"));
        tab.addSub(enc);

        dialog.drawHtml(response.getWriter());
    }
}
