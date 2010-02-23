package org.mightyfrog.util.swingpropviewer;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseEvent;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 *
 *
 */
class FontTable extends ResourceTable {
    //
    private final JTextArea TA = new JTextArea() {
            /** */
            @Override
             public void addNotify() {
                super.addNotify();
                
                new Thread(new Runnable() { // hack
                        /** */
                        public void run() {
                            try {
                                Thread.sleep(200);
                            } catch (InterruptedException e) {
                            }
                            requestFocusInWindow();
                        }
                    }).start();
            }
        };

    /**
     * Creates a FontTable.
     *
     */
    public FontTable() {
        super(new String[]{"Property Name", "Font"});
        
        FontTable.CellRenderer renderer = new FontTable.CellRenderer();
        for (int i = 0; i < getColumnCount(); i++) {
            getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
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
            Font font = (Font) getValueAt(row, 1);

            showPopupDialog(key, font);
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
            if (obj instanceof Font) {
                String key = (String) table.getValueAt(i, 0);
                Font font = (Font) obj;
                addRow(new Object[]{key, font});
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
                                "UIManager.getFont(\"" + key + "\");",
                                "javax.swing.UIManager.getFont(\"" + key + "\");"});
    }

    //
    //
    //

    /**
     *
     *
     * @param key
     * @param font
     */
    private void showPopupDialog(String key, Font font) {
        TA.setFont(font);
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setPreferredSize(new Dimension(300, 200));
        scrollPane.setViewportView(TA);
        JOptionPane.showOptionDialog(JOptionPane.getRootFrame(), scrollPane,
                                     key, JOptionPane.DEFAULT_OPTION,
                                     JOptionPane.PLAIN_MESSAGE,
                                     null, null, null);
    }

    /**
     *
     */
    private void updateCenterStatusText() {
        int row = getSelectedRow();
        if (row == -1) {
            return;
        }
        Font font = (Font) getValueAt(row, 1);
        String family = font.getFamily();
        String name = font.getFontName();
        String style = null;
        switch (font.getStyle()) {
        case Font.PLAIN:
            style = "plain";
            break;
        case Font.BOLD:
            style = "bold";
            break;
        default:
            assert font.getStyle() == Font.ITALIC: "Unknown font style";
            style = "italic";
        }
        int size = font.getSize();

        String text = "family=" + family + "  name=" + name + "  style=" +
            style + "  size=" + size;

        //System.out.println(font.getAttributes().get(java.awt.font.TextAttribute.FAMILY));
        //System.out.println(font.getAttributes().get(java.awt.font.TextAttribute.WEIGHT));
        //System.out.println(font.getAttributes().get(java.awt.font.TextAttribute.WIDTH));
        //System.out.println(font.getAttributes().get(java.awt.font.TextAttribute.POSTURE));
        //System.out.println(font.getAttributes().get(java.awt.font.TextAttribute.SIZE));
        //System.out.println(font.getAttributes().get(java.awt.font.TextAttribute.TRANSFORM));
        //System.out.println(font.getAttributes().get(java.awt.font.TextAttribute.SUPERSCRIPT));
        //System.out.println(font.getAttributes().get(java.awt.font.TextAttribute.FONT));
        //System.out.println(font.getAttributes().get(java.awt.font.TextAttribute.CHAR_REPLACEMENT));
        //System.out.println(font.getAttributes().get(java.awt.font.TextAttribute.FOREGROUND));
        //System.out.println(font.getAttributes().get(java.awt.font.TextAttribute.BACKGROUND));
        //System.out.println(font.getAttributes().get(java.awt.font.TextAttribute.UNDERLINE));
        //System.out.println(font.getAttributes().get(java.awt.font.TextAttribute.STRIKETHROUGH));
        //System.out.println(font.getAttributes().get(java.awt.font.TextAttribute.RUN_DIRECTION));
        //System.out.println(font.getAttributes().get(java.awt.font.TextAttribute.BIDI_EMBEDDING));
        //System.out.println(font.getAttributes().get(java.awt.font.TextAttribute.JUSTIFICATION));
        //System.out.println(font.getAttributes().get(java.awt.font.TextAttribute.INPUT_METHOD_UNDERLINE));
        //System.out.println(font.getAttributes().get(java.awt.font.TextAttribute.SWAP_COLORS));
        //System.out.println(font.getAttributes().get(java.awt.font.TextAttribute.NUMERIC_SHAPING));
        //System.out.println(font.getAttributes().get(java.awt.font.TextAttribute.KERNING));
        //System.out.println(font.getAttributes().get(java.awt.font.TextAttribute.LIGATURES));
        //System.out.println(font.getAttributes().get(java.awt.font.TextAttribute.TRACKING));

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
            if (column == 1) {
                setText(String.valueOf(value));
                setFont((Font) value);
            }

            return this;
        }
    }
}
