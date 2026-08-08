package com.localnovel.engine.service

import com.localnovel.engine.model.PhysicalActionProposal
import com.localnovel.engine.model.PhysicalActionType
import com.localnovel.engine.model.PhysicalAffordanceEvaluationCase
import com.localnovel.engine.model.PhysicalAffordanceSuite
import com.localnovel.engine.model.PhysicalArchetype
import com.localnovel.engine.model.PhysicalBarrier
import com.localnovel.engine.model.PhysicalBoundsCm
import com.localnovel.engine.model.PhysicalDimensionsCm
import com.localnovel.engine.model.PhysicalEntityInstance
import com.localnovel.engine.model.PhysicalSupportSurface
import com.localnovel.engine.model.PhysicalSupportUse
import com.localnovel.engine.model.PhysicalVector3Cm
import com.localnovel.engine.model.PhysicalWorldSnapshot

object PhysicalAffordanceFixtureFactory {
    fun createSuite(): PhysicalAffordanceSuite {
        val base = baseSnapshot()
        val snapshots = buildSnapshots(base)
        return PhysicalAffordanceSuite(
            archetypes = archetypes(),
            snapshots = snapshots,
            cases = buildCases(),
        )
    }

    private fun buildSnapshots(base: PhysicalWorldSnapshot): List<PhysicalWorldSnapshot> = listOf(
        base,
        base.withEntity("snapshot-far-chair", "chair") { it.copy(centerCm = point(500.0, 0.0, 45.0)) },
        base.withEntity("snapshot-high-chair", "chair") { it.copy(centerCm = point(50.0, 0.0, 500.0)) },
        base.copy(
            snapshotId = "snapshot-blocked-reach",
            barriers = listOf(barrier("reach-wall", point(27.0, 0.0, 100.0), dimensions(5.0, 120.0, 200.0))),
        ),
        base.withEntity("snapshot-far-light", "light-crate") { it.copy(centerCm = point(500.0, 0.0, 15.0)) },
        base.withEntity("snapshot-far-heavy", "heavy-crate") { it.copy(centerCm = point(500.0, 0.0, 30.0)) },
        base.withEntity("snapshot-overloaded-chair", "chair") {
            it.copy(supportLoadKgBySurface = mapOf("arm" to 70.0))
        },
        base.holdingLight("snapshot-holding-light"),
        base.withEntity("snapshot-unheld-light-high", "light-crate") { it.copy(centerCm = point(20.0, 0.0, 120.0)) },
        base.holdingLight("snapshot-zero-gravity-holding").copy(gravityMps2 = 0.0),
        base.holdingLight("snapshot-ground-holding").withEntity("snapshot-ground-holding", "light-crate") {
            it.copy(centerCm = point(20.0, 0.0, 15.0))
        },
        base.withEntity("snapshot-fixed-held", "statue") {
            it.copy(centerCm = point(20.0, 0.0, 200.0), heldByEntityId = "adult")
        },
        base.withEntity("snapshot-wrong-holder", "light-crate") {
            it.copy(centerCm = point(20.0, 0.0, 120.0), heldByEntityId = "child")
        },
        base.holdingLight("snapshot-overloaded-table-holding").withEntity("snapshot-overloaded-table-holding", "table") {
            it.copy(supportLoadKgBySurface = mapOf("top" to 95.0))
        },
        base.holdingLight("snapshot-far-table-holding").withEntity("snapshot-far-table-holding", "table") {
            it.copy(centerCm = point(500.0, 0.0, 40.0))
        },
        base.holdingLight("snapshot-blocked-table-holding").copy(
            barriers = listOf(barrier("table-wall", point(27.0, 10.0, 100.0), dimensions(5.0, 120.0, 200.0))),
        ),
        base.copy(
            snapshotId = "snapshot-blocked-push",
            barriers = listOf(barrier("push-wall", point(105.0, 0.0, 50.0), dimensions(5.0, 120.0, 100.0))),
        ),
        base.copy(
            snapshotId = "snapshot-blocked-entry",
            barriers = listOf(barrier("entry-wall", point(50.0, 0.0, 100.0), dimensions(5.0, 120.0, 200.0))),
        ),
    )

