package nl.hnogames.domoticz.UI;

import android.content.Context;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.maps.model.LatLng;
import com.marvinlabs.widget.floatinglabel.edittext.FloatingLabelEditText;

import java.util.List;
import java.util.Random;

import nl.hnogames.domoticz.Containers.LocationInfo;
import nl.hnogames.domoticz.R;

public class LocationDialog implements DialogInterface.OnDismissListener {

    private final MaterialDialog.Builder mdb;
    private Context mContext;
    private LatLng foundLocation;
    private FloatingLabelEditText editAddress;
    private FloatingLabelEditText editName;
    private EditText txtLatitude;
    private EditText txtLongitude;

    private DismissListener dismissListener;

    public LocationDialog(final Context mContext, int layout) {
        this.mContext = mContext;

        mdb = new MaterialDialog.Builder(mContext);
        boolean wrapInScrollView = true;
        //noinspection ConstantConditions
        mdb.customView(layout, wrapInScrollView)
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel);

        mdb.dismissListener(this);
        mdb.onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                String name = editName.getInputWidgetText().toString();

                if (name.isEmpty() || name.length() <= 0)
                    Toast.makeText(mContext, R.string.location_name, Toast.LENGTH_SHORT).show();
                else if (foundLocation == null)
                    Toast.makeText(mContext, R.string.location_not_found, Toast.LENGTH_SHORT).show();
                else {
                    try {
                        if (dismissListener != null)
                            dismissListener.onDismiss(new LocationInfo(new Random().nextInt(999999), editName.getInputWidgetText().toString(),
                                    new LatLng(Double.parseDouble(txtLatitude.getText().toString().replace(mContext.getString(R.string.latitude) + ": ", "")),
                                            Double.parseDouble(txtLongitude.getText().toString().replace(mContext.getString(R.string.longitude) + ": ", "")))));

                    } catch(Exception ex){
                        if (dismissListener != null)
                            dismissListener.onDismiss(new LocationInfo(new Random().nextInt(999999), editName.getInputWidgetText().toString(), foundLocation));
                    }
                }
            }
        });
    }

    public void show() {
        mdb.title(mContext.getString(R.string.title_add_location));
        MaterialDialog md = mdb.build();
        View view = md.getCustomView();

        Button getLocation = (Button) view.findViewById(R.id.get_address);
        txtLatitude = (EditText)view.findViewById(R.id.latitude);
        txtLongitude = (EditText)view.findViewById(R.id.longitude);
        editAddress = (FloatingLabelEditText)view.findViewById(R.id.address);
        editName = (FloatingLabelEditText)view.findViewById(R.id.name);

        getLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                foundLocation = getLocationFromAddress(mContext, String.valueOf(editAddress.getInputWidgetText().toString()));
                if (foundLocation == null)
                    Toast.makeText(mContext, R.string.could_not_find_location, Toast.LENGTH_SHORT).show();
                else {
                    txtLatitude.setText(mContext.getString(R.string.latitude) + ": " + foundLocation.latitude);
                    txtLongitude.setText(mContext.getString(R.string.longitude) + ": " + foundLocation.longitude);
                }
            }
        });

        md.show();
    }

    public LatLng getLocationFromAddress(Context context,String strAddress) {
        Geocoder coder;
        coder = new Geocoder(context);
        List<Address> address;
        LatLng p1 = null;

        try {
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }
            Address location = address.get(0);
            location.getLatitude();
            location.getLongitude();

            p1 = new LatLng(location.getLatitude(), location.getLongitude() );
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return p1;
    }


    @Override
    public void onDismiss(DialogInterface dialog) {
        if (dismissListener != null)
            dismissListener.onDismissEmpty();
    }

    public void onDismissListener(DismissListener dismissListener) {
        this.dismissListener = dismissListener;
    }

    public interface DismissListener {
        void onDismiss(LocationInfo location);
        void onDismissEmpty();
    }
}