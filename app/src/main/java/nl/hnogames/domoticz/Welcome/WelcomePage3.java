package nl.hnogames.domoticz.Welcome;

import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;

import com.marvinlabs.widget.floatinglabel.edittext.FloatingLabelEditText;

import java.util.ArrayList;
import java.util.Set;

import nl.hnogames.domoticz.Domoticz.Domoticz;
import nl.hnogames.domoticz.Interfaces.WifiSSIDListener;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.UI.MultiSelectionSpinner;
import nl.hnogames.domoticz.Utils.PermissionsUtil;
import nl.hnogames.domoticz.Utils.PhoneConnectionUtil;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticz.Utils.UsefulBits;

public class WelcomePage3 extends Fragment {

    private static final String INSTANCE = "INSTANCE";
    private static final int WELCOME_WIZARD = 1;
    private static final int SETTINGS = 2;
    private SharedPrefUtil mSharedPrefs;
    private FloatingLabelEditText remote_server_input, remote_port_input,
            remote_username_input, remote_password_input,
            local_server_input, local_password_input,
            local_username_input, local_port_input;
    private Spinner remote_protocol_spinner, local_protocol_spinner, startScreen_spinner;
    private Switch localServer_switch;
    private int remoteProtocolSelectedPosition, localProtocolSelectedPosition, startScreenSelectedPosition;
    private View v;
    private boolean hasBeenVisibleToUser = false;
    private MultiSelectionSpinner local_wifi_spinner;
    private int callingInstance;
    private PhoneConnectionUtil mPhoneConnectionUtil;

    public static WelcomePage3 newInstance(int instance) {
        WelcomePage3 f = new WelcomePage3();

        Bundle bdl = new Bundle(1);
        bdl.putInt(INSTANCE, instance);
        f.setArguments(bdl);

        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        try {
            callingInstance = getArguments().getInt(INSTANCE);
        } catch (Exception e) {
            callingInstance = WELCOME_WIZARD;
        }

        v = inflater.inflate(R.layout.fragment_welcome3, container, false);

        mSharedPrefs = new SharedPrefUtil(getActivity());

        getLayoutReferences();
        setPreferenceValues();

        return v;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (!isVisibleToUser) {
            if (hasBeenVisibleToUser) writePreferenceValues();
        } else hasBeenVisibleToUser = true;
    }

