/*
 * @(#)DateParser.java  1.00
 *
 * Copyright WEBsina (c)2001  All Rights Reserved.
 *
 */
package com.ceteva.mosaic.util;

import java.util.Date;
import java.util.Calendar;
import java.text.DateFormat;

/**
 * This class deals with Date related issues.
 *
 * @author Samuel Chen
 * @version $Revision: 1.1 $
 * created:  2-10-2001
 * @since 1.0 
 */
public class DateParser {

  private Calendar calendar;

  public DateParser(Date date) {
    calendar = Calendar.getInstance();
    calendar.setTime(date);
  }
  
  public DateParser(String dateStr) {
    this(DateParser.toUtilDate(dateStr));
  }

  public static java.util.Date toUtilDate(String dateStr) {
    Date date = null;
    DateFormat formater;
    try {
      formater = new java.text.SimpleDateFormat("yyyy-MM-dd"); 
      date = formater.parse(dateStr);
    } catch (Exception e) {
      e.printStackTrace();
      try {
	formater = new java.text.SimpleDateFormat("MM/dd/yyyy"); 
	date = formater.parse(dateStr);
      } catch (Exception e2) {
	e2.printStackTrace();
	//give up!
      }
    }
    return date;
  }

  /**
   * @param java.util.Date
   * @return java.sql.Date
   * Convert a java.util.Date to java.sql.Date
   */
  public static java.sql.Date toSqlDate(java.util.Date date) {
    if (date==null) {
      return null;
    } else {
      return new java.sql.Date(date.getTime());
    }
  }
  
  public static java.sql.Date toSqlDate(String dateStr) {
    return DateParser.toSqlDate(DateParser.toUtilDate(dateStr));
  }
  
  /**
   * @return string year
   */
  public String getYear() {
    int year = calendar.get(Calendar.YEAR);
    return Integer.toString(year);
  }
  
  /**
   * @return last two digital part of the year
   */
  public String getXXYear() {
    return getYear().substring(2,4);
  }

  /**
   * @return the month of the year
   */
  public int getMonth(){
    return calendar.get(Calendar.MONTH)+1;
  }

  /**
   * @return the day of the month
   */
  public int getDayOfMonth() {
    return calendar.get(Calendar.DAY_OF_MONTH);
  }

  /**
   * @return the day of the month
   */
  public int getDayOfWeek() {
    return calendar.get(Calendar.DAY_OF_WEEK);
  }
  
  public static void main (String[] args) {
    DateParser parser = new DateParser(args[0]);
    System.out.println("year="+parser.getYear());
    System.out.println("year2="+parser.getXXYear());
    System.out.println("month="+parser.getMonth());
    System.out.println("day="+parser.getDayOfMonth());
    System.out.println("week="+parser.getDayOfWeek());
    //System.out.println("SUNDAY="+Calendar.SUNDAY);
    //System.out.println("JANUARY="+Calendar.JANUARY);
  }
}


