/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.networkbuild.drivers;

import edu.seu.database.drivers.DatabaseConnection;
import java.sql.ResultSet;
import javax.swing.JOptionPane;

/**
 *
 * @author hp-6380
 */
public class GroupInfoFetcher {
    private DatabaseConnection dbConn=null;
    
    public ResultSet getGroupInfo(){
        String sql="select * from TB_WX_GROUP";
        dbConn=new DatabaseConnection();
        ResultSet rs=dbConn.executeQuery(sql);
        return rs;
    }
    
    public void close(){
        try{
            if(dbConn!=null){
                dbConn.close();
            }
        }catch(Exception e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "数据库连接关闭失败");
        }
    }
}
