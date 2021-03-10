package ca.antonious.browser.libraries.layout.libgdx

import ca.antonious.browser.libraries.layout.core.Key
import com.badlogic.gdx.Input

val Int.key: Key
    get() {
        return when (this) {
            Input.Keys.A -> Key.a
            Input.Keys.B -> Key.b
            Input.Keys.C -> Key.c
            Input.Keys.D -> Key.d
            Input.Keys.E -> Key.e
            Input.Keys.F -> Key.f
            Input.Keys.G -> Key.g
            Input.Keys.H -> Key.h
            Input.Keys.I -> Key.i
            Input.Keys.J -> Key.j
            Input.Keys.K -> Key.k
            Input.Keys.L -> Key.l
            Input.Keys.M -> Key.m
            Input.Keys.N -> Key.n
            Input.Keys.O -> Key.o
            Input.Keys.P -> Key.p
            Input.Keys.Q -> Key.q
            Input.Keys.R -> Key.r
            Input.Keys.S -> Key.s
            Input.Keys.T -> Key.t
            Input.Keys.U -> Key.u
            Input.Keys.V -> Key.v
            Input.Keys.W -> Key.w
            Input.Keys.X -> Key.x
            Input.Keys.Y -> Key.y
            Input.Keys.Z -> Key.z
            Input.Keys.SPACE -> Key.space
            Input.Keys.BACKSPACE -> Key.backspace
            Input.Keys.SLASH -> Key.forwardSlash
            Input.Keys.PERIOD -> Key.dot
            Input.Keys.ENTER -> Key.enter
            Input.Keys.COLON -> Key.colon
            Input.Keys.DOWN -> Key.ArrowDown
            Input.Keys.UP -> Key.ArrowUp
            Input.Keys.LEFT -> Key.ArrowLeft
            Input.Keys.RIGHT -> Key.ArrowRight
            else -> Key.space
        }
    }