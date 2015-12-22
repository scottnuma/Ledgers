package com.scottnumamoto.ledgers;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class switch_account_activity extends ActionBarActivity {

    RadioGroup holder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_switch_account_activity);

        this.setTitle("Switch Accounts");
        holder = (RadioGroup) findViewById(R.id.radioGroupAccountNames);


        Intent intent = getIntent();
        String[] accountTitles = intent.getStringArrayExtra("accountNameArray");
        addRadioButtons(accountTitles);


//
//        alert.setSingleChoiceItems(accountTitles, -1, new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int which) {
//                assert which < accounts.size() : "##TOO BIG NUMBER";
//                mainAccount = accounts.get(which);
//                refreshAll();
//                dialog.dismiss();
//            }
//        });
//        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//
//            public void onClick(DialogInterface dialog, int whichButton) {
//            }
//        });
//

    }

    //SomeAndroidActivity
    private void addRadioButtons(String[] accountTitles) {
        for(int i = 0; i < accountTitles.length; i++) {
            RadioButton radioButton = new RadioButton(this);

            radioButton.setText(accountTitles[i]);


            final Intent result = new Intent();
            result.putExtra("account_name", i);

            radioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setResult(RESULT_OK, result);
                    finish();
                }
            });

            //add it to the group.
            holder.addView(radioButton, i);
        }
    }
}

