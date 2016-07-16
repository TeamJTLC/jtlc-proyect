/*
 * This file is part of WebLookAndFeel library.
 *
 * WebLookAndFeel library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *(at your option)any later version.
 *
 * WebLookAndFeel library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with WebLookAndFeel library.  If not, see <http://www.gnu.org/licenses/>.
 */
package jtlc.view.components;

import com.alee.laf.scroll.WebScrollPane;
import com.alee.managers.hotkey.Hotkey;
import com.alee.utils.GraphicsUtils;
import com.alee.utils.ImageUtils;
import com.alee.utils.LafUtils;
import com.alee.utils.SwingUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.List;
import jtlc.assets.Assets;

/**
 * User: mgarin Date: 05.09.11 Time: 15:45
 */
public class CustomWebGallery extends JComponent {
    private final int spacing = 20;
    private int imageLength = 200;
    private final int borderWidth = 3;
    private int maxWidth = 0;
    private int maxHeight = 0;
    //
    private final List<ImageIcon> images = new ArrayList<>();
    private final List<String> descriptions = new ArrayList<>();
    private final List<String> dates = new ArrayList<>();
    private final List<String> titles = new ArrayList<>();
    //
    private int preferredColumnCount = 4;
    private boolean scrollOnSelection = true;
    private int selectedIndex = -1;
    private int oldSelectedIndex = -1;
    //
    private WebScrollPane view;

