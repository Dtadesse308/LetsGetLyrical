package edu.usc.csci310.project.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ArtistTest {

    @Test
    public void testBasicFunctionality() {
        Artist artist = new Artist(1, "Taylor Swift");
        assertEquals(1, artist.getId());
        assertEquals("Taylor Swift", artist.getName());

        artist.setId(2);
        assertEquals(2, artist.getId());

        artist.setName("Drake");
        assertEquals("Drake", artist.getName());

        artist.setImageUrl("abc.com");
        assertEquals("abc.com", artist.getImageUrl());
    }
}