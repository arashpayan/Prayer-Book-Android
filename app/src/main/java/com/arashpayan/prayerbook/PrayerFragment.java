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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

/**
 *
 * @author arash
 */
public class PrayerFragment extends Fragment {
    
    private WebView mWebView = null;
    private Cursor prayerCursor;
    private float mScale = 1.0f;
    
    public static final String PRAYER_ID_ARGUMENT = "PrayerId";
    public static final String PRAYER_TAG = "Prayer";
    
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
        }
        
        return true;
    }
    
    public String getPrayerHTML() {
        float pFontWidth = (float)1.1 * mScale;
        float pFontHeight = (float)1.575 * mScale;
        float pComment = (float)0.8 * mScale;
        float authorWidth = (float)1.03 * mScale;
        float authorHeight = (float)1.825 * mScale;
        float versalWidth = (float)3.5 * mScale;
        float versalHeight = (float)0.75 * mScale;
        
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n");
        
        int langIndex = prayerCursor.getColumnIndexOrThrow(Database.LANGUAGE_COLUMN);
        String langCode = prayerCursor.getString(langIndex);
        Language lang = Language.get(langCode);
        if (!lang.rightToLeft) {
            sb.append("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">\n");
        } else {
            sb.append("<html dir=\"rtl\" xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">\n");
        }
        boolean useClassicTheme = Preferences.getInstance(App.getApp()).useClassicTheme();
        String bgColor;
        String versalAndAuthorColor;
        String font;
        if (useClassicTheme) {
            bgColor = "#D6D2C9";
            versalAndAuthorColor = "#992222";
            font = "Georgia";
        } else {
            bgColor = "#ffffff";
            versalAndAuthorColor = "#33b5e5";
            font = "\"sans-serif\"";
        }

        sb.append("<head>\n");
        sb.append("<meta content=\"text/html;charset=utf-8\" http-equiv=\"Content-Type\">\n");
        sb.append("<meta content=\"utf-8\" http-equiv=\"encoding\">");
        // for XHTML compliance, we add a title
        sb.append("<title>Prayer</title>\n");
        sb.append("<style type=\"text/css\">\n");
        sb.append("#prayer p {margin: 0 0px .75em 0px; color: #333333; font: normal ");
        sb.append(pFontWidth); sb.append("em/"); sb.append(pFontHeight); sb.append("em");
        sb.append(" "); sb.append(font);
        sb.append("; clear: both; text-indent: 1em;}\n");
        sb.append("#prayer p.opening {text-indent: 0;}\n");
        sb.append("body { padding: 8px; background: "); sb.append(bgColor); sb.append("; }\n");
        sb.append("#prayer p.commentcaps {font: normal ");
        sb.append(pComment); sb.append("em");
        sb.append(" \"sans-serif\"; color: #444433; text-transform: uppercase; margin: 0 0px 20px 5px; text-indent: 0; }\n");
        sb.append("#prayer p.comment {font: normal ");
        sb.append(pComment); sb.append("em");
        sb.append(" \"sans-serif\"; color: #444433; margin: 0 0px .825em 1.5em; text-indent: 0; }\n");
        sb.append("#prayer p.noindent {text-indent: 0; margin-bottom: .25em;}\n");
        sb.append("#prayer p.commentnoindent {font: normal ");
        sb.append(pComment); sb.append("em");
        sb.append(" \"sans-serif\"; color: #444433; margin: 0 0px 15px 5px; text-indent: 0;}\n");
        sb.append("#prayer h4#author { float: right; margin: 0 5px 25px 0; font: ");
        if (useClassicTheme) {
            sb.append("italic ");
        }
        sb.append(authorWidth); sb.append("em/"); sb.append(authorHeight); sb.append("em");
        sb.append(" "); sb.append(font);
        sb.append("; color: ");
        sb.append(versalAndAuthorColor);
        sb.append("; text-indent: 0.325em; font-weight: normal; font-size:1.25em }\n");
        sb.append("span.versal {float: left; display: inline; position: relative; color: ");
        sb.append(versalAndAuthorColor); sb.append("; font: normal ");
        sb.append(versalWidth); sb.append("em/"); sb.append(versalHeight); sb.append("em");
        sb.append(" "); sb.append(font);
        sb.append("; margin: .115em .15em 0 0em; padding: 0;}\n");
        sb.append("</style>\n</head>\n<body>\n<div id=\"prayer\">");
        
        // append the prayer text
        int textIndex = prayerCursor.getColumnIndexOrThrow(Database.PRAYERTEXT_COLUMN);
        String prayerText = prayerCursor.getString(textIndex);
        sb.append(prayerText);
        
        // append the author
        int authorIndex = prayerCursor.getColumnIndexOrThrow(Database.AUTHOR_COLUMN);
        String authorText = prayerCursor.getString(authorIndex);
        sb.append("<h4 id=\"author\">"); sb.append(authorText); sb.append("</h4>");
        
        // append the citation (if there is one)
        int citationIndex = prayerCursor.getColumnIndexOrThrow(Database.CITATION_COLUMN);
        String citationText = prayerCursor.getString(citationIndex);
        if (citationText != null && citationText.length() != 0) {
            sb.append("<p class=\"comment\"><br/><br/>");
            sb.append(citationText);
            sb.append("</p>");
        }
        
        // close up the document
        sb.append("</div>\n</body>\n</html>");
        
        return sb.toString();
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
