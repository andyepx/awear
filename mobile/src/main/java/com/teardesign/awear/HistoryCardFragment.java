package com.teardesign.awear;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HistoryCardFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HistoryCardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HistoryCardFragment extends Fragment {

    private static int mAmount;
    private static String mLocation;
    private static LatLng mCoordinates;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param amount Parameter 1.
     * @param location Parameter 2.
     * @param coordinates Parameter 2.
     * @return A new instance of fragment HistoryCardFragment.
     */
    public static HistoryCardFragment newInstance(int amount, String location, LatLng coordinates) {
        HistoryCardFragment fragment = new HistoryCardFragment();

        mAmount = amount;
        mLocation = location;
        mCoordinates = coordinates;

        return fragment;
    }

    public HistoryCardFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView amountHistory = (TextView) getView().findViewById(R.id.history_count_value);
        amountHistory.setText("$"+Integer.toString(mAmount)+" at ");

        TextView locationHistory = (TextView) getView().findViewById(R.id.history_location_value);
        locationHistory.setText(mLocation);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_history_card, container, false);
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
