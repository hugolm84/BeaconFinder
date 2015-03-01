package af.beaconfinder.Beacon;

import android.util.Log;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.BlockingDeque;

import af.beaconfinder.ScanInfo.ScanItem;

/**
 * Created by hugo on 11/02/15.
 */
public class BeaconFilter {

    public static final int PROXIMITY_IMMEDIATE = 1;
    public static final int PROXIMITY_NEAR = 2;
    public static final int PROXIMITY_FAR = 3;
    public static final int PROXIMITY_UNKNOWN = 0;
    public static final double n = 2.0;


    protected static double calculateAccuracy(ScanItem item) {
        if (item.getRssi() == 0) {
            return -1.0; // if we cannot determine accuracy, return -1.
        }

        double ratio = item.getRssi()*1.0/item.getMeasuredPower();
        if (ratio < 1.0) {
            return Math.pow(ratio,10);
        }
        else {
            double accuracy =  (0.89976)*Math.pow(ratio,7.7095) + 0.111;
            return accuracy;
        }
    }

    public static double convertDistance(ScanItem item){
        return calculateAccuracy(item);
        //return Math.pow(10, ((item.getMeasuredPower()-(double)item.getRssi())/(10*n)));
    }

    public static DescriptiveStatistics statistics(final BlockingDeque<Integer> values) {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        DescriptiveStatistics normStats = new DescriptiveStatistics();
        for(Integer value : values)
            stats.addValue(value);
        return stats;
    }

}
