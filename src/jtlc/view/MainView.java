/*
 * Copyright (C) 2015 Baldani Sergio - Tardivo Cristian
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package jtlc.view;

// WebLaF
import com.alee.extended.breadcrumb.WebBreadcrumb;
import com.alee.extended.breadcrumb.WebBreadcrumbToggleButton;
import com.alee.extended.layout.ToolbarLayout;
import com.alee.extended.statusbar.WebMemoryBar;
import com.alee.extended.statusbar.WebStatusBar;
import com.alee.extended.statusbar.WebStatusLabel;
import com.alee.extended.transition.ComponentTransition;
import com.alee.extended.transition.TransitionListener;
import com.alee.extended.transition.effects.Direction;
import com.alee.extended.transition.effects.TransitionEffect;
import com.alee.extended.transition.effects.curtain.CurtainTransitionEffect;
import com.alee.extended.transition.effects.curtain.CurtainType;
import com.alee.extended.transition.effects.fade.FadeTransitionEffect;
import com.alee.extended.transition.effects.slide.SlideTransitionEffect;
import com.alee.extended.transition.effects.slide.SlideType;
import com.alee.extended.transition.effects.zoom.ZoomTransitionEffect;
import com.alee.extended.transition.effects.zoom.ZoomType;
import com.alee.extended.image.WebDecoratedImage;
import com.alee.laf.optionpane.WebOptionPane;
import com.alee.laf.panel.WebPanel;
import com.alee.managers.language.data.TooltipWay;
import com.alee.managers.tooltip.TooltipManager;
import com.alee.utils.SwingUtils;
import com.alee.global.StyleConstants;
import com.alee.extended.filechooser.WebDirectoryChooser;
import com.alee.extended.transition.effects.blocks.BlockType;
import com.alee.extended.transition.effects.blocks.BlocksTransitionEffect;
import com.alee.laf.button.WebButton;
import com.alee.laf.filechooser.WebFileChooser;
import com.alee.laf.menu.MenuBarStyle;
import com.alee.laf.menu.WebMenu;
import com.alee.laf.menu.WebMenuBar;
import com.alee.laf.menu.WebMenuItem;
import com.alee.laf.progressbar.WebProgressBar;
import com.alee.laf.rootpane.WebFrame;
import com.alee.managers.tooltip.WebCustomTooltip;
import com.alee.utils.FileUtils;
// Java Util
import java.util.HashMap;
import java.util.Observable;
// Java AWT
import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
// Javax Swing
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
// Java IO
import java.io.File;
// Panels
import jtlc.view.panels.IPanel;
import jtlc.view.panels.DropPanel;
import jtlc.view.panels.SplitPanel;
import jtlc.view.panels.DataPanel;
import jtlc.view.panels.AnalysisPanel;
import jtlc.view.panels.CutPanel;
import jtlc.view.panels.RotationPanel;
import jtlc.view.panels.GalleryPanel;
// System abstract DTO
import jtlc.view.dto.AbstractDTO;
// Panel DTOs
import jtlc.view.panels.dto.CuttingDTO;
import jtlc.view.panels.dto.DropDTO;
import jtlc.view.panels.dto.GalleryDTO;
import jtlc.view.panels.dto.RotationDTO;
import jtlc.view.panels.dto.SplitDTO;
import jtlc.view.panels.dto.AnalysisDTO;
import jtlc.view.panels.dto.DataDTO;
// Assets
import jtlc.assets.Assets;
import jtlc.main.common.Pair;
import jtlc.main.common.Settings;
// Dialogs
import jtlc.view.dialogs.InfoDialog;
import jtlc.view.dialogs.IDialog;
import jtlc.view.dialogs.ImageExportDialog;
import jtlc.view.dialogs.ProjectDialog;
import jtlc.view.dialogs.SettingsDialog;
import jtlc.view.dialogs.TextPanelDialog;
import jtlc.view.dialogs.dto.ImageExportDTO;
// Dialogs DTOs
import jtlc.view.dialogs.dto.InfoDTO;
import jtlc.view.dialogs.dto.ProjectDTO;
import jtlc.view.dialogs.dto.SettingsDTO;
import jtlc.view.dto.ExportDTO;
import jtlc.view.panels.ReportsPanel;
import jtlc.view.panels.ResultsPanel;
import jtlc.view.panels.dto.ReportsDTO;
import jtlc.view.panels.dto.ResultsDTO;

/**
 * Main View
 * Implements JFrame with main conponents, menu's bars, buttons
 * breadcrumbs, display current step panel.
 * 
 * @author Cristian Tardivo
 */
public class MainView extends Observable {
    
    /**
     * View Panels Enumeration.
     */
    public enum Panels {
        START_PANEL, GALERY_PANEL, LOAD_IMAGE, CUT_IMAGE, ROTATE_IMAGE, SAMPLES_SELECT, 
        SAMPLES_POINTS, SAMPLES_ANALYSIS, SAMPLES_ANALYSIS_RESULTS, ANALYSIS_REPORTS;
    };
        
    // Main Frame
    private final WebFrame mainFrame;
    // Center Panel
    private JComponent centerPanel;
    // Foot panel
    private final WebPanel footPanel;
    // Current Dialog
    private IDialog currentDialog;
    // Transitions Components
    private ComponentTransition currentTransition;
    private ComponentTransition nextTransition;
    // Main Panels references
    private final HashMap<Panels,JComponent> mainPanels;
    // Transitions Status
    private boolean transitionEnabled = Settings.isTransitionsEnabled();
    private boolean transitionStarted = false;
    // Main menu bar
    private final WebMenuBar mainMenuBar;
    // Some MenuItems
    private WebMenu menuExport;
    private WebMenu exportImage;
    private WebMenu exportProcessedImage;
    private WebMenu exportMean;
    private WebMenu exportData;
    private WebMenu exportReports;
    private WebMenu helpMenu;
    private WebMenuItem editItem;
    private WebMenuItem saveItem;
    private WebMenuItem saveAsItem;
    // Buttons
    private WebButton backBt, startBt, restartBt,nextBt;
    // Current step panel reference
    private Panels stepPanel;
    // Current IPanel
    private JComponent currentPanel;
    // BreadCrumb buttons
    private HashMap<Panels,WebBreadcrumbToggleButton> breadcrumbButtons; 
    private WebBreadcrumb stepBreadcrumb;
    // Status Bar Text
    private WebStatusLabel statusText;
    // Main progress bar
    private WebProgressBar progressBar;
    // Simple status bar
    private WebStatusBar statusBar;
    // Default dimension - size
    private final Dimension minimunSize = new Dimension(800, 600);
    private Dimension componetsSize;
    
