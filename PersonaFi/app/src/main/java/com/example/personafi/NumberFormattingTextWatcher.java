package com.example.personafi;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import java.text.DecimalFormat;
import java.util.Locale;

public class NumberFormattingTextWatcher implements TextWatcher {
    private final EditText editText;
    private final DecimalFormat formatter;
    private boolean isFormatting = false;

    public NumberFormattingTextWatcher(EditText editText) {
        this.editText = editText;
        this.formatter = new DecimalFormat("#,##0.##");
        this.formatter.setParseBigDecimal(true);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public void afterTextChanged(Editable s) {
        if (isFormatting) return;

        isFormatting = true;
        String original = s.toString();
        String clean = original.replaceAll("[^\\d.]", "");

        try {
            if (!clean.isEmpty()) {
                double number = Double.parseDouble(clean);
                String formatted = formatter.format(number);
                if (!formatted.equals(original)) {
                    editText.setText(formatted);
                    editText.setSelection(formatted.length());
                }
            }
        } catch (NumberFormatException e) {
            // If parsing fails, leave the text as is
        }

        isFormatting = false;
    }

    public static String parseAmount(String formattedAmount) {
        if (formattedAmount == null || formattedAmount.isEmpty()) {
            return "0";
        }
        return formattedAmount.replaceAll("[^\\d.]", "");
    }
} 