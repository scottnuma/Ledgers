package com.scottnumamoto.ledgers;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.ArrayList;


public class MainActivity extends ActionBarActivity implements View.OnClickListener {
    TextView mainTextView;
    Button mainButton;
    RadioButton withdrawButton;
    RadioButton depositButton;
    EditText priceEntry;
    EditText descEntry;
    EditText tagsEntry;
    ListView actionList;
    ArrayAdapter mArrayAdapter;

    ArrayList<Account> accounts;
    Account mainAccount;

    private static final String PREFS = "prefs";
    private static final String PREF_ACCOUNT = "account";
    SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeAccounts();

        mainTextView = (TextView) findViewById(R.id.main_textview);


        mainButton = (Button) findViewById(R.id.button);
        mainButton.setOnClickListener(this);

        withdrawButton = (RadioButton) findViewById(R.id.radioButtonWithdrawal);
        depositButton = (RadioButton) findViewById(R.id.radioButtonDeposit);


        //The following is an example of creating function buttons
        /*
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
        */

        priceEntry = (EditText) findViewById(R.id.editText);
        descEntry = (EditText) findViewById(R.id.editText2);
        //tagsEntry = (EditText) findViewById(R.id.editTextTags);

        // 4. Access the ListView
            actionList = (ListView) findViewById(R.id.listView);

