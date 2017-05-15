/*
 * This file is part of the JOT game engine core toolkit component. 
 * Copyright (C) 2014 Gonçalo Amador & Abel Gomes
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * E-mail Contacts: G. Amador (g.n.p.amador@gmail.com) & 
 *                  A. Gomes (agomes@it.ubi.pt)
 */
package jot.gui;

//import com.jogamp.graph.curve.opengl.TextRegionUtil;
//import com.jogamp.graph.font.Font;
import static com.jogamp.graph.font.FontSet.STYLE_BOLD;
import com.jogamp.opengl.GLDrawable;
import com.jogamp.opengl.GLException;
//import com.jogamp.nativewindow.util.Rectangle;
import com.jogamp.opengl.util.awt.TextRenderer;
import java.awt.Font;
//import static java.awt.Font.BOLD;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import static java.util.logging.Level.INFO;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;

/**
 * A simple class which uses the TextRenderer to provide informative text (e.g.,
 * demo options), overlaid on top of the scene.
 */
public class Text {

    static final Logger log = getLogger("Text");

    /**
     * Placement constant.
     */
    public static final int UPPER_LEFT = 1;

    /**
     * Placement constant.
     */
    public static final int UPPER_RIGHT = 2;

    /**
     * Placement constant.
     */
    public static final int LOWER_LEFT = 3;

    /**
     * Placement constant.
     */
    public static final int LOWER_RIGHT = 4;

    static {
        log.setLevel(INFO);
    }

    private int textLocation = UPPER_LEFT;
    private GLDrawable drawable;
    private TextRenderer renderer;
    private ArrayList<String> text = new ArrayList<>();
    private int textMagnitude;
    private int textWidth;
    private int textHeight;
    private int textOffset;

    /**
     * Creates a new Text with the given font size. An OpenGL context must be
     * current at the time the constructor is called.
     *
     * @param drawable the drawable to render the text to.
     * @param textSize the point size of the font to use.
     * @throws GLException if an OpenGL context is not current when the
     * constructor is called.
     */
    public Text(GLDrawable drawable, int textSize) throws GLException {
        //renderer = new TextRegionUtil(0);        
        this(drawable, new Font("SansSerif", STYLE_BOLD, textSize));
    }

    /**
     * Creates a new Text with the given font. An OpenGL context must be current
     * at the time the constructor is called.
     *
     * @param drawable the drawable to render the text to.
     * @param font the font to use.
     * @throws GLException if an OpenGL context is not current when the
     * constructor is called.
     */
    public Text(GLDrawable drawable, Font font) throws GLException {
        this(drawable, font, true, true);
    }

    /**
     * Creates a new Text with the given font and rendering attributes. An
     * OpenGL context must be current at the time the constructor is called.
     *
     * @param drawable the drawable to render the text to.
     * @param font the font to use.
     * @param antialiased whether to use anti-aliased fonts.
     * @param useFractionalMetrics whether to use fractional font.
     * @throws GLException if an OpenGL context is not current when the
     * constructor is called.
     */
    public Text(GLDrawable drawable,
            Font font,
            boolean antialiased,
            boolean useFractionalMetrics) throws GLException {
        this.drawable = drawable;
        this.renderer = new TextRenderer(font, antialiased, useFractionalMetrics);
    }

    /**
     * Gets the relative location where the text of this Text will be drawn: one
     * of UPPER_LEFT, UPPER_RIGHT, LOWER_LEFT, or LOWER_RIGHT. Defaults to
     * LOWER_RIGHT.
     *
     * @return the relative location where the text of this Text will be drawn
     */
    public int getTextLocation() {
        return this.textLocation;
    }

    /**
     * Sets the relative location where the text of this Text will be drawn: one
     * of UPPER_LEFT, UPPER_RIGHT, LOWER_LEFT, or LOWER_RIGHT. Defaults to
     * LOWER_RIGHT.
     *
     * @param textLocation
     */
    public void setTextLocation(int textLocation) {
        if (textLocation < UPPER_LEFT || textLocation > LOWER_RIGHT) {
            throw new IllegalArgumentException("textLocation");
        }
        this.textLocation = textLocation;
    }

