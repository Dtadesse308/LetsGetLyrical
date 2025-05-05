package edu.usc.csci310.project.models;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class SongTest {

    @Test
    public void testSongDefaultConstructor() {
        Song song = new Song();
        assertNotNull(song);
        assertNull(song.getId());
        assertNull(song.getTitle());
        assertNull(song.getArtist());
    }

    @Test
    public void testSetAndGetId() {
        Song song = new Song();
        Integer id = 12345;
        song.setId(id);

        assertEquals(id, song.getId());

        Integer newId = 67890;
        song.setId(newId);
        assertEquals(newId, song.getId());

        song.setId(null);
        assertNull(song.getId());
    }

    @Test
    public void testSetAndGetTitle() {
        Song song = new Song();

        String title = "Test Song Title";
        song.setTitle(title);

        assertEquals(title, song.getTitle());
        String newTitle = "Updated Song Title";
        song.setTitle(newTitle);
        assertEquals(newTitle, song.getTitle());

        song.setTitle(null);
        assertNull(song.getTitle());
    }

    @Test
    public void testSetAndGetArtist() {
        Song song = new Song();

        String artist = "Test Artist";
        song.setArtist(artist);
        song.setYear(2021);

        assertEquals(artist, song.getArtist());
        assertEquals(2021, song.getYear());

        String newArtist = "Updated Artist";
        song.setArtist(newArtist);
        assertEquals(newArtist, song.getArtist());

        song.setArtist(null);
        assertNull(song.getArtist());
    }

    @Test
    public void testSetAndGetFavoritedByUsers(){
        Song song = new Song();
        List<User> users = new ArrayList<>();
        User user1 = new User();
        user1.setId(1);
        user1.setUsername("user1");
        users.add(user1);

        song.setFavoritedByUsers(users);
        assertEquals(users, song.getFavoritedByUsers());


        User user2 = new User();
        user2.setId(2);
        user2.setUsername("user2");
        users.add(user2);

        song.addFavoritedByUser(user2);
        assertEquals(users, song.getFavoritedByUsers());

        song.setFavoritedByUsers(null);
        assertNull(song.getFavoritedByUsers());
    }

    @Test
    public void addFavoritesByUserIntialNullTest() {
        Song song = new Song();
        List<User> users = new ArrayList<>();
        User user1 = new User();
        user1.setId(1);
        user1.setUsername("user1");
        users.add(user1);
        song.addFavoritedByUser(user1);
        assertEquals(users, song.getFavoritedByUsers());
    }

    @Test
    void equals_isReflexive() {
        Song s = new Song(1, "Title", "Artist");
        assertEquals(s, s);
    }

    @Test
    void equals_isSymmetric() {
        Song a = new Song(2, "A", "X");
        Song b = new Song(2, "B", "Y");
        assertEquals(a, b);
        assertEquals(b, a);
    }

    @Test
    void equals_isTransitive() {
        Song a = new Song(3);
        Song b = new Song(3);
        Song c = new Song(3);
        assertEquals(a, b);
        assertEquals(b, c);
        assertEquals(a, c);
    }

    @Test
    void equals_differentId() {
        Song s1 = new Song(5);
        Song s2 = new Song(6);
        assertNotEquals(s1, s2);
    }

    @Test
    void equals_bothIdsNull() {
        Song s1 = new Song();
        Song s2 = new Song();
        assertEquals(s1, s2);
    }

    @Test
    void equals_nullAndDifferentRuntimeClass() {
        Song s = new Song(42);
        assertNotEquals(null, s);
        assertNotEquals("abc", s);

        Song s2 = null;
        assertFalse(s.equals(s2));

        Song subclassInstance = new Song(42){ };
        assertNotEquals(subclassInstance, s);
        assertNotEquals(s, subclassInstance);
    }

    @Test
    void hashCode_equalSongsHaveSameHash() {
        Song a = new Song(7);
        Song b = new Song(7);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void hashCode_nullIdProducesZero() {
        Song s = new Song();
        s.setId(null);
        assertEquals(0, s.hashCode());
    }

    @Test
    void testSetAndGetPosition()
    {
        Song song = new Song();
        int position = 1;
        song.setPosition(position);
        assertEquals(position, song.getPosition());

        Integer newPos = 4;
        song.setPosition(position);
        assertEquals(position, song.getPosition());

        song.setPosition(null);
        assertNull(song.getPosition());
    }

    @Test
    public void testSongPositionConstructor() {
        Song song = new Song(1, "Title", "Artist", 2020,1);
        assertEquals(song.getPosition(), 1);
    }
}