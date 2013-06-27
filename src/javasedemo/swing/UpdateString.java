/*
 * Copyright 2013 Weswit Srl
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package javasedemo.swing;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

//this class is used as cell values for the StockTable. 
//This is not a general purpose class as it is strictly bound to the data of the DEMO.QUOTE_ADAPTER data adapter.
//this class implements Comparable so that the view can be easily sorted
public class UpdateString implements Comparable<UpdateString> {
    //will be used to transform Strings into Dates
    private static final DateFormat df = DateFormat.getDateTimeInstance();
    
    //will contain the String value for the field
    private String valueStr = null;
    //for numeric fields, this will hold the value in float form
    private float valueFloat;
    //for the time field, this will hold the value in Date form
    private Date valueDate = null;
    
    //indicates the kind of field this instance is handling
    //0 String 1 float 2 date
    private int kind = 0; 

    //number of updates this instance had since the beginning of the session
    //(when the StockTable is unsubscribed all fields are renewed)
    //this field is used to check whenever a ShutDown task is related to the last update received (and thus should be performed) or not
    private int updateCount = 0;
    
    //whenever the cell showing this field should have a "hot" or "cold" background 
    private boolean isHot;
      
    public UpdateString(String value, boolean isHot) {
        this.setValue(value);
        this.isHot = isHot;
    }
    
    public synchronized void setHot(boolean hot) {
        this.isHot = hot;
    }
    
    public synchronized void setValue(String newValue) {
        //this class is highly bound with the data of the DEMO.QUOTE_ADAPTER data adapter:
        
        if (newValue.indexOf(":") > -1) {
            //the only field that can contain a colon is the time field, so that if a field value
            //contains a  colon we transform it into a date 
            this.kind = 2;
            try {
                this.valueDate = df.parse(newValue);
            } catch (ParseException e) {
                //this should never happen
                this.kind = 0;
            }
        } else if (Character.isDigit(newValue.charAt(0))) {
            //the only string-field is the stock name and there are not names starting with a digit, so
            //if the field value starts with a digit than that's a numeric field and we transform it into 
            //a float
            this.kind = 1;
            this.valueFloat = Float.parseFloat(newValue);
        } else {
            this.kind = 0;
        }
        
        //we save the string version for each kind of field
        this.valueStr = newValue;
        //we increase the number of updates
        this.updateCount++;
    }
    
    
    
    @Override
    public synchronized int compareTo(UpdateString o) {
        //we implement the compareTo method of the Comparable interface based on the kind of update this instance holds.
        //we know that any UpdateString will be compared only with UpdateString of the same kind so that we can exploit
        //the specilized value of this UpdateString. Btw in the case the two are of different kinds, we compare their string values.
        if (o.isOfKind(this.kind)) {
            if (this.kind == 1) {
                return this.valueFloat > o.getFloat() ? 1 : -1;
            } else if (this.kind == 2) {
                return this.valueDate.compareTo(o.getDate());
            }
        }
        return this.valueStr.compareTo(o.toString());
    }

    private synchronized Date getDate() {
        return this.valueDate;
    }

    private synchronized float getFloat() {
        return this.valueFloat;
    }

    public synchronized String toString() {
        return this.valueStr;
    }
    
    public synchronized int getUpdateCount() {
        return updateCount;
    }
    
    public synchronized boolean isOfKind(int kind) {
        return kind == this.kind;
    }

    public synchronized boolean isHot() {
        return this.isHot;
    }
}
