/*
 * @(#)AquaMultiThumbSliderUI.java
 *
 * $Date$
 *
 * Copyright (c) 2015 by Jeremy Wood.
 * All rights reserved.
 *
 * The copyright of this software is owned by Jeremy Wood. 
 * You may not use, copy or modify this software, except in  
 * accordance with the license agreement you entered into with  
 * Jeremy Wood. For details see accompanying license terms.
 * 
 * This software is probably, but not necessarily, discussed here:
 * https://javagraphics.java.net/
 * 
 * That site should also contain the most recent official version
 * of this software.  (See the SVN repository for more details.)
 */
package jtlc.view.components.slider;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.SwingConstants;

import com.bric.geom.ShapeBounds;
import com.bric.plaf.PlafPaintUtils;
import java.util.Arrays;

/**
 * A <code>MultiThumbSliderUI</code> designed to resemble <code>JSliders</code>
 * on Mac 10.10 (Yosemite).
 *
 * @param <T> the parameter for the <code>MultiThumbSlider</code>.
 * 
 * <!-- ======== START OF AUTOGENERATED SAMPLES ======== -->
 * <p>Here are samples demonstrating different possible thumbs:
 * <table summary="Resource&#160;Samples&#160;for&#160;com.bric.plaf.AquaMultiThumbSliderUI"><tr>
 * <td>Thumb.Circle</td>
 * <td><img src="https://javagraphics.java.net/resources/samples/AquaMultiThumbSliderUI/sample.png" alt="com.bric.plaf.DefaultMultiThumbSliderUI.createDemo(&#160;com.bric.plaf.AquaMultiThumbSliderUI.class,&#160;com.bric.plaf.MultiThumbSliderUI$Thumb.Circle&#160;)"></td>
 * </tr><tr>
 * <td>Thumb.Triangle</td>
 * <td><img src="https://javagraphics.java.net/resources/samples/AquaMultiThumbSliderUI/sample2.png" alt="com.bric.plaf.DefaultMultiThumbSliderUI.createDemo(&#160;com.bric.plaf.AquaMultiThumbSliderUI.class,&#160;com.bric.plaf.MultiThumbSliderUI$Thumb.Triangle&#160;)"></td>
 * </tr><tr>
 * <td>Thumb.Rectangle</td>
 * <td><img src="https://javagraphics.java.net/resources/samples/AquaMultiThumbSliderUI/sample3.png" alt="com.bric.plaf.DefaultMultiThumbSliderUI.createDemo(&#160;com.bric.plaf.AquaMultiThumbSliderUI.class,&#160;com.bric.plaf.MultiThumbSliderUI$Thumb.Rectangle&#160;)"></td>
 * </tr><tr>
 * <td>Thumb.Hourglass</td>
 * <td><img src="https://javagraphics.java.net/resources/samples/AquaMultiThumbSliderUI/sample4.png" alt="com.bric.plaf.DefaultMultiThumbSliderUI.createDemo(&#160;com.bric.plaf.AquaMultiThumbSliderUI.class,&#160;com.bric.plaf.MultiThumbSliderUI$Thumb.Hourglass&#160;)"></td>
 * </tr><tr>
 * </tr></table>
 * <!-- ======== END OF AUTOGENERATED SAMPLES ======== -->
 */
public class CustomSliderAquaUI<T> extends CustomSliderUI<T> {
	
    private static final Color UPPER_GRAY = new Color(168,168,168);
    private static final Color LOWER_GRAY = new Color(218, 218, 218);
    private static final Color OUTLINE_OPACITY = new Color(0,0,0,75);

    private final int FOCUS_PADDING = 2;
    private final Color trackHighlightColor = new Color(0x3a99fc);
    private final Color selectedBorderColor = new Color(0, 45, 226, 255);
    private final Color selectedInnerColor = new Color(80, 115, 240, 255);
    private final Color squareColor = new Color(58, 153, 252, 50);
    private final Color borderColor = new Color(58, 100, 255, 255);
    

