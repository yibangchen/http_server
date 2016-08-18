package edu.upenn.cis455.webserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Stack;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

public class ThreadWorker extends Thread{
	
	private BlockingQueue threadQueue;
	private boolean isRunning = false;
	private static String directory = "/";
	private String filePath;		
	private String inputLine;
	private ArrayList<String> rawRequestLines;
	private ArrayList<String> postContentList;
	private HashMap<String, String> requestMap;
	private HashMap<String, String> initLineMap;
	private HashMap<String, String> headLinesMap;	
	private HashMap<String, String> responseMap;
	
	private ServerLogger logger;
	
	public ThreadWorker (BlockingQueue queue) {
		this.threadQueue = queue;
		logger = ServerLogger.getLogger(HttpServer.getLogFileName());
	}
	
	public void run() {
		isRunning = true;
		while (isRunning) {
			
			Socket socket;
			
			rawRequestLines = new ArrayList<String>();
			requestMap = new HashMap<String, String>();
			initLineMap = new HashMap<String, String>();
			headLinesMap = new HashMap<String, String>();	
			responseMap = new HashMap<String, String>();
			
			try {
				socket = (Socket) threadQueue.dequeue();
				socket.setSoTimeout(50000);
				
				InputStreamReader 	reader 	= new InputStreamReader(socket.getInputStream());
				BufferedReader 		in 		= new BufferedReader(reader);
				PrintStream 		out	 	= new PrintStream(socket.getOutputStream(),true);
				
				this.processRequest(in);				
				filePath = initLineMap.get("path");
				putContentType(filePath);
				
				if (checkSecurity(filePath)) {
					int exceptionCode = getErrorCode();
					if (exceptionCode == 200) {
						matchRequest(out);
//						outputResponseHeaders(out);
//						outputResponseContent(out);
					} else {
						sendErrorMessage(out, exceptionCode);
					}
				} else {
					System.out.println("Insecure path request denied: 403 Forbidden");
					printError(out, 403);
				}
				
			} catch (IOException e) { // exception for getOutputStream()
				logger.writeFile(e.toString());
			} catch (InterruptedException e) { // for dequeue
				logger.writeFile(e.toString());
			} catch (NullPointerException e) { // for putContentType
				logger.writeFile(e.toString());
			}
		}
	}
	
	private void matchRequest(PrintStream out) {
		String servletPattern = getServletPattern();
		
		if (servletPattern != null) { // get a servlet to work on a request
			ResponseWriter buffer = new ResponseWriter();
			MyServletRequest request = new MyServletRequest(initLineMap, headLinesMap, buffer);
			MyServletResponse response = new MyServletResponse(buffer);
			
			String method = initLineMap.get("Method");
			String cLength = headLinesMap.get("Content-length");
			request.setMethod(method);
			
			if (method.equals("POST")) {
				String queryString = null;
				if (cLength == null) {
					String[] pathComponents = initLineMap.get("path").split("\\?");
					if (pathComponents.length == 2)
						queryString = pathComponents[1];
				} else {
					queryString = postContentList.get(0);
				}
				
				if (queryString != null) {
					String[] queryComponents = queryString.split("&");
					for (String item : queryComponents) {
						String[] keyValue = item.split("=");
						if (keyValue.length == 2) {
							request.setParameter(keyValue[0], keyValue[1]);
						}
					}
				}
			}
			
			HttpServlet servlet = HttpServer.getServlet(servletPattern);
			
			if (servlet != null) {
				try {
					MyHttpSession session = (MyHttpSession) request.getSession();
					servlet.service(request, response);
					response.flushBuffer();
					out.print(buffer.getFlushedResponse());
					out.flush();
					out.close();
				} catch (ServletException | IOException e) {
					System.out.println("Can't flush response!");
					logger.writeFile(e.toString());;
				}
			}
		}
	}
	
	/**
	 * Find the appropriate servlet name
	 * @return
	 */
	private String getServletPattern() {
		
		
		String[] pathComponents = initLineMap.get("path").split("/");
		
		if (pathComponents.length > 1) {
			String[] queryPath = pathComponents[1].split("\\?");
			String urlPattern = queryPath[0];
			HashMap<String, String> servletUrl = HttpServer.getUrlPattern();
			
			for (String key: servletUrl.keySet()) {
				String[] keyComponents = key.split("/");
				if (keyComponents.length == 3) {
					if (keyComponents[1].equalsIgnoreCase(urlPattern)) {
						return servletUrl.get(key);
					}
				}
			}
			
			if (servletUrl.get("/" + urlPattern) != null){
//				System.out.println("Servlet: /" + urlPattern);
				return servletUrl.get("/" + urlPattern);
			}
			return null;
		}
		return null;
	}
	
