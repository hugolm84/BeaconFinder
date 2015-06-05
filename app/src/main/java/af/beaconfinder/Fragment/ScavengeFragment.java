package af.beaconfinder.Fragment;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.ListIterator;
import java.util.Random;

import af.beaconfinder.MainActivity;
import af.beaconfinder.R;
import af.beaconfinder.ScanInfo.ScanItem;
import af.beaconfinder.Beacon.BeaconFilter;

public class ScavengeFragment extends BluetoothScannerFragment {


    private static final String TAG = "ScavengeFragment";
    private View mView;
    private OnFragmentInteractionListener mListener;
    private ArrayList<Button> mButtons = new ArrayList<>();
    private Button mBtnScan = null;

    public static ScavengeFragment newInstance(int sectionNumber) {
        ScavengeFragment fragment = new ScavengeFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public static int randInt(int min, int max) {
        Random rand = new Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;
        return randomNum;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_scavenge, container, false);

        mButtons.add((Button)mView.findViewById(R.id.btn0));
        mButtons.add((Button)mView.findViewById(R.id.btn1));
        mButtons.add((Button)mView.findViewById(R.id.btn2));

        for(final Button btn : mButtons) {
            btn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Log.d(TAG, "Clicked " + btn.getText());
                    ((MainActivity)getActivity()).ioService().updatePosition(btn.getText().toString(), String.valueOf(randInt(0, 2)));
                }
            });
        }

        mBtnScan = (Button) mView.findViewById(R.id.scanButton);
        mBtnScan.setOnClickListener(this);
        return mView;
    }

    @Override
    protected void onServiceConnected() {
        mBtnScan.setText( mService.isScanningActive() ? "Stop" : "Start");
    }

    @Override
    protected void onServiceDisconnected() {
        mBtnScan.setText( mService.isScanningActive() ? "Stop" : "Start");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    void handleScannedItems(ArrayList<ScanItem> items) {
        if(items.size() >= 3)
            updateButtons(items);
    }


    private void updateButtons(ArrayList<ScanItem> items) {

        ScanItem min = Collections.min(items, new Comparator<ScanItem>() {
            public int compare(ScanItem a, ScanItem b) {
                return new Double(BeaconFilter.convertDistance(a)).compareTo(new Double(BeaconFilter.convertDistance(b)));
            }
        });

        ListIterator<Button> iterator = mButtons.listIterator();
        while(iterator.hasNext()) {
            ScanItem item = items.get(iterator.nextIndex());
            Button btn = iterator.next();
            btn.setText("" + item.getMacAddress());
            if(item.equals(min)) {
                btn.getBackground().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
            }
            else {
                btn.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
            }
        }

        sendPosition(min.getMacAddress(), BeaconFilter.convertDistance(min));
    }

}
