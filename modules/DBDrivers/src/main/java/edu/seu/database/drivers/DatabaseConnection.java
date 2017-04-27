/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.database.drivers;

import edu.seu.database.settings.DBSettings;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import javax.swing.JOptionPane;
import org.openide.util.Exceptions;

/**
 *
 * @author Li Zhenglong
 */
public class DatabaseConnection {
    
       Connection conn=null;
       Statement statement=null;
       ResultSet rs=null;
       
       String userName;
       String password;
       String url;
       
       static{
            try{
               Class.forName("oracle.jdbc.driver.OracleDriver");
            }catch(ClassNotFoundException e){
               e.printStackTrace();
            }
       }
       
       public DatabaseConnection(){
           Properties p=new Properties();
           
           try {
               p.load(new FileInputStream("DBSettings.properties"));
               userName=p.getProperty(DBSettings.USER_NAME);
               password=p.getProperty(DBSettings.PASSWORD);
               url="jdbc:oracle:thin:"+p.getProperty(DBSettings.ADDRESS)+":"+p.getProperty(DBSettings.PORT)+":"+p.getProperty(DBSettings.DB_NAME);
           } catch (IOException ex) {
               JOptionPane.showMessageDialog(null, "加载数据库连接配置信息出错，请检查您的数据库配置文件，或者从新启动您的数据库。");
           }
       }
       
       public boolean testConnection(String url, String userName, String password){
           boolean flag=false;
           try{
                conn=DriverManager.getConnection(url, userName, password);
                flag=true;
            }catch(SQLException e){
                e.printStackTrace();
                flag=false;
            }finally{
               if(conn!=null){
                   try{
                       conn.close();
                   }catch(SQLException ex){
                       ex.printStackTrace();
                   }
               }
           }
           return flag;
       }
       
       //查询
       public ResultSet executeQuery(String mySql){
           
            try{
                conn=DriverManager.getConnection(url, userName, password);
            }catch(SQLException e){
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "数据库连接出错！");
                if(conn!=null){
                    try{
                        conn.close();
                    }catch(SQLException ex){
                        ex.printStackTrace();
                    }
                }
            }
           
           //创建statement对象
           try{
               statement=conn.createStatement();
               statement.setFetchSize(100);
               rs=statement.executeQuery(mySql);
           }catch(SQLException e){
               e.printStackTrace();
               
           }
           return rs;
       }
       
       //关闭数据库连接
       public void close(){
           try{
               if(rs!=null){
                   rs.close();
               }
           }catch(SQLException e){
               e.printStackTrace();
           }finally{
               try{
                   if(statement!=null){
                       statement.close();
                   }
               }catch(SQLException e){
                   e.printStackTrace();
               }finally{
                   try{
                       if(conn!=null){
                           conn.close();
                       }
                   }catch(SQLException e){
                       e.printStackTrace();
                   }
               }
           }
       }
    
}
