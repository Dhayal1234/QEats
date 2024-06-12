
package com.crio.qeats.repositoryservices;

import com.crio.qeats.dto.Restaurant;
import com.crio.qeats.utils.FixtureHelpers;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class RestaurantRepositoryServiceDummyImpl implements RestaurantRepositoryService {
  private static final String FIXTURES = "fixtures/exchanges";
  private ObjectMapper objectMapper = new ObjectMapper();

  private List<Restaurant> loadRestaurantsDuringNormalHours() throws IOException {
    String fixture =
        FixtureHelpers.fixture(FIXTURES + "/normal_hours_list_of_restaurants.json");

    return objectMapper.readValue(fixture, new TypeReference<List<Restaurant>>() {
    });
  }

  // TODO: CRIO_TASK_MODULE_RESTAURANTSAPI - Use this dummy implementation.
  // This function returns a list of restaurants in any lat/long of your choice randomly.
  // It will load some dummy restaurants and change their latitude/longitude near
  // the lat/long you pass. In the next module, once you start using mongodb, you will not use
  // it anymore.
  @Override
  public List<Restaurant> findAllRestaurantsCloseBy(Double latitude, Double longitude,
      LocalTime currentTime, Double servingRadiusInKms) {
    List<Restaurant> restaurantList = new ArrayList<>();
    try {
      restaurantList = loadRestaurantsDuringNormalHours();
    } catch (IOException e) {
      e.printStackTrace();
    }
    for (Restaurant restaurant : restaurantList) {
      restaurant.setLatitude(latitude + ThreadLocalRandom.current().nextDouble(0.000001, 0.2));
      restaurant.setLongitude(longitude + ThreadLocalRandom.current().nextDouble(0.000001, 0.2));
    }

     // Filter restaurants based on the serving radius and open status
     List<Restaurant> filteredRestaurants = new ArrayList<>();
     for (Restaurant restaurant : restaurantList) {
       if (isWithinRadius(restaurant, latitude, longitude, servingRadiusInKms) &&
           isRestaurantOpen(restaurant, currentTime)) {
         filteredRestaurants.add(restaurant);
       }
     }

    return restaurantList;
  }

  private boolean isRestaurantOpen(Restaurant restaurant, LocalTime currentTime) {
    return !currentTime.isBefore(LocalTime.parse(restaurant.getOpensAt())) && !currentTime.isAfter(LocalTime.parse(restaurant.getClosesAt()));
  }
  

  private boolean isWithinRadius(Restaurant restaurant, Double latitude, Double longitude, Double radiusInKms) {
    // Calculate the distance between the restaurant and the given coordinates using Haversine formula
    double distance = haversineDistance(latitude, longitude, restaurant.getLatitude(), restaurant.getLongitude());
    return distance <= radiusInKms;
  }

  private double haversineDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
    final int R = 6371; // Radius of the earth in km

    double latDistance = Math.toRadians(lat2 - lat1);
    double lonDistance = Math.toRadians(lon2 - lon1);
    double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
        + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
        * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    double distance = R * c; // convert to km

    return distance;


  }
}

