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
package jtlc.view.dialogs;

import com.alee.extended.image.WebDecoratedImage;
import com.alee.extended.panel.GroupPanel;
import com.alee.extended.panel.GroupingType;
import com.alee.laf.button.WebButton;
import com.alee.laf.label.WebLabel;
import com.alee.laf.spinner.WebSpinner;
import com.alee.managers.hotkey.ButtonHotkeyRunnable;
import com.alee.managers.hotkey.Hotkey;
import com.alee.managers.hotkey.HotkeyManager;
import com.alee.managers.language.data.TooltipWay;
import com.alee.managers.tooltip.TooltipManager;
import ij.ImagePlus;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import jtlc.assets.Assets;
import jtlc.core.processing.ImageProcessing;
import jtlc.view.dialogs.dto.ImageExportDTO;

/**
 * Project Images Export Dialog.
 * @author Cristian Tardivo
 */
public class ImageExportDialog extends JDialog implements IDialog {
    private boolean result;
    private WebSpinner widthSpinner;
    private WebSpinner heightSpinner;
    private final ImageExportDTO data;
    
    /**
     * Create project data edit dialog and init components
     * @param parent dialog parent/owner frame
     * @param dto data edit dialog dto
     */
    public ImageExportDialog(JFrame parent, ImageExportDTO dto) {
        super(parent, Assets.getString("IMAGE_EXPORT"), true);
        data = dto;
        initComponents();
    }

    /**
     * Init Dialog Components.
     */
    private void initComponents() {
        // Container/Layaout
        Container container = getContentPane();
        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints cns = new GridBagConstraints();
        Insets insetsLeft = new Insets(5, 10, 5, 3);
        Insets insetsRigth = new Insets(5, 3, 5, 10);
        Insets insetsDouble = new Insets(5, 5, 5, 5);
        // Set Layout
        container.setLayout(layout);
        /********************
         * Create Components
         ********************/
        // Labels
        WebLabel settingsLabel = new WebLabel(Assets.getString("IMAGE_SIZE") + ":", WebLabel.CENTER);
        WebLabel widthLabel = new WebLabel(Assets.getString("WIDTH") + ":", WebLabel.RIGHT);
        WebLabel heigthLabel = new WebLabel(Assets.getString("HEIGHT") + ":", WebLabel.RIGHT);
        WebLabel previewLabel = new WebLabel(Assets.getString("IMAGE_PREVIEW") + ":",  WebLabel.LEFT);
        previewLabel.setPreferredWidth(250);
        // Width Spinner
        widthSpinner = new WebSpinner(new SpinnerNumberModel((int)data.getWidth(), 1, Integer.MAX_VALUE, 1));
        // Height Spinner
        heightSpinner = new WebSpinner(new SpinnerNumberModel((int)data.getHeight(), 1, Integer.MAX_VALUE, 1));
        // preserve proportion if necessary
        if (data.isLinked()) {
            // Listen to spinner changes
            ChangeListener changeListener = (ChangeEvent e) -> {
                if (e.getSource() == widthSpinner) {
                    ChangeListener listener = heightSpinner.getChangeListeners()[0];
                    heightSpinner.removeChangeListener(listener);
                    heightSpinner.setValue((int)((int)widthSpinner.getValue() * ((double) data.getHeight() / data.getWidth())));
                    heightSpinner.addChangeListener(listener);
                }
                if (e.getSource() == heightSpinner) {                    
                    ChangeListener listener = widthSpinner.getChangeListeners()[0];
                    widthSpinner.removeChangeListener(listener);
                    widthSpinner.setValue((int)((int)heightSpinner.getValue() * ((double)data.getWidth() / data.getHeight())));
                    widthSpinner.addChangeListener(listener);
                }
            };
            // Init listeners
            widthSpinner.addChangeListener(changeListener);
            heightSpinner.addChangeListener(changeListener);
        }
        
        
        // Preview Image
        ImagePlus preview = data.getPreview();
        if (preview.getWidth() > 600)
            ImageProcessing.resizeImage(preview, 600, -1);
        if (preview.getHeight() > 400)
            ImageProcessing.resizeImage(preview, -1, 400);
        WebDecoratedImage previewImage = new WebDecoratedImage(preview.getImage());
        previewImage.setRound(0);
        previewImage.setDrawGlassLayer(false);
        previewImage.setDrawBorder(false);
        previewImage.setShadeWidth(4);
        // Accept Button
        WebButton accept = new WebButton(Assets.getString("EXPORT"));
        accept.addActionListener(acceptListener);
        HotkeyManager.registerHotkey(this, accept, Hotkey.ENTER, new ButtonHotkeyRunnable(accept, 150), TooltipWay.trailing);
        TooltipManager.setTooltip(accept, Assets.getString("EXPORT_TO_IMAGE"), TooltipWay.up, 200);
        // Cancel Button
        WebButton cancel = new WebButton(Assets.getString("CANCEL"));
        cancel.addActionListener(cancelListener);
        HotkeyManager.registerHotkey(this, cancel, Hotkey.ESCAPE, new ButtonHotkeyRunnable(cancel, 150), TooltipWay.trailing);
        TooltipManager.setTooltip(cancel, Assets.getString("CANCEL_PROCESS"), TooltipWay.up, 200);
        /*****************
         * Add Components
         *****************/
        // Init constrains
        cns.fill = GridBagConstraints.HORIZONTAL;
        // Preview Label
        cns.gridx = 0; cns.gridy = 0; cns.insets = insetsLeft; cns.gridwidth = 2;
        container.add(previewLabel, cns);
        // Preview Image
        cns.gridx = 0; cns.gridy = 1; cns.insets = insetsLeft; cns.gridwidth = 1; cns.gridheight = 1;
        container.add(previewImage, cns);
        // Settings Label
        cns.gridx = 1; cns.gridy = 0; cns.insets = insetsLeft; cns.gridwidth = 1;
        container.add(settingsLabel, cns);
        // Commands Panel
        JPanel panel = new JPanel(new GridBagLayout());
        // Width Label
        cns.gridx = 0; cns.gridy = 1; cns.insets = insetsLeft; cns.gridwidth = 1;
        panel.add(widthLabel, cns);
        // Width Spinner
        cns.gridx = 1; cns.gridy = 1; cns.insets = insetsRigth; cns.gridwidth = 1;
        panel.add(widthSpinner, cns);
        // Heigth Label
        cns.gridx = 0; cns.gridy = 2; cns.insets = insetsLeft; cns.gridwidth = 1;
        panel.add(heigthLabel, cns);
        // Heigth Spinner
        cns.gridx = 1; cns.gridy = 2; cns.insets = insetsRigth; cns.gridwidth = 1;
        panel.add(heightSpinner, cns);
        // Accept/Cancel button
        cns.gridx = 0; cns.gridy = 3; cns.gridwidth = 2; cns.weighty = 1; cns.insets = insetsDouble; cns.anchor = GridBagConstraints.SOUTH; cns.fill = GridBagConstraints.HORIZONTAL;
        panel.add(new GroupPanel(GroupingType.fillAll, 4, true, accept, cancel), cns);
        // Commands Panel
        cns.gridx = 1; cns.gridy = 1; cns.fill = GridBagConstraints.VERTICAL;
        container.add(panel, cns);
        // Dialog Icon
        setIconImage(Assets.loadImage("ic_export_image"));
        // Dialog size
        setResizable(false);
        pack();
        setLocationRelativeTo(getOwner());
        setVisible(true);
    }
    
