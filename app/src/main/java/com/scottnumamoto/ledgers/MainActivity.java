package com.scottnumamoto.ledgers;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
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
import java.util.Set;


public class MainActivity extends ActionBarActivity implements View.OnClickListener {
    public static final String PREFIX = "com.scottnumamoto.ledgers.";
    public static final int CHANGE_ACTION = 24601;

    TextView mainTextView;
    Button mainButton;
    RadioButton withdrawButton;
    RadioButton depositButton;
    EditText priceEntry;
    EditText descEntry;
    EditText tagsEntry;
    ListView actionList;
    ArrayAdapter mArrayAdapter;

    DrawerLayout mDrawerLayout;
    ListView mDrawerList;

    ArrayList<Account> accounts;
    Account mainAccount;

    private static final String PREFS = "prefs";
    private static final String PREF_ACCOUNT = "account";
    private static final String PREF_ACCOUNT_INDEX = "index";
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

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        actionList = (ListView) findViewById(R.id.listView);

        if (mainAccount == null){
            mainAccount = new Account("Default"); // If there is not already a main account, make one
        }
        mArrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1,
            mainAccount.getStringActions());

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

    //Store the index of an account in memory to load upon initalizing app
    private void setRememberedAccount(int accountIndex){
        mSharedPreferences = getSharedPreferences(PREFS, MODE_PRIVATE);
        SharedPreferences.Editor e = mSharedPreferences.edit();
        e.putInt(PREF_ACCOUNT_INDEX, accountIndex);
        e.commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == CHANGE_ACTION) {
            //interpret the data given from the intent

            //Check if correctly received index of action to modify before retreiving
            int index = data.getIntExtra("index", -2);
            assert index != -1: "##Index not received properly in EditActivity";
            assert index != -2: "##Index not received properly in MainActivity";
            Action a = mainAccount.getActions().get(index);


            //Modify the action

            //bundle contains a variable number of key pairs, each requiring different actions
            Bundle bundle = data.getExtras();
            Set<String> stuff = bundle.keySet();

            //Look at each key and take appropriate action for each
            for( String key : stuff){
                switch(key){
                    case "day":
                        Calendar newDate = GregorianCalendar.getInstance();
                        newDate.set(bundle.getInt("year"), bundle.getInt("month"), bundle.getInt("day"));
                        a.setCalendar(newDate);
                        break;
                    case "label":
                        a.setLabel( bundle.getString("label"));
                        break;
                    case "amount":
                        a.setAmount( bundle.getDouble("amount"));
                        break;
                    case "deposit":
                        a.setDeposit( bundle.getBoolean("deposit"));
                        break;
                    default:
                        assert false : "##Unfamiliar case label received";
                        break;
                }
            }
            refreshAll();
        }
    }

    private void switchMainAccount(int index)
    {
        assert index < accounts.size() : "##TOO BIG NUMBER";

        mainAccount = accounts.get(index);
        refreshAll();

        setRememberedAccount(index);
    }

    private void longClickAction(int pos){
        assert mainAccount.getActions().size() > 0 : "##There should be some actions here";
        final Action a = mainAccount.getActions().get(mainAccount.getActions().size() - (1 + pos));
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Delete Transaction");
        if (a.getLabel() != "") {
            alert.setMessage("Delete transaction titled " + a.getLabel() + "?");
        } else {
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
        assert mainAccount.getActions().size() > 0 : "##Check for actions actually here";
        int actionIndex = mainAccount.getActions().size() - (1 + pos);
        Action a = mainAccount.getActions().get(actionIndex);

        //Testing longClickAction with new Activity EditAction
        Intent intent = new Intent(this, EditActivity.class);

        intent.putExtra("index", actionIndex);

        intent.putExtra("label", a.getLabel());

        DecimalFormat df = new DecimalFormat("##0.00");
        String alpha = df.format(a.getAmount());
        intent.putExtra("amount", alpha);

        intent.putExtra("deposit", a.getAddendAmount() > 0);


        startActivityForResult(intent, CHANGE_ACTION);
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
            case R.id.export_account:
                System.out.println("##Exporting");
                windowExportAction();
                return true;
            case R.id.import_actions:
                System.out.println("##Okiedoke");
                mainAccount.getFromCSVText("amount,date,label,deposit\n" +
                        "250.0,12/01/15,December budget,false\n" +
                        "28.0,12/05/15,Pie banquet,true\n" +
                        "13.0,12/05/15,Pie banquet subsidy,false\n" +
                        "4.0,12/09/15,Food from Emily,true\n" +
                        "6.95,12/09/15,Pancho's,true\n" +
                        "10.71,12/09/15,Philippino food,true");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void windowImportAction() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Import from CSV");
        TextView showText = new TextView(this);
        alert.setView(showText);
        alert.setPositiveButton("Okay", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        alert.show();
    }

    private void windowExportAction() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Copied to Clipboard");

        TextView showText = new TextView(this);
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(this.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("Message", mainAccount.exportAsCSV());
        clipboard.setPrimaryClip(clip);
        alert.setView(showText);

        alert.setPositiveButton("Okay", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        alert.show();
    }

    String[] accountTitles( ArrayList<Account> accountList){
        String[] result = new String[accountList.size()];
        for (int i = 0; i < accountList.size(); i++){
            result[i] = accounts.get(i).getName();
        }
        return result;
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
                    setRememberedAccount(0);
                    refreshAll();
                }
            });

            // Make a "Cancel" button that simply dismisses the alert
            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {}
            });
        } else {
            alert.setTitle("Error");
            alert.setMessage("Must have multiple accounts to delete current account");

            // Make a "Okay" button that simply dismisses the alert
            alert.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {}
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

            public void onClick(DialogInterface dialog, int whichButton) {
            }
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

            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        alert.show();
    }

    //Refreshes everything necessary when adding a new action
    private void refreshAll()
    {
        mainAccount.reorderActions();
        refreshActionList();
        refreshBalance();
        refreshDrawer();
    }

    private void refreshDrawer()
    {
        String[] accountTitles = accountTitles(accounts);
        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, accountTitles));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        mDrawerList.setItemChecked(accounts.indexOf(mainAccount), true);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            switchMainAccount(position);
            mDrawerList.setItemChecked(position, true);
            mDrawerLayout.closeDrawer(mDrawerList);
        }
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

    private void initializeAccounts()
    {
        mSharedPreferences = getSharedPreferences(PREFS, MODE_PRIVATE);
        String output = mSharedPreferences.getString(PREF_ACCOUNT, "");

        //If there is something stored in memory
        if (output.length() > 0 && true)
        {
            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<Account>>() {}.getType();

            try {
                accounts = gson.fromJson(output, listType);
            }
            catch (JsonParseException e) {
                //If the account cannot be read, as would be expected for the first time after
                //changing the data format stored, do as if there were nothing stored in the gson
                totalInitializeAccounts();
            }


            if ( !accounts.isEmpty())
            {
                mSharedPreferences = getSharedPreferences(PREFS, MODE_PRIVATE);
                int remember_index = mSharedPreferences.getInt(PREF_ACCOUNT_INDEX, -1);
                assert remember_index > 0 : "##Invalid remembered account index";
                assert remember_index <  accounts.size() : "##remmebed account index too big";

                if (remember_index > 0) {
                    mainAccount = accounts.get(remember_index);
                }
                else {
                    mainAccount = accounts.get(0);
                }
            }
            System.out.println("##Exit");
        }
        else
            totalInitializeAccounts();
    }

    //Initialize when there is noting from memory
    private void totalInitializeAccounts() {
        accounts = new ArrayList<>();
        accounts.add(new Account("Default"));
        assert(!accounts.isEmpty());
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
