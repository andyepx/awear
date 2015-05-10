package com.teardesign.awear;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.concurrent.ExecutionException;

import fi.foyt.foursquare.api.entities.CompactVenue;


public class ExpenseDetailsActivity extends Activity {

    private String mId = "";
    private GoogleMap googleMap;
    private List<String> venues = new ArrayList<String>();
    private CompactVenue[] loadedVenues;
    private ParseObject currentExpense;
    private Marker mapMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_details);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {

            mId = extras.getString("ID");

            ParseQuery<ParseObject> query = ParseQuery.getQuery("ExpenseHistory");
            query.getInBackground(mId, new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject expense, ParseException e) {
                    if (e == null) {

                        currentExpense = expense;

                        TextView tv = (TextView) findViewById(R.id.expense_amount);
                        tv.setText("$"+expense.getInt("amount"));

                        TextView tv2 = (TextView) findViewById(R.id.expense_location);
                        tv2.setText(expense.getString("venue"));

                        Spinner spinner = (Spinner) findViewById(R.id.editlocation_spinner);

                        TextView tv3 = (TextView) findViewById(R.id.expense_date);
                        tv3.setText(expense.getCreatedAt().toString());

                        Button btn = (Button) findViewById(R.id.delete_expense);
                        btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                currentExpense.deleteInBackground(new DeleteCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        finish();
                                    }
                                });

                            }
                        });

                        FragmentManager fm = getFragmentManager();
                        LatLng ll = new LatLng(expense.getDouble("lat"), expense.getDouble("lng"));

                        try {
                            FoursquareParams p = new FoursquareParams();
                            p.distance = 500;
                            p.location = ll;
                            loadedVenues = new FoursquareAPIAccess().execute(p).get();

                            MyAdapter adapter = new MyAdapter(getApplicationContext(), R.layout.spinner_item_edit_location, loadedVenues);
                            spinner.setAdapter(adapter);
                            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                                    CompactVenue selectedVenue = loadedVenues[position];

                                    currentExpense.put("FoursquareVenueID", selectedVenue.getId());
                                    currentExpense.put("venue", selectedVenue.getName());
                                    currentExpense.put("lat", selectedVenue.getLocation().getLat());
                                    currentExpense.put("lng", selectedVenue.getLocation().getLng());
                                    currentExpense.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            if (e == null) {
                                                TextView tv2 = (TextView) findViewById(R.id.expense_location);
                                                tv2.setText(currentExpense.getString("venue"));

                                                mapMarker.setPosition(new LatLng(currentExpense.getDouble("lat"), currentExpense.getDouble("lng")));
                                                mapMarker.setTitle(currentExpense.getString("venue"));

                                            }
                                        }
                                    });

                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {

                                }
                            });

                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        } catch (ExecutionException e1) {
                            e1.printStackTrace();
                        }

                        googleMap = ((MapFragment) fm.findFragmentById(R.id.mapView)).getMap();
                        googleMap.setMyLocationEnabled(true);
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ll, 15));

                        mapMarker = googleMap.addMarker(new MarkerOptions()
                                .title(expense.getString("venue"))
                                .position(ll));


                        Log.d("score", "Retrieved expense.");
                    } else {
                        Log.d("score", "Error: " + e.getMessage());
                    }
                }
            });

        }
    }

    private void updateVenue(CompactVenue selectedVenue) {

    }

    public class MyAdapter extends ArrayAdapter<CompactVenue>{

        CompactVenue[] venues;

        public MyAdapter(Context context, int textViewResourceId, CompactVenue[] objects) {
            super(context, textViewResourceId, objects);
            venues = objects;
        }

        @Override
        public View getDropDownView(int position, View convertView,ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        public View getCustomView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater=getLayoutInflater();
            View row = inflater.inflate(R.layout.spinner_item_edit_location, parent, false);

            TextView label=(TextView)row.findViewById(R.id.venue_name);
            label.setText(venues[position].getName());

            return row;
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_expense_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
