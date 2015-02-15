package af.beaconfinder.Fragment;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidplot.ui.XLayoutStyle;
import com.androidplot.ui.YLayoutStyle;
import com.androidplot.ui.widget.Widget;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PointLabelFormatter;
import com.androidplot.xy.PointLabeler;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;


import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import af.beaconfinder.R;
import af.beaconfinder.ScanInfo.ScanItem;
import af.beaconfinder.Service.BeaconFilter;
import af.beaconfinder.Service.NonLinearLeastSquaresSolver;
import af.beaconfinder.Service.TrilaterationFunction;

import static af.beaconfinder.Service.BeaconFilter.*;

public class TrilaterationFragment extends BluetoothScannerFragment {

    private final static int MARGIN = 100;
    private static final String TAG = "TrilaterationFragment";

    private int width;
    private int height;

    List<double[]> positions = new ArrayList<>();
    List<BeaconFilter.Point> beaconPositions = new ArrayList<>();

    private List<String> beaconMacPos = new ArrayList<String>();

    private double widthRatio;
    private double heightRatio;

    boolean first = true;
    private XYPlot plot;
    XYSeries mApproxSeries = null;

    public static TrilaterationFragment newInstance(int sectionNumber) {
        TrilaterationFragment fragment = new TrilaterationFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public TrilaterationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_trilateration, container, false);
        plot =  (XYPlot) view.findViewById(R.id.mySimpleXYPlot);
        Widget gw = plot.getGraphWidget();
        gw.position(0, XLayoutStyle.ABSOLUTE_FROM_LEFT, 0, YLayoutStyle.ABSOLUTE_FROM_TOP);
        updateIndicatorSeries();
        return view;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void addBeacons(List<ScanItem> beacons) {
        ArrayList<Number> x = new ArrayList<>();
        ArrayList<Number> y = new ArrayList<>();

        for (ScanItem beacon : beacons) {

            final Point py = beaconCoordinates.get(beacon.getMacAddress());
            if(py != null) {
                x.add((float) py.x);
                y.add((float) py.y);

                positions.add(new double[]{py.x, py.y});
                beaconMacPos.add(beacon.getMacAddress());
            }
        }

        updateBeaconSeries(x, y);
    }

    private void updateIndicatorSeries() {

        final ArrayList<Number> x = new ArrayList<Number>() {
            {
                add(0);
                add(18);
                add(18);
            }
        };
        final ArrayList<Number> y = new ArrayList<Number>(){
            {
                add(10);
                add(0);
                add(18);
            }
        };
        final XYSeries s = new SimpleXYSeries(x, y, "Indicator");
        LineAndPointFormatter lpf = new LineAndPointFormatter(null, Color.BLUE, null, new PointLabelFormatter(Color.WHITE));
        lpf.setPointLabeler(new PointLabeler() {
            @Override
            public String getLabel(XYSeries series, int index) {
                return "" + series.getX(index) + ":" + series.getY(index);
            }
        });
        lpf.getVertexPaint().setStrokeWidth(50);
        plot.addSeries(s, lpf);
        plot.redraw();
    }

    private void updateBeaconSeries(final ArrayList<Number> x, final ArrayList<Number> y) {

        final XYSeries s = new SimpleXYSeries(x, y, "Beacon");
        LineAndPointFormatter lpf = new LineAndPointFormatter(null, Color.GREEN, null, new PointLabelFormatter(Color.WHITE));
        lpf.setPointLabeler(new PointLabeler() {
            @Override
            public String getLabel(XYSeries series, int index) {
                return beaconMacPos.get(index);
            }
        });
        lpf.getVertexPaint().setStrokeWidth(100);
        plot.addSeries(s, lpf);
        plot.redraw();

    }

    private void updatePositionSeries(final double x, final double y) {
        if(mApproxSeries != null)
            plot.removeSeries(mApproxSeries);
        mApproxSeries = new SimpleXYSeries(Arrays.asList(x), Arrays.asList(y), "Position");
        LineAndPointFormatter lpf = new LineAndPointFormatter(null, Color.RED, null, new PointLabelFormatter(Color.WHITE));
        lpf.getVertexPaint().setStrokeWidth(50);
        lpf.setPointLabeler(new PointLabeler() {
            @Override
            public String getLabel(XYSeries series, int index) {
                return "X:" + series.getX(index) + "Y:" + series.getY(index);
            }
        });
        plot.addSeries(mApproxSeries, lpf);
        plot.redraw();

    }

    @Override
    void handleScannedItems(ArrayList<ScanItem> items) {
        if(first) {
            first = false;
            addBeacons(items);
        }

        solvePosition(items);
    }

    private void solvePosition(ArrayList<ScanItem> items) {
        double[] distances = new double[items.size()];
        ListIterator<ScanItem> it = items.listIterator();
        while(it.hasNext()) {

            int index = it.nextIndex();
            ScanItem item = it.next();
            double distance = convertDistance(item);
            /*if(distance <= 0.2) {
                final BeaconFilter.Point p = beaconCoordinates.get(item.getMacAddress());
                if(p != null) {
                    Log.d(TAG, "Distance is <= 1! Setting position at" + p.toString());
                    updatePositionSeries(p.x, p.y);
                    return;
                }
            }*/
            distances[index] = distance;
        }

        double[][] pos2D = positions.toArray(new double[0][0]);

        try {

            TrilaterationFunction trilaterationFunction = new TrilaterationFunction(pos2D, distances);
            NonLinearLeastSquaresSolver solver = new NonLinearLeastSquaresSolver(trilaterationFunction, new LevenbergMarquardtOptimizer());
            LeastSquaresOptimizer.Optimum optimum = solver.solve();
            double[] calculatedPosition = optimum.getPoint().toArray();
            updatePositionSeries(calculatedPosition[0], calculatedPosition[1]);

        }catch(IllegalArgumentException e) {
            Log.d(TAG, e.getMessage());
        }
    }

}
