/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.display;

import java.util.Arrays;
import java.util.List;

import edu.cornell.mannlib.vitro.webapp.utils.StringUtils;

public class DateDisplayUtils {

    public static String getDisplayDate(String date) {
        String displayDate = null;
        if (date == null) {
            return displayDate;
        }
        List<String> dateParts = Arrays.asList(date.split("-"));
        int datePartCount = dateParts.size();
        switch (datePartCount) {
        case 2: 
            displayDate = StringUtils.join("/", dateParts.get(1), dateParts.get(0));
            break;
        case 3:
            displayDate = StringUtils.join("/", dateParts.get(1), dateParts.get(2), dateParts.get(0));
            break;
        default: 
            displayDate = date;
        }
        
        return displayDate;     
    }
    
    public static String getDisplayDateRange(String startDate, String endDate) {
        startDate = StringUtils.setNullToEmptyString(startDate);
        endDate = StringUtils.setNullToEmptyString(endDate);
        List<String> dates = Arrays.asList(startDate, endDate);
        return StringUtils.join(dates, " - ");
    }
    
    public static String getDisplayDateRangeFromRawDates(String startDate, String endDate) {
        return getDisplayDateRange(getDisplayDate(startDate), getDisplayDate(endDate));
    }
    

}