    public CustomSliderAquaUI(CustomSlider<T> slider) {
        super(slider);
        DEPTH = 4;
    }

    protected Shape getTrackOutline() {
        trackRect = calculateTrackRect();
        float k = 4;
        int z = 3;
        if(slider.getOrientation()==CustomSlider.VERTICAL) {
            return new RoundRectangle2D.Float(trackRect.x, trackRect.y-z, trackRect.width, trackRect.height+2*z, k, k);
        }
        return new RoundRectangle2D.Float(trackRect.x-z, trackRect.y, trackRect.width+2*z, trackRect.height, k, k);
    }

    protected boolean isTrackHighlightActive() {
        return slider.getThumbCount()==2;
    }

    @Override
    protected int getPreferredComponentDepth() {
        return 24;
    }

    @Override
    protected void paintFocus(Graphics2D g) {
        //do nothing, this is really handled in paintThumb now
    }

    /**
     * METODO MODIFICADO
     * @param thumbIndex
     * @return 
     */
    @Override
    protected Dimension getThumbSize(int thumbIndex) {
        Thumb thumb = getThumb(thumbIndex);
        switch(thumb){
            case Hourglass: return new Dimension(5, 16);
            case Triangle: return new Dimension(10, 18); // 16 18;
            case Rectangle: return new Dimension(10, 20);
            default: return new Dimension(16, 16);
        }
    }

    /**
     * METODO MODIFICADO
     * @param g 
     */
    @Override
    protected void paintTrack(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Shape trackShape = getTrackOutline();

        GradientPaint gradient;
        if(slider.getOrientation()==SwingConstants.HORIZONTAL)
            gradient = new GradientPaint(new Point(trackRect.x, trackRect.y), UPPER_GRAY, new Point(trackRect.x, trackRect.y+trackRect.height), LOWER_GRAY);
        else
            gradient = new GradientPaint(new Point(trackRect.x, trackRect.y), UPPER_GRAY, new Point(trackRect.x+trackRect.width, trackRect.y), LOWER_GRAY);
        //
        g.setPaint(gradient);
        g.fill(trackShape);

        paintTrackHighlight(g);

        g.setPaint(OUTLINE_OPACITY);
        g.setStroke(new BasicStroke(1));
        g.draw(trackShape);


        float pos[] = slider.getThumbPositions();
        boolean status[] = new boolean[slider.getThumbCount()];
        Arrays.fill(status, false);
        // Ticks
        if(slider.isPaintTicks()) {
            for(int i = 0; i < slider.getThumbCount(); i++){
                status[i] = paintTick(g,pos[i],13 + slider.getTickStart(), slider.getTickLength(), i > 0? pos[i - 1] : -1, i > 0? status[i - 1] : false);
            }
        }
        // Numbers
        if(slider.isPaintNumbers()){
            for(int i = 0; i < slider.getThumbCount(); i++){
                status[i] = paintNumber(g,pos[i],13 + slider.getTickStart(), slider.getTickLength(), i > 0? pos[i - 1] : -1, i > 0? status[i - 1] : false, false);
            }
        }
        // Squares
        if(slider.isPaintSquares()){
            paintSquare(g,13,slider.getSquareLength(), slider.getSquareStart());
        }
        
        // Paint over selected thumb (number)
        int i = slider.getSelectedThumb();
        if(i != -1 && slider.isPaintNumbers()){
            status[i] = paintNumber(g,pos[i],13 + slider.getTickStart(), slider.getTickLength(), i > 0? pos[i - 1] : -1, i > 0? status[i - 1] : false, true);
        }
    }

