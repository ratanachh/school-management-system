package com.visor.school.search.service

import com.visor.school.search.integration.ElasticsearchClient
import com.visor.school.search.model.SearchIndex
import com.visor.school.search.model.SearchType
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * Search service for indexing and querying documents
 */
@Service
class SearchService(
    private val elasticsearchClient: ElasticsearchClient
) {
    private val logger = LoggerFactory.getLogger(SearchService::class.java)

    /**
     * Index a document
     */
    fun index(document: SearchIndex) {
        logger.info("Indexing document: ${document.id} of type: ${document.type}")
        elasticsearchClient.index(document)
    }

    /**
     * Search documents by query and type
     */
    fun search(query: String, type: SearchType): List<SearchIndex> {
        logger.info("Searching for: $query in type: $type")
        return elasticsearchClient.search(query, type)
    }

    /**
     * Update an indexed document
     */
    fun updateIndex(document: SearchIndex) {
        logger.info("Updating indexed document: ${document.id}")
        elasticsearchClient.updateIndex(document)
    }

    /**
     * Delete an indexed document
     */
    fun deleteIndex(id: UUID, type: SearchType) {
        logger.info("Deleting indexed document: $id of type: $type")
        elasticsearchClient.deleteIndex(id, type)
    }
}

