package org.mightyfrog.util.swingpropviewer;

import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 *
 *
 */
class SoundTable extends ResourceTable {
    //
    private static final String[] AUDIO_FILE_FORMAT_TYPES =
        new String[]{"aifc", "aiff", "au", "snd", "wav"};

    //
    private final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    /**
     * Creates a SoundTable.
     *
     */
    public SoundTable() {
        super(new String[]{"Property Name", "Sound"});
    }

    /** */
    @Override
    public void	mouseClicked(MouseEvent evt) {
        if (evt.getClickCount() == 2) {
            int row = getSelectedRow();
            if (row == -1) {
                return;
            }
            URL url = getURL((String) getValueAt(row, 1));
            if (url != null) {
                playback(url);
            }
        }
    }

    /** */
    @Override
    public void valueChanged(ListSelectionEvent evt) {
        super.valueChanged(evt);
        if (!evt.getValueIsAdjusting()) {
            setCenterStatusText(SoundTable.SoundPlayer.AUDIO_FORMAT.toString());
        }
    }

    //
    //
    //

    /**
     *
     * @param fileName
     */
    public URL getURL(String fileName) {
        URL url = getClass().getClassLoader().getResource(fileName);
        if (url == null) {
            url = getClass().getResource(fileName);
        }
        if (url == null) {
            String path = UIManager.getLookAndFeel().getClass().getPackage().getName();
            path = path.replaceAll("[.]", "/") + "/" + fileName;
            url = getClass().getClassLoader().getResource(path);
        }
        if (url == null) {
            if (MetalLookAndFeel.class.isAssignableFrom(fileName.getClass())) {
                String path = "javax/swing/plaf/metal/" + fileName;
                url = getClass().getClassLoader().getResource(path);
            }
        }

        return url;
    }

    /** */
    @Override
    protected void load() {
        JTable table = getTable();
        for (int i = 0; i < table.getRowCount(); i++) {
            Object obj = table.getValueAt(i, 1);
            if (obj instanceof String) {
                String str = (String) obj;
                for (String type : AUDIO_FILE_FORMAT_TYPES) {
                    if (str.toLowerCase().endsWith("." + type)) {
                        String key = (String) table.getValueAt(i, 0);
                        addRow(new Object[]{key, str});
                    }
                }
            }
        }
        toggleSortOrder(0);
    }

    /** */
    @Override
    protected void showPopup(MouseEvent evt) {
        int row = rowAtPoint(evt.getPoint());
        String key = "" + getValueAt(row, 0);
        String value = "" + getValueAt(row, 1);
        POPUP.show(this, evt.getX(), evt.getY(),
                   new String[]{null,
                                key,
                                null,
                                "" + getURL(value)});
    }

    //
    //
    //

    /**
     *
     *
     * @param url
     */
    private void playback(URL url) {
        EXECUTOR.execute(new SoundTable.SoundPlayer(url));
    }

    //
    //
    //

    /**
     *
     */
    static class SoundPlayer implements Runnable {
        //
        public static final AudioFormat AUDIO_FORMAT =
            new AudioFormat(22050.0F, 16, 1, true, false);

        // sound url
        private URL url = null;

        /**
         *
         */
        public SoundPlayer(URL url) {
            this.url = url;
        }

        /** */
        public void run() {
            AudioInputStream is = null;
            try {
                is = AudioSystem.getAudioInputStream(url);
            } catch (UnsupportedAudioFileException e) {
            } catch (IOException e) {
            }

            int bytesPerFrame = is.getFormat().getFrameSize();
            if (bytesPerFrame == AudioSystem.NOT_SPECIFIED) {
                bytesPerFrame = 1;
            }

            //setCenterStatusText(AUDIO_FORMAT.toString());
            DataLine.Info dataLineInfo =
                new DataLine.Info(SourceDataLine.class, AUDIO_FORMAT);
            SourceDataLine sourceDataLine = null;
            try {
                sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
                sourceDataLine.open(AUDIO_FORMAT);
                sourceDataLine.start();
            } catch (LineUnavailableException e) {
                e.printStackTrace();
            }

            try{
                int n = 0;
                byte[] b = new byte[10000]; // metal laf 11 files 121KB total
                while((n = is.read(b)) != -1){
                    sourceDataLine.write(b, 0, n);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (sourceDataLine != null) {
                    sourceDataLine.drain();
                    sourceDataLine.close();
                }
            }
        }
    }
}
