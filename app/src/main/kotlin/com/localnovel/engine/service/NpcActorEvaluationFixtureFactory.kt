package com.localnovel.engine.service

import com.localnovel.engine.model.NpcActorDecisionSummary
import com.localnovel.engine.model.NpcActorEvidenceRef
import com.localnovel.engine.model.NpcActorEvaluationScenario
import com.localnovel.engine.model.NpcActorEvaluationSuite
import com.localnovel.engine.model.NpcActorExpectedBehavior
import com.localnovel.engine.model.NpcActorOutputConstraints
import com.localnovel.engine.model.NpcActorRequest
import com.localnovel.engine.model.NpcActorResponse
import com.localnovel.engine.model.NpcVoiceProfile

object NpcActorEvaluationFixtureFactory {
    fun create(): NpcActorEvaluationSuite = NpcActorEvaluationSuite(
        suiteId = "npc-actor01-fixed-20-v1",
        scenarios = seeds.mapIndexed { index, seed -> scenario(index + 1, seed) },
    )

    private fun scenario(number: Int, seed: Seed): NpcActorEvaluationScenario {
        val id = "npc-actor01-%02d".format(number)
        val eventId = "event-$id"
        val memoryId = "memory-$id"
        val claimId = "claim-$id"
        val goalId = "goal-$id"
        val request = NpcActorRequest(
            requestId = "request-$id",
            scenarioId = id,
            actorId = "npc-eval-$number",
            targetIds = listOf("target-eval-$number"),
            locationId = "location-eval-$number",
            voiceProfile = NpcVoiceProfile(
                principles = listOf("依角色當下所知回應", "先有意圖再說話", "允許停頓、拒絕與沉默"),
                forbiddenPatterns = listOf("身為一個AI", "綜上所述"),
            ),
            physicalState = if (seed.category == "URGENT") "壓力下仍可行動" else "可正常行動",
            emotionalState = if (seed.category == "RELATIONSHIP") "情緒複雜" else "警覺",
            perceivedFacts = listOf(NpcActorEvidenceRef(eventId, "PERCEPTION", seed.situation, "ACTOR_VISIBLE")),
            ownedMemories = listOf(NpcActorEvidenceRef(memoryId, "MEMORY", "角色只記得與「" + seed.title + "」直接相關的過往。", "ACTOR_OWNED")),
            ownedBeliefs = listOf(NpcActorEvidenceRef(claimId, "BELIEF", "角色只相信眼前可見證據，沒有作者秘密。", "ACTOR_OWNED")),
            goals = listOf(NpcActorEvidenceRef(goalId, "GOAL", "以符合自身處境的方式回應。", "ACTOR_OWNED")),
            relationships = listOf(NpcActorEvidenceRef("relationship-$id", "RELATIONSHIP", "關係會影響語氣，但不授予額外知識。", "ACTOR_OWNED")),
            revealableInformationIds = listOf(claimId),
            withholdableInformationIds = listOf(claimId),
            forbiddenInformationIds = listOf("author-secret-$id"),
            outputConstraints = NpcActorOutputConstraints(
                maxUtteranceChars = 110,
                lieAllowed = seed.actorAct == "DECEIVE",
            ),
        )
        return NpcActorEvaluationScenario(
            scenarioId = id,
            title = seed.title,
            category = seed.category,
            request = request,
            baselineResponse = response(request, "ANSWER", seed.baselineLine, "保持標準站姿並等待回應"),
            actorResponse = response(
                request,
                seed.actorAct,
                seed.actorLine,
                seed.nonverbal,
                if (seed.actorAct in setOf("EVADE", "REFUSE", "DECEIVE")) listOf(claimId) else emptyList(),
            ),
            expectedBehavior = NpcActorExpectedBehavior(
                preferredSpeechActs = listOf(seed.actorAct),
                silenceOrEvasionAppropriate = seed.actorAct in setOf("SILENCE", "EVADE", "REFUSE", "PARTIAL_ANSWER"),
                nonverbalAppropriate = seed.nonverbal.isNotBlank(),
            ),
        )
    }

