package com.teardesign.awear;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.DrawableContainer;
import android.graphics.drawable.shapes.Shape;
import android.os.Bundle;
import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.view.DelayedConfirmationView;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static android.support.wearable.view.DelayedConfirmationView.*;

public class MainActivity extends Activity implements
        DelayedConfirmationListener,
        GoogleApiClient.ConnectionCallbacks,
        DataApi.DataListener, GoogleApiClient.OnConnectionFailedListener {

    private static final String COUNT_KEY = "com.teardesign.awear.count";
    private TextView mTextView;
    private static String TAG = "Wear";

    GoogleApiClient mGoogleApiClient;
    private int count = 0;
    private float currentScale = 0;
    private float scaleIncrease = 0;
    private float currentY = 0;

    private static final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
    private long lastUpdatedAt;
    private Runnable task;

    private DelayedConfirmationView mDelayedView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDelayedView = (DelayedConfirmationView) findViewById(R.id.delayed_confirm);
        mDelayedView.setTotalTimeMs(1500);
        mDelayedView.setListener(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();

        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
        else {
            mGoogleApiClient.disconnect();
            mGoogleApiClient.connect();
        }

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        scaleIncrease = size.y / 10;
        //scaleIncrease = 1 / 20;

        task = new Runnable() {
            public void run() {
                Date now = new Date();
                long timeDifference = now.getTime() - lastUpdatedAt;
                if (timeDifference > 3000) {
                    //worker.shutdown();
                    //ImageView tip = (ImageView) findViewById(R.id.tip);
                    //tip.setVisibility(GONE);
                    mDelayedView.setVisibility(VISIBLE);
                    mDelayedView.start();
                    //sendNewAmount();
                } else {
                    worker.schedule(task, 1, TimeUnit.SECONDS);
                }
            }
        };


        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {

                Animation rotate = AnimationUtils.loadAnimation(stub.getContext(), R.anim.rotate);

                ImageView tip = (ImageView) findViewById(R.id.tip);
                final ImageView scale = (ImageView) findViewById(R.id.scale);

                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);

                scale.setY(size.y);
                currentY = size.y;

                tip.setClickable(true);
                tip.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (count <= 18) {

                            Date now = new Date();
                            lastUpdatedAt = now.getTime();

                            TextView tv = (TextView) findViewById(R.id.text);
                            count += 2;

                            if (count == 2) {
                                worker.schedule(task, 2, TimeUnit.SECONDS);
                            }

                            tv.setText("$" + Integer.toString(count));

                            if (count > 12) {
                                scale.setBackgroundColor(Color.argb(255, 255, 166, 37));
                            } else {
                                scale.setBackgroundColor(Color.argb(255, 155, 203, 100));
                            }

                            float futureY = currentY - ( scaleIncrease );

                            ObjectAnimator anim = ObjectAnimator.ofFloat(scale, "Y", currentY, futureY);
                            currentY = futureY;
                            anim.setDuration(500);
                            anim.start();
                        }

                    }
                });

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Log.d(TAG, "Wearable Resume...");
    }

    // Create a data map and put data in it
    private void sendNewAmount() {
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/count");
        putDataMapReq.getDataMap().putInt(COUNT_KEY, count);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);

        Intent intent = new Intent(this, ConfirmationActivity.class);
        intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.SUCCESS_ANIMATION);
        intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE, getString(R.string.msg_sent));
        startActivity(intent);
    }

    @Override
    public void onTimerFinished(View view) {
        worker.shutdown();
        sendNewAmount();
    }

    @Override
    public void onTimerSelected(View view) {

    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // DataItem changed
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().compareTo("/count_back") == 0) {

                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    final String result = dataMap.getString(COUNT_KEY);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (result.length() > 0) {
                                TextView tv = (TextView) findViewById(R.id.text);
                                tv.setText(result);
                            }
                        }
                    });
                }
            }
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
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
