package edu.usc.csci310.project.controllers;

import edu.usc.csci310.project.models.Song;
import edu.usc.csci310.project.services.FavoritesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
public class FavoritesController {
    private final FavoritesService favoritesService;

    @Autowired
    public FavoritesController(FavoritesService favoritesService) {
        this.favoritesService = favoritesService;
    }

    @PostMapping
    public ResponseEntity<String> addFavoriteSong(@RequestParam int userID, @RequestParam int songID) {
        try {
            favoritesService.addFavoriteSong(userID, songID);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping
    public ResponseEntity<String> removeFavoriteSong(@RequestParam int userID, @RequestParam int songID) {
        try {
            favoritesService.removeFavoriteSong(userID, songID);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public ResponseEntity<List<Song>> getFavoriteSongs(@RequestParam int userID) {
        try{
            List<Song> favoritesList = favoritesService.getFavoriteSongs(userID);
            return new ResponseEntity<>(favoritesList, HttpStatus.OK);
        }
        catch(Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> isFavoriteSong(@RequestParam int songID, @RequestParam String username) {
        try {
            boolean isFavoritedSong = favoritesService.isFavoriteSong(songID, username);
            return new ResponseEntity<>(isFavoritedSong, HttpStatus.OK);
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/reorder")
    public ResponseEntity<String> reorderFavoriteSong(@RequestParam int songID, @RequestParam int userID, @RequestParam String direction) {
        try{
            if (!direction.equals("up") && !direction.equals("down")) {
                return new ResponseEntity<>("Invalid direction parameter. Must be 'up' or 'down' direction.", HttpStatus.BAD_REQUEST);
            }

            favoritesService.reorderFavoriteSong(userID, songID, direction);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        catch (Exception e)
        {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

