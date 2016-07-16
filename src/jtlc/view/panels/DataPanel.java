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
import com.alee.extended.panel.GroupingType;
import com.alee.laf.button.WebButton;
import com.alee.laf.button.WebToggleButton;
import com.alee.laf.label.WebLabel;
import com.alee.laf.optionpane.WebOptionPane;
import com.alee.laf.panel.WebPanel;
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
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import jtlc.core.processing.ImageProcessing;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import javax.swing.event.ChangeEvent;
import jtlc.main.common.Settings;
import jtlc.view.dto.AbstractDTO;
import jtlc.view.panels.dto.DataDTO;
import jtlc.view.components.slider.CustomSlider;
import jtlc.view.components.slider.CustomSliderUI;

/**
 * Samples Data Panel
 * Implements JLayered panel that allows to select per sample
 * front point and seed point, change sample name and others settings.
 * 
 * @author Cristian Tardivo
 */
public class DataPanel extends JLayeredPane implements IPanel {
    // Components
    private final List<CustomSlider<Character>> slidersList;
    private final List<WebDecoratedImage> bgImageList;
    private final List<JLayeredPane> samplePanelList;
    private final List<WebPanel> containerPanelList;
    private final List<WebLabel> labelsList;
    private final List<ImagePlus> samples;
    private final List<WebToggleButton> linkButtonsList;
    private final List<WebButton> nameButtonsList;
    private final List<WebButton> commentsButtonsList;
    private final List<WebTextArea> commentsTextArea;
    private final SlideContainer compPanel;
    private final JButton leftb, rightb;
    private final WebToolBar commandsTB;
    private final WebTextArea globalTextArea;
    // Panel DTO
    private final DataDTO data;
    
