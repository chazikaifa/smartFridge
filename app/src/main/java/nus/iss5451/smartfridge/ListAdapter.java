package nus.iss5451.smartfridge;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class ListAdapter extends ArrayAdapter<Item> {


    public ListAdapter(Context context, ArrayList<Item> userArrayList){

        super(context,R.layout.item_layout,userArrayList);

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        Item item = getItem(position);

        if (convertView == null){

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_layout,parent,false);

        }

        TextView itemName = convertView.findViewById(R.id.item_name);
        TextView expiryDate = convertView.findViewById(R.id.expiryDate);

        itemName.setText(item.type);
        expiryDate.setText(item.expiredDate);

        return convertView;
    }
}
