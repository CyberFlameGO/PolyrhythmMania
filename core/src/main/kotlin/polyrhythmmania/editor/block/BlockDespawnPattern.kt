package polyrhythmmania.editor.block

import io.github.chrislo27.paintbox.binding.Var
import io.github.chrislo27.paintbox.ui.contextmenu.ContextMenu
import polyrhythmmania.Localization
import polyrhythmmania.editor.Editor
import polyrhythmmania.engine.Event
import polyrhythmmania.world.EventRowBlockDespawn
import java.util.*


class BlockDespawnPattern(editor: Editor) : Block(editor, EnumSet.of(BlockType.INPUT)) {

    val rowData: RowBlockData = RowBlockData()
    
    init {
        this.width = 1f
        val text = Localization.getVar("block.despawnPattern.name", Var.bind {
            rowData.getSymbol(this)
        })
        this.defaultText.bind { text.use() }
    }
    
    override fun compileIntoEvents(): List<Event> {
        val b = this.beat
        return RowSetting.getRows(rowData.rowSetting.getOrCompute(), editor.world).map { row ->
            EventRowBlockDespawn(editor.engine, row, -1, b)
        }
    }

    override fun createContextMenu(): ContextMenu {
        return ContextMenu().also { ctxmenu ->
            rowData.createMenuItems(editor).forEach { ctxmenu.addMenuItem(it) }
        }
    }

    override fun copy(): BlockDespawnPattern {
        return BlockDespawnPattern(editor).also { 
            this.copyBaseInfoTo(it)
            it.rowData.rowSetting.set(this.rowData.rowSetting.getOrCompute())
        }
    }
}