package yeow.tk.devicegravity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ConnectionSettings extends Activity {

    private Button btip;
    private EditText etip;
    private String ip;

    /**
     * initialisize the View
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_settings);
        btip=(Button) findViewById(R.id.Btip);
        etip=(EditText) findViewById(R.id.ETIP);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_connection_settings, menu);
        return true;
    }

    /**
     * Startes the MainActivity and sending
     * the Ip from the Textfield to the  Mainactivity
     * If the IP is null, it does nothings
     * @param view
     */
    public void onIPClick (View view){
        ip = etip.getText().toString();
        if(!ip.isEmpty()){
            Intent intent= new Intent(this, MainActivity.class);
            intent.putExtra("IP",ip);
            startActivity(intent);
        }

    }


    @Override
    public void onPause(){
        super.onPause();
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
