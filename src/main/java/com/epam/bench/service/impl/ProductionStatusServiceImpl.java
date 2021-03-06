package com.epam.bench.service.impl;

import com.epam.bench.service.ProductionStatusService;
import com.epam.bench.domain.ProductionStatus;
import com.epam.bench.repository.ProductionStatusRepository;
import com.epam.bench.repository.search.ProductionStatusSearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * Service Implementation for managing ProductionStatus.
 */
@Service
@Transactional
public class ProductionStatusServiceImpl implements ProductionStatusService{

    private final Logger log = LoggerFactory.getLogger(ProductionStatusServiceImpl.class);
    
    @Inject
    private ProductionStatusRepository productionStatusRepository;

    @Inject
    private ProductionStatusSearchRepository productionStatusSearchRepository;

    /**
     * Save a productionStatus.
     *
     * @param productionStatus the entity to save
     * @return the persisted entity
     */
    public ProductionStatus save(ProductionStatus productionStatus) {
        log.debug("Request to save ProductionStatus : {}", productionStatus);
        ProductionStatus result = productionStatusRepository.save(productionStatus);
        productionStatusSearchRepository.save(result);
        return result;
    }

    /**
     *  Get all the productionStatuses.
     *  
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true) 
    public Page<ProductionStatus> findAll(Pageable pageable) {
        log.debug("Request to get all ProductionStatuses");
        Page<ProductionStatus> result = productionStatusRepository.findAll(pageable);
        return result;
    }

    /**
     *  Get one productionStatus by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true) 
    public ProductionStatus findOne(Long id) {
        log.debug("Request to get ProductionStatus : {}", id);
        ProductionStatus productionStatus = productionStatusRepository.findOne(id);
        return productionStatus;
    }

    /**
     *  Delete the  productionStatus by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete ProductionStatus : {}", id);
        productionStatusRepository.delete(id);
        productionStatusSearchRepository.delete(id);
    }

    /**
     * Search for the productionStatus corresponding to the query.
     *
     *  @param query the query of the search
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<ProductionStatus> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of ProductionStatuses for query {}", query);
        Page<ProductionStatus> result = productionStatusSearchRepository.search(queryStringQuery(query), pageable);
        return result;
    }
}
