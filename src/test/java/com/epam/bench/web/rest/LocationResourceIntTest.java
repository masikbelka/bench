package com.epam.bench.web.rest;

import com.epam.bench.BenchApp;

import com.epam.bench.domain.Location;
import com.epam.bench.repository.LocationRepository;
import com.epam.bench.service.LocationService;
import com.epam.bench.repository.search.LocationSearchRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.hamcrest.Matchers.hasItem;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the LocationResource REST controller.
 *
 * @see LocationResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = BenchApp.class)
public class LocationResourceIntTest {

    private static final String DEFAULT_UPSA_ID = "AAAAA";
    private static final String UPDATED_UPSA_ID = "BBBBB";

    private static final String DEFAULT_NAME = "AAAAA";
    private static final String UPDATED_NAME = "BBBBB";

    @Inject
    private LocationRepository locationRepository;

    @Inject
    private LocationService locationService;

    @Inject
    private LocationSearchRepository locationSearchRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Inject
    private EntityManager em;

    private MockMvc restLocationMockMvc;

    private Location location;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        LocationResource locationResource = new LocationResource();
        ReflectionTestUtils.setField(locationResource, "locationService", locationService);
        this.restLocationMockMvc = MockMvcBuilders.standaloneSetup(locationResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Location createEntity(EntityManager em) {
        Location location = new Location()
                .upsaId(DEFAULT_UPSA_ID)
                .name(DEFAULT_NAME);
        return location;
    }

    @Before
    public void initTest() {
        locationSearchRepository.deleteAll();
        location = createEntity(em);
    }

    @Test
    @Transactional
    public void createLocation() throws Exception {
        int databaseSizeBeforeCreate = locationRepository.findAll().size();

        // Create the Location

        restLocationMockMvc.perform(post("/api/locations")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(location)))
                .andExpect(status().isCreated());

        // Validate the Location in the database
        List<Location> locations = locationRepository.findAll();
        assertThat(locations).hasSize(databaseSizeBeforeCreate + 1);
        Location testLocation = locations.get(locations.size() - 1);
        assertThat(testLocation.getUpsaId()).isEqualTo(DEFAULT_UPSA_ID);
        assertThat(testLocation.getName()).isEqualTo(DEFAULT_NAME);

        // Validate the Location in ElasticSearch
        Location locationEs = locationSearchRepository.findOne(testLocation.getId());
        assertThat(locationEs).isEqualToComparingFieldByField(testLocation);
    }

    @Test
    @Transactional
    public void getAllLocations() throws Exception {
        // Initialize the database
        locationRepository.saveAndFlush(location);

        // Get all the locations
        restLocationMockMvc.perform(get("/api/locations?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.[*].id").value(hasItem(location.getId().intValue())))
                .andExpect(jsonPath("$.[*].upsaId").value(hasItem(DEFAULT_UPSA_ID.toString())))
                .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())));
    }

    @Test
    @Transactional
    public void getLocation() throws Exception {
        // Initialize the database
        locationRepository.saveAndFlush(location);

        // Get the location
        restLocationMockMvc.perform(get("/api/locations/{id}", location.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(location.getId().intValue()))
            .andExpect(jsonPath("$.upsaId").value(DEFAULT_UPSA_ID.toString()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingLocation() throws Exception {
        // Get the location
        restLocationMockMvc.perform(get("/api/locations/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateLocation() throws Exception {
        // Initialize the database
        locationService.save(location);

        int databaseSizeBeforeUpdate = locationRepository.findAll().size();

        // Update the location
        Location updatedLocation = locationRepository.findOne(location.getId());
        updatedLocation
                .upsaId(UPDATED_UPSA_ID)
                .name(UPDATED_NAME);

        restLocationMockMvc.perform(put("/api/locations")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(updatedLocation)))
                .andExpect(status().isOk());

        // Validate the Location in the database
        List<Location> locations = locationRepository.findAll();
        assertThat(locations).hasSize(databaseSizeBeforeUpdate);
        Location testLocation = locations.get(locations.size() - 1);
        assertThat(testLocation.getUpsaId()).isEqualTo(UPDATED_UPSA_ID);
        assertThat(testLocation.getName()).isEqualTo(UPDATED_NAME);

        // Validate the Location in ElasticSearch
        Location locationEs = locationSearchRepository.findOne(testLocation.getId());
        assertThat(locationEs).isEqualToComparingFieldByField(testLocation);
    }

    @Test
    @Transactional
    public void deleteLocation() throws Exception {
        // Initialize the database
        locationService.save(location);

        int databaseSizeBeforeDelete = locationRepository.findAll().size();

        // Get the location
        restLocationMockMvc.perform(delete("/api/locations/{id}", location.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate ElasticSearch is empty
        boolean locationExistsInEs = locationSearchRepository.exists(location.getId());
        assertThat(locationExistsInEs).isFalse();

        // Validate the database is empty
        List<Location> locations = locationRepository.findAll();
        assertThat(locations).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchLocation() throws Exception {
        // Initialize the database
        locationService.save(location);

        // Search the location
        restLocationMockMvc.perform(get("/api/_search/locations?query=id:" + location.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(location.getId().intValue())))
            .andExpect(jsonPath("$.[*].upsaId").value(hasItem(DEFAULT_UPSA_ID.toString())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())));
    }
}
