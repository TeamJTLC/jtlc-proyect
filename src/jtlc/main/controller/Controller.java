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

import com.alee.managers.language.LanguageManager;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Observable;
import java.util.Observer;
import org.slf4j.LoggerFactory;
import ij.ImagePlus;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import org.jopendocument.dom.ODSingleXMLDocument;
//
import jtlc.core.processing.ImageProcessing;
import jtlc.core.processing.AnalysisProcessing;
import jtlc.assets.Assets;
import jtlc.core.storage.ModelLoader;
import jtlc.main.common.Pair;
import jtlc.main.common.Point;
import jtlc.main.common.Settings;
import jtlc.core.storage.ModelSaver;
import jtlc.core.model.Experiment;
import jtlc.core.model.Peak;
import jtlc.core.model.Sample;
import jtlc.core.processing.AnalysisProcessing.Axis;
import jtlc.core.reports.Reporter;
import jtlc.core.storage.ImageStore;
import jtlc.view.panels.dto.CuttingDTO;
import jtlc.view.panels.dto.DropDTO;
import jtlc.view.panels.dto.GalleryDTO;
import jtlc.view.panels.dto.RotationDTO;
import jtlc.view.panels.dto.SplitDTO;
import jtlc.view.panels.dto.AnalysisDTO;
import jtlc.view.panels.dto.DataDTO;
import jtlc.view.MainView;
import jtlc.view.MainView.Panels;
import jtlc.view.dialogs.dto.ImageExportDTO;
import jtlc.view.dialogs.dto.InfoDTO;
import jtlc.view.dialogs.dto.ProjectDTO;
import jtlc.view.dialogs.dto.SettingsDTO;
import jtlc.view.dto.ExportDTO;
import jtlc.view.panels.dto.ReportsDTO;
import jtlc.view.panels.dto.ResultsDTO;

/**
 * jTLC main system controller.
 * Controls interactions between models, persistence and views.
 * 
 * @author Baldani Sergio - Tardivo Cristian
 */
public class Controller implements Observer {
    
    /**
     * Analysis Steps enumeration.
     */
    private enum Step {
        START_SYSTEM, EXPLORE_PROJECTS, LOAD_IMAGE, ROTATE_IMAGE, SAMPLES_SELECT,
        SPECIAL_POINTS, ANALIZE_SAMPLES, CUT_IMAGE, SAMPLES_ANALYSIS_RESULTS, ANALYSIS_REPORTS;
    };
    
    /**
     * System main view.
     */
    private final MainView view;
    
    /**
     * Current analysis step.
     */
    private Step step;
    
    /**
     * Current working experiment.
     */
    private Experiment experiment;
    
    /**
     * Loaded experiments list for gallery/explorer and source folder.
     */
    private List<Experiment> experiments;
    private File folder;
    
    /**
     * Actions Methods mapped to action.
     */
    private HashMap<String,Method> methodsMap;
    
    /**
     * Default controller constructor.
     * @param view controller associated view
     */
    public Controller(MainView view) {
        this.view = view;
        this.step = Step.START_SYSTEM;
        initMethodsMap();
    }
    
    /**
     * Init Methods map to action
     */
    private void initMethodsMap() {
        // Init methods map
        methodsMap = new HashMap<>();
        Class controller = this.getClass();
        Method[] methods = controller.getDeclaredMethods();
        // for all methods in this class with declared Action annotation
        for (Method method: methods) {
            Action annotation = method.getAnnotation(Action.class);
            if (annotation != null)
                methodsMap.put(annotation.value(), method);
        }
    }
    
    /**
     * Observer update method.
     * @param obs Observable caller
     * @param arg caller arguments
     */
    @Override
    public void update(Observable obs, Object arg) {
        String command;
        Object params = null;
        // Get parameters and command
        if (arg instanceof Pair) {
            Pair<String,Object> data = (Pair) arg;
            command = data.getFirst();
            params = data.getSecond();
        } else if (arg instanceof String) { // Only a command
            command = (String)arg;
        } else { // Invalid call
            System.err.println("Invalid command in controller update");
            return;
        }
        // Excute method
        Method method = methodsMap.get(command);
        if (method != null) {
            try {
                // With out parameters
                if (method.getParameterCount() == 0)
                    method.invoke(this);
                else // Parameterized invoke (only 1 parameter allowed)
                    method.invoke(this, method.getParameterTypes()[0].cast(params));
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                LoggerFactory.getLogger(Controller.class).error(command, ex);
            }
        } else {
            LoggerFactory.getLogger(Controller.class).error("Unknow method for command: " + command + " in controller update");
        }
    }
    
