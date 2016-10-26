package com.epam.bench.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.epam.bench.domain.OpportunityPosition;
import com.epam.bench.service.OpportunityPositionService;
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
 * REST controller for managing OpportunityPosition.
 */
@RestController
@RequestMapping("/api")
public class OpportunityPositionResource {

    private final Logger log = LoggerFactory.getLogger(OpportunityPositionResource.class);
        
    @Inject
    private OpportunityPositionService opportunityPositionService;

    /**
     * POST  /opportunity-positions : Create a new opportunityPosition.
     *
     * @param opportunityPosition the opportunityPosition to create
     * @return the ResponseEntity with status 201 (Created) and with body the new opportunityPosition, or with status 400 (Bad Request) if the opportunityPosition has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/opportunity-positions",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<OpportunityPosition> createOpportunityPosition(@Valid @RequestBody OpportunityPosition opportunityPosition) throws URISyntaxException {
        log.debug("REST request to save OpportunityPosition : {}", opportunityPosition);
        if (opportunityPosition.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("opportunityPosition", "idexists", "A new opportunityPosition cannot already have an ID")).body(null);
        }
        OpportunityPosition result = opportunityPositionService.save(opportunityPosition);
        return ResponseEntity.created(new URI("/api/opportunity-positions/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("opportunityPosition", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /opportunity-positions : Updates an existing opportunityPosition.
     *
     * @param opportunityPosition the opportunityPosition to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated opportunityPosition,
     * or with status 400 (Bad Request) if the opportunityPosition is not valid,
     * or with status 500 (Internal Server Error) if the opportunityPosition couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/opportunity-positions",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<OpportunityPosition> updateOpportunityPosition(@Valid @RequestBody OpportunityPosition opportunityPosition) throws URISyntaxException {
        log.debug("REST request to update OpportunityPosition : {}", opportunityPosition);
        if (opportunityPosition.getId() == null) {
            return createOpportunityPosition(opportunityPosition);
        }
        OpportunityPosition result = opportunityPositionService.save(opportunityPosition);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("opportunityPosition", opportunityPosition.getId().toString()))
            .body(result);
    }

    /**
     * GET  /opportunity-positions : get all the opportunityPositions.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of opportunityPositions in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @RequestMapping(value = "/opportunity-positions",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<OpportunityPosition>> getAllOpportunityPositions(Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of OpportunityPositions");
        Page<OpportunityPosition> page = opportunityPositionService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/opportunity-positions");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /opportunity-positions/:id : get the "id" opportunityPosition.
     *
     * @param id the id of the opportunityPosition to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the opportunityPosition, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/opportunity-positions/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<OpportunityPosition> getOpportunityPosition(@PathVariable Long id) {
        log.debug("REST request to get OpportunityPosition : {}", id);
        OpportunityPosition opportunityPosition = opportunityPositionService.findOne(id);
        return Optional.ofNullable(opportunityPosition)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /opportunity-positions/:id : delete the "id" opportunityPosition.
     *
     * @param id the id of the opportunityPosition to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @RequestMapping(value = "/opportunity-positions/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deleteOpportunityPosition(@PathVariable Long id) {
        log.debug("REST request to delete OpportunityPosition : {}", id);
        opportunityPositionService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("opportunityPosition", id.toString())).build();
    }

    /**
     * SEARCH  /_search/opportunity-positions?query=:query : search for the opportunityPosition corresponding
     * to the query.
     *
     * @param query the query of the opportunityPosition search 
     * @param pageable the pagination information
     * @return the result of the search
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @RequestMapping(value = "/_search/opportunity-positions",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<OpportunityPosition>> searchOpportunityPositions(@RequestParam String query, Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of OpportunityPositions for query {}", query);
        Page<OpportunityPosition> page = opportunityPositionService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/opportunity-positions");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }


}
