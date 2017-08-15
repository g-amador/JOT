/*
 * Copyright (c) 2007 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistribution of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN") AND ITS
 * LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A
 * RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 * IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT
 * OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR
 * PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY,
 * ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS
 * BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed or intended for use in the
 * design, construction, operation or maintenance of any nuclear facility.
 *
 * Sun gratefully acknowledges that this software was originally authored and
 * developed by Kenneth Bradley Russell and Christopher John Kline.
 */
package jot.gui;

import static com.jogamp.graph.font.FontSet.STYLE_BOLD;
import com.jogamp.opengl.GLDrawable;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.util.awt.TextRenderer;
import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import static java.util.logging.Level.INFO;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;

/**
 * A simple class which uses the TextRenderer to provide informative text (e.g.,
 * demo options), overlaid on top of the scene.
 *
 * @author G. Amador {@literal &} A. Gomes
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
    private LinkedList<String> text = new LinkedList<>();
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
     * @param text the LinkedList containing the lines of text to output.
     */
    public void setText(LinkedList<String> text) {
        this.text = text;
    }

    /**
     * Set a text line to output.
     *
     * @param textLine to add to the LinkedList containing the lines of text to
     * output.
     */
    public void setTextLine(String textLine) {
        this.text.add(textLine);
    }

    /**
     * Remove a provided text line if said line exists.
     *
     * @param textLine to remove from the LinkedList containing the lines of
     * text to output.
     */
    public void removeTextLine(String textLine) {
        this.text.removeFirstOccurrence(textLine);
    }

    /**
     * Shows in terminal the text to output.
     */
    public void showText() {
        this.text.stream().forEach(s -> log.info(s));
    }

    /**
     * Draws the menu/options text. It is assumed this method will be called
     * only once per frame.
     */
    public void render() {
        int i = 20;
        for (String textLine : this.text) {
            int textMag = textLine.length();

            if (textMag > this.textMagnitude) {
                Rectangle2D bounds = this.renderer.getBounds(textLine);
                this.textWidth = (int) bounds.getWidth();
                this.textHeight = (int) bounds.getHeight();
                this.textOffset = (int) (bounds.getHeight() * 1.0f);
                this.textMagnitude = textMag;
            }

            this.renderer.beginRendering(this.drawable.getSurfaceWidth(), this.drawable.getSurfaceHeight());
            // Figure out the location at which to draw the text
            int x = 0;
            int y = 0;
            switch (this.textLocation) {
                case UPPER_LEFT:
                    x = this.textOffset;
                    y = this.drawable.getSurfaceHeight() - this.textHeight - this.textOffset - i;
                    break;

                case UPPER_RIGHT:
                    x = this.drawable.getSurfaceWidth() - this.textWidth - this.textOffset;
                    y = this.drawable.getSurfaceHeight() - this.textHeight - this.textOffset - i;
                    break;

                case LOWER_LEFT:
                    x = this.textOffset;
                    y = this.textOffset - i;
                    break;

                case LOWER_RIGHT:
                    x = this.drawable.getSurfaceWidth() - this.textWidth - this.textOffset;
                    y = this.textOffset - i;
                    break;
                default:
                    log.info("Non existent option.");
            }
            this.renderer.draw(textLine, x, y);
            this.renderer.endRendering();

            i += 20;
        }
    }
}
