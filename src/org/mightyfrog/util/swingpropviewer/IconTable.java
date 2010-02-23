package org.mightyfrog.util.swingpropviewer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.ColorModel;
import java.awt.image.PixelGrabber;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 *
 *
 */
class IconTable extends ResourceTable implements MouseMotionListener  {
    //
    private static int _rowHeight = 0;

    //
    private int pixels[] = null;

    //
    private final JLabel IMAGE_LABEL = new JLabel();
    private static final int IMAGE_HEIGHT = 250;
    private int imageWidth = 0; // holds aspect ratio of IMAGE_HEIGHT


    /**
     * Creates an IconTable.
     *
     */
    public IconTable() {
        super(new String[]{"Property Name", "Image Icon"});

        getColumnModel().getColumn(1).setCellRenderer(new IconTable.CellRenderer());

        IMAGE_LABEL.addMouseMotionListener(this);
    }

    /** */
    @Override
    public void	mouseClicked(MouseEvent evt) {
        if (evt.getClickCount() == 2) {
            int row = getSelectedRow();
            if (row == -1) {
                return;
            }
            ImageIcon icon = (ImageIcon) getValueAt(row, 1);
            Image image = icon.getImage().getScaledInstance(-1, IMAGE_HEIGHT,
                                                            Image.SCALE_DEFAULT);
            IMAGE_LABEL.setIcon(new ImageIcon(image));
            grabPixels(image);
            JOptionPane.showOptionDialog(JOptionPane.getRootFrame(),
                                         new Object[]{IMAGE_LABEL},
                                         "" + getValueAt(row, 0),
                                         JOptionPane.OK_CANCEL_OPTION,
                                         JOptionPane.PLAIN_MESSAGE,
                                         null, null, null);
        }
    }

    /** */
    @Override
    public void mouseDragged(MouseEvent evt) {
        // no-op
    }

    /** */
    @Override
    public void mouseMoved(MouseEvent evt) {
        if (this.pixels == null) {
            return;
        }

        int x = evt.getX();
        int y = evt.getY();
        if (x > 0 && x < this.imageWidth && y > 0 && y < IMAGE_HEIGHT) {
            int pixel = this.pixels[y * this.imageWidth + x];
            int r = ColorModel.getRGBdefault().getRed(pixel);
            int g = ColorModel.getRGBdefault().getGreen(pixel);
            int b = ColorModel.getRGBdefault().getBlue(pixel);
            int a = ColorModel.getRGBdefault().getAlpha(pixel);
            float[] hsb = Color.RGBtoHSB(r, g, b, new float[3]);
            int h = Math.round(359 * hsb[0]);
            int s = Math.round(100 * hsb[1]);
            int br = Math.round(100 * hsb[2]);
            String text = "RGB=" + r + ", " + g + ", " + b + "  HSB=" + h + ", " +
                s + ", " + br + "  alpha=" + a;
            setCenterStatusText(text);
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
            if (obj instanceof ImageIcon) {
                // obj instanceof javax.swing.Icon won't work
                String key = (String) table.getValueAt(i, 0);
                ImageIcon icon = (ImageIcon) obj;
                if (_rowHeight < icon.getIconHeight()) {
                    _rowHeight = icon.getIconHeight();
                }
                addRow(new Object[]{key, icon});
            }
        }
        toggleSortOrder(0);
    }

    /** */
    @Override
    protected void showPopup(MouseEvent evt) {
        String key = "" + getValueAt(rowAtPoint(evt.getPoint()), 0);
        POPUP.show(this, evt.getX(), evt.getY(),
                   new String[]{null,
                                key,
                                null,
                                "UIManager.getIcon(\"" + key + "\");",
                                "javax.swing.UIManager.getIcon(\"" + key + "\");"});
    }

    //
    //
    //

    /**
     *
     *
     * @param image
     * @param io
     */
    private void grabPixels(Image image) {
        this.imageWidth = image.getWidth(IMAGE_LABEL);
        this.pixels = new int[this.imageWidth * IMAGE_HEIGHT];
        PixelGrabber pg =
            new PixelGrabber(image, 0, 0, this.imageWidth, IMAGE_HEIGHT,
                             this.pixels,0, this.imageWidth);
        try {
            pg.grabPixels();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
                table.setRowHeight(_rowHeight);
                ImageIcon icon = (ImageIcon) value;
                setText(" " + icon.getIconWidth() + "X" + icon.getIconHeight() +
                        " " + icon.toString());
                setIcon(icon);
            }

            return this;
        }
    }
}
