package org.mightyfrog.util.swingpropviewer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.regex.PatternSyntaxException;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/**
 *
 *
 *
 */
abstract class ResourceTable extends JTable implements MouseListener {
    //
    private final ResourceTable.Model MODEL;
    private final TableRowSorter<DefaultTableModel> SORTER;

    {
        getActionMap().put("copy", new AbstractAction() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    String text = "" + getValueAt(getSelectedRow(), getSelectedColumn());
                    exportToClipboard(text);
                }
            });
    }

    //
    protected final ResourceTable.PopupMenu POPUP = new ResourceTable.PopupMenu();

    /**
     *
     * @param identifiers the column identifiers
     */
    protected ResourceTable(String[] identifiers) {
        MODEL = new ResourceTable.Model(identifiers);
        setModel(MODEL);
        SORTER = new TableRowSorter<DefaultTableModel>(MODEL);
        setRowSorter(SORTER);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        addMouseListener(this);

        getTableHeader().setReorderingAllowed(false);
    }

    /** */
    @Override
    public void mouseEntered(MouseEvent evt) {
        // no-op
    }

    /** */
    @Override
    public void mouseClicked(MouseEvent evt) {
        // no-op
    }

    /** */
    @Override
    public void mousePressed(MouseEvent evt) {
        handlePopup(evt);
    }

    /** */
    @Override
    public void mouseReleased(MouseEvent evt) {
        handlePopup(evt);
    }

    /** */
    @Override
    public void mouseExited(MouseEvent evt) {
        // no-op
    }

    //
    //
    //

    /**
     *
     */
    protected abstract void load();

    /**
     *
     */
    protected void showPopup(MouseEvent evt) {
        // no-op
    }

    /**
     *
     */
    protected AllPropertiesTable getTable() {
        return ((SwingPropertiesViewer) JOptionPane.getRootFrame()).TABLE;
    }

    /**
     *
     */
    protected void reset() {
        removeAllRows();
        load();
    }

    /**
     * Adds row data.
     *
     * @param rowData
     */
    protected void addRow(Object[] rowData) {
        MODEL.addRow(rowData);
    }

    /**
     * Removes all the rows in the table.
     *
     */
    protected void removeAllRows() {
        int rowCount = MODEL.getRowCount();
        for (int i = rowCount - 1; i != -1; i--) {
            MODEL.removeRow(i);
        }
    }

    /**
     * Reverses the sort order of the specified column.
     *
     * @param col the column to toggle the sort ordering of, in terms of
     *        the underlying model
     */
    protected void toggleSortOrder(int col) {
        getRowSorter().toggleSortOrder(col);
    }


    /**
     * Returns the key for the specified row.
     *
     * @param row
     */
    protected String getKey(int row) {
        return "" + getValueAt(row, 0);
    }

    /**
     * Returns the value for the specified row.
     *
     * @param row
     */
    protected Object getValue(int row) {
        return getValueAt(row, 1);
    }

    /**
     * Filters out rows that satisfies the specified regular expression.
     *
     * @param regex
     * @return the row count
     */
    protected final int filter(String regex, int... indices) {
        try {
            SORTER.setRowFilter(RowFilter.regexFilter(regex, indices));
        } catch (PatternSyntaxException e) {
            // ignore
        }

        return getRowCount();
    }

    /**
     * Wraps SwingPropertiesViewer#setCenterStatusText(String).
     *
     * @param text
     */
    protected final void setCenterStatusText(String text) {
        ((SwingPropertiesViewer) JOptionPane.getRootFrame()).
            setCenterStatusText(text);
    }

    /**
     *
     * @param text
     */
    protected static final void exportToClipboard(String text) {
        StringSelection ss = new StringSelection(text);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, ss);
    }

    //
    //
    //

    /**
     *
     * @param evt
     */
    private void handlePopup(MouseEvent evt) {
        int row = rowAtPoint(evt.getPoint());
        if (row != -1) {
            setRowSelectionInterval(row, row);
            if (evt.isPopupTrigger()) {
                POPUP.removeAll();
                showPopup(evt);
            }
        }
    }

    //
    //
    //

    /**
     *
     */
    static class Model extends DefaultTableModel {
        //
        private String[] identifiers = null;

        /**
         *
         * @param identifiers the column identifiers
         */
        public Model(String[] identifiers) {
            this.identifiers = identifiers;
            setColumnIdentifiers(identifiers);
        }

        /** */
        @Override
        public int getColumnCount() {
            return identifiers.length;
        }

        /** */
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    }

    /**
     *
     */
    static class PopupMenu extends JPopupMenu {
        //
        private final ActionListener COPY_ACTION = new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    String text = ((JMenuItem) evt.getSource()).getText();
                    exportToClipboard(text);
                }
            };

        /**
         *
         * @param c
         * @param x
         * @param y
         * @param items
         */
        public void show(Component c, int x, int y, String[] items) {
            add(new ResourceTable.Separator("Send to Clipboard:"));
            JMenuItem mi = null;
            for (String item : items) {
                if (item == null) {
                    addSeparator();
                } else {
                    mi = new JMenuItem(item);
                    mi.addActionListener(COPY_ACTION);
                    add(mi);
                }
            }
            super.show(c, x, y);
        }
    }

    /**
     *
     */
    static class Separator extends javax.swing.JSeparator {
        //
        private String text = null;

        /**
         *
         */
        public Separator(String text) {
            super(HORIZONTAL);

            this.text = text;
            setUI(new javax.swing.plaf.basic.BasicSeparatorUI() {
                    /** */
                    public java.awt.Dimension getPreferredSize(javax.swing.JComponent c ) {
                        int height = getFontMetrics(getFont()).getHeight() + 8;
                        return new java.awt.Dimension(0, height);
                    }
                });
        }

        /** */
        @Override
        public void paintComponent(java.awt.Graphics g) {
            int y = getFontMetrics(getFont()).getHeight();
            int offset = UIManager.getInt("MenuItem.minimumTextOffset");
            g.drawString(this.text, Math.max(10, offset), y);
        }
    }
}
