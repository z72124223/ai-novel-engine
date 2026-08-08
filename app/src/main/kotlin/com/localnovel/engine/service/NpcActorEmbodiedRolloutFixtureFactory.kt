package com.localnovel.engine.service

import com.localnovel.engine.model.NpcActorEvidenceRef
import com.localnovel.engine.model.NpcEmbodiedDecisionStep
import com.localnovel.engine.model.NpcEmbodiedEntityState
import com.localnovel.engine.model.NpcEmbodiedEnvironmentBeat
import com.localnovel.engine.model.NpcEmbodiedRolloutScenario
import com.localnovel.engine.model.NpcEmbodiedRolloutSuite
import com.localnovel.engine.model.NpcEmbodiedStateChange
import com.localnovel.engine.model.NpcEmbodiedTrajectory
import com.localnovel.engine.model.NpcEmbodiedZone

object NpcActorEmbodiedRolloutFixtureFactory {
    private val dimensions = listOf(
        "ENVIRONMENT_AWARENESS",
        "SPATIAL_COHERENCE",
        "PRIORITY_AND_TIMING",
        "SOCIAL_REALISM",
        "VOICE_NATURALNESS",
        "STATE_CONTINUITY",
        "KNOWLEDGE_BOUNDARY",
    )

    fun create(): NpcEmbodiedRolloutSuite = NpcEmbodiedRolloutSuite(
        suiteId = "npc-actor04-embodied-holdout-4-v1",
        frozenPolicyId = NpcActorCalibrationFixtureFactory.POLICY_ID,
        randomizationSeed = "npc-actor04-balanced-embodied-seed-v1",
        scenarios = listOf(mira(), yan(), lian(), ren()),
    )

