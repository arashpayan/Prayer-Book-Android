package com.arashpayan.prayerbook;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;

import com.arashpayan.prayerbook.database.Prayer;
import com.arashpayan.prayerbook.database.PrayersDB;
import com.arashpayan.prayerbook.database.UserDB;
import com.arashpayan.prayerbook.thread.UiRunnable;
import com.arashpayan.prayerbook.thread.WorkerRunnable;
import com.google.android.material.appbar.MaterialToolbar;
import com.samskivert.mustache.Mustache;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Locale;

/**
 *
 * @author arash
 */
public class PrayerFragment extends Fragment implements UserDB.Listener, MenuProvider {
    
    private WebView mWebView = null;
    private Prayer prayer;
    private float mScale = 1.0f;
    private long prayerId;
    @Nullable private MenuItem bookmarkMenuItem;
    private boolean bookmarked = false;
    
    private static final String PRAYER_ID_ARGUMENT = "PrayerId";

    @NonNull
    static PrayerFragment newInstance(long prayerId) {
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
        prayerId = arguments.getLong(PRAYER_ID_ARGUMENT, -1);
        if (prayerId == -1) {
            throw new RuntimeException("You must provide a prayer id to this fragment");
        }
        App.runInBackground(new WorkerRunnable() {
            @Override
            public void run() {
                Prayer p = PrayersDB.get().getPrayer(prayerId);
                App.runOnUiThread(new UiRunnable() {
                    @Override
                    public void run() {
                        prayer = p;
                        reloadPrayer();
                    }
                });
            }
        });
        mScale = Prefs.get().getPrayerTextScalar();
    }
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        requireActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        
        mWebView = new WebView(requireContext());
        mWebView.getSettings().setSupportZoom(true);
        mWebView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mWebView.setKeepScreenOn(true);
        reloadPrayer();

