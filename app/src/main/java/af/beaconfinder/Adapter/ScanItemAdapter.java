package af.beaconfinder.Adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import af.beaconfinder.R;
import af.beaconfinder.ScanInfo.ScanItem;
import af.beaconfinder.Beacon.BeaconFilter;


public class ScanItemAdapter extends BaseAdapter {

    private final static String TAG = "ScanItemAdapter";

    private ArrayList<ScanItem> items = new ArrayList<ScanItem>();
    private Activity activity;
    private LayoutInflater inflater = null;

    public ScanItemAdapter(Activity a, ArrayList<ScanItem> objects) {
        activity = a;
        items = objects;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public ScanItem getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if (convertView == null)
            vi = inflater.inflate(R.layout.scan_list_row, null);

        TextView nameText = (TextView) vi.findViewById(R.id.name);
        TextView addressText = (TextView) vi.findViewById(R.id.address);
        TextView distanceText = (TextView) vi.findViewById(R.id.distance);
        TextView rssiText = (TextView) vi.findViewById(R.id.rssi);


        ScanItem info = items.get(position);
        nameText.setText(info.getName());
        addressText.setText(info.getMacAddress());
        distanceText.setText(String.format("%.2f", BeaconFilter.convertDistance(info))+ "m");
        rssiText.setText(String.valueOf(info.getRssi()) + "dBm" + " mTx:" + info.getMeasuredPower());

        return vi;
    }

    @Override
    public void notifyDataSetChanged(){
        super.notifyDataSetChanged();
    }
}
