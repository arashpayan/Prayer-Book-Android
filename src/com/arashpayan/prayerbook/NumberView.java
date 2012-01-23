/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.arashpayan.prayerbook;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.view.View;
import com.arashpayan.util.Graphics;

/**
 *
 * @author arash
 */
public class NumberView extends View {
    
    private int number;
    private Paint textPaint;
    
    public NumberView(Context context) {
        super(context);
        
        number = 0;
        textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(Graphics.pixels(context, 16));
        textPaint.setColor(Color.WHITE);
    }
    
    public int getNumber() {
        return number;
    }
    
    public void setNumber(int aNumber) {
        this.number = aNumber;
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
//        canvas.drawTe
//        canvas.
        TextPaint tp = new TextPaint();
        tp.setTextAlign(Paint.Align.CENTER);
        tp.setTypeface(Typeface.DEFAULT);
        tp.setTextSize(14);
        
        canvas.drawText(Integer.toString(number), getPaddingLeft(), getPaddingTop(), tp);
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(Graphics.pixels(getContext(), 60), Graphics.pixels(getContext(), 40));
    }
}
