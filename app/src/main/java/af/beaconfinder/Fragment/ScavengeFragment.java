package af.beaconfinder.Fragment;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import java.util.ArrayList;
import java.util.ListIterator;

import af.beaconfinder.R;
import af.beaconfinder.ScanInfo.ScanItem;
import af.beaconfinder.Service.BeaconFilter;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ScavengeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ScavengeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ScavengeFragment extends BluetoothScannerFragment implements View.OnClickListener {

    private static final String TAG = "ScavengeFragment";
    private View mView;
    private OnFragmentInteractionListener mListener;
    private ArrayList<Button> mButtons = new ArrayList<>();

    public static ScavengeFragment newInstance(int sectionNumber) {
        ScavengeFragment fragment = new ScavengeFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public ScavengeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_scavenge, container, false);

        mButtons.add((Button)mView.findViewById(R.id.btn0));
        mButtons.add((Button)mView.findViewById(R.id.btn1));
        mButtons.add((Button)mView.findViewById(R.id.btn2));

        return mView;
    }


    public void onButtonPressed(String id) {
        if (mListener != null) {
            mListener.onFragmentInteraction(id);
        }
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

    @Override
    public void onClick(View v) {

    }

    private void updateButtons(ArrayList<ScanItem> items) {
        ListIterator<Button> iterator = mButtons.listIterator();
        while(iterator.hasNext()) {
            ScanItem item = items.get(iterator.nextIndex());
            Button btn = iterator.next();
            btn.setText("" + item.getMacAddress());
            if(BeaconFilter.getDistance(item) < 1) {
                btn.getBackground().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
            }
            else {
                    btn.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
            }
        }
    }
}
