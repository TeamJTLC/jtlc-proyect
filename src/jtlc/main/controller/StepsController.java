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
package jtlc.main.controller;

import ij.ImagePlus;
import java.util.List;
//
import jtlc.core.processing.ImageProcessing;
import jtlc.core.processing.AnalysisProcessing;
import jtlc.assets.Assets;
import jtlc.main.common.Pair;
import jtlc.main.common.Point;
import jtlc.core.model.Experiment;
import jtlc.core.model.Peak;
import jtlc.core.model.Sample;
import jtlc.core.processing.AnalysisProcessing.Axis;
import jtlc.view.panels.dto.CuttingDTO;
import jtlc.view.panels.dto.DropDTO;
import jtlc.view.panels.dto.GalleryDTO;
import jtlc.view.panels.dto.RotationDTO;
import jtlc.view.panels.dto.SplitDTO;
import jtlc.view.panels.dto.AnalysisDTO;
import jtlc.view.panels.dto.DataDTO;
import jtlc.view.MainView.Panels;
import jtlc.view.panels.dto.ResultsDTO;

/**
 * jTLC main system controller.
 * Controls interactions between models, persistence and views.
 * 
 * @author Baldani Sergio - Tardivo Cristian
 */
public class StepsController extends AbstractController {
        
    /***************************
     *                         *
     * Actions Update Methods. *
     *                         *
     ***************************/
  
    /**
     * Advances to the next step.
     */
    @Action("NEXT_STEP")
    protected void nextStep() {
        // Step is the current step
        switch (step) {
            case EXPLORE_PROJECTS: // Explore Porjects -> Load Image
                    if (processExploreProjects())
                        updateViewStep(Step.LOAD_IMAGE);
                    else
                        view.showWarningMessage(Assets.getString("PROJECT_WARNING"));
                    break;
            case LOAD_IMAGE: // Load Image -> Cut Image
                    if (processImageDrop())
                        updateViewStep(Step.CUT_IMAGE);
                    else
                        view.showWarningMessage(Assets.getString("IMAGE_WARNING"));
                    break;                    
            case CUT_IMAGE: // Cut Image -> Rotate Image
                    processImageCutting();
                    updateViewStep(Step.ROTATE_IMAGE);
                    break;
            case ROTATE_IMAGE: // Rotate Image -> Samples select
                    processImageRotation();
                    updateViewStep(Step.SAMPLES_SELECT);
                    break;
            case SAMPLES_SELECT: // Samples Select -> Special Points
                    if (processSamplesSplit())
                        updateViewStep(Step.SPECIAL_POINTS);
                    else
                        view.showWarningMessage(Assets.getString("SAMPLES_WARNING"));
                    break;
            case SPECIAL_POINTS: // Special Points -> Analize Samples
                    processSpecialPointsSelection();
                    updateViewStep(Step.ANALIZE_SAMPLES);
                    break;
            case ANALIZE_SAMPLES: // Analize Samples -> Analize Results
                    if (processSamplesAnalysis())
                        updateViewStep(Step.SAMPLES_ANALYSIS_RESULTS);
                    break;
            case SAMPLES_ANALYSIS_RESULTS: // Analize Results -> 
                    processSamplesAnalysisResults();
                    updateViewStep(Step.ANALYSIS_REPORTS);
                    break;
            default: // Debug
                System.err.println("Invalid Step at next step call :: " + step);
        }
    }
    
