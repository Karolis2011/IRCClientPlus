package tk.kar_programing.ircclient;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import layout.channel_buffer;
import tk.kar_programing.ircclient.CustomViews.Interfaces.ScrollViewListener;
import tk.kar_programing.ircclient.CustomViews.ScrollViewExt;
import tk.kar_programing.ircclient.core.ClientManager;
import tk.kar_programing.ircclient.core.IRC.ManagedIRCClient;
import tk.kar_programing.ircclient.exceptions.GeneralException;

public class channelView extends AppCompatActivity {
    private ViewPager channelPager;
    private PagerAdapter channelPagerAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_view);

        channelPager = (ViewPager) findViewById(R.id.channelPager);
        channelPagerAdapter = new ChannelPagerAdapter(getSupportFragmentManager());
        channelPager.setAdapter(channelPagerAdapter);
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


    private class ChannelPagerAdapter extends FragmentStatePagerAdapter {
        public ChannelPagerAdapter(FragmentManager fm){
            super(fm);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            return new channel_buffer();
        }

        @Override
        public int getCount() {
            return 3;
        }
    }
}
