/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.networkbuild.drivers;

import edu.seu.database.drivers.DatabaseConnection;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DiffusionDataFetcher {
    
    private DatabaseConnection dbConn=null;
    /**
     * 此方法是专门为构建消息传播网络时取数据而准备的
     * @return 
     */
    public ResultSet getFilteredGroupMember(){
//        String sql="select GROUPID, USERID from GROUPMEMBER where USERID not like '0'";
        String sql="select GROUPID, USERID from TB_WX_GROUPMEMBER where USERID not like '0'";
        dbConn=new DatabaseConnection();
        ResultSet rs=dbConn.executeQuery(sql);
        return rs;
    }
    
    
    public ResultSet getFilteredGroupMsg(){
        //获取最近一年的群聊通信记录
        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar now=Calendar.getInstance();
        String endTime=format.format(now.getTime());
        now.add(Calendar.YEAR, -1);
        String startTime=format.format(now.getTime());
        
        
        String sql="select GROUPID, USERID, MSGTIME,CMDID from TB_WX_GROUPMESSAGE order by CMDID asc, MSGTIME asc";
        dbConn=new DatabaseConnection();
        ResultSet rs=dbConn.executeQuery(sql);
        return rs;
    }
    
    public void close(){
        dbConn.close();
    }
}
