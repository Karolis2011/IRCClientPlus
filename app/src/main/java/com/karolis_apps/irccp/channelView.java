package com.karolis_apps.irccp;

import android.app.ActionBar;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcelable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import layout.channelBufferFragment;
import com.karolis_apps.irccp.core.ClientManager;
import com.karolis_apps.irccp.core.IRC.Data.NetworkDetails;
import com.karolis_apps.irccp.core.IRC.Data.ServerDetails;
import com.karolis_apps.irccp.core.IRC.Data.UserDetails;
import com.karolis_apps.irccp.core.IRC.ManagedIRCClient;
import com.karolis_apps.irccp.core.IRC.utils.BufferUpdateRunnable;
import com.karolis_apps.irccp.exceptions.GeneralException;

public class channelView extends AppCompatActivity {
    private ViewPager channelPager;
    private ChannelPagerAdapter channelPagerAdapter;
    private ManagedIRCClient ircClient;
    private TabLayout channelPagerTabLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_view);

        channelPager = (ViewPager) findViewById(R.id.channelPager);
        channelPagerAdapter = new ChannelPagerAdapter(getSupportFragmentManager(), channelPager);
        channelPager.setAdapter(channelPagerAdapter);

        channelPagerTabLayout = (TabLayout) findViewById(R.id.channelPagerTabLayout);
        channelPagerTabLayout.setupWithViewPager(channelPager);

        ircClient = ClientManager.getInstance().GetClientByName("Main");
        if (ircClient == null) {
            //Let's create new client
            SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            NetworkDetails networkDetails = new NetworkDetails();
            String default_username = getResources().getString(R.string.default_username);
            String default_nickname = getResources().getString(R.string.default_nickname);
            String default_realname = getResources().getString(R.string.default_realname);
            networkDetails.userDetails = new UserDetails(
                    sharedPref.getString(getString(R.string.nickname), default_nickname),
                    sharedPref.getString(getString(R.string.username), default_username),
                    sharedPref.getString(getString(R.string.realname), default_realname)
            );
            String default_server = getResources().getString(R.string.default_server);
            int default_port = getResources().getInteger(R.integer.default_port);
            boolean default_ssl = getResources().getBoolean(R.bool.default_ssl);
            networkDetails.serverDetailsList.add(new ServerDetails(
                    "Server",
                    sharedPref.getString(getString(R.string.server), default_server),
                    sharedPref.getInt(getString(R.string.pport), default_port),
                    sharedPref.getBoolean(getString(R.string.ssl), default_ssl)
            ));
            try{
                ircClient = ClientManager.getInstance().NewClient("Main", networkDetails);
            } catch (Exception E){
                //This should never happen
            }
            try {
                ircClient.Connect();
            } catch (GeneralException e) {
                e.printStackTrace();
            }
        } else {
            ircClient.ClearHandles();
            List<String> channels = ircClient.GetAvailableChannels();
            Collections.sort(channels, new Comparator<String>() {
                @Override
                public int compare(String lhs, String rhs) {
                    return lhs.compareToIgnoreCase(rhs);
                }
            });
            for(String name: channels){
                channelPagerAdapter.addChannelBuffer(ircClient, name);
            }
        }
        ircClient.AddNewBufferCallback(new BufferUpdateRunnable() {
            @Override
            public void run(String bufferName) {
                channelPagerAdapter.addChannelBuffer(ircClient, bufferName);
            }
        });

        /*EditText inputText = (EditText) findViewById(R.id.inputBox);
        //Pre hide text box

        //Let's check if there is already a client
        ManagedIRCClient ircClient = ClientManager.getInstance().GetClientByName("Main");
        if (ircClient == null) {
            //Let's create new client
            ircClient = ClientManager.getInstance().CreateTestClient("Main");
            try {
                ircClient.Connect();
            } catch (GeneralException e) {
                e.printStackTrace();
            }

        } else {
            try {
                TextView v = (TextView) findViewById(R.id.chanOutput);
                Spanned sp = Html.fromHtml(ircClient.ChannelBuffers.get("General"));
                v.setText(sp);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }

        // Handling "Send" action
        inputText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean h = false;
                final String vv = v.getText().toString();
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            ClientManager.getInstance().GetClientByName("Main").unmanagedIRCCLient.PhraseCommand(vv + "\r\n");
                        }
                    });
                    t.start();
                    v.setText("");
                    h = true;
                }
                return h;
            }
        });
        ircClient.ClearHandles(); //To make sure we are only ones subscribed to this client
        ircClient.AddUpdateHandle(new Runnable() {
            @Override
            public void run() {
                //General
                ManagedIRCClient client = ClientManager.getInstance().GetClientByName("Main");
                TextView v = (TextView) findViewById(R.id.chanOutput);
                Spanned sp = Html.fromHtml(client.ChannelBuffers.get("General"));
                v.setText(sp);
            }
        });

        ScrollViewExt scrollv = (ScrollViewExt) findViewById(R.id.outputScroller);
        scrollv.setScrollViewListener(new ScrollViewListener() {
            @Override
            public void onScrollChanged(ScrollViewExt scrollView, int x, int y, int oldx, int oldy) {
                View view = scrollView.getChildAt(scrollView.getChildCount() - 1);
                int diff = (view.getBottom() - (scrollView.getHeight() + scrollView.getScrollY()));
                int diffy = y - oldy;
                if (diff <= 15 && isHiddenTextBox) {
                    showTextBox();
                    isHiddenTextBox = false;
                    enabledTextHiding = false;
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            enabledTextHiding = true;
                        }
                    }, 1000);
                } else {
                    if (diffy < -10 && enabledTextHiding && !isHiddenTextBox) {
                        hideTextBox();
                        isHiddenTextBox = true;
                    }
                }
            }
        });*/
    }

    public void Send_FAB(View v){
        channelBufferFragment currentBuffer = channelPagerAdapter.getItemOriginal(channelPager.getCurrentItem());
        if(currentBuffer != null){
            currentBuffer.sendButtonPress();
        }
    }

    private class ChannelPagerAdapter extends FragmentStatePagerAdapter {
        private final ArrayList<channelBufferFragment> fragments = new ArrayList<>();
        private final ViewPager myViewpager;
        public ChannelPagerAdapter(FragmentManager fm, ViewPager viewPager){
            super(fm);
            myViewpager = viewPager;
        }

        public void addChannelBuffer(ManagedIRCClient ircClient, String bufferName){
            channelBufferFragment bufferFragment = new channelBufferFragment();
            if(bufferFragment.init(ircClient, bufferName)){
                fragments.add(bufferFragment);
                //Sort();
            }
            notifyDataSetChanged();
        }

        private void Sort(){
            Collections.sort(fragments, new Comparator<channelBufferFragment>() {
                @Override
                public int compare(channelBufferFragment lhs, channelBufferFragment rhs) {
                    return lhs.getBufferName().compareToIgnoreCase(rhs.getBufferName());
                }
            });
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            return fragments.get(position);
        }

        public channelBufferFragment getItemOriginal(int position) {
            return fragments.get(position);
        }

        public void removeBuffer(ViewPager myPager, String bufferName) {
            //myPager.setAdapter(null);
            for(channelBufferFragment cbf : fragments){
                if (cbf.getBufferName().equals(bufferName)){
                    fragments.remove(cbf);
                }
            }
            notifyDataSetChanged();
            //myPager.setAdapter(this);
        }

        public void addHandles(){
            for(channelBufferFragment cbf : fragments){
                cbf.addHandle();
            }
        }


        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String title = fragments.get(position).getBufferName();
            if(title.equals("!General")){
                title = "General";
            }
            return title;
        }
    }
}
