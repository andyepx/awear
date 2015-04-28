package com.teardesign.awear;

import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.lang.reflect.Array;
import java.net.URL;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import fi.foyt.foursquare.api.*;
import fi.foyt.foursquare.api.entities.Category;
import fi.foyt.foursquare.api.entities.CompactVenue;
import fi.foyt.foursquare.api.entities.VenuesSearchResult;


public class MainActivity extends Activity implements
        DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        HistoryCardFragment.OnFragmentInteractionListener,
        MainFragment.OnFragmentInteractionListener,
        HomeFragment.OnFragmentInteractionListener,
        SettingsFragment.OnFragmentInteractionListener,
        HistoryCardLabelFragment.OnFragmentInteractionListener {

    private String[] mTabsTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;

    GoogleMap googleMap;
    Marker m;
    CompactVenue[] loadedVenues = null;
    ParseUser currentUser = null;
    Boolean running = false;

    private GoogleApiClient mGoogleApiClient;
    private int count = 0;
    private static final String COUNT_KEY = "com.teardesign.awear.count";
    private Category categories[];
    private LocationListener ll;
    private CharSequence mTitle;
    private ActionBarDrawerToggle mDrawerToggle;
    private int mCurrentPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent i = new Intent(this, MyService.class);
        startService(i);

        setup();

//        if (savedInstanceState != null)
//            mCurrentPosition = (int) savedInstanceState.getSerializable("currentPosition");

        Log.d("STATE!", "Create..." + String.valueOf(mCurrentPosition));

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (mDrawerToggle != null) {
            // Sync the toggle state after onRestoreInstanceState has occurred.
            mDrawerToggle.syncState();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        //boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        //menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    /** Swaps fragments in the main content view */
    private void selectItem(int position) {

        if (position == 0) {
            Fragment fragment = new HomeFragment();
            Bundle args = new Bundle();

            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, fragment)
                    .commit();
        } else if (position == 1) {
            Fragment fragment = new MainFragment();
            Bundle args = new Bundle();

            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, fragment)
                    .commit();
        } else if (position == 2) {
            Fragment fragment = new SettingsFragment();
            Bundle args = new Bundle();

            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, fragment)
                    .commit();
        }

        // Highlight the selected item, update the title, and close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(mTabsTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);

        mCurrentPosition = position;
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }


    @Override
    protected void onResume() {

        super.onResume();

        Log.d("STATE!", "Resume...");

//        selectItem(mCurrentPosition);

    }

    @Override
    public void onPostCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);
        mDrawerToggle.syncState();
    }

    private void setup() {

        currentUser = ParseUser.getCurrentUser();

        try {
            categories = new FoursquareCategoriesList().execute(0).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        if (currentUser != null) {
            running = true;
            setContentView(R.layout.activity_main);

            mTabsTitles = getResources().getStringArray(R.array.tabs_array);
            mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            mDrawerList = (ListView) findViewById(R.id.left_drawer);

            // Set the adapter for the list view
            mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                    R.layout.drawer_list_item, mTabsTitles));
            // Set the list's click listener
            mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

            mTitle = getTitle();
            mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            mDrawerToggle = new ActionBarDrawerToggle(
                    this,                  /* host Activity */
                    mDrawerLayout,         /* DrawerLayout object */
                    R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
                    R.string.drawer_close  /* "close drawer" description */
            ) {

                /** Called when a drawer has settled in a completely open state. */
                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);
                    getActionBar().setTitle(mTitle);
                }
            };

            // Set the drawer toggle as the DrawerListener
            mDrawerLayout.setDrawerListener(mDrawerToggle);

            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setHomeButtonEnabled(true);

            // Set the drawer toggle as the DrawerListener
            mDrawerLayout.setDrawerListener(mDrawerToggle);

            Fragment fragment = new HomeFragment();
            Bundle args = new Bundle();

            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, fragment)
                    .commit();


        } else {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        }

    }

    @Override
    public void onConnected(Bundle bundle) {

        //Wearable.DataApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    protected void onPause() {
        super.onPause();

//        Wearable.DataApi.removeListener(mGoogleApiClient, this);
//        mGoogleApiClient.disconnect();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
//        for (DataEvent event : dataEvents) {
//            if (event.getType() == DataEvent.TYPE_CHANGED) {
//                // DataItem changed
//                DataItem item = event.getDataItem();
//                if (item.getUri().getPath().compareTo("/count") == 0) {
//                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
//                    count = dataMap.getInt(COUNT_KEY);
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            TextView tv = (TextView) findViewById(R.id.count_value);
//                            tv.setText("$"+Integer.toString(count)+" at ");
//                            final TextView ltv = (TextView) findViewById(R.id.location_value);
//                            ltv.setText("Locating...");
//
//                            ll = new LocationListener() {
//                                @Override
//                                public void onLocationChanged(Location location) {
//                                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
//                                    ltv.setText(Double.toString(currentLocation.latitude) + " , " + Double.toString(currentLocation.longitude));
//
//                                    try {
//
//                                        loadedVenues = new FoursquareAPIAccess().execute(currentLocation).get();
//                                        CompactVenue currentVenue = null;
//                                        String currentVenueName = "";
//
//                                        for (CompactVenue venue : loadedVenues) {
//                                            float distance = 0;
//                                            float[] results = new float[3];
//                                            Location.distanceBetween(currentLocation.latitude, currentLocation.longitude, venue.getLocation().getLat(), venue.getLocation().getLng(), results);
//                                            if (distance == 0) {
//                                                distance = results[0];
//                                                currentVenue = venue;
//                                            } else if (results[0] < distance) {
//                                                distance = results[0];
//                                                currentVenue = venue;
//                                            }
//                                        }
//
//                                        if (currentVenue != null) {
//                                            ltv.setText(currentVenue.getName());
//                                            currentVenueName = currentVenue.getName();
//                                        } else {
//                                            currentVenueName = "Undefined";
//                                        }
//
//                                        ParseObject expenseHistory = new ParseObject("ExpenseHistory");
//                                        expenseHistory.put("lat", currentLocation.latitude);
//                                        expenseHistory.put("lng", currentLocation.longitude);
//                                        expenseHistory.put("user", currentUser);
//                                        expenseHistory.put("amount", count);
//                                        expenseHistory.put("venue", currentVenueName);
//                                        if (currentVenue != null)
//                                            expenseHistory.put("FoursquareVenueID", currentVenue.getId());
//
//                                        ParseACL dataPermission = new ParseACL();
//                                        dataPermission.setPublicReadAccess(false);
//                                        dataPermission.setReadAccess(currentUser, true);
//                                        dataPermission.setPublicWriteAccess(false);
//                                        dataPermission.setWriteAccess(currentUser, true);
//
//                                        expenseHistory.setACL(dataPermission);
//                                        expenseHistory.saveInBackground();
//
//                                        FragmentTransaction ft = getFragmentManager().beginTransaction();
//                                        HistoryCardFragment historyCard = HistoryCardFragment.newInstance(count, currentVenueName, currentLocation);
//                                        ft.add(R.id.scroll_main_container, historyCard).commit();
//
//                                        ll = null;
//
//                                        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/count_back");
//                                        putDataMapReq.getDataMap().putString(COUNT_KEY, "Done");
//                                        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
//                                        PendingResult<DataApi.DataItemResult> pendingResult =
//                                                Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
//
//                                    } catch (InterruptedException e) {
//                                        e.printStackTrace();
//                                    } catch (ExecutionException e) {
//                                        e.printStackTrace();
//                                    }
//
//                                }
//
//                                @Override
//                                public void onStatusChanged(String provider, int status, Bundle extras) {
//
//                                }
//
//                                @Override
//                                public void onProviderEnabled(String provider) {
//
//                                }
//
//                                @Override
//                                public void onProviderDisabled(String provider) {
//
//                                }
//                            };
//
//                            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, ll);
//                            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, ll);
//                        }
//                    });
//                }
//            } else if (event.getType() == DataEvent.TYPE_DELETED) {
//                // DataItem deleted
//            }
//        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    private class FoursquareCategoriesList extends AsyncTask<Integer, Integer, Category[]> {
        protected Category[] doInBackground(Integer... currentLocation) {

            try {
                return listCategories();
            } catch (FoursquareApiException e) {
                e.printStackTrace();
            }

            return null;
        }

        public Category[] listCategories() throws FoursquareApiException {

            // First we need a initialize FoursquareApi.
            FoursquareApi foursquareApi = new FoursquareApi(Secrets.fourSquareClientID, Secrets.fourSquareClientSecret, "");

            // After client has been initialized we can make queries.
            Result<Category[]> result = foursquareApi.venuesCategories();

            if (result.getMeta().getCode() == 200) {
                return result.getResult();
            } else {
                System.out.println("Error occured: ");
                System.out.println("  code: " + result.getMeta().getCode());
                System.out.println("  type: " + result.getMeta().getErrorType());
                System.out.println("  detail: " + result.getMeta().getErrorDetail());
            }

            return null;
        }
    }

    private class FoursquareAPIAccess extends AsyncTask<LatLng, Integer, CompactVenue[]> {
        protected CompactVenue[] doInBackground(LatLng... currentLocation) {

            try {
                return searchVenues(Double.toString(currentLocation[0].latitude) + "," + Double.toString(currentLocation[0].longitude));
            } catch (FoursquareApiException e) {
                e.printStackTrace();
            }

            return null;
        }

        public CompactVenue[] searchVenues(String ll) throws FoursquareApiException {

            // First we need a initialize FoursquareApi.
            FoursquareApi foursquareApi = new FoursquareApi(Secrets.fourSquareClientID, Secrets.fourSquareClientSecret, "");

            // After client has been initialized we can make queries.
            Result<VenuesSearchResult> result = foursquareApi.venuesSearch(ll, null, null, null, null, null, "browse", "4d4b7105d754a06374d81259,4d4b7105d754a06378d81259", null, null, null, 80, null);

            if (result.getMeta().getCode() == 200) {
                return result.getResult().getVenues();
            } else {
                System.out.println("Error occured: ");
                System.out.println("  code: " + result.getMeta().getCode());
                System.out.println("  type: " + result.getMeta().getErrorType());
                System.out.println("  detail: " + result.getMeta().getErrorDetail());
            }

            return null;
        }
    }
}
