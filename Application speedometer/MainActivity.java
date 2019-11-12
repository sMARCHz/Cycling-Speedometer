package cohesivecomputing.hackatronics.speedometer;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import cohesivecomputing.hackatronics.speedometer.library.ConnectionState;
import cohesivecomputing.hackatronics.speedometer.library.IOController;
import cohesivecomputing.hackatronics.speedometer.library.SpeedometerState;
import cohesivecomputing.hackatronics.speedometer.library.SpeedometerStatusResponse;

public class MainActivity extends AppCompatActivity {

    private static int RetryInterval = 0;
    private static final int ACTIVITY_SETTINGS = 1;
    private static final int ACTIVITY_BLUETOOTH_NAME = 2;
    private static final int ACTIVITY_BT_ENABLE = 3;
    private static final int ACTIVITY_ABOUT = 4;
    private static BluetoothAdapter bluetooth_adapter;

    private MenuItem actionSettings;
    private MenuItem actionResetTrip;
    private MenuItem actionAbout;
    private Timer timer;
    private SharedPreferences prefs;
    private String bluetoothName;

    private TextView revsPerMin;
    private TextView accelMss;
    private TextView currentSpeed;
    private TextView totalDistance;
    private TextView avgTripSpeed;
    private TextView maxTripSpeed;
    private TextView tripDistance;
    private TextView tripTime;


