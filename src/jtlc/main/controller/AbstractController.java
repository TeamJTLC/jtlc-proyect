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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Observable;
import java.util.Observer;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.List;
import jtlc.core.model.Experiment;
import jtlc.main.common.Pair;
import jtlc.view.MainView;
import jtlc.view.dto.ExportDTO;
import jtlc.view.panels.dto.AnalysisDTO;
import jtlc.view.panels.dto.CuttingDTO;
import jtlc.view.panels.dto.DataDTO;
import jtlc.view.panels.dto.DropDTO;
import jtlc.view.panels.dto.GalleryDTO;
import jtlc.view.panels.dto.ReportsDTO;
import jtlc.view.panels.dto.ResultsDTO;
import jtlc.view.panels.dto.RotationDTO;
import jtlc.view.panels.dto.SplitDTO;

/**
 * jTLC main system controller.
 * Controls interactions between models, persistence and views.
 * 
 * @author Baldani Sergio - Tardivo Cristian
 */
public abstract class AbstractController implements Observer { 
        
    /**
     * Analysis Steps enumeration.
     */
    protected enum Step {
        START_SYSTEM, EXPLORE_PROJECTS, LOAD_IMAGE, ROTATE_IMAGE, SAMPLES_SELECT,
        SPECIAL_POINTS, ANALIZE_SAMPLES, CUT_IMAGE, SAMPLES_ANALYSIS_RESULTS, ANALYSIS_REPORTS;
    };
    
    /**
     * System main view.
     */
    protected static MainView view;
    
    /**
     * Current analysis step.
     */
    protected static Step step = Step.START_SYSTEM;
    
    /**
     * Current working experiment.
     */
    protected static Experiment experiment;
    
    /**
     * Loaded experiments list for gallery/explorer and source folder.
     */
    protected static List<Experiment> experiments;
    protected static File folder;
    
    /**
     * Actions Methods mapped to action.
     */
    private HashMap<String,Method> methodsMap;
    
    /**
     * Default controller constructor.
     */
    public AbstractController() {
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
                LoggerFactory.getLogger(AbstractController.class).error(command, ex);
            }
        }
    }
    
    /**
     * Set Main view for all controllers instances
     * @param view controller associated view
     */
    public static void setMainView(MainView view) {
        AbstractController.view = view;
    }
    
    /************************
     *                      *
     * View Update Methods. *
     *                      *
     ***********************/
    
    /**
     * Update view to specific step 
     * @param next
     */
    protected void updateViewStep(Step next) {
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