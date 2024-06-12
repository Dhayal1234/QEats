
/*
 *
 *  * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.services;

import com.crio.qeats.dto.Restaurant;
import com.crio.qeats.exchanges.GetRestaurantsRequest;
import com.crio.qeats.exchanges.GetRestaurantsResponse;
import com.crio.qeats.repositoryservices.RestaurantRepositoryService;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class RestaurantServiceImpl implements RestaurantService {

  private final Double peakHoursServingRadiusInKms = 3.0;
  private final Double normalHoursServingRadiusInKms = 5.0;
  @Autowired
  private RestaurantRepositoryService restaurantRepositoryService;


  // TODO: CRIO_TASK_MODULE_RESTAURANTSAPI - Implement findAllRestaurantsCloseby.
  // Check RestaurantService.java file for the interface contract.
  @Override
  public GetRestaurantsResponse findAllRestaurantsCloseBy(
      GetRestaurantsRequest getRestaurantsRequest, LocalTime currentTime) {
        

          // Determine the service radius based on whether it is peak hours or not
          double serviceRadius = isPeakHour(currentTime) ? peakHoursServingRadiusInKms : normalHoursServingRadiusInKms;
      
          // Fetch all restaurants within the service radius
          List<Restaurant> allRestaurants = restaurantRepositoryService.findAllRestaurantsCloseBy(getRestaurantsRequest.getLatitude(), getRestaurantsRequest.getLongitude(), currentTime, serviceRadius);
      
       
      
          return new GetRestaurantsResponse(allRestaurants);

     
  }

  private boolean isPeakHour(LocalTime currentTime) {
    return (currentTime.isAfter(LocalTime.of(7, 59)) && currentTime.isBefore(LocalTime.of(10, 1))) ||
           (currentTime.isAfter(LocalTime.of(12, 59)) && currentTime.isBefore(LocalTime.of(14, 1))) ||
           (currentTime.isAfter(LocalTime.of(18, 59)) && currentTime.isBefore(LocalTime.of(21, 1)));
  }

  private boolean isOpen(Restaurant restaurant, LocalTime currentTime) {

    if (restaurant.getOpensAt() == null || restaurant.getClosesAt() == null) {
      // Handle case where opening or closing time is not available
      return false;
  }
    LocalTime opensAt = LocalTime.parse(restaurant.getOpensAt());
    LocalTime closesAt = LocalTime.parse(restaurant.getClosesAt());
    return currentTime.isAfter(opensAt) && currentTime.isBefore(closesAt);
  }


}