    private fun mira(): NpcEmbodiedRolloutScenario {
        val zones = listOf(
            zone("hall", "前廳", listOf("service", "front"), listOf("service", "kitchen", "front")),
            zone("service", "備餐通道", listOf("hall", "kitchen", "back"), listOf("hall", "kitchen", "back")),
            zone("kitchen", "廚房", listOf("service"), listOf("service")),
            zone("front", "前門廊", listOf("hall"), listOf("hall")),
            zone("back", "後門", listOf("service"), listOf("service")),
        )
        val entities = listOf(
            entity("npc-eval-mira", "米拉", "ACTOR", "hall", "警覺地巡視前廳"),
            entity("server", "阿澄", "ACTOR", "service", "端著托盤"),
            entity("child", "咳嗽的孩子", "ACTOR", "hall", "受煙嗆而僵住"),
            entity("patron", "酒客", "ACTOR", "hall", "不知所措"),
            entity("cook", "廚工", "ACTOR", "kitchen", "高聲求援"),
            entity("bucket", "水桶", "OBJECT", "service", "裝滿水"),
            entity("blanket", "濕毯", "OBJECT", "service", "摺好放著"),
            entity("crate", "酒箱", "OBJECT", "service", "堵住通道"),
            entity("oil-pan", "油鍋", "OBJECT", "kitchen", "鍋面起火"),
            entity("smoke", "濃煙", "HAZARD", "kitchen", "持續湧入通道"),
            entity("shards", "碎玻璃", "HAZARD", "hall", "散在孩子腳邊"),
        )
        val beats = listOf(
            beat("mira-b1", 10, "廚房傳出爆響，煙從備餐通道湧出；孩子被碎玻璃困在原地。", "smoke", "oil-pan", "cook"),
            beat("mira-b2", 20, "酒箱被慌亂的人撞歪，後方通道只剩一人寬。"),
            beat("mira-b3", 30, "油鍋火舌抬高，濃煙開始壓低到肩膀高度。"),
        )
        val control = trajectory(
            "CONCISE_CONTROL_TRAJECTORY",
            step("mira-b1", listOf("child", "shards", "smoke"), "先把孩子移出碎玻璃區", "ASSIST", listOf("child"), "米拉跨過碎玻璃邊緣，把孩子拉向前門方向。", "", "她用身體擋住孩子回望廚房的視線。", changes(state("child", "受煙嗆而僵住", "被拉離碎玻璃區")), listOf("酒客終於讓出一條路。"), "孩子離開碎玻璃，前廳仍受煙影響。"),
            step("mira-b2", listOf("crate", "server", "bucket"), "清出撤離通道", "CLEAR_PATH", listOf("crate", "server"), "米拉把酒箱拖到牆邊，示意阿澄帶孩子出去。", "阿澄，把前門打開，帶孩子出去。", "她沒有停下解釋。", changes(state("crate", "堵住通道", "被拖到牆邊")), listOf("阿澄放下托盤，跑向前門。"), "備餐通道恢復一人通行。"),
            step("mira-b3", listOf("bucket", "oil-pan", "smoke"), "壓住火勢並阻止旁人接近", "SUPPRESS_FIRE", listOf("bucket", "smoke"), "米拉把水潑向油鍋周圍的燃燒木面，壓低蔓延範圍。", "退後，別碰油鍋。", "她側身守住廚房入口。", changes(state("bucket", "裝滿水", "已用盡"), state("smoke", "持續湧入通道", "湧出速度稍緩")), listOf("酒客退到前門，廚工轉身關閉爐火。"), "人群撤離中，火仍未完全熄滅但不再外竄。"),
        )
        val embodied = trajectory(
            "EMBODIED_ACTOR_TRAJECTORY",
            step("mira-b1", listOf("child", "shards", "crate", "smoke"), "同時打開通道並讓孩子有出口", "MOVE_AND_CLEAR", listOf("npc-eval-mira", "crate"), "米拉先踢開酒箱的一角，從前廳跨入備餐通道。", "", "她用手背掩住口鼻，視線先掃孩子再掃油鍋。", changes(zone("npc-eval-mira", "hall", "service"), state("crate", "堵住通道", "被推開一半")), listOf("酒客看見出口後開始扶孩子往前門移動。"), "米拉進入通道，前廳到前門的撤離線被打開。"),
            step("mira-b2", listOf("bucket", "blanket", "oil-pan", "cook"), "先封住油鍋再讓廚工斷火", "COORDINATE", listOf("blanket", "oil-pan", "cook"), "米拉抽出濕毯壓住油鍋，指向爐閥。", "孩子先走，阿澄開前門。", "她用下巴示意廚工靠近爐閥。", changes(state("blanket", "摺好放著", "覆住油鍋"), state("oil-pan", "鍋面起火", "火焰被濕毯壓住")), listOf("廚工照指示關閉爐火；阿澄帶孩子離開。"), "油鍋主火被壓住，撤離與斷火同時進行。"),
            step("mira-b3", listOf("smoke", "oil-pan", "cook"), "確認火源後從後門換氣", "VERIFY_AND_VENT", listOf("npc-eval-mira", "smoke"), "確認油鍋不再竄火後，米拉退回通道並推開後門。", "關火，其他人別進來。", "她仍守在廚房與通道交界。", changes(zone("npc-eval-mira", "service", "back"), state("smoke", "持續湧入通道", "開始由後門排出")), listOf("廚工留在爐閥旁，酒客與阿澄在前門清點人數。"), "人員分流完成，濃煙有出口，現場仍維持警戒。"),
        )
        return scenario("npc-actor04-mira-fire", "失火的旅店前廳", "CROWD_FIRE_RESPONSE", "npc-eval-mira", "米拉", "警覺、務實，遇到混亂先處理人與出口，不靠長篇命令建立威信。", "狹長旅店由前廳、備餐通道、廚房與兩個出口相連；煙、碎玻璃和堵路酒箱同時造成壓力。", zones, entities, beats, "AUTHOR_ONLY_MIRA_LEDGER_91", control, embodied)
    }