    /**
     * Go backward to the previous step.
     */
    @Action("PREV_STEP")
    protected void previousStep() {
        // Step is the current step
        switch (step) {
            case LOAD_IMAGE: // Load Image -> Explore Projects
                    if (!experiment.isSaved() && !view.showConfirmDialog(Assets.getString("LOSE_UNSAVED_CHANGES")))
                        break;
                    view.removeBreadcrumb(Panels.LOAD_IMAGE);
                    updateViewStep(Step.EXPLORE_PROJECTS);
                    break;
            case CUT_IMAGE: // Cut Image -> Load Image
                    view.removeBreadcrumb(Panels.CUT_IMAGE);
                    updateViewStep(Step.LOAD_IMAGE);
                    break;
            case ROTATE_IMAGE: // Rotate Image -> Cut Image
                    view.removeBreadcrumb(Panels.ROTATE_IMAGE);
                    updateViewStep(Step.CUT_IMAGE);
                    break;
            case SAMPLES_SELECT: // Samples Select -> Rotate Image
                    view.removeBreadcrumb(Panels.SAMPLES_SELECT);
                    updateViewStep(Step.ROTATE_IMAGE);
                    break;
            case SPECIAL_POINTS: // Special Points -> Samples Select
                    view.removeBreadcrumb(Panels.SAMPLES_POINTS);
                    updateViewStep(Step.SAMPLES_SELECT);
                    break;
            case ANALIZE_SAMPLES: // Analize Samples -> Special Points
                    view.removeBreadcrumb(Panels.SAMPLES_ANALYSIS);
                    updateViewStep(Step.SPECIAL_POINTS);
                    break;
            case SAMPLES_ANALYSIS_RESULTS: // Analize Results -> Analize Samples
                    view.removeBreadcrumb(Panels.SAMPLES_ANALYSIS_RESULTS);
                    updateViewStep(Step.ANALIZE_SAMPLES);
                    break;
            case ANALYSIS_REPORTS: // Analize Reports -> Analize Results
                    view.removeBreadcrumb(Panels.ANALYSIS_REPORTS);
                    updateViewStep(Step.SAMPLES_ANALYSIS_RESULTS);
                    break;
            default: // Debug
                System.err.println("Invalid Step at previous step call :: " + step);
        }
    }
   
    /**
     * Reset current step.
     * Only reset view to default values or previous saved values
     */ 
    @Action("RESTART_STEP")
    protected void restartStep() {
        if (view.showConfirmDialog(Assets.getString("RESTART_STEP")))
            resetViewStep();
    }
   
    /**********************
     *                    *
     * Next Step Methods. *
     *                    *
     **********************/

    /**
     * Open selected proyect from proyect explorer gallery.
     * @return true if was selected one proyect.
     */
    private boolean processExploreProjects() {
        GalleryDTO dto = view.getValues();
        Experiment selected = dto.getSelectedExperiment();
        // Clone Experiment
        if (selected != null) {
            experiment = new Experiment(selected);
            return true;
        }
        return false;
    }
    
    /**
     * Retrieves drops panel loaded image.
     * @return true if image was loaded.
     */
    private boolean processImageDrop() {
        // Get Image from drop component
        DropDTO dto = view.getValues();
        ImagePlus img = dto.getImage();
        // Check for valid source image
        if (img == null)
            return false;
        // Check for changes and update experiment
        if (dto.hasChanged()) {
            // Clear Experiment if samples image has changed
            experiment.clear();
            // Save Source image to current experiment
            experiment.setSourceImage(img);
            // Compute Cut points
            experiment.setCutPoints(AnalysisProcessing.searchCutPoints(img));
        }
        // Update comments
        experiment.setSourceImageComments(dto.getComments());
        // Set changed
        experiment.setSaved(!dto.hasChanged());
        // Drop image loaded correctly
        return true;
    }
    
    /**
     * Retrieves cutting panel cuts points and cut experiment image.
     */
    private void processImageCutting() {
        // Get view cut points and cut image
        CuttingDTO dto = view.getValues();
        // Check for changes and update experiment
        if (dto.hasChanged()) {
            experiment.setCutPoints(dto.getUpperPoint(), dto.getLowerPoint());
            // Clear next steps
            experiment.clearProcessedImage();
            experiment.setFlipAxis(Axis.NONE);
            experiment.setRotationAngle(0d);
            experiment.removeAllSamples();
        }
        // Check for changes and update experiment  && Cut experiment source image
        if (dto.hasChanged() || !experiment.hasProcessedImage()) {
            experiment.setProcessedImage(ImageProcessing.cutImage(experiment.getSourceImage(), experiment.getCutPoints()));
        }
        // Update comments
        experiment.setCutComments(dto.getComments());
        // Set changed
        experiment.setSaved(!dto.hasChanged());
    }
    
