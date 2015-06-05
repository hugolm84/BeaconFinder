package af.beaconfinder;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;
import android.widget.Toast;

import af.beaconfinder.Fragment.BaseFragment;
import af.beaconfinder.Fragment.ScanItemFragment;
import af.beaconfinder.Fragment.ScavengeFragment;
import af.beaconfinder.Fragment.TrilaterationCanvasFragment;
import af.beaconfinder.Service.BluetoothScannerService;
import af.beaconfinder.Socket.IO.SocketIOService;
import af.beaconfinder.Socket.IO.SocketIOStatic;

public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, BaseFragment.OnFragmentInteractionListener {


    private static final String TAG = "MainActivity";

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    private SocketIOService mBackgroundSocketService;
    private ServiceConnection mSocketConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            SocketIOService.LocalBinder binder = (SocketIOService.LocalBinder) service;
            mBackgroundSocketService = binder.getService();
            Log.d(TAG, "Connected Service!");
        }

        public void onServiceDisconnected(ComponentName arg0) {
            Log.d(TAG, "DisConnected Service!");
        }
    };

    public SocketIOService ioService() {
        return mBackgroundSocketService;
    }

    /**
     * Do not run scanner thread in the background if application is not in foreground
     */
    @Override
    protected void onStop() {
        super.onStop();
        if(mBackgroundSocketService != null)
            mBackgroundSocketService.stopSelf();
    }

    @Override
    protected void onDestroy() {
        unbindService(mSocketConnection);
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Log.d(TAG, "onCreate");

        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        Intent intent = new Intent(this, SocketIOService.class);
        bindService(intent, mSocketConnection, Context.BIND_AUTO_CREATE);
        startService(intent);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        if(position == 0) {
            fragmentManager.beginTransaction()
                    .replace(R.id.container, ScanItemFragment.newInstance(position + 1))
                    .commit();
            return;
        }
        if(position == 1) {
            fragmentManager.beginTransaction()
                    .replace(R.id.container, ScavengeFragment.newInstance(position + 1))
                    .commit();
            return;
        }
        if(position == 2) {
            fragmentManager.beginTransaction()
                    .replace(R.id.container, TrilaterationCanvasFragment.newInstance(position + 1))
                    .commit();
            return;
        }
        fragmentManager.beginTransaction()
                .replace(R.id.container, BaseFragment.newInstance(position + 1))
                .commit();

    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
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

    @Override
    public void onFragmentInteraction(String id) {
        Toast.makeText(this, id, Toast.LENGTH_SHORT).show();
    }
}
