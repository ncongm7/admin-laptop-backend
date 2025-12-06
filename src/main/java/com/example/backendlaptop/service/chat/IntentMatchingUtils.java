package com.example.backendlaptop.service.chat;

import java.util.*;

/**
 * Utility class for intent matching with fuzzy matching and synonym support
 */
public class IntentMatchingUtils {

    // Synonym dictionary (simple in-memory map, can be moved to database)
    private static final Map<String, List<String>> SYNONYMS = new HashMap<>();
    
    static {
        // Price synonyms
        SYNONYMS.put("giá", Arrays.asList("price", "cost", "gia", "bao nhieu", "bao nhiêu", "tien", "tiền"));
        SYNONYMS.put("price", Arrays.asList("giá", "cost", "gia", "bao nhieu", "bao nhiêu", "tien", "tiền"));
        
        // Product synonyms
        SYNONYMS.put("sản phẩm", Arrays.asList("product", "san pham", "laptop", "may tinh", "máy tính", "sp"));
        SYNONYMS.put("product", Arrays.asList("sản phẩm", "san pham", "laptop", "may tinh", "máy tính", "sp"));
        
        // Warranty synonyms
        SYNONYMS.put("bảo hành", Arrays.asList("warranty", "bao hanh", "bh", "bao tri", "bảo trì"));
        SYNONYMS.put("warranty", Arrays.asList("bảo hành", "bao hanh", "bh", "bao tri", "bảo trì"));
        
        // Order synonyms
        SYNONYMS.put("đơn hàng", Arrays.asList("order", "don hang", "dh", "hoa don", "hóa đơn"));
        SYNONYMS.put("order", Arrays.asList("đơn hàng", "don hang", "dh", "hoa don", "hóa đơn"));
        
        // Payment synonyms
        SYNONYMS.put("thanh toán", Arrays.asList("payment", "thanh toan", "tt", "tien", "tiền"));
        SYNONYMS.put("payment", Arrays.asList("thanh toán", "thanh toan", "tt", "tien", "tiền"));
    }

    /**
     * Calculate Levenshtein distance between two strings
     */
    public static int levenshteinDistance(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return Integer.MAX_VALUE;
        }
        
        int len1 = s1.length();
        int len2 = s2.length();
        
        if (len1 == 0) return len2;
        if (len2 == 0) return len1;
        
        int[][] dp = new int[len1 + 1][len2 + 1];
        
        for (int i = 0; i <= len1; i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= len2; j++) {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(
                    Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                    dp[i - 1][j - 1] + cost
                );
            }
        }
        
        return dp[len1][len2];
    }

    /**
     * Calculate fuzzy similarity score (0.0 to 1.0)
     */
    public static double fuzzySimilarity(String s1, String s2) {
        if (s1 == null || s2 == null || s1.isEmpty() || s2.isEmpty()) {
            return 0.0;
        }
        
        int distance = levenshteinDistance(s1.toLowerCase(), s2.toLowerCase());
        int maxLen = Math.max(s1.length(), s2.length());
        
        if (maxLen == 0) return 1.0;
        
        return 1.0 - ((double) distance / maxLen);
    }

    /**
     * Check if keyword matches message using fuzzy matching
     * Returns similarity score (0.0 to 1.0)
     */
    public static double fuzzyMatch(String message, String keyword, double threshold) {
        // Exact match
        if (message.contains(keyword)) {
            return 1.0;
        }
        
        // Word-by-word fuzzy match
        String[] messageWords = message.split("\\s+");
        String[] keywordWords = keyword.split("\\s+");
        
        double bestScore = 0.0;
        
        for (String keywordWord : keywordWords) {
            for (String messageWord : messageWords) {
                double similarity = fuzzySimilarity(messageWord, keywordWord);
                if (similarity >= threshold) {
                    bestScore = Math.max(bestScore, similarity);
                }
            }
        }
        
        return bestScore;
    }

    /**
     * Get synonyms for a keyword
     */
    public static List<String> getSynonyms(String keyword) {
        String normalized = normalizeKeyword(keyword);
        List<String> synonyms = SYNONYMS.getOrDefault(normalized, new ArrayList<>());
        
        // Also check if keyword is a synonym itself
        for (Map.Entry<String, List<String>> entry : SYNONYMS.entrySet()) {
            if (entry.getValue().contains(normalized)) {
                List<String> allSynonyms = new ArrayList<>(entry.getValue());
                allSynonyms.add(entry.getKey());
                return allSynonyms;
            }
        }
        
        return synonyms;
    }

    /**
     * Check if message contains keyword or its synonyms
     */
    public static boolean containsKeywordOrSynonym(String message, String keyword) {
        String normalizedMessage = normalizeKeyword(message);
        String normalizedKeyword = normalizeKeyword(keyword);
        
        // Check exact match
        if (normalizedMessage.contains(normalizedKeyword)) {
            return true;
        }
        
        // Check synonyms
        List<String> synonyms = getSynonyms(keyword);
        for (String synonym : synonyms) {
            if (normalizedMessage.contains(normalizeKeyword(synonym))) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Calculate fuzzy match score with synonyms
     */
    public static double calculateFuzzyScoreWithSynonyms(String message, String keyword, double fuzzyThreshold) {
        String normalizedMessage = normalizeKeyword(message);
        String normalizedKeyword = normalizeKeyword(keyword);
        
        // Exact match
        if (normalizedMessage.contains(normalizedKeyword)) {
            return 1.0;
        }
        
        // Synonym match
        List<String> synonyms = getSynonyms(keyword);
        for (String synonym : synonyms) {
            if (normalizedMessage.contains(normalizeKeyword(synonym))) {
                return 0.9; // Slightly lower than exact match
            }
        }
        
        // Fuzzy match
        return fuzzyMatch(normalizedMessage, normalizedKeyword, fuzzyThreshold);
    }

    /**
     * Normalize keyword for matching (remove accents, lowercase)
     */
    private static String normalizeKeyword(String text) {
        if (text == null) return "";
        
        // Remove accents
        String normalized = java.text.Normalizer.normalize(text, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        
        // Replace đ → d
        normalized = normalized.replace("đ", "d").replace("Đ", "d");
        
        // Remove extra spaces
        normalized = normalized.trim().replaceAll("\\s+", " ");
        
        return normalized.toLowerCase();
    }
}