    /**
     * Retrieves rotation angle, flip axis and rotate/flip experiment image.
     */
    private void processImageRotation() {
        // Get view transforms values
        RotationDTO dto = view.getValues();
        // Check for changes and update experiment
        if (dto.hasChanged()) {
            // Rotation distorsion fix
            ImagePlus img = experiment.getSourceImage();
            Pair<Point,Point> points = experiment.getCutPoints();
            img = ImageProcessing.cutImage(img, points.getFirst(),  points.getSecond());
            // Process experiment processed image
            img = ImageProcessing.flipImage(img, dto.getFlipAxis());
            img = ImageProcessing.rotateImage(img, dto.getRotationAngle(), true);
            // Save processed image
            experiment.setProcessedImage(img);
            experiment.setFlipAxis(dto.getFlipAxis());
            experiment.setRotationAngle(dto.getRotationAngle());
            // clear subsamples and samples split points because image changed
            experiment.removeAllSamples();
        }
        // Compute Samples split points if necessary
        if (dto.hasChanged() || !experiment.hasSamples()) {
            // Pre-Search image samples
            List<Sample> samples = AnalysisProcessing.searchSamples(experiment.getProcessedImage());
            // Save Samples list (only have sample start-end point)
            for (Sample sample: samples)
                experiment.addSample(sample);
        }
        // Update comments
        experiment.setRotationComments(dto.getComments());
        // Set changed
        experiment.setSaved(!dto.hasChanged());
    }
    
    /**
     * Retrieves split points and split experiment image in sub-samples.
     */
    private boolean processSamplesSplit() {
        // Get view samples points
        SplitDTO dto = view.getValues();
        // Check for valid split points
        if (dto.getSamplesPoints().isEmpty())
            return false;
        // Check for changes and update experiment
        if (dto.hasChanged()) {
            experiment.removeAllSamples();
            for (Point point: dto.getSamplesPoints())
                experiment.addSample(new Sample(point));
        }
        // Experiment processed image
        ImagePlus ip = experiment.getProcessedImage();
        // Sample number
        int i = 1;
        // For each sample, update data and set source image
        for (Sample sample: experiment.getAllSamples()) {
            // Check for changes and update experiment  && Cut experiment samples
            if (dto.hasChanged() || !sample.hasSourceImage()) {
                // Get sample limits point
                Point point = sample.getLimits();
                // Get cuts points
                Point upper = new Point(point.getX(), 0);
                Point lower = new Point(point.getY(), ip.getHeight());
                ImagePlus img = ImageProcessing.cutImage(ip, upper, lower);
                // Save image to sample
                sample.setSourceImage(img);
                sample.setFrontPoint(0);
                sample.setSeedPoint(img.getHeight());
                sample.setName(Assets.getString("SAMPLE_NUMBER", i++));
            }
        }
        // Update comments
        experiment.setSplitComments(dto.getComments());
        // Set changed
        experiment.setSaved(!dto.hasChanged());
        return true;
    }
    
