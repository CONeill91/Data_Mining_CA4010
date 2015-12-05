/**
 * Created by conor on 27/11/15.
 * Predication main running class.
 */

import model.*;
import util.CsvImporter;

import javax.swing.*;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

public class Prediction {

    private static final Logger LOGGER = Logger.getLogger( Prediction.class.getName() );
    // TODO promotor / demographic
    // TODO Test algorithm
    // TODO Tweak Weights.
    //
    public static final double DATE_WEIGHT = 5;
    public static final double DAY_WEIGHT = 20;
    public static final double PROMOTOR_WEIGHT = 30;
    public static final double PRICE_WEIGHT = 20;
    public static final double VENUE_WEIGHT = 15;
    public static final double GENDER_WEIGHT = 10;



    public static void main(String[] args)  {




        CsvImporter importer = new CsvImporter();
        try {
            ArrayList<Event> listOfEvents = importer.importEvents();
            ArrayList<Person> listOfPeople = importer.importPeople();
            ArrayList<Guest> guestList = importer.importGuestList();
            HashMap<String, Person> peopleMap = new HashMap<String, Person>();
            HashMap<String, Event> eventMap = new HashMap<String, Event>();

            for (Person person : listOfPeople) {
                peopleMap.put(person.getUser_id(), person);
            }

            for (Event event : listOfEvents) {
                eventMap.put(event.getId(), event);
                event.getVenue().setAverageCapacity();
                event.setAveragePrice();
            }

            for (Guest guest : guestList) {
                if (peopleMap.containsKey(guest.getUserId())) {

                    peopleMap.get(guest.getUserId()).addEventId(guest.getEventId());
                }
            }

            //for each person in map, adds corresponding events to their arraylist of pastEventAttendance
            for (Map.Entry<String, Person> person : peopleMap.entrySet()) {
                for (String eventID : peopleMap.get(person.getKey()).getIdOfEventsAttended()) {
                    if (eventMap.containsKey(eventID)) {
                        peopleMap.get(person.getKey()).attendedEvent(eventMap.get(eventID));
                    }
                }

            }



            // They don't contribute anything but zeroes
            removePeopleWithNoAttendance(listOfPeople);
            removeEventsWithNoAttendance(listOfEvents);
            calculateDemographic(listOfPeople,listOfEvents);
            calculateGenderSplit(listOfEvents);




            for (Event event : listOfEvents) {

                event.setAttending(event.getMalesAttending() + event.getFemalesAttending());
             //  LOGGER.info("Total : " + event.getAttending() + "  Males: " + event.getMalesAttending() + "  Females: " + event.getFemalesAttending() + "   Nones: " +event.getNonesAttending());
                //LOGGER.info("AverageAttendance :" + event.getVenue().getAverageCapacity() + " AveragePrice: " + event.getAveragePrice());

            }
            for(Person person : listOfPeople){
                person.setAverageSpend();
               // LOGGER.info(person.getAverageSpend() + "");
                person.setEventDays(person.calculateEventDays());
                person.setVenueMap(person.calculateVenueMaps());


            }

        }
        catch(FileNotFoundException e){
            LOGGER.info("Error importing data from CSVs");
            e.printStackTrace();
        }
    }




    public static void calculateDemographic(ArrayList<Person> persons, ArrayList<Event> events){
        for(Event event: events){
            for(Person person: persons){
                ArrayList<Event> personEvents = person.getPastEventAttendance();
                if(personEvents.contains(event)){
                    if(person.getGender().equals("male")){
                        event.incMalesAttending();
                    }if(person.getGender().equals("female")){
                        event.incFemalesAttending();
                    }if(person.getGender().equals("None")){
                        event.incNonesAttending();
                    }
                }
            }
        }

    }
    // Calculate the gender split for each event
    public static void calculateGenderSplit(ArrayList<Event> events){
        for(Event event : events){
            int total = event.getFemalesAttending() + event.getMalesAttending();
            if(total != 0){
                event.setDemograpicMale(((double)event.getMalesAttending() / (double)total) * 100.00);
            }
            if(event.getDemograpicMale() != 0){
                event.setDemograpicFemale(100.00 - event.getDemograpicMale());
            }
        }
    }

