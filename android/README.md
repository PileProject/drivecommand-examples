Android Example (Bluetooth)
=====

This is a sample Android Application
that uses [drivecommand](https://github.com/PileProject/drivecommand),
our communication library.

The following image is a screenshot of this application
and users can control a robot (EV3, NXT, PILE robot, etc.) on the screen.

<img src="https://raw.githubusercontent.com/PileProject/drivecommand-examples/master/android/docs/images/app.png" alt="app" width="250">
<!--<img src="url" alt="alt text" width="whatever" height="whatever">-->


## Tutorial
This application shows a sample usage of our library to
control a LEGO MINDSTROMS EV3 via Bluetooth.
There are three main classes in this application: `DeviceListActivity`, `BluetoothCommunicator` and `MainActivity`.


### [`DeviceListActivity`][DeviceListActivity]
This class is a helper class to pick a Bluetooth device.


### [`BluetoothCommunicator`][BluetoothCommunicator]
This class is an implementation of [ICommunicator][ICommunicator].
Users should copy it (and modify it if they need)
when they use the library for Android to control a robot via Bluetooth.


### [`MainActivity`][MainActivity]
This is the core class of this application.

#### Create a Machine and connect to it

In this example, a `Ev3Machine` is created like the following:

```java
// make a new machine
mMachine = new Ev3Machine(new BluetoothCommunicator(device));

...

// try to connect to the device
mMachine.connect();
```

Here, we pass a `BluetoothDevice` _device_ to `BluetoothCommunicator`'s constructor.
`BluetoothCommunicator` will wrap the communication (e.g., open/close connection and write/read data) between an Android and a EV3.
Users can replace the `new Ev3Machine(...)` with `new NxtMachine(...)` or other machines available in our library.

#### Create Motors and Sensors and use them

After making a machine, users can create some motors and sensors to manipulate them.

```java
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
```

Because we prepared `Ev3Machine` for this sample,
users should specify `Ev3(Out|In)putPort` for the helper methods
and users can manipulate the motors/sensors of a robot through the instances like the following:

```java
// move a motor forward
mMotors[index].forward();

...

// check if the sensor is touched or not
return "Touch.isTouched: " + ((TouchSensor) device).isTouched();
```

**NOTE: for simplicity, we hadn't added any null check for the manipulations of devices.
So, please make sure that all devices (motors/sensors) are connected properly
or add some null checks by yourself.**


## Meta
[PILE Project](http://pileproject.com/en.html)
– [@pileproject](https://twitter.com/pileproject) - dev@pileproject.com

Let's discuss anything on our [Mailing List](https://groups.google.com/forum/#!forum/pile-dev)!

Distributed under the Apache License, Version 2.0. See ``LICENSE`` for more information.

[DeviceListActivity]: https://github.com/PileProject/drivecommand-examples/blob/master/android/app/src/main/java/com/pileproject/drivecommand/examples/android/DeviceListActivity.java
[BluetoothCommunicator]: https://github.com/PileProject/drivecommand-examples/blob/master/android/app/src/main/java/com/pileproject/drivecommand/examples/android/BluetoothCommunicator.java
[MainActivity]: https://github.com/PileProject/drivecommand-examples/blob/master/android/app/src/main/java/com/pileproject/drivecommand/examples/android/MainActivity.java
[ICommunicator]: https://github.com/PileProject/drivecommand/blob/develop/src/main/java/com/pileproject/drivecommand/model/com/ICommunicator.java
[MachineBase]: https://github.com/PileProject/drivecommand/blob/develop/src/main/java/com/pileproject/drivecommand/machine/MachineBase.java
[Ev3Machine]: https://github.com/PileProject/drivecommand/blob/develop/src/main/java/com/pileproject/drivecommand/model/ev3/Ev3Machine.java
[ev3]: https://github.com/PileProject/drivecommand/tree/develop/src/main/java/com/pileproject/drivecommand/model/ev3