    public CustomWebGallery() {
        super();
        SwingUtils.setOrientation(this);
        setFocusable(true);
        setFont(new JLabel().getFont().deriveFont(Font.BOLD));

        final MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    CustomWebGallery.this.requestFocusInWindow();
                    for (int i = 0; i < images.size(); i++) {
                        if (getImageRect(i).contains(e.getPoint())) {
                            setSelectedIndex(i);
                            break;
                        }
                    }
                }
            }

            @Override
            public void mouseWheelMoved(final MouseWheelEvent e) {
                final int index = getSelectedIndex();
                final int maxIndex = images.size() - 1;
                final int wheelRotation = e.getWheelRotation();
                int newIndex;
                if (wheelRotation > 0) {
                    newIndex = index + wheelRotation;
                    while(newIndex > maxIndex) {
                        newIndex -= images.size();
                    }
                } else {
                    newIndex = index + wheelRotation;
                    while(newIndex < 0) {
                        newIndex += images.size();
                    }
                }
                setSelectedIndex(newIndex);
            }
        };
        addMouseListener(mouseAdapter);
        addMouseWheelListener(mouseAdapter);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(final KeyEvent e) {
                if (images.size()> 0) {
                    final int si = getSelectedIndex();
                    if (Hotkey.LEFT.isTriggered(e)) {
                        setSelectedIndex(si == -1 || si == 0 ? images.size() - 1 : si - 1);
                    } else if (Hotkey.RIGHT.isTriggered(e)) {
                        setSelectedIndex(si == -1 || si == images.size() - 1 ? 0 : si + 1);
                    } else if (Hotkey.HOME.isTriggered(e)) {
                        setSelectedIndex(0);
                    } else if (Hotkey.END.isTriggered(e)) {
                        setSelectedIndex(images.size() - 1);
                    }
                }
            }
        });
    }

    public List<ImageIcon> getImages() {
        return images;
    }

    public int getPreferredColumnCount() {
        return preferredColumnCount;
    }

    public void setPreferredColumnCount(final int preferredColumnCount) {
        this.preferredColumnCount = preferredColumnCount;
    }

    public WebScrollPane getView() {
        return getView(true);
    }

    public WebScrollPane getView(final boolean withBorder) {
        if (view == null) {
            view = new WebScrollPane(CustomWebGallery.this, withBorder) {
                @Override
                public Dimension getPreferredSize() {
                    final int columns = Math.min(images.size(), preferredColumnCount);
                    final JScrollBar hsb = getHorizontalScrollBar();
                    final int sbh = hsb != null && hsb.isShowing()? hsb.getPreferredSize().height : 0;
                    return new Dimension(spacing *(columns + 1) + columns * maxWidth, CustomWebGallery.this.getPreferredSize().height + sbh);
                }
            };
            
            view.setHorizontalScrollBarPolicy(WebScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            view.setVerticalScrollBarPolicy(WebScrollPane.VERTICAL_SCROLLBAR_NEVER);

            final InputMap im = view.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
            im.put(KeyStroke.getKeyStroke("UP"), "none");
            im.put(KeyStroke.getKeyStroke("DOWN"), "none");
            im.put(KeyStroke.getKeyStroke("LEFT"), "none");
            im.put(KeyStroke.getKeyStroke("RIGHT"), "none");
        }
        return view;
    }

    public int getImageLength() {
        return imageLength;
    }

    public void setImageLength(final int imageLength) {
        this.imageLength = imageLength;
    }

    public boolean isScrollOnSelection() {
        return scrollOnSelection;
    }

    public void setScrollOnSelection(final boolean scrollOnSelection) {
        this.scrollOnSelection = scrollOnSelection;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(final int selectedIndex) {
        if (this.selectedIndex == selectedIndex) {
            return;
        }

        this.oldSelectedIndex = this.selectedIndex;
        this.selectedIndex = selectedIndex;
        repaint();
        if (scrollOnSelection) {
            final Rectangle rect = getImageRect(selectedIndex);
            SwingUtils.scrollSmoothly(getView(), rect.x + rect.width / 2 - CustomWebGallery.this.getVisibleRect().width / 2, rect.y);
        }
        CustomWebGallery.this.repaint();
    }

    public Rectangle getImageRect(final int index) {
        final int iconWidth = images.get(index).getIconWidth();
        final int iconHeight = images.get(index).getIconHeight();
        final Dimension ps = getPreferredSize();
        final int x = (getWidth() > ps.width ? (getWidth() - ps.width) / 2 : 0) + spacing + (maxWidth + spacing) * index + maxWidth / 2;
        final int y = getHeight() / 2;
        return new Rectangle(x - iconWidth / 2, y - iconHeight / 2, iconWidth, iconHeight);
    }
    
    // Add image to last
    public void addImage(final Image image, final String title, final String description, final String date) {
        final ImageIcon previewIcon = ImageUtils.createPreviewIcon(image, imageLength);
        //            
        images.add(previewIcon);
        descriptions.add(description);
        dates.add(date);
        titles.add(title);
        //
        recalcualteMaxSizes();
        updateContainer();
    }
        
    public void addImage(final int index, final Image image, final String title, final String description, final String date) {
        final ImageIcon previewIcon = ImageUtils.createPreviewIcon(image, imageLength);
        //            
        images.add(index, previewIcon);
        descriptions.add(index, description);
        dates.add(date);
        titles.add(index, title);
        //
        recalcualteMaxSizes();
        updateContainer();
    }


    public void removeImage(final int index) {
        if (index >= 0 && index < images.size()) {
            final boolean wasSelected = getSelectedIndex()== index;

            images.remove(index);
            descriptions.remove(index);
            dates.remove(index);
            recalcualteMaxSizes();
            updateContainer();

            if (wasSelected && images.size() > 0) {
                setSelectedIndex(index < images.size()? index : index - 1);
            }
        }
    }
    
    public void removeAllImages() {
        images.clear();
        descriptions.clear();
        dates.clear();
        //
        recalcualteMaxSizes();
        updateContainer();
        setSelectedIndex(-1);
    }

    private void updateContainer() {
        if (getParent() instanceof JComponent) {
           ((JComponent)getParent()).revalidate();
        }
        repaint();
    }

    private void recalcualteMaxSizes() {
        for (final ImageIcon icon : images) {
            maxWidth = Math.max(maxWidth, icon.getIconWidth());
            maxHeight = Math.max(maxHeight, icon.getIconHeight());
        }
    }

    @Override
    protected void paintComponent(final Graphics g) {
        super.paintComponent(g);

        final int height = getHeight();
        final int width = getWidth();

        final Graphics2D g2d =(Graphics2D)g;
        final Object aa = GraphicsUtils.setupAntialias(g2d);

        g2d.setPaint(new GradientPaint(0, 0, Color.black, 0, height, Color.darkGray));
        g2d.fillRect(0, 0, width, height);

        final Rectangle vr = getVisibleRect();
        final Dimension ps = getPreferredSize();
        final Composite oldComposite = g2d.getComposite();
        for (int i = 0; i < images.size(); i++) {
            if (!getImageRect(i).intersects(vr)) {
                continue;
            }

            final ImageIcon icon = images.get(i);
            final BufferedImage bi = ImageUtils.getBufferedImage(icon);
            final int imageWidth = icon.getIconWidth();
            final int imageHeight = icon.getIconHeight();

            final int x = (getWidth() > ps.width ?(getWidth() - ps.width) / 2 : 0) + spacing + (maxWidth + spacing) * i + maxWidth / 2;
            final int y = height / 2 - imageHeight / 2;
            
            // Initial image
            final float add = selectedIndex == i ? 0.4f : 0f;
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f + add));

            g2d.drawImage(bi, x - imageWidth / 2, y, null);

            g2d.setPaint(selectedIndex == i ? Color.WHITE : Color.GRAY);
            Area gp = new Area(new RoundRectangle2D.Double(x - imageWidth / 2 - borderWidth, y - borderWidth, imageWidth + borderWidth * 2, imageHeight + borderWidth * 2, borderWidth * 2, borderWidth * 2));
            gp.subtract(new Area(new Rectangle(x - imageWidth / 2, y, imageWidth, imageHeight)));
            g2d.fill(gp);
            
            g2d.setComposite(oldComposite);

            // Info text
            if (selectedIndex == i || oldSelectedIndex == i) {
                final float opacity = selectedIndex == i ? 1f : 0f;
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
                g2d.setPaint(Color.WHITE);
                // Title
                String titleText = titles.get(i);
                titleText = Assets.shortString(titleText, 18, true);
                final Point tst = LafUtils.getTextCenterShear(g2d.getFontMetrics(), titleText);
                g2d.drawString(titleText, x + tst.x, y - spacing - tst.y);
                
                // Description
                String infoText = descriptions.get(i);
                infoText = Assets.shortString(infoText, 103, true);
                if (infoText.isEmpty()) infoText = "Not Description";
                AttributedString attributedString = new AttributedString(infoText);
                attributedString.addAttribute(TextAttribute.FONT, getFont().deriveFont(getFont().getSize2D() / 1.25f));
                AttributedCharacterIterator attributedCharacterIterator = attributedString.getIterator();
                int start = attributedCharacterIterator.getBeginIndex();
                int end = attributedCharacterIterator.getEndIndex();
                LineBreakMeasurer lineBreakMeasurer = new LineBreakMeasurer(attributedCharacterIterator, new FontRenderContext(null, false, false));
                
                float  Y = y + spacing + imageHeight;
                lineBreakMeasurer.setPosition(start);
                while(lineBreakMeasurer.getPosition() < end) {
                    TextLayout textLayout = lineBreakMeasurer.nextLayout(imageWidth + imageWidth / 4f);
                    float X = (float)((textLayout.getBounds().getWidth()- imageWidth) / 2);
                    Y += textLayout.getAscent();
                    g2d.setFont(getFont());
                    textLayout.draw(g2d, x - imageWidth / 2 - X, Y);
                    Y += textLayout.getDescent() + textLayout.getLeading();
                }
                // Date
                Font oldf = getFont();
                g2d.setFont(oldf.deriveFont(oldf.getSize2D() / 1.15f));
                final String dateText = dates.get(i);
                final Point dt = LafUtils.getTextCenterShear(g2d.getFontMetrics(), dateText);
                int hg = LafUtils.getTextBounds(dateText, g2d, g2d.getFont()).height;
                g2d.drawString(dateText, x + dt.x, Y + spacing - hg);
                g2d.setComposite(oldComposite);
                g2d.setFont(oldf);
            }
        }
        GraphicsUtils.restoreAntialias(g2d, aa);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(spacing *(images.size() + 1)+ maxWidth * images.size(), spacing * 3 + maxHeight * 2);
    }
    
    void drawString(Graphics g, String text, int x, int y) {
        for (String line : text.split("\n"))
            g.drawString(line, x, y += g.getFontMetrics().getHeight());
    }
}
