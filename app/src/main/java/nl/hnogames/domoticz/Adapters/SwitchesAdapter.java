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

package nl.hnogames.domoticz.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import nl.hnogames.domoticz.Containers.ExtendedStatusInfo;
import nl.hnogames.domoticz.Domoticz.Domoticz;
import nl.hnogames.domoticz.Interfaces.switchesClickListener;
import nl.hnogames.domoticz.R;

public class SwitchesAdapter extends BaseAdapter implements Filterable {

    private final int ID_TEXTVIEW = 1000;
    private final int ID_SWITCH = 0;
    public ArrayList<ExtendedStatusInfo> filteredData = null;
    Domoticz domoticz;
    private Context context;
    private ArrayList<ExtendedStatusInfo> data = null;
    private switchesClickListener listener;
    private int layoutResourceId;
    private int previousDimmerValue;
    private ItemFilter mFilter = new ItemFilter();

    public SwitchesAdapter(Context context,
                           ArrayList<ExtendedStatusInfo> data,
                           switchesClickListener listener) {
        super();
        this.context = context;
        domoticz = new Domoticz(context);

        Collections.sort(data, new Comparator<ExtendedStatusInfo>() {
            @Override
            public int compare(ExtendedStatusInfo left, ExtendedStatusInfo right) {
                return left.getName().compareTo(right.getName());
            }
        });

        this.data = data;
        this.filteredData = data;
        this.listener = listener;
    }


    @Override
    public int getCount() {
        return filteredData.size();
    }

    @Override
    public Object getItem(int i) {
        return filteredData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }


    public Filter getFilter() {
        return mFilter;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        ExtendedStatusInfo extendedStatusInfo = filteredData.get(position);

        //if (convertView == null) {
        holder = new ViewHolder();
        convertView = setSwitchRowId(extendedStatusInfo, holder);
        convertView.setTag(holder);
        //} else holder = (ViewHolder) convertView.getTag();

        setSwitchRowData(extendedStatusInfo, holder, convertView);

        return convertView;
    }

    private View setSwitchRowId(ExtendedStatusInfo mExtendedStatusInfo, ViewHolder holder) {
        View row;
        switch (mExtendedStatusInfo.getSwitchTypeVal()) {
            case Domoticz.Device.Type.Value.ON_OFF:
            case Domoticz.Device.Type.Value.MEDIAPLAYER:
            case Domoticz.Device.Type.Value.X10SIREN:
            case Domoticz.Device.Type.Value.DOORLOCK:
                row = setOnOffSwitchRowId(holder);
                break;

            case Domoticz.Device.Type.Value.MOTION:
            case Domoticz.Device.Type.Value.CONTACT:
            case Domoticz.Device.Type.Value.DUSKSENSOR:
                row = setMotionSwitchRowId(holder);
                break;

            case Domoticz.Device.Type.Value.BLINDVENETIAN:
                row = setBlindsRowId(holder);
                break;

            case Domoticz.Device.Type.Value.PUSH_ON_BUTTON:
            case Domoticz.Device.Type.Value.SMOKE_DETECTOR:
            case Domoticz.Device.Type.Value.DOORBELL:
            case Domoticz.Device.Type.Value.PUSH_OFF_BUTTON:
                row = setPushOnOffSwitchRowId(holder);
                break;

            case Domoticz.Device.Type.Value.DIMMER:
            case Domoticz.Device.Type.Value.BLINDPERCENTAGE:
            case Domoticz.Device.Type.Value.BLINDPERCENTAGEINVERTED:
                if (mExtendedStatusInfo.getSubType().startsWith(Domoticz.Device.SubType.Name.RGB))
                    row = setDimmerRowId(holder, true);
                else
                    row = setDimmerRowId(holder, false);
                break;

            case Domoticz.Device.Type.Value.BLINDS:
            case Domoticz.Device.Type.Value.BLINDINVERTED:
                if(canHandleStopButton(mExtendedStatusInfo))
                    row = setBlindsRowId(holder);
                else
                    row = setOnOffSwitchRowId(holder);

                break;

            default:
                throw new NullPointerException(
                        "Switch type not supported in the adapter for: \n"
                                + mExtendedStatusInfo.toString());
        }
        return row;
    }

