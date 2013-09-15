package com.arashpayan.prayerbook;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SearchView;
import android.widget.TextView;

import com.arashpayan.prayerbook.event.LanguagesChangedEvent;
import com.arashpayan.util.Graphics;
import com.arashpayan.util.L;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class PrayerBook extends FragmentActivity implements ActionBar.OnNavigationListener
{
    private static final String TAG = "PrayerBook";

    private final static int ACTIONITEM_SEARCH              = 4;
    private final static int ACTIONITEM_LANGUAGES           = 5;
    private final static int ACTIONITEM_ABOUT               = 6;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
//        getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
//        getActionBar().setListNavigationCallbacks(new NavigationSpinnerAdapter(this), this);
//        getActionBar().setTitle(null);
        
        int dbVersion = Preferences.getInstance(getApplication()).getDatabaseVersion();
        if (dbVersion != 1) {
            // then we need to copy over the latest database
            File databaseFile = new File(getFilesDir(), "pbdb.db");
            Database.databaseFile = databaseFile; 
            Log.i(TAG, "database file: " + databaseFile.getAbsolutePath());
            try {
                BufferedInputStream is = new BufferedInputStream(getAssets().open("pbdb.jet"), 8192);
                OutputStream os = new BufferedOutputStream(new FileOutputStream(databaseFile), 8192);
                byte[] data = new byte[4096];
                while (is.available() != 0) {
                    int numRead = is.read(data);
                    if (numRead != 0)
                        os.write(data);
                }
                is.close();
                os.close();
                
            } catch (IOException ex) {
                Log.w(TAG, "Error writing prayer database", ex);
            }
        }
        
        Database.getInstance();

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
//        menu.add(0, ACTIONITEM_SEARCH, ACTIONITEM_SEARCH, R.string.search)
//                .setIcon(R.drawable.ic_action_search)
//                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menu.add(0, ACTIONITEM_LANGUAGES, ACTIONITEM_LANGUAGES, R.string.languages);
        menu.add(0, ACTIONITEM_ABOUT, ACTIONITEM_ABOUT, R.string.about);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case ACTIONITEM_SEARCH:

                break;
            case ACTIONITEM_LANGUAGES:
                showLanguageDialog();
                break;
            case ACTIONITEM_ABOUT:
                AboutDialogFragment adf = new AboutDialogFragment();
                adf.show(getSupportFragmentManager(), "dialog");
                break;
            default:
                return false;
        }

        return true;
    }

    public void onResume() {
        super.onResume();

        getActionBar().setTitle(R.string.app_name);
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        return true;
    }

    private void showLanguageDialog() {
        Preferences prefs = Preferences.getInstance(getApplication());
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.languages);
        builder.setMultiChoiceItems(new CharSequence[]{getString(R.string.english),
                getString(R.string.espanol),
                getString(R.string.farsi),
                getString(R.string.francais),
                getString(R.string.nederlands)},
                new boolean[]{
                        prefs.isEnglishEnabled(),
                        prefs.isSpanishEnabled(),
                        prefs.isPersianEnabled(),
                        prefs.isFrenchEnabled(),
                        prefs.isDutchEnabled()},
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        switch (which) {
                            case 0:
                                Preferences.getInstance(getApplication()).setEnglishEnabled(isChecked);
                                break;
                            case 1:
                                Preferences.getInstance(getApplication()).setSpanishEnabled(isChecked);
                                break;
                            case 2:
                                Preferences.getInstance(getApplication()).setPersianEnabled(isChecked);
                                break;
                            case 3:
                                Preferences.getInstance(getApplication()).setFrenchEnabled(isChecked);
                                break;
                            case 4:
                                Preferences.getInstance(getApplication()).setDutchEnabled(isChecked);
                                break;
                            default:
                                break;
                        }
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

    static private class NavigationSpinnerAdapter extends BaseAdapter {

        private Context mContext;

        public NavigationSpinnerAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getCount() {
            return 2;
        }

        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            TextView tv;
            if (convertView != null) {
                tv = (TextView)convertView;
            } else {
                tv = new TextView(mContext);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
                int eightDp = Graphics.pixels(mContext, 8);
                tv.setPadding(eightDp, eightDp, eightDp, eightDp);
            }

            switch (position) {
                case 0:
                    tv.setText(R.string.prayers);
                    break;
                case 1:
                    tv.setText(R.string.favorites);
                    break;
            }

            return tv;
        }

        @Override
        public Object getItem(int i) {
            return Integer.valueOf(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView tv;
            if (convertView != null) {
                tv = (TextView)convertView;
            } else {
                tv = new TextView(mContext);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            }

            switch (position) {
                case 0:
                    tv.setText(R.string.prayers);
                    break;
                case 1:
                    tv.setText(R.string.favorites);
                    break;
            }

            return tv;
        }
    }
}
