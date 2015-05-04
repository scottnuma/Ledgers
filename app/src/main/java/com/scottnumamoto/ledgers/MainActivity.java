package com.scottnumamoto.ledgers;

import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity implements View.OnClickListener {
    TextView mainTextView;
    Button mainButton;
    EditText mainTextBox;
    Account mainAccount;

    private static final String PREFS = "prefs";
    private static final String PREF_ACCOUNT = "account";
    SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeAccount();

        mainTextView = (TextView) findViewById(R.id.main_textview);
        mainTextView.setText("$" + mainAccount.getBalance());

        mainButton = (Button) findViewById(R.id.button);
        mainButton.setOnClickListener(this);

        mainTextBox = (EditText) findViewById(R.id.editText);

        mainAccount = new Account("new one");

    }

    public void initializeAccount (){
        mSharedPreferences = getSharedPreferences(PREFS, MODE_PRIVATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public void onClick(View v) {
        String input = mainTextBox.getText().toString();
        double parsed;
        try
        {
            parsed = Double.parseDouble(input);
            mainAccount.action(parsed);
            mainTextView.setText("" + mainAccount.getBalance());

        }
        catch(NumberFormatException e)
        {

        }
    }
}
