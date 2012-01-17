/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.arashpayan.util;

import android.content.Context;

/**
 *
 * @author arash
 */
public class Graphics {
    
    public static final int GRAVITY_TOP                 = 0x30;
    public static final int GRAVITY_BOTTOM              = 0x50;
    public static final int GRAVITY_LEFT                = 0x03;
    public static final int GRAVITY_RIGHT               = 0x05;
    public static final int GRAVITY_CENTER_VERTICAL     = 0x10;
    public static final int GRAVITY_FILL_VERTICAL       = 0x70;
    public static final int GRAVITY_CENTER_HORIZONTAL   = 0x01;
    public static final int GRAVITY_FILL_HORIZONTAL     = 0x07;
    public static final int GRAVITY_CENTER              = 0x11;
    public static final int GRAVITY_FILL                = 0x77;
    public static final int GRAVITY_CLIP_VERTICAL       = 0x80;
    public static final int GRAVITY_CLIP_HORIZONTAL     = 0x08;
    public static final int GRAVITY_START               = 0x00800003;
    public static final int GRAVITY_END                 = 0x00800005;
    
    public static int pixels(Context ctx, float dip) {
        return (int)(ctx.getResources().getDisplayMetrics().density*dip);
    }
}
