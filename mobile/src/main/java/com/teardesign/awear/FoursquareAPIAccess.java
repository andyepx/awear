package com.teardesign.awear;

import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;

import fi.foyt.foursquare.api.FoursquareApi;
import fi.foyt.foursquare.api.FoursquareApiException;
import fi.foyt.foursquare.api.Result;
import fi.foyt.foursquare.api.entities.CompactVenue;
import fi.foyt.foursquare.api.entities.VenuesSearchResult;

/**
 * Created by Andx on 21/04/15.
 */
public class FoursquareAPIAccess extends AsyncTask<FoursquareParams, Integer, CompactVenue[]> {
    protected CompactVenue[] doInBackground(FoursquareParams... params) {

        try {
            LatLng currentLocation = params[0].location;

            return searchVenues(Double.toString(currentLocation.latitude) + "," + Double.toString(currentLocation.longitude), params[0].distance);
        } catch (FoursquareApiException e) {
            e.printStackTrace();
        }

        return null;
    }

    public CompactVenue[] searchVenues(String ll, int distance) throws FoursquareApiException {

        // First we need a initialize FoursquareApi.
        FoursquareApi foursquareApi = new FoursquareApi(Secrets.fourSquareClientID, Secrets.fourSquareClientSecret, "");

        // After client has been initialized we can make queries.
        Result<VenuesSearchResult> result = foursquareApi.venuesSearch(ll, null, null, null, null, null, "browse", "4d4b7105d754a06374d81259,4d4b7105d754a06378d81259", null, null, null, distance, null);

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
