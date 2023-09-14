package qupath.ui.logviewer.logging.reload4j;

import qupath.ui.logviewer.api.LogMessage;
import qupath.ui.logviewer.api.listener.LoggerListener;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

class Reload4jAppender extends AppenderSkeleton {

    private final LoggerListener listener;
    private boolean isClosed = false;

    public Reload4jAppender(LoggerListener listener) {
        this.listener = listener;
    }

    @Override
    protected void append(LoggingEvent event) {
        if (isClosed) {
            throw new RuntimeException("The appender is closed");
        } else {
            var message = new LogMessage(
                    event.getLoggerName(),
                    event.getTimeStamp(),
                    event.getThreadName(),
                    Reload4jManager.toSlf4JLevel(event.getLevel()),
                    event.getRenderedMessage(),
                    event.getThrowableInformation() == null ? null : event.getThrowableInformation().getThrowable()
            );
            listener.addLogMessage(message);
        }
    }

    @Override
    public void close() {
        isClosed = true;
    }

    @Override
    public boolean requiresLayout() {
        return false;
    }
}

