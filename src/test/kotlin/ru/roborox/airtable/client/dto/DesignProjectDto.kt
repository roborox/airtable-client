package ru.roborox.airtable.client.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class DesignProjectDto(
    @JsonProperty("Kickoff date")
    val kickoffDate: String,

    @JsonProperty("Category")
    val category: String,

    @JsonProperty("Name")
    val name: String
)