	/**
	 * This method takes user input (request)
	 * @param input
	 */
	private void processRequest(BufferedReader input) {
		int contentLen = 0;
		boolean isPost = false;
		
		while (true) {
			try {
				inputLine = input.readLine().trim();
				if (inputLine.contains("POST")) {
					isPost = true;
				}
				if (	inputLine.contains("Content-length")) {
					String contentLenString = inputLine.split(":")[1].trim();
					contentLen = Integer.parseInt(contentLenString);
				}
				if (	inputLine == null || 
						inputLine.equals("")) {
					if (isPost) {
						char[] postContent = new char[contentLen];
						input.read(postContent);
						postContentList.add(String.copyValueOf(postContent));
					}
					break;
				}
				rawRequestLines.add(inputLine);
			} catch (IOException e) {
				logger.writeFile(e.toString());
			} catch (NullPointerException e) {
				logger.writeFile(e.toString());
			}
		}
		
		// Error handling for empty request
		if (rawRequestLines.isEmpty())
			System.out.println("400 Bad Request");
		if (rawRequestLines.get(0) != null) // check if initial line is null -- NullPointerException
			parseIntialLine(rawRequestLines.get(0)); // parse the initial line
		if (rawRequestLines.size() > 1) // check if there are head lines
			parseHeadLines(rawRequestLines); // parse head lines
	}
		
	/**
	 * This method parses the initial line from the user input
	 * 	called from processRequest method
	 * @param initalLine : the first line of user input
	 */
	private void parseIntialLine(String initalLine) {
		final int ARGS_NO = 3;
		String[] args = initalLine.split("\\s+"); //split by whitespace
		
		if (args.length != ARGS_NO) return; //bad request
		
		initLineMap.put("Method", args[0].toUpperCase());
		initLineMap.put("path", args[1].toLowerCase());
		initLineMap.put("HTTP", args[2].toUpperCase());
		
		requestMap.put("Method", args[0].toUpperCase());
		requestMap.put("path", args[1].toLowerCase());
		requestMap.put("HTTP", args[2].toUpperCase());
	}
		
	/**
	 * This method parses the head lines from the user input
	 * 	called from processRequest method
	 * @param rawRequestLines : ArrayList of raw head line strings
	 */
	private void parseHeadLines(ArrayList<String> rawRequestLines) {
		ArrayList<String> headLineComponents;
		for (int i = 1; i < rawRequestLines.size(); i++) {// parse from the second line
			headLineComponents = decomposeHeadLine(rawRequestLines.get(i));
			headLinesMap.put(headLineComponents.get(0), headLineComponents.get(1));
			requestMap.put(headLineComponents.get(0), headLineComponents.get(1));
		}
	}
	
	/**
	 * This method decomposes a head line string into headers
	 * 	called in the parseHeadLines method
	 * @param headLine
	 * @return an ArrayList of headers
	 */
	private ArrayList<String> decomposeHeadLine(String headLine) {
		ArrayList<String> segmentsList = new ArrayList<String>();
		String[] rawSegments = headLine.split(":");
		
		segmentsList.add(rawSegments[0].trim());
		
		if (rawSegments.length == 1)
			segmentsList.add("");
		else if (rawSegments.length == 2)
			segmentsList.add(rawSegments[1].trim());
		else if (rawSegments.length > 2) {
			String comboHead = rawSegments[1];
			for (int i = 2; i < rawSegments.length; i++)
				comboHead = comboHead + ":" + rawSegments[i];
			segmentsList.add(comboHead.trim());
		}
		
		return segmentsList;
	}
	
	/**
	 * This method gets the client error according to HTTP
	 * @return
	 */
	private int getErrorCode () { // need to modify later
		if (	requestMap.get("HTTP").equals("HTTP/1.0") &&
				requestMap.get("path").contains("http://")) {
			return 404;
		} else if (	requestMap.get("HTTP").equals("HTTP/1.0") &&
					requestMap.get("Host") == null) {
			return 400;
		}
		return 200;
	}
	
