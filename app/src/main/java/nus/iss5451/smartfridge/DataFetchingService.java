package nus.iss5451.smartfridge;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

public class DataFetchingService extends Service {

    private final String TAG = "[Firebase Service]";
    private FirebaseDatabase database;
    private DatabaseReference ref_log,ref_item;
    private final LocalBinder binder = new LocalBinder();
    private ArrayList<Item> itemArray = new ArrayList<>();
    private ArrayList<Object> historyArray = new ArrayList<>();

    public interface MyCallback{
        void onDataUpdate(ArrayList data);
        void onDataCanceled(DatabaseError error);
    }

    private MyCallback logCallback = null;
    private MyCallback itemCallback = null;

    public class LocalBinder extends Binder{
        DataFetchingService getService(){
            return DataFetchingService.this;
        }
    }

    public ArrayList getHistory(){
        return historyArray;
    }
    public ArrayList getItems(){return itemArray;}

    public void updateItem(ArrayList<Item> itemArray, boolean isRemote, DatabaseReference.CompletionListener listener){
        this.itemArray = itemArray;
        if(isRemote){
            this.ref_item.setValue(itemArray, null, listener);
        }
    }
    public void setLogCallback(MyCallback callback){
        this.logCallback = callback;
    }
    public void setItemCallback(MyCallback callback){
        this.itemCallback = callback;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        database = FirebaseDatabase.getInstance();
        ref_log = database.getReference("Log");
        ref_log.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Map<String,Object> history = (Map<String, Object>) dataSnapshot.getValue();
                assert history != null;
                historyArray = new ArrayList<>();
                for(String key:history.keySet()){
                    historyArray.add(history.get(key));
                }
                if(logCallback != null){
                    logCallback.onDataUpdate(historyArray);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read history value.", error.toException());
                if(logCallback != null){
                    logCallback.onDataCanceled(error);
                }
            }
        });

        ref_item = database.getReference("items");
        ref_item.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                ArrayList items = (ArrayList) dataSnapshot.getValue();
                itemArray = new ArrayList<>();
                for(Object itemMap:items){
                    Item item = new Item((Map)itemMap);
                    itemArray.add(item);
                }
                Log.d(TAG,itemArray.toString());
                if(itemCallback != null){
                    itemCallback.onDataUpdate(itemArray);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read items value.", error.toException());
                if(itemCallback != null){
                    itemCallback.onDataCanceled(error);
                }
            }
        });
    }

    @Override
    public boolean onUnbind(Intent intent) {
        this.logCallback = null;
        this.itemCallback = null;
        return super.onUnbind(intent);
    }
}
