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

/**
 * DropDTO implements DTO for DropPanel
 * Save drop image and comments.
 * 
 * @author Cristian Tardivo
 */
public class DropDTO extends AbstractDTO {
    private ImagePlus image;
    private String comments;
    
    /**
     * Create new DropDTO
     * @param project source project
     */
    public DropDTO(Experiment project) {
        this.image = project.getSourceImage();
        this.comments = project.getSourceImageComments();
    }
    
    /**
     * Create new DropDTO
     * @param image drop image
     * @param comments drop comments
     */
    public DropDTO(ImagePlus image, String comments) {
        this.image = image;
        this.comments = comments;
    }
    
    /**
     * Set drop image
     * @param image 
     */
    public void setImage(ImagePlus image) {
        this.image = image;
    }
    
    /**
     * Get drop image
     * @return 
     */
    public ImagePlus getImage() {
        return image;
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