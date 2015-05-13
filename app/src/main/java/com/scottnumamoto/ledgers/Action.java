
package com.scottnumamoto.ledgers;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author scottnumamoto
 */
public class Action {
    private double amount;
    private Calendar day;
    private String label;
    
    public Action(double a)
    {
        amount = a;
        day = new GregorianCalendar();
        label = "";
    }
    
    public Action(double a, String l)
    {
        this(a);
        label = l;
    }
    
    public Action(double a, Calendar d)
    {
        this(a);
        day = d;
    }
    
    public Action(double a, Calendar d, String l)
    {
        this(a,d);
        label = l;
    }

    @Override
    public String toString()
    {
        DecimalFormat df = new DecimalFormat("$###.00");
        SimpleDateFormat d = new SimpleDateFormat("MM/dd/yy");
        String result = "" + d.format(day.getTime());
        result += " " + df.format(amount);
        result += " " + label;
        return result;
    }
    
    public void increaseAmount(double a)
    {
        amount += a;
    }
    
    public double getAmount()
    {
        return amount;
    }
    
    public Calendar getCalendar()
    {
        return day;
    }
    
    public String getLabel()
    {
        return label;
    }
}
