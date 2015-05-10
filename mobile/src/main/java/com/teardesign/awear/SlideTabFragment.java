package com.teardesign.awear;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.teardesign.common.view.SlidingTabLayout;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A basic sample which shows how to use {@link com.teardesign.common.view.SlidingTabLayout}
 * to display a custom {@link ViewPager} title strip which gives continuous feedback to the user
 * when scrolling.
 */
public class SlideTabFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private static final int DEFAULT_SELECTED_INDICATOR_COLOR = 0xFF33B5E5;

    public static SlideTabFragment newInstance() {
        SlideTabFragment fragment = new SlideTabFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public SlideTabFragment() {
        // Required empty public constructor
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
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
     * A custom {@link ViewPager} title strip which looks much like Tabs present in Android v4.0 and
     * above, but is designed to give continuous feedback to the user when scrolling.
     */
    private SlidingTabLayout mSlidingTabLayout;

    /**
     * A {@link ViewPager} which will be used in conjunction with the {@link SlidingTabLayout} above.
     */
    private ViewPager mViewPager;

    /**
     * Inflates the {@link View} which will be displayed by this {@link Fragment}, from the app's
     * resources.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.sliding_tabs, container, false);
    }

    /**
     * This is called after the {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)} has finished.
     * Here we can pick out the {@link View}s we need to configure from the content view.
     *
     * We set the {@link ViewPager}'s adapter to be an instance of {@link SamplePagerAdapter}. The
     * {@link SlidingTabLayout} is then given the {@link ViewPager} so that it can populate itself.
     *
     * @param view View created in {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Get the ViewPager and set it's PagerAdapter so that it can display items
        mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
        mViewPager.setAdapter(new SamplePagerAdapter());

        // Give the SlidingTabLayout the ViewPager, this must be done AFTER the ViewPager has had
        // it's PagerAdapter set.
        mSlidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setViewPager(mViewPager);
    }

    /**
     * The {@link android.support.v4.view.PagerAdapter} used to display pages in this sample.
     * The individual pages are simple and just display two lines of text. The important section of
     * this class is the {@link #getPageTitle(int)} method which controls what is displayed in the
     * {@link SlidingTabLayout}.
     */
    class SamplePagerAdapter extends PagerAdapter {

        /**
         * @return the number of pages to display
         */
        @Override
        public int getCount() {
            return 3;
        }

        /**
         * @return true if the value returned from {@link #instantiateItem(ViewGroup, int)} is the
         * same object as the {@link View} added to the {@link ViewPager}.
         */
        @Override
        public boolean isViewFromObject(View view, Object o) {
            return o == view;
        }

        /**
         * Return the title of the item at {@code position}. This is important as what this method
         * returns is what is displayed in the {@link SlidingTabLayout}.
         * <p>
         * Here we construct one using the position value, but for real application the title should
         * refer to the item's contents.
         */
        @Override
        public CharSequence getPageTitle(int i) {
            if (i == 0) return "Today";
            else if (i == 1) return "This week";
            else if (i == 2) return "Social";

            return "";
        }

        /**
         * Instantiate the {@link View} which should be displayed at {@code position}. Here we
         * inflate a layout from the apps resources and then change the text view to signify the position.
         */

        private GoogleMap googleMapHome;
        private GoogleMap googleMap;

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            final int[] todayAmounts = {0,0};
            final int[] weekAmounts = {0,0};

            float todayChart = 0.0f;
            float weekChatt = 0.0f;

            if (position == 0) {

                // Inflate a new layout from our resources
                final View v = getActivity().getLayoutInflater().inflate(R.layout.fragment_home,
                        container, false);
                // Add the newly created View to the ViewPager
                container.addView(v);

                //TextView tv = (TextView) v.findViewById(R.id.userName);
                //tv.setText(ParseUser.getCurrentUser().getUsername());

                final ParseObject[] todayExpensiveLocation = {null};

                TextView tve = (TextView) v.findViewById(R.id.expenses_title);
                tve.setText("Your expenses today");


                if (googleMapHome == null) {
                    googleMapHome = ((MapFragment) getActivity().getFragmentManager().findFragmentById(R.id.mapViewHome)).getMap();
                }
                googleMapHome.setMyLocationEnabled(true);

                ParseQuery<ParseObject> query = ParseQuery.getQuery("ExpenseHistory");
                query.findInBackground(new FindCallback<ParseObject>() {
                    public void done(List<ParseObject> scoreList, com.parse.ParseException e) {
                        if (e == null) {

                            for (ParseObject sl : scoreList) {
                                //LatLng ll = new LatLng(sl.getDouble("lat"), sl.getDouble("lng"));

                                Calendar c = Calendar.getInstance();
                                Calendar d = Calendar.getInstance();
                                d.setTime(sl.getCreatedAt());

                                if (c.get(Calendar.ERA) == d.get(Calendar.ERA) &&
                                        c.get(Calendar.YEAR) == d.get(Calendar.YEAR) &&
                                        c.get(Calendar.DAY_OF_YEAR) == d.get(Calendar.DAY_OF_YEAR)) {
                                    todayAmounts[0] += sl.getInt("amount");

                                    int fillColor = 0;
                                    if (todayAmounts[0] < 5)
                                        fillColor = 0x3000FF00;
                                    else if (todayAmounts[0] < 15)
                                        fillColor = 0x300000FF;
                                    else
                                        fillColor = 0x30FF0000;

                                    //int alpha =  (int)(0x11000000 * Math.ceil(weekAmounts[0] / 5.0f));
                                    //fillColor = alpha | fillColor;

                                    // Instantiates a new CircleOptions object and defines the center and radius
                                    CircleOptions circleOptions = new CircleOptions()
                                            .center(new LatLng(sl.getDouble("lat"), sl.getDouble("lng")))
                                            .fillColor(fillColor)
                                            .strokeColor(fillColor)
                                            .strokeWidth(3)
                                            .radius(20); // In meters

                                    // Get back the mutable Circle
                                    Circle circle = googleMapHome.addCircle(circleOptions);

                                    if (sl.getInt("amount") > todayAmounts[1]) {
                                        todayAmounts[1] = sl.getInt("amount");
                                        todayExpensiveLocation[0] = sl;
                                    }
                                }
                            }

                            //TextView tv = (TextView) v.findViewById(R.id.todayAmount);
                            //TextView tvLabel = (TextView) v.findViewById(R.id.todayExpensiveLabel);
                            //tv.setText("$ "+String.valueOf(todayAmounts[0]));

                            TextView tvLocation = (TextView) v.findViewById(R.id.todayExpensive);
                            if (todayExpensiveLocation[0] != null) {
                                //+ String.valueOf(todayExpensiveLocation[0].getInt("amount"))
                                tvLocation.setText(todayExpensiveLocation[0].getString("venue"));
                                LatLng ll = new LatLng(todayExpensiveLocation[0].getDouble("lat"), todayExpensiveLocation[0].getDouble("lng"));
                                googleMapHome.animateCamera(CameraUpdateFactory.newLatLngZoom(ll, 15));
                            } else {
                                tvLocation.setVisibility(View.GONE);
                            }

                            final DonutProgressAwear dp = (DonutProgressAwear) v.findViewById(R.id.today_expenses_donut);
                            final int amountBar = todayAmounts[0];
                            dp.setRangeDivider(1);
                            dp.setMax(50);

                            final Timer timer = new Timer();

                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {

                                    try {
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (dp.getProgress() < amountBar) {
                                                    dp.setProgress(dp.getProgress() + 1);
                                                } else {
                                                    timer.cancel();
                                                }
                                            }
                                        });
                                    } catch (RuntimeException re) {
                                        Log.d("INIT", "Couldnt init the wheel!");
                                        timer.cancel();
                                    }
                                }
                            }, 1500, 20);


                            Log.d("score", "Retrieved " + scoreList.size() + " scores");
                        } else {
                            Log.d("score", "Error: " + e.getMessage());
                        }
                    }
                });

                return v;

            } else if (position == 1) {

                // Inflate a new layout from our resources
                final View v = getActivity().getLayoutInflater().inflate(R.layout.today_expenses,
                        container, false);
                // Add the newly created View to the ViewPager
                container.addView(v);

                //TextView tv = (TextView) v.findViewById(R.id.userName);
                //tv.setText(ParseUser.getCurrentUser().getUsername());

                final ParseObject[] todayExpensiveLocation = {null};

                TextView tve = (TextView) v.findViewById(R.id.expenses_title);
                tve.setText("Your expenses this week");

                if (googleMap == null) {
                    googleMap = ((MapFragment) getActivity().getFragmentManager().findFragmentById(R.id.mapView)).getMap();
                }
                googleMap.setMyLocationEnabled(true);
//                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ll, 15));
//
//                Marker mapMarker = googleMap.addMarker(new MarkerOptions()
//                        .title(expense.getString("venue"))
//                        .position(ll));

                ParseQuery<ParseObject> query = ParseQuery.getQuery("ExpenseHistory");
                query.findInBackground(new FindCallback<ParseObject>() {
                    public void done(List<ParseObject> scoreList, com.parse.ParseException e) {
                        if (e == null) {

                            for (ParseObject sl : scoreList) {

                                Calendar c = Calendar.getInstance();
                                Calendar d = Calendar.getInstance();
                                d.setTime(sl.getCreatedAt());

                                long diff = c.getTimeInMillis() - d.getTimeInMillis();
                                long days = diff / (24 * 60 * 60 * 1000);

                                if (days <= 7) {
                                    weekAmounts[0] += sl.getInt("amount");

                                    int fillColor = 0;
                                    if (weekAmounts[0] < 5)
                                        fillColor = 0x3000FF00;
                                    else if (weekAmounts[0] < 15)
                                        fillColor = 0x300000FF;
                                    else
                                        fillColor = 0x30FF0000;

                                    //int alpha =  (int)(0x11000000 * Math.ceil(weekAmounts[0] / 5.0f));
                                    //fillColor = alpha | fillColor;

                                    // Instantiates a new CircleOptions object and defines the center and radius
                                    CircleOptions circleOptions = new CircleOptions()
                                            .center(new LatLng(sl.getDouble("lat"), sl.getDouble("lng")))
                                            .fillColor(fillColor)
                                            .strokeColor(fillColor)
                                            .strokeWidth(3)
                                            .radius(20); // In meters

                                    // Get back the mutable Circle
                                    Circle circle = googleMap.addCircle(circleOptions);

                                    if (sl.getInt("amount") > weekAmounts[1]) {
                                        weekAmounts[1] = sl.getInt("amount");
                                        todayExpensiveLocation[0] = sl;
                                    }
                                }
                            }

                            TextView tvLocation = (TextView) v.findViewById(R.id.todayExpensive);
                            if (todayExpensiveLocation[0] != null) {
                                tvLocation.setText(todayExpensiveLocation[0].getString("venue"));

                                LatLng ll = new LatLng(todayExpensiveLocation[0].getDouble("lat"), todayExpensiveLocation[0].getDouble("lng"));
                                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ll, 15));
                            } else {
                                tvLocation.setVisibility(View.GONE);
                            }

                            final DonutProgressAwear dp = (DonutProgressAwear) v.findViewById(R.id.today_expenses_donut);
                            final int amountBar = weekAmounts[0];
                            dp.setRangeDivider(1f);
                            dp.setMax(350);

                            final Timer timer = new Timer();

                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {

                                    try {
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (dp.getProgress() < amountBar) {
                                                    dp.setProgress(dp.getProgress() + 1);
                                                } else {
                                                    timer.cancel();
                                                }
                                            }
                                        });
                                    } catch (RuntimeException re) {
                                        Log.d("INIT", "Couldnt init the wheel!");
                                        timer.cancel();
                                    }
                                }
                            }, 1500, 20);


                            Log.d("score", "Retrieved " + scoreList.size() + " scores");
                        } else {
                            Log.d("score", "Error: " + e.getMessage());
                        }
                    }
                });

                return v;

            } else {

                // Inflate a new layout from our resources
                final View view = getActivity().getLayoutInflater().inflate(R.layout.pager_item,
                        container, false);
                // Add the newly created View to the ViewPager
                container.addView(view);

                ParseQuery<ParseObject> query = ParseQuery.getQuery("ExpenseHistory");
                query.findInBackground(new FindCallback<ParseObject>() {
                    public void done(List<ParseObject> scoreList, com.parse.ParseException e) {
                        if (e == null) {

                            for (ParseObject sl : scoreList) {

                                Calendar c = Calendar.getInstance();
                                Calendar d = Calendar.getInstance();
                                d.setTime(sl.getCreatedAt());

                                long diff = c.getTimeInMillis() - d.getTimeInMillis();
                                long days = diff / (24 * 60 * 60 * 1000);

                                if (days <= 7) {
                                    weekAmounts[0] += sl.getInt("amount");
                                }

                                if (c.get(Calendar.ERA) == d.get(Calendar.ERA) &&
                                        c.get(Calendar.YEAR) == d.get(Calendar.YEAR) &&
                                        c.get(Calendar.DAY_OF_YEAR) == d.get(Calendar.DAY_OF_YEAR)) {
                                    todayAmounts[0] += sl.getInt("amount");
                                }
                            }

                            ArrayList<String> xVals = new ArrayList<String>();
                            xVals.add("Jen"); xVals.add("Mary"); xVals.add("Beth"); xVals.add("You");
                            xVals.add("Eloise"); xVals.add("John"); xVals.add("Max"); xVals.add("Paul");

                            BarChart rl = (BarChart) view.findViewById(R.id.chart);
                            rl.setDragEnabled(false);
                            rl.setPinchZoom(false);
                            rl.setScaleXEnabled(false);
                            rl.setScaleYEnabled(false);
                            rl.setDoubleTapToZoomEnabled(false);
                            rl.getLegend().setEnabled(false);
                            rl.getAxisRight().setDrawLabels(false);

                            YAxis leftAxis = rl.getAxisLeft();
                            leftAxis.setDrawGridLines(false);

                            YAxis rightAxis = rl.getAxisRight();

                            XAxis xAxis = rl.getXAxis();
                            xAxis.setDrawLabels(true);
                            xAxis.setLabelsToSkip(0);
                            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

                            ArrayList<BarEntry> valsComp1 = new ArrayList<BarEntry>();

                            BarEntry c1e1 = new BarEntry(generateRandomDay(10f, 0), 0);
                            valsComp1.add(c1e1);
                            BarEntry c1e2 = new BarEntry(generateRandomDay(26f, 1), 1);
                            valsComp1.add(c1e2);
                            BarEntry c1e3 = new BarEntry(generateRandomDay(8f, 2), 2);
                            valsComp1.add(c1e3);
                            BarEntry c1e4 = new BarEntry((float) todayAmounts[0], 3);
                            valsComp1.add(c1e4);
                            BarEntry c1e5 = new BarEntry(generateRandomDay(4f, 4), 4);
                            valsComp1.add(c1e5);
                            BarEntry c1e6 = new BarEntry(generateRandomDay(32f, 5), 5);
                            valsComp1.add(c1e6);
                            BarEntry c1e7 = new BarEntry(generateRandomDay(18f, 6), 6);
                            valsComp1.add(c1e7);
                            BarEntry c1e8 = new BarEntry(generateRandomDay(16f, 7), 7);
                            valsComp1.add(c1e8);

                            BarDataSet setComp1 = new BarDataSet(valsComp1, "Data");
                            setComp1.setColors(ColorTemplate.JOYFUL_COLORS);

                            ArrayList<BarDataSet> dataSets = new ArrayList<BarDataSet>();
                            dataSets.add(setComp1);

                            BarData data = new BarData(xVals, dataSets);
                            rl.setData(data);
                            rl.invalidate(); // refresh



                            BarChart rl2 = (BarChart) view.findViewById(R.id.chart2);
                            rl2.setDragEnabled(false);
                            rl2.setPinchZoom(false);
                            rl2.setScaleXEnabled(false);
                            rl2.setScaleYEnabled(false);
                            rl2.setDoubleTapToZoomEnabled(false);
                            rl2.getLegend().setEnabled(false);
                            rl2.getAxisRight().setDrawLabels(false);

                            YAxis leftAxis2 = rl2.getAxisLeft();
                            leftAxis2.setDrawGridLines(false);

                            //YAxis rightAxis = rl2.getAxisRight();

                            XAxis xAxis2 = rl2.getXAxis();
                            xAxis2.setDrawLabels(true);
                            xAxis2.setLabelsToSkip(0);
                            xAxis2.setPosition(XAxis.XAxisPosition.BOTTOM);

                            ArrayList<BarEntry> valsComp2 = new ArrayList<BarEntry>();

                            BarEntry c2e1 = new BarEntry(generateRandomWeek(7*12f, 0), 0);
                            valsComp2.add(c2e1);
                            BarEntry c2e2 = new BarEntry(generateRandomWeek(7*26f, 1), 1);
                            valsComp2.add(c2e2);
                            BarEntry c2e3 = new BarEntry(generateRandomWeek(7*8f, 2), 2);
                            valsComp2.add(c2e3);
                            BarEntry c2e4 = new BarEntry((float) weekAmounts[0], 3);
                            valsComp2.add(c2e4);
                            BarEntry c2e5 = new BarEntry(generateRandomWeek(7*4f, 4), 4);
                            valsComp2.add(c2e5);
                            BarEntry c2e6 = new BarEntry(generateRandomWeek(7*32f, 5), 5);
                            valsComp2.add(c2e6);
                            BarEntry c2e7 = new BarEntry(generateRandomWeek(7*18f, 6), 6);
                            valsComp2.add(c2e7);
                            BarEntry c2e8 = new BarEntry(generateRandomWeek(7*16f, 7), 7);
                            valsComp2.add(c2e8);

                            BarDataSet setComp2 = new BarDataSet(valsComp2, "Data");
                            setComp2.setColors(ColorTemplate.JOYFUL_COLORS);
                            //setComp1.setLabel("");

                            ArrayList<BarDataSet> dataSets2 = new ArrayList<BarDataSet>();
                            dataSets2.add(setComp2);

                            BarData data2 = new BarData(xVals, dataSets2);
                            rl2.setData(data2);
                            rl2.invalidate(); // refresh

                        }
                    }
                });

                // Retrieve a TextView from the inflated View, and update it's text
                //TextView title = (TextView) view.findViewById(R.id.item_title);
                //title.setText(String.valueOf(position + 1));

                // Return the View
                return view;

            }
        }

        /**
         * Destroy the item from the {@link ViewPager}. In our case this is simply removing the
         * {@link View}.
         */
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {


            try {
                Fragment f = getFragmentManager().findFragmentById(R.id.mapView);
                if (f != null) getFragmentManager().beginTransaction().remove(f).commit();
            } catch (NullPointerException e) {

            }

            try {
                Fragment f = getFragmentManager().findFragmentById(R.id.mapViewHome);
                if (f != null) getFragmentManager().beginTransaction().remove(f).commit();
            } catch (NullPointerException e) {

            }

            container.removeView((View) object);

        }

        private float generateRandomDay(float base, int p) {

            Calendar c = Calendar.getInstance();

            float random = (p+1) * (base + c.get(Calendar.DAY_OF_WEEK));
            while (random > (25f - p)) {
                random -= 2 * p;
            }

            return random;
        }

        private float generateRandomWeek(float base, int p) {

            Calendar c = Calendar.getInstance();

            float random = (p+1) * (base + c.get(Calendar.WEEK_OF_YEAR));
            while (random > (7*25f-p)) {
                random -= 4 * p;
            }

            return random;
        }

    }
}
