package com.arashpayan.prayerbook;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;

import com.arashpayan.prayerbook.event.LanguagesChangedEvent;

public class PrayerBook extends FragmentActivity implements ActionBar.OnNavigationListener {

    private final static int ACTIONITEM_LANGUAGES           = 5;
    private final static int ACTIONITEM_ABOUT               = 6;
    private final static int ACTIONITEM_CLASSIC_THEME       = 7;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            CategoriesFragment categoriesFragment = new CategoriesFragment();
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(android.R.id.content, categoriesFragment, CategoriesFragment.CATEGORIES_TAG);
            ft.commit();
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, ACTIONITEM_LANGUAGES, ACTIONITEM_LANGUAGES, R.string.languages);
        menu.add(0, ACTIONITEM_ABOUT, ACTIONITEM_ABOUT, R.string.about);
        MenuItem item = menu.add(0, ACTIONITEM_CLASSIC_THEME, ACTIONITEM_CLASSIC_THEME, R.string.classic_theme);
        item.setCheckable(true);
        item.setChecked(Preferences.getInstance(App.getApp()).useClassicTheme());

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case ACTIONITEM_LANGUAGES:
                showLanguageDialog();
                break;
            case ACTIONITEM_ABOUT:
                AboutDialogFragment adf = new AboutDialogFragment();
                adf.show(getSupportFragmentManager(), "dialog");
                break;
            case ACTIONITEM_CLASSIC_THEME:
                boolean useClassic = !menuItem.isChecked(); // toggle the value
                menuItem.setChecked(useClassic);
                Preferences.getInstance(App.getApp()).setUseClassicTheme(useClassic);
                break;
            default:
                return false;
        }

        return true;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getActionBar() != null) {
            getActionBar().setTitle(R.string.app_name);
        }
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        return true;
    }

    private void showLanguageDialog() {
        Preferences prefs = Preferences.getInstance(getApplication());
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.languages);
        final Language[] langs = Language.values();
        CharSequence[] langSequences = new CharSequence[langs.length];
        boolean[] langEnabled = new boolean[langs.length];
        for (int i=0; i<langs.length; i++) {
            langSequences[i] = getString(langs[i].humanName);
            langEnabled[i] = prefs.isLanguageEnabled(langs[i]);
        }
        builder.setMultiChoiceItems(langSequences, langEnabled,
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        Preferences.getInstance(App.getApp()).setLanguageEnabled(langs[which], isChecked);
                    }
                });
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                App.postOnBus(new LanguagesChangedEvent());
            }
        });
        builder.show();
    }
}
