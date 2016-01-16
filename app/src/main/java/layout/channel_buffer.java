package layout;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.util.Timer;
import java.util.TimerTask;

import tk.kar_programing.ircclient.CustomViews.Interfaces.ScrollViewListener;
import tk.kar_programing.ircclient.CustomViews.ScrollViewExt;
import tk.kar_programing.ircclient.R;
import tk.kar_programing.ircclient.core.IRC.ManagedIRCClient;

/**
 * A simple {@link Fragment} subclass.
 */
public class channel_buffer extends Fragment {
    private boolean enabledTextHiding = true;
    private Timer timer = new Timer();
    private boolean isHiddenTextBox = false;
    private ManagedIRCClient myIRCClient;

    public channel_buffer() {
        // Required empty public constructor
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_channel_buffer, container, false);

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
        return rootView;
    }

    private void hideTextBox() {
        final EditText inputText = (EditText) getView().findViewById(R.id.inputBox);
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
        EditText inputText = (EditText) getView().findViewById(R.id.inputBox);
        inputText.setVisibility(View.VISIBLE);
        inputText.animate()
                .translationY(0)
                .alpha(1.0f);
    }

}
