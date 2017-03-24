package com.pileproject.drivecommand.examples.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.pileproject.drivecommand.machine.MachineBase;
import com.pileproject.drivecommand.machine.device.DeviceBase;
import com.pileproject.drivecommand.machine.device.input.ColorSensor;
import com.pileproject.drivecommand.machine.device.input.GyroSensor;
import com.pileproject.drivecommand.machine.device.input.LineSensor;
import com.pileproject.drivecommand.machine.device.input.Rangefinder;
import com.pileproject.drivecommand.machine.device.input.SoundSensor;
import com.pileproject.drivecommand.machine.device.input.TouchSensor;
import com.pileproject.drivecommand.machine.device.output.Buzzer;
import com.pileproject.drivecommand.machine.device.output.Motor;
import com.pileproject.drivecommand.machine.device.output.Servomotor;
import com.pileproject.drivecommand.model.ev3.Ev3Machine;
import com.pileproject.drivecommand.model.ev3.port.Ev3InputPort;
import com.pileproject.drivecommand.model.ev3.port.Ev3OutputPort;

import java.net.UnknownHostException;

public class MainActivity extends Activity {
    private MachineBase mMachine;
    private SeekBar[] mMotorSeekBars = new SeekBar[4];
    private Button[] mForwardButtons = new Button[4];
    private Button[] mBackwardButtons = new Button[4];
    private Button[] mStopButtons = new Button[4];
    private Motor[] mMotors = new Motor[4];

    private TextView[] mSensorNameTexts = new TextView[4];
    private TextView[] mSensorValueTexts = new TextView[4];
    private Button[] mGetValueButtons = new Button[4];
    private DeviceBase[] mSensors = new DeviceBase[4];

    private Button mConnectButton;
    private BluetoothAdapter mBtAdapter;

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_CONNECT_DEVICE = 2;

    private ProgressDialog mProgressDialog;

    private static final int FAILED_TO_CONNECT = 1;
    private static final int SUCCEEDED_CONNECTING = 2;

    private static final int MAX_NUM_OF_MOTORS = 4;
    private static final int MAX_NUM_OF_SENSORS = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialization
        findViews();
        setUpButtons();
        setUpSeekBars();

        // UI should be disabled until this device connects to a robot
        setUiEnabled(false);

