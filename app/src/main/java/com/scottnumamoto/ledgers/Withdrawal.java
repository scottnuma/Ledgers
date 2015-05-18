package com.scottnumamoto.ledgers;

import java.util.Calendar;

/**
 * Created by scottnumamoto on 5/14/15.
 */
public class Withdrawal extends Action {

    public Withdrawal(double a)
    {
        super(a);
    }

    public Withdrawal(double a, String l)
    {
        super(a,l);
    }

    public Withdrawal(double a, Calendar d)
    {
        super(a,d);
    }

    public Withdrawal(double a, Calendar d, String l)
    {
        super(a,d,l);
    }

    @Override
    public double getAddendAmount() {
        return super.getAmount();
    }


}
