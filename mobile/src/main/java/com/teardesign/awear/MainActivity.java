package com.teardesign.awear;

import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

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
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.lang.reflect.Array;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import fi.foyt.foursquare.api.*;
import fi.foyt.foursquare.api.entities.Category;
import fi.foyt.foursquare.api.entities.CompactVenue;
import fi.foyt.foursquare.api.entities.VenuesSearchResult;


public class MainActivity extends Activity implements
        DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, Secrets.parseAppID, Secrets.parseAppSecret);

        currentUser = ParseUser.getCurrentUser();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mGoogleApiClient.connect();

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
            //createMapView();
            //addMarker();
        } else {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        }

    }

    @Override
    protected void onResume() {

        super.onResume();

        currentUser = ParseUser.getCurrentUser();
        mGoogleApiClient.connect();

        if (currentUser != null && !running) {
            setContentView(R.layout.activity_main);

            //createMapView();
            //addMarker();
        } else if (!running) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        }

    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.DataApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
    }

    private String findCategoryRoot(String cID, String rootName, Category cc[]) {

        boolean isRoot = false;
        if (cc == null) {
            isRoot = true;
            cc = categories;
        }

        for (Category c : cc) {
            if (isRoot) rootName = c.getName();
            Category children[] = c.getCategories();
            if (children != null && children.length > 0) {
                String cn = findCategoryRoot(cID, rootName, children);
                if (cn.length() > 0) {
                    return cn;
                }
            }
            if (c.getId().compareTo(cID) == 0) {
                return rootName;
            }
        }

        return "";
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // DataItem changed
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().compareTo("/count") == 0) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    count = dataMap.getInt(COUNT_KEY);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView tv = (TextView) findViewById(R.id.count_value);
                            tv.setText("$"+Integer.toString(count)+" at ");
                            final TextView ltv = (TextView) findViewById(R.id.location_value);
                            ltv.setText("Locating...");

                            ll = new LocationListener() {
                                @Override
                                public void onLocationChanged(Location location) {
                                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                    ltv.setText(Double.toString(currentLocation.latitude) + " , " + Double.toString(currentLocation.longitude));

                                    try {

                                        loadedVenues = new FoursquareAPIAccess().execute(currentLocation).get();
                                        CompactVenue currentVenue = null;
                                        String currentVenueName = "";

                                        for (CompactVenue venue : loadedVenues) {
                                            float distance = 0;
                                            //String allowedCategories[] = new String[2];
                                            //allowedCategories[0] = "Food";
                                            //allowedCategories[1] = "Shop & Service";
                                            //Boolean toAdd = false;
                                            //for (Category c : venue.getCategories()) {
                                            //    String root = findCategoryRoot(c.getId(), null, null);
                                            //    toAdd = Arrays.asList(allowedCategories).contains(root);
                                            //}
                                            //if (toAdd) {
                                                float[] results = new float[3];
                                                Location.distanceBetween(currentLocation.latitude, currentLocation.longitude, venue.getLocation().getLat(), venue.getLocation().getLng(), results);
                                                if (distance == 0) {
                                                    distance = results[0];
                                                    currentVenue = venue;
                                                } else if (results[0] < distance) {
                                                    distance = results[0];
                                                    currentVenue = venue;
                                                }
                                            //}
                                        }

                                        if (currentVenue != null) {
                                            ltv.setText(currentVenue.getName());
                                            currentVenueName = currentVenue.getName();
                                        } else {
                                            currentVenueName = "Undefined";
                                        }

                                        ParseObject expenseHistory = new ParseObject("ExpenseHistory");
                                        expenseHistory.put("lat", currentLocation.latitude);
                                        expenseHistory.put("lng", currentLocation.longitude);
                                        expenseHistory.put("user", currentUser);
                                        expenseHistory.put("amount", count);
                                        expenseHistory.put("venue", currentVenueName);
                                        if (currentVenue != null)
                                            expenseHistory.put("FoursquareVenueID", currentVenue.getId());

                                        ParseACL dataPermission = new ParseACL();
                                        dataPermission.setPublicReadAccess(false);
                                        dataPermission.setReadAccess(currentUser, true);
                                        dataPermission.setPublicWriteAccess(false);
                                        dataPermission.setWriteAccess(currentUser, true);

                                        expenseHistory.setACL(dataPermission);
                                        expenseHistory.saveInBackground();

                                        ll = null;

                                        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/count_back");
                                        putDataMapReq.getDataMap().putString(COUNT_KEY, "Done");
                                        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
                                        PendingResult<DataApi.DataItemResult> pendingResult =
                                                Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);

                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    } catch (ExecutionException e) {
                                        e.printStackTrace();
                                    }

                                }

                                @Override
                                public void onStatusChanged(String provider, int status, Bundle extras) {

                                }

                                @Override
                                public void onProviderEnabled(String provider) {

                                }

                                @Override
                                public void onProviderDisabled(String provider) {

                                }
                            };

                            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, ll);
                            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, ll);
                        }
                    });
                }
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                // DataItem deleted
            }
        }
    }

    /**
     * Initialises the mapview
     */
    private void createMapView() {
        /*
        try {
            if(null == googleMap){
                googleMap = ((MapFragment) getFragmentManager().findFragmentById(
                        R.id.mapView)).getMap();

                if(null == googleMap) {
                    Toast.makeText(getApplicationContext(),
                            "Error creating map", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (NullPointerException exception){
            Log.e("mapApp", exception.toString());
        }*/
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

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
