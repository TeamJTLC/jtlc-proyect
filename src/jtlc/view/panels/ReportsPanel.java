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
import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;
import com.alee.laf.tabbedpane.TabbedPaneStyle;
import com.alee.laf.tabbedpane.WebTabbedPaneUI;
import ij.ImagePlus;
import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.border.LineBorder;
import jtlc.assets.Assets;
import jtlc.main.common.Pair;
import jtlc.main.common.Triplet;
import jtlc.core.processing.ImageProcessing;
import jtlc.view.components.Plotter;
import jtlc.view.dto.AbstractDTO;
import jtlc.view.panels.dto.ReportsDTO;

/**
 * Project Reports Panel
 * 
 * @author Cristian Tardivo
 */
public class ReportsPanel extends JTabbedPane implements IPanel {
    private final ReportsDTO data;

    /**
     * Create AnalysisPanel
     * @param dto AnalysisPanel Data-Transfer-Object
     * @param size Initial Panel Size
     */
    public ReportsPanel(ReportsDTO dto, Dimension size) {
        data = dto;
        // Configure Tab Panel
        WebTabbedPaneUI panelUI = new WebTabbedPaneUI();
        panelUI.setTabbedPaneStyle(TabbedPaneStyle.attached);
        this.setUI(panelUI);
        this.setBorder(BorderFactory.createEmptyBorder(1, 0, 1, 0));
        super.setSize(size);
        /**
         * Create Tabs
         */
        // Experiment report tab
        this.add(Assets.getString("PARAM_EXPERIMENT", Assets.shortString(data.getProjectName(), 13, true)), createExperimentReport());
        // Samples report tabs
        for (int sample: data.getSamplesIds())
            this.add(Assets.getString("PARAM_SAMPLE", Assets.shortString(data.getSampleName(sample), 13, true)), createSampleReport(sample));
        
        /**
         * Add Componetes to panel
         */
        JLayeredPane tab = new JLayeredPane();
        tab.setSize(size.width, size.height - 20);
    }
    
