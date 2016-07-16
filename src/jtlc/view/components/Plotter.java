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
package jtlc.view.components;

import com.alee.extended.layout.VerticalFlowLayout;
import com.alee.laf.button.WebButton;
import com.alee.laf.checkbox.WebCheckBox;
import com.alee.laf.colorchooser.WebColorChooserDialog;
import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.rootpane.WebFrame;
import com.alee.utils.ImageUtils;
import com.alee.utils.swing.DialogOptions;
//
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
//
import ij.ImagePlus;
import java.awt.geom.AffineTransform;
//
import jtlc.assets.Assets;
import jtlc.main.common.Pair;
import jtlc.main.common.Triplet;

/**
 * Multiple Curve Plotter Component
 * 
 * @author Cristian Tardivo
 */
public class Plotter extends JComponent {
    // Limits and Scale
    private double maxX = 0, minX = Double.MAX_VALUE, xScale = 1;
    private double maxY = 0, minY = Double.MIN_VALUE, yScale = 1;
    // Functions to plot
    private List<Pair<Float,Float>>[] functions;
    // Functions names
    private String[] names;
    // Functions visibility
    private boolean[] show;
    // Y-value to draw
    private Float yValueXPos;
    // Functions references elements
    private JComponent[] elements;
    private JComponent[] checkBoxes;
    // Plot size
    private int height, width;
    // Functions reference panel
    private WebPanel references;
    private WebLabel referencesLabel;
    // Plot Margins
    public final int LEFT_MARGIN = 54;
    public final int RIGHT_MARGIN;
    public final int TOP_MARGIN = 15; 
    public final int BOTTOM_MARGIN = 54;
    // Grid and Ticks Intervals
    private final int X_INTERVALS = 10;
    private final int X_TICKS_INTERVAL = 4;
    private final int Y_INTERVALS = 10;
    private final int Y_TICKS_INTERVAL = 4;
    // Colors
    private final Color BACKGROUND_COLOR = new Color(255,255,255);
    private final Color PLOT_BG_COLOR = new Color(255,255,255);
    private final Color AXIS_COLOR = new Color(0,0,0);
    private final Color AXIS_LIMIT_COLOR = new Color(150,150,150);
    private final Color TICKS_COLOR = new Color(0,0,0);
    private final Color NUMBERS_COLOR = new Color(0,0,0);
    private final Color BIG_GRID_COLOR = new Color(240,240,240);
    private final Color SMALL_GRID_COLOR = new Color(248,248,248);
    private final Color AXIS_NAME_COLOR = new Color(0,0,0);
    private final Color BASELINE_COLOR = new Color(255,20,10);
    // Curves Colors
    private Color[] curveColors = {new Color(71,20,55), new Color(131, 48, 12),
                                   new Color(0,169,19), new Color(81,39,137),
                                   new Color(41,122,167), new Color(56,97,182),
                                   new Color(65,146,83), new Color(6,145,54),
                                   new Color(245,126,36), new Color(14,14,168),
                                   new Color(183,58,216), new Color(121,125,24)};
    // Axis Labels
    private String yaxisName = Assets.getString("AXIS_Y");
    private String xaxisName = Assets.getString("AXIS_X");
    // Paint Status
    private boolean paintBackground = true;
    private boolean fillCurve = true;
    private boolean drawIntegrationArea = true;
    private boolean drawBaseline = true;
    private boolean drawPeaksData = true;
    private boolean drawYValue = false;
    // Curve Integration Area
    private List<Pair<Float,Float>> integrationArea;
    // Maximum pos-x y-value  + Peak Number
    private List<Triplet<Float,Float,Integer>> peaksData;
    
    /**
     * Private plot panel constructor
     * @param lp integration area points
     * @param nm functions names
     * @param fn functions to plot
     */
    private Plotter(List<Pair<Float,Float>> lp, String[] nm, List<Pair<Float,Float>> ... fn) {
        // Save functions
        functions = fn;
        // Save functions names
        names = nm;
        // Save integration area
        integrationArea = lp;
        // Init right margin        
        RIGHT_MARGIN = (fn.length > 1)? 200 : 20;
        // If more functions that colors: Random Color
        if (fn.length > curveColors.length) {
            int oldSize = curveColors.length;
            curveColors = Arrays.copyOf(curveColors, functions.length);
            Arrays.fill(curveColors, oldSize - 1, curveColors.length, new Color((int)(Math.random() * 0x1000000)));
        }
        // enable all functions
        show = new boolean[fn.length];
        Arrays.fill(show, true);
        // Init Components
        initPanel();
        // Init functions reference panel
        initReferecesPanel();
    }
    
