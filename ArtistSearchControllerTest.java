package edu.usc.csci310.project.controllers;

import edu.usc.csci310.project.models.Artist;
import edu.usc.csci310.project.models.Song;
import edu.usc.csci310.project.models.Word;
import edu.usc.csci310.project.services.ApiService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
@WebMvcTest(ArtistSearchController.class)
public class ArtistSearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApiService geniusApiService;

    // Test for GET /api/search/songs?artistID=&artistName=&num=
    @Test
    public void testSearchSongsByArtist() throws Exception {
        Integer artistID = 1;
        String artistName = "Taylor Swift";
        int num = 2;
        List<Song> expectedSongs = Arrays.asList(
                new Song(1, "Blank Space", "Taylor Swift"),
                new Song(2, "Shake It Off", "Taylor Swift")
        );

        when(geniusApiService.getSongsByArtist(artistID, artistName, num)).thenReturn(expectedSongs);

        mockMvc.perform(get("/api/search/songs")
                        .param("artistID", artistID.toString())
                        .param("artistName", artistName)
                        .param("num", String.valueOf(num))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Blank Space"))
                .andExpect(jsonPath("$[0].artist").value("Taylor Swift"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].title").value("Shake It Off"))
                .andExpect(jsonPath("$[1].artist").value("Taylor Swift"));
    }

    @Test
    public void testSearchSongsByArtist_EmptyResult() throws Exception {
        Integer artistID = 1;
        String artistName = "NonExistentArtist";
        int num = 2;
        List<Song> expectedSongs = Collections.emptyList();

        when(geniusApiService.getSongsByArtist(artistID, artistName, num)).thenReturn(expectedSongs);

        mockMvc.perform(get("/api/search/songs")
                        .param("artistID", artistID.toString())
                        .param("artistName", artistName)
                        .param("num", String.valueOf(num))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    public void testSearchArtistsByName() throws Exception {
        String artistName = "Taylor Swift";
        List<Artist> expectedArtists = Arrays.asList(
                new Artist(1, "Taylor Swift"),
                new Artist(2, "Taylor Swift")
        );

        when(geniusApiService.getArtists(artistName)).thenReturn(expectedArtists);

        mockMvc.perform(get("/api/search/artists")
                        .param("name", artistName)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Taylor Swift"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Taylor Swift"));
    }

    @Test
    public void testGenerateWordCloud() throws Exception {
        Integer artistID = 1;
        String artistName = "Taylor Swift";
        int num = 2;

        Map<Song, Integer> occurrences = new HashMap<>();
        occurrences.put(new Song(1), 3);
        occurrences.put(new Song(2), 2);
        Word word = new Word("love", occurrences);
        List<Word> expectedCloud = Collections.singletonList(word);

        List<Song> songs = Arrays.asList(
                new Song(1, "Song One", "Taylor Swift"),
                new Song(2, "Song Two", "Taylor Swift")
        );
        when(geniusApiService.getSongsByArtist(artistID, artistName, num)).thenReturn(songs);
        when(geniusApiService.generateWordCloud(songs)).thenReturn(expectedCloud);

        mockMvc.perform(get("/api/search/generate")
                        .param("artistID", artistID.toString())
                        .param("artistName", artistName)
                        .param("num", String.valueOf(num))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].word").value("love"))
                .andExpect(jsonPath("$[0].songOccurrences[*].count", containsInAnyOrder(3, 2)));
    }

    @Test
    void generateWordCloudFromCustomSongs() throws Exception {
        // expected word cloud? unsure if necessary
        Map<Song, Integer> occurrences1 = new HashMap<>();
        occurrences1.put(new Song(1), 5);
        occurrences1.put(new Song(3), 2);

        Map<Song, Integer> occurrences2 = new HashMap<>();
        occurrences2.put(new Song(2), 4);
        occurrences2.put(new Song(3), 3);

        List<Word> expectedCloud = Arrays.asList(
                new Word("love", occurrences1),
                new Word("time", occurrences2)
        );

        when(geniusApiService.generateWordCloud(anyList())).thenReturn(expectedCloud);
        String songsJson = "[" +
                "{\"id\":1,\"title\":\"Fortnight\",\"artist\":\"Taylor Swift\"}," +
                "{\"id\":2,\"title\":\"Cardigan\",\"artist\":\"Taylor Swift\"}," +
                "{\"id\":3,\"title\":\"Anti-Hero\",\"artist\":\"Taylor Swift\"}" +
                "]";

        mockMvc.perform(post("/api/search/generate/custom")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(songsJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].word").value("love"))
                .andExpect(jsonPath("$[0].songOccurrences[*].id", containsInAnyOrder(1, 3)))
                .andExpect(jsonPath("$[0].songOccurrences[*].count", containsInAnyOrder(5, 2)))
                .andExpect(jsonPath("$[1].word").value("time"))
                .andExpect(jsonPath("$[1].songOccurrences[*].id", containsInAnyOrder(2, 3)))
                .andExpect(jsonPath("$[1].songOccurrences[*].count", containsInAnyOrder(4, 3)));
    }
}