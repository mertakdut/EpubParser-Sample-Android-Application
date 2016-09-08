package com.github.epubparsersampleandroidapplication;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

/**
 * Created by Mert on 08.09.2016.
 */
public class PageFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_TAB_POSITON = "tab_position";

    private OnFragmentReadyListener onFragmentReadyListener;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static PageFragment newInstance(int tabPosition) {
        PageFragment fragment = new PageFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TAB_POSITON, tabPosition);
        fragment.setArguments(args);
        return fragment;
    }

    public PageFragment() {
    }

    public interface OnFragmentReadyListener {
        View onFragmentReady(int position);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        onFragmentReadyListener = (OnFragmentReadyListener) context; //Activity version is deprecated.
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        onFragmentReadyListener = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_display, container, false);

        RelativeLayout mainLayout = (RelativeLayout) rootView.findViewById(R.id.fragment_main_layout);

        View view = onFragmentReadyListener.onFragmentReady(getArguments().getInt(ARG_TAB_POSITON));

        if (view != null) {
            mainLayout.addView(view);
        }

        return rootView;
    }
}