    /**
     *  METODO AGREGADO
     * @param g
     * @param d1
     * @param d2
     * @param d3 
     */
    private void paintSquare(Graphics2D g,int d1, int d2, int d3){
        if(slider.getThumbCount() % 2 != 0) return;
        Color aux = g.getColor();
        g.setColor(squareColor);
        float pos[] = slider.getThumbPositions();
        //
        if(slider.getOrientation()==CustomSlider.HORIZONTAL) {
            for (int i = 0; i < slider.getThumbCount(); i += 2) {
                int x = (int)(trackRect.x+trackRect.width*pos[i]+.5f);
                int x2 = (int)(trackRect.x+trackRect.width*pos[i+1]+.5f);
                int y = trackRect.y+trackRect.height+d3;
                g.fillRect(x, y + d1, x2 - x, d2);
                g.drawRect(x, y + d1, x2 - x, d2);
            }
        } else {
            for (int i = slider.getThumbCount() - 1; i >= 0 ; i -= 2) {
                int x = trackRect.x+trackRect.width+d1;
                int y2 = (int)(trackRect.y+trackRect.height*((slider.isInverted())? pos[i]:1-pos[i]));
                int y = (int)(trackRect.y+trackRect.height*((slider.isInverted())? pos[i-1]:1-pos[i-1]));
                g.fillRect(x, (slider.isInverted())? y:y2, d2, ((slider.isInverted())? y2:y) - ((slider.isInverted())? y:y2));
                g.drawRect(x, (slider.isInverted())? y:y2, d2, ((slider.isInverted())? y2:y) - ((slider.isInverted())? y:y2));

            }
        }
        g.setColor(aux);
    }

    /**
     *  METODO AGREGADO
     * @param g
     * @param f
     * @param d1
     * @param d2
     * @param f2
     * @param down
     * @return 
     */
    protected boolean paintNumber(Graphics2D g, float f, int d1, int d2, float f2, boolean down, boolean selected) {
        Color tmp = g.getColor();
        boolean over = false;
        if(slider.getOrientation()==CustomSlider.HORIZONTAL) {
            int x = (int)(trackRect.x+trackRect.width*f+.5f);
            int y = trackRect.y;
            float value = f * slider.getMultValue();
            String pos = (slider.getMultValue() == 1)? String.valueOf(Math.round(value * 1000.0) / 1000.0) : String.valueOf(Math.round(value));
            int sql = g.getFontMetrics().getStringBounds(pos, g).getBounds().width / 2;
            //
            if(f2 != -1 && !down){
                int x2 = (int)(trackRect.x+trackRect.width*f2+.5f);
                float value2 = f2 * slider.getMultValue();
                String pos2 = (slider.getMultValue() == 1)? String.valueOf(Math.round(value2 * 1000.0) / 1000.0) : String.valueOf(Math.round(value2));
                int sql2 = g.getFontMetrics().getStringBounds(pos2, g).getBounds().width / 2;
                int dist = (x - sql) - (x2 - sql2);
                if(dist <= sql + sql2 + 2){
                    y += 17;
                    over = true;
                } else {
                    over = false;
                }
            }
            //
            g.setColor(selected? selectedInnerColor : trackHighlightColor);
            g.fillRect(x - sql - 1, y + d1 + d2 + 8, 2 * (sql + 1), 15);
            g.setColor(selected? selectedBorderColor : borderColor);
            g.drawRect(x - sql - 1, y + d1 + d2 + 8, 2 * (sql + 1), 15);
            g.setColor(Color.WHITE);
            g.drawString(pos, x - sql, y + d1 + d2 + 20);
        } else {
            int x = trackRect.x+trackRect.width;
            int y = (int)(trackRect.y+trackRect.height*((slider.isInverted())? f:1-f)+.5f);
            float value = ((slider.isInverted())? 1-f:f) * slider.getMultValue();
            String pos = (slider.getMultValue() == 1)? String.valueOf(Math.round(value * 1000.0) / 1000.0) : String.valueOf(Math.round(value));
            int sql = g.getFontMetrics().getStringBounds(pos, g).getBounds().width;
            int sqlh = g.getFontMetrics().getStringBounds(pos, g).getBounds().height / 2;
            g.setColor(selected? selectedInnerColor : trackHighlightColor);
            g.fillRect(x + d1 + d2 + 1, y - sqlh, sql + 6, 15);
            g.setColor(selected? selectedBorderColor : borderColor);
            g.drawRect(x + d1 + d2 + 1, y - sqlh, sql + 6, 15);
            g.setColor(Color.WHITE);
            g.drawString(pos, x + d1 + d2 + 5, y + sqlh - 2);       
        }
        g.setColor(tmp);
        return over;
    }

