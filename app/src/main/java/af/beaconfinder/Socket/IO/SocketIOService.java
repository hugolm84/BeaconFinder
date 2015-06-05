package af.beaconfinder.Socket.IO;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;

/**
 * Created by hugo on 18/04/15.
 */
public class SocketIOService extends Service implements SocketTokenTask.OnTaskComplete {

    private final static String TAG = "SocketIOService";
    private final IBinder mBinder = new LocalBinder();

    private Socket mSocket;
    private String mUuid;


    @Override public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void completed(String token) {
        try {

            mSocket = SocketIOStatic.socket(token);
            mSocket.on(Socket.EVENT_CONNECT_ERROR,      onConnectError);
            mSocket.on(Socket.EVENT_CONNECT_TIMEOUT,    onConnectError);

            // Local
            mSocket.on(SocketIOStatic.EVENT_SOCKET_CONNECTED, onConnected);
            mSocket.on(SocketIOStatic.EVENT_SOCKET_DISCONNECTED, onDisconnected);
            mSocket.on(SocketIOStatic.EVENT_SOCKET_DISCONNECT, onDisconnect);
            mSocket.on(SocketIOStatic.EVENT_SOCKET_MESSAGE, onNewMessage);

            mSocket.connect();

        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

    }

    public class LocalBinder extends Binder {
        public SocketIOService getService() {
            return SocketIOService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "OnCreate");

    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Destroying service");
        if(mSocket != null && mSocket.connected())
            mSocket.disconnect();
        super.onDestroy();
    }

    private void authenticate() {
        try {
            SocketIOStatic.authenticate(this);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Starting");
        //if getAuthToken from pref blabla
        authenticate();
        return START_STICKY;
    }

    public void updatePosition(final String beaconId, final String distance) {
        if(mSocket != null && mSocket.connected()) {
            mSocket.emit("position", beaconId.substring(beaconId.lastIndexOf(":") +1), distance, mUuid);
        } else {
            Log.d(TAG, "Failed to emit, not connected!");
        }
    }

    public void emit(final String type, final Object ... args ) {
        mSocket.emit(type, args, mUuid);
    }

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.d(TAG, "ConnectionError " + args[0].toString());
        }
    };

    private Emitter.Listener onConnected = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.d(TAG, "Connected " + args[0].toString());
            mUuid = args[0].toString();
        }
    };

    private Emitter.Listener onDisconnected = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.d(TAG, "Disconnected " + args[0].toString());
        }
    };

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.d(TAG, "Disconnect " + args[0].toString());
        }
    };

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.d(TAG, "New message: " + args[0]);
        }
    };
}
