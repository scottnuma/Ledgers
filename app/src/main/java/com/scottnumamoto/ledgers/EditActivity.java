package com.scottnumamoto.ledgers;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;

public class EditActivity extends ActionBarActivity {
    private boolean changed;
    private int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_action);

        //Parse intent to fill with relevant information

        Intent intent = getIntent();

        index = intent.getIntExtra("index", -1);

        String label = intent.getStringExtra("label");
        final EditText labelBox = (EditText) findViewById(R.id.editName);
        labelBox.setHint(label);

        String amount = intent.getStringExtra("amount");
        final EditText amountBox = (EditText) findViewById(R.id.editPrice);
        amountBox.setHint(amount);

        final RadioButton depositChange = (RadioButton) findViewById(R.id.deposit);
        if (intent.getBooleanExtra("deposit", true)) {

            depositChange.toggle();
        }

        //Add functionality to the buttons

        Button confirmButton = (Button) findViewById(R.id.button2);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent result = new Intent();

                result.putExtra("index", index);

                DatePicker dateChange = (DatePicker) findViewById(R.id.datePicker2);
                result.putExtra("day", dateChange.getDayOfMonth());
                result.putExtra("month", dateChange.getMonth());
                result.putExtra("year", dateChange.getYear());

                String title = labelBox.getText().toString();
                if (!title.isEmpty())
                    result.putExtra("label", title);

                String price = amountBox.getText().toString();
                if (!price.isEmpty()) {
                    //Only change the amount if the user correctly writes a double
                    String input = amountBox.getText().toString();
                    double parsed = 0;

                    boolean parseWorks = true;
                    //This try statement breaks if the input is not a double
                    try {
                        parsed = Double.parseDouble(input);
                    }

                    //If the user incorrectly enters a number for price, ignore
                    catch (NumberFormatException e) {
                        parseWorks = false;
                    }

                    if (parseWorks)
                        result.putExtra("amount", parsed);
                }

                if (depositChange.isChecked())
                    result.putExtra("deposit", true);
                else
                    result.putExtra("deposit", false);

                setResult( RESULT_OK, result);
                finish();
            }
        });


    }
}
