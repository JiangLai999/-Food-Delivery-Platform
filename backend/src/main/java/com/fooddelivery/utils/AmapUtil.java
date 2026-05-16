package com.fooddelivery.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 高德地图工具类
 */
@Slf4j
@Component
public class AmapUtil {

    @Value("${amap.key}")
    private String amapKey;

    @Value("${amap.service-url}")
    private String serviceUrl;

    /**
     * 经纬度坐标
     */
    public static class LatLng {
        private Double longitude;
        private Double latitude;

        public LatLng() {}

        public LatLng(Double longitude, Double latitude) {
            this.longitude = longitude;
            this.latitude = latitude;
        }

        public Double getLongitude() {
            return longitude;
        }

        public void setLongitude(Double longitude) {
            this.longitude = longitude;
        }

        public Double getLatitude() {
            return latitude;
        }

        public void setLatitude(Double latitude) {
            this.latitude = latitude;
        }

        @Override
        public String toString() {
            return longitude + "," + latitude;
        }
    }

    /**
     * 获取路径信息（用于配送任务）
     * @param originLng 起点经度
     * @param originLat 起点纬度
     * @param destLng 终点经度
     * @param destLat 终点纬度
     * @return 包含路径信息的Map（包含steps和distance）
     */
    public java.util.Map<String, Object> getRoute(java.math.BigDecimal originLng, java.math.BigDecimal originLat,
                                                    java.math.BigDecimal destLng, java.math.BigDecimal destLat) {
        java.util.Map<String, Object> result = new java.util.HashMap<>();

        try {
            LatLng origin = new LatLng(originLng.doubleValue(), originLat.doubleValue());
            LatLng destination = new LatLng(destLng.doubleValue(), destLat.doubleValue());

            String url = String.format(
                "%s/direction/riding?origin=%s&destination=%s&key=%s",
                serviceUrl,
                origin.toString(),
                destination.toString(),
                amapKey
            );

            String response = httpGet(url);
            JSONObject jsonObject = JSON.parseObject(response);

            if ("1".equals(jsonObject.getString("status"))) {
                JSONObject route = jsonObject.getJSONObject("route");
                if (route != null) {
                    JSONArray paths = route.getJSONArray("paths");
                    if (paths != null && paths.size() > 0) {
                        JSONObject firstPath = paths.getJSONObject(0);
                        JSONArray steps = firstPath.getJSONArray("steps");

                        // 添加步骤信息
                        result.put("steps", steps);

                        // 计算总距离
                        Integer distance = firstPath.getInteger("distance");
                        if (distance == null) {
                            distance = 0;
                        }
                        result.put("distance", distance);
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取路径信息失败", e);
            // 返回默认值
            result.put("steps", new JSONArray());
            result.put("distance", (int) calculateDistance(
                new LatLng(originLng.doubleValue(), originLat.doubleValue()),
                new LatLng(destLng.doubleValue(), destLat.doubleValue())
            ));
        }

        return result;
    }

    /**
     * 路径规划
     * @param origin 起点坐标
     * @param destination 终点坐标
     * @return 路径坐标点列表
     */
    public List<LatLng> planRoute(LatLng origin, LatLng destination) {
        List<LatLng> path = new ArrayList<>();

        try {
            String url = String.format(
                "%s/direction/riding?origin=%s&destination=%s&key=%s",
                serviceUrl,
                origin.toString(),
                destination.toString(),
                amapKey
            );

            String response = httpGet(url);
            JSONObject jsonObject = JSON.parseObject(response);

            if ("1".equals(jsonObject.getString("status"))) {
                JSONObject route = jsonObject.getJSONObject("route");
                if (route != null) {
                    JSONArray paths = route.getJSONArray("paths");
                    if (paths != null && paths.size() > 0) {
                        JSONObject firstPath = paths.getJSONObject(0);
                        JSONArray steps = firstPath.getJSONArray("steps");

                        for (int i = 0; i < steps.size(); i++) {
                            JSONObject step = steps.getJSONObject(i);
                            String polyline = step.getString("polyline");

                            // 解析polyline坐标
                            String[] points = polyline.split(";");
                            for (String point : points) {
                                String[] coords = point.split(",");
                                if (coords.length == 2) {
                                    path.add(new LatLng(
                                        Double.parseDouble(coords[0]),
                                        Double.parseDouble(coords[1])
                                    ));
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("高德地图路径规划失败", e);
        }

        // 如果API调用失败，返回简单的直线路径
        if (path.isEmpty()) {
            path.add(origin);
            path.add(destination);
        }

        return path;
    }

    /**
     * 计算两点之间的距离（米）
     */
    public double calculateDistance(LatLng point1, LatLng point2) {
        double lat1 = Math.toRadians(point1.getLatitude());
        double lon1 = Math.toRadians(point1.getLongitude());
        double lat2 = Math.toRadians(point2.getLatitude());
        double lon2 = Math.toRadians(point2.getLongitude());

        double earthRadius = 6371000; // 地球半径（米）

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(lat1) * Math.cos(lat2) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return earthRadius * c;
    }

    /**
     * HTTP GET请求
     */
    private String httpGet(String url) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);

        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            return EntityUtils.toString(response.getEntity(), "UTF-8");
        } finally {
            httpClient.close();
        }
    }
}