    private fun yan(): NpcEmbodiedRolloutScenario {
        val zones = listOf(
            zone("gate", "城門內側", listOf("slope", "checkpoint", "yard"), listOf("slope", "checkpoint", "yard")),
            zone("slope", "泥坡", listOf("gate", "alcove"), listOf("gate", "alcove")),
            zone("alcove", "避雨凹處", listOf("slope"), listOf("slope")),
            zone("checkpoint", "關卡棚", listOf("gate"), listOf("gate")),
            zone("yard", "門後空地", listOf("gate"), listOf("gate")),
        )
        val entities = listOf(
            entity("npc-eval-yan", "嚴", "ACTOR", "gate", "正在檢查門閂"),
            entity("courier", "急遞員", "ACTOR", "slope", "護著防水信筒"),
            entity("guard", "守衛", "ACTOR", "checkpoint", "等候命令"),
            entity("refugee", "避雨婦人", "ACTOR", "gate", "抱著包袱"),
            entity("child", "坡上的孩子", "ACTOR", "slope", "被車聲嚇住"),
            entity("cart", "失控板車", "OBJECT", "slope", "向城門滑動"),
            entity("rope", "粗繩", "OBJECT", "alcove", "盤成一圈"),
            entity("gatebar", "門閂", "OBJECT", "gate", "完全落下"),
            entity("lantern", "信號燈", "OBJECT", "checkpoint", "亮著白光"),
            entity("mud", "濕滑泥面", "HAZARD", "slope", "無法穩定站立"),
            entity("ditch", "積水溝", "HAZARD", "slope", "水位快速上升"),
        )
        val beats = listOf(
            beat("yan-b1", 5, "板車沿泥坡滑下，孩子僵在車道中間。"),
            beat("yan-b2", 15, "急遞員喊出洪水警訊，門外仍有人朝城門靠近。"),
            beat("yan-b3", 25, "雨勢加重，積水溝漫過坡腳，板車再次鬆動。", "rope", "guard"),
        )
        val control = trajectory(
            "CONCISE_CONTROL_TRAJECTORY",
            step("yan-b1", listOf("cart", "child", "mud"), "讓孩子離開板車路徑", "RESCUE", listOf("child", "cart"), "嚴把孩子拽到城門牆邊，用肩抵住板車。", "", "他的靴底在泥上打滑一下。", changes(state("child", "被車聲嚇住", "靠在城門牆邊"), state("cart", "向城門滑動", "暫時被肩抵住")), listOf("避雨婦人伸手把孩子接到身後。"), "孩子安全，板車只是暫停。"),
            step("yan-b2", listOf("courier", "gatebar", "lantern"), "核實警訊並開出窄縫", "CONTROL_ENTRY", listOf("gatebar", "guard"), "嚴抬起半截門閂，叫守衛只放急遞員進入。", "只開一人寬，先驗信筒。", "他仍用腳卡著板車輪。", changes(state("gatebar", "完全落下", "抬起一人寬")), listOf("守衛接過信筒核對封印，其他人停在門外。"), "城門未全開，警訊進入核驗流程。"),
            step("yan-b3", listOf("cart", "rope", "ditch"), "固定板車避免撞門", "SECURE", listOf("cart", "rope"), "嚴把粗繩套上車軸，另一端繫到門柱。", "守住門縫，別讓人往坡上擠。", "他拉緊繩結後才鬆開肩膀。", changes(state("rope", "盤成一圈", "繫住板車與門柱"), state("cart", "暫時被肩抵住", "固定在門柱旁")), listOf("守衛守住門縫，急遞員開始報告上游情況。"), "板車固定，城門維持受控通行。"),
        )
        val embodied = trajectory(
            "EMBODIED_ACTOR_TRAJECTORY",
            step("yan-b1", listOf("cart", "child", "mud"), "先改變孩子與板車的相對位置", "INTERCEPT", listOf("npc-eval-yan", "child"), "嚴斜切進泥坡，把孩子推進避雨凹處，自己留在板車側面。", "", "他不正面硬接，而是抓住車側框讓車頭偏向積水溝。", changes(zone("npc-eval-yan", "gate", "slope"), zone("child", "slope", "alcove")), listOf("急遞員貼牆讓路；避雨婦人在城門內招呼孩子。"), "孩子脫離車道，板車仍滑動但方向偏離城門正中。"),
            step("yan-b2", listOf("rope", "cart", "courier", "ditch"), "先固定危險，再處理警訊", "ANCHOR", listOf("rope", "cart"), "嚴從凹處扯出粗繩，繞過板車橫梁並踩住繩尾。", "把信筒給守衛，門先別全開。", "他向城門抬了一下下巴，手上沒有停。", changes(state("rope", "盤成一圈", "套住板車橫梁"), state("cart", "向城門滑動", "被繩索牽制")), listOf("急遞員把信筒拋給守衛；守衛開始驗封。"), "板車被牽制，洪水警訊同步核驗。"),
            step("yan-b3", listOf("gatebar", "cart", "ditch", "guard"), "退回城門建立受控入口", "REPOSITION", listOf("npc-eval-yan", "gatebar"), "確認繩結吃力後，嚴退回城門，把門閂抬到只容一人的高度。", "一個一個進，別踩坡中央。", "他用手指向較高的牆邊路線。", changes(zone("npc-eval-yan", "slope", "gate"), state("gatebar", "完全落下", "抬起一人寬")), listOf("守衛沿牆引導人流；避雨婦人牽住孩子。"), "危險物受控，人流改走高處，警訊進入城內。"),
        )
        return scenario("npc-actor04-yan-storm-gate", "暴雨中的城門泥坡", "SPATIAL_RESCUE_AND_ACCESS_CONTROL", "npc-eval-yan", "嚴", "寡言、判斷快，不逞強；在公共危險中會先改變位置與路線，再下短命令。", "城門、泥坡、避雨凹處與關卡棚形成高低差；板車、人流、洪水警訊和門禁必須同時處理。", zones, entities, beats, "AUTHOR_ONLY_YAN_BRIBE_42", control, embodied)
    }

