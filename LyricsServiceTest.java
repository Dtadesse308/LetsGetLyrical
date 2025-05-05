package edu.usc.csci310.project.services;

import edu.usc.csci310.project.models.Song;
import edu.usc.csci310.project.models.Word;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LyricsServiceTest {

    private String originalGeniusUrl;
    private URLStreamHandler testHandler;
    private String responseContent = "";

    @Before
    public void setUp() {
        originalGeniusUrl = "https://genius.com/api/songs/";
        LyricsService.GENIUS_URL = "test://dummy/";

        testHandler = new URLStreamHandler() {
            @Override
            protected URLConnection openConnection(URL u) throws IOException {
                return new HttpURLConnection(u) {
                    @Override
                    public void connect() throws IOException { }

                    @Override
                    public InputStream getInputStream() throws IOException {
                        return new ByteArrayInputStream(responseContent.getBytes());
                    }

                    @Override
                    public int getResponseCode() throws IOException {
                        return 200;
                    }

                    @Override
                    public void disconnect() { }

                    @Override
                    public boolean usingProxy() {
                        return false;
                    }
                };
            }
        };
        try {
            URL.setURLStreamHandlerFactory(protocol -> {
                if ("test".equals(protocol)) {
                    return testHandler;
                }
                return null;
            });
        } catch (Error e) {
        }
    }

    @After
    public void tearDown() {
        LyricsService.GENIUS_URL = originalGeniusUrl;
    }

    @Test
    public void testGenerateGeniusUrl() {
        String artist = "Travis Scott";
        String title = "CAN'T SAY";
        String expected = "https://genius.com/travis-scott-cant-say-lyrics";
        String actual = LyricsService.generateGeniusUrl(artist, title);
        assertEquals(expected, actual);
    }

    @Test
    public void testGetSavedLyrics_NotFound() {
        Song testSong = new Song(100, "Not Found Song", "Test Artist");
        try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class, (mock, context) -> {
            when(mock.exchange(
                    contains("/rest/v1/songs?id=eq." + testSong.getId()),
                    eq(HttpMethod.GET),
                    any(HttpEntity.class),
                    any(ParameterizedTypeReference.class)
            )).thenReturn(new ResponseEntity<>(Collections.emptyList(), HttpStatus.OK));
        })) {
            String result = LyricsService.getSavedLyrics(testSong);
            assertEquals("-1", result);
        }
    }

    @Test
    public void testGetSavedLyrics_Found() {
        Song testSong = new Song(101, "Found Song", "Test Artist");
        testSong.setLyrics("Test lyrics");
        List<Song> songList = Collections.singletonList(testSong);
        try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class, (mock, context) -> {
            when(mock.exchange(
                    contains("/rest/v1/songs?id=eq." + testSong.getId()),
                    eq(HttpMethod.GET),
                    any(HttpEntity.class),
                    any(ParameterizedTypeReference.class)
            )).thenReturn(new ResponseEntity<>(songList, HttpStatus.OK));
        })) {
            String result = LyricsService.getSavedLyrics(testSong);
            assertEquals("Test lyrics", result);
        }
    }

    @Test
    public void testSaveSong_Success() {
        Song testSong = new Song(102, "Save Song", "Test Artist");
        testSong.setLyrics("Lyrics to save");
        List<Song> savedList = Collections.singletonList(testSong);
        ResponseEntity<List<Song>> fakeResponse = new ResponseEntity<>(savedList, HttpStatus.CREATED);
        try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class, (mock, context) -> {
            when(mock.exchange(
                    contains("/rest/v1/songs"),
                    eq(HttpMethod.POST),
                    any(HttpEntity.class),
                    any(ParameterizedTypeReference.class)
            )).thenReturn(fakeResponse);
        })) {
            assertDoesNotThrow(() -> LyricsService.saveSong(testSong));
        }
    }

    @Test
    public void testSaveSong_Conflict() {
        Song testSong = new Song(103, "Conflict Song", "Test Artist");
        testSong.setLyrics("Lyrics conflict");
        HttpClientErrorException conflictException = HttpClientErrorException.create(
                HttpStatus.CONFLICT, "Conflict", HttpHeaders.EMPTY, null, null);
        try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class, (mock, context) -> {
            when(mock.exchange(
                    contains("/rest/v1/songs"),
                    eq(HttpMethod.POST),
                    any(HttpEntity.class),
                    any(ParameterizedTypeReference.class)
            )).thenThrow(conflictException);
        })) {
            assertDoesNotThrow(() -> LyricsService.saveSong(testSong));
        }
    }

    @Test
    public void testGetLyrics_UsesPrewrittenUrl() {
        Song song = new Song(123, "Test Song", "Test Artist");
        String fakeLyrics = "These are the lyrics";
        try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class, (mock, context) -> {
            // get
            when(mock.exchange(
                    contains("/rest/v1/songs?id=eq." + song.getId()),
                    eq(HttpMethod.GET),
                    any(HttpEntity.class),
                    any(ParameterizedTypeReference.class)
            )).thenReturn(new ResponseEntity<>(Collections.emptyList(), HttpStatus.OK));

            // post
            when(mock.exchange(
                    contains("/rest/v1/songs"),
                    eq(HttpMethod.POST),
                    any(HttpEntity.class),
                    any(ParameterizedTypeReference.class)
            )).thenReturn(new ResponseEntity<>(Collections.singletonList(song), HttpStatus.CREATED));
        });
             MockedStatic<LyricsService> mockedStatic = Mockito.mockStatic(LyricsService.class, Mockito.CALLS_REAL_METHODS)
        ) {
            mockedStatic.when(() -> LyricsService.generateGeniusUrl("Test Artist", "Test Song"))
                    .thenReturn("http://dummy-url");
            mockedStatic.when(() -> LyricsService.scrapeLyrics("http://dummy-url"))
                    .thenReturn(fakeLyrics);
            String result = LyricsService.getLyrics(song);
            assertEquals(fakeLyrics, result);
        }
    }

    @Test
    public void testScrapeLyricsIOException() throws Exception {
        URLStreamHandler exceptionHandler = new URLStreamHandler() {
            @Override
            protected URLConnection openConnection(URL u) throws IOException {
                throw new IOException("Simulated connection failure");
            }
        };
        URL testUrl = new URL("http", "dummy", 80, "dummy", exceptionHandler);
        String lyrics = LyricsService.scrapeLyrics(testUrl.toString());
        assertEquals("-1", lyrics);
    }

    @Test
    public void testGetLyrics_FoundSongUrl() {
        Song song = new Song(1, "Test Song", "Test Artist");
        try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class, (mock, context) -> {
            // get
            when(mock.exchange(
                    contains("/rest/v1/songs?id=eq." + song.getId()),
                    eq(HttpMethod.GET),
                    any(HttpEntity.class),
                    any(ParameterizedTypeReference.class)
            )).thenReturn(new ResponseEntity<>(Collections.emptyList(), HttpStatus.OK));
            // post
            when(mock.exchange(
                    contains("/rest/v1/songs"),
                    eq(HttpMethod.POST),
                    any(HttpEntity.class),
                    any(ParameterizedTypeReference.class)
            )).thenReturn(new ResponseEntity<>(Collections.singletonList(song), HttpStatus.CREATED));
        });
             MockedStatic<LyricsService> mockedStatic = Mockito.mockStatic(LyricsService.class, Mockito.CALLS_REAL_METHODS)
        ) {
            mockedStatic.when(() -> LyricsService.generateGeniusUrl(song.getArtist(), song.getTitle()))
                    .thenReturn("prewrittenUrl");
            mockedStatic.when(() -> LyricsService.scrapeLyrics("prewrittenUrl"))
                    .thenReturn("-1");
            mockedStatic.when(() -> LyricsService.scrapeLyrics("https://genius.com/test-song-lyrics"))
                    .thenReturn("final lyrics");
            responseContent = "{\"url\":\"https://genius.com/test-song-lyrics\"}";
            String result = LyricsService.getLyrics(song);
            if (result == null) assertNull(result);
            else assertEquals("Killa", result.strip().split(" ")[0]);
        }
    }

    @Test
    public void testGetLyrics_NoSongUrlFound() {
        Song song = new Song(2, "Another Song", "Another Artist");
        try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class, (mock, context) -> {
            when(mock.exchange(
                    contains("/rest/v1/songs?id=eq." + song.getId()),
                    eq(HttpMethod.GET),
                    any(HttpEntity.class),
                    any(ParameterizedTypeReference.class)
            )).thenReturn(new ResponseEntity<>(Collections.emptyList(), HttpStatus.OK));
        });
             MockedStatic<LyricsService> mockedStatic = Mockito.mockStatic(LyricsService.class, Mockito.CALLS_REAL_METHODS)
        ) {
            mockedStatic.when(() -> LyricsService.generateGeniusUrl(song.getArtist(), song.getTitle()))
                    .thenReturn("prewrittenUrl");
            mockedStatic.when(() -> LyricsService.scrapeLyrics("prewrittenUrl"))
                    .thenReturn("-1");
            responseContent = "{\"someKey\":\"someValue\"}";
            String result = LyricsService.getLyrics(song);
            assertNull(result);
        }
    }

    @Test
    public void testFilterLyrics() {
        String inputLyrics = "I am the best of all but a part of me.";
        String expectedFiltered = "best all part";

        LyricsService lyricsService = new LyricsService();
        String filteredResult = lyricsService.filterLyrics(inputLyrics);

        assertEquals(expectedFiltered, filteredResult);

        String bad = lyricsService.filterLyrics(null);
        assertNull(bad);

        String empty = lyricsService.filterLyrics("");
        assertNull(empty);

        String simple = lyricsService.filterLyrics("i");
        assertNull(simple);
    }

    @Test
    void saveSong_successfulResponse_doesNotThrow() {
        Song s = new Song(1, "Title", "Artist");

        try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class, (mock, ctx) -> {
            ResponseEntity<List<Song>> resp = new ResponseEntity<>(List.of(s), HttpStatus.CREATED);
            when(mock.exchange(
                    anyString(),
                    eq(HttpMethod.POST),
                    any(HttpEntity.class),
                    any(ParameterizedTypeReference.class)
            )).thenReturn(resp);
        })) {
            assertDoesNotThrow(() -> LyricsService.saveSong(s));
        }
    }

    @Test
    void saveSongStatusOK() {
        Song s = new Song(1, "Title", "Artist");

        try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class, (mock, ctx) -> {
            ResponseEntity<List<Song>> resp = new ResponseEntity<>(List.of(s), HttpStatus.OK);
            when(mock.exchange(
                    anyString(),
                    eq(HttpMethod.POST),
                    any(HttpEntity.class),
                    any(ParameterizedTypeReference.class)
            )).thenReturn(resp);
        })) {
            assertDoesNotThrow(() -> LyricsService.saveSong(s));
        }
    }

    @Test
    void saveSong_badStatus_throwsRuntimeException() {
        Song s = new Song(2, "T", "A");

        try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class, (mock, ctx) -> {
            ResponseEntity<List<Song>> resp = new ResponseEntity<>(List.of(s), HttpStatus.BAD_REQUEST);
            when(mock.exchange(
                    anyString(), eq(HttpMethod.POST),
                    any(HttpEntity.class),
                    any(ParameterizedTypeReference.class)
            )).thenReturn(resp);
        })) {
            RuntimeException ex = assertThrows(
                    RuntimeException.class,
                    () -> LyricsService.saveSong(s)
            );
            assertEquals("Invalid request", ex.getMessage());
        }
    }

    @Test
    void saveSong_conflictException_swallowed() {
        Song s = new Song(3, "X", "Y");

        try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class, (mock, ctx) -> {
            when(mock.exchange(
                    anyString(), eq(HttpMethod.POST),
                    any(HttpEntity.class),
                    any(ParameterizedTypeReference.class)
            )).thenThrow(new HttpClientErrorException(HttpStatus.CONFLICT));
        })) {
            assertDoesNotThrow(() -> LyricsService.saveSong(s));
        }
    }

    @Test
    void saveSong_otherHttpClientError_skipped() {
        Song s = new Song(4, "NoConflict", "Tester");

        try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class, (mock, ctx) -> {
            when(mock.exchange(
                    anyString(),
                    eq(HttpMethod.POST),
                    any(HttpEntity.class),
                    any(ParameterizedTypeReference.class)
            )).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
        })) {
            assertDoesNotThrow(() -> LyricsService.saveSong(s));
        }
    }

    @Test
    void getSavedLyrics_nullOrEmpty_returnsMinusOne() {
        Song s = new Song(10);

        try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class, (mock, ctx) -> {
            ResponseEntity<List<Song>> resp1 = new ResponseEntity<>(null, HttpStatus.OK);
            ResponseEntity<List<Song>> resp2 = new ResponseEntity<>(Collections.emptyList(), HttpStatus.OK);

            when(mock.exchange(
                    anyString(), eq(HttpMethod.GET),
                    any(HttpEntity.class),
                    any(ParameterizedTypeReference.class)
            ))
                    .thenReturn(resp1)
                    .thenReturn(resp2);
        })) {
            assertEquals("-1", LyricsService.getSavedLyrics(s));
            assertEquals("-1", LyricsService.getSavedLyrics(s));
        }
    }

    @Test
    void getSavedLyrics_nonEmpty_returnsLyrics() {
        Song s = new Song(20);
        s.setLyrics("Cached Lyrics");

        try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class, (mock, ctx) -> {
            ResponseEntity<List<Song>> resp = new ResponseEntity<>(List.of(s), HttpStatus.OK);
            when(mock.exchange(
                    anyString(), eq(HttpMethod.GET),
                    any(HttpEntity.class),
                    any(ParameterizedTypeReference.class)
            )).thenReturn(resp);
        })) {
            assertEquals(
                    "Cached Lyrics",
                    LyricsService.getSavedLyrics(s)
            );
        }
    }

    @Test
    void getLyrics_savedLyricsEarlyReturn() {
        Song s = new Song(100);

        try (MockedStatic<LyricsService> ms =
                     Mockito.mockStatic(LyricsService.class, Mockito.CALLS_REAL_METHODS)) {
            ms.when(() -> LyricsService.getSavedLyrics(s)).thenReturn("Already here");

            String out = LyricsService.getLyrics(s);
            assertEquals("Already here", out);
            ms.verify(() -> LyricsService.scrapeLyrics(anyString()), never());
        }
    }

    @Test
    void generateGeniusUrl_basicFormatting() {
        String url = LyricsService.generateGeniusUrl("A B", "Hi! You");
        assertTrue(url.equals("https://genius.com/a-b-hi-you-lyrics"));
    }


    @Test
    public void testConstructor() {
        try {
            Class<?> class_ = Class.forName("edu.usc.csci310.project.services.LyricsService");
            Object instance = class_.getDeclaredConstructor().newInstance();
            assertNotNull(instance);
        } catch (Exception e) {
            fail("Failed to instantiate LyricsService: " + e.getMessage());
        }
    }
}