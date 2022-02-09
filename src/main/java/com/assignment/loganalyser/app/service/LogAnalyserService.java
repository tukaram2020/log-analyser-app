package com.assignment.loganalyser.app.service;

import com.assignment.loganalyser.app.model.Context;

/**
 * 
 * @author tukaram
 *
 */

public interface LogAnalyserService {
	void parseAndPersistEvents(Context context);
}
