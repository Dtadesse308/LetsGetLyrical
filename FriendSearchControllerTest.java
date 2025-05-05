package edu.usc.csci310.project.controllers;

//import edu.usc.csci310.project.models.ComparisonSong;
//import edu.usc.csci310.project.models.Friend;
import edu.usc.csci310.project.models.Song;
import edu.usc.csci310.project.models.User;
import edu.usc.csci310.project.requests.CompareFriendRequest;
import edu.usc.csci310.project.services.UserService;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class FriendSearchControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private FriendSearchController friendSearchController;

    @BeforeEach
    void setUp() {
       // MockitoAnnotations.openMocks(this);
        userService = mock(UserService.class);
        friendSearchController = new FriendSearchController(userService);
    }

    @Test
    void findFriendByUsername() {
        User mockFriend = new User(1, "danny", "password");

        //String hashedUsername = DigestUtils.sha256Hex("danny");
        when(userService.getUserByUsername("danny")).thenReturn(mockFriend);

        ResponseEntity<User> response = friendSearchController.findFriendByUsername("danny");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("danny", response.getBody().getUsername());
        verify(userService, times(1)).getUserByUsername("danny");
    }

    @Test
    void compareFavoriteSongs() {

        //TODO fix this test

        List<User> mockUsers = new ArrayList<>();
        mockUsers.add(new User(1, "user1", "password"));
        mockUsers.add(new User(2, "user2", "password"));
        List<Song> mockComparisonSongs =  new ArrayList<>();

        mockComparisonSongs.add(new Song(1, "songTitl1", "artist1", 2020, mockUsers ));
        mockComparisonSongs.add(new Song(2, "songTitl2", "artist2", 2021, mockUsers ));

        CompareFriendRequest request = new CompareFriendRequest();
        when(userService.compareFavoriteSongs(request)).thenReturn(mockComparisonSongs);

        ResponseEntity<List<Song>> response = friendSearchController.compareFavoriteSongs(request);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(2, response.getBody().size());
    }
}