    /**
     * Create Main View Componets
     */
    public MainView() {
        // Main Components
        mainFrame = new WebFrame(Assets.getString("MAIN_TITLE"));
        // Set Main Frame Settings
        mainFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        mainFrame.setMinimumSize(minimunSize);
        mainFrame.setSize(Settings.getWindowSize());
        mainFrame.setLocation(Settings.getWindowLocation());
        mainFrame.setExtendedState(Settings.getExtendedState());
        mainFrame.setResizable(true);
        mainFrame.setUndecorated(true);
        mainFrame.setShowResizeCorner(true);
        mainFrame.setAttachButtons(true);
        mainFrame.setIconImage(Assets.loadIcon("ic_jtlc").getImage());
        // Center Panel and menu bar
        centerPanel = createStartPanel();
        mainMenuBar = createMainMenuBar();
        // Add Componets
        mainFrame.setLayout(new BorderLayout(0,5));
        mainFrame.add(createBreadcrumb(), BorderLayout.PAGE_START);
        mainFrame.add(centerPanel, BorderLayout.CENTER);
        // Foot Panel and Components
        footPanel = new WebPanel(new BorderLayout());
        footPanel.add(createButtonsPanel(),BorderLayout.PAGE_START);
        footPanel.add(createStatusBar(),BorderLayout.PAGE_END);
        mainFrame.add(footPanel, BorderLayout.PAGE_END);
        // Main Menu Bar
        mainFrame.setJMenuBar(mainMenuBar);
        // Init main component
        mainFrame.remove(centerPanel);
        currentTransition = new ComponentTransition(centerPanel, createEffect(Direction.left, 0));
        currentTransition.addTransitionListener(transitionListener);
        mainFrame.add(currentTransition, BorderLayout.CENTER);
        stepPanel = Panels.START_PANEL;
        // Initi frame visibility
        mainFrame.pack();
        mainFrame.setVisible(true);
        // Fix for frame size        
        if (Settings.getExtendedState() != JFrame.MAXIMIZED_BOTH)
            mainFrame.resize(Settings.getWindowSize());
        // Get Center Panel Size (for component size)
        componetsSize = centerPanel.getSize();
        // Frame/Components Listeners
        mainFrame.addWindowListener(windowListener);
        mainFrame.addWindowStateListener(windowStateListener);
        mainFrame.addComponentListener(componentListener);
        currentTransition.addComponentListener(componentListener);
        // Save start panel
        mainPanels = new HashMap<>();
        mainPanels.put(Panels.START_PANEL, centerPanel);
    }

    /**
     * Create Start Panel
     * @return center panel
     */
    private WebPanel createStartPanel() {
        WebPanel res = new WebPanel(new BorderLayout());
        // Center Logo
        WebDecoratedImage logo = new WebDecoratedImage(Assets.loadIcon("logo_jtlc"));
        logo.setRound(0);
        logo.setDrawGlassLayer(false);
        logo.setDrawBorder(false);
        logo.setShadeWidth(0);
        res.add(logo, BorderLayout.CENTER);
        return res;
    }
    
    /**
     * Create Main Menu Bar
     * @return menu bar
     */
    private WebMenuBar createMainMenuBar() {
        WebMenuBar mainBar = new WebMenuBar(MenuBarStyle.attached);
        int icoSize = 20;
        /**
         * Menu: File
         */
        WebMenu menuFile = new WebMenu(Assets.getString("FILE"));
        Assets.associateComponent(menuFile, "setText", "FILE");
        mainBar.add(menuFile);
        // Item: Open
        WebMenuItem newItem = new WebMenuItem(Assets.getString("NEW_PROJECT"));
        Assets.associateComponent(newItem, "setText", "NEW_PROJECT");
        newItem.setActionCommand("NEW_PROJECT");
        newItem.setIcon(Assets.loadIcon("ic_new", icoSize));
        newItem.addActionListener(actionListener);
        menuFile.add(newItem);
        menuFile.addSeparator();
        // Item: Load
        WebMenuItem loadItem = new WebMenuItem(Assets.getString("LOAD_PROJECT"));
        Assets.associateComponent(loadItem, "setText", "LOAD_PROJECT");
        loadItem.setActionCommand("LOAD_PROJECT");
        loadItem.setIcon(Assets.loadIcon("ic_open", icoSize));
        loadItem.addActionListener(actionListener);
        menuFile.add(loadItem);
        // Item: Explore
        WebMenuItem exploreItem = new WebMenuItem(Assets.getString("EXPLORE_PROJECTS"));
        Assets.associateComponent(exploreItem, "setText", "EXPLORE_PROJECTS");
        exploreItem.setActionCommand("EXPLORE_PROJECTS");
        exploreItem.setIcon(Assets.loadIcon("ic_folder", icoSize));
        exploreItem.addActionListener(actionListener);
        menuFile.add(exploreItem);
        menuFile.addSeparator();
        // Item: Save
        saveItem = new WebMenuItem(Assets.getString("SAVE_PROJECT"));
        Assets.associateComponent(saveItem, "setText", "SAVE_PROJECT");
        saveItem.setEnabled(false);
        saveItem.setActionCommand("SAVE_PROJECT");
        saveItem.setIcon(Assets.loadIcon("ic_save", icoSize));
        saveItem.addActionListener(actionListener);
        menuFile.add(saveItem);
        // Item: Save As
        saveAsItem = new WebMenuItem(Assets.getString("SAVE_AS_PROJECT"));
        Assets.associateComponent(saveAsItem, "setText", "SAVE_AS_PROJECT");
        saveAsItem.setActionCommand("SAVE_AS_PROJECT");
        saveAsItem.setEnabled(false);
        saveAsItem.setIcon(Assets.loadIcon("ic_save_as", icoSize));
        saveAsItem.addActionListener(actionListener);
        menuFile.add(saveAsItem);       
        //
        menuFile.addSeparator();
        // Item: Exit
        WebMenuItem exitItem = new WebMenuItem(Assets.getString("EXIT"));
        Assets.associateComponent(exitItem, "setText", "EXIT");
        exitItem.setActionCommand("EXIT_SYSTEM");
        exitItem.setIcon(Assets.loadIcon("ic_exit", icoSize));
        exitItem.addActionListener(actionListener);
        menuFile.add(exitItem);       
        /**
         * Menu: Edit
         */
        WebMenu menuEdit = new WebMenu(Assets.getString("EDIT"));
        Assets.associateComponent(menuEdit, "setText", "EDIT");
        mainBar.add(menuEdit);
        // Item: Edit data
        editItem = new WebMenuItem(Assets.getString("EDIT_PROJECT"));
        Assets.associateComponent(editItem, "setText", "EDIT_PROJECT");
        editItem.setEnabled(false);
        editItem.setActionCommand("EDIT_PROJECT");
        editItem.setIcon(Assets.loadIcon("ic_edit", icoSize));
        editItem.addActionListener(actionListener);
        menuEdit.add(editItem);
        // Item: Edit settings
        WebMenuItem settingsItem = new WebMenuItem(Assets.getString("CHANGE_SETTINGS"));
        Assets.associateComponent(settingsItem, "setText", "CHANGE_SETTINGS");
        settingsItem.setActionCommand("EDIT_SETTINGS");
        settingsItem.setIcon(Assets.loadIcon("ic_settings", icoSize));
        settingsItem.addActionListener(actionListener);
        menuEdit.add(settingsItem);
        /**
         * Menu: Export
         */
        menuExport = new WebMenu(Assets.getString("EXPORT"));
        Assets.associateComponent(menuExport, "setText", "EXPORT");
        menuExport.setEnabled(false);
        mainBar.add(menuExport);
        // Export Images
        exportImage = new WebMenu(Assets.getString("SOURCE_IMAGES"), Assets.loadIcon("ic_picture", icoSize));
        Assets.associateComponent(exportImage, "setText", "SOURCE_IMAGES");
        menuExport.add(exportImage);
        // Export Processed Images
        exportProcessedImage = new WebMenu(Assets.getString("PROCESSED_IMAGES"), Assets.loadIcon("ic_image", icoSize));
        Assets.associateComponent(exportProcessedImage, "setText", "PROCESSED_IMAGES");
        menuExport.add(exportProcessedImage);
        // Export Mean
        exportMean = new WebMenu(Assets.getString("SAMPLE_MEAN"), Assets.loadIcon("ic_line_chart", icoSize));
        Assets.associateComponent(exportMean, "setText", "SAMPLE_MEAN");
        menuExport.add(exportMean);
        // Export Data
        exportData = new WebMenu(Assets.getString("PLAIN_DATA"), Assets.loadIcon("ic_data", icoSize));
        Assets.associateComponent(exportData, "setText", "PLAIN_DATA");
        menuExport.add(exportData);
        // Export Reports
        exportReports = new WebMenu(Assets.getString("EXPERIMENT_REPORTS"), Assets.loadIcon("ic_report_document", icoSize));
        Assets.associateComponent(exportReports, "setText", "EXPERIMENT_REPORTS");
        menuExport.add(exportReports);
        /**
         * Menu: Help
         */
        helpMenu = new WebMenu(Assets.getString("HELP"));
        Assets.associateComponent(helpMenu, "setText", "HELP");
        mainBar.add(helpMenu);
        // About
        WebMenuItem aboutItem = new WebMenuItem(Assets.getString("ABOUT"));
        Assets.associateComponent(aboutItem, "setText", "ABOUT");
        aboutItem.setActionCommand("ABOUT_JTL");
        aboutItem.setIcon(Assets.loadIcon("ic_about", icoSize));
        aboutItem.addActionListener(actionListener);
        helpMenu.add(aboutItem);
        // User Manual
        WebMenuItem userManualItem = new WebMenuItem(Assets.getString("USER_MANUAL"));
        Assets.associateComponent(userManualItem, "setText", "USER_MANUAL");
        userManualItem.setActionCommand("SHOW_HELP");
        userManualItem.setIcon(Assets.loadIcon("ic_user_manual", icoSize));
        userManualItem.addActionListener(actionListener);
        helpMenu.add(userManualItem);
        // Licenses
        WebMenuItem licensesItem = new WebMenuItem(Assets.getString("LICENSES"));
        Assets.associateComponent(licensesItem, "setText", "LICENSES");
        licensesItem.setActionCommand("LICENSES");
        licensesItem.setIcon(Assets.loadIcon("ic_licenses", icoSize));
        licensesItem.addActionListener(actionListener);
        helpMenu.add(licensesItem);
        //
        return mainBar;
    }
    
