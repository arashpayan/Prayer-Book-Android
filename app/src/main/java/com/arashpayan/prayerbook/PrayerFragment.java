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
import android.widget.ShareActionProvider;

/**
 *
 * @author arash
 */
public class PrayerFragment extends Fragment {
    
    private WebView mWebView = null;
    private Cursor prayerCursor;
    private float mScale = 1.0f;
    
    private static final int ACTIONITEM_INCREASETEXT        = 1;
    private static final int ACTIONITEM_DECREASETEXT        = 2;
    private static final int ACTIONITEM_SHARE               = 3;
    private static final int ACTIONITEM_PRINT               = 4;
    
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
        mWebView.loadDataWithBaseURL(null, getPrayerHTML(), "text/html", "UTF-8", null);

        return mWebView;
    }
    
    @Override
    public void onPause() {
        super.onPause();
        
        getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
    }
    
    @Override
    public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) {
        menu.add(0, ACTIONITEM_INCREASETEXT, ACTIONITEM_INCREASETEXT, "A+").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add(0, ACTIONITEM_DECREASETEXT, ACTIONITEM_DECREASETEXT, "A-").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        
        MenuItem shareItem = menu.add(0, ACTIONITEM_SHARE, ACTIONITEM_SHARE, R.string.share);
        ShareActionProvider provider = new ShareActionProvider(getActivity());
        shareItem.setActionProvider(provider);
        
        if (Build.VERSION.SDK_INT >= 19) {
            menu.add(0, ACTIONITEM_PRINT, ACTIONITEM_PRINT, R.string.print_ellipsis);
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // .75 to 1.60
        switch (item.getItemId()) {
            case ACTIONITEM_INCREASETEXT:
                if (mScale < 1.6f) {
                    mScale += 0.05f;
                    Preferences.getInstance(App.getApp()).setPrayerTextScalar(mScale);
                    mWebView.loadDataWithBaseURL(null, getPrayerHTML(), "text/html", "UTF-8", null);
                }
                break;
            case ACTIONITEM_DECREASETEXT:
                if (mScale > .75) {
                    mScale -= 0.05f;
                    Preferences.getInstance(App.getApp()).setPrayerTextScalar(mScale);
                    mWebView.loadDataWithBaseURL(null, getPrayerHTML(), "text/html", "UTF-8", null);
                }
                break;
            case ACTIONITEM_SHARE:
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(Intent.EXTRA_TEXT, getPrayerText());
                startActivity(Intent.createChooser(sharingIntent, "Share via"));
                break;
            case ACTIONITEM_PRINT:
                printPrayer();
                break;
            case android.R.id.home:
                getFragmentManager().popBackStack();
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
        sb.append("<head>\n");
        sb.append("<meta content=\"text/html;charset=utf-8\" http-equiv=\"Content-Type\">\n");
        sb.append("<meta content=\"utf-8\" http-equiv=\"encoding\">");
        // for XHTML compliance, we add a title
        sb.append("<title>Prayer</title>\n");
        sb.append("<style type=\"text/css\">\n");
        sb.append("#prayer p {margin: 0 0px .75em 5px; color: #333333; font: normal ");
        sb.append(pFontWidth); sb.append("em/"); sb.append(pFontHeight); sb.append("em");
        sb.append(" \"sans-serif\"; clear: both; text-indent: 1em;}\n");
        sb.append("#prayer p.opening {text-indent: 0;}\n");
        // the background image goes here
        sb.append("body { background: #ffffff; }\n");
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
        sb.append(authorWidth); sb.append("em/"); sb.append(authorHeight); sb.append("em");
        sb.append(" \"sans-serif\"; color: #33b5e5; text-indent: 0.325em; font-weight: normal; font-size:1.25em }\n");
        sb.append("span.versal {float: left; display: inline; position: relative; color: #33b5e5; font: normal ");
        sb.append(versalWidth); sb.append("em/"); sb.append(versalHeight); sb.append("em");
        sb.append(" \"sans-serif\"; margin: .115em .15em 0 0em; padding: 0;}\n");
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
        if (citationText == null || citationText.length() == 0) {
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

    @TargetApi(19)
    private void printPrayer() {
        if (mWebView == null) {
            // shouldn't happen, but just in case
            return;
        }
        
        PrintManager manager = (PrintManager)getActivity().getSystemService(Context.PRINT_SERVICE);
        PrintDocumentAdapter adapter = mWebView.createPrintDocumentAdapter();
        
        String jobName = getString(R.string.app_name) + " " + getString(R.string.document);
        manager.print(jobName, adapter, new PrintAttributes.Builder().build());
    }
}
