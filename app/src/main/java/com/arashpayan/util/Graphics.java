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
    
    public static int pixels(Context ctx, float dip) {
        return (int)(ctx.getResources().getDisplayMetrics().density*dip);
    }
}
