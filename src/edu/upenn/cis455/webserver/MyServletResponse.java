package edu.upenn.cis455.webserver;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

public class MyServletResponse implements HttpServletResponse{
	
	private HashMap<String, ArrayList<String>> initLineMap;
	private HashMap<String, ArrayList<String>> headLinesMap;
	private ArrayList<Cookie> cookieList;
	private ResponseWriter buffer;
	
	private int bufferSize = 1000;
	private String characterEncoding = "ISO-8859-1";
	private String contentType = "text/html";
	private Locale locale;
	private static ServerLogger logger;
	
	public MyServletResponse (ResponseWriter buffer) {
		this.buffer = buffer;
		initLineMap = buffer.getInitLineMap();
		setInitLineMap();
		headLinesMap = buffer.getHeadLinesMap();
		cookieList = buffer.getCookieList();
		logger = ServerLogger.getLogger(HttpServer.getLogFileName());
	}
	
	@Override
	/**
	 * Forces any content in buffer written to client:
	 * 	- Commit the response, i.e. status code and headers written
	 */
	public void flushBuffer() throws IOException {
		buffer.buildInitLine(initLineMap);
		buffer.buildHeadLines(headLinesMap);
		buffer.flushBuffer();
	}

	@Override
	public int getBufferSize() {
		return this.bufferSize;
	}

	@Override
	public String getCharacterEncoding() {
		return this.characterEncoding;
	}

	@Override
	public String getContentType() {
		return this.contentType;
	}

	@Override
	public Locale getLocale() {
		return this.locale;
	}

	@Override
	/**
	 * Get a PrintWriter object that can return character data to the client
	 */
	public PrintWriter getWriter() throws IOException {
		return (PrintWriter) (new MyPrintWriter(buffer, this));
	}

	@Override
	public boolean isCommitted() {
		return false;
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resetBuffer() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setBufferSize(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setCharacterEncoding(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setContentLength(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setContentType(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLocale(Locale arg0) {
		// TODO Auto-generated method stub
		
	}
	
	private void setInitLineMap() {
		ArrayList<String> http = new ArrayList<String>();
        http.add("HTTP/1.1");
        this.initLineMap.put("HTTP", http);
        ArrayList<String> statusCode = new ArrayList<String>();
        statusCode.add("200");
        this.initLineMap.put("statusCode", statusCode);
        ArrayList<String> statusDescription = new ArrayList<String>();
        statusDescription.add("OK");
        this.initLineMap.put("statusDescription", statusDescription);
	}

	@Override
	public void addCookie(Cookie clientCookie) {
		for (Cookie cookie : cookieList) {
			if (clientCookie.getName().equals(cookie.getName()) &&
					clientCookie.getValue().equals(cookie.getValue()))
				return;
		}
		cookieList.add(clientCookie);
	}

	@Override
	public void addDateHeader(String headerName, long dateLong) {
		addHeader(headerName, getDateString(dateLong));
	}

	@Override
	/**
	 * Add a value to a header name
	 * 	- multiple values allow for a header
	 */
	public void addHeader(String headerName, String headerValue) {
		ArrayList<String> headerValues = headLinesMap.get(headerName);
		if (headerValues == null) {
			headerValues = new ArrayList<String>(); 			
		}
		headerValues.add(headerValue);
		headLinesMap.put(headerName, headerValues);
	}

	@Override
	public void addIntHeader(String headerName, int value) {
		addHeader(headerName, Integer.toString(value));
	}
	
	@Override
	/**
	 * Method that checks if the named response header already exists
	 */
	public boolean containsHeader(String headerName) {
		return headLinesMap.containsKey(headerName);
	}

	@Override
	public String encodeRedirectUrl(String url) {
		return url;
	}

	@Override
	/**
	 * Returns the encoded URL if encoding is needed
	 */
	public String encodeURL(String url) {
		return url;
	}

	@Override
	public void sendError(int statusCode) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendError(int arg0, String arg1) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	/**
	 * Method that sends temp redirect response to client using URL (can be relative URL). 
	 */
	public void sendRedirect(String url) throws IOException {
		System.out.println("[DEBUG] redirect to " + url + " requested");
		System.out.println("[DEBUG] stack trace: ");
		Exception e = new Exception();
		StackTraceElement[] frames = e.getStackTrace();
		for (int i = 0; i < frames.length; i++) {
			System.out.print("[DEBUG]   ");
			System.out.println(frames[i].toString());
		}
	}
	@Override
	public void setDateHeader(String arg0, long arg1) {
		
	}

	@Override
	/**
	 * Set response header with (name, value):
	 * 	- If name exist, overwrite value
	 */
	public void setHeader(String headerName, String headerValue) {
		ArrayList<String> headerValues = headLinesMap.get(headerName);
		if (headerValues != null) { // add new header set
			headerValues.clear();
			headerValues.add(headerValue);
		} else { // update headerValue
			headerValues = new ArrayList<String>();
			headerValues.add(headerValue);
			headLinesMap.put(headerName, headerValues);
		}
	}

	@Override
	public void setIntHeader(String arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	/**
	 * Set the status code for response when NO error
	 * 	- if error: sendError
	 * 	- clears the buffer and sets the location header
	 *  - preserves cookies and headers
	 */
	public void setStatus(int statusCode) {
		if (statusCode == 200) {
			initLineMap.clear();
			ArrayList<String> http	 = new ArrayList<String>();
			ArrayList<String> respStatusCode = new ArrayList<String>();
			ArrayList<String> respStatusDesc = new ArrayList<String>();
			
			http.add("HTTP/1.1");
			respStatusCode.add("200");
			respStatusDesc.add("OK");
			
			initLineMap.put("HTTP", http);
			initLineMap.put("statusCode", respStatusCode);
			initLineMap.put("statusDescription", respStatusDesc);
		} else {
			try {
				sendError(statusCode);
			} catch (IOException e) {
				logger.writeFile(e.toString());
			}
		}
	}
	
	private String getDateString(long dateLong) {
		Date date = new Date(dateLong);
		SimpleDateFormat df = new SimpleDateFormat ("EEE, dd MMM yyyy HH:mm:ss z");
		df.setTimeZone(TimeZone.getTimeZone("GMT"));
		return df.format(date);
	}
	
	public ResponseWriter getResponseBuffer() {
        return buffer;
    }
		
	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		// NO NEED TO IMPLEMENT
		return null;
	}

	@Override
	public String encodeRedirectURL(String arg0) {
		// DEPRECATED
		return null;
	}

	@Override
	public String encodeUrl(String arg0) {
		// DEPRECATED
		return null;
	}

	@Override
	public void setStatus(int arg0, String arg1) {
		// DEPRECATED
	}

}
