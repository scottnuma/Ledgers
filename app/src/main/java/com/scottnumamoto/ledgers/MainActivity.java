package com.scottnumamoto.ledgers;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
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
import java.util.Calendar;
import java.util.GregorianCalendar;


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

    //For Edit Action Dialog
    EditText nameChange;
    EditText priceChange;
    RadioButton depositChange;
    RadioButton withdrawChange;

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

        actionList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println("##list (short)" + position);
                shortClickAction(position);
            }


        });

        actionList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos, long id) {
                System.out.println("##list" + pos);
                longClickAction(pos);

                return true;
            }
        });

        refreshAll();

    }

    /*
    private void editCalendar()
    {
        //Change the time of action

        final DatePicker dateChange = (DatePicker) dialog.findViewById(R.id.datePicker);
        int year = a.getCalendar().get(Calendar.YEAR);
        int month = a.getCalendar().get(Calendar.MONTH);
        int day = a.getCalendar().get(Calendar.DAY_OF_MONTH);
        dateChange.updateDate(year, month, day);

        int year = dateChange.getYear();
        int month = dateChange.getMonth();
        int day = dateChange.getDayOfMonth();
        Calendar c = GregorianCalendar.getInstance();
        c.set(year, month, day);

        a.setCalendar(c);
    }*/
    private void longClickAction(int pos){

        assert mainAccount.getActions().size() > 0 : "##There should be some actions here";
        final Action a = mainAccount.getActions().get(mainAccount.getActions().size() - (1 + pos));
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Delete Transaction");
        if (a.getLabel() != "") {
            alert.setMessage("Delete transaction titled " + a.getLabel() + "?");
        }
        else
        {
            alert.setMessage("Delete transaction titled [blank]?");
        }
        alert.setPositiveButton("Okay", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
                mainAccount.getActions().remove(a);
                refreshAll();
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        alert.show();

    }
    private void shortClickAction(final int pos){
        assert mainAccount.getActions().size() > 0 : "##There should be some actions here";
        final Action a = mainAccount.getActions().get(mainAccount.getActions().size() - (1 + pos));

        // create a Dialog component
        final Dialog dialog = new Dialog(this);

        //tell the Dialog to use the dialog.xml as it's layout description
        dialog.setContentView(R.layout.editaction);
        dialog.setTitle("Edit Action");

        //Fill in the presets
        nameChange = (EditText) dialog.findViewById(R.id.editName);
        nameChange.setHint(a.getLabel());

        priceChange = (EditText) dialog.findViewById(R.id.editPrice);
        DecimalFormat df = new DecimalFormat("##0.00");
        priceChange.setHint("" + df.format(a.getAmount()));

        withdrawChange = (RadioButton) dialog.findViewById(R.id.withdraw);
        depositChange = (RadioButton) dialog.findViewById(R.id.deposit);
        if (a.getAddendAmount() > 0)
            depositChange.toggle();

        Button cancelButton = (Button) dialog.findViewById(R.id.button3);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        Button calendarButton = (Button) dialog.findViewById(R.id.buttonCalendar);
        calendarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                windowEditActionDate(pos);
                dialog.dismiss();
            }
        });

        Button confirmButton = (Button) dialog.findViewById(R.id.button2);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = nameChange.getText().toString();
                if (!title.isEmpty())
                    a.setLabel(title);

                String price = priceChange.getText().toString();
                if (!price.isEmpty()) {
                    //Only change the amount if the user correctly writes a double
                    String input = priceChange.getText().toString();
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
                        a.setAmount(parsed);
                }

                if (depositChange.isChecked())
                    a.setDeposit(true);
                else
                    a.setDeposit(false);


                dialog.dismiss();
                refreshAll();
            }
        });


        dialog.show();



    }

    private void windowEditActionDate(int pos) {
        assert mainAccount.getActions().size() > 0 : "##There should be some actions here";
        final Action a = mainAccount.getActions().get(mainAccount.getActions().size() - (1 + pos));

        // create a Dialog component
        final Dialog dialog = new Dialog(this);

        final DatePicker dateChange = (DatePicker) dialog.findViewById(R.id.datePicker);
        int year = a.getCalendar().get(Calendar.YEAR);
        int month = a.getCalendar().get(Calendar.MONTH);
        int day = a.getCalendar().get(Calendar.DAY_OF_MONTH);
        dateChange.updateDate(year, month, day);
        //TODO find out why dateChange is showing up as null



        //tell the Dialog to use the dialog.xml as it's layout description
        dialog.setContentView(R.layout.calendar);
        dialog.setTitle("Edit Action");

        Button cancelButton = (Button) dialog.findViewById(R.id.calButtonCancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });


        Button confirmButton = (Button) dialog.findViewById(R.id.calButtonConfirm);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int newYear = dateChange.getYear();
                int newMonth = dateChange.getMonth();
                int newDay = dateChange.getDayOfMonth();
                Calendar c = GregorianCalendar.getInstance();
                c.set(newYear, newMonth, newDay);

                a.setCalendar(c);

                dialog.dismiss();
            }
        });
        dialog.show();
    }



    //Commit certain actions upon the main menu being selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.reset_account:
                windowResetAccount();
                return true;
            case R.id.new_account:
                windowCreateNewAccount();
                return true;
            case R.id.delete_account:
                windowDeleteAccount();
                return true;
            case R.id.switch_account:
                windowSwitchAccount();
                return true;
            case R.id.export_account:
                windowExportAction();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void windowExportAction() {
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
    private void windowSwitchAccount() {
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


    private void windowDeleteAccount() {
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
    private void windowCreateNewAccount()
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
    private void windowResetAccount()
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
