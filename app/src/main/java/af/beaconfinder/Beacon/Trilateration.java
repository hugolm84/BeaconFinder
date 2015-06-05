package af.beaconfinder.Beacon;

import android.graphics.Color;
import android.util.Log;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Vector;

/**
 * Created by hugo on 01/03/15.
 */
public class Trilateration {

    private static final String TAG = "Trilateration";

    /**
     * Points are in meters
     * o--------x
     * |     7C (3:1)
     * |
     * |        4A (4:3)
     * | 45 (1:4)
     * y
     */
    static public LinkedHashMap<String, Beacon> mBeacons = new LinkedHashMap<String, Beacon>(){{
        put("00:02:5B:00:3A:7C", new Beacon("00:02:5B:00:3A:7C", new Point(3.0, 1.0), Color.CYAN)); // x:y
        put("00:02:5B:00:42:4A", new Beacon("00:02:5B:00:42:4A", new Point(4.0, 4.0), Color.RED));
        put("00:02:5B:00:42:45", new Beacon("00:02:5B:00:42:45", new Point(1.0, 5.0), Color.BLUE));
    }};


    public static class Point {
        public Double x, y;
        public Double sqrX, sqrY;

        Point(Double x, Double y) {
            this.x = x;
            this.y = y;
            this.sqrX = Math.pow(x, 2);
            this.sqrY = Math.pow(y, 2);
        }
    }

    public static class Beacon {

        private final String id;
        private final Point position;
        private Double distance;
        private int color;

        public Beacon(final String id, final Point pos, final int color) {
            this.id = id;
            this.position = pos;
            this.color = color;
        }

        public int getColor() {
            return color;
        }

        public String getId() {
            return id;
        }
        public Double x() {
            return this.position.x;
        }

        public Double y() {
            return this.position.y;
        }

        public Double sqrX() {
            return this.position.sqrX;
        }

        public Double sqrY() {
            return this.position.sqrY;
        }

        public Double sqrDistance() {
            return (distance == null ? 0 : Math.pow(distance, 2));
        }

        public Point getPosition() {
            return position;
        }
        public void setDistance(final Double distance) {
            this.distance = distance;
        }

        public Double getDistance() {
            return distance;
        }

    }

    //private LinkedHashMap<String, Beacon> mBeacons = new LinkedHashMap<String, Beacon>();

    public void addBeacon(final String id, final Double x, final Double y, final int color) {
        mBeacons.put(id, new Beacon(id, new Point(x, y), color));
    }

    public void setDistance(final String id, final Double distance) {
        mBeacons.get(id).setDistance(distance);
    }

    private Double kk(Beacon b0, Beacon b) {
        return (b0.sqrX() + b0.sqrY() - b.sqrX() - b.sqrY() - b0.sqrDistance() + b.sqrDistance()) / (2 * (b0.y() - b.y()));
    }

    public Point calculatePosition() {
        double j, k, x, y;

        if (mBeacons.size() < 3) {
            Log.d(TAG, "Need atleast three beacons to calculate position");
            return new Point(0.0, 0.0);
        }

        Log.d(TAG, "Calculating position based on" + mBeacons.toString());

        Iterator<Beacon> it = mBeacons.values().iterator();
        Beacon b0 = it.next();
        Beacon b1 = it.next();
        Beacon b2 = it.next();

        k = (kk(b0, b1)) - (kk(b0, b2));
        j = (b2.x() - b0.x())
          / (b0.y() - b2.y())
          - (b1.x() - b0.x())
          / (b0.y() - b1.y());

        x = k / j;

        y = ((b1.x() - b0.x())
          / (b0.y() - b1.y()))
          * x + (b0.sqrX() + b0.sqrY() - b1.sqrX() - b1.sqrY() - b0.sqrDistance() + b1.sqrDistance())
          / (2 * (b0.y() - b1.y()));

        Log.d(TAG, "Calculated position " +  x + ":" + y);

        return new Point(x, y);
    }
}
