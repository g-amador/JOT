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

//import com.jogamp.graph.curve.opengl.TextRegionUtil;
//import com.jogamp.graph.font.Font;
import static com.jogamp.graph.font.FontSet.STYLE_BOLD;
import com.jogamp.opengl.GLDrawable;
import com.jogamp.opengl.GLException;
//import com.jogamp.nativewindow.util.Rectangle;
import com.jogamp.opengl.util.AnimatorBase;
import com.jogamp.opengl.util.awt.TextRenderer;
import java.awt.Font;
//import static java.awt.Font.BOLD;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;

/**
 * A simple class which uses the TextRenderer to provide an FPS counter overlaid
 * on top of the scene.
 */
public class FPSCounter {

    static final Logger log = getLogger("FPSCounter");

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
        log.setLevel(OFF);
    }

    private int textLocation = UPPER_LEFT;
    private GLDrawable drawable;
    private AnimatorBase animator;
    private TextRenderer renderer;
    //private TextRegionUtil renderer;
    private final DecimalFormat format = new DecimalFormat("####.00");
    private String fpsText;
    private int fpsMagnitude;
    private int fpsWidth;
    private int fpsHeight;
    private int fpsOffset;

    /**
     * Creates a new FPSCounter with the given font size. An OpenGL context must
     * be current at the time the constructor is called.
     *
     * @param drawable the drawable to render the text to.
     * @param animator animator to access FPS counter.
     * @param textSize the point size of the font to use.
     * @throws GLException if an OpenGL context is not current when the
     * constructor is called.
     */
    public FPSCounter(GLDrawable drawable, AnimatorBase animator, int textSize) throws GLException {
        //renderer = new TextRegionUtil(0);        
        this(drawable, animator, new Font("SansSerif", STYLE_BOLD, textSize));
    }

    /**
     * Creates a new FPSCounter with the given font. An OpenGL context must be
     * current at the time the constructor is called.
     *
     * @param drawable the drawable to render the text to.
     * @param animator animator to access FPS counter.
     * @param font the font to use.
     * @throws GLException if an OpenGL context is not current when the
     * constructor is called.
     */
    public FPSCounter(GLDrawable drawable, AnimatorBase animator, Font font) throws GLException {
        this(drawable, animator, font, true, true);
    }

    /**
     * Creates a new FPSCounter with the given font and rendering attributes. An
     * OpenGL context must be current at the time the constructor is called.
     *
     * @param drawable the drawable to render the text to.
     * @param animator animator to access FPS counter.
     * @param font the font to use.
     * @param antialiased whether to use anti-aliased fonts.
     * @param useFractionalMetrics whether to use fractional font.
     * @throws GLException if an OpenGL context is not current when the
     * constructor is called.
     */
    public FPSCounter(GLDrawable drawable,
            AnimatorBase animator,
            Font font,
            boolean antialiased,
            boolean useFractionalMetrics) throws GLException {
        this.drawable = drawable;
        this.animator = animator;
        this.renderer = new TextRenderer(font, antialiased, useFractionalMetrics);
    }

    /**
     * Get the latest FPS value.
     *
     * @return the latest FPS value.
     */
    public float getFPS() {
        return this.animator != null
                ? this.animator.getLastFPS()
                : 0;
    }

    /**
     * Gets the relative location where the text of this FPSCounter will be
     * drawn: one of UPPER_LEFT, UPPER_RIGHT, LOWER_LEFT, or LOWER_RIGHT.
     * Defaults to LOWER_RIGHT.
     *
     * @return the relative location where the text of this FPSCounter will be
     * drawn
     */
    public int getTextLocation() {
        return this.textLocation;
    }

    /**
     * Sets the relative location where the text of this FPSCounter will be
     * drawn: one of UPPER_LEFT, UPPER_RIGHT, LOWER_LEFT, or LOWER_RIGHT.
     * Defaults to LOWER_RIGHT.
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
     * Updates the FPSCounter's internal timer and counter and draws the
     * computed FPS. It is assumed this method will be called only once per
     * frame.
     */
    public void render() {
        float fps = this.getFPS();
        this.recomputeFPSSize(fps);
        this.fpsText = "FPS: " + this.format.format(fps);
        if (this.fpsText != null) {
            this.renderer.beginRendering(this.drawable.getSurfaceWidth(), this.drawable.getSurfaceHeight());
            // Figure out the location at which to draw the text
            int x = 0;
            int y = 0;
            switch (this.textLocation) {
                case UPPER_LEFT:
                    x = this.fpsOffset;
                    y = this.drawable.getSurfaceHeight() - this.fpsHeight - this.fpsOffset;
                    break;

                case UPPER_RIGHT:
                    x = this.drawable.getSurfaceWidth() - this.fpsWidth - this.fpsOffset;
                    y = this.drawable.getSurfaceHeight() - this.fpsHeight - this.fpsOffset;
                    break;

                case LOWER_LEFT:
                    x = this.fpsOffset;
                    y = this.fpsOffset;
                    break;

                case LOWER_RIGHT:
                    x = this.drawable.getSurfaceWidth() - this.fpsWidth - this.fpsOffset;
                    y = this.fpsOffset;
                    break;
                default:
                    log.info("Non existent option.");
            }

            this.renderer.draw(this.fpsText, x, y);
            this.renderer.endRendering();
        }
    }

    private void recomputeFPSSize(float fps) {
        String fpsTxt;
        int fpsMag;
        if (fps >= 10_000) {
            fpsTxt = "10000.00";
            fpsMag = 5;
        } else if (fps >= 1_000) {
            fpsTxt = "1000.00";
            fpsMag = 4;
        } else if (fps >= 100) {
            fpsTxt = "100.00";
            fpsMag = 3;
        } else if (fps >= 10) {
            fpsTxt = "10.00";
            fpsMag = 2;
        } else {
            fpsTxt = "9.00";
            fpsMag = 1;
        }

        if (fpsMag > this.fpsMagnitude) {
            Rectangle2D bounds = this.renderer.getBounds("FPS: " + fpsTxt);
            //Rectangle bounds = renderer.getBounds("FPS: " + fpsTxt);
            this.fpsWidth = (int) bounds.getWidth();
            this.fpsHeight = (int) bounds.getHeight();
            this.fpsOffset = (int) (this.fpsHeight * 0.5f);
            this.fpsMagnitude = fpsMag;
        }
    }
}
