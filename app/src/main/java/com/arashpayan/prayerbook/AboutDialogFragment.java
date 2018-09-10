/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.arashpayan.prayerbook;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import com.arashpayan.util.Graphics;
import com.arashpayan.util.L;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author arash
 */
public class AboutDialogFragment extends DialogFragment {

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

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstance) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(requireContext());
        alertBuilder.setTitle(R.string.about);
        String html = "";
        try {
            html = getStringFromInputStream(getResources().openRawResource(R.raw.about));
        } catch (IOException ex) {
            L.e("Unable to open 'about' HTML", ex);
        }
        TextView tv = new TextView(requireContext());
        int pad = Graphics.pixels(requireContext(), 24);
        tv.setPadding(pad, pad, pad, pad);
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        Spanned spanned;
        if (Build.VERSION.SDK_INT >= 24) {
            spanned = Html.fromHtml(html, 0);
        } else {
            spanned = Html.fromHtml(html);
        }
        tv.setText(spanned);
        alertBuilder.setView(tv);
        alertBuilder.setPositiveButton(android.R.string.ok, new OnClickListener() {

            public void onClick(DialogInterface arg0, int arg1) {
                requireFragmentManager().popBackStack();
            }
        });
        
        return alertBuilder.create();
    }
}
