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
package jtlc.view.panels;


import com.alee.laf.scroll.WebScrollPane;
import java.awt.Dimension;
import java.awt.Image;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import javax.swing.JLayeredPane;
import jtlc.assets.Assets;
import jtlc.core.model.Experiment;
import jtlc.view.dto.AbstractDTO;
import jtlc.view.panels.dto.GalleryDTO;
import jtlc.view.components.CustomWebGallery;

/**
 * Experiment Gallery
 * Implements JLayeredPane with web gallery component.
 * 
 * @author Cristian Tardivo
 */
public class GalleryPanel extends JLayeredPane implements IPanel {
    // Panel Componenets
    private CustomWebGallery webGallery;
    private WebScrollPane galeryComponent;
    private GalleryDTO data;
    
    /**
     * Create DropPanel
     * @param dto DropPanel Data-Transfer-Object
     * @param size Initial Panel Size
     */  
    public GalleryPanel(GalleryDTO dto,Dimension size) {
        // Save data
        data = dto;
        // Create Galery
        webGallery = new CustomWebGallery();
        webGallery.setPreferredColumnCount(3);
        webGallery.setScrollOnSelection(true);
        webGallery.setSize(size);
        DateFormat df = new SimpleDateFormat(Assets.getString("DATE_FORMAT"));
        // Add Images to galery
        for (Experiment exp : data.getExperiments()) {
            Image image = exp.hasSourceImage()? exp.getSourceImage().getImage() : Assets.loadImage("logo_jtlc");
            webGallery.addImage(image, exp.getName(), exp.getDescription(), df.format(exp.getSampleDate()));
        }
        // Add Galery to panel
        galeryComponent = webGallery.getView(false);
        galeryComponent.setLocation(0, 0);
        galeryComponent.setSize(size);
        galeryComponent.requestFocus();
        this.add(galeryComponent, JLayeredPane.DEFAULT_LAYER);
    }
    
    /**
     * Resize Panel Components
     * @param size new panel size
     */
    @Override
    public void setSize(Dimension size) {
        webGallery.setSize(size);
        galeryComponent.setSize(size);
        super.setSize(size);
    }
    
    /**
     * Get Changes
     * @return DropPanel Data-Transfer-Object
     */
    @Override
    public GalleryDTO getResults() {
        int index = webGallery.getSelectedIndex();
        if (index != -1) {
            data.setChanged(true);
            data.setSelectedIndex(index);
        }
        return data;
    }
    
    /**
     * Update panel components to new values in dto
     * @param dto Panel Data-Transfer-Object
     */
    @Override
    public void updatePanel(AbstractDTO dto) {
        // Check for valid dto
        if (!(dto instanceof GalleryDTO))
            return;
        data = (GalleryDTO) dto;
        // Clear panel gallery component
        this.remove(galeryComponent);
        // Create Galery
        webGallery = new CustomWebGallery();
        webGallery.setPreferredColumnCount(3);
        webGallery.setScrollOnSelection(true);
        webGallery.setSize(this.getSize());
        DateFormat df = new SimpleDateFormat(Assets.getString("DATE_FORMAT"));
        // Add Images to galery
        for (Experiment exp : data.getExperiments()) {
            Image image = exp.hasSourceImage()? exp.getSourceImage().getImage() : Assets.loadImage("logo_jtlc");
            webGallery.addImage(image, exp.getName(), exp.getDescription(), df.format(exp.getSampleDate()));
        }
        // Add Galery to panel
        galeryComponent = webGallery.getView(false);
        galeryComponent.setLocation(0, 0);
        galeryComponent.setSize(this.getSize());
        galeryComponent.requestFocus();
        this.add(galeryComponent, JLayeredPane.DEFAULT_LAYER);
    }
}