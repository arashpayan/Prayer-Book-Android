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
import com.actionbarsherlock.app.SherlockDialogFragment;

/**
 *
 * @author arash
 */
public class AboutDialogFragment extends SherlockDialogFragment {
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstance) {
        Builder alertBuilder = new AlertDialog.Builder(getActivity());
        alertBuilder.setTitle("About");
        alertBuilder.setMessage("This is the message for the alert.");
        alertBuilder.setPositiveButton(android.R.string.ok, new OnClickListener() {

            public void onClick(DialogInterface arg0, int arg1) {
                getFragmentManager().popBackStack();
            }
        });
        
        return alertBuilder.create();
    }
}
