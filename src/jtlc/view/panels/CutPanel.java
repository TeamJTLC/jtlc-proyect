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
import com.alee.laf.scroll.WebScrollPane;
import com.alee.laf.text.WebEditorPane;
import com.alee.laf.text.WebTextArea;
import com.alee.laf.toolbar.ToolbarStyle;
import com.alee.laf.toolbar.WebToolBar;
import com.alee.managers.language.data.TooltipWay;
import com.alee.managers.popup.PopupWay;
import com.alee.managers.popup.WebButtonPopup;
import com.alee.managers.tooltip.TooltipManager;
import com.alee.managers.tooltip.WebCustomTooltip;
import ij.ImagePlus;
import java.awt.*;
import java.util.Arrays;
import javax.swing.*;
import jtlc.main.common.Point;
import jtlc.assets.Assets;
import jtlc.core.processing.ImageProcessing;
import jtlc.view.dto.AbstractDTO;
import jtlc.view.panels.dto.CuttingDTO;
import jtlc.view.components.slider.CustomSlider;
import jtlc.view.components.slider.CustomSliderUI;

/**
 * Image Cut Panel
 * Implements JLayeredPane with two sliders for image crop.
 * 
 * @author Cristian Tardivo
 */
public class CutPanel extends JLayeredPane implements IPanel {
    // Panel Componets
    private ImagePlus samplesImg;
    private ImagePlus resizedImg;
    private final CustomSlider<Character> sliderH, sliderV;
    private final WebDecoratedImage bgImage;
    private final WebToolBar commandsTB;
    private final WebTextArea textArea;
    // Panel DTO
    private CuttingDTO data;
    
