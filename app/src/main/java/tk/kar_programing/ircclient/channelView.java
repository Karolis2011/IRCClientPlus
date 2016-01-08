package tk.kar_programing.ircclient;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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

import tk.kar_programing.ircclient.CustomViews.Interfaces.ScrollViewListener;
import tk.kar_programing.ircclient.CustomViews.ScrollViewExt;
import tk.kar_programing.ircclient.core.ClientManager;
import tk.kar_programing.ircclient.core.IRC.ManagedIRCClient;
import tk.kar_programing.ircclient.exceptions.GeneralException;

public class channelView extends AppCompatActivity {
    private Timer timer = new Timer();
    private boolean enabledTextHiding = true;
    private boolean isHiddenTextBox = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_view);
        EditText inputText = (EditText) findViewById(R.id.inputBox);
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
        });
    }

    protected void hideTextBox() {
        final EditText inputText = (EditText) findViewById(R.id.inputBox);
//        inputText.setVisibility(View.GONE);
        inputText.animate()
                .translationY(inputText.getHeight())
                .alpha(0.0f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        if(isHiddenTextBox){
                            inputText.setVisibility(View.GONE);
                        }
                    }
                });
    }

    protected void showTextBox() {
        EditText inputText = (EditText) findViewById(R.id.inputBox);
        inputText.setVisibility(View.VISIBLE);
        inputText.animate()
                .translationY(0)
                .alpha(1.0f);
    }

}
