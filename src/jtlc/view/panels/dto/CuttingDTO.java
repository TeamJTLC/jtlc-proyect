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
import jtlc.view.dto.AbstractDTO;
import jtlc.main.common.Point;
import jtlc.core.model.Experiment;

/**
 * CuttingDTO implements DTO for CutPanel
 * Save processing image, cuts points and comments.
 * 
 * @author Cristian Tardivo
 */
public class CuttingDTO extends AbstractDTO {
    private ImagePlus image;
    private Point upper, lower;
    private String comments;
    
    /**
     * Create new CuttingDTO
     * @param project source project
     */
    public CuttingDTO(Experiment project) {
        this.image = project.getSourceImage();
        this.upper = project.getCutPoints().getFirst();
        this.lower = project.getCutPoints().getSecond();
        this.comments = project.getCutComments();        
    }
    
    /**
     * Create new CuttingDTO
     * @param image samples image
     * @param upper upper cut point
     * @param lower lower cut point
     * @param comments step comments
     */
    public CuttingDTO(ImagePlus image, Point upper, Point lower, String comments) {
        this.image = image;
        this.upper = upper;
        this.lower = lower;
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
        this.image = image;
    }
    
    /**
     * Get upper cut point
     * @return 
     */
    public Point getUpperPoint() {
        return upper;
    }
    
    /**
     * Set upper cut point
     * @param upper 
     */
    public void setUpperPoint(Point upper) {
        this.upper = upper;
    }
    
    /**
     * Get lower cut point
     * @return 
     */
    public Point getLowerPoint() {
        return lower;
    }
    
    /**
     * Set lower cut point
     * @param lower 
     */
    public void setLowerPoint(Point lower) {
        this.lower = lower;
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