package edu.usc.csci310.project.services;

import edu.usc.csci310.project.models.Song;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FavoritesService {
    private static final String SUPABASE_URL = "https://cqqwwzmgbeckmdotlwdx.supabase.co";
    private final RestTemplate restTemplate;
    private final String SUPABASE_API_KEY;

    public FavoritesService(RestTemplate restTemplate, @Value("${SUPABASE_API_KEY}") String apiKey) {
        this.restTemplate = restTemplate;
        this.SUPABASE_API_KEY = apiKey;
    }

    // add favorite song to user
    public void addFavoriteSong(int userID, int songID) {
        try {
            String getMaxPositionUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&select=position&order=position.desc.nullslast&limit=1";

            HttpHeaders headers = new HttpHeaders();
            headers.set("apikey", SUPABASE_API_KEY);
            headers.set("Authorization", "Bearer " + SUPABASE_API_KEY);
            headers.set("Content-Type", "application/json");

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<List<Map<String, Object>>> maxPositionResponse = restTemplate.exchange(
                    getMaxPositionUrl,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );

            int nextPosition = 1;

            if (maxPositionResponse.getBody() != null && !maxPositionResponse.getBody().isEmpty() &&
                    maxPositionResponse.getBody().get(0).get("position") != null) {
                int currentMax = Integer.parseInt(maxPositionResponse.getBody().get(0).get("position").toString());
                nextPosition = currentMax + 1;
            }

            //System.out.println("Adding new favorite with position: " + nextPosition);

            String url = SUPABASE_URL + "/rest/v1/favorites";

            Map<String, Object> request = new HashMap<>();
            request.put("user_id", userID);
            request.put("song_id", songID);
            request.put("position", nextPosition);
            HttpHeaders postHeaders = new HttpHeaders();
            postHeaders.set("apikey", SUPABASE_API_KEY);
            postHeaders.set("Authorization", "Bearer " + SUPABASE_API_KEY);
            postHeaders.set("Content-Type", "application/json");
            postHeaders.set("Prefer", "return=minimal");

            HttpEntity<Map<String, Object>> postEntity = new HttpEntity<>(request, postHeaders);

            ResponseEntity<Void> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    postEntity,
                    Void.class
            );
            if (response.getStatusCode() != HttpStatus.CREATED) {
                throw new RuntimeException("Failed to add user's favorite song");
            }
        }
        catch(HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.CONFLICT) {
                throw new RuntimeException("Song is already in user's favorites");
            }
            else {
                throw new RuntimeException("Error adding favorite song: " + e.getMessage());
            }
        }
    }

    // remove fav song from user
    public void removeFavoriteSong(int userID, int songID) {
        try {
            // get the current position of the song being removed
            String getPositionUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&song_id=eq." + songID + "&select=position";

            HttpHeaders headers = new HttpHeaders();
            headers.set("apikey", SUPABASE_API_KEY);
            headers.set("Authorization", "Bearer " + SUPABASE_API_KEY);
            headers.set("Content-Type", "application/json");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<List<Map<String, Object>>> positionResponse = restTemplate.exchange(
                    getPositionUrl,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );

            // get the position value of the song being removed
            int removedPosition = 0;
            if (positionResponse.getBody() != null && !positionResponse.getBody().isEmpty() &&
                    positionResponse.getBody().get(0).get("position") != null) {
                removedPosition = Integer.parseInt(positionResponse.getBody().get(0).get("position").toString());
                //System.out.println("Removing song at position: " + removedPosition);
            }

            //  delete the song from favorites
            String deleteUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&song_id=eq." + songID;

            ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                    deleteUrl,
                    HttpMethod.DELETE,
                    entity,
                    Void.class
            );

            // check successful deletion
            if (deleteResponse.getStatusCode() != HttpStatus.NO_CONTENT) {
                throw new RuntimeException("Failed to remove favorite song");
            }

            // update positions of all songs with greater positions
            if (removedPosition > 0) {
                String getHigherPositionsUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID +
                        "&position=gt." + removedPosition +
                        "&select=song_id,position";

                ResponseEntity<List<Map<String, Object>>> higherPositionsResponse = restTemplate.exchange(
                        getHigherPositionsUrl,
                        HttpMethod.GET,
                        entity,
                        new ParameterizedTypeReference<List<Map<String, Object>>>() {}
                );

                if (higherPositionsResponse.getBody() != null && !higherPositionsResponse.getBody().isEmpty()) {
                    for (Map<String, Object> song : higherPositionsResponse.getBody()) {
                        int songId = Integer.parseInt(song.get("song_id").toString());
                        int currentPosition = Integer.parseInt(song.get("position").toString());
                        int newPosition = currentPosition - 1; // Decrement by 1

                        String deleteHigherUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&song_id=eq." + songId;

                        restTemplate.exchange(
                                deleteHigherUrl,
                                HttpMethod.DELETE,
                                entity,
                                Void.class
                        );

                        //reinsert with the new position
                        String insertUrl = SUPABASE_URL + "/rest/v1/favorites";

                        Map<String, Object> insertData = new HashMap<>();
                        insertData.put("user_id", userID);
                        insertData.put("song_id", songId);
                        insertData.put("position", newPosition);

                        HttpHeaders insertHeaders = new HttpHeaders();
                        insertHeaders.set("apikey", SUPABASE_API_KEY);
                        insertHeaders.set("Authorization", "Bearer " + SUPABASE_API_KEY);
                        insertHeaders.set("Content-Type", "application/json");
                        insertHeaders.set("Prefer", "return=minimal");

                        HttpEntity<Map<String, Object>> insertEntity = new HttpEntity<>(insertData, insertHeaders);

                        restTemplate.exchange(
                                insertUrl,
                                HttpMethod.POST,
                                insertEntity,
                                Void.class
                        );

                        //System.out.println("Updated song " + songId + " position from " + currentPosition + " to " + newPosition);
                    }
                }
            }
        }
        catch (HttpClientErrorException e) {
            throw new RuntimeException("Error removing song from favorites: " + e.getMessage());
        }
    }

    //check if song is a favorite
    public boolean isFavoriteSong(int songID, String username)
    {
        String url = SUPABASE_URL + "/rest/v1/favorites?username=eq." + username + "&song_id=eq." + songID;

        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", SUPABASE_API_KEY);
        headers.set("Authorization", "Bearer " + SUPABASE_API_KEY);
        headers.set("Content-Type", "application/json");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<List<Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<Object>>() {}
            );

            // if list is not empty, then song is a favorite
            List<Object> favorites = response.getBody();
            return favorites != null && !favorites.isEmpty();

        } catch (HttpClientErrorException e) {
            return false;
        }
    }

    // get users favorite songs
    public List<Song> getFavoriteSongs(Integer userID) {
        //String url = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&select=song_id";
        String url = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&select=song_id,position";

        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", SUPABASE_API_KEY);
        headers.set("Authorization", "Bearer " + SUPABASE_API_KEY);
        headers.set("Content-Type", "application/json");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<List<Map<String, Object>>> favoritesResponse = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {
                    }
            );

            if (favoritesResponse.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("Failed to get favorite song list");
            }

            List<Map<String, Object>> favorites = favoritesResponse.getBody();

            if (favorites == null || favorites.isEmpty()) {
                return List.of();
            }


            StringBuilder songIdsQuery = new StringBuilder();
            for (int i = 0; i < favorites.size(); i++) {
                Object songId = favorites.get(i).get("song_id");
                if (i > 0) {
                    songIdsQuery.append(",");
                }
                songIdsQuery.append(songId);
            }

            String songsUrl = SUPABASE_URL + "/rest/v1/songs?id=in.(" + songIdsQuery + ")";

            ResponseEntity<List<Map<String, Object>>> songsResponse = restTemplate.exchange(
                    songsUrl,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {
                    }
            );

            if (songsResponse.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("Failed to get song details");
            }

            // map JSON rows into Song objects
            List<Map<String,Object>> rows = songsResponse.getBody();
            if (rows == null || rows.isEmpty()) {
                return new ArrayList<>();
            }
            List<Song> songs = new ArrayList<>(rows.size());
            for (Map<String,Object> row : rows) {
                Song s = new Song();
                s.setId(Integer.parseInt(row.get("id").toString()));
                s.setTitle(   (String) row.get("title"));
                s.setArtist(  (String) row.get("artist"));
                s.setLyrics(  (String) row.get("lyrics"));
                Object yearVal = row.get("year");
                if (yearVal != null) {
                    s.setYear(Integer.parseInt(yearVal.toString()));
                }
                songs.add(s);
            }
            // After mapping the songs, add the position from favorites
            for (Song song : songs) {
                for (Map<String, Object> favorite : favorites) {
                    if (Integer.parseInt(favorite.get("song_id").toString()) == song.getId()) {
                        if (favorite.get("position") != null) {
                            song.setPosition(Integer.parseInt(favorite.get("position").toString()));
                        }
                        break;
                    }
                }
            }

            return songs;

        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Error getting favorite songs: " + e.getMessage());
        }
    }

    public void reorderFavoriteSong(int userID, int songID, String direction) {
        try {
            String getCurrentPositionUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&song_id=eq." + songID + "&select=position";

            HttpHeaders headers = new HttpHeaders();
            headers.set("apikey", SUPABASE_API_KEY);
            headers.set("Authorization", "Bearer " + SUPABASE_API_KEY);
            headers.set("Content-Type", "application/json");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<List<Map<String, Object>>> currentPositionResponse = restTemplate.exchange(
                    getCurrentPositionUrl,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );

            if (currentPositionResponse.getBody() == null || currentPositionResponse.getBody().isEmpty()) {
                throw new RuntimeException("Song not found in user's favorites");
            }

            int currentPosition = 0;
            if (currentPositionResponse.getBody().get(0).get("position") != null) {
                currentPosition = Integer.parseInt(currentPositionResponse.getBody().get(0).get("position").toString());
            }

            //System.out.println("Current song ID: " + songID + ", Position: " + currentPosition);

            int targetPosition;
            if (direction.equals("up")) {

                String getMaxPositionUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&select=position&order=position.desc.nullslast&limit=1";

                ResponseEntity<List<Map<String, Object>>> maxPositionResponse = restTemplate.exchange(
                        getMaxPositionUrl,
                        HttpMethod.GET,
                        entity,
                        new ParameterizedTypeReference<List<Map<String, Object>>>() {}
                );

                if (maxPositionResponse.getBody() != null && !maxPositionResponse.getBody().isEmpty() &&
                        maxPositionResponse.getBody().get(0).get("position") != null) {
                    int maxPosition = Integer.parseInt(maxPositionResponse.getBody().get(0).get("position").toString());

                    if (currentPosition >= maxPosition) {
                        //System.out.println("Song already at top position: " + currentPosition);
                        return; // already at top, don't change
                    }
                }
                targetPosition = currentPosition + 1;

            }
            else {
                if (currentPosition <= 1) {
                    return;
                }
                targetPosition = currentPosition - 1;
            }

            //System.out.println("Target position: " + targetPosition);

            String checkTargetPositionUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&position=eq." + targetPosition + "&select=song_id,position";

            ResponseEntity<List<Map<String, Object>>> targetPositionResponse = restTemplate.exchange(
                    checkTargetPositionUrl,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );

            if (targetPositionResponse.getBody() != null && !targetPositionResponse.getBody().isEmpty()) {
                Map<String, Object> targetSong = targetPositionResponse.getBody().get(0);
                int targetSongId = Integer.parseInt(targetSong.get("song_id").toString());

                //System.out.println("Song with ID " + targetSongId + " already has position " + targetPosition);

                String deleteTargetUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&song_id=eq." + targetSongId;

                restTemplate.exchange(
                        deleteTargetUrl,
                        HttpMethod.DELETE,
                        entity,
                        Void.class
                );

                String insertTargetUrl = SUPABASE_URL + "/rest/v1/favorites";

                Map<String, Object> targetInsertData = new HashMap<>();
                targetInsertData.put("user_id", userID);
                targetInsertData.put("song_id", targetSongId);
                targetInsertData.put("position", currentPosition); // Give it our current position

                HttpHeaders insertHeaders = new HttpHeaders();
                insertHeaders.set("apikey", SUPABASE_API_KEY);
                insertHeaders.set("Authorization", "Bearer " + SUPABASE_API_KEY);
                insertHeaders.set("Content-Type", "application/json");
                insertHeaders.set("Prefer", "return=minimal");

                HttpEntity<Map<String, Object>> insertTargetEntity = new HttpEntity<>(targetInsertData, insertHeaders);

                restTemplate.exchange(
                        insertTargetUrl,
                        HttpMethod.POST,
                        insertTargetEntity,
                        Void.class
                );

                //System.out.println("Updated song " + targetSongId + " position to " + currentPosition);
            }

            String deleteUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&song_id=eq." + songID;

            restTemplate.exchange(
                    deleteUrl,
                    HttpMethod.DELETE,
                    entity,
                    Void.class
            );

            String insertUrl = SUPABASE_URL + "/rest/v1/favorites";

            Map<String, Object> insertData = new HashMap<>();
            insertData.put("user_id", userID);
            insertData.put("song_id", songID);
            insertData.put("position", targetPosition);

            HttpHeaders insertHeaders = new HttpHeaders();
            insertHeaders.set("apikey", SUPABASE_API_KEY);
            insertHeaders.set("Authorization", "Bearer " + SUPABASE_API_KEY);
            insertHeaders.set("Content-Type", "application/json");
            insertHeaders.set("Prefer", "return=minimal");

            HttpEntity<Map<String, Object>> insertEntity = new HttpEntity<>(insertData, insertHeaders);

            restTemplate.exchange(
                    insertUrl,
                    HttpMethod.POST,
                    insertEntity,
                    Void.class
            );

            //System.out.println("Updated song " + songID + " position to " + targetPosition);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error reordering song: " + e.getMessage());
        }
    }
}