    /**
     * METODO AGREGADO
     * @param g
     * @param f
     * @param d1
     * @param d2
     * @param f2
     * @param down
     * @return 
     */
    protected boolean paintTick(Graphics2D g, float f, int d1, int d2, float f2, boolean down) {
        boolean over = false;
        if(slider.getOrientation()==CustomSlider.HORIZONTAL) {
            int x = (int)(trackRect.x+trackRect.width*f+.5f);
            int y = trackRect.y+trackRect.height;
            Color tmp = g.getColor();
            float value = f * slider.getMultValue();
            String pos = (slider.getMultValue() == 1)? String.valueOf(Math.round(value * 1000.0) / 1000.0) : String.valueOf(Math.round(value));
            int sql = g.getFontMetrics().getStringBounds(pos, g).getBounds().width / 2;
            //
            if(slider.isPaintNumbers() && f2 != -1 && !down){
                int x2 = (int)(trackRect.x+trackRect.width*f2+.5f);
                float value2 = f2 * slider.getMultValue();
                String pos2 = (slider.getMultValue() == 1)? String.valueOf(Math.round(value2 * 1000.0) / 1000.0) : String.valueOf(Math.round(value2));
                int sql2 = g.getFontMetrics().getStringBounds(pos2, g).getBounds().width / 2;
                int dist = (x - sql) - (x2 - sql2);
                if(dist <= sql + sql2 + 2){
                    d2 += 17;
                    over = true;
                } else {
                    over = false;
                }
            }
            //
            g.setColor(trackHighlightColor);
            g.drawLine(x,y+d1,x,y+d1+d2+3);
            g.setColor(tmp);
            
        } else {
            int y = (int)(trackRect.y+trackRect.height*((slider.isInverted())? f:1-f)+.5f);
            int x = trackRect.x+trackRect.width;
            Color tmp = g.getColor();
            g.setColor(trackHighlightColor);
            g.drawLine(x+d1,y,x+d1+d2,y);
            g.setColor(tmp);
        }
        return over;
    }

    /**
     * METODO AGREGADO
     * @param g 
     */
    protected void paintTrackHighlight(Graphics2D g) {
        if(!isTrackHighlightActive() && (slider.getThumbCount() % 2 != 0)) return;
        g = (Graphics2D)g.create();
        // Paint Cut
        if(slider.isPaintCut() && slider.getThumbCount() == 2){
            Point2D p0 = getThumbCenter(0);
            Point2D p1 = getThumbCenter(1);
            Shape outline;
            if(slider.getOrientation()==CustomSlider.HORIZONTAL) {
                // TODO si necesario
            } else {
                float k = 4;
                float minY = (float) p1.getY();
                float maxY = trackRect.height + getThumbSize(1).width / 2 - 1;
                outline = new RoundRectangle2D.Float(trackRect.x, minY, trackRect.width, maxY  - minY + 22, k, k);
                g.setColor(Color.red);
                g.fill(outline);
                //
                minY = trackRect.y - 2;
                maxY = (float) p0.getY() - getThumbSize(0).width / 2 + 1;
                outline = new RoundRectangle2D.Float(trackRect.x, minY, trackRect.width, maxY - minY, k, k);
                g.fill(outline);
            }
        } else {
            // Paint Paired highlight
            for(int i = 0; i < slider.getThumbCount(); i += 2){
                Point2D p1 = getThumbCenter(i);
                Point2D p2 = getThumbCenter(i+1);
                Shape outline;
                if(slider.getOrientation()==CustomSlider.HORIZONTAL) {
                    float minX = (float)Math.min(p1.getX(), p2.getX());
                    float maxX = (float)Math.max(p1.getX(), p2.getX());
                    outline = new Rectangle2D.Float(minX, trackRect.y, maxX - minX, trackRect.height);
                } else {
                    float minY = (float)Math.min(p1.getY(), p2.getY());
                    float maxY = (float)Math.max(p1.getY(), p2.getY());
                    outline = new Rectangle2D.Float(trackRect.x, minY, trackRect.width, maxY - minY);
                }
                g.setColor(trackHighlightColor);
                g.fill(outline);
            }
        }
        g.dispose();
    }  

