/**
 * 
 */
package info.magnolia.module.dms.gui;

import info.magnolia.cms.gui.control.Button;
import info.magnolia.cms.gui.dialog.DialogControlImpl;
import info.magnolia.cms.util.FreeMarkerUtil;

import java.io.IOException;
import java.io.Writer;

public class VersionCommentPopup extends DialogControlImpl {

    public void drawHtml(Writer out) throws IOException {
        out.write(FreeMarkerUtil.process(this));
    }

    public Button getCancelButton() {
        Button cancelButton = new Button("cancel", "Cancel");
        cancelButton.setEvent("onclick", "mgnl.dms.VersionCommentPopup.cancel();");
        return cancelButton;
    }

    public Button getSaveButton() {
        Button saveButton = new Button("save", "Save");
        saveButton.setEvent("onclick", "mgnl.dms.VersionCommentPopup.save();");
        return saveButton;
    }
}