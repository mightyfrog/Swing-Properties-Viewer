package org.mightyfrog.util.swingpropviewer;

import java.awt.event.MouseEvent;
import javax.swing.ActionMap;
import javax.swing.JTable;

/**
 *
 *
 *
 */
class ActionMapTable extends ResourceTable {
    /**
     * Creates a ActionMapTable.
     *
     */
    public ActionMapTable() {
        super(new String[]{"Property Name", "Action Command", "Action"});
    }

    //
    //
    //

    /** */
    @Override
    protected void load() {
        JTable table = getTable();
        for (int i = 0; i < table.getRowCount(); i++) {
            Object obj = table.getValueAt(i, 1);
            if (obj instanceof ActionMap) {
                ActionMap map = (ActionMap) obj;
                String value = (String) table.getValueAt(i, 0);
                Object[] keys = map.keys();
                for (Object key : keys) {
                    addRow(new Object[]{value, key, map.get(key)});
                }
            }
        }
        toggleSortOrder(0);
    }

    /** */
    @Override
    protected void showPopup(MouseEvent evt) {
        String key = "" + getValueAt(rowAtPoint(evt.getPoint()), 0);
        String actionCommand = "" + getValueAt(rowAtPoint(evt.getPoint()), 1);
        POPUP.show(this, evt.getX(), evt.getY(),
                   new String[]{null,
                                key,
                                null,
                                "getActionMap().get(\"" + actionCommand + "\");",
                                "getActionMap().put(\"" + actionCommand + "\", null);",
                                "getActionMap().remove(\"" + actionCommand + "\");"});
    }
}