    @Override
    protected Rectangle calculateTrackRect() {
        int k = (int)(10 + FOCUS_PADDING+.5);
        Rectangle r;
        if(slider.getOrientation()==CustomSlider.HORIZONTAL) {
            r = new Rectangle(k, k, slider.getWidth()-2*k-1, DEPTH);
        } else {
            r = new Rectangle(k, k, DEPTH, slider.getHeight()-2*k-1);
        }

        //why so much dead space? I don't know. This only tries to emulate
        //what Apple is doing.
        k = 22;
        if(slider.getOrientation()==SwingConstants.HORIZONTAL) {
            r.x = k;
            r.width = slider.getWidth()-k*2;
        } else {
            r.y = k;
            r.height = slider.getHeight()-k*2;
        }
        return r;
    }

    protected void paintThumb(Graphics2D g, int thumbIndex, float selected) {
        Shape outline = getThumbShape(thumbIndex);

        if(Thumb.Triangle.equals(getThumb(thumbIndex))) {
            if(slider.getOrientation()==CustomSlider.HORIZONTAL) {
                g.translate(0,2);
            } else {
                g.translate(2,0);
            }
        }

        Rectangle2D thumbBounds = ShapeBounds.getBounds(outline);

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        Paint fill = new LinearGradientPaint(new Point2D.Double(0,thumbBounds.getMinY()), 
                        new Point2D.Double(0,thumbBounds.getMaxY()), new float[]{0, .5f, .501f, 1},
                        new Color[]{new Color(0xFFFFFF), new Color(0xF4F4F4), new Color(0xECECEC), new Color(0xEDEDED)});
        g.setPaint(fill);
        g.fill(outline);

        if(mouseIsDown && thumbIndex==slider.getSelectedThumb()) {
            g.setPaint(new Color(0,0,0,28));
            g.fill(outline);
        }

        if(Thumb.Triangle.equals(getThumb(thumbIndex))) {
            g.setStroke(new BasicStroke(2f));
            g.setPaint(new Color(0,0,0,10));
            g.draw(outline);
            g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
            g.setPaint(new Color(0,0,0,85));
            g.setStroke(new BasicStroke(1f));
            g.draw(outline);
        } else {
            g.setStroke(new BasicStroke(1f));
            g.setPaint(new Color(0,0,0,110));
            g.draw(outline);
        }

        if(thumbIndex==slider.getSelectedThumb()) {
            Color focusColor = new Color(0xa7, 0xd5, 0xff, 240);
            PlafPaintUtils.paintFocus(g, outline, FOCUS_PADDING, focusColor, false);
            g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g.setStroke(new BasicStroke(1f));
            g.setPaint(new Color(0,0,0,23));
            g.draw(outline);
        }
    }

    @Override
    protected void paintThumbs(Graphics2D g) {
        float[] values = slider.getThumbPositions();
        for(int a =0 ; a<values.length; a++) {
            float darkness = a==slider.getSelectedThumb()? 1 : thumbIndications[a]*.5f;
            Graphics2D g2 = (Graphics2D)g.create();
            paintThumb(g2, a, darkness);
            g2.dispose();
        }
    }
}
