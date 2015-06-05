package af.beaconfinder.Service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import java.util.ArrayList;
import java.util.InvalidPropertiesFormatException;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

import af.beaconfinder.Beacon.BeaconFilter;
import af.beaconfinder.Beacon.BeaconPDUParser;
import af.beaconfinder.ScanInfo.ScanItem;

public class BluetoothScannerService extends Service implements BluetoothAdapter.LeScanCallback {

    private static final String TAG             = "BluetoothScannerService";
    public  static final String TAG_PARCEL      = "ScanItemArrayList";
    // @HACK I have not figured out yet how to set Bluetooth Type in CSR code. This checks for
    // vendor, 00025B	Cambridge Silicon
    private static final String CAMBRIDGE_TAG   = "00:02:5B:00";
    private static final Long   INTERVAL        = 3000l;

    private final IBinder mBinder = new BluetoothScannerBinder();
    private final BeaconPDUParser mParser = new BeaconPDUParser();

    /**
     * Scan history and message handler
     */
    private static int BUFFER_SIZE = 100;
    private final Handler mHandler = new Handler();
    private final static ArrayList<ScanItem> mScanResults = new ArrayList<>();
    private final static ConcurrentHashMap<String, BlockingDeque<Integer>> mScanHistory = new ConcurrentHashMap<>();

    private boolean mHasLeSupport = true;
    private boolean scanningActive = false;
    private BluetoothAdapter mBluetoothAdapter;
    private Intent parcelIntent = new Intent(TAG_PARCEL);

    public class BluetoothScannerBinder extends Binder {
        public BluetoothScannerService getService() {
            return BluetoothScannerService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        /**
         * Bluetooth is disabled, ask user to enable it
         */
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            enableBtIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(enableBtIntent);
        }

        /*
         * Check for Bluetooth LE Support.
         */
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "No LE Support.", Toast.LENGTH_LONG).show();
            mHasLeSupport = false;
            return START_STICKY;
        }

        return START_STICKY;
    }

    public void pause() {
        Log.d(TAG, "Pausing scanning");
        this.scanningActive = false;
    }

    public void resume() {
        Log.d(TAG, "Resuming scanning");
        this.scanningActive = true;
        startLeScan();
        stopScanningCycle();
    }

    /**
     * Only start LE scan if activated and device supports it
     */
    private void startLeScan() {
        if(isScanningActive())
            mBluetoothAdapter.startLeScan(BluetoothScannerService.this);
    }
    /**
     * Scanning for INTERVAL, then IDLES for INTERVAL
     * Cyclic
     */
    private void startScanningCycle() {
        this.mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!mHasLeSupport)
                    return;
                if (scanningActive) {
                    mBluetoothAdapter.startLeScan(BluetoothScannerService.this);
                    stopScanningCycle();
                } else {
                    mBluetoothAdapter.stopLeScan(BluetoothScannerService.this);
                }
            }
        }, INTERVAL);
    }

    /**
     * This will stop the leScan and restart it if still active
     */
    private void stopScanningCycle() {
        this.mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!mHasLeSupport)
                    return;
                mBluetoothAdapter.stopLeScan(BluetoothScannerService.this);
                if (scanningActive) {
                    Log.d(TAG, "Scheduling a new scancycle");
                    startScanningCycle();
                }
                finishScanningCycle();
            }
        }, INTERVAL);
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] data) {
        /**
         * Depending on beacon advert frequency, we get multiple callbacks from many devices
         * Check that this isn't a device we have already seen, and add it to the list.
         */
        if (!mScanHistory.containsKey(device.getAddress())) {
            mScanHistory.put(device.getAddress(), new LinkedBlockingDeque<Integer>());

            if (device.getType() == BluetoothDevice.DEVICE_TYPE_LE
                    || device.getAddress().startsWith(CAMBRIDGE_TAG)) {
                try {

                    ScanItem item = mParser.handleFoundDevice(device, rssi, data);
                    mScanResults.add(item);
                    mScanHistory.get(device.getAddress()).add(rssi);
                } catch (InvalidPropertiesFormatException e) {
                    Log.d(TAG, "Skipping BLE device, not an iBeacon" + device.toString());
                }
            }
        }
        // Else, add the device to the history, pop first if history gets to big
        // This will let us do some normalization of the values on each scan cycle
        else {
               if (mScanHistory.get(device.getAddress()).size() > BUFFER_SIZE)
                    mScanHistory.get(device.getAddress()).removeFirst();
                mScanHistory.get(device.getAddress()).add(rssi);
        }
    }

    public final boolean isScanningActive() {
        return (mHasLeSupport && scanningActive);
    }


    private void finishScanningCycle() {
        for (Map.Entry<String, BlockingDeque<Integer>> entry : mScanHistory.entrySet()) {
            final BlockingDeque<Integer> values = entry.getValue();


            if (values.size() < BUFFER_SIZE) {
                Log.d(TAG, entry.getKey() + ": Sorry, only got " + values.size() + " out of " + BUFFER_SIZE);
            }

            DescriptiveStatistics stats = BeaconFilter.statistics(values);

            synchronized (mScanResults) {
                for (ScanItem info : mScanResults) {
                    if (info.getMacAddress().equalsIgnoreCase((entry.getKey()))) {
                        info.setRssi((int) stats.getMean());
                        Log.d(TAG, entry.getKey() + ": Calculated mean of RSSI to " + info.getRssi());
                        break;
                    }
                }
            }
            mScanHistory.get(entry.getKey()).clear();
        }
        parcelIntent.putParcelableArrayListExtra(BluetoothScannerService.TAG_PARCEL, mScanResults);
        sendBroadcast(parcelIntent);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "SERVICE DESTROYED");
        this.scanningActive = false;

        if (this.mBluetoothAdapter != null && mHasLeSupport) {
            this.mBluetoothAdapter.stopLeScan(this);
        }
        super.onDestroy();
    }
}
