package info.magnolia.ui.admincentral.column.client;

import com.vaadin.terminal.gwt.client.ui.VCustomComponent;

public class VEditable extends VCustomComponent implements com.vaadin.terminal.gwt.client.ui.TableCellWidget {

    @Override
    public boolean isRowSelector() {
        return true;
    }
}
