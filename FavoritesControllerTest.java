package edu.usc.csci310.project.controllers;

import edu.usc.csci310.project.models.Song;
import edu.usc.csci310.project.services.FavoritesService;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;


import static org.mockito.ArgumentMatchers.anyInt;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.List;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@RequestMapping("/api/favorites")
@WebMvcTest(FavoritesController.class)
public class FavoritesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FavoritesService favoritesService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public RestTemplate restTemplate() {
            return new RestTemplate();
        }
    }

    @Test
    public void testAddFavoriteSong_Success() throws Exception {
        mockMvc.perform(post("/api/favorites")
                        .param("userID", "200")
                        .param("songID", "100")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void testAddFavoriteSong_Failure() throws Exception {
        doThrow(new RuntimeException("Failed to add favorite")).when(favoritesService)
                .addFavoriteSong(anyInt(), anyInt());

        mockMvc.perform(post("/api/favorites")
                        .param("userID", "123")
                        .param("songID", "200")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void testRemoveFavoriteSong_Success() throws Exception {

        mockMvc.perform(delete("/api/favorites")
                        .param("userID", "123")
                        .param("songID", "180")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void testRemoveFavoriteSong_Failure() throws Exception {
        doThrow(new RuntimeException("Failed to remove favorite")).when(favoritesService)
                .removeFavoriteSong(anyInt(), anyInt());

        mockMvc.perform(delete("/api/favorites")
                        .param("userID", "123")
                        .param("songID", "180")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void testIsFavoriteSong_Success() throws Exception {
        int songId = 1;
        String username = "testUser";

        when(favoritesService.isFavoriteSong(songId, username)).thenReturn(true);

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/favorites/check")
                                .param("songID", String.valueOf(songId))
                                .param("username", username)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("true"));

        verify(favoritesService).isFavoriteSong(songId, username);
    }

    @Test
    public void testGetFavoriteSongs_Success() throws Exception {
        int userID = 123;
        List<Song> mockFavorites = new ArrayList<>();

        Song song1 = new Song(1, "Test Song 1", "Test Artist 1");
        Song song2 = new Song(2, "Test Song 2", "Test Artist 2");

        mockFavorites.add(song1);
        mockFavorites.add(song2);

        when(favoritesService.getFavoriteSongs(userID)).thenReturn(mockFavorites);

        mockMvc.perform(get("/api/favorites")
                        .param("userID", String.valueOf(userID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Test Song 1"))
                .andExpect(jsonPath("$[1].artist").value("Test Artist 2"));

        verify(favoritesService).getFavoriteSongs(userID);
    }

    @Test
    public void testGetFavoriteSongs_Empty() throws Exception {
        int userID = 456;

        // empty list of favorites
        when(favoritesService.getFavoriteSongs(userID)).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/api/favorites")
                        .param("userID", String.valueOf(userID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(favoritesService).getFavoriteSongs(userID);
    }

    @Test
    public void testGetFavoriteSongs_Failure() throws Exception {
        int userID = 789;

        when(favoritesService.getFavoriteSongs(userID))
                .thenThrow(new RuntimeException("Failed to get favorites"));

        mockMvc.perform(get("/api/favorites")
                        .param("userID", String.valueOf(userID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(favoritesService).getFavoriteSongs(userID);
    }

    @Test
    public void testIsFavoriteSong_Failure() throws Exception {
        int songId = 1;
        String username = "testUser";

        when(favoritesService.isFavoriteSong(songId, username))
                .thenThrow(new RuntimeException("Error checking favorite status"));

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/favorites/check")
                                .param("songID", String.valueOf(songId))
                                .param("username", username)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError());

        verify(favoritesService).isFavoriteSong(songId, username);
    }

    @Test
    public void testReorderFavoriteSong_up_Success() throws Exception {
        int songId = 1;
        int userId = 1;
        String direction = "up";

        doNothing().when(favoritesService).reorderFavoriteSong(userId, songId, direction);

        mockMvc.perform(put("/api/favorites/reorder")
                        .param("userID", String.valueOf(userId))
                        .param("songID", String.valueOf(songId))
                        .param("direction", direction))
                .andExpect(status().isOk());

        verify(favoritesService, times(1)).reorderFavoriteSong(userId, songId, direction);

    }

    @Test
    public void testReorderFavoriteSong_down_Success() throws Exception {
        int songId = 1;
        int userId = 1;
        String direction = "down";

        doNothing().when(favoritesService).reorderFavoriteSong(userId, songId, direction);

        mockMvc.perform(put("/api/favorites/reorder")
                        .param("userID", String.valueOf(userId))
                        .param("songID", String.valueOf(songId))
                        .param("direction", direction))
                .andExpect(status().isOk());

        verify(favoritesService, times(1)).reorderFavoriteSong(userId, songId, direction);

    }

    @Test
    public void testReorderFavoriteSong_InvalidDirection() throws Exception {
        int userId = 1;
        int songId = 1;
        String direction = "invalid";

        mockMvc.perform(put("/api/favorites/reorder")
                        .param("userID", String.valueOf(userId))
                        .param("songID", String.valueOf(songId))
                        .param("direction", direction))
                .andExpect(status().isBadRequest());

        verify(favoritesService, never()).reorderFavoriteSong(anyInt(), anyInt(), anyString());
    }

    @Test
    public void testReorderFavoriteSong_ServiceThrowsException() throws Exception {
        // Setup
        int userId = 1;
        int songId = 1;
        String direction = "up";

        doThrow(new RuntimeException("Error reordering song")).when(favoritesService)
                .reorderFavoriteSong(userId, songId, direction);

        mockMvc.perform(put("/api/favorites/reorder")
                        .param("userID", String.valueOf(userId))
                        .param("songID", String.valueOf(songId))
                        .param("direction", direction))
                .andExpect(status().isInternalServerError());

        verify(favoritesService, times(1)).reorderFavoriteSong(userId, songId, direction);
    }

    @Test
    public void testReorderFavoriteSong_MissingParameters() throws Exception {
        mockMvc.perform(put("/api/favorites/reorder")
                        .param("songID", "1")
                        .param("direction", "up"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put("/api/favorites/reorder")
                        .param("userID", "1")
                        .param("direction", "up"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put("/api/favorites/reorder")
                        .param("userID", "1")
                        .param("songID", "1"))
                .andExpect(status().isBadRequest());

        verify(favoritesService, never()).reorderFavoriteSong(anyInt(), anyInt(), anyString());
    }

}

