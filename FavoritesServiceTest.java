package edu.usc.csci310.project.services;

import edu.usc.csci310.project.models.Song;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import edu.usc.csci310.project.services.FavoritesService;
import edu.usc.csci310.project.controllers.FavoritesController;


@ExtendWith(MockitoExtension.class)
public class FavoritesServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private FavoritesService favoritesService;
    private final String SUPABASE_URL = "https://cqqwwzmgbeckmdotlwdx.supabase.co";
    @Value("${SUPABASE_API_KEY}")
    private String SUPABASE_API_KEY;

    @BeforeEach
    void setUp() {
        favoritesService = new FavoritesService(restTemplate, SUPABASE_API_KEY);
    }

    @Test
    void testAddFavoriteSong_Success() {
        int songId = 1;
        int userId = 1;

        String getMaxPositionUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userId + "&select=position&order=position.desc.nullslast&limit=1";

        List<Map<String, Object>> maxPositionList = new ArrayList<>();
        Map<String, Object> maxPositionMap = new HashMap<>();
        maxPositionMap.put("position", 0);
        maxPositionList.add(maxPositionMap);
        ResponseEntity<List<Map<String, Object>>> maxPositionResponse =
                new ResponseEntity<>(maxPositionList, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(getMaxPositionUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(maxPositionResponse);

        String expectedUrl = SUPABASE_URL + "/rest/v1/favorites";

        // create expected headers to connect to db
        HttpHeaders expectedHeaders = new HttpHeaders();
        expectedHeaders.set("apikey", SUPABASE_API_KEY);
        expectedHeaders.set("Authorization", "Bearer " + SUPABASE_API_KEY);
        expectedHeaders.set("Content-Type", "application/json");
        expectedHeaders.set("Prefer", "return=minimal");

        Map<String, Object> expectedRequestBody = new HashMap<>();
        expectedRequestBody.put("user_id", userId);
        expectedRequestBody.put("song_id", songId);
        expectedRequestBody.put("position", 1);


        HttpEntity<Map<String, Object>> expectedEntity = new HttpEntity<>(expectedRequestBody, expectedHeaders);

        ResponseEntity<Void> responseEntity = new ResponseEntity<>(HttpStatus.CREATED);

        when(restTemplate.exchange(
                expectedUrl,
                HttpMethod.POST,
                expectedEntity,
                Void.class
        )).thenReturn(responseEntity);

        favoritesService.addFavoriteSong(userId, songId);

        verify(restTemplate).exchange(
                eq(getMaxPositionUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        );

        verify(restTemplate).exchange(
                expectedUrl,
                HttpMethod.POST,
                expectedEntity,
                Void.class
        );
    }

    @Test
    void testAddFavoriteSong_Failure() {

        int songId = 1;
        int userId = 1;

        String getMaxPositionUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userId + "&select=position&order=position.desc.nullslast&limit=1";

        List<Map<String, Object>> maxPositionList = new ArrayList<>();
        Map<String, Object> maxPositionMap = new HashMap<>();
        maxPositionMap.put("position", 0);
        maxPositionList.add(maxPositionMap);
        ResponseEntity<List<Map<String, Object>>> maxPositionResponse =
                new ResponseEntity<>(maxPositionList, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(getMaxPositionUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(maxPositionResponse);

        String expectedUrl = SUPABASE_URL + "/rest/v1/favorites";

        HttpHeaders expectedHeaders = new HttpHeaders();
        expectedHeaders.set("apikey", SUPABASE_API_KEY);
        expectedHeaders.set("Authorization", "Bearer " + SUPABASE_API_KEY);
        expectedHeaders.set("Content-Type", "application/json");
        expectedHeaders.set("Prefer", "return=minimal");

        Map<String, Object> expectedRequestBody = new HashMap<>();
        expectedRequestBody.put("user_id", userId);
        expectedRequestBody.put("song_id", songId);
        expectedRequestBody.put("position", 1);

        HttpEntity<Map<String, Object>> expectedEntity = new HttpEntity<>(expectedRequestBody, expectedHeaders);

        ResponseEntity<Void> responseEntity = new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        when(restTemplate.exchange(
                expectedUrl,
                HttpMethod.POST,
                expectedEntity,
                Void.class
        )).thenReturn(responseEntity);

        Exception exception = assertThrows(RuntimeException.class, () ->
                favoritesService.addFavoriteSong(userId, songId));

        assertTrue(exception.getMessage().contains("Failed to add user's favorite song"));
    }

    @Test
    void testAddFavoriteSong_ConflictException() {
        int songId = 1;
        int userId = 1;

        String getMaxPositionUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userId + "&select=position&order=position.desc.nullslast&limit=1";

        List<Map<String, Object>> maxPositionList = new ArrayList<>();
        Map<String, Object> maxPositionMap = new HashMap<>();
        maxPositionMap.put("position", 0);
        maxPositionList.add(maxPositionMap);
        ResponseEntity<List<Map<String, Object>>> maxPositionResponse =
                new ResponseEntity<>(maxPositionList, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(getMaxPositionUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(maxPositionResponse);

        String expectedUrl = SUPABASE_URL + "/rest/v1/favorites";

        // create expected headers to connect to db
        HttpHeaders expectedHeaders = new HttpHeaders();
        expectedHeaders.set("apikey", SUPABASE_API_KEY);
        expectedHeaders.set("Authorization", "Bearer " + SUPABASE_API_KEY);
        expectedHeaders.set("Content-Type", "application/json");
        expectedHeaders.set("Prefer", "return=minimal");

        Map<String, Object> expectedRequestBody = new HashMap<>();
        expectedRequestBody.put("user_id", userId);
        expectedRequestBody.put("song_id", songId);
        expectedRequestBody.put("position", 1);

        HttpEntity<Map<String, Object>> expectedEntity = new HttpEntity<>(expectedRequestBody, expectedHeaders);

        //exception mocked
        HttpClientErrorException conflictException = HttpClientErrorException.create(
                HttpStatus.CONFLICT,
                "Conflict",
                HttpHeaders.EMPTY,
                null,
                null
        );

        when(restTemplate.exchange(
                expectedUrl,
                HttpMethod.POST,
                expectedEntity,
                Void.class
        )).thenThrow(conflictException);

        Exception exception = assertThrows(RuntimeException.class, () ->
                favoritesService.addFavoriteSong(userId, songId));

        assertTrue(exception.getMessage().contains("Song is already in user's favorites"));

        verify(restTemplate).exchange(
                eq(getMaxPositionUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        );

        verify(restTemplate).exchange(
                expectedUrl,
                HttpMethod.POST,
                expectedEntity,
                Void.class
        );
    }
    @Test
    void testAddFavoriteSong_OtherException() {
        int songId = 1;
        int userId = 1;
        String getMaxPositionUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userId + "&select=position&order=position.desc.nullslast&limit=1";

        List<Map<String, Object>> maxPositionList = new ArrayList<>();
        Map<String, Object> maxPositionMap = new HashMap<>();
        maxPositionMap.put("position", 0);
        maxPositionList.add(maxPositionMap);
        ResponseEntity<List<Map<String, Object>>> maxPositionResponse =
                new ResponseEntity<>(maxPositionList, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(getMaxPositionUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(maxPositionResponse);
        String expectedUrl = SUPABASE_URL + "/rest/v1/favorites";

        HttpHeaders expectedHeaders = new HttpHeaders();
        expectedHeaders.set("apikey", SUPABASE_API_KEY);
        expectedHeaders.set("Authorization", "Bearer " + SUPABASE_API_KEY);
        expectedHeaders.set("Content-Type", "application/json");
        expectedHeaders.set("Prefer", "return=minimal");

        Map<String, Object> expectedRequestBody = new HashMap<>();
        expectedRequestBody.put("user_id", userId);
        expectedRequestBody.put("song_id", songId);
        expectedRequestBody.put("position", 1);

        HttpEntity<Map<String, Object>> expectedEntity = new HttpEntity<>(expectedRequestBody, expectedHeaders);

        HttpClientErrorException badRequestException = HttpClientErrorException.create(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                HttpHeaders.EMPTY,
                null,
                null
        );

        when(restTemplate.exchange(
                expectedUrl,
                HttpMethod.POST,
                expectedEntity,
                Void.class
        )).thenThrow(badRequestException);

        Exception exception = assertThrows(RuntimeException.class, () ->
                favoritesService.addFavoriteSong(userId, songId));

        assertTrue(exception.getMessage().contains("Error adding favorite song:"));

        verify(restTemplate).exchange(
                eq(getMaxPositionUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        );

        verify(restTemplate).exchange(
                expectedUrl,
                HttpMethod.POST,
                expectedEntity,
                Void.class
        );
    }

    @Test
    void testAddFavoriteSong_NullPositionInMaxPositionResponse() {
        int songId = 1;
        int userId = 1;

        // Test when position is null in the response
        String getMaxPositionUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userId + "&select=position&order=position.desc.nullslast&limit=1";

        List<Map<String, Object>> maxPositionList = new ArrayList<>();
        Map<String, Object> maxPositionMap = new HashMap<>();
        maxPositionMap.put("position", null); // Position is null here
        maxPositionList.add(maxPositionMap);
        ResponseEntity<List<Map<String, Object>>> maxPositionResponse =
                new ResponseEntity<>(maxPositionList, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(getMaxPositionUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(maxPositionResponse);

        String expectedUrl = SUPABASE_URL + "/rest/v1/favorites";

        // Expected position should be 1 since the position was null
        Map<String, Object> expectedRequestBody = new HashMap<>();
        expectedRequestBody.put("user_id", userId);
        expectedRequestBody.put("song_id", songId);
        expectedRequestBody.put("position", 1); // Default position = 1

        HttpHeaders expectedHeaders = new HttpHeaders();
        expectedHeaders.set("apikey", SUPABASE_API_KEY);
        expectedHeaders.set("Authorization", "Bearer " + SUPABASE_API_KEY);
        expectedHeaders.set("Content-Type", "application/json");
        expectedHeaders.set("Prefer", "return=minimal");

        HttpEntity<Map<String, Object>> expectedEntity = new HttpEntity<>(expectedRequestBody, expectedHeaders);

        ResponseEntity<Void> responseEntity = new ResponseEntity<>(HttpStatus.CREATED);

        when(restTemplate.exchange(
                expectedUrl,
                HttpMethod.POST,
                expectedEntity,
                Void.class
        )).thenReturn(responseEntity);

        favoritesService.addFavoriteSong(userId, songId);

        verify(restTemplate).exchange(
                eq(getMaxPositionUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        );

        verify(restTemplate).exchange(
                expectedUrl,
                HttpMethod.POST,
                expectedEntity,
                Void.class
        );
    }

    @Test
    void testAddFavoriteSong_NullMaxPositionResponseBody() {
        int songId = 1;
        int userId = 1;

        // Test when maxPositionResponse.getBody() is null
        String getMaxPositionUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userId + "&select=position&order=position.desc.nullslast&limit=1";

        // Create a response with a null body
        ResponseEntity<List<Map<String, Object>>> maxPositionResponse =
                new ResponseEntity<>(null, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(getMaxPositionUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(maxPositionResponse);

        String expectedUrl = SUPABASE_URL + "/rest/v1/favorites";

        // Expected position should be 1 since maxPositionResponse.getBody() is null
        Map<String, Object> expectedRequestBody = new HashMap<>();
        expectedRequestBody.put("user_id", userId);
        expectedRequestBody.put("song_id", songId);
        expectedRequestBody.put("position", 1); // Default position = 1

        HttpHeaders expectedHeaders = new HttpHeaders();
        expectedHeaders.set("apikey", SUPABASE_API_KEY);
        expectedHeaders.set("Authorization", "Bearer " + SUPABASE_API_KEY);
        expectedHeaders.set("Content-Type", "application/json");
        expectedHeaders.set("Prefer", "return=minimal");

        HttpEntity<Map<String, Object>> expectedEntity = new HttpEntity<>(expectedRequestBody, expectedHeaders);

        ResponseEntity<Void> responseEntity = new ResponseEntity<>(HttpStatus.CREATED);

        when(restTemplate.exchange(
                expectedUrl,
                HttpMethod.POST,
                expectedEntity,
                Void.class
        )).thenReturn(responseEntity);

        favoritesService.addFavoriteSong(userId, songId);

        verify(restTemplate).exchange(
                eq(getMaxPositionUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        );

        verify(restTemplate).exchange(
                expectedUrl,
                HttpMethod.POST,
                expectedEntity,
                Void.class
        );
    }

    @Test
    void testAddFavoriteSong_EmptyMaxPositionResponseBody() {
        int songId = 1;
        int userId = 1;

        // Test when maxPositionResponse.getBody() is empty
        String getMaxPositionUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userId + "&select=position&order=position.desc.nullslast&limit=1";

        // Create an empty list response
        List<Map<String, Object>> emptyList = new ArrayList<>();
        ResponseEntity<List<Map<String, Object>>> maxPositionResponse =
                new ResponseEntity<>(emptyList, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(getMaxPositionUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(maxPositionResponse);

        String expectedUrl = SUPABASE_URL + "/rest/v1/favorites";

        // Expected position should be 1 since maxPositionResponse.getBody() is empty
        Map<String, Object> expectedRequestBody = new HashMap<>();
        expectedRequestBody.put("user_id", userId);
        expectedRequestBody.put("song_id", songId);
        expectedRequestBody.put("position", 1); // Default position = 1

        HttpHeaders expectedHeaders = new HttpHeaders();
        expectedHeaders.set("apikey", SUPABASE_API_KEY);
        expectedHeaders.set("Authorization", "Bearer " + SUPABASE_API_KEY);
        expectedHeaders.set("Content-Type", "application/json");
        expectedHeaders.set("Prefer", "return=minimal");

        HttpEntity<Map<String, Object>> expectedEntity = new HttpEntity<>(expectedRequestBody, expectedHeaders);

        ResponseEntity<Void> responseEntity = new ResponseEntity<>(HttpStatus.CREATED);

        when(restTemplate.exchange(
                expectedUrl,
                HttpMethod.POST,
                expectedEntity,
                Void.class
        )).thenReturn(responseEntity);

        favoritesService.addFavoriteSong(userId, songId);

        verify(restTemplate).exchange(
                eq(getMaxPositionUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        );

        verify(restTemplate).exchange(
                expectedUrl,
                HttpMethod.POST,
                expectedEntity,
                Void.class
        );
    }

    @Test
    void testRemoveFavoriteSong_Success() {
        int songID = 180;
        int userID = 205;
        int position = 3;

        String getPositionUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&song_id=eq." + songID + "&select=position";

        List<Map<String, Object>> positionList = new ArrayList<>();
        Map<String, Object> positionMap = new HashMap<>();
        positionMap.put("position", position);
        positionList.add(positionMap);
        ResponseEntity<List<Map<String, Object>>> positionResponse =
                new ResponseEntity<>(positionList, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(getPositionUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(positionResponse);

        // Mock API call to get higher position songs
        String getHigherPositionsUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID +
                "&position=gt." + position +
                "&select=song_id,position";

        List<Map<String, Object>> higherPositionsList = new ArrayList<>();
        Map<String, Object> higherPos1 = new HashMap<>();
        higherPos1.put("song_id", 190);
        higherPos1.put("position", 4);
        higherPositionsList.add(higherPos1);

        ResponseEntity<List<Map<String, Object>>> higherPositionsResponse =
                new ResponseEntity<>(higherPositionsList, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(getHigherPositionsUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(higherPositionsResponse);

        // Mock delete and insert operations
        String deleteUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&song_id=eq." + songID;
        String deleteHigherUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&song_id=eq.190";
        String insertUrl = SUPABASE_URL + "/rest/v1/favorites";

        when(restTemplate.exchange(
                eq(deleteUrl),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(Void.class)
        )).thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));

        when(restTemplate.exchange(
                eq(deleteHigherUrl),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(Void.class)
        )).thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));

        when(restTemplate.exchange(
                eq(insertUrl),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Void.class)
        )).thenReturn(new ResponseEntity<>(HttpStatus.CREATED));

        favoritesService.removeFavoriteSong(userID, songID);

        // Verify all API calls
        verify(restTemplate).exchange(
                eq(getPositionUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        );

        verify(restTemplate).exchange(
                eq(getHigherPositionsUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        );

        verify(restTemplate).exchange(
                eq(deleteUrl),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(Void.class)
        );

        verify(restTemplate).exchange(
                eq(deleteHigherUrl),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(Void.class)
        );

        verify(restTemplate).exchange(
                eq(insertUrl),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Void.class)
        );
    }

    @Test
    void testRemoveFavoriteSong_FailedRequest() {
        int songID = 180;
        int userID = 205;
        int position = 3;

        // Mock API call to get current position
        String getPositionUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&song_id=eq." + songID + "&select=position";

        List<Map<String, Object>> positionList = new ArrayList<>();
        Map<String, Object> positionMap = new HashMap<>();
        positionMap.put("position", position);
        positionList.add(positionMap);
        ResponseEntity<List<Map<String, Object>>> positionResponse =
                new ResponseEntity<>(positionList, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(getPositionUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(positionResponse);

        String deleteUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&song_id=eq." + songID;

        when(restTemplate.exchange(
                eq(deleteUrl),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(Void.class)
        )).thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        Exception exception = assertThrows(RuntimeException.class, () ->
                favoritesService.removeFavoriteSong(userID, songID));

        assertFalse(exception.getMessage().contains("Failed to remove favorite song from user's favorites"));

        verify(restTemplate).exchange(
                eq(getPositionUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        );

        verify(restTemplate).exchange(
                eq(deleteUrl),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(Void.class)
        );
    }

    @Test
    void testRemoveFavoriteSong_Exception() {
        int songID = 180;
        int userID = 205;
        int position = 3;

        // Mock API call to get current position
        String getPositionUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&song_id=eq." + songID + "&select=position";

        List<Map<String, Object>> positionList = new ArrayList<>();
        Map<String, Object> positionMap = new HashMap<>();
        positionMap.put("position", position);
        positionList.add(positionMap);
        ResponseEntity<List<Map<String, Object>>> positionResponse =
                new ResponseEntity<>(positionList, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(getPositionUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(positionResponse);

        // Mock delete call with exception
        String deleteUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&song_id=eq." + songID;

        HttpClientErrorException notFoundException = HttpClientErrorException.create(
                HttpStatus.NOT_FOUND,
                "Not Found",
                HttpHeaders.EMPTY,
                null,
                null
        );

        when(restTemplate.exchange(
                eq(deleteUrl),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(Void.class)
        )).thenThrow(notFoundException);

        Exception exception = assertThrows(RuntimeException.class, () ->
                favoritesService.removeFavoriteSong(userID, songID));

        assertTrue(exception.getMessage().contains("Error removing song from favorites:"));

        verify(restTemplate).exchange(
                eq(getPositionUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        );

        verify(restTemplate).exchange(
                eq(deleteUrl),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(Void.class)
        );
    }

    @Test
    void testRemoveFavoriteSong_NullPositionResponseBody() {
        int songID = 180;
        int userID = 205;

        // Mock API call to get current position with NULL body response
        String getPositionUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&song_id=eq." + songID + "&select=position";

        // Return NULL body
        ResponseEntity<List<Map<String, Object>>> nullPositionResponse =
                new ResponseEntity<>(null, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(getPositionUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(nullPositionResponse);

        // Mock delete operation - should still proceed with deletion
        String deleteUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&song_id=eq." + songID;

        when(restTemplate.exchange(
                eq(deleteUrl),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(Void.class)
        )).thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));

        favoritesService.removeFavoriteSong(userID, songID);

        // Verify all API calls
        verify(restTemplate).exchange(
                eq(getPositionUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        );

        // Verify delete operation was called
        verify(restTemplate).exchange(
                eq(deleteUrl),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(Void.class)
        );

        // Verify that no higher positions API call was made
        verify(restTemplate, never()).exchange(
                contains("position=gt."),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        );
    }

    @Test
    void testRemoveFavoriteSong_NullPosition() {
        int songID = 180;
        int userID = 205;

        // Mock API call to get current position where position is null
        String getPositionUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&song_id=eq." + songID + "&select=position";

        List<Map<String, Object>> positionList = new ArrayList<>();
        Map<String, Object> positionMap = new HashMap<>();
        positionMap.put("position", null); // Position is null
        positionList.add(positionMap);
        ResponseEntity<List<Map<String, Object>>> positionResponse =
                new ResponseEntity<>(positionList, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(getPositionUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(positionResponse);

        // Since removedPosition will be 0, we shouldn't get higherPositions calls
        String deleteUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&song_id=eq." + songID;

        when(restTemplate.exchange(
                eq(deleteUrl),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(Void.class)
        )).thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));

        favoritesService.removeFavoriteSong(userID, songID);

        // Verify calls were made correctly
        verify(restTemplate).exchange(
                eq(getPositionUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        );

        verify(restTemplate).exchange(
                eq(deleteUrl),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(Void.class)
        );

        // Verify that higherPositionsUrl was NOT called (removedPosition should be 0)
        verify(restTemplate, never()).exchange(
                contains("position=gt.0"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        );
    }

    @Test
    void testRemoveFavoriteSong_EmptyHigherPositions() {
        int songID = 180;
        int userID = 205;
        int position = 3;

        // Mock API call to get current position
        String getPositionUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&song_id=eq." + songID + "&select=position";

        List<Map<String, Object>> positionList = new ArrayList<>();
        Map<String, Object> positionMap = new HashMap<>();
        positionMap.put("position", position);
        positionList.add(positionMap);
        ResponseEntity<List<Map<String, Object>>> positionResponse =
                new ResponseEntity<>(positionList, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(getPositionUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(positionResponse);

        // Mock API call to get higher position songs - return empty list
        String getHigherPositionsUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID +
                "&position=gt." + position +
                "&select=song_id,position";

        List<Map<String, Object>> emptyList = new ArrayList<>(); // Empty list
        ResponseEntity<List<Map<String, Object>>> emptyResponse =
                new ResponseEntity<>(emptyList, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(getHigherPositionsUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(emptyResponse);

        // Mock delete operation
        String deleteUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&song_id=eq." + songID;

        when(restTemplate.exchange(
                eq(deleteUrl),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(Void.class)
        )).thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));

        favoritesService.removeFavoriteSong(userID, songID);

        // Verify all API calls
        verify(restTemplate).exchange(
                eq(getPositionUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        );

        verify(restTemplate).exchange(
                eq(getHigherPositionsUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        );

        verify(restTemplate).exchange(
                eq(deleteUrl),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(Void.class)
        );

        // Verify that no OTHER songs were deleted or inserted (besides the main one we're removing)
        // Instead of never(), we need to verify exactly how many times delete was called
        verify(restTemplate, times(1)).exchange(
                contains("user_id=eq." + userID + "&song_id=eq."),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(Void.class)
        );

        // Also verify that no POST requests were made (which would be used for repositioning)
        verify(restTemplate, never()).exchange(
                eq(SUPABASE_URL + "/rest/v1/favorites"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Void.class)
        );
    }

    @Test
    void testRemoveFavoriteSong_EmptyPositionList() {
        int songID = 180;
        int userID = 205;

        // Mock API call to get current position with empty list response
        String getPositionUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&song_id=eq." + songID + "&select=position";

        List<Map<String, Object>> emptyList = new ArrayList<>();
        ResponseEntity<List<Map<String, Object>>> emptyResponse =
                new ResponseEntity<>(emptyList, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(getPositionUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(emptyResponse);

        // Mock delete operation - should still proceed with deletion
        String deleteUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&song_id=eq." + songID;

        when(restTemplate.exchange(
                eq(deleteUrl),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(Void.class)
        )).thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));

        favoritesService.removeFavoriteSong(userID, songID);

        // Verify all API calls
        verify(restTemplate).exchange(
                eq(getPositionUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        );

        // Verify delete operation was called
        verify(restTemplate).exchange(
                eq(deleteUrl),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(Void.class)
        );

        // Verify that no higher positions API call was made
        verify(restTemplate, never()).exchange(
                contains("position=gt."),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        );
    }

    @Test
    void testRemoveFavoriteSong_NullHigherPositionsResponse() {
        int songID = 180;
        int userID = 205;
        int position = 3;

        // Mock API call to get current position
        String getPositionUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&song_id=eq." + songID + "&select=position";

        List<Map<String, Object>> positionList = new ArrayList<>();
        Map<String, Object> positionMap = new HashMap<>();
        positionMap.put("position", position);
        positionList.add(positionMap);
        ResponseEntity<List<Map<String, Object>>> positionResponse =
                new ResponseEntity<>(positionList, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(getPositionUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(positionResponse);

        // Mock API call to get higher position songs - return null body
        String getHigherPositionsUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID +
                "&position=gt." + position +
                "&select=song_id,position";

        ResponseEntity<List<Map<String, Object>>> nullResponse =
                new ResponseEntity<>(null, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(getHigherPositionsUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(nullResponse);

        // Mock delete operation
        String deleteUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&song_id=eq." + songID;

        when(restTemplate.exchange(
                eq(deleteUrl),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(Void.class)
        )).thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));

        favoritesService.removeFavoriteSong(userID, songID);

        // Verify all API calls
        verify(restTemplate).exchange(
                eq(getPositionUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        );

        verify(restTemplate).exchange(
                eq(getHigherPositionsUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        );

        verify(restTemplate).exchange(
                eq(deleteUrl),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(Void.class)
        );

        // Verify that no other DELETE or POST operations were performed
        // Should be exactly 1 DELETE (for the song being removed)
        verify(restTemplate, times(1)).exchange(
                contains("user_id=eq." + userID),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(Void.class)
        );

        // No POST operations should be performed
        verify(restTemplate, never()).exchange(
                eq(SUPABASE_URL + "/rest/v1/favorites"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Void.class)
        );
    }



    @Test
    void testIsFavoriteSong_True() {
        int songId = 1;
        String username = "testUser";

        String expectedUrl = SUPABASE_URL + "/rest/v1/favorites?username=eq." + username + "&song_id=eq." + songId;

        HttpHeaders expectedHeaders = new HttpHeaders();
        expectedHeaders.set("apikey", SUPABASE_API_KEY);
        expectedHeaders.set("Authorization", "Bearer " + SUPABASE_API_KEY);
        expectedHeaders.set("Content-Type", "application/json");

        HttpEntity<String> expectedEntity = new HttpEntity<>(expectedHeaders);

        List<Object> favoritesList = new ArrayList<>();
        favoritesList.add(new HashMap<>());

        ResponseEntity<List<Object>> responseEntity =
                new ResponseEntity<>(favoritesList, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(expectedUrl),
                eq(HttpMethod.GET),
                eq(expectedEntity),
                any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        boolean result = favoritesService.isFavoriteSong(songId, username);
        assertTrue(result);
    }

    @Test
    void testIsFavoriteSong_False() {
        int songId = 1;
        String username = "testUser";

        String expectedUrl = SUPABASE_URL + "/rest/v1/favorites?username=eq." + username + "&song_id=eq." + songId;

        HttpHeaders expectedHeaders = new HttpHeaders();
        expectedHeaders.set("apikey", SUPABASE_API_KEY);
        expectedHeaders.set("Authorization", "Bearer " + SUPABASE_API_KEY);
        expectedHeaders.set("Content-Type", "application/json");

        HttpEntity<String> expectedEntity = new HttpEntity<>(expectedHeaders);

        List<Object> emptyList = new ArrayList<>();

        ResponseEntity<List<Object>> responseEntity =
                new ResponseEntity<>(emptyList, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(expectedUrl),
                eq(HttpMethod.GET),
                eq(expectedEntity),
                any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        boolean result = favoritesService.isFavoriteSong(songId, username);
        assertFalse(result);
    }

    @Test
    void testIsFavoriteSong_NullResponse() {
        int songId = 1;
        String username = "testUser";

        String expectedUrl = SUPABASE_URL + "/rest/v1/favorites?username=eq." + username + "&song_id=eq." + songId;

        HttpHeaders expectedHeaders = new HttpHeaders();
        expectedHeaders.set("apikey", SUPABASE_API_KEY);
        expectedHeaders.set("Authorization", "Bearer " + SUPABASE_API_KEY);
        expectedHeaders.set("Content-Type", "application/json");

        HttpEntity<String> expectedEntity = new HttpEntity<>(expectedHeaders);

        ResponseEntity<List<Object>> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(expectedUrl),
                eq(HttpMethod.GET),
                eq(expectedEntity),
                any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        boolean result = favoritesService.isFavoriteSong(songId, username);
        assertFalse(result);

        verify(restTemplate).exchange(
                eq(expectedUrl),
                eq(HttpMethod.GET),
                eq(expectedEntity),
                any(ParameterizedTypeReference.class)
        );
    }

    @Test
    void testIsFavoriteSong_Exception() {
        int songId = 1;
        String username = "testUser";

        String expectedUrl = SUPABASE_URL + "/rest/v1/favorites?username=eq." + username + "&song_id=eq." + songId;

        HttpHeaders expectedHeaders = new HttpHeaders();
        expectedHeaders.set("apikey", SUPABASE_API_KEY);
        expectedHeaders.set("Authorization", "Bearer " + SUPABASE_API_KEY);
        expectedHeaders.set("Content-Type", "application/json");

        HttpEntity<String> expectedEntity = new HttpEntity<>(expectedHeaders);

        HttpClientErrorException exception = HttpClientErrorException.create(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error",
                HttpHeaders.EMPTY,
                null,
                null
        );

        when(restTemplate.exchange(
                eq(expectedUrl),
                eq(HttpMethod.GET),
                eq(expectedEntity),
                any(ParameterizedTypeReference.class)
        )).thenThrow(exception);

        boolean result = favoritesService.isFavoriteSong(songId, username);
        assertFalse(result);
    }

    @Test
    void testGetFavoriteSongs_Success() {
        int userID = 1;
        String favoritesUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&select=song_id,position";

        HttpHeaders expectedHeaders = new HttpHeaders();
        expectedHeaders.set("apikey", SUPABASE_API_KEY);
        expectedHeaders.set("Authorization", "Bearer " + SUPABASE_API_KEY);
        expectedHeaders.set("Content-Type", "application/json");

        HttpEntity<String> expectedEntity = new HttpEntity<>(expectedHeaders);

        List<Map<String, Object>> favoritesResponse = new ArrayList<>();
        Map<String, Object> favorite1 = new HashMap<>();
        favorite1.put("song_id", 1);
        favorite1.put("position", 1);
        Map<String, Object> favorite2 = new HashMap<>();
        favorite2.put("song_id", 2);
        favorite2.put("position", 2);
        favoritesResponse.add(favorite1);
        favoritesResponse.add(favorite2);



        ResponseEntity<List<Map<String, Object>>> favoritesEntity =
                new ResponseEntity<>(favoritesResponse, HttpStatus.OK);

        String songsUrl = SUPABASE_URL + "/rest/v1/songs?id=in.(1,2)";

        //  mock for songs query
        List<Map<String, Object>> songsResponse = new ArrayList<>();
        Map<String, Object> song1 = new HashMap<>();
        song1.put("id", 1);
        song1.put("title", "Test Song 1");
        song1.put("artist", "Test Artist 1");
        song1.put("year", "2023");
        song1.put("lyrics", "Test lyrics 1");


        Map<String, Object> song2 = new HashMap<>();
        song2.put("id", 2);
        song2.put("title", "Test Song 2");
        song2.put("artist", "Test Artist 2");
        song2.put("year", "2024");
        song2.put("lyrics", "Test lyrics 2");

        songsResponse.add(song1);
        songsResponse.add(song2);

        ResponseEntity<List<Map<String, Object>>> songsEntity =
                new ResponseEntity<>(songsResponse, HttpStatus.OK);

        doReturn(favoritesEntity).when(restTemplate).exchange(
                eq(favoritesUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        );

        when(restTemplate.exchange(
                eq(songsUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(songsEntity);

        List<Song> result = favoritesService.getFavoriteSongs(userID);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Test Song 1", result.get(0).getTitle());
        assertEquals("Test Artist 2", result.get(1).getArtist());
        assertEquals(1, result.get(0).getPosition());
        assertEquals(2, result.get(1).getPosition());

        verify(restTemplate).exchange(
                eq(favoritesUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        );

        verify(restTemplate).exchange(
                eq(songsUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        );
    }

    @Test
    void testGetFavoriteSongs_EmptyList() {

        int userID = 1;
        String favoritesUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&select=song_id,position";

        HttpHeaders expectedHeaders = new HttpHeaders();
        expectedHeaders.set("apikey", SUPABASE_API_KEY);
        expectedHeaders.set("Authorization", "Bearer " + SUPABASE_API_KEY);
        expectedHeaders.set("Content-Type", "application/json");

        HttpEntity<String> expectedEntity = new HttpEntity<>(expectedHeaders);

        List<Map<String, Object>> emptyResponse = new ArrayList<>();
        ResponseEntity<List<Map<String, Object>>> emptyEntity =
                new ResponseEntity<>(emptyResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(favoritesUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(emptyEntity);

        List<Song> result = favoritesService.getFavoriteSongs(userID);
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(restTemplate).exchange(
                eq(favoritesUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        );
    }

    @Test
    void testGetFavoriteSongs_Exception() {

        int userID = 1;
        String favoritesUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&select=song_id,position";

        HttpHeaders expectedHeaders = new HttpHeaders();
        expectedHeaders.set("apikey", SUPABASE_API_KEY);
        expectedHeaders.set("Authorization", "Bearer " + SUPABASE_API_KEY);
        expectedHeaders.set("Content-Type", "application/json");

        HttpEntity<String> expectedEntity = new HttpEntity<>(expectedHeaders);

        HttpClientErrorException exception = HttpClientErrorException.create(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error",
                HttpHeaders.EMPTY,
                null,
                null
        );

        when(restTemplate.exchange(
                eq(favoritesUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenThrow(exception);

        Exception thrownException = assertThrows(RuntimeException.class, () -> {
            favoritesService.getFavoriteSongs(userID);
        });
        assertTrue(thrownException.getMessage().contains("Error getting favorite songs"));

        verify(restTemplate).exchange(
                eq(favoritesUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        );
    }

    @Test
    void testGetFavoriteSongs_FailedFirstRequest() {
        int userID = 1;
        String favoritesUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&select=song_id,position";

        HttpHeaders expectedHeaders = new HttpHeaders();
        expectedHeaders.set("apikey", SUPABASE_API_KEY);
        expectedHeaders.set("Authorization", "Bearer " + SUPABASE_API_KEY);
        expectedHeaders.set("Content-Type", "application/json");

        HttpEntity<String> expectedEntity = new HttpEntity<>(expectedHeaders);

        ResponseEntity<List<Map<String, Object>>> badRequestResponse =
                new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        when(restTemplate.exchange(
                eq(favoritesUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(badRequestResponse);

        Exception exception = assertThrows(RuntimeException.class, () ->
                favoritesService.getFavoriteSongs(userID));

        assertTrue(exception.getMessage().contains("Failed to get favorite song list"));

        verify(restTemplate).exchange(
                eq(favoritesUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        );
    }

    @Test
    void testGetFavoriteSongs_FailedSecondRequest() {
        int userID = 1;

        String favoritesUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&select=song_id,position";

        HttpHeaders expectedHeaders = new HttpHeaders();
        expectedHeaders.set("apikey", SUPABASE_API_KEY);
        expectedHeaders.set("Authorization", "Bearer " + SUPABASE_API_KEY);
        expectedHeaders.set("Content-Type", "application/json");

        HttpEntity<String> expectedEntity = new HttpEntity<>(expectedHeaders);

        List<Map<String, Object>> favoritesResponse = new ArrayList<>();
        Map<String, Object> favorite1 = new HashMap<>();
        favorite1.put("song_id", 1);
        favorite1.put("position", 2);
        favoritesResponse.add(favorite1);

        ResponseEntity<List<Map<String, Object>>> favoritesEntity =
                new ResponseEntity<>(favoritesResponse, HttpStatus.OK);

        String songsUrl = SUPABASE_URL + "/rest/v1/songs?id=in.(1)";

        ResponseEntity<List<Map<String, Object>>> badResponse =
                new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        when(restTemplate.exchange(
                eq(favoritesUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(favoritesEntity);

        when(restTemplate.exchange(
                eq(songsUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(badResponse);

        Exception exception = assertThrows(RuntimeException.class, () ->
                favoritesService.getFavoriteSongs(userID));

        assertTrue(exception.getMessage().contains("Failed to get song details"));

        verify(restTemplate).exchange(
                eq(favoritesUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        );

        verify(restTemplate).exchange(
                eq(songsUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        );
    }

    @Test
    void testGetFavoriteSongs_NullResponse() {
        int userID = 1;
        String favoritesUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&select=song_id,position";

        HttpHeaders expectedHeaders = new HttpHeaders();
        expectedHeaders.set("apikey", SUPABASE_API_KEY);
        expectedHeaders.set("Authorization", "Bearer " + SUPABASE_API_KEY);
        expectedHeaders.set("Content-Type", "application/json");

        HttpEntity<String> expectedEntity = new HttpEntity<>(expectedHeaders);

        // Create a response with null body but OK status
        ResponseEntity<List<Map<String, Object>>> nullResponse =
                new ResponseEntity<>(null, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(favoritesUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(nullResponse);

        List<Song> result = favoritesService.getFavoriteSongs(userID);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(restTemplate).exchange(
                eq(favoritesUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        );
    }

    @Test
    void testGetFavoriteSongs_NoSongsFound_ReturnsEmptyList() {
        int userID = 42;
        String favoritesUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&select=song_id,position";
        String songsUrl = SUPABASE_URL + "/rest/v1/songs?id=in.(99)";

        // Stub favorites call
        Map<String,Object> favEntry = new HashMap<>();
        favEntry.put("song_id", 99);
        favEntry.put("position", 1);
        List<Map<String,Object>> favBody = new ArrayList<>();
        favBody.add(favEntry);
        ResponseEntity<List<Map<String,Object>>> favEntity =
                new ResponseEntity<>(favBody, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(favoritesUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(favEntity);

        ResponseEntity<List<Map<String,Object>>> songsEntity =
                new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);

        when(restTemplate.exchange(
                eq(songsUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(songsEntity);

        List<Song> result = favoritesService.getFavoriteSongs(userID);

        assertNotNull(result, "Should never return null");
        assertTrue(result.isEmpty(), "With no song rows, we should get an empty List<Song>");

        verify(restTemplate).exchange(
                eq(favoritesUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        );
        verify(restTemplate).exchange(
                eq(songsUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        );
    }

    @Test
    void testGetFavoriteSongs_SongsRowsNull_ReturnsEmptyList() {
        int userID = 77;
        String favoritesUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&select=song_id,position";
        String songsUrl = SUPABASE_URL + "/rest/v1/songs?id=in.(5)";

        Map<String,Object> fav = new HashMap<>();
        fav.put("song_id", 5);
        fav.put("position", 1);
        List<Map<String,Object>> favList = List.of(fav);

        ResponseEntity<List<Map<String,Object>>> favResp =
                new ResponseEntity<>(favList, HttpStatus.OK);
        when(restTemplate.exchange(
                eq(favoritesUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(favResp);

        ResponseEntity<List<Map<String,Object>>> songsResp =
                new ResponseEntity<>(null, HttpStatus.OK);
        when(restTemplate.exchange(
                eq(songsUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(songsResp);

        List<Song> result = favoritesService.getFavoriteSongs(userID);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetFavoriteSongs_YearFieldPresentAndMissing() {
        int userID = 123;
        String favoritesUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&select=song_id,position";
        String songsUrl = SUPABASE_URL + "/rest/v1/songs?id=in.(5,6)";

        Map<String,Object> fav1 = new HashMap<>();
        fav1.put("song_id", 5);
        fav1.put("position", 2);

        Map<String,Object> fav2 = new HashMap<>();
        fav2.put("song_id", 6);
        fav2.put("position", 1);

        ResponseEntity<List<Map<String,Object>>> favResp =
                new ResponseEntity<>(List.of(fav1, fav2), HttpStatus.OK);

        when(restTemplate.exchange(
                eq(favoritesUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(favResp);

        Map<String,Object> rowWithYear = new HashMap<>();
        rowWithYear.put("id",     5);
        rowWithYear.put("title",  "HasYear");
        rowWithYear.put("artist", "ArtistA");
        rowWithYear.put("year",   2025);

        Map<String,Object> rowNoYear = new HashMap<>();
        rowNoYear.put("id",     6);
        rowNoYear.put("title",  "NoYear");
        rowNoYear.put("artist", "ArtistB");
        // no "year" key here

        ResponseEntity<List<Map<String,Object>>> songsResp =
                new ResponseEntity<>(List.of(rowWithYear, rowNoYear), HttpStatus.OK);
        when(restTemplate.exchange(
                eq(songsUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(songsResp);

        List<Song> result = favoritesService.getFavoriteSongs(userID);

        assertEquals(2, result.size());

        Song s1 = result.get(0);
        assertEquals(5, s1.getId());
        assertEquals("HasYear", s1.getTitle());
        assertEquals(2025, s1.getYear()); // yearVal != null branch

        Song s2 = result.get(1);
        assertEquals(6, s2.getId());
        assertEquals("NoYear", s2.getTitle());
        assertNull(s2.getYear(), "Expected year to remain null when missing in JSON"); // yearVal == null branch
    }

    @Test
    void testGetFavoriteSongs_WithMissingPositions() {
        int userID = 42;
        String favoritesUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&select=song_id,position";
        String songsUrl = SUPABASE_URL + "/rest/v1/songs?id=in.(1,2)";

        // Create favorites response with one having position and one having null position
        List<Map<String, Object>> favoritesResponse = new ArrayList<>();
        Map<String, Object> favorite1 = new HashMap<>();
        favorite1.put("song_id", 1);
        favorite1.put("position", 1);
        Map<String, Object> favorite2 = new HashMap<>();
        favorite2.put("song_id", 2);
        favorite2.put("position", null); // Null position
        favoritesResponse.add(favorite1);
        favoritesResponse.add(favorite2);

        ResponseEntity<List<Map<String, Object>>> favoritesEntity =
                new ResponseEntity<>(favoritesResponse, HttpStatus.OK);

        // Mock songs response
        List<Map<String, Object>> songsResponse = new ArrayList<>();
        Map<String, Object> song1 = new HashMap<>();
        song1.put("id", 1);
        song1.put("title", "Test Song 1");
        song1.put("artist", "Test Artist 1");
        song1.put("year", "2023");
        song1.put("lyrics", "Test lyrics 1");

        Map<String, Object> song2 = new HashMap<>();
        song2.put("id", 2);
        song2.put("title", "Test Song 2");
        song2.put("artist", "Test Artist 2");
        song2.put("year", "2024");
        song2.put("lyrics", "Test lyrics 2");

        songsResponse.add(song1);
        songsResponse.add(song2);

        ResponseEntity<List<Map<String, Object>>> songsEntity =
                new ResponseEntity<>(songsResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(favoritesUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(favoritesEntity);

        when(restTemplate.exchange(
                eq(songsUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(songsEntity);

        List<Song> result = favoritesService.getFavoriteSongs(userID);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getPosition());
        // Second song should have position = 0 (default)
        assertNull(result.get(1).getPosition());

        verify(restTemplate).exchange(
                eq(favoritesUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        );

        verify(restTemplate).exchange(
                eq(songsUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        );
    }


    @Test
    void testReorderFavoriteSong_up_success() {
        int userID = 123;
        int songID = 456;
        String direction = "up";

        String getCurrentPositionUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&song_id=eq." + songID + "&select=position";
        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", SUPABASE_API_KEY);
        headers.set("Authorization", "Bearer " + SUPABASE_API_KEY);
        headers.set("Content-Type", "application/json");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        List<Map<String, Object>> currentPositionList = new ArrayList<>();
        Map<String, Object> currentPositionMap = new HashMap<>();
        currentPositionMap.put("position", 2);
        currentPositionList.add(currentPositionMap);
        ResponseEntity<List<Map<String, Object>>> currentPositionResponse =
                new ResponseEntity<>(currentPositionList, HttpStatus.OK);

        String getMaxPositionUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&select=position&order=position.desc.nullslast&limit=1";

        List<Map<String, Object>> maxPositionList = new ArrayList<>();
        Map<String, Object> maxPositionMap = new HashMap<>();
        maxPositionMap.put("position", 3);
        maxPositionList.add(maxPositionMap);
        ResponseEntity<List<Map<String, Object>>> maxPositionResponse =
                new ResponseEntity<>(maxPositionList, HttpStatus.OK);

        String checkTargetPositionUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&position=eq.3&select=song_id,position";

        List<Map<String, Object>> targetPositionList = new ArrayList<>();
        Map<String, Object> targetPositionMap = new HashMap<>();
        targetPositionMap.put("song_id", 789);
        targetPositionMap.put("position", 3);
        targetPositionList.add(targetPositionMap);
        ResponseEntity<List<Map<String, Object>>> targetPositionResponse =
                new ResponseEntity<>(targetPositionList, HttpStatus.OK);

        String deleteTargetUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&song_id=eq.789";
        String deleteSourceUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&song_id=eq." + songID;
        String insertUrl = SUPABASE_URL + "/rest/v1/favorites";

        when(restTemplate.exchange(eq(getCurrentPositionUrl), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(currentPositionResponse);
        when(restTemplate.exchange(eq(getMaxPositionUrl), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(maxPositionResponse);
        when(restTemplate.exchange(eq(checkTargetPositionUrl), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(targetPositionResponse);
        when(restTemplate.exchange(eq(deleteTargetUrl), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));
        when(restTemplate.exchange(eq(deleteSourceUrl), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));
        when(restTemplate.exchange(eq(insertUrl), eq(HttpMethod.POST), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.CREATED));

        favoritesService.reorderFavoriteSong(userID, songID, direction);

        verify(restTemplate).exchange(eq(getCurrentPositionUrl), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class));
        verify(restTemplate).exchange(eq(getMaxPositionUrl), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class));
        verify(restTemplate).exchange(eq(checkTargetPositionUrl), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class));
        verify(restTemplate).exchange(eq(deleteTargetUrl), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Void.class));
        verify(restTemplate).exchange(eq(deleteSourceUrl), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Void.class));
        verify(restTemplate, times(2)).exchange(eq(insertUrl), eq(HttpMethod.POST), any(HttpEntity.class), eq(Void.class));
    }

    @Test
    void testReorderFavoriteSong_down_success() {
        int userID = 123;
        int songID = 456;
        String direction = "down";

        String getCurrentPositionUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&song_id=eq." + songID + "&select=position";
        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", SUPABASE_API_KEY);
        headers.set("Authorization", "Bearer " + SUPABASE_API_KEY);
        headers.set("Content-Type", "application/json");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        List<Map<String, Object>> currentPositionList = new ArrayList<>();
        Map<String, Object> currentPositionMap = new HashMap<>();
        currentPositionMap.put("position", 2);
        currentPositionList.add(currentPositionMap);
        ResponseEntity<List<Map<String, Object>>> currentPositionResponse =
                new ResponseEntity<>(currentPositionList, HttpStatus.OK);

        String checkTargetPositionUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&position=eq.1&select=song_id,position";

        List<Map<String, Object>> targetPositionList = new ArrayList<>();
        Map<String, Object> targetPositionMap = new HashMap<>();
        targetPositionMap.put("song_id", 789);
        targetPositionMap.put("position", 1);
        targetPositionList.add(targetPositionMap);
        ResponseEntity<List<Map<String, Object>>> targetPositionResponse =
                new ResponseEntity<>(targetPositionList, HttpStatus.OK);

        String deleteTargetUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&song_id=eq.789";
        String deleteSourceUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&song_id=eq." + songID;
        String insertUrl = SUPABASE_URL + "/rest/v1/favorites";

        when(restTemplate.exchange(eq(getCurrentPositionUrl), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(currentPositionResponse);
        when(restTemplate.exchange(eq(checkTargetPositionUrl), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(targetPositionResponse);
        when(restTemplate.exchange(eq(deleteTargetUrl), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));
        when(restTemplate.exchange(eq(deleteSourceUrl), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));
        when(restTemplate.exchange(eq(insertUrl), eq(HttpMethod.POST), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.CREATED));

        favoritesService.reorderFavoriteSong(userID, songID, direction);

        verify(restTemplate).exchange(eq(getCurrentPositionUrl), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class));
        verify(restTemplate).exchange(eq(checkTargetPositionUrl), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class));
        verify(restTemplate).exchange(eq(deleteTargetUrl), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Void.class));
        verify(restTemplate).exchange(eq(deleteSourceUrl), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Void.class));
        verify(restTemplate, times(2)).exchange(eq(insertUrl), eq(HttpMethod.POST), any(HttpEntity.class), eq(Void.class));
    }

    @Test
    void testReorderFavoriteSong_up_alreadyAtTop() {
        int userID = 123;
        int songID = 456;
        String direction = "up";

        String getCurrentPositionUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&song_id=eq." + songID + "&select=position";
        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", SUPABASE_API_KEY);
        headers.set("Authorization", "Bearer " + SUPABASE_API_KEY);
        headers.set("Content-Type", "application/json");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        List<Map<String, Object>> currentPositionList = new ArrayList<>();
        Map<String, Object> currentPositionMap = new HashMap<>();
        currentPositionMap.put("position", 3);
        currentPositionList.add(currentPositionMap);
        ResponseEntity<List<Map<String, Object>>> currentPositionResponse =
                new ResponseEntity<>(currentPositionList, HttpStatus.OK);

        String getMaxPositionUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&select=position&order=position.desc.nullslast&limit=1";

        List<Map<String, Object>> maxPositionList = new ArrayList<>();
        Map<String, Object> maxPositionMap = new HashMap<>();
        maxPositionMap.put("position", 3);
        maxPositionList.add(maxPositionMap);
        ResponseEntity<List<Map<String, Object>>> maxPositionResponse =
                new ResponseEntity<>(maxPositionList, HttpStatus.OK);

        when(restTemplate.exchange(eq(getCurrentPositionUrl), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(currentPositionResponse);
        when(restTemplate.exchange(eq(getMaxPositionUrl), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(maxPositionResponse);

        favoritesService.reorderFavoriteSong(userID, songID, direction);

        verify(restTemplate).exchange(eq(getCurrentPositionUrl), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class));
        verify(restTemplate).exchange(eq(getMaxPositionUrl), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class));
        verify(restTemplate, never()).exchange(anyString(), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Void.class));
        verify(restTemplate, never()).exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Void.class));
    }

    @Test
    void testReorderFavoriteSong_down_alreadyAtBottom() {
        int userID = 123;
        int songID = 456;
        String direction = "down";

        String getCurrentPositionUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&song_id=eq." + songID + "&select=position";
        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", SUPABASE_API_KEY);
        headers.set("Authorization", "Bearer " + SUPABASE_API_KEY);
        headers.set("Content-Type", "application/json");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        List<Map<String, Object>> currentPositionList = new ArrayList<>();
        Map<String, Object> currentPositionMap = new HashMap<>();
        currentPositionMap.put("position", 1);
        currentPositionList.add(currentPositionMap);
        ResponseEntity<List<Map<String, Object>>> currentPositionResponse =
                new ResponseEntity<>(currentPositionList, HttpStatus.OK);

        when(restTemplate.exchange(eq(getCurrentPositionUrl), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(currentPositionResponse);

        favoritesService.reorderFavoriteSong(userID, songID, direction);

        verify(restTemplate).exchange(eq(getCurrentPositionUrl), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class));
        verify(restTemplate, never()).exchange(anyString(), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Void.class));
        verify(restTemplate, never()).exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Void.class));
    }

    @Test
    void testReorderFavoriteSong_songNotFound() {
        int userID = 123;
        int songID = 456;
        String direction = "up";

        String getCurrentPositionUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&song_id=eq." + songID + "&select=position";
        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", SUPABASE_API_KEY);
        headers.set("Authorization", "Bearer " + SUPABASE_API_KEY);
        headers.set("Content-Type", "application/json");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        List<Map<String, Object>> emptyList = new ArrayList<>();
        ResponseEntity<List<Map<String, Object>>> emptyResponse =
                new ResponseEntity<>(emptyList, HttpStatus.OK);

        when(restTemplate.exchange(eq(getCurrentPositionUrl), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(emptyResponse);

        Exception exception = assertThrows(RuntimeException.class, () ->
                favoritesService.reorderFavoriteSong(userID, songID, direction));

        assertTrue(exception.getMessage().contains("Song not found in user's favorites"));

        verify(restTemplate).exchange(eq(getCurrentPositionUrl), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class));
    }
    @Test
    void testReorderFavoriteSong_noOtherSongAtTargetPosition() {
        int userID = 123;
        int songID = 456;
        String direction = "up";

        String getCurrentPositionUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&song_id=eq." + songID + "&select=position";
        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", SUPABASE_API_KEY);
        headers.set("Authorization", "Bearer " + SUPABASE_API_KEY);
        headers.set("Content-Type", "application/json");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        List<Map<String, Object>> currentPositionList = new ArrayList<>();
        Map<String, Object> currentPositionMap = new HashMap<>();
        currentPositionMap.put("position", 2);
        currentPositionList.add(currentPositionMap);
        ResponseEntity<List<Map<String, Object>>> currentPositionResponse =
                new ResponseEntity<>(currentPositionList, HttpStatus.OK);

        String getMaxPositionUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&select=position&order=position.desc.nullslast&limit=1";

        List<Map<String, Object>> maxPositionList = new ArrayList<>();
        Map<String, Object> maxPositionMap = new HashMap<>();
        maxPositionMap.put("position", 3);
        maxPositionList.add(maxPositionMap);
        ResponseEntity<List<Map<String, Object>>> maxPositionResponse =
                new ResponseEntity<>(maxPositionList, HttpStatus.OK);

        String checkTargetPositionUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&position=eq.3&select=song_id,position";

        List<Map<String, Object>> emptyList = new ArrayList<>();
        ResponseEntity<List<Map<String, Object>>> emptyResponse =
                new ResponseEntity<>(emptyList, HttpStatus.OK);

        String deleteSourceUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&song_id=eq." + songID;
        String insertUrl = SUPABASE_URL + "/rest/v1/favorites";

        when(restTemplate.exchange(eq(getCurrentPositionUrl), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(currentPositionResponse);
        when(restTemplate.exchange(eq(getMaxPositionUrl), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(maxPositionResponse);
        when(restTemplate.exchange(eq(checkTargetPositionUrl), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(emptyResponse);
        when(restTemplate.exchange(eq(deleteSourceUrl), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));
        when(restTemplate.exchange(eq(insertUrl), eq(HttpMethod.POST), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.CREATED));

        favoritesService.reorderFavoriteSong(userID, songID, direction);

        verify(restTemplate).exchange(eq(getCurrentPositionUrl), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class));
        verify(restTemplate).exchange(eq(getMaxPositionUrl), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class));
        verify(restTemplate).exchange(eq(checkTargetPositionUrl), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class));
        verify(restTemplate).exchange(eq(deleteSourceUrl), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Void.class));
        verify(restTemplate, times(1)).exchange(eq(insertUrl), eq(HttpMethod.POST), any(HttpEntity.class), eq(Void.class));
    }


    @Test
    void testReorderFavoriteSong_NullCurrentPosition() {
        int userID = 123;
        int songID = 456;
        String direction = "up";

        String getCurrentPositionUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&song_id=eq." + songID + "&select=position";
        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", SUPABASE_API_KEY);
        headers.set("Authorization", "Bearer " + SUPABASE_API_KEY);
        headers.set("Content-Type", "application/json");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        List<Map<String, Object>> currentPositionList = new ArrayList<>();
        Map<String, Object> currentPositionMap = new HashMap<>();
        currentPositionMap.put("position", null); // Null position
        currentPositionList.add(currentPositionMap);
        ResponseEntity<List<Map<String, Object>>> currentPositionResponse =
                new ResponseEntity<>(currentPositionList, HttpStatus.OK);

        // The method should use position=0 if null
        String getMaxPositionUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&select=position&order=position.desc.nullslast&limit=1";

        List<Map<String, Object>> maxPositionList = new ArrayList<>();
        Map<String, Object> maxPositionMap = new HashMap<>();
        maxPositionMap.put("position", 3);
        maxPositionList.add(maxPositionMap);
        ResponseEntity<List<Map<String, Object>>> maxPositionResponse =
                new ResponseEntity<>(maxPositionList, HttpStatus.OK);

        when(restTemplate.exchange(eq(getCurrentPositionUrl), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(currentPositionResponse);
        when(restTemplate.exchange(eq(getMaxPositionUrl), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(maxPositionResponse);

        // Null = 0, target position = 1
        String checkTargetPositionUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&position=eq.1&select=song_id,position";

        List<Map<String, Object>> targetPositionList = new ArrayList<>();
        Map<String, Object> targetPositionMap = new HashMap<>();
        targetPositionMap.put("song_id", 789);
        targetPositionMap.put("position", 1);
        targetPositionList.add(targetPositionMap);
        ResponseEntity<List<Map<String, Object>>> targetPositionResponse =
                new ResponseEntity<>(targetPositionList, HttpStatus.OK);

        when(restTemplate.exchange(eq(checkTargetPositionUrl), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(targetPositionResponse);

        String deleteTargetUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&song_id=eq.789";
        String deleteSourceUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&song_id=eq." + songID;
        String insertUrl = SUPABASE_URL + "/rest/v1/favorites";

        when(restTemplate.exchange(eq(deleteTargetUrl), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));
        when(restTemplate.exchange(eq(deleteSourceUrl), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));
        when(restTemplate.exchange(eq(insertUrl), eq(HttpMethod.POST), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.CREATED));

        favoritesService.reorderFavoriteSong(userID, songID, direction);

        verify(restTemplate).exchange(eq(getCurrentPositionUrl), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class));
        verify(restTemplate).exchange(eq(getMaxPositionUrl), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class));
        verify(restTemplate).exchange(eq(checkTargetPositionUrl), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class));
        verify(restTemplate).exchange(eq(deleteTargetUrl), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Void.class));
        verify(restTemplate).exchange(eq(deleteSourceUrl), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Void.class));
        verify(restTemplate, times(2)).exchange(eq(insertUrl), eq(HttpMethod.POST), any(HttpEntity.class), eq(Void.class));
    }

    @Test
    void testReorderFavoriteSong_NullMaxPosition() {
        int userID = 123;
        int songID = 456;
        String direction = "up";

        String getCurrentPositionUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&song_id=eq." + songID + "&select=position";
        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", SUPABASE_API_KEY);
        headers.set("Authorization", "Bearer " + SUPABASE_API_KEY);
        headers.set("Content-Type", "application/json");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        List<Map<String, Object>> currentPositionList = new ArrayList<>();
        Map<String, Object> currentPositionMap = new HashMap<>();
        currentPositionMap.put("position", 2);
        currentPositionList.add(currentPositionMap);
        ResponseEntity<List<Map<String, Object>>> currentPositionResponse =
                new ResponseEntity<>(currentPositionList, HttpStatus.OK);

        String getMaxPositionUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&select=position&order=position.desc.nullslast&limit=1";

        List<Map<String, Object>> maxPositionList = new ArrayList<>();
        Map<String, Object> maxPositionMap = new HashMap<>();
        maxPositionMap.put("position", null); // Null max position
        maxPositionList.add(maxPositionMap);
        ResponseEntity<List<Map<String, Object>>> maxPositionResponse =
                new ResponseEntity<>(maxPositionList, HttpStatus.OK);

        when(restTemplate.exchange(eq(getCurrentPositionUrl), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(currentPositionResponse);
        when(restTemplate.exchange(eq(getMaxPositionUrl), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(maxPositionResponse);

        // Should proceed to target position check since maxPosition condition is not met
        String checkTargetPositionUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&position=eq.3&select=song_id,position";

        List<Map<String, Object>> emptyList = new ArrayList<>();
        ResponseEntity<List<Map<String, Object>>> emptyResponse =
                new ResponseEntity<>(emptyList, HttpStatus.OK);

        when(restTemplate.exchange(eq(checkTargetPositionUrl), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(emptyResponse);

        String deleteSourceUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&song_id=eq." + songID;
        String insertUrl = SUPABASE_URL + "/rest/v1/favorites";

        when(restTemplate.exchange(eq(deleteSourceUrl), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));
        when(restTemplate.exchange(eq(insertUrl), eq(HttpMethod.POST), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.CREATED));

        favoritesService.reorderFavoriteSong(userID, songID, direction);

        verify(restTemplate).exchange(eq(getCurrentPositionUrl), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class));
        verify(restTemplate).exchange(eq(getMaxPositionUrl), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class));
        verify(restTemplate).exchange(eq(checkTargetPositionUrl), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class));
        verify(restTemplate).exchange(eq(deleteSourceUrl), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Void.class));
        verify(restTemplate).exchange(eq(insertUrl), eq(HttpMethod.POST), any(HttpEntity.class), eq(Void.class));
    }

    @Test
    void testReorderFavoriteSong_NoTargetPositionSong() {
        int userID = 123;
        int songID = 456;
        String direction = "up";

        String getCurrentPositionUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&song_id=eq." + songID + "&select=position";
        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", SUPABASE_API_KEY);
        headers.set("Authorization", "Bearer " + SUPABASE_API_KEY);
        headers.set("Content-Type", "application/json");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        List<Map<String, Object>> currentPositionList = new ArrayList<>();
        Map<String, Object> currentPositionMap = new HashMap<>();
        currentPositionMap.put("position", 2);
        currentPositionList.add(currentPositionMap);
        ResponseEntity<List<Map<String, Object>>> currentPositionResponse =
                new ResponseEntity<>(currentPositionList, HttpStatus.OK);

        String getMaxPositionUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&select=position&order=position.desc.nullslast&limit=1";

        List<Map<String, Object>> maxPositionList = new ArrayList<>();
        Map<String, Object> maxPositionMap = new HashMap<>();
        maxPositionMap.put("position", 5);
        maxPositionList.add(maxPositionMap);
        ResponseEntity<List<Map<String, Object>>> maxPositionResponse =
                new ResponseEntity<>(maxPositionList, HttpStatus.OK);

        when(restTemplate.exchange(eq(getCurrentPositionUrl), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(currentPositionResponse);
        when(restTemplate.exchange(eq(getMaxPositionUrl), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(maxPositionResponse);

        // Return empty response for target position (no song at position 3)
        String checkTargetPositionUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&position=eq.3&select=song_id,position";
        List<Map<String, Object>> emptyList = new ArrayList<>();
        ResponseEntity<List<Map<String, Object>>> emptyResponse =
                new ResponseEntity<>(emptyList, HttpStatus.OK);

        when(restTemplate.exchange(eq(checkTargetPositionUrl), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(emptyResponse);

        // Only need to update our song, no target song to swap with
        String deleteSourceUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&song_id=eq." + songID;
        String insertUrl = SUPABASE_URL + "/rest/v1/favorites";

        when(restTemplate.exchange(eq(deleteSourceUrl), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));
        when(restTemplate.exchange(eq(insertUrl), eq(HttpMethod.POST), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.CREATED));

        favoritesService.reorderFavoriteSong(userID, songID, direction);

        verify(restTemplate).exchange(eq(getCurrentPositionUrl), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class));
        verify(restTemplate).exchange(eq(getMaxPositionUrl), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class));
        verify(restTemplate).exchange(eq(checkTargetPositionUrl), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class));
        verify(restTemplate).exchange(eq(deleteSourceUrl), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Void.class));
        verify(restTemplate, times(1)).exchange(eq(insertUrl), eq(HttpMethod.POST), any(HttpEntity.class), eq(Void.class));
    }

    @Test
    void testReorderFavoriteSong_NullCurrentPositionResponseBody() {
        int userID = 123;
        int songID = 456;
        String direction = "up";

        // Mock API call to get current position with null body response
        String getCurrentPositionUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&song_id=eq." + songID + "&select=position";

        // Return null body response
        ResponseEntity<List<Map<String, Object>>> nullResponse =
                new ResponseEntity<>(null, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(getCurrentPositionUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(nullResponse);

        // Should throw exception as per the code
        Exception exception = assertThrows(RuntimeException.class, () ->
                favoritesService.reorderFavoriteSong(userID, songID, direction));

        assertTrue(exception.getMessage().contains("Song not found in user's favorites"));

        verify(restTemplate).exchange(
                eq(getCurrentPositionUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        );

        // Verify that no other API calls were made
        verify(restTemplate, times(1)).exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        );
    }
    @Test
    void testReorderFavoriteSong_EmptyMaxPositionResponse() {
        int userID = 123;
        int songID = 456;
        String direction = "up";

        // Mock API call to get current position
        String getCurrentPositionUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&song_id=eq." + songID + "&select=position";

        List<Map<String, Object>> currentPositionList = new ArrayList<>();
        Map<String, Object> currentPositionMap = new HashMap<>();
        currentPositionMap.put("position", 2);
        currentPositionList.add(currentPositionMap);
        ResponseEntity<List<Map<String, Object>>> currentPositionResponse =
                new ResponseEntity<>(currentPositionList, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(getCurrentPositionUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(currentPositionResponse);

        // Mock API call to get max position with empty list
        String getMaxPositionUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&select=position&order=position.desc.nullslast&limit=1";

        List<Map<String, Object>> emptyList = new ArrayList<>();
        ResponseEntity<List<Map<String, Object>>> emptyResponse =
                new ResponseEntity<>(emptyList, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(getMaxPositionUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(emptyResponse);

        // Target position check (for position 3)
        String checkTargetPositionUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&position=eq.3&select=song_id,position";

        // No song at target position
        ResponseEntity<List<Map<String, Object>>> emptyTargetResponse =
                new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);

        when(restTemplate.exchange(
                eq(checkTargetPositionUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(emptyTargetResponse);

        // Mock delete and insert operations
        String deleteSourceUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&song_id=eq." + songID;
        String insertUrl = SUPABASE_URL + "/rest/v1/favorites";

        when(restTemplate.exchange(
                eq(deleteSourceUrl),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(Void.class)
        )).thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));

        when(restTemplate.exchange(
                eq(insertUrl),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Void.class)
        )).thenReturn(new ResponseEntity<>(HttpStatus.CREATED));

        favoritesService.reorderFavoriteSong(userID, songID, direction);

        verify(restTemplate).exchange(
                eq(getCurrentPositionUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        );

        verify(restTemplate).exchange(
                eq(getMaxPositionUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        );

        verify(restTemplate).exchange(
                eq(checkTargetPositionUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        );

        verify(restTemplate).exchange(
                eq(deleteSourceUrl),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(Void.class)
        );

        verify(restTemplate).exchange(
                eq(insertUrl),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Void.class)
        );
    }

    @Test
    void testReorderFavoriteSong_NullTargetPositionResponseBody() {
        int userID = 123;
        int songID = 456;
        String direction = "up";

        // Mock current position
        String getCurrentPositionUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&song_id=eq." + songID + "&select=position";
        List<Map<String, Object>> currentPositionList = new ArrayList<>();
        Map<String, Object> currentPositionMap = new HashMap<>();
        currentPositionMap.put("position", 2);
        currentPositionList.add(currentPositionMap);
        ResponseEntity<List<Map<String, Object>>> currentPositionResponse =
                new ResponseEntity<>(currentPositionList, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(getCurrentPositionUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(currentPositionResponse);

        // Mock max position
        String getMaxPositionUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&select=position&order=position.desc.nullslast&limit=1";
        List<Map<String, Object>> maxPositionList = new ArrayList<>();
        Map<String, Object> maxPositionMap = new HashMap<>();
        maxPositionMap.put("position", 5);
        maxPositionList.add(maxPositionMap);
        ResponseEntity<List<Map<String, Object>>> maxPositionResponse =
                new ResponseEntity<>(maxPositionList, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(getMaxPositionUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(maxPositionResponse);

        // Return NULL for target position response
        String checkTargetPositionUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&position=eq.3&select=song_id,position";
        ResponseEntity<List<Map<String, Object>>> nullTargetResponse =
                new ResponseEntity<>(null, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(checkTargetPositionUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(nullTargetResponse);

        // Mock delete and insert operations
        String deleteSourceUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&song_id=eq." + songID;
        String insertUrl = SUPABASE_URL + "/rest/v1/favorites";

        when(restTemplate.exchange(
                eq(deleteSourceUrl),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(Void.class)
        )).thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));

        when(restTemplate.exchange(
                eq(insertUrl),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Void.class)
        )).thenReturn(new ResponseEntity<>(HttpStatus.CREATED));

        favoritesService.reorderFavoriteSong(userID, songID, direction);

        // Verify all API calls
        verify(restTemplate).exchange(
                eq(getCurrentPositionUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        );

        verify(restTemplate).exchange(
                eq(getMaxPositionUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        );

        verify(restTemplate).exchange(
                eq(checkTargetPositionUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        );

        verify(restTemplate).exchange(
                eq(deleteSourceUrl),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(Void.class)
        );

        verify(restTemplate).exchange(
                eq(insertUrl),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Void.class)
        );
    }

    @Test
    void testReorderFavoriteSong_NullMaxPositionResponseBody() {
        int userID = 123;
        int songID = 456;
        String direction = "up";

        // Mock current position
        String getCurrentPositionUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&song_id=eq." + songID + "&select=position";
        List<Map<String, Object>> currentPositionList = new ArrayList<>();
        Map<String, Object> currentPositionMap = new HashMap<>();
        currentPositionMap.put("position", 2);
        currentPositionList.add(currentPositionMap);
        ResponseEntity<List<Map<String, Object>>> currentPositionResponse =
                new ResponseEntity<>(currentPositionList, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(getCurrentPositionUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(currentPositionResponse);

        // Mock max position with NULL body response
        String getMaxPositionUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&select=position&order=position.desc.nullslast&limit=1";
        ResponseEntity<List<Map<String, Object>>> nullMaxPositionResponse =
                new ResponseEntity<>(null, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(getMaxPositionUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(nullMaxPositionResponse);

        // Target position check
        String checkTargetPositionUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&position=eq.3&select=song_id,position";
        List<Map<String, Object>> emptyList = new ArrayList<>();
        ResponseEntity<List<Map<String, Object>>> emptyResponse =
                new ResponseEntity<>(emptyList, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(checkTargetPositionUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(emptyResponse);

        // Mock delete and insert operations
        String deleteSourceUrl = SUPABASE_URL + "/rest/v1/favorites?user_id=eq." + userID + "&song_id=eq." + songID;
        String insertUrl = SUPABASE_URL + "/rest/v1/favorites";

        when(restTemplate.exchange(
                eq(deleteSourceUrl),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(Void.class)
        )).thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));

        when(restTemplate.exchange(
                eq(insertUrl),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Void.class)
        )).thenReturn(new ResponseEntity<>(HttpStatus.CREATED));

        favoritesService.reorderFavoriteSong(userID, songID, direction);

        // Verify all API calls
        verify(restTemplate).exchange(
                eq(getCurrentPositionUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        );

        verify(restTemplate).exchange(
                eq(getMaxPositionUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        );

        verify(restTemplate).exchange(
                eq(checkTargetPositionUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        );

        verify(restTemplate).exchange(
                eq(deleteSourceUrl),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(Void.class)
        );

        verify(restTemplate).exchange(
                eq(insertUrl),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Void.class)
        );
    }


}

