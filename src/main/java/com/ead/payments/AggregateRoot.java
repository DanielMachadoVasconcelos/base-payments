package com.ead.payments;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AggregateRoot {

    protected UUID id;
    private int version = -1;
    private final List<BaseEvent> changes = new LinkedList<>();
    private final Logger logger = Logger.getLogger(AggregateRoot.class.getSimpleName());

    public UUID getId() {
        return id;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public List<BaseEvent> getUncommittedChanges() {
        return this.changes;
    }

    public void markChangesAsCommitted() {
        this.changes.clear();
    }

    protected void applyChange(BaseEvent event, Boolean isNewEvent) {
        try {
            var method = getClass().getDeclaredMethod("apply", event.getClass());
            method.setAccessible(true);
            method.invoke(this, event);
        } catch (NoSuchMethodException e) {
            logger.log(Level.WARNING, MessageFormat.format("The 'apply' method was not found in the aggregate for event {0}", event.getClass().getName()));
        } catch (InvocationTargetException e) {
            logger.log(Level.SEVERE, MessageFormat.format("The 'apply' method throw a error for event {0}", event.getClass().getName()), e);
        } catch (IllegalAccessException e) {
            logger.log(Level.SEVERE, MessageFormat.format("Error applying the event {0} to the aggregate", event.getClass().getName()), e);
        } finally {
            if (isNewEvent) {
                changes.add(event);
            }
        }
    }

    public void raiseEvent(BaseEvent event) {
        applyChange(event, true);
    }

    public void replayEvents(Iterable<BaseEvent> events) {
        events.forEach(event -> applyChange(event, false));
    }
}
