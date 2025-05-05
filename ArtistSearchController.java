package edu.usc.csci310.project.controllers;

import edu.usc.csci310.project.models.Artist;
import edu.usc.csci310.project.models.Song;
import edu.usc.csci310.project.models.Word;
import edu.usc.csci310.project.services.ApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
public class ArtistSearchController {

    private final ApiService geniusApiService;

    @Autowired
    public ArtistSearchController(ApiService geniusApiService) {
        this.geniusApiService = geniusApiService;
    }

    @GetMapping("/artists")
    public ResponseEntity<List<Artist>> searchArtistsByName(@RequestParam String name) {
        List<Artist> artists = geniusApiService.getArtists(name);
        return ResponseEntity.ok(artists);
    }

    @GetMapping("/songs")
    public ResponseEntity<List<Song>> searchSongsByArtist(@RequestParam Integer artistID, @RequestParam String artistName, @RequestParam int num) {
        List<Song> songs = geniusApiService.getSongsByArtist(artistID, artistName, num);
        return ResponseEntity.ok(songs);
    }

    @GetMapping("/generate")
    public ResponseEntity<List<Word>> generateWordCloud(@RequestParam Integer artistID, @RequestParam String artistName, @RequestParam int num) {
        List<Song> songs = geniusApiService.getSongsByArtist(artistID, artistName, num);
        List<Word> cloud = geniusApiService.generateWordCloud(songs);
        return ResponseEntity.ok(cloud);
    }

    @PostMapping("/generate/custom")
    public ResponseEntity<List<Word>> generateWordCloudFromCustomSongs(@RequestBody List<Song> songs) {
        List<Word> cloud = geniusApiService.generateWordCloud(songs);
        return ResponseEntity.ok(cloud);
    }
}