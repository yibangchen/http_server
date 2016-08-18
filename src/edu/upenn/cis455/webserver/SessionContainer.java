package edu.upenn.cis455.webserver;

import java.util.HashMap;

/**
 * This class stores a pool of sessions opened and can check session availability
 * @author Yibang Chen
 *
 */
public class SessionContainer {
    private static SessionContainer container = null;
    static HashMap<String, MyHttpSession> sessionPool;

    private SessionContainer() {
        sessionPool = new HashMap<String, MyHttpSession>();
    }

    public static SessionContainer getInstance() {
        if (container == null) {
            container = new SessionContainer();
        }
        return container;
    }

    /**
     * return the Session Cookie Pair by id
     * 
     * @param id
     * @return
     */
    public MyHttpSession getSessionCookiePair(String id) {
        return sessionPool.get(id);
    }

    public static MyHttpSession getSession(String sessionId) {
        return sessionPool.get(sessionId);
    }

    public static void addSession(MyHttpSession m_session) {
        sessionPool.put(m_session.getId(), m_session);
    }

}