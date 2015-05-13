package com.scottnumamoto.ledgers;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.text.DecimalFormat;
import java.util.ArrayList;


public class MainActivity extends ActionBarActivity implements View.OnClickListener {
    TextView mainTextView;
    Button mainButton;
    Button resetButton;
    EditText priceEntry;
    EditText descEntry;
    Account mainAccount;
    ListView actionList;
    ArrayAdapter mArrayAdapter;

    private static final String PREFS = "prefs";
    private static final String PREF_ACCOUNT = "account";
    SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeAccount();

        mainTextView = (TextView) findViewById(R.id.main_textview);
        refreshBalance();


        mainButton = (Button) findViewById(R.id.button);
        mainButton.setOnClickListener(this);



        //Create a button independent of the activity listener

        resetButton = (Button) findViewById(R.id.resetButton);

            //Design a separate popup window to confirm deletion

            final AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Reset");
            alert.setMessage("Are you sure you want to clear the account?");

            // Make a "YES" button to continue the action
            alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int whichButton) {
                    mainAccount.reset();
                    refreshAll();
                }
            });

            // Make a "Cancel" button that simply dismisses the alert
            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int whichButton) {}
            });

        //Create the listener to do actions and such
        View.OnClickListener resetButtonListener = new Button.OnClickListener(){
            public void onClick(View v) {
                //Display the creation upon request
                alert.show();
            }
        };
        //Connect the listener and action
        resetButton.setOnClickListener(resetButtonListener);

        priceEntry = (EditText) findViewById(R.id.editText);
        descEntry = (EditText) findViewById(R.id.editText2);

        // 4. Access the ListView
        actionList = (ListView) findViewById(R.id.listView);

        // Create an ArrayAdapter for the ListView
        mArrayAdapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1,
                mainAccount.getStringActions());

        // Set the ListView to use the ArrayAdapter
        actionList.setAdapter(mArrayAdapter);
        refreshActionList();

    }

    //Refreshes everything necessary when adding a new action
    private void refreshAll()
    {
        refreshActionList();
        refreshBalance();
    }

    private void refreshActionList()
    {

        // Recreate an ArrayAdapter for the ListView
        mArrayAdapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1,
                mainAccount.getStringActions()); //Necessary, since can't directly access real getStringActions

        // Set the ListView to use the ArrayAdapter
        actionList.setAdapter(mArrayAdapter);
    }

    private void refreshBalance()
    {
        DecimalFormat df = new DecimalFormat("$###.00");
        mainTextView.setText("Current Balance: " + df.format(mainAccount.getBalance()));
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
        double parsed = 0;

        boolean parseWorks = true;
        //This try statement breaks if the input is not a double
        try {
            parsed = Double.parseDouble(input);
        }

        //If the user incorrectly enters a number for price, ignore
        catch(NumberFormatException e)
        {
            parseWorks = false;
        }

        if (parseWorks)
        {
            //If the user includes a description, include it in the action
            String name = descEntry.getText().toString();
            if (name.length() <= 0) {
                mainAccount.action(parsed);
            }
            else{
                Action a = new Action(parsed, name);
                mainAccount.action(a);
            }

            refreshBalance();
            refreshActionList();

            //Clear the textboxes
            descEntry.setText("");
            priceEntry.setText("");
        }
    }
}