    private fun buildCases(): List<PhysicalAffordanceEvaluationCase> = buildList {
        add(case("support-01-chair-assist", "snapshot-base", action("a-support-01", PhysicalActionType.USE_AS_SUPPORT, "adult", "chair", surface = "arm", use = PhysicalSupportUse.STAND_UP_ASSIST), true))
        add(case("support-02-dog-shoulder-adult", "snapshot-base", action("a-support-02", PhysicalActionType.USE_AS_SUPPORT, "adult", "dog", surface = "shoulder", use = PhysicalSupportUse.STAND_UP_ASSIST), false, listOf(PhysicalAffordanceValidator.BODY_PLAN_NOT_SUPPORTED, PhysicalAffordanceValidator.SUPPORT_CAPACITY_EXCEEDED, PhysicalAffordanceValidator.SUPPORT_USE_NOT_ALLOWED, PhysicalAffordanceValidator.TARGET_NOT_STABLE_SUPPORT)))
        add(case("support-03-full-body-on-chair-arm", "snapshot-base", action("a-support-03", PhysicalActionType.USE_AS_SUPPORT, "adult", "chair", surface = "arm", use = PhysicalSupportUse.FULL_BODY_SUPPORT), false, listOf(PhysicalAffordanceValidator.SUPPORT_USE_NOT_ALLOWED)))
        add(case("support-04-overloaded-chair", "snapshot-overloaded-chair", action("a-support-04", PhysicalActionType.USE_AS_SUPPORT, "adult", "chair", surface = "arm", use = PhysicalSupportUse.STAND_UP_ASSIST), false, listOf(PhysicalAffordanceValidator.SUPPORT_CAPACITY_EXCEEDED)))
        add(case("support-05-far-chair", "snapshot-far-chair", action("a-support-05", PhysicalActionType.USE_AS_SUPPORT, "adult", "chair", surface = "arm", use = PhysicalSupportUse.STAND_UP_ASSIST), false, listOf(PhysicalAffordanceValidator.OUT_OF_REACH)))
        add(case("support-06-through-wall", "snapshot-blocked-reach", action("a-support-06", PhysicalActionType.USE_AS_SUPPORT, "adult", "chair", surface = "arm", use = PhysicalSupportUse.STAND_UP_ASSIST), false, listOf(PhysicalAffordanceValidator.REACH_BLOCKED_BY_BARRIER)))
        add(case("support-07-unknown-surface", "snapshot-base", action("a-support-07", PhysicalActionType.USE_AS_SUPPORT, "adult", "chair", surface = "back", use = PhysicalSupportUse.STAND_UP_ASSIST), false, listOf(PhysicalAffordanceValidator.SUPPORT_SURFACE_NOT_FOUND)))
        add(case("support-08-use-missing", "snapshot-base", action("a-support-08", PhysicalActionType.USE_AS_SUPPORT, "adult", "chair", surface = "arm"), false, listOf(PhysicalAffordanceValidator.SUPPORT_USE_REQUIRED)))

        add(case("reach-01-near-chair", "snapshot-base", action("a-reach-01", PhysicalActionType.REACH, "adult", "chair"), true))
        add(case("reach-02-far-chair", "snapshot-far-chair", action("a-reach-02", PhysicalActionType.REACH, "adult", "chair"), false, listOf(PhysicalAffordanceValidator.OUT_OF_REACH)))
        add(case("reach-03-high-chair", "snapshot-high-chair", action("a-reach-03", PhysicalActionType.REACH, "adult", "chair"), false, listOf(PhysicalAffordanceValidator.OUT_OF_REACH)))
        add(case("reach-04-wall", "snapshot-blocked-reach", action("a-reach-04", PhysicalActionType.REACH, "adult", "chair"), false, listOf(PhysicalAffordanceValidator.REACH_BLOCKED_BY_BARRIER)))
        add(case("reach-05-target-missing", "snapshot-base", action("a-reach-05", PhysicalActionType.REACH, "adult", "missing"), false, listOf(PhysicalAffordanceValidator.TARGET_NOT_FOUND)))
        add(case("reach-06-actor-missing", "snapshot-base", action("a-reach-06", PhysicalActionType.REACH, "missing", "chair"), false, listOf(PhysicalAffordanceValidator.ACTOR_NOT_FOUND)))
        add(case("reach-07-child-far", "snapshot-far-chair", action("a-reach-07", PhysicalActionType.REACH, "child", "chair"), false, listOf(PhysicalAffordanceValidator.OUT_OF_REACH)))
        add(case("reach-08-wall-to-rail", "snapshot-blocked-reach", action("a-reach-08", PhysicalActionType.REACH, "adult", "rail"), false, listOf(PhysicalAffordanceValidator.REACH_BLOCKED_BY_BARRIER)))

        add(case("lift-01-light-crate", "snapshot-base", action("a-lift-01", PhysicalActionType.LIFT, "adult", "light-crate"), true))
        add(case("lift-02-heavy-crate", "snapshot-base", action("a-lift-02", PhysicalActionType.LIFT, "adult", "heavy-crate"), false, listOf(PhysicalAffordanceValidator.LIFT_LIMIT_EXCEEDED)))
        add(case("lift-03-fixed-statue", "snapshot-base", action("a-lift-03", PhysicalActionType.LIFT, "adult", "statue"), false, listOf(PhysicalAffordanceValidator.LIFT_LIMIT_EXCEEDED, PhysicalAffordanceValidator.TARGET_NOT_MOVABLE)))
        add(case("lift-04-dog-no-grasp", "snapshot-base", action("a-lift-04", PhysicalActionType.LIFT, "dog", "light-crate"), false, listOf(PhysicalAffordanceValidator.GRASPING_NOT_AVAILABLE, PhysicalAffordanceValidator.LIFT_LIMIT_EXCEEDED)))
        add(case("lift-05-far-light-crate", "snapshot-far-light", action("a-lift-05", PhysicalActionType.LIFT, "adult", "light-crate"), false, listOf(PhysicalAffordanceValidator.OUT_OF_REACH)))
        add(case("lift-06-through-wall", "snapshot-blocked-reach", action("a-lift-06", PhysicalActionType.LIFT, "adult", "light-crate"), false, listOf(PhysicalAffordanceValidator.REACH_BLOCKED_BY_BARRIER)))
        add(case("lift-07-target-missing", "snapshot-base", action("a-lift-07", PhysicalActionType.LIFT, "adult", "missing"), false, listOf(PhysicalAffordanceValidator.TARGET_NOT_FOUND)))
        add(case("lift-08-child-limit", "snapshot-base", action("a-lift-08", PhysicalActionType.LIFT, "child", "light-crate"), false, listOf(PhysicalAffordanceValidator.LIFT_LIMIT_EXCEEDED)))

        add(case("push-01-heavy-crate", "snapshot-base", action("a-push-01", PhysicalActionType.PUSH, "adult", "heavy-crate", destination = point(140.0, 0.0, 30.0)), true))
        add(case("push-02-fixed-statue", "snapshot-base", action("a-push-02", PhysicalActionType.PUSH, "adult", "statue", destination = point(140.0, 0.0, 100.0)), false, listOf(PhysicalAffordanceValidator.PUSH_LIMIT_EXCEEDED, PhysicalAffordanceValidator.TARGET_NOT_MOVABLE)))
        add(case("push-03-anvil-limit", "snapshot-base", action("a-push-03", PhysicalActionType.PUSH, "adult", "anvil", destination = point(140.0, 0.0, 30.0)), false, listOf(PhysicalAffordanceValidator.PUSH_LIMIT_EXCEEDED)))
        add(case("push-04-destination-missing", "snapshot-base", action("a-push-04", PhysicalActionType.PUSH, "adult", "heavy-crate"), false, listOf(PhysicalAffordanceValidator.DESTINATION_REQUIRED)))
        add(case("push-05-path-blocked", "snapshot-blocked-push", action("a-push-05", PhysicalActionType.PUSH, "adult", "heavy-crate", destination = point(150.0, 0.0, 30.0)), false, listOf(PhysicalAffordanceValidator.MOVEMENT_BLOCKED_BY_BARRIER)))
        add(case("push-06-target-far", "snapshot-far-heavy", action("a-push-06", PhysicalActionType.PUSH, "adult", "heavy-crate", destination = point(560.0, 0.0, 30.0)), false, listOf(PhysicalAffordanceValidator.OUT_OF_REACH)))
        add(case("push-07-target-missing", "snapshot-base", action("a-push-07", PhysicalActionType.PUSH, "adult", "missing", destination = point(100.0, 0.0, 0.0)), false, listOf(PhysicalAffordanceValidator.TARGET_NOT_FOUND)))
        add(case("push-08-dog-limit", "snapshot-base", action("a-push-08", PhysicalActionType.PUSH, "dog", "heavy-crate", destination = point(140.0, 0.0, 30.0)), false, listOf(PhysicalAffordanceValidator.PUSH_LIMIT_EXCEEDED)))

        add(case("place-01-light-on-table", "snapshot-holding-light", action("a-place-01", PhysicalActionType.PLACE_ON, "adult", "light-crate", secondary = "table", surface = "top"), true))
        add(case("place-02-object-not-held", "snapshot-base", action("a-place-02", PhysicalActionType.PLACE_ON, "adult", "light-crate", secondary = "table", surface = "top"), false, listOf(PhysicalAffordanceValidator.OBJECT_NOT_HELD_BY_ACTOR)))
        add(case("place-03-support-missing", "snapshot-holding-light", action("a-place-03", PhysicalActionType.PLACE_ON, "adult", "light-crate", secondary = "missing", surface = "top"), false, listOf(PhysicalAffordanceValidator.SECONDARY_TARGET_NOT_FOUND)))
        add(case("place-04-surface-missing", "snapshot-holding-light", action("a-place-04", PhysicalActionType.PLACE_ON, "adult", "light-crate", secondary = "table", surface = "edge"), false, listOf(PhysicalAffordanceValidator.SUPPORT_SURFACE_NOT_FOUND)))
        add(case("place-05-overloaded-table", "snapshot-overloaded-table-holding", action("a-place-05", PhysicalActionType.PLACE_ON, "adult", "light-crate", secondary = "table", surface = "top"), false, listOf(PhysicalAffordanceValidator.SUPPORT_CAPACITY_EXCEEDED)))
        add(case("place-06-narrow-shelf", "snapshot-holding-light", action("a-place-06", PhysicalActionType.PLACE_ON, "adult", "light-crate", secondary = "narrow-shelf", surface = "shelf"), false, listOf(PhysicalAffordanceValidator.SUPPORT_FOOTPRINT_TOO_SMALL)))
        add(case("place-07-far-table", "snapshot-far-table-holding", action("a-place-07", PhysicalActionType.PLACE_ON, "adult", "light-crate", secondary = "table", surface = "top"), false, listOf(PhysicalAffordanceValidator.OUT_OF_REACH)))
        add(case("place-08-through-wall", "snapshot-blocked-table-holding", action("a-place-08", PhysicalActionType.PLACE_ON, "adult", "light-crate", secondary = "table", surface = "top"), false, listOf(PhysicalAffordanceValidator.REACH_BLOCKED_BY_BARRIER)))

        add(case("drop-01-held-above-ground", "snapshot-holding-light", action("a-drop-01", PhysicalActionType.DROP, "adult", "light-crate"), true, effects = listOf(PhysicalAffordanceValidator.FALL_DOWNWARD, PhysicalAffordanceValidator.RELEASE_HELD_OBJECT)))
        add(case("drop-02-not-held", "snapshot-unheld-light-high", action("a-drop-02", PhysicalActionType.DROP, "adult", "light-crate"), false, listOf(PhysicalAffordanceValidator.OBJECT_NOT_HELD_BY_ACTOR)))
        add(case("drop-03-zero-gravity", "snapshot-zero-gravity-holding", action("a-drop-03", PhysicalActionType.DROP, "adult", "light-crate"), false, listOf(PhysicalAffordanceValidator.GRAVITY_NOT_POSITIVE)))
        add(case("drop-04-no-fall-distance", "snapshot-ground-holding", action("a-drop-04", PhysicalActionType.DROP, "adult", "light-crate"), false, listOf(PhysicalAffordanceValidator.NO_FALL_DISTANCE)))
        add(case("drop-05-fixed-object", "snapshot-fixed-held", action("a-drop-05", PhysicalActionType.DROP, "adult", "statue"), false, listOf(PhysicalAffordanceValidator.TARGET_NOT_MOVABLE)))
        add(case("drop-06-target-missing", "snapshot-holding-light", action("a-drop-06", PhysicalActionType.DROP, "adult", "missing"), false, listOf(PhysicalAffordanceValidator.TARGET_NOT_FOUND)))
        add(case("drop-07-wrong-holder", "snapshot-wrong-holder", action("a-drop-07", PhysicalActionType.DROP, "adult", "light-crate"), false, listOf(PhysicalAffordanceValidator.OBJECT_NOT_HELD_BY_ACTOR)))
        add(case("drop-08-actor-missing", "snapshot-holding-light", action("a-drop-08", PhysicalActionType.DROP, "missing", "light-crate"), false, listOf(PhysicalAffordanceValidator.ACTOR_NOT_FOUND)))

        add(case("enter-01-large-container", "snapshot-base", action("a-enter-01", PhysicalActionType.ENTER_CONTAINER, "adult", "large-container"), true))
        add(case("enter-02-opening-small", "snapshot-base", action("a-enter-02", PhysicalActionType.ENTER_CONTAINER, "adult", "narrow-container"), false, listOf(PhysicalAffordanceValidator.OPENING_TOO_SMALL)))
        add(case("enter-03-interior-small", "snapshot-base", action("a-enter-03", PhysicalActionType.ENTER_CONTAINER, "adult", "small-container"), false, listOf(PhysicalAffordanceValidator.CONTAINER_INTERIOR_TOO_SMALL)))
        add(case("enter-04-load-exceeded", "snapshot-base", action("a-enter-04", PhysicalActionType.ENTER_CONTAINER, "adult", "weak-container"), false, listOf(PhysicalAffordanceValidator.CONTAINER_LOAD_EXCEEDED)))
        add(case("enter-05-not-container", "snapshot-base", action("a-enter-05", PhysicalActionType.ENTER_CONTAINER, "adult", "plain-box"), false, listOf(PhysicalAffordanceValidator.TARGET_NOT_CONTAINER)))
        add(case("enter-06-path-blocked", "snapshot-blocked-entry", action("a-enter-06", PhysicalActionType.ENTER_CONTAINER, "adult", "large-container"), false, listOf(PhysicalAffordanceValidator.MOVEMENT_BLOCKED_BY_BARRIER)))
        add(case("enter-07-target-missing", "snapshot-base", action("a-enter-07", PhysicalActionType.ENTER_CONTAINER, "adult", "missing"), false, listOf(PhysicalAffordanceValidator.TARGET_NOT_FOUND)))
        add(case("enter-08-child-small-interior", "snapshot-base", action("a-enter-08", PhysicalActionType.ENTER_CONTAINER, "child", "small-container"), false, listOf(PhysicalAffordanceValidator.CONTAINER_INTERIOR_TOO_SMALL)))

        add(case("pass-01-wide-door", "snapshot-base", action("a-pass-01", PhysicalActionType.PASS_THROUGH, "adult", "wide-door", destination = point(160.0, 0.0, 87.5)), true))
        add(case("pass-02-narrow-door", "snapshot-base", action("a-pass-02", PhysicalActionType.PASS_THROUGH, "adult", "narrow-door", destination = point(160.0, 0.0, 87.5)), false, listOf(PhysicalAffordanceValidator.OPENING_TOO_SMALL)))
        add(case("pass-03-low-door", "snapshot-base", action("a-pass-03", PhysicalActionType.PASS_THROUGH, "adult", "low-door", destination = point(160.0, 0.0, 87.5)), false, listOf(PhysicalAffordanceValidator.OPENING_TOO_SMALL)))
        add(case("pass-04-not-opening", "snapshot-base", action("a-pass-04", PhysicalActionType.PASS_THROUGH, "adult", "plain-box", destination = point(160.0, 0.0, 87.5)), false, listOf(PhysicalAffordanceValidator.TARGET_NOT_OPENING)))
        add(case("pass-05-destination-missing", "snapshot-base", action("a-pass-05", PhysicalActionType.PASS_THROUGH, "adult", "wide-door"), false, listOf(PhysicalAffordanceValidator.DESTINATION_REQUIRED)))
        add(case("pass-06-path-blocked", "snapshot-blocked-entry", action("a-pass-06", PhysicalActionType.PASS_THROUGH, "adult", "wide-door", destination = point(160.0, 0.0, 87.5)), false, listOf(PhysicalAffordanceValidator.MOVEMENT_BLOCKED_BY_BARRIER)))
        add(case("pass-07-target-missing", "snapshot-base", action("a-pass-07", PhysicalActionType.PASS_THROUGH, "adult", "missing", destination = point(160.0, 0.0, 87.5)), false, listOf(PhysicalAffordanceValidator.TARGET_NOT_FOUND)))
        add(case("pass-08-child-medium-door", "snapshot-base", action("a-pass-08", PhysicalActionType.PASS_THROUGH, "child", "medium-door", destination = point(160.0, 0.0, 60.0)), true))
    }