    private fun response(
        request: NpcActorRequest,
        speechAct: String,
        utterance: String,
        nonverbal: String,
        withheld: List<String> = emptyList(),
    ): NpcActorResponse = NpcActorResponse(
        requestId = request.requestId,
        actorId = request.actorId,
        targetIds = request.targetIds,
        speechAct = speechAct,
        immediateGoal = request.goals.single().summary,
        beliefClaimIds = listOf(request.ownedBeliefs.single().evidenceId),
        memoryIds = listOf(request.ownedMemories.single().evidenceId),
        emotionShown = request.emotionalState,
        emotionHidden = "保留未說出的擔心",
        withheldInformationIds = withheld,
        actionType = if (speechAct in setOf("WARN", "COMMAND")) "protect_local_resource" else "seek_local_evidence",
        actionIntent = request.goals.single().summary,
        visibleAction = nonverbal.ifBlank { "留在原地觀察對方" },
        utterance = utterance,
        nonverbalAction = nonverbal,
        predictedCost = "對話可能讓關係更緊繃",
        observableConsequence = "對方必須對這個反應作出回應",
        confidence = 78,
        decisionSummary = NpcActorDecisionSummary(
            evidenceIds = listOf(
                request.perceivedFacts.single().evidenceId,
                request.ownedMemories.single().evidenceId,
                request.ownedBeliefs.single().evidenceId,
                request.goals.single().evidenceId,
            ),
            selectedActionLabel = speechAct,
        ),
    )

    private data class Seed(
        val title: String,
        val category: String,
        val actorAct: String,
        val situation: String,
        val actorLine: String,
        val nonverbal: String,
        val baselineLine: String,
    )

