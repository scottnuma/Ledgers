package com.scottnumamoto.ledgers;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.EditText;
import android.widget.RadioButton;

public class EditActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_action);

        Intent intent = getIntent();


        String label = intent.getStringExtra("label");
        EditText labelBox = (EditText) findViewById(R.id.editName);
        labelBox.setHint(label);

        String amount = intent.getStringExtra("amount");
        EditText amountBox = (EditText) findViewById(R.id.editPrice);
        amountBox.setHint(amount);

        if (intent.getBooleanExtra("deposit", true)){
            RadioButton depositChange = (RadioButton) findViewById(R.id.deposit);
            depositChange.toggle();
        }
    }

    @Override
    public android.support.v4.app.FragmentManager getSupportFragmentManager() {
        return null;
    }
}