	/**
	 * This method print the error to the client based on the errorCode
	 * @param out
	 * @param errorCode
	 */
	private void printError(PrintStream out, int errorCode) {
		if (errorCode == 404)
			out.print("404 Not Found\r\n");
		else if (errorCode == 400)
			out.print("400 Bad Request\r\n");
		else if (errorCode == 403)
			out.print("403 Forbidden\r\n");
		out.flush();
		out.close();
	}
	
	/**
	 * This method print the error to the server and trigger printError to the client
	 * @param responsePage
	 * @param errorCode
	 */
	private void sendErrorMessage(PrintStream responsePage, int errorCode) {
		if (errorCode == 404) {
			System.out.println("404 Not Found");
			printError(responsePage, 404);
		} else if (errorCode == 400) {
			System.out.println("400 Not Found");
			printError(responsePage, 400);
		} else if (errorCode == 403) {
			System.out.println("403 Forbidden");
			printError(responsePage, 403);
		}
	}
	
	/**
	 * This method checks if the directory path in request is secure
	 * @param directory
	 * @return true if secure; false otherwise
	 */
	private boolean checkSecurity(String directory) {
		String[] split = directory.split("/");
		Stack<String> folders = new Stack<String>();
		
		for (int i = 0; i < split.length; i++) {
			if (split[i].equals("..")) {
				if (!folders.empty()) {
					folders.pop();
					if (folders.empty())
						return false;
				} else throw new EmptyStackException();
			} else folders.push(split[i]);
		}
		return true;
	}
	
	/**
	 * This method outputs headers response to the client
	 * @param out
	 */
	private void outputResponseHeaders(PrintStream out) {
		String fileName =  initLineMap.get("path");  
		File file = new File(directory + fileName);
		String responseHeadLines;
		
		addResponseHeaders(file);
		responseHeadLines = buildResponseString();
		out.print(responseHeadLines);
		
		if(requestMap.get("Method").equalsIgnoreCase("HEAD")) {
			out.flush();
			out.close();
		}
	}
	
	/**
	 * This method outputs directory content response to the client
	 * @param out
	 */
	private void outputResponseContent(PrintStream out) {
        String fileName = initLineMap.get("path");
        
        if(!requestMap.get("Method").equals("HEAD")) { 
        	
        	File file = new File(directory + fileName);
        	this.addResponseHeaders(file);
            
            if(fileName.equalsIgnoreCase("/control")) {
                displayControlPage(out);
            }
            else if(fileName.equalsIgnoreCase("/shutdown")) {
                displayShutdownPage(out);
                shutdown();
            }
            else if(file.exists()) {
                putContentType(fileName);
                sendResponse(out, fileName);
            } else {
                System.out.println("File Not Exist: 404 Not Found\n");
                printError(out, 404);
            }
        }
    }
	
	/**
	 * This method outputs File content response to the client
	 * @param out
	 * @param fileName
	 */
    private void sendResponse(PrintStream out, String fileName) {
    	File f = new File(directory + fileName);
        String responseHeadlines = buildResponseString();
        try {
            if(fileName.equalsIgnoreCase("/shutdown")) {
                displayShutdownPage(out);
                shutdown();
            }else if(fileName.equalsIgnoreCase("/control")) {
            	displayControlPage(out);
            }else if(f.exists() 
                    && !responseHeadlines.contains("304") 
                    && !responseHeadlines.contains("412")) {
                if(f.isDirectory()) {
                    displayDirectoryPage(out, fileName);
                }
                else{
                	byte[] byteFile = getByteFile(fileName);
                	out.print("Content-length:" + byteFile.length + "\r\n");
                	out.println("\r\n");
                    out.write(byteFile);
                    out.flush();
                    out.close();
                }
            } else {
            	out.flush();
            	out.close();
            }
        } catch (IOException e) {
        	logger.writeFile(e.toString());
        }
    }
    
    /**
     * This method builds control page in HTML and displays to the client
     * @param out
     */
	private void displayControlPage(PrintStream out) {
		String htmlContent;
		HashMap<String, String> threadStatus =  ThreadPool.getThreadStatus();
        
        htmlContent = "<html>"
	        		+ "  <body>" 
	        		+ "    <h2>CIS455HW1 server by Yibang Chen, SEAS login: yibang</h2>" 
	        		+ "    <h3>Control Panel: current thread status</h3>" 
	        		+ "    <hr>";

        for(String threadId: threadStatus.keySet())
        	htmlContent += "<h4>Thread " + threadId + ":\t" + threadStatus.get(threadId) + "</h4>";
        
        htmlContent +="<a href=\"" + "shutdown" + "\"" + ">" + "Shutdown" + "</a>"
        			+ "  </body>" 
        			+ "</html>";
        
        out.println(htmlContent);
        out.flush();
        out.close();
    }
	