    /***************************
     *                         *
     * Actions Update Methods. *
     *                         *
     ***************************/
    
    /**
     * Start new proyect.
     */
    @Action("NEW_PROJECT")
    private void newProject() {
        boolean clear = false;
        // Check for previous experiment
        if (experiment != null) {
            if (view.showConfirmDialog(Assets.getString("OVERRIDE_PROJECT")))
                clear = true;
            else
                return;
        }
        // Show new project dialog
        ProjectDTO dto = view.showNewProjectDialog();
        if (dto.hasChanged()) {
            if (clear) {
                experiment.clear();
                view.restartView();
                step = Step.START_SYSTEM;
            }
            experiment = new Experiment(dto.getName(), dto.getDescription(), dto.getSampleDate(), dto.getAnalysisDate());
            updateViewStep(Step.LOAD_IMAGE);
        }
    }
    
    /**
     * Load saved proyect.
     */
    @Action("LOAD_PROJECT")
    private void loadProject() {
        boolean clear = false;
        // Check for previous experiment
        if (experiment != null) {
            if (view.showConfirmDialog(Assets.getString("OVERRIDE_PROJECT")))
                clear = true;
            else
                return;
        }
        // Load other project
        File selectedFile = view.showLoadProject("jtlc");
        if (selectedFile != null) {
            // Clear old project
            if (clear) {
                experiment.clear();
                view.restartView();
                step = Step.START_SYSTEM;
            }
            // Load new project
            view.showProgress(true);
            new Thread(() -> {
                experiment = ModelLoader.loadExperiment(selectedFile);
                if (experiment != null) {
                    view.showProgress(false);
                    view.showMessage(Assets.getString("PROJECT_LOADED"));
                    updateViewStep(Step.LOAD_IMAGE);
                } else {
                    view.showProgress(false);
                    view.showWarningMessage(Assets.getString("PROJECT_LOAD_ERROR"));
                }
            }).start();
        }
    }
    
    /**
     * Explore old Proyects.
     */ 
    @Action("EXPLORE_PROJECTS")
    private void exploreProjects() {
        boolean clear = false;
        boolean previous = false;
        // Check for previous experiment
        if (experiment != null) {
            if (view.showConfirmDialog(Assets.getString("OVERRIDE_PROJECT")))
                clear = true;
            else
                return;
        }
        // Check for previous explored floder
        if (experiments != null && !view.showConfirmDialog(Assets.getString("EXPLORE_OTHER_FOLDER"))) {
            previous = true;
        }
        // If explore new folder, show folder chooser
        if (!previous) {
            folder = view.showDirectoryChooser();
            if (folder == null) return;
        }
        // Clear old project
        if (clear) {
            experiment.clear();
            view.restartView();
            step = Step.START_SYSTEM;
        }
        // Show progress bar
        view.showProgress(true);
        // Load new folder
        final boolean loadFolder = !previous;
        // Open Files
        new Thread(() -> {
            if (loadFolder && folder != null) experiments = ModelLoader.loadExperiments(folder);
            // Create Gallery dto from experiments list
            GalleryDTO dto = new GalleryDTO(experiments);
            // Check open project results and show galery panel
            if (!dto.isEmpty()) {
                step = Step.EXPLORE_PROJECTS;
                view.showImageGaleryPanel(dto, false);
                view.updateButtons(true, false, false, false);
            } else {
                view.showWarningMessage(Assets.getString("EMPTY_FOLDER"));
                folder = null;
            }
            // Hide progress bar
            view.showProgress(false);
        }).start();
    }
    
