package yeow.tk.devicegravity;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ConnectionService extends Service {


    private float[] valuesGravity = new float[3];
    private double x;
    private double y;

    private static final int DSTPORT = 2016;
    private int message = 5;
    private int message2 = message;
    private String IP = "192.168.2.1";


    private DataSender dataSender = new DataSender();

    @Override
    public void onCreate() {
    }

    /**
     * receive the Destination- IP and
     * store it in the Variable IP
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("START", "??+??");
        if (intent.getExtras() != null) {
            Bundle bundle = intent.getExtras();
            IP = bundle.getString("IP");

        }
        try {
            dataSender.connectToServer(IP, DSTPORT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i("IP", IP + " ;;;;");
        try {
            dataSender.sendIfConnected(DroneControlCommand.HOVER);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return START_STICKY;
    }

    /**
     * build a Connection to the Server at the Destination- IP
     *      */
    //Verbindung zur Drohne erstellen
    public class ConnectorTask extends AsyncTask<String, Void, Socket> {
        @Override
        protected Socket doInBackground(String... strings) {
            try {
                String serverIP = strings[0];
                return connectToServer(serverIP, DSTPORT);
            } catch (Exception e) {
                Log.e("mmm_Exception", e.getMessage(), e);
                return null;
            }
        }

        /**
         * creates a Socket witch build a TCP- Conection to the Destination- IP
         * @param serverIP
         * @param serverPort
         * @return Socket
         * @throws IOException
         */
        private Socket connectToServer(String serverIP, int serverPort) throws IOException {
            Log.i("mmm_MyClientTask", "ConnectorTask connecting to server: Server IP=" + serverIP);
            Socket socket = new Socket(serverIP, serverPort);
            //   Log.i("mmm_MyClientTask_doInBackground", "MyClientTask connected to server: Server IP=" + serverIP);
            return socket;
        }
    }

    /**
     * class IncommingHandler
     * receives, Messages from the Main- Activity
     */
    class IncomingHandler extends Handler {
        /**
         * geht's called, if the MainActivity send's Command's
         * sends the Command's by using the sendifConnected from the
         * DataSender to the Destination- IP, using TCP
         * @param msg
         */
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            int message = bundle.getInt("MESSAGE");
            DroneControlCommand cmd = DroneControlCommand.createFromIntValue(message);
            try {
                dataSender.sendIfConnected(cmd);
            } catch (IOException eioexception) {
                Log.d("Incoming handler", "");
            }
            Log.i("Start: ", message + " S");
        }
    }

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());


    /**
     * When binding to the service, we return an interface to our messenger
     * for sending messages to the service.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    /**
     * private class DataSender
     *
     */
    private final class DataSender {
        String dstAddress;
        int dstPort;
        Socket socket;
        DataOutputStream output;


        DataSender() {
            Log.i("mmm_DataSender", "DataSender created.");
        }

        /**
         * return's true if the Socket is Connected
         * @return
         */
        public boolean isConnected() {
            return socket != null && socket.isConnected();
        }


        /**
         * Build a TCP- Connection
         * throws a IO- Exception if the IP is empty and if
         * the Connection is already used
         * @param addr
         * @param port
         * @throws Exception
         */
        public void connectToServer(String addr, int port) throws Exception {
            if (addr == null || addr.trim().isEmpty())
                throw new IOException("IP address is empty.");

            Log.i("mmm_DataSender", "DataSender connectToServer: Server IP=" + addr);
            if (isConnected()) {
                Log.i("mmm_DataSender", "Already connected" + socket);
                throw new IOException("Connection already established.");
            }
            dstAddress = addr;
            dstPort = port;

            ConnectorTask t = new ConnectorTask();

            t.execute(addr);
            socket = t.get();
            if (socket == null)
                throw new IOException("Could not establish connection to server: IP=" + addr);
            output = new DataOutputStream(socket.getOutputStream());
        }


//        private final byte[] intTo4ByteArray(int value) {
//            return new byte[]{
//                    (byte) (value >>> 24),
//                    (byte) (value >>> 16),
//                    (byte) (value >>> 8),
//                    (byte) value};
//        }

        /**
         * send's the DroneControlCommands if there is a Connection
         * create's a Error- Log is something is wrong
         * @param cmd
         * @throws IOException
         */
        public void sendIfConnected(DroneControlCommand cmd) throws IOException {
            if (!isConnected())
                // throw new IOException("Cannot send data. Not connected to server.");
                return;
            try {
                int data = cmd.getIntValue();
                output.writeInt(data);
                Log.i("mmm_time", "Data sent at " + System.currentTimeMillis());
            } catch (IOException e) {
                Log.e("mmm_exception", "Could not send data to server: " + e.getMessage(), e);
            }
//            } finally {
//                disconnectFromServer();
//            }
        }

        /**
         * close the TCP- Connection
         * @throws IOException
         */
        public void disconnectFromServer() throws IOException {
            try {
                if (output != null)
                    output.close();
                if (socket != null)
                    socket.close();
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        Log.e("mmm_Exception", e.getMessage(), e);
                    }
                }
            }
        }
    }

    /**
     * close the TCP- Conection by using disconnectFromServer
     * from Datasender
     */
    @Override
    public void onDestroy(){
        super.onDestroy();
        try {
            dataSender.disconnectFromServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
