package com.epam.bench.service;

import com.epam.bench.domain.BenchHistory;
import com.epam.bench.domain.Employee;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service Interface for managing BenchHistory.
 */
public interface BenchHistoryService {

    /**
     * Save a benchHistory.
     *
     * @param benchHistory the entity to save
     * @return the persisted entity
     */
    BenchHistory save(BenchHistory benchHistory);

    /**
     *  Get all the benchHistories.
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    Page<BenchHistory> findAll(Pageable pageable);

    /**
     *  Get the "id" benchHistory.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    BenchHistory findOne(Long id);

    /**
     *  Delete the "id" benchHistory.
     *
     *  @param id the id of the entity
     */
    void delete(Long id);

    /**
     * Search for the benchHistory corresponding to the query.
     *
     *  @param query the query of the search
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    Page<BenchHistory> search(String query, Pageable pageable);

    /**
     * Find all BenchHistories that related to this employee
     *
     * @param employee
     * @return
     */
    List<BenchHistory> find(Employee employee);
}
