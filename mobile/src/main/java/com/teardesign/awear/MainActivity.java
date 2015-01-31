package com.teardesign.awear;

import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.view.Window;
import android.widget.Toast;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.net.URL;
import java.util.concurrent.ExecutionException;

import fi.foyt.foursquare.api.*;
import fi.foyt.foursquare.api.entities.Category;
import fi.foyt.foursquare.api.entities.CompactVenue;
import fi.foyt.foursquare.api.entities.VenuesSearchResult;


public class MainActivity extends Activity {

    GoogleMap googleMap;
    Marker m;
    CompactVenue[] loadedVenues = null;
    ParseUser currentUser = null;
    Boolean running = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, Secrets.parseAppID, Secrets.parseAppSecret);

        currentUser = ParseUser.getCurrentUser();

        if (currentUser != null) {

            running = true;

            setContentView(R.layout.activity_main);
            createMapView();
            addMarker();

        } else {

            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);

        }

    }

    @Override
    protected void onResume() {

        super.onResume();

        currentUser = ParseUser.getCurrentUser();

        if (currentUser != null && !running) {

            setContentView(R.layout.activity_main);
            createMapView();
            addMarker();

        } else if (!running) {

            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);

        }

    }

    /**
     * Initialises the mapview
     */
    private void createMapView(){
        /**
         * Catch the null pointer exception that
         * may be thrown when initialising the map
         */
        try {
            if(null == googleMap){
                googleMap = ((MapFragment) getFragmentManager().findFragmentById(
                        R.id.mapView)).getMap();

                /**
                 * If the map is still null after attempted initialisation,
                 * show an error to the user
                 */
                if(null == googleMap) {
                    Toast.makeText(getApplicationContext(),
                            "Error creating map", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (NullPointerException exception){
            Log.e("mapApp", exception.toString());
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

            System.out.println(ll);

            // First we need a initialize FoursquareApi.
            FoursquareApi foursquareApi = new FoursquareApi(Secrets.fourSquareClientID, Secrets.fourSquareClientSecret, "");

            // After client has been initialized we can make queries.
            Result<VenuesSearchResult> result = foursquareApi.venuesSearch(ll, null, null, null, null, null, null, null, null, null, null, 50, null);

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

    /**
     * Adds a marker to the map
     */
    private void addMarker(){

        /** Make sure that the map has been initialised **/
        if(null != googleMap){

            m = googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(0, 0))
                    .title("Marker")
                    .draggable(true));

            LocationListener ll = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {

                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    m.setPosition(currentLocation);
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));

                    ParseObject locationObject = new ParseObject("LocationHistory");
                    locationObject.put("lat", currentLocation.latitude);
                    locationObject.put("lng", currentLocation.longitude);
                    locationObject.put("user", currentUser);

                    ParseACL dataPermission = new ParseACL();
                    dataPermission.setPublicReadAccess(false);
                    dataPermission.setReadAccess(currentUser, true);
                    dataPermission.setPublicWriteAccess(false);
                    dataPermission.setWriteAccess(currentUser, true);

                    locationObject.setACL(dataPermission);
                    locationObject.saveInBackground();

                    if (loadedVenues == null) {
                        try {
                            loadedVenues = new FoursquareAPIAccess().execute(currentLocation).get();

                            for (CompactVenue venue : loadedVenues) {
                                String illegalCategories = "Park, Residential Building (Apartment / Condo), Playground, Field";
                                Boolean toAdd = false;
                                for (Category c : venue.getCategories()) {
                                    toAdd = illegalCategories.contains(c.getName());
                                }
                                if (!toAdd) {
                                    googleMap.addMarker(new MarkerOptions()
                                            .position(new LatLng(venue.getLocation().getLat(), venue.getLocation().getLng()))
                                            .title(venue.getName())
                                            .draggable(false));
                                }
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
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

            LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, ll);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, ll);
        }
    }

}
