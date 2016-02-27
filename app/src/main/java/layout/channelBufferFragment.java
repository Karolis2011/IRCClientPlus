package layout;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.os.Looper;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import com.karolis_apps.irccp.CustomViews.Interfaces.ScrollViewListener;
import com.karolis_apps.irccp.CustomViews.ScrollViewExt;
import com.karolis_apps.irccp.R;
import com.karolis_apps.irccp.core.ClientManager;
import com.karolis_apps.irccp.core.IRC.ManagedIRCClient;
import com.karolis_apps.irccp.core.IRC.utils.BufferUpdateRunnable;

/**
 * A simple {@link Fragment} subclass.
 */
public class channelBufferFragment extends Fragment {
    private boolean enabledTextHiding = true;
    private final Timer timer = new Timer();
    private final Timer generalTimer = new Timer();
    private boolean isHiddenTextBox = false;
    private String myClientName;
    private String myBuffer;
    private View rootView;

    public channelBufferFragment() {
        // Required empty public constructor
    }

    public String getBufferName(){
        return  myBuffer;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("IRCCName", myClientName);
        outState.putString("Buffer", myBuffer);
        super.onSaveInstanceState(outState);
    }

    public boolean init(ManagedIRCClient ircClient, String bufferName){
        myClientName = ircClient.name;
        myBuffer = bufferName;
        ManagedIRCClient ircClient1 = ClientManager.getInstance().GetClientByName(myClientName);
        return ircClient1.GetAvailableChannels().contains(bufferName);
    }

    public void addHandle(){
        ClientManager.getInstance().GetClientByName(myClientName).AddUpdateHandle(new BufferUpdateRunnable() {
            @Override
            public void run(String bufferName) {
                if (bufferName.equals(myBuffer)) {
                    //Update buffer
                    updateBuffer();
                }
            }
        }); //Let's listen when our buffer is being updated
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_channel_buffer, container, false);

        ScrollViewExt scrollv = (ScrollViewExt) rootView.findViewById(R.id.outputScroller);
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
        EditText inputText = (EditText) rootView.findViewById(R.id.inputBox);
        inputText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean h = false;
                final String vv = v.getText().toString();
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            ClientManager.getInstance().GetClientByName(myClientName).PhraseInput(vv + "\r\n", myBuffer);
                        }
                    });
                    t.start();
                    v.setText("");
                    h = true;
                }
                return h;
            }
        });
        if (savedInstanceState != null){
            myClientName = savedInstanceState.getString("IRCCName");
            myBuffer = savedInstanceState.getString("Buffer");
        }
        Log.d("UI", "channelBufferFragment created UI, MagicID: " + myClientName + ":" + myBuffer);
        addHandle();
        updateBuffer();
        return rootView;
    }


    private void updateBuffer(){
        if(rootView == null){
            generalTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    updateBuffer();
                }
            }, 100); //Let's wait when our UI will be created, so we could update it
            return;
        }
        Handler h = new Handler(Looper.getMainLooper());
        h.post(new Runnable() {
            @Override
            public void run() {
                TextView v = (TextView) rootView.findViewById(R.id.chanOutput);
                Spanned sp = Html.fromHtml(ClientManager.getInstance().GetClientByName(myClientName).ChannelBuffers.get(myBuffer));
                v.setText(sp);
            }
        });
    }

    private void hideTextBox() {
        final EditText inputText = (EditText) rootView.findViewById(R.id.inputBox);
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

    private void showTextBox() {
        EditText inputText = (EditText) rootView.findViewById(R.id.inputBox);
        inputText.setVisibility(View.VISIBLE);
        inputText.animate()
                .translationY(0)
                .alpha(1.0f);
    }

}
