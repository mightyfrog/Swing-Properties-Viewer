package org.mightyfrog.util.swingpropviewer;

import javax.swing.event.ListSelectionEvent;

/**
 *
 */
class AllPropertiesTable extends ResourceTable {
    /**
     * Creates a AllPropertiesTable.
     *
     */
    public AllPropertiesTable() {
        super(new String[]{"Property Name", "Value"});
    }

    /** */
    @Override
    public void load() {
        // no-op
    }

    /** */
    @Override
    public void reset() {
        // no-op
    }

    /** */
    @Override
    public void valueChanged(ListSelectionEvent evt) {
        super.valueChanged(evt);
        if (!evt.getValueIsAdjusting()) {
            Object obj = getValueAt(evt.getFirstIndex(), 1);
            setCenterStatusText(obj.getClass().getName());
        }
    }

    /** */
    @Override
    public void showPopup(java.awt.event.MouseEvent evt) {
        int row = rowAtPoint(evt.getPoint());
        Object obj = getValueAt(row, 1);
        String key = "" + getValueAt(row, 0);
        String type = "";
        if (obj instanceof java.awt.Color) {
            type = "Color";
        } else if (obj instanceof java.lang.String) {
            type = "String";
        } else if (obj instanceof javax.swing.border.Border) {
            type = "Border";
        } else if (obj instanceof javax.swing.Icon) {
            type = "Icon";
        } else if (obj instanceof java.lang.Integer) {
            type = "Int";
        } else if (obj instanceof java.awt.Insets) {
            type = "Insets";
        } else if (obj instanceof java.awt.Dimension) {
            type = "Dimension";
        } else if (obj instanceof java.lang.Boolean) {
            type = "Boolean";
        } else {
            // thru
        }
        POPUP.show(this, evt.getX(), evt.getY(),
                   new String[]{null,
                                key,
                                null,
                                "UIManager.get" + type + "(\"" + key + "\");",
                                "javax.swing.UIManager.get" + type + "(\"" + key + "\");"});
    }
}
