package af.beaconfinder.Service;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import java.util.InvalidPropertiesFormatException;

import af.beaconfinder.ScanInfo.ScanItem;

/**
 * Created by hugo on 11/02/15.
 */
public class BeaconPDUParser {

    private final static String TAG = "BeaconPDUParser";

    private static final char[] HEX_CHARS = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    protected static byte toByte(String hex) {
        int result = Integer.parseInt(hex, 16);

        if (result > 127) {
            result -= 256;
        }

        return (byte)result;
    }

    protected int unsignedByteToInt(byte value) {
        return value & 0xff;
    }

    protected String toHex(byte[] bytes) {
        char[] chars = new char[bytes.length * 2];

        for (int i = 0; i < bytes.length; i++) {
            int value = bytes[i] & 0xFF;
            chars[i * 2] = BeaconPDUParser.HEX_CHARS[value >>> 4];
            chars[i * 2 + 1] = BeaconPDUParser.HEX_CHARS[value & 0x0F];
        }

        return new String(chars);
    }


    /**
     * Takes the bytes in PDU and converts it to a iBeacon advert
     * @param device
     * @param rssi
     * @param scanRecord
     */
    protected ScanItem handleFoundDevice(BluetoothDevice device, int rssi,
                                     byte[] scanRecord) throws InvalidPropertiesFormatException {

        final String scanRecordAsHex = toHex(scanRecord);
        int major = -1, minor = -1, measuredPower = -1;
        String proximityUUID = null;

        for (int i = 0; i < scanRecord.length; i++) {
            int payloadLength = unsignedByteToInt(scanRecord[i]);
            if ((payloadLength == 0) || (i + 1 >= scanRecord.length)) {
                break;
            }
            if (unsignedByteToInt(scanRecord[(i + 1)]) != 255) {
                i += payloadLength;
            } else {
                if (payloadLength == 26) {
                    if ((unsignedByteToInt(scanRecord[(i + 2)]) == 76)
                            && (unsignedByteToInt(scanRecord[(i + 3)]) == 0)
                            && (unsignedByteToInt(scanRecord[(i + 4)]) == 2)
                            && (unsignedByteToInt(scanRecord[(i + 5)]) == 21)) {
                        proximityUUID = String.format(
                                "%s-%s-%s-%s-%s",
                                new Object[] {
                                        scanRecordAsHex.substring(18,
                                                26),
                                        scanRecordAsHex.substring(26,
                                                30),
                                        scanRecordAsHex.substring(30,
                                                34),
                                        scanRecordAsHex.substring(34,
                                                38),
                                        scanRecordAsHex.substring(38,
                                                50) });

                        major = unsignedByteToInt(scanRecord[(i + 22)])
                                * 256
                                + unsignedByteToInt(scanRecord[(i + 23)]);
                        minor = unsignedByteToInt(scanRecord[(i + 24)])
                                * 256
                                + unsignedByteToInt(scanRecord[(i + 25)]);
                        measuredPower = scanRecord[(i + 26)];
                    }
                }
            }
        }

        if(proximityUUID == null || major == -1 || minor == -1 || measuredPower == -1)
            throw new InvalidPropertiesFormatException("ScanItem is not an iBeacon!");

        return new ScanItem(proximityUUID, device.getName(),
                device.getAddress(), major, minor, measuredPower, rssi);
    }

}
