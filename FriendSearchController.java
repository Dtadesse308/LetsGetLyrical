package edu.usc.csci310.project.controllers;

import ch.qos.logback.core.net.SyslogOutputStream;
//import edu.usc.csci310.project.models.ComparisonSong;
//import edu.usc.csci310.project.models.Friend;
import edu.usc.csci310.project.models.Song;
import edu.usc.csci310.project.models.User;
import edu.usc.csci310.project.requests.CompareFriendRequest;
import edu.usc.csci310.project.services.UserService;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/friend")
public class FriendSearchController {

    private final UserService friendApiService;

    @Autowired
    public FriendSearchController(UserService friendApiService) {
        this.friendApiService = friendApiService;
    }

    @GetMapping("/search")
    public ResponseEntity<User> findFriendByUsername(@RequestParam String username) {
        username = username.toLowerCase();
        //String hashedUsername = DigestUtils.sha256Hex(username);
        User friend = friendApiService.getUserByUsername(username);
        System.out.println("got the friend from services = " + friend.getUsername());
        //TODO is there a better way to do this? I get a hased username but set it back to the original
        friend.setUsername(username);
        return ResponseEntity.ok(friend);
    }

    @PostMapping ("/compare")
    public ResponseEntity<List<Song>> compareFavoriteSongs(@RequestBody CompareFriendRequest request ) {
        //revieve a friend object from the service
        //TODO return an object containing the song details, number of occurences, and the users that played it
        System.out.println("comparing favoriteSongs");

        List<Song> comparedSongs = friendApiService.compareFavoriteSongs(request);
        return ResponseEntity.ok(comparedSongs);

    }
}
