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

import java.io.File;
import ij.ImagePlus;
import java.io.IOException;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import org.jopendocument.dom.ODSingleXMLDocument;
//
import jtlc.core.processing.ImageProcessing;
import jtlc.assets.Assets;
import jtlc.core.storage.ModelSaver;
import jtlc.core.model.Sample;
import jtlc.core.reports.Reporter;
import jtlc.core.storage.ImageStore;
import jtlc.view.dialogs.dto.ImageExportDTO;

/**
 * jTLC main system controller.
 * Controls interactions between models, persistence and views.
 * 
 * @author Baldani Sergio - Tardivo Cristian
 */
public class ExportController extends AbstractController {
    
    /***************************
     *                         *
     * Actions Update Methods. *
     *                         *
     ***************************/
    
    /**
     * Export Experiment data and images.
     * @param param experiment or sample reference id
     * @throws IOException
     */
    @Action("EXPORT_DATA")
    protected void export(String param) throws IOException {
        // Parse parameters
        String[] command = param.split("#");
        if (command.length < 2) {
            System.err.println("Invalid command at Data Export: " + param);
            return;
        }
        // Get references
        String which = command[0];
        int id = Integer.valueOf(command[1]);
        // Export Data
        if (which.equals("DATA")) {
            exportData(id);
            return;
        }
        // Export Sample Mean
        if (which.equals("MEAN")) {
            exportMean(id);
            return;
        }
        // Export Image (source or processed) (Sample or Experiment)
        if (which.equals("IMAGE") || which.equals("PROCESSED_IMAGE")) {
            exportImage(id, which);
            return;
        }
        // Create and export Reports
        if (which.equals("REPORT")) {
            exportReport(id, command[2]);
        }
    }
        
    /**
     * Export sample/experiment data
     * @param id  -1 for experiment, other for sample id
     */
    private void exportData(int id) {
        // Short name
        String expName = Assets.shortString(experiment.getName(), 40, false);
        String smpName = (id >= 0)? Assets.shortString(experiment.getSampleById(id).getName(), 40, false) : null;
        // sample data
        if (id >= 0) { 
            Sample sample = experiment.getSampleById(id);
            File file = view.showFileSave("TXT", "txt", expName + "-" + smpName + "-sample_data");
            // Check file and save
            if (file != null) {
                view.showProgress(true);
                new Thread(() -> {
                    if (ModelSaver.saveSampleData(file, sample))
                        view.showMessage(Assets.getString("SAMPLE_DATA_SAVED"));
                    else
                        view.showWarningMessage(Assets.getString("ERROR_SAVING_FILE"));
                    view.showProgress(false);
                }).start();
            } else {
                view.showWarningMessage(Assets.getString("INVALID_SELECTED_FILE"));
            }
        } else { // experiment data
            File file = view.showFileSave("TXT", "txt", expName + "-experiment_data");
            // Check file and save
            if (file != null) {
                view.showProgress(true);
                new Thread(() -> {
                    if (ModelSaver.saveExperimentData(file, experiment))
                        view.showMessage(Assets.getString("EXPERIMENT_DATA_SAVED"));
                    else
                        view.showWarningMessage(Assets.getString("ERROR_SAVING_FILE"));
                    view.showProgress(false);
                }).start();
            } else {
                view.showWarningMessage(Assets.getString("INVALID_SELECTED_FILE"));
            }
        }
    }
    
    /**
     * Export sample mean
     * @param id sample id
     */
    private void exportMean(int id) {
        // Short name
        String expName = Assets.shortString(experiment.getName(), 40, false);
        String smpName = (id >= 0)? Assets.shortString(experiment.getSampleById(id).getName(), 40, false) : null;
        // sample mean
        Sample sample = experiment.getSampleById(id);
        File file = view.showFileSave("TXT", "txt", expName + "-" + smpName + "-sample_mean");
        if (file != null) {
            view.showProgress(true);
            new Thread(() -> {
                if (ModelSaver.saveMean(file, sample.getMean()))
                    view.showMessage(Assets.getString("SAMPLE_MEAN_SAVED"));
                else 
                    view.showWarningMessage(Assets.getString("ERROR_SAVING_FILE"));
                view.showProgress(false);
            }).start();
        } else {
            view.showWarningMessage(Assets.getString("INVALID_SELECTED_FILE"));
        }
    }
    
