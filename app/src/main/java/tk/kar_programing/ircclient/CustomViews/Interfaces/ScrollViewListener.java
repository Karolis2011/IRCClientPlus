package tk.kar_programing.ircclient.CustomViews.Interfaces;

import tk.kar_programing.ircclient.CustomViews.ScrollViewExt;

public interface ScrollViewListener {
    void onScrollChanged(ScrollViewExt scrollView, int x, int y, int oldx, int oldy);
}
