/**
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pileproject.drivecommand.examples.android;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Set;

/**
 * This Activity appears as a dialog. It lists any paired devices and devices
 * detected in the area after discovery. When a device is chosen by the user,
 * the MAC address of the device is sent back to the parent Activity in the
 * result Intent.
 */
public class DeviceListActivity extends Activity {
    public static final String KEY_PARING = "is_paring";

    // return intent extra
    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    private ArrayAdapter<String> mNewDevicesArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // setup the window
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.device_list);

        // set result 'canceled' in case the user backs out
        setResult(Activity.RESULT_CANCELED);

        // initialize the Button to perform device discovery
        Button scanButton = (Button) findViewById(R.id.button_scan);
        scanButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                doDiscovery();
                v.setVisibility(View.GONE);
            }
        });

        // initialize array adapters
        // one for already paired devices and the other for newly discovered devices
        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
        mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);

        // find and set up a ListView for paired devices
        ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);

        // find and set up a ListView for newly discovered devices
        ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

        // register a filter for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // register a filter for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        // get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        // get a set of currently paired devices
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

        // counter of the number of robots
        int count = 0;

        // if there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {
                // this limits the robots only LEGO MINDSTOMRS series (00:16:53)
                // if (device.getAddress().startsWith("00:16:53")) {
                    mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                    count++;
                // }
            }
        }

        // if there is no robots, show a message
        if (count == 0) {
            String noDevices = getResources().getText(R.string.deviceList_none_paired).toString();
            mPairedDevicesArrayAdapter.add(noDevices);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // make sure that we're not doing discovery anymore
        if (mBtAdapter != null) mBtAdapter.cancelDiscovery();

        // unregister broadcast listeners
        this.unregisterReceiver(mReceiver);
    }

    /**
     * Start discovering a device with the {@link BluetoothAdapter}.
     */
    private void doDiscovery() {
        // indicate scanning in the title
        setProgressBarIndeterminateVisibility(true);
        setTitle(R.string.deviceList_scanning);

        // turn on sub-title for new devices
        findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);

        // if we're already discovering, stop it
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }

        // request discover from a BluetoothAdapter
        mBtAdapter.startDiscovery();
    }

    /**
     *  An {@link OnItemClickListener} for all devices in {@link ListView}s.
     */
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            String info = ((TextView) v).getText().toString();

            // if the R.string.none_found raw was selected, do nothing and return
            if (info.equals(getString(R.string.deviceList_none_found))
                    || info.equals(getString(R.string.deviceList_none_paired))) {
                return;
            }

            // cancel discovery because it's costly and we're about to connect
            mBtAdapter.cancelDiscovery();

            // get the device MAC address, which is the last 17 chars in the View
            String address = info.substring(info.length() - 17);

            // create the result Intent and include the MAC address
            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE_ADDRESS, address);

            // if the device has not paired, put true to the intent
            if (v.getParent().equals(findViewById(R.id.new_devices))) {
                intent.putExtra(KEY_PARING, true);
            }

            // set result and finish this Activity
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    };

    /**
     *  A {@link BroadcastReceiver} that listens for discovered devices and
     *  changes the title when discovery is finished.
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // when discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // get a BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // if it's already paired, skip it, because it's been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    // if (device.getAddress().startsWith("00:16:53")) {
                        mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                    // }
                }
            // when discovery is finished, change the activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                setTitle(R.string.deviceList_select_device);
                if (mNewDevicesArrayAdapter.getCount() == 0) {
                    String noDevices = getResources().getText(R.string.deviceList_none_found).toString();
                    mNewDevicesArrayAdapter.add(noDevices);
                }
            }
        }
    };
}
