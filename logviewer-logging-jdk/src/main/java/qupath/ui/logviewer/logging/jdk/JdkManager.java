package qupath.ui.logviewer.logging.jdk;

import qupath.ui.logviewer.api.listener.LoggerListener;
import qupath.ui.logviewer.api.manager.LoggerManager;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Logger;

/**
 * Manager setting up and managing the JDK 1.4 logger.
 */
public class JdkManager implements LoggerManager {

    private static final Logger rootLogger = Logger.getLogger("");
    private static final Map<LoggerListener, Handler> handlers = new HashMap<>();

    @Override
    public void addListener(LoggerListener listener) {
        if (!handlers.containsKey(listener)) {
            Handler handler = new JdkHandler(listener);
            handlers.put(listener, handler);
            rootLogger.addHandler(handler);
        }
    }

    @Override
    public void removeListener(LoggerListener listener) {
        if (handlers.containsKey(listener)) {
            Handler handler = handlers.remove(listener);
            rootLogger.removeHandler(handler);
        }
    }

    @Override
    public void setRootLogLevel(Level level) {
        rootLogger.setLevel(switch (level) {
            case ERROR -> java.util.logging.Level.SEVERE;
            case WARN -> java.util.logging.Level.WARNING;
            case DEBUG, INFO -> java.util.logging.Level.INFO;
            case TRACE -> java.util.logging.Level.FINE;
        });
    }

    @Override
    public Level getRootLogLevel() {
        return toJdk14JLevel(rootLogger.getLevel());
    }

    @Override
    public boolean isFrameworkActive() {
        ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();
        return loggerFactory != null && loggerFactory.getClass().getName().startsWith("org.slf4j.jul");
    }

    static Level toJdk14JLevel(java.util.logging.Level level) {
        if (level.equals(java.util.logging.Level.SEVERE) || level.equals(java.util.logging.Level.ALL)) {
            return Level.ERROR;
        } else if (level.equals(java.util.logging.Level.WARNING)) {
            return Level.WARN;
        } else if (level.equals(java.util.logging.Level.INFO)) {
            return Level.INFO;
        } else {
            return Level.TRACE;
        }
    }
}
