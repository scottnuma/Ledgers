package com.scottnumamoto.ledgers;

import java.util.Calendar;

/**
 * Created by scottnumamoto on 5/14/15.
 */
public class Deposit extends Action {
    public Deposit(double a)
    {
        super(a);
    }

    public Deposit(double a, String l)
    {
        super(a,l);
    }

    public Deposit(double a, Calendar d)
    {
        super(a,d);
    }

    public Deposit(double a, Calendar d, String l)
    {
        super(a,d,l);
    }

    public double getAddendAmount() {
        return super.getAmount();
    }
}
