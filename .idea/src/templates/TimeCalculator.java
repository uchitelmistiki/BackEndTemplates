package templates;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

public class TimeCalculator {
    public static Timestamp subtractOneHourFromCurrentTime(){
        Timestamp currentDateTime = new Timestamp(System.currentTimeMillis());
        Calendar c = Calendar.getInstance();
        c.setTime(currentDateTime);
        c.add(Calendar.HOUR, -1);
        Timestamp currentSubtractOneHour = new Timestamp(c.getTimeInMillis());
        return currentSubtractOneHour;
    }

    public static Timestamp subtractOneHourFromEventTime (Timestamp eventTime){
        Calendar c = Calendar.getInstance();
        c.setTime(eventTime);
        c.add(Calendar.HOUR, -1);
        Timestamp currentSubtractOneHour = new Timestamp(c.getTimeInMillis());
        return currentSubtractOneHour;
    }

    public static Timestamp getCurrentDate(){
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY,0);
        cal.set(Calendar.MINUTE,0);
        cal.set(Calendar.SECOND,0);
        cal.set(Calendar.MILLISECOND,0);
        Timestamp currentDate = new Timestamp(cal.getTimeInMillis());
        return currentDate;
    }

    public static Timestamp getCurrentDateAndTime(){
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        Timestamp currentDate = new Timestamp(cal.getTimeInMillis());
        return currentDate;
    }
}