    /**
     * Create Experiment Report Panel
     * @return Scrollable panel with experiment report data 
     */
    private WebScrollPane createExperimentReport() {
        // Data container panel
        WebPanel panel = new WebPanel(false);
        panel.setLayout(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setPreferredSize(700, 1520);
        // Data scrollable panel
        WebScrollPane scrollPanel = new WebScrollPane(panel, false, true);
        scrollPanel.setPreferredSize(new Dimension(0, 0));
        scrollPanel.setBorder(new LineBorder(Color.GRAY, 1));
        /**
         * Components, labels and images
         */
        final DateFormat df = new SimpleDateFormat(Assets.getString("DATE_FORMAT"));
        // Experiment name
        WebLabel name = new WebLabel(Assets.getString("PARAM_EXPERIMENT", data.getProjectName()), WebLabel.CENTER); name.setBoldFont(); name.setMaximumWidth(600);
        Assets.associateComponentAndParams(name, "setText", "PARAM_EXPERIMENT", data.getProjectName());
        // Description
        String descriptionText = isEmpty(data.getDescription())? Assets.getString("NO_COMMENTS"): data.getDescription();
        WebLabel description = new WebLabel(Assets.getString("PARAM_DESCRIPTION", descriptionText), WebLabel.LEFT); description.setMaximumWidth(600);
        Assets.associateComponentAndParams(description, "setText", "PARAM_COMMENTS", descriptionText);
        // Creation date
        String dateText = df.format(data.getSampleDate());
        WebLabel creationDate = new WebLabel(Assets.getString("PARAM_SAMPLE_DATE", dateText), WebLabel.LEFT);
        Assets.associateComponentAndParams(creationDate, "setText", "PARAM_SAMPLE_DATE", dateText);
        // Source Image text
        WebLabel sourceImage = new WebLabel(Assets.getString("SOURCE_IMAGES"), WebLabel.CENTER); sourceImage.setItalicFont();
        Assets.associateComponentAndParams(sourceImage, "setText", "SOURCE_IMAGES");
        // Source image comments
        String sourceCommentsText = isEmpty(data.getSourceImageComments())? Assets.getString("NO_COMMENTS"): data.getSourceImageComments();
        WebLabel sourceComments = new WebLabel(Assets.getString("PARAM_SOURCE_COMMENTS", sourceCommentsText), WebLabel.LEFT); sourceComments.setMaximumWidth(600);
        Assets.associateComponentAndParams(sourceComments, "setText", "PARAM_SOURCE_COMMENTS", sourceCommentsText);
        // Analysis text
        WebLabel analysis = new WebLabel(Assets.getString("ANALYSIS"), WebLabel.CENTER); analysis.setBoldFont();
        Assets.associateComponentAndParams(analysis, "setText", "ANALYSIS");
        // Analysis date
        String aDateText = df.format(data.getAnalysisDate());
        WebLabel analysisDate = new WebLabel(Assets.getString("PARAM_ANALYSIS_DATE", aDateText), WebLabel.LEFT);
        Assets.associateComponentAndParams(analysisDate, "setText", "PARAM_ANALYSIS_DATE", aDateText);
        // Processed Image text
        WebLabel processedImage = new WebLabel(Assets.getString("PROCESSED_IMAGES"), WebLabel.CENTER); processedImage.setItalicFont();
        Assets.associateComponentAndParams(processedImage, "setText", "PROCESSED_IMAGES");
        // Cut Image text
        WebLabel cutImage = new WebLabel(Assets.getString("CUT_IMAGE"), WebLabel.LEFT); cutImage.setItalicFont();
        Assets.associateComponentAndParams(cutImage, "setText", "CUT_IMAGE");
        // Cut comments
        String cutCommentsText = isEmpty(data.getCutComments())? Assets.getString("NO_COMMENTS"): data.getCutComments();
        WebLabel cutComments = new WebLabel(Assets.getString("PARAM_COMMENTS", cutCommentsText), WebLabel.LEFT); cutComments.setMaximumWidth(600);
        Assets.associateComponentAndParams(cutComments, "setText", "PARAM_COMMENTS", cutCommentsText);
        // Cut points
        String cutPointsText = Assets.getString("UPPER_LOWER", data.getCutPoints().getFirst(), data.getCutPoints().getSecond());
        WebLabel cutPoints = new WebLabel(Assets.getString("PARAM_CUT_POINT", cutPointsText), WebLabel.LEFT);
        Assets.associateComponentAndParams(cutPoints, "setText", "PARAM_CUT_POINT", cutPointsText);
        // Rotate Image text
        WebLabel rotateImage = new WebLabel(Assets.getString("ROTATE_IMAGE"), WebLabel.LEFT); rotateImage.setItalicFont();
        Assets.associateComponentAndParams(rotateImage, "setText", "ROTATE_IMAGE");
        // Rotate comments
        String rotateCommentsText = isEmpty(data.getRotationComments())? Assets.getString("NO_COMMENTS"): data.getRotationComments();
        WebLabel rotateComments = new WebLabel(Assets.getString("PARAM_COMMENTS", rotateCommentsText), WebLabel.LEFT); rotateComments.setMaximumWidth(600);
        Assets.associateComponentAndParams(rotateComments, "setText", "PARAM_COMMENTS", rotateCommentsText);
        // Rotation angle
        WebLabel rotationAngle = new WebLabel(Assets.getString("PARAM_ROTATION_ANGLE", data.getRotationAngle()), WebLabel.LEFT);
        Assets.associateComponentAndParams(rotationAngle, "setText", "PARAM_ROTATION_ANGLE", data.getRotationAngle());
        // Flip Axis
        WebLabel flipAxis = new WebLabel(Assets.getString("PARAM_FLIP_AXIS", data.getFlipAxis()), WebLabel.LEFT);
        Assets.associateComponentAndParams(flipAxis, "setText", "PARAM_FLIP_AXIS", data.getFlipAxis());
        // Split Samples
        WebLabel splitSamples = new WebLabel(Assets.getString("SPLIT_SAMPLES"), WebLabel.LEFT);
        Assets.associateComponentAndParams(splitSamples, "setText", "SPLIT_SAMPLES");
        // Split comments
        String splitCommentsText = isEmpty(data.getSplitComments())? Assets.getString("NO_COMMENTS"): data.getSplitComments();
        WebLabel splitComments = new WebLabel(Assets.getString("PARAM_COMMENTS", splitCommentsText), WebLabel.LEFT); splitComments.setMaximumWidth(600);
        Assets.associateComponentAndParams(splitComments, "setText", "PARAM_COMMENTS", splitCommentsText);
        // Split points
        WebLabel splitPoints = new WebLabel(Assets.getString("PARAM_POINTS", data.getSplitPoints()), WebLabel.LEFT); splitPoints.setMaximumWidth(600);
        Assets.associateComponentAndParams(splitPoints, "setText", "PARAM_POINTS", data.getSplitPoints());
        // Samples data
        WebLabel samplesData = new WebLabel(Assets.getString("SAMPLES_DATA"), WebLabel.LEFT); samplesData.setItalicFont();
        Assets.associateComponentAndParams(samplesData, "setText", "SAMPLES_DATA");
        // Data comments
        String dataCommentsText = isEmpty(data.getDataComments())? Assets.getString("NO_COMMENTS"): data.getDataComments();
        WebLabel dataComments = new WebLabel(Assets.getString("PARAM_COMMENTS", dataCommentsText), WebLabel.LEFT); dataComments.setMaximumWidth(600);
        Assets.associateComponentAndParams(dataComments, "setText", "PARAM_COMMENTS", dataCommentsText);
        
        // Define max height for sample image
        int maxHeight = 500;
        // Sample source image
        ImagePlus experimentSourceImage = data.getSourceImage();
        if (experimentSourceImage.getHeight() > maxHeight)
            ImageProcessing.resizeImage(experimentSourceImage, -1, maxHeight);
        WebDecoratedImage sourceImageIcon = new WebDecoratedImage(experimentSourceImage.getImage());
        sourceImageIcon.setRound(0);
        sourceImageIcon.setDrawGlassLayer(false);
        sourceImageIcon.setDrawBorder(false);
        sourceImageIcon.setShadeWidth(2);
        // Sample processed image
        ImagePlus experimentProcessedImage = data.getProcessedImage();
        if (experimentProcessedImage.getHeight() > maxHeight)
            ImageProcessing.resizeImage(experimentProcessedImage, -1, maxHeight);
        WebDecoratedImage processedImageIcon = new WebDecoratedImage(experimentProcessedImage.getImage());
        processedImageIcon.setRound(0);
        processedImageIcon.setDrawGlassLayer(false);
        processedImageIcon.setDrawBorder(false);
        processedImageIcon.setShadeWidth(2);
        // Layout constraints and insets
        GridBagConstraints cns = new GridBagConstraints();
        Insets insetsLeft = new Insets(1, 5, 1, 0);
        Insets insetsTab = new Insets(1, 10, 1, 0);
        Insets insetsTop = new Insets(10, 0, 0, 0);
        Insets insetsBoth = new Insets(10, 0, 10, 0);
        Insets insetLeftUp = new Insets(10, 5, 1, 0);
        /**
         * Add Experiment data
         */
        // Init constrains
        cns.anchor = GridBagConstraints.NORTH;
        cns.fill = GridBagConstraints.HORIZONTAL;
        // Name
        cns.gridx = 0; cns.gridy = 0; cns.gridwidth = 2; cns.insets = insetsBoth;
        panel.add(name, cns);
        // Comments
        cns.gridx = 0; cns.gridy = 1; cns.insets = insetsLeft; cns.gridwidth = 2;
        panel.add(description, cns);
        // Creation date
        cns.gridx = 0; cns.gridy = 2; cns.gridwidth = 1;
        panel.add(creationDate, cns);
        // Source Image Lbl
        cns.gridx = 0; cns.gridy = 3; cns.insets = insetsTop; cns.gridwidth = 2;
        panel.add(sourceImage, cns);
        // Source Image Icon
        cns.gridx = 0; cns.gridy = 4; cns.fill = GridBagConstraints.BOTH; cns.insets = insetsBoth;
        panel.add(sourceImageIcon, cns);
        // Source image comments
        cns.gridx = 0; cns.gridy = 5; cns.fill = GridBagConstraints.HORIZONTAL; cns.insets = insetsLeft;
        panel.add(sourceComments, cns);
        // Analysis
        cns.gridx = 0; cns.gridy = 6; cns.insets = insetsBoth;
        panel.add(analysis, cns);
        // Analysis
        cns.gridx = 0; cns.gridy = 7; cns.gridwidth = 1; cns.insets = insetsLeft;
        panel.add(analysisDate, cns);
        // Processed Image Lbl
        cns.gridx = 0; cns.gridy = 8; cns.gridwidth = 2; cns.insets = insetsTop;
        panel.add(processedImage, cns);
        // Processed Image Icon
        cns.gridx = 0; cns.gridy = 9; cns.fill = GridBagConstraints.BOTH; cns.insets = insetsBoth;
        panel.add(processedImageIcon, cns);
        // Cut image
        cns.gridx = 0; cns.gridy = 10; cns.gridwidth = 1; cns.fill = GridBagConstraints.HORIZONTAL; cns.insets = insetLeftUp;
        panel.add(cutImage, cns);
        // Cut image commetns
        cns.gridx = 0; cns.gridy = 11; cns.insets = insetsTab; cns.gridwidth = 2;
        panel.add(cutComments, cns);
        // Cut image points
        cns.gridx = 0; cns.gridy = 12;
        panel.add(cutPoints, cns);
        // rotate image
        cns.gridx = 0; cns.gridy = 13; cns.insets = insetLeftUp; cns.gridwidth = 1;
        panel.add(rotateImage, cns);
        // rotate image comments
        cns.gridx = 0; cns.gridy = 14; cns.insets = insetsTab; cns.gridwidth = 2;
        panel.add(rotateComments, cns);
        // rotate image angle
        cns.gridx = 0; cns.gridy = 15; cns.gridwidth = 1;
        panel.add(rotationAngle, cns);
        // rotate image flip axis
        cns.gridx = 0; cns.gridy = 16;
        panel.add(flipAxis, cns);
        // split samples
        cns.gridx = 0; cns.gridy = 17; cns.insets = insetLeftUp;
        panel.add(splitSamples, cns);
        // split samples comments
        cns.gridx = 0; cns.gridy = 18; cns.insets = insetsTab; cns.gridwidth = 2;
        panel.add(splitComments, cns);
        // split samples points
        cns.gridx = 0; cns.gridy = 19;
        panel.add(splitPoints, cns);
        // samples data
        cns.gridx = 0; cns.gridy = 20; cns.insets = insetLeftUp; cns.gridwidth = 1;
        panel.add(samplesData, cns);
        // samples data comments
        cns.gridx = 0; cns.gridy = 21; cns.weighty = 2; cns.insets = insetsTab; cns.gridwidth = 2;
        panel.add(dataComments, cns);
                
        // result panel
        return scrollPanel;
    }
    
    /**
     * Create Sample Report Panel
     * @param sampleId sample id to report
     * @return Scrollable panel with sample report data
     */
    private WebScrollPane createSampleReport(int sampleId) {
        // Data container panel
        WebPanel panel = new WebPanel(false);
        panel.setLayout(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setPreferredSize(700, 1200 + data.getSamplePeaksIds(sampleId).size() * 140);
        // Data scrollable panel
        WebScrollPane scrollPanel = new WebScrollPane(panel, false, true);
        scrollPanel.setPreferredSize(new Dimension(0, 0));
        scrollPanel.setBorder(new LineBorder(Color.GRAY, 1));
        /**
         * Components, labels and images
         */
        // Sample name
        WebLabel name = new WebLabel(Assets.getString("PARAM_SAMPLE", data.getSampleName(sampleId)), WebLabel.CENTER); name.setBoldFont(); name.setMaximumWidth(600);
        Assets.associateComponentAndParams(name, "setText", "PARAM_SAMPLE", data.getSampleName(sampleId));
        // Comments
        String commentText = isEmpty(data.getSampleComments(sampleId))? Assets.getString("NO_COMMENTS"): data.getSampleComments(sampleId);
        WebLabel comments = new WebLabel(Assets.getString("PARAM_COMMENTS", commentText), WebLabel.LEFT); comments.setMaximumWidth(600);
        Assets.associateComponentAndParams(comments, "setText", "PARAM_COMMENTS", commentText);
        // Source Image text
        WebLabel sourceImage = new WebLabel(Assets.getString("SOURCE_IMAGES"), WebLabel.CENTER); sourceImage.setItalicFont();
        Assets.associateComponentAndParams(sourceImage, "setText", "SOURCE_IMAGES");
        // Processed Image text
        WebLabel processedImage = new WebLabel(Assets.getString("PROCESSED_IMAGES"), WebLabel.CENTER); processedImage.setItalicFont();
        Assets.associateComponentAndParams(processedImage, "setText", "PROCESSED_IMAGES");
        // Sample data text
        WebLabel sampleData = new WebLabel(Assets.getString("SAMPLE_DATA"), WebLabel.LEFT); sampleData.setItalicFont();
        Assets.associateComponentAndParams(sampleData, "setText", "SAMPLE_DATA");
        // Sample limits
        WebLabel limits = new WebLabel(Assets.getString("PARAM_LIMITS", data.getSampleLimits(sampleId)), WebLabel.LEFT);
        Assets.associateComponentAndParams(limits, "setText", "PARAM_LIMITS", data.getSampleLimits(sampleId));
        // Sample seed point
        WebLabel seed = new WebLabel(Assets.getString("PARAM_SEED_POINT", data.getSampleSeedPoint(sampleId)), WebLabel.LEFT);
        Assets.associateComponentAndParams(seed, "setText", "PARAM_SEED_POINT", data.getSampleSeedPoint(sampleId));
        // Sample front point
        WebLabel front = new WebLabel(Assets.getString("PARAM_FRONT_POINT", data.getSampleFrontPoint(sampleId)), WebLabel.LEFT);
        Assets.associateComponentAndParams(front, "setText", "PARAM_FRONT_POINT", data.getSampleFrontPoint(sampleId));
        // Mean and Peaks text
        WebLabel meanAndPeaks = new WebLabel(Assets.getString("MEAN_AND_PEAKS"), WebLabel.CENTER); meanAndPeaks.setBoldFont();
        Assets.associateComponentAndParams(meanAndPeaks, "setText", "MEAN_AND_PEAKS");
        // Analysis Results text
        WebLabel results = new WebLabel(Assets.getString("ANALYSIS_RESULTS"), WebLabel.LEFT); results.setItalicFont();
        Assets.associateComponentAndParams(results, "setText", "ANALYSIS_RESULTS");
        // Sample total surface
        WebLabel totalsurface = new WebLabel(Assets.getString("PARAM_ABSOLUTE_SURFACE", data.getSampleTotalSurface(sampleId)), WebLabel.LEFT);
        Assets.associateComponentAndParams(totalsurface, "setText", "PARAM_ABSOLUTE_SURFACE", data.getSampleTotalSurface(sampleId));
        // Sample analysis comments
        String analysisText = isEmpty(data.getSampleAnalysisComments(sampleId))? Assets.getString("NO_COMMENTS"): data.getSampleAnalysisComments(sampleId);
        WebLabel analysiscomments = new WebLabel(Assets.getString("PARAM_ANALYSIS_COMMENTS", analysisText), WebLabel.LEFT); analysiscomments.setMaximumWidth(600);
        Assets.associateComponentAndParams(analysiscomments, "setText", "PARAM_ANALYSIS_COMMENTS", analysisText);
        // Sample results comments
        String resultsText = isEmpty(data.getResultsComments(sampleId))? Assets.getString("NO_COMMENTS"): data.getResultsComments(sampleId);
        WebLabel resultscomments = new WebLabel(Assets.getString("PARAM_RESULTS_COMMENTS", resultsText), WebLabel.LEFT); resultscomments.setMaximumWidth(600);
        Assets.associateComponentAndParams(resultscomments, "setText", "PARAM_RESULTS_COMMENTS", resultsText);
        // Peaks text
        WebLabel peaks = new WebLabel(Assets.getString("PEAKS"), WebLabel.CENTER); peaks.setBoldFont();
        Assets.associateComponentAndParams(peaks, "setText", "PEAKS");

        // Define max height for sample image
        int maxHeight = 500;
        // Sample source image
        ImagePlus sampleSourceImage = data.getSampleSourceImage(sampleId);
        if (sampleSourceImage.getHeight() > maxHeight)
            ImageProcessing.resizeImage(sampleSourceImage, -1, maxHeight);
        WebDecoratedImage sourceImageIcon = new WebDecoratedImage(sampleSourceImage.getImage());
        sourceImageIcon.setRound(0);
        sourceImageIcon.setDrawGlassLayer(false);
        sourceImageIcon.setDrawBorder(false);
        sourceImageIcon.setShadeWidth(2);
        // Sample processed image
        ImagePlus sampleProcessedImage = data.getSampleProcessedImage(sampleId);
        if (sampleProcessedImage.getHeight() > maxHeight)
            ImageProcessing.resizeImage(sampleProcessedImage, -1, maxHeight);
        WebDecoratedImage processedImageIcon = new WebDecoratedImage(sampleProcessedImage.getImage());
        processedImageIcon.setRound(0);
        processedImageIcon.setDrawGlassLayer(false);
        processedImageIcon.setDrawBorder(false);
        processedImageIcon.setShadeWidth(2);
        // Extra sample data
        java.util.List<Integer> samplePeaksIds = data.getSamplePeaksIds(sampleId);
        java.util.List<Triplet<Float, Float, Integer>> peaksData = samplePeaksIds.stream().map(peakId -> new Triplet<Float,Float,Integer>(data.getPeakMaximum(sampleId, peakId), data.getPeakPosition(sampleId, peakId))).collect(Collectors.toList());
        // Sample mean plot (peaks and data)
        ImagePlus peaksAndMeanPlot = new Plotter(data.getSampleMean(sampleId), data.getSampleBaseline(sampleId), data.getSampleName(sampleId), peaksData).getImagePlus(600, 400);
        WebDecoratedImage peaksAndMeanPlotIcon = new WebDecoratedImage(peaksAndMeanPlot.getImage());
        peaksAndMeanPlotIcon.setRound(0);
        peaksAndMeanPlotIcon.setDrawGlassLayer(false);
        peaksAndMeanPlotIcon.setDrawBorder(false);
        peaksAndMeanPlotIcon.setShadeWidth(0);
        // Layout constraints and insets
        GridBagConstraints cns = new GridBagConstraints();
        Insets insetsLeft = new Insets(5, 10, 5, 0);
        Insets insetsTab = new Insets(1, 10, 1, 0);
        Insets insetsTop = new Insets(10, 0, 0, 0);
        Insets insetsCero = new Insets(0, 0, 0, 0);
        /**
         * Add sample data
         */
        // Init constrains
        cns.anchor = GridBagConstraints.NORTH;
        cns.fill = GridBagConstraints.HORIZONTAL;
        // Name
        cns.gridx = 0; cns.gridy = 0; cns.gridwidth = 2; cns.insets = insetsTop;
        panel.add(name, cns);
        // Comments
        cns.gridx = 0; cns.gridy = 1; cns.insets = insetsCero;
        panel.add(comments, cns);
        // Source Image Lbl
        cns.gridx = 0; cns.gridy = 2; cns.gridwidth = 1; cns.insets = insetsLeft;
        panel.add(sourceImage, cns);
        // Processed Image Lbl
        cns.gridx = 1; cns.gridy = 2;
        panel.add(processedImage, cns);
        // Source Image Icon
        cns.gridx = 0; cns.gridy = 3; cns.fill = GridBagConstraints.BOTH;
        panel.add(sourceImageIcon, cns);
        // Processed Image Icon
        cns.gridx = 1; cns.gridy = 3;
        panel.add(processedImageIcon, cns);
        // Sample data Lbl
        cns.gridx = 0; cns.gridy = 4; cns.fill = GridBagConstraints.HORIZONTAL; cns.insets = insetsCero;
        panel.add(sampleData, cns);
        // Limits Lbl
        cns.gridx = 0; cns.gridy = 5; cns.insets = insetsTab;
        panel.add(limits, cns);
        // Seed Lbl
        cns.gridx = 0; cns.gridy = 6;
        panel.add(seed, cns);
        // Front Lbl
        cns.gridx = 0; cns.gridy = 7;
        panel.add(front, cns);
        // Mean Lbl
        cns.gridx = 0; cns.gridy = 8; cns.gridwidth = 2; cns.insets = insetsCero;
        panel.add(meanAndPeaks, cns);
        // Peaks Mean Plot Icon
        cns.gridx = 0; cns.gridy = 9; cns.gridwidth = 2; cns.fill = GridBagConstraints.BOTH;
        panel.add(peaksAndMeanPlotIcon, cns);        
        // Results Lbl
        cns.gridx = 0; cns.gridy = 10; cns.gridwidth = 1; cns.fill = GridBagConstraints.HORIZONTAL;
        panel.add(results, cns);
        // Total Surface Lbl
        cns.gridx = 0; cns.gridy = 11; cns.insets = insetsTab;
        panel.add(totalsurface, cns);
        // Analisis Comments Lbl
        cns.gridx = 0; cns.gridy = 12; cns.gridwidth = 2;
        panel.add(analysiscomments, cns);
        // Results Commnets Lbl
        cns.gridx = 0; cns.gridy = 13; cns.gridwidth = 2;
        panel.add(resultscomments, cns);
        // Peaks
        cns.gridx = 0; cns.gridy = 14; cns.gridwidth = 2; cns.insets = insetsCero;
        panel.add(peaks, cns);
        // Now for each peak create/add data
        List<Integer> peaksId = data.getSamplePeaksIds(sampleId);
        for (int i = 0; i < peaksId.size(); i++) {
            int offset = (i * 8);
            int peakId = peaksId.get(i);
            // Peak Name
            WebLabel peakName = new WebLabel(Assets.getString("PARAM_NAME", data.getPeakName(sampleId, peakId)), WebLabel.LEFT); peakName.setItalicFont(); peakName.setMaximumWidth(600);
            Assets.associateComponentAndParams(peakName, "setText", "PARAM_NAME", data.getPeakName(sampleId, peakId));
            // Peak Position
            WebLabel position = new WebLabel(Assets.getString("PARAM_POSITION", data.getPeakPosition(sampleId, peakId)), WebLabel.LEFT);
            Assets.associateComponentAndParams(position, "setText", "PARAM_POSITION", data.getPeakPosition(sampleId, peakId));
            // Peak Limits
            WebLabel peakLimits = new WebLabel(Assets.getString("PARAM_LIMITS", data.getPeaksLimits(sampleId, peakId)), WebLabel.LEFT);
            Assets.associateComponentAndParams(peakLimits, "setText", "PARAM_LIMITS", data.getPeaksLimits(sampleId, peakId));
            // Peak Maximum
            Pair<Float,Float> peakMaximum = data.getPeakMaximum(sampleId, peakId);
            WebLabel maximum = new WebLabel(Assets.getString("PARAM_MAXIMUM", peakMaximum.getSecond(), peakMaximum.getFirst()), WebLabel.LEFT);
            Assets.associateComponentAndParams(maximum, "setText", "PARAM_MAXIMUM", peakMaximum.getSecond(), peakMaximum.getFirst());
            // Peak Height
            Pair<Float,Float> peakHeight = data.getPeakHeight(sampleId, peakId);
            WebLabel height = new WebLabel(Assets.getString("PARAM_HEIGHT", peakHeight.getSecond(), peakHeight.getFirst()), WebLabel.LEFT);
            Assets.associateComponentAndParams(height, "setText", "PARAM_HEIGHT", peakHeight.getSecond(), peakHeight.getFirst());
            // Peak Total Surface
            WebLabel surface = new WebLabel(Assets.getString("PARAM_ABSOLUTE_SURFACE", data.getPeakSurface(sampleId, peakId)), WebLabel.LEFT);
            Assets.associateComponentAndParams(surface, "setText", "PARAM_ABSOLUTE_SURFACE", data.getPeakSurface(sampleId, peakId));
            // Peak Relative Surface
            WebLabel relative = new WebLabel(Assets.getString("PARAM_RELATIVE_SURFACE", data.getPeakRelativeSurface(sampleId, peakId)), WebLabel.LEFT);
            Assets.associateComponentAndParams(relative, "setText", "PARAM_RELATIVE_SURFACE", data.getPeakRelativeSurface(sampleId, peakId));
            // Peak Baseline
            String baselinePoints = data.getPeakBaseline(sampleId, peakId).stream().map(b -> b.toString()).collect(Collectors.joining(" "));
            WebLabel baseline = new WebLabel(Assets.getString("PARAM_BASELINE", baselinePoints), WebLabel.LEFT); baseline.setMaximumWidth(600);
            Assets.associateComponentAndParams(baseline, "setText", "PARAM_BASELINE", baselinePoints);
            /**
             * Add elements
             */
            // peak name
            cns.gridx = 0; cns.gridy = 15 + offset; cns.gridwidth = 2; cns.insets = insetsTop;
            panel.add(peakName, cns);
            // position
            cns.gridx = 0; cns.gridy = 16 + offset; cns.insets = insetsTab;
            panel.add(position, cns);
            // limits
            cns.gridx = 0; cns.gridy = 17 + offset;
            panel.add(peakLimits, cns);
            // maximum
            cns.gridx = 0; cns.gridy = 18 + offset;
            panel.add(maximum, cns);
            // height
            cns.gridx = 0; cns.gridy = 19 + offset;
            panel.add(height, cns);
            // surface
            cns.gridx = 0; cns.gridy = 20 + offset;
            panel.add(surface, cns);
            // relative
            cns.gridx = 0; cns.gridy = 21 + offset;
            panel.add(relative, cns);
            // baseline
            cns.gridx = 0; cns.gridy = 22 + offset; cns.weighty = (i == peaksId.size() - 1)? 2 : 0;
            panel.add(baseline, cns);
        }
        // result panel
        return scrollPanel;
    }
    
    /**
     * Check if a string is empty (no characters or null)
     * @param string string to check
     * @return true/false
     */
    private boolean isEmpty(String string) {
        return !(string != null && !string.isEmpty() && !string.trim().isEmpty());
    }
    
    /**
     * Resize Panel Components and change location
     * @param size new panel size
     */
    @Override
    public void setSize(Dimension size) {
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
    public ReportsDTO getResults() {
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
}