    private fun baseSnapshot(): PhysicalWorldSnapshot = PhysicalWorldSnapshot(
        snapshotId = "snapshot-base",
        entities = listOf(
            entity("adult", "成年角色", "adult-human", point(0.0, 0.0, 87.5)),
            entity("child", "兒童角色", "child-human", point(0.0, 10.0, 60.0)),
            entity("dog", "中型犬", "medium-dog", point(50.0, 0.0, 27.5)),
            entity("chair", "木椅", "chair", point(50.0, 0.0, 45.0)),
            entity("rail", "固定扶手", "rail", point(50.0, 10.0, 50.0)),
            entity("light-crate", "輕木箱", "light-crate", point(50.0, 0.0, 15.0)),
            entity("heavy-crate", "重木箱", "heavy-crate", point(60.0, 0.0, 30.0)),
            entity("statue", "固定石像", "statue", point(50.0, 0.0, 100.0)),
            entity("anvil", "鐵砧", "anvil", point(60.0, 0.0, 30.0)),
            entity("table", "工作桌", "table", point(55.0, 20.0, 40.0)),
            entity("narrow-shelf", "窄架", "narrow-shelf", point(55.0, -20.0, 60.0)),
            entity("large-container", "大容器", "large-container", point(100.0, 0.0, 100.0)),
            entity("narrow-container", "窄口容器", "narrow-container", point(100.0, 10.0, 100.0)),
            entity("small-container", "小容器", "small-container", point(100.0, -10.0, 75.0)),
            entity("weak-container", "弱承重容器", "weak-container", point(100.0, 20.0, 100.0)),
            entity("plain-box", "普通箱體", "plain-box", point(100.0, -20.0, 50.0)),
            entity("wide-door", "寬門洞", "wide-door", point(100.0, 0.0, 105.0)),
            entity("narrow-door", "窄門洞", "narrow-door", point(100.0, 10.0, 105.0)),
            entity("low-door", "低門洞", "low-door", point(100.0, -10.0, 50.0)),
            entity("medium-door", "中門洞", "medium-door", point(100.0, 20.0, 80.0)),
        ),
    )

