package org.mightyfrog.util.swingpropviewer;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

/**
 *
 *
 */
class FileChooser extends JFileChooser {
    //
    public static final String CLASS_EXT = ".class";
    public static final String JAR_EXT = ".jar";
    public static final String PROPERTIES_EXT = ".properties";
    public static final String CLASS_DESC = "Jar Files (*.class)";
    public static final String JAR_DESC = "Jar Files (*.jar)";
    public static final String PROPERTIES_DESC = "Properties Files (*.properties)";

    //
    static enum TYPE {CLASS, JAR, PROPERTIES};

    //
    private CFileFilter FILTER = new CFileFilter();

    /**
     * Creates a FileChooser.
     *
     */
    public FileChooser() {
        super();
        setFileFilter(FILTER);
    }

    /**
     *
     * @param type
     */
    public void setType(TYPE type) {
        if (type == TYPE.JAR) {
            FILTER.setExtension(JAR_EXT);
            FILTER.setDescription(JAR_DESC);
        } else if (type == TYPE.CLASS) {
            FILTER.setExtension(CLASS_EXT);
            FILTER.setDescription(CLASS_DESC);
        } else {
            assert type == TYPE.PROPERTIES : "Unexpected file type";

            FILTER.setExtension(PROPERTIES_EXT);
            FILTER.setDescription(PROPERTIES_DESC);
        }
    }

    /** */
    @Override
    public void updateUI() {
        super.updateUI();

        // workaround for jdk bug
        resetChoosableFileFilters();
        setFileFilter(FILTER);
    }

    //
    //
    //

    /**
     *
     */
    private class CFileFilter extends FileFilter {
        //
        private String extension = JAR_EXT;
        private String description = JAR_DESC;

        /** */
        @Override
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }
            
            return f.getName().endsWith(this.extension);
        }

        /** */
        @Override
        public String getDescription() {
            return this.description;
        }

        /**
         *
         */
        public void setExtension(String extension) {
            this.extension = extension;
        }

        /**
         *
         */
        public void setDescription(String description) {
            this.description = description;
        }
    }
}
