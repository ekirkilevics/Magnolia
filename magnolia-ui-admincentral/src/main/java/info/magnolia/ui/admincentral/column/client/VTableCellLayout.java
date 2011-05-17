package info.magnolia.ui.admincentral.column.client;

import com.vaadin.terminal.gwt.client.ui.TableCellWidget;
import com.vaadin.terminal.gwt.client.ui.VCssLayout;


public class VTableCellLayout extends VCssLayout implements TableCellWidget {

    @Override
    public boolean isRowSelector() {
        return true;
    }
}
