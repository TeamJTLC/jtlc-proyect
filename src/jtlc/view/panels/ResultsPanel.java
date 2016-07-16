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

import com.alee.extended.panel.GroupPanel;
import com.alee.extended.panel.WebAccordion;
import com.alee.extended.panel.WebAccordionStyle;
import com.alee.laf.button.WebButton;
import com.alee.laf.filechooser.WebFileChooser;
import com.alee.laf.label.WebLabel;
import com.alee.laf.optionpane.WebOptionPane;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;
import com.alee.laf.tabbedpane.TabbedPaneStyle;
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
import java.io.File;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.stream.Collectors;
import static javax.swing.JLayeredPane.DRAG_LAYER;
import javax.swing.filechooser.FileNameExtensionFilter;
import jtlc.assets.Assets;
//
import jtlc.main.common.Pair;
import jtlc.main.common.Settings;
import jtlc.main.common.Triplet;
import jtlc.core.storage.ImageStore;
import jtlc.view.dto.AbstractDTO;
import jtlc.view.components.slider.CustomSliderUI;
import jtlc.view.components.slider.CustomSlider;
import jtlc.view.components.Plotter;
import jtlc.view.dialogs.ImageExportDialog;
import jtlc.view.dialogs.dto.ImageExportDTO;
import jtlc.view.panels.dto.ResultsDTO;

/**
 * Samples Analysis Results Panel
 * Implements JTabbedPane with a tab per sample, where each tab
 * show a plot components with sample mean, peaks, maximuns and other
 * information; show's post analysis results
 * 
 * @author Cristian Tardivo
 */
public class ResultsPanel extends JTabbedPane implements IPanel {
    private final ResultsDTO data;
    // Tabs Components
    private final List<Plotter> plotPanels;
    private final List<CustomSlider<Character>> horizontalSliders;
    private final List<JLayeredPane> tabs;
    private final List<Integer> samples;
    private final List<WebTextArea> comments;
    private final List<WebToolBar> toolbars;
    private final List<GroupPanel> peakResults;

