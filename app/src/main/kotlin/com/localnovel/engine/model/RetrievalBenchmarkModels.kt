package com.localnovel.engine.model

import kotlinx.serialization.Serializable

@Serializable
data class RetrievalBenchmarkQuerySet(
    val schemaVersion: String,
    val querySetId: String,
    val status: String,
    val sourceManifestPath: String,
    val sourceManifestSha256: String,
    val queries: List<RetrievalBenchmarkQuery>,
)

@Serializable
data class RetrievalBenchmarkQuery(
    val queryId: String,
    val category: String,
    val text: String,
    val expectedSourceIds: List<String> = emptyList(),
    val visibilityTarget: String,
    val expectedDisposition: String,
    val note: String = "",
)

@Serializable
data class RetrievalBaselineHit(
    val rank: Int,
    val chunkId: String,
    val sourceId: String,
    val sourcePath: String,
    val sourceSha256: String,
    val chunkSha256: String,
    val heading: String,
    val sourceQuote: String,
    val score: Double,
    val reasons: List<String>,
)

@Serializable
data class RetrievalBaselineQueryResult(
    val queryId: String,
    val category: String,
    val text: String,
    val expectedDisposition: String,
    val actualDisposition: String,
    val expectedSourceIds: List<String>,
    val likeTop8Hit: Boolean,
    val lexicalTop8Hit: Boolean,
    val visibilityLeak: Boolean,
    val hits: List<RetrievalBaselineHit>,
)

@Serializable
data class RetrievalBaselineCategorySummary(
    val category: String,
    val queryCount: Int,
    val retrievableCount: Int,
    val likeTop8HitCount: Int,
    val lexicalTop8HitCount: Int,
    val blockedByPolicyCount: Int,
    val corpusGapCount: Int,
)

@Serializable
data class RetrievalExactBaselineReport(
    val verdict: String,
    val status: String,
    val querySetId: String,
    val querySetSha256: String,
    val queryCount: Int,
    val retrievableCount: Int,
    val blockedByPolicyCount: Int,
    val corpusGapCount: Int,
    val likeTop8HitCount: Int,
    val lexicalTop8HitCount: Int,
    val likeTop8HitRate: Double,
    val lexicalTop8HitRate: Double,
    val visibilityLeakCount: Int,
    val categorySummaries: List<RetrievalBaselineCategorySummary>,
    val results: List<RetrievalBaselineQueryResult>,
    val findings: List<String>,
    val nextAllowedStep: String,
)
