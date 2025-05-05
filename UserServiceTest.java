package edu.usc.csci310.project.services;

import edu.usc.csci310.project.models.Song;
import edu.usc.csci310.project.models.User;
import edu.usc.csci310.project.requests.CompareFriendRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private FavoritesService favoritesService;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
//        restTemplate = mock(RestTemplate.class);
//        favoritesService = mock(FavoritesService.class);
//        userService = new UserService();
//        userService.setRestTemplate(restTemplate);
//        userService.setFavoritesService(favoritesService);
    }

    @Test
    void getUserByUsername() {
        User mockUser = new User(1, "danny", "password");
        List<User> mockUsers = Arrays.asList(mockUser);
        ResponseEntity<List<User>> mockResponse = new ResponseEntity<>(mockUsers, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(mockResponse);

        User friend = userService.getUserByUsername("danny");

        assertEquals("danny", friend.getUsername());
        verify(restTemplate, times(1)).exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        );
    }

    @Test
    void getUserByUsername_UserNull() {

        //return null
        ResponseEntity<List<User>> mockResponse = new ResponseEntity<>(null, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(mockResponse);

        Exception exception = null;
        try {
            userService.getUserByUsername("nonexistent");
        } catch (RuntimeException e) {
            exception = e;
        }

        assertEquals("No friend with name nonexistent found", exception.getMessage());
    }

    @Test
    void getUserByUsername_UserEmpty(){

        List<User> mockUsers = Collections.emptyList();
        ResponseEntity<List<User>> mockResponse = new ResponseEntity<>(mockUsers, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(mockResponse);

        Exception exception = null;
        try {
            userService.getUserByUsername("nonexistent");
        } catch (RuntimeException e) {
            exception = e;
        }

        assertEquals("No friend with name nonexistent found", exception.getMessage());
    }

    @Test
    void compareFavoriteSongs() {

        CompareFriendRequest request = new CompareFriendRequest();
        request.setLoggedInUserId(123);
        List <User> users = new ArrayList<>();
        users.add(new User(456, "user456", "password"));
        users.add(new User(568, "user568", "password"));

        request.setUsers(users);

        List<Song> loggedInUserSongs = new ArrayList<>();
        List<Song> user456Songs = new ArrayList<>();
        List<Song> user568Songs = new ArrayList<>();


        Song song1 = new Song(1, "song1", "artist1");
        Song song2 = new Song(2, "song2", "artist2");
        Song song3 = new Song(3, "song3", "artist3");


        loggedInUserSongs.add(song1);
        loggedInUserSongs.add(song2);
        loggedInUserSongs.add(song3);
        user456Songs.add(song1);
        user456Songs.add(song2);
        user456Songs.add(song3);
        user568Songs.add(song1);

        when(favoritesService.getFavoriteSongs(123)).thenReturn(loggedInUserSongs);
        when(favoritesService.getFavoriteSongs(456)).thenReturn(user456Songs);
        when(favoritesService.getFavoriteSongs(568)).thenReturn(user568Songs);

//
        List<Song>  result = userService.compareFavoriteSongs(request);
//
        assertEquals(3, result.size());
        assertEquals("song1", result.get(0).getTitle());
        assertEquals("song2", result.get(1).getTitle());
        assertEquals("song3", result.get(2).getTitle());
    }

    @Test
    void noCommonSongs() {
        //TODO fix this test

        CompareFriendRequest request = new CompareFriendRequest();
        request.setLoggedInUserId(123);
        List <User> users = new ArrayList<>();
        users.add(new User(456, "user456", "password"));
        request.setUsers(users);

        List<Song> loggedInUserSongs = new ArrayList<>();
        List<Song> user456Songs = new ArrayList<>();

        Song song1 = new Song(1, "song1", "artist1");
        Song song2 = new Song(2, "song2", "artist2");
        Song song3 = new Song(3, "song3", "artist3");


        loggedInUserSongs.add(song1);
        loggedInUserSongs.add(song2);
        user456Songs.add(song3);

        when(favoritesService.getFavoriteSongs(123)).thenReturn(loggedInUserSongs);
        when(favoritesService.getFavoriteSongs(456)).thenReturn(user456Songs);
//
        List<Song>  result = userService.compareFavoriteSongs(request);
//
        assertEquals(0, result.size());

    }

    @Test
    void testGetPublicUsers_NullBody() {
        // Simulate a null body in the response
        ResponseEntity<List<User>> mockResponse = new ResponseEntity<>(null, HttpStatus.OK);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(mockResponse);

        List<User> result = userService.getPublicUsers();
        assertNotNull(result, "Should never return null");
        assertTrue(result.isEmpty(), "Null body should yield empty list");

        verify(restTemplate, times(1)).exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        );
    }

    @Test
    void testGetPublicUsers_EmptyList() {
        // Simulate an empty list in the response
        ResponseEntity<List<User>> mockResponse = new ResponseEntity<>(Collections.emptyList(), HttpStatus.OK);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(mockResponse);

        List<User> result = userService.getPublicUsers();
        assertNotNull(result, "Should never return null");
        assertTrue(result.isEmpty(), "Empty list body should yield empty list");
    }

    @Test
    void testGetPublicUsers_NonEmptyMasksPasswords() {
        // Simulate a non-empty list of users
        User u1 = new User(1, "alice", "secret");
        User u2 = new User(2, "bob",   "hunter2");
        List<User> mockUsers = Arrays.asList(u1, u2);
        ResponseEntity<List<User>> mockResponse = new ResponseEntity<>(mockUsers, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(mockResponse);

        List<User> result = userService.getPublicUsers();
        assertEquals(2, result.size(), "Should return both users");
        for (User u : result) {
            assertEquals("***", u.getPassword(), "Password should be masked");
        }
    }


}
