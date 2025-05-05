package edu.usc.csci310.project.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.usc.csci310.project.models.Song;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class LyricsService {

    public static String GENIUS_URL = "https://genius.com/api/songs/";
    private static final String SUPABASE_URL = "https://cqqwwzmgbeckmdotlwdx.supabase.co";
    private static String SUPABASE_API_KEY;

    @Value("${SUPABASE_API_KEY}")
    private String supabaseApiKey;

    @PostConstruct
    private void init() {
        SUPABASE_API_KEY = supabaseApiKey;
    }

    public static void saveSong(Song song) {
        System.out.println("Saving song: " + song.getTitle() + " (" + song.getId() + ") by " + song.getArtist() + " in " + song.getYear());
        RestTemplate restTemplate = new RestTemplate();
        String url = SUPABASE_URL + "/rest/v1/songs";

        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", SUPABASE_API_KEY);
        headers.set("Authorization", "Bearer " + SUPABASE_API_KEY);
        headers.set("Content-Type", "application/json");
        headers.set("Prefer", "return=representation");

        // Remove favoritedByUsers
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> songMap = mapper.convertValue(song, new TypeReference<Map<String, Object>>() {});
        songMap.remove("favoritedByUsers");
        songMap.remove("position");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(songMap, headers);
        try {
            ResponseEntity<List<Song>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<>() {}
            );
            List<Song> songs = response.getBody();
            System.out.println("good");
            if (response.getStatusCode() == HttpStatus.CREATED || response.getStatusCode() == HttpStatus.OK) {
                Song result = songs.get(0);
                System.out.println("Saved Song: " + result.getTitle() + " (" + result.getId() + ") by " + result.getArtist() + " in " + result.getYear());
            }
            else {
                throw new RuntimeException("Invalid request");
            }
        }
        catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.CONFLICT) {
                System.out.println("Song already exists");
            }
            System.out.println("bad");
        }
    }

    public static String getSavedLyrics(Song song) {
        RestTemplate restTemplate = new RestTemplate();
        String url = SUPABASE_URL + "/rest/v1/songs?id=eq." + song.getId();

        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", SUPABASE_API_KEY);
        headers.set("Authorization", "Bearer " + SUPABASE_API_KEY);
        headers.set("Content-Type", "application/json");
        headers.set("Prefer", "return=representation");

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<List<Song>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {}
        );
        List<Song> songs = response.getBody();

        if (songs == null || songs.isEmpty()) return "-1";
        else {
            System.out.println("Got lyrics from database for song: " + songs.get(0).getTitle());
            return songs.get(0).getLyrics();
        }
    }

    public static String getLyrics(Song song) {
        try {
            // first: check if song in database
            String lyrics = getSavedLyrics(song);
            if (!lyrics.equals("-1")) return lyrics;

            // second: try with pre-written url
            lyrics = scrapeLyrics(generateGeniusUrl(song.getArtist(), song.getTitle()));
            if (!lyrics.equals("-1")) {
                song.setLyrics(lyrics);
                saveSong(song);
                return lyrics;
            }

            // third: didn't work, just do regular process
            String searchUrl = GENIUS_URL + song.getId();
            URL url = new URL(searchUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            Pattern pattern = Pattern.compile("\"url\":\"(https://genius.com/[^\"]+)\"");
            Matcher matcher = pattern.matcher(response.toString());

            matcher.find();
            String songUrl = matcher.group(1).replace("\\", "");
            System.out.println("Found song URL: " + songUrl);

            lyrics = scrapeLyrics(songUrl);
            song.setLyrics(lyrics);
            saveSong(song);
            return lyrics;
        } catch (IOException e) {
            System.out.println("Error searching for song: " + e.getMessage());
            return null;
        }
    }

    public static String generateGeniusUrl(String artist, String title) {
        String formattedArtist = artist.toLowerCase();
        String formattedTitle = title.toLowerCase();

        formattedArtist = formattedArtist.replaceAll("[^a-z0-9\\s]", "");
        formattedTitle = formattedTitle.replaceAll("[^a-z0-9\\s]", "");

        formattedArtist = formattedArtist.trim().replaceAll("\\s+", "-");
        formattedTitle = formattedTitle.trim().replaceAll("\\s+", "-");

        return "https://genius.com/" + formattedArtist + "-" + formattedTitle + "-lyrics";
    }

    public static String scrapeLyrics(String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            String html = response.toString();
            int startIndex = html.indexOf("<div data-lyrics-container=\"true\"");
            int endIndex = html.indexOf("<div class=\"LyricsFooter", startIndex);
            String targetHtml = html.substring(startIndex, endIndex);
            String cleaned = targetHtml.replaceAll("<[^>]+>", "\n");

            // from the start
            int furtherStartIndex = cleaned.indexOf("[");
            cleaned = cleaned.substring(furtherStartIndex);

            // remove [] groups
            cleaned = cleaned.replaceAll("\\[[^\\]]+\\]", "");

            String lyrics = cleaned.replaceAll("&#x27;","'")
                    .replaceAll("&amp;", "&")
                    .replaceAll("&lt;", "<")
                    .replaceAll("&gt;", ">")
                    .replaceAll("&quot;", "\"")
                    .replaceAll("&#39;", "'")
                    .replaceAll("&nbsp;", " ");// there's still more weird signs

            //System.out.println("Lyrics: " + lyrics);
            return lyrics;

        } catch (Exception e) {
            return "-1";
        }
    }

    public static String filterLyrics(String lyrics) {
        if (lyrics == null || lyrics.isEmpty()) return null;

        Set<String> bad = new HashSet<>(Arrays.asList(
                "a", "an", "the", "and", "or", "but", "if", "in", "on", "of",
                "to", "for", "by", "with", "i", "you", "he", "she", "it", "we",
                "they", "me", "him", "her", "them", "my", "your", "his", "her",
                "its", "our", "their", "this", "that", "these", "those", "at",
                "from", "as", "is", "are", "was", "were", "ayy", "like", "yeah",
                "uh", "woo", "ok", "okay", "alright", "ooh", "oh", "whoa", "so",
                "because", "don"
        ));

        String[] words = lyrics.toLowerCase().split("\\W+");
        StringBuilder filteredLyrics = new StringBuilder();

        for (String word : words) {
            if (word.length() > 2 && !bad.contains(word)) {
                filteredLyrics.append(word).append(" ");
            }
        }

        String filtered = filteredLyrics.toString().trim();
        if (filtered.isEmpty()) return null;
        return filtered;
    }
}