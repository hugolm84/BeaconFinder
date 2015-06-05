package af.beaconfinder.Beacon;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import java.util.concurrent.BlockingDeque;
import af.beaconfinder.ScanInfo.ScanItem;

public class BeaconFilter {

    public static final double n = 2.0;

    public static double convertDistance(ScanItem item){
        return Math.pow(10, ((item.getMeasuredPower()-(double)item.getRssi())/(10*n)));
    }

    public static DescriptiveStatistics statistics(final BlockingDeque<Integer> values) {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        for(Integer value : values)
            stats.addValue(value);
        return stats;
    }

}
