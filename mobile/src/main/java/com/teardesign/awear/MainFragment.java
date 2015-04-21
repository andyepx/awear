package com.teardesign.awear;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.maps.model.LatLng;
import com.parse.FindCallback;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MainFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    public static MainFragment newInstance(String param1, String param2) {
        MainFragment fragment = new MainFragment();
        return fragment;
    }

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main, container, false);
        createHistoryCards(v);

        //Tracker t = ((AwearApp) getActivity().getApplication()).getTracker(AwearApp.TrackerName.APP_TRACKER);

        //t.setScreenName("com.teardesign.awear.HomeFragment");
        //t.send(new HitBuilders.ScreenViewBuilder().build());

        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    private void createHistoryCards(View v) {

        ParseQuery<ParseObject> query = ParseQuery.getQuery("ExpenseHistory");
        query.orderByDescending("createdAt");
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> expensesList, com.parse.ParseException e) {
                if (e == null) {

                    String text = "";
                    String previousText = "-";
                    Calendar c = Calendar.getInstance();
                    c.add(Calendar.DAY_OF_YEAR, -1); // yesterday

                    int a = 0;
                    for (ParseObject sl : expensesList) {

                        Calendar c2 = Calendar.getInstance();
                        c2.setTime(sl.getCreatedAt()); // your date

                        if (DateUtils.isToday(sl.getCreatedAt().getTime())) {
                            text = "Today";
                        } else if (c.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
                                && c.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)) {
                            text = "Yesterday";
                        } else {
                            text = DateFormat.format("dd MMMM yyyy", sl.getCreatedAt()).toString();
                        }

                        TextView tv = new TextView(getActivity());
                        tv.setText(text);

                        if (previousText.compareTo(text) != 0) {
                            previousText = text;
                            HistoryCardLabelFragment hclf = new HistoryCardLabelFragment().newInstance(text);
                            getChildFragmentManager().beginTransaction().add(
                                    R.id.scroll_main_container,
                                    hclf,
                                    "HistoryLabel_"+String.valueOf(a)).commit();
                        }

                        LatLng ll = new LatLng(sl.getDouble("lat"), sl.getDouble("lng"));

                        HistoryCardFragment hcf = new HistoryCardFragment().newInstance(sl.getInt("amount"), sl.getString("venue"), DateFormat.format("dd/MM/yyyy HH:mm", sl.getCreatedAt()).toString(), ll, sl.getObjectId());
                        getChildFragmentManager().beginTransaction().add(
                                R.id.scroll_main_container,
                                hcf,
                                "HistoryFragment_"+String.valueOf(a)).commit();

                    }

                    Log.d("score", "Retrieved " + expensesList.size() + " records");
                } else {
                    Log.d("score", "Error: " + e.getMessage());
                }
            }
        });

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
