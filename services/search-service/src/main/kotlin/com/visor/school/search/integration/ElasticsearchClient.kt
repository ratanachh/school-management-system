package com.visor.school.search.integration

import com.visor.school.search.model.SearchIndex
import com.visor.school.search.model.SearchType
import org.slf4j.LoggerFactory
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.data.elasticsearch.core.SearchHit
import org.springframework.data.elasticsearch.core.query.Query
import org.springframework.data.elasticsearch.core.query.StringQuery
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * Elasticsearch client for indexing and querying documents
 */
@Component
class ElasticsearchClient(
    private val elasticsearchOperations: ElasticsearchOperations
) {
    private val logger = LoggerFactory.getLogger(ElasticsearchClient::class.java)

    /**
     * Index a document in Elasticsearch
     */
    fun index(document: SearchIndex) {
        try {
            elasticsearchOperations.save(document)
            logger.info("Indexed document: ${document.id} of type: ${document.type}")
        } catch (e: Exception) {
            logger.error("Failed to index document: ${document.id}", e)
            throw RuntimeException("Failed to index document", e)
        }
    }

    /**
     * Search documents by query and type
     */
    fun search(query: String, type: SearchType): List<SearchIndex> {
        try {
            val searchQuery = StringQuery(
                """
                {
                  "bool": {
                    "must": [
                      {
                        "match": {
                          "title": {
                            "query": "$query",
                            "fuzziness": "AUTO"
                          }
                        }
                      },
                      {
                        "term": {
                          "type": "$type"
                        }
                      }
                    ]
                  }
                }
                """.trimIndent()
            )

            val searchHits = elasticsearchOperations.search(searchQuery, SearchIndex::class.java)
            return searchHits.map { it.content }
        } catch (e: Exception) {
            logger.error("Failed to search documents with query: $query", e)
            throw RuntimeException("Failed to search documents", e)
        }
    }

    /**
     * Update an indexed document
     */
    fun updateIndex(document: SearchIndex) {
        try {
            elasticsearchOperations.save(document)
            logger.info("Updated indexed document: ${document.id}")
        } catch (e: Exception) {
            logger.error("Failed to update indexed document: ${document.id}", e)
            throw RuntimeException("Failed to update indexed document", e)
        }
    }

    /**
     * Delete an indexed document
     */
    fun deleteIndex(id: UUID, type: SearchType) {
        try {
            // Find document first
            val query = StringQuery(
                """
                {
                  "bool": {
                    "must": [
                      {
                        "term": {
                          "_id": "$id"
                        }
                      },
                      {
                        "term": {
                          "type": "$type"
                        }
                      }
                    ]
                  }
                }
                """.trimIndent()
            )

            val searchHits = elasticsearchOperations.search(query, SearchIndex::class.java)
            searchHits.forEach { hit ->
                elasticsearchOperations.delete(hit.content)
            }
            logger.info("Deleted indexed document: $id of type: $type")
        } catch (e: Exception) {
            logger.error("Failed to delete indexed document: $id", e)
            throw RuntimeException("Failed to delete indexed document", e)
        }
    }
}

