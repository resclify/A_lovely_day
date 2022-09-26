package com.tastyrhino

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.physics.box2d.joints.WheelJoint
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.kotcrab.vis.ui.widget.VisImage
import com.kotcrab.vis.ui.widget.VisTextButton
import ktx.actors.alpha
import ktx.actors.centerPosition
import ktx.actors.onClick
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.box2d.body
import ktx.box2d.box
import ktx.graphics.begin
import ktx.scene2d.actors
import ktx.scene2d.vis.visImage
import ktx.scene2d.vis.visTextButton

class GameScreen : KtxScreen {
    enum class GameState {
        INTRO, COUNTER, PLAYING, GAMEOVER
    }


    var currentState = GameState.INTRO

    val worldWidth = 100f
    val world = World(Vector2(0f, -9.81f), false)
    val sb = PolygonSpriteBatch()

    private val worldStage = Stage(ExtendViewport(worldWidth, worldWidth * 1080f / 1920f))
    private val uiStage = Stage(ExtendViewport(1920f, 1080f))

    private val camera: Camera
        get() = worldStage.camera

    val debugRenderer = Box2DDebugRenderer()
    var enableDebugRenderer = false

    enum class CollisionObjectType {
        DRIVER, FINISH
    }

    lateinit var tireFront: Body
    lateinit var tireBack: Body
    lateinit var bikeBody: Body
    lateinit var jointFront: WheelJoint
    lateinit var jointBack: WheelJoint

    val frictionLand = 1f
    val bikeScaling = 0.05f
    val motorSpeed = -2500f//-1600f

    val bikeStartPosition = Vector2(0f, 11.5f).add(2.7941f, 2.875546f)

    val counterImage5: VisImage
    val counterImage4: VisImage
    val counterImage3: VisImage
    val counterImage2: VisImage
    val counterImage1: VisImage
    val counterImage0: VisImage

    val startScreen: VisImage
    val finishScreen: VisImage
    val diedScreen: VisImage

    val buttonW: VisTextButton
    val buttonS: VisTextButton
    val buttonA: VisTextButton
    val buttonD: VisTextButton

    init {
        loadPhysics()
        Gdx.input.inputProcessor = InputMultiplexer(uiStage, worldStage)
        uiStage.actors {
            counterImage5 = visImage(Texture("counter_5.png")) {
                isVisible = false
            }
            counterImage4 = visImage(Texture("counter_4.png")) {
                isVisible = false
            }
            counterImage3 = visImage(Texture("counter_3.png")) {
                isVisible = false
            }
            counterImage2 = visImage(Texture("counter_2.png")) {
                isVisible = false
            }
            counterImage1 = visImage(Texture("counter_1.png")) {
                isVisible = false
            }
            counterImage0 = visImage(Texture("counter_go.png")) {
                isVisible = false
            }

            startScreen = visImage(Texture("start_screen2.png")) {
                onClick {
                    currentState = GameState.COUNTER
                    this.isVisible = false
                    restartGame()
                }
            }

            finishScreen = visImage(Texture("finish_screen.png")) {
                isVisible = false
                onClick {
                    currentState = GameState.COUNTER
                    this.isVisible = false
                    restartGame()
                }
            }

            diedScreen = visImage(Texture("died_screen.png")) {
                isVisible = false
                onClick {
                    currentState = GameState.COUNTER
                    this.isVisible = false
                    restartGame()
                }
            }

            uiStage.actors {
                buttonW = visTextButton("W") {
                    setSize(400f, 400f)
                    isVisible = Gdx.app.type == Application.ApplicationType.Android
                    it.alpha = 0.3f
                }
                buttonS = visTextButton("S") {

                    setSize(400f, 400f)
                    isVisible = Gdx.app.type == Application.ApplicationType.Android
                    it.alpha = 0.3f
                }
                buttonA = visTextButton("A") {
                    setPosition(0f, 600f)
                    setSize(400f, 400f)
                    isVisible = Gdx.app.type == Application.ApplicationType.Android
                    it.alpha = 0.3f
                }
                buttonD = visTextButton("D") {
                    setPosition(0f, 100f)
                    setSize(400f, 400f)
                    isVisible = Gdx.app.type == Application.ApplicationType.Android
                    it.alpha = 0.3f
                }
            }

        }

        val finishBody = world.body {
            position.set(1670f, -100f)
            box(20f, 500f) {
                isSensor = true
                userData = CollisionObjectType.FINISH

            }
        }

        val cl = object : ContactListener {
            override fun beginContact(contact: Contact?) {
                if (contact?.fixtureA?.userData == CollisionObjectType.FINISH || contact?.fixtureB?.userData == CollisionObjectType.FINISH) {
                    finishScreen.isVisible = true
                    currentState = GameState.GAMEOVER
                } else if (contact?.fixtureA?.userData == CollisionObjectType.DRIVER || contact?.fixtureB?.userData == CollisionObjectType.DRIVER) {
                    diedScreen.isVisible = true
                    currentState = GameState.GAMEOVER
                }
            }

            override fun endContact(contact: Contact?) {
            }

            override fun preSolve(contact: Contact?, oldManifold: Manifold?) {
            }

            override fun postSolve(contact: Contact?, impulse: ContactImpulse?) {
            }
        }
        world.setContactListener(cl)
    }

