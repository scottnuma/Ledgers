
package com.scottnumamoto.ledgers;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Scanner;

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
    private boolean deposit;
    private List<String> tags;
    
    public Action(double a, boolean d)
    {
        amount = a;
        day = new GregorianCalendar();
        label = "";
        deposit = d;
    }
    
    public Action(double a, boolean d, String l)
    {
        this(a, d);
        label = l.trim();
    }

    public Action(double a, boolean d, String l, String t)
    {
        this(a,d,l);
        tags = tagGetter(t);
    }

    private List<String> tagGetter(String t)
    {
        tags = new ArrayList<>();
        Scanner s = new Scanner(t);
        while(s.hasNext())
        {
            tags.add(s.next());
        }
        return tags;
    }

    @Override
    public String toString()
    {
        DecimalFormat df = new DecimalFormat("$##0.00");
        SimpleDateFormat d = new SimpleDateFormat("MM/dd/yy");

        String result = "" + d.format(day.getTime());
        if (!deposit)
        {
            result += " -" + df.format(amount);
        } else {
            result += " " + df.format(amount);
        }
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

    public double getAddendAmount()
    {
        if (deposit)
            return amount;
        else
            return -1 * amount;
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
