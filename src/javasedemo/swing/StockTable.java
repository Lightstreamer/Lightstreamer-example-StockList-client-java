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

import java.security.InvalidParameterException;

import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import com.lightstreamer.ls_client.HandyTableListener;
import com.lightstreamer.ls_client.UpdateInfo;

public class StockTable extends AbstractTableModel {

    private static final long serialVersionUID = -7846472930012885468L;
    
    private String[] group;
    private String[] schema;
    private String[] columnNames;
    private Class<?>[] classes;
    
    //the actual model
    private UpdateString[][] data;
    
    private int phase = 0;
    
    //whenever on an update fire a focused refresh request or a complete refresh request
    private boolean fullRefreshEnabled = false;
    
    //the max row index
    private final int maxRowIndex;

    private UpdateListener listener;
    private boolean firstUpdate = true;

    public StockTable(String[] group, String[] schema, String columnNames[], Class<?>[] classes) {
        this.group = group;
        this.schema = schema;
        this.columnNames = columnNames;
        this.classes = classes;
        
        this.maxRowIndex = this.group.length-1;
        
        if (columnNames.length != schema.length || classes.length != columnNames.length) {
            throw new InvalidParameterException("schema columNames and classes must be of the same length");
        }
        this.data = new UpdateString[this.group.length][this.schema.length];
        this.init();
    }
    
    private void init() {
        this.phase++;
        this.firstUpdate = true;
        this.listener = new UpdateListener(this.phase);
    }
    
    private void onFirstUpdate() {
        this.data = new UpdateString[this.group.length][this.schema.length];
        this.fireAsyncTableDataChanged();
        this.firstUpdate = false;
    }
    
    public HandyTableListener getTableListener() {
        return this.listener;
    }

    public void enableCompleteRefresh(boolean enabled) {
        this.fullRefreshEnabled  = enabled;
    }
    
    public void fireAsyncTableDataChanged() {
        //sends the full refresh notification on the GUI's thread because the fireTableDataChanged call directly
        //calls for a refresh on the view class (and changes on the view must be performed on the
        //GUI's thread)
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                fireTableDataChanged();
            }
        });
    }
    
    public void fireAsyncAllRowsChanged() {
        //see fireAsyncTableDataChanged comment
        //to call fireTableRowsUpdated(0,maxRowIndex) is quite the same as calling
        //fireTableDataChanged, btw calling fireTableDataChanged also means that the
        //number of rows may be changed
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                fireTableRowsUpdated(0, maxRowIndex);
            }
        });
    }
    
    public void fireAsyncTableRowsUpdated(final int start, final int end) {
        if (this.fullRefreshEnabled) {
            //if full refresh is enabled we don't call for a row(s) refresh but for a complete one
            this.fireAsyncAllRowsChanged();
        } else {
            //see fireAsyncTableDataChanged comment
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    fireTableRowsUpdated(start, end);
                }
            });
        }
    }
    
    public void fireAsyncTableCellUpdated(final int row, final int col, boolean forceNotFull) {
        if (this.fullRefreshEnabled && !forceNotFull) {
            //if full refresh is enabled we don't call for a cell refresh but for a complete one
            this.fireAsyncAllRowsChanged();
        } else {
            //see fireAsyncTableDataChanged comment
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    fireTableCellUpdated(row,col);
                }
            });
        }
    }
    
    public synchronized void setCold(int row, int col, int update) {
        if (this.data[row][col].getUpdateCount() == update) {
            this.data[row][col].setHot(false);
            
            //the value doesn't change so the sort doesn't change so in cases where full refresh
            //is enabled we can force the refresh on the single field instead of fire a full refresh
            this.fireAsyncTableCellUpdated(row, col, true);
        }
        
    }

    @Override
    public synchronized void setValueAt(Object value, int rowIndex, int columIndex) {
        /*if (!(value instanceof UpdateString)) {
            throw ...
        }*/
        //change the value on the model and fires the update notification
        this.data[rowIndex][columIndex] = (UpdateString) value;
        this.fireAsyncTableCellUpdated(rowIndex, columIndex, false); 
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columIndex) {
        return this.data[rowIndex][columIndex];
    }
    
    @Override
    public int getColumnCount() {
        return this.schema.length;
    }

    @Override
    public String getColumnName(int columIndex) {
        return this.columnNames[columIndex];
    }

    @Override
    public int getRowCount() {
        return this.group.length;
    }

    @Override
    public Class<?> getColumnClass(int columIndex) {
        return this.classes[columIndex];
    }

    
    class UpdateListener implements HandyTableListener {
        
        private int ph;

        UpdateListener(int ph) {
            this.ph = ph;
        }
        
        @Override
        public void onRawUpdatesLost(int itemIndex, String itemName, int lostUpdates) {
            return;
        }
    
        @Override
        public void onSnapshotEnd(int itemIndex, String itemName) {
            return;
        }
    
        @Override
        public void onUnsubscr(int itemIndex, String itemName) {
            return;
        }
    
        @Override
        public void onUnsubscrAll() {
            init();
            return;
        }
    
        @Override
        public synchronized void onUpdate(int itemIndex, String itemName, UpdateInfo update) {
            if (phase != this.ph){
                return;
            } else if (firstUpdate) {
                onFirstUpdate();
            }
            
            boolean isSnap = update.isSnapshot();
            
            for (int i=1; schema.length>=i; i++) {
                if (update.isValueChanged(i)) {
                    if (data[itemIndex-1][i-1] != null) {
                        //we already had an update for this cell so we just change the values and the hot state
                        data[itemIndex-1][i-1].setValue(update.getNewValue(i));
                        //we avoid the hot state on snapshot events
                        data[itemIndex-1][i-1].setHot(!isSnap);
                    } else {
                        //first update for this cell, we need a new UpdateString instance
                        data[itemIndex-1][i-1] = new UpdateString(update.getNewValue(i),!isSnap);
                    }
                    if (!isSnap || !fullRefreshEnabled) {
                        //if this is not the snapshot and the full refresh is not enabled, we fire a focused notification
                        fireAsyncTableCellUpdated(itemIndex-1, i-1, false); 
                    }
                }
            }
            
            if (isSnap || fullRefreshEnabled) {
                //if this is the snapshot or the full refresh is enabled, we fire a row-related refresh 
                //(note that if full refresh is enabled this will be made as a full refresh)
                fireAsyncTableRowsUpdated(itemIndex-1, itemIndex-1);
            }
        }
    }
    


}
