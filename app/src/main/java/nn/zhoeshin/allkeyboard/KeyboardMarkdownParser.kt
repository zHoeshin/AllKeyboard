package nn.zhoeshin.allkeyboard

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser
import org.apache.commons.text.StringEscapeUtils

enum class Direction {
    LEFT, RIGHT, UP, DOWN, ADD, NO
}
data class State(val name: String, val keys: List<KeyData>)
data class Directed<T>(val value: T, val dir: Direction)

object KeyboardMarkdownParser : Grammar<MutableMap<String, List<KeyData>>>() {
    //val GRID by literalToken("grid")
    //val CELL by literalToken("cell")
    val AT by literalToken("@")
    val L by literalToken("<")
    val R by literalToken(">")
    val U by literalToken("^")
    val D by literalToken("v")
    val TAP by literalToken(".")
    val UNDERSCORE by literalToken("_")
    val EXCLAM by literalToken("!")
    val COLON by literalToken(":")
    val SIGIL by literalToken("#")
    val AS by literalToken("as")

    val NUMERIC by regexToken("""\d+""")
    val COMMA by literalToken(",")
    val numericcomma by (NUMERIC * -COMMA * NUMERIC).map {(n1, n2) -> (n1.text + "." + n2.text).toFloat()}
    val numeric by (NUMERIC * -TAP * NUMERIC).map {(n1, n2) -> (n1.text + "." + n2.text).toFloat()} or (NUMERIC).map{n -> n.text.toFloat()}
    val lbrace by literalToken("{")
    val rbrace by literalToken("}")
    val lbracket by literalToken("[")
    val rbracket by literalToken("]")
    val lparen by literalToken("(")
    val rparen by literalToken(")")
    val STRING by regexToken(""""((\\.)|[^"])*"""")
    val string by STRING.map {s -> StringEscapeUtils.unescapeJava(s.text.substring(1, s.text.length - 1))}
    val IDENTIFIER by regexToken("""[^\s\n\r\t {}!\[\]()":]+""")
    val identifier by IDENTIFIER.map { s -> s.text}
    val WHITESPACE by regexToken("""[\s\n\r\t ]+""", ignore=true)

    //val ANY by regexToken(".")

    private var gridWidth = 0f
    private var gridHeight = 0f
    private var cellWidth = 0.1f
    private var cellHeight = 0.2f

    val numberx: Parser<Float> = (numericcomma).map{n -> n} or (numeric).map{n -> if (gridWidth == 0f) {n} else {n / gridWidth}}
    val numbery: Parser<Float> = (numericcomma).map{n -> n} or (numeric).map{n -> if (gridHeight == 0f) {n} else {n / gridHeight}}

    

    val directedString: Parser<Directed<String>> =
        (-L * string).map { s: String -> Directed(s, Direction.LEFT) } or
        (-R * string).map { s: String -> Directed(s, Direction.RIGHT) } or
        (-U * string).map { s: String -> Directed(s, Direction.UP) } or
        (-D * string).map { s: String -> Directed(s, Direction.DOWN) } or
        (-TAP * string).map { s: String -> Directed(s, Direction.NO) } or
        (-UNDERSCORE * string).map { s: String -> Directed(s, Direction.ADD) }

    val label: Parser<KeyLabel> = (-optional(TAP) * string * zeroOrMore(directedString)).map { (s, d) ->
        var l = KeyLabel(s)
        for(ds in d) {
            when (ds.dir) {
                Direction.UP -> l.up = ds.value
                Direction.DOWN -> l.down = ds.value
                Direction.LEFT -> l.left = ds.value
                Direction.RIGHT -> l.right = ds.value
                Direction.ADD -> l.corner = ds.value
                Direction.NO -> l.center = ds.value
            }
        }
        l
    }

    val oneKeyAction: Parser<KeyAction> = (string).map{s -> KeyAction(KeyActionType.STRING, s)} or
            (-R * identifier * -EXCLAM).map{ i -> KeyAction(KeyActionType.TRANSITIONPERM, i)} or
            (-R * identifier).map{ i -> KeyAction(KeyActionType.TRANSITIONTEMP, i)} or
            (-SIGIL * identifier).map{ i -> KeyAction(KeyActionType.MAGIC, i)}

    val keyAction: Parser<KeyActions> = (optional(identifier) * oneKeyAction).map{(f, ka) ->
        KeyActions(listOf(ka), flags=KeyActionFlags(
            immediate = f?.contains("i") ?: false,
            repeated = f?.contains("r") ?: false,
            cancelling = f?.contains("c") ?: false,
            start = f?.contains("s") ?: false,
            blockedSwitch = f?.contains("b") ?: false,
            delayedSwitch = f?.contains("d") ?: false,
        ))
    } or (optional(identifier) * -lparen * zeroOrMore(oneKeyAction) * -rparen).map {(f, acts) ->
        KeyActions(acts, flags=KeyActionFlags(
            immediate = f?.contains("i") ?: false,
            repeated = f?.contains("r") ?: false,
            cancelling = f?.contains("c") ?: false,
            start = f?.contains("s") ?: false,
            blockedSwitch = f?.contains("b") ?: false,
            delayedSwitch = f?.contains("d") ?: false,
        ))
    }


    val longPressActions: Parser<List<KeyActions>> = (keyAction * optional(-AS * label)).map{(ka, l) ->
        listOf(KeyActions(ka.actions, l ?: KeyLabel(ka.firstString())))
    } or (-lbrace * zeroOrMore(keyAction * optional(-AS * label)) * -rbrace).map { a ->
        List(a.size) { i ->
            KeyActions(
                a[i].component1().actions,
                a[i].component2() ?: KeyLabel(a[i].component1().firstString()),
            )
        }
    }

    val directedKeyAction: Parser<Directed<out Any>> =
        (-L * keyAction).map { ka -> Directed(ka, Direction.LEFT) } or
        (-R * keyAction).map { ka -> Directed(ka, Direction.RIGHT) } or
        (-U * keyAction).map { ka -> Directed(ka, Direction.UP) } or
        (-D * keyAction).map { ka -> Directed(ka, Direction.DOWN) } or
        (-optional(TAP) * keyAction).map { ka -> Directed(ka, Direction.NO) } or
        (-UNDERSCORE * longPressActions).map { ka -> Directed(ka, Direction.ADD) }


//    val keyActions: Parser<KeyActions> = (zeroOrMore(directedKeyAction)).map{kas ->
//        var ka =
//        for(a in kas) {
//            when ((a as Directed<*>).dir) {
//                Direction.UP ->    ka.swipeUp    = a.value as List<KeyAction>
//                Direction.DOWN ->  ka.swipeDown  = a.value as List<KeyAction>
//                Direction.LEFT ->  ka.swipeLeft  = a.value as List<KeyAction>
//                Direction.RIGHT -> ka.swipeRight = a.value as List<KeyAction>
//                Direction.ADD ->   ka.longPress  = a.value as List<KeyData>
//                Direction.NO ->    ka.action     = a.value as List<KeyAction>
//            }
//        }
//        ka
//    }

    val style: Parser<String> = string.map{v -> v}

    val keyLiteral: Parser<KeyData> =(label * -AT * numberx * numbery * optional(numberx * numbery) * optional(-lbracket * zeroOrMore(directedKeyAction) * -rbracket) * optional(COLON * style)).map {(l, x, y, dim, kas, s) ->
        val ka = KeyData(
            l, null, x, y, dim?.component1() ?: cellWidth, dim?.component2() ?: cellHeight, style = s?.component2()
        )
        if (l.center.isNotEmpty()) {
            ka.action = KeyActions(listOf(KeyAction(KeyActionType.STRING, l.center)), KeyLabel(l.center))
        }
        if (l.up.isNotEmpty()) {
            ka.swipeUp = KeyActions(listOf(KeyAction(KeyActionType.STRING, l.up)), KeyLabel(l.up))
        }
        if (l.down.isNotEmpty()) {
            ka.swipeDown = KeyActions(listOf(KeyAction(KeyActionType.STRING, l.down)), KeyLabel(l.down))
        }
        if (l.left.isNotEmpty()) {
            ka.swipeLeft = KeyActions(listOf(KeyAction(KeyActionType.STRING, l.left)), KeyLabel(l.left))
        }
        if (l.right.isNotEmpty()) {
            ka.swipeRight = KeyActions(listOf(KeyAction(KeyActionType.STRING, l.right)), KeyLabel(l.right))
        }
        if (l.corner.isNotEmpty()) {
            ka.longPress = listOf(KeyActions(listOf(KeyAction(KeyActionType.STRING, l.corner)), KeyLabel(l.corner)))
        }
        if (kas != null) {
            for(a in kas) {
                when ((a as Directed<*>).dir) {
                    Direction.UP ->    ka.swipeUp    = a.value as KeyActions
                    Direction.DOWN ->  ka.swipeDown  = a.value as KeyActions
                    Direction.LEFT ->  ka.swipeLeft  = a.value as KeyActions
                    Direction.RIGHT -> ka.swipeRight = a.value as KeyActions
                    Direction.ADD ->   ka.longPress  = a.value as List<KeyActions>
                    Direction.NO ->    ka.action     = a.value as KeyActions
                }
            }
        }
        ka
    }

    val key: Parser<List<KeyData>> = keyLiteral.map{key ->
        listOf(key)
    }

    val keys: Parser<List<KeyData>> = zeroOrMore(key).map{keys ->
        keys.flatten()
    }

    val state: Parser<State?> = (identifier * -lbrace * keys * -rbrace).map { (name, keys) ->
        State(name, keys)
    } or (identifier * numeric * numeric).map{ (i, w, h) ->
        when(i){
            "grid" -> {
                gridWidth = w
                gridHeight = h
            }
            "cell" -> {
                cellWidth = if(gridWidth != 0f) { w / gridWidth } else { w }
                cellHeight = if(gridHeight != 0f) { h / gridHeight } else { h }
            }
        }
        null
    }

    override val rootParser: Parser<MutableMap<String, List<KeyData>>> =
        zeroOrMore(state).map { stateList ->
            stateList.filterNotNull().associate { it.name to it.keys }.toMutableMap()
        }
}