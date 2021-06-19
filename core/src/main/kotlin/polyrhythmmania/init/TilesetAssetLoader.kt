package polyrhythmmania.init

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.assets.loaders.TextureLoader
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Texture
import paintbox.packing.Packable
import paintbox.packing.PackedSheet
import paintbox.packing.PackedSheetLoader
import paintbox.registry.AssetRegistry
import polyrhythmmania.soundsystem.BeadsMusic
import polyrhythmmania.soundsystem.BeadsMusicLoader
import polyrhythmmania.soundsystem.BeadsSound
import polyrhythmmania.soundsystem.BeadsSoundLoader


class TilesetAssetLoader : AssetRegistry.IAssetLoader {
    override fun addManagedAssets(manager: AssetManager) {
        fun linearTexture(): TextureLoader.TextureParameter = TextureLoader.TextureParameter().apply {
            this.magFilter = Texture.TextureFilter.Linear
            this.minFilter = Texture.TextureFilter.Linear
        }
        
        AssetRegistry.loadAsset<Texture>("tileset_gba", "textures/world/gba_spritesheet.png")

        AssetRegistry.loadAssetNoFile<PackedSheet>("tileset_parts", PackedSheetLoader.PackedSheetLoaderParam(listOf(
                "cube_border",
                "cube_face_x",
                "cube_face_y",
                "cube_face_z",
                "explosion_0",
                "explosion_1",
                "explosion_2",
                "explosion_3",
                "indicator_a",
                "indicator_dpad",
                "piston_a",
                "piston_a_extended",
                "piston_a_extended_face_x",
                "piston_a_extended_face_z",
                "piston_a_partial",
                "piston_a_partial_face_x",
                "piston_a_partial_face_z",
                "piston_dpad",
                "piston_dpad_extended",
                "piston_dpad_extended_face_x",
                "piston_dpad_extended_face_z",
                "piston_dpad_partial",
                "piston_dpad_partial_face_x",
                "piston_dpad_partial_face_z",
                "platform",
                "platform_border",
                "platform_with_line",
                "red_line",
                "rods",
                "sign_a",
                "sign_a_shadow",
                "sign_bo",
                "sign_bo_shadow",
                "sign_dpad",
                "sign_dpad_shadow",
                "sign_n",
                "sign_n_shadow",
                "sign_ta",
                "sign_ta_shadow",
        ).map { Packable(it, "textures/world/parts/$it.png") }, PackedSheet.Config(padding = 1, maxSize = 256, duplicateBorder = false)))
    }

    override fun addUnmanagedAssets(assets: MutableMap<String, Any>) {
    }
}