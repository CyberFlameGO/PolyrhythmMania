package polyrhythmmania.editor

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import io.github.chrislo27.paintbox.ui.SceneRoot
import io.github.chrislo27.paintbox.util.gdxutils.disposeQuietly
import polyrhythmmania.PRManiaGame
import polyrhythmmania.PRManiaScreen


class EditorScreen(main: PRManiaGame) : PRManiaScreen(main) {

    val batch: SpriteBatch = main.batch
    val editor: Editor = Editor(main)
    private val sceneRoot: SceneRoot = editor.sceneRoot

    init {
        editor.resize(Gdx.graphics.width, Gdx.graphics.height)
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        super.render(delta)

        editor.render(delta, batch)
    }

    override fun renderUpdate() {
        super.renderUpdate()

        // FIXME DEBUG resets editor scene
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            Gdx.app.postRunnable { 
                main.screen = EditorScreen(main)
                Gdx.app.postRunnable {
                    this.dispose()
                }
            }
        }
    }

    override fun show() {
        super.show()
        main.inputMultiplexer.removeProcessor(sceneRoot.inputSystem)
        main.inputMultiplexer.addProcessor(sceneRoot.inputSystem)
    }

    override fun hide() {
        super.hide()
        main.inputMultiplexer.removeProcessor(sceneRoot.inputSystem)
    }

    override fun resize(width: Int, height: Int) {
        super.resize(width, height)
        editor.resize(width, height)
    }
    
    override fun dispose() {
        editor.dispose()
    }
}