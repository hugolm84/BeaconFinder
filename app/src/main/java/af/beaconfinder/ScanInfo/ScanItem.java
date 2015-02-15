package af.beaconfinder.ScanInfo;

/**
 * Created by hugo on 11/02/15.
 */

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by hugo on 11/02/15.
 */
public class ScanItem implements Comparable<ScanItem>, Parcelable {

    private final String proximityUUID;
    private final String name;
    private final String macAddress;
    private final int major;
    private final int minor;
    private final int measuredPower;
    private int rssi; //Updateable

    public ScanItem(final String proximityUUID, final String name, final String macAddress,
                    final int major, final int minor, final int measuredPower, final int rssi) {

        this.proximityUUID = proximityUUID;
        this.name = (name == null) ? "Unknown Device" : name;
        this.macAddress = macAddress;
        this.major = major;
        this.minor = minor;
        this.measuredPower = measuredPower;
        this.rssi = rssi;

    }

    /**
     * Created by Parcel constructor
     * @param in
     */
    private ScanItem(Parcel in){
        this.proximityUUID = in.readString();
        this.name = in.readString();
        this.macAddress = in.readString();
        this.major = in.readInt();
        this.minor = in.readInt();
        this.measuredPower = in.readInt();
        this.rssi = in.readInt();
    }

    /**
     * Parcel creation
     */
    public static final Parcelable.Creator<ScanItem> CREATOR = new Parcelable.Creator<ScanItem>() {

        @Override
        public ScanItem createFromParcel(Parcel source) {
            return new ScanItem(source);
        }

        @Override
        public ScanItem[] newArray(int size) {
            return new ScanItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.proximityUUID);
        dest.writeString(this.name);
        dest.writeString(this.macAddress);
        dest.writeInt(this.major);
        dest.writeInt(this.minor);
        dest.writeInt(this.measuredPower);
        dest.writeInt(this.rssi);
    }

    @Override
    public int compareTo(ScanItem another) {
        final int BEFORE = -1;
        final int EQUAL = 0;
        final int AFTER = 1;

        if (rssi == another.rssi) return EQUAL;
        if (this.rssi < another.rssi) return AFTER;
        if (this.rssi > another.rssi) return BEFORE;

        return EQUAL;
    }

    @Override
    public String toString() {
        return "Name: " + name + " MAC: " + macAddress + " RSSI: " + rssi + " txCal: " + measuredPower +" UUID: " + proximityUUID;
    }

    public String getProximityUUID() {
        return proximityUUID;
    }

    public String getName() {
        return name;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getMeasuredPower() {
        return measuredPower;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }
}
