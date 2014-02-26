package com.arashpayan.prayerbook;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.arashpayan.util.Graphics;

/**
 * Created by arash on 6/16/13.
 */
public class PrayerItemView extends RelativeLayout {
    private final TextView titleTextView;
    private final TextView authorTextView;
    private final TextView wordCountTextView;
    private RatingBar mFavoriteRatingBar;

    private static final int TITLE_TEXTVIEW_ID      = 29;
    private static final int AUTHOR_TEXTVIEW_ID     = 48;
    private static final int WORDCOUNT_TEXTVIEW_ID  = 82;
    private static final int FAVORITE_RATINGBAR_ID  = 51;

    public PrayerItemView(Context context) {
        super(context);

        int eightDp = Graphics.pixels(context, 8);
        int sixteenDp = Graphics.pixels(context, 16);

        titleTextView = new TextView(context);
        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        titleTextView.setPadding(sixteenDp, eightDp, eightDp, Graphics.pixels(context, 2));
        titleTextView.setTypeface(Typeface.DEFAULT_BOLD);
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
        authorTextView.setPadding(sixteenDp, 0, eightDp, eightDp);
        authorTextView.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
        authorTextView.setTextColor(Color.rgb(128, 128, 128));
        authorTextView.setId(AUTHOR_TEXTVIEW_ID);
        params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.addRule(ALIGN_PARENT_LEFT);
        params.addRule(BELOW, TITLE_TEXTVIEW_ID);
        authorTextView.setLayoutParams(params);
        addView(authorTextView);

        wordCountTextView = new TextView(context);
        wordCountTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        wordCountTextView.setPadding(Graphics.pixels(context, 0), Graphics.pixels(context, 0), sixteenDp, eightDp);
        wordCountTextView.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
        wordCountTextView.setTextColor(Color.GRAY);
        wordCountTextView.setId(WORDCOUNT_TEXTVIEW_ID);
        params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        params.addRule(ALIGN_PARENT_RIGHT);
        params.addRule(ALIGN_TOP, AUTHOR_TEXTVIEW_ID);
        wordCountTextView.setLayoutParams(params);
        addView(wordCountTextView);

//        mFavoriteRatingBar = new RatingBar(context);
//        mFavoriteRatingBar.setId(FAVORITE_RATINGBAR_ID);
//        mFavoriteRatingBar.setNumStars(1);
//        mFavoriteRatingBar.setStepSize(1.0f);
//        mFavoriteRatingBar.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                L.i("Rating bar clicked");
//                if (mFavoriteRatingBar.getRating() == 1.0f) {
//                    mFavoriteRatingBar.setRating(0);
//                } else {
//                    mFavoriteRatingBar.setRating(1.0f);
//                }
//            }
//        });
//        params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
//        params.addRule(ALIGN_BOTTOM, TITLE_TEXTVIEW_ID);
//        params.addRule(ALIGN_TOP, TITLE_TEXTVIEW_ID);
//        params.addRule(ALIGN_PARENT_RIGHT);
//        params.setMargins(0, eightDp, sixteenDp, 0);
//        mFavoriteRatingBar.setLayoutParams(params);
//        addView(mFavoriteRatingBar);
    }

    public CharSequence getTitle() {
        return titleTextView.getText();
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

    public CharSequence getWordCount() {
        return wordCountTextView.getText();
    }

    public void setWordCount(CharSequence wordCount) {
        wordCountTextView.setText(wordCount);
    }

}
