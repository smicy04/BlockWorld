package com.blockworld.game

import android.content.Context
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import kotlin.math.max
import kotlin.math.min

class GameView(context: Context) : GLSurfaceView(context) {

    private val renderer: GameRenderer

    private var leftPointerId = -1
    private var rightPointerId = -1

    private var leftStartX = 0f
    private var leftStartY = 0f

    private var moveX = 0f
    private var moveZ = 0f

    private var lastRightX = 0f
    private var lastRightY = 0f

    init {
        setEGLContextClientVersion(2)

        renderer = GameRenderer(context)
        setRenderer(renderer)

        renderMode = RENDERMODE_CONTINUOUSLY
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {

            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_POINTER_DOWN -> {

                val index = event.actionIndex
                val id = event.getPointerId(index)
                val x = event.getX(index)
                val y = event.getY(index)

                if (x < width * 0.45f && leftPointerId == -1) {
                    leftPointerId = id
                    leftStartX = x
                    leftStartY = y
                    moveX = 0f
                    moveZ = 0f
                } else if (x >= width * 0.45f && rightPointerId == -1) {
                    rightPointerId = id
                    lastRightX = x
                    lastRightY = y
                }
            }

            MotionEvent.ACTION_MOVE -> {

                for (i in 0 until event.pointerCount) {
                    val id = event.getPointerId(i)
                    val x = event.getX(i)
                    val y = event.getY(i)

                    if (id == leftPointerId) {
                        val dx = x - leftStartX
                        val dy = y - leftStartY

                        val maxDistance = 120f

                        moveX = max(
                            -1f,
                            min(1f, dx / maxDistance)
                        )

                        moveZ = max(
                            -1f,
                            min(1f, dy / maxDistance)
                        )
                    }

                    if (id == rightPointerId) {
                        val dx = x - lastRightX
                        val dy = y - lastRightY

                        renderer.addCameraRotation(
                            dx * 0.35f,
                            dy * 0.35f
                        )

                        lastRightX = x
                        lastRightY = y
                    }
                }
            }

            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_POINTER_UP,
            MotionEvent.ACTION_CANCEL -> {

                val index = event.actionIndex
                val id = event.getPointerId(index)

                if (id == leftPointerId) {
                    leftPointerId = -1
                    moveX = 0f
                    moveZ = 0f
                }

                if (id == rightPointerId) {
                    rightPointerId = -1
                }
            }
        }

        renderer.setMovement(moveX, moveZ)

        return true
    }
}
