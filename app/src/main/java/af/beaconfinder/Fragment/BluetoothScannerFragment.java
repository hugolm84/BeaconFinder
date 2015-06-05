package af.beaconfinder.Fragment;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.ListIterator;

import af.beaconfinder.Beacon.BeaconFilter;
import af.beaconfinder.MainActivity;
import af.beaconfinder.R;
import af.beaconfinder.ScanInfo.ScanItem;
import af.beaconfinder.Service.BluetoothScannerService;

abstract class BluetoothScannerFragment extends BaseFragment implements View.OnClickListener {

    private static final String TAG = "BluetoothFragment";

    // Hook up to our bluetooth scanner service
    protected BluetoothScannerService mService;

    private ServiceConnection mScannerConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = ((BluetoothScannerService.BluetoothScannerBinder)service).getService();
            BluetoothScannerFragment.this.onServiceConnected();
        }

        public void onServiceDisconnected(ComponentName arg0) {
            Log.d(TAG, "DisConnected Service!");
            BluetoothScannerFragment.this.onServiceDisconnected();
        }
    };

    // Receiver for results from bluetooth scanner
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if(bundle != null) {
                ArrayList<ScanItem> items = intent.getExtras().getParcelableArrayList(BluetoothScannerService.TAG_PARCEL);
                handleScannedItems(items);
            }
        }
    };

    /**
     * Implement this and you will recieve the list of scanned items
     * @param items
     */
    abstract void handleScannedItems(final ArrayList<ScanItem> items);

    /**
     * When service dis/connects
     */
    abstract void onServiceConnected();
    abstract void onServiceDisconnected();

    /**
     * Convenience method for sending position updates to socket
     * @param id
     * @param distance
     */
    protected void sendPosition(final String id, final Double distance) {
        Log.d(TAG, "Sending nearest position " + id + " distance:" + distance + "m");
        ((MainActivity)getActivity()).ioService().updatePosition(id, String.valueOf(distance));
    }

    /**
     * Calculates nearest beacon and sends message to socket
     * @param items
     */
    protected void sendNearestPosition(final ArrayList<ScanItem> items) {
        if(items.isEmpty()) {
            Log.d(TAG, "Cannot send nearest position, collection is empty");
            return;
        }
        ScanItem min = Collections.min(items, new Comparator<ScanItem>() {
            public int compare(ScanItem a, ScanItem b) {
                return new Double(BeaconFilter.convertDistance(a)).compareTo(new Double(BeaconFilter.convertDistance(b)));
            }
        });

        sendPosition(min.getMacAddress(), BeaconFilter.convertDistance(min));
    }

    /**
     * When created, bind, register and start the bluetooth service
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(getActivity(), BluetoothScannerService.class);
        getActivity().bindService(intent, mScannerConnection, Context.BIND_AUTO_CREATE);
        getActivity().registerReceiver(mReceiver, new IntentFilter(BluetoothScannerService.TAG_PARCEL));
        getActivity().startService(intent);
    }

    /**
     * Remember to re-register the receiver on resume
     */
    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(mReceiver, new IntentFilter(BluetoothScannerService.TAG_PARCEL));
    }

    /**
     * Remember to unregister
     */
    @Override
    public void onDetach() {
        super.onDetach();
        getActivity().unregisterReceiver(mReceiver);
    }

    /**
     * Destroy unbinds the service,
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unbindService(mScannerConnection);
    }

    /**
     * If there's a button for scan on/off change the text on it
     * @param v
     */
    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.scanButton) {
            if(mService.isScanningActive()) {
                mService.pause();

            } else {
                mService.resume();
            }
            ((Button)v).setText( mService.isScanningActive() ? "Stop" : "Start");
        }
    }
}
