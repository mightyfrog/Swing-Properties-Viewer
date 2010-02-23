package org.mightyfrog.util.swingpropviewer;

import java.awt.event.MouseEvent;
import javax.swing.InputMap;
import javax.swing.JTable;
import javax.swing.KeyStroke;

/**
 *
 *
 *
 */
class InputMapTable extends ResourceTable {
    /**
     * Creates a InputMapTable.
     *
     */
    public InputMapTable() {
        super(new String[]{"Property Name", "Key Stroke", "Action Command"});
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
            if (obj instanceof InputMap) {
                InputMap im = (InputMap) obj;
                String key = (String) table.getValueAt(i, 0);
                KeyStroke[] keyStrokes = im.keys();
                for (KeyStroke ks : keyStrokes) {
                    addRow(new Object[]{key, ks, im.get(ks)});
                }
                }
        }
        toggleSortOrder(0);
    }

    /** */
    @Override
    protected void showPopup(MouseEvent evt) {
        String key = "" + getValueAt(rowAtPoint(evt.getPoint()), 0);
        String keyStroke = "" + getValueAt(rowAtPoint(evt.getPoint()), 1);
        String actionCommand = "" + getValueAt(rowAtPoint(evt.getPoint()), 2);
        POPUP.show(this, evt.getX(), evt.getY(),
                   new String[]{null,
                                key,
                                null,
                                "getInputMap().get(KeyStroke.getKeyStroke(\"" +
                                keyStroke + "\"));",
                                "getInputMap().put(KeyStroke.getKeyStroke(\"" +
                                keyStroke + "\"), \"" + actionCommand + "\");",
                                "getInputMap().remove(KeyStroke.getKeyStroke(\"" +
                                keyStroke + "\"));"});
    }
}
