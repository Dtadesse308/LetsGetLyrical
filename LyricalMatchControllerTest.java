package edu.usc.csci310.project.controllers;

import edu.usc.csci310.project.models.Song;
import edu.usc.csci310.project.models.User;
import edu.usc.csci310.project.services.FavoritesService;
import edu.usc.csci310.project.services.LyricalMatchService;
import edu.usc.csci310.project.services.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LyricalMatchController.class)
public class LyricalMatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LyricalMatchService lyricalMatchService;

    @MockBean
    private UserService userService;

    @MockBean
    private FavoritesService favoritesService;

    @Test
    public void testGetLyricalSoulmateFound() throws Exception {
        String username = "testuser";
        User currentUser = new User();
        currentUser.setId(1);
        currentUser.setUsername(username);

        User soulmate = new User();
        soulmate.setId(2);
        soulmate.setUsername("soulmateUser");

        List<Song> mockFavorites = new ArrayList<>();
        mockFavorites.add(new Song(101, "Song A", "Artist A", 2020));
        mockFavorites.add(new Song(102, "Song B", "Artist B", 2021));

        Mockito.when(userService.getUserByUsername(username)).thenReturn(currentUser);
        Mockito.when(lyricalMatchService.findLyricalSoulmate(currentUser)).thenReturn(soulmate);
        Mockito.when(favoritesService.getFavoriteSongs(soulmate.getId())).thenReturn(mockFavorites);

        mockMvc.perform(get("/match/soulmate").param("username", username))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.id").value(2))
                .andExpect(jsonPath("$.user.username").value("soulmateUser"))
                .andExpect(jsonPath("$.favorites").isArray())
                .andExpect(jsonPath("$.favorites[0].title").value("Song A"));
    }

    @Test
    public void testGetLyricalSoulmateNotFound() throws Exception {
        String username = "testuser";
        User currentUser = new User();
        currentUser.setId(1);
        currentUser.setUsername(username);

        Mockito.when(userService.getUserByUsername(username)).thenReturn(currentUser);
        Mockito.when(lyricalMatchService.findLyricalSoulmate(currentUser)).thenReturn(null);

        mockMvc.perform(get("/match/soulmate").param("username", username))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetLyricalSoulmateMutual() throws Exception {
        String username = "testuser";
        User currentUser = new User();
        currentUser.setId(1);
        currentUser.setUsername(username);

        User soulmate = new User();
        soulmate.setId(2);
        soulmate.setUsername("soulmateUser");

        List<Song> mockFavorites = List.of(new Song(101, "Song A", "Artist A", 2020));

        Mockito.when(userService.getUserByUsername(username)).thenReturn(currentUser);
        Mockito.when(lyricalMatchService.findLyricalSoulmate(currentUser)).thenReturn(soulmate);
        Mockito.when(lyricalMatchService.findLyricalSoulmate(soulmate, currentUser)).thenReturn(currentUser);
        Mockito.when(favoritesService.getFavoriteSongs(soulmate.getId())).thenReturn(mockFavorites);

        mockMvc.perform(get("/match/soulmate").param("username", username))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mutual").value(true));
    }


    @Test
    public void testGetLyricalEnemyFound() throws Exception {
        String username = "testuser";
        User currentUser = new User();
        currentUser.setId(1);
        currentUser.setUsername(username);

        User enemy = new User();
        enemy.setId(3);
        enemy.setUsername("enemyUser");

        List<Song> mockFavorites = new ArrayList<>();
        mockFavorites.add(new Song(201, "Enemy Song", "Enemy Artist", 2019));

        Mockito.when(userService.getUserByUsername(username)).thenReturn(currentUser);
        Mockito.when(lyricalMatchService.findLyricalEnemy(currentUser)).thenReturn(enemy);
        Mockito.when(favoritesService.getFavoriteSongs(enemy.getId())).thenReturn(mockFavorites);

        mockMvc.perform(get("/match/enemy").param("username", username))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.id").value(3))
                .andExpect(jsonPath("$.user.username").value("enemyUser"))
                .andExpect(jsonPath("$.favorites").isArray())
                .andExpect(jsonPath("$.favorites[0].title").value("Enemy Song"));
    }

    @Test
    public void testGetLyricalEnemyNotFound() throws Exception {
        String username = "testuser";
        User currentUser = new User();
        currentUser.setId(1);
        currentUser.setUsername(username);

        Mockito.when(userService.getUserByUsername(username)).thenReturn(currentUser);
        Mockito.when(lyricalMatchService.findLyricalEnemy(currentUser)).thenReturn(null);

        mockMvc.perform(get("/match/enemy").param("username", username))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetLyricalEnemyMutual() throws Exception {
        String username = "testuser";
        User currentUser = new User();
        currentUser.setId(1);
        currentUser.setUsername(username);

        User enemy = new User();
        enemy.setId(3);
        enemy.setUsername("enemyUser");

        List<Song> mockFavorites = List.of(new Song(301, "Enemy Song", "Artist X", 2021));

        Mockito.when(userService.getUserByUsername(username)).thenReturn(currentUser);
        Mockito.when(lyricalMatchService.findLyricalEnemy(currentUser)).thenReturn(enemy);
        Mockito.when(lyricalMatchService.findLyricalEnemy(enemy, currentUser)).thenReturn(currentUser);
        Mockito.when(favoritesService.getFavoriteSongs(enemy.getId())).thenReturn(mockFavorites);

        mockMvc.perform(get("/match/enemy").param("username", username))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mutual").value(true));
    }

}
