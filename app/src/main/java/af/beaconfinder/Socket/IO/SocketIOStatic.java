package af.beaconfinder.Socket.IO;

/**
 * Created by hugo on 18/04/15.
 */

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.engineio.client.Transport;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Manager;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;
import java.util.Map;

/**
 * Created by hugo on 27/03/15.
 */
public class SocketIOStatic {

    private static final String TAG = "SocketIOStatic";
    private static final String URL = "http://10.40.230.158:3001";

    public static final String EVENT_SOCKET_MESSAGE         = "message";
    public static final String EVENT_SOCKET_CONNECTED       = "connected";
    public static final String EVENT_SOCKET_DISCONNECT      = "disconnect";
    public static final String EVENT_SOCKET_DISCONNECTED    = "disconnected";

    private SocketIOStatic() {}

    public static Socket socket(final String authToken, IO.Options options) throws URISyntaxException {
        /**
         * Creates a new Socket.IO with a Authorization: Bearer Token header is set before connection
         * attempts are being made.
         */
        Socket socket = IO.socket(URL, options);
        socket.io().on(Manager.EVENT_TRANSPORT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Transport transport = (Transport) args[0];
                transport.on(Transport.EVENT_REQUEST_HEADERS, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        Map<String, String> headers = (Map<String, String>) args[0];
                        headers.put("authorization", "Bearer " + authToken);
                    }
                });
            }
        });
        return socket;
    }

    public static Socket socket(final String authToken) throws URISyntaxException {
        IO.Options options = new IO.Options();
        options.forceNew = true;
        options.reconnection = true;
        return socket(authToken, options);
    }

    public static void authenticate(SocketTokenTask.OnTaskComplete callback) throws URISyntaxException {
        final String url = URL + "/v1/token";
        SocketTokenTask task = new SocketTokenTask();
        task.setMyTaskCompleteListener(callback);
        task.execute(url);
    }
}
