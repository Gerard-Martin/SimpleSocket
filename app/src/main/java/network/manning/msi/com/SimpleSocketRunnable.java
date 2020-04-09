package network.manning.msi.com;

import androidx.appcompat.app.AppCompatActivity;


import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;

public class SimpleSocketRunnable extends AppCompatActivity {

    private static final String CLASSTAG = SimpleSocketMessage.class.getSimpleName();

    private EditText ipAddress2;
    private EditText port2;
    private EditText socketInput2;
    private TextView socketOutput2;
    Handler handler;

    //private static final String SERVER_IP = "10.0.2.2";

    @Override
    public void onCreate(final Bundle icicle) {
        Button socketButton;

        super.onCreate(icicle);
        this.setContentView(R.layout.activity_simple_socket_runnable);

        this.ipAddress2 = (EditText) findViewById(R.id.socket_ip2);
        this.port2 = (EditText) findViewById(R.id.socket_port2);
        this.socketInput2 = (EditText) findViewById(R.id.socket_input2);
        this.socketOutput2 = (TextView) findViewById(R.id.socket_output2);
        socketButton = (Button) findViewById(R.id.socket_button2);

        this.handler = new Handler();

        socketButton.setOnClickListener(new View.OnClickListener() {


            public void onClick(final View v) {
                socketOutput2.setText("");
                final Thread tr = new Thread() {
                    public void run() {
                        try {
                            InetAddress serverAddr = InetAddress.getByName(ipAddress2.getText().toString());
                            String output = callSocket(serverAddr, port2.getText().toString(), socketInput2.getText().toString());
                            SimpleSocketRunnable.this.handler.post(new setTextThread(output));
                        } catch (java.net.UnknownHostException e1) {
                            e1.printStackTrace();
                        }
                    }
                };
                tr.start();
            }
        });
    }

    private String callSocket(final InetAddress ad, final String port, final String socketData) {

        Socket socket = null;
        BufferedWriter writer = null;
        BufferedReader reader = null;
        String output = null;

        try {
            socket = new Socket(ad, Integer.parseInt(port));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // send input terminated with \n
            String input = socketData;
            writer.write(input + "\n", 0, input.length() + 1);
            writer.flush();
            // read back output
            output = reader.readLine();
            Log.d(getText(R.string.log_tag).toString(), " " + SimpleSocketRunnable.CLASSTAG + " output - " + output);
            // send EXIT and close
            writer.write("EXIT\n", 0, 5);
            writer.flush();
        } catch (IOException e) {
            Log.e(getText(R.string.log_tag).toString(), " " + SimpleSocketRunnable.CLASSTAG + " IOException calling socket", e);
        } finally {
            try {
                if (writer!=null)
                    writer.close();
            } catch (IOException e) { // swallow
            }
            try {
                if (reader!=null)
                    reader.close();
            } catch (IOException e) { // swallow
            }
            try {
                if (socket!=null)
                    socket.close();
            } catch (IOException e) { // swallow
            }
        }
        return output;
    }
    private class setTextThread implements Runnable {
        private String msg;

        public setTextThread(String msg) {
            this.msg = msg;
        }

        @Override
        public void run () {
            socketOutput2.setText(msg);
        }
    }
}

