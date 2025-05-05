package edu.usc.csci310.project.services;

import edu.usc.csci310.project.models.Song;
import edu.usc.csci310.project.models.User;
import edu.usc.csci310.project.models.Word;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LyricalMatchServiceTest {

    @Mock private UserService userService;
    @Mock private FavoritesService favoritesService;
    @Mock private ApiService apiService;

    @Spy
    @InjectMocks
    private LyricalMatchService lyricalMatchService;

    private User currentUser;
    private User candidate1;
    private User candidate2;

    @BeforeEach
    void setUp() {
        currentUser = new User(1, "current",   "pw", false);
        candidate1  = new User(2, "candidate1","pw", false);
        candidate2  = new User(3, "candidate2","pw", false);
    }

    @Test
    void testFindLyricalSoulmate() {
        when(userService.getPublicUsers())
                .thenReturn(Arrays.asList(currentUser, candidate1, candidate2));

        // Stub favoritesService
        List<Song> favCurrent = List.of(new Song(1), new Song(2));
        List<Song> fav1       = List.of(new Song(2), new Song(3));
        List<Song> fav2       = List.of(new Song(4));
        when(favoritesService.getFavoriteSongs(currentUser.getId())).thenReturn(favCurrent);
        when(favoritesService.getFavoriteSongs(candidate1 .getId())).thenReturn(fav1);
        when(favoritesService.getFavoriteSongs(candidate2 .getId())).thenReturn(fav2);

        List<Word> wcCurrent = List.of(
                new Word("Rap",  new HashMap<>()),
                new Word("music", new HashMap<>())
        );
        List<Word> wc1 = List.of(
                new Word("Rap",  new HashMap<>()),
                new Word("music", new HashMap<>()),
                new Word("rock",  new HashMap<>())
        );
        List<Word> wc2 = List.of(
                new Word("jazz",  new HashMap<>()),
                new Word("blues", new HashMap<>())
        );
        when(apiService.generateWordCloud(favCurrent)).thenReturn(wcCurrent);
        when(apiService.generateWordCloud(fav1)).thenReturn(wc1);
        when(apiService.generateWordCloud(fav2)).thenReturn(wc2);

        User soulmate = lyricalMatchService.findLyricalSoulmate(currentUser);
        assertNotNull(soulmate);
        assertEquals(candidate1.getId(), soulmate.getId());
    }

    @Test
    void testFindLyricalEnemy() {
        when(userService.getPublicUsers())
                .thenReturn(Arrays.asList(currentUser, candidate1, candidate2));

        List<Song> favCurrent = List.of(new Song(1), new Song(2));
        List<Song> fav1       = List.of(new Song(2), new Song(3));
        List<Song> fav2       = List.of(new Song(4));
        when(favoritesService.getFavoriteSongs(currentUser.getId())).thenReturn(favCurrent);
        when(favoritesService.getFavoriteSongs(candidate1 .getId())).thenReturn(fav1);
        when(favoritesService.getFavoriteSongs(candidate2 .getId())).thenReturn(fav2);

        List<Word> wcCurrent = List.of(
                new Word("Rap",  new HashMap<>()),
                new Word("music", new HashMap<>())
        );
        List<Word> wc1 = List.of(
                new Word("Rap",  new HashMap<>()),
                new Word("music", new HashMap<>()),
                new Word("rock",  new HashMap<>())
        );
        List<Word> wc2 = List.of(
                new Word("jazz",  new HashMap<>()),
                new Word("blues", new HashMap<>())
        );
        when(apiService.generateWordCloud(favCurrent)).thenReturn(wcCurrent);
        when(apiService.generateWordCloud(fav1))      .thenReturn(wc1);
        when(apiService.generateWordCloud(fav2))      .thenReturn(wc2);

        User enemy = lyricalMatchService.findLyricalEnemy(currentUser);
        assertNotNull(enemy);
        assertEquals(candidate2.getId(), enemy.getId());
    }

    @Test
    void testNoOtherPublicUsers() {
        when(userService.getPublicUsers())
                .thenReturn(Collections.singletonList(currentUser));

        User soulmate = lyricalMatchService.findLyricalSoulmate(currentUser);
        User enemy    = lyricalMatchService.findLyricalEnemy(currentUser);

        assertNull(soulmate);
        assertNull(enemy);
    }

    @Test
    void testFindLyricalSoulmateWhenCandidateWordCloudEmpty() {
        when(userService.getPublicUsers())
                .thenReturn(Arrays.asList(currentUser, candidate1));

        // currentUser has favorites, candidate has none
        List<Song> favCurrent = List.of(new Song(1));
        when(favoritesService.getFavoriteSongs(currentUser.getId())).thenReturn(favCurrent);
        when(favoritesService.getFavoriteSongs(candidate1 .getId())).thenReturn(Collections.emptyList());

        List<Word> wcCurrent = List.of(
                new Word("Rap",  new HashMap<>()),
                new Word("music", new HashMap<>())
        );
        when(apiService.generateWordCloud(favCurrent)).thenReturn(wcCurrent);

        User soulmate = lyricalMatchService.findLyricalSoulmate(currentUser);
        assertNull(soulmate);
    }

    @Test
    void testFindLyricalSoulmateWhenCurrentWordCloudEmpty() {
        when(favoritesService.getFavoriteSongs(currentUser.getId())).thenReturn(Collections.emptyList());
        when(apiService.generateWordCloud(Collections.emptyList())).thenReturn(Collections.emptyList());
        User soulmate = lyricalMatchService.findLyricalSoulmate(currentUser);
        assertNull(soulmate);
    }

    @Test
    void testGetWordCloudForUserReturnsEmptyListWhenNoFavorites() {
        when(favoritesService.getFavoriteSongs(currentUser.getId())).thenReturn(Collections.emptyList());
        when(apiService.generateWordCloud(Collections.emptyList())).thenReturn(Collections.emptyList());

        List<Word> cloud = lyricalMatchService.getWordCloudForUser(currentUser);
        assertNotNull(cloud);
        assertTrue(cloud.isEmpty());
    }

    @Test
    void testFindLyricalEnemy_DoesNotUpdateWhenSimilarityHigher() {
        when(userService.getPublicUsers())
                .thenReturn(Arrays.asList(currentUser, candidate1, candidate2));

        when(favoritesService.getFavoriteSongs(candidate1.getId()))
                .thenReturn(List.of(new Song()));
        when(favoritesService.getFavoriteSongs(candidate2.getId()))
                .thenReturn(List.of(new Song()));


        // candidate1 is less similar than current and candidate2 is more similar
        List<Word> wcCurrent = List.of(
                new Word("common1", Map.of()),
                new Word("common2", Map.of())
        );
        List<Word> wc1 = List.of(
                new Word("common1", Map.of())
        );
        List<Word> wc2 = List.of(
                new Word("common1", Map.of()),
                new Word("common2", Map.of())
        );

        doReturn(wcCurrent).when(lyricalMatchService).getWordCloudForUser(currentUser);
        doReturn(wc1).when(lyricalMatchService).getWordCloudForUser(candidate1);
        doReturn(wc2).when(lyricalMatchService).getWordCloudForUser(candidate2);

        User enemy = lyricalMatchService.findLyricalEnemy(currentUser);

        assertNotNull(enemy);
        assertEquals(candidate1.getId(), enemy.getId());
    }

    @Test
    void testFindLyricalSoulmateWithForcedCandidate() {
        when(userService.getPublicUsers()).thenReturn(new ArrayList<>());

        // currentUser word cloud
        List<Song> favCurrent = List.of(new Song(1));
        when(favoritesService.getFavoriteSongs(currentUser.getId())).thenReturn(favCurrent);
        List<Word> wcCurrent = List.of(new Word("Peace", Map.of()));
        when(apiService.generateWordCloud(favCurrent)).thenReturn(wcCurrent);

        // forcedCandidate word cloud
        List<Song> favForced = List.of(new Song(2));
        when(favoritesService.getFavoriteSongs(candidate1.getId())).thenReturn(favForced);
        List<Word> wcForced = List.of(new Word("Peace", Map.of()), new Word("music", Map.of()));
        when(apiService.generateWordCloud(favForced)).thenReturn(wcForced);

        User soulmate = lyricalMatchService.findLyricalSoulmate(currentUser, candidate1);
        assertNotNull(soulmate);
        assertEquals(candidate1.getId(), soulmate.getId());
    }

    @Test
    void testFindLyricalEnemyWithForcedCandidate() {
        when(userService.getPublicUsers()).thenReturn(new ArrayList<>());

        // currentUser word cloud
        List<Song> favCurrent = List.of(new Song(1));
        when(favoritesService.getFavoriteSongs(currentUser.getId())).thenReturn(favCurrent);
        List<Word> wcCurrent = List.of(new Word("rock", Map.of()));
        when(apiService.generateWordCloud(favCurrent)).thenReturn(wcCurrent);

        // forcedCandidate word cloud
        List<Song> favForced = List.of(new Song(2));
        when(favoritesService.getFavoriteSongs(candidate1.getId())).thenReturn(favForced);
        List<Word> wcForced = List.of(new Word("pop", Map.of()));
        when(apiService.generateWordCloud(favForced)).thenReturn(wcForced);

        User enemy = lyricalMatchService.findLyricalEnemy(currentUser, candidate1);
        assertNotNull(enemy);
        assertEquals(candidate1.getId(), enemy.getId());
    }

}