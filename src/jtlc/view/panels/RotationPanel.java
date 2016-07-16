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

import com.alee.extended.image.WebDecoratedImage;
import com.alee.extended.panel.GroupPanel;
import com.alee.laf.button.WebButton;
import com.alee.laf.label.WebLabel;
import com.alee.laf.toolbar.ToolbarStyle;
import com.alee.laf.toolbar.WebToolBar;
import com.alee.managers.language.data.TooltipWay;
import com.alee.managers.popup.PopupWay;
import com.alee.managers.popup.WebButtonPopup;
import com.alee.managers.tooltip.TooltipManager;
import jtlc.assets.Assets;
import com.alee.laf.scroll.WebScrollPane;
import com.alee.laf.spinner.WebSpinner;
import com.alee.laf.text.WebEditorPane;
import com.alee.laf.text.WebTextArea;
import com.alee.managers.tooltip.WebCustomTooltip;
import ij.ImagePlus;
import java.awt.*;
import java.awt.event.ActionEvent;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import jtlc.core.processing.AnalysisProcessing.Axis;
import jtlc.core.processing.ImageProcessing;
import java.awt.event.ActionListener;
import javax.swing.event.ChangeListener;
import jtlc.view.dto.AbstractDTO;
import jtlc.view.panels.dto.RotationDTO;
import jtlc.view.components.slider.CustomSliderUI;
import jtlc.view.components.slider.CustomSlider;

/**
 * Image Rotation Panel
 * Implements JLayeredPane that allows rotate samples image.
 * 
 * @author Cristian Tardivo
 */
public class RotationPanel extends JLayeredPane implements IPanel {
    // Components
    private final CustomSlider<Character> sliderH, sliderV;
    private WebSpinner rotSpinner;
    private final WebToolBar commandsTB;
    private final WebTextArea textArea;
    private ImagePlus samplesImg, resizedImg, oldResizedImg;
    private Axis flipAxis;
    private Dimension prefSize;
    private final WebDecoratedImage bgImage;
    private boolean panelUpdate = false;
    // Panel DTO
    private RotationDTO data;
    
