package com.blockworld.game

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import kotlin.math.cos
import kotlin.math.sin

class GameRenderer(private val context: Context) : GLSurfaceView.Renderer {

    private var program = 0
    private var positionHandle = 0
    private var colorHandle = 0
    private var matrixHandle = 0

    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)

    private var width = 1
    private var height = 1

    private var cameraX = 0f
    private var cameraY = 2.8f
    private var cameraZ = 8f

    private var yaw = 180f
    private var pitch = -12f

    private var moveX = 0f
    private var moveZ = 0f

    private val cubeVertices = floatArrayOf(
        // Front
        -0.5f, -0.5f,  0.5f,
         0.5f, -0.5f,  0.5f,
         0.5f,  0.5f,  0.5f,

        -0.5f, -0.5f,  0.5f,
         0.5f,  0.5f,  0.5f,
        -0.5f,  0.5f,  0.5f,

        // Back
         0.5f, -0.5f, -0.5f,
        -0.5f, -0.5f, -0.5f,
        -0.5f,  0.5f, -0.5f,

         0.5f, -0.5f, -0.5f,
        -0.5f,  0.5f, -0.5f,
         0.5f,  0.5f, -0.5f,

        // Left
        -0.5f, -0.5f, -0.5f,
        -0.5f, -0.5f,  0.5f,
        -0.5f,  0.5f,  0.5f,

        -0.5f, -0.5f, -0.5f,
        -0.5f,  0.5f,  0.5f,
        -0.5f,  0.5f, -0.5f,

        // Right
         0.5f, -0.5f,  0.5f,
         0.5f, -0.5f, -0.5f,
         0.5f,  0.5f, -0.5f,

         0.5f, -0.5f,  0.5f,
         0.5f,  0.5f, -0.5f,
         0.5f,  0.5f,  0.5f,

        // Top
        -0.5f,  0.5f,  0.5f,
         0.5f,  0.5f,  0.5f,
         0.5f,  0.5f, -0.5f,

        -0.5f,  0.5f,  0.5f,
         0.5f,  0.5f, -0.5f,
        -0.5f,  0.5f, -0.5f,

        // Bottom
        -0.5f, -0.5f, -0.5f,
         0.5f, -0.5f, -0.5f,
         0.5f, -0.5f,  0.5f,

        -0.5f, -0.5f, -0.5f,
         0.5f, -0.5f,  0.5f,
        -0.5f, -0.5f,  0.5f
    )

    private val vertexBuffer =
        java.nio.ByteBuffer
            .allocateDirect(cubeVertices.size * 4)
            .order(java.nio.ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(cubeVertices)
                position(0)
            }

    override fun onSurfaceCreated(
        gl: javax.microedition.khronos.opengles.GL10?,
        config: javax.microedition.khronos.egl.EGLConfig?
    ) {
        GLES20.glClearColor(
            0.45f,
            0.72f,
            0.95f,
            1f
        )

        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        val vertexShader = """
            uniform mat4 uMVPMatrix;
            attribute vec4 aPosition;

            void main() {
                gl_Position = uMVPMatrix * aPosition;
            }
        """.trimIndent()

        val fragmentShader = """
            precision mediump float;

            uniform vec4 uColor;

            void main() {
                gl_FragColor = uColor;
            }
        """.trimIndent()

        val vertexShaderId = loadShader(
            GLES20.GL_VERTEX_SHADER,
            vertexShader
        )

        val fragmentShaderId = loadShader(
            GLES20.GL_FRAGMENT_SHADER,
            fragmentShader
        )

        program = GLES20.glCreateProgram()

        GLES20.glAttachShader(program, vertexShaderId)
        GLES20.glAttachShader(program, fragmentShaderId)
        GLES20.glLinkProgram(program)

        positionHandle =
            GLES20.glGetAttribLocation(
                program,
                "aPosition"
            )

        colorHandle =
            GLES20.glGetUniformLocation(
                program,
                "uColor"
            )

        matrixHandle =
            GLES20.glGetUniformLocation(
                program,
                "uMVPMatrix"
            )
    }

    override fun onSurfaceChanged(
        gl: javax.microedition.khronos.opengles.GL10?,
        width: Int,
        height: Int
    ) {
        this.width = width
        this.height = height

        GLES20.glViewport(
            0,
            0,
            width,
            height
        )

        val ratio =
            width.toFloat() / height.toFloat()

        Matrix.frustumM(
            projectionMatrix,
            0,
            -ratio,
            ratio,
            -1f,
            1f,
            0.1f,
            100f
        )
    }

    override fun onDrawFrame(
        gl: javax.microedition.khronos.opengles.GL10?
    ) {
        GLES20.glClear(
            GLES20.GL_COLOR_BUFFER_BIT or
                    GLES20.GL_DEPTH_BUFFER_BIT
        )

        updateCamera()

        Matrix.setLookAtM(
            viewMatrix,
            0,
            cameraX,
            cameraY,
            cameraZ,
            cameraX + lookX(),
            cameraY + lookY(),
            cameraZ + lookZ(),
            0f,
            1f,
            0f
        )

        drawWorld()
    }

    private fun updateCamera() {
        val speed = 0.06f

        val yawRad =
            Math.toRadians(yaw.toDouble())

        val forwardX =
            sin(yawRad).toFloat()

        val forwardZ =
            cos(yawRad).toFloat()

        val rightX =
            cos(yawRad).toFloat()

        val rightZ =
            -sin(yawRad).toFloat()

        cameraX +=
            (forwardX * moveZ + rightX * moveX) * speed

        cameraZ +=
            (forwardZ * moveZ + rightZ * moveX) * speed

        cameraX =
            cameraX.coerceIn(-20f, 20f)

        cameraZ =
            cameraZ.coerceIn(-20f, 20f)
    }

    private fun lookX(): Float {
        val yawRad =
            Math.toRadians(yaw.toDouble())

        return sin(yawRad).toFloat()
    }

    private fun lookY(): Float {
        val pitchRad =
            Math.toRadians(pitch.toDouble())

        return sin(pitchRad).toFloat()
    }

    private fun lookZ(): Float {
        val yawRad =
            Math.toRadians(yaw.toDouble())

        val pitchRad =
            Math.toRadians(pitch.toDouble())

        return (
            cos(yawRad) * cos(pitchRad)
        ).toFloat()
    }

    private fun drawWorld() {
        for (x in -10..10) {
            for (z in -10..10) {

                drawCube(
                    x.toFloat(),
                    0f,
                    z.toFloat(),
                    0.22f,
                    0.65f,
                    0.25f,
                    1f
                )

                drawCube(
                    x.toFloat(),
                    -1f,
                    z.toFloat(),
                    0.45f,
                    0.30f,
                    0.12f,
                    1f
                )
            }
        }

        // Pohon pertama
        drawTree(-4f, 1f, -3f)

        // Pohon kedua
        drawTree(4f, 1f, -6f)

        // Pohon ketiga
        drawTree(6f, 1f, 4f)
    }

    private fun drawTree(
        x: Float,
        y: Float,
        z: Float
    ) {
        // Batang
        for (i in 0..2) {
            drawCube(
                x,
                y + i,
                z,
                0.55f,
                0.30f,
                0.10f,
                1f
            )
        }

        // Daun
        for (dx in -1..1) {
            for (dz in -1..1) {
                drawCube(
                    x + dx,
                    y + 3f,
                    z + dz,
                    0.10f,
                    0.65f,
                    0.15f,
                    1f
                )
            }
        }

        drawCube(
            x,
            y + 4f,
            z,
            0.10f,
            0.70f,
            0.20f,
            1f
        )
    }

    private fun drawCube(
        x: Float,
        y: Float,
        z: Float,
        r: Float,
        g: Float,
        b: Float,
        a: Float
    ) {
        Matrix.setIdentityM(
            modelMatrix,
            0
        )

        Matrix.translateM(
            modelMatrix,
            0,
            x,
            y,
            z
        )

        Matrix.multiplyMM(
            mvpMatrix,
            0,
            viewMatrix,
            0,
            modelMatrix,
            0
        )

        Matrix.multiplyMM(
            mvpMatrix,
            0,
            projectionMatrix,
            0,
            mvpMatrix,
            0
        )

        GLES20.glUseProgram(program)

        vertexBuffer.position(0)

        GLES20.glEnableVertexAttribArray(
            positionHandle
        )

        GLES20.glVertexAttribPointer(
            positionHandle,
            3,
            GLES20.GL_FLOAT,
            false,
            3 * 4,
            vertexBuffer
        )

        GLES20.glUniform4f(
            colorHandle,
            r,
            g,
            b,
            a
        )

        GLES20.glUniformMatrix4fv(
            matrixHandle,
            1,
            false,
            mvpMatrix,
            0
        )

        GLES20.glDrawArrays(
            GLES20.GL_TRIANGLES,
            0,
            36
        )

        GLES20.glDisableVertexAttribArray(
            positionHandle
        )
    }

    fun setMovement(
        x: Float,
        z: Float
    ) {
        moveX = x
        moveZ = z
    }

    fun addCameraRotation(
        dx: Float,
        dy: Float
    ) {
        yaw -= dx
        pitch -= dy

        pitch =
            pitch.coerceIn(-80f, 80f)
    }

    private fun loadShader(
        type: Int,
        source: String
    ): Int {
        val shader =
            GLES20.glCreateShader(type)

        GLES20.glShaderSource(
            shader,
            source
        )

        GLES20.glCompileShader(shader)

        return shader
    }
}
