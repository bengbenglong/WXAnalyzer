/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.database.drivers;

import java.sql.ResultSet;

/**
 *
 * @author hp-6380
 */
public class AuditPerspectiveFetcher {
    
    private DatabaseConnection dbConn;
    
    public ResultSet getAllResult(){
        String sql="select * from TB_AUDIT where (AUDITRESULT =1 or AUDITRESULT =-2)";
        return getResultSet(sql);
    }
    
    public ResultSet getSpecificNumTypeResult(int numType){
        String sql="select * from TB_AUDIT where NUMBERTYPE = "+numType+" and (AUDITRESULT =1 or AUDITRESULT =-2)";
        return getResultSet(sql);
    }
    
    public ResultSet getSepeficAuditTypeResult(int auditType){
        String sql="select * from TB_AUDIT where AUDITTYPE = "+auditType+" and (AUDITRESULT =1 or AUDITRESULT =-2)";
        return getResultSet(sql);
    }
    
    public ResultSet getAllMonitorResult(){
        String sql="select * from TB_AUDIT where (AUDITTYPE = 4 or AUDITTYPE = 5) and (AUDITRESULT = 1 or AUDITRESULT = -2)";
        return getResultSet(sql);
    }
    
    public ResultSet getSpecificNumtypeMointor(int numType){
        String sql="select * from TB_AUDIT where (AUDITTYPE =4 or AUDITTYPE =5) and (AUDITRESULT = 1 or AUDITRESULT = -2) and NUMBERTYPE = "+numType;
        return getResultSet(sql);
    }
    
    public ResultSet getResult(int auditType, int numType){
        String sql="select * from TB_AUDIT where AUDITTYPE = "+auditType+" and NUMBERTYPE = "+numType+" and (AUDITRESULT =1 or AUDITRESULT= -2)";
        return getResultSet(sql);
    }
    
    public void closeConnection(){
        dbConn.close();
    }
    
    private ResultSet getResultSet(String sql){
        dbConn=new DatabaseConnection();
        ResultSet rs=dbConn.executeQuery(sql);
        return rs;
    }
    
}