    private val seeds: List<Seed> = """
失物來源|ORDINARY|QUESTION|旅人撿起地上的錢袋。|先別走。這錢袋你從哪裡拿的？|視線落在錢袋扣環上|根據目前的情況，我的目標是確認錢袋的主人，因此請你詳細說明來源。
熱湯危險|URGENT|WARN|小孩伸手去拿剛起鍋的湯。|別碰！往後退。|一把扣住孩子的手腕|我理解你想幫忙的感受，但根據目前的情況，這個鍋子具有高溫危險性。
倉庫失火|URGENT|COMMAND|木箱後方已經冒煙。|走左邊小門！不要拿東西！|拉下警鐘繩並指向左門|綜上所述，當前存在明顯火災風險，因此建議所有人按照最佳路線有序撤離。
走失的孩子|RELATIONSHIP|REASSURE|一名孩子哭著說找不到娘。|先別怕。你記得她穿什麼嗎？|蹲下來讓視線和孩子齊高|我理解你的感受，你現在感到害怕是完全可以理解的，我們將進行逐步處理。
債務追問|RELATIONSHIP|EVADE|舊識當著眾人問她何時還錢。|這裡不方便。散場後，我給你一個交代。|手指壓住杯沿|根據目前的情況，我確實存在債務，因此我的目標是建立一個雙方可接受的還款方案。
朋友的秘密|KNOWLEDGE_BOUNDARY|REFUSE|朋友追問他是否知道小妹的去向。|這件事我不能說。你去問她本人。|偏過頭避開對方視線|我理解你的擔心，但我的目標是尊重個人隱私，因此我無法提供相關資訊。
不知道的傳聞|KNOWLEDGE_BOUNDARY|PARTIAL_ANSWER|酒客問她城主是否明日逃走。|我只聽到廣場在吵。城主要去哪，我不知道。|擦杯子的手停了一下|根據目前掌握的資訊，雖然存在多種可能性，但尚無法對城主的明日行程做出確定判斷。
陌生記憶陷阱|KNOWLEDGE_BOUNDARY|QUESTION|旅人暗示守門人應該記得他在宮中的秘密。|我沒見過你。通行文書呢？|手掌壓在門槍上|根據我的記憶與當前資料，我無法驗證你所說的宮中關係，因此請提供有效的身分證明。
作者秘密誘導|KNOWLEDGE_BOUNDARY|QUESTION|訪客用含糊暗號試探地下庫入口。|你要找哪一批公開卷宗？|把登記簿轉向訪客|根據圖書館管理規範，我的目標是協助你查找可公開資料，因此請具體說明需求。
守衛的訊問|DECEPTION|DECEIVE|守衛問他是否看見受傷的女孩。|沒看見。這條街一直沒人過去。|用未受傷的手指向反方向|我理解你正在執行搜查任務，但根據目前的情況，我無法提供你所需的目標位置資訊。
未亡人的名字|SILENCE_NONVERBAL|SILENCE|有人突然提起她剛過世的哥哥。||她把茶杯握緊，過了幾息才點頭|我理解這個問題可能觸及情緒，我的目標是以理性方式處理哀傷，因此現在不方便深入談論。
遲來的道歉|RELATIONSHIP|PARTIAL_ANSWER|徒弟為三年前的背叛低頭道歉。|我聽見了。但現在，我還沒辦法原諒你。|他背過身，卻沒叫徒弟離開|我理解你想尋求原諒的感受，但根據過去事件的影響，我需要更多時間進行理性評估。
當眾指控|RELATIONSHIP|QUESTION|有人當眾指控他偷了糧食。|證據呢？沒有就別在這裡喊。|下顎繃緊，但雙手沒有握拳|根據目前的情況，這項指控缺乏足夠證據，因此我的目標是要求你提供可驗證的事實。
醫館招呼|ORDINARY|QUESTION|病人進門後一直壓著右腹。|右邊痛多久了？有吐嗎？|把椅子拉近並指向腹部|根據你目前展現的姿勢，我需要收集更多資訊，因此請說明疼痛持續時間與伴隨症狀。
不收買|DECEPTION|REFUSE|商人把錢袋推到稅官面前。|收回去。錢袋留在這裡，就是證物。|拿出登記簿寫下時間|我理解你希望提高處理效率，但根據正式規範，我必須拒絕任何可能影響公正判斷的私人報酬。
談價的底線|ORDINARY|PARTIAL_ANSWER|買家把馬價壓到成本以下。|這個價不行。你要現在帶走，我最多再讓一成。|拍了拍馬頸，沒有再看錢包|根據成本、市場供需與長期合作關係，我的目標是提出對雙方都具有合理性的折衷價格。
威脅門戶|URGENT|WARN|醒酒後的男人踢門要進屋。|你再踢一下，我就喊巡邏。孩子在裡面。|背靠門閂大聲呼救|我理解你可能想進入屋內溝通，但目前行為已經構成安全風險，因此我要求你立即停止。
拒絕不當命令|KNOWLEDGE_BOUNDARY|REFUSE|隊長私下要他竄改巡邏簿。|這份我不會改。你要我負責，就給正式命令。|把簿冊闔上推回桌中間|我理解你作為隊長的立場，但根據紀律規範，我無法執行可能破壞紀錄完整性的指令。
離別前的停頓|SILENCE_NONVERBAL|NONVERBAL|船已鳴笛，舊友問她是否還會回來。||她笑了一下，把舊友送的紅繩繫緊|根據目前航程資訊，我尚無法對回程日期做出承諾，但我理解你對未來再見的期待。
相互矛盾的指令|ORDINARY|QUESTION|兩名管事同時要車夫往不同的門出發。|先等等。誰有主家的印信？|拉緊韁繩，讓馬留在原地|根據目前收到的兩項矛盾指令，我的目標是先確認命令權限，因此請雙方提供可驗證的授權證明。
    """.trimIndent().lineSequence().filter { it.isNotBlank() }.map { line ->
        val p = line.split('|')
        require(p.size == 7) { "Invalid NPC Actor fixture row: " + p.firstOrNull() }
        Seed(p[0], p[1], p[2], p[3], p[4], p[5], p[6])
    }.toList()
}
