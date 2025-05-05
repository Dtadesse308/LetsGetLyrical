package edu.usc.csci310.project.models;

import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class WordTest {

    @Test
    public void testBasicFunctionality() {
        Map<Song, Integer> occurrences = new HashMap<>();
        occurrences.put(new Song(1), 3);

        Word word = new Word("hello", occurrences);
        assertEquals("hello", word.getWord());
        assertEquals(occurrences, word.getSongOccurrences());

        word.setWord("sanya");
        assertEquals("sanya", word.getWord());

        Map<Song, Integer> newOccurrences = new HashMap<>();
        newOccurrences.put(new Song(2), 5);
        word.setSongOccurrences(newOccurrences);
        assertEquals(newOccurrences, word.getSongOccurrences());

        word.addSongOccurrence(new Song(3), 2);
        assertEquals(2, word.getSongOccurrences().get(new Song(3)).intValue());

        word.addSongOccurrence(new Song(2), 3);
        assertEquals(8, word.getSongOccurrences().get(new Song(2)).intValue());

        Word newWord = new Word("hello", null);
        List<Map<String, Object>> list = newWord.getSongOccurrencesAsList();
        assertNotNull(list);
        assertTrue(list.isEmpty());
    }
}