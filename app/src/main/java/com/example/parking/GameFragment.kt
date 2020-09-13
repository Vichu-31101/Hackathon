package com.example.parking

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.fragment_game.view.*


class GameFragment : Fragment() {
    lateinit var canvas:MyCanvasView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this
        canvas = context?.let { MyCanvasView(it) }!!
        val params = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val rootView: View = inflater.inflate(R.layout.fragment_game, container, false)

        rootView.root.addView(canvas,params)
        return rootView
    }
}