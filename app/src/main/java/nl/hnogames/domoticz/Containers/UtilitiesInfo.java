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

package nl.hnogames.domoticz.Containers;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

public class UtilitiesInfo {

    private final boolean isProtected;
    JSONObject jsonObject;

    int idx;
    String Name;
    String Data;
    String LastUpdate;
    double setPoint;
    String Type;
    String SubType;
    int Favorite;
    int HardwareID;
    String HardwareName;
    String TypeImg;
    String CounterToday;
    int signalLevel;

    public UtilitiesInfo(JSONObject row) throws JSONException {
        this.jsonObject = row;

        if (row.has("Favorite"))
            Favorite = row.getInt("Favorite");
        if (row.has("TypeImg"))
            TypeImg = row.getString("TypeImg");
        isProtected = row.getBoolean("Protected");
        if (row.has("HardwareID"))
            HardwareID = row.getInt("HardwareID");
        if (row.has("HardwareName"))
            HardwareName = row.getString("HardwareName");
        if (row.has("LastUpdate"))
            LastUpdate = row.getString("LastUpdate");
        if (row.has("SetPoint"))
            setPoint = row.getLong("SetPoint");
        if (row.has("Name"))
            Name = row.getString("Name");
        if (row.has("Data"))
            Data = row.getString("Data");
        if (row.has("Type"))
            Type = row.getString("Type");
        if (row.has("CounterToday"))
            CounterToday = row.getString("CounterToday");
        if (row.has("SubType"))
            SubType = row.getString("SubType");
        idx = row.getInt("idx");

        if (row.has("SignalLevel")) {
            try {
                signalLevel = row.getInt("SignalLevel");
            } catch (Exception ex) {
                signalLevel = 0;
            }
        }
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                new Gson().toJson(this) +
                '}';
    }

    public int getIdx() {
        return idx;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    public boolean isProtected() {
        return isProtected;
    }

    public int getSignalLevel() {
        return signalLevel;
    }

    public void setSignalLevel(int signalLevel) {
        this.signalLevel = signalLevel;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getCounterToday() {
        return CounterToday;
    }

    public String getTypeImg() {
        return TypeImg;
    }

    public String getData() {
        return Data;
    }

    public String getHardwareName() {
        return HardwareName;
    }

    public double getSetPoint() {
        return setPoint;
    }

    public void setSetPoint(double setPoint) {
        this.setPoint = setPoint;
    }

    public int getFavorite() {
        return Favorite;
    }

    public void setFavorite(int favorite) {
        Favorite = favorite;
    }

    public boolean getFavoriteBoolean() {
        boolean favorite = false;
        if (this.Favorite == 1) favorite = true;
        return favorite;
    }

    public void setFavoriteBoolean(boolean favorite) {
        if (favorite) this.Favorite = 1;
        else this.Favorite = 0;
    }

    public int getHardwareID() {
        return HardwareID;
    }

    public void setHardwareID(int hardwareID) {
        HardwareID = hardwareID;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public String getSubType() {
        return SubType;
    }

    public String getLastUpdate() {
        return LastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        LastUpdate = lastUpdate;
    }

    public JSONObject getJsonObject() {
        return this.jsonObject;
    }
}