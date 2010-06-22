package info.magnolia.module.genuinecentral.dialogx;

public abstract class AbstractDialogItem implements DialogItem {

    private DialogItem parent;

    public DialogItem getParent() {
        return parent;
    }

    public void setParent(DialogItem parent) {
        this.parent = parent;
    }
}