    /**
     * Create CutPanel
     * @param dto CutPanel Data-Transfer-Object
     * @param size Initial Panel Size
     */
    public CutPanel(CuttingDTO dto, Dimension size) {
        // Save params
        data = dto;
        samplesImg = dto.getImage();
        // H positions
        float x1 = (float) dto.getUpperPoint().getX() / (float) samplesImg.getWidth();
        float x2 = (float) dto.getLowerPoint().getX() / (float) samplesImg.getWidth();
        // V positions
        float y1 = (float) dto.getUpperPoint().getY() / (float) samplesImg.getHeight();
        float y2 = (float) dto.getLowerPoint().getY() / (float) samplesImg.getHeight();
        // Array float positions
        float[] positionsH = new float[]{x1,x2};
        float[] positionsV = new float[]{y1,y2};
        // Check positions
        if (!checkArray(positionsH))
            positionsH = new float[]{0,1};
        if (!checkArray(positionsV))
            positionsV = new float[]{0,1};
        // Resize Image Resized
        resizedImg = samplesImg.duplicate();
        ImageProcessing.resizeImage(resizedImg, -1, size.height - 60);
        if (resizedImg.getWidth() > size.width - 100)
            ImageProcessing.resizeImage(resizedImg, size.width - 100, -1);
        // Init Background Image
        bgImage = new WebDecoratedImage(resizedImg.getImage());
        bgImage.setSize(resizedImg.getWidth() + 4, resizedImg.getHeight() + 4);
        bgImage.setRound(0);
        bgImage.setDrawGlassLayer(false);
        bgImage.setDrawBorder(false);
        bgImage.setShadeWidth(4);
        // Init Horizontal MultiThumbSlider
        Character chrs[] = new Character[positionsH.length];
        Arrays.fill(chrs, 'A');
        sliderH = new CustomSlider<>(CustomSlider.HORIZONTAL, positionsH, chrs);
        sliderH.setThumbShape(CustomSliderUI.Thumb.Triangle);
        // Init Default Slider Options
        sliderH.setSelectedThumb(-1);
        sliderH.setThumbOverlap(false);
        sliderH.setThumbRemovalAllowed(false);
        sliderH.setMouseThumbRemovalAllowed(false);
        sliderH.setAutoAdding(false);
        sliderH.setPaintTicks(true);
        sliderH.setVisible(true);
        sliderH.setPaintSquares(false);
        sliderH.setPaintNumbers(false);
        sliderH.setCollisionPolicy(CustomSlider.Collision.NUDGE_OTHER);
        sliderH.setMinimumThumbnailCount(2);
        // Multiplier Value
        sliderH.setMultValue(samplesImg.getWidth());
        // Slider Size
        sliderH.setSize(resizedImg.getWidth() + 43, resizedImg.getHeight() + 70);
        sliderH.setTickLength(resizedImg.getHeight() + 25);
        sliderH.setTickStart(-3);
        sliderH.setSquareLength(resizedImg.getHeight() + 25);
        sliderH.setSquareStart(40);
        // Init Vertical MultiThumbSlider
        Character chrs2[] = new Character[positionsV.length];
        Arrays.fill(chrs2, 'A');
        sliderV = new CustomSlider<>(CustomSlider.VERTICAL, positionsV, chrs2);
        sliderV.setThumbShape(CustomSliderUI.Thumb.Triangle);
        // Init Default Slider Options
        sliderV.setSelectedThumb(-1);
        sliderV.setThumbOverlap(false);
        sliderV.setThumbRemovalAllowed(false);
        sliderV.setMouseThumbRemovalAllowed(false);
        sliderV.setAutoAdding(false);
        sliderV.setPaintTicks(true);
        sliderV.setPaintNumbers(false);
        sliderV.setVisible(true);
        sliderV.setPaintSquares(false);
        sliderV.setInverted(true);
        sliderV.setCollisionPolicy(CustomSlider.Collision.STOP_AGAINST);
        sliderV.setMinimumThumbnailCount(2);
        // Multiplier Value
        sliderV.setMultValue(samplesImg.getHeight());
        // Slider Size
        sliderV.setSize(resizedImg.getWidth() + 70, resizedImg.getHeight() + 43);
        sliderV.setTickLength(resizedImg.getWidth() + 35);
        sliderV.setTickStart(-3);
        sliderV.setSquareLength(resizedImg.getWidth() + 35);
        sliderV.setSquareStart(16);
        // Comments Pop-up
        WebButton showComments = new WebButton(Assets.loadIcon("ic_dialog"));
        WebCustomTooltip commentsTooltip = TooltipManager.setTooltip(showComments, Assets.getString("SHOW_COMMENTS"), TooltipWay.left, 100);
        Assets.associateComponent(commentsTooltip, "setTooltip", "SHOW_COMMENTS");
        textArea = new WebTextArea();
        textArea.setText(data.getComments());
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        WebScrollPane areaScroll = new WebScrollPane(textArea);
        areaScroll.setPreferredSize(new Dimension(220, 140));
        WebButtonPopup commentPopup = new WebButtonPopup(showComments, PopupWay.leftUp);
        WebLabel comentLabel = new WebLabel(Assets.getString("COMMENTS"), WebLabel.CENTER);
        Assets.associateComponent(comentLabel, "setText", "COMMENTS");
        GroupPanel commentContent = new GroupPanel(10, false, comentLabel, areaScroll);
        commentContent.setMargin(10);
        commentPopup.setContent(commentContent);
        commentPopup.setDefaultFocusComponent(areaScroll);
        // Help Button
        WebButton showHelp = new WebButton(Assets.loadIcon("ic_help"));
        WebCustomTooltip helpTooltip = TooltipManager.setTooltip(showHelp, Assets.getString("SHOW_HELP"), TooltipWay.left, 100);
        Assets.associateComponent(helpTooltip, "setTooltip", "SHOW_HELP");
        WebEditorPane editorPane = new WebEditorPane("text/html", Assets.getString("CUT_HELP"));
        Assets.associateComponent(editorPane, "setText", "CUT_HELP");
        editorPane.setEditable(false);
        editorPane.setFocusable(false);
        WebScrollPane editorPaneScroll = new WebScrollPane(editorPane);
        editorPaneScroll.setPreferredSize(new Dimension(300, 200));
        editorPaneScroll.setFocusable(false);
        WebButtonPopup helpPopup = new WebButtonPopup(showHelp, PopupWay.leftUp);
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
        commandsTB.add(showHelp);
        commandsTB.add(showComments);
        commandsTB.setSize(45, 78);        
        // Set Main Panel Size
        super.setSize(size);
        // Set Components Position
        bgImage.setLocation(size.width / 2 - resizedImg.getWidth() / 2, (size.height / 2 - resizedImg.getHeight() / 2) + 15);
        sliderH.setLocation(bgImage.getLocation().x - 20, bgImage.getLocation().y - 40);
        sliderV.setLocation(bgImage.getLocation().x - 45, bgImage.getLocation().y - 20);
        commandsTB.setLocation(size.width - 50, size.height - commandsTB.getSize().height);
        // Add Components
        this.add(bgImage, FRAME_CONTENT_LAYER);
        this.add(sliderV, DEFAULT_LAYER);
        this.add(sliderH, DEFAULT_LAYER);
        this.add(commandsTB, DRAG_LAYER);
    }
  