    private fun archetypes(): List<PhysicalArchetype> = listOf(
        archetype("adult-human", "ACTOR", "ADULT_HUMANOID", 70.0, dimensions(45.0, 30.0, 175.0), true, true, 75.0, 30.0, 120.0),
        archetype("child-human", "ACTOR", "CHILD_HUMANOID", 25.0, dimensions(35.0, 25.0, 120.0), true, true, 50.0, 8.0, 30.0),
        archetype(
            "medium-dog", "ACTOR", "CANINE_QUADRUPED", 18.0, dimensions(30.0, 80.0, 55.0), true,
            false, 25.0, 5.0, 15.0,
            support = listOf(surface("shoulder", false, 8.0, 20.0, 30.0, listOf(PhysicalSupportUse.LIGHT_CONTACT), listOf("CANINE_QUADRUPED"))),
        ),
        archetype("chair", "OBJECT", "FURNITURE", 7.0, dimensions(55.0, 55.0, 90.0), true, support = listOf(surface("arm", true, 80.0, 20.0, 40.0, listOf(PhysicalSupportUse.LIGHT_CONTACT, PhysicalSupportUse.STAND_UP_ASSIST)))),
        archetype("rail", "OBJECT", "FIXED_STRUCTURE", 60.0, dimensions(10.0, 100.0, 100.0), false, support = listOf(surface("rail", true, 150.0, 10.0, 100.0, listOf(PhysicalSupportUse.LIGHT_CONTACT, PhysicalSupportUse.STAND_UP_ASSIST)))),
        archetype("light-crate", "OBJECT", "RIGID_BOX", 10.0, dimensions(30.0, 30.0, 30.0), true),
        archetype("heavy-crate", "OBJECT", "RIGID_BOX", 80.0, dimensions(60.0, 60.0, 60.0), true),
        archetype("statue", "OBJECT", "FIXED_STRUCTURE", 200.0, dimensions(60.0, 60.0, 200.0), false),
        archetype("anvil", "OBJECT", "RIGID_OBJECT", 160.0, dimensions(60.0, 40.0, 60.0), true),
        archetype("table", "OBJECT", "FURNITURE", 40.0, dimensions(100.0, 60.0, 80.0), false, support = listOf(surface("top", true, 100.0, 100.0, 60.0, listOf(PhysicalSupportUse.OBJECT_PLACEMENT)))),
        archetype("narrow-shelf", "OBJECT", "FIXED_STRUCTURE", 20.0, dimensions(20.0, 20.0, 120.0), false, support = listOf(surface("shelf", true, 100.0, 20.0, 20.0, listOf(PhysicalSupportUse.OBJECT_PLACEMENT)))),
        archetype("large-container", "CONTAINER", "RIGID_CONTAINER", 50.0, dimensions(100.0, 100.0, 200.0), false, interior = dimensions(80.0, 80.0, 190.0), opening = dimensions(70.0, 60.0, 185.0), capacity = 120.0),
        archetype("narrow-container", "CONTAINER", "RIGID_CONTAINER", 50.0, dimensions(100.0, 100.0, 200.0), false, interior = dimensions(80.0, 80.0, 190.0), opening = dimensions(20.0, 20.0, 185.0), capacity = 120.0),
        archetype("small-container", "CONTAINER", "RIGID_CONTAINER", 20.0, dimensions(40.0, 30.0, 150.0), false, interior = dimensions(30.0, 20.0, 150.0), opening = dimensions(60.0, 40.0, 180.0), capacity = 120.0),
        archetype("weak-container", "CONTAINER", "RIGID_CONTAINER", 20.0, dimensions(100.0, 100.0, 200.0), false, interior = dimensions(80.0, 80.0, 190.0), opening = dimensions(70.0, 60.0, 185.0), capacity = 50.0),
        archetype("plain-box", "OBJECT", "RIGID_BOX", 15.0, dimensions(60.0, 60.0, 100.0), false),
        archetype("wide-door", "OPENING", "ARCHITECTURAL_OPENING", 100.0, dimensions(100.0, 20.0, 210.0), false, opening = dimensions(90.0, 20.0, 210.0)),
        archetype("narrow-door", "OPENING", "ARCHITECTURAL_OPENING", 100.0, dimensions(30.0, 20.0, 210.0), false, opening = dimensions(20.0, 20.0, 210.0)),
        archetype("low-door", "OPENING", "ARCHITECTURAL_OPENING", 100.0, dimensions(100.0, 20.0, 100.0), false, opening = dimensions(90.0, 20.0, 100.0)),
        archetype("medium-door", "OPENING", "ARCHITECTURAL_OPENING", 100.0, dimensions(40.0, 20.0, 160.0), false, opening = dimensions(30.0, 20.0, 160.0)),
    )

