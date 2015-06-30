package com.junjunguo.pocketmaps.controller;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.junjunguo.pocketmaps.R;
import com.junjunguo.pocketmaps.model.map.MapHandler;
import com.junjunguo.pocketmaps.model.map.Navigator;
import com.junjunguo.pocketmaps.model.util.Destination;
import com.junjunguo.pocketmaps.model.util.InstructionAdapter;
import com.junjunguo.pocketmaps.model.util.MapHandlerListener;
import com.junjunguo.pocketmaps.model.util.NavigatorListener;
import com.junjunguo.pocketmaps.model.util.Variable;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.MapPosition;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.model.MapViewPosition;

/**
 * This file is part of PocketMaps
 * <p/>
 * Created by GuoJunjun <junjunguo.com> on June 24, 2015.
 */
public class MapActions implements NavigatorListener, MapHandlerListener {
    private Activity activity;
    protected FloatingActionButton showPositionBtn, navigationBtn, settingsBtn, controlBtn;
    protected FloatingActionButton zoomInBtn, zoomOutBtn;
    private ViewGroup sideBarVP, sideBarMenuVP, navSettingsVP, navSettingsFromVP, navSettingsToVP, navInstructionVP,
            navInstructionListVP, appSettingsVP;
    private boolean menuVisible;
    /**
     * true handle on start point ; false handle on end point
     */
    private boolean onStartPoint;
    private EditText fromLocalET, toLocalET;

    public MapActions(Activity activity, MapView mapView) {
        this.activity = activity;
        this.showPositionBtn = (FloatingActionButton) activity.findViewById(R.id.map_show_my_position_fab);
        this.navigationBtn = (FloatingActionButton) activity.findViewById(R.id.map_nav_fab);
        this.settingsBtn = (FloatingActionButton) activity.findViewById(R.id.map_settings_fab);
        this.controlBtn = (FloatingActionButton) activity.findViewById(R.id.map_sidebar_control_fab);
        this.zoomInBtn = (FloatingActionButton) activity.findViewById(R.id.map_zoom_in_fab);
        this.zoomOutBtn = (FloatingActionButton) activity.findViewById(R.id.map_zoom_out_fab);
        // view groups managed by separate layout xml file
        this.sideBarVP = (ViewGroup) activity.findViewById(R.id.map_sidebar_layout);
        this.appSettingsVP = (ViewGroup) activity.findViewById(R.id.app_settings_layout);
        this.sideBarMenuVP = (ViewGroup) activity.findViewById(R.id.map_sidebar_menu_layout);
        this.navSettingsVP = (ViewGroup) activity.findViewById(R.id.nav_settings_layout);
        this.navSettingsFromVP = (ViewGroup) activity.findViewById(R.id.nav_settings_from_layout);
        this.navSettingsToVP = (ViewGroup) activity.findViewById(R.id.nav_settings_to_layout);
        this.navInstructionVP = (ViewGroup) activity.findViewById(R.id.nav_instruction_layout);
        this.navInstructionListVP = (ViewGroup) activity.findViewById(R.id.nav_instruction_list_layout);
        //form location and to location textView
        this.fromLocalET = (EditText) activity.findViewById(R.id.nav_settings_from_local_et);
        this.toLocalET = (EditText) activity.findViewById(R.id.nav_settings_to_local_et);
        this.menuVisible = false;
        this.onStartPoint = true;
        MapHandler.getMapHandler().setMapHandlerListener(this);
        Navigator.getNavigator().addListener(this);
        controlBtnHandler();
        zoomControlHandler(mapView);
        showMyLocation(mapView);
        navBtnHandler();
        navSettingsHandler();
        settingsBtnHandler();
    }

