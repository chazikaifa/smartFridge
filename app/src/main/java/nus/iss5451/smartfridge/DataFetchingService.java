package nus.iss5451.smartfridge;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.LongDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DataFetchingService extends Service {

    private final String TAG = "[Firebase Service]";
    private FirebaseDatabase database;
    private DatabaseReference ref_log,ref_item;
    private final LocalBinder binder = new LocalBinder();
    private ArrayList<Item> itemArray = null;
    private Map<String,Object> recent_log = new HashMap<>();
    private ArrayList<String> tempItem = null;
    private boolean syn_flag = false;

    public interface MyCallback{
        void onDataUpdate(Object data);
        void onDataCanceled(DatabaseError error);
    }

    private MyCallback logCallback = null;
    private MyCallback itemCallback = null;

    public class LocalBinder extends Binder{
        DataFetchingService getService(){
            return DataFetchingService.this;
        }
    }

    public Map<String, Object> getLog(){
        return recent_log;
    }
    public ArrayList getItems(){return itemArray;}

    public void updateItem(ArrayList<Item> itemArray, boolean isRemote, DatabaseReference.CompletionListener listener){
        this.itemArray = new ArrayList<>(itemArray);
        if(itemArray.size() == 0){
            itemArray.add(new Item("No data"));
        }
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

    private void synItems(ArrayList<String> itemString){
        if(itemString.size() == 1 && itemString.get(0).equals("No data")){
            itemArray = new ArrayList<>();
        }else{
            ArrayList<Item> newArray = new ArrayList<>();
            for(int i=0;i<itemString.size();i++){
                for(int j=0;j<itemArray.size();j++){
                    Item t = itemArray.get(j);
                    if(t.type.equals(itemString.get(i))){
                        newArray.add(t);
                        itemArray.remove(j);
                        itemString.remove(i--);
                        break;
                    }
                }
            }
            if(itemString.size()>0){
                for(String is:itemString){
                    Item tmp = new Item(is);
                    newArray.add(tmp);
                }
            }
            itemArray = new ArrayList<>(newArray);
        }
        updateItem(itemArray,true,null);
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
                if(itemArray.size() == 1 && itemArray.get(0).type.equals("No data")){
                    itemArray = new ArrayList<>();
                }
                if(syn_flag && tempItem != null){
                    synItems(tempItem);
                    syn_flag = false;
                    tempItem = null;
                }
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

        ref_log = database.getReference("Most Recent");
        ref_log.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                recent_log = (Map<String, Object>) dataSnapshot.getValue();
                assert recent_log != null;
                //If item data haven't receive, set flag true and wait for the items
                ArrayList<String> itemString = (ArrayList<String>) recent_log.get("item(s)");
                if(itemArray == null){
                    syn_flag = true;
                    tempItem = itemString;
                }else{
                    synItems(itemString);
                }
                if(logCallback != null){
                    logCallback.onDataUpdate(recent_log);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read recent log value.", error.toException());
                if(logCallback != null){
                    logCallback.onDataCanceled(error);
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
