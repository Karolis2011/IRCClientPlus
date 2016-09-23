package com.karolis_apps.irccp.deprecated.core.Android;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;

import com.karolis_apps.irccp.deprecated.core.IRC.utils.IRCProtocol.Colours;

public class IRCColour {
    private static final int COLOUR_STATE_NONE = 0;
    private static final int COLOUR_STATE_FG = 1;
    private static final int COLOUR_STATE_BG = 2;
    private static final int COLOUR_STATE_COMMA = 3;
    public static Spannable fromIRCText(String text){
        String cleanText = cleanIRCText(text);
        Spannable s = new SpannableString(cleanText);
        //Styling statuses
        int state_bold_start = -1;
        int state_italics_start = -1;
        int state_underline_start = -1;
        //Colour statuses
        int fg_colour = -1;
        int fg_colour_start = -1;
        int bg_colour = -1;
        int bg_colour_start = -1;
        int colour_state = COLOUR_STATE_NONE;
        int colour_state_updated = 0;
        //Counter for removed chars at this point to offset position
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
                    if(fg_colour != -1){
                        s.setSpan(new ForegroundColorSpan(ircColourCodeToJava(fg_colour)), fg_colour_start, c - removed_chars, 0);
                    }
                    if(bg_colour != -1){
                        s.setSpan(new BackgroundColorSpan(ircColourCodeToJava(bg_colour)), bg_colour_start, c - removed_chars, 0);
                    }
                    fg_colour = -1;
                    bg_colour = -1;
                    state_bold_start = -1;
                    state_italics_start = -1;
                    state_underline_start = -1;
                    removed_chars++;
                    break;
                case Colours.COLOUR:
                    colour_state = COLOUR_STATE_FG;
                    colour_state_updated = 0;
                    char peek = text.charAt(c + 1);
                    if(fg_colour != -1){
                        s.setSpan(new ForegroundColorSpan(ircColourCodeToJava(fg_colour)), fg_colour_start, c - removed_chars, 0);
                        if(!Character.isDigit(peek) && peek != ','){
                            fg_colour = -1;
                        }
                    }
                    if(bg_colour != -1){
                        s.setSpan(new BackgroundColorSpan(ircColourCodeToJava(bg_colour)), bg_colour_start, c - removed_chars, 0);
                        if(!Character.isDigit(peek) && peek != ','){
                            bg_colour = -1;
                        }
                    }
                    removed_chars++;
                    break;
                default:
                    boolean isDigit = Character.isDigit(ch);
                    if(colour_state == COLOUR_STATE_FG){
                        if(isDigit){
                            //Read Number
                            if(colour_state_updated == COLOUR_STATE_FG){
                                colour_state = COLOUR_STATE_COMMA;
                                fg_colour = Integer.parseInt(String.valueOf(fg_colour) + String.valueOf(ch));
                            } else {
                                fg_colour = Integer.parseInt(String.valueOf(ch));
                            }
                            colour_state_updated = COLOUR_STATE_FG;
                            fg_colour_start = c - removed_chars;
                            removed_chars++;
                        } else if(ch == ',') {
                            colour_state = COLOUR_STATE_BG;
                            removed_chars++;
                        } else {
                            colour_state = COLOUR_STATE_NONE;
                        }
                    } else if(colour_state == COLOUR_STATE_BG){
                        if(isDigit){
                            //Read Number
                            if(colour_state_updated == COLOUR_STATE_BG){
                                colour_state = COLOUR_STATE_NONE;
                                bg_colour = Integer.parseInt(String.valueOf(bg_colour) + String.valueOf(ch));
                            } else {
                                bg_colour = Integer.parseInt(String.valueOf(ch));
                            }
                            colour_state_updated = COLOUR_STATE_BG;
                            bg_colour_start = c - removed_chars;
                            removed_chars++;

                        } else {
                            colour_state = COLOUR_STATE_NONE;
                        }
                    } else if(colour_state == COLOUR_STATE_COMMA){
                        if(ch == ','){
                            removed_chars++;
                            colour_state = COLOUR_STATE_BG;
                        } else {
                            colour_state = COLOUR_STATE_NONE;
                        }
                    }
            }

        }
        return s;
    }

    public static String cleanIRCText(String text){
        text = text.replaceAll("([\u0003](\\d{1,2})?([,](\\d{1,2}))?)", "");
        return text.replace(String.valueOf(Colours.ITALICS),"").replace(String.valueOf(Colours.BOLD), "").replace(String.valueOf(Colours.UNDERLINE), "").replace(String.valueOf(Colours.RESET), "");
    }

    public static int ircColourCodeToJava(int colour){
        switch (colour){
            case 0:
                return Color.rgb(255,255,255);
            case 1:
                return Color.rgb(0,0,0);
            case 2:
                return Color.rgb(0,0,127);
            case 3:
                return Color.rgb(0,147,0);
            case 4:
                return Color.rgb(255,0,0);
            case 5:
                return Color.rgb(127,0,0);
            case 6:
                return Color.rgb(156,0,156);
            case 7:
                return Color.rgb(252,127,0);
            case 8:
                return Color.rgb(255,255,0);
            case 9:
                return Color.rgb(0,252,0);
            case 10:
                return Color.rgb(0,147,147);
            case 11:
                return Color.rgb(0,255,255);
            case 12:
                return Color.rgb(0,0,252);
            case 13:
                return Color.rgb(255,0,255);
            case 14:
                return Color.rgb(127,127,127);
            case 15:
                return Color.rgb(210,210,210);
            default:
                return Color.rgb(0,0,0);
        }
    }
}