    private fun case(
        id: String,
        snapshot: String,
        proposal: PhysicalActionProposal,
        accepted: Boolean,
        codes: List<String> = emptyList(),
        effects: List<String> = emptyList(),
    ) = PhysicalAffordanceEvaluationCase(id, snapshot, proposal, accepted, codes.sorted(), effects.sorted())

    private fun action(
        id: String,
        type: PhysicalActionType,
        actor: String,
        target: String,
        secondary: String? = null,
        surface: String? = null,
        use: PhysicalSupportUse? = null,
        destination: PhysicalVector3Cm? = null,
    ) = PhysicalActionProposal(id, type, actor, target, secondary, surface, use, destination)

    private fun archetype(
        id: String,
        kind: String,
        bodyPlan: String,
        mass: Double,
        dimensions: PhysicalDimensionsCm,
        movable: Boolean,
        canGrasp: Boolean = false,
        reach: Double = 0.0,
        lift: Double = 0.0,
        push: Double = 0.0,
        support: List<PhysicalSupportSurface> = emptyList(),
        interior: PhysicalDimensionsCm? = null,
        opening: PhysicalDimensionsCm? = null,
        capacity: Double? = null,
    ) = PhysicalArchetype(id, kind, bodyPlan, mass, dimensions, movable, canGrasp, reach, lift, push, support, interior, opening, capacity)