    private fun lian(): NpcEmbodiedRolloutScenario {
        val zones = listOf(
            zone("aisle", "市場走道", listOf("stall", "clinic", "alley"), listOf("stall", "clinic", "alley")),
            zone("stall", "布棚攤位", listOf("aisle"), listOf("aisle", "clinic")),
            zone("clinic", "藥鋪門口", listOf("aisle"), listOf("aisle")),
            zone("alley", "側巷", listOf("aisle", "yard"), listOf("aisle", "yard")),
            zone("yard", "卸貨小院", listOf("alley"), listOf("alley")),
        )
        val entities = listOf(
            entity("npc-eval-lian", "蓮", "ACTOR", "aisle", "正在挑選布料"),
            entity("vendor", "受傷攤販", "ACTOR", "stall", "手臂流血且壓在棚布下"),
            entity("apprentice", "藥鋪學徒", "ACTOR", "clinic", "抱著藥箱發愣"),
            entity("constable", "巡市差役", "ACTOR", "alley", "正往市場趕"),
            entity("child", "找母親的孩子", "ACTOR", "aisle", "哭著擋在走道中"),
            entity("awning", "塌落棚布", "OBJECT", "stall", "壓住攤販肩膀"),
            entity("pole", "支棚木桿", "OBJECT", "stall", "底座鬆脫"),
            entity("cloth", "成捆布料", "OBJECT", "stall", "散落一地"),
            entity("medicine", "止血藥箱", "OBJECT", "clinic", "封扣完整"),
            entity("glass", "碎瓷片", "HAZARD", "aisle", "散在主要通道"),
            entity("beam", "搖晃橫梁", "HAZARD", "stall", "仍可能再次下落"),
        )
        val beats = listOf(
            beat("lian-b1", 8, "布棚突然塌下一角，攤販被壓住，碎瓷片飛入走道。"),
            beat("lian-b2", 18, "橫梁發出裂聲，孩子逆著人流尋找母親。"),
            beat("lian-b3", 28, "側巷傳來差役呼聲，攤販出血加快。", "constable"),
        )
        val control = trajectory(
            "CONCISE_CONTROL_TRAJECTORY",
            step("lian-b1", listOf("vendor", "awning", "glass"), "避免第二次砸落並接近傷者", "STABILIZE", listOf("pole", "awning"), "蓮扶正木桿，讓棚布暫時不再下沉。", "", "她先試了兩次受力才把肩膀頂上去。", changes(state("pole", "底座鬆脫", "由蓮抵住"), state("awning", "壓住攤販肩膀", "停止繼續下沉")), listOf("旁邊商販開始把碎瓷片踢離人群。"), "棚布暫穩，但蓮無法離手。"),
            step("lian-b2", listOf("child", "glass", "apprentice", "medicine"), "清出孩子與救援路線", "DIRECT", listOf("child", "apprentice"), "蓮叫孩子停下，讓學徒沿牆把孩子帶到藥鋪門口。", "你牽住他，沿牆走，藥箱帶過來。", "她的肩膀仍頂著木桿。", changes(state("child", "哭著擋在走道中", "被帶往藥鋪門口"), state("apprentice", "抱著藥箱發愣", "開始協助疏散")), listOf("學徒回過神，牽起孩子並拿穩藥箱。"), "主要走道不再被孩子堵住，藥箱正在靠近。"),
            step("lian-b3", listOf("vendor", "medicine", "constable", "beam"), "讓差役接手支撐並止血", "HANDOFF", listOf("constable", "vendor", "medicine"), "差役抵住木桿後，蓮跪到攤販身側壓住傷口。", "你頂住這裡；藥箱給我。", "她確認橫梁不再移動才鬆肩。", changes(state("pole", "由蓮抵住", "由差役固定"), state("vendor", "手臂流血且壓在棚布下", "傷口受到加壓止血"), state("medicine", "封扣完整", "已開啟使用")), listOf("差役接手支撐；學徒遞出繃帶。"), "結構與傷者各有人處理，救援形成分工。"),
        )
        val embodied = trajectory(
            "EMBODIED_ACTOR_TRAJECTORY",
            step("lian-b1", listOf("beam", "pole", "vendor", "glass"), "先取得能看見橫梁與傷者的位置", "MOVE_AND_BRACE", listOf("npc-eval-lian", "pole"), "蓮繞開碎瓷，進到棚布側邊，用木桿斜撐橫梁。", "", "她蹲低身體，讓視線保持在橫梁接點。", changes(zone("npc-eval-lian", "aisle", "stall"), state("pole", "底座鬆脫", "斜撐住橫梁")), listOf("攤販停止掙扎；旁人退開棚架正下方。"), "橫梁受力改變，傷者仍受壓但不再被二次砸落。"),
            step("lian-b2", listOf("child", "apprentice", "medicine", "beam"), "讓可移動的人先完成疏散與取藥", "COORDINATE", listOf("child", "apprentice", "medicine"), "蓮從棚內指向沒有碎瓷的藥鋪牆邊路線。", "牽孩子走牆邊，再把藥箱滑過來。", "她用鞋跟卡住木桿底端。", changes(state("child", "哭著擋在走道中", "沿牆移往藥鋪"), state("apprentice", "抱著藥箱發愣", "開始引導孩子")), listOf("學徒牽走孩子，接著把藥箱沿地面推向攤位。"), "孩子離開危險走道，藥箱抵達棚邊。"),
            step("lian-b3", listOf("constable", "vendor", "medicine", "beam"), "在不失去支撐的前提下完成接手", "TRANSFER_LOAD", listOf("npc-eval-lian", "constable", "vendor"), "蓮讓差役從外側接住木桿，確認受力後退回走道，立刻替攤販止血。", "先接我的力，再抬棚布。", "她等差役點頭才換手。", changes(zone("npc-eval-lian", "stall", "aisle"), state("pole", "斜撐住橫梁", "由差役穩定支撐"), state("vendor", "手臂流血且壓在棚布下", "傷口受到加壓止血")), listOf("差役承接木桿；學徒打開藥箱；其他商販準備抬棚布。"), "支撐交接沒有空窗，止血與抬升可同時開始。"),
        )
        return scenario("npc-actor04-lian-market-collapse", "市場布棚塌落", "STRUCTURAL_RESCUE_AND_CROWD_FLOW", "npc-eval-lian", "蓮", "細心、有同理心但不會只安慰；她會先找受力點與可用的人，再用簡短話語建立合作。", "市場走道連接攤位、藥鋪與側巷；塌棚、碎瓷、哭泣孩子和流血傷者會爭奪注意力。", zones, entities, beats, "AUTHOR_ONLY_LIAN_PARENTAGE_73", control, embodied)
    }

