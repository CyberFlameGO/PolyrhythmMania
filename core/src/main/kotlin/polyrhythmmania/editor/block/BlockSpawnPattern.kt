package polyrhythmmania.editor.block

import io.github.chrislo27.paintbox.ui.contextmenu.ContextMenu
import polyrhythmmania.Localization
import polyrhythmmania.editor.Editor
import polyrhythmmania.engine.Event
import polyrhythmmania.world.EntityRowBlock
import polyrhythmmania.world.EventRowBlockSpawn
import polyrhythmmania.world.Row
import java.util.*


class BlockSpawnPattern(editor: Editor) : Block(editor, EnumSet.of(BlockType.INPUT)) {

    companion object {
        val ROW_COUNT: Int = 10
    }

    val patternData: PatternBlockData = PatternBlockData(ROW_COUNT)

    init {
        this.width = 4f
        this.defaultText.bind { Localization.getVar("block.spawnPattern.name").use() }
    }

    init {
        // FIXME debug, remove later
        patternData.rowATypes[0] = CubeType.PISTON
        patternData.rowATypes[2] = CubeType.PISTON
        patternData.rowATypes[4] = CubeType.PISTON
        patternData.rowATypes[6] = CubeType.PISTON
        patternData.rowATypes[8] = CubeType.PLATFORM
        patternData.rowATypes[9] = CubeType.PLATFORM
    }

    override fun compileIntoEvents(): List<Event> {
        val b = this.beat
        val events = mutableListOf<Event>()

        events += compileRow(b, patternData.rowATypes, editor.world.rowA, EntityRowBlock.Type.PISTON_A)
        events += compileRow(b, patternData.rowDpadTypes, editor.world.rowDpad, EntityRowBlock.Type.PISTON_DPAD)

        return events
    }

    private fun compileRow(beat: Float, rowArray: Array<CubeType>, row: Row, pistonType: EntityRowBlock.Type): List<Event> {
        val events = mutableListOf<Event>()

        /*
        - Pistons show up at the time based on their position
        - Platforms show up when:
          - If the section encounters a piston, when said piston appears
          - Otherwise, when the FIRST platform should appear
        - If the very last index
          - is a piston: fill in the rest of the blocks 2 after
          - is a platform: fill in the rest of the blocks at the same time
          - is nothing: fill in the rest of the blocks at the time of the first end index
        */

        val timings: FloatArray = FloatArray(rowArray.size) { it * 0.5f }

        var index: Int = 0
        while (index in 0 until rowArray.size) {
            val type = rowArray[index]
            if (type == CubeType.PLATFORM && index < rowArray.size - 1) {
                // Find contiguous section
                var subindex = index + 1
                var endType: CubeType = rowArray[subindex]
                while (subindex in 0 until rowArray.size) {
                    endType = rowArray[subindex]
                    if (endType != CubeType.PLATFORM) break
                    subindex++
                }
                
                when (endType) {
                    CubeType.PISTON -> {
                        for (i in index until subindex) {
                            timings[i] = timings[subindex]
                        }
                        index = subindex
                    }
                    else -> {
                        for (i in index until subindex) {
                            timings[i] = timings[index]
                        }
                        index = subindex
                    }
                }
            }
            
            index++
        }

        var anyNotNone = false
        timings.forEachIndexed { ind, b ->
            val cube = rowArray[ind]
            if (cube != CubeType.NONE) {
                anyNotNone = true
                events += EventRowBlockSpawn(editor.engine, row, ind,
                        if (cube == CubeType.PLATFORM) EntityRowBlock.Type.PLATFORM else pistonType,
                        beat + b)
            }
            
            if (ind == timings.size - 1 && anyNotNone) {
                when (cube) {
                    CubeType.NONE -> {
                        val next = ind + 1
                        if (next < row.length) {
                            events += EventRowBlockSpawn(editor.engine, row, next, EntityRowBlock.Type.PLATFORM,
                                    beat + next * 0.5f, affectThisIndexAndForward = true)
                        }
                    }
                    CubeType.PLATFORM -> {
                        val next = ind + 1
                        if (next < row.length) {
                            events += EventRowBlockSpawn(editor.engine, row, next, EntityRowBlock.Type.PLATFORM,
                                    beat + b, affectThisIndexAndForward = true)
                        }
                    }
                    CubeType.PISTON -> {
                        val next = ind + 2
                        if (next < row.length) {
                            events += EventRowBlockSpawn(editor.engine, row, next, EntityRowBlock.Type.PLATFORM,
                                    beat + next * 0.5f, affectThisIndexAndForward = true)
                        }
                    }
                }
            }
        }

        return events
    }

    override fun createContextMenu(): ContextMenu {
        return ContextMenu().also { ctxmenu ->
            patternData.createMenuItems(editor).forEach { ctxmenu.addMenuItem(it) }
        }
    }

    override fun copy(): BlockSpawnPattern {
        return BlockSpawnPattern(editor).also {
            this.copyBaseInfoTo(it)
            for (i in 0 until ROW_COUNT) {
                it.patternData.rowATypes[i] = this.patternData.rowATypes[i]
                it.patternData.rowDpadTypes[i] = this.patternData.rowDpadTypes[i]
            }
        }
    }
}