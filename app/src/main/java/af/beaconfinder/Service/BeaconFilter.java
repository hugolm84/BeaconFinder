package af.beaconfinder.Service;

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

    public static class Point {

        public double x;
        public double y;

        public Point(double x, double y){
            this.x=x;
            this.y=y;
        }

        @Override
        public String toString() {
            return new String("[" + this.x + "," + this.y + "]");
        }
    }

    /**
     * Beacons are setup in a square of 6x6
     */
    static public HashMap<String, Point> beaconCoordinates = new HashMap<String, Point>(){{
        put("00:02:5B:00:42:45", new Point(5, 0));
        put("00:02:5B:00:3A:7C", new Point(5, 7));
        put("00:02:5B:00:42:4A", new Point(5, 14));
    }};

    /*
    * RSSI = TxPower - 10 * n * lg(d)
    * n = 2 (in free space)
    *
    * d = 10 ^ ((TxPower - RSSI) / (10 * n))
    */
    public static double getDistance(ScanItem item) {
        double txCalibratedPower = item.getMeasuredPower();
        double ratio_db = (txCalibratedPower) - item.getRssi();
        double ratio_linear = Math.pow(10, ratio_db / 10);
        return Math.sqrt(ratio_linear);
    }

    public static final double n = 3.0;

    public static double convertDistance(ScanItem item){
        return Math.pow(10, ((item.getMeasuredPower()-(double)item.getRssi())/(10*n)));
    }

    /**
     * x => (x - Mean) / Deviation
     */
    public static double normalized(double val, double mean, double std) {
        Log.d("BaconFilter", val + "=> (" + val + " - " + mean + ") / " + std);
        return ((val - mean) / std);
    }


    public static DescriptiveStatistics statistics(final BlockingDeque<Integer> values) {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        DescriptiveStatistics normStats = new DescriptiveStatistics();
        for(Integer value : values)
            stats.addValue(value);
        return stats;
    }

}
