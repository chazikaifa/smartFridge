package nus.iss5451.smartfridge;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.IBinder;
import android.util.Log;
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import nus.iss5451.smartfridge.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.ScrollView;
import android.widget.TextView;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Dictionary;
import java.util.Map;

import androidx.navigation.fragment.NavHostFragment;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private DataFetchingService dataFetchingService = null;
    private ArrayList<Item> itemArray = null;
    private ArrayList<Object> historyArray = null;

    private Dictionary<Item, TextView>  ArrayDict = null;

    ScrollView ingredientDisplay = null;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ingredientDisplay = findViewById(R.id.IngredientsDisplay);

        TextView humidity = findViewById(R.id.HumidityValue);
        TextView temperature = findViewById(R.id.TemperatureValue);


        ServiceConnection serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                dataFetchingService = ((DataFetchingService.LocalBinder)iBinder).getService();
                DataFetchingService.MyCallback itemCallback = new DataFetchingService.MyCallback() {
                    @Override
                    public void onDataUpdate(ArrayList data) {
                        itemArray = data;

//                        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
//                        for(Item item : itemArray) {
//                            if (item.expiredDate.equals("")) {
//                                item.expiredDate = ft.format(new Date());
//                            }
//                        }
//                        Log.d("[MainActivity]",data.toString());
                        //If you want to update the items in the realtime database, change isRemote to 'true'.
                        //Otherwise it will just update items locally.
                        //Note that update realtime database will trigger onDataUpdate(),
                        //So, do NOT update the realtime database here, or it may create a loop.
//                        dataFetchingService.updateItem(data, false,
//                                (error, ref) -> Log.d("[MainActivity]","update Complete!"));

                    }

                    @Override
                    public void onDataCanceled(DatabaseError error) {

                    }
                };
                dataFetchingService.setItemCallback(itemCallback);

                DataFetchingService.MyCallback historyCallback = new DataFetchingService.MyCallback() {
                    @Override
                    public void onDataUpdate(ArrayList data) {
                        historyArray = data;
                    }

                    @Override
                    public void onDataCanceled(DatabaseError error) {

                    }
                };
                dataFetchingService.setLogCallback(historyCallback);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                dataFetchingService = null;
            }
        };
        bindService(new Intent(this,DataFetchingService.class),serviceConnection,BIND_AUTO_CREATE);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
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

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void ChangeValueForTextView(TextView textView, float x, int decimalPlace){
        BigDecimal bd = new BigDecimal(x);
        bd = bd.setScale(2);
        textView.setText(String.valueOf(bd.floatValue()));
    }
    private void AddIngredient(Item item){
        TextView tv = new TextView(this);
        tv.setText(item.type + "  " + item.expiredDate);
        ingredientDisplay.addView((tv));
    }
}