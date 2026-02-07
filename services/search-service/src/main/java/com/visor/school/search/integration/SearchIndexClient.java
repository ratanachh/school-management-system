package com.visor.school.search.integration;

import com.visor.school.search.model.SearchIndex;
import com.visor.school.search.model.SearchType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Elasticsearch client for indexing and querying documents
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SearchIndexClient {

  private final ElasticsearchOperations elasticsearchOperations;

  /**
   * Index a document in Elasticsearch
   */
  public void index(SearchIndex document) {
    try {
      elasticsearchOperations.save(document);
      log.info("Indexed document: {} of type: {}", document.getId(), document.getType());
    } catch (Exception e) {
      log.error("Failed to index document: {}", document.getId(), e);
      throw new RuntimeException("Failed to index document", e);
    }
  }

  /**
   * Search documents by query and type
   */
  public List<SearchIndex> search(String query, SearchType type) {
    try {
      String queryString = """
          {
            "bool": {
              "must": [
                {
                  "match": {
                    "title": {
                      "query": "%s",
                      "fuzziness": "AUTO"
                    }
                  }
                },
                {
                  "term": {
                    "type": "%s"
                  }
                }
              ]
            }
          }
          """.formatted(query, type);

      StringQuery stringQuery = new StringQuery(queryString);
      SearchHits<SearchIndex> searchHits = elasticsearchOperations.search(stringQuery, SearchIndex.class);
      return searchHits.stream()
          .map(hit -> hit.getContent())
          .collect(Collectors.toList());
    } catch (Exception e) {
      log.error("Failed to search documents with query: {}", query, e);
      throw new RuntimeException("Failed to search documents", e);
    }
  }

  /**
   * Update an indexed document
   */
  public void updateIndex(SearchIndex document) {
    try {
      elasticsearchOperations.save(document);
      log.info("Updated indexed document: {}", document.getId());
    } catch (Exception e) {
      log.error("Failed to update indexed document: {}", document.getId(), e);
      throw new RuntimeException("Failed to update indexed document", e);
    }
  }

  /**
   * Delete an indexed document
   */
  public void deleteIndex(UUID id, SearchType type) {
    try {
      // Find document first
      String queryString = """
          {
            "bool": {
              "must": [
                {
                  "term": {
                    "_id": "%s"
                  }
                },
                {
                  "term": {
                    "type": "%s"
                  }
                }
              ]
            }
          }
          """.formatted(id, type);

      StringQuery query = new StringQuery(queryString);

      SearchHits<SearchIndex> searchHits = elasticsearchOperations.search(query, SearchIndex.class);
      searchHits.forEach(hit -> elasticsearchOperations.delete(hit.getContent()));

      log.info("Deleted indexed document: {} of type: {}", id, type);
    } catch (Exception e) {
      log.error("Failed to delete indexed document: {}", id, e);
      throw new RuntimeException("Failed to delete indexed document", e);
    }
  }
}
