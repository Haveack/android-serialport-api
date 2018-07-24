package me.raid.serialport;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;
import java.io.File;
import java.io.OutputStream;
import me.raid.libserialport.serialport.SerialPort;
import me.raid.libserialport.serialport.SerialPortFinder;

public class MainActivity extends Activity {

    Spinner spinner;
    String[] serials;
    String[] serialPaths;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinner = (Spinner)  findViewById(R.id.sp);
        SerialPortFinder finder = new SerialPortFinder();
        serials =  finder.getAllDevices();
        serialPaths = finder.getAllDevicesPath();
        SpinnerAdapter adapter =
            new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, serials);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectSerial(serialPaths[position]);
            }


            @Override public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void selectSerial(String serial) {
        SerialPort serialPort = new SerialPort(new File(serial));
        try {
            serialPort.open();
            OutputStream os = serialPort.getOutputStream();
            os.write(new byte[] {12});
            os.write(new byte[] {24});
            os.write(new byte[] {27, 64});
            serialPort.close();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