    var counter = 0f
    fun restartGame() {
        counter = 5.5f
        bikeBody.setTransform(bikeStartPosition, 0f)
        bikeBody.setLinearVelocity(0f, 0f)
        bikeBody.angularVelocity = 0f
        tireFront.setTransform(Vector2(bikeStartPosition.x + 4f - 2.7941f, bikeStartPosition.y + 2f - 2.875546f), 0f)
        tireFront.setLinearVelocity(0f, 0f)
        tireFront.angularVelocity = 0f
        tireBack.setTransform(Vector2(bikeStartPosition.x + 1f - 2.7941f, bikeStartPosition.y + 2f - 2.875546f), 0f)
        tireBack.setLinearVelocity(0f, 0f)
        tireBack.angularVelocity = 0f

    }


    fun updateCounter(delta: Float) {
        counter -= delta
        counterImage5.isVisible = false
        counterImage4.isVisible = false
        counterImage3.isVisible = false
        counterImage2.isVisible = false
        counterImage1.isVisible = false
        counterImage0.isVisible = false


        if (counter > 5f) {
            counterImage5.isVisible = true
        } else if (counter > 4f) {
            counterImage4.isVisible = true
        } else if (counter > 3f) {
            counterImage3.isVisible = true
        } else if (counter > 2f) {
            counterImage2.isVisible = true
        } else if (counter > 1f) {
            counterImage1.isVisible = true

        } else if (counter > 0f) {
            counterImage0.isVisible = true
        } else {
            currentState = GameState.PLAYING
        }


    }

    private fun loadPhysics() {
        tireFront = PhysicsObjects.createTireFront(world, bikeStartPosition, bikeScaling)
        tireBack = PhysicsObjects.createTireBack(world, bikeStartPosition, bikeScaling)
        bikeBody = PhysicsObjects.createBikeBody(world, bikeStartPosition, bikeScaling)
        jointFront = PhysicsObjects.createJointFront(bikeBody, tireFront, tireBack, bikeScaling)
        jointBack = PhysicsObjects.createJointBack(bikeBody, tireFront, tireBack, bikeScaling)
    }