    /**
     * Cancel Button Listener
     */
    ActionListener cancelListener = (ActionEvent e) -> {
        result = false;
        dispose();
    };
    
    /**
     * Accept Button Listener
     */
    ActionListener acceptListener = (ActionEvent e) -> {
        boolean valid = true;
        // Check width
        int width = (int) widthSpinner.getValue();
        if (width < 1 || width > 2000) {
            TooltipManager.showOneTimeTooltip(widthSpinner, null, "<html><center><font color=orange>"+ Assets.getString("INVALID_WIDTH") +"</font></center></html>", TooltipWay.leading);
            valid = false;
        }
        // Check heigth
        int heigth = (int) heightSpinner.getValue();
        if (heigth < 1 || heigth > 2000) {
            TooltipManager.showOneTimeTooltip(heightSpinner, null, "<html><center><font color=orange>"+ Assets.getString("INVALID_HEIGTH") +"</font></center></html>", TooltipWay.leading);
            valid = false;
        }
        // If valid data close dialog
        if (valid) {
            result = true;
            dispose();
        }
    };
    
    /**
     * Get Dialog Result DTO
     * @return DataDTO with changes 
     */
    @Override
    public ImageExportDTO getResults() {
        // If accept return dto with data and mark as changed (if necessary)
        if (result) {
            data.setWidth((int)widthSpinner.getValue());
            data.setHeight((int)heightSpinner.getValue());
            data.setChanged(true);
            return data;
        }
        // if cancel process return empty dto mark as not changed
        return data;
    }
}