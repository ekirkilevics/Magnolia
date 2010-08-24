package info.magnolia.module.admincentral.navigation;

import com.vaadin.Application;
import com.vaadin.ui.Window.Notification;

public class DefaultMenuAction extends MenuAction {

    private String onClick;

    public DefaultMenuAction(String label) {
        super(label);
    }

    @Override
    public void handleAction(Object sender, Object target) {
        ((Application) sender).getMainWindow().showNotification("OnClick", onClick, Notification.TYPE_HUMANIZED_MESSAGE);
    }

    public void setOnClick(String onClick) {
        this.onClick = onClick;
    }

}