    /**
     * Retrieces special points selection data and update sub-samples.
     */
    private void processSpecialPointsSelection() {
        // Get view data
        DataDTO dto = view.getValues();
        // Check for changes and update experiment
        if (dto.hasChanged()) {
            for (Sample sample: experiment.getAllSamples()) {
                if (dto.hasChanged(sample.getId())) {
                    sample.setFrontPoint(dto.getSampleFrontPoint(sample.getId()));
                    sample.setSeedPoint(dto.getSampleSeedPoint(sample.getId()));
                    sample.clearProcessedImage();
                    // Clear all sample data that depends of seed/front point
                    sample.clearMean();
                    sample.clearPeaks();
                }
            }
        }
        // Check for changes and update experiment && process samples
        for (Sample sample: experiment.getAllSamples()) {
            // process sub sample image
            if (dto.hasChanged(sample.getId()) || !sample.hasProcessedImage()) {
                // Cut Sample image to seed and front point
                ImagePlus ip = sample.getSourceImage();
                Point upper = new Point(0, sample.getFrontPoint());
                Point lower = new Point(ip.getWidth(), sample.getSeedPoint());
                ImagePlus img = ImageProcessing.cutImage(ip, upper, lower);
                // Save sample processed image
                sample.setProcessedImage(img);
            }
            // Get sample processed image
            ImagePlus img = sample.getProcessedImage();
            // Compute if necessary sample mean
            if (dto.hasChanged(sample.getId()) || !sample.hasMean())
                sample.setMean(AnalysisProcessing.computeGIM(img));
            // Compute if necessary sample peaks points and baseline
            if (dto.hasChanged(sample.getId()) || !sample.hasPeaks()) {
                List<Peak> peaks = AnalysisProcessing.searchPeaks(img);
                int i = 1;
                // Sample peaks
                for (Peak peak: peaks) {
                    // Set Peak baseline
                    peak.setBaseline(AnalysisProcessing.searchBaseline(sample, peak));
                    // Set Peak Name
                    peak.setName(Assets.getString("PEAK", i));
                    // Set Peak Position
                    peak.setPosition(i++);
                    // Save peak
                    sample.addPeak(peak);
                }
            }
        }
        // Update name, comments and sample linked status
        for (Sample sample: experiment.getAllSamples()) {
            sample.setName(dto.getSampleName(sample.getId()));
            sample.setComments(dto.getSampleComment(sample.getId()));
            sample.setLinked(dto.isLinked(sample.getId()));
        }
        // Update comments
        experiment.setDataComments(dto.getComments());
        // Set changed
        experiment.setSaved(!dto.hasChanged());
    }
    
    /**
     * Process Samples Analysis, integration areas selection.
     */
    private boolean processSamplesAnalysis() {
        // Get view data
        AnalysisDTO dto = view.getValues();
        // Check for changes and update experiment
        if (dto.hasChanged()) {
            for (Sample sample: experiment.getAllSamples()) {
                if (dto.hasChanged(sample.getId())) {
                    // Clear sample peaks
                    sample.clearPeaks();
                    // Get new sample peaks
                    List<Pair<Float,Float>> peaksPoints = dto.getSamplePeaks(sample.getId());                    
                    // Sample peaks
                    for (Pair<Float,Float> point: peaksPoints) {
                        // Avoid empty peaks
                        if (point.getSecond() - point.getFirst() == 0) continue;
                        // Save peak
                        Peak peak = new Peak(point);
                        // Set Peak baseline
                        peak.setBaseline(AnalysisProcessing.searchBaseline(sample, peak));
                        // Save peak
                        sample.addPeak(peak);
                    }
                }
                // Avoid samples without peaks
                if (!sample.hasPeaks()) {
                    view.showWarningMessage(Assets.getString("SELECT_AT_LEAST_ONE_PEAK", sample.getName()));
                    return false;
                }
            }
        }
        // Check for changes and update experiment && process samples
        for (Sample sample: experiment.getAllSamples()) {
            // For each peak in sample
            for (Peak peak: sample.getPeaks()) {
                // Compute if necessary sample peaks surfaces
                if (dto.hasChanged(sample.getId()) || !peak.hasSurface())
                    peak.setSurface(AnalysisProcessing.integratePeak(sample, peak));
                // Compute if necessary sample local maximuns positions and values
                if (dto.hasChanged(sample.getId()) || !peak.hasMaximum())
                    peak.setMaximum(AnalysisProcessing.computeMaximum(sample, peak));
                // Compute if necessary sample peaks heights positions and values
                if (dto.hasChanged(sample.getId()) || !peak.hasHeight())
                    peak.setHeight(AnalysisProcessing.computeHeight(sample, peak));
            }
            // Remove empty surface peaks
            sample.getPeaks().removeIf(p -> p.getSurface() == 0);
            // Set Peaks Id's and Names
            List<Peak> peaks = sample.getPeaks();
            for (int i = 0; i < peaks.size(); i++) {
                Peak peak = peaks.get(i);
                peak.setId(i);
                peak.setName(Assets.getString("PEAK", i + 1));
                peak.setPosition(i + 1);
            }
            // Compute if necessary sample total surface
            if (dto.hasChanged(sample.getId()) || !sample.hasTotalSurface())
                sample.setTotalSurface(AnalysisProcessing.computeTotalSurface(sample));
            // For all peaks now can compute relative peak surface
            for (Peak peak: sample.getPeaks()) {
                // Compute if necessary sample peaks relative surfaces
                if (dto.hasChanged(sample.getId()) || !peak.hasRelativeSurface())
                    peak.setRelativeSurface(AnalysisProcessing.relativizeSurface(sample, peak));
            }
            // Check for empty peaks (no sample surface)
            if (sample.getTotalSurface() == 0) {
                view.showWarningMessage(Assets.getString("SELECT_AT_LEAST_ONE_PEAK", sample.getName()));
                return false;
            }
        }
        // Update comments
        for (Sample sample: experiment.getAllSamples()) {
            sample.setAnalysisComments(dto.getAnalysisComments(sample.getId()));
        }
        // Update comparation comments
        experiment.setComparationComments(dto.getComparationComments());
        // Set changed
        experiment.setSaved(!dto.hasChanged());
        // Peaks correctly selected
        return true;
    }
    
