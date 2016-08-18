package edu.upenn.cis455.webserver;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;

import javax.servlet.http.HttpServlet;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class HttpServer {
	private static ServerSocket serverSocket;
	private static Socket socket;
	private static int port = 8080;
	private static boolean isRunning = true;
	private static final int THREADCOUND = 100;
	private static final int TASKCAPACITY = 10000;
	private static String webdotxmlPath;
	private static String logFileName = "serverlog.txt";
	private static ServerLogger logger;
	private static SessionContainer sessionContainer;
	
	public static HashMap<String, HttpServlet> servlets;
	private static HashMap<String, String> servletUrl;
	
	/**
	 * This main method set up the server, create a thread pool and wait for client request
	 * @param args : args[0] - port number, args[1] - root directory
	 */
	public static void main(String args[]) {
		if (args.length < 3 || args.length % 2 == 0) {
			usage();
			System.exit(-1);
		}
		
		port = Integer.valueOf(args[0]);
		ThreadWorker.setDirectory(args[1]);
		webdotxmlPath = args[2];
		
		logger = ServerLogger.getLogger(logFileName);
		
		try {
			serverSocket = new ServerSocket(port, 10000);
		} catch (IOException e) {
			System.out.println("Cannot start server!");
			logger.writeFile(e.toString());
		}
		
		
		ThreadPool threadPool = new ThreadPool(THREADCOUND, TASKCAPACITY);
		sessionContainer = SessionContainer.getInstance();
		System.out.println("****Server is running...");
		System.out.println("Created by: Yibang Chen, PennKey: yibang");
		loadServlet();
		
		while (isRunning) {
			try {
				socket = serverSocket.accept();				
				threadPool.addToQueue(socket);
			} catch (SocketException e){
				logger.writeFile(e.toString());
				System.out.println("Socket closed!");
				return;
			} catch (IOException e) {
				logger.writeFile(e.toString());
			} finally {
				if (isRunning == false) {		
					break;
				}
			}
		}
	}
	
	public static void closeSocket() {
		try {
			closeServlet();
			isRunning = false;
			System.out.println("Server closed");
			serverSocket.close();
			logger.closeLogFile();
		} catch (IOException e) {
			logger.writeFile(e.toString());
		}
	}
	
	private static void closeServlet() {
		for (String servletPattern: servlets.keySet()) {
			servlets.get(servletPattern).destroy();
		}
	}
	
	public static SessionContainer getSessionManager() {
		return sessionContainer;
	}
			
	private static void loadServlet() {
		Handler h = null;
		MyServletContext context;
		
		try {
			h = parseWebdotxml(webdotxmlPath);
			context = createContext(h);
			servlets = createServlets(h, context);
			servletUrl = h.m_urlPattern;
			System.out.println("**Successfully loaded servlets");
		} catch (Exception e) {
			System.out.println("**servlets load UNSUCCESSFUl");
			System.out.println(e.toString());
			logger.writeFile(e.toString());
		}
	}
	
	public static String getLogFileName() {
		return logFileName;
	}
	
	private static Handler parseWebdotxml(String webdotxml) throws Exception {
		Handler h = new Handler();
		File file = new File(webdotxml);
		if (!file.exists()) {
			System.err.println("error: cannot find " + file.getPath());
			System.exit(-1);
		}
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		parser.parse(file, h);
		
		return h;
	}
	
	private static MyServletContext createContext(Handler h) {
		MyServletContext fc = new MyServletContext();
		for (String param : h.m_contextParams.keySet()) {
			fc.setInitParam(param, h.m_contextParams.get(param));
		}
		return fc;
	}
	
	private static HashMap<String,HttpServlet> createServlets(Handler h, MyServletContext fc) throws Exception {
		HashMap<String,HttpServlet> servlets = new HashMap<String,HttpServlet>();
		
		for (String servletName : h.m_servlets.keySet()) {			
			MyServletConfig config = new MyServletConfig(servletName, fc);
			String className = h.m_servlets.get(servletName);
			Class servletClass = Class.forName(className);
			HttpServlet servlet = (HttpServlet) servletClass.newInstance();
			HashMap<String,String> servletParams = h.m_servletParams.get(servletName);
			
			if (servletParams != null) {
				for (String param : servletParams.keySet()) {
					config.setInitParam(param, servletParams.get(param));
				}
			}
			servlet.init(config);
			servlets.put(servletName, servlet);
		}

		return servlets;
	}
	
	private static void usage() {
		System.err.println("usage: java TestHarness <path to web.xml> " 
				+ "[<GET|POST> <servlet?params> ...]");
	}
	
	public static HashMap<String, String> getUrlPattern () {
		return servletUrl;
	}
	
	public static HttpServlet getServlet(String servletPattern) {
		return servlets.get(servletPattern);
	}
	
    static class Handler extends DefaultHandler {
        private int m_state = 0;
        private String m_servletName;
        private String m_paramName;
        private String m_servletName2;
        HashMap<String, String> m_servlets = new HashMap<String, String>();
        HashMap<String, String> m_urlPattern = new HashMap<String, String>();
        HashMap<String, String> m_contextParams = new HashMap<String, String>();
        HashMap<String, HashMap<String, String>> m_servletParams = new HashMap<String, HashMap<String, String>>();
        
        public void startElement(String uri, String localName, String qName,
                Attributes attributes) {
            if (qName.compareTo("servlet") == 0) {
                m_state = 1;
            } else if (qName.compareTo("servlet-mapping") == 0) {
                m_state = 2;
            } else if (qName.compareTo("context-param") == 0) {
                m_state = 3;
            } else if (qName.compareTo("init-param") == 0) {
                m_state = 4;
            } else if (qName.compareTo("servlet-name") == 0) {
                m_state = (m_state == 1) ? 300 : 400;
            } else if (qName.compareTo("servlet-class") == 0) {
                m_state = 301;
            } else if (qName.compareTo("url-pattern") == 0) {
                m_state = 401;
            } else if (qName.compareTo("param-name") == 0) {
                m_state = (m_state == 3) ? 10 : 20;
            } else if (qName.compareTo("param-value") == 0) {
                m_state = (m_state == 10) ? 11 : 21;
            }
        }

        public void characters(char[] ch, int start, int length) {
            String value = new String(ch, start, length);
            /**
             * if(m_state == 1) { m_serverName = value; m_state = 0; }
             **/
            if (m_state == 300) {
                m_servletName = value;
                m_state = 0;
            } else if (m_state == 301) {
                m_servlets.put(m_servletName, value);
                m_state = 0;
            } else if (m_state == 400) {
                m_servletName2 = value;
                m_state = 0;
            } else if(m_state == 401) {
                m_urlPattern.put(value, m_servletName2);
                m_state = 0;
            } else if (m_state == 1) {
                m_servletName = value;
                m_state = 0;
            } else if (m_state == 2) {
                m_servlets.put(m_servletName, value);
                m_state = 0;
            } else if (m_state == 10 || m_state == 20) {
                m_paramName = value;
            } else if (m_state == 11) {
                if (m_paramName == null) {
                    System.err.println("Context parameter value '" + value + "' without name");
                    System.exit(-1);
                }
                m_contextParams.put(m_paramName, value);
                m_paramName = null;
                m_state = 0;
            } else if (m_state == 21) {
                if (m_paramName == null) {
                    System.err.println("Servlet parameter value '" + value + "' without name");
                    System.exit(-1);
                }
                HashMap<String, String> p = m_servletParams.get(m_servletName);
                if (p == null) {
                    p = new HashMap<String, String>();
                    m_servletParams.put(m_servletName, p);
                }
                p.put(m_paramName, value);
                m_paramName = null;
                m_state = 0;
            }
        } 
    } // End of DefaultHandler Class

}