    /**
     * Create RotationPanel
     * @param dto RotationPanel Data-Transfer-Object
     * @param size Initial Panel Size
     */
    public RotationPanel(RotationDTO dto, Dimension size) {
        //save Params
        data = dto;
        samplesImg = data.getImage();
        prefSize = size;
        // Resize Image Resized
        resizedImg = samplesImg.duplicate();
        ImageProcessing.resizeImage(resizedImg, -1, prefSize.height - 60);
        if (resizedImg.getWidth() > size.width - 200)
            ImageProcessing.resizeImage(resizedImg, size.width - 200, -1);
        oldResizedImg = resizedImg.duplicate();
        /**
         * Apply old rotation (image distorsion fix)
         */
        flipAxis = data.getFlipAxis();
        resizedImg = ImageProcessing.flipImage(resizedImg, flipAxis);
        oldResizedImg = resizedImg.duplicate();
        resizedImg = ImageProcessing.rotateImage(resizedImg, data.getRotationAngle(), true);
        ImageProcessing.resizeImage(resizedImg, -1, prefSize.height - 60);
        if (resizedImg.getWidth() > size.width - 200)
            ImageProcessing.resizeImage(resizedImg, size.width - 200, -1);
        // Init Background Image
        bgImage = new WebDecoratedImage(resizedImg.getImage());
        bgImage.setSize(resizedImg.getWidth() + 4, resizedImg.getHeight() + 4);
        bgImage.setRound(0);
        bgImage.setDrawGlassLayer(false);
        bgImage.setDrawBorder(false);
        bgImage.setShadeWidth(4);
        // Init Horizontal MultiThumbSlider
        Character chrs[] = new Character[]{'A'};
        float positionsX[] = new float[]{0};
        sliderH = new CustomSlider<>(CustomSlider.HORIZONTAL, positionsX, chrs);
        sliderH.setThumbShape(CustomSliderUI.Thumb.Triangle);
        //
        sliderH.setThumbOverlap(false);
        sliderH.setThumbRemovalAllowed(false);
        sliderH.setMouseThumbRemovalAllowed(false);
        sliderH.setAutoAdding(false);
        sliderH.setPaintTicks(true);
        sliderH.setVisible(true);
        sliderH.setPaintSquares(false);
        sliderH.setPaintNumbers(false);
        sliderH.setCollisionPolicy(CustomSlider.Collision.NUDGE_OTHER);
        sliderH.setMinimumThumbnailCount(1);
        sliderH.setSelectedThumb(-1);
        // Multiplier Value
        sliderH.setMultValue(samplesImg.getWidth());
        // Slider Size
        sliderH.setSize(resizedImg.getWidth() + 44, resizedImg.getHeight() + 70);
        sliderH.setTickLength(resizedImg.getHeight() + 25);
        sliderH.setTickStart(-3);
        sliderH.setSquareLength(resizedImg.getHeight() + 25);
        sliderH.setSquareStart(40);
        // Init MultiThumbSlider Vertical
        Character chrs2[] = new Character[]{'A'};
        float positionsY[] = new float[]{0};
        sliderV = new CustomSlider<>(CustomSlider.VERTICAL, positionsY, chrs2);
        sliderV.setThumbShape(CustomSliderUI.Thumb.Triangle);
        //
        sliderV.setThumbOverlap(true);
        sliderV.setThumbRemovalAllowed(true);
        sliderV.setMouseThumbRemovalAllowed(false);
        sliderV.setAutoAdding(false);
        sliderV.setPaintTicks(true);
        sliderV.setPaintNumbers(false);
        sliderV.setVisible(true);
        sliderV.setPaintSquares(false);
        sliderV.setInverted(true);
        sliderV.setCollisionPolicy(CustomSlider.Collision.STOP_AGAINST);
        sliderV.setMinimumThumbnailCount(1);
        sliderV.setSelectedThumb(-1);
        // Multiplier Value
        sliderV.setMultValue(samplesImg.getHeight());
        // Slider Size
        sliderV.setSize(resizedImg.getWidth() + 70, resizedImg.getHeight() + 43);
        sliderV.setTickLength(resizedImg.getWidth() + 35);
        sliderV.setTickStart(-3);
        sliderV.setSquareLength(resizedImg.getWidth() + 35);
        sliderV.setSquareStart(16);
        // Rotation Spinner
        rotSpinner = new WebSpinner(new SpinnerNumberModel((double)data.getRotationAngle(), -359.9d, 359.9d, 0.1d));
        rotSpinner.addChangeListener(changeListener);
        //
        WebButton rotateLeft = new WebButton(Assets.loadIcon("ic_rotate_left"));
        WebCustomTooltip rotlTooltip = TooltipManager.setTooltip(rotateLeft, Assets.getString("ROTATE_TO_LEFT"), TooltipWay.right, 100);
        Assets.associateComponent(rotlTooltip, "setTooltip", "ROTATE_TO_LEFT");
        rotateLeft.setRound(2);
        rotateLeft.setActionCommand("ROT_LEFT");
        rotateLeft.addActionListener(actionListener);
        //
        WebButton rotateRight = new WebButton(Assets.loadIcon("ic_rotate_right"));
        WebCustomTooltip rotrTooltip = TooltipManager.setTooltip(rotateRight, Assets.getString("ROTATE_TO_RIGHT"), TooltipWay.right, 100);
        Assets.associateComponent(rotrTooltip, "setTooltip", "ROTATE_TO_RIGHT");
        rotateRight.setRound(2);
        rotateRight.setActionCommand("ROT_RIGHT");
        rotateRight.addActionListener(actionListener);
        //
        WebButton rotateFlipH = new WebButton(Assets.loadIcon("ic_flip_h"));
        WebCustomTooltip fliphTooltip = TooltipManager.setTooltip(rotateFlipH, Assets.getString("INVERT_HORIZONTAL"), TooltipWay.right, 100);
        Assets.associateComponent(fliphTooltip, "setTooltip", "INVERT_HORIZONTAL");
        rotateFlipH.setRound(2);
        rotateFlipH.setActionCommand("FLIP_H");
        rotateFlipH.addActionListener(actionListener);
        //
        WebButton rotateFlipV = new WebButton(Assets.loadIcon("ic_flip_v"));
        WebCustomTooltip flipvTooltip = TooltipManager.setTooltip(rotateFlipV, Assets.getString("INVERT_VERTICAL"), TooltipWay.right, 100);
        Assets.associateComponent(flipvTooltip, "setTooltip", "INVERT_VERTICAL");
        rotateFlipV.setRound(2);
        rotateFlipV.setActionCommand("FLIP_V");
        rotateFlipV.addActionListener(actionListener);
        //
        WebButton resetImage = new WebButton(Assets.loadIcon("ic_refresh"));
        WebCustomTooltip resetTooltip = TooltipManager.setTooltip(resetImage, Assets.getString("RESET_TO_DEFAULT"), TooltipWay.right, 100);
        Assets.associateComponent(resetTooltip, "setTooltip", "RESET_TO_DEFAULT");
        resetImage.setRound(2);
        resetImage.setActionCommand("RESET_IMG");
        resetImage.addActionListener(actionListener);
        // Button that calls for popup
        WebButton showSpinner = new WebButton(Assets.loadIcon("ic_rotate"));
        WebCustomTooltip rotationTooltip = TooltipManager.setTooltip(showSpinner, Assets.getString("SHOW_ROTATION_ANGLE"), TooltipWay.right, 100);
        Assets.associateComponent(rotationTooltip, "setTooltip", "SHOW_ROTATION_ANGLE");
        showSpinner.setRound(2);
        // Popup itself
        WebButtonPopup popup = new WebButtonPopup(showSpinner, PopupWay.rightUp);
        // Rotation popup content
        WebLabel label = new WebLabel(Assets.getString("ROTATION_ANGLE"), WebLabel.CENTER);
        Assets.associateComponent(label, "setText", "ROTATION_ANGLE");
        GroupPanel content = new GroupPanel(5, false, label, rotSpinner);
        content.setMargin(10);
        // Setup popup content
        popup.setContent(content);
        // Component focused by default
        popup.setDefaultFocusComponent(rotSpinner);
        // Comments Pop-up
        WebButton showComments = new WebButton(Assets.loadIcon("ic_dialog"));
        WebCustomTooltip commentsTooltip = TooltipManager.setTooltip(showComments, Assets.getString("SHOW_COMMENTS"), TooltipWay.right, 100);
        Assets.associateComponent(commentsTooltip, "setTooltip", "SHOW_COMMENTS");
        showComments.setRound(2);
        textArea = new WebTextArea();
        textArea.setText(data.getComments());
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        WebScrollPane areaScroll = new WebScrollPane(textArea);
        areaScroll.setPreferredSize(new Dimension(220, 140));
        WebButtonPopup commentPopup = new WebButtonPopup(showComments, PopupWay.rightCenter);
        WebLabel comentLabel = new WebLabel(Assets.getString("COMMENTS"), WebLabel.CENTER);
        Assets.associateComponent(comentLabel, "setText", "COMMENTS");
        GroupPanel commentContent = new GroupPanel(10, false, comentLabel, areaScroll);
        commentContent.setMargin(10);
        commentPopup.setContent(commentContent);
        commentPopup.setDefaultFocusComponent(areaScroll);
        // Help Button
        WebButton showHelp = new WebButton(Assets.loadIcon("ic_help"));
        WebCustomTooltip helpTooltip = TooltipManager.setTooltip(showHelp, Assets.getString("SHOW_HELP"), TooltipWay.right, 100);
        Assets.associateComponent(helpTooltip, "setTooltip", "SHOW_HELP");
        showHelp.setRound(2);
        WebEditorPane editorPane = new WebEditorPane("text/html", Assets.getString("ROTATION_HELP"));
        Assets.associateComponent(editorPane, "setText", "ROTATION_HELP");
        editorPane.setEditable(false);
        editorPane.setFocusable(false);
        WebScrollPane editorPaneScroll = new WebScrollPane(editorPane);
        editorPaneScroll.setPreferredSize(new Dimension(300, 200));
        editorPaneScroll.setFocusable(false);
        WebButtonPopup helpPopup = new WebButtonPopup(showHelp, PopupWay.rightUp);
        WebLabel helpLabel = new WebLabel(Assets.getString("HELP"), WebLabel.CENTER);
        Assets.associateComponent(helpLabel, "setText", "HELP");
        GroupPanel helpContent = new GroupPanel(10, false, helpLabel, editorPaneScroll);
        helpContent.setMargin(10);
        helpPopup.setContent(helpContent);
        // Commands Toolbar
        commandsTB = new WebToolBar(WebToolBar.VERTICAL);
        commandsTB.setFloatable(false);
        commandsTB.setToolbarStyle(ToolbarStyle.standalone);
        commandsTB.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        commandsTB.add(showSpinner);
        commandsTB.add(rotateLeft);
        commandsTB.add(rotateRight);
        commandsTB.add(rotateFlipH);
        commandsTB.add(rotateFlipV);
        commandsTB.add(resetImage);
        commandsTB.add(showComments);
        commandsTB.add(showHelp);
        commandsTB.setSize(50, 294);
        // Set Main Panel Size
        super.setSize(prefSize);
        // Set Components Position
        bgImage.setLocation(prefSize.width / 2 - resizedImg.getWidth() / 2, (prefSize.height / 2 - resizedImg.getHeight() / 2) + 15);
        sliderH.setLocation(bgImage.getLocation().x - 20, bgImage.getLocation().y - 40);
        sliderV.setLocation(bgImage.getLocation().x - 45, bgImage.getLocation().y - 20);
        commandsTB.setLocation(5, prefSize.height / 2 - commandsTB.getSize().height / 2);
        // Add Components
        this.add(bgImage, FRAME_CONTENT_LAYER);
        this.add(sliderH, DEFAULT_LAYER);
        this.add(sliderV, DEFAULT_LAYER);
        this.add(commandsTB, PALETTE_LAYER);
    }
  