    /**
     * Save current proyect.
     */
    @Action("SAVE_AS_PROJECT")
    private void saveProject() {
        // Validate experiment
        if (experiment == null) return;
        // Short name
        String expName = Assets.shortString(experiment.getName(), 40, false);
        // Show file saver
        File file = view.showFileSave(Assets.getString("JTLC_PROJECT"), "jtlc", expName);
        // Validate file
        if (file == null) return;
        // Save experiment
        if (!ModelSaver.saveExperiment(experiment, file)) {
            view.showWarningMessage(Assets.getString("PROJECT_SAVE_ERROR", file.getName()));
        } else {
            view.showMessage(Assets.getString("PROJECT_SAVED_MESSAGE", file.getName()));
            view.updateMenu(true, true, true);
        }
    }
    
    /**
     * Overwrite Saved proyect.
     */
    @Action("SAVE_PROJECT")
    private void overwriteProject() {
        // Validate experiment
        if (experiment == null) return;
        // Get expperiment file reference
        File file = experiment.getFile();
        // Validate filde
        if (file == null) return;
        // Check for replace
        if (file.exists() && !view.showConfirmDialog(Assets.getString("OVERWRITE_FILE") + "\n" + Assets.getString("FILE") + ": " + file.getName())) return;
        // Save experiment
        if (!ModelSaver.saveExperiment(experiment, file))
            view.showWarningMessage(Assets.getString("PROJECT_SAVE_ERROR", file.getName()));
        else
            view.showMessage(Assets.getString("PROJECT_SAVED_MESSAGE", file.getName()));
    }
    
    /**
     * Exit jTLC system.
     */
    @Action("EXIT_SYSTEM")
    private void exitSystem() {
        // Check for unsaved changes
        String unsaved = "";
        if (experiment != null && !experiment.isSaved())
            unsaved = "\n" + Assets.getString("PROJECT_UNSAVED_CHANGES");
        // Check for exit confirm
        if (view.showConfirmDialog(Assets.getString("EXIT_DIALOG") + unsaved)) {
            Settings.setExtendedState(view.getWindowExtendedState());
            Settings.setWindowSize(view.getWindowSize());
            Settings.setWindowLocation(view.getWindowLocation());
            Settings.saveSettings();
            System.exit(0);
        }
    }
    
    /**
     * Edit current proyect.
     */
    @Action("EDIT_PROJECT")
    private void editProject() {
        if (experiment == null)
            return;
        //
        InfoDTO dto = view.showEditProjectDialog(new InfoDTO(experiment));
        if (dto.hasChanged()) {
            experiment.setName(dto.getName());
            experiment.setDescription(dto.getDescription());
            experiment.setAnalysisDate(dto.getAnalysisDate());
            experiment.setSampleDate(dto.getSampleDate());
            // Show Project update message
            view.showMessage(Assets.getString("PROJECT_UPDATED"));
        }
    }
    
    /**
     * Edit system settings
     */
    @Action("EDIT_SETTINGS")
    private void editSettings() {
        SettingsDTO dto = view.showSettingsDialog(new SettingsDTO());
        if (dto.hasChanged()) {
            Settings.setLocale(dto.getCurrentLocale());
            Settings.setTransitionsEnabled(dto.isTransitionsEnabled());
            Settings.setWorkSpace(dto.getWorkSpacePath());
            Settings.saveSettings();
            // Update Current locale
            Assets.changeLocale(Settings.getLocale());
            LanguageManager.setLanguage(Settings.getLocale());
            // Update view transitions status
            view.setTransitionEnabled(Settings.isTransitionsEnabled());
            // Show Settings update message
            view.showMessage(Assets.getString("SETTINGS_UPDATED"));
        }
    }
    
