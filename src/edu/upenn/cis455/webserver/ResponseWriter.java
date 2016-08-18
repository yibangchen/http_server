package edu.upenn.cis455.webserver;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import javax.servlet.http.Cookie;

/**
 * This class prepares servlet response
 * @author Yibang Chen
 *
 */
public class ResponseWriter {

    public StringBuilder initLineResponse = new StringBuilder();
    public StringBuilder headLinesResponse = new StringBuilder();
    private StringBuilder cookieHeaders = new StringBuilder();
    public StringBuilder messageBody = new StringBuilder();
    private String flushedMessageBody = "";
    private String initLineFinal = "";
    private String headerLinesFinal = "";
    private HashMap<String,ArrayList<String>> initLineMap = new HashMap<String,ArrayList<String>>();
    private HashMap<String,ArrayList<String>> headLinesMap = new HashMap<String,ArrayList<String>>();
    private ArrayList<Cookie> cookieList = new ArrayList<Cookie>();
    
    public void flushBuffer() {
        initLineFinal = initLineResponse.toString();
        System.out.println(initLineFinal);
        headerLinesFinal = headLinesResponse.toString();
        flushedMessageBody  =  messageBody.toString();
    }
    
    /**
     * This method builds the initial response line
     * @param initLineMap
     */
    public void buildInitLine(HashMap<String, ArrayList<String>> initLineMap) {
    	initLineResponse.append(	  getListString(initLineMap.get("HTTP"))+
        						" " + getListString(initLineMap.get("statusCode")) +
        						" " + getListString(initLineMap.get("statusDescription")) +
        						"\r\n");
    }

    /**
     * This method builds the header line
     * with one empty line at the end
     * @param initLineMap
     */
    public void buildHeadLines(HashMap<String, ArrayList<String>> headLinesMap) {
        for(String key:headLinesMap.keySet()) {
            if(key.equals("Set-Cookie")) {
                for(String cookie: headLinesMap.get(key))
                    headLinesResponse.append(key + ":" + cookie + "\r\n");
            }else
                headLinesResponse.append(key + ":" + getListString(headLinesMap.get(key)) + "\r\n");
        }
        headLinesResponse.append("\n");
    }

    /**
     * @return flushed response
     */
    public String getFlushedResponse() {
    	this.setCookieHeaderLines();
    	
        return initLineFinal 
        		+ cookieHeaders.toString()
        		+ headerLinesFinal  
        		+ flushedMessageBody;
    }
    
    /**
     * This method adds a new cookie to the list of cookies
     * @param c : cookie
     * @return void
     */   
    public void addCookie(Cookie c) {
        this.cookieList.add(c);
    }
    
    /**
     * This method adds a response header or updating its values
     * @param name
     * @param value
     */
	public void addHeader(String name, String value) {
    	ArrayList<String> headerValues = headLinesMap.get(name);
    	if (headerValues == null) {
    		headerValues = new ArrayList<String>();
    		headerValues.add(value);
    		headLinesMap.put(name, headerValues);
    	} else {
    		headerValues.add(value);
        }
    }
    
    /**
     * This method sets the cookies to the header lines
     */
	public void setCookieHeaderLines(){
		for (Cookie cookie : cookieList) {
			if (cookie.getMaxAge() == -1) { //persist session until shutdown
				String entry = String.format("Set-Cookie:%s=%s", cookie.getName(), cookie.getValue());
				this.cookieHeaders.append(entry + "\r\n");
			} else {
				Date timestamp = new Date();
				long cookieExp = timestamp.getTime() + cookie.getMaxAge() * 1000;
				String entry = String.format("Set-Cookie:%s=%s;expires=%s", 
								cookie.getName(), cookie.getValue(), getDateString(cookieExp));
				this.cookieHeaders.append(entry + "\r\n");
			}
		}
	}
	
	/**
	 * This method converts a long format date to string
	 * @param dateLong
	 * @return
	 */
	private String getDateString(long dateLong) {
		Date date = new Date(dateLong);
		SimpleDateFormat df = new SimpleDateFormat ("EEE, dd MMM yyyy HH:mm:ss z");
		df.setTimeZone(TimeZone.getTimeZone("GMT"));
		return df.format(date);
	}
	
    /**
     * This method converts a string list to a single string
     * @param list
     * @return string of content of list
     */
    private String getListString(ArrayList<String> list) {
        StringBuilder stringBuilder = new StringBuilder();
        if (list.size() >= 1) {
        	stringBuilder.append(list.get(0));
        	for (int i = 1; i < list.size(); i++) {
        		stringBuilder.append("," + list.get(i));
        	}
        }
        return stringBuilder.toString();
    }
    
    public HashMap<String,ArrayList<String>> getInitLineMap() {
    	return initLineMap;
    }
    
    public HashMap<String,ArrayList<String>> getHeadLinesMap() {
    	return headLinesMap;
    }
    
    public ArrayList<Cookie> getCookieList() {
    	return cookieList;
    }
    
    public void append(String str) {
        messageBody.append(str);
    }

    public void append(boolean b) {
        messageBody.append("" + b);
    }

    public void append(char c) {
        messageBody.append("" + c);
    }

    public void append(int i) {
        messageBody.append("" + i);
    }

    public void append(long l) {
        messageBody.append("" + l);
    }

    public void append(float f) {
        messageBody.append("" + f);
    }

    public void append(double d) {
        messageBody.append("" + d);
    }

    public void append(char[] s) {
        messageBody.append(new String(s));
    }
}