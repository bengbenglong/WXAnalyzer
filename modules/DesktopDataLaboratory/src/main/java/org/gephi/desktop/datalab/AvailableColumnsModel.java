/*
 Copyright 2008-2010 Gephi
 Authors : Eduardo Ramos <eduramiba@gmail.com>
 Website : http://www.gephi.org

 This file is part of Gephi.

 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

 Copyright 2011 Gephi Consortium. All rights reserved.

 The contents of this file are subject to the terms of either the GNU
 General Public License Version 3 only ("GPL") or the Common
 Development and Distribution License("CDDL") (collectively, the
 "License"). You may not use this file except in compliance with the
 License. You can obtain a copy of the License at
 http://gephi.org/about/legal/license-notice/
 or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
 specific language governing permissions and limitations under the
 License.  When distributing the software, include this License Header
 Notice in each file and include the License files at
 /cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
 License Header, with the fields enclosed by brackets [] replaced by
 your own identifying information:
 "Portions Copyrighted [year] [name of copyright owner]"

 If you wish your version of this file to be governed by only the CDDL
 or only the GPL Version 3, indicate your decision by adding
 "[Contributor] elects to include this software in this distribution
 under the [CDDL or GPL Version 3] license." If you do not indicate a
 single choice of license, a recipient has the option to distribute
 your version of this file under either the CDDL, the GPL Version 3 or
 to extend the choice of license to its licensees as provided above.
 However, if you add GPL Version 3 code and therefore, elected the GPL
 Version 3 license, then the option applies only if the new code is
 made subject to such option by the copyright holder.

 Contributor(s):

 Portions Copyrighted 2011 Gephi Consortium.
 */
package org.gephi.desktop.datalab;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Table;

/**
 * Class to keep available state (in data laboratory) of the columns of a table of a workspace. Useful, but also necessary to limit the maximum number of available columns when there are a lot of
 * columns.
 *  这个类用的是Set<column> 集合来保存列内容的
 * @author Eduardo
 */
public class AvailableColumnsModel {

    private static final int MAX_AVAILABLE_COLUMNS = 20;
    private final Set<Column> availableColumns = new HashSet<>();
    private final Set<Column> allKnownColumns = new HashSet<>();
    private final Table table;

    public AvailableColumnsModel(Table table) {
        this.table = table;
    }

    public boolean isColumnAvailable(Column column) {
        return availableColumns.contains(column);
    }

    /**
     * Add a column as available if it can be added.
     *
     * @param column Column to add
     * @return True if the column was successfully added, false otherwise (no more columns can be available)
     */
    public synchronized boolean addAvailableColumn(Column column) {
        if (canAddAvailableColumn()) {
            //这部分更改过，原先的是这样的。更改这部分的目的是为了不添加Lable和Timestamp栏
            //if (!availableColumns.contains(column)) {
            //    availableColumns.add(column);
            //}
            if(!availableColumns.contains(column)){
                if(column.getTitle().equals("Id")
                        ||column.getTitle().equals("度")
                        ||column.getTitle().equals("特征向量")
                        ||column.getTitle().equals("离心率")
                        ||column.getTitle().equals("介数")
                        ||column.getTitle().equals("接近度")
                        ||column.getTitle().equals("消息传播到达时间")
                        ||column.getTitle().equals("节点性质")
                        ||column.getTitle().equals("发送时间")
                        ||column.getTitle().equals("消息内容")
                    ){
                    //添加
                    availableColumns.add(column);
                }
            }
            
            return true;
        } else {
            return false;
        }
    }
    
    //我自己修改过的，根据边表格和节点表格来确定现实不同的列
//    public synchronized boolean addAvailableColumnWithTable(Column column) {
//        if (canAddAvailableColumn()) {
//            //这部分更改过，原先的是这样的。更改这部分的目的是为了不添加Lable和Timestamp栏
//            //if (!availableColumns.contains(column)) {
//            //    availableColumns.add(column);
//            //}
//            if(!availableColumns.contains(column)){
//                if(column.getTitle().equals("Id")
//                        ||column.getTitle().equals("度")
//                        ||column.getTitle().equals("特征向量")
//                        ||column.getTitle().equals("离心率")
//                        ||column.getTitle().equals("介数")
//                        ||column.getTitle().equals("接近度")
//                        ||column.getTitle().equals("消息传播到达时间")
//                        ||column.getTitle().equals("节点性质")
//                        ||column.getTitle().equals("发送时间")
//                        ||column.getTitle().equals("消息内容")
//                    ){
//                    //添加
//                    availableColumns.add(column);
//                }
//            }
//            
//            return true;
//        } else {
//            return false;
//        }
//    }

    /**
     * Remove an available column from the model if possible.
     *
     * @param column Column to make not available
     * @return True if the column could be removed
     */
    public synchronized boolean removeAvailableColumn(Column column) {
        return availableColumns.remove(column);
    }

    /**
     * Clear all available columns
     */
    public synchronized void removeAllColumns() {
        availableColumns.clear();
    }

    /**
     * Indicates if more columns can be made available a the moment
     *
     * @return
     */
    public boolean canAddAvailableColumn() {
        return availableColumns.size() < MAX_AVAILABLE_COLUMNS;
    }

    /**
     * Return available columns, sorted by index
     *
     * @return
     */
    public Column[] getAvailableColumns() {
        List<Column> availableColumnsList = new ArrayList<>();
        for (Column column : table) {
            if(availableColumns.contains(column)){
                availableColumnsList.add(column);
            }
        }
        return availableColumnsList.toArray(new Column[0]);
    }

    public int getAvailableColumnsCount() {
        return availableColumns.size();
    }

    /**
     * Syncronizes this AvailableColumnsModel to contain the table current columns, checking for deleted and new columns.
     */
    public synchronized void syncronizeTableColumns() {
        Set<Column> availableColumnsCopy = new HashSet<>(availableColumns);
        
        removeAllColumns();
        
        //Detect new columns and make them available by default:
        //Also keep existing available columns as available.
        
        //Note: We need to remove all columns and add them all again because there could be a new column with the same title but different index 
        //if the old one with the same title was removed, and we should not keep the old column with same title.
        for (Column column : table) {
            if (availableColumnsCopy.contains(column) || !allKnownColumns.contains(column)) {
                allKnownColumns.add(column);

                addAvailableColumn(column);
            }
        }
    }
}