    private fun surface(
        id: String,
        stable: Boolean,
        maxLoad: Double,
        width: Double,
        depth: Double,
        uses: List<PhysicalSupportUse>,
        bodyPlans: List<String> = emptyList(),
    ) = PhysicalSupportSurface(id, stable, maxLoad, width, depth, uses, bodyPlans)

    private fun entity(id: String, label: String, archetype: String, center: PhysicalVector3Cm) =
        PhysicalEntityInstance(id, label, archetype, center)

    private fun barrier(id: String, center: PhysicalVector3Cm, dimensions: PhysicalDimensionsCm) =
        PhysicalBarrier(id, PhysicalBoundsCm(center, dimensions))

    private fun point(x: Double, y: Double, z: Double) = PhysicalVector3Cm(x, y, z)

    private fun dimensions(width: Double, depth: Double, height: Double) = PhysicalDimensionsCm(width, depth, height)

    private fun PhysicalWorldSnapshot.withEntity(
        newId: String,
        entityId: String,
        transform: (PhysicalEntityInstance) -> PhysicalEntityInstance,
    ): PhysicalWorldSnapshot = copy(
        snapshotId = newId,
        entities = entities.map { if (it.entityId == entityId) transform(it) else it },
    )

    private fun PhysicalWorldSnapshot.holdingLight(newId: String): PhysicalWorldSnapshot =
        withEntity(newId, "light-crate") {
            it.copy(centerCm = point(20.0, 0.0, 120.0), heldByEntityId = "adult")
        }
}
