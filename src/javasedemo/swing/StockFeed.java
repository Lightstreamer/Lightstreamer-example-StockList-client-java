/*
 * Copyright 2015 Weswit Srl
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

import javax.swing.SwingUtilities;

import com.lightstreamer.client.ClientListener;
import com.lightstreamer.client.LightstreamerClient;
import com.lightstreamer.client.Subscription;
//this class is responsible for the connection to the 
public class StockFeed {
    
    public static final int DISCONNECTED = 1;
    public static final int STREAMING = 2;
    public static final int POLLING = 3;
    public static final int STALLED = 4;
    
    //the list of items that will be subscribed to the lightstreamer server
    private static final String[] group = {"item1", "item2", "item3", "item4", "item5", "item6", "item7", "item8", "item9", "item10",
            "item11", "item12", "item13", "item14", "item15", "item16", "item17", "item18", "item19", "item20",
            "item21", "item22", "item23", "item24", "item25", "item26", "item27", "item28", "item29", "item30"}; 
    
    //the list of fields that will be subscribed to the lightstreamer server
    private static final  String[] schema = {"stock_name", "last_price", "time", "pct_change", "bid_quantity", "bid", "ask", "ask_quantity", 
            "min", "max", "ref_price", "open_price"};
    
    //the names that will be shown on top of each column
    private static final  String[] columnNames = {"Name","Last price", "Time", "Change", "Bid Size", "Bid", "Ask", "Ask Size", 
            "Min", "Max","Ref","Open"};
    
    //all of our fields will be represented by UpdateString instances
    private static final  Class<?>[] classes = new Class<?>[12];
    static {
        for (int i=0; i<=11; i++) {
            classes[i] = UpdateString.class;
        }
    }
    
    
    final private LightstreamerClient client;
    final private Subscription stocks;
    final private StockView view;
  
    final private StockTable table;
    
    
    public StockFeed(String lighstreamerAddress, StockView _view) {
      
        this.view = _view;
          
        //the StockTable instance will receive subscription updates (it will be our SubscriptionListener)
        this.table = new StockTable(group, schema, columnNames, classes);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                view.setModel(table);
            }
        });
          
        //setup the client, will handle our connection and our subscriptions making sure we're always connected 
        //and our items are always subscribed (well, as long as there is a network between us and the server)
        this.client = new LightstreamerClient(lighstreamerAddress, "DEMO");
        client.addListener(new StatusListener());
        
        //setup the subscription, it will receive the data for us
        this.stocks = new Subscription("MERGE",group,schema);
        stocks.setDataAdapter("QUOTE_ADAPTER");
        stocks.setRequestedSnapshot("yes");
        stocks.addListener(this.table.getTableListener());
        
        client.subscribe(stocks);
        client.connect();  
        
    }
    
    
    //notification of a change in the status of the connection
    private void changeStatus(final int status) {
        //we ask the view to change the shown status exploiting the invokeLater method that will
        //execute the call in the GUI's thread
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                view.changeStatus(status);
            }
        });
    }
    
    
    private class StatusListener implements ClientListener {
        
     
     
        @Override
        public void onListenEnd(LightstreamerClient arg0) {
          // we never call removeListener, thus this never happen
        }

        @Override
        public void onListenStart(LightstreamerClient arg0) {
          this.onStatusChange(client.getStatus()); //we actually know that this will be DISCONNECTED
        }

        @Override
        public void onPropertyChange(String arg0) {
          // we have no interest in this event 
        }

        @Override
        public void onServerError(int arg0, String arg1) {
          // TODO ??
        }

        @Override
        public void onStatusChange(String status) {
          switch(status) {
            case "DISCONNECTED":
            case "DISCONNECTED:WILL_RETRY":
            case "CONNECTING": 
            case "CONNECTED:STREAM-SENSE": //we're actually connected here
              changeStatus(DISCONNECTED);
              break;
            case "CONNECTED:WS-STREAMING":
            case "CONNECTED:HTTP-STREAMING":
              changeStatus(STREAMING);
              break;
            case "CONNECTED:WS-POLLING":
            case "CONNECTED:HTTP-POLLING":
              changeStatus(POLLING);
              break;
            case "STALLED":
              changeStatus(STALLED);
              break;
          }
        }

    }
    
}
