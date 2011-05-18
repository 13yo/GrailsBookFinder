package de.mpg.mis.neuesbibliothekssystem.stacks.factories.dbdriver;

import com.sybase.jdbc3.jdbc.*; // Version 6 
// Version 4.1
//import com.sybase.jdbcx.*;
import java.sql.*;
import java.util.*;

public class DBConnector {
    private Connection con;
    private String produkt;
    private String db;
    private String user;
    private String password;
    private String server;
    private int port;

    private Connection getSybaseConnection() throws Exception {
	// SybDriver
	// sybDriver=(SybDriver)Class.forName("com.sybase.jdbc.SybDriver").newInstance();
	SybDriver sybDriver = (SybDriver) Class.forName(
		"com.sybase.jdbc3.jdbc.SybDriver").newInstance();
	sybDriver.setVersion(com.sybase.jdbc3.jdbc.SybDriver.VERSION_6);
	try {
	    DriverManager.registerDriver(sybDriver);
	} catch (SQLException e) {
	    System.out.println("Fehler");
	}
	Properties props = new Properties();
	props.put("user", user);
	props.put("password", password);
	// Connection
	// test=DriverManager.getConnection("jdbc:sybase:Tds:194.95.184.5:5000/"+db,props);
	Connection test = DriverManager.getConnection("jdbc:sybase:Tds:"
		+ server + ":" + port + "/" + db, props);
	return test;
    }

    private Connection getMysqlConnection() throws Exception {
	Driver sybDriver = (Driver) Class.forName("com.mysql.jdbc.Driver")
		.newInstance();
	try {
	    DriverManager.registerDriver(sybDriver);
	} catch (SQLException e) {
	    System.out.println("Fehler");
	}
	Connection test = DriverManager.getConnection("jdbc:mysql:" + server
		+ ":" + port + "/" + db + "?user=" + user + "&password="
		+ password);
	return test;
    }

    private void produktToMethod() throws Exception {
	if (produkt.equals("Sybase")) {
	    con = getSybaseConnection();
	} else {
	    throw new Exception("nicht installiert");
	}

    }

    public DBConnector(String pr, String d, String u, String pa, String s, int p)
	    throws Exception {
	produkt = pr;
	db = d;
	user = u;
	password = pa;
	server = s;
	port = p;
    }

    public Connection getConnection() throws Exception {
	if (con == null) {
	    System.out.println("trying ...");
	    produktToMethod();
	    System.out.println("... connected.");
	}
	if (con.isClosed()) {
	    produktToMethod();
	}
	return con;
    }
}