    /**
     * Resize Panel Components and change location
     * @param size new panel size
     */
    @Override
    public void setSize(Dimension size) {
        // Avoid set size immediately after panel update
        if (panelUpdate) {
            panelUpdate = false;
            return;
        }
        // Set panel size
        prefSize = size;
        // Update Background Image Size 
        resizedImg = samplesImg.duplicate();
        // Resize Image
        resizedImg = samplesImg.duplicate();
        ImageProcessing.resizeImage(resizedImg, -1, prefSize.height - 60);
        if (resizedImg.getWidth() > size.width - 200)
            ImageProcessing.resizeImage(resizedImg, size.width - 200, -1);
        oldResizedImg = resizedImg.duplicate();
        // Flip Image
        resizedImg = ImageProcessing.flipImage(oldResizedImg, getFlipAxis());
        oldResizedImg = resizedImg.duplicate();
        // Rotate old resized image
        resizedImg = ImageProcessing.rotateImage(oldResizedImg, getRotationAngle(), true);
        // Resize image if image canvas has resized
        ImageProcessing.resizeImage(resizedImg, -1, prefSize.height - 60);
        if (resizedImg.getWidth() > size.width - 200)
            ImageProcessing.resizeImage(resizedImg, size.width - 200, -1);
        // Update main panel size
        super.setSize(prefSize.width, prefSize.height);
        // Re-init Elements Size & Positions
        updateSizeAndPosition();
    }
    
