package com.assignment.loganalyser.app.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.assignment.loganalyser.app.dto.Alert;

/**
 * 
 * @author tukaram
 *
 */

@Repository
public interface AlertRepository extends CrudRepository<Alert, String> {
}
