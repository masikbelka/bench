package com.epam.bench.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.epam.bench.domain.BenchHistory;
import com.epam.bench.service.BenchHistoryService;
import com.epam.bench.web.rest.util.HeaderUtil;
import com.epam.bench.web.rest.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * REST controller for managing BenchHistory.
 */
@RestController
@RequestMapping("/api")
public class BenchHistoryResource {

    private final Logger log = LoggerFactory.getLogger(BenchHistoryResource.class);
        
    @Inject
    private BenchHistoryService benchHistoryService;

    /**
     * POST  /bench-histories : Create a new benchHistory.
     *
     * @param benchHistory the benchHistory to create
     * @return the ResponseEntity with status 201 (Created) and with body the new benchHistory, or with status 400 (Bad Request) if the benchHistory has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/bench-histories",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<BenchHistory> createBenchHistory(@Valid @RequestBody BenchHistory benchHistory) throws URISyntaxException {
        log.debug("REST request to save BenchHistory : {}", benchHistory);
        if (benchHistory.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("benchHistory", "idexists", "A new benchHistory cannot already have an ID")).body(null);
        }
        BenchHistory result = benchHistoryService.save(benchHistory);
        return ResponseEntity.created(new URI("/api/bench-histories/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("benchHistory", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /bench-histories : Updates an existing benchHistory.
     *
     * @param benchHistory the benchHistory to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated benchHistory,
     * or with status 400 (Bad Request) if the benchHistory is not valid,
     * or with status 500 (Internal Server Error) if the benchHistory couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/bench-histories",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<BenchHistory> updateBenchHistory(@Valid @RequestBody BenchHistory benchHistory) throws URISyntaxException {
        log.debug("REST request to update BenchHistory : {}", benchHistory);
        if (benchHistory.getId() == null) {
            return createBenchHistory(benchHistory);
        }
        BenchHistory result = benchHistoryService.save(benchHistory);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("benchHistory", benchHistory.getId().toString()))
            .body(result);
    }

    /**
     * GET  /bench-histories : get all the benchHistories.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of benchHistories in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @RequestMapping(value = "/bench-histories",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<BenchHistory>> getAllBenchHistories(Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of BenchHistories");
        Page<BenchHistory> page = benchHistoryService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/bench-histories");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /bench-histories/:id : get the "id" benchHistory.
     *
     * @param id the id of the benchHistory to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the benchHistory, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/bench-histories/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<BenchHistory> getBenchHistory(@PathVariable Long id) {
        log.debug("REST request to get BenchHistory : {}", id);
        BenchHistory benchHistory = benchHistoryService.findOne(id);
        return Optional.ofNullable(benchHistory)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /bench-histories/:id : delete the "id" benchHistory.
     *
     * @param id the id of the benchHistory to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @RequestMapping(value = "/bench-histories/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deleteBenchHistory(@PathVariable Long id) {
        log.debug("REST request to delete BenchHistory : {}", id);
        benchHistoryService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("benchHistory", id.toString())).build();
    }

    /**
     * SEARCH  /_search/bench-histories?query=:query : search for the benchHistory corresponding
     * to the query.
     *
     * @param query the query of the benchHistory search 
     * @param pageable the pagination information
     * @return the result of the search
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @RequestMapping(value = "/_search/bench-histories",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<BenchHistory>> searchBenchHistories(@RequestParam String query, Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of BenchHistories for query {}", query);
        Page<BenchHistory> page = benchHistoryService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/bench-histories");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }


}