    // Remove user's which have 0 events attended.
    public static void removePeopleWithNoAttendance(ArrayList<Person> persons){
        Iterator<Person> iter = persons.iterator();

        while (iter.hasNext()) {
            Person str = iter.next();

            if (str.getPastEventAttendance().size() == 0)
                iter.remove();
        }
    }

    public static void removeEventsWithNoAttendance(ArrayList<Event> events){
        Iterator<Event> iter = events.iterator();

        while (iter.hasNext()) {
            Event str = iter.next();

            if (str.getAttending() == 0)
                iter.remove();
        }
    }






    // ** Prediction Methods **

    public static int calculatepromoterCorrelation(ArrayList<Person> persons, ArrayList<Event> events ){
        HashMap<String[],Integer> correlationPairs = new HashMap<String[], Integer>();
        return 0;
        }

    public static int promotorPrediction(Person person, Event futureEvent){
        return 0;
    }



    public static double genderPrediction(Person person, Event futureEvent) {
        ArrayList<Event> events = person.getPastEventAttendance();
        double count = 0;
        double totalMaleDemo = 0;
        double totalFemaleDemo = 0;
        for (Event event : events) {
            totalMaleDemo += event.getDemograpicMale();
            totalFemaleDemo += event.getDemograpicFemale();
            count++;
        }
        if (count != 0) {
            double averageFemaleDemo = totalFemaleDemo / count;
            double averageMaleDemo = totalMaleDemo / count;

            double eventMaleDemo = futureEvent.getDemograpicMale();
            double eventFemaleDemo = futureEvent.getDemograpicFemale();

            if ((averageFemaleDemo >= eventFemaleDemo - 5 && averageFemaleDemo <= eventFemaleDemo + 5) &&
                    (averageMaleDemo >= eventMaleDemo - 5 && averageMaleDemo <= eventMaleDemo + 5)) {
                return GENDER_WEIGHT;
            } else if ((averageFemaleDemo >= eventFemaleDemo - 10 && averageFemaleDemo <= eventFemaleDemo + 10) &&
                    (averageMaleDemo >= eventMaleDemo - 10 && averageMaleDemo <= eventMaleDemo + 10)) {
                return GENDER_WEIGHT * 0.80;
            } else if ((averageFemaleDemo >= eventFemaleDemo - 15 && averageFemaleDemo <= eventFemaleDemo + 15) &&
                    (averageMaleDemo >= eventMaleDemo - 15 && averageMaleDemo <= eventMaleDemo + 15)) {
                return GENDER_WEIGHT * 0.50;
            } else if ((averageFemaleDemo >= eventFemaleDemo - 20 && averageFemaleDemo <= eventFemaleDemo + 20) &&
                    (averageMaleDemo >= eventMaleDemo - 20 && averageMaleDemo <= eventMaleDemo + 20)) {
                return GENDER_WEIGHT * 0.30;


            }

        }
        return 0;
    }



    // Done
    public static double datePrediction(Event event){
        ArrayList<Date> bankHolidays = initBankHolidayList();
        for(Date date : bankHolidays){
            if(date.equals(event.getDate())){
                return DATE_WEIGHT;
            }
        }
        return 0;
    }

    public static double venuePrediction(Person person, Event futureEvent){
        String futureEventVenue = futureEvent.getVenue().getName();
        HashMap<String,Integer> personVenueMap = person.getVenueMap();
        int numTimesAtVenue = 0;
        if (personVenueMap.containsKey(futureEventVenue)){
             numTimesAtVenue = personVenueMap.get(futureEventVenue);
        }

        if(numTimesAtVenue >= 15){
            return VENUE_WEIGHT;
        }
        else if (numTimesAtVenue < 15 && numTimesAtVenue >= 10){
            return VENUE_WEIGHT * 0.80;
        }
        else if(numTimesAtVenue < 10 && numTimesAtVenue >= 7){
            return VENUE_WEIGHT * 0.50;
        }
        else if(numTimesAtVenue < 7 && numTimesAtVenue >= 3){
            return VENUE_WEIGHT * 0.30;
        }

        return 0;
    }


