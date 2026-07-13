package com.localnovel.engine.cli

import com.localnovel.engine.service.RetrievalExactBaselineService
import java.nio.file.Path

internal object RetrievalBenchmarkCli {
    fun benchmarkExactBaseline(options: Map<String, String>): Int {
        val report = RetrievalExactBaselineService(cliRepository(options)).evaluate(
            querySetPath = Path.of(options.requiredCliOption("queries")),
            topK = options["top-k"]?.toIntOrNull() ?: 8,
        )
        val output = Path.of(options.requiredCliOption("out"))
        writeCliJson(output, report)
        println("verdict=${report.verdict}")
        println("queries=${report.queryCount}")
        println("retrievable=${report.retrievableCount}")
        println("blockedByPolicy=${report.blockedByPolicyCount}")
        println("corpusGaps=${report.corpusGapCount}")
        println("likeTop8HitRate=${report.likeTop8HitRate}")
        println("lexicalTop8HitRate=${report.lexicalTop8HitRate}")
        println("visibilityLeaks=${report.visibilityLeakCount}")
        println("wrote=$output")
        return if (report.verdict == "PASS") 0 else 1
    }
}
