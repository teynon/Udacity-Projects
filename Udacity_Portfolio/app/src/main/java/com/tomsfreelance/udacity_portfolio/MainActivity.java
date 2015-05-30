package com.tomsfreelance.udacity_portfolio;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void click_SpotifyStreamer(View view) {
        show_Toast("This will launch the Spotify Streamer app.");
    }

    public void click_ScoresApp(View view) {
        show_Toast("This will launch the Scores app.");
    }

    public void click_Library(View view) {
        show_Toast("This will launch the Library app.");
    }

    public void click_BuildItBigger(View view) {
        show_Toast("This will launch the Build It Bigger app.");
    }

    public void click_XYZReader(View view) {
        show_Toast("This will launch the XYZ Reader app.");
    }

    public void click_Capstone(View view) {
        show_Toast("This will launch the Capstone app.");
    }

    public void show_Toast(String message) {
        show_Toast(message, Toast.LENGTH_SHORT);
    }
    public void show_Toast(String message, int duration) {
        Toast toast = Toast.makeText(this, message, duration);
        toast.show();
    }
}