	/**
	 * This method builds the directory page in HTML and displays to the client
	 * @param out
	 * @param fileName
	 */
	private void displayDirectoryPage(PrintStream out, String fileName) {
        File f = new File(directory + fileName);
        File[] fileList = f.listFiles();
        String htmlContent;
        
        htmlContent = "<html>"
         			+ "  <body>"
         			+ "    <h2>CIS455HW1 server by Yibang Chen, SEAS login: yibang</h2>"
         			+ "    <h3>Current directory: "+ fileName +"</h3>"
        			+ "    <hr>";
        
        for(File file: fileList) {
                String path =  fileName + "/" + file.getName();
                path = path.trim().replaceAll("/+", "/");
                htmlContent += "<p>"
                			+  "	<a href=\"" + path + "\"" + ">" + file.getName() + "</a>"
                			+  "</p>";
        }
        
        htmlContent += "  </body>"
        			+  "<html>";
        out.println(htmlContent);
        out.flush();
        out.close();
    }
	
	/**
	 * This method builds the shutdown page in HTML and displays it to the client
	 * @param out
	 */
    private void displayShutdownPage(PrintStream out) {
    	String htmlContent;
    	
    	htmlContent = 	"<html>" + 
    					"	<body>" +
    					"		<h2>CIS455HW1 server by Yibang Chen, SEAS login: yibang</h2>" +
    					"		<h3>Server Control Panel</h3>" + 
    					"		<hr>" + 
    					"		<p>" +
    					"	 		<a href=\"" + "shutdown" + "\"" + ">" + "Shutdown" + "</a>" + 
    					"		</p>" +
    					"	</body>" + 
    					"</html>";
        out.println(htmlContent);
        out.flush();
        out.close();      
    }

	/**
	 * This method gets a file by filename and converts it to a byte array
	 * @param fileName
	 * @return byte file of the requested file name
	 */
	private byte[] getByteFile(String fileName) {
        File srcFile = new File(directory + fileName);
        int fileSize = (int)srcFile.length();
        byte[] byteFile = new byte[fileSize];
        FileInputStream fileInput;
        
        try {
            fileInput = new FileInputStream(srcFile);
            fileInput.read(byteFile);
            fileInput.close();
        } catch (IOException e) {
        	logger.writeFile(e.toString());
        }
        return byteFile;
    } 
	
	/**
	 * This method builds a string of response ready to output to the client
	 * @return
	 */
	private String buildResponseString() {
		String responseString = "";
		
        if(responseMap.get("expect") != null) {
            if(responseMap.get("expect").equalsIgnoreCase("100-continue"))
                responseString += "HTTP/1.1 100 Continue\r\n\n";
        }
        
        if(responseMap.get("modify") != null) {
            if(responseMap.get("modify").equalsIgnoreCase("304 Not Modified")) {
                responseString += 	"HTTP/1.1 304 Not Modified\r\n" +
                					responseMap.get("Date") + "\r\n" +
                					"Connection: close\r\n\n";
            }
            if(responseMap.get("modify").equalsIgnoreCase("412 Precondition Failed")) {
                responseString += 	"HTTP/1.1 412 Precondition Failed\r\n" +
                					responseMap.get("Date") + "\r\n" +
                					"Connection: close\r\n\n";
            }
            return responseString;
        }
        // main content:
        if(responseMap.get("HTTP").equals("HTTP/1.0")) {
            responseString += "HTTP/1.0 200 OK\r\n";
            if(responseMap.get("Date") != null) {
                responseString += "Date: " + responseMap.get("Date") + "\r\n";
            }
            responseString = "Content-Type: " + responseMap.get("Content-Type") + "\r\n";            
        }
        else if(responseMap.get("HTTP").equals("HTTP/1.1")) {
            responseString += "HTTP/1.1 200 OK\r\n";
            if(responseMap.get("Date") != null) {
                responseString += "Date: " + responseMap.get("Date") + "\r\n";
            }
            responseString += "Content-Type: " + responseMap.get("Content-Type") + "\r\n";           
        }
        
        responseString += "Connection: close\r\n\n";
        return responseString;
	}
	
