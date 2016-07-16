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

import com.alee.extended.image.WebDecoratedImageStyle;
import com.alee.extended.image.WebImageDrop;
import com.alee.global.StyleConstants;
import com.alee.laf.label.WebLabel;
import com.alee.utils.GraphicsUtils;
import com.alee.utils.ImageUtils;
import com.alee.extended.drag.ImageDropHandler;
import com.alee.extended.panel.GroupPanel;
import com.alee.laf.button.WebButton;
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
import com.alee.utils.DragUtils;
import ij.ImagePlus;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Transparency;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import jtlc.view.dto.AbstractDTO;
import jtlc.view.panels.dto.DropDTO;
import jtlc.core.storage.ImageStore;
import jtlc.assets.Assets;
import jtlc.main.common.Settings;

/**
 * Image Drop/Load Panel
 * Implements JLayeredPane with imagedrop component and filechooser for image load.
 * 
 * @author Cristian Tardivo
 */
public class DropPanel extends JLayeredPane implements IPanel {
    // Panel Componets
    private final JPanel textPanel;
    private final CustomImageDrop imageDropComponet;
    private final WebTextArea textArea;
    private final WebToolBar commandsTB;
    // Panel DTO
    private DropDTO data;
    
    /**
     * Create DropPanel
     * @param dto DropPanel Data-Transfer-Object
     * @param size Initial Panel Size
     */  
    public DropPanel(DropDTO dto, Dimension size) {
        // Save dto
        data = dto;
        // Image Drop Component
        imageDropComponet = new CustomImageDrop(size.width - 100, size.height);
        imageDropComponet.setLocation(50, 0);
        imageDropComponet.setRound(0);
        // Tooltip Text
        textPanel = new JPanel();
        textPanel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        WebLabel dragIcon = new WebLabel(Assets.loadIcon("down_arrow"));
        dragIcon.setHorizontalAlignment(JLabel.CENTER);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 0;
        constraints.gridy = 0;
        textPanel.add(dragIcon,constraints);
        //
        WebLabel dragHere = new WebLabel(Assets.getString("DRAG_IMAGE_HERE"));
        Assets.associateComponent(dragHere, "setText", "DRAG_IMAGE_HERE");
        dragHere.setForeground(StyleConstants.backgroundColor);
        dragHere.setShadeColor(Color.DARK_GRAY);
        dragHere.setFontSize(25);
        dragHere.setDrawShade(true);
        dragHere.setBoldFont(true);
        dragHere.setHorizontalAlignment(JLabel.CENTER);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 0;
        constraints.gridy = 1;
        textPanel.add(dragHere,constraints);
        //
        WebLabel clickLoad = new WebLabel(Assets.getString("CLICK_TO_LOAD"));
        Assets.associateComponent(clickLoad, "setText", "CLICK_TO_LOAD");
        clickLoad.setForeground(StyleConstants.backgroundColor);
        clickLoad.setShadeColor(Color.DARK_GRAY);
        clickLoad.setFontSize(15);
        clickLoad.setDrawShade(true);
        clickLoad.setBoldFont(true);
        clickLoad.setHorizontalAlignment(JLabel.CENTER);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 0;
        constraints.gridy = 2;
        textPanel.add(clickLoad,constraints);
        //
        WebLabel clickRemove = new WebLabel(Assets.getString("DOUBLE_CLICK_TO_REMOVE"));
        Assets.associateComponent(clickRemove, "setText", "DOUBLE_CLICK_TO_REMOVE");
        clickRemove.setForeground(StyleConstants.backgroundColor);
        clickRemove.setShadeColor(Color.DARK_GRAY);
        clickRemove.setFontSize(15);
        clickRemove.setDrawShade(true);
        clickRemove.setBoldFont(true);
        clickRemove.setHorizontalAlignment(JLabel.CENTER);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 0;
        constraints.gridy = 3;
        textPanel.add(clickRemove,constraints);
        //
        int min_dim = (size.height > size.width)? size.width : size.height;        
        textPanel.setOpaque(false);
        textPanel.setSize(min_dim,min_dim);
        textPanel.setLocation(size.width / 2 - (min_dim / 2), size.height / 2 - (min_dim / 2));
        textPanel.setTransferHandler(imageDropComponet.getTransferHandler());
        // Listener to remove/add drop message
        imageDropComponet.addChangeListener((ChangeEvent e) -> {
            if (imageDropComponet.getImage() == null) {
                this.add(textPanel, DEFAULT_LAYER);
                TooltipManager.removeTooltips(imageDropComponet);
            } else {
                this.remove(textPanel);
                WebCustomTooltip tooltip = TooltipManager.setTooltip(imageDropComponet, Assets.getString("DOUBLE_CLICK_TO_REMOVE"), TooltipWay.up, 50);
                Assets.associateComponent(tooltip, "setTooltip", "DOUBLE_CLICK_TO_REMOVE");
            }
        });
        // Listener to remove image
        imageDropComponet.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Check for valid click (inside drop rectangle or over the image)
                if (!imageDropComponet.checkPoint(e.getPoint())) return;
                // Double-Click - Clear Image
                if (e.getClickCount () == 2 && imageDropComponet.getImage() != null) {
                    imageDropComponet.setImage((ImagePlus)null);
                    return;
                }
                if (e.getClickCount() == 1 && imageDropComponet.getImage() == null) {
                    String path = showOpenImageFile();
                    if (path != null) {
                        ImagePlus ip = ImageStore.openImage(path);
                        imageDropComponet.setImage(ip);
                    }
                }
            }
        });
        // Init drop image
        imageDropComponet.setImage(data.getImage());
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
        showComments.setSize(36, 36);
        // Help Button
        WebButton showHelp = new WebButton(Assets.loadIcon("ic_help"));
        WebCustomTooltip helpTooltip = TooltipManager.setTooltip(showHelp, Assets.getString("SHOW_HELP"), TooltipWay.left, 100);
        Assets.associateComponent(helpTooltip, "setTooltip", "SHOW_HELP");
        WebEditorPane editorPane = new WebEditorPane("text/html", Assets.getString("DROP_HELP"));
        Assets.associateComponent(editorPane, "setText", "DROP_HELP");
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
        showHelp.setSize(36, 36);
        // Commands Toolbar
        commandsTB = new WebToolBar(WebToolBar.VERTICAL);
        commandsTB.setFloatable(false);
        commandsTB.setToolbarStyle(ToolbarStyle.standalone);
        commandsTB.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        commandsTB.add(showHelp);
        commandsTB.add(showComments);
        commandsTB.setSize(45, 78);
        commandsTB.setLocation(size.width - 50, size.height - commandsTB.getSize().height);
        // Add Componets
        super.setSize(size);
        this.add(imageDropComponet, FRAME_CONTENT_LAYER);
        this.add(commandsTB, DRAG_LAYER);
    }
    
    /**
     * Resize Panel Components and change location
     * @param size new panel size
     */
    @Override
    public void setSize(Dimension size) {
        // Update drop component size        
        imageDropComponet.setSize(size.width - 100, size.height);
        imageDropComponet.setImageWidth(size.width - 100);
        imageDropComponet.setImageHeight(size.height);
        imageDropComponet.setLocation(50, 0);
        // Update text Panel Location
        int min_dim = (size.height > size.width)? size.width : size.height;        
        textPanel.setSize(min_dim,min_dim);
        textPanel.setLocation(size.width / 2 - (min_dim / 2), size.height / 2 - (min_dim / 2));
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
    public DropDTO getResults() {
        // Check for changes and update values if changed
        if (data.getImage() != imageDropComponet.getImagePlus()) {
            data.setChanged(true);
            data.setImage(imageDropComponet.getImagePlus());
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
        if (!(dto instanceof DropDTO))
            return;
        data = (DropDTO) dto;
        // Update components
        textArea.setText(data.getComments());
        imageDropComponet.setImage(data.getImage());
    }
        
    /**
     * Show Open File - file chooser
     * @return selected file path (null if cancel)
     */
    private String showOpenImageFile() {
        JFileChooser fc = new JFileChooser(Settings.getWorkSpace());
        fc.setApproveButtonText(Assets.getString("OPEN"));
        fc.setFileFilter(new FileNameExtensionFilter(Assets.getString("IMAGE_FILES"), "jpg", "png", "tif", "bmp"));
        fc.showOpenDialog(this);
        if (fc.getSelectedFile() != null) {
            return fc.getSelectedFile().getPath();
        }
        return null;
    }
    
    /**
     * Custom WebImageDrop component with changeListeners and better drophandler.
     */
    private class CustomImageDrop extends WebImageDrop {
        /** ChangeListeners registered. */
	List<ChangeListener> changeListeners;
        // Basic effect
        private final int shadeWidth = 4;
        // ImagePlus
        private ImagePlus importedImage;
        
        /** Construct new custom image drop with the specified preview image area size. */
        public CustomImageDrop(int w, int h) {
            super(w,h);
            setSize(w, h);
            setTransferHandler(dropHandler);
        }
        
        /** Update setImage method to fire change listeners. */
        public void setImage(final ImagePlus image) {
            super.setImage((image != null)? image.getBufferedImage(): null);
            importedImage = image;
            fireChangeListeners();
        }
        
        /** Get imported ImagePlus. **/
        public ImagePlus getImagePlus() {
            return importedImage;
        }
        
        /** Check if borders contains point. **/
        public boolean checkPoint(Point p) {
            if (image != null) {
                Shape bs = getBorderShape((getWidth() / 2 - image.getWidth() / 2 + 1) - shadeWidth,  (getHeight() / 2 - image.getHeight() / 2 + 1) - shadeWidth,
                                            new Dimension(image.getWidth() + shadeWidth * 2, image.getHeight() + shadeWidth * 2));
                return bs.contains(p);
            } else {
                int min_dim = (height > width)? width:height;
                final Shape border = new RoundRectangle2D.Double(getWidth() / 2 - min_dim / 2 + 1, getHeight() / 2 - min_dim / 2 + 1,
                    min_dim - (image == null ? 3 : 1), min_dim - (image == null ? 3 : 1), round * 2, round * 2);
                return border.contains(p);
            }
        }
        
        /**
         * Updates image preview.
         */
        @Override
        protected void updatePreview() {
            if (image != null) {
                // Creating image preview
                image = ImageUtils.createPreviewImage(actualImage, width - shadeWidth * 2, height - shadeWidth * 2);
                // Restore decoration
                final BufferedImage f = ImageUtils.createCompatibleImage(image, Transparency.TRANSLUCENT);
                final Graphics2D g2d = f.createGraphics();
                GraphicsUtils.setupAntialias(g2d);
                g2d.setPaint(Color.WHITE);
                g2d.fillRoundRect(0, 0, image.getWidth(), image.getHeight(), round * 2, round * 2);
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_IN));
                g2d.drawImage(image, 0, 0, null);
                g2d.dispose();
                //
                image.flush();
                image = f;
            }
        }
       
        /**
         * {@inheritDoc}
         */
        @Override
        protected void paintComponent(final Graphics g) {
            //
            if (ui != null) {
                Graphics scratchGraphics = (g == null) ? null : g.create();
                if (scratchGraphics != null) {
                    try {
                        ui.update(scratchGraphics, this);
                    }
                    finally {
                        scratchGraphics.dispose();
                    }
                }
            }
            //
            Graphics2D g2d = (Graphics2D) g;
            final Object aa = GraphicsUtils.setupAntialias(g2d);
            // Paint image and border shadow
            if (image != null && g2d != null) {
                GraphicsUtils.setupAntialias(g2d);
                Shape bs = getBorderShape((getWidth() / 2 - image.getWidth() / 2 + 1) - shadeWidth,  (getHeight() / 2 - image.getHeight() / 2 + 1) - shadeWidth,
                                            new Dimension(image.getWidth() + shadeWidth * 2, image.getHeight() + shadeWidth * 2));
                // Shade
                GraphicsUtils.drawShade(g2d, bs, WebDecoratedImageStyle.shadeType, new Color(90, 90, 90), shadeWidth);
                // Image itself
                g2d.drawImage(image, getWidth() / 2 - image.getWidth() / 2 + 1, getHeight() / 2 - image.getHeight() / 2 + 1, null);
                g2d.dispose();
            }
            // Paint Empty Border
            if (image == null && g2d != null) {
                int min_dim = (height > width)? width:height;
                final Shape border = new RoundRectangle2D.Double(getWidth() / 2 - min_dim / 2 + 1, getHeight() / 2 - min_dim / 2 + 1,
                    min_dim - (image == null ? 3 : 1), min_dim - (image == null ? 3 : 1), round * 2, round * 2);

                g2d.setPaint(new Color(242, 242, 242));
                g2d.fill(border);

                g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f,
                        new float[]{Math.max(5f, Math.min(Math.max(width, height) / 6, 10f)), 8f}, 4f));
                g2d.setPaint(Color.LIGHT_GRAY);
                g2d.draw(border);
            }
            //
            GraphicsUtils.restoreAntialias(g2d, aa);
        }
        
        /** Border Shape. **/
        private Shape getBorderShape(int x, int y, Dimension ps) {
            if (round > 0) {
                return new RoundRectangle2D.Double(x + shadeWidth, y + shadeWidth, ps.width - shadeWidth * 2 - 1, ps.height - shadeWidth * 2 - 1, round * 2, round * 2);
            } else {
                return new Rectangle(x + shadeWidth, y + shadeWidth, ps.width - shadeWidth * 2 - 1, ps.height - shadeWidth * 2 - 1);
            }
        }
        
        /** AddChangeListeners. */
	public void addChangeListener(ChangeListener l) {
            if (changeListeners==null)
                changeListeners = new ArrayList<>();
            if (changeListeners.contains(l))
                return;
            changeListeners.add(l);
	}
	
	/** Removes a ChangeListener. */
	public void removeChangeListener(ChangeListener l) {
            if (changeListeners==null)
                return;
            changeListeners.remove(l);
	}
        
        /** Invokes all the ChangeListeners. */
	protected void fireChangeListeners() {
            if (changeListeners==null)
                    return;
            for (ChangeListener changeListener : changeListeners) {
                try {
                    (changeListener).stateChanged(new ChangeEvent(this));
                } catch(Throwable t) {
                    t.printStackTrace();
                }
            }
	}
        
        /**
         * Custom Image Drop Handler.
         */
        ImageDropHandler dropHandler = new ImageDropHandler() {
            // Import Image
            protected boolean importImage(ImagePlus image) {
                try {
                    setImage(image);
                    return true;
                } catch (final Throwable e) { }
                return false;
            }
            
            // Import File image
            @Override
            public boolean importData(final Transferable t) {
                if (isDropEnabled()) {
                    // Import and Check images files
                    final List<File> files = DragUtils.getImportedFiles(t);
                    if (files != null) {
                        for (final File file : files) {
                            ImagePlus ip = ImageStore.openImage(file.getAbsolutePath());
                            if (ip != null)
                                return isDropEnabled() && importImage(ip);
                        }
                    }    
                }
                return false;
            }
        };
    }
}