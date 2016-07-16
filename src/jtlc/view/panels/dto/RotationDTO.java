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
import jtlc.core.model.Experiment;
import jtlc.view.dto.AbstractDTO;
import jtlc.core.processing.AnalysisProcessing.Axis;
import jtlc.core.processing.ImageProcessing;

/**
 * RotationDTO implements DTO for RotationPanel
 * Save processing image, rotation angle, flip axis and comments.
 * 
 * @author Cristian Tardivo
 */
public class RotationDTO extends AbstractDTO {
    private ImagePlus image;
    private Axis axis;
    private Double angle;
    private String comments;
    
    /**
     * Create new RotationDTO
     * @param project source poryect
     */
    public RotationDTO(Experiment project) {
        this.axis = project.getFlipAxis();
        this.angle = project.getRotationAngle();
        this.comments = project.getRotationComments();
        // Avoid image quality loss (rotation distorsion fix)
        if (this.angle != 0 || this.axis != Axis.NONE)
            this.image = ImageProcessing.cutImage(project.getSourceImage(), project.getCutPoints());
        else
            this.image = project.getProcessedImage().duplicate();        
    }
    
    /**
     * Create new RotationDTO
     * @param image processing image
     * @param axis default flip axis
     * @param angle default rotation angle
     * @param comments rotation comments
     */
    public RotationDTO(ImagePlus image, Axis axis, Double angle, String comments) {
        this.image = image;
        this.axis = axis;
        this.angle = angle;
        this.comments = comments;
    }
    
    /**
     * Get processing image
     * @return 
     */
    public ImagePlus getImage() {
        return image;
    }
    
    /**
     * Set processing image
     * @param image 
     */
    public void setImage(ImagePlus image) {
        this.image = image.duplicate();
    }
    
    /**
     * Get flip axis
     * @return 
     */
    public Axis getFlipAxis() {
        return axis;
    }
    
    /**
     * Set flip axis
     * @param axis 
     */
    public void setFlipAxis(Axis axis) {
        this.axis = axis;
    }
    
    /**
     * Get rotation angle
     * @return 
     */
    public Double getRotationAngle() {
        return angle;
    }
    
    /**
     * Set rotation angle
     * @param angle 
     */
    public void setRotationAngle(Double angle) {
        this.angle = angle;
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