    /**
     * Advances to the next step.
     */
    @Action("NEXT_STEP")
    private void nextStep() {
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
    private void previousStep() {
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
    private void restartStep() {
        if (view.showConfirmDialog(Assets.getString("RESTART_STEP")))
            resetViewStep();
    }
   
    /**
     * Show about Jtlc text dialog.
     */
    @Action("ABOUT_JTL")
    private void showAbout() {
        String file = Assets.getString("ABOUT_FILE");
        view.showTextPanelDialog(Assets.getString("ABOUT"), Assets.loadDocument(file), "ic_about");
    }
   
    /**
     * Show about help text dialog.
     */
    @Action("SHOW_HELP")
    private void showHelp() {
       String file = Assets.getString("MANUAL_FILE");
       view.showTextPanelDialog(Assets.getString("USER_MANUAL"), Assets.loadDocument(file), "ic_user_manual");
    }
   
    /**
     * Show about licenses text dialog.
     */
    @Action("LICENSES")
    private void showLicenses() {
        String file = Assets.getString("LICENSES_FILE");
        view.showTextPanelDialog(Assets.getString("LICENSES"), Assets.loadDocument(file), "ic_licenses");
    }
    
    /**
     * Export Experiment data and images.
     * @param param experiment or sample reference id
     */
    @Action("EXPORT_DATA")
    private void export(String param) throws IOException {
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
    
    /*******************
     *                 *
     * Export Methods. *
     *                 *
     *******************/
    
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
     * View Update Methods. *
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
    
    /**
     * Update view to specific step
     * @param step 
     */
    private void updateViewStep(Step next) {
        // Save next step
        step = next;
        // Update main view components
        switch (step) {
            case EXPLORE_PROJECTS: // Show Gallery Panel
                view.showImageGaleryPanel(new GalleryDTO(experiments), false);
                view.updateButtons(true, false, false, false);
                view.updateMenu(false, false, false);
                view.updateExportMenu(null);
                break;
            case LOAD_IMAGE: // Show Drop Panel
                view.showImageDropPanel(new DropDTO(experiment), false);
                view.updateExportMenu(new ExportDTO(experiment));
                view.updateButtons(experiments == null, true, experiments != null, experiments != null);
                view.updateMenu(true, experiment.hasFile(), true);
                break;
            case CUT_IMAGE: // Show Cut Panel
                view.showCutPanel(new CuttingDTO(experiment), false);
                view.updateExportMenu(new ExportDTO(experiment));
                view.updateButtons(false, true, true, true);
                view.updateMenu(true, experiment.hasFile(), true);
                break;
            case ROTATE_IMAGE: // Show Rotation Panel
                view.showRotionPanel(new RotationDTO(experiment), false);
                view.updateExportMenu(new ExportDTO(experiment));
                view.updateButtons(false, true, true, true);
                view.updateMenu(true, experiment.hasFile(), true);
                break;
            case SAMPLES_SELECT: // Show Samples Selector Panel
                view.showSamplesSelector(new SplitDTO(experiment), false);
                view.updateExportMenu(new ExportDTO(experiment));
                view.updateButtons(false, true, true, true);
                view.updateMenu(true, experiment.hasFile(), true);
                break;
            case SPECIAL_POINTS: // Show Special Points Selection Panel
                view.showSamplesSpecialPoints(new DataDTO(experiment), false);
                view.updateExportMenu(new ExportDTO(experiment));
                view.updateButtons(false, true, true, true);
                view.updateMenu(true, experiment.hasFile(), true);
                break;
            case ANALIZE_SAMPLES: // Show Sample Analysis Tabs Panel
                view.showAnalysisPanel(new AnalysisDTO(experiment), false);
                view.updateExportMenu(new ExportDTO(experiment));
                view.updateButtons(false, true, true, true);
                view.updateMenu(true, experiment.hasFile(), true);
                break;
            case SAMPLES_ANALYSIS_RESULTS: // Show Sample Analysis Results Tabs Panel
                view.showAnalysisResultsPanel(new ResultsDTO(experiment), false);
                view.updateExportMenu(new ExportDTO(experiment));
                view.updateButtons(false, true, true, true);
                view.updateMenu(true, experiment.hasFile(), true);
                break;
            case ANALYSIS_REPORTS: // Show Samples Analysis Reports Panel
                view.showAnalysisReportsPanel(new ReportsDTO(experiment), false);
                view.updateExportMenu(new ExportDTO(experiment, true));
                view.updateButtons(false, false, true, false);
                view.updateMenu(false, experiment.hasFile(), true);
                break;
            default: // Debug
                System.err.println("Invalid Step at update view call :: " + step);
        }
    }
}