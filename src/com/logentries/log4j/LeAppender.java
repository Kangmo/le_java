package com.logentries.log4j; 
/*
  * ﻿Logentries Log4J Logging agent
  * Copyright 2010,2011 Logentries, Jlizard
  * Mark Lacomber <marklacomber@gmail.com>
  * 
  * September 1st, 2011
  */                                        

import java.io.IOException;
import java.io.OutputStream;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class LeAppender extends AppenderSkeleton {

	private SSLSocket sock;
	private OutputStream conn;
	protected String key;
	protected String location;
	protected boolean debug;
	
	public void setKey(String key) { this.key = key; }
	public String getKey()			{ return this.key; }

	public void setLocation(String location)	{ this.location = location; }
	public String getLocation()				{ return this.location; }
	
	public void setDebug(boolean debug) {this.debug = debug; }
	public boolean getDebug() { return this.debug; }


	/**
	 * Empty Constructor
	 */
	public LeAppender() 
	{		
	}
	
	/**
	 * Opens the connection to Logentries, uses TCP with SSL
	 * @param key
	 * @param location
	 * @throws IOException
	 */
	 
	public void activateOptions()
	{
		try{
			SSLSocketFactory factory = (SSLSocketFactory)SSLSocketFactory.getDefault();
			this.sock = (SSLSocket)factory.createSocket("api.logentries.com", 443);
			this.sock.setTcpNoDelay(true);
			this.sock.startHandshake();
			this.conn = this.sock.getOutputStream();
			String buff = "PUT /" + this.getKey() + "/hosts/" + this.getLocation() + "/?realtime=1 HTTP/1.1\r\n";
			this.conn.write(buff.getBytes());
			buff = "Host: api.logentries.com\r\n";
			this.conn.write(buff.getBytes());
			buff = "Accept-Encoding: identity\r\n";
			this.conn.write(buff.getBytes());
			buff = "Transfer_Encoding: chunked\r\n\r\n";
			this.conn.write(buff.getBytes());
		}catch(IOException e)
		{
			if (this.getDebug()){
				System.err.println("Unable to connect to Logentries");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Implements AppenderSkeleton Append method, handles time and format
	 */
	protected void append(LoggingEvent event) {
		
		if(this.conn == null || this.sock == null)
		{
			this.activateOptions();
		}
		
		String MESSAGE = this.layout.format(event)+"\n";
		String complete = MESSAGE;
		try {
			this.conn.write(MESSAGE.getBytes());
			this.conn.flush();

			String[] stack = event.getThrowableStrRep();
			if (stack != null)
			{
				for(int i = 0; i < stack.length; i++)
				{
					complete += stack[i]+"\n";
					this.conn.write(stack[i].getBytes(), 0, stack[i].length());
					this.conn.write("\n".getBytes());
					this.conn.flush();
				}
			}
		}catch (IOException e2) {
			// TODO Auto-generated catch block
			if(this.debug){
				e2.printStackTrace();
			}
			try{
				this.activateOptions();
				this.conn.write(complete.getBytes());
				this.conn.flush();
			}catch(IOException ex){
				if(this.debug)
					ex.printStackTrace();
			}
		}
	}

	/**
	 * Close's all connection's to Logentries
	 */
	public void close() {
		
		try {
			this.conn.close();
			this.sock.close();
		} catch (IOException e) {
			return;
		}
	}

	public boolean requiresLayout() { return true; }

}
