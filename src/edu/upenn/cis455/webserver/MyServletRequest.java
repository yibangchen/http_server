package edu.upenn.cis455.webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.Vector;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class MyServletRequest implements HttpServletRequest{
	
	private Properties m_params = new Properties();
	private Properties m_props = new Properties();
    private MyHttpSession m_session = null;
    private String m_method;
    private String characterEncoding = "ISO-8859-1";
    private String contextPath = "";
    private ResponseWriter buffer;
    private HashMap<String, String> initialLineMap;
    private HashMap<String, String> headLinesMap;
    private ArrayList<Cookie> cookieList;
    private static ServerLogger logger;
    	
	public MyServletRequest(HashMap<String, String> initialLineMap, 
							HashMap<String, String> headLinesMap,
							ResponseWriter buffer) {
		this.buffer = buffer;
		this.initialLineMap = initialLineMap;
		this.headLinesMap = headLinesMap;
		this.cookieList = buffer.getCookieList();
		logger = ServerLogger.getLogger(HttpServer.getLogFileName());
		initCookies();
	}
	
	public MyServletRequest(MyHttpSession session) {
		m_session = session;

	}
	
    public void setMethod(String method) {
        m_method = method;
    }

    public void setParameter(String key, String value) {
        m_params.setProperty(key, value);
    }

    public void clearParameters() {
        m_params.clear();
    }

	@Override
	public Object getAttribute(String key) {
		return m_props.get(key);
	}

	@Override
	public Enumeration<Object> getAttributeNames() {
		return m_props.keys(); // change to String here
	}

	@Override
	public String getCharacterEncoding() {
		return characterEncoding;
	}

	@Override
	public int getContentLength() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getContentType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLocalAddr() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLocalName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getLocalPort() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Locale getLocale() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getParameter(String value) {
		return m_params.getProperty(value);
	}

	@Override
	public Map getParameterMap() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Enumeration<Object> getParameterNames() {
		return m_params.keys();
	}

	@Override
	public String[] getParameterValues(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getProtocol() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BufferedReader getReader() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRealPath(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRemoteAddr() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRemoteHost() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getRemotePort() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getScheme() {
		return "http";
	}

	@Override
	public String getServerName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getServerPort() {
		return 0;
	}

	@Override
	public boolean isSecure() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeAttribute(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setAttribute(String arg0, Object arg1) {
		m_props.put(arg0, arg1);
	}

	@Override
	public void setCharacterEncoding(String value) throws UnsupportedEncodingException {
		characterEncoding = value;
	}

	@Override
	public String getAuthType() {
		return BASIC_AUTH; //Rewrite this part
	}

	@Override
	public String getContextPath() { // N/A for single app
		return contextPath;
	}

	@Override
	public Cookie[] getCookies() {
		return cookieList.toArray(new Cookie[cookieList.size()]);
	}

	@Override
	public long getDateHeader(String key) {
		String dateString = headLinesMap.get(key);
		Date dateObj;
		String format = "EEE, dd MMM yyyy HH:mm:ss z";
		SimpleDateFormat dateFormat = new SimpleDateFormat(format);
		long dateHeader = 0;
		
		if (dateString == null) return -1;
		
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		try {
			dateObj = dateFormat.parse(dateString);
			dateHeader = dateObj.getTime();
		} catch (ParseException e) {
			logger.writeFile(e.toString());
		}
		
		return dateHeader;
	}

	@Override
	public String getHeader(String key) {
		return headLinesMap.get(key);
	}

	@Override
	public Enumeration<String> getHeaderNames() {
		Set<String> keys = headLinesMap.keySet();
		Vector<String> headers = new Vector<String>(keys);
		return headers.elements();
	}

	@Override
	public Enumeration<String> getHeaders(String key) {
		String value = headLinesMap.get(key);
		Vector<String> vals = new Vector<String>();
		vals.addElement(value);
		return vals.elements();
	}

	@Override
	public int getIntHeader(String header) {
		String value = headLinesMap.get(header);
		return Integer.parseInt(value); //Exceptions??
	}

	@Override
	public String getMethod() {
		return m_method;
	}

	@Override
	public String getPathInfo() {
		String path = initialLineMap.get("path");
		String pathInfo = "";
		
		if (path.contains("?")) {
			String pathWithoutQuery = path.substring(0, path.lastIndexOf("?"));
			String[] pathElements = pathWithoutQuery.split("/");
			
			StringBuilder pathBuilder = new StringBuilder();
			for (int i = 2; i < pathElements.length; i++)
				pathBuilder.append("/" + pathElements[i]);
			pathInfo += pathBuilder.toString();
		}
		
		return pathInfo;
	}

	@Override
	public String getQueryString() {
		String path = initialLineMap.get("path");
		String queryString = path.substring(path.lastIndexOf("?") + 1, path.length());
		
		return queryString;
	}

	@Override
	public String getRemoteUser() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRequestURI() {
		String requestedURL = initialLineMap.get("path");
		if (requestedURL != null)
			return requestedURL.split("?")[1];
		return null;
	}

	@Override
	public StringBuffer getRequestURL() {
		String requestedURL = initialLineMap.get("path");
		
		return new StringBuffer(requestedURL);
	}

	@Override
	public String getRequestedSessionId() {
		if (m_session != null)
			return m_session.getId();
		return null;
	}

	@Override
	public String getServletPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpSession getSession() {
		return getSession(true);
	}

	@Override
	public HttpSession getSession(boolean flag) { // need to implement this!!
		boolean sessionInStock = false;
        if (flag) {
            if (!hasSession()) {
                for (Cookie c : cookieList) {
                    if (c.getName().equalsIgnoreCase("JSESSIONID")) {
                        if (SessionContainer.getSession(c.getValue()) != null) {
                            sessionInStock = true;
                            m_session = SessionContainer.getSession(c.getValue());
                            return m_session;
                        }
                    }
                }
                if (!sessionInStock) {
                    m_session = new MyHttpSession();
                    Cookie sessionCookie = new Cookie("JSESSIONID",
                            m_session.getSessoinId());
                    buffer.addCookie(sessionCookie);
                    SessionContainer.addSession(m_session);
                }
            }
        } else {
            if (!hasSession()) {
                m_session = null;
            }
        }
        return m_session;
	}
	
	boolean hasSession() {
        return ((m_session != null) && m_session.isValid());
    }

	@Override
	public boolean isRequestedSessionIdFromCookie() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromUrl() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRequestedSessionIdValid() {
		return false;
	}
	
	private void initCookies() {
        String cookiesString = headLinesMap.get("Cookie");
        if (cookiesString != null) {
            ArrayList<String> cookiesStringList = new ArrayList<String>(
                    Arrays.asList(cookiesString.split(";")));

            for (String cookie : cookiesStringList) {
                String trimmedString = cookie.trim(); // trim the string
                ArrayList<String> cookieString = new ArrayList<String>(
                        Arrays.asList(trimmedString.split("=")));
                cookieList.add(new Cookie(cookieString.get(0), cookieString
                        .get(1)));
            }
        }
    }
	
	@Override
	public ServletInputStream getInputStream() throws IOException {
		// NO NEED TO IMPLEMENT
		return null;
	}
	

	@Override
	public Enumeration getLocales() {
		// NO NEED TO IMPLEMENT
		return null;
	}
	
	@Override
	public RequestDispatcher getRequestDispatcher(String arg0) {
		// NO NEED TO IMPLEMENT
		return null;
	}

	@Override
	public String getPathTranslated() {
		// NO NEED TO IMPLEMENT
		return null;
	}

	@Override
	public Principal getUserPrincipal() {
		// NO NEED TO IMPLEMENT
		return null;
	}
	
	@Override
	public boolean isRequestedSessionIdFromURL() {
		// DEPRECATED
		return false;
	}

	@Override
	public boolean isUserInRole(String arg0) {
		// NO NEED TO IMPLEMENT
		return false;
	}

}
