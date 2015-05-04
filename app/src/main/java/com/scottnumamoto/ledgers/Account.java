package com.scottnumamoto.ledgers;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.util.*;
/**
 *
 * @author scottnumamoto
 */
public class Account {
    private double balance;
    private List<Action> actions;
    private String name;
    
    public Account(String n)
    {
        balance = 0;
        actions = new ArrayList<Action>();
        name = n;
    }
    
    
    public void action( Action t )
    {
        actions.add(t);
        balance += t.getAmount();
    }
    
    public void action( double amount )
    {
        Action t = new Action(amount);
        action(t);
    }
    
    public double getBalance()
    {
        return balance;
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
                result += a.getAmount();
            }
        }
        return result;
    }
    
    public double monthlyChange()
    {
        Calendar today = new GregorianCalendar();
        return monthlyChange(today.get(Calendar.MONTH));
    }
    
    
    
    
}
