package com.group_finity.mascot;

import com.group_finity.mascot.config.Configuration;
import com.group_finity.mascot.config.Entry;
import com.group_finity.mascot.exception.BehaviorInstantiationException;
import com.group_finity.mascot.exception.CantBeAliveException;
import com.group_finity.mascot.exception.ConfigurationException;
import com.group_finity.mascot.image.ImagePairs;
import com.group_finity.mascot.imagesetchooser.ImageSetChooser;
import com.group_finity.mascot.sound.Sounds;
import com.joconner.i18n.Utf8ResourceBundleControl;
import com.nilo.plaf.nimrod.NimRODLookAndFeel;
import com.nilo.plaf.nimrod.NimRODTheme;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Program entry point.
 * <p>
 * Original Author: Yuki Yamada of Group Finity (http://www.group-finity.com/Shimeji/)
 * Currently developed by Shimeji-ee Group.
 */
public class Main {
    private static final Logger log = Logger.getLogger(Main.class.getName());
    // Action that matches the "Gather Around Mouse!" context menu command
    static final String BEHAVIOR_GATHER = "ChaseMouse";

    static {
        try {
            LogManager.getLogManager().readConfiguration(Files.newInputStream(Paths.get("./conf/logging.properties")));
        } catch (final SecurityException | IOException e) {
            System.out.println("error on log manager reading configuration from 'logging.properties'");
            e.printStackTrace();
        } catch (OutOfMemoryError err) {
            log.log(Level.SEVERE, "Out of Memory Exception.  There are probably have too many "
                    + "Shimeji mascots in the image folder for your computer to handle.  Select fewer"
                    + " image sets or move some to the img/unused folder and try again.", err);
            Main.showError("Out of Memory.  There are probably have too many \n"
                    + "Shimeji mascots for your computer to handle.\n"
                    + "Select fewer image sets or move some to the \n"
                    + "img/unused folder and try again.");
            System.exit(0);
        }
    }

    private final Manager manager = new Manager();
    private ArrayList<String> imageSets = new ArrayList<>();
    private final ConcurrentHashMap<String, Configuration> configurations = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ArrayList<String>> childImageSets = new ConcurrentHashMap<>();
    private static final Main instance = new Main();
    private Properties properties = new Properties();
    private Platform platform;
    private ResourceBundle languageBundle;

    private JDialog form;

    public static Main getInstance() {
        return instance;
    }

    private static final JFrame frame = new javax.swing.JFrame();

    public static void showError(String message) {
        JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(final String[] args) {
        try {
            getInstance().run();
        } catch (OutOfMemoryError err) {
            log.log(Level.SEVERE, "Out of Memory Exception.  There are probably have too many "
                    + "Shimeji mascots in the image folder for your computer to handle.  Select fewer"
                    + " image sets or move some to the img/unused folder and try again.", err);
            Main.showError("Out of Memory.  There are probably have too many \n"
                    + "Shimeji mascots for your computer to handle.\n"
                    + "Select fewer image sets or move some to the \n"
                    + "img/unused folder and try again.");
            System.exit(0);
        }
    }

    public void run() {
        // test operating system
        if (!System.getProperty("sun.arch.data.model").equals("64"))
            platform = Platform.x86;
        else
            platform = Platform.x86_64;

        // load properties
        properties = new Properties();
        FileInputStream input;
        try {
            input = new FileInputStream("./conf/settings.properties");
            properties.load(input);
        } catch (IOException ex) {
            System.out.println("error on properties loading ./conf/settings.properties");
        }
        Manager.TICK_INTERVAL = Integer.parseInt(Main.getInstance().getProperties().getProperty("Tickrate", "35"));
        Manager.isMascotPopUpShowing = Boolean.parseBoolean(Main.getInstance().getProperties().getProperty("MascotsPopUpShowing", "true"));

        // load languages
        try {
            ResourceBundle.Control utf8Control = new Utf8ResourceBundleControl(false);
            languageBundle = ResourceBundle.getBundle("language", Locale.forLanguageTag(properties.getProperty("Language", "en-GB")), utf8Control);
        } catch (Exception ex) {
            Main.showError("The default language file could not be loaded. Ensure that you have the latest shimeji language.properties in your conf directory.");
            exit();
        }

        // load theme
        try {
            // default light theme
            NimRODLookAndFeel lookAndFeel = new NimRODLookAndFeel();

            // check for theme properties
            NimRODTheme theme = null;
            try {
                if (new File("./conf/theme.properties").isFile()) {
                    theme = new NimRODTheme("./conf/theme.properties");
                }
            } catch (Exception exc) {
                theme = null;
            }

            if (theme == null) {
                // default back to light theme if not found/valid
                theme = new NimRODTheme();
                theme.setPrimary1(Color.decode("#1EA6EB"));
                theme.setPrimary2(Color.decode("#28B0F5"));
                theme.setPrimary3(Color.decode("#32BAFF"));
                theme.setSecondary1(Color.decode("#BCBCBE"));
                theme.setSecondary2(Color.decode("#C6C6C8"));
                theme.setSecondary3(Color.decode("#D0D0D2"));
                theme.setMenuOpacity(255);
                theme.setFrameOpacity(255);
            }

            // handle menu size
            if (!properties.containsKey("MenuDPI")) {
                properties.setProperty("MenuDPI", String.valueOf(Math.max(java.awt.Toolkit.getDefaultToolkit().getScreenResolution(), 96)));
                updateConfigFile();
            }
            float menuScaling = Float.parseFloat(properties.getProperty("MenuDPI", "96")) / 96;
            java.awt.Font font = theme.getUserTextFont().deriveFont(theme.getUserTextFont().getSize() * menuScaling);
            theme.setFont(font);

            NimRODLookAndFeel.setCurrentTheme(theme);
            JFrame.setDefaultLookAndFeelDecorated(true);
            JDialog.setDefaultLookAndFeelDecorated(true);
            // all done
            lookAndFeel.initialize();
            UIManager.setLookAndFeel(lookAndFeel);
        } catch (Exception ex) {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ex1) {
                log.log(Level.SEVERE, "Look & Feel unsupported.", ex1);
                exit();
            }
        }

        // Get the image sets to use
        if (!Boolean.parseBoolean(properties.getProperty("AlwaysShowShimejiChooser", "false"))) {
            for (String set : properties.getProperty("ActiveShimeji", "").split("/"))
                if (!set.trim().isEmpty())
                    imageSets.add(set.trim());
        }
        if (imageSets.isEmpty()) {
            imageSets = new ImageSetChooser(frame, true).display();
            if (imageSets == null) {
                exit();
            }
        }

        // Load settings
        for (int index = 0; index < imageSets.size(); index++) {
            if (!loadConfiguration(imageSets.get(index))) {
                // failed validation
                configurations.remove(imageSets.get(index));
                imageSets.remove(imageSets.get(index));
                index--;
            }
        }
        if (imageSets.isEmpty()) {
            exit();
        }

        // Create the tray icon
        if (Boolean.parseBoolean(Main.getInstance().getProperties().getProperty("CreateTrayIcon", "true")))
            createTrayIcon();

        for (String imageSet : imageSets) {
            createMascot(imageSet);
        }

        getManager().start();
    }

    private boolean loadConfiguration(final String imageSet) {
        try {
            // try to load in the correct xml files
            String filePath = "./conf/";
            String actionsFile = filePath + "actions.xml";
            if (new File(filePath + "動作.xml").exists())
                actionsFile = filePath + "動作.xml";

            filePath = "./conf/" + imageSet + "/";
            if (new File(filePath + "actions.xml").exists())
                actionsFile = filePath + "actions.xml";
            else if (new File(filePath + "動作.xml").exists())
                actionsFile = filePath + "動作.xml";
            else if (new File(filePath + "Õïòõ¢£.xml").exists())
                actionsFile = filePath + "Õïòõ¢£.xml";
            else if (new File(filePath + "¦-º@.xml").exists())
                actionsFile = filePath + "¦-º@.xml";
            else if (new File(filePath + "ô«ìý.xml").exists())
                actionsFile = filePath + "ô«ìý.xml";
            else if (new File(filePath + "one.xml").exists())
                actionsFile = filePath + "one.xml";
            else if (new File(filePath + "1.xml").exists())
                actionsFile = filePath + "1.xml";

            filePath = "./img/" + imageSet + "/conf/";
            if (new File(filePath + "actions.xml").exists())
                actionsFile = filePath + "actions.xml";
            else if (new File(filePath + "動作.xml").exists())
                actionsFile = filePath + "動作.xml";
            else if (new File(filePath + "Õïòõ¢£.xml").exists())
                actionsFile = filePath + "Õïòõ¢£.xml";
            else if (new File(filePath + "¦-º@.xml").exists())
                actionsFile = filePath + "¦-º@.xml";
            else if (new File(filePath + "ô«ìý.xml").exists())
                actionsFile = filePath + "ô«ìý.xml";
            else if (new File(filePath + "one.xml").exists())
                actionsFile = filePath + "one.xml";
            else if (new File(filePath + "1.xml").exists())
                actionsFile = filePath + "1.xml";

            log.log(Level.INFO, imageSet + " Read Action File ({0})", actionsFile);


            final Document actions = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
                    Files.newInputStream(new File(actionsFile).toPath()));

            Configuration configuration = new Configuration();

            configuration.load(new Entry(actions.getDocumentElement()), imageSet);

            filePath = "./conf/";
            String behaviorsFile = filePath + "behaviors.xml";
            if (new File(filePath + "行動.xml").exists())
                behaviorsFile = filePath + "行動.xml";

            filePath = "./conf/" + imageSet + "/";
            if (new File(filePath + "behaviors.xml").exists())
                behaviorsFile = filePath + "behaviors.xml";
            else if (new File(filePath + "behavior.xml").exists())
                behaviorsFile = filePath + "behavior.xml";
            else if (new File(filePath + "行動.xml").exists())
                behaviorsFile = filePath + "行動.xml";
            else if (new File(filePath + "ÞíîÕïò.xml").exists())
                behaviorsFile = filePath + "ÞíîÕïò.xml";
            else if (new File(filePath + "ªµ¦-.xml").exists())
                behaviorsFile = filePath + "ªµ¦-.xml";
            else if (new File(filePath + "ìsô«.xml").exists())
                behaviorsFile = filePath + "ìsô«.xml";
            else if (new File(filePath + "two.xml").exists())
                behaviorsFile = filePath + "two.xml";
            else if (new File(filePath + "2.xml").exists())
                behaviorsFile = filePath + "2.xml";

            filePath = "./img/" + imageSet + "/conf/";
            if (new File(filePath + "behaviors.xml").exists())
                behaviorsFile = filePath + "behaviors.xml";
            else if (new File(filePath + "behavior.xml").exists())
                behaviorsFile = filePath + "behavior.xml";
            else if (new File(filePath + "行動.xml").exists())
                behaviorsFile = filePath + "行動.xml";
            else if (new File(filePath + "ÞíîÕïò.xml").exists())
                behaviorsFile = filePath + "ÞíîÕïò.xml";
            else if (new File(filePath + "ªµ¦-.xml").exists())
                behaviorsFile = filePath + "ªµ¦-.xml";
            else if (new File(filePath + "ìsô«.xml").exists())
                behaviorsFile = filePath + "ìsô«.xml";
            else if (new File(filePath + "two.xml").exists())
                behaviorsFile = filePath + "two.xml";
            else if (new File(filePath + "2.xml").exists())
                behaviorsFile = filePath + "2.xml";

            log.log(Level.INFO, imageSet + " Read Behavior File ({0})", behaviorsFile);

            final Document behaviors = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(Files.newInputStream(Paths.get(behaviorsFile)));


            configuration.load(new Entry(behaviors.getDocumentElement()), imageSet);

            configuration.validate();

            configurations.put(imageSet, configuration);

            ArrayList<String> childMascots = new ArrayList<>();

            // born mascot bit goes here...
            for (final Entry list : new Entry(actions.getDocumentElement()).selectChildren("ActionList")) {
                for (final Entry node : list.selectChildren("Action")) {
                    if (node.getAttributes().containsKey("BornMascot")) {
                        String set = node.getAttribute("BornMascot");
                        if (!childMascots.contains(set))
                            childMascots.add(set);
                        if (!configurations.containsKey(set))
                            loadConfiguration(set);
                    }
                    if (node.getAttributes().containsKey("TransformMascot")) {
                        String set = node.getAttribute("TransformMascot");
                        if (!childMascots.contains(set))
                            childMascots.add(set);
                        if (!configurations.containsKey(set))
                            loadConfiguration(set);
                    }
                }
            }

            childImageSets.put(imageSet, childMascots);

            return true;
        } catch (final SAXException | ConfigurationException | ParserConfigurationException e) {
            e.printStackTrace();
            log.log(Level.SEVERE, "Failed to load configuration files", e);
            Main.showError(languageBundle.getString("FailedLoadConfigErrorMessage") + "\n" + e.getMessage() + "\n" + languageBundle.getString("SeeLogForDetails"));
        } catch (final Exception e) {
            e.printStackTrace();
            log.log(Level.SEVERE, "Failed to load configuration files", e);
            Main.showError(languageBundle.getString("FailedLoadConfigErrorMessage") + "\n" + e.getMessage() + "\n" + languageBundle.getString("SeeLogForDetails"));
        }

        return false;
    }

    /**
     * Create a tray icon.
     *
     * @ Throws AWTException
     * @ Throws IOException
     */
    private void createTrayIcon() {
        log.log(Level.INFO, "create a tray icon");

        // get the tray icon image
        BufferedImage image = null;
        try {
            image = ImageIO.read(Files.newInputStream(Paths.get("./icon.png")));
        } catch (final Exception e) {
            log.log(Level.SEVERE, "Failed to create tray icon", e);
            Main.showError(languageBundle.getString("FailedDisplaySystemTrayErrorMessage") + "\n" + languageBundle.getString("SeeLogForDetails"));
        } finally {
            if (image == null)
                image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
        }

        try {
            // Create the tray icon
            final TrayIcon icon = new TrayIcon(image, languageBundle.getString("ShimejiEE"));

            // attach menu
            icon.addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent event) {
                }

                @Override
                public void mousePressed(MouseEvent e) {
                }

                @Override
                public void mouseReleased(MouseEvent event) {
                    if (event.isPopupTrigger()) {
                        // close the form if it's open
                        if (form != null)
                            form.dispose();

                        // create the form and border
                        form = new JDialog(frame, false);
                        final JPanel panel = new JPanel();
                        panel.setBorder(new EmptyBorder(5, 5, 5, 5));
                        form.add(panel);

                        // buttons and action handling
                        JButton btnCallShimeji = new JButton(languageBundle.getString("CallShimeji"));
                        btnCallShimeji.addActionListener(new ActionListener() {
                            public void actionPerformed(final ActionEvent event) {
                                createMascot();
                                form.dispose();
                            }
                        });

                        JButton btnFollowCursor = new JButton(languageBundle.getString("FollowCursor"));
                        btnFollowCursor.addActionListener(new ActionListener() {
                            public void actionPerformed(final ActionEvent event) {
                                getManager().setBehaviorAll(BEHAVIOR_GATHER);
                                form.dispose();
                            }
                        });

                        JButton btnReduceToOne = new JButton(languageBundle.getString("ReduceToOne"));
                        btnReduceToOne.addActionListener(new ActionListener() {
                            public void actionPerformed(final ActionEvent event) {
                                getManager().remainOne();
                                form.dispose();
                            }
                        });

                        JButton btnRestoreWindows = new JButton(languageBundle.getString("RestoreWindows"));
                        btnRestoreWindows.addActionListener(new ActionListener() {
                            public void actionPerformed(final ActionEvent event) {
                                NativeFactory.getInstance().getEnvironment().restoreIE();
                                form.dispose();
                            }
                        });

                        final JButton btnAllowedBehaviours = new JButton(languageBundle.getString("AllowedBehaviours"));
                        btnAllowedBehaviours.addMouseListener(new MouseListener() {
                            @Override
                            public void mouseClicked(MouseEvent e) {
                            }

                            @Override
                            public void mousePressed(MouseEvent e) {
                            }

                            @Override
                            public void mouseReleased(MouseEvent e) {
                                btnAllowedBehaviours.setEnabled(true);
                            }

                            @Override
                            public void mouseEntered(MouseEvent e) {
                            }

                            @Override
                            public void mouseExited(MouseEvent e) {
                            }
                        });
                        btnAllowedBehaviours.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(final ActionEvent event) {
                                // "Disable Breeding" menu item
                                final JCheckBoxMenuItem breedingMenu = new JCheckBoxMenuItem(languageBundle.getString("BreedingCloning"), Boolean.parseBoolean(properties.getProperty("Breeding", "true")));
                                breedingMenu.addItemListener(new ItemListener() {
                                    public void itemStateChanged(final ItemEvent e) {
                                        breedingMenu.setState(toggleBooleanSetting("Breeding", true));
                                        updateConfigFile();
                                        btnAllowedBehaviours.setEnabled(true);
                                    }
                                });

                                // "Disable Breeding Transient" menu item
                                final JCheckBoxMenuItem transientMenu = new JCheckBoxMenuItem(languageBundle.getString("BreedingTransient"), Boolean.parseBoolean(properties.getProperty("Transients", "true")));
                                transientMenu.addItemListener(new ItemListener() {
                                    public void itemStateChanged(final ItemEvent e) {
                                        transientMenu.setState(toggleBooleanSetting("Transients", true));
                                        updateConfigFile();
                                        btnAllowedBehaviours.setEnabled(true);
                                    }
                                });

                                // "Disable Transformations" menu item
                                final JCheckBoxMenuItem transformationMenu = new JCheckBoxMenuItem(languageBundle.getString("Transformation"), Boolean.parseBoolean(properties.getProperty("Transformation", "true")));
                                transformationMenu.addItemListener(new ItemListener() {
                                    public void itemStateChanged(final ItemEvent e) {
                                        transformationMenu.setState(toggleBooleanSetting("Transformation", true));
                                        updateConfigFile();
                                        btnAllowedBehaviours.setEnabled(true);
                                    }
                                });

                                // "Throwing Windows" menu item
                                final JCheckBoxMenuItem throwingMenu = new JCheckBoxMenuItem(languageBundle.getString("ThrowingWindows"), Boolean.parseBoolean(properties.getProperty("Throwing", "true")));
                                throwingMenu.addItemListener(new ItemListener() {
                                    public void itemStateChanged(final ItemEvent e) {
                                        throwingMenu.setState(toggleBooleanSetting("Throwing", true));
                                        updateConfigFile();
                                        btnAllowedBehaviours.setEnabled(true);
                                    }
                                });

                                // "Mute Sounds" menu item
                                final JCheckBoxMenuItem soundsMenu = new JCheckBoxMenuItem(languageBundle.getString("SoundEffects"), Boolean.parseBoolean(properties.getProperty("Sounds", "true")));
                                soundsMenu.addItemListener(new ItemListener() {
                                    public void itemStateChanged(final ItemEvent e) {
                                        boolean result = toggleBooleanSetting("Sounds", true);
                                        soundsMenu.setState(result);
                                        Sounds.setMuted(!result);
                                        updateConfigFile();
                                        btnAllowedBehaviours.setEnabled(true);
                                    }
                                });

                                // "Multiscreen" menu item
                                final JCheckBoxMenuItem multiscreenMenu = new JCheckBoxMenuItem(languageBundle.getString("Multiscreen"), Boolean.parseBoolean(properties.getProperty("Multiscreen", "true")));
                                multiscreenMenu.addItemListener(new ItemListener() {
                                    public void itemStateChanged(final ItemEvent e) {
                                        multiscreenMenu.setState(toggleBooleanSetting("Multiscreen", true));
                                        updateConfigFile();
                                        btnAllowedBehaviours.setEnabled(true);
                                    }
                                });

                                JPopupMenu behaviourPopup = new JPopupMenu();
                                behaviourPopup.add(breedingMenu);
                                behaviourPopup.add(transientMenu);
                                behaviourPopup.add(transformationMenu);
                                behaviourPopup.add(throwingMenu);
                                behaviourPopup.add(soundsMenu);
                                behaviourPopup.add(multiscreenMenu);
                                behaviourPopup.addPopupMenuListener(new PopupMenuListener() {
                                    @Override
                                    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                                    }

                                    @Override
                                    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                                        if (panel.getMousePosition() != null) {
                                            btnAllowedBehaviours.setEnabled(!(panel.getMousePosition().x > btnAllowedBehaviours.getX() &&
                                                    panel.getMousePosition().x < btnAllowedBehaviours.getX() + btnAllowedBehaviours.getWidth() &&
                                                    panel.getMousePosition().y > btnAllowedBehaviours.getY() &&
                                                    panel.getMousePosition().y < btnAllowedBehaviours.getY() + btnAllowedBehaviours.getHeight()));
                                        } else {
                                            btnAllowedBehaviours.setEnabled(true);
                                        }
                                    }

                                    @Override
                                    public void popupMenuCanceled(PopupMenuEvent e) {
                                    }
                                });
                                behaviourPopup.show(btnAllowedBehaviours, 0, btnAllowedBehaviours.getHeight());
                                btnAllowedBehaviours.requestFocusInWindow();
                            }
                        });

                        final JButton btnChooseShimeji = new JButton(languageBundle.getString("ChooseShimeji"));
                        btnChooseShimeji.addActionListener(new ActionListener() {
                            public void actionPerformed(final ActionEvent event) {
                                form.dispose();
                                ImageSetChooser chooser = new ImageSetChooser(frame, true);
                                chooser.setIconImage(icon.getImage());
                                setActiveImageSets(chooser.display());
                            }
                        });

                        final JButton btnSettings = new JButton(languageBundle.getString("Settings"));
                        btnSettings.addActionListener(new ActionListener() {
                            public void actionPerformed(final ActionEvent event) {
                                form.dispose();
                                SettingsWindow dialog = new SettingsWindow(frame, true);
                                dialog.setIconImage(icon.getImage());
                                dialog.display();

                                if (dialog.getEnvironmentReloadRequired()) {
                                    NativeFactory.getInstance().getEnvironment().dispose();
                                    NativeFactory.resetInstance();
                                }
                                if (dialog.getEnvironmentReloadRequired() || dialog.getImageReloadRequired()) {
                                    // need to reload the shimeji as the images have rescaled
                                    boolean isExit = getManager().isExitOnLastRemoved();
                                    getManager().setExitOnLastRemoved(false);
                                    getManager().disposeAll();

                                    // Wipe all loaded data
                                    ImagePairs.clear();
                                    configurations.clear();

                                    // Load settings
                                    for (String imageSet : imageSets) {
                                        loadConfiguration(imageSet);
                                    }

                                    // Create the first mascot
                                    for (String imageSet : imageSets) {
                                        createMascot(imageSet);
                                    }

                                    Main.this.getManager().setExitOnLastRemoved(isExit);
                                }
                                if (dialog.getInteractiveWindowReloadRequired())
                                    NativeFactory.getInstance().getEnvironment().refreshCache();
                            }
                        });

                        final JButton btnLanguage = new JButton(languageBundle.getString("Language"));
                        btnLanguage.addMouseListener(new MouseListener() {
                            @Override
                            public void mouseClicked(MouseEvent e) {
                            }

                            @Override
                            public void mousePressed(MouseEvent e) {
                            }

                            @Override
                            public void mouseReleased(MouseEvent e) {
                                btnLanguage.setEnabled(true);
                            }

                            @Override
                            public void mouseEntered(MouseEvent e) {
                            }

                            @Override
                            public void mouseExited(MouseEvent e) {
                            }
                        });
                        btnLanguage.addActionListener(new ActionListener() {
                            public void actionPerformed(final ActionEvent e) {
                                // English menu item
                                final JMenuItem englishMenu = new JMenuItem("English");
                                englishMenu.addActionListener(new ActionListener() {
                                    public void actionPerformed(final ActionEvent e) {
                                        form.dispose();
                                        updateLanguage("en-GB");
                                        updateConfigFile();
                                    }
                                });

                                // Arabic menu item
                                final JMenuItem arabicMenu = new JMenuItem("عربي");
                                arabicMenu.addActionListener(new ActionListener() {
                                    public void actionPerformed(final ActionEvent e) {
                                        form.dispose();
                                        updateLanguage("ar-SA");
                                        updateConfigFile();
                                    }
                                });

                                // Catalan menu item
                                final JMenuItem catalanMenu = new JMenuItem("Català");
                                catalanMenu.addActionListener(new ActionListener() {
                                    public void actionPerformed(final ActionEvent e) {
                                        form.dispose();
                                        updateLanguage("ca-ES");
                                        updateConfigFile();
                                    }
                                });

                                // German menu item
                                final JMenuItem germanMenu = new JMenuItem("Deutsch");
                                germanMenu.addActionListener(new ActionListener() {
                                    public void actionPerformed(final ActionEvent e) {
                                        form.dispose();
                                        updateLanguage("de-DE");
                                        updateConfigFile();
                                    }
                                });

                                // Spanish menu item
                                final JMenuItem spanishMenu = new JMenuItem("Español");
                                spanishMenu.addActionListener(new ActionListener() {
                                    public void actionPerformed(final ActionEvent e) {
                                        form.dispose();
                                        updateLanguage("es-ES");
                                        updateConfigFile();
                                    }
                                });

                                // French menu item
                                final JMenuItem frenchMenu = new JMenuItem("Français");
                                frenchMenu.addActionListener(new ActionListener() {
                                    public void actionPerformed(final ActionEvent e) {
                                        form.dispose();
                                        updateLanguage("fr-FR");
                                        updateConfigFile();
                                    }
                                });

                                // Croatian menu item
                                final JMenuItem croatianMenu = new JMenuItem("Hrvatski");
                                croatianMenu.addActionListener(new ActionListener() {
                                    public void actionPerformed(final ActionEvent e) {
                                        form.dispose();
                                        updateLanguage("hr-HR");
                                        updateConfigFile();
                                    }
                                });

                                // Italian menu item
                                final JMenuItem italianMenu = new JMenuItem("Italiano");
                                italianMenu.addActionListener(new ActionListener() {
                                    public void actionPerformed(final ActionEvent e) {
                                        form.dispose();
                                        updateLanguage("it-IT");
                                        updateConfigFile();
                                    }
                                });

                                // Dutch menu item
                                final JMenuItem dutchMenu = new JMenuItem("Nederlands");
                                dutchMenu.addActionListener(new ActionListener() {
                                    public void actionPerformed(final ActionEvent e) {
                                        form.dispose();
                                        updateLanguage("nl-NL");
                                        updateConfigFile();
                                    }
                                });

                                // Polish menu item
                                final JMenuItem polishMenu = new JMenuItem("Polski");
                                polishMenu.addActionListener(new ActionListener() {
                                    public void actionPerformed(final ActionEvent e) {
                                        form.dispose();
                                        updateLanguage("pl-PL");
                                        updateConfigFile();
                                    }
                                });

                                // Brazilian Portuguese menu item
                                final JMenuItem brazilianPortugueseMenu = new JMenuItem("Português Brasileiro");
                                brazilianPortugueseMenu.addActionListener(new ActionListener() {
                                    public void actionPerformed(final ActionEvent e) {
                                        form.dispose();
                                        updateLanguage("pt-BR");
                                        updateConfigFile();
                                    }
                                });

                                // Portuguese menu item
                                final JMenuItem portugueseMenu = new JMenuItem("Português");
                                portugueseMenu.addActionListener(new ActionListener() {
                                    public void actionPerformed(final ActionEvent e) {
                                        form.dispose();
                                        updateLanguage("pt-PT");
                                        updateConfigFile();
                                    }
                                });

                                // Russian menu item
                                final JMenuItem russianMenu = new JMenuItem("ру́сский язы́к");
                                russianMenu.addActionListener(new ActionListener() {
                                    public void actionPerformed(final ActionEvent e) {
                                        form.dispose();
                                        updateLanguage("ru-RU");
                                        updateConfigFile();
                                    }
                                });

                                // Romanian menu item
                                final JMenuItem romanianMenu = new JMenuItem("Română");
                                romanianMenu.addActionListener(new ActionListener() {
                                    public void actionPerformed(final ActionEvent e) {
                                        form.dispose();
                                        updateLanguage("ro-RO");
                                        updateConfigFile();
                                    }
                                });

                                // Srpski menu item
                                final JMenuItem serbianMenu = new JMenuItem("Srpski");
                                serbianMenu.addActionListener(new ActionListener() {
                                    public void actionPerformed(final ActionEvent e) {
                                        form.dispose();
                                        updateLanguage("sr-RS");
                                        updateConfigFile();
                                    }
                                });

                                // Finnish menu item
                                final JMenuItem finnishMenu = new JMenuItem("Suomi");
                                finnishMenu.addActionListener(new ActionListener() {
                                    public void actionPerformed(final ActionEvent e) {
                                        form.dispose();
                                        updateLanguage("fi-FI");
                                        updateConfigFile();
                                    }
                                });

                                // Vietnamese menu item
                                final JMenuItem vietnameseMenu = new JMenuItem("tiếng Việt");
                                vietnameseMenu.addActionListener(new ActionListener() {
                                    public void actionPerformed(final ActionEvent e) {
                                        form.dispose();
                                        updateLanguage("vi-VN");
                                        updateConfigFile();
                                    }
                                });

                                // Chinese menu item
                                final JMenuItem chineseMenu = new JMenuItem("简体中文");
                                chineseMenu.addActionListener(new ActionListener() {
                                    public void actionPerformed(final ActionEvent e) {
                                        form.dispose();
                                        updateLanguage("zh-CN");
                                        updateConfigFile();
                                    }
                                });

                                // Chinese (Traditional) menu item
                                final JMenuItem chineseTraditionalMenu = new JMenuItem("繁體中文");
                                chineseTraditionalMenu.addActionListener(new ActionListener() {
                                    public void actionPerformed(final ActionEvent e) {
                                        form.dispose();
                                        updateLanguage("zh-TW");
                                        updateConfigFile();
                                    }
                                });

                                // Korean menu item
                                final JMenuItem koreanMenu = new JMenuItem("한국어");
                                koreanMenu.addActionListener(new ActionListener() {
                                    public void actionPerformed(final ActionEvent e) {
                                        form.dispose();
                                        updateLanguage("ko-KR");
                                        updateConfigFile();
                                    }
                                });

                                // Japanese menu item
                                final JMenuItem japaneseMenu = new JMenuItem("日本語");
                                japaneseMenu.addActionListener(new ActionListener() {
                                    public void actionPerformed(final ActionEvent e) {
                                        form.dispose();
                                        updateLanguage("ja-JP");
                                        updateConfigFile();
                                    }
                                });

                                JPopupMenu languagePopup = new JPopupMenu();
                                languagePopup.add(englishMenu);
                                languagePopup.addSeparator();
                                languagePopup.add(arabicMenu);
                                languagePopup.add(catalanMenu);
                                languagePopup.add(germanMenu);
                                languagePopup.add(spanishMenu);
                                languagePopup.add(frenchMenu);
                                languagePopup.add(croatianMenu);
                                languagePopup.add(italianMenu);
                                languagePopup.add(dutchMenu);
                                languagePopup.add(polishMenu);
                                languagePopup.add(portugueseMenu);
                                languagePopup.add(brazilianPortugueseMenu);
                                languagePopup.add(russianMenu);
                                languagePopup.add(romanianMenu);
                                languagePopup.add(serbianMenu);
                                languagePopup.add(finnishMenu);
                                languagePopup.add(vietnameseMenu);
                                languagePopup.add(chineseMenu);
                                languagePopup.add(chineseTraditionalMenu);
                                languagePopup.add(koreanMenu);
                                languagePopup.add(japaneseMenu);
                                languagePopup.addPopupMenuListener(new PopupMenuListener() {
                                    @Override
                                    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                                    }

                                    @Override
                                    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                                        if (panel.getMousePosition() != null) {
                                            btnLanguage.setEnabled(!(panel.getMousePosition().x > btnLanguage.getX() &&
                                                    panel.getMousePosition().x < btnLanguage.getX() + btnLanguage.getWidth() &&
                                                    panel.getMousePosition().y > btnLanguage.getY() &&
                                                    panel.getMousePosition().y < btnLanguage.getY() + btnLanguage.getHeight()));
                                        } else {
                                            btnLanguage.setEnabled(true);
                                        }
                                    }

                                    @Override
                                    public void popupMenuCanceled(PopupMenuEvent e) {
                                    }
                                });
                                languagePopup.show(btnLanguage, 0, btnLanguage.getHeight());
                                btnLanguage.requestFocusInWindow();
                            }
                        });

                        JButton btnPauseAll = new JButton(getManager().isPaused() ? languageBundle.getString("ResumeAnimations") : languageBundle.getString("PauseAnimations"));
                        btnPauseAll.addActionListener(new ActionListener() {
                            public void actionPerformed(final ActionEvent e) {
                                form.dispose();
                                getManager().togglePauseAll();
                            }
                        });

                        JButton btnDismissAll = new JButton(languageBundle.getString("DismissAll"));
                        btnDismissAll.addActionListener(new ActionListener() {
                            public void actionPerformed(final ActionEvent e) {
                                exit();
                            }
                        });

                        // layout
                        float scaling = Float.parseFloat(properties.getProperty("MenuDPI", "96")) / 96;
                        panel.setLayout(new java.awt.GridBagLayout());
                        GridBagConstraints gridBag = new GridBagConstraints();
                        gridBag.fill = GridBagConstraints.HORIZONTAL;
                        gridBag.gridx = 0;
                        gridBag.gridy = 0;
                        panel.add(btnCallShimeji, gridBag);
                        gridBag.insets = new Insets((int) (5 * scaling), 0, 0, 0);
                        gridBag.gridy++;
                        panel.add(btnFollowCursor, gridBag);
                        gridBag.gridy++;
                        panel.add(btnReduceToOne, gridBag);
                        gridBag.gridy++;
                        panel.add(btnRestoreWindows, gridBag);
                        gridBag.gridy++;
                        panel.add(new JSeparator(), gridBag);
                        gridBag.gridy++;
                        panel.add(btnAllowedBehaviours, gridBag);
                        gridBag.gridy++;
                        panel.add(btnChooseShimeji, gridBag);
                        gridBag.gridy++;
                        panel.add(btnSettings, gridBag);
                        gridBag.gridy++;
                        panel.add(btnLanguage, gridBag);
                        gridBag.gridy++;
                        panel.add(new JSeparator(), gridBag);
                        gridBag.gridy++;
                        panel.add(btnPauseAll, gridBag);
                        gridBag.gridy++;
                        panel.add(btnDismissAll, gridBag);

                        form.setIconImage(icon.getImage());
                        form.setTitle(languageBundle.getString("ShimejiEE"));
                        form.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
                        form.setAlwaysOnTop(true);

                        // set the form dimensions
                        java.awt.FontMetrics metrics = btnCallShimeji.getFontMetrics(btnCallShimeji.getFont());
                        int width = metrics.stringWidth(btnCallShimeji.getText());
                        width = Math.max(metrics.stringWidth(btnFollowCursor.getText()), width);
                        width = Math.max(metrics.stringWidth(btnReduceToOne.getText()), width);
                        width = Math.max(metrics.stringWidth(btnRestoreWindows.getText()), width);
                        width = Math.max(metrics.stringWidth(btnAllowedBehaviours.getText()), width);
                        width = Math.max(metrics.stringWidth(btnChooseShimeji.getText()), width);
                        width = Math.max(metrics.stringWidth(btnSettings.getText()), width);
                        width = Math.max(metrics.stringWidth(btnLanguage.getText()), width);
                        width = Math.max(metrics.stringWidth(btnPauseAll.getText()), width);
                        width = Math.max(metrics.stringWidth(btnDismissAll.getText()), width);
                        panel.setPreferredSize(new Dimension(width + 64,
                                (int) (24 * scaling) + // 12 padding on top and bottom
                                        (int) (75 * scaling) + // 13 insets of 5 height normally
                                        10 * metrics.getHeight() + // 10 button faces
                                        84));
                        form.pack();

                        // setting location of the form
                        form.setLocation(event.getPoint().x - form.getWidth(), event.getPoint().y - form.getHeight());

                        // make sure that it is on the screen if people are using exotic taskbar locations
                        Rectangle screen = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
                        if (form.getX() < screen.getX()) {
                            form.setLocation(event.getPoint().x, form.getY());
                        }
                        if (form.getY() < screen.getY()) {
                            form.setLocation(form.getX(), event.getPoint().y);
                        }
                        form.setVisible(true);
                        form.setMinimumSize(form.getSize());
                    } else if (event.getButton() == MouseEvent.BUTTON1) {
                        createMascot();
                    } else if (event.getButton() == MouseEvent.BUTTON2 && event.getClickCount() == 2) {
                        if (getManager().isExitOnLastRemoved()) {
                            getManager().setExitOnLastRemoved(false);
                            getManager().disposeAll();
                        } else {
                            for (String imageSet : imageSets) {
                                createMascot(imageSet);
                            }
                            getManager().setExitOnLastRemoved(true);
                        }
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                }

                @Override
                public void mouseExited(MouseEvent e) {
                }
            });

            // Show tray icon
            SystemTray.getSystemTray().add(icon);
        } catch (final AWTException e) {
            log.log(Level.SEVERE, "Failed to create tray icon", e);
            Main.showError(languageBundle.getString("FailedDisplaySystemTrayErrorMessage") + "\n" + languageBundle.getString("SeeLogForDetails"));
            exit();
        }
    }

    // Randomly creates a mascot
    public void createMascot() {
        int length = imageSets.size();
        int random = (int) (length * Math.random());
        createMascot(imageSets.get(random));
    }

    /**
     * Create a mascot
     */
    public void createMascot(String imageSet) {
        log.log(Level.INFO, "create a mascot");

        // Create one mascot
        final Mascot mascot = new Mascot(imageSet);

        // Create it outside the bounds of the screen
        mascot.setAnchor(new Point(-4000, -4000));

        // Randomize the initial orientation
        mascot.setLookRight(Math.random() < 0.5);

        try {
            mascot.setBehavior(getConfiguration(imageSet).buildBehavior(null, mascot));
            this.getManager().add(mascot);
        } catch (final BehaviorInstantiationException e) {
            log.log(Level.SEVERE, "Failed to initialize the first action", e);
            Main.showError(languageBundle.getString("FailedInitialiseFirstActionErrorMessage") + "\n" + e.getMessage() + "\n" + languageBundle.getString("SeeLogForDetails"));
            mascot.dispose();
        } catch (final CantBeAliveException e) {
            log.log(Level.SEVERE, "Fatal Error", e);
            Main.showError(languageBundle.getString("FailedInitialiseFirstActionErrorMessage") + "\n" + e.getMessage() + "\n" + languageBundle.getString("SeeLogForDetails"));
            mascot.dispose();
        } catch (Exception e) {
            log.log(Level.SEVERE, imageSet + " fatal error, can not be started.", e);
            Main.showError(languageBundle.getString("CouldNotCreateShimejiErrorMessage") + " " + imageSet + ".\n" + e.getMessage() + "\n" + languageBundle.getString("SeeLogForDetails"));
            mascot.dispose();
        }
    }

    private void refreshLanguage() {
        ResourceBundle.Control utf8Control = new Utf8ResourceBundleControl(false);
        languageBundle = ResourceBundle.getBundle("language", Locale.forLanguageTag(properties.getProperty("Language", "en-GB")), utf8Control);

        boolean isExit = getManager().isExitOnLastRemoved();
        getManager().setExitOnLastRemoved(false);
        getManager().disposeAll();

        // Load settings
        for (String imageSet : imageSets) {
            loadConfiguration(imageSet);
        }

        // Create the first mascot
        for (String imageSet : imageSets) {
            createMascot(imageSet);
        }

        getManager().setExitOnLastRemoved(isExit);
    }

    private void updateLanguage(String language) {
        if (!properties.getProperty("Language", "en-GB").equals(language)) {
            properties.setProperty("Language", language);
            refreshLanguage();
        }
    }

    private boolean toggleBooleanSetting(String propertyName, boolean defaultValue) {
        if (Boolean.parseBoolean(properties.getProperty(propertyName, String.valueOf(defaultValue)))) {
            properties.setProperty(propertyName, "false");
            return false;
        } else {
            properties.setProperty(propertyName, "true");
            return true;
        }
    }

    private void updateConfigFile() {
        try {
            try (FileOutputStream output = new FileOutputStream("./conf/settings.properties")) {
                properties.store(output, "Shimeji-ee Configuration Options");
            }
        } catch (Exception ignored) {
            System.out.println("error on updateConfigFile");
        }
    }

    /**
     * Replaces the current set of active imageSets without modifying
     * valid imageSets that are already active. Does nothing if newImageSets
     * are null
     *
     * @param newImageSets All the imageSets that should now be active
     * @author snek, with some tweaks by Kilkakon
     */
    private void setActiveImageSets(ArrayList<String> newImageSets) {
        if (newImageSets == null)
            return;

        // I don't think there would be enough imageSets chosen at any given
        // time for it to be worth using HashSet but i might be wrong
        ArrayList<String> toRemove = new ArrayList<>(imageSets);
        toRemove.removeAll(newImageSets);

        ArrayList<String> toAdd = new ArrayList<>();
        ArrayList<String> toRetain = new ArrayList<>();
        for (String set : newImageSets) {
            if (!imageSets.contains(set))
                toAdd.add(set);
            if (!toRetain.contains(set))
                toRetain.add(set);
            populateArrayListWithChildSets(set, toRetain);
        }

        boolean isExit = Main.this.getManager().isExitOnLastRemoved();
        Main.this.getManager().setExitOnLastRemoved(false);

        for (String r : toRemove)
            removeLoadedImageSet(r, toRetain);

        for (String a : toAdd)
            addImageSet(a);

        Main.this.getManager().setExitOnLastRemoved(isExit);
    }

    private void populateArrayListWithChildSets(String imageSet, ArrayList<String> childList) {
        if (childImageSets.containsKey(imageSet)) {
            for (String set : childImageSets.get(imageSet)) {
                if (!childList.contains(set)) {
                    populateArrayListWithChildSets(set, childList);
                    childList.add(set);
                }
            }
        }
    }

    private void removeLoadedImageSet(String imageSet, ArrayList<String> setsToIgnore) {
        if (childImageSets.containsKey(imageSet)) {
            for (String set : childImageSets.get(imageSet)) {
                if (!setsToIgnore.contains(set)) {
                    setsToIgnore.add(set);
                    imageSets.remove(imageSet);
                    getManager().remainNone(imageSet);
                    configurations.remove(imageSet);
                    ImagePairs.removeAll(imageSet);
                    removeLoadedImageSet(set, setsToIgnore);
                }
            }
        }

        if (!setsToIgnore.contains(imageSet)) {
            imageSets.remove(imageSet);
            getManager().remainNone(imageSet);
            configurations.remove(imageSet);
            ImagePairs.removeAll(imageSet);
        }
    }

    private void addImageSet(String imageSet) {
        if (configurations.containsKey(imageSet)) {
            imageSets.add(imageSet);
            createMascot(imageSet);
        } else {
            if (loadConfiguration(imageSet)) {
                imageSets.add(imageSet);
                createMascot(imageSet);
            } else {
                // conf failed
                configurations.remove(imageSet); // maybe move this to the loadConfig catch
            }
        }
    }

    public Configuration getConfiguration(String imageSet) {
        return configurations.get(imageSet);
    }

    private Manager getManager() {
        return this.manager;
    }

    public Platform getPlatform() {
        return platform;
    }

    public Properties getProperties() {
        return properties;
    }

    public ResourceBundle getLanguageBundle() {
        return languageBundle;
    }

    public void exit() {
        this.getManager().disposeAll();
        this.getManager().stop();
        System.exit(0);
    }
}
