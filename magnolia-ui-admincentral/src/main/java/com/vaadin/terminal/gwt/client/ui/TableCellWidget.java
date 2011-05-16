package com.vaadin.terminal.gwt.client.ui;

/**
 * A widget that's intended to be used as a table cell.
 */
public interface TableCellWidget {

    /**
     * Returns true if clicking on this widget should select the table row.
     */
    boolean isRowSelector();
}
