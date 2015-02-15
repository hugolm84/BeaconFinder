package af.beaconfinder.Fragment;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;

import af.beaconfinder.ScanInfo.ScanItem;
import af.beaconfinder.Service.BluetoothScannerService;

/**
 * Created by hugo on 13/02/15.
 */
abstract class BluetoothScannerFragment extends BaseFragment {

    protected BluetoothScannerService mService;

    private ServiceConnection mScannerConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            BluetoothScannerService.BluetoothScannerBinder binder = (BluetoothScannerService.BluetoothScannerBinder) service;
            mService = binder.getService();
            Log.d(TAG, "Connected Service!");
        }

        public void onServiceDisconnected(ComponentName arg0) {
            Log.d(TAG, "DisConnected Service!");
        }

    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(getActivity(), BluetoothScannerService.class);
        getActivity().bindService(intent, mScannerConnection, Context.BIND_AUTO_CREATE);
        getActivity().registerReceiver(mReceiver, new IntentFilter(BluetoothScannerService.TAG_PARCEL));
        getActivity().startService(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(mReceiver, new IntentFilter(BluetoothScannerService.TAG_PARCEL));
    }

    @Override
    public void onDetach() {
        super.onDetach();
        getActivity().unregisterReceiver(mReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unbindService(mScannerConnection);
    }

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

    abstract void handleScannedItems(final ArrayList<ScanItem> items);

}
