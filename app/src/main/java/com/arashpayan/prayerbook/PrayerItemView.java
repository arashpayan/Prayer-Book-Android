package com.arashpayan.prayerbook;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.arashpayan.util.Graphics;

/**
 * Created by Arash Payan on 6/16/13.
 */
public class PrayerItemView extends RelativeLayout {
    private final TextView titleTextView;
    private final TextView authorTextView;
    private final TextView wordCountTextView;

    private static final int TITLE_TEXTVIEW_ID      = 29;
    private static final int AUTHOR_TEXTVIEW_ID     = 48;
    private static final int WORDCOUNT_TEXTVIEW_ID  = 82;

    public PrayerItemView(Context context) {
        super(context);

        setMinimumHeight(Graphics.pixels(context, 72));
        int eightDp = Graphics.pixels(context, 8);
        int sixteenDp = Graphics.pixels(context, 16);

        titleTextView = new TextView(context);
        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        titleTextView.setPadding(sixteenDp, sixteenDp, eightDp, Graphics.pixels(context, 2));
        titleTextView.setLines(1);
        titleTextView.setId(TITLE_TEXTVIEW_ID);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.addRule(ALIGN_PARENT_LEFT);
        params.addRule(ALIGN_PARENT_TOP);
        params.addRule(LEFT_OF, WORDCOUNT_TEXTVIEW_ID);
        titleTextView.setLayoutParams(params);
        addView(titleTextView);

        authorTextView = new TextView(context);
        authorTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        authorTextView.setPadding(sixteenDp, 0, eightDp, sixteenDp);
        authorTextView.setTextColor(Color.rgb(128, 128, 128));
        authorTextView.setId(AUTHOR_TEXTVIEW_ID);
        params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.addRule(ALIGN_PARENT_LEFT);
        params.addRule(BELOW, TITLE_TEXTVIEW_ID);
        authorTextView.setLayoutParams(params);
        addView(authorTextView);

        wordCountTextView = new TextView(context);
        wordCountTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        wordCountTextView.setPadding(Graphics.pixels(context, 0), Graphics.pixels(context, 0), sixteenDp, sixteenDp);
        wordCountTextView.setTextColor(Color.GRAY);
        wordCountTextView.setId(WORDCOUNT_TEXTVIEW_ID);
        params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        params.addRule(ALIGN_PARENT_RIGHT);
        params.addRule(ALIGN_TOP, AUTHOR_TEXTVIEW_ID);
        wordCountTextView.setLayoutParams(params);
        addView(wordCountTextView);
    }

    public void setTitle(CharSequence title) {
        titleTextView.setText(title);
    }

    public CharSequence getAuthor() {
        return authorTextView.getText();
    }

    public void setAuthor(CharSequence author) {
        authorTextView.setText(author);
    }

    public void setWordCount(CharSequence wordCount) {
        wordCountTextView.setText(wordCount);
    }

}
