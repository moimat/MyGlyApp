package com.example.matthieu.mygly;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MyBluetooth extends Service {

    private Handler bluetoothIn;

    private final int handlerState = 0;
    private BluetoothAdapter btAdapter;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();
    private BluetoothDevice mmDevice;
    private InputStream btInputStream;
    private String SAVE_FILE = "MyglyVal.txt";


    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // String for MAC address
    private static String address;

    public MyBluetooth() {
        
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){


        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null)
        {
            // Device does not support Bluetooth
            Toast.makeText(getApplicationContext(), "Device does not support bluetooth", Toast.LENGTH_LONG).show();
            return -1;
        }
        else
        {
            if (!btAdapter.isEnabled())
            {
                mmDevice=null;
                btAdapter.enable();

                //Toast.makeText(getApplicationContext(), "Bluetooth switched ON", Toast.LENGTH_LONG).show();

            }

            try {
                if(queryPairedDevices()) {
                    try {
                       // Toast.makeText(getApplicationContext(), "Bluetooth found", Toast.LENGTH_LONG).show();
                        openBluetooth();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "Bluetooth device not found", Toast.LENGTH_LONG).show();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        }


        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {

        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private boolean queryPairedDevices() throws InterruptedException {
        Set<String> mArrayAdapter=null;
        Set<BluetoothDevice> pairedDevices;

        //waiting time for bluetooth discovery
        TimeUnit.MILLISECONDS.sleep(200);
            pairedDevices = btAdapter.getBondedDevices();

       // mmDevice = btAdapter.getRemoteDevice("30:14:10:09:16:40");
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView

                //System.out.println(device.getName());
                if(device.getName().equals("HC-05")) {

                    mmDevice = device;

                    break;
                }
            }
            return true;
        }

        return false;
        /*if(mmDevice!=null) {
            Toast.makeText(getApplicationContext(), mmDevice.getName(), Toast.LENGTH_LONG).show();
            return true;
        }else{
            return false;
        }*/
    }


    private void openBluetooth()throws IOException{

        if(mmDevice!=null) {
            btSocket = mmDevice.createRfcommSocketToServiceRecord(BTMODULEUUID);
            btSocket.connect();
            //mmOutputStream = btSocket.getOutputStream();
            btInputStream = btSocket.getInputStream();
            listenForData();
        }

    }

    private void listenForData(){
        bluetoothIn=new Handler();

        Thread workerThread = new Thread(new Runnable()
        {
            boolean stopWorker=false;
            int readBufferPosition = 0;
            byte delimiter = 10;
            byte[] readBuffer = new byte[1024];

            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopWorker)
                {
                    try
                    {
                        int bytesAvailable = btInputStream.available();
                        if(bytesAvailable > 0)
                        {
                            byte[] packetBytes = new byte[bytesAvailable];
                            btInputStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++)
                            {
                                byte b = packetBytes[i];
                                if(b == delimiter)
                                {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;
                                    bluetoothIn.post(new Runnable()
                                    {
                                        public void run()
                                        {
                                            System.out.println("gly: "+data);
                                            saveValue(Double.parseDouble(data));

                                            /*if(Result.getText().toString().equals("..")) {
                                                Result.setText(data);
                                            } else {
                                                Result.append("\n"+data);
                                            }

	                                        	/* You also can use Result.setText(data); it won't display multilines
	                                        	*/

                                        }
                                    });
                                }
                                else
                                {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex)
                    {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();

    }

    private boolean saveValue(double val) {


        FileOutputStream fos = null;
        try {
            fos = openFileOutput(SAVE_FILE, Context.MODE_APPEND);
            fos.write((new Date().toString()+"\n").getBytes());
            fos.write((val+"\n").getBytes());
            fos.close();
            return true;
        } catch (FileNotFoundException e) {
            Toast.makeText(getApplicationContext(), "File not found", Toast.LENGTH_LONG).show();
            e.printStackTrace();
            return false;
        } catch (IOException e) {

            e.printStackTrace();
            return false;
        }




    }

}