    /**
     * init and implement performance for settings
     */
    private void settingsBtnHandler() {
        appsettingsHandler();
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                sideBarVP.setVisibility(View.INVISIBLE);
                appSettingsVP.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * init app settings layout
     */
    private void appsettingsHandler() {
        ImageButton appsettingsClearBtn = (ImageButton) activity.findViewById(R.id.app_settings_clear_btn);
        appsettingsClearBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                appSettingsVP.setVisibility(View.INVISIBLE);
                sideBarVP.setVisibility(View.VISIBLE);
            }
        });

    }

    /**
     * navigation settings implementation
     */
    private void navSettingsHandler() {
        final ImageButton navSettingsClearBtn = (ImageButton) activity.findViewById(R.id.nav_settings_clear_btn);
        navSettingsClearBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                navSettingsVP.setVisibility(View.INVISIBLE);
                sideBarVP.setVisibility(View.VISIBLE);
            }
        });
        travelModeSetting();
        settingsFromItemHandler();
        settingsToItemHandler();
    }

    /**
     * settings layout:
     * <p/>
     * to item handler: when to item is clicked
     */
    private void settingsToItemHandler() {
        final ViewGroup toItemVG = (ViewGroup) activity.findViewById(R.id.map_nav_settings_to_item);
        toItemVG.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        toItemVG.setBackgroundColor(activity.getResources().getColor(R.color.my_primary_light));
                        return true;
                    case MotionEvent.ACTION_UP:
                        toItemVG.setBackgroundColor(activity.getResources().getColor(R.color.my_primary));
                        navSettingsVP.setVisibility(View.INVISIBLE);
                        navSettingsToVP.setVisibility(View.VISIBLE);
                        return true;
                }
                return false;
            }
        });
        //        to layout
        //clear button
        ImageButton toLayoutClearBtn = (ImageButton) activity.findViewById(R.id.nav_settings_to_clear_btn);
        toLayoutClearBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                navSettingsVP.setVisibility(View.VISIBLE);
                navSettingsToVP.setVisibility(View.INVISIBLE);
            }
        });
        //  to layout: items
        toUseCurrentLocationHandler();
        toChooseFromFavoriteHandler();
        toPointOnMapHandler();
    }

    /**
     * from layout : point item view group  (on to layout)
     * <p/>
     * preform actions when point on map item is clicked
     */
    private void toPointOnMapHandler() {
        final ViewGroup pointItem = (ViewGroup) activity.findViewById(R.id.map_nav_settings_to_point);
        pointItem.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        pointItem.setBackgroundColor(activity.getResources().getColor(R.color.my_primary_light));
                        return true;
                    case MotionEvent.ACTION_UP:
                        pointItem.setBackgroundColor(activity.getResources().getColor(R.color.my_primary));
                        onStartPoint = false;
                        navSettingsToVP.setVisibility(View.INVISIBLE);
                        //touch on map
                        //                        Toast.makeText(activity, "Touch on Map to choose your
                        // destination!", Toast.LENGTH_SHORT).show();
                        MapHandler.getMapHandler().setNeedLocation(true);
                        return true;
                }
                return false;
            }
        });
    }

    /**
     * choose from favorite list handler: preform actions when choose from favorite item is clicked (on to layout)
     */
    private void toChooseFromFavoriteHandler() {
        //create a list view
        //read from Json file inflater to RecyclerView
        final ViewGroup chooseFavorite = (ViewGroup) activity.findViewById(R.id.map_nav_settings_to_favorite);
        chooseFavorite.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        chooseFavorite.setBackgroundColor(activity.getResources().getColor(R.color.my_primary_light));
                        return true;
                    case MotionEvent.ACTION_UP:
                        chooseFavorite.setBackgroundColor(activity.getResources().getColor(R.color.my_primary));

                        return true;
                }
                return false;
            }
        });
    }

    /**
     * current location handler: preform actions when current location item is clicked (on to layout)
     */
    private void toUseCurrentLocationHandler() {
        final ViewGroup useCurrentLocal = (ViewGroup) activity.findViewById(R.id.map_nav_settings_to_current);
        useCurrentLocal.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        useCurrentLocal.setBackgroundColor(activity.getResources().getColor(R.color.my_primary_light));
                        return true;
                    case MotionEvent.ACTION_UP:
                        useCurrentLocal.setBackgroundColor(activity.getResources().getColor(R.color.my_primary));
                        if (MapActivity.getmCurrentLocation() != null) {
                            Destination.getDestination().setEndPoint(
                                    new LatLong(MapActivity.getmCurrentLocation().getLatitude(),
                                            MapActivity.getmCurrentLocation().getLongitude()));
                            toLocalET.setText(Destination.getDestination().getEndPointToString());
                            addToMarker(Destination.getDestination().getEndPoint());
                            navSettingsToVP.setVisibility(View.INVISIBLE);
                            navSettingsVP.setVisibility(View.VISIBLE);
                            activeNavigator();
                        } else {
                            Toast.makeText(activity, "Current Location not available, Check your GPS signal!",
                                    Toast.LENGTH_SHORT).show();
                        }
                        return true;
                }
                return false;
            }
        });
    }

    /**
     * add end point marker to map
     *
     * @param endPoint
     */
    private void addToMarker(LatLong endPoint) {
        MapHandler.getMapHandler().addEndMarker(endPoint);
    }

    /**
     * add start point marker to map
     *
     * @param startPoint
     */
    private void addFromMarker(LatLong startPoint) {
        MapHandler.getMapHandler().addStartMarker(startPoint);
    }

    /**
     * settings layout:
     * <p/>
     * from item handler: when from item is clicked
     */
    private void settingsFromItemHandler() {
        final ViewGroup fromFieldVG = (ViewGroup) activity.findViewById(R.id.map_nav_settings_from_item);
        fromFieldVG.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        fromFieldVG.setBackgroundColor(activity.getResources().getColor(R.color.my_primary_light));
                        return true;
                    case MotionEvent.ACTION_UP:
                        fromFieldVG.setBackgroundColor(activity.getResources().getColor(R.color.my_primary));
                        navSettingsVP.setVisibility(View.INVISIBLE);
                        navSettingsFromVP.setVisibility(View.VISIBLE);
                        return true;
                }
                return false;
            }
        });
        //        from layout
        //clear button
        ImageButton fromLayoutClearBtn = (ImageButton) activity.findViewById(R.id.nav_settings_from_clear_btn);
        fromLayoutClearBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                navSettingsVP.setVisibility(View.VISIBLE);
                navSettingsFromVP.setVisibility(View.INVISIBLE);
            }
        });
        //  from layout: items
        useCurrentLocationHandler();
        chooseFromFavoriteHandler();
        pointOnMapHandler();
    }

    /**
     * from layout : point item view group
     * <p/>
     * preform actions when point on map item is clicked
     */
    private void pointOnMapHandler() {
        final ViewGroup pointItem = (ViewGroup) activity.findViewById(R.id.map_nav_settings_from_point);
        pointItem.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        pointItem.setBackgroundColor(activity.getResources().getColor(R.color.my_primary_light));
                        return true;
                    case MotionEvent.ACTION_UP:
                        pointItem.setBackgroundColor(activity.getResources().getColor(R.color.my_primary));
                        onStartPoint = true;
                        navSettingsFromVP.setVisibility(View.INVISIBLE);
                        //touch on map
                        Toast.makeText(activity, "Touch on Map to choose your start Location", Toast.LENGTH_SHORT)
                                .show();
                        MapHandler.getMapHandler().setNeedLocation(true);
                        return true;
                }
                return false;
            }
        });
    }

    /**
     * choose from favorite list handler: preform actions when choose from favorite item is clicked
     */
    private void chooseFromFavoriteHandler() {
        //create a list view
        //read from Json file inflater to RecyclerView
        final ViewGroup chooseFavorite = (ViewGroup) activity.findViewById(R.id.map_nav_settings_from_favorite);
        chooseFavorite.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        chooseFavorite.setBackgroundColor(activity.getResources().getColor(R.color.my_primary_light));
                        return true;
                    case MotionEvent.ACTION_UP:
                        chooseFavorite.setBackgroundColor(activity.getResources().getColor(R.color.my_primary));

                        return true;
                }
                return false;
            }
        });
    }

    /**
     * current location handler: preform actions when current location item is clicked
     */
    private void useCurrentLocationHandler() {
        final ViewGroup useCurrentLocal = (ViewGroup) activity.findViewById(R.id.map_nav_settings_from_current);
        useCurrentLocal.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        useCurrentLocal.setBackgroundColor(activity.getResources().getColor(R.color.my_primary_light));
                        return true;
                    case MotionEvent.ACTION_UP:
                        useCurrentLocal.setBackgroundColor(activity.getResources().getColor(R.color.my_primary));
                        if (MapActivity.getmCurrentLocation() != null) {
                            Destination.getDestination().setStartPoint(
                                    new LatLong(MapActivity.getmCurrentLocation().getLatitude(),
                                            MapActivity.getmCurrentLocation().getLongitude()));
                            addFromMarker(Destination.getDestination().getStartPoint());
                            fromLocalET.setText(Destination.getDestination().getStartPointToString());
                            navSettingsFromVP.setVisibility(View.INVISIBLE);
                            navSettingsVP.setVisibility(View.VISIBLE);
                            activeNavigator();
                        } else {
                            Toast.makeText(activity, "Current Location not available, Check your GPS signal!",
                                    Toast.LENGTH_SHORT).show();
                        }
                        return true;
                }
                return false;
            }
        });
    }

    /**
     * when use press on the screen to get a location form map
     *
     * @param latLong
     */
    @Override public void onPressLocation(LatLong latLong) {
        if (onStartPoint) {
            Destination.getDestination().setStartPoint(latLong);
            addFromMarker(latLong);
            fromLocalET.setText(Destination.getDestination().getStartPointToString());
        } else {
            Destination.getDestination().setEndPoint(latLong);
            addToMarker(latLong);
            toLocalET.setText(Destination.getDestination().getEndPointToString());
        }
        navSettingsVP.setVisibility(View.VISIBLE);
        activeNavigator();
    }

    /**
     * calculate path calculating (running) true NOT running or finished false
     *
     * @param shortestPathRunning
     */
    @Override public void pathCalculating(boolean shortestPathRunning) {
        if (!shortestPathRunning && Navigator.getNavigator().getGhResponse() != null) {
            activeDirections();
        }
    }

    /**
     * drawer polyline on map , active navigator instructions(directions) if on
     */
    private void activeNavigator() {
        LatLong startPoint = Destination.getDestination().getStartPoint();
        LatLong endPoint = Destination.getDestination().getEndPoint();
        if (startPoint != null && endPoint != null) {
            MapHandler mapHandler = MapHandler.getMapHandler();
            mapHandler.calcPath(startPoint.latitude, startPoint.longitude, endPoint.latitude, endPoint.longitude);

            if (Variable.getVariable().isDirectionsON()) {
                mapHandler.setNeedPathCal(true);
                //rest running at
            }
        }
    }


    /**
     * active directions, and directions view
     */
    private void activeDirections() {
        RecyclerView instructionsRV;
        RecyclerView.Adapter instructionsAdapter;
        RecyclerView.LayoutManager instructionsLayoutManager;

        instructionsRV = (RecyclerView) activity.findViewById(R.id.nav_instruction_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        instructionsRV.setHasFixedSize(true);

        // use a linear layout manager
        instructionsLayoutManager = new LinearLayoutManager(activity);
        instructionsRV.setLayoutManager(instructionsLayoutManager);

        // specify an adapter (see also next example)
        instructionsAdapter = new InstructionAdapter(Navigator.getNavigator().getGhResponse().getInstructions());
        instructionsRV.setAdapter(instructionsAdapter);
        initNavListView();
    }

    /**
     * make nav list view control button ready to use
     */
    private void initNavListView() {
        fillNavListSummaryValues();
        navSettingsVP.setVisibility(View.INVISIBLE);
        navInstructionListVP.setVisibility(View.VISIBLE);
        ImageButton clearBtn, stopBtn;
        stopBtn = (ImageButton) activity.findViewById(R.id.nav_instruction_list_stop_btn);
        clearBtn = (ImageButton) activity.findViewById(R.id.nav_instruction_list_clear_btn);
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {

                // 1. Instantiate an AlertDialog.Builder with its constructor
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);

                // 2. Chain together various setter methods to set the dialog characteristics
                builder.setMessage(R.string.stop_navigation_msg).setTitle(R.string.stop_navigation)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // stop!
                                Navigator.getNavigator().setOn(false);
                                //delete polyline and markers
                                removeNavigation();
                                navInstructionListVP.setVisibility(View.INVISIBLE);
                                navSettingsVP.setVisibility(View.VISIBLE);
                                dialog.dismiss();
                            }
                        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        dialog.dismiss();
                    }
                });
                // Create the AlertDialog object and return it

                // 3. Get the AlertDialog from create()
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                navInstructionListVP.setVisibility(View.INVISIBLE);
                sideBarVP.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * fill up values for nav list summary
     */
    private void fillNavListSummaryValues() {
        ImageView travelMode;
        travelMode = (ImageView) activity.findViewById(R.id.nav_instruction_list_travel_mode_iv);
        travelMode.setImageResource(Navigator.getNavigator().getTravelModeResId(true));
        TextView from, to, distance, time;
        from = (TextView) activity.findViewById(R.id.nav_instruction_list_summary_from_tv);
        to = (TextView) activity.findViewById(R.id.nav_instruction_list_summary_to_tv);
        distance = (TextView) activity.findViewById(R.id.nav_instruction_list_summary_distance_tv);
        time = (TextView) activity.findViewById(R.id.nav_instruction_list_summary_time_tv);

        from.setText(Destination.getDestination().getStartPointToString());
        to.setText(Destination.getDestination().getEndPointToString());
        distance.setText(Navigator.getNavigator().getDistance());
        time.setText(Navigator.getNavigator().getTime());
    }

    /**
     * remove polyline, markers from map layers
     * <p/>
     * set from & to = null
     */
    private void removeNavigation() {
        MapHandler.getMapHandler().removeMarkers();
        fromLocalET.setText("");
        toLocalET.setText("");
        Navigator.getNavigator().setOn(false);
        Destination.getDestination().setStartPoint(null);
        Destination.getDestination().setEndPoint(null);
    }

    /**
     * set up travel mode
     */
    private void travelModeSetting() {
        final ImageButton footBtn, bikeBtn, carBtn;
        footBtn = (ImageButton) activity.findViewById(R.id.nav_settings_foot_btn);
        bikeBtn = (ImageButton) activity.findViewById(R.id.nav_settings_bike_btn);
        carBtn = (ImageButton) activity.findViewById(R.id.nav_settings_car_btn);
        // init travel mode
        switch (Variable.getVariable().getTravelMode()) {
            case "foot":
                footBtn.setImageResource(R.drawable.ic_directions_walk_orange_24dp);
                break;
            case "bike":
                bikeBtn.setImageResource(R.drawable.ic_directions_bike_orange_24dp);
                break;
            case "car":
                carBtn.setImageResource(R.drawable.ic_directions_car_orange_24dp);
                break;
        }

        //foot
        footBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (!Variable.getVariable().getTravelMode().equalsIgnoreCase("foot")) {
                    Variable.getVariable().setTravelMode("foot");
                    footBtn.setImageResource(R.drawable.ic_directions_walk_orange_24dp);
                    bikeBtn.setImageResource(R.drawable.ic_directions_bike_white_24dp);
                    carBtn.setImageResource(R.drawable.ic_directions_car_white_24dp);
                    activeNavigator();
                }
            }
        });
        //bike
        bikeBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (!Variable.getVariable().getTravelMode().equalsIgnoreCase("bike")) {
                    Variable.getVariable().setTravelMode("bike");
                    footBtn.setImageResource(R.drawable.ic_directions_walk_white_24dp);
                    bikeBtn.setImageResource(R.drawable.ic_directions_bike_orange_24dp);
                    carBtn.setImageResource(R.drawable.ic_directions_car_white_24dp);
                    activeNavigator();
                }
            }
        });
        // car
        carBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (!Variable.getVariable().getTravelMode().equalsIgnoreCase("car")) {
                    Variable.getVariable().setTravelMode("car");
                    footBtn.setImageResource(R.drawable.ic_directions_walk_white_24dp);
                    bikeBtn.setImageResource(R.drawable.ic_directions_bike_white_24dp);
                    carBtn.setImageResource(R.drawable.ic_directions_car_orange_24dp);
                    activeNavigator();
                }
            }
        });
    }

    /**
     * handler clicks on nav button
     */
    private void navBtnHandler() {
        navigationBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                sideBarVP.setVisibility(View.INVISIBLE);
                if (Navigator.getNavigator().isOn()) {
                    navInstructionListVP.setVisibility(View.VISIBLE);
                } else {
                    navSettingsVP.setVisibility(View.VISIBLE);
                }
            }
        });
    }


    /**
     * start button: control button handler
     */

    private void controlBtnHandler() {
        final ScaleAnimation anim = new ScaleAnimation(0, 1, 0, 1);
        anim.setFillBefore(true);
        anim.setFillAfter(true);
        anim.setFillEnabled(true);
        anim.setDuration(300);
        anim.setInterpolator(new OvershootInterpolator());

        controlBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (isMenuVisible()) {
                    setMenuVisible(false);
                    sideBarMenuVP.setVisibility(View.INVISIBLE);
                    controlBtn.setImageResource(R.drawable.ic_keyboard_arrow_up_white_24dp);
                    controlBtn.startAnimation(anim);
                } else {
                    setMenuVisible(true);
                    sideBarMenuVP.setVisibility(View.VISIBLE);
                    controlBtn.setImageResource(R.drawable.ic_clear_white_24dp);
                    controlBtn.startAnimation(anim);
                }
            }
        });
    }

    /**
     * implement zoom btn
     */
    protected void zoomControlHandler(final MapView mapView) {
        zoomInBtn.setImageResource(R.drawable.ic_add_white_24dp);
        zoomOutBtn.setImageResource(R.drawable.ic_remove_white_24dp);

        zoomInBtn.setOnClickListener(new View.OnClickListener() {
            MapViewPosition mvp = mapView.getModel().mapViewPosition;

            @Override public void onClick(View v) {
                if (mvp.getZoomLevel() < Variable.getVariable().getZoomLevelMax()) mvp.zoomIn();
            }
        });
        zoomOutBtn.setOnClickListener(new View.OnClickListener() {
            MapViewPosition mvp = mapView.getModel().mapViewPosition;

            @Override public void onClick(View v) {
                if (mvp.getZoomLevel() > Variable.getVariable().getZoomLevelMin()) mvp.zoomOut();
            }
        });
    }

    /**
     * move map to my current location as the center of the screen
     */
    protected void showMyLocation(final MapView mapView) {
        showPositionBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (MapActivity.getmCurrentLocation() != null) {
                    showPositionBtn.setImageResource(R.drawable.ic_my_location_white_24dp);
                    mapView.getModel().mapViewPosition.setMapPosition(new MapPosition(
                            new LatLong(MapActivity.getmCurrentLocation().getLatitude(),
                                    MapActivity.getmCurrentLocation().getLongitude()),
                            mapView.getModel().mapViewPosition.getZoomLevel()));
                } else {
                    showPositionBtn.setImageResource(R.drawable.ic_location_searching_white_24dp);
                    Toast.makeText(activity, "No Location Available", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * @return side bar menu visibility status
     */
    public boolean isMenuVisible() {
        return menuVisible;
    }

    /**
     * side bar menu visibility
     *
     * @param menuVisible
     */
    public void setMenuVisible(boolean menuVisible) {
        this.menuVisible = menuVisible;
    }

    /**
     * the change on navigator: navigation is used or not
     *
     * @param on
     */
    @Override public void statusChanged(boolean on) {
        if (on) {
            navigationBtn.setImageResource(R.drawable.ic_directions_white_24dp);
        } else {
            navigationBtn.setImageResource(R.drawable.ic_navigation_white_24dp);
        }
    }

}