    val tireTexture = Texture("wheel.png")
    val bikeTexture = Texture("bike_body2.png")
    val tireFrontSprite = Sprite(tireTexture)
    val tireBackSprite = Sprite(tireTexture)
    val bikeSprite = Sprite(bikeTexture)
    val stoneTexture = TextureRegion(Texture("background2.png").apply {
        setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat)
    })
    val stoneTexture2 = TextureRegion(Texture("background3.png").apply {
        setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat)
    })

    val terrainPart1 = TerrainPart(stoneTexture2, physicsObject = PhysicsObjects.terrainGround1).apply {
        addBodyToWorld(
            world, frictionVal = frictionLand
        )
    }

    val terrainOverhang1 = TerrainPart(stoneTexture, physicsObject = PhysicsObjects.overhang1).apply {
        addBodyToWorld(world, frictionLand)
    }

    val terrainOverhang2 = TerrainPart(stoneTexture, physicsObject = PhysicsObjects.overhang2).apply {
        addBodyToWorld(world, frictionLand)
    }

    val terrainParts = listOf(terrainPart1, terrainOverhang1, terrainOverhang2)


    val startSign = Sprite(Texture("sign_instructions.png")).apply {
        setSize(15f, 15f * texture.height.toFloat() / texture.width.toFloat())
        setPosition(25f, 12f)
    }
    val deadTree = Sprite(Texture("dead_tree.png")).apply {
        setSize(15f, 15f * texture.height.toFloat() / texture.width.toFloat())
        setPosition(109f, 12f)
    }

    val backgroundSprites = listOf(startSign, deadTree)


    val averageFloat = AverageFloat(count = 200, startValue = 0.70f)

    private fun update(delta: Float) {
        worldStage.act()
        uiStage.act()

        if (currentState == GameState.PLAYING) {
            world.step(1 / 60f, 8, 3)
        }

        camera.position.x = bikeBody.position.x
        camera.position.y = bikeBody.position.y + 10f

        averageFloat.update(calculateCameraZoom())

        (camera as OrthographicCamera).zoom = averageFloat.average()

        camera.update(true)

        updateInput()

        if (currentState == GameState.COUNTER) {
            updateCounter(delta)
        }
    }

    private fun calculateCameraZoom(): Float {
        val bikeSpeed = bikeBody.linearVelocity.len()
        val upperLimit = 30f
        val lowerLimit = 20f
        val lowerZoom = 0.70f
        val upperZoom = 1.1f

        if (bikeSpeed < lowerLimit) {
            return lowerZoom
        }
        if (bikeSpeed > upperLimit) {
            return upperZoom
        }
        return lowerZoom + ((bikeSpeed - lowerLimit) / (upperLimit - lowerLimit)) * (upperZoom - lowerZoom)

    }

    override fun resize(width: Int, height: Int) {
        uiStage.viewport.update(width, height, false)
        worldStage.viewport.update(width, height, false)

        buttonW.setPosition(width - 400f, 600f)
        buttonS.setPosition(width - 400f, 100f)

        startScreen.centerPosition()
        diedScreen.centerPosition()
        finishScreen.centerPosition()
        counterImage5.setPosition(width * 0.5f, height * 0.75f, Align.center)
        counterImage4.setPosition(width * 0.5f, height * 0.75f, Align.center)
        counterImage3.setPosition(width * 0.5f, height * 0.75f, Align.center)
        counterImage2.setPosition(width * 0.5f, height * 0.75f, Align.center)
        counterImage1.setPosition(width * 0.5f, height * 0.75f, Align.center)
        counterImage0.setPosition(width * 0.5f, height * 0.75f, Align.center)


        super.resize(width, height)
    }

    private var buttonALastPressed = false
    private fun buttonAJustPressed() = buttonA.isPressed && !buttonALastPressed
    private var buttonDLastPressed = false
    private fun buttonDJustPressed() = buttonD.isPressed && !buttonDLastPressed

    private fun updateInput() {
        jointBack.enableMotor(false)
        jointFront.enableMotor(false)
        if ((Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP) || buttonW.isPressed) && currentState == GameState.PLAYING) {
            jointBack.enableMotor(true)
            jointBack.motorSpeed = motorSpeed * MathUtils.degreesToRadians
        }
        if ((Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN) || buttonS.isPressed) && currentState == GameState.PLAYING) {
            jointFront.enableMotor(true)
            jointBack.enableMotor(true)
            jointFront.motorSpeed = 0f
            jointBack.motorSpeed = 0f
        }
        if ((Gdx.input.isKeyJustPressed(Input.Keys.D) || Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || buttonAJustPressed()) && currentState == GameState.PLAYING) {
            bikeBody.applyAngularImpulse(-30f, true)
        }
        if ((Gdx.input.isKeyJustPressed(Input.Keys.A) || Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || buttonDJustPressed()) && currentState == GameState.PLAYING) {
            bikeBody.applyAngularImpulse(30f, true)
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) {
            enableDebugRenderer = !enableDebugRenderer
        }
        buttonALastPressed = buttonA.isPressed
        buttonDLastPressed = buttonD.isPressed
    }

    override fun render(delta: Float) {
        update(delta)

        clearScreen(111 / 255f, 76 / 255f, 76 / 255f)

        sb.begin(camera)

        terrainParts.forEach { it.polySprite.draw(sb) }
        backgroundSprites.forEach { it.draw(sb) }

        tireFrontSprite.setSize(13.5f * bikeScaling * 2f, 13.5f * bikeScaling * 2f)
        tireFrontSprite.setOriginCenter()
        tireFrontSprite.transformFromBody(tireFront)
        tireFrontSprite.draw(sb)

        tireBackSprite.setSize(13.5f * bikeScaling * 2f, 13.5f * bikeScaling * 2f)
        tireBackSprite.setOriginCenter()
        tireBackSprite.transformFromBody(tireBack)
        tireBackSprite.draw(sb)
        bikeSprite.setSize(5.5f, 3.3f)
        //.map { it.sub(Vector2(2.7941f, 2.875546f)) }
        bikeSprite.setOrigin(0.15f + 2.7941f, -1.75f + 2.875546f)
        bikeSprite.transformFromBody(bikeBody)
        bikeSprite.draw(sb)

        sb.end()

        worldStage.draw()
        uiStage.draw()

        if (enableDebugRenderer) {
            debugRenderer.render(world, worldStage.camera.combined)
        }
        super.render(delta)
    }
}

private fun Sprite.transformFromBody(body: Body) {
    setOriginBasedPosition(body.position.x, body.position.y)
    rotation = body.angle * MathUtils.radiansToDegrees
}