    private void getLayoutReferences() {

        remote_server_input = (FloatingLabelEditText) v.findViewById(R.id.remote_server_input);
        remote_port_input = (FloatingLabelEditText) v.findViewById(R.id.remote_port_input);
        remote_username_input = (FloatingLabelEditText) v.findViewById(R.id.remote_username_input);
        remote_password_input = (FloatingLabelEditText) v.findViewById(R.id.remote_password_input);
        remote_protocol_spinner = (Spinner) v.findViewById(R.id.remote_protocol_spinner);
        local_server_input = (FloatingLabelEditText) v.findViewById(R.id.local_server_input);
        local_port_input = (FloatingLabelEditText) v.findViewById(R.id.local_port_input);
        local_username_input = (FloatingLabelEditText) v.findViewById(R.id.local_username_input);
        local_password_input = (FloatingLabelEditText) v.findViewById(R.id.local_password_input);
        local_protocol_spinner = (Spinner) v.findViewById(R.id.local_protocol_spinner);
        local_wifi_spinner = (MultiSelectionSpinner) v.findViewById(R.id.local_wifi);

        startScreen_spinner = (Spinner) v.findViewById(R.id.startScreen_spinner);

        if (callingInstance == SETTINGS) {
            // Hide these settings if being called by settings (instead of welcome wizard)
            startScreen_spinner.setVisibility(View.GONE);
            v.findViewById(R.id.startScreen_title).setVisibility(View.GONE);
            v.findViewById(R.id.server_settings_title).setVisibility(View.GONE);
        }

        final LinearLayout local_server_settings = (LinearLayout)
                v.findViewById(R.id.local_server_settings);
        localServer_switch = (Switch) v.findViewById(R.id.localServer_switch);
        localServer_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked) local_server_settings.setVisibility(View.VISIBLE);
                else local_server_settings.setVisibility(View.GONE);
            }
        });

    }

    private void setPreferenceValues() {
        remote_username_input.setInputWidgetText(mSharedPrefs.getDomoticzRemoteUsername());
        remote_password_input.setInputWidgetText(mSharedPrefs.getDomoticzRemotePassword());
        remote_server_input.setInputWidgetText(mSharedPrefs.getDomoticzRemoteUrl());
        remote_port_input.setInputWidgetText(mSharedPrefs.getDomoticzRemotePort());

        localServer_switch.setChecked(mSharedPrefs.isLocalServerAddressDifferent());

        local_username_input.setInputWidgetText(mSharedPrefs.getDomoticzLocalUsername());
        local_password_input.setInputWidgetText(mSharedPrefs.getDomoticzLocalPassword());
        local_server_input.setInputWidgetText(mSharedPrefs.getDomoticzLocalUrl());
        local_port_input.setInputWidgetText(mSharedPrefs.getDomoticzLocalPort());

        setProtocol_spinner();
        setStartScreen_spinner();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!PermissionsUtil.canAccessLocation(getActivity())) {
                requestPermissions(PermissionsUtil.INITIAL_LOCATION_PERMS, PermissionsUtil.INITIAL_LOCATION_REQUEST);
            } else
                setSsid_spinner();
        } else
            setSsid_spinner();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PermissionsUtil.INITIAL_LOCATION_REQUEST:
                if (PermissionsUtil.canAccessLocation(getActivity())) {
                    setSsid_spinner();
                } else {
                     if(mPhoneConnectionUtil!=null)
                         mPhoneConnectionUtil.stopReceiver();

                    ((WelcomeViewActivity) getActivity()).finishWithResult(false);
                }break;
        }
    }

    private void setSsid_spinner() {
        Set<String> ssidFromPrefs = mSharedPrefs.getLocalSsid();
        final ArrayList<String> ssidListFromPrefs = new ArrayList<>();
        //noinspection SpellCheckingInspection
        final ArrayList<String> ssids = new ArrayList<>();

        if (ssidFromPrefs != null) {
            if (ssidFromPrefs.size() > 0) {
                for (String wifi : ssidFromPrefs) {
                    ssids.add(wifi);
                    ssidListFromPrefs.add(wifi);
                }
            }
        }

        mPhoneConnectionUtil = new PhoneConnectionUtil(getActivity(), new WifiSSIDListener() {
            @Override
            public void ReceiveSSIDs(CharSequence[] ssidFound) {
                if (ssidFound == null || ssidFound.length < 1) {
                    // No wifi ssid nearby found!
                    local_wifi_spinner.setEnabled(false);                       // Disable spinner
                    ssids.add(getString(R.string.welcome_msg_no_ssid_found));
                    // Set selection to the 'no ssids found' message to inform user
                    local_wifi_spinner.setItems(ssids);
                    local_wifi_spinner.setSelection(0);
                } else {
                    for (CharSequence ssid : ssidFound) {
                        if (!UsefulBits.isEmpty(ssid) && !ssids.contains(ssid))
                            ssids.add(ssid.toString());  // Prevent double SSID's
                    }
                    local_wifi_spinner.setTitle(R.string.welcome_ssid_spinner_prompt);
                    local_wifi_spinner.setItems(ssids);

                    local_wifi_spinner.setSelection(ssidListFromPrefs);
                }
                mPhoneConnectionUtil.stopReceiver();
            }
        });
        mPhoneConnectionUtil.startSsidScan();
    }

    private void setProtocol_spinner() {
        String[] protocols = getResources().getStringArray(R.array.remote_server_protocols);
        ArrayAdapter<String> protocolAdapter
                = new ArrayAdapter<>(getActivity(), R.layout.spinner_list_item, protocols);
        remote_protocol_spinner.setAdapter(protocolAdapter);
        remote_protocol_spinner.setSelection(getPrefsDomoticzRemoteSecureIndex());
        remote_protocol_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView,
                                       View view, int position, long id) {
                remoteProtocolSelectedPosition = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        local_protocol_spinner.setAdapter(protocolAdapter);
        local_protocol_spinner.setSelection(getPrefsDomoticzLocalSecureIndex());
        local_protocol_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView,
                                       View view, int position, long id) {
                localProtocolSelectedPosition = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    private void setStartScreen_spinner() {
        String[] startScreens = getResources().getStringArray(R.array.drawer_actions);
        ArrayAdapter<String> startScreenAdapter
                = new ArrayAdapter<>(getActivity(), R.layout.spinner_list_item, startScreens);
        startScreen_spinner.setAdapter(startScreenAdapter);
        startScreen_spinner.setSelection(mSharedPrefs.getStartupScreenIndex());
        startScreen_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView,
                                       View view, int position, long id) {
                startScreenSelectedPosition = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    private void writePreferenceValues() {

        mSharedPrefs.setDomoticzRemoteUsername(
                remote_username_input.getInputWidgetText().toString());
        mSharedPrefs.setDomoticzRemotePassword(
                remote_password_input.getInputWidgetText().toString());
        mSharedPrefs.setDomoticzRemoteUrl(
                remote_server_input.getInputWidgetText().toString());
        mSharedPrefs.setDomoticzRemotePort(
                remote_port_input.getInputWidgetText().toString());
        mSharedPrefs.setDomoticzRemoteSecure(
                getSpinnerDomoticzRemoteSecureBoolean());
        if (callingInstance == WELCOME_WIZARD)
            mSharedPrefs.setStartupScreenIndex(startScreenSelectedPosition);

        Switch useSameAddress = (Switch) v.findViewById(R.id.localServer_switch);
        if (!useSameAddress.isChecked()) {
            mSharedPrefs.setLocalSameAddressAsRemote();
            mSharedPrefs.setLocalServerUsesSameAddress(false);
        } else {
            mSharedPrefs.setDomoticzLocalUsername(
                    local_username_input.getInputWidgetText().toString());
            mSharedPrefs.setDomoticzLocalPassword(
                    local_password_input.getInputWidgetText().toString());
            mSharedPrefs.setDomoticzLocalUrl(
                    local_server_input.getInputWidgetText().toString());
            mSharedPrefs.setDomoticzLocalPort(
                    local_port_input.getInputWidgetText().toString());
            mSharedPrefs.setDomoticzLocalSecure(
                    getSpinnerDomoticzLocalSecureBoolean());
            mSharedPrefs.setLocalServerUsesSameAddress(true);
        }

        mSharedPrefs.setLocalSsid(local_wifi_spinner.getSelectedStrings());

    }

    private boolean getSpinnerDomoticzRemoteSecureBoolean() {
        String[] protocols = getResources().getStringArray(R.array.remote_server_protocols);
        return protocols[remoteProtocolSelectedPosition].equalsIgnoreCase(Domoticz.Protocol.SECURE);
    }

    private boolean getSpinnerDomoticzLocalSecureBoolean() {
        String[] protocols = getResources().getStringArray(R.array.remote_server_protocols);
        return protocols[localProtocolSelectedPosition].equalsIgnoreCase(Domoticz.Protocol.SECURE);
    }

    private int getPrefsDomoticzRemoteSecureIndex() {

        boolean isSecure = mSharedPrefs.isDomoticzRemoteSecure();
        String[] protocols = getResources().getStringArray(R.array.remote_server_protocols);
        int i = 0;
        String protocolString;

        if (isSecure) protocolString = Domoticz.Protocol.SECURE;
        else protocolString = Domoticz.Protocol.INSECURE;

        for (String protocol : protocols) {
            if (protocol.equalsIgnoreCase(protocolString)) return i;
            i++;
        }
        return i;
    }

    private int getPrefsDomoticzLocalSecureIndex() {
        boolean isSecure = mSharedPrefs.isDomoticzLocalSecure();
        String[] protocols = getResources().getStringArray(R.array.remote_server_protocols);
        int i = 0;
        String protocolString;

        if (isSecure) protocolString = Domoticz.Protocol.SECURE;
        else protocolString = Domoticz.Protocol.INSECURE;

        for (String protocol : protocols) {
            if (protocol.equalsIgnoreCase(protocolString)) return i;
            i++;
        }
        return i;
    }

    @Override
    public void onStop()
    {
        if(mPhoneConnectionUtil!=null)
            mPhoneConnectionUtil.stopReceiver();

        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (callingInstance == SETTINGS) {

            writePreferenceValues();   // Only when used by settings
        }
    }
}