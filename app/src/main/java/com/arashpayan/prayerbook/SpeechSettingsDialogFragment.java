package com.arashpayan.prayerbook;

import android.app.Dialog;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import androidx.fragment.app.DialogFragment;
import com.google.android.material.slider.Slider;
import com.google.android.material.switchmaterial.SwitchMaterial;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SpeechSettingsDialogFragment extends DialogFragment {

    public static final String TAG = "SpeechSettingsDialog";
    private static final String ARG_LANGUAGE_CODE = "language_code";
    private TextToSpeech tts;

    public static SpeechSettingsDialogFragment newInstance(String languageCode) {
        SpeechSettingsDialogFragment fragment = new SpeechSettingsDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_LANGUAGE_CODE, languageCode);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_speech_settings, null);

        final Prefs prefs = Prefs.get();
        final DecimalFormat formatter = new DecimalFormat("0.0x");

        // Rate Slider
        final Slider rateSlider = view.findViewById(R.id.rate_slider);
        final TextView rateValueText = view.findViewById(R.id.rate_value_text);
        rateSlider.setValue(prefs.getSpeechRate());
        rateValueText.setText(formatter.format(rateSlider.getValue()));
        rateSlider.addOnChangeListener((slider, value, fromUser) -> {
            rateValueText.setText(formatter.format(value));
        });

        // Pitch Slider
        final Slider pitchSlider = view.findViewById(R.id.pitch_slider);
        final TextView pitchValueText = view.findViewById(R.id.pitch_value_text);
        pitchSlider.setValue(prefs.getSpeechPitch());
        pitchValueText.setText(formatter.format(pitchSlider.getValue()));
        pitchSlider.addOnChangeListener((slider, value, fromUser) -> {
            pitchValueText.setText(formatter.format(value));
        });

        // Speak on open switch
        final SwitchMaterial speakOnOpenSwitch = view.findViewById(R.id.speak_on_open_switch);
        speakOnOpenSwitch.setChecked(prefs.getSpeakPrayerOnOpen());

        // Voice Spinner
        final Spinner voiceSpinner = view.findViewById(R.id.voice_spinner);
        final List<Voice> availableVoices = new ArrayList<>();
        final List<String> voiceNames = new ArrayList<>();
        voiceNames.add("Default"); // Initial placeholder

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, voiceNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        voiceSpinner.setAdapter(adapter);

        String argLanguageCode = getArguments() != null ? getArguments().getString(ARG_LANGUAGE_CODE) : null;
        final String languageCode = argLanguageCode != null ? argLanguageCode : prefs.getLanguage();
        final Language language = Language.get(languageCode);

        tts = new TextToSpeech(requireContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                Locale locale = language.locale;
                String currentVoiceName = prefs.getSpeechVoice();

                voiceNames.clear();
                availableVoices.clear();
                voiceNames.add("System Default");
                availableVoices.add(null); // Represents system default

                int selectedIndex = 0;
                try {
                    for (Voice voice : tts.getVoices()) {
                        if (voice.getLocale().getLanguage().equals(locale.getLanguage())) {
                            availableVoices.add(voice);
                            voiceNames.add(voice.getName());
                            if (voice.getName().equals(currentVoiceName)) {
                                selectedIndex = voiceNames.size() - 1;
                            }
                        }
                    }
                } catch (Exception e) {
                    // Some TTS engines might throw exceptions on getVoices()
                }

                int finalSelectedIndex = selectedIndex;
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        adapter.notifyDataSetChanged();
                        voiceSpinner.setSelection(finalSelectedIndex);
                    });
                }
            }
        });

        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.speech_settings) + " (" + getString(language.humanName) + ")")
                .setView(view)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    // Save the final values when the user clicks OK
                    prefs.setSpeechRate(rateSlider.getValue());
                    prefs.setSpeechPitch(pitchSlider.getValue());
                    prefs.setSpeakPrayerOnOpen(speakOnOpenSwitch.isChecked());
                    
                    int selection = voiceSpinner.getSelectedItemPosition();
                    if (selection >= 0 && selection < availableVoices.size()) {
                        Voice selectedVoice = availableVoices.get(selection);
                        prefs.setSpeechVoice(selectedVoice != null ? selectedVoice.getName() : null);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (tts != null) {
            tts.shutdown();
        }
    }
}
