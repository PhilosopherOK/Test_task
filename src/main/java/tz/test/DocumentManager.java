package tz.test;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * For implement this task focus on clear code, and make this solution as simple readable as possible
 * Don't worry about performance, concurrency, etc
 * You can use in Memory collection for sore data
 * <p>
 * Please, don't change class name, and signature for methods save, search, findById
 * Implementations should be in a single class
 * This class could be auto tested
 */
public class DocumentManager {

    private final Map<String, Document> storage = new HashMap<>();


    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist, don't change [created] field
     *
     * @param document - document content and author data
     * @return saved document
     */

    public Document save(Document document) {
        if (document == null) {
            throw new IllegalArgumentException("Document cannot be null");
        }

        if (document.getId() == null || document.getId().isEmpty()) {
            document.setId(UUID.randomUUID().toString());
        }

        storage.compute(document.getId(), (id, existingDocument) -> {
            // If the document already exists, retain its 'created' field while updating other fields
            if (existingDocument != null) {
                return Document.builder()
                        .id(existingDocument.getId())
                        .title(document.getTitle())
                        .content(document.getContent())
                        .author(document.getAuthor())
                        .created(existingDocument.getCreated())
                        .build();
            } else {
                // If the document is new, set its 'created' field to the current time if it's not already set
                return Document.builder()
                        .id(document.getId())
                        .title(document.getTitle())
                        .content(document.getContent())
                        .author(document.getAuthor())
                        .created(document.getCreated() != null ? document.getCreated() : Instant.now())
                        .build();
            }
        });
        return storage.get(document.getId());
    }

    /**
     * Implementation this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(SearchRequest request) {
        // Convert the storage values to a stream, filter them using the helper methods, and collect the matching documents into a list
        return storage.values().stream()
                .filter(document ->
                        matchesTitlePrefixes(document.getTitle(), request.getTitlePrefixes()) &&
                                matchesContainsContents(document.getContent(), request.getContainsContents()) &&
                                matchesAuthorIds(document.getAuthor(), request.getAuthorIds()) &&
                                matchesCreatedDate(document.getCreated(), request.getCreatedFrom(), request.getCreatedTo())
                )
                .collect(Collectors.toList());
    }

    private boolean matchesTitlePrefixes(String title, List<String> prefixes) {
        if (title == null || prefixes == null || prefixes.isEmpty()) return true;
        return prefixes.stream().anyMatch(title::startsWith);
    }

    private boolean matchesContainsContents(String content, List<String> contains) {
        if (contains == null || contains.isEmpty()) return true;
        return contains.stream().anyMatch(content::contains);
    }

    private boolean matchesAuthorIds(Author author, List<String> authorIds) {
        if (authorIds == null || authorIds.isEmpty()) return true;
        return author != null &&
                author.getId() != null &&
                authorIds.contains(author.getId());
    }

    private boolean matchesCreatedDate(Instant created, Instant from, Instant to) {
        if (created == null) return false;
        boolean matchesFrom = (from == null || !created.isBefore(from));
        boolean matchesTo = (to == null || !created.isAfter(to));
        return matchesFrom && matchesTo;
    }


    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */
    public Optional<Document> findById(String id) {
        if (id == null || id.isEmpty()) {
            return Optional.empty();
        }

        Document document = storage.get(id);

        return Optional.ofNullable(document);
    }


    @Data
    @Builder
    public static class SearchRequest {
        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;
    }

    @Data
    @Builder
    public static class Document {
        private String id;
        private String title;
        private String content;
        private Author author;
        private Instant created;
    }

    @Data
    @Builder
    public static class Author {
        private String id;
        private String name;
    }
}