            // Create an ArrayAdapter for the ListView
            mArrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1,
                    mainAccount.getStringActions());

            // Set the ListView to use the ArrayAdapter
            actionList.setAdapter(mArrayAdapter);

        actionList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos, long id) {
                System.out.println("##list" + pos);
                createActionEditWindow(pos);

                return true;
            }
        });

        refreshAll();

    }

    public void createActionEditWindow(int pos) {
        assert pos < mainAccount.getActions().size() : "##Weird stuff with the number of actions";
        final int convertedPos = mainAccount.getActions().size() - (1 + pos);
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Delete Action");

        String actionTitle = mainAccount.getActions().get(convertedPos).getLabel();
        if (!actionTitle.isEmpty()) {
            alert.setMessage("Delete " + actionTitle + " transaction?");
        } else {
            DecimalFormat df = new DecimalFormat("$##0.00");
            double actionPrice = mainAccount.getActions().get(convertedPos).getAmount();

            alert.setMessage("Delete " + df.format(actionPrice) + " transaction?");
        }
        alert.setPositiveButton("Okay", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
                mainAccount.getActions().remove(convertedPos);
                refreshAll();
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        alert.show();


    }

    //Commit certain actions upon the main menu being selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.reset_account:
                createResetWindow();
                return true;
            case R.id.new_account:
                createNewAccountWindow();
                return true;
            case R.id.delete_account:
                deleteAccountWindow();
                return true;
            case R.id.switch_account:
                createAccountSwitchWindow();
                return true;
            case R.id.export_account:
                createAccountExportWindow();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void createAccountExportWindow() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Export Account (Copy and Paste)");

        TextView showText = new TextView(this);
        showText.setPadding(50, 50, 50, 50);
        showText.setText(mainAccount.exportString());
        showText.setTextIsSelectable(true);

        alert.setView(showText);

        alert.setPositiveButton("Okay", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        alert.show();
    }

    //Creates a window that will switch between accounts
    private void createAccountSwitchWindow() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        if (accounts.size() > 1) {
            alert.setTitle("Switch Accounts");

            String[] accountTitles = new String[accounts.size()];
            for (int i = 0; i < accounts.size(); i++) {
                accountTitles[i] = accounts.get(i).getName();
            }

            alert.setSingleChoiceItems(accountTitles, -1, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    assert which < accounts.size() : "##TOO BIG NUMBER";
                    mainAccount = accounts.get(which);
                    refreshAll();
                    dialog.dismiss();
                }
            });
            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int whichButton) {
                }
            });
        } else {
            alert.setTitle("Error");
            alert.setMessage("Must have multiple accounts to delete current account");

            // Make a "Okay" button that simply dismisses the alert
            alert.setPositiveButton("Okay", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int whichButton) {
                }
            });

        }
        alert.show();


    }

    private void deleteAccountWindow() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        if (accounts.size() > 1) {
            alert.setTitle("Delete Account");
            alert.setMessage("Are you sure you want to delete " + mainAccount.getName());

            // Make a "YES" button to continue the action
            alert.setPositiveButton("Delete", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int whichButton) {
                    assert accounts.size() > 1 : "##IF statement failure";
                    accounts.remove(mainAccount);


                    mainAccount = accounts.get(0);
                    refreshAll();
                }
            });

            // Make a "Cancel" button that simply dismisses the alert
            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int whichButton) {
                }
            });
        } else {
            alert.setTitle("Error");
            alert.setMessage("Must have multiple accounts to delete current account");

            // Make a "Okay" button that simply dismisses the alert
            alert.setPositiveButton("Okay", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int whichButton) {
                }
            });

        }

        alert.show();
    }

    //Design a window for creating a new account
    private void createNewAccountWindow()
    {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("New Account");
        alert.setMessage("Name:");

        // Create EditText for entry
        final EditText input = new EditText(this);
        alert.setView(input);

        // Make a "YES" button to continue the action
        alert.setPositiveButton("Create", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
                String name = input.getText().toString();
                Account a = new Account(name);
                accounts.add(a);
                mainAccount = a;
                refreshAll();
            }
        });

        // Make a "Cancel" button that simply dismisses the alert
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {}
        });
        alert.show();
    }

    //Design a separate popup window to confirm deletion
    private void createResetWindow()
    {


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
        alert.show();
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
        DecimalFormat df = new DecimalFormat("$##0.00");
        double d = mainAccount.getBalance();

        //If the balance is really small, reset to zero, to not show negative starting balance
        //due to small residuals.
        if (Math.abs(d) < .005 )
            d = 0;
        String accountName = mainAccount.getName();
        mainTextView.setText(accountName + " Balance: " + df.format(d));
    }

    private void initializeAccounts(){
        mSharedPreferences = getSharedPreferences(PREFS, MODE_PRIVATE);
        String output = mSharedPreferences.getString(PREF_ACCOUNT, "");



        //If there is something stored in memory
        if (output.length() > 0 && true)
        {
            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<Account>>() {}.getType();

            try
            {
                accounts = gson.fromJson(output, listType);
            }
            catch (JsonParseException e)
            {
                //If the account cannot be read, as would be expected for the first time after
                //changing the data format stored, do as if there were nothing stored in the gson
                totalInitializeAccounts();
            }


            if ( !accounts.isEmpty())
            {
                mainAccount = accounts.get(0);
            }
            System.out.println("##Exit");
        }
        else
            totalInitializeAccounts();
    }

    //Initialize when there is noting from memory
    private void totalInitializeAccounts()
    {
        accounts = new ArrayList<>();
        accounts.add( new Account("default"));
        assert(!accounts.isEmpty());
        mainAccount = accounts.get(0);
    }

    //Written using: http://www.raywenderlich.com/78576/android-tutorial-for-beginners-part-2
    @Override
    public void onPause(){
        //Default pause stuff
        super.onPause();

        //Encode the data of the main Account as a string
        Gson gson = new Gson();
        String account_json = gson.toJson(accounts);

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
            boolean deposit = depositButton.isChecked();

            //Make sure either one or the other radio button is selected
            assert( withdrawButton.isChecked() != deposit );

            //String tags = tagsEntry.getText().toString();
            String tags = ""; //Not yet ready for tags
            Action a;
            if (name.length() <= 0) {
                if (tags.length() == 0) {
                    a = new Action(parsed, deposit);

                }
                else{
                    a = new Action(parsed, deposit, "", tags);
                }
                mainAccount.action(a);
            }
            else{
                if (tags.length() == 0){
                    a = new Action(parsed, deposit, name);
                }
                else {
                    a = new Action(parsed, deposit, name, tags);
                }
                mainAccount.action(a);
            }

            refreshAll();

            //Clear the textboxes
            descEntry.setText("");
            priceEntry.setText("");
            //tagsEntry.setText("");
        }
    }
}