        // get default adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    private View findView(String prefix, int index) {
        return findViewById(getResources().getIdentifier(prefix + index, "id", getPackageName()));
    }

    private void findViews() {
        // motors
        for (int i = 0; i < MAX_NUM_OF_MOTORS; i++) {
            int index = i + 1;
            mMotorSeekBars[i] = (SeekBar) findView("sb.motor", index);
            mForwardButtons[i] = (Button) findView("bt.forward", index);
            mBackwardButtons[i] = (Button) findView("bt.backward", index);
            mStopButtons[i] = (Button) findView("bt.stop", index);
        }

        // sensors
        for (int i = 0; i < MAX_NUM_OF_SENSORS; i++) {
            int index = i + 1;
            mSensorNameTexts[i] = (TextView) findView("tv.sensorName", index);
            mSensorValueTexts[i] = (TextView) findView("tv.sensor", index);
            mGetValueButtons[i] = (Button) findView("bt.getValue", index);
        }
        mConnectButton = (Button) findViewById(R.id.bt_connect);
    }

    private void setUiEnabled(boolean enabled) {
        // motors
        for (int i = 0; i < MAX_NUM_OF_MOTORS; i++) {
            mMotorSeekBars[i].setEnabled(enabled);
            mForwardButtons[i].setEnabled(enabled);
            mBackwardButtons[i].setEnabled(enabled);
            mStopButtons[i].setEnabled(enabled);
        }
        // sensors
        for (int i = 0; i < MAX_NUM_OF_SENSORS; i++) {
            mGetValueButtons[i].setEnabled(enabled);
        }
    }

    private void setUpButtons() {
        for (int i = 0; i < MAX_NUM_OF_SENSORS; i++) {
            final int index = i;
            mForwardButtons[i].setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMotors[index].forward();
                }
            });
            mBackwardButtons[i].setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMotors[index].backward();
                }
            });
            mStopButtons[i].setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMotors[index].stop();
                }
            });
        }

        for (int i = 0; i < MAX_NUM_OF_SENSORS; i++) {
            final int index = i;
            mGetValueButtons[i].setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    String result = getValue(mSensors[index]);
                    mSensorValueTexts[index].setText(result);
                }

                private String getValue(DeviceBase device) {
                    if (device instanceof TouchSensor) {
                        // return "Touch.getTouchedCount: " + ((TouchSensor)device).getTouchedCount();
                        return "Touch.isTouched: " + ((TouchSensor) device).isTouched();
                    } else if (device instanceof SoundSensor) {
                        return "Sound.getDb: " + ((SoundSensor) device).getDb();
                    } else if (device instanceof LineSensor) {
                        return "LineSensor.getSensorValue: " + ((LineSensor) device).getSensorValue();
                    } else if (device instanceof Buzzer) {
                        ((Buzzer) device).beep();
                        return "Buzzer.beep!";
                    } else if (device instanceof ColorSensor) {
                        // float[] rgb = ((ColorSensor)device).getRgb();
                        // return "ColorSensor.getRGB: (" + rgb[0] + ", " + rgb[1] + ", " + rgb[2] + ")";
                        return "ColorSensor.getIlluminance: " + ((ColorSensor) device).getIlluminance();
                    } else if (device instanceof Rangefinder) {
                        return "Rangefinder.getDistance: " + ((Rangefinder) device).getDistance();
                    } else if (device instanceof GyroSensor) {
                        // return "GyroSensor.getRate: " + ((GyroSensor) device).getRate();
                        return "GyroSensor.getAngle: " + ((GyroSensor) device).getAngle();
                    } else if (device instanceof Servomotor) {
                        return "Servomotor.getAngle: " + ((Servomotor) device).getAngle();
                    }

                    /* more sensors... (e.g., RemoteControlReceiver) */

                    return "Not implemented sensor";
                }
            });
        }

        mConnectButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                findRobot(); // show the device list
            }
        });
    }

    private void setUpSeekBars() {
        for (int i = 0; i < MAX_NUM_OF_MOTORS; i++) {
            final int index = i;
            mMotorSeekBars[i].setMax(100);
            mMotorSeekBars[i].setProgress(50); // set the initial value
            mMotorSeekBars[i].setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    mMotors[index].setSpeed(seekBar.getProgress());
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                }
            });
        }
    }

    private void findRobot() {
        // turn on Bluetooth
        if (!mBtAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            return;
        }

        // start DeviceListActivity to pick a device
        Intent serverIntent = new Intent(this, DeviceListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
    }

    private void foundRobot(BluetoothDevice device) {
        // make a new machine
        mMachine = new Ev3Machine(new BluetoothCommunicator(device));

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(R.string.main_connecting);
        mProgressDialog.setMessage(getString(R.string.main_please_wait));
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        // connect to the robot
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mMachine.connect();
                    mConnectingHandler.sendEmptyMessage(SUCCEEDED_CONNECTING);
                } catch (Exception e) {
                    // NOTE: This exception also occurs when this device hasn't finished paring
                    e.printStackTrace();
                    mConnectingHandler.sendEmptyMessage(FAILED_TO_CONNECT);
                }
                mProgressDialog.dismiss(); // dismiss the dialog
            }
        }).start();
    }

    private void connected() {
        mConnectButton.setText(R.string.main_disconnect);
        mConnectButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                disconnect();
            }
        });

        try {
            setUpRobot();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < MAX_NUM_OF_SENSORS; i++) {
            String name = mSensors[i].getClass().getSimpleName();
            mSensorNameTexts[i].setText(name);
        }
        setUiEnabled(true);
        Toast.makeText(this, R.string.main_device_connected, Toast.LENGTH_SHORT).show();
    }

    private void disconnect() {
        // close connection
        try {
            mMachine.disconnect();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        // toggle the button text
        mConnectButton.setText(R.string.main_connect);
        mConnectButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                findRobot();
            }
        });
        setUiEnabled(false);
        Toast.makeText(this, R.string.main_device_disconnected, Toast.LENGTH_SHORT).show();
    }

    private void setUpRobot() throws UnknownHostException {
        if (mMachine == null) {
            throw new UnknownHostException();
        }

        // motors
        mMotors[0] = mMachine.createMotor(Ev3OutputPort.PORT_A);
        mMotors[1] = mMachine.createMotor(Ev3OutputPort.PORT_B);
        mMotors[2] = mMachine.createMotor(Ev3OutputPort.PORT_C);
        mMotors[3] = mMachine.createMotor(Ev3OutputPort.PORT_D);

        // sensors
        mSensors[0] = mMachine.createTouchSensor(Ev3InputPort.PORT_1);
        mSensors[1] = mMachine.createTouchSensor(Ev3InputPort.PORT_2);
        mSensors[2] = mMachine.createColorSensor(Ev3InputPort.PORT_3);
        mSensors[3] = mMachine.createRangefinder(Ev3InputPort.PORT_4);
    }

    private Handler mConnectingHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case FAILED_TO_CONNECT:
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle(R.string.main_bluetooth_connection_error)
                            .setMessage(R.string.main_bluetooth_failed_to_connect)
                            .setPositiveButton(R.string.ok, null)
                            .show();
                    return true;

                case SUCCEEDED_CONNECTING:
                    connected();
                    return true;
            }
            return false;
        }
    });

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                }
                break;

            case REQUEST_CONNECT_DEVICE:
                // when DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // get the device MAC address
                    String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    // get the BluetoothDevice instance
                    BluetoothDevice device = mBtAdapter.getRemoteDevice(address);
                    foundRobot(device);
                }
                break;
        }
    }
}