    /**
     * Create Plot Panel
     * @param fn function to plot
     * @param nm function name
     */
    public Plotter(List<Pair<Float,Float>> fn, String nm) {
        this(null, new String[]{nm}, fn);
    }
    
    /**
     * Create Plot Panel
     * @param lp baseline points list
     * @param fn function to plot
     * @param nm function name
     */
    public Plotter(List<Pair<Float,Float>> fn, List<Pair<Float,Float>> lp, String nm) {
        this(lp, new String[]{nm}, fn);
    }
    
    /**
     * Create Plot Panel with peaks data (maximum positions-values and peaks numbers)
     * @param fn function to plot
     * @param lp baseline points
     * @param nm function name
     * @param peaksData peaks data
     */
    public Plotter(List<Pair<Float,Float>> fn, List<Pair<Float,Float>> lp, String nm, List<Triplet<Float,Float,Integer>> peaksData) {
        this(lp, new String[]{nm}, fn);
        this.peaksData = peaksData;
    }
    
    /**
     * Create Multi-Plot panel
     * @param fn functions to plot
     * @param nm functions names
     */
    public Plotter(List<Pair<Float,Float>>[] fn, String[] nm) {
        this(null, nm, fn);
    }
    
    /**
     * Init Panel Values.
     */    
    private void initPanel() {
        // Search max-values
        for (List<Pair<Float,Float>> fn : functions) {
            // Check  x order values
            if (!checkOrder(fn))
                throw  new IllegalArgumentException("Invalid functions: not ascending order");
            // Max X-Value
            float x = fn.get(fn.size() - 1).getFirst();
            if (x > maxX)
                maxX = x;
            x = fn.get(0).getFirst();
            if (x < minX)
                minX = x;
            // Max Y-Value
            for (Pair<Float,Float> p: fn) {
                float y = p.getSecond();
                if (y > maxY)
                    maxY = y;
                if (y < minY)
                    minY = y;
            }
        }
        // Excess for y value
        maxY += maxY * 0.08; // 8% excess
        // Round values ??
        maxY = Math.round(maxY);
        minY = Math.round(minY);
        minX = Math.round(minX);
        maxX = Math.round(maxX);
    }
    
    /**
     * Init Functions references panel.
     */
    private void initReferecesPanel() {
        // Check functions count
        if (functions.length <= 1) return;
        // Reference to this plotter panel
        Plotter owner = this;
        // Refereces panel
        references = new WebPanel(true);
        references.setPaintFocus(false);
        references.setMargin(10, 2, 10, 2);
        references.setPaintBackground(true);
        references.setShadeWidth(0);
        references.setRound(0);
        references.setBackground(BACKGROUND_COLOR);
        references.setWebColoredBackground(false);
        references.setLayout(new VerticalFlowLayout(5, 5));
        // References label
        referencesLabel = new WebLabel("<HTML><U>" + Assets.getString("REFERENCES") + ":</U></HTML>", WebLabel.CENTER);
        references.add(referencesLabel);
        elements = new JComponent[functions.length];
        checkBoxes = new JComponent[functions.length];
        // For each function draw name and color
        for (int i = 0; i < functions.length; i++) {
            // Color chooser
            final WebButton colorChooserButton = new WebButton(ImageUtils.createColorIcon(curveColors[i]));
            colorChooserButton.setToolTip(Assets.getString("CHANGE_COLOR"));
            colorChooserButton.setUndecorated(true);
            colorChooserButton.setMargin(2, 2, 2, 2);
            final int index = i;
            // Button listener
            colorChooserButton.addActionListener(new ActionListener() {
                private WebColorChooserDialog colorChooser = null;
                private Color lastColor = curveColors[index];
                // On button click
                @Override
                public void actionPerformed(final ActionEvent e) {
                    // Show color chooser
                    if (colorChooser == null)
                        colorChooser = new WebColorChooserDialog(owner);
                    colorChooser.setColor(lastColor);
                    colorChooser.setVisible(true);
                    // When color has choosed
                    if (colorChooser.getResult() == DialogOptions.OK_OPTION) {
                        final Color color = colorChooser.getColor();
                        colorChooserButton.setIcon(ImageUtils.createColorIcon(color));
                        lastColor = color;
                        // Update function color
                        curveColors[index] = color;
                        // Repaint functions
                        owner.repaint();
                    }
                }
            });
            // Animated check box
            WebCheckBox checkBox = new WebCheckBox(show[i]);
            checkBoxes[i] = checkBox;
            checkBox.setToolTip(Assets.getString("SHOW_HIDE"));
            checkBox.addChangeListener((ChangeEvent e) -> {
                show[index] = checkBox.isSelected();
                // Repaint functions
                owner.repaint();
            });
            // Name label
            WebLabel label = new WebLabel(names[i], WebLabel.LEFT);
            label.setMinimumWidth(130);
            label.setMaximumWidth(130);
            // Create container element
            WebPanel element = new WebPanel(true);
            element.setPaintFocus(false);
            element.setPaintBackground(false);
            element.setPaintSideLines(false, false, false, false);
            element.setPaintSides(false, false, false, false);
            element.setShadeWidth(0);
            element.setRound(0);
            element.setLayout(new BoxLayout(element, BoxLayout.X_AXIS));
            element.add(colorChooserButton);
            element.add(Box.createRigidArea(new Dimension(5,0)));
            element.add(label);
            element.add(Box.createHorizontalGlue());
            element.add(checkBox);
            // Function position up buttom
            /*WebButton up = WebButton.createIconWebButton(new ImageIcon(WebSpinnerUI.class.getResource("icons/up.png")), StyleConstants.smallRound, 1, 1);
            up.setDrawFocus(false);
            up.setFocusable(false);
            up.setDrawBottom(false);
            up.addActionListener((ActionEvent e) -> {

            });
            // Function position down buttom
            WebButton down = WebButton.createIconWebButton(new ImageIcon(WebSpinnerUI.class.getResource("icons/down.png")), StyleConstants.smallRound, 1, 1);
            down.setDrawTop(false);
            down.setDrawFocus(false);
            down.setFocusable(false);
            down.addActionListener((ActionEvent e) -> {

            });
            // Function position change panel
            WebPanel position = new WebPanel(new GridLayout(2, 1));
            position.add(up);
            position.add(down);
            // add position change panel
            element.add(position);*/
            // Only draw enabled functions
            element.setVisible(show[i]);
            // Save element reference
            elements[i] = element;
            // Add current element
            references.add(element);
        }
        // Add references panel
        this.add(references);
    }
    