    /**
     * Get panel changes (flip and rotation angle)
     * @return RotationPanel Data-Transfer-Object
     */
    @Override
    public RotationDTO getResults() {
        // Check for changes and update values if changed
        if (data.getFlipAxis() != getFlipAxis() || data.getRotationAngle() != getRotationAngle()) {
            data.setChanged(true);
            data.setFlipAxis(getFlipAxis());
            data.setRotationAngle(getRotationAngle());
        }
        // Comments not count like a change
        data.setComments(textArea.getText());
        return data;
    }
    
    /**
     * Update panel components to new values in dto
     * @param dto Panel Data-Transfer-Object
     */
    @Override
    public void updatePanel(AbstractDTO dto) {
        // Check for valid dto
        if (!(dto instanceof RotationDTO))
            return;
        // set panel update
        panelUpdate = true;
        // save data
        data = (RotationDTO) dto;
        // Update components
        textArea.setText(data.getComments());
        //
        samplesImg = data.getImage();
        prefSize = this.getSize();
        Dimension size = this.getSize();
        // Resize Image Resized
        resizedImg = samplesImg.duplicate();
        ImageProcessing.resizeImage(resizedImg, -1, prefSize.height - 60);
        if (resizedImg.getWidth() > size.width - 200)
            ImageProcessing.resizeImage(resizedImg, size.width - 200, -1);
        oldResizedImg = resizedImg.duplicate();
        /**
         * Apply old rotation (image distorsion fix)
         */
        flipAxis = data.getFlipAxis();
        resizedImg = ImageProcessing.flipImage(resizedImg, flipAxis);
        oldResizedImg = resizedImg.duplicate();
        resizedImg = ImageProcessing.rotateImage(resizedImg, data.getRotationAngle(), true);
        ImageProcessing.resizeImage(resizedImg, -1, prefSize.height - 60);
        if (resizedImg.getWidth() > size.width - 200)
            ImageProcessing.resizeImage(resizedImg, size.width - 200, -1);
        // RotSpinner value
        rotSpinner.removeChangeListener(changeListener);
        rotSpinner.setValue(data.getRotationAngle());
        rotSpinner.addChangeListener(changeListener);
        // Reset Sliders
        Character chrs[] = new Character[]{'A'};
        float positionsX[] = new float[]{0};
        sliderH.setValues(positionsX, chrs);
        Character chrs2[] = new Character[]{'A'};
        float positionsY[] = new float[]{0};
        sliderV.setValues(positionsY, chrs2);
        // Update Sizes and Positions
        updateSizeAndPosition();
    }
        
