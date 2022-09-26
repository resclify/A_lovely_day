package com.tastyrhino

import com.badlogic.gdx.Application.LOG_DEBUG
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.kotcrab.vis.ui.VisUI
import ktx.app.KtxGame
import ktx.app.KtxScreen

/** [com.badlogic.gdx.ApplicationListener] implementation shared by all platforms. */
class ALovelyDay : KtxGame<KtxScreen>() {
    override fun create() {
        Gdx.app.logLevel = LOG_DEBUG
        VisUI.load(VisUI.SkinScale.X2)
        addScreen(GameScreen())
        setScreen<GameScreen>()
        super.create()
    }
}
