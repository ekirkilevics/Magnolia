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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.vaadin.terminal.Resource;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Upload;
import com.vaadin.ui.VerticalLayout;

/**
 * View for the file upload field.
 *
 * @version $Id$
 */
public class FileUploadView extends CustomComponent implements Upload.SucceededListener, Upload.FailedListener, Upload.Receiver {

    /**
     * Presenter for FileUploadView.
     *
     * @version $Id$
     */
    public interface Presenter {

        void uploadSucceeded(File file, String fileName, String mimeType) throws IOException;

        void previewRemoved();
    }

    private Layout thumbnailLayout;
    private Presenter presenter;

    private File file;
    private String fileName;
    private String mimeType;

    public FileUploadView(Presenter presenter) {
        this.presenter = presenter;

        Upload upload = new Upload(null, this);
        upload.addListener((Upload.SucceededListener) this);
        upload.addListener((Upload.FailedListener) this);

        thumbnailLayout = new VerticalLayout();

        Layout root = new VerticalLayout();
        root.addComponent(upload);
        root.addComponent(thumbnailLayout);
        setCompositionRoot(root);
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

    @Override
    public void uploadSucceeded(Upload.SucceededEvent event) {
        try {
            presenter.uploadSucceeded(file, fileName, mimeType);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void uploadFailed(Upload.FailedEvent event) {
        this.file = null;
        this.fileName = null;
        this.mimeType = null;
    }

    public void removeThumbnail() {
        thumbnailLayout.removeAllComponents();
        if (file != null) {
            file.delete();
        }
        this.file = null;
        this.fileName = null;
        this.mimeType = null;
    }

    public boolean isShowingThumbnail() {
        return !thumbnailLayout.getComponentIterator().hasNext();
    }

    public void setThumbnail(ImageSize imageSize, Resource imageResource) {
        // TODO: should support showing SWF in the preview

        ImageSize scaledImageSize = imageSize.scaleToFitIfLarger(150, 150);

        Embedded embedded = new Embedded("", imageResource);
        embedded.setWidth(scaledImageSize.getWidth() + "px");
        embedded.setHeight(scaledImageSize.getHeight() + "px");

        Button removeButton = new Button("Remove");
        removeButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                removeThumbnail();
                presenter.previewRemoved();
            }
        });

        thumbnailLayout.removeAllComponents();
        thumbnailLayout.addComponent(embedded);
        thumbnailLayout.addComponent(new Label("width: " + imageSize.getWidth() + " height: " + imageSize.getHeight()));
        thumbnailLayout.addComponent(removeButton);
    }
}
