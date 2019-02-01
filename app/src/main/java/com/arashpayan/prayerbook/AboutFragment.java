package com.arashpayan.prayerbook;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.arashpayan.prayerbook.databinding.FragmentAboutBinding;
import com.arashpayan.util.L;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

public class AboutFragment extends Fragment {

    static final String TAG = "about";

    @NonNull
    static AboutFragment newInstance() {
        return new AboutFragment();
    }

    private FragmentAboutBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_about, container, false);
        loadAboutHtml();
        binding.webview.setWebViewClient(new WVC());

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();

        Toolbar toolbar = requireActivity().findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.getMenu().clear();
            toolbar.setTitle(getString(R.string.about));
            toolbar.setNavigationIcon(null);
        }
    }

    //region Business logic

    private String getStringFromInputStream(InputStream inputStream) throws IOException {
        InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        char[] buffer = new char[8];
        StringWriter sw = new StringWriter();
        int numRead;
        while ((numRead = reader.read(buffer, 0, buffer.length)) != -1) {
            sw.write(buffer, 0, numRead);
        }

        return sw.toString();
    }

    private void loadAboutHtml() {
        try {
            String html = getStringFromInputStream(getResources().openRawResource(R.raw.about));
            binding.webview.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
        } catch (IOException ex) {
            L.w("Unable to open 'about' html", ex);
        }
    }

    //endregion

    //region WebViewClient

    private class WVC extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);

            return true;
        }
    }

    //endregion
}
