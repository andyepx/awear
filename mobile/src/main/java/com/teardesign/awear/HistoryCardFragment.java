package com.teardesign.awear;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HistoryCardFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HistoryCardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HistoryCardFragment extends Fragment implements OnMapReadyCallback {

    private int mAmount;
    private String mLocation;
    private String mDate;
    private LatLng mCoordinates;
    private String mId;

    GoogleMap googleMap;

    private OnFragmentInteractionListener mListener;
    private Activity myContext;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param amount Parameter 1.
     * @param location Parameter 2.
     * @param coordinates Parameter 2.
     * @return A new instance of fragment HistoryCardFragment.
     */
    public HistoryCardFragment newInstance(int amount, String location, String date, LatLng coordinates, String id) {
       // HistoryCardFragment fragment = new HistoryCardFragment();

        mAmount = amount;
        mLocation = location;
        mDate = date;
        mCoordinates = coordinates;
        mId = id;

        return this;
    }

    public HistoryCardFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_history_card, container, false);

        createMapView(v);

        TextView dateHistory = (TextView) v.findViewById(R.id.history_date_value);
        dateHistory.setText(mDate);

        TextView amountHistory = (TextView) v.findViewById(R.id.history_count_value);
        amountHistory.setText("$"+Integer.toString(mAmount)+" at ");

        TextView locationHistory = (TextView) v.findViewById(R.id.history_location_value);
        String loc = mLocation;
        if (loc.length() > 30) {
            loc = loc.substring(0, 28) + "...";
        }
        locationHistory.setText(loc);

        CardView cv = (CardView) v.findViewById(R.id.card_view);
        cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), ExpenseDetailsActivity.class);
                intent.putExtra("ID", mId);
                startActivity(intent);

                //Toast.makeText(v.getContext(), mLocation, Toast.LENGTH_LONG).show();

            }
        });

        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        myContext = activity;
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        try {
            if(null == googleMap){
                //googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.mapView)).getMap();

                //googleMap = ((MapView) v.findViewById(R.id.mapView)).getMap();
                //googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.mapView)).getMap();

                googleMap.setMyLocationEnabled(true);
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mCoordinates, 13));

                googleMap.addMarker(new MarkerOptions()
                        .title(mLocation)
                        .position(mCoordinates));
            }
        } catch (NullPointerException exception){
            Log.e("mapApp", exception.toString());
        }
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

    /**
     * Initialises the mapview
     */
    private void createMapView(View v) {
        FragmentManager fm = getChildFragmentManager();
        //MapFragment mapFragment = (MapFragment) fm.findFragmentById(R.id.mapView);

//        googleMap = ((MapFragment) fm.findFragmentById(R.id.mapView)).getMap();
//        googleMap.setMyLocationEnabled(true);
//        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mCoordinates, 20));
//
//        googleMap.addMarker(new MarkerOptions()
//                .title(mLocation)
//                .position(mCoordinates));

        //mapFragment.getMapAsync(this);
    }

}