    private View setOnOffSwitchRowId(ViewHolder holder) {
        layoutResourceId = R.layout.switch_row_on_off;
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View row = inflater.inflate(layoutResourceId, null);

        holder.onOffSwitch = (Switch) row.findViewById(R.id.switch_button);
        holder.signal_level = (TextView) row.findViewById(R.id.switch_signal_level);
        holder.iconRow = (ImageView) row.findViewById(R.id.rowIcon);
        holder.switch_name = (TextView) row.findViewById(R.id.switch_name);
        holder.switch_battery_level = (TextView) row.findViewById(R.id.switch_battery_level);
        holder.buttonLog = (Button) row.findViewById(R.id.log_button);
        holder.buttonTimer = (Button) row.findViewById(R.id.timer_button);

        return row;
    }

    private View setMotionSwitchRowId(ViewHolder holder) {

        layoutResourceId = R.layout.switch_row_motion;
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View row = inflater.inflate(layoutResourceId, null);

        holder.onOffSwitch = (Switch) row.findViewById(R.id.switch_button);
        holder.signal_level = (TextView) row.findViewById(R.id.switch_signal_level);
        holder.iconRow = (ImageView) row.findViewById(R.id.rowIcon);
        holder.switch_name = (TextView) row.findViewById(R.id.switch_name);
        holder.switch_battery_level = (TextView) row.findViewById(R.id.switch_battery_level);
        holder.buttonLog = (Button) row.findViewById(R.id.log_button);
        holder.buttonTimer = (Button) row.findViewById(R.id.timer_button);

        return row;
    }

    private View setPushOnOffSwitchRowId(ViewHolder holder) {
        layoutResourceId = R.layout.switch_row_pushon;
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View row = inflater.inflate(layoutResourceId, null);

        holder.buttonOn = (Button) row.findViewById(R.id.on_button);
        holder.buttonLog = (Button) row.findViewById(R.id.log_button);
        holder.buttonTimer = (Button) row.findViewById(R.id.timer_button);

        holder.iconRow = (ImageView) row.findViewById(R.id.rowIcon);
        holder.switch_name = (TextView) row.findViewById(R.id.switch_name);
        holder.switch_battery_level = (TextView) row.findViewById(R.id.switch_battery_level);
        holder.signal_level = (TextView) row.findViewById(R.id.switch_signal_level);

        return row;
    }


    private View setBlindsRowId(ViewHolder holder) {
        layoutResourceId = R.layout.switch_row_blinds;
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View row = inflater.inflate(layoutResourceId, null);
        holder.switch_name = (TextView) row.findViewById(R.id.switch_name);
        holder.switch_status = (TextView) row.findViewById(R.id.switch_status);
        holder.switch_battery_level = (TextView) row.findViewById(R.id.switch_signal_level);
        holder.buttonUp = (ImageButton) row.findViewById(R.id.switch_button_up);
        holder.iconRow = (ImageView) row.findViewById(R.id.rowIcon);
        holder.buttonStop = (ImageButton) row.findViewById(R.id.switch_button_stop);
        holder.buttonDown = (ImageButton) row.findViewById(R.id.switch_button_down);
        return row;
    }

    private View setDimmerRowId(ViewHolder holder, boolean isRGB) {
        if (isRGB)
            layoutResourceId = R.layout.switch_row_rgb_dimmer;
        else
            layoutResourceId = R.layout.switch_row_dimmer;

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View row = inflater.inflate(layoutResourceId, null);

        holder.switch_name = (TextView) row.findViewById(R.id.switch_name);
        holder.signal_level = (TextView) row.findViewById(R.id.switch_signal_level);
        holder.iconRow = (ImageView) row.findViewById(R.id.rowIcon);
        holder.switch_battery_level = (TextView) row.findViewById(R.id.switch_battery_level);
        holder.switch_dimmer_level = (TextView) row.findViewById(R.id.switch_dimmer_level);
        holder.dimmerOnOffSwitch = (Switch) row.findViewById(R.id.switch_dimmer_switch);
        holder.dimmer = (SeekBar) row.findViewById(R.id.switch_dimmer);

        holder.buttonLog = (Button) row.findViewById(R.id.log_button);
        holder.buttonTimer = (Button) row.findViewById(R.id.timer_button);

        if (isRGB)
            holder.buttonColor = (Button) row.findViewById(R.id.color_button);
        return row;
    }

