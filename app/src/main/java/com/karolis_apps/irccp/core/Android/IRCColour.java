package com.karolis_apps.irccp.core.Android;

import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;

import com.karolis_apps.irccp.core.IRC.utils.IRCProtocol.Colours;

import java.util.Arrays;

public class IRCColour {
    public static Spannable fromIRCText(String text){
        String cleanText = cleanIRCText(text);
        Spannable s = new SpannableString(cleanText);
        int state_bold_start = -1;
        int state_italics_start = -1;
        int state_underline_start = -1;
        int removed_chars = 0;
        for (int c = 0; c < text.length(); c++) {
            char ch = text.charAt(c);
            switch (ch){
                case Colours.BOLD:
                    if(state_bold_start != -1){
                        s.setSpan(new StyleSpan(Typeface.BOLD), state_bold_start, c - removed_chars, 0);
                        state_bold_start = -1;
                    } else {
                        state_bold_start = c - removed_chars;
                    }
                    removed_chars++;
                    break;
                case Colours.ITALICS:
                    if(state_italics_start != -1){
                        s.setSpan(new StyleSpan(Typeface.ITALIC), state_italics_start, c - removed_chars, 0);
                        state_italics_start = -1;
                    } else {
                        state_italics_start = c - removed_chars;
                    }
                    removed_chars++;
                    break;
                case Colours.UNDERLINE:
                    if(state_underline_start != -1){
                        s.setSpan(new UnderlineSpan(), state_underline_start, c - removed_chars, 0);
                        state_underline_start = -1;
                    } else {
                        state_underline_start = c - removed_chars;
                    }
                    removed_chars++;
                    break;
                case Colours.RESET:
                    //Apply all styles and close tags
                    if(state_bold_start != -1){
                        s.setSpan(new StyleSpan(Typeface.BOLD), state_bold_start, c - removed_chars, 0);
                    }
                    if(state_italics_start != -1){
                        s.setSpan(new StyleSpan(Typeface.ITALIC), state_italics_start, c - removed_chars, 0);
                    }
                    if(state_underline_start != -1){
                        s.setSpan(new UnderlineSpan(), state_underline_start, c - removed_chars, 0);
                    }
                    state_bold_start = -1;
                    state_italics_start = -1;
                    state_underline_start = -1;
                    removed_chars++;
                    break;
            }

        }
        return s;
    }

    public static String cleanIRCText(String text){
        //text = text.replaceAll("([\u0003](\\d{1,2})?([,](\\d{1,2}))?)", "");
        return text.replace(String.valueOf(Colours.ITALICS),"").replace(String.valueOf(Colours.BOLD), "").replace(String.valueOf(Colours.UNDERLINE), "").replace(String.valueOf(Colours.RESET), "");
    }
}
