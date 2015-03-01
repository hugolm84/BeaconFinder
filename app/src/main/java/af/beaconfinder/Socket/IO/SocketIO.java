package af.beaconfinder.Socket.IO;

import android.app.Activity;
import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.engineio.client.Transport;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Manager;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.Map;


/**
 * Created by hugo on 17/02/15.
 */
public class SocketIO {

    private static final String EVENT_SOCKET_MESSAGE        = "message";
    private static final String EVENT_SOCKET_CONNECTED      = "connected";
    private static final String EVENT_SOCKET_DISCONNECTED   = "disconnected";
    private static final String EVENT_SOCKET_DISCONNECT     = "disconnect";
    private static final String TAG            = "SocketIO";

    private static String mServer = "http://10.0.1.43:3000";

    private Activity mActivity;
    private Socket mSocket;
    private boolean mConnected = false;

    public SocketIO(Activity activity) {
        this(activity, mServer);
    }

    public SocketIO(Activity activity, final String server) {
        this.mActivity = activity;
        this.mServer = server;
        try {
            connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

    }

    private void connect() throws URISyntaxException {

        final String url = mServer+"/token";
        SocketTokenTask task = new SocketTokenTask();
        task.execute(url);

        task.setMyTaskCompleteListener(new SocketTokenTask.OnTaskComplete() {
            @Override
            public void completed(final String token) {
                {
                    try {
                        mSocket = IO.socket(mServer);
                        mSocket.io().on(Manager.EVENT_TRANSPORT, new Emitter.Listener() {
                            @Override
                            public void call(Object... args) {
                                Transport transport = (Transport) args[0];
                                transport.on(Transport.EVENT_REQUEST_HEADERS, new Emitter.Listener() {
                                    @Override
                                    public void call(Object... args) {
                                        @SuppressWarnings("unchecked")
                                        Map<String, String> headers = (Map<String, String>) args[0];
                                        headers.put("authorization", "Bearer " + token);
                                    }
                                });
                            }
                        });
                    } catch (URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                }

                mSocket.on(Socket.EVENT_CONNECT_ERROR,      onConnectError);
                mSocket.on(Socket.EVENT_CONNECT_TIMEOUT,    onConnectError);

                // Local
                mSocket.on(EVENT_SOCKET_CONNECTED, onConnected);
                mSocket.on(EVENT_SOCKET_DISCONNECTED, onDisconnected);
                mSocket.on(EVENT_SOCKET_DISCONNECT, onDisconnect);
                mSocket.on(EVENT_SOCKET_MESSAGE, onNewMessage);
                mSocket.connect();

            }
        });
    }

    public boolean isConnect() {
        return mConnected;
    }

    public void disconnect() {
        mSocket.disconnect();
    }

    public void send(final String type, final JSONObject msg) {
        mSocket.emit(type, msg);
    }

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "ConnectionError " + args[0].toString());
                }
            });
        }
    };

    private Emitter.Listener onConnected = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "Connected " + args[0].toString());
                    mConnected = true;
                }
            });
        }
    };

    private Emitter.Listener onDisconnected = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "Disconnected " + args[0].toString());
                    mConnected = false;
                }
            });
        }
    };

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "Disconnect " + args[0].toString());
                    mConnected = false;
                }
            });
        }
    };

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "New message: " + args[0]);
                }
            });
        }
    };

}
