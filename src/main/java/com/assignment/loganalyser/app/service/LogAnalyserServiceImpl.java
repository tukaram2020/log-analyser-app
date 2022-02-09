package com.assignment.loganalyser.app.service;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.assignment.loganalyser.app.config.ApplicationData;
import com.assignment.loganalyser.app.dto.Alert;
import com.assignment.loganalyser.app.model.Context;
import com.assignment.loganalyser.app.model.Event;
import com.assignment.loganalyser.app.model.State;
import com.assignment.loganalyser.app.repository.AlertRepository;
import com.assignment.loganalyser.app.validator.LogAnalyserValidator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @author tukaram
 *
 */

@Service
public class LogAnalyserServiceImpl implements LogAnalyserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogAnalyserServiceImpl.class);


    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private ApplicationData applicationData;
    
    @Autowired
    private LogAnalyserValidator validator;


    public void execute(String... args) {
        Context context = Context.getInstance();
        validator.validateInput(context, args);
        parseAndPersistEvents(context);
    }

	@Override
	public void parseAndPersistEvents(Context context) {
		// EventMap temporarily holds the events while we find the matching-STARTED or FINISHED-events.
        // Once found, the corresponding event would be removed from the map.
        Map<String, Event> eventMap = new HashMap<>();

        //Alerts map holds the events that are parsed before persisting in a DB table.
        // Each alert would have its execution time calculated and flagged (isAlert TRUE or FALSE).
        Map<String, Alert> alerts = new HashMap<>();

        LOGGER.info("Parsing the events and persisting the alerts. This may take a while...");
        try (LineIterator li = FileUtils.lineIterator(new ClassPathResource("samples/" + context.getLogFilePath()).getFile())) {
            while (li.hasNext()) {
                Event event;
                try {
                    event = new ObjectMapper().readValue(li.nextLine(), Event.class);
                    LOGGER.trace("{}", event);

                    // Check if we have either STARTED or FINISHED event already for the given ID.
                    // If yes, then find the execution time between STARTED and FINISHED states and update the alert.
                    if (eventMap.containsKey(event.getId())) {
                        extracted(eventMap, alerts, event);
                    } else {
                        eventMap.put(event.getId(), event);
                    }
                } catch (JsonProcessingException e) {
                    LOGGER.error("Unable to parse the event! {}", e.getMessage());
                }

                // to reduce memory consumption, write off the alerts once the pool has enough alerts
                if (alerts.size() > applicationData.getTableRowsWriteoffCount()) {
                    persistAlerts(alerts.values());
                    alerts = new HashMap<>();
                }
            } // END while
            if (alerts.size() != 0) {
                persistAlerts(alerts.values());
            }
        } catch (IOException e) {
            LOGGER.error("!!! Unable to access the file: {}", e.getMessage());
        }
		
	}

	/**
	 * @param eventMap
	 * @param alerts
	 * @param event
	 */
	private void extracted(Map<String, Event> eventMap, Map<String, Alert> alerts, Event event) {
		Event e1 = eventMap.get(event.getId());
		long executionTime = getEventExecutionTime(event, e1);

		// the alert created off an event would have the alert flag set to FALSE by default.
		Alert alert = new Alert(event, Math.toIntExact(executionTime));

		// if the execution time is more than the specified threshold, flag the alert as TRUE
		if (executionTime > applicationData.getAlertThresholdMs()) {
		    alert.setAlert(Boolean.TRUE);
		    LOGGER.trace("!!! Execution time for the event {} is {}ms", event.getId(), executionTime);
		}

		// add it to the pool of alerts that are yet to be persisted
		alerts.put(event.getId(), alert);

		// remove from the temporary map as we found the matching event
		eventMap.remove(event.getId());
	}
	
	 private void persistAlerts(Collection<Alert> alerts) {
	        LOGGER.debug("Persisting {} alerts...", alerts.size());
	        alertRepository.saveAll(alerts);
	    }

	    private long getEventExecutionTime(Event event1, Event event2) {
	        Event endEvent = Stream.of(event1, event2).filter(e -> State.FINISHED.equals(e.getState())).findFirst().orElse(null);
	        Event startEvent = Stream.of(event1, event2).filter(e -> State.STARTED.equals(e.getState())).findFirst().orElse(null);

	        return Objects.requireNonNull(endEvent).getTimestamp() - Objects.requireNonNull(startEvent).getTimestamp();
	    }

}
