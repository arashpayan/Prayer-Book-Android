/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.arashpayan.prayerbook;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import com.arashpayan.util.Graphics;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

/**
 *
 * @author arash
 */
public class AboutDialogFragment extends DialogFragment {

    private String getStringFromInputStream(InputStream inputStream) throws IOException {
        InputStreamReader reader = new InputStreamReader(inputStream, "utf-8");
        char[] buffer = new char[8];
        StringWriter sw = new StringWriter();
        int numRead;
        while ((numRead = reader.read(buffer, 0, buffer.length)) != -1) {
            sw.write(buffer, 0, numRead);
        }

        return sw.toString();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstance) {
        Builder alertBuilder = new AlertDialog.Builder(getActivity());
        alertBuilder.setTitle(R.string.about);
        String html = "";
        try {
            html = getStringFromInputStream(getResources().openRawResource(R.raw.about));
        } catch (IOException ex) {
        }
        TextView tv = new TextView(getActivity());
        int sixteenDp = Graphics.pixels(getActivity(), 16);
        tv.setPadding(sixteenDp, sixteenDp, sixteenDp, sixteenDp);
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        tv.setText(Html.fromHtml(html));
        alertBuilder.setView(tv);
        alertBuilder.setPositiveButton(android.R.string.ok, new OnClickListener() {

            public void onClick(DialogInterface arg0, int arg1) {
                getFragmentManager().popBackStack();
            }
        });
        
        return alertBuilder.create();
    }
}