    private fun ren(): NpcEmbodiedRolloutScenario {
        val zones = listOf(
            zone("quay", "石砌碼頭", listOf("ramp", "warehouse", "office"), listOf("ramp", "warehouse", "office")),
            zone("ramp", "登船斜板", listOf("quay", "deck"), listOf("quay", "deck", "warehouse", "office")),
            zone("deck", "貨船甲板", listOf("ramp"), listOf("ramp")),
            zone("warehouse", "倉門", listOf("quay"), listOf("quay")),
            zone("office", "港務亭", listOf("quay"), listOf("quay")),
        )
        val entities = listOf(
            entity("npc-eval-ren", "任", "ACTOR", "quay", "正在核對卸貨次序"),
            entity("buyer", "催貨買主", "ACTOR", "quay", "要求立刻交貨"),
            entity("deckhand", "甲板水手", "ACTOR", "deck", "扶著滑動木箱"),
            entity("porter", "腳夫", "ACTOR", "ramp", "進退兩難"),
            entity("harbor-master", "港務員", "ACTOR", "office", "準備敲響警鐘"),
            entity("rope", "磨損纜繩", "OBJECT", "quay", "纖維逐條斷裂"),
            entity("gangplank", "登船斜板", "OBJECT", "ramp", "右側下沉"),
            entity("crate", "重木箱", "OBJECT", "deck", "朝斜板滑動"),
            entity("hook", "吊貨鉤", "OBJECT", "warehouse", "掛在牆架"),
            entity("tide", "急漲潮水", "HAZARD", "ramp", "拍擊船身"),
            entity("slick", "濕滑石面", "HAZARD", "quay", "靠近繫纜柱最滑"),
        )
        val beats = listOf(
            beat("ren-b1", 12, "漲潮把船身推離碼頭，磨損纜繩開始斷股。"),
            beat("ren-b2", 22, "重木箱滑向斜板，腳夫被夾在進退路線中。", "crate", "deckhand"),
            beat("ren-b3", 32, "船身再次外擺，登船斜板右側離開石階。"),
        )
        val control = trajectory(
            "CONCISE_CONTROL_TRAJECTORY",
            step("ren-b1", listOf("rope", "tide", "slick", "buyer"), "避免斷繩掃到人群", "CLEAR_HAZARD", listOf("buyer", "rope"), "任把買主推離繫纜柱，叫碼頭邊的人後退。", "", "他站在繩索可能回彈的外側。", changes(state("buyer", "要求立刻交貨", "退到安全線後"), state("rope", "纖維逐條斷裂", "無人靠近回彈區")), listOf("港務員舉起警鐘槌；腳夫停在斜板中段。"), "回彈區清空，纜繩仍未替換。"),
            step("ren-b2", listOf("crate", "porter", "gangplank"), "阻止木箱撞上腳夫", "BLOCK_CARGO", listOf("crate", "porter"), "任衝上斜板邊緣，用肩頂住木箱下角，叫腳夫退回碼頭。", "先退回來，貨晚一刻不會死。", "他把重心壓向靠碼頭的一側。", changes(state("crate", "朝斜板滑動", "被任暫時頂住"), state("porter", "進退兩難", "退回碼頭")), listOf("甲板水手抓住箱後繩環，腳夫退下斜板。"), "人員離開夾擊線，木箱暫停滑動。"),
            step("ren-b3", listOf("gangplank", "hook", "tide"), "收回失去支撐的斜板", "RETRACT", listOf("gangplank", "hook"), "任取下吊貨鉤，勾住斜板內側讓甲板水手把它拉回船上。", "拉回去，碼頭這邊清空。", "他退到石階上維持鉤索角度。", changes(state("hook", "掛在牆架", "勾住斜板內側"), state("gangplank", "右側下沉", "被拉回船上")), listOf("甲板水手收回斜板；港務員敲鐘封閉臨時通道。"), "船岸連接中止，所有人留在各自安全側。"),
        )
        val embodied = trajectory(
            "EMBODIED_ACTOR_TRAJECTORY",
            step("ren-b1", listOf("rope", "tide", "slick", "harbor-master"), "先把人與斷繩回彈面分開", "MARK_AND_SIGNAL", listOf("rope", "harbor-master"), "任把卸貨粉筆線延長成禁入線，抬手示意港務員敲鐘。", "", "他沿繩索受力方向看了一眼，選在側後方移動。", changes(state("rope", "纖維逐條斷裂", "回彈區已標示"), state("harbor-master", "準備敲響警鐘", "開始敲警示鐘")), listOf("買主被鐘聲逼停；腳夫抓住斜板扶手。"), "碼頭人流停止進入斷繩回彈區。"),
            step("ren-b2", listOf("crate", "deckhand", "porter", "gangplank"), "讓腳夫先退，甲板端再改變木箱方向", "COORDINATE_LOAD", listOf("porter", "deckhand", "crate"), "任踏上斜板靠碼頭的一端，用手勢讓水手把木箱轉向船艏。", "你退；甲板那邊，把箱角往左帶。", "他沒有站進木箱正下方。", changes(zone("npc-eval-ren", "quay", "ramp"), state("porter", "進退兩難", "退回碼頭"), state("crate", "朝斜板滑動", "被水手轉向船艏")), listOf("腳夫倒退下斜板；水手改拉左側繩環。"), "木箱離開斜板軸線，人員退出夾擊區。"),
            step("ren-b3", listOf("gangplank", "tide", "hook", "harbor-master"), "撤離斜板並切斷船岸通行", "WITHDRAW", listOf("npc-eval-ren", "gangplank", "hook"), "任退回碼頭，取吊鉤勾住斜板，讓船上收板。", "收板，這一潮先停工。", "他等雙腳回到乾石面才開始拉鉤。", changes(zone("npc-eval-ren", "ramp", "quay"), state("hook", "掛在牆架", "勾住斜板內側"), state("gangplank", "右側下沉", "被拉回甲板")), listOf("水手收板；港務員封住碼頭入口；買主不再催人上船。"), "船岸動線完全斷開，漲潮期間停止卸貨。"),
        )
        return scenario("npc-actor04-ren-dock-surge", "漲潮時的斷纜碼頭", "MOVING_PLATFORM_AND_CARGO_RISK", "npc-eval-ren", "任", "沉著、懂勞動現場，不拿命換進度；他會看受力方向、站位與人群動線。", "石碼頭、斜板、甲板、倉門與港務亭構成移動作業面；潮水、斷纜和滑動貨物會連鎖改變安全路線。", zones, entities, beats, "AUTHOR_ONLY_REN_SMUGGLING_18", control, embodied)
    }