    private void setSwitchRowData(ExtendedStatusInfo mExtendedStatusInfo,
                                  ViewHolder holder,
                                  View convertView) {

        switch (mExtendedStatusInfo.getSwitchTypeVal()) {
            case Domoticz.Device.Type.Value.ON_OFF:
            case Domoticz.Device.Type.Value.MEDIAPLAYER:
            case Domoticz.Device.Type.Value.X10SIREN:
            case Domoticz.Device.Type.Value.MOTION:
            case Domoticz.Device.Type.Value.CONTACT:
            case Domoticz.Device.Type.Value.DUSKSENSOR:
            case Domoticz.Device.Type.Value.DOORLOCK:
                setOnOffSwitchRowData(mExtendedStatusInfo, holder);
                break;

            case Domoticz.Device.Type.Value.PUSH_ON_BUTTON:
            case Domoticz.Device.Type.Value.SMOKE_DETECTOR:
            case Domoticz.Device.Type.Value.DOORBELL:
                setPushOnOffSwitchRowData(mExtendedStatusInfo, holder, true);
                break;

            case Domoticz.Device.Type.Value.PUSH_OFF_BUTTON:
                setPushOnOffSwitchRowData(mExtendedStatusInfo, holder, false);
                break;

            case Domoticz.Device.Type.Value.BLINDVENETIAN:
                setBlindsRowData(mExtendedStatusInfo, holder);
                break;

            case Domoticz.Device.Type.Value.DIMMER:
            case Domoticz.Device.Type.Value.BLINDPERCENTAGE:
            case Domoticz.Device.Type.Value.BLINDPERCENTAGEINVERTED:
                if (mExtendedStatusInfo.getSubType().startsWith(Domoticz.Device.SubType.Name.RGB))
                    setDimmerRowData(mExtendedStatusInfo, holder, true);
                else
                    setDimmerRowData(mExtendedStatusInfo, holder, false);
                break;

            case Domoticz.Device.Type.Value.BLINDS:
            case Domoticz.Device.Type.Value.BLINDINVERTED:
                if(canHandleStopButton(mExtendedStatusInfo))
                    setBlindsRowData(mExtendedStatusInfo, holder);
                else
                    setOnOffSwitchRowData(mExtendedStatusInfo, holder);
                break;

            default:
                throw new NullPointerException(
                        "No supported switch type defined in the adapter (setSwitchRowData)");
        }
    }

    private boolean canHandleStopButton(ExtendedStatusInfo mExtendedStatusInfo)
    {
        if((mExtendedStatusInfo.getSubType().indexOf("RAEX") >= 0 ) ||
        (mExtendedStatusInfo.getSubType().indexOf("A-OK") >= 0)||
        (mExtendedStatusInfo.getSubType().indexOf("RollerTrol") >= 0)||
        (mExtendedStatusInfo.getSubType().indexOf("RFY") >= 0)||
        (mExtendedStatusInfo.getSubType().indexOf("ASA") >= 0)||
        (mExtendedStatusInfo.getSubType().indexOf("T6 DC") >= 0))
            return true;
        else
            return false;
    }