    /**
     * Update Elemnts Size and Positions.
     */
    private void updateSizeAndPosition() {
        // Re-init Background Image
        bgImage.setImage(resizedImg.getImage());
        bgImage.setSize(resizedImg.getWidth() + 4, resizedImg.getHeight() + 4);
        bgImage.setLocation(prefSize.width / 2 - resizedImg.getWidth() / 2, (prefSize.height / 2 - resizedImg.getHeight() / 2) + 15);
        // Update Sliders Size And Positions
        sliderV.setTickLength(resizedImg.getWidth() + 35);
        sliderV.setSize(resizedImg.getWidth() + 70, resizedImg.getHeight() + 43);
        sliderV.setLocation(bgImage.getLocation().x - 45, bgImage.getLocation().y - 20);
        sliderH.setSize(resizedImg.getWidth() + 43, resizedImg.getHeight() + 70);
        sliderH.setTickLength(resizedImg.getHeight() + 25);
        sliderH.setLocation(bgImage.getLocation().x - 20, bgImage.getLocation().y - 40);        
        // Update Commands Buttons Location
        commandsTB.setLocation(5, prefSize.height / 2 - commandsTB.getSize().height / 2);
        // Re-Paint panel
        this.revalidate();
        this.repaint();
    }
    
    /**
     * Spinner Change Listener.
     */
    private final ChangeListener changeListener = (ChangeEvent e) -> {
        // Rotate old resized image
        resizedImg = ImageProcessing.rotateImage(oldResizedImg, getRotationAngle(), true);
        // Resize image if image canvas has resized
        ImageProcessing.resizeImage(resizedImg, -1, prefSize.height - 60);
        // Update Panel Components
        updateSizeAndPosition();
    };
  
