package edu.upenn.cis455.webserver;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

public class MyServletContext implements ServletContext{
	
	private ConcurrentHashMap<String, Object> attributes;
	private ConcurrentHashMap<String, String> initParams;
	
	public MyServletContext () {
		attributes = new ConcurrentHashMap<String,Object>();
		initParams = new ConcurrentHashMap<String,String>();
	}
	
	@Override
	public Object getAttribute(String name) {
		return attributes.get(name);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		Set<String> keys = attributes.keySet();
		Vector<String> atts = new Vector<String>(keys);
		return atts.elements();
	}

	@Override
	public ServletContext getContext(String name) {
		return null;
	}

	@Override
	public String getInitParameter(String name) {
		return initParams.get(name);
	}

	@Override
	public Enumeration<String> getInitParameterNames() {
		Set<String> keys = initParams.keySet();
		Vector<String> atts = new Vector<String>(keys);
		return atts.elements();
	}

	@Override
	public int getMajorVersion() {
		return 2;
	}

	@Override
	public int getMinorVersion() {
		return 4;
	}

	@Override
	public String getRealPath(String path) {
		return null;
	}

	@Override
	public String getServerInfo() {
		return null; // change later
	}

	@Override
	public Servlet getServlet(String name) throws ServletException {
		return null;
	}

	@Override
	public String getServletContextName() {
		return "Yibang's Servlet Context";
	}

	@Override
	public Enumeration<String> getServletNames() {
		return null;
	}

	@Override
	public Enumeration<String> getServlets() {
		return null;
	}

	public void log(String msg) {
		System.err.println(msg);
	}
	
	public void log(String message, Throwable throwable) {
		System.err.println(message);
		throwable.printStackTrace(System.err);
	}

	@Override
	public void removeAttribute(String name) {
		attributes.remove(name);
	}

	@Override
	public void setAttribute(String name, Object object) {
		attributes.put(name, object);
	}
	
	void setInitParam(String name, String value) {
		initParams.put(name, value);
	}

	
	
	@Override
	public String getMimeType(String arg0) {
		// NO NEED TO IMPLEMENT
		return null;
	}

	@Override
	public RequestDispatcher getNamedDispatcher(String arg0) {
		// NO NEED TO IMPLEMENT
		return null;
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String arg0) {
		// NO NEED TO IMPLEMENT
		return null;
	}

	@Override
	public URL getResource(String arg0) throws MalformedURLException {
		// NO NEED TO IMPLEMENT
		return null;
	}

	@Override
	public InputStream getResourceAsStream(String arg0) {
		// NO NEED TO IMPLEMENT
		return null;
	}

	@Override
	public Set getResourcePaths(String arg0) {
		// NO NEED TO IMPLEMENT
		return null;
	}

	@Override
	public void log(Exception arg0, String arg1) {
		// NO NEED TO IMPLEMENT
		
	}
}
