package com.karolis_apps.irccp.CustomViews;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.karolis_apps.irccp.R;

public class IRCConnectionView extends ConstraintLayout {

    private String config_id;

    public IRCConnectionView(Context context) {
        super(context);
        LayoutInflater mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mInflater.inflate(R.layout.view_irc_connection, this, true);
    }

    public IRCConnectionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mInflater.inflate(R.layout.view_irc_connection, this, true);
    }

    public IRCConnectionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mInflater.inflate(R.layout.view_irc_connection, this, true);
    }

    public void setClientConfigID(String id){
        this.config_id = id;
        final TextView desc = (TextView) findViewById(R.id.connectionDescriptionText);
        final TextView title = (TextView) findViewById(R.id.connectionNameText);

    }

}
