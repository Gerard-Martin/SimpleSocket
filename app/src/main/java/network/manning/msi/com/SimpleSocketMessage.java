package network.manning.msi.com;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
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

/**
 * Android direct to Socket example.
 *
 * For this to work you need a server listening on the IP address and port specified. See the
 * NetworkSocketServer project for an example.
 *
 *
 * @author charliecollins
 *
 */
public class SimpleSocketMessage extends Activity {

    private static final String CLASSTAG = SimpleSocketMessage.class.getSimpleName();

    private EditText ipAddress;
    private EditText port;
    private EditText socketInput;
    private TextView socketOutput;
    Handler handler;

    //private static final String SERVER_IP = "10.0.2.2";
    @SuppressLint("HandlerLeak")
    @Override
    public void onCreate(final Bundle icicle) {
        Button socketButton;

        super.onCreate(icicle);
        this.setContentView(R.layout.simple_socket);

        this.ipAddress = (EditText) findViewById(R.id.socket_ip);
        this.port = (EditText) findViewById(R.id.socket_port);
        this.socketInput = (EditText) findViewById(R.id.socket_input);
        this.socketOutput = (TextView) findViewById(R.id.socket_output);
        socketButton = (Button) findViewById(R.id.socket_button);

        this.handler = new Handler(){
            @Override
            public void handleMessage (Message msg){
                switch (msg.what) {
                    case 0:
                        socketOutput.setText((String) msg.obj);
                }
            }

        };


        socketButton.setOnClickListener(new OnClickListener() {


            public void onClick(final View v) {
                socketOutput.setText("");
                final Thread tr = new Thread() {
                    public void run() {
                        try {
                            InetAddress serverAddr = InetAddress.getByName(ipAddress.getText().toString());
                            String output = callSocket(serverAddr, port.getText().toString(), socketInput.getText().toString());
                            Message msg = new Message();
                            msg.obj = output;
                            msg.what = 0;
                            SimpleSocketMessage.this.handler.sendMessage(msg);
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
            Log.d(getText(R.string.log_tag).toString(), " " + SimpleSocketMessage.CLASSTAG + " output - " + output);
            // send EXIT and close
            writer.write("EXIT\n", 0, 5);
            writer.flush();
        } catch (IOException e) {
            Log.e(getText(R.string.log_tag).toString(), " " + SimpleSocketMessage.CLASSTAG + " IOException calling socket", e);
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
    public void openActivityRunnable (View view) {
        Intent i = new Intent(this, SimpleSocketRunnable.class);
        startActivity(i);
    }
}
