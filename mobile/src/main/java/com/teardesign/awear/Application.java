package com.teardesign.awear;

import com.parse.Parse;
import com.parse.ParseUser;

public class Application extends android.app.Application {

    public void onCreate() {
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, Secrets.parseAppID, Secrets.parseAppSecret);
    }

}