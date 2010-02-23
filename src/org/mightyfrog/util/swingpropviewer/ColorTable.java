package org.mightyfrog.util.swingpropviewer;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JColorChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 *
 *
 */
class ColorTable extends ResourceTable implements MouseListener {
    //
    public static final String[] COLUMN_IDENTIFIERS = {"Property Name", "Color"};

    // predefined color & name pairs
    private final Map<Color, String> COLOR_MAP = new HashMap<Color, String>();

    {
        COLOR_MAP.put(Color.BLACK, "BLACK");
        COLOR_MAP.put(Color.BLUE, "BLUE");
        COLOR_MAP.put(Color.CYAN, "CYAN");
        COLOR_MAP.put(Color.DARK_GRAY, "DARK_GRAY");
        COLOR_MAP.put(Color.GRAY, "GRAY");
        COLOR_MAP.put(Color.GREEN, "GREEN");
        COLOR_MAP.put(Color.LIGHT_GRAY, "LIGHT_GRAY");
        COLOR_MAP.put(Color.MAGENTA, "MAGENTA");
        COLOR_MAP.put(Color.ORANGE, "ORANGE");
        COLOR_MAP.put(Color.PINK, "PINK");
        COLOR_MAP.put(Color.RED, "RED");
        COLOR_MAP.put(Color.WHITE, "WHITE");
        COLOR_MAP.put(Color.YELLOW, "YELLOW");
    }

    /**
     * Creates a ColorTable.
     *
     */
    public ColorTable() {
        super(COLUMN_IDENTIFIERS);

        getColumnModel().getColumn(1).setCellRenderer(new ColorTable.CellRenderer());
    }

    /** */
    @Override
    public void	mouseClicked(MouseEvent evt) {
        if (evt.getClickCount() == 2) {
            int row = getSelectedRow();
            if (row == -1) {
                return;
            }
            String key = (String) getValueAt(row, 0);
            Color color = (Color) getValueAt(row, 1);
            JColorChooser.showDialog(JOptionPane.getRootFrame(), key, color);
        }
    }

    /** */
    @Override
    public void valueChanged(ListSelectionEvent evt) {
        super.valueChanged(evt);
        if (!evt.getValueIsAdjusting()) {
            updateCenterStatusText();
        }
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
            if (obj instanceof Color) {
                String key = (String) table.getValueAt(i, 0);
                Color color = (Color) obj;
                addRow(new Object[]{key, color});
            }
        }
        toggleSortOrder(0);
        updateCenterStatusText();
    }

    /** */
    @Override
    protected void showPopup(MouseEvent evt) {
        String key = "" + getValueAt(rowAtPoint(evt.getPoint()), 0);
        POPUP.show(this, evt.getX(), evt.getY(),
                   new String[]{null,
                                key,
                                null,
                                "UIManager.getColor(\"" + key + "\");",
                                "javax.swing.UIManager.getColor(\"" + key + "\");"});
    }

    //
    //
    //

    /**
     *
     */
    private void updateCenterStatusText() {
        int row = getSelectedRow();
        if (row == -1) {
            return;
        }
        Color color = (Color) getValueAt(row, 1);
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        int a = color.getAlpha();
        float[] hsb = Color.RGBtoHSB(r, g, b, new float[3]);
        int h = Math.round(359 * hsb[0]);
        int s = Math.round(100 * hsb[1]);
        int br = Math.round(100 * hsb[2]);
        String text = "RGB=" + r + ", " + g + ", " + b + "  HSB=" + h + ", " +
            s + ", " + br + "  alpha=" + a;

        if (COLOR_MAP.get(color) != null) {
            text += " (java.awt.Color." + COLOR_MAP.get(color) + ")";
        }

        setCenterStatusText(text);
    }

    //
    //
    //

    /**
     *
     */
    static class CellRenderer extends DefaultTableCellRenderer {
        /** */
        @Override
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean hasFocus,
                                                       int row,
                                                       int column) {
            super.getTableCellRendererComponent(table, value, isSelected,
                                                hasFocus, row, column);
            setText(value.toString());
            if (column == 1) {
                Color color = (Color) value;
                if (!isSelected) {
                    setBackground(color);
                }
            }

            return this;
        }
    }
}
