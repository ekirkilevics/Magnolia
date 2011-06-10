/**
 * This file Copyright (c) 2011 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.ui.admincentral.dialog.field;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Button;
import info.magnolia.cms.beans.config.URI2RepositoryManager;
import info.magnolia.cms.beans.runtime.FileProperties;
import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.cms.util.PathUtil;
import info.magnolia.jcr.util.BinaryInFile;
import info.magnolia.ui.admincentral.dialog.view.DialogView;
import info.magnolia.ui.framework.editor.NodeEditor;
import info.magnolia.ui.model.dialog.definition.DialogDefinition;
import info.magnolia.ui.model.dialog.definition.FileUploadFieldDefinition;
import info.magnolia.ui.model.dialog.definition.TabDefinition;

/**
 * Dialog field for uploading files.
 *
 * TODO: should support width
 * TODO: should support SWF in its preview
 * TODO: preview should have an icon mode
 *
 * @version $Id$
 */
public class DialogFileUploadField extends AbstractDialogField implements NodeEditor {

    private FileUploadView uploadView;
    private FileUploadFieldDefinition fieldDefinition;
    private List imageExtensions = new ArrayList();
    private boolean removed = false;

    public DialogFileUploadField(DialogDefinition dialogDefinition, TabDefinition tabDefinition, FileUploadFieldDefinition fieldDefinition, DialogView.Presenter presenter) {
        super(dialogDefinition, tabDefinition, fieldDefinition, presenter);
        uploadView = new FileUploadView();
        this.view.setComponent(uploadView);
        this.editor = this;
        this.fieldDefinition = fieldDefinition;
        initImageExtensions();
    }

    public List getImageExtensions() {
        return this.imageExtensions;
    }

    public void setImageExtensions(List l) {
        this.imageExtensions = l;
    }

    public void initImageExtensions() {
        this.getImageExtensions().add("jpg"); //$NON-NLS-1$
        this.getImageExtensions().add("jpeg"); //$NON-NLS-1$
        this.getImageExtensions().add("gif"); //$NON-NLS-1$
        this.getImageExtensions().add("png"); //$NON-NLS-1$
        this.getImageExtensions().add("bpm"); //$NON-NLS-1$
        this.getImageExtensions().add("swf"); //$NON-NLS-1$
    }

    @Override
    public void edit(final Node node) throws RepositoryException {
        final String name = fieldDefinition.getName();

        if (node.hasNode(name)) {
            Node binaryNode = node.getNode(name);

            final boolean preview = fieldDefinition.isPreview();
            final boolean extensionIsDisplayableImage = this.getImageExtensions().contains(binaryNode.getProperty(FileProperties.EXTENSION).getString().toLowerCase());
            final boolean showImage = extensionIsDisplayableImage && preview;

            if (showImage) {
                String uri = URI2RepositoryManager.getInstance().getURI(binaryNode.getSession().getWorkspace().getName(), binaryNode.getPath());
                // TODO we should handle the case of these properties being missing on the node better
                ImageSize imageSize = new ImageSize(binaryNode.getProperty(FileProperties.PROPERTY_WIDTH).getLong(), binaryNode.getProperty(FileProperties.PROPERTY_HEIGHT).getLong());
                uploadView.setThumbnail(imageSize, new ExternalResource(uri));
            }

            uploadView.addRemoveButton(new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent event) {
                    removed = true;
                }
            });
        }
    }

    @Override
    public void save(Node node) throws RepositoryException {
        File file = uploadView.getFile();
        if (file != null) {
            String fileName = PathUtil.stripExtension(uploadView.getFileName());
            String extension = PathUtil.getExtension(uploadView.getFileName());
            saveBinary(node, fieldDefinition.getName(), file, fileName, uploadView.getMimeType(), extension, file.length(), fieldDefinition.getNodeDataTemplate());
        } else if (removed) {
            if (node.hasNode(fieldDefinition.getName())) {
                node.getNode(fieldDefinition.getName()).remove();
            }
        }
    }

    public static void saveBinary(Node node, String name, File file, String fileName, String contentType, String extension, long size, String template) throws RepositoryException {

        Node child;
        if (node.hasNode(name)) {
            child = node.getNode(name);
        } else {
            child = node.addNode(name, MgnlNodeType.NT_RESOURCE);
        }

        child.setProperty(MgnlNodeType.JCR_DATA, new BinaryInFile(file));

        child.setProperty(FileProperties.PROPERTY_FILENAME, fileName);
        child.setProperty(FileProperties.PROPERTY_CONTENTTYPE, contentType);
        child.setProperty(FileProperties.PROPERTY_LASTMODIFIED, new GregorianCalendar(TimeZone.getDefault()));
        child.setProperty(FileProperties.PROPERTY_SIZE, Long.toString(size));
        child.setProperty(FileProperties.PROPERTY_EXTENSION, extension);
        child.setProperty(FileProperties.PROPERTY_TEMPLATE, template);

        ImageSize imageSize;
        try {
            imageSize = ImageSize.valueOf(file);
        } catch (FileNotFoundException e) {
            throw new RepositoryException(e);
        }
        child.setProperty(FileProperties.PROPERTY_WIDTH, Long.toString(imageSize.getWidth()));
        child.setProperty(FileProperties.PROPERTY_HEIGHT, Long.toString(imageSize.getHeight()));
    }
}
