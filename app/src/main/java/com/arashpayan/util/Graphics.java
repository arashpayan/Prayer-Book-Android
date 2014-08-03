/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.arashpayan.util;

import android.content.Context;
import android.graphics.Color;

/**
 *
 * @author arash
 */
public class Graphics {

    public final static int HOLO_BLUE = Color.rgb(51, 181, 229);
    
    public static int pixels(Context ctx, float dip) {
        return (int)(ctx.getResources().getDisplayMetrics().density*dip);
    }
}
