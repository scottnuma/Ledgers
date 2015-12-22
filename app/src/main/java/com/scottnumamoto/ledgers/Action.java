
package com.scottnumamoto.ledgers;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author scottnumamoto
 */
public class Action implements Parcelable {
    private double amount;
    private Calendar date;
    private String label;
    private boolean deposit;
    private List<String> tags;
    
    public Action(double a, boolean d)
    {
        amount = a;
        date = new GregorianCalendar();
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

        String result = "" + d.format(date.getTime());
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
        return date;
    }

    public void setLabel(String l) { label = l;}
    public void setAmount(Double d) { amount = d;}
    public void setCalendar(Calendar c){ date = c;}
    public void setDeposit(Boolean b){ deposit = b;}
    
    public String getLabel()
    {
        return label;
    }


    //Implement the parcelable interface

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(amount);
        dest.writeValue(date);
        dest.writeString(label);
        dest.writeValue(deposit);
        dest.writeStringList(tags);
    }

    public static final Parcelable.Creator<Action> CREATOR = new Parcelable.Creator<Action>() {
        public Action createFromParcel(Parcel in) {
            return new Action(in);
        }

        public Action[] newArray(int size) {
            return new Action[size];
        }
    };

    private Action(Parcel in){
        amount = in.readDouble();
        date = new GregorianCalendar();
//                (Calendar) in.readValue(Calendar.class.getClassLoader());
        label = in.readString();
        deposit = (Boolean) in.readValue(Boolean.class.getClassLoader());
        in.readStringList(tags);
    }
}
