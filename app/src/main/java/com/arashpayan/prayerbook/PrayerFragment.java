/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.arashpayan.prayerbook;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.arashpayan.util.L;
import com.samskivert.mustache.Mustache;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Locale;

/**
 *
 * @author arash
 */
public class PrayerFragment extends Fragment {
    
    private WebView mWebView = null;
    private Cursor prayerCursor;
    private float mScale = 1.0f;
    
    public static final String PRAYER_ID_ARGUMENT = "PrayerId";
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        long prayerId = arguments.getLong(PRAYER_ID_ARGUMENT, -1);
        if (prayerId == -1) {
            throw new IllegalArgumentException("You must provide a prayer id to this fragment");
        }
        prayerCursor = Database.getInstance().getPrayer(prayerId);
        prayerCursor.moveToFirst();
        mScale = Preferences.getInstance(App.getApp()).getPrayerTextScalar();
        
        setHasOptionsMenu(true);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= 19) {
            getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        }
        
        mWebView = new WebView(this.getActivity());
        mWebView.getSettings().setSupportZoom(true);
        mWebView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mWebView.setKeepScreenOn(true);
        reloadPrayer();

        return mWebView;
    }

    private void reloadPrayer() {
        mWebView.loadDataWithBaseURL(null, getPrayerHTML(), "text/html", "UTF-8", null);
    }

    @Override
    public void onPause() {
        super.onPause();
        
        getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
    }

    @Override
    public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.prayer, menu);

        // set the current value for classic theme
        menu.findItem(R.id.action_classic_theme).setChecked(Preferences.getInstance(App.getApp()).useClassicTheme());

        // hide the print option on older devices
        if (Build.VERSION.SDK_INT < 19) {
            menu.findItem(R.id.action_print_prayer).setVisible(false);
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // .75 to 1.60
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().onBackPressed();
                break;
            case R.id.action_increase_text_size:
                if (mScale < 1.6f) {
                    mScale += 0.05f;
                    Preferences.getInstance(App.getApp()).setPrayerTextScalar(mScale);
                    reloadPrayer();
                }
                break;
            case R.id.action_decrease_text_size:
                if (mScale > .75) {
                    mScale -= 0.05f;
                    Preferences.getInstance(App.getApp()).setPrayerTextScalar(mScale);
                    reloadPrayer();
                }
                break;
            case R.id.action_classic_theme:
                boolean useClassic = !item.isChecked(); // toggle the value
                item.setChecked(useClassic);
                Preferences.getInstance(App.getApp()).setUseClassicTheme(useClassic);
                reloadPrayer();
                break;
            case R.id.action_share_prayer:
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(Intent.EXTRA_TEXT, getPrayerText());
                startActivity(Intent.createChooser(sharingIntent, "Share via"));
                break;
            case R.id.action_print_prayer:
                printPrayer();
                break;
            default:
                return false;
        }
        
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();

        ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (ab != null) {
            ab.setTitle(null);
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setHomeButtonEnabled(true);
        }
    }

    public String getPrayerHTML() {
        float pFontWidth = (float)1.1 * mScale;
        float pFontHeight = (float)1.575 * mScale;
        float pComment = (float)0.8 * mScale;
        float authorWidth = (float)1.03 * mScale;
        float authorHeight = (float)1.825 * mScale;
        float versalWidth = (float)3.5 * mScale;
        float versalHeight = (float)0.75 * mScale;

        HashMap<String, String> args = new HashMap<>();
        args.put("fontWidth", String.format(Locale.US, "%f", pFontWidth));
        args.put("fontHeight", String.format(Locale.US, "%f", pFontHeight));
        args.put("commentSize", String.format(Locale.US, "%f", pComment));
        args.put("authorWidth", String.format(Locale.US, "%f", authorWidth));
        args.put("authorHeight", String.format(Locale.US, "%f", authorHeight));
        args.put("versalWidth", String.format(Locale.US, "%f", versalWidth));
        args.put("versalHeight", String.format(Locale.US, "%f", versalHeight));
        boolean useClassicTheme = Preferences.getInstance(App.getApp()).useClassicTheme();
        String bgColor;
        String versalAndAuthorColor;
        String font;
        String italicOrNothing;
        if (useClassicTheme) {
            bgColor = "#D6D2C9";
            versalAndAuthorColor = "#992222";
            font = "Georgia";
            italicOrNothing = "italic";
        } else {
            bgColor = "#ffffff";
            versalAndAuthorColor = "#33b5e5";
            font = "sans-serif";
            italicOrNothing = "";
        }
        args.put("backgroundColor", bgColor);
        args.put("versalAndAuthorColor", versalAndAuthorColor);
        args.put("font", font);
        args.put("italicOrNothing", italicOrNothing);

        int textIndex = prayerCursor.getColumnIndexOrThrow(Database.PRAYERTEXT_COLUMN);
        String prayerText = prayerCursor.getString(textIndex);
        args.put("prayer", prayerText);

        int authorIndex = prayerCursor.getColumnIndexOrThrow(Database.AUTHOR_COLUMN);
        String authorText = prayerCursor.getString(authorIndex);
        args.put("author", authorText);

        int citationIndex = prayerCursor.getColumnIndexOrThrow(Database.CITATION_COLUMN);
        String citationText = prayerCursor.getString(citationIndex);
        if (citationText.isEmpty()) {
            L.i("no citation");
            args.put("citation", "");
        } else {
            String citationHTML = String.format("<p class=\"comment\"><br/><br/>%s</p>", citationText);
            args.put("citation", citationHTML);
        }

        int langIndex = prayerCursor.getColumnIndexOrThrow(Database.LANGUAGE_COLUMN);
        String langCode = prayerCursor.getString(langIndex);
        Language lang = Language.get(langCode);
        if (lang.rightToLeft) {
            args.put("layoutDirection", "rtl");
        } else {
            args.put("layoutDirection", "ltr");
        }

        InputStream is = getResources().openRawResource(R.raw.prayer_template);
        InputStreamReader isr = new InputStreamReader(is);

        return Mustache.compiler().escapeHTML(false).compile(isr).execute(args);
    }
    
    private String getPrayerText() {
        int searchTextIndex = prayerCursor.getColumnIndexOrThrow(Database.SEARCHTEXT_COLUMN);
        
        return prayerCursor.getString(searchTextIndex);
    }

    @TargetApi(19) @SuppressWarnings("deprecation")
    private void printPrayer() {
        if (mWebView == null) {
            // shouldn't happen, but just in case
            return;
        }
        
        PrintManager manager = (PrintManager)getActivity().getSystemService(Context.PRINT_SERVICE);
        PrintDocumentAdapter adapter;
        if (Build.VERSION.SDK_INT >= 21) {
            adapter = mWebView.createPrintDocumentAdapter("Prayer");
        } else {
            adapter = mWebView.createPrintDocumentAdapter();
        }
        
        String jobName = getString(R.string.app_name) + " " + getString(R.string.document);
        manager.print(jobName, adapter, new PrintAttributes.Builder().build());
    }
}
