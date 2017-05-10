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
import jtlc.assets.Assets;
import jtlc.core.storage.ModelLoader;
import jtlc.main.common.Settings;
import jtlc.core.storage.ModelSaver;
import jtlc.core.model.Experiment;
import jtlc.view.panels.dto.GalleryDTO;
import jtlc.view.dialogs.dto.InfoDTO;
import jtlc.view.dialogs.dto.ProjectDTO;
import jtlc.view.dialogs.dto.SettingsDTO;

/**
 * jTLC main system controller.
 * Controls interactions between models, persistence and views.
 * 
 * @author Baldani Sergio - Tardivo Cristian
 */
public class BaseController extends AbstractController {
        
    /***************************
     *                         *
     * Actions Update Methods. *
     *                         *
     ***************************/
    
    /**
     * Start new proyect.
     */
    @Action("NEW_PROJECT")
    protected void newProject() {
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
    protected void loadProject() {
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
    protected void exploreProjects() {
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
    protected void saveProject() {
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
    protected void overwriteProject() {
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
    protected void exitSystem() {
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
    protected void editProject() {
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
    protected void editSettings() {
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
     * Show about Jtlc text dialog.
     */
    @Action("ABOUT_JTL")
    protected void showAbout() {
        String file = Assets.getString("ABOUT_FILE");
        view.showTextPanelDialog(Assets.getString("ABOUT"), Assets.loadDocument(file), "ic_about");
    }
   
    /**
     * Show about help text dialog.
     */
    @Action("SHOW_HELP")
    protected void showHelp() {
       String file = Assets.getString("MANUAL_FILE");
       view.showTextPanelDialog(Assets.getString("USER_MANUAL"), Assets.loadDocument(file), "ic_user_manual");
    }
   
    /**
     * Show about licenses text dialog.
     */
    @Action("LICENSES")
    protected void showLicenses() {
        String file = Assets.getString("LICENSES_FILE");
        view.showTextPanelDialog(Assets.getString("LICENSES"), Assets.loadDocument(file), "ic_licenses");
    }
}