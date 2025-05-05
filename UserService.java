package edu.usc.csci310.project.services;

import edu.usc.csci310.project.models.Song;
import edu.usc.csci310.project.models.User;
import edu.usc.csci310.project.requests.CompareFriendRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {
    private static final String SUPABASE_URL = "https://cqqwwzmgbeckmdotlwdx.supabase.co";
    private RestTemplate restTemplate = new RestTemplate();

    @Value("${SUPABASE_API_KEY}")
    private String SUPABASE_API_KEY;

    @Autowired
    private FavoritesService favoritesService;

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public User getUserByUsername(@PathVariable String username) {
        // Construct the full URL with username query
        String url = SUPABASE_URL + "/rest/v1/users?username=eq." + username;

        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", SUPABASE_API_KEY);
        headers.set("Authorization", "Bearer " + SUPABASE_API_KEY);
        headers.set("Content-Type", "application/json");
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        //make the get call to the supabase
        ResponseEntity<List<User>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<User>>() {
                }
        );

        List<User> users = response.getBody();

        if (users == null || users.isEmpty()) { // NOT FOUND
            throw new RuntimeException("No friend with name " + username + " found");

        }

        User user = users.get(0);
        //hide password so it doesnt show up on api response
        user.setPassword("***");
        return user;

    }

    public List<User> getPublicUsers() {
        String url = SUPABASE_URL + "/rest/v1/users?is_private=eq.false";

        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", SUPABASE_API_KEY);
        headers.set("Authorization", "Bearer " + SUPABASE_API_KEY);
        headers.set("Content-Type", "application/json");
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        // Make the get call to supabase
        ResponseEntity<List<User>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<User>>() {}
        );

        List<User> users = response.getBody();

        if (users == null || users.isEmpty()) {
            return Collections.emptyList();
        }

        // hide password so it doesn't show up on api response
        users.forEach(user -> user.setPassword("***"));

        return users;
    }

    public List<Song> compareFavoriteSongs(CompareFriendRequest request) {


        //TODO return the song object, containing the song details, number of occurences, and the users that played it

        //make set of favorite song ids of the logged in user
        List<Song> loggedInUsersfavoriteSongs = favoritesService.getFavoriteSongs(request.getLoggedInUserId());
        Set<Integer> myFavoriteSongIds = loggedInUsersfavoriteSongs.stream()
                .map(Song::getId)
                .collect(Collectors.toSet());


        Map<Integer, Song> commonSongs = new HashMap<>();
        //go through all selected usernames
        List<User> users = request.getUsers();
        for (User currUser : users) {
            if (currUser.getIs_private()) {
                throw new RuntimeException("User " + currUser.getUsername() + " is private");
            }
            List<Song> currUserFavSongs = favoritesService.getFavoriteSongs(currUser.getId());
            //go through all the favorite songs of the user
            for (Song song : currUserFavSongs) {
                //int songId = (int) song.get("id");
                // String songTitle = (String) song.get("title");
                //if logged in user shares the song with the user
                Integer songId = song.getId();
                if (myFavoriteSongIds.contains(songId)) {
                    //TODO add users name to a list of the users that played the song
                    if (commonSongs.get(songId) == null) {
                        commonSongs.put(songId, new Song(songId, song.getTitle(), song.getArtist(), song.getYear() , new ArrayList<>()));
                    }
                    commonSongs.get(songId).addFavoritedByUser(currUser);

                }
            }

        }
        //sorts the songs by increasing order of appearances
        List<Song> sortedSongs = new ArrayList<>();
        // Sort the commonSongs map by the size of the favoritedByUsers list in descending order
        sortedSongs = commonSongs.values().stream()
                .sorted((song1, song2) -> Integer.compare(
                        song2.getFavoritedByUsers().size(),
                        song1.getFavoritedByUsers().size()
                ))
                .collect(Collectors.toList());

        return sortedSongs;

    }


}
