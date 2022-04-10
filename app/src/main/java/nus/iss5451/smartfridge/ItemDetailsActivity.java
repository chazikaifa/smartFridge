package nus.iss5451.smartfridge;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import nus.iss5451.smartfridge.databinding.ActivityItemDetailsBinding;

public class ItemDetailsActivity extends AppCompatActivity {

    ActivityItemDetailsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityItemDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = this.getIntent();

        if (intent != null) {

            String itemName = intent.getStringExtra("itemName");

            binding.itemName.setText(itemName);


        }

    }
}