package com.visor.school.search.model

import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType
import java.util.UUID

/**
 * Search index document for Elasticsearch
 */
@Document(indexName = "school_search")
data class SearchIndex(
    @Id
    val id: UUID,

    @Field(type = FieldType.Keyword)
    val type: SearchType,

    @Field(type = FieldType.Text, analyzer = "standard")
    val title: String,

    @Field(type = FieldType.Text, analyzer = "standard")
    val content: String,

    @Field(type = FieldType.Object)
    val metadata: Map<String, Any> = emptyMap()
)

