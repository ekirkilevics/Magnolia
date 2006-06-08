package info.magnolia.module.admininterface;

import info.magnolia.cms.gui.dialog.Dialog;

/**
 * This save handler needs the dialog to proceed a proper save. The save handler may get the values from the dialog or
 * may get additional informations hold by the dialog.
 */
public interface DialogAwareSaveHandler extends SaveHandler {
    public Dialog getDialog();
    public void setDialog(Dialog dialog);
}
