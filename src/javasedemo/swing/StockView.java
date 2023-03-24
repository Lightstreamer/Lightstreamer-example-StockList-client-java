/*
 * Copyright (c) Lightstreamer Srl
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

import java.awt.Color;
import java.awt.Component;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

public class StockView extends JFrame {

    private static final long serialVersionUID = 1L;
    
    private static final String TITLE = "Lightstreamer :: Java Swing :: Stock-List Demo";
    private static final ImageIcon LOGO = new ImageIcon(StockView.class.getResource("/images/logo.png"));
    
    private static final ImageIcon disconnectedIcon = new ImageIcon( StockView.class.getResource("/images/status_disconnected.png") );
    private static final ImageIcon pollingIcon = new ImageIcon( StockView.class.getResource("/images/status_connected_polling.png") );
    private static final ImageIcon stalledIcon = new ImageIcon( StockView.class.getResource("/images/status_stalled.png") );  
    private static final ImageIcon streamingIcon = new ImageIcon( StockView.class.getResource("/images/status_connected_streaming.png") ); 
    
    private JTable table;
    private JLabel statusLabel;

    private boolean initialized = false;
    
    private static ConcurrentLinkedQueue<ShutDownTask> shutDown = new ConcurrentLinkedQueue<ShutDownTask>();
    private static ShutDownThread shutDownThread;

    public StockView() {
        super();
    }

    public synchronized void initialize() {
        if (this.initialized) {
            return;
        }
        this.initialized = true;
        
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        //prepares the data-JTable and the status-JLabel then initiate the JContentPane
        
        this.table = new JTable();
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        //we use our custom renderer to paint the cells
        table.setDefaultRenderer(UpdateString.class, new HighlightTableCellRenderer());
        
        this.statusLabel = new JLabel("",disconnectedIcon,JLabel.LEFT);
        this.statusLabel.setToolTipText("DISCONNECTED");
        
        this.setSize(850, 625);
        this.setContentPane(createJContentPane(this.statusLabel,this.table));
        this.setTitle(TITLE);
    
        this.setVisible(true);
        
        synchronized(shutDown) {
            if (shutDownThread == null) {
                //this thread will be responsible of the change of the background of the cells
                //updates cause cells to have a "hot" background, this thread change the background to "cold"
                //this Thread instance is static so that only the first initialized StockView needs to 
                //create and start it
                shutDownThread = new ShutDownThread();
                shutDownThread.start();
            }
        }
    }
    
    private static JPanel createJContentPane(JLabel statusLabel, JTable table) {
        //this is the main JPanel
        JPanel jContentPane = new JPanel();
        jContentPane.setBackground(Color.white);
        jContentPane.setLayout(new BoxLayout(jContentPane,BoxLayout.Y_AXIS));
        
        //we create a JPanel containing the status indicator, our logo and the demo name
        //we'll use this JPanel as first element on the main JPanel
        JPanel firstLine = new JPanel();
        firstLine.setBackground(Color.white);
        firstLine.setLayout(new BoxLayout(firstLine,BoxLayout.X_AXIS));
        
        firstLine.add(statusLabel);
        
        JLabel label = new JLabel(TITLE,LOGO,JLabel.LEFT);
        firstLine.add(label);
        
        firstLine.add(Box.createHorizontalGlue());
   
        //then we add the previously created first-line-JPanel and the JTable to
        //the main JPanel
        jContentPane.add(firstLine);
        //(we wrap the JTable in a JSCrollPane so that we have scrollbars)
        JScrollPane scrollPane = new JScrollPane(table);
        jContentPane.add(scrollPane);
                
        return jContentPane;
    }
    
    public synchronized void setModel(StockTable model) {
        //we pass the model to the JTable
        this.table.setModel(model);
        //we set the first column to be larger (it contains the stock_name)
        TableColumn col = table.getColumnModel().getColumn(0); 
        col.setPreferredWidth(250); 
        
        //we add a sorter to the JTable (and we listen for sort changes, see below)
        TableRowSorter<StockTable> trs = new TableRowSorter<StockTable>(model);
        trs.addRowSorterListener(new SortListener());
        
        table.setRowSorter(trs);
    }
    
    public synchronized void enableDynamicSort(boolean enabled) {
        //enable or disable the dynamic sort. When dynamic sort is ON we always need that the model, each times it
        //receives an update, asks the view for a complete refresh, so to enable the SortsOnUpdates we have also to 
        //enable the complete refresh on each update
        ((StockTable) table.getModel()).enableCompleteRefresh(enabled);
        ((TableRowSorter<?>) table.getRowSorter()).setSortsOnUpdates(enabled);
    }
    
    public synchronized void changeStatus(int status) {
        //changes connection status icon and tooltip text
        String statusTxt = null;
        ImageIcon icon = null; 
        switch(status) {
            case StockFeed.DISCONNECTED:
                statusTxt = "DISCONNECTED";
                icon = disconnectedIcon;
                break;
            case StockFeed.STREAMING:
                statusTxt = "STREAMING";
                icon = streamingIcon;
                break;
            case StockFeed.POLLING:
                statusTxt = "POLLING";
                icon = pollingIcon;
                break;
            case StockFeed.STALLED:
                statusTxt = "STALLED";
                icon = stalledIcon;
                break;
        }
        
        
        statusLabel.setToolTipText(statusTxt);
        statusLabel.setIcon(icon);
    }
    
    public class SortListener implements RowSorterListener {
        @Override
        public synchronized void sorterChanged(RowSorterEvent e) {
            //the sort listener waits for changes on the sorted columns
            //if the first sort key is the stock_name or the open_price column
            //(that are fields that never change) it disable the dynamic sort
            if (e.getType().equals(RowSorterEvent.Type.SORT_ORDER_CHANGED)) {
                @SuppressWarnings("unchecked")
                List<? extends RowSorter.SortKey> keys = e.getSource().getSortKeys();
                if(!keys.isEmpty()) {
                    int y = keys.get(0).getColumn();
                    if (y != 0 && y != 11) {
                        enableDynamicSort(true);
                    } else {
                        enableDynamicSort(false);
                    }
                }
            } 
        }
    }
        
    public class HighlightTableCellRenderer extends DefaultTableCellRenderer {

        private static final long serialVersionUID = 7837295534229006872L;
        
        private final Color oddRow = new Color(238,238,238);
        private final Color evenRow = Color.white;
        
        private final Color oddHot = Color.yellow;
        private final Color evenHot = new Color(255,255,100);
        
        //note that column and rows received by this method are view-related, so we'll use the convertColumnIndexToModel
        //to convert them when wee nedd'em to be model-related
        @Override
        public synchronized Component getTableCellRendererComponent (JTable table, Object value, boolean selected, boolean focused, int row, int column) {
            //all the columns but the stock_name one have to be aligned on the right
            if(table.convertColumnIndexToModel(column) != 0) {
                this.setHorizontalAlignment(RIGHT);
            } else {
                this.setHorizontalAlignment(LEFT);
            }
            
            UpdateString valueUS;
            //we check on the UpdateString if the value is hot or cold and we set the background color
            //accordingly; in case the field is null or is not an UpdatString we use the cold colors
            if (value != null && value instanceof UpdateString && (valueUS = (UpdateString) value).isHot()) {
                if ((row % 2) == 0) {
                    this.setBackground(evenHot);
                } else {
                    this.setBackground(oddHot);
                } 
                //in case the update is hot we schedule a task to make it cold after 300ms
                shutDown.add(new ShutDownTask(new Date().getTime()+300,valueUS.getUpdateCount(),table.convertRowIndexToModel(row),table.convertColumnIndexToModel(column)));
                
            } else {
                if ((row % 2) == 0) {
                    this.setBackground(evenRow);
                } else {
                    this.setBackground(oddRow);
                } 
            }
            
            
            return super.getTableCellRendererComponent(table, value, selected, focused, row, column);
        }
        
    }
    
    //this is just a simple bean
    public class ShutDownTask {
        
        private final long time;
        private final int update;
        private final int row;
        private final int col;

        ShutDownTask(long time, int update, int row, int col) {
            this.time = time;
            this.update = update;
            this.row = row;
            this.col = col;
        }
        
    }
    
    public class ShutDownThread extends Thread {
        
        @Override
        public void run() {
            //this thread will run forever even while the client is not connected or while there are no active StockView instances.
            //(in our demo there is always one StockView instance)
            //you may want to pause this thread for when the client is not connected to the server
            //and/or you may want to stop this thread if you implement a more complex application and 
            //need to dismiss all the StockView instances 
            while(true) { 
                long now = new Date().getTime();
                ShutDownTask sdt;
               
                //shutDown is a queue so that the peek element is the oldest
                //while ShutDownTasks are old enough to be executed we go on, then we sleep
                //Only this thread is a consumer for the static shutDown queue, so we can peek() and then poll() being
                //sure that no other thread changed the peek element in the meanwhile
                while ((sdt = shutDown.peek()) != null && now >= sdt.time) {
                    //the setCold method on the model changes the status hot/cold of the update and
                    //generates the updated-event
                    ((StockTable)table.getModel()).setCold(sdt.row,sdt.col,sdt.update);
                    shutDown.poll();
                }
                
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
            }
        }
        
    }


}  
