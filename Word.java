package edu.usc.csci310.project.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Word {
    private String word;
    private Map<Song, Integer> songOccurrences;

    public Word(String word, Map<Song, Integer> songOccurrences) {
        this.word = word;
        this.songOccurrences = songOccurrences;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public Map<Song, Integer> getSongOccurrences() {
        return songOccurrences;
    }

    public void setSongOccurrences(Map<Song, Integer> songOccurrences) {
        this.songOccurrences = songOccurrences;
    }

    public void addSongOccurrence(Song song, int occurrences) {
        if (songOccurrences.containsKey(song)) songOccurrences.put(song, songOccurrences.get(song) + occurrences);
        else songOccurrences.put(song, occurrences);
    }

    @JsonProperty("songOccurrences")
    public List<Map<String, Object>> getSongOccurrencesAsList() {
        List<Map<String, Object>> list = new ArrayList<>();
        if (songOccurrences != null) {
            for (Map.Entry<Song, Integer> entry : songOccurrences.entrySet()) {
                Song song = entry.getKey();
                Map<String, Object> map = new HashMap<>();
                map.put("id", song.getId());
                map.put("title", song.getTitle());
                map.put("artist", song.getArtist());
                map.put("year", song.getYear());
                map.put("lyrics", song.getLyrics());
                map.put("count", entry.getValue());
                list.add(map);
            }
        }
        return list;
    }
}
