package xyz.andrewh.remotify;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Applications extends AppCompatActivity {
    ArrayList<ApplicationModel> applicationModels = new ArrayList<>();
    ListView listView;
    private static ListAdapter adapter;
    private String TAG = this.getClass().getSimpleName();
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_applications);

        databaseHelper = new DatabaseHelper(this);

        listView=(ListView)findViewById(R.id.application_list);

        ArrayList<String> databasePackages = readSettings();

        if(databasePackages.isEmpty()) {
            Log.d(TAG, "Database empty");
        }
            PackageManager pm = getPackageManager();
            List<ApplicationInfo> apps = pm.getInstalledApplications(0);

            List<ApplicationInfo> installedApps = new ArrayList<>();

            for(ApplicationInfo app : apps) {
                //checks for flags; if flagged, check if updated system app
                if((app.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                    installedApps.add(app);
                    //it's a system app, not interested
                } else if ((app.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                    //Discard this one
                    //in this case, it should be a user-installed app
                } else {
                    installedApps.add(app);
                }
            }

            for(ApplicationInfo appInfo : installedApps){
                boolean isChecked = false;
                if(databasePackages.contains(appInfo.packageName)){
                    isChecked = true;
                }
                ApplicationModel temp = new ApplicationModel(appInfo.loadLabel(pm).toString(), appInfo.packageName, isChecked);
                applicationModels.add(temp);
            }


        adapter= new ListAdapter(applicationModels, getApplicationContext());

        listView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.checkmark, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.done:
                saveSettings();
                Intent main = new Intent(this, MainActivity.class);
                startActivity(main);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void saveSettings(){
        for(ApplicationModel app : applicationModels){
            if(app.isChecked()){
                databaseHelper.save(app);
            }
        }
    }

    private ArrayList<String> readSettings(){
        return databaseHelper.findAllPackages();
    }
}


