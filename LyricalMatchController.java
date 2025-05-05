package edu.usc.csci310.project.controllers;

import edu.usc.csci310.project.models.Song;
import edu.usc.csci310.project.models.User;
import edu.usc.csci310.project.services.FavoritesService;
import edu.usc.csci310.project.services.LyricalMatchService;
import edu.usc.csci310.project.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/match")
public class LyricalMatchController {

    private final LyricalMatchService lyricalMatchService;
    private final UserService userService;
    private final FavoritesService favoritesService;

    @Autowired
    public LyricalMatchController(LyricalMatchService lyricalMatchService, UserService userService, FavoritesService favoritesService) {
        this.lyricalMatchService = lyricalMatchService;
        this.userService = userService;
        this.favoritesService = favoritesService;
    }

    @GetMapping("/soulmate")
    public ResponseEntity<Map<String, Object>> getLyricalSoulmate(@RequestParam String username) {
        User currentUser = userService.getUserByUsername(username);
        User soulmate = lyricalMatchService.findLyricalSoulmate(currentUser);

        boolean isMutual;
        if (soulmate != null) {
            User reverse = lyricalMatchService.findLyricalSoulmate(soulmate, currentUser);
            isMutual = reverse != null && reverse.getId().equals(currentUser.getId());
            List<Song> favorites = favoritesService.getFavoriteSongs(soulmate.getId());
            Map<String, Object> result = new HashMap<>();
            result.put("user", soulmate);
            result.put("favorites", favorites);
            result.put("mutual", isMutual);
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/enemy")
    public ResponseEntity<Map<String, Object>> getLyricalEnemy(@RequestParam String username) {
        User currentUser = userService.getUserByUsername(username);
        User enemy = lyricalMatchService.findLyricalEnemy(currentUser);

        boolean isMutual;
        if (enemy != null) {
            User reverse = lyricalMatchService.findLyricalEnemy(enemy, currentUser);
            isMutual = reverse != null && reverse.getId().equals(currentUser.getId());
            List<Song> favorites = favoritesService.getFavoriteSongs(enemy.getId());
            Map<String, Object> result = new HashMap<>();
            result.put("user", enemy);
            result.put("favorites", favorites);
            result.put("mutual", isMutual);
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