    /**
     * Create AnalysisPanel
     * @param dto AnalysisPanel Data-Transfer-Object
     * @param size Initial Panel Size
     */
    public ResultsPanel(ResultsDTO dto, Dimension size) {
        // Save Params
        data = dto;
        samples = data.getSamplesIds();
        // Init List
        plotPanels = new ArrayList<>(samples.size());
        horizontalSliders = new ArrayList<>(samples.size());
        tabs = new ArrayList<>(samples.size());
        comments = new ArrayList<>(samples.size());
        toolbars = new ArrayList<>(samples.size());
        peakResults = new ArrayList<>(samples.size());
        // Configure Tab Panel
        WebTabbedPaneUI panelUI = new WebTabbedPaneUI();
        panelUI.setTabbedPaneStyle(TabbedPaneStyle.attached);
        this.setUI(panelUI);
        this.setBorder(BorderFactory.createEmptyBorder(1, 0, 1, 0));
        super.setSize(size);
        // Create Tabs
        for (Integer sample: samples) {
            // Main Data
            List<Pair<Float,Float>> function = data.getSampleMean(sample);
            List<Pair<Float,Float>> positions = data.getSamplePeaks(sample);
            List<Pair<Float,Float>> baseline = data.getSampleBaseline(sample);
            List<Integer> samplePeaksIds = data.getSamplePeaksIds(sample);
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
                System.err.println("Invalid Thumbs Positions for sample " + data.getSampleName(sample));
            }
            // Plot Size
            int plotWidth = size.width - 300;
            int plotHeight = size.height - 40;
            // Create plot panel
            List<Triplet<Float, Float, Integer>> peaksData = samplePeaksIds.stream().map(peak -> new Triplet<Float,Float,Integer>(data.getPeakMaximum(sample, peak), data.getPeakPosition(sample, peak))).collect(Collectors.toList());
            Plotter plotPanel = new Plotter(function, baseline, data.getSampleName(sample), peaksData);
            plotPanel.setSize(plotWidth, plotHeight);
            plotPanel.setPaintBackground(false);
            plotPanel.setAxisLabels(Assets.getString("IMAGE_MEAN"), Assets.getString("TIME"));
            Assets.associateComponent(plotPanel, "setTexts", "TIME", "IMAGE_MEAN");
            // Init Horizontal MultiThumbSlider
            Character chrs[] = new Character[fpositions.length];
            Arrays.fill(chrs, 'A');
            CustomSlider<Character> sliderPlot = new CustomSlider<>(CustomSlider.HORIZONTAL, fpositions, chrs);
            sliderPlot.setThumbShape(CustomSliderUI.Thumb.Triangle);
            // Init Default Slider Options
            sliderPlot.setSelectedThumb(-1);
            sliderPlot.setThumbOverlap(true);
            sliderPlot.setThumbRemovalAllowed(true);
            sliderPlot.setMouseThumbRemovalAllowed(false);
            sliderPlot.setAutoAdding(true);
            sliderPlot.setPaintTicks(true);
            sliderPlot.requestFocus();
            sliderPlot.setVisible(true);
            sliderPlot.setPaintSquares(false);
            sliderPlot.setPaintNumbers(true);
            sliderPlot.setCollisionPolicy(CustomSlider.Collision.NUDGE_OTHER);
            sliderPlot.setMinimumThumbnailCount(2);
            sliderPlot.setTickThumbMove(true);
            sliderPlot.setLinkedStatus(true);
            sliderPlot.setEnabled(false);
            // Multiplier Value
            sliderPlot.setMultValue(1);
            // Slider Size
            sliderPlot.setSize(plotPanel.getWidth() - 30, plotPanel.getHeight() + 20);
            sliderPlot.setTickLength(plotPanel.getHeight() - plotPanel.BOTTOM_MARGIN - plotPanel.TOP_MARGIN + 1);
            sliderPlot.setTickStart(-3);
            sliderPlot.setSquareLength(plotPanel.getHeight() - plotPanel.BOTTOM_MARGIN - plotPanel.TOP_MARGIN);
            sliderPlot.setSquareStart(-1);            
            // Title icon
            ImageIcon icon = Assets.loadIcon("ic_reports", 18);
            // Simple pane
            WebAccordion accordion = new WebAccordion(WebAccordionStyle.accordionStyle);
            accordion.setMultiplySelectionAllowed(false);
            accordion.setPaintFocus(false);
            Insets insetsLeft = new Insets(2, 10, 2, 3);
            Insets insetsRigth = new Insets(2, 3, 2, 10);
            List<WebLabel> peaksNameLabels = new LinkedList<>();
            // Peaks data per sample
            for (int peak: samplePeaksIds) {
                // Peak Data
                String peakName = data.getPeakName(sample, peak);
                int peakPosition = data.getPeakPosition(sample, peak);
                Pair<Float, Float> peaksLimits = data.getPeaksLimits(sample, peak);
                Pair<Float, Float> peakHeight = data.getPeakHeight(sample, peak);
                Pair<Float, Float> peakMaximum = data.getPeakMaximum(sample, peak);
                Float peakRelativeSurface = data.getPeakRelativeSurface(sample, peak);
                Float peakSurface = data.getPeakSurface(sample, peak);
                List<Pair<Float,Float>> peakBaseline = data.getPeakBaseline(sample, peak);                
                // Content Panel
                WebPanel dataPanel = new WebPanel(false);
                dataPanel.setPaintFocus(false);
                dataPanel.setSize(260, plotHeight - 35);
                dataPanel.setPreferredSize(260, plotHeight - 35);
                dataPanel.setBackground(Color.WHITE);
                // Panel layout
                dataPanel.setLayout(new GridBagLayout());
                GridBagConstraints cns = new GridBagConstraints();
                cns.fill = GridBagConstraints.HORIZONTAL;
                // Components
                WebLabel nameLabel = new WebLabel(Assets.getString("NAME"), WebLabel.RIGHT);
                Assets.associateComponent(nameLabel, "setText", "NAME");
                WebLabel peakNameLabel = new WebLabel(peakName, WebLabel.LEFT);
                peakNameLabel.setMaximumWidth(260 / 2);
                peaksNameLabels.add(peakNameLabel);
                WebLabel limitsLabel = new WebLabel(Assets.getString("LIMITS"), WebLabel.RIGHT);
                Assets.associateComponent(limitsLabel, "setText", "LIMITS");
                String start = String.valueOf(Math.round(peaksLimits.getFirst() * 1000.0) / 1000.0);
                String end = String.valueOf(Math.round(peaksLimits.getSecond() * 1000.0) / 1000.0);
                WebLabel peakLimitsLabel = new WebLabel(Assets.getString("FROM_TO", start, end), WebLabel.LEFT);
                Assets.associateComponentAndParams(peakLimitsLabel, "setText", "FROM_TO", start, end);
                WebLabel maximumLabel = new WebLabel(Assets.getString("MAXIMUM"), WebLabel.RIGHT);
                Assets.associateComponent(maximumLabel, "setText", "MAXIMUM");
                String posM = String.valueOf(Math.round(peakMaximum.getFirst() * 1000.0) / 1000.0);
                String valueM = String.valueOf(Math.round(peakMaximum.getSecond() * 1000.0) / 1000.0);
                WebLabel peakMaximumLabel = new WebLabel(Assets.getString("X_AT_Y", valueM, posM), WebLabel.LEFT);
                Assets.associateComponentAndParams(peakMaximumLabel, "setText", "X_AT_Y", valueM, posM);
                WebLabel heightLabel = new WebLabel(Assets.getString("HEIGHT"), WebLabel.RIGHT);
                Assets.associateComponent(heightLabel, "setText", "HEIGHT");
                String posH = String.valueOf(Math.round(peakHeight.getFirst() * 1000.0) / 1000.0);
                String valueH = String.valueOf(Math.round(peakHeight.getSecond() * 1000.0) / 1000.0);
                WebLabel peakHeightLabel = new WebLabel(Assets.getString("X_AT_Y", valueH, posH), WebLabel.LEFT);
                Assets.associateComponentAndParams(peakHeightLabel, "setText", "X_AT_Y", valueH, posH);
                WebLabel surfaceLabel = new WebLabel(Assets.getString("SURFACE"), WebLabel.RIGHT);
                Assets.associateComponent(surfaceLabel, "setText", "SURFACE");
                WebLabel peakSurfaceLabel = new WebLabel(String.valueOf(Math.round(peakSurface * 1000.0) / 1000.0), WebLabel.LEFT);
                WebLabel relativeLabel = new WebLabel(Assets.getString("RELATIVE"), WebLabel.RIGHT);
                Assets.associateComponent(relativeLabel, "setText", "RELATIVE");
                WebLabel peakRelativeLabel = new WebLabel(String.valueOf(Math.round(peakRelativeSurface * 1000.0) / 1000.0) + "%", WebLabel.LEFT);
                WebLabel infoLabel = new WebLabel(Assets.getString("ROUNDED_TO_3_DECIMALS"));
                Assets.associateComponent(infoLabel, "setText", "ROUNDED_TO_3_DECIMALS");
                // Peak baseline, single file if have only 1 point, double file if have more
                WebLabel baselineLabel = new WebLabel(Assets.getString("BASELINE"), WebLabel.RIGHT);
                Assets.associateComponent(baselineLabel, "setText", "BASELINE");
                WebLabel peakBaselineLabel;
                boolean largeBaseline = (peakBaseline.size() > 1);
                if (!largeBaseline) {
                    String startB = String.valueOf(Math.round(peakBaseline.get(0).getFirst() * 1000.0) / 1000.0);
                    String endB = String.valueOf(Math.round(peakBaseline.get(0).getSecond() * 1000.0) / 1000.0);
                    peakBaselineLabel = new WebLabel(Assets.getString("FROM_TO", startB, endB), WebLabel.LEFT);
                    Assets.associateComponentAndParams(peakBaselineLabel, "setText", "FROM_TO", startB, endB);
                } else {
                    String points = peakBaseline.stream().map(p -> {
                        String startB = String.valueOf(Math.round(p.getFirst() * 1000.0) / 1000.0);
                        String endB = String.valueOf(Math.round(p.getSecond() * 1000.0) / 1000.0);
                        return Assets.getString("FROM_TO", startB, endB);
                    }).collect(Collectors.joining("<br>"));
                    peakBaselineLabel = new WebLabel("<html>" + points + "</html>", WebLabel.LEFT);
                }
                //
                cns.gridx = 0; cns.gridy = 0; cns.insets = insetsLeft; cns.gridheight = 1; cns.weighty = 1; cns.anchor = GridBagConstraints.NORTH;
                dataPanel.add(nameLabel, cns);
                cns.gridx = 1; cns.gridy = 0; cns.insets = insetsRigth;
                dataPanel.add(peakNameLabel, cns);
                //
                cns.gridx = 0; cns.gridy = 1; cns.insets = insetsLeft;
                dataPanel.add(limitsLabel, cns);
                cns.gridx = 1; cns.gridy = 1; cns.insets = insetsRigth;
                dataPanel.add(peakLimitsLabel, cns);
                //
                cns.gridx = 0; cns.gridy = 2; cns.insets = insetsLeft;
                dataPanel.add(maximumLabel, cns);
                cns.gridx = 1; cns.gridy = 2; cns.insets = insetsRigth;
                dataPanel.add(peakMaximumLabel, cns);
                //
                cns.gridx = 0; cns.gridy = 3; cns.insets = insetsLeft;
                dataPanel.add(heightLabel, cns);
                cns.gridx = 1; cns.gridy = 3; cns.insets = insetsRigth;
                dataPanel.add(peakHeightLabel, cns);
                //
                cns.gridx = 0; cns.gridy = 4; cns.insets = insetsLeft;
                dataPanel.add(surfaceLabel, cns);
                cns.gridx = 1; cns.gridy = 4; cns.insets = insetsRigth;
                dataPanel.add(peakSurfaceLabel, cns);
                //
                cns.gridx = 0; cns.gridy = 5; cns.insets = insetsLeft;
                dataPanel.add(relativeLabel, cns);
                cns.gridx = 1; cns.gridy = 5; cns.insets = insetsRigth;
                dataPanel.add(peakRelativeLabel, cns);
                //
                cns.gridx = 0; cns.gridy = 6; cns.insets = insetsLeft;
                dataPanel.add(baselineLabel, cns);
                cns.gridx = 1; cns.gridy = 6; cns.insets = insetsRigth; cns.weighty = 20;
                dataPanel.add(peakBaselineLabel, cns);
                //
                cns.gridx = 0; cns.gridy = largeBaseline? 8 : 6; cns.insets = insetsRigth; cns.gridwidth = 2; cns.anchor = GridBagConstraints.SOUTH;
                dataPanel.add(infoLabel, cns);
                // Add panel
                accordion.addPane(icon, Assets.getString("PEAK_NUMBER", peakPosition), dataPanel);
            }
            GroupPanel peakResult = new GroupPanel(4, accordion);
            peakResult.setSize(270, plotHeight - 35);
            peakResult.setPaintFocus(false);
            // Change peak name Button
            WebButton changePeekName = new WebButton(Assets.loadIcon("ic_text_edit"));
            WebCustomTooltip peekNameTooltip = TooltipManager.setTooltip(changePeekName, Assets.getString("CHANGE_SELECTED_PEAK_NAME"), TooltipWay.up, 100);
            Assets.associateComponent(peekNameTooltip, "setTooltip", "CHANGE_SELECTED_PEAK_NAME");
            changePeekName.addActionListener((ActionEvent e) -> {
                if (accordion.getSelectionCount() > 0) {
                    Integer selectedPeak = accordion.getSelectedIndices().get(0);                    
                    Object name = WebOptionPane.showInputDialog(this, Assets.getString("PEAK_NAME"), Assets.getString("EDIT_PEAK_NAME"), JOptionPane.QUESTION_MESSAGE, null, null, data.getPeakName(sample, selectedPeak));
                    if (name != null) {
                        String txt = ((String)name).trim();
                        if (txt.length() > 0) {
                            // Update Peak Name
                            int peak = samplePeaksIds.get(selectedPeak);
                            data.setPeakName(sample, peak, txt);
                            data.setChanged(sample, peak, true);
                            data.setChanged(true);
                            // Update peak name Label
                            peaksNameLabels.get(selectedPeak).setText(txt);
                        } else {
                            WebOptionPane.showMessageDialog(this, Assets.getString("INVALID_PEAK_NAME"), Assets.getString("ERROR"), WebOptionPane.ERROR_MESSAGE);
                        }
                    }
                    
                }
            });
            changePeekName.setSize(30, 30);            
            // Comments Pop-up
            WebButton showComments = new WebButton(Assets.loadIcon("ic_dialog"));
            WebCustomTooltip commentsTooltip = TooltipManager.setTooltip(showComments, Assets.getString("SHOW_RESULTS_COMMENTS"), TooltipWay.up, 100);
            Assets.associateComponent(commentsTooltip, "setTooltip", "SHOW_RESULTS_COMMENTS");
            WebTextArea textArea = new WebTextArea();
            textArea.setText(data.getResultsComments(sample));
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            WebScrollPane areaScroll = new WebScrollPane(textArea);
            areaScroll.setPreferredSize(new Dimension(220, 140));
            WebButtonPopup commentPopup = new WebButtonPopup(showComments, PopupWay.upLeft);
            WebLabel comentLabel = new WebLabel(Assets.getString("COMMENTS"), WebLabel.CENTER);
            Assets.associateComponent(comentLabel, "setText", "COMMENTS");
            GroupPanel commentContent = new GroupPanel(10, false, comentLabel, areaScroll);
            commentContent.setMargin(10);
            commentPopup.setContent(commentContent);
            commentPopup.setDefaultFocusComponent(areaScroll);
            showComments.setSize(30, 30);
            // Help Button
            WebButton showHelp = new WebButton(Assets.loadIcon("ic_help"));
            WebCustomTooltip helpTooltip = TooltipManager.setTooltip(showHelp, Assets.getString("SHOW_HELP"), TooltipWay.up, 100);
            Assets.associateComponent(helpTooltip, "setTooltip", "SHOW_HELP");
            WebEditorPane editorPane = new WebEditorPane("text/html",Assets.getString("RESULTS_HELP"));
            Assets.associateComponent(editorPane, "setText", "RESULTS_HELP");
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
            showHelp.setSize(30, 30);
            // Export image button and action
            WebButton exportImage = new WebButton(Assets.loadIcon("ic_export_image"));
            WebCustomTooltip exportTooltip = TooltipManager.setTooltip(exportImage, Assets.getString("EXPORT_TO_IMAGE"), TooltipWay.up, 100);
            Assets.associateComponent(exportTooltip, "setTooltip", "EXPORT_TO_IMAGE");
            ResultsPanel aPanel = this;
            exportImage.addActionListener((ActionEvent e) -> {
                ImagePlus compExport = plotPanel.getImagePlus(600, 400);
                ImageExportDialog dialog = new ImageExportDialog(getOwningFrame(aPanel), new ImageExportDTO(compExport));
                ImageExportDTO results = dialog.getResults();
                if (results.hasChanged()) {
                    WebFileChooser fc = new WebFileChooser(Settings.getWorkSpace());
                    fc.setFileFilter(new FileNameExtensionFilter("JPG", "jpg"));
                    fc.setSelectedFile(data.getSampleName(sample) + "-peaks-plot.jpg");
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
            exportImage.setSize(30, 30);
            // Commands Toolbar
            WebToolBar commandsTB = new WebToolBar(WebToolBar.HORIZONTAL);
            commandsTB.setFloatable(false);
            commandsTB.setToolbarStyle(ToolbarStyle.standalone);
            commandsTB.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            commandsTB.add(showHelp);
            commandsTB.add(exportImage);
            commandsTB.add(showComments);
            commandsTB.add(changePeekName);
            commandsTB.setSize(150, 39);
            // Locate Components
            plotPanel.setLocation(0, 20);
            sliderPlot.setLocation(plotPanel.LEFT_MARGIN - 22, 7);
            peakResult.setLocation(plotPanel.getWidth() + 10, 10);
            commandsTB.setLocation(size.width - commandsTB.getWidth() - 10, size.height - commandsTB.getSize().height - 23);
            /**
             * Add Componetes to panel
             */
            JLayeredPane tab = new JLayeredPane();
            tab.setSize(size.width, size.height - 20);
            tab.add(plotPanel, JLayeredPane.FRAME_CONTENT_LAYER);
            tab.add(sliderPlot, JLayeredPane.DEFAULT_LAYER);
            tab.add(peakResult, JLayeredPane.DEFAULT_LAYER);
            tab.add(commandsTB, DRAG_LAYER);
            /**
             * Save Componets
             */
            plotPanels.add(plotPanel);
            horizontalSliders.add(sliderPlot);
            comments.add(textArea);
            toolbars.add(commandsTB);
            tabs.add(tab);
            peakResults.add(peakResult);
            /**
             * Add Tab
             */
            this.addTab(Assets.shortString(data.getSampleName(sample), 25, true), tab);
        }
    }
    
    /**
     * Resize Panel Components and change location
     * @param size new panel size
     */
    @Override
    public void setSize(Dimension size) {
        for (int i = 0; i < samples.size(); i++) {
            // Plotter panel
            Plotter plotPanel = plotPanels.get(i);
            // Plot Size
            int plotWidth = size.width - 300;
            int plotHeight = size.height - 40;
            plotPanel.setSize(plotWidth, plotHeight);
            // Slider Horizontal Size
            CustomSlider<Character> sliderH = horizontalSliders.get(i);
            sliderH.setSize(plotPanel.getWidth() - 30, plotPanel.getHeight() + 20);
            sliderH.setTickLength(plotPanel.getHeight() - plotPanel.BOTTOM_MARGIN - plotPanel.TOP_MARGIN + 1);
            sliderH.setSquareLength(plotPanel.getHeight() - plotPanel.BOTTOM_MARGIN - plotPanel.TOP_MARGIN);
            // Update Tab Size
            JLayeredPane tab = tabs.get(i);
            tab.setSize(size.width, size.height - 20);
            // Commands toolbar
            WebToolBar commandsTB = toolbars.get(i);
            // Peak Result
            GroupPanel peakResult = peakResults.get(i);
            peakResult.setSize(270, plotHeight - 35);
            // Locate Components
            plotPanel.setLocation(0, 20);
            sliderH.setLocation(plotPanel.LEFT_MARGIN - 22, 7);
            peakResult.setLocation(plotPanel.getWidth() + 10, 10);
            commandsTB.setLocation(size.width - commandsTB.getWidth() - 10, size.height - commandsTB.getSize().height - 23);
        }
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
     * @return ResultsPanel Data-Transfer-Object
     */
    @Override
    public ResultsDTO getResults() {
        int i = 0;
        for (int sample: data.getSamplesIds()) {
            String text = comments.get(i).getText().trim();
            String origin = data.getResultsComments(sample);
            if ((origin != null && !origin.equals(text)) || (origin == null && !text.isEmpty())) {
                data.setResultsComments(sample, text);
                data.setChanged(sample, true);
                data.setChanged(true);
            }
            i++;
        }
        // Return data
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