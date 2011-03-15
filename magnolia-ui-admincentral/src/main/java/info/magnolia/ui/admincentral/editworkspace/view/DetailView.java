package info.magnolia.ui.admincentral.editworkspace.view;

import info.magnolia.ui.framework.view.View;
import info.magnolia.ui.model.command.Command;

import java.util.List;
/**
 * DetailView.
 * @author fgrilli
 *
 */
public interface DetailView extends View{
    /**
     * Presenter that is called when the user selects a command.
     */
    public interface Presenter {
        void onCommandSelected(String commandName);
    }

    void showCommands(List<Command> commands);

}