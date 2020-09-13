package com.example.parking

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat
import kotlin.math.pow
import kotlin.math.sqrt

private const val STROKE_WIDTH = 12f

class MyCanvasView(context: Context) : View(context) {


    lateinit var customHandler: Handler
    private val curPath = Path()
    var carX = -50f
    var carY = -50f
    var index = 1
    val path = mutableListOf<MutableList<Float>>()
    val coins = mutableListOf<Coins>()
    var coinsCounter = 0
    val obstacles = mutableListOf<Obstacles>()
    var gameOver = false
    var newGame = false
    var gameStart = false
    var gameWon = false
    var endText = false

    lateinit var curCanvas: Canvas
    lateinit var curBitmap: Bitmap

    private val drawColor = ResourcesCompat.getColor(resources, R.color.colorAccent, null)
    private val carColor = ResourcesCompat.getColor(resources, R.color.car, null)
    private val coinColor = ResourcesCompat.getColor(resources, R.color.coins, null)
    private val goalColor = ResourcesCompat.getColor(resources, R.color.goal, null)
    private val obstacleColor = ResourcesCompat.getColor(resources, R.color.obstacle, null)
    private val obstacleHitColor = ResourcesCompat.getColor(resources, R.color.obstaclered, null)

    // Set up the paint with which to draw.
    private val paint = Paint().apply {
        color = drawColor
        // Smooths out edges of what is drawn without affecting shape.
        isAntiAlias = true
        // Dithering affects how colors with higher-precision than the device are down-sampled.
        isDither = true
        style = Paint.Style.STROKE // default: FILL
        strokeJoin = Paint.Join.ROUND // default: MITER
        strokeCap = Paint.Cap.ROUND // default: BUTT
        strokeWidth = STROKE_WIDTH // default: Hairline-width (really thin)
        textSize = 50f
    }

    private val carPaint = Paint().apply {
        color = carColor
        // Smooths out edges of what is drawn without affecting shape.
        isAntiAlias = true
        // Dithering affects how colors with higher-precision than the device are down-sampled.
        isDither = true
        strokeJoin = Paint.Join.ROUND // default: MITER
        strokeCap = Paint.Cap.ROUND // default: BUTT
        strokeWidth = STROKE_WIDTH // default: Hairline-width (really thin)
    }

    private val coinPaint = Paint().apply {
        color = coinColor
        // Smooths out edges of what is drawn without affecting shape.
        isAntiAlias = true
        // Dithering affects how colors with higher-precision than the device are down-sampled.
        isDither = true
        strokeJoin = Paint.Join.ROUND // default: MITER
        strokeCap = Paint.Cap.ROUND // default: BUTT
        strokeWidth = STROKE_WIDTH // default: Hairline-width (really thin)
    }

    private val textPaint = Paint().apply {
        color = carColor
        // Smooths out edges of what is drawn without affecting shape.
        isAntiAlias = true
        // Dithering affects how colors with higher-precision than the device are down-sampled.
        isDither = true
        strokeJoin = Paint.Join.ROUND // default: MITER
        strokeCap = Paint.Cap.ROUND // default: BUTT
        textSize = 70f
    }

    private val obstaclePint = Paint().apply {
        color = obstacleColor
        // Smooths out edges of what is drawn without affecting shape.
        isAntiAlias = true
        // Dithering affects how colors with higher-precision than the device are down-sampled.
        isDither = true
        strokeJoin = Paint.Join.ROUND // default: MITER
        strokeCap = Paint.Cap.ROUND // default: BUTT
        strokeWidth = STROKE_WIDTH // default: Hairline-width (really thin)
    }
    private val goalPaint = Paint().apply {
        color = goalColor
        // Smooths out edges of what is drawn without affecting shape.
        isAntiAlias = true
        // Dithering affects how colors with higher-precision than the device are down-sampled.
        isDither = true
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND // default: MITER
        strokeCap = Paint.Cap.ROUND // default: BUTT
        strokeWidth = STROKE_WIDTH // default: Hairline-width (really thin)
    }

    private var currentX = 0f
    private var currentY = 0f

