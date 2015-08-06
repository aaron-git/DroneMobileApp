package yeow.tk.devicegravity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Activity with Controllelements to navigate a Drone
 * start's a Connetionservice and send the Drone Controll Comments to this
 * Service
 * @author Ruslan, Ivan, Atai, Jerome, Aaron
 */
public class MainActivity extends Activity {

    private MainActivity ma = this;
    private RelativeLayout layout_joystick;
    private Button disconnectButton;
    private Button startButton;

    private JoyStick js;
    private SensorManager sensorManager;
    private Sensor sensorGravity;
    private DroneControlCommand oldMessage;
    private Messenger mService = null;

    private float[] valuesGravity = new float[3];
    private String ip;
    private boolean mBound;
    private boolean sendJoyStickDataHover = false;
    private boolean isStart = false;
    private TextView counter;
    private int mCurrentPeriod = 0;
    private Timer myTimer;

    private void TimerMethod() {
        this.runOnUiThread(Timer_Tick);
    }

    private Runnable Timer_Tick = new Runnable() {
        public void run() {

            mCurrentPeriod++;
            String temp = (new SimpleDateFormat("mm:ss")).format(new Date(
                    mCurrentPeriod * 1000));
            counter.setText(temp);
        }
    };


    /**
     * init's the JoyStick
     * ini't's the View
     * init's the ButtonListener
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorGravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        layout_joystick = (RelativeLayout) findViewById(R.id.layout_joystick);
        counter = (TextView) findViewById(R.id.tvCounter);

        disconnectButton = (Button) findViewById(R.id.disconnect);
        startButton = (Button) findViewById(R.id.fly);
        DisplayMetrics metrics= new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        Bundle bundle = getIntent().getExtras();
        ip = bundle.getString("IP");

        js = new JoyStick(getApplicationContext(), layout_joystick, R.drawable.image_button);
        switch(metrics.densityDpi) {
            case DisplayMetrics.DENSITY_LOW: {
                js.setStickSize(10, 10);
                js.setLayoutSize(1000, 1000);
                js.setLayoutAlpha(150);
                js.setStickAlpha(100);
                js.setOffset(180);
                js.setMinimumDistance(100);
                Log.i("LAYOUT","LOW");
            }break;
            case DisplayMetrics.DENSITY_MEDIUM: {
                js.setStickSize(10, 10);
                js.setLayoutSize(500, 500);
                js.setLayoutAlpha(150);
                js.setStickAlpha(100);
                js.setOffset(90);
                js.setMinimumDistance(50);
                Log.i("LAYOUT","MEDIUM");

            } break;
            case DisplayMetrics.DENSITY_HIGH: {
                js.setStickSize(10, 10);
                js.setLayoutSize(300, 300);
                js.setLayoutAlpha(150);
                js.setStickAlpha(100);
                js.setOffset(45);
                js.setMinimumDistance(50);
                Log.i("LAYOUT","HIGTH");

            }break;
            default:{
                js.setStickSize(10, 10);
                js.setLayoutSize(1000, 1000);
                js.setLayoutAlpha(150);
                js.setStickAlpha(100);
                js.setOffset(180);
                js.setMinimumDistance(100);
                Log.i("LAYOUT","DEFAULT");
            } break;
        }

        layout_joystick.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                js.drawStick(arg1);
                if (arg1.getAction() == MotionEvent.ACTION_DOWN || arg1.getAction()
                        == MotionEvent.ACTION_MOVE) {
                    int direction = js.get4Direction();

                    if (direction == JoyStick.STICK_UP) {
                        sendGyroData(DroneControlCommand.UP);
                        layout_joystick.setBackgroundResource(R.drawable.up);
                    } else if (direction == JoyStick.STICK_RIGHT) {
                        sendGyroData(DroneControlCommand.ROTATE_RIGHT);
                        layout_joystick.setBackgroundResource(R.drawable.right);
                    } else if (direction == JoyStick.STICK_DOWN) {
                        sendGyroData(DroneControlCommand.DOWN);
                        layout_joystick.setBackgroundResource(R.drawable.down);
                    } else if (direction == JoyStick.STICK_LEFT) {
                        sendGyroData(DroneControlCommand.ROTATE_LEFT);
                        layout_joystick.setBackgroundResource(R.drawable.left);
                    }

                } else if (arg1.getAction() == MotionEvent.ACTION_UP) {
                    sendGyroData(DroneControlCommand.HOVER);
                    sendJoyStickDataHover = true;
                    layout_joystick.setBackgroundResource(R.drawable.background);
                }
                return true;
            }
        });

        startButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (isStart == false) {
                    sendGyroData(DroneControlCommand.TAKEOFF);
                    isStart = true;
                    startButton.setText("Landen");
                    startButton.setBackgroundResource(R.color.darkgray);

                    myTimer = new Timer();
                    myTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            TimerMethod();
                        }
                    }, 0, 1000);
                } else {
                    sendGyroData(DroneControlCommand.LAND);
                    isStart = false;
                    startButton.setText("Starten");
                    startButton.setBackgroundResource(R.color.gray);


                    if (myTimer != null)
                        myTimer.cancel();

                }
            }
        });

        disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mConnection.onServiceDisconnected(getComponentName());
                mCurrentPeriod = 0;
                if (myTimer != null)
                    myTimer.cancel();
                Intent intent= new Intent(ma, ConnectionSettings.class);
                startActivity(intent);
                ma.onStop();
            }
        });
    }

    /**
     * register the Listener from the Gyroskophsensor
     */
    @Override
    public void onResume() {
        super.onResume();
        sensorManager.registerListener(listener, sensorGravity,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    /**
     * create a Sensorlistener witch react on the Gyroskoph
     */
    SensorEventListener listener = new SensorEventListener() {
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        /**
         * sendes the x and y- values of the Gyroskoph by
         * using getGyroData and sendGyroData
         * @param event
         */
        @Override
        public void onSensorChanged(SensorEvent event) {
            System.arraycopy(event.values, 0, valuesGravity, 0, 3);
            sendGyroData(getGyroData(valuesGravity[0], valuesGravity[1]));
        }
    };
    /**
     * create a ServiceConnection witch is used to get
     * a Connection with the ConnetcionService
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        /**
         * create the Messenger witch is used to send
         * Commands to the Drone
         * @param className
         * @param service
         */
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            mBound = true;
        }

        /**
         * clear the Messenger
         * @param className
         */
        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
            mBound = false;
        }
    };

    /**
     * sendes the Datas of the Gyroskoph to the Service
     * don't send 2 times the same commend
     * use the funtion sendData()
     * @param c
     */
    public void sendGyroData(DroneControlCommand c) {

        if (c == oldMessage)
            // Message to be sent has already been sent in the previous call
            return;

        if((c == DroneControlCommand.HOVER ||c == DroneControlCommand.BACKWARD ||
                c == DroneControlCommand.FORWARD ||c == DroneControlCommand.LEFT || c == DroneControlCommand.RIGHT)
                && oldMessage!=null && sendJoyStickDataHover == false){
            switch (oldMessage){
                case UP: case DOWN: case ROTATE_LEFT: case ROTATE_RIGHT :
                    // ignore new HOVER c
                    return;
            }
            sendData(c);
        }else{
            sendData(c);
        }
    }

    /**
     * Send the commands to  the ConnectionService
     * @param c
     */
    public void sendData(DroneControlCommand c){
        Log.i("MESSAGE", c+"");
        Bundle bundle = new Bundle();
        bundle.putInt("MESSAGE", c.getIntValue());

        if (!mBound) return;
        Message msg = new Message();
        msg.setData(bundle);

        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        oldMessage = c;
        sendJoyStickDataHover = false;
    }

    /**
     * calculates from the x und y- Values of the Gyroskoph
     * Dronecommands
     * @param x
     * @param y
     * @return DroneControlCommands
     */
    protected DroneControlCommand getGyroData(double x, double y) {
        //Front
        if (x < -2 && (y < 3 && y > -3)) {
            return DroneControlCommand.FORWARD;
        }
        //Back
        else if (x > 2 && (y < 3 && y > -3)) {
            return DroneControlCommand.BACKWARD;
        }
        //Left
        if (y < -2 && (x < 3 && x > -3)) {
            return DroneControlCommand.LEFT;
        }
        //Right
        else if (y > 2 && (x < 3 && x > -3)) {
            return DroneControlCommand.RIGHT;
        }
        //Hover
        else if (y > -1.5 && y < 1.5 && (x > -1 && x < 1)) {
            return DroneControlCommand.HOVER;
        }
        return DroneControlCommand.HOVER;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    /**
     * start's and bind the ConnectionService
     * and give the ConnectionService the Destination- IP
     */
    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, ConnectionService.class);
        intent.putExtra("IP", ip);
        startService(intent);
        bindService(intent, mConnection,
                Context.BIND_AUTO_CREATE);
    }

    /**
     * unbind the ConnectionService
     */
    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }
    /**
     * terminates the App
     */
    @Override
    public void onDestroy(){
        super.onDestroy();
        System.exit(0);
    }
}