    /**
     * Resize Panel Components and change location
     * @param size new panel size
     */
    @Override
    public void setSize(Dimension size) {
        // Update Background Image Size - Resize Image
        resizedImg = samplesImg.duplicate();
        ImageProcessing.resizeImage(resizedImg, -1, size.height - 60);
        if (resizedImg.getWidth() > size.width - 100)
            ImageProcessing.resizeImage(resizedImg, size.width - 100, -1);
        // Re-init Background Image
        bgImage.setImage(resizedImg.getImage());
        bgImage.setSize(resizedImg.getWidth() + 4, resizedImg.getHeight() + 4);
        bgImage.setLocation(size.width / 2 - resizedImg.getWidth() / 2, (size.height / 2 - resizedImg.getHeight() / 2) + 15);
        // Update Sliders Size And Positions
        sliderH.setSize(resizedImg.getWidth() + 43, resizedImg.getHeight() + 70);
        sliderH.setLocation(bgImage.getLocation().x - 20, bgImage.getLocation().y - 40);
        sliderH.setTickLength(resizedImg.getHeight() + 25);
        sliderV.setSize(resizedImg.getWidth() + 70, resizedImg.getHeight() + 43);
        sliderV.setLocation(bgImage.getLocation().x - 45, bgImage.getLocation().y - 20);
        sliderV.setTickLength(resizedImg.getWidth() + 35);
        // Update toolbar location
        commandsTB.setLocation(size.width - 50, size.height - commandsTB.getSize().height);
        // Update main panel size
        super.setSize(size.width, size.height);
    }
    
    /**
     * Get Changes
     * @return DropPanel Data-Transfer-Object
     */
    @Override
    public CuttingDTO getResults() {
        // Get Thumbs positions
        float xPoints[] = sliderH.getThumbPositions();
        float yPoints[] = sliderV.getThumbPositions();
        // (x_1,y_1)
        int x1 = Math.round(xPoints[0] * samplesImg.getWidth());
        int y1 = Math.round(yPoints[0] * samplesImg.getHeight());
        Point top = new Point(x1,y1);
        // (x_2,y_2)
        int x2 = Math.round(xPoints[1] * samplesImg.getWidth());
        int y2 = Math.round(yPoints[1] * samplesImg.getHeight());
        Point foot = new Point(x2,y2);
        // Check for changes and update values if changed
        if (!top.equals(data.getUpperPoint()) || !foot.equals(data.getLowerPoint())) {
            data.setChanged(true);
            data.setUpperPoint(top);
            data.setLowerPoint(foot);
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
        if (!(dto instanceof CuttingDTO))
            return;
        data = (CuttingDTO) dto;
        // Update components
        textArea.setText(data.getComments());
        // Save Image
        samplesImg = data.getImage();
        // Resize Image Resized
        resizedImg = samplesImg.duplicate();
        Dimension size = this.getSize();
        ImageProcessing.resizeImage(resizedImg, -1, size.height - 60);
        if (resizedImg.getWidth() > size.width - 100)
            ImageProcessing.resizeImage(resizedImg, size.width - 100, -1);
        // Init Background Image
        bgImage.setImage(resizedImg.getImage());
        bgImage.setSize(resizedImg.getWidth() + 4, resizedImg.getHeight() + 4);
        bgImage.setLocation(size.width / 2 - resizedImg.getWidth() / 2, (size.height / 2 - resizedImg.getHeight() / 2) + 15);
        // Update Sliders Size And Positions
        sliderH.setSize(resizedImg.getWidth() + 43, resizedImg.getHeight() + 70);
        sliderH.setLocation(bgImage.getLocation().x - 20, bgImage.getLocation().y - 40);
        sliderH.setTickLength(resizedImg.getHeight() + 25);
        sliderV.setSize(resizedImg.getWidth() + 70, resizedImg.getHeight() + 43);
        sliderV.setLocation(bgImage.getLocation().x - 45, bgImage.getLocation().y - 20);
        sliderV.setTickLength(resizedImg.getWidth() + 35);
        // H positions
        float x1 = (float) data.getUpperPoint().getX() / (float) samplesImg.getWidth();
        float x2 = (float) data.getLowerPoint().getX() / (float) samplesImg.getWidth();
        // V positions
        float y1 = (float) data.getUpperPoint().getY() / (float) samplesImg.getHeight();
        float y2 = (float) data.getLowerPoint().getY() / (float) samplesImg.getHeight();
        // Array float positions
        float[] positionsH = new float[]{x1,x2};
        float[] positionsV = new float[]{y1,y2};
        // Check positions
        if (!checkArray(positionsH))
            positionsH = new float[]{0,1};
        if (!checkArray(positionsV))
            positionsV = new float[]{0,1};
        //
        Character chrs[] = new Character[positionsH.length];
        Arrays.fill(chrs, 'A');
        Character chrs2[] = new Character[positionsH.length];
        Arrays.fill(chrs2, 'A');
        // Update positions
        sliderH.setValues(positionsH, chrs);
        sliderV.setValues(positionsV, chrs2);
    }
    
    /**
     * Check if array is valid for thumbs positions
     * @param array float array to check
     * @return valid or invalid (true / false)
     */
    private boolean checkArray(float[] array) {
        // Check Thumbs positions
        if (array.length % 2 != 0 || array.length == 0)
            return false;
        // Check postions range
        for (float pos : array)
            if (pos > 1 || pos < 0)
                return false;
        // Check growing order
        for (int i = 0; i < array.length - 1; i ++) {
            if (array[i] > array[i + 1])
                return false;
        }
        return true;
    }
}