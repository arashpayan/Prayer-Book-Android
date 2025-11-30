package com.arashpayan.prayerbook

import android.app.Dialog
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.slider.Slider
import com.google.android.material.switchmaterial.SwitchMaterial
import java.text.DecimalFormat

class SpeechSettingsDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_speech_settings, null)

        val prefs = Prefs.get()
        val formatter = DecimalFormat("0.0x")

        // Rate Slider
        val rateSlider = view.findViewById<Slider>(R.id.rate_slider)
        val rateValueText = view.findViewById<TextView>(R.id.rate_value_text)
        rateSlider.value = prefs.getSpeechRate()
        rateValueText.text = formatter.format(rateSlider.value)
        rateSlider.addOnChangeListener { _, value, _ ->
            rateValueText.text = formatter.format(value)
        }

        // Pitch Slider
        val pitchSlider = view.findViewById<Slider>(R.id.pitch_slider)
        val pitchValueText = view.findViewById<TextView>(R.id.pitch_value_text)
        pitchSlider.value = prefs.getSpeechPitch()
        pitchValueText.text = formatter.format(pitchSlider.value)
        pitchSlider.addOnChangeListener { _, value, _ ->
            pitchValueText.text = formatter.format(value)
        }

        // Speak on open switch
        val speakOnOpenSwitch = view.findViewById<SwitchMaterial>(R.id.speak_on_open_switch)
        speakOnOpenSwitch.isChecked = prefs.speakPrayerOnOpen


        return AlertDialog.Builder(requireContext())
            .setTitle(R.string.speech_settings)
            .setView(view)
            .setPositiveButton(R.string.ok) { _, _ ->
                // Save the final values when the user clicks OK
                prefs.setSpeechRate(rateSlider.value)
                prefs.setSpeechPitch(pitchSlider.value)
                prefs.speakPrayerOnOpen = speakOnOpenSwitch.isChecked
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
    }

    companion object {
        const val TAG = "SpeechSettingsDialog"
    }
}
