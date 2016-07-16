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
package jtlc.view.panels.dto;

import java.util.LinkedList;
import java.util.List;
import jtlc.view.dto.AbstractDTO;
import jtlc.core.model.Experiment;

/**
 * GalleryDTO implements DTO for GaleryPanel
 * Save galery images, titles, descriptions and id/selected index
 *
 * @author Cristian Tardivo
 */
public class GalleryDTO extends AbstractDTO {
    private final List<Experiment> experiments;
    private int selectedIndex = -1;
    
    /**
     * Create new GaleryDto.
     */
    public GalleryDTO() {
        experiments = new LinkedList<>();
    }
    
    /**
     * Create new GaleryDto.
     * @param experiments
     */
    public GalleryDTO(List<Experiment> experiments) {
        this.experiments = experiments;
    }
    
    /**
     * Add Galery Element
     * @param experiment loaded Experiment to add
     */
    public void addExperiment(Experiment experiment) {
        experiments.add(experiment);
    }
    
    /**
     * Get Experiment list
     * @return 
     */
    public List<Experiment> getExperiments() {
        return experiments;
    }
    
    /**
     * Check if experiments list is empty
     * @return 
     */
    public boolean isEmpty() {
        return experiments.isEmpty();
    }
    
    /**
     * Set selected experiment index
     * @param index 
     */
    public void setSelectedIndex(int index) {
        selectedIndex = index;
    }
    
    /**
     * Get selected experiment from gallery
     * @return 
     */
    public Experiment getSelectedExperiment() {
        if (selectedIndex >= 0 && selectedIndex < experiments.size())
            return experiments.get(selectedIndex);
        return null;
    }
}