    public static double pricePrediction(Person person,Event event){
        if(event.getAveragePrice() <= person.getAverageSpend()){
            return PRICE_WEIGHT;
        }
        else if(event.getAveragePrice() - person.getAverageSpend() <= 2.50){
            return PRICE_WEIGHT * 0.80;
        }
        else if(event.getAveragePrice() - person.getAverageSpend() <= 5 && event.getAveragePrice() - person.getAverageSpend() >= 2.50){
                return PRICE_WEIGHT * 0.50;
        }
        return 0;

    }


    // Day Weights
    // Out on this day :
    // >= 15 100
    // >= 10 80%
    // >= 7 50%
    // >= 3 30*
    
    
    public static double dayPrediction(Person person,Event futureEvent){
        String dayofFutureEvent = futureEvent.getDay();
        HashMap<String,Integer> dayMap = person.getEventDays();
        int numTimesOutThatDay = 0;
        if(dayMap.containsKey(dayofFutureEvent)){
             numTimesOutThatDay = dayMap.get(dayofFutureEvent);
        }
        if(numTimesOutThatDay >= 15){
           return (double)DAY_WEIGHT;
        }
        else if (numTimesOutThatDay < 15 && numTimesOutThatDay >= 10){
            return (double)DAY_WEIGHT * 0.80;
        }
        else if(numTimesOutThatDay < 10 && numTimesOutThatDay >= 7){
            return (double)DAY_WEIGHT * 0.50;
        }
        else if(numTimesOutThatDay < 7 && numTimesOutThatDay >= 3){
            return (double) DAY_WEIGHT * 0.30;
        }
        return 0;
    }


    /*public static double calculatePredictionScore(Person person, Event futureEvent){
        int totalScore = dayPrediction(person,futureEvent) + datePrediction(futureEvent) + pricePrediction(person,futureEvent)
                + venuePrediction(person,futureEvent) + venuePrediction(person,futureEvent) + promotorPrediction(person);

         return (double) totalScore / 100.00;



    }*/

    // Method which initializes list with all date values of BH's in 2016,2015,2014,2013;
    private static ArrayList<Date> initBankHolidayList() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        ArrayList<Date> bankholidays = new ArrayList<Date>();
        String [] bhDates13 =  {"2012/12/31","2013/3/16","2013/3/28","2013/3/31","2013/5/5","2013/6/2","2013/9/4","2013/10/27","2013/12/23"};
        String [] bhDates14 =  {"2013/12/31","2014/3/16","2014/4/17","2014/4/20","2014/5/4","2014/6/1","2014/9/3","2014/10/26","2014/12/23"};
        String [] bhDates15 = {"2014/12/31","2015/3/16","2015/4/2","2015/4/5","2015/5/3","2015/5/31","2015/9/2","2015/10/25","2015/12/23"};
        String [] bhDates16 = {"2015/12/31","2016/3/16","2016/3/24","2016/3/27","2016/4/30","2016/6/5","2016/7/31","2016/10/30","2016/12/23"};
        try {
            for(String date : bhDates13){
                bankholidays.add(format.parse(date));
            }
            for(String date : bhDates14){
                bankholidays.add(format.parse(date));
            }
            for(String date : bhDates15){
                bankholidays.add(format.parse(date));
            }
            for(String date : bhDates16){
                bankholidays.add(format.parse(date));
            }
        }
        catch (ParseException p){
            LOGGER.info("Error parsing BH dates");
        }
        return bankholidays;
    }




}


