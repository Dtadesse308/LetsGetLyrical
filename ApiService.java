package edu.usc.csci310.project.services;

import edu.usc.csci310.project.models.Artist;
import edu.usc.csci310.project.models.Song;
import edu.usc.csci310.project.models.Word;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

@Service
public class ApiService {
    private final LemmatizerService lemmatizerService;
    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String baseUrl = "https://api.genius.com";

    public ApiService(RestTemplate restTemplate, @Value("${GENIUS_API_KEY}") String apiKey, LemmatizerService lemmatizerService) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.lemmatizerService = lemmatizerService;
    }

    public List<Song> getSongsByArtist(Integer artistID, String artistName, int num) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String requestUrl = baseUrl + "/artists/" + artistID + "/songs?sort=popularity";
        List<Song> songs = new ArrayList<>();
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    requestUrl,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null) return songs;

            Map<String, Object> responseData = (Map<String, Object>) responseBody.get("response");
            List<Map<String, Object>> songList = (List<Map<String, Object>>) responseData.get("songs");

            if (songList == null || songList.isEmpty()) {
                System.out.println("No songs found for artist " + artistName);
                return songs;
            }

            int count = 0;
            for (Map<String, Object> songData : songList) {
                Integer idNum = (Integer) songData.get("id");
                String title = (String) songData.get("title");
                String artist = (String) songData.get("primary_artist_names");

                Map<String, Object> dates = (Map<String, Object>) songData.get("release_date_components");
                Integer year = null;
                if (dates != null) year = (Integer) dates.get("year");

                if (idNum != null && title != null) {

                    Song song = new Song(idNum, title, artist, year);
                    songs.add(song);
                    System.out.println("Added song: " + song.getTitle() + " (" + song.getId() + ")");

                    count++;
                    if (count == num) break;
                }
            }
        }
        catch (Exception e) {
            System.err.println("API error: " + e.getMessage());
            e.printStackTrace();
        }

        return songs;
    }

    public List<Word> generateWordCloud(List<Song> songs) {
        System.out.println("Generating wordcloud");

        Map<String, Word> words = new HashMap<>();

        for (Song song : songs) {
            System.out.println("Song: " + song.getTitle() + " (" + song.getId() + ")");
            String lyrics = LyricsService.getLyrics(song);
            if (lyrics.equals("-1")) {
                System.out.println("Error with lyrics for song: " + song.getTitle());
                continue;
            }
            System.out.println("Got lyrics!");

            // Filter lyrics for basic words
            song.setLyrics(lyrics);
            lyrics = LyricsService.filterLyrics(lyrics);
            System.out.println("Lyrics updated: " + lyrics);
            if (lyrics == null) continue;

            // For each unique word in the lyrics, create a Word(word, song.getID(), occurrences) and add it to wordCloud
            Map<String, Integer> songWordCounts = new HashMap<>();
            StanfordCoreNLP pipeline = lemmatizerService.getPipeline();
            CoreDocument doc = new CoreDocument(lyrics);
            pipeline.annotate(doc);
            // Iterate over tokens and pull out lemmas
            for (CoreLabel token : doc.tokens()) {
                String lemma = token.lemma().toLowerCase();
                if (lemma.equals("wan")) lemma = "want";
                songWordCounts.put(lemma, songWordCounts.getOrDefault(lemma, 0) + 1);
            }

            for (Map.Entry<String, Integer> entry : songWordCounts.entrySet()) {
                String wordStr = entry.getKey();
                int count = entry.getValue();

                if (words.containsKey(wordStr)) {
                    words.get(wordStr).addSongOccurrence(song, count);
                } else {
                    Map<Song, Integer> occurrences = new HashMap<>();
                    occurrences.put(song, count);
                    Word newWord = new Word(wordStr, occurrences);
                    words.put(wordStr, newWord);
                }
            }
        }

        return new ArrayList<>(words.values());
    }

    public List<Artist> getArtists(String artistName) {
        String encodedArtist = URLEncoder.encode(artistName, StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String requestUrl = baseUrl + "/search?q=" + encodedArtist;
        List<Artist> artists = new ArrayList<>();
        Set<Integer> seen = new HashSet<>();
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    requestUrl,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null) return artists;

            Map<String, Object> responseData = (Map<String, Object>) responseBody.get("response");
            List<Map<String, Object>> hits = (List<Map<String, Object>>) responseData.get("hits");

            for (Map<String, Object> hit : hits) {
                Map<String, Object> result = (Map<String, Object>) hit.get("result");
                if (result == null) continue;

                Map<String, Object> primaryArtist = (Map<String, Object>) result.get("primary_artist");
                if (primaryArtist == null) continue;

                String newArtistName = (String) primaryArtist.get("name");
                Integer newArtistID = (Integer) primaryArtist.get("id");
                String imageUrl = (String) primaryArtist.get("image_url"); // Get image URL from API response

                if (!seen.contains(newArtistID)) {
                    seen.add(newArtistID);
                    Artist artist = new Artist(newArtistID, newArtistName, imageUrl); // Pass image URL to constructor
                    artists.add(artist);
                }
            }
        } catch (Exception e) {
            System.err.println("API error: " + e.getMessage());
            e.printStackTrace();
        }

        return artists;
    }
}