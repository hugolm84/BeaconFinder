package af.beaconfinder.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.ListIterator;

import af.beaconfinder.Beacon.BeaconFilter;
import af.beaconfinder.Beacon.Trilateration;
import af.beaconfinder.R;
import af.beaconfinder.ScanInfo.ScanItem;
import af.beaconfinder.View.CanvasView;

public class TrilaterationCanvasFragment extends BluetoothScannerFragment {

    private static final String TAG = "TrilaterationCanvasFragment";
    private Trilateration mTrilateration = new Trilateration();
    private CanvasView mCanvasView = null;

    public static TrilaterationCanvasFragment newInstance(int sectionNumber) {
        TrilaterationCanvasFragment fragment = new TrilaterationCanvasFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_canvas, container, false);
        mCanvasView =  (CanvasView) view.findViewById(R.id.canvasView);
        return view;
    }


    @Override
    protected void onServiceConnected() {
    }

    @Override
    protected void onServiceDisconnected() {
    }

    @Override
    void handleScannedItems(ArrayList<ScanItem> items) {
        ListIterator<ScanItem> it = items.listIterator();
        while(it.hasNext()) {
            ScanItem item = it.next();
            Trilateration.mBeacons.get(item.getMacAddress()).setDistance(BeaconFilter.convertDistance(item));
        }

        Trilateration.Point p = mTrilateration.calculatePosition();
        mCanvasView.updatePositionPoint(p);
    }
}