    /**
     * Create buttons panel
     * @return buttons panel
     */
    private WebPanel createButtonsPanel() {
        WebPanel panel = new WebPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, StyleConstants.bottomBgColor));
        panel.setBackground(StyleConstants.darkBackgroundColor);
        GridBagConstraints constraints = new GridBagConstraints();
        //
        backBt = new WebButton(Assets.getString("BACK"), Assets.loadIcon("ic_previous"));
        backBt.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        WebCustomTooltip backToolTip = TooltipManager.setTooltip(backBt, Assets.getString("PREVIOUS_STEP_TOOLTIP"), TooltipWay.up, 250);
        Assets.associateComponent(backBt, "setText", "BACK");
        Assets.associateComponent(backToolTip, "setTooltip", "PREVIOUS_STEP_TOOLTIP");
        backBt.setActionCommand("PREV_STEP");
        backBt.addActionListener(actionListener);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.insets = new Insets(10, 2, 10, 2);
        backBt.setVisible(true);
        backBt.setEnabled(false);
        panel.add(backBt,constraints);
        //
        startBt = new WebButton(Assets.getString("START"), Assets.loadIcon("ic_start"));
        startBt.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        WebCustomTooltip startToolTip = TooltipManager.setTooltip(startBt, Assets.getString("START_PROCESS_TOOLTIP"), TooltipWay.up, 250);
        Assets.associateComponent(startBt, "setText", "START");
        Assets.associateComponent(startToolTip, "setTooltip", "START_PROCESS_TOOLTIP");
        startBt.setActionCommand("NEXT_STEP");
        startBt.addActionListener(actionListener);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 2;
        constraints.gridy = 0;
        constraints.insets = new Insets(10, 2, 10, 2);
        startBt.setVisible(true);
        startBt.setEnabled(false);
        panel.add(startBt,constraints);
        //
        restartBt = new WebButton(Assets.getString("RESTART"), Assets.loadIcon("ic_restart"));
        restartBt.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        WebCustomTooltip restartToolTip = TooltipManager.setTooltip(restartBt, Assets.getString("RESTART_STEP_TOOLTIP"), TooltipWay.up, 250);
        Assets.associateComponent(restartBt, "setText", "RESTART");
        Assets.associateComponent(restartToolTip, "setTooltip", "RESTART_STEP_TOOLTIP");
        restartBt.setActionCommand("RESTART_STEP");
        restartBt.addActionListener(actionListener);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.insets = new Insets(10, 2, 10, 2);
        restartBt.setVisible(true);
        restartBt.setEnabled(false);
        panel.add(restartBt,constraints);
        //
        nextBt = new WebButton(Assets.getString("NEXT"), Assets.loadIcon("ic_next"));
        nextBt.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        WebCustomTooltip nextToolTip = TooltipManager.setTooltip(nextBt, Assets.getString("NEXT_STEP_TOOLTIP"), TooltipWay.up, 250);
        Assets.associateComponent(nextBt, "setText", "NEXT");
        Assets.associateComponent(nextToolTip, "setTooltip", "NEXT_STEP_TOOLTIP");
        nextBt.setActionCommand("NEXT_STEP");
        nextBt.addActionListener(actionListener);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 3;
        constraints.gridy = 0;
        constraints.insets = new Insets(10, 2, 10, 2);
        nextBt.setVisible(false);
        nextBt.setEnabled(false);
        panel.add(nextBt,constraints);
        //
        return panel;
    }
    
    /**
     * Create current step breadcrumb
     * @return breadcrumb
     */
    private WebPanel createBreadcrumb() {
        // Breadcrumb
        stepBreadcrumb = new WebBreadcrumb(false);
        // Buttons
        breadcrumbButtons = new HashMap<>();
        int icSize = 16;
        // System Start
        WebBreadcrumbToggleButton startSystem = new WebBreadcrumbToggleButton(Assets.getString("START_SYSTEM"));
        Assets.associateComponent(startSystem, "setText", "START_SYSTEM");
        startSystem.removeMouseListener(startSystem.getMouseListeners()[0]);
        startSystem.setIcon(Assets.loadIcon("ic_tlc", icSize));
        breadcrumbButtons.put(Panels.START_PANEL, startSystem);
        stepBreadcrumb.add(startSystem);
        // Image Galery
        WebBreadcrumbToggleButton imageGalery = new WebBreadcrumbToggleButton(Assets.getString("IMAGE_GALERY"));
        Assets.associateComponent(imageGalery, "setText", "IMAGE_GALERY");
        imageGalery.removeMouseListener(imageGalery.getMouseListeners()[0]);
        imageGalery.setIcon(Assets.loadIcon("ic_picture", icSize));
        breadcrumbButtons.put(Panels.GALERY_PANEL, imageGalery);
        // Load Image
        WebBreadcrumbToggleButton loadImg = new WebBreadcrumbToggleButton(Assets.getString("IMAGE_LOAD_BC"));
        Assets.associateComponent(loadImg, "setText", "IMAGE_LOAD_BC");
        loadImg.removeMouseListener(loadImg.getMouseListeners()[0]);
        loadImg.setIcon(Assets.loadIcon("ic_drop", icSize));
        breadcrumbButtons.put(Panels.LOAD_IMAGE, loadImg);
        // Image Cut
        WebBreadcrumbToggleButton cutImg = new WebBreadcrumbToggleButton(Assets.getString("IMAGE_CROP_BC"));
        Assets.associateComponent(cutImg, "setText", "IMAGE_CROP_BC");
        cutImg.removeMouseListener(cutImg.getMouseListeners()[0]);
        cutImg.setIcon(Assets.loadIcon("ic_crop", icSize));
        breadcrumbButtons.put(Panels.CUT_IMAGE, cutImg);
        // Image Rotation
        WebBreadcrumbToggleButton rotImg = new WebBreadcrumbToggleButton(Assets.getString("IMAGE_ROTATION_BC"));
        Assets.associateComponent(rotImg, "setText", "IMAGE_ROTATION_BC");
        rotImg.removeMouseListener(rotImg.getMouseListeners()[0]); 
        rotImg.setIcon(Assets.loadIcon("ic_rotate", icSize));
        breadcrumbButtons.put(Panels.ROTATE_IMAGE, rotImg);
        // Select Samples
        WebBreadcrumbToggleButton selectSamples = new WebBreadcrumbToggleButton(Assets.getString("SELECT_SAMPLES_BC"));
        Assets.associateComponent(selectSamples, "setText", "SELECT_SAMPLES_BC");
        selectSamples.removeMouseListener(selectSamples.getMouseListeners()[0]);
        selectSamples.setIcon(Assets.loadIcon("ic_samples", icSize));
        breadcrumbButtons.put(Panels.SAMPLES_SELECT, selectSamples);
        // Special Points
        WebBreadcrumbToggleButton specialPoints = new WebBreadcrumbToggleButton(Assets.getString("SPECIAL_POINTS_BC"));
        Assets.associateComponent(specialPoints, "setText", "SPECIAL_POINTS_BC");
        specialPoints.removeMouseListener(specialPoints.getMouseListeners()[0]);
        specialPoints.setIcon(Assets.loadIcon("ic_points", icSize));
        breadcrumbButtons.put(Panels.SAMPLES_POINTS, specialPoints);
        // Samples Analysis
        WebBreadcrumbToggleButton samplesAnalysis = new WebBreadcrumbToggleButton(Assets.getString("SAMPLES_ANALYSIS_BC"));
        Assets.associateComponent(samplesAnalysis, "setText", "SAMPLES_ANALYSIS_BC");
        samplesAnalysis.removeMouseListener(samplesAnalysis.getMouseListeners()[0]); 
        samplesAnalysis.setIcon(Assets.loadIcon("ic_line_chart", icSize));
        breadcrumbButtons.put(Panels.SAMPLES_ANALYSIS, samplesAnalysis);
        // Samples Analysis Results
        WebBreadcrumbToggleButton samplesResultsAnalysis = new WebBreadcrumbToggleButton(Assets.getString("SAMPLES_ANALYSIS_RESULTS_BC"));
        Assets.associateComponent(samplesResultsAnalysis, "setText", "SAMPLES_ANALYSIS_RESULTS_BC");
        samplesResultsAnalysis.removeMouseListener(samplesResultsAnalysis.getMouseListeners()[0]); 
        samplesResultsAnalysis.setIcon(Assets.loadIcon("ic_results", icSize));
        breadcrumbButtons.put(Panels.SAMPLES_ANALYSIS_RESULTS, samplesResultsAnalysis);
        // Samples Analysis Reports
        WebBreadcrumbToggleButton samplesResultsReports = new WebBreadcrumbToggleButton(Assets.getString("SAMPLES_ANALYSIS_REPORTS_BC"));
        Assets.associateComponent(samplesResultsReports, "setText", "SAMPLES_ANALYSIS_REPORTS_BC");
        samplesResultsReports.removeMouseListener(samplesResultsReports.getMouseListeners()[0]); 
        samplesResultsReports.setIcon(Assets.loadIcon("ic_report_document", icSize));
        breadcrumbButtons.put(Panels.ANALYSIS_REPORTS, samplesResultsReports);
        // Group
        SwingUtils.groupButtons(stepBreadcrumb);
        // Settings
        stepBreadcrumb.setEncloseLastElement(true);
        stepBreadcrumb.setUndecorated(true);
        // Container panel
        WebPanel panel = new WebPanel();
        panel.setUndecorated(false);
        panel.setPaintBackground(true);
        panel.setPaintTop(false);
        panel.setPaintLeft(false);
        panel.setPaintRight(false);
        panel.setMargin(0);
        panel.setRound(0);
        panel.add(stepBreadcrumb);
        //
        return panel;
    }
    
    /**
     * Create Main Status Bar
     * @return status bar
     */
    private WebStatusBar createStatusBar() {
        // Main Status Bar
        statusBar = new WebStatusBar();
        // Simple label
        statusText = new WebStatusLabel(Assets.getString("WAITING"));
        Assets.associateComponent(statusText, "setText", "WAITING");
        statusBar.add(statusText, ToolbarLayout.CENTER);
        // Indeterminate Progress Var
        progressBar = new WebProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setStringPainted(true);
        progressBar.setString(Assets.getString("PLEASE_WAIT"));
        progressBar.setVisible(false);
        Assets.associateComponent(progressBar, "setString", "PLEASE_WAIT");
        statusBar.add(progressBar, ToolbarLayout.MIDDLE);
        // Simple memory bar
        WebMemoryBar memoryBar = new WebMemoryBar();
        memoryBar.setShowMaximumMemory(false);
        memoryBar.setPreferredWidth(memoryBar.getPreferredSize().width + 20);
        statusBar.add(memoryBar, ToolbarLayout.END);
        //
        return statusBar;
    }
       
    /**
     * Update Center panel
     * @param npanel new panel to show
     */
    private void updatePanel(JComponent npanel, boolean restart) {
        centerPanel = npanel;
        // If transition is enabled
        if (transitionEnabled) {
            nextTransition = new ComponentTransition(npanel,createEffect(restart? Direction.right : Direction.vertical, 1));
            nextTransition.addComponentListener(componentListener);
            currentTransition.setTransitionEffect(createEffect(restart? Direction.right : Direction.vertical, 1));
            currentTransition.performTransition(nextTransition);
        } else {
            currentTransition.setContent(npanel);
        }
    }
    
    /**
     * Create Panel Transition Effect
     * @param dir
     * @return 
     */
    private TransitionEffect createEffect(Direction dir, int effect) {
        switch (effect) {
            case 0: // Slide effect
                SlideTransitionEffect slide = new SlideTransitionEffect();
                slide.setDirection(dir);
                slide.setFade(false);
                slide.setType(SlideType.moveBoth);
                slide.setSpeed(30);
                return slide;
            case 1: // Curtain effect
                CurtainTransitionEffect curtain = new CurtainTransitionEffect();
                curtain.setSpeed(100);
                curtain.setSize(50);
                curtain.setType(CurtainType.slide);
                curtain.setDirection(dir);
                return curtain;
            case 2: // Zoom effect
                ZoomTransitionEffect zoom = new ZoomTransitionEffect();
                zoom.setMinimumSpeed(0.03f);
                zoom.setSpeed(0.05f);
                zoom.setType(ZoomType.random);
                return zoom;
            case 3: // Fade effect
                FadeTransitionEffect fade = new FadeTransitionEffect();
                fade.setMinimumSpeed(0.01f);
                fade.setSpeed(0.05f);
                return fade;
            case 4: // Block effect
                BlocksTransitionEffect block = new BlocksTransitionEffect();
                block.setDirection(Direction.random);
                block.setType(BlockType.cascade);
                block.setSize(20);
                block.setSpeed(15);
                block.setFade(true);
                return block;
        }
        return null;
    }
    
    /**
     * Window Listener
     */
    private final WindowAdapter windowListener = new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent e) {
            setChanged();
            notifyObservers("EXIT_SYSTEM");
        }
    };
    
    /**
     * Main Window State Listener
     */
    private final WindowStateListener windowStateListener = new WindowStateListener() {
        @Override
        public void windowStateChanged(WindowEvent e) {
            // Nothing for now
        }
    };
    
    /**
     * Components Change Listeners
     */    
    private final ComponentListener componentListener = new ComponentListener() {
        @Override
        public void componentResized(ComponentEvent e) {
            // Current Transition
            if (e.getSource() == currentTransition && !transitionStarted) {
                componetsSize = currentTransition.getSize();
                currentTransition.getContent().setSize(componetsSize);
            }
            // Next Transition
            if (e.getSource() == nextTransition && !transitionStarted) {
                componetsSize = nextTransition.getSize();
                nextTransition.getContent().setSize(componetsSize);
            }
            
            // Main Frame
            if (e.getSource() == mainFrame) {
                // Aboid resize while animation
                if (transitionStarted)
                    mainFrame.pack();
            }
        }

        @Override
        public void componentMoved(ComponentEvent e) {}

        @Override
        public void componentShown(ComponentEvent e) {}

        @Override
        public void componentHidden(ComponentEvent e) {}
    };
    
    /**
     * Transitions Listener
     */
    TransitionListener transitionListener = new TransitionListener() {
        @Override
        public void transitionStarted() {
            transitionStarted = true;
        }
        @Override
        public void transitionFinished() {
            transitionStarted = false;
        }
    };
    
    /**
     * General View Action Listener
     */
    private final ActionListener actionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (!transitionStarted) {
                setChanged();
                notifyObservers(e.getActionCommand());
            }
        }
    };
    
    /**
     * Export Menu's Action Listener
     */
    ActionListener exportListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (!transitionStarted) {
                setChanged();
                notifyObservers(new Pair<>("EXPORT_DATA", e.getActionCommand()));
            }
        }
    };
    
    /**
     * 
     * Public Methods Start.
     * 
     * Show Panels && Components
     * 
     */
    
    /**
     * Show Warning Message Pop-up dialog
     * @param message 
     */
    public void showWarningMessage(String message) {
        WebOptionPane.showMessageDialog(mainFrame, message, Assets.getString("WARNING"), WebOptionPane.WARNING_MESSAGE);
    }
    
    /**
     * Show Information Message Pop-up dialog
     * @param message 
     */
    public void showMessage(String message) {
        WebOptionPane.showMessageDialog(mainFrame, message, Assets.getString("MAIN_TITLE"), WebOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Show Confirm Message Pop-up dialog
     * @param message 
     * @return  
     */
    public boolean showConfirmDialog(String message) {
        int res = WebOptionPane.showConfirmDialog(mainFrame, message, Assets.getString("CONFIRM"),  WebOptionPane.YES_NO_OPTION, WebOptionPane.QUESTION_MESSAGE);
        return (res == WebOptionPane.YES_OPTION);
    }

    /**
     * Show/Hide indeterminate progress bar on the status bar
     * @param status show/hide
     */
    public void showProgress(boolean status) {
        progressBar.setVisible(status);
        statusBar.repaint();
    }
    
    /**
     * Show Load project - file Chooser
     * @param ext file extension
     * @return selected file path (null if cancel)
     */
    public File showLoadProject(String ext) {
        WebFileChooser fc = new WebFileChooser(Settings.getWorkSpace());
        fc.setApproveButtonText(Assets.getString("OPEN"));
        fc.setFileFilter(new FileNameExtensionFilter(Assets.getString("JTLC_PROJECT"), ext));
        fc.setAcceptAllFileFilterUsed(false);
        fc.setMultiSelectionEnabled(false);
        if (fc.showOpenDialog(mainFrame) == WebFileChooser.APPROVE_OPTION)
            return fc.getSelectedFile();
        return null;
    }
    
    /**
     * Show Save Proyect - file chooser
     * @param extName file extension display name
     * @param ext file extension
     * @param defName default file name
     * @return selected file path (null if cancel)
     */
    public File showFileSave(String extName, String ext, String defName) {
        WebFileChooser fc = new WebFileChooser(Settings.getWorkSpace());
        fc.setFileFilter(new FileNameExtensionFilter(extName, ext));
        fc.setSelectedFile(defName + "." + ext);
        fc.setAcceptAllFileFilterUsed(false);
        fc.setMultiSelectionEnabled(false);
        if (fc.showSaveDialog(mainFrame) ==  WebFileChooser.APPROVE_OPTION && fc.getSelectedFile() != null) {
            File file = fc.getSelectedFile();
            // Check file overwrite
            if (file.exists() && !showConfirmDialog(Assets.getString("OVERWRITE_FILE") + "\n" + Assets.getString("FILE") + ": " + file.getName())) {
                return null;
            }
            // Check file extension
            String extf = FileUtils.getFileExtPart(fc.getSelectedFile(), false);
            if (!extf.equals(ext)) {
                file = new File(file.getPath() + "." + ext);
            }
            return file;
        }
        return null;
    }
    
    /**
     * Show Select Directory dialog - Directory Chooser.
     * @return selected directory path (null if cancel)
     */
    public File showDirectoryChooser() {
        WebDirectoryChooser dc = new WebDirectoryChooser(mainFrame, Assets.getString("SELECT_FOLDER"));
        dc.setSelectedDirectory(new File(Settings.getWorkSpace()));
        int opt = dc.showDialog();
        if (WebDirectoryChooser.OK_OPTION == opt)
            if (dc.getSelectedDirectory() != null)
                return dc.getSelectedDirectory();
        return null;
    }
    
    /**
     * Show New Proyect data input dialog.
     * @return new project data (as ProjectDTO)
     */
    public ProjectDTO showNewProjectDialog() {
        currentDialog = new ProjectDialog(mainFrame);
        return currentDialog.getResults();
    }
    
    /**
     * Show Edit Proyect data input dialog.
     * @param dto
     * @return project data (as InfoDTO)
     */
    public InfoDTO showEditProjectDialog(InfoDTO dto) {
        currentDialog = new InfoDialog(mainFrame, dto);
        return currentDialog.getResults();
    }
    
    /**
     * Show System Settings change dialog.
     * @param dto
     * @return system settings (as SettingsDTO)
     */
    public SettingsDTO showSettingsDialog(SettingsDTO dto) {
        currentDialog = new SettingsDialog(mainFrame, dto);
        return currentDialog.getResults();
    }
    
    /**
     * Show Image export dialog.
     * @param dto
     * @return image export data (as ImageExportDTO)
     */
    public ImageExportDTO showImageExportDialog(ImageExportDTO dto) {
        currentDialog = new ImageExportDialog(mainFrame, dto);
        return currentDialog.getResults();
    }
    
    /**
     * Show scrollable text dialog.
     * @param title dialog title
     * @param text dialog content text
     * @param icon dialog icon (icon name from assets without extension)
     */
    public void showTextPanelDialog(String title, String text, String icon) {
        currentDialog = new TextPanelDialog(mainFrame, title, text, icon);
    }
    
    /**
     * Show Image Galery Panel.
     * @param dto panel data
     * @param restart if call is a for a restart
     */
    public void showImageGaleryPanel(GalleryDTO dto, boolean restart) {
        // Breadcrumb update
        stepBreadcrumb.remove(breadcrumbButtons.get(Panels.START_PANEL));
        statusText.setText(Assets.getString("SELECT_GALERY"));
        Assets.associateComponent(statusText, "setText", "SELECT_GALERY");
        stepBreadcrumb.add(breadcrumbButtons.get(Panels.GALERY_PANEL));
        // Check for previous panel, create or update
        JComponent oldPanel = mainPanels.get(Panels.GALERY_PANEL);
        if (oldPanel == null) {
            // Create Panel
            currentPanel = new GalleryPanel(dto, componetsSize);
            mainPanels.put(Panels.GALERY_PANEL, currentPanel);
        } else {
            // Update previus panel
            ((IPanel) oldPanel).updatePanel(dto);
            oldPanel.setSize(componetsSize);
            currentPanel = oldPanel;
        }
        // Update to gallery panel
        updatePanel(currentPanel, restart);
        stepPanel = Panels.GALERY_PANEL;
    }
    
    /**
     * Show Image Drop Panel.
     * @param dto panel data
     * @param restart if call is a for a restart
     */
    public void showImageDropPanel(DropDTO dto, boolean restart) {
        // Breadcrumb update
        stepBreadcrumb.remove(breadcrumbButtons.get(Panels.START_PANEL));
        //stepBreadcrumb.remove(breadcrumbButtons.get(Panels.GALERY_PANEL));
        statusText.setText(Assets.getString("LOAD_SAMPLES_IMAGE"));
        Assets.associateComponent(statusText, "setText", "LOAD_SAMPLES_IMAGE");
        stepBreadcrumb.add(breadcrumbButtons.get(Panels.LOAD_IMAGE));
        // Check for previous panel, create or update
        JComponent oldPanel = mainPanels.get(Panels.LOAD_IMAGE);
        if (oldPanel == null) {
            // Create Panel
            currentPanel = new DropPanel(dto, componetsSize);
            mainPanels.put(Panels.LOAD_IMAGE, currentPanel);
        } else {
            // Update previus panel
            ((IPanel) oldPanel).updatePanel(dto);
            oldPanel.setSize(componetsSize);
            currentPanel = oldPanel;
        }
        // Update to drop panel
        updatePanel(currentPanel, restart);
        stepPanel = Panels.LOAD_IMAGE;
    }
    
    /**
     * Show Image Cut Panel.
     * @param dto panel data
     * @param restart if call is a for a restart
     */
    public void showCutPanel(CuttingDTO dto, boolean restart) {
        statusText.setText(Assets.getString("CROP_SAMPLES_IMAGE"));
        Assets.associateComponent(statusText, "setText", "CROP_SAMPLES_IMAGE");
        stepBreadcrumb.add(breadcrumbButtons.get(Panels.CUT_IMAGE));        
        // Check for previous panel, create or update
        JComponent oldPanel = mainPanels.get(Panels.CUT_IMAGE);
        if (oldPanel == null) {
            // Create Panel
            currentPanel = new CutPanel(dto, componetsSize);
            mainPanels.put(Panels.CUT_IMAGE, currentPanel);
        } else {
            // Update previus panel
            ((IPanel) oldPanel).updatePanel(dto);
            oldPanel.setSize(componetsSize);
            currentPanel = oldPanel;
        }
        // Update to cut panel
        updatePanel(currentPanel, restart);
        stepPanel = Panels.CUT_IMAGE;
    }
    
    /**
     * Show Image rotation panel
     * @param dto panel data
     * @param restart if call is a for a restart
     */
    public void showRotionPanel(RotationDTO dto, boolean restart) {
        statusText.setText(Assets.getString("ROTATE_SAMPLES_IMAGE"));
        Assets.associateComponent(statusText, "setText", "ROTATE_SAMPLES_IMAGE");
        stepBreadcrumb.add(breadcrumbButtons.get(Panels.ROTATE_IMAGE));
        // Check for previous panel, create or update
        JComponent oldPanel = mainPanels.get(Panels.ROTATE_IMAGE);
        if (oldPanel == null) {
            // Create Panel
            currentPanel = new RotationPanel(dto, componetsSize);
            mainPanels.put(Panels.ROTATE_IMAGE, currentPanel);
        } else {
            // Update previus panel
            ((IPanel) oldPanel).updatePanel(dto);
            oldPanel.setSize(componetsSize);
            currentPanel = oldPanel;
        }
        // Update to rotation panel
        updatePanel(currentPanel, restart);
        stepPanel = Panels.ROTATE_IMAGE;
    }
    
    /**
     * Show Samples Selector panel
     * @param dto panel data
     * @param restart if call is a for a restart
     */
    public void showSamplesSelector(SplitDTO dto, boolean restart) {
        statusText.setText(Assets.getString("SELECT_SAMPLES"));
        Assets.associateComponent(statusText, "setText", "SELECT_SAMPLES");
        stepBreadcrumb.add(breadcrumbButtons.get(Panels.SAMPLES_SELECT));        
        // Check for previous panel, create or update
        JComponent oldPanel = mainPanels.get(Panels.SAMPLES_SELECT);
        if (oldPanel == null) {
            // Create Panel
            currentPanel = new SplitPanel(dto, componetsSize);
            mainPanels.put(Panels.SAMPLES_SELECT, currentPanel);
        } else {
            // Update previus panel
            ((IPanel) oldPanel).updatePanel(dto);
            oldPanel.setSize(componetsSize);
            currentPanel = oldPanel;
        }
        // Update to rotation panel
        updatePanel(currentPanel, restart);
        stepPanel = Panels.SAMPLES_SELECT;
    }
    
    /**
     * Show Samples Special Points Selector Panel.
     * @param dto panel data
     * @param restart if call is a for a restart
     */
    public void showSamplesSpecialPoints(DataDTO dto, boolean restart) {
        statusText.setText(Assets.getString("SELECT_SPECIALS_POINTS"));
        Assets.associateComponent(statusText, "setText", "SELECT_SPECIALS_POINTS");
        stepBreadcrumb.add(breadcrumbButtons.get(Panels.SAMPLES_POINTS));
        //
        currentPanel = new DataPanel(dto, componetsSize);
        updatePanel(currentPanel, restart);
        stepPanel = Panels.SAMPLES_POINTS;
    }
    
    /**
     * Show Per sample analysis panel
     * @param dto panel data
     * @param restart if call is a for a restart
     */
    public void showAnalysisPanel(AnalysisDTO dto, boolean restart) {
        statusText.setText(Assets.getString("SAMPLES_ANALYSIS"));
        Assets.associateComponent(statusText, "setText", "SAMPLES_ANALYSIS");
        stepBreadcrumb.add(breadcrumbButtons.get(Panels.SAMPLES_ANALYSIS));
        // Tabs Panel
        currentPanel = new AnalysisPanel(dto, componetsSize);
        updatePanel(currentPanel, restart);
        stepPanel = Panels.SAMPLES_ANALYSIS;
    }
    
    /**
     * Show Per sample analysis results panel
     * @param dto panel data
     * @param restart if call is a for a restart
     */
    public void showAnalysisResultsPanel(ResultsDTO dto, boolean restart) {
        statusText.setText(Assets.getString("SAMPLES_ANALYSIS_RESULTS"));
        Assets.associateComponent(statusText, "setText", "SAMPLES_ANALYSIS_RESULTS");
        stepBreadcrumb.add(breadcrumbButtons.get(Panels.SAMPLES_ANALYSIS_RESULTS));
        // Tabs Panel
        currentPanel = new ResultsPanel(dto, componetsSize);
        updatePanel(currentPanel, restart);
        stepPanel = Panels.SAMPLES_ANALYSIS_RESULTS;
    }
    
    /**
     * Show Analisis Reports panel
     * @param dto panel data
     * @param restart if call is a for a restart
     */
    public void showAnalysisReportsPanel(ReportsDTO dto, boolean restart) {
        statusText.setText(Assets.getString("SAMPLES_ANALYSIS_REPORTS"));
        Assets.associateComponent(statusText, "setText", "SAMPLES_ANALYSIS_REPORTS");
        stepBreadcrumb.add(breadcrumbButtons.get(Panels.ANALYSIS_REPORTS));
        // Tabs Panel
        currentPanel = new ReportsPanel(dto, componetsSize);
        updatePanel(currentPanel, restart);
        stepPanel = Panels.ANALYSIS_REPORTS;
    }
    
    /**
     * Remove Current panel and breadcrumb buttom.
     * @param panel Panel to remove
     */
    public void removeBreadcrumb(Panels panel) {
        // Check for valid panel
        if (panel != stepPanel)
            return;
        stepBreadcrumb.remove(breadcrumbButtons.get(panel));
        stepBreadcrumb.repaint();
    }
    
    /**
     * Restart View panel.
     */
    public void restartView() {
        stepBreadcrumb.removeAll();
        if (mainPanels.containsKey(Panels.GALERY_PANEL))
            stepBreadcrumb.add(breadcrumbButtons.get(Panels.GALERY_PANEL));
        else
            stepBreadcrumb.add(breadcrumbButtons.get(Panels.START_PANEL));
        updatePanel(createStartPanel(), true);
    }
    
    /**
     * 
     * Status update/get Methods.
     * 
     */
          
    /**
     * Get transitions enabled status
     * @return enabled/disabled
     */
    public boolean isTransitionsEnabled() {
        return transitionEnabled;
    }
    
    /**
     * Set transitions enabled status
     * @param status enabled/disabled
     */
    public void setTransitionEnabled(boolean status) {
        transitionEnabled = status;
    }
    
    /**
     * 
     * Frame Components Update Methods.
     * 
     */
    
    /**
     * Update Buttons status
     * @param start
     * @param restart
     * @param back
     * @param next 
     */
    public void updateButtons(boolean start, boolean restart, boolean back, boolean next) {
        startBt.setVisible(start);
        startBt.setEnabled(start);
        restartBt.setVisible(true);
        restartBt.setEnabled(restart);
        backBt.setEnabled(back);
        nextBt.setEnabled(next);
        nextBt.setVisible(!start);
    }
    
    /**
     * Enable disable menu buttons
     * @param edit menu edit
     * @param save menu save
     * @param as menu save as
     */
    public void updateMenu(boolean edit, boolean save, boolean as) {
        editItem.setEnabled(edit);
        saveItem.setEnabled(save);
        saveAsItem.setEnabled(as);
    }
    
    /**
     * Update Experiment/Samples data export menu.
     * @param dto export data references
     */
    public void updateExportMenu(ExportDTO dto) {
        int icoSize = 16;
        // Clear current menu
        exportImage.removeAll();
        exportData.removeAll();
        exportMean.removeAll();
        exportProcessedImage.removeAll();
        exportReports.removeAll();
        // Enable/Disable export menu
        menuExport.setEnabled(dto != null && dto.hasChanged());
        // Avoid empty list
        if (dto == null) return;
        // Add per sample and experiment data
        exportData.setEnabled(dto.hasDatas());
        if (dto.hasDatas()) {
            for (Pair<Integer,String> pair: dto.getDatas()) {
                String name = Assets.shortString(pair.getSecond(), 40, true);
                WebMenuItem data = new WebMenuItem(Assets.getString(pair.getFirst() == -1 ? "PARAM_EXPERIMENT" : "PARAM_SAMPLE", name), Assets.loadIcon(pair.getFirst() == -1? "ic_jtlc" : "ic_sample", icoSize));
                Assets.associateComponentAndParams(data, "setText", pair.getFirst() == -1 ? "PARAM_EXPERIMENT" : "PARAM_SAMPLE", new Object[]{name});
                data.setActionCommand("DATA#" + pair.getFirst());
                data.addActionListener(exportListener);
                exportData.add(data);
            }
        }
        exportMean.setEnabled(dto.hasMeans());
        if (dto.hasMeans()) {
            for (Pair<Integer,String> pair: dto.getMeans()) {
                String name = Assets.shortString(pair.getSecond(), 40, true);
                WebMenuItem mean = new WebMenuItem(Assets.getString(pair.getFirst() == -1 ? "PARAM_EXPERIMENT" : "PARAM_SAMPLE", name), Assets.loadIcon(pair.getFirst() == -1? "ic_jtlc" : "ic_sample", icoSize));
                Assets.associateComponentAndParams(mean, "setText", pair.getFirst() == -1 ? "PARAM_EXPERIMENT" : "PARAM_SAMPLE", new Object[]{name});
                mean.setActionCommand("MEAN#" + pair.getFirst());
                mean.addActionListener(exportListener);
                exportMean.add(mean);
            }
        }
        exportImage.setEnabled(dto.hasImages());
        if (dto.hasImages()) {
            for (Pair<Integer,String> pair: dto.getImages()) {
                String name = Assets.shortString(pair.getSecond(), 40, true);
                WebMenuItem image = new WebMenuItem(Assets.getString(pair.getFirst() == -1 ? "PARAM_EXPERIMENT" : "PARAM_SAMPLE", name), Assets.loadIcon(pair.getFirst() == -1? "ic_jtlc" : "ic_sample", icoSize));
                Assets.associateComponentAndParams(image, "setText", pair.getFirst() == -1 ? "PARAM_EXPERIMENT" : "PARAM_SAMPLE", new Object[]{name});
                image.setActionCommand("IMAGE#" + pair.getFirst());
                image.addActionListener(exportListener);
                exportImage.add(image);
            }
        }
        exportProcessedImage.setEnabled(dto.hasProcessedImages());
        if (dto.hasProcessedImages()) {
            for (Pair<Integer,String> pair: dto.getProcessedImages()) {
                String name = Assets.shortString(pair.getSecond(), 40, true);
                WebMenuItem image = new WebMenuItem(Assets.getString(pair.getFirst() == -1 ? "PARAM_EXPERIMENT" : "PARAM_SAMPLE", name), Assets.loadIcon(pair.getFirst() == -1? "ic_jtlc" : "ic_sample", icoSize));
                Assets.associateComponentAndParams(image, "setText", pair.getFirst() == -1 ? "PARAM_EXPERIMENT" : "PARAM_SAMPLE", new Object[]{name});
                image.setActionCommand("PROCESSED_IMAGE#" + pair.getFirst());
                image.addActionListener(exportListener);
                exportProcessedImage.add(image);
            }
        }
        exportReports.setEnabled(dto.hasReports());
        if (dto.hasReports()) {
            for (Pair<Integer,String> pair: dto.getProcessedImages()) {
                String name = Assets.shortString(pair.getSecond(), 40, true);
                WebMenu report = new WebMenu(Assets.getString(pair.getFirst() == -1 ? "PARAM_EXPERIMENT" : "PARAM_SAMPLE", name), Assets.loadIcon(pair.getFirst() == -1? "ic_jtlc" : "ic_sample", icoSize));
                Assets.associateComponentAndParams(report, "setText", pair.getFirst() == -1 ? "PARAM_EXPERIMENT" : "PARAM_SAMPLE", new Object[]{name});
                WebMenuItem pdf = new WebMenuItem(Assets.getString("AS_PDF"), Assets.loadIcon("ic_pdf", icoSize));
                pdf.setActionCommand("REPORT#" + pair.getFirst() + "#PDF");
                pdf.addActionListener(exportListener);
                report.add(pdf);
                WebMenuItem odt = new WebMenuItem(Assets.getString("AS_ODT"), Assets.loadIcon("ic_doc", icoSize));
                odt.setActionCommand("REPORT#" + pair.getFirst() + "#ODT");
                odt.addActionListener(exportListener);
                report.add(odt);
                WebMenuItem html = new WebMenuItem(Assets.getString("AS_HTML"), Assets.loadIcon("ic_html", icoSize));
                html.setActionCommand("REPORT#" + pair.getFirst() + "#HTML");
                html.addActionListener(exportListener);
                report.add(html);
                WebMenuItem txt = new WebMenuItem(Assets.getString("AS_CSV"), Assets.loadIcon("ic_csv", icoSize));
                txt.setActionCommand("REPORT#" + pair.getFirst() + "#CSV");
                txt.addActionListener(exportListener);
                report.add(txt);
                //
                exportReports.add(report);
            }
        }
    }
    
    /**
     * Get Current Panel Results as specific DTO.
     * @param <T> Desired DTO
     * @return current panel results specific DTO
     */
    public <T extends AbstractDTO> T getValues() {
        return ((IPanel)centerPanel).getResults();
    }
    
    /**
     * 
     * General Frame data get methods.
     * 
     */
    
    /**
     * Get main frame window extended state
     * @return 
     */
    public int getWindowExtendedState() {
        return mainFrame.getExtendedState();
    }
    
    /**
     * Get main frame window size
     * @return 
     */
    public Dimension getWindowSize() {
        if (mainFrame.getExtendedState() != JFrame.MAXIMIZED_BOTH)
            return mainFrame.getSize();
        return Settings.getWindowSize();
    }
    
    /**
     * Get main frame window location
     * @return 
     */
    public Point getWindowLocation() {
        return mainFrame.getLocation();
    }
}