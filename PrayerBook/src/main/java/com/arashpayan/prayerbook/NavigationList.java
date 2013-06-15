/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.arashpayan.prayerbook;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.arashpayan.util.Graphics;

/**
 *
 * @author arash
 */
public class NavigationList extends BaseAdapter {

    private Context context;
    
    private final static int ItemPrayers    = 0;
    private final static int ItemRecents    = 1;
    private final static int ItemBookmarks  = 2;
    
    public NavigationList(Context context) {
        this.context = context;
    }
    
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        TextView tv = null;
        if (convertView == null)
        {
            tv = new TextView(context);
            tv.setMinimumHeight(Graphics.pixels(tv.getContext(), 72));
            tv.setMinimumWidth(Graphics.pixels(tv.getContext(), 200));
            tv.setTextSize(20);
            tv.setGravity(Graphics.GRAVITY_LEFT | Graphics.GRAVITY_CENTER_VERTICAL);
            tv.setPadding(Graphics.pixels(tv.getContext(), 8), 0, 0, 0);
            tv.setTextColor(Color.argb(255, 255, 255, 255));
        }
        else
            tv = (TextView)convertView;
        
        switch (position) {
            case ItemPrayers:
                tv.setText(R.string.prayers);
                break;
            case ItemRecents:
                tv.setText(R.string.recents);
                break;
            case ItemBookmarks:
                tv.setText(R.string.bookmarks);
                break;
            default:
                break;
        }
        
        return tv;
    }

    public int getCount() {
        return 3;
    }

    public Object getItem(int position) {
        return new Integer(position);
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        TextView tv = null;
        if (convertView == null)
        {
            tv = new TextView(context);
            tv.setMinimumHeight(Graphics.pixels(tv.getContext(), 48));
            tv.setTextSize(16);
            tv.setGravity(Graphics.GRAVITY_LEFT | Graphics.GRAVITY_CENTER_VERTICAL);
            tv.setTextColor(Color.argb(255, 255, 255, 255));
        }
        else
            tv = (TextView)convertView;
        
        switch (position) {
            case ItemPrayers:
                tv.setText(R.string.prayers);
                break;
            case ItemRecents:
                tv.setText(R.string.recents);
                break;
            case ItemBookmarks:
                tv.setText(R.string.bookmarks);
                break;
            default:
                break;
        }
        
        return tv;
    }
    
    @Override
    public int getItemViewType(int viewTypeId) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
    
}
