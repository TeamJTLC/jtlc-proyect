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

import ij.ImagePlus;
import java.util.List;
import java.util.stream.Collectors;
import jtlc.view.dto.AbstractDTO;
import jtlc.main.common.Point;
import jtlc.core.model.Experiment;

/**
 * SplitDTO implements DTO for SplitPanel
 * Save samples image, samples split points and comments
 * 
 * @author Cristian Tardivo
 */
public class SplitDTO extends AbstractDTO {
    private ImagePlus image;
    private List<Point> samplesPoints;
    private String comments;
    
    /**
     * Create new SplitDTO
     * @param project source project
     */
    public SplitDTO(Experiment project) {
        this.image = project.getProcessedImage();
        this.samplesPoints = project.getAllSamples().stream().map(s -> s.getLimits()).collect(Collectors.toList());
        this.comments = project.getSplitComments();
    }
    
    /**
     * Create new SplitDTO
     * @param image samples image
     * @param samplesPoints samples split points
     * @param comments split comments
     */
    public SplitDTO(ImagePlus image, List<Point> samplesPoints, String comments) {
        this.image = image;
        this.samplesPoints = samplesPoints;
        this.comments = comments;
    }
    
    /**
     * Get samples image
     * @return 
     */
    public ImagePlus getImage() {
        return image;
    }
    
    /**
     * Set samples image
     * @param image 
     */
    public void setImage(ImagePlus image) {
        this.image = image;
    }
    
    /**
     * Get samples split points
     * @return 
     */
    public List<Point> getSamplesPoints() {
        return samplesPoints;
    }
    
    /**
     * Set samples split points
     * @param samplesPoints 
     */
    public void setSamplesPoints(List<Point> samplesPoints) {
        this.samplesPoints = samplesPoints;
    }
    
    /**
     * Set comments
     * @param s 
     */
    public void setComments(String s) {
        comments = s;
    }
    
    /**
     * Get comments
     * @return 
     */
    public String getComments() {
        return comments;
    }
}