    private void setOnOffSwitchRowData(final ExtendedStatusInfo mExtendedStatusInfo,
                                       final ViewHolder holder) {

        holder.isProtected = mExtendedStatusInfo.isProtected();
        holder.switch_name.setText(mExtendedStatusInfo.getName());

        String text = context.getString(R.string.last_update) + ": " +
                String.valueOf(mExtendedStatusInfo.getLastUpdate().substring(mExtendedStatusInfo.getLastUpdate().indexOf(" ") + 1));
        holder.signal_level.setText(text);

        text = context.getString(R.string.data) + ": " +
                String.valueOf(mExtendedStatusInfo.getData());
        holder.switch_battery_level.setText(text);

        if (holder.isProtected) holder.onOffSwitch.setEnabled(false);

        holder.onOffSwitch.setId(mExtendedStatusInfo.getIdx());
        holder.onOffSwitch.setChecked(mExtendedStatusInfo.getStatusBoolean());

        holder.onOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                handleOnOffSwitchClick(compoundButton.getId(), checked);
                mExtendedStatusInfo.setStatusBoolean(checked);

                if (!checked)
                    holder.iconRow.setAlpha(0.5f);
                else
                    holder.iconRow.setAlpha(1f);
            }
        });

        holder.buttonLog.setId(mExtendedStatusInfo.getIdx());
        holder.buttonLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogButtonClick(v.getId());
            }
        });
        holder.buttonTimer.setId(mExtendedStatusInfo.getIdx());
        holder.buttonTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleTimerButtonClick(v.getId());
            }
        });

        if (mExtendedStatusInfo.getTimers().toLowerCase().equals("false"))
            holder.buttonTimer.setVisibility(View.INVISIBLE);

        if (!mExtendedStatusInfo.getStatusBoolean())
            holder.iconRow.setAlpha(0.5f);
        else
            holder.iconRow.setAlpha(1f);

        Picasso.with(context).load(domoticz.getDrawableIcon(mExtendedStatusInfo.getTypeImg(), mExtendedStatusInfo.getSwitchType(), mExtendedStatusInfo.getStatusBoolean())).into(holder.iconRow);
        if (!mExtendedStatusInfo.getStatusBoolean())
            holder.iconRow.setAlpha(0.5f);
        else
            holder.iconRow.setAlpha(1f);

    }

    private void setPushOnOffSwitchRowData(ExtendedStatusInfo mExtendedStatusInfo, ViewHolder holder, boolean action) {
        holder.isProtected = mExtendedStatusInfo.isProtected();
        holder.switch_name.setText(mExtendedStatusInfo.getName());

        String text = context.getString(R.string.last_update) + ": " +
                String.valueOf(mExtendedStatusInfo.getLastUpdate().substring(mExtendedStatusInfo.getLastUpdate().indexOf(" ") + 1));
        holder.signal_level.setText(text);

        text = context.getString(R.string.data) + ": " +
                String.valueOf(mExtendedStatusInfo.getData());
        holder.switch_battery_level.setText(text);

        if (holder.isProtected)
            holder.buttonOn.setEnabled(false);

        holder.buttonOn.setId(mExtendedStatusInfo.getIdx());
        if (action) {
            holder.buttonOn.setText(context.getString(R.string.button_state_on));
            holder.buttonOn.setBackground(context.getResources().getDrawable(R.drawable.button));
        } else {
            holder.buttonOn.setText(context.getString(R.string.button_state_off));
            holder.buttonOn.setBackground(context.getResources().getDrawable(R.drawable.button_off));
        }

        holder.buttonOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = (String) ((Button) v).getText();
                if (text.equals(context.getString(R.string.button_state_on)))
                    handleOnButtonClick(v.getId(), true);
                else
                    handleOnButtonClick(v.getId(), false);
            }
        });

        holder.buttonLog.setId(mExtendedStatusInfo.getIdx());
        holder.buttonLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogButtonClick(v.getId());
            }
        });
        holder.buttonTimer.setId(mExtendedStatusInfo.getIdx());
        holder.buttonTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleTimerButtonClick(v.getId());
            }
        });

        if (mExtendedStatusInfo.getTimers().toLowerCase().equals("false"))
            holder.buttonTimer.setVisibility(View.INVISIBLE);

        Picasso.with(context).load(domoticz.getDrawableIcon(mExtendedStatusInfo.getTypeImg(), mExtendedStatusInfo.getSwitchType(), mExtendedStatusInfo.getStatusBoolean())).into(holder.iconRow);
        if (!mExtendedStatusInfo.getStatusBoolean())
            holder.iconRow.setAlpha(0.5f);
        else
            holder.iconRow.setAlpha(1f);

    }

    private void setBlindsRowData(ExtendedStatusInfo mExtendedStatusInfo,
                                  ViewHolder holder) {
        holder.isProtected = mExtendedStatusInfo.isProtected();

        holder.switch_name.setText(mExtendedStatusInfo.getName());

        String text = context.getString(R.string.last_update) + ": " +
                String.valueOf(mExtendedStatusInfo.getLastUpdate().substring(mExtendedStatusInfo.getLastUpdate().indexOf(" ") + 1));
        holder.switch_status.setText(text);

        text = context.getString(R.string.data) + ": " +
                String.valueOf(mExtendedStatusInfo.getData());
        holder.switch_battery_level.setText(text);

        if (holder.isProtected) holder.buttonUp.setEnabled(false);
        holder.buttonUp.setId(mExtendedStatusInfo.getIdx());
        holder.buttonUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(ExtendedStatusInfo e : filteredData)
                {
                    if(e.getIdx() == view.getId())
                    {
                        if(e.getSwitchTypeVal() == Domoticz.Device.Type.Value.BLINDVENETIAN)
                            handleBlindsClick(e.getIdx(), Domoticz.Device.Blind.Action.OFF);
                        else if(e.getSwitchTypeVal() == Domoticz.Device.Type.Value.BLINDINVERTED)
                            handleBlindsClick(e.getIdx(), Domoticz.Device.Blind.Action.DOWN);
                        else
                            handleBlindsClick(e.getIdx(), Domoticz.Device.Blind.Action.UP);
                    }
                }
            }
        });

        if (holder.isProtected) holder.buttonStop.setEnabled(false);
        holder.buttonStop.setId(mExtendedStatusInfo.getIdx());
        holder.buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(ExtendedStatusInfo e : filteredData)
                {
                    if(e.getIdx() == view.getId())
                    {
                        handleBlindsClick(e.getIdx(), Domoticz.Device.Blind.Action.STOP);
                    }
                }
            }
        });

        if (holder.isProtected) holder.buttonDown.setEnabled(false);
        holder.buttonDown.setId(mExtendedStatusInfo.getIdx());
        holder.buttonDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(ExtendedStatusInfo e : filteredData)
                {
                    if(e.getIdx() == view.getId())
                    {
                        if(e.getSwitchTypeVal() == Domoticz.Device.Type.Value.BLINDVENETIAN)
                            handleBlindsClick(e.getIdx(), Domoticz.Device.Blind.Action.ON);
                        else if(e.getSwitchTypeVal() == Domoticz.Device.Type.Value.BLINDINVERTED)
                        handleBlindsClick(e.getIdx(), Domoticz.Device.Blind.Action.UP);
                    else
                        handleBlindsClick(e.getIdx(), Domoticz.Device.Blind.Action.DOWN);
                    }
                }
            }
        });

        Picasso.with(context).load(domoticz.getDrawableIcon(mExtendedStatusInfo.getTypeImg(), mExtendedStatusInfo.getSwitchType(), mExtendedStatusInfo.getStatusBoolean())).into(holder.iconRow);
        if (!mExtendedStatusInfo.getStatusBoolean())
            holder.iconRow.setAlpha(0.5f);
        else
            holder.iconRow.setAlpha(1f);
    }

    private void setDimmerRowData(final ExtendedStatusInfo mExtendedStatusInfo,
                                  final ViewHolder holder,
                                  final boolean isRGB) {

        holder.isProtected = mExtendedStatusInfo.isProtected();
        holder.switch_name.setText(mExtendedStatusInfo.getName());

        String text = context.getString(R.string.last_update) + ": " +
                String.valueOf(mExtendedStatusInfo.getLastUpdate().substring(mExtendedStatusInfo.getLastUpdate().indexOf(" ") + 1));
        holder.signal_level.setText(text);

        text = context.getString(R.string.data) + ": " +
                String.valueOf(mExtendedStatusInfo.getData());
        holder.switch_battery_level.setText(text);

        holder.switch_dimmer_level.setId(mExtendedStatusInfo.getIdx() + ID_TEXTVIEW);
        String percentage = calculateDimPercentage(
                mExtendedStatusInfo.getMaxDimLevel(), mExtendedStatusInfo.getLevel());
        holder.switch_dimmer_level.setText(percentage);

        holder.dimmerOnOffSwitch.setId(mExtendedStatusInfo.getIdx() + ID_SWITCH);
        if (holder.isProtected) holder.dimmerOnOffSwitch.setEnabled(false);

        holder.dimmerOnOffSwitch.setChecked(mExtendedStatusInfo.getStatusBoolean());
        holder.dimmerOnOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                handleOnOffSwitchClick(compoundButton.getId(), checked);
                mExtendedStatusInfo.setStatusBoolean(checked);
                if (checked) {
                    holder.switch_dimmer_level.setVisibility(View.VISIBLE);
                    holder.dimmer.setVisibility(View.VISIBLE);
                    if (holder.dimmer.getProgress() <= 10) {
                        holder.dimmer.setProgress(20);//dimmer turned on with default progress value
                    }
                    if (isRGB)
                        holder.buttonColor.setVisibility(View.VISIBLE);
                } else {
                    holder.switch_dimmer_level.setVisibility(View.GONE);
                    holder.dimmer.setVisibility(View.GONE);
                    if (isRGB)
                        holder.buttonColor.setVisibility(View.GONE);
                }
                if (!checked)
                    holder.iconRow.setAlpha(0.5f);
                else
                    holder.iconRow.setAlpha(1f);
            }
        });

        if (holder.isProtected)
            holder.dimmer.setEnabled(false);

        holder.dimmer.setProgress(mExtendedStatusInfo.getLevel());
        holder.dimmer.setMax(mExtendedStatusInfo.getMaxDimLevel());
        holder.dimmer.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String percentage = calculateDimPercentage(seekBar.getMax(), progress);
                TextView switch_dimmer_level = (TextView) seekBar.getRootView()
                        .findViewById(mExtendedStatusInfo.getIdx() + ID_TEXTVIEW);
                switch_dimmer_level.setText(percentage);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                previousDimmerValue = seekBar.getProgress();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                Switch dimmerOnOffSwitch = (Switch) seekBar.getRootView()
                        .findViewById(mExtendedStatusInfo.getIdx() + ID_SWITCH);

                if (progress == 0 && dimmerOnOffSwitch.isChecked()) {
                    dimmerOnOffSwitch.setChecked(false);
                    seekBar.setProgress(previousDimmerValue);
                } else if (progress > 0 && !dimmerOnOffSwitch.isChecked()) {
                    dimmerOnOffSwitch.setChecked(true);
                }

                handleDimmerChange(mExtendedStatusInfo.getIdx(), progress + 1);
                mExtendedStatusInfo.setLevel(progress);
            }
        });

        if (!mExtendedStatusInfo.getStatusBoolean()) {
            holder.switch_dimmer_level.setVisibility(View.GONE);
            holder.dimmer.setVisibility(View.GONE);
            if (isRGB)
                holder.buttonColor.setVisibility(View.GONE);
        } else {
            holder.switch_dimmer_level.setVisibility(View.VISIBLE);
            holder.dimmer.setVisibility(View.VISIBLE);
            if (isRGB)
                holder.buttonColor.setVisibility(View.VISIBLE);
        }

        holder.buttonLog.setId(mExtendedStatusInfo.getIdx());
        holder.buttonLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogButtonClick(v.getId());
            }
        });

        holder.buttonTimer.setId(mExtendedStatusInfo.getIdx());
        holder.buttonTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleTimerButtonClick(v.getId());
            }
        });

        if (mExtendedStatusInfo.getTimers().toLowerCase().equals("false"))
            holder.buttonTimer.setVisibility(View.INVISIBLE);

        if (isRGB) {
            holder.buttonColor.setId(mExtendedStatusInfo.getIdx());
            holder.buttonColor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleColorButtonClick(v.getId());
                }
            });
        }

        Picasso.with(context).load(domoticz.getDrawableIcon(mExtendedStatusInfo.getTypeImg(), mExtendedStatusInfo.getSubType(), mExtendedStatusInfo.getStatusBoolean())).into(holder.iconRow);
        if (!mExtendedStatusInfo.getStatusBoolean())
            holder.iconRow.setAlpha(0.5f);
        else
            holder.iconRow.setAlpha(1f);
    }

    private String calculateDimPercentage(int maxDimLevel, int level) {
        float percentage = ((float) level / (float) maxDimLevel) * 100;
        return String.format("%.0f", percentage) + "%";
    }

    private void handleOnOffSwitchClick(int idx, boolean action) {
        listener.onSwitchClick(idx, action);
    }

    private void handleOnButtonClick(int idx, boolean action) {
        listener.onButtonClick(idx, action);
    }

    private void handleLogButtonClick(int idx) {
        listener.onLogButtonClick(idx);
    }

    private void handleColorButtonClick(int idx) {
        listener.onColorButtonClick(idx);
    }

    private void handleTimerButtonClick(int idx) {
        listener.onTimerButtonClick(idx);
    }

    private void handleBlindsClick(int idx, int action) {
        listener.onBlindClick(idx, action);
    }

    private void handleDimmerChange(final int idx, final int value) {
        listener.onDimmerChange(idx, value);
    }

    static class ViewHolder {
        TextView switch_name, signal_level, switch_status, switch_battery_level, switch_dimmer_level;
        Switch onOffSwitch, dimmerOnOffSwitch;
        ImageButton buttonUp, buttonDown, buttonStop;
        Button buttonOn, buttonLog, buttonTimer, buttonColor;
        Boolean isProtected;
        ImageView iconRow;
        SeekBar dimmer;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();

            final ArrayList<ExtendedStatusInfo> list = data;

            int count = list.size();
            final ArrayList<ExtendedStatusInfo> nlist = new ArrayList<ExtendedStatusInfo>(count);

            ExtendedStatusInfo filterableObject;

            for (int i = 0; i < count; i++) {
                filterableObject = list.get(i);
                if (filterableObject.getName().toLowerCase().contains(filterString)) {
                    nlist.add(filterableObject);
                }
            }

            results.values = nlist;
            results.count = nlist.size();

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredData = (ArrayList<ExtendedStatusInfo>) results.values;
            notifyDataSetChanged();
        }
    }
}