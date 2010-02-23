package org.mightyfrog.util.swingpropviewer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 *
 *
 */
public class SwingPropertiesViewer extends JFrame implements DocumentListener,
                                                             ActionListener,
                                                             ChangeListener,
                                                             FocusListener {
    //
    private static final String INSTALL_LAF_AC = "installLaf";
    private static final String RUN_CLASS_AC = "executeClass";
    private static final String RUN_EXECUTABLE_JAR_AC = "executeJar";
    private static final String SAVE_AC = "save";

    //
    private final JLabel L_STATUS_LABEL = new JLabel(""); // left
    private final JLabel C_STATUS_LABEL = new JLabel(""); // center
    private final JLabel R_STATUS_LABEL = new JLabel("Metal"); // right

    // north panel components
    private final JLabel FILTER_LABEL = new JLabel("Filter:");
    private final JTextField FILTER_FIELD = new JTextField();
    private final JCheckBox IGNORE_CASE_CHECK_BOX = new JCheckBox("Ignore Case");

    //
    private final JMenuBar MENU_BAR = new JMenuBar();
    private final JMenu FILE_MENU = new JMenu("File");
    private final JMenu LAF_MENU = new JMenu("Look And Feel");
    private final JMenuItem SAVE_MI = new JMenuItem("Save");
    private final JMenuItem INSTALL_LAF_MI = new JMenuItem("Install LAF...");
    private final JMenuItem RUN_CLASS_MI = new JMenuItem("Run Class...");
    private final JMenuItem RUN_JAR_MI = new JMenuItem("Run Executable Jar...");

    //
    private final Map<String, String> LAF_MAP = new HashMap<String, String>();

    //
    private final JTabbedPane TABBED_PANE = new JTabbedPane() {
            /** */
            @Override
            public Component getSelectedComponent() {
                JScrollPane sp = (JScrollPane) super.getSelectedComponent();

                return sp.getViewport().getView();
            }

            /** */
            @Override
            public void addTab(String title, Component component) {
                super.addTab(title, new JScrollPane(component));
            }
        };
    private final IconTable ICON_PANEL = new IconTable();
    private final ColorTable COLOR_TABLE = new ColorTable();
    private final FontTable FONT_TABLE = new FontTable();
    private final InputMapTable INPUT_MAP_TABLE = new InputMapTable();
    private final ActionMapTable ACTION_MAP_TABLE = new ActionMapTable();
    private final SoundTable SOUND_TABLE = new SoundTable();

    //
    protected final AllPropertiesTable TABLE = new AllPropertiesTable();

    //
    private FileChooser fileChooser = null;

    //
    private final int DELAY = 300; // 0.3 secs
    private final Action FILTER_ACTION = new AbstractAction() {
            /** */
            @Override
            public void actionPerformed(ActionEvent evt) {
                filter();
                setLastTyped(Long.MAX_VALUE);
            }
        };
    private final Timer TIMER = new Timer(DELAY, FILTER_ACTION);

    private long lastTyped = Long.MAX_VALUE;

    {
        TIMER.setRepeats(false);
    }

    /**
     * Creates a SwingPropertiesViewer.
     *
     */
    public SwingPropertiesViewer() {
        setTitle("Swing UI Properties Viewer");
        setIconImage(new ImageIcon(SwingPropertiesViewer.class.getResource("icon.png")).getImage());
        JOptionPane.setRootFrame(this);

        FILTER_FIELD.getDocument().addDocumentListener(this);
        FILTER_FIELD.addFocusListener(this);
        IGNORE_CASE_CHECK_BOX.setSelected(true);
        IGNORE_CASE_CHECK_BOX.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    filter();
                }
            });

        initMenuBar();
        setJMenuBar(MENU_BAR);

        add(createNorthPanel(), BorderLayout.PAGE_START);
        add(createCenterPanel());
        add(createSouthPanel(), BorderLayout.PAGE_END);

        loadProperties();

        TABLE.toggleSortOrder(0);
        TABBED_PANE.addChangeListener(this);

        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }

    /** */
    @Override
    public void changedUpdate(DocumentEvent e) {
        // no-op
    }

    /** */
    @Override
    public void insertUpdate(DocumentEvent e) {
        if (System.currentTimeMillis() - getLastTyped() > DELAY) {
            filter();
        } else {
            setLastTyped(System.currentTimeMillis());
            TIMER.stop();
            TIMER.start();
        }
    }

    /** */
    @Override
    public void removeUpdate(DocumentEvent e) {
        if (System.currentTimeMillis() - getLastTyped() > DELAY) {
            filter();
        } else {
            setLastTyped(System.currentTimeMillis());
            TIMER.stop();
            TIMER.start();
        }
    }

    /** */
    @Override
    public void actionPerformed(ActionEvent evt) {
        final String AC = evt.getActionCommand();
        if (AC.equals(INSTALL_LAF_AC)) {
            getFileChooser().setType(FileChooser.TYPE.JAR);
            int option = getFileChooser().showOpenDialog(this);
            if (option == JFileChooser.APPROVE_OPTION) {
                try {
                    File file = getFileChooser().getSelectedFile();
                    String[] lafNames = installLookAndFeel(file);
                    if (lafNames.length == 0) {
                        showMessageDialog("No Look And Feel class found.");
                    }
                } catch (Exception e) {
                    showMessageDialog(e.getMessage());
                }
            }
        } else if (AC.equals(SAVE_AC)) {
            try {
                save();
            } catch (IOException e) {
                showMessageDialog(e.getMessage());
            }
        } else if (AC.equals(RUN_EXECUTABLE_JAR_AC)) {
            getFileChooser().setType(FileChooser.TYPE.JAR);
            int option = getFileChooser().showOpenDialog(this);
            if (option == JFileChooser.APPROVE_OPTION) {
                invokeMain(getFileChooser().getSelectedFile());
            }
        } else if (AC.equals(RUN_CLASS_AC)) {
            getFileChooser().setType(FileChooser.TYPE.CLASS);
            int option = getFileChooser().showOpenDialog(this);
            if (option == JFileChooser.APPROVE_OPTION) {
                invokeMain(getFileChooser().getSelectedFile());
            }
        } else {
            EventQueue.invokeLater(new Runnable() {
                    /** */
                    public void run() {
                        try {
                            changeLookAndFeel(AC);
                            updateTabbedPane();
                        } catch (Exception e) {
                            showMessageDialog(e.getMessage());
                        }
                    }
                });
        }
    }

    /** */
    @Override
    public void focusGained(FocusEvent evt) {
        filter();
    }

    /** */
    @Override
    public void focusLost(FocusEvent evt) {
    }

    /** */
    @Override
    public void stateChanged(ChangeEvent evt) {
        updateTabbedPane();
    }

    /** */
    @Override
    public void addNotify() {
        super.addNotify();

        for (int i = 0; i < TABBED_PANE.getTabCount(); i++) {
            JScrollPane sc = (JScrollPane) TABBED_PANE.getComponentAt(i);
            ResourceTable table = (ResourceTable) sc.getViewport().getView();
            table.load();
        }
    }

    /** */
    public static void main(String[] args) {
        try{
            String className = UIManager.getSystemLookAndFeelClassName();
            UIManager.setLookAndFeel(className);
        }catch(Exception e){
            UIManager.put("swing.boldMetal", Boolean.FALSE);
            e.printStackTrace();
            return;
        }

        EventQueue.invokeLater(new Runnable() {
                /** */
                @Override
                public void run() {
                    new SwingPropertiesViewer();
                }
            });
    }

    //
    //
    //

    /**
     * Sets the status text.
     *
     * @param text
     */
    void setLeftStatusText(final String text) {
        if (text!= null) {
            L_STATUS_LABEL.setText(" " + text);
        } else {
            L_STATUS_LABEL.setText(null);
        }
    }

    /**
     * Sets the status text.
     *
     * @param text
     */
    void setCenterStatusText(final String text) {
        if (text!= null) {
            C_STATUS_LABEL.setText(" " + text);
        } else {
            C_STATUS_LABEL.setText(null);
        }
    }

    /**
     * Sets the status text.
     *
     * @param text
     */
    void setRightStatusText(final String text) {
        if (text!= null) {
            R_STATUS_LABEL.setText(" " + text);
        } else {
            R_STATUS_LABEL.setText(null);
        }
    }

    //
    // PRIVATE METHODS
    //

    /**
     *
     * @param lastTyped
     */
    private void setLastTyped(long lastTyped) {
        this.lastTyped = lastTyped;
    }

    /**
     *
     */
    private long getLastTyped() {
        return this.lastTyped;
    }

    /**
     *
     */
    private FileChooser getFileChooser() {
        if (this.fileChooser == null) {
            this.fileChooser = new FileChooser();
        }

        return this.fileChooser;
    }

    /**
     *
     */
    private void filter() {
        ResourceTable table = (ResourceTable) TABBED_PANE.getSelectedComponent();
        String regex = FILTER_FIELD.getText();
        regex = IGNORE_CASE_CHECK_BOX.isSelected() ? "(?i)" + regex : regex;
        setLeftStatusText(table.filter(regex) + " properties");
    }

    /**
     *
     */
    private void resetTabbedPane() {
        for (int i = 1; i < TABBED_PANE.getTabCount(); i++) {
            JScrollPane sp = (JScrollPane) TABBED_PANE.getComponentAt(i);
            ResourceTable table = (ResourceTable) sp.getViewport().getView();
            table.reset();
        }
    }

    /**
     *
     */
    private void updateTabbedPane() {
        setCenterStatusText(null);
        //ResourceTable table = (ResourceTable) TABBED_PANE.getSelectedComponent();
        //table.load();
        filter();
    }

    /**
     * TODO: separate them
     */
    private void invokeMain(final File file) {
        if (file.getName().endsWith(".class")) {
            new Thread(new Runnable() {
                    /** */
                    public void run() {
                        executeClass(file);
                    }
                }).start();
        } else if (file.getName().endsWith(".jar")) {
            new Thread(new Runnable() {
                    /** */
                    public void run() {
                        executeJar(file);
                    }
                }).start();
        } else {
            showMessageDialog(file.getName() + " is not a class/jar file");
        }
    }

    /**
     * Executes the specified class.
     *
     * MalformedURLException, IOException, ClassNotFoundException, NoSuchMethodException,
     * IllegalAccessException,InvocationTargetException shadowed
     *
     * @param file the class file
     */
    private void executeClass(File file) {
        String name = file.getName();
        if (!name.toLowerCase().endsWith(".class")) {
            return;
        }
        try {
            ClassLoader cl =
                new URLClassLoader(new URL[]{file.getParentFile().toURI().toURL()});
            name = name.substring(0, name.indexOf(".class"));
            Class<?> c = cl.loadClass(name);
            Method m = c.getDeclaredMethod("main", String[].class);
            m.invoke(getClass().getClassLoader(), new Object[]{null});
        } catch (Throwable e) {
            if (e instanceof NoClassDefFoundError) { // hack
                String s = e.getMessage();
                int index = s.indexOf("wrong name: ");
                s = s.substring(index + "wrong name: ".length(), s.length() - 1);
                String[] paths = s.split("/");
                for (int i = 0; i < s.split("/").length; i++) {
                    file = file.getParentFile();
                }
                try {
                    ClassLoader cl = new URLClassLoader(new URL[]{file.toURI().toURL()});
                    s = s.replace("/", ".");
                    Class<?> c = cl.loadClass(s);
                    Method m = c.getDeclaredMethod("main", String[].class);
                    m.invoke(getClass().getClassLoader(), new Object[]{null});
                } catch (Exception ex) {
                }
            } else {
                showMessageDialog(e.toString());
            }
        }
    }

    /**
     * Executes the specified executable jar file.
     *
     * MalformedURLException, IOException, ClassNotFoundException, NoSuchMethodException,
     * IllegalAccessException,InvocationTargetException shadowed
     *
     * @param file the jar file
     */
    private void executeJar(File file) {
        try {
            JarFile jar = new JarFile(file, true, JarFile.OPEN_READ);
            Manifest mf = jar.getManifest();
            if (mf == null) {
                showMessageDialog(file.getName() + " is not an executable jar.");
                return;
            }
            Attributes attr = mf.getMainAttributes();
            String mainClass = attr.getValue(Attributes.Name.MAIN_CLASS);
            if (mainClass != null) {
                addClasspath(file.toURI().toURL());
                Class<?> c = Class.forName(mainClass);
                Method m = c.getDeclaredMethod("main", String[].class);
                m.invoke(getClass().getClassLoader(), new Object[]{null});
            } else {
                showMessageDialog(file.getName() + " is not an executable jar.");
            }
        } catch (Exception e) {
            showMessageDialog(e.toString());
        }
    }

    /**
     *
     *
     * @throws java.io.IOException
     */
    private void save() throws IOException {
        getFileChooser().setType(FileChooser.TYPE.PROPERTIES);
        int option = getFileChooser().showSaveDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            File file = getFileChooser().getSelectedFile();
            Properties prop = new Properties();
            for (int i = 0; i < TABLE.getRowCount(); i++) {
                prop.setProperty(TABLE.getKey(i), "" + TABLE.getValue(i));
            }
            OutputStream os = null;
            try {
                os = new FileOutputStream(file);
                LookAndFeel laf = UIManager.getLookAndFeel();
                String comment = laf.getClass().getName() + "(" +
                    laf.getName() + ") properties";
                prop.store(os, comment);
            } catch (IOException e) {
                throw e;
            } finally {
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException e) {
                        os = null;
                    }
                }
            }
        }

    }

    /**
     * Initializes menu bar.
     *
     */
    private void initMenuBar() {
        INSTALL_LAF_MI.setActionCommand(INSTALL_LAF_AC);
        RUN_CLASS_MI.setActionCommand(RUN_CLASS_AC);
        RUN_JAR_MI.setActionCommand(RUN_EXECUTABLE_JAR_AC);
        SAVE_MI.setActionCommand(SAVE_AC);

        INSTALL_LAF_MI.addActionListener(this);
        RUN_CLASS_MI.addActionListener(this);
        RUN_JAR_MI.addActionListener(this);
        SAVE_MI.addActionListener(this);

        // file menu
        MENU_BAR.add(FILE_MENU);
        FILE_MENU.add(SAVE_MI);
        FILE_MENU.addSeparator();
        FILE_MENU.add(RUN_JAR_MI);
        FILE_MENU.add(RUN_CLASS_MI);

        // laf menu
        MENU_BAR.add(LAF_MENU);
        UIManager.LookAndFeelInfo[] info = UIManager.getInstalledLookAndFeels();
        for (UIManager.LookAndFeelInfo i : info) {
            LAF_MAP.put(i.getName(), i.getClassName());
            JMenuItem mi = new JMenuItem(i.getName());
            mi.addActionListener(this);
            LAF_MENU.add(mi);
        }
        LAF_MENU.addSeparator();
        LAF_MENU.add(INSTALL_LAF_MI);
    }

    /**
     * Changes Look And Feel.
     *
     * @param name the Look And Feel name
     * @throws java.lang.Exception
     */
    private void changeLookAndFeel(final String name) throws Exception {
        try {
            UIManager.getLookAndFeel();
            UIManager.getDefaults().clear(); // no effect?
            UIManager.setLookAndFeel(LAF_MAP.get(name));
        } catch (Exception e) {
            // ClassNotFoundException, InstantiationException,
            // IllegalAccessException, UnsupportedLookAndFeelException
            throw e;
        }

        SwingUtilities.updateComponentTreeUI(this);
        SwingUtilities.updateComponentTreeUI(getFileChooser());

        setRightStatusText(name);

        loadProperties();
        resetTabbedPane();
    }

    /**
     * Installs Look And Feels from the specified jar file.
     *
     * @param file the jar file
     * @return the number of installed Look And Feel
     * @throws java.lang.Exception NoSuchMethodException, IllegalAccessException,
     *         InovcationTargetException, IOException
     */
    private String[] installLookAndFeel(File file) throws Exception {
        URL url = null;
        try {
            url = file.toURI().toURL();
        } catch (MalformedURLException e) {
            throw e;
        }

        try {
            addClasspath(url);
        } catch (Exception e) {
            throw e;
        }

        Class[] lafClasses = null;
        try {
            lafClasses = findLookAndFeelClass(file);
        } catch (IOException e) {
            throw e;
        }

        List<String> lafNameList = new ArrayList<String>();
        for (Class c : lafClasses) {
            LookAndFeel laf = null;
            try {
                laf = (LookAndFeel) c.newInstance();
            } catch (InstantiationException e) {
                throw e;
            } catch (IllegalAccessException e) {
                throw e;
            }

            String name = laf.getName();
            String className = laf.getClass().getName();
            if (LAF_MAP.get(name) != null) {
                String message =
                    "" + name + " is already installed. Rename?";
                String newName = JOptionPane.showInputDialog(this, message);
                if (newName != null) {
                    name = newName;
                } else {
                    continue;
                }
            }
            LAF_MAP.put(name, className);
            lafNameList.add(name);
            addMenuItem(name, laf.getDescription());
            UIManager.installLookAndFeel(name, className);
        }

        return (String[]) lafNameList.toArray(new String[]{});
    }

    /**
     * Adds a new menu item.
     *
     * @param name the Look And Feel name
     * @param description the Look And Feel description
     */
    private void addMenuItem(String name, String description) {
        JMenuItem mi = new JMenuItem(name);
        mi.setToolTipText(description);
        mi.addActionListener(this);
        LAF_MENU.insert(mi, LAF_MENU.getItemCount() - 2); // separators + mi
    }

    /**
     * Finds and returns instances of javax.swing.LookAndFeel subclasses.
     *
     * @param file the jar file that contains Look And Feel classes
     * @throws java.io.IOException
     */
    private Class[] findLookAndFeelClass(File file) throws IOException {
        List<Class> lafList = new ArrayList<Class>();
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            JarInputStream jis = new JarInputStream(is);
            JarEntry je = null;
            while ((je = jis.getNextJarEntry()) != null) {
                String name = je.getName();
                if (name.endsWith(".class")) {
                    name = name.replaceAll("/", ".");
                    name = name.substring(0, name.lastIndexOf(".class"));
                    try {
                        Class c = Class.forName(name);
                        if (LookAndFeel.class.isAssignableFrom(c)) {
                            lafList.add(c);
                        }
                    } catch (ClassNotFoundException e) {
                        // ignore
                    }
                }
            }
        } catch (IOException e) {
            throw e;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }

        return (Class[]) lafList.toArray(new Class[]{});
    }

    /**
     *
     */
    private void loadProperties() {
        TABLE.removeAllRows();
        UIDefaults defaults = UIManager.getDefaults();
        Enumeration enm = defaults.keys();
        for ( ; enm.hasMoreElements(); ) {
            Object key = enm.nextElement();
            TABLE.addRow(new Object[]{key, defaults.get(key)});
        }
        setLeftStatusText(TABLE.getRowCount() + " properties");
    }

    /**
     *
     */
    private JPanel createNorthPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);

        panel.add(FILTER_LABEL, gbc);
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(FILTER_FIELD, gbc);
        gbc.weightx = 0.0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        panel.add(IGNORE_CASE_CHECK_BOX, gbc);

        return panel;
    }

    /**
     *
     */
    private JPanel createCenterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));

        TABBED_PANE.addTab("All Properties", TABLE);
        TABBED_PANE.addTab("Fonts", FONT_TABLE);
        TABBED_PANE.addTab("Image Icons", ICON_PANEL);
        TABBED_PANE.addTab("Colors", COLOR_TABLE);
        TABBED_PANE.addTab("InputMaps", INPUT_MAP_TABLE);
        TABBED_PANE.addTab("ActionMaps", ACTION_MAP_TABLE);
        TABBED_PANE.addTab("Sounds", SOUND_TABLE);

        panel.add(TABBED_PANE);

        return panel;
    }

    /**
     *
     */
    private JPanel createSouthPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 2, 2, 0);

        L_STATUS_LABEL.setBorder(BorderFactory.createEtchedBorder());
        C_STATUS_LABEL.setBorder(BorderFactory.createEtchedBorder());
        R_STATUS_LABEL.setBorder(BorderFactory.createEtchedBorder());

        gbc.weightx = 0.1;
        panel.add(L_STATUS_LABEL, gbc);
        gbc.gridwidth = GridBagConstraints.RELATIVE;
        gbc.weightx = 0.8;
        panel.add(C_STATUS_LABEL, gbc);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 0.1;
        panel.add(R_STATUS_LABEL, gbc);

        return panel;
    }

    /**
     *
     */
    private void addClasspath(URL url) throws NoSuchMethodException,
                                              IllegalAccessException,
                                              InvocationTargetException {
        try {
            Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            method.invoke(getClass().getClassLoader(), new Object[]{url});
        } catch (NoSuchMethodException e) {
            throw e;
        } catch (IllegalAccessException e) {
            throw e;
        } catch (InvocationTargetException e) {
            throw e;
        }
    }

    /**
     * Shows the message dialog.
     *
     * @param message
     */
    private void showMessageDialog(String message) {
        JOptionPane.showMessageDialog(this, message);
    }
}
