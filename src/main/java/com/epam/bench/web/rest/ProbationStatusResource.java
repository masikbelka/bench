package com.epam.bench.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.epam.bench.domain.ProbationStatus;
import com.epam.bench.service.ProbationStatusService;
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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * REST controller for managing ProbationStatus.
 */
@RestController
@RequestMapping("/api")
public class ProbationStatusResource {

    private final Logger log = LoggerFactory.getLogger(ProbationStatusResource.class);
        
    @Inject
    private ProbationStatusService probationStatusService;

    /**
     * POST  /probation-statuses : Create a new probationStatus.
     *
     * @param probationStatus the probationStatus to create
     * @return the ResponseEntity with status 201 (Created) and with body the new probationStatus, or with status 400 (Bad Request) if the probationStatus has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/probation-statuses",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<ProbationStatus> createProbationStatus(@RequestBody ProbationStatus probationStatus) throws URISyntaxException {
        log.debug("REST request to save ProbationStatus : {}", probationStatus);
        if (probationStatus.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("probationStatus", "idexists", "A new probationStatus cannot already have an ID")).body(null);
        }
        ProbationStatus result = probationStatusService.save(probationStatus);
        return ResponseEntity.created(new URI("/api/probation-statuses/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("probationStatus", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /probation-statuses : Updates an existing probationStatus.
     *
     * @param probationStatus the probationStatus to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated probationStatus,
     * or with status 400 (Bad Request) if the probationStatus is not valid,
     * or with status 500 (Internal Server Error) if the probationStatus couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/probation-statuses",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<ProbationStatus> updateProbationStatus(@RequestBody ProbationStatus probationStatus) throws URISyntaxException {
        log.debug("REST request to update ProbationStatus : {}", probationStatus);
        if (probationStatus.getId() == null) {
            return createProbationStatus(probationStatus);
        }
        ProbationStatus result = probationStatusService.save(probationStatus);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("probationStatus", probationStatus.getId().toString()))
            .body(result);
    }

    /**
     * GET  /probation-statuses : get all the probationStatuses.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of probationStatuses in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @RequestMapping(value = "/probation-statuses",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<ProbationStatus>> getAllProbationStatuses(Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of ProbationStatuses");
        Page<ProbationStatus> page = probationStatusService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/probation-statuses");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /probation-statuses/:id : get the "id" probationStatus.
     *
     * @param id the id of the probationStatus to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the probationStatus, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/probation-statuses/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<ProbationStatus> getProbationStatus(@PathVariable Long id) {
        log.debug("REST request to get ProbationStatus : {}", id);
        ProbationStatus probationStatus = probationStatusService.findOne(id);
        return Optional.ofNullable(probationStatus)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /probation-statuses/:id : delete the "id" probationStatus.
     *
     * @param id the id of the probationStatus to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @RequestMapping(value = "/probation-statuses/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deleteProbationStatus(@PathVariable Long id) {
        log.debug("REST request to delete ProbationStatus : {}", id);
        probationStatusService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("probationStatus", id.toString())).build();
    }

    /**
     * SEARCH  /_search/probation-statuses?query=:query : search for the probationStatus corresponding
     * to the query.
     *
     * @param query the query of the probationStatus search 
     * @param pageable the pagination information
     * @return the result of the search
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @RequestMapping(value = "/_search/probation-statuses",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<ProbationStatus>> searchProbationStatuses(@RequestParam String query, Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of ProbationStatuses for query {}", query);
        Page<ProbationStatus> page = probationStatusService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/probation-statuses");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }


}