	/**
	 * This method adds Content-Type header based on the fileName, which has suffix
	 * @param fileName
	 */
	private void putContentType(String fileName) {
		String mimeType;
		Path fPath;
		
		fPath = Paths.get(directory + fileName.toLowerCase());
		try {
			mimeType = Files.probeContentType(fPath);
			if (mimeType == null) {
				responseMap.put("Content-Type", "text/html");
			} else if (!mimeType.contains("directory")){
				responseMap.put("Content-Type", mimeType);
			} else {
				responseMap.put("Content-Type", "text/html");
			}	
		} catch (IOException e) { // for getting mimeType
			logger.writeFile(e.toString());
		}		
		
//		if			(!fName.contains(".")) {
//			responseMap.put("Content-Type", "text/html");
//		} else if	(fName.contains(".txt")) {
//			responseMap.put("Content-Type", "text/plain");
//		} else if 	(fName.contains(".html")) {
//			responseMap.put("Content-Type", "text/html");
//		} else if 	(fName.contains(".jpg")) {
//			responseMap.put("Content-Type", "image/jpeg");
//		} else if 	(fName.contains(".gif")) {
//			responseMap.put("Content-Type", "image/gif");
//		} else if 	(fName.contains(".png")) {
//			responseMap.put("Content-Type", "image/png");
//		} else { // for all other suffix, download directly
//			responseMap.put("Content-Type", "application/octet-stream");
//		}
	}
	
	/**
	 * This method add response headers based on request
	 * @param file
	 */
	public void addResponseHeaders(File file) {
		Date date;
		Date modifiedDate;
		Date unmodifiedDate;
		String form = "EEE, dd MMM yyyy HH:mm:ss z";		
		SimpleDateFormat dateFormat = new SimpleDateFormat(form);
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		// stuffing parsedRequests from requests
		responseMap.put("HTTP", requestMap.get("HTTP"));
		responseMap.put("path", requestMap.get("path"));
		responseMap.put("Method", requestMap.get("Method"));
		if (requestMap.get("expect") != null) //100 continue
			responseMap.put("expect", requestMap.get("expect"));
		
		// add "If-Modified-Since" and "If-Unmodified-Since" headers
		date = new Date();
		responseMap.put("Date", dateFormat.format(date));
		
		if(requestMap.get("If-Modified-Since") != null) {
			modifiedDate = convertToDate(requestMap.get("If-Modified-Since"));
			if (modifiedDate.getTime() < file.lastModified()) { //true
				responseMap.put("Modify", "True");
			} else { //false
				responseMap.put("Modify", "304 Not Modified");
			}
		}
		if(requestMap.get("If-Unmodified-Since") != null) {
			unmodifiedDate = convertToDate(requestMap.get("If-Unmodified-Since"));
			if (unmodifiedDate.getTime() > file.lastModified()) { //true
				responseMap.put("Modify", "True");
			} else { //false
				responseMap.put("Modify", "412 Precondition Failed");
			}
		}
	}
	
	/**
	 * This method converts a string of 3 formatted date into a date object
	 * @param dateString
	 * @return a date object
	 */
	private Date convertToDate(String dateString) {
		String form1 = "EEE, dd MMM yyyy HH:mm:ss z";
		String form2 = "EEEE, dd-MMM-yy HH:mm:ss z";
		String form3 = "EEE MMM dd HH:mm:ss yyyy";
		String form4 = "EEE, MMM dd, yyyy HH:mm:ss z";
		
		SimpleDateFormat dateFormat;
		Date date = null;
		
		if (dateString.charAt(3) == ',') {
			if (dateString.charAt(11) == ',')
				dateFormat = new SimpleDateFormat(form4);
			else 
				dateFormat = new SimpleDateFormat(form1);
		} else if (dateString.toUpperCase().contains("GMT")) {
			dateFormat = new SimpleDateFormat(form2);
		} else {
			dateFormat = new SimpleDateFormat(form3);
		}
		
		try {
			date = dateFormat.parse(dateString);
		} catch (ParseException e) {
			logger.writeFile(e.toString());
		}
		
		return date;
	}
	
	public static void setDirectory(String dirAddress) {
		directory = dirAddress;
	}
	
	public String getCurrentDirectory() {
		return filePath;
	}
	
	/**
	 * This method terminates all threads in the threadPool and shuts down the server
	 */
	private void shutdown() { 
        ThreadPool.terminate();
        HttpServer.closeSocket();
    }
	
	/**
	 * This method terminates this particular thread only
	 */
	public synchronized void terminate() {
		isRunning = false;
		this.interrupt();
	}
}
