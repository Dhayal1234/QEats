
/*
 *
 *  * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.crio.qeats.QEatsApplication;
import com.crio.qeats.dto.Restaurant;
import com.crio.qeats.exchanges.GetRestaurantsRequest;
import com.crio.qeats.exchanges.GetRestaurantsResponse;
import com.crio.qeats.repositoryservices.RestaurantRepositoryService;
import com.crio.qeats.utils.FixtureHelpers;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import static org.mockito.ArgumentMatchers.eq;

// TODO: CRIO_TASK_MODULE_RESTAURANTSAPI
//  Pass all the RestaurantService test cases.
// Contains necessary test cases that check for implementation correctness.
// Objectives:
// 1. Make modifications to the tests if necessary so that all test cases pass
// 2. Test RestaurantService Api by mocking RestaurantRepositoryService.

@SpringBootTest(classes = {QEatsApplication.class})
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@DirtiesContext
@ActiveProfiles("test")
class RestaurantServiceTest {

  private static final String FIXTURES = "fixtures/exchanges";
  @InjectMocks
  private RestaurantServiceImpl restaurantService;
  @MockBean
  private RestaurantRepositoryService restaurantRepositoryServiceMock;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setup() {
    MockitoAnnotations.initMocks(this);

    objectMapper = new ObjectMapper();
  }

  private String getServingRadius(List<Restaurant> restaurants, LocalTime timeOfService) {
    when(restaurantRepositoryServiceMock
        .findAllRestaurantsCloseBy(any(Double.class), any(Double.class), any(LocalTime.class),
            any(Double.class)))
        .thenReturn(restaurants);

    GetRestaurantsResponse allRestaurantsCloseBy = restaurantService
        .findAllRestaurantsCloseBy(new GetRestaurantsRequest(20.0, 30.0),
            timeOfService); //LocalTime.of(19,00));

    assertEquals(2, allRestaurantsCloseBy.getRestaurants().size());
    assertEquals("11", allRestaurantsCloseBy.getRestaurants().get(0).getRestaurantId());
    assertEquals("12", allRestaurantsCloseBy.getRestaurants().get(1).getRestaurantId());

    ArgumentCaptor<Double> servingRadiusInKms = ArgumentCaptor.forClass(Double.class);
    verify(restaurantRepositoryServiceMock, times(1))
        .findAllRestaurantsCloseBy(any(Double.class), any(Double.class), any(LocalTime.class),
            servingRadiusInKms.capture());

    return servingRadiusInKms.getValue().toString();
  }

  @Test
  void peakHourServingRadiusOf3KmsAt7Pm() throws IOException {
    assertEquals(getServingRadius(loadRestaurantsDuringPeakHours(), LocalTime.of(19, 0)), "3.0");
  }


  @Test
  void normalHourServingRadiusIs5Kms() throws IOException {

    // TODO: CRIO_TASK_MODULE_RESTAURANTSAPI
    // We must ensure the API retrieves only restaurants that are closeby and are open
    // In short, we need to test:
    // 1. If the mocked service methods are being called
    // 2. If the expected restaurants are being returned
    // HINT: Use the `loadRestaurantsDuringNormalHours` utility method to speed things up
    // Load the list of restaurants during normal hours
    List<Restaurant> restaurants = loadRestaurantsDuringNormalHours();
    
    // Mock the service method to return the list of restaurants
    when(restaurantRepositoryServiceMock
        .findAllRestaurantsCloseBy(any(Double.class), any(Double.class), any(LocalTime.class),
            any(Double.class)))
        .thenReturn(restaurants);

    // Invoke the service method
    GetRestaurantsResponse response = restaurantService
        .findAllRestaurantsCloseBy(new GetRestaurantsRequest(20.0, 30.0),
            LocalTime.of(12, 0)); // Assuming 12 PM is during normal hours

    // Verify that the mocked service method is called with correct arguments
    verify(restaurantRepositoryServiceMock, times(1))
        .findAllRestaurantsCloseBy(any(Double.class), any(Double.class), any(LocalTime.class),
            eq(5.0)); // Ensure the serving radius is 5 kms during normal hours

    // Verify that the response contains the expected restaurants
    assertEquals(restaurants.size(), response.getRestaurants().size());
    // You may add more assertions to validate the contents of the response if needed


    // assertFalse(false);
  }
  
  @Test
    void testFindAllRestaurantsCloseBy() throws IOException {
    // Prepare test data
    List<Restaurant> mockRestaurants = new ArrayList<>();
          Restaurant mockRestaurant = new Restaurant();
      mockRestaurant.setRestaurantId("1");
      mockRestaurant.setName("Mock Restaurant");
      mockRestaurant.setCity("Mock City");
      mockRestaurant.setImageUrl("https://example.com/mock-image.jpg");
      mockRestaurant.setLatitude(20.0);
      mockRestaurant.setLongitude(30.0);
      mockRestaurant.setOpensAt("10:00");
      mockRestaurant.setClosesAt("20:00");
// Set other properties as needed

// Add the mock restaurant to the list of mock restaurants
    mockRestaurants.add(mockRestaurant);

    // Mock behavior
    when(restaurantRepositoryServiceMock
        .findAllRestaurantsCloseBy(any(Double.class), any(Double.class), any(LocalTime.class),
            any(Double.class)))
        .thenReturn(mockRestaurants);

    // Invoke the method under test
    GetRestaurantsResponse response = restaurantService
        .findAllRestaurantsCloseBy(new GetRestaurantsRequest(20.0, 30.0),
            LocalTime.of(12, 0)); // Assuming 12 PM is during normal hours

    // Verify the mocked method is called with the correct arguments
    verify(restaurantRepositoryServiceMock, times(1))
        .findAllRestaurantsCloseBy(eq(20.0), eq(30.0), any(LocalTime.class), eq(5.0));

    // Verify the response contains the expected restaurants
    assertEquals(mockRestaurants.size(), response.getRestaurants().size());
    // You may add more assertions to validate the contents of the response if needed
  }


  
  private List<Restaurant> loadRestaurantsDuringNormalHours() throws IOException {
    String fixture =
        FixtureHelpers.fixture(FIXTURES + "/normal_hours_list_of_restaurants.json");

    return objectMapper.readValue(fixture, new TypeReference<List<Restaurant>>() {
    });
  }

  private List<Restaurant> loadRestaurantsSearchedByAttributes() throws IOException {
    String fixture =
        FixtureHelpers.fixture(FIXTURES + "/list_restaurants_searchedby_attributes.json");

    return objectMapper.readValue(fixture, new TypeReference<List<Restaurant>>() {
    });
  }

  private List<Restaurant> loadRestaurantsDuringPeakHours() throws IOException {
    String fixture =
        FixtureHelpers.fixture(FIXTURES + "/peak_hours_list_of_restaurants.json");

    return objectMapper.readValue(fixture, new TypeReference<List<Restaurant>>() {
    });
  }
}
