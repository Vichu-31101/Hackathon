package com.example.parking

import kotlin.math.pow
import kotlin.math.sqrt

class Coins{
    var xPos = 0f
    var yPos = 0f
    var isHit = false
    var coinSize = 25f

    fun checkCollision(x:Float, y:Float, size: Float, counter: Int): Int {
        var dist = sqrt((x - xPos).pow(2) + (y - yPos).pow(2))
        var newCounter = counter
        if(dist < (size+coinSize)){
            if(!isHit){
                newCounter = counter + 1
            }
            isHit = true

        }
        return newCounter
    }
}

class Obstacles{
    var xPos = 0f
    var yPos = 0f
    var isHit = false
    var obstacleSize = 60f

    fun checkCollision(x:Float, y:Float, size: Float): Boolean {
        var dist = sqrt((x - xPos).pow(2) + (y - yPos).pow(2))
        if(dist < (size+obstacleSize)){
            isHit = true
            return true
        }
        return  false
    }
}