    private static void setSpeedo(BluetoothDevice device) {
        if (Globals.Speedo == null && device != null) {
            Globals.Speedo = new IOController(device);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        revsPerMin = (TextView) findViewById(R.id.revsPerMin);
        accelMss = (TextView) findViewById(R.id.accelMss);
        currentSpeed = (TextView) findViewById(R.id.currentSpeed);
        totalDistance = (TextView) findViewById(R.id.totalDistance);
        avgTripSpeed = (TextView) findViewById(R.id.avgTripSpeed);
        maxTripSpeed = (TextView) findViewById(R.id.maxTripSpeed);
        tripDistance = (TextView) findViewById(R.id.tripDistance);
        tripTime = (TextView) findViewById(R.id.tripTime);

        prefs = getSharedPreferences("PREFS", MODE_PRIVATE);

        getBluetoothName();


        if (Globals.BtState == null) {
            //Log.i("README", "onCreate : BtState == null");
            Globals.BtState = BTState.NoAdapter;
            bluetooth_adapter = BluetoothAdapter.getDefaultAdapter();

            if (bluetooth_adapter == null) {
                toastMsg(R.string.bluetooth_adapter_not_found);
            }
            else {
                bluetooth_adapter.cancelDiscovery();
                if (!bluetooth_adapter.isEnabled()) {
                    Globals.BtState = BTState.Enabling;
                    Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBluetooth, ACTIVITY_BT_ENABLE);
                } else {
                    assignBluetoothDevice(bluetooth_adapter);
                }
            }
        } else if (Globals.BtState == BTState.NotEnabled || Globals.BtState == BTState.DeviceFound || Globals.BtState == BTState.NoDevice) {
            //Log.i("README", "onCreate : BtState == " + Globals.BtState);
            // Since static variables can outlive Activities, need to re-check bluetooth when Activity recreated.

            bluetooth_adapter.cancelDiscovery();
            if (!bluetooth_adapter.isEnabled()) {
                Globals.BtState = BTState.Enabling;
                Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBluetooth, ACTIVITY_BT_ENABLE);
            } else if (Globals.BtState == BTState.NoDevice) {
                assignBluetoothDevice(bluetooth_adapter);
            }
        }
        else {
            //Log.i("README", "onCreate else");
        }
        //Log.i("README", "onCreate end");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        actionSettings = menu.getItem(0);   // assign 'Settings' menu item for later use.
        actionResetTrip = menu.getItem(1);
        actionAbout = menu.getItem(2);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            launchSettingsActivity();
            return true;
        }
        else if (id == R.id.action_bluetooth_device) {
            launchBluetoothNameActivity();
            return true;
        }
        else if (id == R.id.action_reset_trip) {
            if (Globals.Speedo != null) {
                Globals.Speedo.resetTrip();
            }
        }
        else if (id == R.id.action_about) {
            launchAboutActivity();
            return true;


        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode, Intent intent) {

        //Log.i("README", "onActivityResult");

        if (requestCode == ACTIVITY_SETTINGS){
            if (resultCode == RESULT_OK){

            }
        } else if (requestCode == ACTIVITY_BLUETOOTH_NAME){
            if (resultCode == RESULT_OK){
                // User has inputted a bluetooth name.
                if (Globals.Speedo != null) {
                    Globals.Speedo.close();
                }
                getBluetoothName();
                assignBluetoothDevice(bluetooth_adapter);
            }
        }
        else if (requestCode == ACTIVITY_BT_ENABLE ) {
            if (resultCode == RESULT_OK){
                assignBluetoothDevice(bluetooth_adapter);
            } else {
                Globals.BtState = BTState.NotEnabled;
            }
        }
        else if(requestCode == ACTIVITY_ABOUT){
            if(resultCode == RESULT_OK){

            }
        }

    }



    @Override
    public void onResume() {
        super.onResume();

        //Log.i("README", "onResume begin");

        timer = new Timer();   // Note: timer is cancelled on onPause().

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // Regularly poll bluetooth device (i.e. arduino), requesting status.
                if (Globals.Speedo == null) {
                    //Log.i("README", "run : Speedo == null  BtState == " + Globals.BtState.toString());
                    uiHandler.obtainMessage(1, null).sendToTarget();
                } else if (Globals.Speedo.getConnectionState() == ConnectionState.Faulted) {
                    // There was a comms problem, so close the connection. // and increase timer interval.
                    RetryInterval = 8;
                    Globals.Speedo.close();
                    //Log.i("README", "run : ConnectionState.Faulted");
                    uiHandler.obtainMessage(1, null).sendToTarget();
                } else if (Globals.Speedo.getConnectionState() == ConnectionState.Closed) {
                    if (RetryInterval > 0) {
                        RetryInterval--;
                    }
                    else {
                        try {
                            //Log.i("README", "run : Speedo.open() attempt");
                            Globals.Speedo.open();
                        } catch (IOException e) {
                            //Log.i("README", "run : Speedo.open() exception");
                        }
                        uiHandler.obtainMessage(1, null).sendToTarget();
                    }
                } else {  // connection is open
                    SpeedometerStatusResponse status = Globals.Speedo.getStatus();

                    //Log.i("README", "run : else block : status == " + status.getSpeedometerState());
                    // send status to handler.
                    uiHandler.obtainMessage(1, status).sendToTarget();
                }
            }
        }, 0, 250);
        //Log.i("README", "onResume end");
    }


    final Handler uiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // Update UI using status supplied via msg.
            boolean buttonEnabled = false;
            boolean menuItemsEnabled = false;

            if (msg.obj != null) {
                SpeedometerStatusResponse status = (SpeedometerStatusResponse) msg.obj;
                SpeedometerState state = status.getSpeedometerState();

                switch (state) {
                    case Running:
                        float avgTripSpeedKmh = status.getTripTimeSeconds() > 0
                                                ? (status.getTripDistanceMetres() * 3.6f) / (status.getTripTimeSeconds())
                                                : 0;
                        int tripDistanceHrs = status.getTripTimeSeconds() / 3600;
                        int tripDistanceMins = (status.getTripTimeSeconds() % 3600) / 60;
                        int tripDistanceSecs = status.getTripTimeSeconds() - (tripDistanceHrs * 3600 + tripDistanceMins * 60);

                        revsPerMin.setText(String.format("%03d", status.getCurrentRevsPerMin()));
                        accelMss.setText(String.format("%04.1f", status.getCurrentAccelMss()));
                        currentSpeed.setText(String.format("%04.1f", status.getCurrentSpeedKmh()));
                        totalDistance.setText(String.format("%06.1f", (float)status.getTotalDistanceMeters() / 1000));
                        avgTripSpeed.setText(String.format("%04.1f", avgTripSpeedKmh));
                        maxTripSpeed.setText(String.format("%04.1f", status.getTripMaxSpeedKmh()));
                        tripDistance.setText(String.format("%05.1f", (float)status.getTripDistanceMetres() / 1000));
                        tripTime.setText(String.format("%02d:%02d:%02d", tripDistanceHrs, tripDistanceMins, tripDistanceSecs));
                        break;
                    default:
                        revsPerMin.setText("000");
                        accelMss.setText("00.0");
                        currentSpeed.setText("00.0");
                        totalDistance.setText("0000.0");
                        avgTripSpeed.setText("00.0");
                        maxTripSpeed.setText("00.0");
                        tripDistance.setText("000.0");
                        tripTime.setText("00:00:00");

                        break;
                }
                menuItemsEnabled = (state == SpeedometerState.Running);
            }

            if (actionSettings != null) {
                actionSettings.setEnabled(menuItemsEnabled);
            }
            if (actionResetTrip != null) {
                actionResetTrip.setEnabled(menuItemsEnabled);
            }
            if(actionAbout != null){
                actionAbout.setEnabled(menuItemsEnabled);
            }
        }
    };


    @Override
    public void onPause() {
        //Log.i("README", "onPause begin");
        super.onPause();
        timer.cancel();
        timer.purge();
        uiHandler.removeCallbacksAndMessages(null);
    }


    private void launchSettingsActivity() {
        Intent intent =
                new Intent(this, SettingsActivity.class);
        startActivityForResult(intent, ACTIVITY_SETTINGS);
    }

    private void launchAboutActivity() {
        Intent intent =
                new Intent(this, AboutActivity.class);
        startActivityForResult(intent, ACTIVITY_ABOUT);
    }

    private void launchBluetoothNameActivity() {
        Intent intent =
                new Intent(this, BluetoothNameActivity.class);
        startActivityForResult(intent, ACTIVITY_BLUETOOTH_NAME);
    }


    // Get stored bluetooth name, or get default otherwise.
    private void getBluetoothName() {
        if (prefs != null) {
            bluetoothName = prefs.getString("BluetoothName", getString(R.string.default_bluetooth_name));
        }
        else {
            bluetoothName = getString(R.string.default_bluetooth_name);
        }
    }


    // Find our device in the paired bluetooth device list.
    private void assignBluetoothDevice(BluetoothAdapter bluetoothAdapter) {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        BluetoothDevice device = null;

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice btDevice : pairedDevices) {
                if (btDevice.getName().toLowerCase().equals(bluetoothName.toLowerCase())) {
                    device = btDevice;
                    break;
                }
            }
        }

        if (device == null) {
            Globals.BtState = BTState.NoDevice;
            Globals.Speedo = null;
            toastMsg(R.string.bluetooth_device_not_found);
        } else {
            Globals.BtState = BTState.DeviceFound;
            setSpeedo(device);
            toastMsg(R.string.bluetooth_device_found);
        }
    }


    void toastMsg(int resource) {
        Toast.makeText(this, resource, Toast.LENGTH_SHORT).show();
    }
}
