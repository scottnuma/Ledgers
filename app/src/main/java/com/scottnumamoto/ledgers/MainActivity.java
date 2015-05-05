package com.scottnumamoto.ledgers;

import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;


public class MainActivity extends ActionBarActivity implements View.OnClickListener {
    TextView mainTextView;
    Button mainButton;
    EditText priceEntry;
    EditText descEntry;
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

        priceEntry = (EditText) findViewById(R.id.editText);
        descEntry = (EditText) findViewById(R.id.editText);


    }

    public void initializeAccount (){
        mSharedPreferences = getSharedPreferences(PREFS, MODE_PRIVATE);
        String output = mSharedPreferences.getString(PREF_ACCOUNT, "");

        //If there is something stored in memory
        if (output.length() > 0)
        {
            Gson gson = new Gson();
            mainAccount = gson.fromJson(output, Account.class);
        }
        else
        {
            mainAccount = new Account("default");
        }
    }


    //Written using: http://www.raywenderlich.com/78576/android-tutorial-for-beginners-part-2
    @Override
    public void onPause(){
        //Default pause stuff
        super.onPause();

        //Encode the data of the main Account as a string
        Gson gson = new Gson();
        String account_json = gson.toJson(mainAccount);

        //Store this string within the SharedPreferences
        mSharedPreferences = getSharedPreferences(PREFS, MODE_PRIVATE);
        SharedPreferences.Editor e = mSharedPreferences.edit();
        e.putString(PREF_ACCOUNT, account_json);
        e.commit();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public void onClick(View v) {
        String input = priceEntry.getText().toString();
        double parsed;
        try
        {
            parsed = Double.parseDouble(input);

            //If the user includes a description, include it in the action
            String name = descEntry.getText().toString();
            if (name.length() > 0) {
                mainAccount.action(parsed);
            }
            else{
                Action a = new Action(parsed, name);
                mainAccount.action(a);
            }
            mainTextView.setText("" + mainAccount.getBalance());

            //Clear the textboxes
            priceEntry.setText("");
            descEntry.setText("");

        }

        //If the user incorrectly enters a number for price, ignore
        catch(NumberFormatException e)
        {

        }
    }
}
