package com.visor.school.search.service;

import com.visor.school.search.integration.SearchIndexClient;
import com.visor.school.search.model.SearchIndex;
import com.visor.school.search.model.SearchType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Search service for indexing and querying documents
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SearchService {

    private final SearchIndexClient searchIndexClient;

    /**
     * Index a document
     */
    public void index(SearchIndex document) {
        log.info("Indexing document: {} of type: {}", document.getId(), document.getType());
        searchIndexClient.index(document);
    }

    /**
     * Search documents by query and type
     */
    public List<SearchIndex> search(String query, SearchType type) {
        log.info("Searching for: {} in type: {}", query, type);
        return searchIndexClient.search(query, type);
    }

    /**
     * Update an indexed document
     */
    public void updateIndex(SearchIndex document) {
        log.info("Updating indexed document: {}", document.getId());
        searchIndexClient.updateIndex(document);
    }

    /**
     * Delete an indexed document
     */
    public void deleteIndex(UUID id, SearchType type) {
        log.info("Deleting indexed document: {} of type: {}", id, type);
        searchIndexClient.deleteIndex(id, type);
    }
}
