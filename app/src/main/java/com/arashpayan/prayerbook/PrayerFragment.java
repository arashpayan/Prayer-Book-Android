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
import android.support.annotation.NonNull;
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
    
    private static final String PRAYER_ID_ARGUMENT = "PrayerId";

    public static PrayerFragment newInstance(long prayerId) {
        PrayerFragment fragment = new PrayerFragment();
        Bundle args = new Bundle();
        args.putLong(PRAYER_ID_ARGUMENT, prayerId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        if (arguments == null) {
            throw new RuntimeException("Fragment should be created via newInstance");
        }
        long prayerId = arguments.getLong(PRAYER_ID_ARGUMENT, -1);
        if (prayerId == -1) {
            throw new IllegalArgumentException("You must provide a prayer id to this fragment");
        }
        prayerCursor = DB.get().getPrayer(prayerId);
        prayerCursor.moveToFirst();
        mScale = Prefs.get(App.getApp()).getPrayerTextScalar();
        
        setHasOptionsMenu(true);
    }
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        requireActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        
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
        
        requireActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
    }

    @Override
    public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.prayer, menu);

        // set the current value for classic theme
        menu.findItem(R.id.action_classic_theme).setChecked(Prefs.get(App.getApp()).useClassicTheme());
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // .75 to 1.60
        switch (item.getItemId()) {
            case android.R.id.home:
                requireActivity().onBackPressed();
                break;
            case R.id.action_increase_text_size:
                if (mScale < 1.6f) {
                    mScale += 0.05f;
                    Prefs.get(App.getApp()).setPrayerTextScalar(mScale);
                    reloadPrayer();
                }
                break;
            case R.id.action_decrease_text_size:
                if (mScale > .75) {
                    mScale -= 0.05f;
                    Prefs.get(App.getApp()).setPrayerTextScalar(mScale);
                    reloadPrayer();
                }
                break;
            case R.id.action_classic_theme:
                boolean useClassic = !item.isChecked(); // toggle the value
                item.setChecked(useClassic);
                Prefs.get(App.getApp()).setUseClassicTheme(useClassic);
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

        ActionBar ab = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (ab != null) {
            ab.setTitle(null);
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setHomeButtonEnabled(true);
        }
    }

    public String getPrayerHTML() {
        float pFontWidth = 1.1f * mScale;
        float pFontHeight = 1.575f * mScale;
        float pComment = 0.8f * mScale;
        float authorWidth = 1.03f * mScale;
        float authorHeight = 1.825f * mScale;
        float versalWidth = 3.5f * mScale;
        float versalHeight = 0.75f * mScale;

        HashMap<String, String> args = new HashMap<>();
        args.put("fontWidth", String.format(Locale.US, "%f", pFontWidth));
        args.put("fontHeight", String.format(Locale.US, "%f", pFontHeight));
        args.put("commentSize", String.format(Locale.US, "%f", pComment));
        args.put("authorWidth", String.format(Locale.US, "%f", authorWidth));
        args.put("authorHeight", String.format(Locale.US, "%f", authorHeight));
        args.put("versalWidth", String.format(Locale.US, "%f", versalWidth));
        args.put("versalHeight", String.format(Locale.US, "%f", versalHeight));
        boolean useClassicTheme = Prefs.get(App.getApp()).useClassicTheme();
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

        int textIndex = prayerCursor.getColumnIndexOrThrow(DB.PRAYERTEXT_COLUMN);
        String prayerText = prayerCursor.getString(textIndex);
        args.put("prayer", prayerText);

        int authorIndex = prayerCursor.getColumnIndexOrThrow(DB.AUTHOR_COLUMN);
        String authorText = prayerCursor.getString(authorIndex);
        args.put("author", authorText);

        int citationIndex = prayerCursor.getColumnIndexOrThrow(DB.CITATION_COLUMN);
        String citationText = prayerCursor.getString(citationIndex);
        if (citationText.isEmpty()) {
            args.put("citation", "");
        } else {
            String citationHTML = String.format("<p class=\"comment\"><br/><br/>%s</p>", citationText);
            args.put("citation", citationHTML);
        }

        int langIndex = prayerCursor.getColumnIndexOrThrow(DB.LANGUAGE_COLUMN);
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
        int searchTextIndex = prayerCursor.getColumnIndexOrThrow(DB.SEARCHTEXT_COLUMN);
        
        return prayerCursor.getString(searchTextIndex);
    }

    @TargetApi(19)
    private void printPrayer() {
        if (mWebView == null) {
            // shouldn't happen, but just in case
            return;
        }
        
        PrintManager manager = (PrintManager)requireContext().getSystemService(Context.PRINT_SERVICE);
        if (manager == null) {
            throw new RuntimeException("Where's the print manager?");
        }
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