    private var motionTouchEventX = 0f
    private var motionTouchEventY = 0f



    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        curBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        curCanvas = Canvas(curBitmap)
        customHandler = Handler()
        reinitGame()
    }

    override fun onDraw(canvas: Canvas) {
        // Draw any current squiggle
        canvas.drawPath(curPath, paint)
        //Car
        canvas.drawCircle(carX, carY, 50f, carPaint)
        //Park
        canvas.drawCircle((width/2).toFloat(), 200f, 75f, goalPaint)
        //
        for (i in coins){
            if(!i.isHit){
                canvas.drawCircle(i.xPos, i.yPos, i.coinSize, coinPaint)
            }
        }
        canvas.drawText(coinsCounter.toString(), 50f, 75f, textPaint)

        for (i in obstacles){
            if(!i.isHit){
                obstaclePint.color = obstacleColor
            }
            else{
                obstaclePint.color = obstacleHitColor
            }
            canvas.drawCircle(i.xPos, i.yPos, i.obstacleSize, obstaclePint)
        }

        if(endText){
            if(gameWon){
                canvas.drawText("You Won!", (width/2).toFloat()-50f, (height/2).toFloat(), textPaint)
            }
        }

    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        motionTouchEventX = event.x
        motionTouchEventY = event.y

        if(gameStart){
            return false
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> touchStart()
            MotionEvent.ACTION_MOVE -> touchMove()
            MotionEvent.ACTION_UP -> touchUp()
        }
        return true
    }


    private fun touchStart() {
        customHandler.removeCallbacks(clockTick)
        if(gameOver && newGame){
            reinitGame()
            gameOver = false
            newGame = false
        }else if(gameOver || newGame){
            newGame = true
        }
        curPath.reset()
        curPath.moveTo(motionTouchEventX, motionTouchEventY)
        currentX = motionTouchEventX
        currentY = motionTouchEventY
    }

    private fun touchMove() {
        path.add(mutableListOf(motionTouchEventX, motionTouchEventY))
        curPath.quadTo(currentX, currentY, (motionTouchEventX + currentX) / 2, (motionTouchEventY + currentY) / 2)
        currentX = motionTouchEventX
        currentY = motionTouchEventY

        invalidate()
    }

    private fun touchUp() {
        // Reset the path so it doesn't get drawn again.
        gameOver = true
        gameStart = true
        curCanvas.drawPath(curPath,paint)
        curPath.reset()
        customHandler.postDelayed(clockTick,50)

    }

    private var clockTick = object: Runnable {
        var time = 50L
        override fun run() {
            endText = false
            Log.d("test",gameOver.toString())
            //
            if(index < path.size){
                carX = path[index][0]
                carY = path[index][1]
                var dist = sqrt((path[index-1][0] - carX).pow(2) + (path[index-1][1] - carY).pow(2))
                time = (1000/dist).toLong()
                time = ((time - 20)/230)*15 + 30

                index += 1
                for(i in coins){
                    coinsCounter = i.checkCollision(carX, carY, 50f, coinsCounter)
                }
                for(i in obstacles){
                    gameOver = i.checkCollision(carX, carY, 50f)
                    if(gameOver){
                        //checkWin()
                        index = path.size
                        break
                    }
                }

            }
            else{
                gameOver = true
                curBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                invalidate()
            }
            if(!gameOver){
                customHandler.postDelayed(this, time)

            }else{
                gameStart = false
                endText = true
                checkWin()
            }
            invalidate()
        }
    }

    fun reinitGame(){
        coins.clear()
        obstacles.clear()
        endText = false
        coinsCounter = 0

        for (i in (0..5)){
            var tempCoin = Coins()
            tempCoin.xPos = (10..(width-10)).random().toFloat()
            tempCoin.yPos = (250..(height-100)).random().toFloat()
            coins.add(tempCoin)
        }
        for (i in (0..3)){
            var tempObstacle = Obstacles()
            tempObstacle.xPos = (10..(width-10)).random().toFloat()
            tempObstacle.yPos = (250..(height-100)).random().toFloat()
            obstacles.add(tempObstacle)
        }

        carX = (width/2).toFloat()
        carY = height-50f
        invalidate()
    }

    fun checkWin(){
        var dist = sqrt((carX-(width/2).toFloat()).pow(2)+(carY-200f).pow(2))
        gameWon = dist < 25


    }


}