package com.scottnumamoto.ledgers;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
/**
 *
 * @author scottnumamoto
 */
public class Account {

    //TODO implement a starting amount
    private List<Action> actions;
    private String name;
    
    public Account(String n)
    {
        actions = new ArrayList<>();
        name = n;
    }
    
    
    public void action( Action t )
    {
        actions.add(t);
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
            double amount = act.getAmount();
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

    public List<Action> getActions()
    {
        return actions;
    }

    //Creates a string version of all the actions for output
    //Prints in the reverse array order
    public List<String> getStringActions() {
        List<String> result = new ArrayList<>();

        if (actions.isEmpty())
        {
            //TODO research what's appropriate for an empty string
            result.add("HI");
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
    
    
    
}
