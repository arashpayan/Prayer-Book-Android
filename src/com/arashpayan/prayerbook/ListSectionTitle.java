/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.arashpayan.prayerbook;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.widget.TextView;

import com.arashpayan.util.Graphics;

/**
 *
 * @author arash
 */
public class ListSectionTitle extends TextView {
    
    private final Paint mPaint = new Paint();
    
    public ListSectionTitle(Context aContext) {
        this(aContext, null);
    }
    
    public ListSectionTitle(Context aContext, String title) {
        super(aContext);
        
        mPaint.setColor(Graphics.HOLO_BLUE);
        
        setPadding(
                Graphics.pixels(aContext, 16),
                Graphics.pixels(aContext, 4),
                Graphics.pixels(aContext, 16),
                Graphics.pixels(aContext, 4));
        setTextColor(Graphics.HOLO_BLUE);
        setSingleLine();
        setTypeface(Typeface.DEFAULT_BOLD);
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        if (title != null)
            setText(title);
    }
    
    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // draw a solid line at the bottom
        final int oneDipInPixels = Graphics.pixels(getContext(), 1);
        for (int i=0; i<oneDipInPixels; i++) {
            canvas.drawLine(
                    0, // start x
                    getHeight()-oneDipInPixels-i,  // start y
                    getWidth(),  // stop x
                    getHeight()-oneDipInPixels-i,    // stop y
                    mPaint);
        }
    }
}
