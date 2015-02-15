package af.beaconfinder.Service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

import af.beaconfinder.ScanInfo.ScanItem;

public class BluetoothScannerService extends Service implements BluetoothAdapter.LeScanCallback {

    private static final String TAG = "BluetoothScannerService";
    public static final String TAG_PARCEL = TAG+"ScanItemArrayList";
    public static final String TAG_PARCEL_POSITION = TAG+"ScanItemCalcuatedPositionArray";


    final List<double[]> positions = new ArrayList<>();

    private final IBinder mBinder = new BluetoothScannerBinder();

    public class BluetoothScannerBinder extends Binder {
        public BluetoothScannerService getService() {
            return BluetoothScannerService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private BeaconPDUParser mParser = new BeaconPDUParser();
    private BluetoothAdapter mBluetoothAdapter;
    private volatile boolean scanningActive = false;
    private Intent parcelIntent = new Intent(TAG_PARCEL);

    /**
     * Handle messages on the main thread
     */
    private final Handler mHandler = new Handler();

    private static int BUFFER_SIZE = 50;
    private final static ArrayList<ScanItem> mScanResults = new ArrayList<>();
    private final static ConcurrentHashMap<String, BlockingDeque<Integer>> mScanHistory = new ConcurrentHashMap<>();

    @Override
    public void onCreate() {
        super.onCreate();
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        /*
         * We need to enforce that Bluetooth is first enabled, and take the
         * user to settings to enable it if they have not done so.
         */

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            //Bluetooth is disabled
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            enableBtIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(enableBtIntent);
        }

        /*
         * Check for Bluetooth LE Support.  In production, our manifest entry will keep this
         * from installing on these devices, but this will allow test devices or other
         * sideloads to report whether or not the feature exists.
         */
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "No LE Support.", Toast.LENGTH_SHORT).show();
            return START_STICKY;
        }


        this.scanningActive = true;
        return START_STICKY;
    }

    public boolean isScanningActive() { return this.scanningActive; }

    public void pause() {
        this.mBluetoothAdapter.stopLeScan(this);
        this.scanningActive = false;
    }

    public void resume() {
        this.mBluetoothAdapter.startLeScan(this);
        this.scanningActive = true;
        startScanningCycle();
    }

    /**
     * Scanning for 2 seconds, then notifies binders
     */
    private void startScanningCycle() {
        if (this.scanningActive) {
            this.mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finishScanningCycle();
                    startScanningCycle();
                }
            }, 1000);
        }
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] data) {
        synchronized (mScanHistory) {
            // Check that this isn't a device we have already seen, and add it to the list.
            if (!mScanHistory.containsKey(device.getAddress())) {
                mScanHistory.put(device.getAddress(), new LinkedBlockingDeque<Integer>());
                // @HACK I have not figured out yet how to set Bluetooth Type in CSR code. This checks for
                // vendor, 00025B	Cambridge Silicon
                if (device.getType() == BluetoothDevice.DEVICE_TYPE_LE
                        || device.getAddress().startsWith("00:02:5B:00")) {
                    try {
                        ScanItem item = mParser.handleFoundDevice(device, rssi, data);
                        mScanResults.add(item);
                        mScanHistory.get(device.getAddress()).add(rssi);
                        Log.d(TAG, "Found " + item.toString());
                    } catch (InvalidPropertiesFormatException e) {
                        Log.d(TAG, "Skipping BLE device, not an iBeacon");
                    }
                }
            }
            else {
                   if (mScanHistory.get(device.getAddress()).size() > BUFFER_SIZE)
                        mScanHistory.get(device.getAddress()).removeFirst();
                    mScanHistory.get(device.getAddress()).add(rssi);
            }
        }
    }

    private void finishScanningCycle() {
        if (this.scanningActive) {
            Log.d(TAG, "FINISHED SCANNING CYCLE");

            synchronized (mScanHistory) {
                for (Map.Entry<String, BlockingDeque<Integer>> entry : mScanHistory.entrySet()) {
                    final BlockingDeque<Integer> values = entry.getValue();
                    synchronized (values) {
                        if (values.size() > 50) {


                            DescriptiveStatistics stats = BeaconFilter.statistics(values);

                            Log.d(TAG, entry.getKey()
                                    + "\nDistanceMean:" + stats.getMean()
                                    + "\nGeoMean:" + stats.getGeometricMean()
                                    + "\nSkew:" + stats.getSkewness()
                                    + "\nStDev" + stats.getStandardDeviation()
                                    + "\nVariance" + stats.getVariance()
                                    + "\nPopVariance" + stats.getPopulationVariance());

                            for (ScanItem info : mScanResults) {
                                if (info.getMacAddress().equalsIgnoreCase((entry.getKey()))) {
                                    info.setRssi((int) stats.getMean());
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            parcelIntent.putParcelableArrayListExtra(BluetoothScannerService.TAG_PARCEL, mScanResults);
            sendBroadcast(parcelIntent);
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "SERVICE DESTROYED");
        this.scanningActive = false;

        if (this.mBluetoothAdapter != null) {
            this.mBluetoothAdapter.stopLeScan(this);
        }
        super.onDestroy();
    }
}
