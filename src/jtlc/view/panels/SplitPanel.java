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

import jtlc.assets.Assets;
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
import jtlc.core.processing.ImageProcessing;
import java.util.ArrayList;
import java.util.List;
import jtlc.view.dto.AbstractDTO;
import jtlc.view.panels.dto.SplitDTO;
import jtlc.view.components.slider.CustomSliderUI;
import jtlc.view.components.slider.CustomSlider;

/**
 * Samples Split Panel
 * Implements JLayeredPanel with a slider that allows to select individual samples.
 * 
 * @author Cristian Tardivo
 */
public class SplitPanel extends JLayeredPane implements IPanel {
    // Components
    private ImagePlus samplesImg;
    private ImagePlus resizedImg;
    private final WebDecoratedImage bgImage;
    private final CustomSlider<Character> slider;
    private final WebToolBar commandsTB;
    private final WebTextArea textArea;
    // Panel DTO
    private SplitDTO data;
    
    
    /**
     * Create SplitPanel
     * @param dto SplitPanel Data-Transfer-Object
     * @param size Initial Panel Size
     */
    public SplitPanel(SplitDTO dto, Dimension size) {
        // Save Params
        data = dto;
        samplesImg = dto.getImage();
        List<Point> positions = dto.getSamplesPoints();
        // Basic transform to relative values (between 0...1)
        float fPositions[] = new float[positions.size() * 2];
          for (int i = 0; i < positions.size(); i++) {
              Point p = positions.get(i);
              fPositions[i * 2] = (float) p.getX() / (float) samplesImg.getWidth();
              fPositions[(i * 2) + 1] = (float) p.getY() / (float) samplesImg.getWidth();
        }
        // Check Thumbs positions
        if (!checkArray(fPositions))
            fPositions = new float[]{0,0};
        // Resize Image Resized
        resizedImg = samplesImg.duplicate();
        ImageProcessing.resizeImage(resizedImg, -1, size.height - 40);
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
        Character chrs[] = new Character[fPositions.length];
        Arrays.fill(chrs, 'A');
        slider = new CustomSlider<>(CustomSlider.HORIZONTAL, fPositions, chrs);
        slider.setThumbShape(CustomSliderUI.Thumb.Triangle);
        // Init Default Slider Options
        slider.setSelectedThumb(-1);
        slider.setThumbOverlap(true);
        slider.setThumbRemovalAllowed(true);
        slider.setMouseThumbRemovalAllowed(false);
        slider.setAutoAdding(true);
        slider.setPaintTicks(true);
        slider.setVisible(true);
        slider.setPaintSquares(true);
        slider.setPaintNumbers(false);
        slider.setCollisionPolicy(CustomSlider.Collision.NUDGE_OTHER);
        slider.setMinimumThumbnailCount(2);
        // Multiplier Value
        slider.setMultValue(samplesImg.getWidth());
        // Slider Size
        slider.setSize(resizedImg.getWidth() + 43, resizedImg.getHeight() + 40);
        slider.setTickLength(resizedImg.getHeight() - 4);
        slider.setTickStart(3);
        slider.setSquareLength(resizedImg.getHeight());
        slider.setSquareStart(2);
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
        WebEditorPane editorPane = new WebEditorPane("text/html", Assets.getString("SPLIT_HELP"));
        Assets.associateComponent(editorPane, "setText", "SPLIT_HELP");
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
        bgImage.setLocation(size.width / 2 - bgImage.getWidth() / 2, (size.height / 2 - bgImage.getHeight() / 2) + 10);
        slider.setLocation(bgImage.getLocation().x - 20, bgImage.getLocation().y - 30);
        commandsTB.setLocation(size.width - 50, size.height - commandsTB.getSize().height);
        // Add Components
        this.add(bgImage, FRAME_CONTENT_LAYER);
        this.add(slider, DEFAULT_LAYER);
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
        ImageProcessing.resizeImage(resizedImg, -1, size.height - 40);
        if (resizedImg.getWidth() > size.width - 100)
            ImageProcessing.resizeImage(resizedImg, size.width - 100, -1);
        // Re-init Background Image
        bgImage.setImage(resizedImg.getImage());
        bgImage.setSize(resizedImg.getWidth() + 4, resizedImg.getHeight() + 4);
        bgImage.setLocation(size.width / 2 - bgImage.getWidth() / 2, (size.height / 2 - bgImage.getHeight() / 2) + 10);
        // Update Sliders Size And Positions
        slider.setSize(resizedImg.getWidth() + 43, resizedImg.getHeight() + 40);
        slider.setLocation(bgImage.getLocation().x - 20, bgImage.getLocation().y - 30);
        slider.setSquareLength(resizedImg.getHeight());
        slider.setTickLength(resizedImg.getHeight() - 4);
        // Update toolbar location
        commandsTB.setLocation(size.width - 50, size.height - commandsTB.getSize().height);
        // Update main panel size
        super.setSize(size.width, size.height);
    }
  
    /**
     * Get panel changes (Samples Slip Points)
     * @return SplitPanel Data-Transfer-Object
     */
    @Override
    public SplitDTO getResults() {
        // Compute new positions
        float positions[] = slider.getThumbPositions();
        List<Point> newPoints = new ArrayList<>(positions.length / 2);
        for (int i = 0; i < positions.length; i += 2) {
            int a = Math.round(positions[i] * samplesImg.getWidth());
            int b = Math.round(positions[i + 1] * samplesImg.getWidth());
            if (Math.abs(b - a) >= 10)
                newPoints.add(new Point(a, b));
        }
        // Check for changes and update values if changed
        if (!data.getSamplesPoints().equals(newPoints)) {
            data.setChanged(true);
            data.setSamplesPoints(newPoints);
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
        if (!(dto instanceof SplitDTO))
            return;
        data = (SplitDTO) dto;
        // Update components
        textArea.setText(data.getComments());
        //
        samplesImg = data.getImage();
        List<Point> positions = data.getSamplesPoints();
        // Basic transform to relative values (between 0...1)
        float fPositions[] = new float[positions.size() * 2];
          for (int i = 0; i < positions.size(); i++) {
              Point p = positions.get(i);
              fPositions[i * 2] = (float) p.getX() / (float) samplesImg.getWidth();
              fPositions[(i * 2) + 1] = (float) p.getY() / (float) samplesImg.getWidth();
        }
        // Check Thumbs positions
        if (!checkArray(fPositions))
            fPositions = new float[]{0,0};
        Dimension size = this.getSize();
        // Resize Image Resized
        resizedImg = samplesImg.duplicate();
        ImageProcessing.resizeImage(resizedImg, -1, size.height - 40);
        if (resizedImg.getWidth() > size.width - 100)
            ImageProcessing.resizeImage(resizedImg, size.width - 100, -1);
        // Init Background Image
        bgImage.setImage(resizedImg.getImage());
        bgImage.setSize(resizedImg.getWidth() + 4, resizedImg.getHeight() + 4);
        bgImage.setLocation(size.width / 2 - bgImage.getWidth() / 2, (size.height / 2 - bgImage.getHeight() / 2) + 10);
        // Init Horizontal MultiThumbSlider
        Character chrs[] = new Character[fPositions.length];
        Arrays.fill(chrs, 'A');
        slider.setValues(fPositions, chrs);
        // Update Sliders Size And Positions
        slider.setSize(resizedImg.getWidth() + 43, resizedImg.getHeight() + 40);
        slider.setLocation(bgImage.getLocation().x - 20, bgImage.getLocation().y - 30);
        slider.setSquareLength(resizedImg.getHeight());
        slider.setTickLength(resizedImg.getHeight() - 4);
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