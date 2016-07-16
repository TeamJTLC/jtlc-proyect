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
package jtlc.view.panels;

import com.alee.extended.image.WebDecoratedImage;
import com.alee.extended.panel.GroupPanel;
import com.alee.laf.button.WebButton;
import com.alee.laf.button.WebToggleButton;
import com.alee.laf.filechooser.WebFileChooser;
import com.alee.laf.label.WebLabel;
import com.alee.laf.optionpane.WebOptionPane;
import com.alee.laf.scroll.WebScrollPane;
import com.alee.laf.tabbedpane.TabStretchType;
import com.alee.laf.tabbedpane.TabbedPaneStyle;
import com.alee.laf.tabbedpane.WebTabbedPane;
import com.alee.laf.tabbedpane.WebTabbedPaneUI;
import com.alee.laf.text.WebEditorPane;
import com.alee.laf.text.WebTextArea;
import com.alee.laf.toolbar.ToolbarStyle;
import com.alee.laf.toolbar.WebToolBar;
import com.alee.managers.language.data.TooltipWay;
import com.alee.managers.popup.PopupWay;
import com.alee.managers.popup.WebButtonPopup;
import com.alee.managers.tooltip.TooltipManager;
import com.alee.managers.tooltip.WebCustomTooltip;
import ij.ImagePlus;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.util.ArrayList;
import java.util.HashMap;
import static javax.swing.JLayeredPane.DRAG_LAYER;
import javax.swing.filechooser.FileNameExtensionFilter;
import jtlc.assets.Assets;
//
import jtlc.main.common.Pair;
import jtlc.main.common.Settings;
import jtlc.core.processing.AnalysisProcessing;
import jtlc.core.processing.ImageProcessing;
import jtlc.core.storage.ImageStore;
import jtlc.view.dto.AbstractDTO;
import jtlc.view.panels.dto.AnalysisDTO;
import jtlc.view.components.slider.CustomSliderUI;
import jtlc.view.components.slider.CustomSlider;
import jtlc.view.components.Plotter;
import jtlc.view.dialogs.ImageExportDialog;
import jtlc.view.dialogs.dto.ImageExportDTO;

/**
 * Samples Analysis Panel
 * Implements JTabbedPane with a tab per sample, where each tab
 * show a plot components with sample mean, peaks, maximuns and other
 * information; allows to change/add/remove peaks areas.
 * 
 * @author Cristian Tardivo
 */
public class AnalysisPanel extends WebTabbedPane implements IPanel {
    private final AnalysisDTO data;
    // Tabs Components
    private final List<Plotter> plotPanels;
    private final List<WebDecoratedImage> samplesImagesComponets;
    private final List<CustomSlider<Character>> horizontalSliders;
    private final List<CustomSlider<Character>> verticalSliders;
    private final HashMap<ImagePlus,List<Pair<Float,Float>>> baselinePoints;
    private final List<JLayeredPane> tabs;
    private final List<ImagePlus> samples;
    private final List<WebTextArea> comments;
    private final List<WebToolBar> toolbars;
    private final WebToolBar comparationToolBar;
    // Comparation Components
    private final JLayeredPane comparationTab;
    private final Plotter comparationPlot;
    private final WebTextArea comparationText;

