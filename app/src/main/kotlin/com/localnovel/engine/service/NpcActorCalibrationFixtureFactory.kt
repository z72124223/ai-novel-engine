package com.localnovel.engine.service

import com.localnovel.engine.model.NpcActorCalibrationCandidate
import com.localnovel.engine.model.NpcActorCalibrationPolicy
import com.localnovel.engine.model.NpcActorCalibrationPolicyRule
import com.localnovel.engine.model.NpcActorCalibrationScenario
import com.localnovel.engine.model.NpcActorCalibrationSignals
import com.localnovel.engine.model.NpcActorCalibrationSuite
import com.localnovel.engine.model.NpcActorCalibrationTurn
import com.localnovel.engine.model.NpcActorEvidenceRef

object NpcActorCalibrationFixtureFactory {
    const val POLICY_ID = "npc-actor-adaptive-expression-v1"

    fun createPolicy(): NpcActorCalibrationPolicy = NpcActorCalibrationPolicy(
        policyId = POLICY_ID,
        decisionOrder = listOf(
            "IMMEDIATE_NEED",
            "KNOWLEDGE_AND_SAFETY_BOUNDARY",
            "RELATIONSHIP_AND_AUDIENCE",
            "PRIVATE_MOTIVE_EXPRESSION_CHANNEL",
            "VOICE_INTENSITY",
        ),
        rules = listOf(
            rule("need-first", "先處理眼前的實際或關係需求，再決定是否表現個人風格。"),
            rule("action-before-exposition", "私人動機可透過行動、省略或時機表現，不必每次說破。"),
            rule("scale-by-context", "依關係距離、急迫度、在場觀眾與風險調整語氣強度。"),
            rule("neutral-is-valid", "角色在合適情境可以說中性而直接的句子。"),
            rule("preserve-withholding", "角色擁有依據時，保留沉默、遲疑、拒絕或欺瞞的可能。"),
            rule("no-signature-every-turn", "不得在每一輪都強塞招牌挖苦、威脅或比喻。"),
        ),
        claimBoundary = listOf(
            "This is one offline calibration revision over manually authored fixtures.",
            "The revealed NPC-ACTOR-02 set is development data and not holdout evidence.",
            "No live API, production rollout, Canon promotion or SQLite state write is authorized.",
        ),
    )

    fun createSuite(policyId: String = POLICY_ID): NpcActorCalibrationSuite = NpcActorCalibrationSuite(
        suiteId = "npc-actor03-adaptive-holdout-8-v1",
        policyId = policyId,
        randomizationSeed = "npc-actor03-balanced-holdout-seed-v1",
        scenarios = seeds.mapIndexed { index, seed -> scenario(index + 1, policyId, seed) },
    )

    private fun scenario(number: Int, policyId: String, seed: Seed): NpcActorCalibrationScenario {
        val id = "npc-actor03-%02d".format(number)
        val interlocutorId = "interlocutor-$id"
        return NpcActorCalibrationScenario(
            scenarioId = id,
            title = seed.title,
            category = seed.category,
            policyId = policyId,
            actorId = seed.actorId,
            actorSpeakerId = seed.actorId,
            reviewerCharacterBrief = seed.characterBrief,
            sceneContext = seed.sceneContext,
            calibrationSignals = NpcActorCalibrationSignals(
                immediateNeed = seed.immediateNeed,
                relationshipDistance = seed.relationshipDistance,
                audiencePressure = seed.audiencePressure,
                urgency = seed.urgency,
                expressionLevel = seed.expressionLevel,
            ),
            actorOwnedEvidence = listOf(
                evidence("profile-$id", "PROFILE", seed.characterBrief, "ACTOR_OWNED"),
                evidence("scene-$id", "PERCEPTION", seed.sceneContext, "ACTOR_VISIBLE"),
            ),
            forbiddenEvidence = listOf(
                evidence("author-secret-$id", "AUTHOR_SECRET", seed.forbiddenToken, "AUTHOR_ONLY"),
            ),
            sharedOpeningTurns = listOf(turn(interlocutorId, seed.interlocutorLabel, seed.openingLine)),
            naturalControlCandidate = NpcActorCalibrationCandidate(
                sourceType = "NATURAL_CONTROL_FIXTURE",
                turns = listOf(
                    turn(seed.actorId, seed.actorLabel, seed.controlLine1, seed.controlAction1),
                    turn(interlocutorId, seed.interlocutorLabel, seed.followUpLine),
                    turn(seed.actorId, seed.actorLabel, seed.controlLine2, seed.controlAction2),
                ),
            ),
            adaptiveActorCandidate = NpcActorCalibrationCandidate(
                sourceType = "ADAPTIVE_ACTOR_FIXTURE",
                turns = listOf(
                    turn(seed.actorId, seed.actorLabel, seed.adaptiveLine1, seed.adaptiveAction1),
                    turn(interlocutorId, seed.interlocutorLabel, seed.followUpLine),
                    turn(seed.actorId, seed.actorLabel, seed.adaptiveLine2, seed.adaptiveAction2),
                ),
            ),
            ratingDimensions = listOf(
                "NATURALNESS",
                "VOICE_CONSISTENCY",
                "MOTIVE_COHERENCE",
                "KNOWLEDGE_BOUNDARY",
                "CONTINUITY",
                "MULTI_TURN_REPAIR",
            ),
        )
    }

