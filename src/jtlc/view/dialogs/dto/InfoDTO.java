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
package jtlc.view.dialogs.dto;

import java.util.Date;
import jtlc.core.model.Experiment;
import jtlc.view.dto.AbstractDTO;

/**
 * InfoDTO implements DTO for InfoDialog
 * 
 * @author Cristian Tardivo
 */
public class InfoDTO extends AbstractDTO {
    private String name;
    private String description;
    private Date sampleDate;
    private Date analysisDate;
    
    /**
     * Create new empty InfoDTO
     */
    public InfoDTO() {
        
    }
    
    /**
     * Create new InfoDTO
     * @param project
     */
    public InfoDTO(Experiment project) {
        this.name = project.getName();
        this.description = project.getDescription();
        this.sampleDate = project.getSampleDate();
        this.analysisDate = project.getAnalysisDate();
    }
    
    /**
     * Create new InfoDTO
     * @param name
     * @param description
     * @param sampleDate
     * @param analysisDate 
     */
    public InfoDTO(String name, String description, Date sampleDate, Date analysisDate) {
        this.name = name;
        this.description = description;
        this.sampleDate = sampleDate;
        this.analysisDate = analysisDate;
    }

    /**
     * get Project name
     * @return 
     */
    public String getName() {
        return name;
    }

    /**
     * Set project name
     * @param name 
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * get project description
     * @return 
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set project description
     * @param description 
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get project sample date
     * @return 
     */
    public Date getSampleDate() {
        return sampleDate;
    }

    /**
     * Set project sample date
     * @param sampleDate 
     */
    public void setSampleDate(Date sampleDate) {
        this.sampleDate = sampleDate;
    }

    /**
     * Get project analysis date
     * @return 
     */
    public Date getAnalysisDate() {
        return analysisDate;
    }
    
    /**
     * Set project analysis date
     * @param analysisDate 
     */
    public void setAnalysisDate(Date analysisDate) {
        this.analysisDate = analysisDate;
    }
}