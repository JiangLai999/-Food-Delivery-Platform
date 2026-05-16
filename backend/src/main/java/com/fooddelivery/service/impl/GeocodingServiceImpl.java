package com.fooddelivery.service.impl;

import com.fooddelivery.service.GeocodingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class GeocodingServiceImpl implements GeocodingService {

    private static final String AMAP_GEOCODE_URL = "https://restapi.amap.com/v3/geocode/geo";

    @Value("${amap.key:a4d6ee2613113ed2072b9b771cce3ab1}")
    private String amapKey;

    private static final Pattern LOCATION_PATTERN = Pattern.compile("\"location\":\"([0-9.]+),([0-9.]+)\"");

    @Override
    public BigDecimal[] geocode(String address) {
        if (address == null || address.trim().isEmpty()) {
            return null;
        }

        try {
            String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8.toString());
            String urlString = AMAP_GEOCODE_URL + "?key=" + amapKey + "&address=" + encodedAddress;
            
            log.info("Geocoding URL: {}", urlString);
            
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept-Charset", "UTF-8");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            
            int responseCode = connection.getResponseCode();
            log.info("Response code: {}", responseCode);
            
            if (responseCode != 200) {
                log.warn("Geocoding HTTP error for address: {}, response code: {}", address, responseCode);
                return null;
            }
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            
            String responseBody = response.toString();
            log.info("Geocoding response: {}", responseBody.substring(0, Math.min(responseBody.length(), 200)));

            if (!responseBody.contains("\"status\":\"1\"")) {
                log.warn("Geocoding failed for address: {}, response: {}", address, responseBody);
                return null;
            }

            Matcher matcher = LOCATION_PATTERN.matcher(responseBody);
            if (matcher.find()) {
                String longitude = matcher.group(1);
                String latitude = matcher.group(2);
                log.info("Geocoded address {} to {},{}", address, longitude, latitude);
                return new BigDecimal[]{
                    new BigDecimal(longitude),
                    new BigDecimal(latitude)
                };
            }

            log.warn("No location found in geocoding response for address: {}", address);
            return null;

        } catch (Exception e) {
            log.error("Error geocoding address: {}", address, e);
            return null;
        }
    }
}
