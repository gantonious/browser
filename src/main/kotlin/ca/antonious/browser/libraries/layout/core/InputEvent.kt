package ca.antonious.browser.libraries.layout.core

import ca.antonious.browser.libraries.graphics.core.Point

sealed class InputEvent {
    data class OnScrolled(val dy: Float) : InputEvent()
    data class KeyDown(val key: Key) : InputEvent()
    data class TouchUp(val mousePosition: Point) : InputEvent()
}

enum class Key {
    a, b, c, d, e, f, g, h, i,
    j, k, l, m, n, o, p, q, r,
    s, t, u, v, w, x, y, z,

    space,
    backspace,
    dot,
    colon,
    dash,
    underscore,
    forwardSlash,
    enter;

    val char: Char?
        get() {
            return when (this) {
                a -> 'a'
                b -> 'b'
                c -> 'c'
                d -> 'd'
                e -> 'e'
                f -> 'f'
                g -> 'g'
                h -> 'h'
                i -> 'i'
                j -> 'j'
                k -> 'k'
                l -> 'l'
                m -> 'm'
                n -> 'n'
                o -> 'o'
                p -> 'p'
                q -> 'q'
                r -> 'r'
                s -> 's'
                t -> 't'
                u -> 'u'
                v -> 'v'
                w -> 'w'
                x -> 'x'
                y -> 'y'
                z -> 'z'
                dot -> '.'
                forwardSlash -> '/'
                dash -> '-'
                underscore -> '_'
                space -> ' '
                colon -> ':'
                else -> null
            }
        }
}