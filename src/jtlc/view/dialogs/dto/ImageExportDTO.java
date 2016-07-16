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

import ij.ImagePlus;
import jtlc.view.dto.AbstractDTO;

/**
 * ImageExportDTO implements DTO for ImageExportDialog
 * 
 * @author Cristian Tardivo
 */
public class ImageExportDTO extends AbstractDTO {
    private ImagePlus preview;
    private int width;
    private int height;
    private boolean linked;
    
    /**
     * Create new image export dialog DTO.
     * @param preview export image preview
     */
    public ImageExportDTO(ImagePlus preview) {
        this.preview = preview.duplicate();
        this.width = preview.getWidth();
        this.height = preview.getHeight();
        this.linked = false;
    }
    
    /**
     * Create new image export dialog DTO.
     * @param preview export image preview
     * @param linked linked proportions
     */
    public ImageExportDTO(ImagePlus preview, boolean linked) {
        this(preview);
        this.linked = linked;
    }

    /**
     * Get preview image
     * @return 
     */
    public ImagePlus getPreview() {
        return preview;
    }

    /**
     * Set preview image
     * @param preview 
     */
    public void setPreview(ImagePlus preview) {
        this.preview = preview.duplicate();
    }

    /**
     * Get image width
     * @return 
     */
    public int getWidth() {
        return width;
    }

    /**
     * Set image widt
     * @param width 
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * Get image height
     * @return 
     */        
    public int getHeight() {
        return height;
    }

    /**
     * Set image height
     * @param height 
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * Is linked size proportion
     * @return 
     */
    public boolean isLinked() {
        return linked;
    }

    /**
     * Set linked size proportions
     * @param linked 
     */
    public void setLinked(boolean linked) {
        this.linked = linked;
    }
}