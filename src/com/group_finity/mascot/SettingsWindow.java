/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.group_finity.mascot;

import java.awt.Desktop;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.ResourceBundle;
import javax.swing.ImageIcon;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SpinnerNumberModel;

/**
 *
 * @author Monolith
 */
public class SettingsWindow extends javax.swing.JDialog
{
    private final String configFile = "./conf/settings.properties";	// Config file name
    private final ArrayList<String> listData = new ArrayList<String>( );
    private Boolean alwaysShowShimejiChooser = false;
    private String filter = "nearest";
    private double scaling = 1.0;
    private Boolean windowedMode = false;
    private Dimension windowSize = new Dimension( 600, 500 );
    private Color background = new Color( 0, 255, 0 );
    
    private Boolean imageReloadRequired = false;
    private Boolean interactiveWindowReloadRequired = false;
    private Boolean environmentReloadRequired = false;
    
    /**
     * Creates new form SettingsWindow
     */    
    public SettingsWindow( java.awt.Frame parent, boolean modal )
    {
        super( parent, modal );
        initComponents( );
    }
    
    public boolean display( )
    {
        // initialise controls
        setLocationRelativeTo( null );
        grpFilter.add( radFilterNearest );
        grpFilter.add( radFilterBicubic );
        grpFilter.add( radFilterHqx );
        java.util.Hashtable<Integer,JLabel> labelTable = new java.util.Hashtable<Integer,JLabel>( );
        for( int index = 0; index < 9; index++ )
            labelTable.put( index * 10, new JLabel( index + "x" ) );
        sldScaling.setLabelTable( labelTable );
        sldScaling.setPaintLabels( true );
        sldScaling.setSnapToTicks( true );

        // load existing settings
        Properties properties = Main.getInstance( ).getProperties( );
        alwaysShowShimejiChooser = Boolean.parseBoolean( properties.getProperty( "AlwaysShowShimejiChooser", "false" ) );
        String filterText = Main.getInstance( ).getProperties( ).getProperty( "Filter", "false" );
        filter = "nearest";
        if( filterText.equalsIgnoreCase( "true" ) || filterText.equalsIgnoreCase( "hqx" ) )
            filter = "hqx";
        else if( filterText.equalsIgnoreCase( "bicubic" ) )
            filter = "bicubic";
        scaling = Double.parseDouble( properties.getProperty( "Scaling", "1.0" ) );
        windowedMode = properties.getProperty( "Environment", "generic" ).equals( "virtual" );
        String[ ] windowArray = properties.getProperty( "WindowSize", "600x500" ).split( "x" );
        windowSize = new Dimension( Integer.parseInt( windowArray[ 0 ] ), Integer.parseInt( windowArray[ 1 ] ) );
        background = Color.decode( properties.getProperty( "Background", "#00FF00" ) );
        
        chkAlwaysShowShimejiChooser.setSelected( alwaysShowShimejiChooser );
        if( filter.equals( "bicubic" ) )
            radFilterBicubic.setSelected( true );
        else if( filter.equals( "hqx" ) )
            radFilterHqx.setSelected( true );
        else
            radFilterNearest.setSelected( true );
        sldScaling.setValue( (int)( scaling * 10 ) );
        listData.addAll( Arrays.asList( properties.getProperty( "InteractiveWindows", "" ).split( "/" ) ) );
        lstInteractiveWindows.setListData( listData.toArray( ) );
        chkWindowModeEnabled.setSelected( windowedMode );
        spnWindowWidth.setBackground( txtBackground.getBackground( ) );
        spnWindowHeight.setBackground( txtBackground.getBackground( ) );
        spnWindowWidth.setEnabled( windowedMode );
        spnWindowHeight.setEnabled( windowedMode );
        spnWindowWidth.setValue( windowSize.width );
        spnWindowHeight.setValue( windowSize.height );
        txtBackground.setText( String.format( "#%02X%02X%02X", background.getRed( ), background.getGreen( ), background.getBlue( ) ) );
        btnBackgroundChange.setEnabled( windowedMode );
        pnlBackgroundPreview.setBackground( background );
        
        // localisation
        ResourceBundle language = Main.getInstance( ).getLanguageBundle( );
        setTitle( language.getString( "Settings" ) );
        pnlTabs.setTitleAt( 0, language.getString( "General" ) );
        pnlTabs.setTitleAt( 1, language.getString( "InteractiveWindows" ) );
        pnlTabs.setTitleAt( 2, language.getString( "WindowMode" ) );
        pnlTabs.setTitleAt( 3, language.getString( "About" ) );
        chkAlwaysShowShimejiChooser.setText( language.getString( "AlwaysShowShimejiChooser" ) );
        lblScaling.setText( language.getString( "Scaling" ) );
        lblFilter.setText( language.getString( "FilterOptions" ) );
        radFilterNearest.setText( language.getString( "NearestNeighbour" ) );
        radFilterHqx.setText( language.getString( "Filter" ) );
        radFilterBicubic.setText( language.getString( "BicubicFilter" ) );
        btnAddInteractiveWindow.setText( language.getString( "Add" ) );
        btnRemoveInteractiveWindow.setText( language.getString( "Remove" ) );
        chkWindowModeEnabled.setText( language.getString( "WindowedModeEnabled" ) );
        lblDimensions.setText( language.getString( "Dimensions" ) );
        lblBackground.setText( language.getString( "Background" ) );
        btnBackgroundChange.setText( language.getString( "Change" ) );
        lblShimejiEE.setText( language.getString( "ShimejiEE" ) );
        lblDevelopedBy.setText( language.getString( "DevelopedBy" ) );
        btnWebsite.setText( language.getString( "Website" ) );
        btnDone.setText( language.getString( "Done" ) );
        btnCancel.setText( language.getString( "Cancel" ) );
        
        // scale controls to fit
        float menuScaling = Float.parseFloat( properties.getProperty( "MenuDPI", "96" ) ) / 96;
        getContentPane( ).setPreferredSize( new Dimension( (int)( 500 * menuScaling ), (int)( 360 * menuScaling ) ) );
        sldScaling.setPreferredSize( new Dimension( (int)( sldScaling.getPreferredSize( ).width * menuScaling ), (int)( sldScaling.getPreferredSize( ).height * menuScaling ) ) );
        btnAddInteractiveWindow.setPreferredSize( new Dimension( (int)( btnAddInteractiveWindow.getPreferredSize( ).width * menuScaling ), (int)( btnAddInteractiveWindow.getPreferredSize( ).height * menuScaling ) ) );
        btnRemoveInteractiveWindow.setPreferredSize( new Dimension( (int)( btnRemoveInteractiveWindow.getPreferredSize( ).width * menuScaling ), (int)( btnRemoveInteractiveWindow.getPreferredSize( ).height * menuScaling ) ) );
        pnlInteractiveButtons.setPreferredSize( new Dimension( pnlInteractiveButtons.getPreferredSize( ).width, btnAddInteractiveWindow.getPreferredSize( ).height + 6 ) );
        spnWindowWidth.setPreferredSize( new Dimension( (int)( spnWindowWidth.getPreferredSize( ).width * menuScaling ), (int)( spnWindowWidth.getPreferredSize( ).height * menuScaling ) ) );
        spnWindowHeight.setPreferredSize( new Dimension( (int)( spnWindowHeight.getPreferredSize( ).width * menuScaling ), (int)( spnWindowHeight.getPreferredSize( ).height * menuScaling ) ) );
        txtBackground.setPreferredSize( new Dimension( (int)( txtBackground.getPreferredSize( ).width * menuScaling ), (int)( txtBackground.getPreferredSize( ).height * menuScaling ) ) );
        pnlBackgroundPreview.setPreferredSize( new Dimension( (int)( pnlBackgroundPreview.getPreferredSize( ).width * menuScaling ), (int)( pnlBackgroundPreview.getPreferredSize( ).height * menuScaling ) ) );
        lblIcon.setPreferredSize( new Dimension( (int)( lblIcon.getPreferredSize( ).width * menuScaling ), (int)( lblIcon.getPreferredSize( ).height * menuScaling ) ) );
        lblIcon.setMaximumSize( lblIcon.getPreferredSize( ) );
        if( getIconImages( ).size( ) > 0 )
            lblIcon.setIcon( new ImageIcon( getIconImages( ).get( 0 ).getScaledInstance( lblIcon.getPreferredSize( ).width, lblIcon.getPreferredSize( ).height, java.awt.Image.SCALE_DEFAULT ) ) );
        btnWebsite.setPreferredSize( new Dimension( (int)( btnWebsite.getPreferredSize( ).width * menuScaling ), (int)( btnWebsite.getPreferredSize( ).height * menuScaling ) ) );
        btnDiscord.setPreferredSize( new Dimension( (int)( btnDiscord.getPreferredSize( ).width * menuScaling ), (int)( btnDiscord.getPreferredSize( ).height * menuScaling ) ) );
        btnPatreon.setPreferredSize( new Dimension( (int)( btnPatreon.getPreferredSize( ).width * menuScaling ), (int)( btnPatreon.getPreferredSize( ).height * menuScaling ) ) );
        pnlAboutButtons.setPreferredSize( new Dimension( pnlAboutButtons.getPreferredSize( ).width, btnWebsite.getPreferredSize( ).height + 6 ) );
        btnDone.setPreferredSize( new Dimension( (int)( btnDone.getPreferredSize( ).width * menuScaling ), (int)( btnDone.getPreferredSize( ).height * menuScaling ) ) );
        btnCancel.setPreferredSize( new Dimension( (int)( btnCancel.getPreferredSize( ).width * menuScaling ), (int)( btnCancel.getPreferredSize( ).height * menuScaling ) ) );
        pnlFooter.setPreferredSize( new Dimension( pnlFooter.getPreferredSize( ).width, btnDone.getPreferredSize( ).height + 6 ) );
        pack( );
        setVisible( true );
        
        return true;
    }
    