    private fun rule(id: String, instruction: String) = NpcActorCalibrationPolicyRule(id, instruction)

    private fun evidence(id: String, kind: String, summary: String, visibility: String) =
        NpcActorEvidenceRef(id, kind, summary, visibility)

    private fun turn(id: String, label: String, utterance: String, nonverbal: String = "") =
        NpcActorCalibrationTurn(id, label, utterance, nonverbal)

    private data class Seed(
        val title: String,
        val category: String,
        val actorId: String,
        val actorLabel: String,
        val interlocutorLabel: String,
        val characterBrief: String,
        val sceneContext: String,
        val immediateNeed: String,
        val relationshipDistance: String,
        val audiencePressure: String,
        val urgency: String,
        val expressionLevel: String,
        val openingLine: String,
        val followUpLine: String,
        val controlLine1: String,
        val controlAction1: String,
        val controlLine2: String,
        val controlAction2: String,
        val adaptiveLine1: String,
        val adaptiveAction1: String,
        val adaptiveLine2: String,
        val adaptiveAction2: String,
        val forbiddenToken: String,
    )

    private val seeds = listOf(
        Seed(
            "打碎的酒罈", "PUBLIC_MISTAKE_REPAIR", "npc-eval-mira", "米拉", "跑堂",
            "米拉嘴硬、眼尖，重視酒館秩序；人多時會先止損，也會用自己的方式護住手下。",
            "晚市客滿，跑堂失手打碎最後一罈陳酒，幾桌客人都看了過來。",
            "清掉碎片並處理客人", "WORKING_ALLY", "HIGH", "HIGH", "RESTRAINED",
            "掌櫃，我把最後那罈打了。", "可那是今晚最貴的一罈。",
            "先拿掃帚，別讓人踩到。", "攔住想靠近的客人",
            "損失晚點再算，先把桌上的酒補齊。", "指向後廚",
            "先掃乾淨。割傷人，帳算你頭上。", "把客人擋在碎片外",
            "貴的是酒，不是命。去拿備用小罈。", "自己蹲下撿大塊陶片",
            "跑堂其實受人指使打碎酒罈",
        ),
        Seed(
            "雨夜欠房錢", "PRACTICAL_MERCY", "npc-eval-mira", "米拉", "旅人",
            "米拉不愛空泛同情，但看得出人快撐不住時會提供能落地的交換。",
            "深夜大雨，一名渾身濕透的旅人請求賒住一晚，酒館只剩柴房可用。",
            "避免失溫並決定住宿交換", "STRANGER", "LOW", "MEDIUM", "LOW",
            "我沒錢，能讓我躲一晚嗎？", "我明早一定會走。",
            "柴房還有位置。先把濕衣服換下來。", "遞出一條乾布",
            "明早劈完門口那堆柴，就算房錢。", "把柴房鑰匙放上桌",
            "先擦乾。你病倒了，我還得多洗一床被子。", "把乾布丟過去",
            "明早劈柴抵房錢，劈完再走。", "推過柴房鑰匙",
            "旅人攜帶失竊的城防圖",
        ),
        Seed(
            "夜間藥車", "EMERGENCY_PROCEDURE", "npc-eval-yan", "嚴朔", "藥師",
            "嚴朔寡言、守程序；緊急時允許有記錄、有見證的例外，不用口氣代替查核。",
            "宵禁後，一輛藥車要進城救治高燒孩童，但通行印章尚未補蓋，值勤同僚在場。",
            "在可追溯條件下讓急救藥通行", "KNOWN_PROFESSIONAL", "MEDIUM", "HIGH", "MINIMAL",
            "城南孩子等著退燒藥，印章明早才能補。", "再拖半刻，孩子可能撐不住。",
            "開箱驗藥。同僚作證，我記下車號。", "示意同僚過來",
            "驗完先進，天亮前補印章。", "翻開夜間通行簿",
            "箱子打開。車號、藥名、去處都報一遍。", "把燈提近封條",
            "驗完放行。你天亮前回來補章。", "讓同僚在簿上簽名",
            "藥箱底層藏有逃犯",
        ),
        Seed(
            "新兵漏鎖", "ACCOUNTABLE_REPAIR", "npc-eval-yan", "嚴朔", "新兵",
            "嚴朔先控制風險，再留下責任紀錄；他不靠羞辱新兵維持紀律。",
            "清晨交班前，新兵發現北側門閂整夜沒扣好，幸而沒有入侵跡象。",
            "立即封鎖、檢查並留下紀錄", "SUBORDINATE", "LOW", "HIGH", "RESTRAINED",
            "昨晚北側門閂……我可能沒扣。別寫進簿裡行嗎？", "真的沒出事，能不能算了？",
            "先把門鎖上，再查腳印。", "拿起巡檢燈",
            "沒出事也要記。你簽名，我複核。", "翻開值勤簿",
            "扣門。你查內側，我查外側。", "把另一盞燈交給新兵",
            "不是結果替你值勤。照實寫，我在後面簽。", "把筆放到紀錄欄旁",
            "隊長昨夜偷偷打開北門",
        ),
        Seed(
            "不肯喝藥", "PATIENT_AUTONOMY", "npc-eval-lian", "連霜", "老病人",
            "連霜直白、重診斷，也尊重清醒病人的拒絕；她用替代方案處理問題，不把關心說成訓話。",
            "老病人嫌藥苦，把藥碗推開；他意識清楚，病情穩定但需要退熱。",
            "確認拒絕並提供可行替代", "LONG_TERM_PATIENT", "LOW", "LOW", "RESTRAINED",
            "太苦了，我不喝。", "妳別又說我像小孩。",
            "你可以不喝，但熱不會自己退。", "摸了摸病人的額頭",
            "我改成藥粉，分兩次吞。你選。", "把藥碗移到一旁",
            "不說。你清醒，拒絕是你的事。", "重新量了一次脈",
            "但燒要退。藥粉分兩次，或這碗一次喝完。", "拿出兩張藥紙",
            "病人其實對藥材過敏",
        ),
        Seed(
            "學徒手抖", "SKILL_SUPPORT", "npc-eval-lian", "連霜", "學徒",
            "連霜要求精準，發現學徒緊張時會先穩住操作條件，再決定是否接手。",
            "傷口已清潔，學徒第一次在病人清醒時縫合，拿針的手開始發抖。",
            "確保縫合安全並讓學徒恢復控制", "APPRENTICE", "MEDIUM", "MEDIUM", "LOW",
            "師父，我的手一直抖。", "要不還是妳來？",
            "先放下針，呼吸慢一點。", "托住學徒的手腕",
            "第一針我示範，第二針你來。", "重新調整燈的位置",
            "針先放下。手抖時別逞強。", "把針接過來放回盤中",
            "第一針我來。下一針你縫，我托住腕骨。", "把燈移到傷口正上方",
            "病人的傷口已經感染入骨",
        ),
        Seed(
            "帳冊少一頁", "PUBLIC_ACCOUNTABILITY", "npc-eval-ren", "任七", "帳房",
            "任七嘴利但懂生意；投資人在場時，他先補證據鏈，不把手下當表演用的替罪羊。",
            "投資人正在查帳，帳房低聲承認上月帳冊少了一頁，桌上仍有收據副本。",
            "重建缺頁並控制公開損害", "EMPLOYEE", "HIGH", "MEDIUM", "RESTRAINED",
            "東家，上月帳冊少了一頁。", "投資人就在旁邊，現在怎麼辦？",
            "把那日的收據和庫單都找出來。", "闔上缺頁的帳冊",
            "你重抄，我逐筆核。先別讓別人碰原冊。", "把原冊收進抽屜",
            "少一頁不會死人，少錢才會。先找收據。", "把缺頁處夾上紙籤",
            "你重建，我核對。原冊鎖起來。", "把抽屜鑰匙交給帳房",
            "投資人已收買帳房刪除證據",
        ),
        Seed(
            "舊船漏水", "HONEST_BARGAINING", "npc-eval-ren", "任七", "買船人",
            "任七會算也會挖苦，但知道隱瞞船況只會讓交易日後反咬；他把缺陷轉成可議價的明細。",
            "買船人看見船腹一處新補的漏縫，碼頭工人都在旁邊聽價。",
            "說明修補狀態並重新議價", "CUSTOMER", "MEDIUM", "LOW", "MEDIUM",
            "這裡剛補過。你原本怎麼沒說？", "補過的船還敢開這個價？",
            "三天前補的，工單和木料都在。", "把修船單遞過去",
            "你可以請別的船匠驗。驗完再談價。", "敲了敲補板邊緣",
            "舊船不是神話。補縫寫在工單上。", "把修船單展開",
            "找船匠驗。再漏，我替你罵木板。", "把槌子遞給旁邊船匠",
            "船底龍骨已經腐爛",
        ),
    )
}
