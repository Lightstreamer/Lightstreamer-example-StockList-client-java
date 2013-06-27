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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.SwingUtilities;

import com.lightstreamer.ls_client.ConnectionInfo;
import com.lightstreamer.ls_client.ConnectionListener;
import com.lightstreamer.ls_client.ExtendedTableInfo;
import com.lightstreamer.ls_client.LSClient;
import com.lightstreamer.ls_client.PushConnException;
import com.lightstreamer.ls_client.PushServerException;
import com.lightstreamer.ls_client.PushUserException;
import com.lightstreamer.ls_client.SubscrException;

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
    
    final private ConnectionInfo cInfo = new ConnectionInfo();
    final private LSClient client = new LSClient();
    final private StockView view;
   
    //the phase will change on each connection effort so that calls from older StatusListener will be ignored
    private AtomicInteger phase = new AtomicInteger(0);
    
    final private ExecutorService connectionThread;
    final private StockTable table;
    
    public StockFeed(String pushServerHost, int pushServerPort, StockView _view) {
        this.cInfo.pushServerUrl = "http://" + pushServerHost + ":" + pushServerPort;
        this.cInfo.adapter = "DEMO";
        this.view = _view;
        
      //the StockTable instance will receive table-related updates (it will be our HandyTableListener)
        this.table = new StockTable(group, schema, columnNames, classes);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                view.setModel(table);
            }
        });
        
        //prepare an ExecutorService that will handle our connection efforts
        connectionThread = Executors.newSingleThreadExecutor();
    }
    
    public void start(int ph) {
        if (ph != phase.get()) {
            //we ignore old calls
            return;
        }
        this.start();
    }
    
    public void start() {
        //this method starts a connection effort
        int ph = phase.incrementAndGet();
        connectionThread.execute(new ConnectionThread(ph));
    }
    
    //notification of a change in the status of the connection
    private void changeStatus(int ph, final int status) {
        if (ph != phase.get()) {
            //we ignore old calls
            return;
        }
        
        //we ask the view to change the shown status exploiting the invokeLater method that will
        //execute the call in the GUI's thread
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                view.changeStatus(status);
            }
        });
    }
    
    private void execute(int ph) {
        if (ph != phase.get()) {
             return;
        }
        ph = phase.incrementAndGet();
        this.connect(ph);
        this.subscribe();
    }
    
    private void connect(int ph) {
        boolean connected = false;
        //this method will not exit until the openConnection returns without throwing an exception
        while (!connected) {
            try {
                if (ph != phase.get())
                    return;
                ph = phase.incrementAndGet();
                client.openConnection(this.cInfo, new StatusListener(ph));
                connected = true;
            } catch (PushConnException e) {
            } catch (PushServerException e) {
            } catch (PushUserException e) {
            }
            
            if (!connected) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                }
            }
         }
    }

    private synchronized void subscribe() {
        //this method will try just one subscription.
        //we know that when this method executes we should be already connected
        //If we're not or we disconnect while subscribing we don't have to do anything here as an
        //event will be (or was) sent to the ConnectionListener that will handle the case.
        //If we're connected but the subscription fails we can't do anything as the same subscription 
        //would fail again and again (btw this should never happen)
        ExtendedTableInfo tInfo;
        
        try {
            tInfo = new ExtendedTableInfo(group,"MERGE",schema,true);
            tInfo.setDataAdapter("QUOTE_ADAPTER");
            client.subscribeTable(tInfo, this.table.getTableListener(), false);

        } catch (SubscrException e) {
        } catch (PushServerException e) {
        } catch (PushUserException e) {
        } catch (PushConnException e) {
        }
    }
    
    
    private class ConnectionThread extends Thread {
        private final int ph;

        public ConnectionThread(int ph) {
            this.ph = ph;
        }

        public void run() {
            execute(this.ph);
        }
    }
    
    private class StatusListener implements ConnectionListener {
        
        private int ph;
        private boolean isPolling;
        
        public StatusListener(int ph) {
            this.ph = ph;
        }
        
        //synchronization is useless in this class as there is only one thread calling method on this listener
        
        private void onDisconnection() {
            changeStatus(this.ph,DISCONNECTED);
            start(this.ph);
        }
        
        private void onConnection() {
            if (this.isPolling) {
                changeStatus(this.ph,POLLING);
            } else {
                changeStatus(this.ph,STREAMING);
            }
        }
        
        @Override
        public void onActivityWarning(boolean warningOn) {
            if (warningOn) {
                changeStatus(this.ph, STALLED);
            } else {
                this.onConnection();
            }
        }
    
        @Override
        public void onClose() {
            this.onDisconnection();
        }
    
        @Override
        public void onConnectionEstablished() {
        }
    
        @Override
        public void onDataError(PushServerException arg0) {
        }
    
        @Override
        public void onEnd(int arg0) {
            this.onDisconnection();
        }
    
        @Override
        public void onFailure(PushServerException arg0) {
            this.onDisconnection();
        }
    
        @Override
        public void onFailure(PushConnException arg0) {
            this.onDisconnection();
        }
    
        @Override
        public void onNewBytes(long bytes) {
        }
    
        @Override
        public void onSessionStarted(boolean isPolling) {
            this.isPolling = isPolling;
            
            this.onConnection();
            
        }

    }
    
}