    private void browseToUrl( String url )
    {
        try
        {
            Desktop desktop = Desktop.isDesktopSupported( ) ? Desktop.getDesktop( ) : null;
            if( desktop != null && desktop.isSupported( Desktop.Action.BROWSE ) )
                desktop.browse( new URI( url ) );
            else
                throw new UnsupportedOperationException( Main.getInstance( ).getLanguageBundle( ).getString( "FailedOpenWebBrowserErrorMessage" ) + " " + url );
        }
        catch( Exception e )
        {
            JOptionPane.showMessageDialog( this, e.getMessage( ), "Error", JOptionPane.PLAIN_MESSAGE );
        }
    }
    
    public boolean getEnvironmentReloadRequired( )
    {
        return environmentReloadRequired;
    }
    
    public boolean getImageReloadRequired( )
    {
        return imageReloadRequired;
    }
    
    public boolean getInteractiveWindowReloadRequired( )
    {
        return interactiveWindowReloadRequired;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings( "unchecked" )
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        grpFilter = new javax.swing.ButtonGroup();
        pnlTabs = new javax.swing.JTabbedPane();
        pnlGeneral = new javax.swing.JPanel();
        chkAlwaysShowShimejiChooser = new javax.swing.JCheckBox();
        lblScaling = new javax.swing.JLabel();
        sldScaling = new javax.swing.JSlider();
        lblFilter = new javax.swing.JLabel();
        radFilterNearest = new javax.swing.JRadioButton();
        radFilterBicubic = new javax.swing.JRadioButton();
        radFilterHqx = new javax.swing.JRadioButton();
        pnlInteractiveWindows = new javax.swing.JPanel();
        pnlInteractiveButtons = new javax.swing.JPanel();
        btnAddInteractiveWindow = new javax.swing.JButton();
        btnRemoveInteractiveWindow = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstInteractiveWindows = new javax.swing.JList();
        pnlWindowMode = new javax.swing.JPanel();
        chkWindowModeEnabled = new javax.swing.JCheckBox();
        pnlBackgroundPreview = new javax.swing.JPanel();
        lblDimensions = new javax.swing.JLabel();
        lblDimensionsX = new javax.swing.JLabel();
        lblBackground = new javax.swing.JLabel();
        txtBackground = new javax.swing.JTextField();
        btnBackgroundChange = new javax.swing.JButton();
        spnWindowWidth = new javax.swing.JSpinner();
        spnWindowHeight = new javax.swing.JSpinner();
        pnlAbout = new javax.swing.JPanel();
        glue1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 32767));
        lblIcon = new javax.swing.JLabel();
        rigid1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 10), new java.awt.Dimension(0, 10), new java.awt.Dimension(0, 10));
        lblShimejiEE = new javax.swing.JLabel();
        rigid2 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 5), new java.awt.Dimension(0, 5), new java.awt.Dimension(0, 5));
        lblVersion = new javax.swing.JLabel();
        rigid3 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 15), new java.awt.Dimension(0, 15), new java.awt.Dimension(0, 15));
        lblDevelopedBy = new javax.swing.JLabel();
        lblKilkakon = new javax.swing.JLabel();
        rigid4 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 15), new java.awt.Dimension(0, 15), new java.awt.Dimension(0, 15));
        pnlAboutButtons = new javax.swing.JPanel();
        btnWebsite = new javax.swing.JButton();
        btnDiscord = new javax.swing.JButton();
        btnPatreon = new javax.swing.JButton();
        pnlFooter = new javax.swing.JPanel();
        btnDone = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        chkAlwaysShowShimejiChooser.setText("Always Show Shimeji Chooser");
        chkAlwaysShowShimejiChooser.addItemListener(new java.awt.event.ItemListener()
        {
            public void itemStateChanged(java.awt.event.ItemEvent evt)
            {
                chkAlwaysShowShimejiChooserItemStateChanged(evt);
            }
        });

        lblScaling.setText("Scaling");

        sldScaling.setMajorTickSpacing(10);
        sldScaling.setMaximum(80);
        sldScaling.setMinorTickSpacing(5);
        sldScaling.setPaintLabels(true);
        sldScaling.setPaintTicks(true);
        sldScaling.setSnapToTicks(true);
        sldScaling.setValue(10);
        sldScaling.setPreferredSize(new java.awt.Dimension(300, 45));
        sldScaling.addChangeListener(new javax.swing.event.ChangeListener()
        {
            public void stateChanged(javax.swing.event.ChangeEvent evt)
            {
                sldScalingStateChanged(evt);
            }
        });

        lblFilter.setText("Filter");

        radFilterNearest.setText("Nearest");
        radFilterNearest.addItemListener(new java.awt.event.ItemListener()
        {
            public void itemStateChanged(java.awt.event.ItemEvent evt)
            {
                radFilterItemStateChanged(evt);
            }
        });

        radFilterBicubic.setText("Bicubic");
        radFilterBicubic.addItemListener(new java.awt.event.ItemListener()
        {
            public void itemStateChanged(java.awt.event.ItemEvent evt)
            {
                radFilterItemStateChanged(evt);
            }
        });

        radFilterHqx.setText("hqx");
        radFilterHqx.addItemListener(new java.awt.event.ItemListener()
        {
            public void itemStateChanged(java.awt.event.ItemEvent evt)
            {
                radFilterItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout pnlGeneralLayout = new javax.swing.GroupLayout(pnlGeneral);
        pnlGeneral.setLayout(pnlGeneralLayout);
        pnlGeneralLayout.setHorizontalGroup(
            pnlGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlGeneralLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chkAlwaysShowShimejiChooser)
                    .addComponent(lblScaling)
                    .addComponent(lblFilter)
                    .addGroup(pnlGeneralLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(pnlGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(radFilterNearest)
                            .addComponent(sldScaling, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(radFilterBicubic)
                            .addComponent(radFilterHqx))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlGeneralLayout.setVerticalGroup(
            pnlGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlGeneralLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(chkAlwaysShowShimejiChooser)
                .addGap(18, 18, 18)
                .addComponent(lblScaling)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sldScaling, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblFilter)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(radFilterNearest)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(radFilterBicubic)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(radFilterHqx)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlTabs.addTab("General", pnlGeneral);

        pnlInteractiveButtons.setPreferredSize(new java.awt.Dimension(380, 36));
        pnlInteractiveButtons.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 10, 5));

        btnAddInteractiveWindow.setText("Add");
        btnAddInteractiveWindow.setMaximumSize(new java.awt.Dimension(130, 26));
        btnAddInteractiveWindow.setMinimumSize(new java.awt.Dimension(95, 23));
        btnAddInteractiveWindow.setName(""); // NOI18N
        btnAddInteractiveWindow.setPreferredSize(new java.awt.Dimension(130, 26));
        btnAddInteractiveWindow.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btnAddInteractiveWindowActionPerformed(evt);
            }
        });
        pnlInteractiveButtons.add(btnAddInteractiveWindow);

        btnRemoveInteractiveWindow.setText("Remove");
        btnRemoveInteractiveWindow.setMaximumSize(new java.awt.Dimension(130, 26));
        btnRemoveInteractiveWindow.setMinimumSize(new java.awt.Dimension(95, 23));
        btnRemoveInteractiveWindow.setPreferredSize(new java.awt.Dimension(130, 26));
        btnRemoveInteractiveWindow.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btnRemoveInteractiveWindowActionPerformed(evt);
            }
        });
        pnlInteractiveButtons.add(btnRemoveInteractiveWindow);

        lstInteractiveWindows.setModel(new javax.swing.AbstractListModel()
        {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane1.setViewportView(lstInteractiveWindows);

        javax.swing.GroupLayout pnlInteractiveWindowsLayout = new javax.swing.GroupLayout(pnlInteractiveWindows);
        pnlInteractiveWindows.setLayout(pnlInteractiveWindowsLayout);
        pnlInteractiveWindowsLayout.setHorizontalGroup(
            pnlInteractiveWindowsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlInteractiveWindowsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlInteractiveWindowsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1)
                    .addComponent(pnlInteractiveButtons, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlInteractiveWindowsLayout.setVerticalGroup(
            pnlInteractiveWindowsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlInteractiveWindowsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlInteractiveButtons, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pnlTabs.addTab("InteractiveWindows", pnlInteractiveWindows);

        chkWindowModeEnabled.setText("Enable Windowed Mode");
        chkWindowModeEnabled.addItemListener(new java.awt.event.ItemListener()
        {
            public void itemStateChanged(java.awt.event.ItemEvent evt)
            {
                chkWindowModeEnabledItemStateChanged(evt);
            }
        });

        pnlBackgroundPreview.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        pnlBackgroundPreview.setPreferredSize(new java.awt.Dimension(40, 40));

        javax.swing.GroupLayout pnlBackgroundPreviewLayout = new javax.swing.GroupLayout(pnlBackgroundPreview);
        pnlBackgroundPreview.setLayout(pnlBackgroundPreviewLayout);
        pnlBackgroundPreviewLayout.setHorizontalGroup(
            pnlBackgroundPreviewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 36, Short.MAX_VALUE)
        );
        pnlBackgroundPreviewLayout.setVerticalGroup(
            pnlBackgroundPreviewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 36, Short.MAX_VALUE)
        );

        lblDimensions.setText("Dimensions");

        lblDimensionsX.setText("x");

        lblBackground.setText("Background");

        txtBackground.setEditable(false);
        txtBackground.setText("#00FF00");
        txtBackground.setPreferredSize(new java.awt.Dimension(70, 24));

        btnBackgroundChange.setText("Change");
        btnBackgroundChange.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btnBackgroundChangeActionPerformed(evt);
            }
        });

        spnWindowWidth.setModel(new javax.swing.SpinnerNumberModel(0, 0, 10000, 1));
        spnWindowWidth.setPreferredSize(new java.awt.Dimension(60, 24));
        spnWindowWidth.addChangeListener(new javax.swing.event.ChangeListener()
        {
            public void stateChanged(javax.swing.event.ChangeEvent evt)
            {
                spnWindowWidthStateChanged(evt);
            }
        });

        spnWindowHeight.setModel(new javax.swing.SpinnerNumberModel(0, 0, 10000, 1));
        spnWindowHeight.setMinimumSize(new java.awt.Dimension(30, 20));
        spnWindowHeight.setPreferredSize(new java.awt.Dimension(60, 24));
        spnWindowHeight.addChangeListener(new javax.swing.event.ChangeListener()
        {
            public void stateChanged(javax.swing.event.ChangeEvent evt)
            {
                spnWindowHeightStateChanged(evt);
            }
        });

        javax.swing.GroupLayout pnlWindowModeLayout = new javax.swing.GroupLayout(pnlWindowMode);
        pnlWindowMode.setLayout(pnlWindowModeLayout);
        pnlWindowModeLayout.setHorizontalGroup(
            pnlWindowModeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlWindowModeLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlWindowModeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chkWindowModeEnabled)
                    .addGroup(pnlWindowModeLayout.createSequentialGroup()
                        .addGroup(pnlWindowModeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblDimensions)
                            .addComponent(lblBackground))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(pnlWindowModeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(pnlBackgroundPreview, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(pnlWindowModeLayout.createSequentialGroup()
                                .addComponent(spnWindowWidth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblDimensionsX)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spnWindowHeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(pnlWindowModeLayout.createSequentialGroup()
                                .addComponent(txtBackground, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnBackgroundChange)))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlWindowModeLayout.setVerticalGroup(
            pnlWindowModeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlWindowModeLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(chkWindowModeEnabled)
                .addGap(18, 18, 18)
                .addGroup(pnlWindowModeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblDimensions)
                    .addComponent(lblDimensionsX)
                    .addComponent(spnWindowWidth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(spnWindowHeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlWindowModeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtBackground, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblBackground)
                    .addComponent(btnBackgroundChange))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlBackgroundPreview, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlTabs.addTab("WindowMode", pnlWindowMode);

        pnlAbout.setLayout(new javax.swing.BoxLayout(pnlAbout, javax.swing.BoxLayout.Y_AXIS));
        pnlAbout.add(glue1);

        lblIcon.setAlignmentX(0.5F);
        lblIcon.setMinimumSize(new java.awt.Dimension(64, 64));
        lblIcon.setPreferredSize(new java.awt.Dimension(64, 64));
        pnlAbout.add(lblIcon);
        pnlAbout.add(rigid1);

        lblShimejiEE.setFont(lblShimejiEE.getFont().deriveFont(lblShimejiEE.getFont().getStyle() | java.awt.Font.BOLD, lblShimejiEE.getFont().getSize()+10));
        lblShimejiEE.setText("Shimeji");
        lblShimejiEE.setAlignmentX(0.5F);
        pnlAbout.add(lblShimejiEE);
        pnlAbout.add(rigid2);

        lblVersion.setFont(lblVersion.getFont().deriveFont(lblVersion.getFont().getSize()+4f));
        lblVersion.setText("1.0.20");
        lblVersion.setAlignmentX(0.5F);
        pnlAbout.add(lblVersion);
        pnlAbout.add(rigid3);

        lblDevelopedBy.setText("developed by");
        lblDevelopedBy.setAlignmentX(0.5F);
        pnlAbout.add(lblDevelopedBy);

        lblKilkakon.setText("Kilkakon");
        lblKilkakon.setAlignmentX(0.5F);
        pnlAbout.add(lblKilkakon);
        pnlAbout.add(rigid4);

        pnlAboutButtons.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 10, 5));

        btnWebsite.setText("Website");
        btnWebsite.setAlignmentX(0.5F);
        btnWebsite.setMaximumSize(new java.awt.Dimension(130, 26));
        btnWebsite.setPreferredSize(new java.awt.Dimension(100, 26));
        btnWebsite.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btnWebsiteActionPerformed(evt);
            }
        });
        pnlAboutButtons.add(btnWebsite);

        btnDiscord.setText("Discord");
        btnDiscord.setAlignmentX(0.5F);
        btnDiscord.setMaximumSize(new java.awt.Dimension(130, 26));
        btnDiscord.setPreferredSize(new java.awt.Dimension(100, 26));
        btnDiscord.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btnDiscordActionPerformed(evt);
            }
        });
        pnlAboutButtons.add(btnDiscord);

        btnPatreon.setText("Patreon");
        btnPatreon.setAlignmentX(0.5F);
        btnPatreon.setMaximumSize(new java.awt.Dimension(130, 26));
        btnPatreon.setPreferredSize(new java.awt.Dimension(100, 26));
        btnPatreon.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btnPatreonActionPerformed(evt);
            }
        });
        pnlAboutButtons.add(btnPatreon);

        pnlAbout.add(pnlAboutButtons);

        pnlTabs.addTab("About", pnlAbout);

        pnlFooter.setPreferredSize(new java.awt.Dimension(380, 36));
        pnlFooter.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 10, 5));

        btnDone.setText("Done");
        btnDone.setMaximumSize(new java.awt.Dimension(130, 26));
        btnDone.setMinimumSize(new java.awt.Dimension(95, 23));
        btnDone.setName(""); // NOI18N
        btnDone.setPreferredSize(new java.awt.Dimension(130, 26));
        btnDone.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btnDoneActionPerformed(evt);
            }
        });
        pnlFooter.add(btnDone);

        btnCancel.setText("Cancel");
        btnCancel.setMaximumSize(new java.awt.Dimension(130, 26));
        btnCancel.setMinimumSize(new java.awt.Dimension(95, 23));
        btnCancel.setPreferredSize(new java.awt.Dimension(130, 26));
        btnCancel.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btnCancelActionPerformed(evt);
            }
        });
        pnlFooter.add(btnCancel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlTabs)
                    .addComponent(pnlFooter, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlTabs)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlFooter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnDoneActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnDoneActionPerformed
    {//GEN-HEADEREND:event_btnDoneActionPerformed
        // done button
        try
        {
            FileOutputStream output = new FileOutputStream( configFile );
            try
            {
                Properties properties = Main.getInstance( ).getProperties( );
                String interactiveWindows = listData.toString( ).replace( "[", "" ).replace( "]", "" ).replace( ", ", "/" );
                String[ ] windowArray = properties.getProperty( "WindowSize", "600x500" ).split( "x" );
                Dimension window = new Dimension( Integer.parseInt( windowArray[ 0 ] ), Integer.parseInt( windowArray[ 1 ] ) );
                
                environmentReloadRequired = properties.getProperty( "Environment", "generic" ).equals( "virtual" ) != windowedMode ||
                                            window != windowSize ||
                                            Color.decode( properties.getProperty( "Background", "#00FF00" ) ) != background;
                imageReloadRequired = !properties.getProperty( "Filter", "false" ).equalsIgnoreCase( filter ) || 
                                      Double.parseDouble( properties.getProperty( "Scaling", "1.0" ) ) != scaling;
                interactiveWindowReloadRequired = !properties.getProperty( "InteractiveWindows", "" ).equals( interactiveWindows );
                
                properties.setProperty( "AlwaysShowShimejiChooser", alwaysShowShimejiChooser.toString( ) );
                properties.setProperty( "Scaling", Double.toString( scaling ) );
                properties.setProperty( "Filter", filter );
                properties.setProperty( "InteractiveWindows", interactiveWindows );
                properties.setProperty( "Environment", windowedMode ? "virtual" : "generic" );
                if( windowedMode )
                {
                    properties.setProperty( "WindowSize", windowSize.width + "x" + windowSize.height );
                    properties.setProperty( "Background", String.format( "#%02X%02X%02X", background.getRed( ), background.getGreen( ), background.getBlue( ) ) );
                }
                
                properties.store( output, "Shimeji-ee Configuration Options" );
            }
            finally
            {
                output.close( );
            }
        }
        catch( Exception e )
        {
        }
        dispose( );
    }//GEN-LAST:event_btnDoneActionPerformed

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnCancelActionPerformed
    {//GEN-HEADEREND:event_btnCancelActionPerformed
        dispose( );
    }//GEN-LAST:event_btnCancelActionPerformed

    private void btnAddInteractiveWindowActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnAddInteractiveWindowActionPerformed
    {//GEN-HEADEREND:event_btnAddInteractiveWindowActionPerformed
        // add button
        String inputValue = JOptionPane.showInputDialog( rootPane, Main.getInstance( ).getLanguageBundle( ).getString( "InteractiveWindowHintMessage" ), Main.getInstance( ).getLanguageBundle( ).getString( "AddInteractiveWindow" ), JOptionPane.QUESTION_MESSAGE ).trim( );
        if( !inputValue.isEmpty( ) && !inputValue.contains( "/" ) )
        {
            listData.add( inputValue );
            lstInteractiveWindows.setListData( listData.toArray( ) );
        }
    }//GEN-LAST:event_btnAddInteractiveWindowActionPerformed

    private void btnRemoveInteractiveWindowActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnRemoveInteractiveWindowActionPerformed
    {//GEN-HEADEREND:event_btnRemoveInteractiveWindowActionPerformed
        // delete button
        if( lstInteractiveWindows.getSelectedIndex( ) != -1 )
        {
            listData.remove( lstInteractiveWindows.getSelectedIndex( ) );
            lstInteractiveWindows.setListData( listData.toArray( ) );
        }
    }//GEN-LAST:event_btnRemoveInteractiveWindowActionPerformed

    private void chkAlwaysShowShimejiChooserItemStateChanged(java.awt.event.ItemEvent evt)//GEN-FIRST:event_chkAlwaysShowShimejiChooserItemStateChanged
    {//GEN-HEADEREND:event_chkAlwaysShowShimejiChooserItemStateChanged
        alwaysShowShimejiChooser = evt.getStateChange( ) == ItemEvent.SELECTED;
    }//GEN-LAST:event_chkAlwaysShowShimejiChooserItemStateChanged

    private void radFilterItemStateChanged(java.awt.event.ItemEvent evt)//GEN-FIRST:event_radFilterItemStateChanged
    {//GEN-HEADEREND:event_radFilterItemStateChanged
        if( evt.getStateChange( ) == ItemEvent.SELECTED )
        {
            Object source = evt.getItemSelectable( );
            
            if( source == radFilterNearest )
                filter = "nearest";
            else if( source == radFilterHqx )
                filter = "hqx";
            else
                filter = "bicubic";
        }
    }//GEN-LAST:event_radFilterItemStateChanged

    private void sldScalingStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_sldScalingStateChanged
    {//GEN-HEADEREND:event_sldScalingStateChanged
        if( !sldScaling.getValueIsAdjusting( ) )
        {
            if( sldScaling.getValue( ) == 0 )
                sldScaling.setValue( 5 );
            else
            {
                scaling = sldScaling.getValue( ) / 10.0;
                if( scaling == 2 || scaling == 3 || scaling == 4 || scaling == 6 || scaling == 8 )
                {
                    radFilterHqx.setEnabled( true );
                }
                else
                {
                    radFilterHqx.setEnabled( false );
                    if( filter.equals( "hqx" ) )
                    {
                        radFilterNearest.setSelected( true );
                    }
                }
            }
        }
    }//GEN-LAST:event_sldScalingStateChanged

    private void btnBackgroundChangeActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnBackgroundChangeActionPerformed
    {//GEN-HEADEREND:event_btnBackgroundChangeActionPerformed
        Color color = JColorChooser.showDialog( this, "Choose Background Color", background );

        if( color != null )
        {
            background = color;
            txtBackground.setText( String.format( "#%02X%02X%02X", background.getRed( ), background.getGreen( ), background.getBlue( ) ) );
            pnlBackgroundPreview.setBackground( background );
        }
    }//GEN-LAST:event_btnBackgroundChangeActionPerformed

    private void chkWindowModeEnabledItemStateChanged(java.awt.event.ItemEvent evt)//GEN-FIRST:event_chkWindowModeEnabledItemStateChanged
    {//GEN-HEADEREND:event_chkWindowModeEnabledItemStateChanged
        windowedMode = evt.getStateChange( ) == ItemEvent.SELECTED;

        spnWindowWidth.setEnabled( windowedMode );
        spnWindowHeight.setEnabled( windowedMode );
        btnBackgroundChange.setEnabled( windowedMode );
    }//GEN-LAST:event_chkWindowModeEnabledItemStateChanged

    private void spnWindowHeightStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_spnWindowHeightStateChanged
    {//GEN-HEADEREND:event_spnWindowHeightStateChanged
        windowSize.height = ( (SpinnerNumberModel)spnWindowHeight.getModel( ) ).getNumber( ).intValue( );
    }//GEN-LAST:event_spnWindowHeightStateChanged

    private void spnWindowWidthStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_spnWindowWidthStateChanged
    {//GEN-HEADEREND:event_spnWindowWidthStateChanged
        windowSize.width = ( (SpinnerNumberModel)spnWindowWidth.getModel( ) ).getNumber( ).intValue( );
    }//GEN-LAST:event_spnWindowWidthStateChanged

    private void btnWebsiteActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnWebsiteActionPerformed
    {//GEN-HEADEREND:event_btnWebsiteActionPerformed
        browseToUrl( "http://kilkakon.com/" );
    }//GEN-LAST:event_btnWebsiteActionPerformed

    private void btnDiscordActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnDiscordActionPerformed
    {//GEN-HEADEREND:event_btnDiscordActionPerformed
        browseToUrl( "https://discord.gg/NBq3zqfA2B" );
    }//GEN-LAST:event_btnDiscordActionPerformed

    private void btnPatreonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnPatreonActionPerformed
    {//GEN-HEADEREND:event_btnPatreonActionPerformed
        browseToUrl( "https://patreon.com/kilkakon" );
    }//GEN-LAST:event_btnPatreonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main( String args[] )
    {
        /* Create and display the form */
        java.awt.EventQueue.invokeLater( new Runnable()
        {
            public void run( )
            {
                new SettingsWindow( new javax.swing.JFrame( ), true ).display( );
            }
        } );
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddInteractiveWindow;
    private javax.swing.JButton btnBackgroundChange;
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnDiscord;
    private javax.swing.JButton btnDone;
    private javax.swing.JButton btnPatreon;
    private javax.swing.JButton btnRemoveInteractiveWindow;
    private javax.swing.JButton btnWebsite;
    private javax.swing.JCheckBox chkAlwaysShowShimejiChooser;
    private javax.swing.JCheckBox chkWindowModeEnabled;
    private javax.swing.Box.Filler glue1;
    private javax.swing.ButtonGroup grpFilter;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblBackground;
    private javax.swing.JLabel lblDevelopedBy;
    private javax.swing.JLabel lblDimensions;
    private javax.swing.JLabel lblDimensionsX;
    private javax.swing.JLabel lblFilter;
    private javax.swing.JLabel lblIcon;
    private javax.swing.JLabel lblKilkakon;
    private javax.swing.JLabel lblScaling;
    private javax.swing.JLabel lblShimejiEE;
    private javax.swing.JLabel lblVersion;
    private javax.swing.JList lstInteractiveWindows;
    private javax.swing.JPanel pnlAbout;
    private javax.swing.JPanel pnlAboutButtons;
    private javax.swing.JPanel pnlBackgroundPreview;
    private javax.swing.JPanel pnlFooter;
    private javax.swing.JPanel pnlGeneral;
    private javax.swing.JPanel pnlInteractiveButtons;
    private javax.swing.JPanel pnlInteractiveWindows;
    private javax.swing.JTabbedPane pnlTabs;
    private javax.swing.JPanel pnlWindowMode;
    private javax.swing.JRadioButton radFilterBicubic;
    private javax.swing.JRadioButton radFilterHqx;
    private javax.swing.JRadioButton radFilterNearest;
    private javax.swing.Box.Filler rigid1;
    private javax.swing.Box.Filler rigid2;
    private javax.swing.Box.Filler rigid3;
    private javax.swing.Box.Filler rigid4;
    private javax.swing.JSlider sldScaling;
    private javax.swing.JSpinner spnWindowHeight;
    private javax.swing.JSpinner spnWindowWidth;
    private javax.swing.JTextField txtBackground;
    // End of variables declaration//GEN-END:variables
}
