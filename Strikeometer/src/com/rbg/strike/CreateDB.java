package com.rbg.strike;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.commons.io.FileUtils;


public class CreateDB {
	private Connection connect = null;
	private Statement statement = null;
	private ResultSet resultSet = null;
	private String sql;
	
	public CreateDB() throws Exception {
	    try {
	    	
	    	//first delete the database if it already exists
	    	FileUtils.deleteDirectory(new File("c:/javamtas/db"));

	      Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
	      connect = DriverManager
	          .getConnection("jdbc:derby:c:\\javamtas\\db;create=true");

	      statement=connect.createStatement();
	      
	      
	      String sql="create table tower (\"Tower ID\" int not null primary key GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"
	    		  +"Tower varchar(100), Comment varchar(200))";
	      statement.execute(sql);
	      
	      sql="create table trainingdata (\"Training ID\" int not null primary key GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"
	    		  +"\"Tower ID\" int, version int, Bell int, Sample int, Value double)";
	      statement.execute(sql);
	      
	      sql="insert into tower (Tower, Comment) values ('Birmingham St Philip','')";
	      statement.execute(sql);
	      
	      sql="insert into tower (Tower, Comment) values ('Birmingham St Paul','')";
	      statement.execute(sql);
	      
	      sql="insert into tower (Tower, Comment) values ('Birmingham St Martin','')";
	      statement.execute(sql);
	      
	      sql="insert into tower (Tower, Comment) values ('Adderbury','')";
	      statement.execute(sql);
	      
	      sql="insert into tower (Tower, Comment) values ('Cambridge GSM','')";
	      statement.execute(sql);
	      
	      sql="insert into trainingdata (\"Tower ID\", version,  Bell, Sample, Value) values(1,1,1,0,.0000000035352)";
	      statement.execute(sql);
	      sql="insert into trainingdata (\"Tower ID\", version,  Bell, Sample, Value) values(1,1,2,0,.002200000035352)";
	      statement.execute(sql);
	      
	      resultSet=statement.executeQuery("select * from trainingdata");
	      while (resultSet.next()) {
	    	System.out.println("ID: " + resultSet.getInt("Training ID"));
	        System.out.println("Tower ID: " + resultSet.getInt("tower id"));
	        System.out.println("Bell: " + resultSet.getInt("Bell"));
	        System.out.println("Sample: " + resultSet.getInt("Sample"));
	        System.out.println("Value: " + resultSet.getDouble("value"));
	      }
	    } catch (Exception e) {
	      throw e;
	    } finally {
	      close();
	    }

	  }

	  private void close() {
	    try {
	      if (resultSet != null) {
	        resultSet.close();
	      }
	      if (statement != null) {
	        statement.close();
	      }
	      if (connect != null) {
	        connect.close();
	      }
	    } catch (Exception e) {

	    }
	  }

	  public static void main(String[] args) throws Exception {
	    CreateDB dao = new CreateDB();
	  }

	} 


