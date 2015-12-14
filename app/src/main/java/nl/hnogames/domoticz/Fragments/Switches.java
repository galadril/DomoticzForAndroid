/*
 * Copyright (C) 2015 Domoticz
 *
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package nl.hnogames.domoticz.Fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import nl.hnogames.domoticz.Adapters.SwitchesAdapter;
import nl.hnogames.domoticz.Containers.DevicesInfo;
import nl.hnogames.domoticz.Containers.ExtendedStatusInfo;
import nl.hnogames.domoticz.Containers.SwitchInfo;
import nl.hnogames.domoticz.Containers.SwitchLogInfo;
import nl.hnogames.domoticz.Containers.SwitchTimerInfo;
import nl.hnogames.domoticz.Domoticz.Domoticz;
import nl.hnogames.domoticz.Interfaces.DomoticzFragmentListener;
import nl.hnogames.domoticz.Interfaces.StatusReceiver;
import nl.hnogames.domoticz.Interfaces.SwitchLogReceiver;
import nl.hnogames.domoticz.Interfaces.SwitchTimerReceiver;
import nl.hnogames.domoticz.Interfaces.SwitchesReceiver;
import nl.hnogames.domoticz.Interfaces.setCommandReceiver;
import nl.hnogames.domoticz.Interfaces.switchesClickListener;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.UI.ColorPickerDialog;
import nl.hnogames.domoticz.UI.SwitchInfoDialog;
import nl.hnogames.domoticz.UI.SwitchLogInfoDialog;
import nl.hnogames.domoticz.UI.SwitchTimerInfoDialog;
import nl.hnogames.domoticz.app.DomoticzFragment;

public class Switches extends DomoticzFragment implements DomoticzFragmentListener,
        switchesClickListener {

    @SuppressWarnings("unused")
    private static final String TAG = Switches.class.getSimpleName();
    private ArrayList<ExtendedStatusInfo> supportedSwitches = new ArrayList<>();
    private ProgressDialog progressDialog;
    private Domoticz mDomoticz;
    private Context mActivity;
    private int currentSwitch = 1;
    private SwitchesAdapter adapter;
    private ListView listView;

    private CoordinatorLayout coordinatorLayout;
    private ArrayList<ExtendedStatusInfo> extendedStatusSwitches;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public void refreshFragment() {
        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setRefreshing(true);

        getSwitchesData();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        getActionBar().setTitle(R.string.title_switches);
    }

    @Override
    public void Filter(String text) {
        try {
            if (adapter != null)
                adapter.getFilter().filter(text);
            super.Filter(text);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onConnectionOk() {
        showProgressDialog();

        mDomoticz = new Domoticz(mActivity);
        getSwitchesData();
    }

    private void getSwitchesData() {
        mDomoticz.getSwitches(new SwitchesReceiver() {
            @Override
            public void onReceiveSwitches(ArrayList<SwitchInfo> switches) {
                processSwitches(switches);
            }

            @Override
            public void onError(Exception error) {
                errorHandling(error);
            }
        });
    }

    private void processSwitches(ArrayList<SwitchInfo> switchInfos) {
        currentSwitch = 1;//reset values
        extendedStatusSwitches = new ArrayList<>();
        final int totalNumberOfSwitches = switchInfos.size();

        for (SwitchInfo switchInfo : switchInfos) {
            successHandling(switchInfo.toString(), false);
            int idx = switchInfo.getIdx();

            mDomoticz.getStatus(idx, new StatusReceiver() {
                @Override
                public void onReceiveStatus(ExtendedStatusInfo extendedStatusInfo) {
                    extendedStatusSwitches.add(extendedStatusInfo);     // Add to array
                    if (currentSwitch == totalNumberOfSwitches) {
                        createListView(extendedStatusSwitches);         // All extended info is in
                    } else currentSwitch++;                               // Not there yet
                }

                @Override
                public void onError(Exception error) {
                    errorHandling(error);
                }
            });
        }
    }

    // add dynamic list view
    // https://github.com/nhaarman/ListViewAnimations
    private void createListView(ArrayList<ExtendedStatusInfo> switches) {
        try {
            coordinatorLayout = (CoordinatorLayout) getView().findViewById(R.id.coordinatorLayout);

            mSwipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipe_refresh_layout);

            supportedSwitches = new ArrayList<>();
            final List<Integer> appSupportedSwitchesValues = mDomoticz.getSupportedSwitchesValues();
            final List<String> appSupportedSwitchesNames = mDomoticz.getSupportedSwitchesNames();

            for (ExtendedStatusInfo mExtendedStatusInfo : switches) {
                String name = mExtendedStatusInfo.getName();
                int switchTypeVal = mExtendedStatusInfo.getSwitchTypeVal();
                String switchType = mExtendedStatusInfo.getSwitchType();

                if (!name.startsWith(Domoticz.HIDDEN_CHARACTER) &&
                        appSupportedSwitchesValues.contains(switchTypeVal) &&
                        appSupportedSwitchesNames.contains(switchType)) {
                    if (super.getSort().equals(null) || super.getSort().length() <= 0 || super.getSort().equals(getContext().getString(R.string.sort_all))) {
                        supportedSwitches.add(mExtendedStatusInfo);
                    } else {
                        Snackbar.make(coordinatorLayout, "Filter on :" + super.getSort(), Snackbar.LENGTH_SHORT).show();
                        if ((super.getSort().equals(getContext().getString(R.string.sort_on)) && mExtendedStatusInfo.getStatusBoolean()) && isOnOffSwitch(mExtendedStatusInfo)) {
                            supportedSwitches.add(mExtendedStatusInfo);
                        }
                        if ((super.getSort().equals(getContext().getString(R.string.sort_off)) && !mExtendedStatusInfo.getStatusBoolean()) && isOnOffSwitch(mExtendedStatusInfo)) {
                            supportedSwitches.add(mExtendedStatusInfo);
                        }
                        if ((super.getSort().equals(getContext().getString(R.string.sort_static)) ) && !isOnOffSwitch(mExtendedStatusInfo)) {
                            supportedSwitches.add(mExtendedStatusInfo);
                        }
                    }
                }
            }

            final switchesClickListener listener = this;
            adapter = new SwitchesAdapter(mActivity, supportedSwitches, listener);
            listView = (ListView) getView().findViewById(R.id.listView);
            listView.setAdapter(adapter);
            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view,
                                               int index, long id) {
                    showInfoDialog(adapter.filteredData.get(index));
                    return true;
                }
            });

            mSwipeRefreshLayout.setRefreshing(false);
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    getSwitchesData();
                }
            });
        } catch (Exception ex) {
            errorHandling(ex);
        }
        hideProgressDialog();
    }

    private boolean isOnOffSwitch(ExtendedStatusInfo testSwitch)
    {
        switch (testSwitch.getSwitchTypeVal()) {
            case Domoticz.Device.Type.Value.ON_OFF:
            case Domoticz.Device.Type.Value.MEDIAPLAYER:
            case Domoticz.Device.Type.Value.X10SIREN:
            case Domoticz.Device.Type.Value.DOORLOCK:
            case Domoticz.Device.Type.Value.BLINDPERCENTAGE:
            case Domoticz.Device.Type.Value.BLINDINVERTED:
            case Domoticz.Device.Type.Value.BLINDPERCENTAGEINVERTED:
            case Domoticz.Device.Type.Value.BLINDVENETIAN:
            case Domoticz.Device.Type.Value.BLINDS:
            case Domoticz.Device.Type.Value.DIMMER:
                return true;
        }
        switch (testSwitch.getType()) {
            case Domoticz.Scene.Type.GROUP:
                return true;
        }

        return false;
    }
    private void showInfoDialog(final ExtendedStatusInfo mSwitch) {

        SwitchInfoDialog infoDialog = new SwitchInfoDialog(
                getActivity(),
                mSwitch,
                R.layout.dialog_switch_info);
        infoDialog.setIdx(String.valueOf(mSwitch.getIdx()));
        infoDialog.setLastUpdate(mSwitch.getLastUpdate());
        infoDialog.setSignalLevel(String.valueOf(mSwitch.getSignalLevel()));
        infoDialog.setBatteryLevel(String.valueOf(mSwitch.getBatteryLevel()));
        infoDialog.setIsFavorite(mSwitch.getFavoriteBoolean());
        infoDialog.show();
        infoDialog.onDismissListener(new SwitchInfoDialog.DismissListener() {
            @Override
            public void onDismiss(boolean isChanged, boolean isFavorite) {
                if (isChanged) changeFavorite(mSwitch, isFavorite);
            }
        });
    }

    private void showLogDialog(ArrayList<SwitchLogInfo> switchLogs) {
        if (switchLogs.size() <= 0) {
            Toast.makeText(getContext(), "No logs found.", Toast.LENGTH_LONG).show();
        } else {
            SwitchLogInfoDialog infoDialog = new SwitchLogInfoDialog(
                    getActivity(),
                    switchLogs,
                    R.layout.dialog_switch_logs);
            infoDialog.show();
        }
    }

    private void showTimerDialog(ArrayList<SwitchTimerInfo> switchLogs) {
        if (switchLogs.size() <= 0) {
            Toast.makeText(getContext(), "No timer found.", Toast.LENGTH_LONG).show();
        } else {
            SwitchTimerInfoDialog infoDialog = new SwitchTimerInfoDialog(
                    getActivity(),
                    switchLogs,
                    R.layout.dialog_switch_logs);
            infoDialog.show();
        }
    }

    private void changeFavorite(final ExtendedStatusInfo mSwitch, final boolean isFavorite) {
        addDebugText("changeFavorite");
        addDebugText("Set idx " + mSwitch.getIdx() + " favorite to " + isFavorite);

        if (isFavorite)
            Snackbar.make(coordinatorLayout, mSwitch.getName() + " " + getActivity().getString(R.string.favorite_added), Snackbar.LENGTH_SHORT).show();
        else
            Snackbar.make(coordinatorLayout, mSwitch.getName() + " " + getActivity().getString(R.string.favorite_removed), Snackbar.LENGTH_SHORT).show();

        int jsonAction;
        int jsonUrl = Domoticz.Json.Url.Set.FAVORITE;

        if (isFavorite) jsonAction = Domoticz.Device.Favorite.ON;
        else jsonAction = Domoticz.Device.Favorite.OFF;

        mDomoticz.setAction(mSwitch.getIdx(), jsonUrl, jsonAction, 0, new setCommandReceiver() {
            @Override
            public void onReceiveResult(String result) {
                successHandling(result, false);
                mSwitch.setFavoriteBoolean(isFavorite);
            }

            @Override
            public void onError(Exception error) {
                errorHandling(error);
            }
        });

    }

    @Override
    public void onLogButtonClick(int idx) {
        mDomoticz.getSwitchLogs(idx, new SwitchLogReceiver() {
            @Override
            public void onReceiveSwitches(ArrayList<SwitchLogInfo> switcheLogs) {
                showLogDialog(switcheLogs);
            }

            @Override
            public void onError(Exception error) {
                errorHandling(error);
            }
        });
    }

    @Override
    public void onColorButtonClick(final int idx) {

        ColorPickerDialog colorDialog = new ColorPickerDialog(
                getActivity());
        colorDialog.show();
        colorDialog.onDismissListener(new ColorPickerDialog.DismissListener() {
            @Override
            public void onDismiss(int selectedColor) {
                float[] hsv = new float[3];
                Color.RGBToHSV(Color.red(selectedColor), Color.green(selectedColor), Color.blue(selectedColor), hsv);
                Log.v(TAG, "Selected HVS Color: h:" + hsv[0] + " v:" + hsv[1] + " s:" + hsv[2]);

                mDomoticz.setRGBColorAction(idx,
                        Domoticz.Json.Url.Set.RGBCOLOR,
                        Math.round(hsv[0]),
                        100,
                        new setCommandReceiver() {
                            @Override
                            public void onReceiveResult(String result) {
                                Snackbar.make(coordinatorLayout, "Color set for: " + getSwitch(idx).getName(), Snackbar.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onError(Exception error) {
                                Snackbar.make(coordinatorLayout, "Could not change the Color", Snackbar.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    @Override
    public void onTimerButtonClick(int idx) {
        mDomoticz.getSwitchTimers(idx, new SwitchTimerReceiver() {
            @Override
            public void onReceiveSwitchTimers(ArrayList<SwitchTimerInfo> switchTimers) {
                showTimerDialog(switchTimers);
            }

            @Override
            public void onError(Exception error) {
                errorHandling(error);
            }
        });
    }

    private ExtendedStatusInfo getSwitch(int idx) {
        ExtendedStatusInfo clickedSwitch = null;
        for (ExtendedStatusInfo mExtendedStatusInfo : extendedStatusSwitches) {
            if (mExtendedStatusInfo.getIdx() == idx) {
                clickedSwitch = mExtendedStatusInfo;
            }
        }
        return clickedSwitch;
    }

    @Override
    public void onSwitchClick(int idx, boolean checked) {
        addDebugText("onSwitchClick");
        addDebugText("Set idx " + idx + " to " + checked);
        ExtendedStatusInfo clickedSwitch = getSwitch(idx);

        if (checked)
            Snackbar.make(coordinatorLayout, getActivity().getString(R.string.switch_on) + ": " + clickedSwitch.getName(), Snackbar.LENGTH_SHORT).show();
        else
            Snackbar.make(coordinatorLayout, getActivity().getString(R.string.switch_off) + ": " + clickedSwitch.getName(), Snackbar.LENGTH_SHORT).show();

        if (clickedSwitch != null) {
            int jsonAction;
            int jsonUrl = Domoticz.Json.Url.Set.SWITCHES;

            if (clickedSwitch.getSwitchTypeVal() == Domoticz.Device.Type.Value.BLINDS ||
                    clickedSwitch.getSwitchTypeVal() == Domoticz.Device.Type.Value.BLINDPERCENTAGE) {
                if (checked) jsonAction = Domoticz.Device.Switch.Action.OFF;
                else jsonAction = Domoticz.Device.Switch.Action.ON;
            } else {
                if (checked) jsonAction = Domoticz.Device.Switch.Action.ON;
                else jsonAction = Domoticz.Device.Switch.Action.OFF;
            }

            mDomoticz.setAction(idx, jsonUrl, jsonAction, 0, new setCommandReceiver() {
                @Override
                public void onReceiveResult(String result) {
                    successHandling(result, false);
                }

                @Override
                public void onError(Exception error) {
                    errorHandling(error);
                }
            });
        }
    }

    @Override
    public void onButtonClick(int idx, boolean checked) {
        addDebugText("onButtonClick");
        addDebugText("Set idx " + idx + " to ON");
        ExtendedStatusInfo clickedSwitch = getSwitch(idx);

        if (checked)
            Snackbar.make(coordinatorLayout, getActivity().getString(R.string.switch_on) + ": " + clickedSwitch.getName(), Snackbar.LENGTH_SHORT).show();
        else
            Snackbar.make(coordinatorLayout, getActivity().getString(R.string.switch_off) + ": " + clickedSwitch.getName(), Snackbar.LENGTH_SHORT).show();

        int jsonAction;
        int jsonUrl = Domoticz.Json.Url.Set.SWITCHES;

        if (checked)
            jsonAction = Domoticz.Device.Switch.Action.ON;
        else jsonAction =
                Domoticz.Device.Switch.Action.OFF;

        mDomoticz.setAction(idx, jsonUrl, jsonAction, 0, new setCommandReceiver() {
            @Override
            public void onReceiveResult(String result) {
                successHandling(result, false);
            }

            @Override
            public void onError(Exception error) {
                errorHandling(error);
            }
        });
    }

    @Override
    public void onBlindClick(int idx, int jsonAction) {
        addDebugText("onBlindClick");
        addDebugText("Set idx " + idx + " to " + String.valueOf(jsonAction));
        ExtendedStatusInfo clickedSwitch = getSwitch(idx);

        if ((jsonAction == Domoticz.Device.Blind.Action.UP || jsonAction == Domoticz.Device.Blind.Action.OFF) && (clickedSwitch.getSwitchTypeVal() != Domoticz.Device.Type.Value.BLINDINVERTED))
            Snackbar.make(coordinatorLayout, getActivity().getString(R.string.blind_up) + ": " + clickedSwitch.getName(), Snackbar.LENGTH_SHORT).show();
        else if ((jsonAction == Domoticz.Device.Blind.Action.DOWN || jsonAction == Domoticz.Device.Blind.Action.ON) && (clickedSwitch.getSwitchTypeVal() != Domoticz.Device.Type.Value.BLINDINVERTED))
            Snackbar.make(coordinatorLayout, getActivity().getString(R.string.blind_down) + ": " + clickedSwitch.getName(), Snackbar.LENGTH_SHORT).show();
        else if ((jsonAction == Domoticz.Device.Blind.Action.UP || jsonAction == Domoticz.Device.Blind.Action.OFF) && (clickedSwitch.getSwitchTypeVal() == Domoticz.Device.Type.Value.BLINDINVERTED))
            Snackbar.make(coordinatorLayout, getActivity().getString(R.string.blind_down) + ": " + clickedSwitch.getName(), Snackbar.LENGTH_SHORT).show();
        else if ((jsonAction == Domoticz.Device.Blind.Action.DOWN || jsonAction == Domoticz.Device.Blind.Action.ON) && (clickedSwitch.getSwitchTypeVal() == Domoticz.Device.Type.Value.BLINDINVERTED))
            Snackbar.make(coordinatorLayout, getActivity().getString(R.string.blind_up) + ": " + clickedSwitch.getName(), Snackbar.LENGTH_SHORT).show();
        else
            Snackbar.make(coordinatorLayout, getActivity().getString(R.string.blind_stop) + ": " + clickedSwitch.getName(), Snackbar.LENGTH_SHORT).show();

        int jsonUrl = Domoticz.Json.Url.Set.SWITCHES;
        mDomoticz.setAction(idx, jsonUrl, jsonAction, 0, new setCommandReceiver() {
            @Override
            public void onReceiveResult(String result) {
                successHandling(result, false);
            }

            @Override
            public void onError(Exception error) {
                errorHandling(error);
            }
        });
    }

    @Override
    public void onDimmerChange(int idx, int value) {
        addDebugText("onDimmerChange");
        ExtendedStatusInfo clickedSwitch = getSwitch(idx);

        Snackbar.make(coordinatorLayout, "Setting level for switch: " + clickedSwitch.getName() + " to " + value, Snackbar.LENGTH_SHORT).show();

        int jsonUrl = Domoticz.Json.Url.Set.SWITCHES;
        int jsonAction = Domoticz.Device.Dimmer.Action.DIM_LEVEL;
        mDomoticz.setAction(idx, jsonUrl, jsonAction, value, new setCommandReceiver() {
            @Override
            public void onReceiveResult(String result) {
                successHandling(result, false);
            }

            @Override
            public void onError(Exception error) {
                errorHandling(error);
            }
        });
    }

    /**
     * Initializes the progress dialog
     */
    private void initProgressDialog() {
        progressDialog = new ProgressDialog(this.getActivity());
        progressDialog.setMessage(getString(R.string.msg_please_wait));
        progressDialog.setCancelable(false);
    }

    /**
     * Shows the progress dialog if isn't already showing
     */
    private void showProgressDialog() {
        if (progressDialog == null) initProgressDialog();
        if (!progressDialog.isShowing())
            progressDialog.show();
    }

    /**
     * Hides the progress dialog if it is showing
     */
    private void hideProgressDialog() {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
    }

    @Override
    public void errorHandling(Exception error) {
        super.errorHandling(error);
        hideProgressDialog();
    }
}