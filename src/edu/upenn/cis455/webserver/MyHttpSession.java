package edu.upenn.cis455.webserver;

import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

public class MyHttpSession implements HttpSession{
	
	private Properties m_props = new Properties();
	private boolean m_valid = true;
	private String sessionId;
	private Date lastAccessedTime;
	private Date creationTime;

	public MyHttpSession() {
		creationTime = new Date();
		lastAccessedTime = creationTime;
		sessionId = UUID.randomUUID().toString();
	}
	
	public String getSessoinId() {
        return sessionId;
    }
	
	@Override
	public Object getAttribute(String attr) {
		return m_props.get(attr);
	}

	@Override
	public Enumeration getAttributeNames() {
		return m_props.keys();
	}

	@Override
	public long getCreationTime() {
		return creationTime.getTime();
	}

	@Override
	public String getId() {
		return sessionId;
	}

	@Override
	public long getLastAccessedTime() {
		return lastAccessedTime.getTime();
	}

	@Override
	public int getMaxInactiveInterval() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ServletContext getServletContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void invalidate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isNew() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeAttribute(String arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void setAttribute(String arg0, Object arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setMaxInactiveInterval(int arg0) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public HttpSessionContext getSessionContext() {
		// DEPRECATED
		return null;
	}
	
	@Override
	public Object getValue(String arg0) {
		// DEPRECATED
		return null;
	}
	
	@Override
	public String[] getValueNames() {
		// DEPRECATED
		return null;
	}
	
	@Override
	public void putValue(String arg0, Object arg1) {
		// DEPRECATED
		
	}

	@Override
	public void removeValue(String arg0) {
		// DEPRECATED
		
	}

	public boolean isValid() {
		return m_valid;
	}
}
