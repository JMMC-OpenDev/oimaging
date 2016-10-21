 /*******************************************************************************
 * Copyright 2010-2014 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
 *
 * This file is part of SITools2.
 *
 * SITools2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SITools2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SITools2.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.cnes.sitools.extensions.astro.application.uws.common;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * {Insert class description here}
 *
 * @author Jean-Christophe Malapert
 */
public class Util {

    public static XMLGregorianCalendar computeDestructionTime(Date dateNow, long delayTimeMilliSeconds) throws DatatypeConfigurationException {
        long sum = dateNow.getTime() + delayTimeMilliSeconds;
        Date currentDestructionTime = new Date(sum);
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(currentDestructionTime.getTime());
        XMLGregorianCalendar xmlCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
        return xmlCalendar;
    }

    public static boolean isSet(Object o) {
        return (o == null) ? false : true;
    }

    public static XMLGregorianCalendar convertIntoXMLGregorian(String val) throws DatatypeConfigurationException, ParseException {
        Calendar cal = ISO8601.parse(val);
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(cal.getTimeInMillis());
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
    }

    public static XMLGregorianCalendar convertIntoXMLGregorian(Date date) throws DatatypeConfigurationException {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setGregorianChange(date);
        XMLGregorianCalendar xmlCalendar = null;
        xmlCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
        return xmlCalendar;
    }

    public static void wait(int n) {
        long t0, t1;
        t0 = System.currentTimeMillis();
        do {
            t1 = System.currentTimeMillis();
        } while (t1 - t0 < n);
    }
}    //  end __NAME__