        return mWebView;
    }

    private void reloadPrayer() {
        if (prayer == null) {
            return;
        }
        if (mWebView == null) {
            return;
        }
        mWebView.loadDataWithBaseURL(null, getPrayerHTML(), "text/html", "UTF-8", null);
    }

    @Override
    public void onPause() {
        super.onPause();
        
        requireActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);

        UserDB.get().removeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        UserDB.get().addListener(this);
        // check if this prayer is bookmarked, and if so, change the icon color
        App.runInBackground(new WorkerRunnable() {
            @Override
            public void run() {
                // It's bookmarked, so we need to change the color
                bookmarked = UserDB.get().isBookmarked(prayerId);
                App.runOnUiThread(new UiRunnable() {
                    @Override
                    public void run() {
                        updateBookmarkIconColor();
                    }
                });
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialToolbar toolbar = requireActivity().findViewById(R.id.prayer_toolbar);
        toolbar.setTitle("");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_24dp);
        toolbar.addMenuProvider(this);
        // add a listener so we can go back when tapped
        toolbar.setNavigationOnClickListener(view1 -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
    }

    private String getPrayerHTML() {
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
        boolean useClassicTheme = Prefs.get().useClassicTheme();
        String bgColor;
        String textColor;
        String commentColor;
        String versalAndAuthorColor;
        String font;
        String italicOrNothing;
        if (useClassicTheme) {
            bgColor = "#D6D2C9";
            textColor = "#333333";
            commentColor = "#444433";
            versalAndAuthorColor = "#992222";
            font = "Georgia";
            italicOrNothing = "italic";
        } else {
            boolean isDark = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
            if (isDark) {
                bgColor = "#000000";
                textColor = "#cccccc";
                commentColor = "#ddddcc";
            } else {
                bgColor = "#ffffff";
                textColor = "#333333";
                commentColor = "#444433";
            }
            versalAndAuthorColor = "#33b5e5";
            font = "sans-serif";
            italicOrNothing = "";
        }
        args.put("backgroundColor", bgColor);
        args.put("textColor", textColor);
        args.put("commentColor", commentColor);
        args.put("versalAndAuthorColor", versalAndAuthorColor);
        args.put("font", font);
        args.put("italicOrNothing", italicOrNothing);
        args.put("prayer", prayer.text);
        args.put("author", prayer.author);

        if (prayer.citation.isEmpty()) {
            args.put("citation", "");
        } else {
            String citationHTML = String.format("<p class=\"comment\"><br/><br/>%s</p>", prayer.citation);
            args.put("citation", citationHTML);
        }

        if (prayer.language.rightToLeft) {
            args.put("layoutDirection", "rtl");
        } else {
            args.put("layoutDirection", "ltr");
        }

        InputStream is = getResources().openRawResource(R.raw.prayer_template);
        InputStreamReader isr = new InputStreamReader(is);

        return Mustache.compiler().escapeHTML(false).compile(isr).execute(args);
    }

    private String getPrayerText() {
        return prayer.searchText + "\n\n" + prayer.author;
    }

    private void printPrayer() {
        if (mWebView == null) {
            // shouldn't happen, but just in case
            return;
        }
        
        PrintManager manager = (PrintManager)requireContext().getSystemService(Context.PRINT_SERVICE);
        if (manager == null) {
            throw new RuntimeException("Where's the print manager?");
        }
        PrintDocumentAdapter adapter = mWebView.createPrintDocumentAdapter("Prayer");
        
        String jobName = getString(R.string.app_name) + " " + getString(R.string.document);
        manager.print(jobName, adapter, new PrintAttributes.Builder().build());
    }

    @UiThread
    private void updateBookmarkIconColor() {
        if (bookmarkMenuItem == null) {
            return;
        }
        @ColorInt int color;
        if (bookmarked) {
            color = ContextCompat.getColor(requireContext(), R.color.prayer_book_accent);
        } else {
            color = Color.WHITE;
        }
        Drawable icon = bookmarkMenuItem.getIcon();
        if (icon != null) { // it should never be null
            DrawableCompat.setTint(icon, color);
        }
    }

    @UiThread
    private void toggleBookmark() {
        App.runInBackground(new WorkerRunnable() {
            @Override
            public void run() {
                UserDB db = UserDB.get();
                boolean isBookmarked = db.isBookmarked(prayerId);
                if (isBookmarked) {
                    db.deleteBookmark(prayerId);
                } else {
                    db.addBookmark(prayerId);
                }
            }
        });
    }

    //region UserDB.Listener

    @Override
    public void onBookmarkAdded(long bookmarkPrayerId) {
        if (bookmarkPrayerId != prayerId) {
            return;
        }
        bookmarked = true;
        updateBookmarkIconColor();
    }

    @Override
    public void onBookmarkDeleted(long bookmarkPrayerId) {
        if (bookmarkPrayerId != prayerId) {
            return;
        }
        bookmarked = false;
        updateBookmarkIconColor();
    }

    //region MenuProvider

    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.prayer, menu);

        // set the current value for classic theme
        menu.findItem(R.id.action_classic_theme).setChecked(Prefs.get().useClassicTheme());

        bookmarkMenuItem = menu.findItem(R.id.action_toggle_bookmark);
        updateBookmarkIconColor();
    }

    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem item) {
        Prefs prefs = Prefs.get();
        // .75 to 1.60
        int itemId = item.getItemId();
        if (itemId == R.id.action_increase_text_size) {
            if (mScale < 1.6f) {
                mScale += 0.05f;
                prefs.setPrayerTextScalar(mScale);
                reloadPrayer();
            }
        } else if (itemId == R.id.action_decrease_text_size) {
            if (mScale > .75) {
                mScale -= 0.05f;
                prefs.setPrayerTextScalar(mScale);
                reloadPrayer();
            }
        } else if (itemId == R.id.action_classic_theme) {
            boolean useClassic = !item.isChecked(); // toggle the value
            item.setChecked(useClassic);
            prefs.setUseClassicTheme(useClassic);
            reloadPrayer();
        } else if (itemId == R.id.action_share_prayer) {
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(Intent.EXTRA_TEXT, getPrayerText());
            startActivity(Intent.createChooser(sharingIntent, "Share via"));
        } else if (itemId == R.id.action_print_prayer) {
            printPrayer();
        } else if (itemId == R.id.action_toggle_bookmark) {
            toggleBookmark();
        } else {
            // unexpected id
            return false;
        }

        return true;
    }

    //endregion
}