    /**
     * Export sample/experiment images
     * @param id  -1 for experiment, other for sample id
     * @param which if source image or processed image
     */
    private void exportImage(int id, String which) {
        // Short name
        String expName = Assets.shortString(experiment.getName(), 40, false);
        String smpName = (id >= 0)? Assets.shortString(experiment.getSampleById(id).getName(), 40, false) : null;
        // Image and name
        ImagePlus image;
        String name;
        boolean source = which.equals("IMAGE");
        // Sample
        if (id >= 0) {
            Sample sample = experiment.getSampleById(id);
            image = source? sample.getSourceImage() : sample.getProcessedImage();
            name =  expName + "-" + smpName + (source? "-source_image" : "-processed_image");
        } else { // Experiment
            image = source? experiment.getSourceImage() : experiment.getProcessedImage();
            name = expName + (source? "-source_image" : "-processed_image");
        }
        // Show image export dialog (duplicate image to preserve project image)
        ImageExportDTO result = view.showImageExportDialog(new ImageExportDTO(image.duplicate(), true));
        // If accepted export image
        if (result.hasChanged()) {
            // duplicate to preserve original
            ImagePlus resized = image.duplicate();
            // resize if necessary
            ImageProcessing.resizeImage(resized, result.getWidth(), result.getHeight());
            // show image file selector
            File file = view.showFileSave("JPG", "jpg", name);
            // Save image and show saved message
            if (file != null) {
                view.showProgress(true);
                new Thread(() -> {
                    ImageStore.saveImage(resized, file.getPath());
                    view.showMessage(Assets.getString("IMAGE_SAVED"));
                    view.showProgress(false);
                }).start();
            } else {
                view.showWarningMessage(Assets.getString("INVALID_SELECTED_FILE"));
            }
        }
    }
    
    /**
     * Export sample/experiment report
     * @param id  -1 for experiment, other for sample id
     * @param format if save in PDF, HTML or ODT
     */
    private void exportReport(int id, String format) throws IOException {
        // Short names
        String expName = Assets.shortString(experiment.getName(), 40, false);
        String smpName = (id >= 0)? Assets.shortString(experiment.getSampleById(id).getName(), 40, false) : null;
        // Reports Generator
        Reporter reporter = new Reporter();
        // File selector helper
        Supplier<File> selector = () -> {
            String fileName = (id >= 0)? expName + "-" + smpName + "-report" : expName + "-report";
            return view.showFileSave(format, format.toLowerCase(), fileName);
        };
        // Document Saver helper
        BiFunction<ODSingleXMLDocument, File, Boolean> saver = (report, file) -> {
            try {
                // Save as ODT
                if (format.equals("ODT")) {
                    reporter.saveAsODT(report, file);
                    return true;
                }
                // Save as PDF
                if (format.equals("PDF")) {
                    reporter.saveAsPDF(report, file);
                    return true;
                }
                // Save as HTML
                if (format.equals("HTML")) {
                    reporter.saveAsHTML(report, file);
                    return true;
                }
                // Error
                if (format.equals("HTML") || format.equals("PDF") || format.equals("ODT"))
                    throw new Exception();
            } catch (Exception ex) {
                view.showWarningMessage(Assets.getString("ERROR_SAVING_FILE"));
            }
            // if can't save report document as selected file
            return false;
        };
        // Show file selector
        File file = selector.get();
        // Save document if file has selected
        if (file != null) {
            // Report generation and save
            if (id >= 0) { // sample
                Sample sample = experiment.getSampleById(id);
                // Save and show saved message
                view.showProgress(true);
                new Thread(() -> {
                    if (format.equals("CSV") && reporter.saveSampleCSVReport(sample, file)) {
                        view.showMessage(Assets.getString("SAMPLE_REPORT_SAVED"));
                    } else if (saver.apply(reporter.createSampleReport(sample), file)) {
                        view.showMessage(Assets.getString("SAMPLE_REPORT_SAVED"));
                    }
                    view.showProgress(false);
                }).start();
            } else { // experiment
                // Save and show saved message
                view.showProgress(true);
                new Thread(() -> {
                    if (format.equals("CSV") && reporter.saveExperimentCSVReport(experiment, file)) {
                        view.showMessage(Assets.getString("EXPERIMENT_REPORT_SAVED"));
                    } else if (saver.apply(reporter.createExperimentReport(experiment, true), file)) {
                        view.showMessage(Assets.getString("EXPERIMENT_REPORT_SAVED"));
                    }
                    view.showProgress(false);
                }).start();
            }
        } else {
            view.showWarningMessage(Assets.getString("INVALID_SELECTED_FILE"));
        }
    }    
}