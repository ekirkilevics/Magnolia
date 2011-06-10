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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.vaadin.terminal.FileResource;
import com.vaadin.terminal.Resource;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Upload;
import com.vaadin.ui.VerticalLayout;

/**
 * View for the file upload field.
 *
 * @version $Id$
 */
public class FileUploadView extends CustomComponent implements Upload.SucceededListener, Upload.FailedListener, Upload.Receiver {

    private Layout root;
    private Layout thumbnailLayout;

    private File file;
    private String fileName;
    private String mimeType;

    public FileUploadView() {
        root = new VerticalLayout();
        setCompositionRoot(root);

        Upload upload = new Upload(null, this);
        upload.addListener((Upload.SucceededListener) this);
        upload.addListener((Upload.FailedListener) this);
        root.addComponent(upload);

        thumbnailLayout = new VerticalLayout();
        root.addComponent(thumbnailLayout);
    }

    /**
     * Callback method to begin receiving the upload.
     */
    @Override
    public OutputStream receiveUpload(String fileName, String mimeType) {

        this.fileName = fileName;
        this.mimeType = mimeType;

        FileOutputStream fos;
        try {
            this.file = File.createTempFile("upload.", fileName);
            // Open the file for writing.
            fos = new FileOutputStream(file);
        } catch (java.io.FileNotFoundException e) {
            // Error while opening the file. Not reported here.
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return fos;
    }

    /**
     * This is called if the upload is finished.
     */
    @Override
    public void uploadSucceeded(Upload.SucceededEvent event) {
        // TODO: if the preview is switched off in the field definition we shouldn't do this
        try {
            setThumbnail(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    /**
     * This is called if the upload fails.
     */
    @Override
    public void uploadFailed(Upload.FailedEvent event) {
        reset();
    }

    private void reset() {
        thumbnailLayout.removeAllComponents();
        if (file != null) {
            file.delete();
        }
        this.file = null;
        this.fileName = null;
        this.mimeType = null;
    }

    public void setThumbnail(File file) throws FileNotFoundException {
        ImageSize imageSize = ImageSize.valueOf(file);
        setThumbnail(imageSize, new FileResource(file, getApplication()));
    }

    public void setThumbnail(ImageSize imageSize, Resource imageResource) {

        // TODO: should filter based on supported file extension just like in DialogFileUploadField

        // TODO: should support showing SWF in the preview

        thumbnailLayout.removeAllComponents();
        Embedded embedded = new Embedded("", imageResource);

        imageSize = imageSize.scaleToFitIfLarger(150, 150);

        embedded.setWidth(imageSize.getWidth() + "px");
        embedded.setHeight(imageSize.getHeight() + "px");
        thumbnailLayout.addComponent(embedded);
    }

    public File getFile() {
        return file;
    }

    public String getFileName() {
        return fileName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void addRemoveButton(final Button.ClickListener clickListener) {
        Button button = new Button("Remove");
        button.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                reset();
                clickListener.buttonClick(event);
            }
        });
        root.addComponent(button);
    }
}
