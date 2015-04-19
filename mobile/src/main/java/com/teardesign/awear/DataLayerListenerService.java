package com.teardesign.awear;

import android.app.FragmentTransaction;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.parse.ParseACL;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import fi.foyt.foursquare.api.FoursquareApi;
import fi.foyt.foursquare.api.FoursquareApiException;
import fi.foyt.foursquare.api.Result;
import fi.foyt.foursquare.api.entities.CompactVenue;
import fi.foyt.foursquare.api.entities.VenuesSearchResult;

public class DataLayerListenerService extends WearableListenerService {

    private LocationListener ll;
    private CompactVenue[] loadedVenues;
    private ParseUser currentUser;
    private LocationManager locationManager;

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        //showToast(messageEvent.getPath());

        byte[] b = messageEvent.getData();
        ByteBuffer wrapped = ByteBuffer.wrap(b); // big-endian by default
        final int newValue = wrapped.getInt();

        currentUser = ParseUser.getCurrentUser();

        ll = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

                try {

                    loadedVenues = new FoursquareAPIAccess().execute(currentLocation).get();
                    CompactVenue currentVenue = null;
                    String currentVenueName = "";

                    for (CompactVenue venue : loadedVenues) {
                        float distance = 0;
                        float[] results = new float[3];
                        Location.distanceBetween(currentLocation.latitude, currentLocation.longitude, venue.getLocation().getLat(), venue.getLocation().getLng(), results);
                        if (distance == 0) {
                            distance = results[0];
                            currentVenue = venue;
                        } else if (results[0] < distance) {
                            distance = results[0];
                            currentVenue = venue;
                        }
                    }

                    if (currentVenue != null) {
                        //ltv.setText(currentVenue.getName());
                        currentVenueName = currentVenue.getName();
                    } else {
                        currentVenueName = "Undefined";
                    }

                    ParseObject expenseHistory = new ParseObject("ExpenseHistory");
                    expenseHistory.put("lat", currentLocation.latitude);
                    expenseHistory.put("lng", currentLocation.longitude);
                    expenseHistory.put("user", currentUser);
                    expenseHistory.put("amount", newValue);
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

                    locationManager.removeUpdates(ll);
                    ll = null;

                    showToast("$"+Integer.toString(newValue) + " at " + currentVenueName);

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

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, ll);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, ll);

    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
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
