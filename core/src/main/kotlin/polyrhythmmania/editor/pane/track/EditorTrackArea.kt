package polyrhythmmania.editor.pane.track

import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import io.github.chrislo27.paintbox.binding.Var
import io.github.chrislo27.paintbox.ui.*
import io.github.chrislo27.paintbox.ui.area.Insets
import io.github.chrislo27.paintbox.ui.border.SolidBorder
import io.github.chrislo27.paintbox.util.gdxutils.drawRect
import io.github.chrislo27.paintbox.util.gdxutils.fillRect
import io.github.chrislo27.paintbox.util.gdxutils.grey
import polyrhythmmania.editor.Click
import polyrhythmmania.editor.Editor
import polyrhythmmania.editor.TrackView
import polyrhythmmania.editor.pane.EditorPane
import kotlin.math.floor

/**
 * The area where blocks are placed on tracks.
 *
 * Note: Does NOT support [margin], [border], or [padding]!
 * You should wrap this [EditorTrackArea] in a [Pane] to set those properties.
 */
class EditorTrackArea(val allTracksPane: AllTracksPane) : Pane() {
    val editorPane: EditorPane = allTracksPane.editorPane
    val trackView: TrackView = allTracksPane.trackView
    val editor: Editor = editorPane.editor

    private val lastMouseAbsolute: Vector2 = Vector2()
    private val lastMouseRelative: Vector2 = Vector2()
    private val tmpRect: Rectangle = Rectangle()

    init {
        this.doClipping.set(true)

        this += VerticalBeatLinesPane(editorPane)
        this += Pane().apply {
            this.border.set(Insets(0f, 2f, 0f, 0f))
            this.borderStyle.set(SolidBorder().apply { this.color.set(Color().grey(0.4f, 1f)) })
        }
    }

    init {
        addInputEventListener { event ->
            when (event) {
                is MouseMoved -> {
                    onMouseMovedOrDragged(event.x, event.y)
                    true
                }
                is TouchDragged -> {
                    onMouseMovedOrDragged(event.x, event.y)
                    true
                }
                is TouchDown -> {
                    onMouseMovedOrDragged(event.x, event.y)
                    val relMouse = lastMouseRelative
                    val mouseBeat = getBeatFromRelative(relMouse.x)
                    val mouseTrack = getTrackFromRelative(relMouse.y)

                    val blockClickedOn = editor.selectedBlocks.keys.firstOrNull { block ->
                        tmpRect.set(block.beat, block.trackIndex.toFloat(), block.width, 1f).contains(mouseBeat, mouseTrack)
                    }
                    
                    if (event.button == Input.Buttons.LEFT) {
                        // If clicking on a selected block, start dragging 
                        if (blockClickedOn != null) {
                            val newClick = Click.DragSelection.create(editor, editor.selectedBlocks.keys.toList(),
                                    Vector2(mouseBeat - blockClickedOn.beat, mouseTrack - blockClickedOn.trackIndex),
                                    blockClickedOn, false)
                            if (newClick != null) {
                                editor.click.set(newClick)
                            }
                        } else {
                            editor.click.set(Click.CreateSelection(editor, mouseBeat, mouseTrack, editor.selectedBlocks.keys.toSet()))
                        }
                    }
                    true
                }
                else -> false
            }
        }
        editor.trackView.beat.addListener {
            onMouseMovedOrDragged(lastMouseAbsolute.x, lastMouseAbsolute.y)
        }
    }

    fun onMouseMovedOrDragged(x: Float, y: Float) {
        lastMouseAbsolute.set(x, y)
        val thisPos = this.getPosRelativeToRoot(lastMouseRelative)
        lastMouseRelative.x = x - thisPos.x
        lastMouseRelative.y = y - thisPos.y

        val trackY = getTrackFromRelative(lastMouseRelative.y)
        editor.click.getOrCompute().onMouseMoved(getBeatFromRelative(lastMouseRelative.x), floor(trackY).toInt(), trackY)
    }

    fun getTrackFromRelative(relY: Float = lastMouseRelative.y): Float {
        return relY / allTracksPane.editorTrackHeight
    }

    fun getBeatFromRelative(relX: Float = lastMouseRelative.x): Float {
        return editor.trackView.translateXToBeat(relX)
    }

    fun trackToRenderY(originY: Float, track: Int): Float {
        val renderBounds = this.contentZone
        val y = originY - renderBounds.y.getOrCompute()
        return y - (track * allTracksPane.editorTrackHeight)
    }

    fun trackToRenderY(originY: Float, track: Float): Float {
        val renderBounds = this.contentZone
        val y = originY - renderBounds.y.getOrCompute()
        return y - (track * allTracksPane.editorTrackHeight)
    }

    fun beatToRenderX(originX: Float, beat: Float): Float {
        val renderBounds = this.contentZone
        val x = renderBounds.x.getOrCompute() + originX
        return x + (trackView.translateBeatToX(beat))
    }

    override fun renderSelfAfterChildren(originX: Float, originY: Float, batch: SpriteBatch) {
        super.renderSelfAfterChildren(originX, originY, batch)

//        val renderBounds = this.contentZone
//        val x = renderBounds.x.getOrCompute() + originX
//        val y = originY - renderBounds.y.getOrCompute()
//        val w = renderBounds.width.getOrCompute()
//        val h = renderBounds.height.getOrCompute()
        val trackView = editor.trackView
        val click = editor.click.getOrCompute()
        val trackHeight = allTracksPane.editorTrackHeight
        val lastPackedColor = batch.packedColor
        val tmpColor = ColorStack.getAndPush()

        // FIXME refactor out?

        // Render blocks
        editor.blocks.forEach { block ->
            val track = allTracksPane.editorTrackSides.getOrNull(block.trackIndex)
            block.render(batch, trackView, this, originX, originY, trackHeight,
                    track?.sidePanel?.sidebarBgColor?.getOrCompute() ?: Color.WHITE)
        }

        // Render drag selection outlines
        if (click is Click.DragSelection) {
            if (click.isPlacementInvalid) {
                batch.setColor(1f, 0f, 0f, 1f) 
            } else {
                batch.setColor(1f, 1f, 0f, 1f) // yellow
            }
            click.regions.entries.forEach { (block, region) ->
                val renderX = beatToRenderX(originX, region.beat)
                batch.drawRect(renderX,
                        trackToRenderY(originY, region.track) - trackHeight,
                        beatToRenderX(originX, region.beat + block.width) - renderX,
                        trackHeight, 2f)
            }
        } else if (click is Click.CreateSelection) {
            val rect = click.rectangle

            batch.setColor(0.1f, 0.75f, 0.75f, 0.333f)
            val renderX = beatToRenderX(originX, rect.x)
            batch.fillRect(renderX, trackToRenderY(originY, rect.y + rect.height),
                    beatToRenderX(originX, rect.x + rect.width) - renderX,
                    trackHeight * rect.height)
            batch.setColor(0.1f, 0.85f, 0.85f, 1f)
            batch.drawRect(renderX, trackToRenderY(originY, rect.y + rect.height),
                    beatToRenderX(originX, rect.x + rect.width) - renderX,
                    trackHeight * rect.height, 2f)
        }

        // DEBUG
        batch.setColor(1f, 0f, 0f, 1f)
        batch.fillRect(lastMouseRelative.x + (this.bounds.x.getOrCompute() + originX),
                (originY - this.bounds.y.getOrCompute()) - lastMouseRelative.y,
                5f, 5f)

        ColorStack.pop()
        batch.packedColor = lastPackedColor
    }
}