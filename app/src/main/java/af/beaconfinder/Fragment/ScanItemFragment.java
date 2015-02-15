package af.beaconfinder.Fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;

import java.util.ArrayList;

import af.beaconfinder.R;
import af.beaconfinder.ScanInfo.ScanItem;
import af.beaconfinder.ScanInfo.ScanItemAdapter;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class ScanItemFragment extends BluetoothScannerFragment implements AbsListView.OnItemClickListener, View.OnClickListener {

    private static final String TAG = "ScanItemFragment";
    private OnFragmentInteractionListener mListener;

    private boolean scanning = false;
    private AbsListView mScanListView = null;
    private Button mBtnScan = null;

    private static ArrayList<ScanItem> mScanResults = new ArrayList<ScanItem>();
    private static ScanItemAdapter mScanResultsAdapter;

    public static ScanItemFragment newInstance(int sectionNumber) {
        ScanItemFragment fragment = new ScanItemFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mScanResultsAdapter = new ScanItemAdapter(getActivity(), mScanResults);
    }

    @Override
    void handleScannedItems(ArrayList<ScanItem> items) {
        clearScanResults();
        //Collections.sort(items);
        for(ScanItem item : items) {
            mScanResults.add(item);
        }
        mScanResultsAdapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scanitem, container, false);

        // Set the adapter
        mScanListView = (AbsListView) view.findViewById(R.id.scanListView);
        mScanListView.setAdapter(mScanResultsAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mScanListView.setOnItemClickListener(this);
        mBtnScan = (Button) view.findViewById(R.id.scanButton);
        mBtnScan.setOnClickListener(this);

        return view;
    }

    /**
     * Clear the cached scan results, and update the display.
     */
    private void clearScanResults() {
        mScanResults.clear();
        // Make sure the display is updated; any old devices are removed from the ListView.
        mScanResultsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.scanButton:
                if(!scanning) {
                    mService.resume();
                }
                else {
                    mService.pause();
                }
                scanning = !scanning;
                mBtnScan.setText( scanning ? "Stop" : "");
                break;
        }
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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.onFragmentInteraction(mScanResults.get(position).getMacAddress());
        }
    }

}
