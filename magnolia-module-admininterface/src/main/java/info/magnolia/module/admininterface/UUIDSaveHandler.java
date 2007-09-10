/**
 * 
 */
package info.magnolia.module.admininterface;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.gui.dialog.Dialog;
import info.magnolia.cms.gui.dialog.DialogControlImpl;
import info.magnolia.cms.gui.dialog.UUIDDialogControl;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.util.ContentUtil;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;


/**
 * This save handler checks each control if it is implementing the UUIDDialogControl interface. If so it stores the uuid
 * instead of the path.
 * @author Philipp Bracher
 * @version $Id$
 */
public class UUIDSaveHandler extends SaveHandlerImpl implements DialogAwareSaveHandler {

    /**
     * The dialog we are saving
     */
    private Dialog dialog;

    public Dialog getDialog() {
        return dialog;
    }

    public void setDialog(Dialog dialog) {
        this.dialog = dialog;
    }

    /**
     * Process a singel value
     */
    protected void processString(Content node, String name, int type, int encoding, String[] values, String valueStr)
        throws PathNotFoundException, RepositoryException, AccessDeniedException {
        DialogControlImpl control = getControl(name);
        if (control instanceof UUIDDialogControl) {
            // convert it to an uuid
            if (StringUtils.isNotEmpty(valueStr)) {
                valueStr = getUUID(name, valueStr);
            }
        }

        super.processString(node, name, type, encoding, values, valueStr);
    }

    /**
     * Process a multiple value
     */
    protected void processMultiple(Content node, String name, int type, String[] values) throws RepositoryException,
        PathNotFoundException, AccessDeniedException {
        DialogControlImpl control = getControl(name);

        if (control instanceof UUIDDialogControl) {
            if (values != null && values.length != 0) {
                String[] uuidValues = new String[values.length];
                for (int i = 0; i < values.length; i++) {
                    uuidValues[i] = getUUID(name, values[i]);
                }
                values = uuidValues;
            }
        }
        super.processMultiple(node, name, type, values);
    }

    /**
     * Get the contorl which is processed now
     * @param name
     * @return the control or null if not found
     */
    protected DialogControlImpl getControl(String name) {
        Object control = this.getDialog().getSub(name);
        if (control instanceof DialogControlImpl) {
            return (DialogControlImpl) control;
        }
        return null;
    }

    /**
     * Convert the path to an uuid. It uses the config value 'repository' of the control if found.
     * @param name
     * @param path
     * @return thr uuid or the path if the path was not found
     */
    private String getUUID(String name, String path) {
        String repository = ((UUIDDialogControl)getControl(name)).getRepository();
        Content node = ContentUtil.getContent(repository, path);
        if (node == null) {
            return path;
        }
        return node.getUUID();
    }

}