    private fun scenario(
        id: String,
        title: String,
        category: String,
        actorId: String,
        actorLabel: String,
        brief: String,
        world: String,
        zones: List<NpcEmbodiedZone>,
        entities: List<NpcEmbodiedEntityState>,
        beats: List<NpcEmbodiedEnvironmentBeat>,
        forbiddenToken: String,
        control: NpcEmbodiedTrajectory,
        embodied: NpcEmbodiedTrajectory,
    ) = NpcEmbodiedRolloutScenario(
        scenarioId = id,
        title = title,
        category = category,
        actorId = actorId,
        actorLabel = actorLabel,
        reviewerCharacterBrief = brief,
        worldBrief = world,
        zones = zones,
        initialEntities = entities,
        environmentBeats = beats,
        actorOwnedEvidence = listOf(
            evidence("profile-$id", "PROFILE", brief, "ACTOR_OWNED"),
            evidence("world-$id", "PERCEPTION", world, "ACTOR_VISIBLE"),
        ),
        forbiddenEvidence = listOf(evidence("secret-$id", "AUTHOR_SECRET", forbiddenToken, "AUTHOR_ONLY")),
        naturalControlTrajectory = control,
        embodiedActorTrajectory = embodied,
        ratingDimensions = dimensions,
    )

    private fun zone(id: String, label: String, adjacent: List<String>, visible: List<String>) =
        NpcEmbodiedZone(id, label, adjacent, visible)