    /**
     * Changes the current color of this TextRenderer to the supplied one, where
     * each component ranges from 0.0f - 1.0f. The alpha component, if used,
     * does not need to be premultiplied into the color channels as described in
     * the documentation for
     * {@link com.jogamp.opengl.util.texture.Texture Texture}, although
     * premultiplied colors are used internally. The default color is opaque
     * white.
     *
     * @param r the red component of the new color
     * @param g the green component of the new color
     * @param b the blue component of the new color
     * @param a the alpha component of the new color, 0.0f = completely
     * transparent, 1.0f = completely opaque
     * @throws GLException If an OpenGL context is not current when this method
     * is called
     */
    public void setColor(float r, float g, float b, float a) throws GLException {
        this.renderer.setColor(r, g, b, a);
    }

    /**
     * Set the text to output.
     *
     * @param text the ArrayList containing the lines of text to output.
     */
    public void setText(ArrayList<String> text) {
        this.text = text;
    }

    /**
     * Set a text line to output.
     *
     * @param textLine to add to the ArrayList containing the lines of text to
     * output.
     */
    public void setTextLine(String textLine) {
        this.text.add(textLine);
    }

    /**
     * Shows in terminal the text to output.
     */
    public void showText() {
        this.text.stream().forEach(s -> log.info(s));
    }

    /**
     * Updates the Text's internal timer and counter and draws the computed FPS.
     * It is assumed this method will be called only once per frame.
     */
    public void render() {
//        float fps = getFPS();
//        recomputeFPSSize(fps);
//        textLine = format.format(fps);

        //for (int i = 0; i < text.size(); i++) {
        for (int i = 0; i < 3; i++) {
            String textLine = this.text.get(2);
            //String textLine = text.get(i);
            int textMag = textLine.length();

            if (textMag > this.textMagnitude) {
                Rectangle2D bounds = this.renderer.getBounds(textLine);
                this.textWidth = (int) bounds.getWidth();
                this.textHeight = (int) bounds.getHeight();
                this.textOffset = (int) (bounds.getHeight() * 1.0f + 20);
                this.textMagnitude = textMag;
            }

            //        if (fpsText != null) {
            this.renderer.beginRendering(this.drawable.getSurfaceWidth(), this.drawable.getSurfaceHeight());
            // Figure out the location at which to draw the text
            int x = 0;
            int y = 0;
            switch (this.textLocation) {
                case UPPER_LEFT:
                    x = this.textOffset;
                    y = this.drawable.getSurfaceHeight() - this.textHeight - this.textOffset;
                    break;

                case UPPER_RIGHT:
                    x = this.drawable.getSurfaceWidth() - this.textWidth - this.textOffset;
                    y = this.drawable.getSurfaceHeight() - this.textHeight - this.textOffset;
                    break;

                case LOWER_LEFT:
                    x = this.textOffset;
                    y = this.textOffset;
                    break;

                case LOWER_RIGHT:
                    x = this.drawable.getSurfaceWidth() - this.textWidth - this.textOffset;
                    y = this.textOffset;
                    break;
                default:
                    log.info("Non existent option.");
            }
            this.renderer.draw(textLine, x, y);
            this.renderer.endRendering();
        }
    }

//    private void recomputeFPSSize(float fps) {
//        String fpsTxt;
//        int fpsMag;
//        if (fps >= 10_000) {
//            fpsTxt = "10000.00";
//            fpsMag = 5;
//        } else if (fps >= 1_000) {
//            fpsTxt = "1000.00";
//            fpsMag = 4;
//        } else if (fps >= 100) {
//            fpsTxt = "100.00";
//            fpsMag = 3;
//        } else if (fps >= 10) {
//            fpsTxt = "10.00";
//            fpsMag = 2;
//        } else {
//            fpsTxt = "9.00";
//            fpsMag = 1;
//        }
//
//        if (fpsMag > this.fpsMagnitude) {
//            Rectangle2D bounds = renderer.getBounds("FPS: " + fpsTxt);
//            //Rectangle bounds = renderer.getBounds("FPS: " + fpsTxt);
//            fpsWidth = (int) bounds.getWidth();
//            fpsHeight = (int) bounds.getHeight();
//            fpsOffset = (int) (fpsHeight * 0.5f);
//            this.fpsMagnitude = fpsMag;
//        }
//    }
}
