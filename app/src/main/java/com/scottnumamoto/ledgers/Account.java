package com.scottnumamoto.ledgers;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;

/**
 *
 * @author scottnumamoto
 */
public class Account {

    private List<Action> actions;
    private String name;

    //Delimiter used in CSV file
    private static final String COMMA_DELIMITER = ",";
    private static final String NEW_LINE_SEPARATOR = "\n";
    private static final String FILE_HEADER = "amount,date,label,withdrawal";

    //Student attributes index
    private static final int NUM_AMOUNT = 0;
    private static final int NUM_DATE = 1;
    private static final int NUM_LABEL = 2;
    private static final int NUM_DEPOSIT = 3;

    public Account(String n)
    {
        actions = new ArrayList<>();
        name = n.trim();
    }

    public void action( Action t )
    {
        actions.add(t);
    }

    public void action( List<Action> actions){
        for (int i = 0; i < actions.size(); i++){
            Action temp_action = actions.get(i);
            actions.add(temp_action);
        }
    }

    public double getBalance()
    {
        if (actions.isEmpty())
        {
            return 0;
        }
        double result = 0;
        for (int i = 0; i < actions.size(); i++)
        {
            Action act = actions.get(i);
            double amount = act.getAddendAmount();
            result += amount;
        }

        return result;
    }
    
    //The amount of spending and deposits for the month specified
    //int month should be a Calendar.MONTH object
    public double monthlyChange(int month)
    {
        double result = 0;
        for (Action a : actions)
        {
            if (a.getCalendar().get(Calendar.MONTH) == month)
            {
                result += a.getAddendAmount();
            }
        }
        return result;
    }


    //Net monthly withdrawls and deposits
    public double monthlyChange()
    {
        Calendar today = new GregorianCalendar();
        return monthlyChange(today.get(Calendar.MONTH));
    }

    public String getName()
    {
        return name;
    }
    public List<Action> getActions()
    {
        return actions;
    }

    //Reorders all the actions to be chronological
    public void reorderActions(){
        List<Action> newActions = new ArrayList<>();
        if (actions.size() > 0)
            newActions.add(actions.get(0));

        for (int i = 1; i < actions.size(); i++)
        {
            Action a = actions.get(i);

            int j = 0;
            while( j != newActions.size() && a.getCalendar().after(newActions.get(j).getCalendar()) ){
                j++;
            }

            newActions.add(j, a);
        }

        actions = newActions;

    }

    //Creates a string version of all the actions for output
    //Prints in the reverse array order
    public List<String> getStringActions() {
        List<String> result = new ArrayList<>();

        if (actions.isEmpty())
        {
            return result;
        }
        else {
            List<Action> prev = actions;
            for (int i = prev.size() - 1; i >= 0; i--) {
                result.add(prev.get(i).toString());
            }
            return result;
        }
    }

    //Returns a string of the past couple actions
    public String actionString()
    {
        return actionString(3);
    }

    public String exportString() {
        String result = "Name: " + this.getName() + "\n";
        result += "Actions:\n";
        for (int i = 0; i < actions.size(); i++) {

            result += actions.get(i).toString();
            if (i != actions.size() - 1)
                result += "\n";
        }
        return result;
    }

    //Returns a string of the last n actions
    public String actionString(int numActions)
    {
        String result = "";
        List<Action> actionList = getActions();

        SimpleDateFormat d = new SimpleDateFormat("MM/dd/yy");
        if (actionList.size() >= numActions) {
            for (int i = 0; i < numActions; i++) {
                Action a = actionList.get(actionList.size() - 1 - i);
                result += a.toString() + "\n";
            }
        }
        //If there aren't enough actions to fill the requested number
        else
        {
            for (int i = actionList.size() - 1; i >= 0; i--)
            {
                Action a = actionList.get(i);
                result += a.toString() + "\n";
            }
        }
        return result;
    }

    //Will clear the account of any (incriminating) data
    public void reset()
    {
        actions.clear();
    }

    public String exportAsCSV() {
        String result = "";
        result += FILE_HEADER + NEW_LINE_SEPARATOR;
        for (Action action : actions) {
            result += String.valueOf(action.getAmount());
            result += COMMA_DELIMITER;

            SimpleDateFormat d = new SimpleDateFormat("MM/dd/yy");
            String result2 = "" + d.format(action.getCalendar().getTime());

            result += result2;
            result += COMMA_DELIMITER;
            result += String.valueOf(action.getLabel());
            result += COMMA_DELIMITER;
            boolean desposit = action.getAddendAmount() < 0;
            result += String.valueOf(desposit);
            result += NEW_LINE_SEPARATOR;
        }
        return result;
    }

    public void getFromCSVText( String csv ){
        System.out.println("##Started getFromCSVText");
        List<Action> actionList;
        actionList = new ArrayList();

        String line = "";
        String delims = "\n";
        try {

            String[] separatedString = csv.split(delims);
            for (int i = 1; i < separatedString.length; i++) {
                line = separatedString[i];

                //Get all tokens available in line
                String[] tokens = line.split(COMMA_DELIMITER);
                if (tokens.length > 0) {
                    Double amt = Double.parseDouble(tokens[NUM_AMOUNT]);
                    Calendar c = calendarFromString(tokens[NUM_DATE]);
                    String l = tokens[NUM_LABEL];
                    Boolean d = Boolean.parseBoolean(tokens[NUM_DEPOSIT]);

                    Action a = new Action(amt, d, l);
                    a.setCalendar(c);
                    actionList.add(a);
                }
                System.out.println("##Processing lines");
            }

            System.out.println(actionList);

        }
        catch (Exception e){
            System.out.println("##Error in importing stuff");
            e.printStackTrace();
        }
        finally{
            action(actionList);
        }
    }

    public Calendar calendarFromString (String s){
        String[] tokens = s.split("/");
        int month = Integer.parseInt(tokens[0]) - 1;
        int day = Integer.parseInt(tokens[1]);
        int year = Integer.parseInt(tokens[2]);
        System.out.println(s + "_" + month);

        Calendar result = new GregorianCalendar();
        result.set(year, month, day);
        return result;
    }

    //CSV add actions from CSV file
    public void addFromCSV( String fileName){
        BufferedReader fileReader = null;

        try {
            List<Action> actionList;
            actionList = new ArrayList();

            String line = "";

            //Create the file reader
            fileReader = new BufferedReader(new FileReader(fileName));

            //Read the CSV file header to skip it
            fileReader.readLine();

            //Read the file line by line starting from the second line
            while ((line = fileReader.readLine()) != null) {

                //Get all tokens available in line
                String[] tokens = line.split(COMMA_DELIMITER);
                if (tokens.length > 0) {
                    Double amt = Double.parseDouble(tokens[NUM_AMOUNT]);
                    Calendar c = calendarFromString(tokens[NUM_DATE]);
                    String l = tokens[NUM_LABEL];
                    Boolean d = Boolean.parseBoolean(tokens[NUM_DEPOSIT]);

                    Action a = new Action(amt, d, l);
                    a.setCalendar(c);
                    actionList.add(a);
                }
            }

            actions = actionList;

        }
        catch (Exception e) {
            System.out.println("Error in CsvFileReader !!!");
            e.printStackTrace();
        } finally {
            try {
                fileReader.close();
            } catch (IOException e) {
                System.out.println("Error while closing fileReader !!!");
                e.printStackTrace();
            }
        }
    }

    public void replaceFromCSV( String fileName){
        reset();
        addFromCSV( fileName);
    }
    
    
}