    private fun entity(id: String, label: String, type: String, zone: String, state: String) =
        NpcEmbodiedEntityState(id, label, type, zone, state)

    private fun beat(id: String, tick: Int, summary: String, vararg visibleAdded: String) =
        NpcEmbodiedEnvironmentBeat(id, tick, summary, visibleAdded.toList())

    private fun trajectory(type: String, vararg steps: NpcEmbodiedDecisionStep) = NpcEmbodiedTrajectory(type, steps.toList())

    private fun step(
        beat: String,
        perceived: List<String>,
        priority: String,
        actionType: String,
        targets: List<String>,
        action: String,
        speech: String,
        nonverbal: String,
        changes: List<NpcEmbodiedStateChange>,
        reactions: List<String>,
        result: String,
    ) = NpcEmbodiedDecisionStep(beat, perceived, priority, actionType, targets, action, speech, nonverbal, changes, reactions, result)

    private fun changes(vararg changes: NpcEmbodiedStateChange) = changes.toList()

    private fun state(entityId: String, before: String, after: String) =
        NpcEmbodiedStateChange(entityId, "state", before, after)

    private fun zone(entityId: String, before: String, after: String) =
        NpcEmbodiedStateChange(entityId, "zoneId", before, after)

    private fun evidence(id: String, kind: String, summary: String, visibility: String) =
        NpcActorEvidenceRef(id, kind, summary, visibility)
}
