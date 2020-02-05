package hu.vtominator.edu.Model;


import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;


public class Event {
    private static final String TAG = "Event";


    private int event_id;
    private String main_category;
    private String side_category;
    private String eventname;
    private String organiser;
    private String date;
    private String time;
    private String location;
    private String description;
    private String picture;
    private int pinned;

    private boolean favorite;
    private int like, comment;


    public Event(int event_id, String main_category, String side_category, String eventname, String organiser, String date, String time, String location, String description, String picture, int pinned) {
        this.event_id = event_id;
        this.main_category = main_category;
        this.side_category = side_category;
        this.eventname = eventname;
        this.organiser = organiser;
        this.date = date;
        this.time = time;
        this.location = location;
        this.description = description;
        this.picture = picture;
        this.pinned = pinned;
    }


    public static long getTimeLeft(Event currentEvent) {
        long diff;

        String pattern = "yyyy-MM-dd HH:mm:ss";
        String date = currentEvent.getDate() + " " + currentEvent.getTime();


        Date today = new Date();

        try {
            DateFormat df = new SimpleDateFormat(pattern);
            Date eventDay = df.parse(date);
            Log.d(TAG, "getTimeLeft: " + eventDay);
            diff = eventDay.getTime() - today.getTime();
            return diff;


        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static final Comparator<Event> BY_DATE = new Comparator<Event>() {

        @Override
        public int compare(Event event, Event o2) {
            if (getTimeLeft(event) < getTimeLeft(o2)) return -1;
            else if (getTimeLeft(event) > getTimeLeft(o2)) return 1;
            return 0;
        }
    };
    public static final Comparator<Event> BY_DATE_DESC = new Comparator<Event>() {

        @Override
        public int compare(Event event, Event o2) {
            if (getTimeLeft(event) < getTimeLeft(o2)) return 1;
            else if (getTimeLeft(event) > getTimeLeft(o2)) return -1;
            return 0;
        }
    };

    public static final Comparator<Event> BY_NAME = new Comparator<Event>() {

        @Override
        public int compare(Event event, Event o2) {
            return event.getEventname().compareTo(o2.getEventname());
        }
    };

    public static final Comparator<Event> BY_PRIOR = new Comparator<Event>() {
        @Override
        public int compare(Event event, Event o2) {
            if (event.getPinned() < o2.getPinned()) return 1;
            else if (event.getPinned() > o2.getPinned()) return -1;
            return 0;
        }
    };


    public int getEvent_id() {
        return event_id;
    }

    public String getMain_category() {
        return main_category;
    }

    public String getSide_category() {
        return side_category;
    }

    public String getEventname() {
        return eventname;
    }

    public String getOrganiser() {
        return organiser;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getLocation() {
        return location;
    }

    public String getDescription() {
        return description;
    }

    public String getPicture() {return picture;}

    public int getPinned() {
        return pinned;
    }

    public boolean isFavorite() {
        return this.favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public void setLike(int like) {
        this.like = like;
    }

    public int getLike() {
        return like;
    }

    public void setCommentCount(int comment) {
        this.comment = comment;
    }

    public int getCommentCount() {
        return comment;
    }

}