    /**
     * Buttons Actions Listener.
     */
    private final ActionListener actionListener = (ActionEvent e) -> {
        // Rotate to Left
        if (e.getActionCommand().equals("ROT_LEFT")) {
            rotSpinner.setValue((getRotationAngle() - 90.0d) % 360.0d);
        }
        // Rotate to Right
        if (e.getActionCommand().equals("ROT_RIGHT")) {
            rotSpinner.setValue((getRotationAngle() + 90.0d) % 360.0d);
        }
        // Flip Horizontal
        if (e.getActionCommand().equals("FLIP_H")) {
            // Flip Image
            resizedImg = ImageProcessing.flipImage(oldResizedImg, Axis.AXIS_X);
            oldResizedImg = resizedImg.duplicate();
            // Update Rotation
            rotSpinner.setValue(-getRotationAngle());
            // Save Flip result
            setFlipAxis(Axis.AXIS_X);
            // Update view
            updateSizeAndPosition();
        }
        // Flip Vertical
        if (e.getActionCommand().equals("FLIP_V")) {
            // Flip Image
            resizedImg = ImageProcessing.flipImage(oldResizedImg, Axis.AXIS_Y);
            oldResizedImg = resizedImg.duplicate();
            // Update Rotation
            rotSpinner.setValue(-getRotationAngle());
            // Save Flip result
            setFlipAxis(Axis.AXIS_Y);
            // Update view
            updateSizeAndPosition();
        }
        // Reset Image
        if (e.getActionCommand().equals("RESET_IMG")) {
            // Reset Flip
            setFlipAxis(Axis.NONE);
            // Reset Rotation
            rotSpinner.removeChangeListener(changeListener);
            rotSpinner.setValue(0.0);
            rotSpinner.addChangeListener(changeListener);
            // Re-init Images
            resizedImg = samplesImg.duplicate();
            ImageProcessing.resizeImage(resizedImg, -1, prefSize.height - 60);
            oldResizedImg = resizedImg.duplicate();
            // Update view
            updateSizeAndPosition();
        }
    };
    
    /**
     * Get selected rotation angle
     * @return rotation angle in degrees (Double)
     */
    private double getRotationAngle() {
        return (Double)rotSpinner.getValue();
    }
    
    /**
     * Get current image flip
     * @return flip transforms (Axis combinations)
     */
    private Axis getFlipAxis() {
        return flipAxis;
    } 
    
    /**
     * Set Correct flip axis result
     * @param axis new flip
     */
    private void setFlipAxis(Axis axis) {
        if (flipAxis == Axis.NONE || axis == Axis.NONE) {
            flipAxis = axis;
            return;
        }
        if (flipAxis == Axis.AXIS_X && axis == Axis.AXIS_X) {
            flipAxis = Axis.NONE;
            return;
        }
        if (flipAxis == Axis.AXIS_Y && axis == Axis.AXIS_Y) {
            flipAxis = Axis.NONE;
            return;
        }
        if (flipAxis == Axis.AXIS_X && axis == Axis.AXIS_Y) {
            flipAxis = Axis.AXIS_XY;
            return;
        }
        if (flipAxis == Axis.AXIS_Y && axis == Axis.AXIS_X) {
            flipAxis = Axis.AXIS_YX;
            return;
        }
        if (flipAxis == Axis.AXIS_XY && axis == Axis.AXIS_X) {
            flipAxis = Axis.AXIS_Y;
            return;
        }
        if (flipAxis == Axis.AXIS_YX && axis == Axis.AXIS_Y) {
            flipAxis = Axis.AXIS_X;
        }
    }
}