    /**
     * Print plot component to imageplus
     * @param imgWidth image width
     * @param imgHeight image height
     * @return 
     */
    public ImagePlus getImagePlus(int imgWidth, int imgHeight) {
        // Try to create graphic context for this component
        if (getGraphics() == null) new WebFrame().add(this);
        // BufferedImage Painter
        BufferedImage image = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_ARGB);
        // Save old component size
        int oldw = width, oldh = height;
        // Configure for image print
        boolean oldpbg = paintBackground;
        paintBackground = true;
        // Hide Elements and checkboxes
        if (functions.length > 1) {
            for (int i = 0; i < functions.length; i++) {
                checkBoxes[i].setVisible(false);
                elements[i].setVisible(show[i]);
            }
        }
        // Update Size and Scale
        setScale(imgWidth, imgHeight);
        // Draw resized plot in image
        paintComponent(image.getGraphics());
        // Draw references panel
        if (functions.length > 1) {
            Graphics g = image.getGraphics();
            g.setClip(0, 0, width, height);
            g.translate(references.getLocation().x, references.getLocation().y);
            references.paintAll(g);
        }
        // Show Elements and checkboxes
        if (functions.length > 1) {
            for (int i = 0; i < functions.length; i++) {
                checkBoxes[i].setVisible(true);
                elements[i].setVisible(true);
            }
        }
        // Restore size and scale
        setScale(oldw, oldh);
        // Restore settings
        paintBackground = oldpbg;
        // Return image plus
        return new ImagePlus(Assets.getString("PLOT_IMAGE"), image);
    }
    
    /**
     * Set plot component Size
     * @param dim 
     */
    @Override
    public void setSize(Dimension dim) {
        setSize(dim.width, dim.height);
    }
    
    /**
     * Set plot component Size
     * @param width component width
     * @param height component height
     */
    @Override
    public void setSize(int width, int height) {
        // Set && save size
        super.setSize(width, height);
        setScale(width, height);
    }
    
    /**
     * Update components scale accord to component size
     * @param width component width
     * @param height component height
     */
    private void setScale(int width, int height) {
        // Save width && height for faster access
        this.width = width;
        this.height = height;
        // set y-axis scale
        if (maxY - minY != 0.0f)
            yScale = (double)(height - TOP_MARGIN - BOTTOM_MARGIN) / (maxY - minY);
        // set x-axis scale
        if (maxX - minX != 0.0f)
            xScale = (double)(width - LEFT_MARGIN - RIGHT_MARGIN) / (maxX - minX);
        // References panel
        if (functions.length > 1) {            
            // References panel location
            references.setLocation(width - RIGHT_MARGIN + 8, TOP_MARGIN);
            // References panel size
            int visibles = (int)Arrays.stream(elements).filter(x -> x.isVisible()).count();
            references.setSize(200 - 15, 25 * visibles + 35);
        }
    }
    
    /**
     * Set plot peaks points
     * @param lp list of start/end points in same magnitud that curve values
     */
    public void setIntegrationAreas(List<Pair<Float,Float>> lp) {
        integrationArea = lp;
    }

    /**
     * Set Y-Value to draw
     * @param xPos
     */
    public void setYValueXPos(Float xPos) {
        this.yValueXPos = xPos;
    }
    
    /**
     * Enable/disable paint background color
     * @param status true/false
     */
    public void setPaintBackground(boolean status) {
        paintBackground = status;
    }
    
    /**
     * Fill Polyline curve with alpha color
     * @param status true/false
     */
    public void setFillCurve(boolean status) {
        fillCurve = status;
    }
    
    /**
     * Get Fill Polyline curve with alpha color status
     * @return true/false
     */
    public boolean getFillCurve() {
        return fillCurve;
    }
    
    /**
     * Fill Polyline area between baselien point with alpha color
     * @param status true/false
     */
    public void setFillArea(boolean status) {
        drawIntegrationArea = status;
    }

    /**
     * Enable/Disable baseline draw
     * @param status true/false
     */
    public void setDrawBaseline(boolean status) {
        this.drawBaseline = status;
    }
    
    /**
     * Enable/Disable peaks data draw
     * @param status 
     */
    public void setDrawPeaksData(boolean status) {
        this.drawPeaksData = status;
    }
    
    /**
     * Enable/Disable Y-Value draw
     * @param status 
     */
    public void setDrawYValue(boolean status) {
        this.drawYValue = status;
    }
    
    /**
     * Set Plotter texts values
     * @param values 
     */
    public void setTexts(String... values) {        
        yaxisName = values[0];
        xaxisName = values[1];        
        if (values.length == 3)
            referencesLabel.setText("<HTML><U>" + values[2] + ":</U></HTML>");
    }
    
    /**
     * Set Axis Labels
     * @param axisY
     * @param axisX 
     */
    public void setAxisLabels(String axisY, String axisX) {
        yaxisName = axisY;
        xaxisName = axisX;
    }
    
    /**
     * Set Axis-X Label
     * @param axisX 
     */
    public void setAxisXLabel(String axisX) {
        xaxisName = axisX;
    }
    
    /**
     * Set Axis-Y Label
     * @param axisY
     */
    public void setAxisYLabel(String axisY) {
        yaxisName = axisY;
    }

    /**
     * Get Peaks data
     * @return 
     */
    public List<Triplet<Float,Float,Integer>> getPeaksData() {
        return peaksData;
    }

    /**
     * Set Peaks data
     * @param peaskData 
     */
    public void setPeaksData(List<Triplet<Float,Float,Integer>> peaskData) {
        this.peaksData = peaskData;
    }
    
    /**
     * Paint Plotter and top components
     * @param g 
     */
    @Override
    public void paint(Graphics g) {
        super.paintChildren(g);
        paintComponent(g);
    }
    
    /**
     * Paint Plot Components
     * @param g 
     */
    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // Draw Component background color !!
        if (paintBackground) {
            g2d.setColor(BACKGROUND_COLOR);
            g2d.fillRect(0, 0, width, height);
        }
        // Draw curve plot background color
        g2d.setColor(PLOT_BG_COLOR);
        g2d.fillRect(LEFT_MARGIN, TOP_MARGIN, width - RIGHT_MARGIN - LEFT_MARGIN, height - BOTTOM_MARGIN - TOP_MARGIN);
        // Draw Background grid
        drawSmallGrid(g);
        drawBigGrid(g);
        // Draw Curves
        for (int i = 0; i < functions.length; i++)
            if (show[i])
                drawCurve(g, functions[i], LEFT_MARGIN, height - BOTTOM_MARGIN, curveColors[i], fillCurve);
        if (functions.length == 1) {
            // Draw Integration Areas and baseline if necessary
            if (integrationArea != null && (drawBaseline || drawIntegrationArea))
                drawIntegrationAreasAndBaseline(g2d, curveColors[0]);
            // Draw Peaks data if necessary
            if (peaksData != null && drawPeaksData)
                drawPeaksData(g);
        }
        drawAxis(g);
        // Draw Main Axis
        drawBigTicks(g);
        drawSmallTicks(g);
        drawAxisNumbers(g);
        // Draw Labels
        drawAxisLabels(g2d);
        // Draw Specific y-value over Y axis
        if (functions.length == 1 && yValueXPos != null && drawYValue)
            drawYValue(g);   
    }
    
    /**
     * Draw Polyline Curve
     * @param g
     * @param xValues x-points values
     * @param yValues y-points values
     * @param xOffset x-start offset
     * @param yOffset y-start offset
     * @param color curve line and fill color
     * @param fill enable/disable curve fill
     */
    private void drawCurve(Graphics g, List<Pair<Float,Float>> fn, int xOffset, int yOffset, Color color, boolean fill) {
        // Integer x-values/y-values
        int xPoints[] = new int[fn.size() + (fill? 2 : 0)];
        int yPoints[] = new int[fn.size() + (fill? 2 : 0)];
        for (int i = 0; i < fn.size(); i++) {
            Pair<Float, Float> pair = fn.get(i);
            xPoints[i + (fill? 1 : 0)] = xOffset + (int)((pair.getFirst() - minX) * xScale);
            yPoints[i + (fill? 1 : 0)] = yOffset - (int)((pair.getSecond() - minY) * yScale);
        }
        // Extends X/Y to fill polygon
        if (fill) {
            xPoints[0] = xOffset + (int)(minX * xScale);
            yPoints[0] = yOffset - (int)(minY * yScale);
            xPoints[xPoints.length - 1] = xOffset + (int)((maxX - minX) * xScale);
            yPoints[yPoints.length - 1] = yOffset - (int)(minY * yScale);
        }
        // Draw the curve
        if (fill) {
            g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 50));
            g.fillPolygon(xPoints, yPoints, xPoints.length);
        }
        g.setColor(color);
        g.drawPolyline(xPoints, yPoints, xPoints.length);
    }
    
    /**
     * Draw Curve integration areas and baseline
     * @param g
     * @param color Curve color
     */
    private void drawIntegrationAreasAndBaseline(Graphics2D g, Color color) {
        // Baseline relative values (between 0..1)
        List<Pair<Float,Float>> fn = functions[0];
        // Quick fix, check indexs (for a bug when load other proyect and plotter is in the panel)
        if (fn.size() - 1 < 0) return;
        // Max X-Value
        float max_x = fn.get(fn.size() - 1).getFirst();
        // Draw Integration Areas Polygons and Baseline
        for (Pair<Float,Float> p: integrationArea) {
            // Avoid empty areas
            if (p.getSecond() - p.getFirst() == 0) continue;
            // Compute Indexs
            int i = Math.round(((fn.size() - 1) * (p.getFirst() / max_x)));
            int j = Math.round(((fn.size() - 1) * (p.getSecond() / max_x)));
            // Pair's
            Pair<Float, Float> pi = fn.get(i);
            Pair<Float, Float> pj = fn.get(j);
            // X Points
            int x1 = LEFT_MARGIN + (int)(pi.getFirst() * xScale);
            int x2 = LEFT_MARGIN + (int)(pj.getFirst() * xScale);
            // Y Points
            int y1 = (height - BOTTOM_MARGIN) - (int)(pi.getSecond() * yScale);
            int y2 = (height - BOTTOM_MARGIN) - (int)(pj.getSecond() * yScale);
            // Draw if need the curve integration areas
            if (drawIntegrationArea) {
                // Curve Fill Polygon
                int[] xPoints = new int[j - i + 3];
                int[] yPoints = new int[j - i + 3];
                // Compute Values
                for (int n = i; n <= j; n++) {
                    Pair<Float, Float> pair = fn.get(n);
                    xPoints[n - i + 1] = LEFT_MARGIN + (int)((pair.getFirst() - minX) * xScale);
                    yPoints[n - i + 1] = height - BOTTOM_MARGIN - (int)((pair.getSecond() - minY) * yScale);
                }
                // Limits Values
                xPoints[0] = xPoints[1];
                yPoints[0] = height - BOTTOM_MARGIN  - (int)(minY * yScale);
                //
                xPoints[xPoints.length - 1] = xPoints[xPoints.length - 2];
                yPoints[yPoints.length - 1] = height - BOTTOM_MARGIN - (int)(minY * yScale);
                // Save Curve Polygon
                Polygon polyCurve = new Polygon(xPoints, yPoints, xPoints.length);
                // Baseline Polygon
                int[] xPoints2 = new int[]{x1,x1,x2,x2};
                int[] yPoints2 = new int[]{height - BOTTOM_MARGIN, y1, y2, height - BOTTOM_MARGIN};
                Polygon basePoly = new Polygon(xPoints2, yPoints2, xPoints2.length);
                // Polygon Result
                Area polyRes = new Area(polyCurve);
                polyRes.subtract(new Area(basePoly));
                // Area Color
                g.setColor(new Color(color.getRed(),color.getGreen(),color.getBlue(), 90));
                // Draw Polygon
                g.fill(polyRes);
            }
            // Draw if need the curve integration baseline
            if (drawBaseline) {
                // Line Color
                g.setColor(BASELINE_COLOR);
                // Draw Line
                g.drawLine(x1, y1, x2, y2);
                // Draw Dots
                g.fillOval(x1 - 2, y1 - 2, 4, 4);
                g.fillOval(x2 - 2, y2 - 2, 4, 4);
            }
        }
    }
    
    /**
     * Draw data for each peak
     * @param g 
     */
    private void drawPeaksData(Graphics g) {
        g.setColor(Color.BLACK);
        FontMetrics metrics = g.getFontMetrics(getFont());
        // Baseline relative values (between 0..1)
        for (Triplet<Float,Float,Integer> data: peaksData) {
            Integer peakNumber = data.getThird();
            Float peakPosition = data.getFirst();
            Float peakMaximum = data.getSecond();
            // X-Y Points
            int x = LEFT_MARGIN + (int)(peakPosition * xScale);
            int y = (height - BOTTOM_MARGIN) - (int)(peakMaximum * yScale) - 2;
            //
            int xpoints[] = new int[]{x, x - 5, x + 5};
            int ypoints[] = new int[]{y, y - 5, y - 5};
            // Maximum value triangle
            g.fillPolygon(xpoints, ypoints, 3);
            // Peak position
            String txt = String.valueOf(peakNumber);
            Rectangle rt = metrics.getStringBounds(txt, g).getBounds();
            g.drawString(txt, x  - rt.width / 2, y - 6);
        }
    }
    
    /**
     * Draw a value over the Y-Axis (Number in a square)
     * @param g 
     */
    private void drawYValue(Graphics g) {
        // Max X-Value
        float max_x = functions[0].get(functions[0].size() - 1).getFirst();
        Pair<Float, Float> point = functions[0].get(Math.round(((functions[0].size() - 1) * (yValueXPos / max_x))));
        // Y-Value
        double yvalue = Math.round(point.getSecond() * 100.0) / 100.0;
        // Text
        String value = String.valueOf(yvalue);
        int yp = height - BOTTOM_MARGIN - Math.round((float)(yvalue * yScale));
        FontMetrics metrics = g.getFontMetrics(getFont());
        Rectangle rt = metrics.getStringBounds(value, g).getBounds();
        // Square
        g.setColor(PLOT_BG_COLOR);
        g.fillRect(LEFT_MARGIN - rt.width - 8, yp - rt.height + metrics.getMaxDescent() + 2, rt.width + 4, rt.height + 4);
        g.setColor(TICKS_COLOR);
        g.drawRect(LEFT_MARGIN - rt.width - 8, yp - rt.height + metrics.getMaxDescent() + 2, rt.width + 4, rt.height + 4);
        // Line
        int ypos = (yp - rt.height + metrics.getMaxDescent() + 2) + ((rt.height + 4) / 2) + 1;
        g.drawLine(LEFT_MARGIN - 4, ypos, LEFT_MARGIN, ypos);
        // Text
        g.setColor(NUMBERS_COLOR);
        g.drawString(value, LEFT_MARGIN - rt.width - 6, yp + rt.height / 2  - metrics.getMaxDescent());
    }
    
    /**
     * Draw Main Axis Rectangle
     * @param g 
     */
    private void drawAxis(Graphics g) {
        g.setColor(AXIS_COLOR);
        g.drawLine(LEFT_MARGIN, TOP_MARGIN, LEFT_MARGIN, height - BOTTOM_MARGIN);
        g.drawLine(LEFT_MARGIN, height - BOTTOM_MARGIN, width - RIGHT_MARGIN, height - BOTTOM_MARGIN);
        g.setColor(AXIS_LIMIT_COLOR);
        g.drawLine(width - RIGHT_MARGIN, TOP_MARGIN, width - RIGHT_MARGIN, height - BOTTOM_MARGIN);
        g.drawLine(LEFT_MARGIN, TOP_MARGIN, width - RIGHT_MARGIN, TOP_MARGIN);
    }
    
    /**
     * Draw Axis Numbers
     * @param g 
     */
    private void drawAxisNumbers(Graphics g) {
        g.setColor(NUMBERS_COLOR);
        FontMetrics metrics = g.getFontMetrics(getFont());
        // Y-Numbers
        double sp = niceNumber((maxY - minY) / Y_INTERVALS, true);
        for (double i = minY; i <= maxY; i = exactSum(i,sp)) {
            int ypos = BOTTOM_MARGIN + (int)((i * yScale) + 0.5);
            String txt = isInteger(i)? Integer.toString((int)(i)) : Double.toString(i);
            Rectangle rt = metrics.getStringBounds(txt, g).getBounds();
            g.drawString(txt, LEFT_MARGIN - rt.width - 5, height - ypos + rt.height / 2 - metrics.getMaxDescent());
        }
        // X-Numbers
        sp = niceNumber((maxX - minX) / X_INTERVALS, true);
        for (double i = minX; i <= maxX; i = exactSum(i,sp)) {
            int xPos = LEFT_MARGIN + (int)((i * xScale) + 0.5);
            String txt = isInteger(i)? Integer.toString((int)(i)) : Double.toString(i);
            Rectangle rt = metrics.getStringBounds(txt, g).getBounds();
            g.drawString(txt, xPos - rt.width / 2, height - BOTTOM_MARGIN + rt.height);
        }
    }
    
    /**
     * Draw Big Ticks lines
     * @param g 
     */
    private void drawBigTicks(Graphics g) {
        g.setColor(TICKS_COLOR);
        // Y-Ticks
        double sp = niceNumber((maxY - minY) / Y_INTERVALS, true);
        for (double i = minY; i <= maxY; i = exactSum(i,sp)) {
            int ypos = BOTTOM_MARGIN + (int)((i * yScale) + 0.5);
            g.drawLine(LEFT_MARGIN, height - ypos, LEFT_MARGIN + 6, height - ypos);
        }
        // X-Ticks
        sp = niceNumber((maxX - minX) / X_INTERVALS, true);
        for (double i = minX; i <= maxX; i = exactSum(i,sp)) {
            int xpos = LEFT_MARGIN + (int)((i * xScale) + 0.5);
            g.drawLine(xpos, height - BOTTOM_MARGIN - 6, xpos, height - BOTTOM_MARGIN);
        }
    }
    
    /**
     * Draw Small Ticks lines
     * @param g 
     */
    private void drawSmallTicks(Graphics g) {
        g.setColor(TICKS_COLOR);
        // Draw Horizontal Lines
        double sp = niceNumber((maxY - minY) / Y_INTERVALS, true);
        double tick = niceNumber(sp / Y_TICKS_INTERVAL, true);
        for (double i = minY; i <= maxY; i = exactSum(i,sp)) {
            for (double j = 0; j < sp; j = exactSum(j,tick)) {
                if (exactSum(i,j) > maxY)
                    break;
                int yPos = BOTTOM_MARGIN + (int)(((i + j) * yScale) + 0.5);
                g.drawLine(LEFT_MARGIN, height - yPos, LEFT_MARGIN + 3, height - yPos);
            }
        }
        // Draw Vertical Lines
        sp = niceNumber((maxX - minX) / X_INTERVALS, true);
        tick = niceNumber(sp / X_TICKS_INTERVAL, true);
        for (double i = minX; i <= maxX; i = exactSum(i,sp)) {
            for (double j = 0; j < sp; j = exactSum(j,tick)) {
                if (exactSum(i,j) > maxX)
                    break;
                int xPos = LEFT_MARGIN + (int)(((i + j) * xScale) + 0.5);
                g.drawLine(xPos, height - BOTTOM_MARGIN - 3, xPos, height - BOTTOM_MARGIN);
            }
        }
    }
    
    /**
     * Draw Big Grid lines
     * @param g 
     */
    private void drawBigGrid(Graphics g) {
        g.setColor(BIG_GRID_COLOR);
        // Draw Horizontal Lines
        double sp = niceNumber((maxY - minY) / Y_INTERVALS, true);
        for (double i = minY; i < maxY; i = exactSum(i,sp)) {
            int ypos = BOTTOM_MARGIN + (int)((i * yScale) + 0.5);
            g.drawLine(LEFT_MARGIN, height - ypos, width - RIGHT_MARGIN, height - ypos);
        }
        // Draw Vertical Lines
        sp = niceNumber((maxX - minX) / X_INTERVALS, true);
        for (double i = minX; i < maxX; i = exactSum(i,sp)) {
            int xPos = LEFT_MARGIN + (int)((i * xScale) + 0.5);
            g.drawLine(xPos, height - BOTTOM_MARGIN, xPos, TOP_MARGIN);
        }
    }
    
    /**
     * Draw Small Grid lines
     * @param g 
     */
    private void drawSmallGrid(Graphics g) {
        g.setColor(SMALL_GRID_COLOR);
        // Draw Horizontal Lines
        double sp = niceNumber((maxY - minY) / Y_INTERVALS, true);
        double tick = niceNumber(sp / Y_TICKS_INTERVAL, true);
        for (double i = minY; i <= maxY; i = exactSum(i,sp)) {
            for (double j = 0; j < sp; j = exactSum(j,tick)) {
                if (exactSum(i,j) > maxY)
                    break;
                int yPos = BOTTOM_MARGIN + (int)(((i + j) * yScale) + 0.5);
                g.drawLine(LEFT_MARGIN, height - yPos, width - RIGHT_MARGIN, height - yPos);
            }
        }
        // Draw Vertical Lines
        sp = niceNumber((maxX - minX) / X_INTERVALS, true);
        tick = niceNumber(sp / X_TICKS_INTERVAL, true);
        for (double i = minX; i <= maxX; i = exactSum(i,sp)) {
            for (double j = 0; j < sp; j = exactSum(j,tick)) {
                if (exactSum(i,j) > maxX)
                    break;
                int xPos = LEFT_MARGIN + (int)(((i + j) * xScale) + 0.5);
                g.drawLine(xPos, TOP_MARGIN, xPos, height - BOTTOM_MARGIN);
            }
        }
    }
    
    /**
     * Draw Axis labels
     * @param g2d 
     */
    private void drawAxisLabels(Graphics2D g2d) {
        g2d.setColor(AXIS_NAME_COLOR);
        // Horizontal Text
        FontMetrics metrics = g2d.getFontMetrics(getFont());
        Rectangle rt = metrics.getStringBounds(xaxisName, g2d).getBounds();
        int y = height - rt.height / 2;
        int x = (width - LEFT_MARGIN - RIGHT_MARGIN) / 2 - rt.width / 2 + LEFT_MARGIN;
        g2d.drawString(xaxisName, x, y);
        // Vertical Text
        rt = metrics.getStringBounds(yaxisName, g2d).getBounds();
        y = (height - TOP_MARGIN - BOTTOM_MARGIN) / 2 + rt.width / 2 + TOP_MARGIN;
        x = LEFT_MARGIN / 3;
        AffineTransform transform = g2d.getTransform();
        g2d.translate(x, y);
        g2d.rotate(Math.toRadians(-90));
        g2d.drawString(yaxisName, 0, 0);
        g2d.setTransform(transform);
    }
    
    /**
     * Reference: Paul Heckbert, "Nice Numbers for Graph Labels", Graphics Gems, pp 61-63.
     * Finds a "nice" number approximately equal to x.
     * @param x target number
     * @param round If non-zero, round. Otherwise take ceiling of value.
     * @return nice number
     */
    private double niceNumber(double x, boolean round) {
        //   expt -- Exponent of x
        double expt = Math.floor(Math.log10(x));
        //   frac -- Fractional part of x
        double frac = x / Math.pow(10.0f, expt);
        //   nice -- Nice, rounded fraction
        double nice;
        if (round) {
            if (frac < 1.5) {
                nice = 1.0;
            } else if (frac < 3.0) {
                nice = 2.0;
            } else if (frac < 7.0) {
                nice = 5.0;
            } else {
                nice = 10.0;
            }
        } else {
            if (frac <= 1.0) {
                nice = 1.0;
            } else if (frac <= 2.0) {
                nice = 2.0;
            } else if (frac <= 5.0) {
                nice = 5.0;
            } else {
                nice = 10.0;
            }
        }
        return nice * Math.pow(10.0f, expt);
    }
    
    /**
     * Exact Sum of double values
     * @param a First double value
     * @param b double value to sum
     * @return sum of a + b whit 64bits decimal precision
     */
    private double exactSum(double a, double b) {
        return (new BigDecimal(a).add(new BigDecimal(b), MathContext.DECIMAL64)).doubleValue();
    }
    
    /**
     * Exact Multiplication of double values
     * @param a First double value
     * @param b double value to mult
     * @return multiplication of a * b whit 64bits decimal precision
     */
    private double exactMult(double a, double b) {
        return (new BigDecimal(a).multiply(new BigDecimal(b), MathContext.DECIMAL64)).doubleValue();
    }
    
    /**
     * Check if float is a integer.
     * @param n
     * @return 
     */
    private boolean isInteger(float n) {
        return n == Math.round(n);
    }
    
    /**
     * Check if double is a integer
     * @param n
     * @return 
     */
    private boolean isInteger(double n) {
        return n == Math.round(n);
    }
    
    /**
     * Check ascending order
     * @param array
     * @return 
     */
    private boolean checkOrder(List<Pair<Float,Float>> fn) {
        for (int i = 0; i < fn.size() - 1; i++) {
            if (fn.get(i).getFirst() > fn.get(i + 1).getFirst())
                return false;
        }
        return true;
    }
}