package edu.usc.csci310.project.services;

import edu.usc.csci310.project.models.Artist;
import edu.usc.csci310.project.models.Song;
import edu.usc.csci310.project.models.Word;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ApiServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private ApiService apiService;
    private final String API_KEY = "test-api-key";
    private final LemmatizerService lemmatizerService = new LemmatizerService();

    @BeforeEach
    void setUp() {
        apiService = new ApiService(restTemplate, API_KEY, lemmatizerService);
    }

    @Test
    void testApiService_AllMethods() {
        Map<String, Object> responseBody = new HashMap<>();
        Map<String, Object> responseData = new HashMap<>();

        Integer artistID = 1;
        String artistName = "Artist";
        int num = 2;

        List<Map<String, Object>> songList = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            Map<String, Object> song = new HashMap<>();
            song.put("id", 100 + i);
            song.put("title", "Song " + i);
            songList.add(song);
        }

        responseData.put("songs", songList);
        responseBody.put("response", responseData);

        ResponseEntity<Map> successResponse = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.exchange(contains("/artists/"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(successResponse);

        List<Song> songs = apiService.getSongsByArtist(artistID, artistName, num);
        assertEquals(2, songs.size());

        List<Map<String, Object>> emptySongList = new ArrayList<>();
        Map<String, Object> emptyResponseData = new HashMap<>();
        Map<String, Object> emptyResponseBody = new HashMap<>();

        emptyResponseData.put("songs", emptySongList);
        emptyResponseBody.put("response", emptyResponseData);

        when(restTemplate.exchange(contains("/artists/"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(emptyResponseBody, HttpStatus.OK));

        songs = apiService.getSongsByArtist(artistID, artistName, num);
        assertTrue(songs.isEmpty());

        Map<String, Object> nullSongListData = new HashMap<>();
        Map<String, Object> nullSongListBody = new HashMap<>();

        nullSongListData.put("songs", null);
        nullSongListBody.put("response", nullSongListData);

        when(restTemplate.exchange(contains("/artists/"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(nullSongListBody, HttpStatus.OK));

        songs = apiService.getSongsByArtist(artistID, artistName, num);
        assertTrue(songs.isEmpty());

        List<Map<String, Object>> nullSongDataList = new ArrayList<>();
        Map<String, Object> nullSongData = new HashMap<>();

        nullSongData.put("id", null);
        nullSongData.put("title", null);
        nullSongDataList.add(nullSongData);

        Map<String, Object> nullDataResponse = new HashMap<>();
        Map<String, Object> nullDataResponseBody = new HashMap<>();

        nullDataResponse.put("songs", nullSongDataList);
        nullDataResponseBody.put("response", nullDataResponse);

        when(restTemplate.exchange(contains("/artists/"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(nullDataResponseBody, HttpStatus.OK));

        songs = apiService.getSongsByArtist(artistID, artistName, num);
        assertTrue(songs.isEmpty());

        when(restTemplate.exchange(contains("/artists/"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new RestClientException("API error"));
        songs = apiService.getSongsByArtist(artistID, artistName, num);
        assertTrue(songs.isEmpty());

        Song song1 = new Song(101, "Song One", "Artist A");
        Song song2 = new Song(102, "Song Two", "Artist A");
        List<Song> songObjects = Arrays.asList(song1, song2);

        try (MockedStatic<LyricsService> mockedLyrics = mockStatic(LyricsService.class)) {
            mockedLyrics.when(() -> LyricsService.getLyrics(song1))
                    .thenReturn("Hello world");
            mockedLyrics.when(() -> LyricsService.getLyrics(song2))
                    .thenReturn("World of code");

            mockedLyrics.when(() -> LyricsService.filterLyrics(anyString()))
                    .thenCallRealMethod();

            List<Word> wordCloud = apiService.generateWordCloud(songObjects);
            assertFalse(wordCloud.isEmpty());

            mockedLyrics.when(() -> LyricsService.getLyrics(any(Song.class)))
                    .thenReturn("");
            wordCloud = apiService.generateWordCloud(songObjects);
            assertTrue(wordCloud.isEmpty());
        }

        List<Map<String, Object>> hits = new ArrayList<>();

        Map<String, Object> hit1 = new HashMap<>();
        Map<String, Object> result1 = new HashMap<>();
        Map<String, Object> primaryArtist1 = new HashMap<>();
        primaryArtist1.put("id", 1);
        primaryArtist1.put("name", "Artist 1");
        result1.put("primary_artist", primaryArtist1);
        hit1.put("result", result1);
        hits.add(hit1);

        Map<String, Object> hit2 = new HashMap<>();
        Map<String, Object> result2 = new HashMap<>();
        Map<String, Object> primaryArtist2 = new HashMap<>();
        primaryArtist2.put("id", 1);
        primaryArtist2.put("name", "Artist 1");
        result2.put("primary_artist", primaryArtist2);
        hit2.put("result", result2);
        hits.add(hit2);

        Map<String, Object> hit3 = new HashMap<>();
        hit3.put("result", null);
        hits.add(hit3);

        Map<String, Object> hit4 = new HashMap<>();
        Map<String, Object> result4 = new HashMap<>();
        result4.put("primary_artist", null);
        hit4.put("result", result4);
        hits.add(hit4);

        responseData = new HashMap<>();
        responseData.put("hits", hits);
        responseBody = new HashMap<>();
        responseBody.put("response", responseData);

        when(restTemplate.exchange(contains("/search"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));

        List<Artist> artists = apiService.getArtists("Test Artist");
        assertEquals(1, artists.size());

        when(restTemplate.exchange(contains("/search"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.OK));
        artists = apiService.getArtists("Test Artist");
        assertTrue(artists.isEmpty());

        when(restTemplate.exchange(contains("/search"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new RestClientException("API error"));
        artists = apiService.getArtists("Test Artist");
        assertTrue(artists.isEmpty());

        String specialCharArtist = "Test Artist & Special";
        when(restTemplate.exchange(contains("/search"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));
        artists = apiService.getArtists(specialCharArtist);
        assertNotNull(artists);
    }

    @Test
    void getSongsByArtist_whenDatesNotNull_setsYear() {
        Map<String,Object> dates = new HashMap<>();
        dates.put("year", 2020);

        Map<String,Object> songData = new HashMap<>();
        songData.put("id", 123);
        songData.put("title", "Test Song");
        songData.put("primary_artist_names", "Test Artist");
        songData.put("release_date_components", dates);

        List<Map<String,Object>> songsList = List.of(songData);
        Map<String,Object> responseData = Map.of("songs", songsList);
        Map<String,Object> responseBody = Map.of("response", responseData);

        ResponseEntity<Map> fakeResponse =
                new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(restTemplate.exchange(
                eq("https://api.genius.com/artists/1/songs?sort=popularity"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(fakeResponse);

        List<Song> result = apiService.getSongsByArtist(1, "ignored", 10);

        assertEquals(1, result.size());
        Song s = result.get(0);
        assertEquals(123, s.getId());
        assertEquals("Test Song", s.getTitle());
        assertEquals("Test Artist", s.getArtist());
        assertEquals(2020, s.getYear());
    }

    @Test
    void generateWordCloud_skipsSongsWithLyricsError() {
        Song skip = new Song(1, "Skip", "A");
        Song ok = new Song(2, "OK",   "B");
        List<Song> songs = List.of(skip, ok);

        try (MockedStatic<LyricsService> m = Mockito.mockStatic(LyricsService.class)) {
            m.when(() -> LyricsService.getLyrics(skip)).thenReturn("-1");
            m.when(() -> LyricsService.getLyrics(ok)).thenReturn("Hello hello world");
            m.when(() -> LyricsService.filterLyrics("Hello hello world"))
                    .thenReturn("Hello hello world");

            List<Word> cloud = apiService.generateWordCloud(songs);

            assertEquals(2, cloud.size());

            Map<String,Word> byWord = new HashMap<>();
            for (Word w : cloud) byWord.put(w.getWord(), w);

            assertTrue(byWord.containsKey("hello"));
            assertEquals(2,
                    byWord.get("hello")
                            .getSongOccurrences()
                            .get(ok)
                            .intValue()
            );

            assertTrue(byWord.containsKey("world"));
            assertEquals(1,
                    byWord.get("world")
                            .getSongOccurrences()
                            .get(ok)
                            .intValue()
            );
        }
    }

    @Test
    public void testGetSongsByArtist_ResponseBodyNullAndInvalidSongData() {
        Integer artistID = 1;
        String artistName = "Test Artist";
        int num = 2;

        when(restTemplate.exchange(contains("/artists/"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.OK));
        List<Song> songs = apiService.getSongsByArtist(artistID, artistName, num);
        assertTrue(songs.isEmpty(), "Expected no songs when response body is null");

        // both title and id bad
        Map<String, Object> responseBody = new HashMap<>();
        Map<String, Object> responseData = new HashMap<>();
        List<Map<String, Object>> songList = new ArrayList<>();
        Map<String, Object> invalidSongData = new HashMap<>();
        invalidSongData.put("id", null);
        invalidSongData.put("title", null);
        songList.add(invalidSongData);

        responseData.put("songs", songList);
        responseBody.put("response", responseData);

        when(restTemplate.exchange(contains("/artists/"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));
        songs = apiService.getSongsByArtist(artistID, artistName, num);
        assertTrue(songs.isEmpty());

        // just id bad
        responseBody = new HashMap<>();
        responseData = new HashMap<>();
        songList = new ArrayList<>();
        invalidSongData = new HashMap<>();
        invalidSongData.put("id", null);
        invalidSongData.put("title", "abc");
        songList.add(invalidSongData);
        responseData.put("songs", songList);
        responseBody.put("response", responseData);

        when(restTemplate.exchange(contains("/artists/"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));
        songs = apiService.getSongsByArtist(artistID, artistName, num);
        assertTrue(songs.isEmpty());

        // just title bad
        responseBody = new HashMap<>();
        responseData = new HashMap<>();
        songList = new ArrayList<>();
        invalidSongData = new HashMap<>();
        invalidSongData.put("id", 1);
        invalidSongData.put("title", null);
        songList.add(invalidSongData);
        responseData.put("songs", songList);
        responseBody.put("response", responseData);

        when(restTemplate.exchange(contains("/artists/"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));
        songs = apiService.getSongsByArtist(artistID, artistName, num);
        assertTrue(songs.isEmpty());
    }
}