    /**
     * Create AnalysisPanel
     * @param dto AnalysisPanel Data-Transfer-Object
     * @param size Initial Panel Size
     */
    public AnalysisPanel(AnalysisDTO dto, Dimension size) {
        // Save Params
        data = dto;
        samples = data.getSamplesImage();
        // Init List
        plotPanels = new ArrayList<>(samples.size());
        samplesImagesComponets = new ArrayList<>(samples.size());
        horizontalSliders = new ArrayList<>(samples.size());
        verticalSliders = new ArrayList<>(samples.size());
        tabs = new ArrayList<>(samples.size());
        comments = new ArrayList<>(samples.size());
        toolbars = new ArrayList<>(samples.size());
        baselinePoints = new HashMap<>(samples.size());
        // Configure Tab Panel
        WebTabbedPaneUI panelUI = new WebTabbedPaneUI();
        panelUI.setTabbedPaneStyle(TabbedPaneStyle.attached);
        panelUI.setTabStretchType(TabStretchType.multiline);
        this.setUI(panelUI);
        this.setBorder(BorderFactory.createEmptyBorder(1, 0, 1, 0));
        /* for future release of WebLaF
        this.setTabLayoutPolicy(WebTabbedPane.SCROLL_TAB_LAYOUT);
        this.setTabbedPaneStyle(TabbedPaneStyle.attached);
        */
        super.setSize(size);
        // Create Tabs
        for (ImagePlus img: samples) {
            // Main Data
            List<Pair<Float,Float>> function = data.getSampleMean(img);
            List<Pair<Float,Float>> positions = data.getSamplePeaks(img);
            List<Pair<Float,Float>> baseline = data.getSampleBaseline(img);
            // Function Maximum X value
            float max_x = function.get(function.size() - 1).getFirst();
            // Basic transform to relative values (between 0...1)
            float fpositions[] = new float[positions.size() * 2];
              for (int i = 0; i < positions.size(); i++) {
                  Pair<Float,Float> p = positions.get(i);
                  /// Get and relativize values (no changes when the values are already relatives)
                  fpositions[i * 2] = p.getFirst() / max_x;
                  fpositions[(i * 2) + 1] = p.getSecond() / max_x;
            }
            // Check Thumbs positions
            if (!checkArray(fpositions)) {
                fpositions = new float[]{0,1};
                System.err.println("Invalid Thumbs Positions for sample " + data.getSampleName(img));
            }
            // Resize Image
            ImagePlus resizedImg = img.duplicate();
            ImageProcessing.resizeImage(resizedImg, -1, size.height - 60);
            // Plot Size
            int plotWidth = size.width - resizedImg.getWidth() - 85;
            int plotHeight = size.height - 40;
            // Use sample seed/front points to compute the size of the vertical slider
            float pdiff = (float)resizedImg.getHeight() / (float)img.getHeight();
            int offset = (int)((float)data.getSampleFrontPoint(img) * pdiff + 0.5f);
            int height = (int)((float)data.getSampleSeedPoint(img) * pdiff - 0.5f);
            // Draw horizontal lines to mark limits (seed base line, front top lone)
            ImageProcessing.drawHorizontalLine(resizedImg, offset, 1, Color.RED);
            ImageProcessing.drawHorizontalLine(resizedImg, height, 1, Color.RED);
            // Create plot panel
            Plotter plotPanel = new Plotter(function, baseline, data.getSampleName(img));
            plotPanel.setSize(plotWidth, plotHeight);
            plotPanel.setPaintBackground(false);
            plotPanel.setAxisLabels(Assets.getString("IMAGE_MEAN"), Assets.getString("TIME"));
            Assets.associateComponent(plotPanel, "setTexts", "TIME", "IMAGE_MEAN");
            // Init Horizontal MultiThumbSlider
            Character chrs[] = new Character[fpositions.length];
            Arrays.fill(chrs, 'A');
            CustomSlider<Character> sliderH = new CustomSlider<>(CustomSlider.HORIZONTAL, fpositions, chrs);
            sliderH.setThumbShape(CustomSliderUI.Thumb.Triangle);
            // Init Default Slider Options
            sliderH.setSelectedThumb(-1);
            sliderH.setThumbOverlap(true);
            sliderH.setThumbRemovalAllowed(true);
            sliderH.setMouseThumbRemovalAllowed(false);
            sliderH.setAutoAdding(true);
            sliderH.setPaintTicks(true);
            sliderH.requestFocus();
            sliderH.setVisible(true);
            sliderH.setPaintSquares(false);
            sliderH.setPaintNumbers(true);
            sliderH.setCollisionPolicy(CustomSlider.Collision.NUDGE_OTHER);
            sliderH.setMinimumThumbnailCount(2);
            sliderH.setTickThumbMove(true);
            sliderH.setLinkedStatus(true);
            sliderH.setOpaque(false);
            // Multiplier Value
            sliderH.setMultValue(1);
            // Slider Size
            sliderH.setSize(plotPanel.getWidth() - 30, plotPanel.getHeight() + 20);
            sliderH.setTickLength(plotPanel.getHeight() - plotPanel.BOTTOM_MARGIN - plotPanel.TOP_MARGIN + 1);
            sliderH.setTickStart(-3);
            sliderH.setSquareLength(plotPanel.getHeight() - plotPanel.BOTTOM_MARGIN - plotPanel.TOP_MARGIN);
            sliderH.setSquareStart(-1);
            // Init Sample Image Component
            WebDecoratedImage sampleImageComponet = new WebDecoratedImage(resizedImg.getImage());
            sampleImageComponet.setSize(resizedImg.getWidth() + 4, resizedImg.getHeight() + 4);
            sampleImageComponet.setRound(0);
            sampleImageComponet.setDrawGlassLayer(false);
            sampleImageComponet.setDrawBorder(false);
            sampleImageComponet.setShadeWidth(4);
            // Init Vertical MultiThumbSlider
            Character chrs2[] = new Character[fpositions.length];
            Arrays.fill(chrs2, 'A');
            CustomSlider<Character> sliderV = new CustomSlider<>(CustomSlider.VERTICAL, fpositions, chrs2);
            sliderV.setThumbShape(CustomSliderUI.Thumb.Triangle);
            // Init Default Slider Options
            sliderV.setSelectedThumb(-1);
            sliderV.setThumbOverlap(true);
            sliderV.setThumbRemovalAllowed(true);
            sliderV.setMouseThumbRemovalAllowed(false);
            sliderV.setAutoAdding(true);
            sliderV.setPaintTicks(true);
            sliderV.requestFocus();
            sliderV.setVisible(true);
            sliderV.setPaintSquares(true);
            sliderV.setPaintNumbers(true);
            sliderV.setInverted(false);
            sliderV.setCollisionPolicy(CustomSlider.Collision.NUDGE_OTHER);
            sliderV.setMinimumThumbnailCount(2);
            sliderV.setMultValue(1);
            sliderV.setTickThumbMove(true);
            sliderV.setLinkedStatus(true);
            sliderV.setSize(plotPanel.getWidth() + 100, height - offset + 44);
            sliderV.setTickLength(resizedImg.getWidth() + 5);
            sliderV.setTickStart(-3);
            sliderV.setSquareLength(resizedImg.getWidth());
            sliderV.setSquareStart(0);
            /**
             * Change Listeners
             */
            sliderH.addChangeListener((ChangeEvent e) -> {
                plotPanel.setVisible(false);
                float[] thumbs = sliderH.getThumbPositions();
                // Update Vertical Slider
                if (sliderV.isLinkedEnabled()) {
                    sliderV.setFireChangeListeners(false);
                    sliderV.setValues(thumbs, sliderH.getValues());
                    sliderV.setFireChangeListeners(true);
                }
                // Compute and save new baseline points
                List<Pair<Float, Float>> points = AnalysisProcessing.validateAreas(function, thumbs);
                baselinePoints.put(img, points);
                // Update BaselinePoints
                plotPanel.setIntegrationAreas(points);
                // Update Y-Value pos to draw
                int thumb = sliderH.getSelectedThumb();
                if (thumb != -1 && sliderH.hasFocus()) {
                    plotPanel.setYValueXPos(thumbs[thumb]);
                    plotPanel.setDrawYValue(true);
                }
                plotPanel.setVisible(true);
            });        
            sliderV.addChangeListener((ChangeEvent e) -> {
                // Update Horizontal Slider
                if (sliderH.isLinkedEnabled()) {
                    sliderH.setFireChangeListeners(false);
                    sliderH.setValues(sliderV.getThumbPositions(), sliderV.getValues());
                    sliderH.setFireChangeListeners(true);
                }
                // Compute and save new baseline points
                List<Pair<Float, Float>> points = AnalysisProcessing.validateAreas(function, sliderV.getThumbPositions());
                baselinePoints.put(img, points);
                // Update BaseLinePoints
                plotPanel.setIntegrationAreas(points);
            });
            sliderH.addPropertyChangeListener((PropertyChangeEvent evt) -> {
                String name = evt.getPropertyName();
                if (name.equals(CustomSlider.SELECTED_THUMB_PROPERTY)) {
                    if (sliderH.hasFocus()) {
                        plotPanel.setVisible(false);
                        int thumb = sliderH.getSelectedThumb();
                        if (thumb != -1) {
                            plotPanel.setYValueXPos(sliderH.getThumbPositions()[thumb]);
                            plotPanel.setDrawYValue(true);
                        }
                        plotPanel.setVisible(true);
                    }
                }
            });
            // When Slider Horizontal gain focus draw y value of selected thumb
            sliderH.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                    plotPanel.setVisible(false);
                    int thumb = sliderH.getSelectedThumb();
                    if (thumb != -1) {
                        plotPanel.setYValueXPos(sliderH.getThumbPositions()[thumb]);
                        plotPanel.setDrawYValue(true);
                    }
                    plotPanel.setVisible(true);
                }

                @Override
                public void focusLost(FocusEvent e) {
                    plotPanel.setVisible(false);
                    int thumb = sliderH.getSelectedThumb();
                    if (thumb == -1) {
                        plotPanel.setDrawYValue(false);
                        plotPanel.setYValueXPos(null);
                        plotPanel.repaint();
                    }
                    plotPanel.setVisible(true);
                }
            });
            // Comments Pop-up
            WebButton showComments = new WebButton(Assets.loadIcon("ic_dialog", 20));
            WebCustomTooltip commentsTooltip = TooltipManager.setTooltip(showComments, Assets.getString("SHOW_ANALYSIS_COMMENTS"), TooltipWay.up, 100);
            Assets.associateComponent(commentsTooltip, "setTooltip", "SHOW_ANALYSIS_COMMENTS");
            WebTextArea textArea = new WebTextArea();
            textArea.setText(data.getAnalysisComments(img));
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            WebScrollPane areaScroll = new WebScrollPane(textArea);
            areaScroll.setPreferredSize(new Dimension(220, 140));
            WebButtonPopup commentPopup = new WebButtonPopup(showComments, PopupWay.upRight);
            WebLabel comentLabel = new WebLabel(Assets.getString("COMMENTS"), WebLabel.CENTER);
            Assets.associateComponent(comentLabel, "setText", "COMMENTS");
            GroupPanel commentContent = new GroupPanel(10, false, comentLabel, areaScroll);
            commentContent.setMargin(10);
            commentPopup.setContent(commentContent);
            commentPopup.setDefaultFocusComponent(areaScroll);
            showComments.setSize(28, 28);
            // Help Button
            WebButton showHelp = new WebButton(Assets.loadIcon("ic_help", 20));
            WebCustomTooltip helpTooltip = TooltipManager.setTooltip(showHelp, Assets.getString("SHOW_HELP"), TooltipWay.up, 100);
            Assets.associateComponent(helpTooltip, "setTooltip", "SHOW_HELP");
            WebEditorPane editorPane = new WebEditorPane("text/html",Assets.getString("ANALYSIS_HELP"));
            Assets.associateComponent(editorPane, "setText", "ANALYSIS_HELP");
            editorPane.setEditable(false);
            editorPane.setFocusable(false);
            WebScrollPane editorPaneScroll = new WebScrollPane(editorPane);
            editorPaneScroll.setPreferredSize(new Dimension(300, 200));
            editorPaneScroll.setFocusable(false);
            WebButtonPopup helpPopup = new WebButtonPopup(showHelp, PopupWay.upRight);
            WebLabel helpLabel = new WebLabel(Assets.getString("HELP"), WebLabel.CENTER);
            Assets.associateComponent(helpLabel, "setText", "HELP");
            GroupPanel helpContent = new GroupPanel(10, false, helpLabel, editorPaneScroll);
            helpContent.setMargin(10);
            helpPopup.setContent(helpContent);
            showHelp.setSize(28, 28);
            // Export image button and action
            WebButton exportImage = new WebButton(Assets.loadIcon("ic_export_image", 20));
            WebCustomTooltip exportTooltip = TooltipManager.setTooltip(exportImage, Assets.getString("EXPORT_TO_IMAGE"), TooltipWay.up, 100);
            Assets.associateComponent(exportTooltip, "setTooltip", "EXPORT_TO_IMAGE");
            AnalysisPanel aPanel = this;
            exportImage.addActionListener((ActionEvent e) -> {
                ImagePlus compExport = plotPanel.getImagePlus(600, 400);
                ImageExportDialog dialog = new ImageExportDialog(getOwningFrame(aPanel), new ImageExportDTO(compExport));
                ImageExportDTO results = dialog.getResults();
                if (results.hasChanged()) {
                    WebFileChooser fc = new WebFileChooser(Settings.getWorkSpace());
                    fc.setFileFilter(new FileNameExtensionFilter("JPG", "jpg"));
                    fc.setSelectedFile(data.getSampleName(img) + "-plot.jpg");
                    fc.setAcceptAllFileFilterUsed(false);
                    fc.setMultiSelectionEnabled(false);
                    if (fc.showSaveDialog(getOwningFrame(aPanel)) ==  WebFileChooser.APPROVE_OPTION && fc.getSelectedFile() != null) {
                        File file = fc.getSelectedFile();
                        if (file.exists() && !(WebOptionPane.showConfirmDialog(getOwningFrame(aPanel), Assets.getString("OVERWRITE_FILE") + "\n" + Assets.getString("FILE") + ": " + file.getName(), Assets.getString("CONFIRM"),  WebOptionPane.YES_NO_OPTION, WebOptionPane.QUESTION_MESSAGE) == WebOptionPane.YES_OPTION)) {
                            return;
                        }
                        compExport = plotPanel.getImagePlus(results.getWidth(), results.getHeight());
                        String path = file.getPath();
                        ImageStore.saveImage(compExport, path.endsWith(".jpg")? path : path + ".jpg");
                        WebOptionPane.showMessageDialog(getOwningFrame(aPanel), Assets.getString("IMAGE_SAVED"), Assets.getString("MAIN_TITLE"), WebOptionPane.INFORMATION_MESSAGE);
                    }
                }
            });
            exportImage.setSize(28, 28);
            // Commands Toolbar
            WebToolBar commandsTB = new WebToolBar(WebToolBar.VERTICAL);
            commandsTB.setFloatable(false);
            commandsTB.setToolbarStyle(ToolbarStyle.standalone);
            commandsTB.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            commandsTB.add(showHelp);
            commandsTB.add(exportImage);
            commandsTB.add(showComments);
            commandsTB.setSize(38, 102);
            // Locate Components
            plotPanel.setLocation(4, 20);
            sliderH.setLocation(plotPanel.LEFT_MARGIN - 18, 7);
            sampleImageComponet.setLocation(plotPanel.getLocation().x + plotPanel.getSize().width + 24, 20);
            sliderV.setLocation(plotPanel.getLocation().x + plotPanel.getSize().width - 3, offset);
            commandsTB.setLocation(0, size.height - commandsTB.getSize().height - 23);
            /**
             * Add Componetes to panel
             */
            JLayeredPane tab = new JLayeredPane();
            tab.setSize(size.width, size.height - 20);
            tab.add(commandsTB, JLayeredPane.FRAME_CONTENT_LAYER);
            tab.add(plotPanel, JLayeredPane.DEFAULT_LAYER);
            tab.add(sampleImageComponet, JLayeredPane.FRAME_CONTENT_LAYER);
            tab.add(sliderH, JLayeredPane.MODAL_LAYER);
            tab.add(sliderV, JLayeredPane.MODAL_LAYER);
            
            /**
             * Save Componets
             */
            plotPanels.add(plotPanel);
            samplesImagesComponets.add(sampleImageComponet);
            horizontalSliders.add(sliderH);
            verticalSliders.add(sliderV);
            comments.add(textArea);
            toolbars.add(commandsTB);
            tabs.add(tab);
            baselinePoints.put(img, baseline);
            /**
             * Add Tab
             */
            this.addTab(Assets.shortString(data.getSampleName(img), 25, true), tab);
        }
        // Comparation Panel        
        List<Pair<Float,Float>>[] values = new List[samples.size()];
        String[] names = new String[samples.size()];
        for (int i = 0; i < samples.size(); i++) {
            values[i] = data.getSampleMean(samples.get(i));
            names[i] = data.getSampleName(samples.get(i));
        }
        comparationPlot = new Plotter(values, names);
        comparationPlot.setPaintBackground(false);
        comparationPlot.setAxisLabels(Assets.getString("IMAGE_MEAN"), Assets.getString("TIME"));
        Assets.associateComponent(comparationPlot, "setTexts", "TIME", "IMAGE_MEAN", "REFERENCES");
        comparationPlot.setSize(size.width, size.height - 20);
        comparationPlot.setLocation(0, 0);
        // Comments Pop-up
        WebButton showComments = new WebButton(Assets.loadIcon("ic_dialog"));
        WebCustomTooltip commentsTooltip = TooltipManager.setTooltip(showComments, Assets.getString("SHOW_COMMENTS"), TooltipWay.up, 100);
        Assets.associateComponent(commentsTooltip, "setTooltip", "SHOW_COMMENTS");
        comparationText = new WebTextArea();
        comparationText.setText(data.getComparationComments());
        comparationText.setLineWrap(true);
        comparationText.setWrapStyleWord(true);
        WebScrollPane areaScroll = new WebScrollPane(comparationText);
        areaScroll.setPreferredSize(new Dimension(220, 140));
        WebButtonPopup commentPopup = new WebButtonPopup(showComments, PopupWay.upLeft);
        WebLabel comentLabel = new WebLabel(Assets.getString("COMMENTS"), WebLabel.CENTER);
        Assets.associateComponent(comentLabel, "setText", "COMMENTS");
        GroupPanel commentContent = new GroupPanel(10, false, comentLabel, areaScroll);
        commentContent.setMargin(10);
        commentPopup.setContent(commentContent);
        commentPopup.setDefaultFocusComponent(areaScroll);
        showComments.setSize(36, 36);
        // Help Button
        WebButton showHelp = new WebButton(Assets.loadIcon("ic_help"));
        WebCustomTooltip helpTooltip = TooltipManager.setTooltip(showHelp, Assets.getString("SHOW_HELP"), TooltipWay.up, 100);
        Assets.associateComponent(helpTooltip, "setTooltip", "SHOW_HELP");
        WebEditorPane editorPane = new WebEditorPane("text/html", Assets.getString("COMPARATION_HELP"));
        Assets.associateComponent(editorPane, "setText", "COMPARATION_HELP");
        editorPane.setEditable(false);
        editorPane.setFocusable(false);
        WebScrollPane editorPaneScroll = new WebScrollPane(editorPane);
        editorPaneScroll.setPreferredSize(new Dimension(300, 200));
        editorPaneScroll.setFocusable(false);
        WebButtonPopup helpPopup = new WebButtonPopup(showHelp, PopupWay.upLeft);
        WebLabel helpLabel = new WebLabel(Assets.getString("HELP"), WebLabel.CENTER);
        Assets.associateComponent(helpLabel, "setText", "HELP");
        GroupPanel helpContent = new GroupPanel(10, false, helpLabel, editorPaneScroll);
        helpContent.setMargin(10);
        helpPopup.setContent(helpContent);
        showHelp.setSize(36, 36);
        // Fill Button
        WebToggleButton fillCurves = new WebToggleButton(Assets.loadIcon("ic_fill"));
        WebCustomTooltip fillTooltip = TooltipManager.setTooltip(fillCurves, Assets.getString("FILL_CURVE"), TooltipWay.up, 100);
        Assets.associateComponent(fillTooltip, "setTooltip", "FILL_CURVE");
        fillCurves.addActionListener((ActionEvent e) -> {
            comparationPlot.setFillCurve(!comparationPlot.getFillCurve());
            comparationPlot.repaint();
        });
        fillCurves.setShadeToggleIcon(true);
        fillCurves.setSelected(comparationPlot.getFillCurve());
        // Export image button and action
        WebButton exportImage = new WebButton(Assets.loadIcon("ic_export_image"));
        WebCustomTooltip exportTooltip = TooltipManager.setTooltip(exportImage, Assets.getString("EXPORT_TO_IMAGE"), TooltipWay.up, 100);
        Assets.associateComponent(exportTooltip, "setTooltip", "EXPORT_TO_IMAGE");
        AnalysisPanel aPanel = this;
        exportImage.addActionListener((ActionEvent e) -> {
            ImagePlus compExport = comparationPlot.getImagePlus(600, 400);
            ImageExportDialog dialog = new ImageExportDialog(getOwningFrame(aPanel), new ImageExportDTO(compExport));
            ImageExportDTO results = dialog.getResults();
            if (results.hasChanged()) {
                WebFileChooser fc = new WebFileChooser(Settings.getWorkSpace());
                fc.setFileFilter(new FileNameExtensionFilter("JPG", "jpg"));
                fc.setSelectedFile("samples-comparation-plot.jpg");
                fc.setAcceptAllFileFilterUsed(false);
                fc.setMultiSelectionEnabled(false);
                if (fc.showSaveDialog(getOwningFrame(aPanel)) ==  WebFileChooser.APPROVE_OPTION && fc.getSelectedFile() != null) {
                    File file = fc.getSelectedFile();
                    if (file.exists() && !(WebOptionPane.showConfirmDialog(getOwningFrame(aPanel), Assets.getString("OVERWRITE_FILE") + "\n" + Assets.getString("FILE") + ": " + file.getName(), Assets.getString("CONFIRM"),  WebOptionPane.YES_NO_OPTION, WebOptionPane.QUESTION_MESSAGE) == WebOptionPane.YES_OPTION)) {
                        return;
                    }
                    compExport = comparationPlot.getImagePlus(results.getWidth(), results.getHeight());
                    String path = file.getPath();
                    ImageStore.saveImage(compExport, path.endsWith(".jpg")? path : path + ".jpg");
                    WebOptionPane.showMessageDialog(getOwningFrame(aPanel), Assets.getString("IMAGE_SAVED"), Assets.getString("MAIN_TITLE"), WebOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
        // Commands Toolbar
        comparationToolBar = new WebToolBar(WebToolBar.HORIZONTAL);
        comparationToolBar.setFloatable(false);
        comparationToolBar.setToolbarStyle(ToolbarStyle.standalone);
        comparationToolBar.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        comparationToolBar.add(showHelp);
        comparationToolBar.add(fillCurves);
        comparationToolBar.add(exportImage);
        comparationToolBar.add(showComments);
        comparationToolBar.setSize(150, 45);
        comparationToolBar.setLocation(size.width - comparationToolBar.getWidth() - 5, size.height - comparationToolBar.getSize().height - 23);
        //
        comparationTab = new JLayeredPane();
        comparationTab.add(comparationPlot, JLayeredPane.DEFAULT_LAYER);
        comparationTab.add(comparationToolBar, DRAG_LAYER);
        /**
         * Add Tab
         */
        this.addTab(Assets.getString("COMPARATION"), comparationTab);
        Assets.associateComponent(this, "setTabsTitles", "COMPARATION");
    }
    
    /**
     * Method for update Comparation Tab title when language change.
     * @param title new title
     */
    public void setTabsTitles(String title) {
        this.setTitleAt(this.getTabCount() - 1, title);
    }
    
    /**
     * Resize Panel Components and change location
     * @param size new panel size
     */
    @Override
    public void setSize(Dimension size) {
        for (int i = 0; i < samples.size(); i++) {
            // Resize Image
            ImagePlus img = samples.get(i);
            ImagePlus resizedImg = img.duplicate();
            ImageProcessing.resizeImage(resizedImg, -1, size.height - 60);
            // Use sample seed/front points to compute the size of the vertical slider
            float pdiff = (float)resizedImg.getHeight() / (float)img.getHeight();
            int offset = (int)((float)data.getSampleFrontPoint(img) * pdiff + 0.5f);
            int height = (int)((float)data.getSampleSeedPoint(img) * pdiff - 0.5f);
            // Draw horizontal lines to mark limits (seed base line, front top lone)
            ImageProcessing.drawHorizontalLine(resizedImg, offset, 1, Color.RED);
            ImageProcessing.drawHorizontalLine(resizedImg, height, 1, Color.RED);
            // Plot Size
            Plotter plotPanel = plotPanels.get(i);
            int plotWidth = size.width - resizedImg.getWidth() - 85;
            int plotHeight = size.height - 40;
            plotPanel.setSize(plotWidth, plotHeight);
            // Slider Horizontal Size
            CustomSlider<Character> sliderH = horizontalSliders.get(i);
            sliderH.setSize(plotPanel.getWidth() - 30, plotPanel.getHeight() + 20);
            sliderH.setTickLength(plotPanel.getHeight() - plotPanel.BOTTOM_MARGIN - plotPanel.TOP_MARGIN + 1);
            sliderH.setSquareLength(plotPanel.getHeight() - plotPanel.BOTTOM_MARGIN - plotPanel.TOP_MARGIN);
            // Slider Vertical Size
            CustomSlider<Character> sliderV = verticalSliders.get(i);
            sliderV.setSize(plotPanel.getWidth() + 100, height - offset + 44);
            sliderV.setTickLength(resizedImg.getWidth() + 5);
            sliderV.setSquareLength(resizedImg.getWidth());
            // Update image component
            WebDecoratedImage sampleImageComponet = samplesImagesComponets.get(i);
            sampleImageComponet.setSize(resizedImg.getWidth() + 4, resizedImg.getHeight() + 4);
            sampleImageComponet.setImage(resizedImg.getImage());
            // Update Tab Size
            JLayeredPane tab = tabs.get(i);
            tab.setSize(size.width, size.height - 20);
            // Commands toolbar
            WebToolBar commandsTB = toolbars.get(i);
            // Locate Components
            plotPanel.setLocation(4, 20);
            sliderH.setLocation(plotPanel.LEFT_MARGIN - 18, 7);
            sampleImageComponet.setLocation(plotPanel.getLocation().x + plotPanel.getSize().width + 24, 20);
            sliderV.setLocation(plotPanel.getLocation().x + plotPanel.getSize().width - 3, offset);
            commandsTB.setLocation(0, size.height - commandsTB.getSize().height - 23);
        }
        // Update Comparation tab size
        comparationPlot.setSize(size.width, size.height - 20);
        comparationPlot.setLocation(0, 0);
        // Update comparation tab commands toolbar location
        comparationToolBar.setLocation(size.width - comparationToolBar.getWidth() - 5, size.height - comparationToolBar.getSize().height - 23);
        // Update main panel size
        super.setSize(size.width, size.height);
    }

    /**
     * Get Panel Owning Frame
     * @param comp
     * @return 
     */
    public static JFrame getOwningFrame(Component comp) {
        if (comp == null)
          throw new IllegalArgumentException("null Component passed");
        // check if frame
        if (comp instanceof JFrame)
            return (JFrame) comp;
        return getOwningFrame(SwingUtilities.windowForComponent(comp));
    }
    
    /**
     * Get Panel Changes
     * @return AnalysisPanel Data-Transfer-Object
     */
    @Override
    public AnalysisDTO getResults() {
        List<ImagePlus> images = data.getSamplesImage();
        data.setChanged(false);
        for (int i = 0; i < images.size(); i++) {
            ImagePlus img = images.get(i);
            // Compute sample peaks
            CustomSlider<Character> slider = horizontalSliders.get(i);            
            float positions[] = slider.getThumbPositions();
            List<Pair<Float,Float>> peaks = new ArrayList<>(positions.length / 2);
            for (int j = 0; j < positions.length; j += 2) {
                float a = positions[j];
                float b = positions[j + 1];
                peaks.add(new Pair<>(a, b));
            }
            // Check for changes and update values if changed
            if (!data.getSamplePeaks(img).equals(peaks)) {
                // Save sample peaks
                data.setSamplePeaks(img, peaks);
                // Sample data changes
                data.setChanged(img, true);
                // Global changes
                data.setChanged(true);
            }
            // Get sample baseline and check for updates/changes
            List<Pair<Float, Float>> baseline = baselinePoints.get(img);
            if (!data.getSampleBaseline(img).equals(baseline)) {
                // Sabe sample baseline
                data.setSampleBaseline(img, baseline);
                // Sample data changes
                data.setChanged(img, true);
                // Global changes
                data.setChanged(true);
            }
            // Comments not count like a change
            data.setAnalysisComments(img, comments.get(i).getText());
        }
        // Save comparation plot comments
        data.setComparationComments(comparationText.getText());
        return data;
    }
    
    /**
     * Update panel components to new values in dto
     * @param dto Panel Data-Transfer-Object
     */
    @Override
    public void updatePanel(AbstractDTO dto) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /**
     * Check if array is valid for thumbs positions
     * @param array float array to check
     * @return valid or invalid (true / false)
     */
    private boolean checkArray(float[] array) {
        // Check Thumbs positions
        if (array.length % 2 != 0 || array.length == 0)
            return false;
        // Check postions range
        for (float pos : array)
            if (pos > 1 || pos < 0)
                return false;
        // Check growing order
        for (int i = 0; i < array.length - 1; i ++) {
            if (array[i] > array[i + 1])
                return false;
        }
        return true;
    }
}