    /**
     * Process samples analysis results.
     */
    private void processSamplesAnalysisResults() {
        // get panel results
        ResultsDTO dto = view.getValues();
        // If dto has changed, can change peaks names and comments
        if (dto.hasChanged()) {
            for (Sample sample: experiment.getAllSamples()) {
                // Peaks name changes
                for (Peak peak: sample.getPeaks())
                    if (dto.hasChanged(sample.getId(), peak.getId()))
                        peak.setName(dto.getPeakName(sample.getId(), peak.getId()));
                // Sample results comments changes
                if (dto.hasChanged(sample.getId()))
                    sample.setResultsComments(dto.getResultsComments(sample.getId()));
            }
        }
        // Set changed
        experiment.setSaved(!dto.hasChanged());
    }
    
    /************************
     *                      *
     * View Reset Methods. *
     *                      *
     ***********************/
    
    private void resetViewStep() {
        // Reset main view panel
        switch (step) {
            case LOAD_IMAGE: // Reset Drop Panel
                view.showImageDropPanel(new DropDTO(experiment), true);
                break;
            case CUT_IMAGE: // Reset Cut Panel
                view.showCutPanel(new CuttingDTO(experiment), true);
                break;
            case ROTATE_IMAGE: // Reset Rotation Panel
                view.showRotionPanel(new RotationDTO(experiment), true);
                break;
            case SAMPLES_SELECT: // Reset Samples Selector Panel
                view.showSamplesSelector(new SplitDTO(experiment), true);
                break;
            case SPECIAL_POINTS: // Reset Special Points Selection Panel
                view.showSamplesSpecialPoints(new DataDTO(experiment), true);
                break;
            case ANALIZE_SAMPLES: // Reset Sample Analysis Tabs Panel
                view.showAnalysisPanel(new AnalysisDTO(experiment), true);
                break;
            case SAMPLES_ANALYSIS_RESULTS: // Reset Sample Analysis Results Tabs Panel
                view.showAnalysisResultsPanel(new ResultsDTO(experiment), true);
                break;
            default: // Debug
                System.err.println("Invalid Step at update view call :: " + step);
        }
    }
}