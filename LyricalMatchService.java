package edu.usc.csci310.project.services;

import edu.usc.csci310.project.models.User;
import edu.usc.csci310.project.models.Song;
import edu.usc.csci310.project.models.Word;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LyricalMatchService {

    @Autowired
    private ApiService apiService;

    @Autowired
    private UserService userService;

    @Autowired
    private FavoritesService favoritesService;

    private Set<String> convertWordCloudToSet(List<Word> wordCloud) {
        Set<String> words = new HashSet<>();
        for (Word word : wordCloud) {
            words.add(word.getWord());
        }
        return words;
    }

    /**
     * Compute similarity between two sets of words.
     * Returns a value between 0.0 (no similarity) and 1.0 (exact match).
     */
    private double computeSimilarity(Set<String> set1, Set<String> set2) {
        if (set1.isEmpty() || set2.isEmpty()) {
            return 0.0; // One or both empty.
        }
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);
        return (double) intersection.size() / union.size();
    }

    // Generates the word cloud for a given user using their favorite songs.
    public List<Word> getWordCloudForUser(User user) {
        List<Song> favoriteSongs = favoritesService.getFavoriteSongs(user.getId());
        return apiService.generateWordCloud(favoriteSongs);
    }

    // Finds the user with the highest similarity in word cloud among public users.
    public User findLyricalSoulmate(User currentUser, User forcedCandidate) {
        // build current user's cloud and set
        List<Word> currentWordCloud = getWordCloudForUser(currentUser);
        Set<String> currentWordsSet = convertWordCloudToSet(currentWordCloud);

        if (currentWordsSet.isEmpty()) return null;

        // print current user's set
        System.out.println("Current user: " + currentUser.getUsername());
        System.out.println("Words: " + currentWordsSet);

        List<User> candidateUsers = userService.getPublicUsers();

        if (forcedCandidate != null) {
            candidateUsers.add(forcedCandidate);  // forcibly include even if private
        }

        User soulmate = null;
        double bestSimilarity = -1.0;

        for (User candidate : candidateUsers) {
            if (candidate.getId().equals(currentUser.getId()) ||
                    favoritesService.getFavoriteSongs(candidate.getId()).isEmpty()) continue;

            // build candidate's cloud and set
            List<Word> candidateWordCloud = getWordCloudForUser(candidate);
            Set<String> candidateWordsSet = convertWordCloudToSet(candidateWordCloud);

            // print candidate info
            System.out.println("Candidate user: " + candidate.getUsername());
            System.out.println("Words: " + candidateWordsSet);

            // compute and print similarity
            double similarity = computeSimilarity(currentWordsSet, candidateWordsSet);
            System.out.printf("Similarity(%s ↔ %s) = %.4f%n",
                    currentUser.getUsername(), candidate.getUsername(), similarity);

            if (similarity > bestSimilarity) {
                bestSimilarity = similarity;
                soulmate = candidate;
            }
        }

        return soulmate;
    }


    // Finds the user with the lowest similarity in word cloud among public users.
    public User findLyricalEnemy(User currentUser, User forcedCandidate) {
        // build current user's cloud and set
        List<Word> currentWordCloud = getWordCloudForUser(currentUser);
        Set<String> currentWordsSet = convertWordCloudToSet(currentWordCloud);

        // print current user's set
        System.out.println("Current user: " + currentUser.getUsername());
        System.out.println("Words: " + currentWordsSet);

        List<User> candidateUsers = userService.getPublicUsers();

        if (forcedCandidate != null) {
            candidateUsers.add(forcedCandidate);  // forcibly include even if private
        }

        User enemy = null;
        double worstSimilarity = Double.MAX_VALUE;

        for (User candidate : candidateUsers) {
            if (candidate.getId().equals(currentUser.getId()) ||
                    favoritesService.getFavoriteSongs(candidate.getId()).isEmpty()) continue;

            // build candidate's cloud and set
            List<Word> candidateWordCloud = getWordCloudForUser(candidate);
            Set<String> candidateWordsSet = convertWordCloudToSet(candidateWordCloud);

            // print candidate info
            System.out.println("Candidate user: " + candidate.getUsername());
            System.out.println("Words: " + candidateWordsSet);

            // compute and print similarity
            double similarity = computeSimilarity(currentWordsSet, candidateWordsSet);
            System.out.printf("Similarity(%s ↔ %s) = %.4f%n",
                    currentUser.getUsername(), candidate.getUsername(), similarity);

            if (similarity < worstSimilarity) {
                worstSimilarity = similarity;
                enemy = candidate;
            }
        }

        return enemy;
    }

    public User findLyricalSoulmate(User currentUser) {
        return findLyricalSoulmate(currentUser, null);
    }

    public User findLyricalEnemy(User currentUser) {
        return findLyricalEnemy(currentUser, null);
    }

}