    /**
     * Create DataPanel panel
     * @param dto DataPanel Data-Transfer-Object
     * @param size Intial Panel Size
     */
    public DataPanel(DataDTO dto, Dimension size) {
        data = dto;
        samples = data.getSamplesImage();
        // Init Lists
        slidersList = new ArrayList<>(samples.size());
        bgImageList = new ArrayList<>(samples.size());
        samplePanelList = new ArrayList<>(samples.size());
        containerPanelList = new ArrayList<>(samples.size());
        labelsList = new ArrayList<>(samples.size());
        linkButtonsList = new ArrayList<>(samples.size());
        nameButtonsList = new ArrayList<>(samples.size());
        commentsButtonsList = new ArrayList<>(samples.size());
        commentsTextArea = new ArrayList<>(samples.size());
        // Init Main panel
        compPanel = new SlideContainer();
        // Create Components
        int maxX = 0, minX = 0, lastY = 0;
        for (ImagePlus img: samples) {
            // Resize Sample Image
            ImagePlus resizedImg = img.duplicate();
            ImageProcessing.resizeImage(resizedImg, -1, size.height - 80);
            // Init Background Image
            WebDecoratedImage bgImage = new WebDecoratedImage(resizedImg.getImage());
            bgImage.setSize(resizedImg.getWidth() + 4, resizedImg.getHeight() + 4);
            bgImage.setRound(0);
            bgImage.setDrawGlassLayer(false);
            bgImage.setDrawBorder(false);
            bgImage.setShadeWidth(4);
            // Init MultiThumbSlider
            float y1 = data.getSampleFrontPoint(img) / (float) img.getHeight();
            float y2 = data.getSampleSeedPoint(img) / (float) img.getHeight();
            float[] positions = new float[]{y1,y2};
            Character chrs[] = new Character[positions.length];
            Arrays.fill(chrs, 'A');
            CustomSlider<Character> slider = new CustomSlider<>(CustomSlider.VERTICAL, positions, chrs);
            slider.setThumbShape(CustomSliderUI.Thumb.Triangle);
            //
            slider.setSelectedThumb(-1);
            slider.setThumbOverlap(false);
            slider.setThumbRemovalAllowed(false);
            slider.setMouseThumbRemovalAllowed(false);
            slider.setAutoAdding(false);
            slider.setPaintTicks(true);
            slider.setPaintNumbers(false);
            slider.setVisible(true);
            slider.setPaintSquares(false);
            slider.setInverted(true);
            slider.setPaintCut(true);
            slider.setCollisionPolicy(CustomSlider.Collision.STOP_AGAINST);
            slider.setMinimumThumbnailCount(2);
            slider.setLinkedStatus(data.isLinked(img));
            //
            slider.setSize(resizedImg.getWidth() + 45, resizedImg.getHeight() + 43);
            slider.setTickLength(resizedImg.getWidth() + 10);
            slider.setTickStart(-3);
            // Slider Change listener
            slider.addChangeListener((ChangeEvent e) -> {
                if (slider.isLinkedEnabled()) {
                    for (CustomSlider cs : slidersList) {
                        if (cs.isLinkedEnabled()) {
                            cs.setFireChangeListeners(false);
                            cs.setValues(slider.getThumbPositions(), slider.getValues());
                            cs.setFireChangeListeners(true);
                        }
                    }
                }
            });
            //
            bgImage.setLocation(40, 28);
            slider.setLocation(bgImage.getLocation().x - 30, bgImage.getLocation().y - 20);
            // Sample Name label
            WebLabel sampleName = new WebLabel(data.getSampleName(img));
            sampleName.setDrawShade(true);            
            // Slider and image panel
            JLayeredPane samplePanel = new JLayeredPane();
            samplePanel.setOpaque(false);
            samplePanel.setSize(resizedImg.getWidth() + 75, resizedImg.getHeight() + 70);
            samplePanel.add(bgImage, FRAME_CONTENT_LAYER);
            samplePanel.add(sampleName, DEFAULT_LAYER);
            samplePanel.add(slider, DEFAULT_LAYER);
            // Sample name Label Size and location
            FontMetrics metrics = sampleName.getFontMetrics(sampleName.getFont());
            Rectangle rt = metrics.getStringBounds(sampleName.getText(), samplePanel.getGraphics()).getBounds();
            sampleName.setSize((rt.width <= samplePanel.getWidth())? rt.width : samplePanel.getWidth() - 1, 15);
            sampleName.setLocation((samplePanel.getWidth() / 2) - (sampleName.getWidth() / 2), 7);
            // Link Sliders Button
            WebToggleButton linkBtn = new WebToggleButton(Assets.loadIcon("ic_link"));
            WebCustomTooltip linkTooltip = TooltipManager.setTooltip(linkBtn, Assets.getString("LINK_SAMPLE_SLIDER"), TooltipWay.up, 250);
            Assets.associateComponent(linkTooltip, "setTooltip", "LINK_SAMPLE_SLIDER");
            linkBtn.setSize(28, 28);
            linkBtn.setLocation(samplePanel.getWidth() - 31, samplePanel.getHeight() - 31);
            linkBtn.setUndecorated(false);
            linkBtn.setOpaque(false);
            linkBtn.setDrawFocus(false);
            linkBtn.setDrawSides(true, true, true, true);
            linkBtn.setShadeToggleIcon(true);
            linkBtn.setSelected(data.isLinked(img));
            linkBtn.setRound(0);
            samplePanel.add(linkBtn, MODAL_LAYER);
            linkBtn.addActionListener((ActionEvent e) -> {
                slider.setLinkedStatus(!slider.isLinkedEnabled());
                data.setLinked(img, slider.isLinkedEnabled());
            });
            // Change Sample Name Button
            WebButton nameBtn = new WebButton(Assets.loadIcon("ic_text_edit"));
            WebCustomTooltip nameTooltip = TooltipManager.setTooltip(nameBtn, Assets.getString("CHANGE_SAMPLE_NAME"), TooltipWay.up, 250);
            Assets.associateComponent(nameTooltip, "setTooltip", "CHANGE_SAMPLE_NAME");
            nameBtn.setSize(28, 28);
            nameBtn.setLocation(samplePanel.getWidth() - 54, samplePanel.getHeight() - 31);
            nameBtn.setUndecorated(false);
            nameBtn.setOpaque(false);
            nameBtn.setDrawFocus(false);
            nameBtn.setDrawSides(true, true, true, true);
            nameBtn.setDrawShade(true);
            nameBtn.setRound(0);
            samplePanel.add(nameBtn, MODAL_LAYER);
            // Comments Pop-up
            WebButton showComments = new WebButton(Assets.loadIcon("ic_dialog", 20));
            WebCustomTooltip scommentsTooltip = TooltipManager.setTooltip(showComments, Assets.getString("SHOW_SAMPLE_COMMENTS"), TooltipWay.up, 100);
            Assets.associateComponent(scommentsTooltip, "setTooltip", "SHOW_SAMPLE_COMMENTS");
            WebTextArea textArea = new WebTextArea();
            textArea.setText(data.getSampleComment(img));
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            WebScrollPane areaScroll = new WebScrollPane(textArea);
            areaScroll.setPreferredSize(new Dimension(175,120));
            WebButtonPopup commentPopup = new WebButtonPopup(showComments, PopupWay.upCenter);
            WebLabel comentLabel = new WebLabel(Assets.getString("COMMENTS") , WebLabel.RIGHT);
            Assets.associateComponent(comentLabel, "setText", "COMMENTS");
            WebLabel cnameLabel = new WebLabel(data.getSampleName(img), WebLabel.LEFT);
            GroupPanel commentContent = new GroupPanel(10, false, new GroupPanel(GroupingType.fillAll, 4, true, comentLabel, cnameLabel), areaScroll);
            commentContent.setMargin(10);
            commentPopup.setContent(commentContent);
            commentPopup.setDefaultFocusComponent(areaScroll);
            showComments.setSize(28, 28);
            showComments.setLocation(samplePanel.getWidth() - 77, samplePanel.getHeight() - 31);
            showComments.setUndecorated(false);
            showComments.setOpaque(false);
            showComments.setDrawFocus(false);
            showComments.setDrawSides(true, true, true, true);
            showComments.setRound(0);
            showComments.setDrawShade(true);   
            samplePanel.add(showComments, MODAL_LAYER);
            // Change Name Button Listener
            nameBtn.addActionListener((ActionEvent e) -> {
                Object name = WebOptionPane.showInputDialog(compPanel, Assets.getString("SAMPLE_NAME"), Assets.getString("EDIT_SAMPLE_NAME"), JOptionPane.QUESTION_MESSAGE, null, null, sampleName.getText());
                if (name != null) {
                    String txt = ((String)name).trim();
                    if (txt.length() > 0) {
                        // Update Sample Title (Change name not count like a change)
                        data.setSampleName(img, txt);
                        // Update Sample Label
                        sampleName.setText(txt);
                        FontMetrics me = sampleName.getFontMetrics(sampleName.getFont());
                        Rectangle r = me.getStringBounds(sampleName.getText(), samplePanel.getGraphics()).getBounds();
                        sampleName.setSize((r.width <= samplePanel.getWidth())? r.width : samplePanel.getWidth() - 1, 15);
                        sampleName.setLocation((samplePanel.getWidth() / 2) - (sampleName.getWidth() / 2), 7);
                        cnameLabel.setText(data.getSampleName(img));
                    } else {
                        WebOptionPane.showMessageDialog(compPanel, Assets.getString("INVALID_SAMPLE_NAME"), Assets.getString("ERROR"), WebOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            // Container Panel
            WebPanel containerPanel = new WebPanel(true, new BorderLayout());
            containerPanel.setSize(samplePanel.getWidth(), samplePanel.getHeight());
            containerPanel.setPreferredWidth(samplePanel.getWidth());
            containerPanel.setPreferredHeight(samplePanel.getHeight());
            containerPanel.setPaintFocus(true);
            containerPanel.setMargin(0);
            containerPanel.add(samplePanel, BorderLayout.CENTER);
            containerPanel.setLocation(maxX, 0);
            // Last component positions            
            lastY = containerPanel.getHeight();
            maxX += containerPanel.getWidth();
            if (maxX < size.width - 100)
                minX += containerPanel.getWidth();
            compPanel.addComponent(containerPanel);
            // Save Componets for resize and relocation
            labelsList.add(sampleName);
            slidersList.add(slider);
            bgImageList.add(bgImage);
            samplePanelList.add(samplePanel);
            containerPanelList.add(containerPanel);
            linkButtonsList.add(linkBtn);
            nameButtonsList.add(nameBtn);
            commentsButtonsList.add(showComments);
            commentsTextArea.add(textArea);
        }
        // Update Compontes Panel Size and location
        compPanel.setSize(minX, lastY);
        compPanel.setLocation(size.width / 2 - compPanel.getWidth() / 2, size.height / 2 - compPanel.getHeight() / 2);
        // Slide Left Button
        leftb = new JButton(Assets.loadIcon("ic_left_arrow"));
        WebCustomTooltip prevTooltip = TooltipManager.setTooltip(leftb, Assets.getString("PREVIOUS_SAMPLE"), TooltipWay.up, 250);
        Assets.associateComponent(prevTooltip, "setTooltip", "PREVIOUS_SAMPLE");
        leftb.setLocation(compPanel.getLocation().x - 60, size.height / 2 - leftb.getHeight() / 2);
        leftb.setSize(30, 30);
        leftb.setEnabled(compPanel.noVisiblesAtLeft());
        // Slide Right Button
        rightb = new JButton(Assets.loadIcon("ic_right_arrow"));
        WebCustomTooltip nextTooltip = TooltipManager.setTooltip(rightb, Assets.getString("NEXT_SAMPLE"), TooltipWay.up, 250);
        Assets.associateComponent(nextTooltip, "setTooltip", "NEXT_SAMPLE");
        rightb.setLocation(compPanel.getLocation().x + compPanel.getWidth() + 30, size.height / 2 - rightb.getHeight() / 2);
        rightb.setSize(30, 30);
        rightb.setEnabled(compPanel.noVisiblesAtRight());
        // Buttons Listeners
        leftb.addActionListener((ActionEvent e) -> {
            if (!compPanel.isBusy()) {
                boolean more = compPanel.slideToRight();
                leftb.setEnabled(more);
                rightb.setEnabled(true);
                // Relocate components panel because swipe can change panel size
                // Temporal fix: need to disable this because a glitch with panel height
                //compPanel.setLocation(size.width / 2 - compPanel.getWidth() / 2, size.height / 2 - compPanel.getHeight() / 2);
            }
        });
        rightb.addActionListener((ActionEvent e) -> {
            if (!compPanel.isBusy()) {
                boolean more = compPanel.slideToLeft();
                rightb.setEnabled(more);
                leftb.setEnabled(true);
                // Relocate components panel because swipe can change panel size
                // Temporal fix: need to disable this because a glitch with panel height
                //compPanel.setLocation(size.width / 2 - compPanel.getWidth() / 2, size.height / 2 - compPanel.getHeight() / 2);
            }
        });
        // Comments Pop-up
        WebButton showComments = new WebButton(Assets.loadIcon("ic_dialog"));
        WebCustomTooltip commentsTooltip = TooltipManager.setTooltip(showComments, Assets.getString("SHOW_COMMENTS"), TooltipWay.left, 100);
        Assets.associateComponent(commentsTooltip, "setTooltip", "SHOW_COMMENTS");
        globalTextArea = new WebTextArea();
        globalTextArea.setText(data.getComments());
        globalTextArea.setLineWrap(true);
        globalTextArea.setWrapStyleWord(true);
        WebScrollPane areaScroll = new WebScrollPane(globalTextArea);
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
        WebEditorPane editorPane = new WebEditorPane("text/html",Assets.getString("DATA_HELP"));
        Assets.associateComponent(editorPane, "setText", "DATA_HELP");
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
        commandsTB.setLocation(size.width - 50, size.height - commandsTB.getSize().height);
        // Add Componets panel to main panel
        this.add(compPanel, FRAME_CONTENT_LAYER);
        this.add(leftb, DEFAULT_LAYER);
        this.add(rightb, DEFAULT_LAYER);
        this.add(commandsTB, DRAG_LAYER);
    }
    
    /**
     * Resize Panel Components and change location
     * @param size new panel size
     */
    @Override
    public void setSize(Dimension size) {
        // Update all components
        int maxX = 0, lastY = 0, minX = 0;
        for (int i = 0; i < samples.size(); i++) {
            ImagePlus img = samples.get(i);
            WebDecoratedImage bgImage = bgImageList.get(i);
            // Resize Sample Image
            ImagePlus resizedImg = img.duplicate();
            ImageProcessing.resizeImage(resizedImg, -1, size.height - 80);
            // Resize and relocate background Image
            bgImage.setSize(resizedImg.getWidth() + 4, resizedImg.getHeight() + 4);
            bgImage.setImage(resizedImg.getImage());
            bgImage.setLocation(40, 28);
            // Resize and relocate slider
            CustomSlider<Character> slider = slidersList.get(i);
            slider.setSize(resizedImg.getWidth() + 45, resizedImg.getHeight() + 43);
            slider.setTickLength(resizedImg.getWidth() + 10);
            slider.setTickStart(-3);
            slider.setLocation(bgImage.getLocation().x - 30, bgImage.getLocation().y - 20);
            // Resize and relocate sample panel
            JLayeredPane samplePanel = samplePanelList.get(i);
            samplePanel.setSize(resizedImg.getWidth() + 75, resizedImg.getHeight() + 70);
            samplePanel.setLocation(0, 0);
            // Relocate name label
            WebLabel sampleName = labelsList.get(i);
            // Sample name Label Size and location
            FontMetrics metrics = sampleName.getFontMetrics(sampleName.getFont());
            Rectangle rt = metrics.getStringBounds(sampleName.getText(), samplePanel.getGraphics()).getBounds();
            sampleName.setSize((rt.width <= samplePanel.getWidth())? rt.width : samplePanel.getWidth() - 1, 15);
            sampleName.setLocation((samplePanel.getWidth() / 2) - (sampleName.getWidth() / 2), 7);
            // Link/Comments and name buttons locations
            WebToggleButton linkBtn = linkButtonsList.get(i);
            linkBtn.setLocation(samplePanel.getWidth() - 31, samplePanel.getHeight() - 31);
            WebButton nameBtn = nameButtonsList.get(i);
            nameBtn.setLocation(samplePanel.getWidth() - 54, samplePanel.getHeight() - 31);
            WebButton showComments = commentsButtonsList.get(i);
            showComments.setLocation(samplePanel.getWidth() - 77, samplePanel.getHeight() - 31);
            // Resize and relocate container panel
            WebPanel containerPanel = containerPanelList.get(i);
            containerPanel.setSize(samplePanel.getWidth(), samplePanel.getHeight());
            containerPanel.setLocation(maxX, 0);
            containerPanel.revalidate();
            // Save data
            lastY = containerPanel.getHeight();
            maxX += containerPanel.getWidth();
            if (maxX < size.width - 100)
                minX += containerPanel.getWidth();
        }
        // Resize and relocate components panel
        compPanel.setSize(minX, lastY);
        compPanel.setLocation(size.width / 2 - compPanel.getWidth() / 2, size.height / 2 - compPanel.getHeight() / 2);
        // Relocate Slide Buttons && check for enabled
        leftb.setLocation(compPanel.getLocation().x - 60, size.height / 2 - leftb.getHeight() / 2);
        leftb.setEnabled(compPanel.noVisiblesAtLeft());
        rightb.setLocation(compPanel.getLocation().x + compPanel.getWidth() + 30, size.height / 2 - rightb.getHeight() / 2);
        rightb.setEnabled(compPanel.noVisiblesAtRight());
        // Update toolbar location
        commandsTB.setLocation(size.width - 50, size.height - commandsTB.getSize().height);
        // Update main panel size
        super.setSize(size.width, size.height);
    }
  
    /**
     * Get panel changes
     * @return DataPanel Data-Transfer-Object
     */
    @Override
    public DataDTO getResults() {
        List<ImagePlus> images = data.getSamplesImage();
        data.setChanged(false);
        for (int i = 0; i < images.size(); i++) {
            ImagePlus img = images.get(i);
            CustomSlider<Character> slider = slidersList.get(i);
            float[] pos = slider.getThumbPositions();
            // Front point
            int front = Math.round(pos[0] * img.getHeight());
            if (front != data.getSampleFrontPoint(img)) {
                // Save sample front point
                data.setSampleFrontPoint(img, front);
                // Sample data changes
                data.setChanged(img, true);
                // Global changes
                data.setChanged(true);
            }
            // Seed point
            int seed = Math.round(pos[1] * img.getHeight());
            if (seed != data.getSampleSeedPoint(img)) {
                // Save sample seed point
                data.setSampleSeedPoint(img, seed);
                // Sample data changes
                data.setChanged(img, true);
                // Global changes
                data.setChanged(true);
            }
            // Comments not count like a change
            data.setSampleComment(img, commentsTextArea.get(i).getText());
        }
        data.setComments(globalTextArea.getText());
        return data;
    }
    
    /**
     * Update panel components to new values in dto
     * @param dto Panel Data-Transfer-Object
     */
    @Override
    public void updatePanel(AbstractDTO dto) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /**
     * Custom Sliding Panel.
     */
    private class SlideContainer extends JLayeredPane {
        // Animation Speed
        private static final int SLIDE_DELAY = 20;
        protected static final int DELTA_X = 8; // Integer.MAX_VALUE disable animation
        // Components List
        private final List<Component> componets;
        // First left visible component
        private int flvc = -1;
        // First right no visible component
        private int frnvc = -1;
        // Animation Status
        private boolean busy = false;

        /**
         * Create Slider Container.
         */
        public SlideContainer() {
           setLayout(null);
           componets = new LinkedList<>();
        }
        
        /**
         * Add Components
         * @param comp new componet
         */
        public void addComponent(Component comp) {
           super.add(comp, JLayeredPane.DEFAULT_LAYER);
           componets.add(comp);
        }
        
        /**
         * Check Slide Status
         * @return busy/idle
         */
        public boolean isBusy() {
            return busy;
        }
        
        /**
         * Set Panel Size and check visible components
         * @param width panel width
         * @param height panel height
         */
        @Override
        public void setSize(int width, int height) {
            super.setSize(width, height);
            // Check Visible components
            for (Component cmp : componets) {
                // Check Left Minimun
                if (cmp.getLocation().x < 0) {
                    cmp.setVisible(false);
                    continue;
                }
                // Check Right Maximun
                cmp.setVisible(!(cmp.getWidth() + cmp.getLocation().x > width));
            }
            // Get Indexs
            flvc = -1;
            frnvc = componets.size();
            for (int i = 0; i < componets.size(); i++) {
                if (flvc == -1)
                    flvc = (componets.get(i).isVisible())? i : flvc;
                else
                    if (frnvc == componets.size())
                        frnvc = (componets.get(i).isVisible())? frnvc : i;
                if (flvc != -1 && frnvc != componets.size())
                    break;
            }
        }
        
        /**
         * Check if exist no visible components at left
         * @return true/false
         */
        public boolean noVisiblesAtLeft() {
            return flvc != 0;
        }
        
        /**
         * Check if exist no visible components at right
         * @return true/false
         */
        public boolean noVisiblesAtRight() {
            return frnvc != componets.size();
        }
        
        /**
         * Slide Components to Left.
         */
        public boolean slideToLeft() {
            // Check valid Slide
            if (frnvc >= componets.size() || frnvc < 0) return frnvc < componets.size();
            // Animation Start
            busy = true;
            // Update First no-visible component status
            Component next = componets.get(frnvc);
            next.setVisible(true);
            // Update indexs
            frnvc++;
            // Update Component Size
            super.setSize(next.getLocation().x + next.getWidth() - componets.get(flvc + 1).getLocation().x, getSize().height);
            // Transition Timer Delay
            new Timer(SLIDE_DELAY, new ActionListener() {
                // Transition length
                int length = (next.getLocation().x + next.getWidth()) - getSize().width;
                int step = Settings.isTransitionsEnabled()? DELTA_X : Integer.MAX_VALUE;
                
                // Perfom Transition
                @Override
                public void actionPerformed(ActionEvent evt) {
                    // Check Step Length
                    if (step > length)
                        step = length;
                    // Do transition
                    if (length > 0) {
                        // Move Components
                        for (Component cmp : componets)
                            cmp.setLocation(cmp.getLocation().x - step, cmp.getLocation().y);
                        // Update Transition length
                        length -= step;
                    } else {
                        // Stop Timer
                        ((Timer)evt.getSource()).stop();
                        // Update Components Data
                        componets.get(flvc).setVisible(true);
                        flvc++;
                        // Animation End
                        busy = false;
                    }
                    // Repaint Components
                    repaint();
                }
            }).start();
            return frnvc < componets.size();
        }
        
        /**
         * Slide Component to Right.
         */
        public boolean slideToRight() {
            // Check valid Slide
            if (flvc <= 0 || flvc >= componets.size()) return flvc > 0;
            // Animation Start
            busy = true;
            // Update Indexs
            flvc--;
            // Update First no-visible component status
            Component next = componets.get(flvc);
            next.setVisible(true);
            // Update Component Size
            super.setSize(componets.get(frnvc - 2).getLocation().x + componets.get(frnvc - 2).getWidth() + next.getWidth(), getSize().height);
            // Transition Timer Delay
            new Timer(SLIDE_DELAY, new ActionListener() {
                // Transition length
                int length = next.getLocation().x;
                int step = Settings.isTransitionsEnabled()? DELTA_X : Integer.MAX_VALUE;
                
                // Perfom Transition
                @Override
                public void actionPerformed(ActionEvent evt) {
                    // Check Step Length
                    if (step > Math.abs(length))
                        step = Math.abs(length);
                    // Do transition
                    if (length < 0) {
                        // Move Components
                        for (Component cmp : componets)
                            cmp.setLocation(cmp.getLocation().x + step, cmp.getLocation().y);
                        // Update Transition length
                        length += step;
                    } else {
                        // Stop Timer
                        ((Timer)evt.getSource()).stop();
                        // Update Components Data
                        frnvc--;
                        componets.get(frnvc).setVisible(true);
                        // Animation End
                        busy = false;
                    }
                    // Repaint Components
                    repaint();
                }
            }).start();
            return flvc > 0;
        }        
    }
}