package com.teardesign.awear;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HomeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.fragment_home, container, false);

        TextView tv = (TextView) v.findViewById(R.id.userName);
        tv.setText(ParseUser.getCurrentUser().getUsername());

        final int[] todayAmounts = {0,0};
        final ParseObject[] todayExpensiveLocation = {null};

        ParseQuery<ParseObject> query = ParseQuery.getQuery("ExpenseHistory");
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> scoreList, com.parse.ParseException e) {
                if (e == null) {

                    for (ParseObject sl : scoreList) {
                        //LatLng ll = new LatLng(sl.getDouble("lat"), sl.getDouble("lng"));

                        Calendar c = Calendar.getInstance();
                        Calendar d = Calendar.getInstance();
                        d.setTime(sl.getCreatedAt());

                        if (c.get(Calendar.ERA) == d.get(Calendar.ERA) &&
                            c.get(Calendar.YEAR) == d.get(Calendar.YEAR) &&
                            c.get(Calendar.DAY_OF_YEAR) == d.get(Calendar.DAY_OF_YEAR)) {
                            todayAmounts[0] += sl.getInt("amount");

                            if (sl.getInt("amount") > todayAmounts[1]) {
                                todayAmounts[1] = sl.getInt("amount");
                                todayExpensiveLocation[0] = sl;
                            }
                        }
                    }

                    //TextView tv = (TextView) v.findViewById(R.id.todayAmount);
                    //TextView tvLabel = (TextView) v.findViewById(R.id.todayExpensiveLabel);
                    //tv.setText("$ "+String.valueOf(todayAmounts[0]));

                    TextView tvLocation = (TextView) v.findViewById(R.id.todayExpensive);
                    if (todayExpensiveLocation[0] != null) {
                        //+ String.valueOf(todayExpensiveLocation[0].getInt("amount"))
                        tvLocation.setText(todayExpensiveLocation[0].getString("venue"));
                    } else {
                        tvLocation.setVisibility(View.GONE);
                    }

                    final DonutProgressAwear dp = (DonutProgressAwear) v.findViewById(R.id.today_expenses);
                    final int amountBar = 5 * todayAmounts[0];

                    final Timer timer = new Timer();

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {

                                try {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (dp.getProgress() < amountBar) {
                                                dp.setProgress(dp.getProgress() + 1);
                                            } else {
                                                timer.cancel();
                                            }
                                        }
                                    });
                                } catch (RuntimeException re) {
                                    Log.d("INIT", "Couldnt init the wheel!");
                                    timer.cancel();
                                }
                            }
                        }, 1500, 20);


                    Log.d("score", "Retrieved " + scoreList.size() + " scores");
                } else {
                    Log.d("score", "Error: " + e.getMessage());
                }
            }
        });

        return v;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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
        public void onFragmentInteraction(Uri uri);
    }

}
