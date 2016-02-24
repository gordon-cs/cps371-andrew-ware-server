/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.appengine.helloworld;

// Client side:
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

// Server side:
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.*;
import java.util.*;

@SuppressWarnings("serial")
public class HelloServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws IOException {
        resp.setHeader("content-type", "application/json; charset=utf-8");
        PrintWriter out = resp.getWriter();

        // parse the query string
        Map < String, String > queryParams = new HashMap < String, String > ();
        String[] queryParamsArr = req.getQueryString().split("[&]");
        for (int i = 0; i < queryParamsArr.length; i++) {
            String[] queryItem = queryParamsArr[i].split("[=]");
            queryParams.put(queryItem[0], queryItem[1]);
        }

        if (queryParams.get("lat") != null && queryParams.get("lon") != null) {
            
            /* check if in cache first
            if (in cache) {
                // out.println(cachedObject);
            } else {
                // add it to the cache
                // out.println(this.createJSONResponse(this.getWeather(queryParams.get("lat"), queryParams.get("lon"))));
            } 
            */
            out.println(this.createJSONResponse(this.getWeather(queryParams.get("lat"), queryParams.get("lon"))));
        } else {
            out.println("{ 'error': 'lat and lon coords not supplied or invalid.' }");
        }

    }

    private JSONObject getJSON(String urlString) {

        /* some code originally authored by Nathan Gray modified by Andrew Ware
         *  https://github.com/gordon-cs/cps371-nathan-gray-server/blob/master/java-docs-samples/appengine/helloworld/src/main/java/com/example/appengine/helloworld/RequestSender.java */
        String inString = "";
        JSONObject inJSON = new JSONObject(); // A JSON object to store the data

        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/JSON");
            conn.connect();

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                (conn.getInputStream())));
            String line = "";
            // Read the response line by line and add it to a string
            while ((line = br.readLine()) != null) {
                inString += line;
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
            inString = "{ 'error': 'MalformedURLException' }";
        } catch (IOException e) {
            e.printStackTrace();
            inString = "{ 'error': 'IOException' }";
        }
        inJSON = new JSONObject(inString); // Convert string to JSON
        return inJSON;

    }

    /* some code originally authored by Nathan Gray modified by Andrew Ware
     *  https://github.com/gordon-cs/cps371-nathan-gray-server/blob/master/java-docs-samples/appengine/helloworld/src/main/java/com/example/appengine/helloworld */
    private JSONObject createJSONResponse(JSONObject weatherJSON) {

        JSONObject currentlyIn = weatherJSON.getJSONObject("currently");
        JSONObject currentlyOut = new JSONObject();
        JSONObject respObject = new JSONObject();
        // Add the desired fields from the original to the new object
        currentlyOut.put("currently", currentlyIn.getString("summary"));
        currentlyOut.put("temperature", currentlyIn.getDouble("temperature"));
        currentlyOut.put("windSpeed", currentlyIn.getDouble("windSpeed"));
        currentlyOut.put("humidity", currentlyIn.getDouble("humidity"));
        currentlyOut.put("icon", currentlyIn.getString("icon"));

        // Get the daily data array from the JSON
        JSONArray dailyIn = weatherJSON.getJSONObject("daily").getJSONArray("data");
        // Create a replacement
        JSONArray dailyOut = new JSONArray();
        // Loop through the items in the array
        for (int i = 0; i < dailyIn.length(); i++) {
            JSONObject dayIn = dailyIn.getJSONObject(i); // Get the day object
            JSONObject dayOut = new JSONObject(); // Create a replacement
            // Add the desired fields to the new day object
            dayOut.put("maxTemp", dayIn.getDouble("temperatureMax"));
            dayOut.put("minTemp", dayIn.getDouble("temperatureMin"));
            dayOut.put("summary", dayIn.getString("summary"));
            dayOut.put("icon", dayIn.getString("icon"));
            // Add that object to the array
            dailyOut.put(i, dayOut);
        }

        // send the newly organized data back
        respObject.put("currently", currentlyOut);
        respObject.put("daily", dailyOut);
        return respObject;

    }

    private JSONObject getZip(String lat, String lon) {

        String urlString = "http://maps.googleapis.com/maps/api/geocode/json?latlng=" + lat + "," + lon + "&sensor=true";
        String inString = "";
        JSONObject inJSON = new JSONObject(); // A JSON object to store the data
        JSONObject zipJSON = this.getJSON(urlString);
        JSONObject respObject = new JSONObject();
        respObject.put("zip", zipJSON.getJSONArray("results").getJSONObject(2).getJSONArray("address_components").getJSONObject(0).getString("long_name"));
        respObject.put("city", zipJSON.getJSONArray("results").getJSONObject(1).getJSONArray("address_components").getJSONObject(0).getString("long_name"));
        respObject.put("state", zipJSON.getJSONArray("results").getJSONObject(1).getJSONArray("address_components").getJSONObject(2).getString("short_name"));

        return respObject;

    }

    private JSONObject getWeather(String lat, String lon) {

        String apiUrl = "https://api.forecast.io/forecast/";
        String apiKey = "14e723fbe931ee119ade496aabcf28ba";
        String urlString = apiUrl + apiKey + "/" + lat + "," + lon;
        String response = new String();
        return this.getJSON(urlString);

    }

}