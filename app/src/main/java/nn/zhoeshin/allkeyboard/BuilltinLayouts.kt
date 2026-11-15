package nn.zhoeshin.allkeyboard

object BuiltinLayouts {
    val QWERTY = mutableMapOf(
        "main" to listOf(
            KeyData(
                label = KeyLabel(center = "1"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "1"))),
                x = 0.0f, y = 0f, width = 0.1f, height = 0.2f
            ),
            KeyData(
                label = KeyLabel(center = "2"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "2"))),
                x = 0.1f, y = 0f, width = 0.1f, height = 0.2f
            ),
            KeyData(
                label = KeyLabel(center = "3"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "3"))),
                x = 0.2f, y = 0f, width = 0.1f, height = 0.2f
            ),
            KeyData(
                label = KeyLabel(center = "4"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "4"))),
                x = 0.3f, y = 0f, width = 0.1f, height = 0.2f
            ),
            KeyData(
                label = KeyLabel(center = "5"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "5"))),
                x = 0.4f, y = 0f, width = 0.1f, height = 0.2f
            ),
            KeyData(
                label = KeyLabel(center = "6"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "6"))),
                x = 0.5f, y = 0f, width = 0.1f, height = 0.2f
            ),
            KeyData(
                label = KeyLabel(center = "7"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "7"))),
                x = 0.6f, y = 0f, width = 0.1f, height = 0.2f
            ),
            KeyData(
                label = KeyLabel(center = "8"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "8"))),
                x = 0.7f, y = 0f, width = 0.1f, height = 0.2f
            ),
            KeyData(
                label = KeyLabel(center = "9", corner = "{"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "9"))),
                x = 0.8f, y = 0f, width = 0.1f, height = 0.2f,
                longPress = listOf(
                    KeyActions(
                        label = KeyLabel(center = "{"),
                        actions = listOf(KeyAction(KeyActionType.STRING, "{"))
                    )
                )
            ),
            KeyData(
                label = KeyLabel(center = "0", corner = "}"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "0"))),
                x = 0.9f, y = 0f, width = 0.1f, height = 0.2f,
                longPress = listOf(
                    KeyActions(
                        label = KeyLabel(center = "}"),
                        actions = listOf(KeyAction(KeyActionType.STRING, "}"))
                    )
                )
            ),
            KeyData(
                label = KeyLabel(center = "q", corner = "+"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "q"))),
                x = 0.0f, y = 0.2f, width = 0.1f, height = 0.2f,
                longPress = listOf(
                    KeyActions(
                        label = KeyLabel(center = "+"),
                        actions = listOf(KeyAction(KeyActionType.STRING, "+"))
                    )
                ),
                swipeDown = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "`"))),
                swipeUp = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "```")))
            ),
            KeyData(
                label = KeyLabel(center = "w", corner = "|"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "w"))),
                x = 0.1f, y = 0.2f, width = 0.1f, height = 0.2f,
                longPress = listOf(
                    KeyActions(
                        label = KeyLabel(center = "|"),
                        actions = listOf(KeyAction(KeyActionType.STRING, "|"))
                    )
                )
            ),
            KeyData(
                label = KeyLabel(center = "e", corner = "\\"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "e"))),
                x = 0.2f, y = 0.2f, width = 0.1f, height = 0.2f,
                longPress = listOf(
                    KeyActions(
                        label = KeyLabel(center = "\\"),
                        actions = listOf(KeyAction(KeyActionType.STRING, "\\"))
                    )
                )
            ),
            KeyData(
                label = KeyLabel(center = "r", corner = "="),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "r"))),
                x = 0.3f, y = 0.2f, width = 0.1f, height = 0.2f,
                longPress = listOf(
                    KeyActions(
                        label = KeyLabel(center = "="),
                        actions = listOf(KeyAction(KeyActionType.STRING, "="))
                    )
                )
            ),
            KeyData(
                label = KeyLabel(center = "t", corner = "/"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "t"))),
                x = 0.4f, y = 0.2f, width = 0.1f, height = 0.2f,
                longPress = listOf(
                    KeyActions(
                        label = KeyLabel(center = "/"),
                        actions = listOf(KeyAction(KeyActionType.STRING, "/"))
                    )
                )
            ),
            KeyData(
                label = KeyLabel(center = "y", corner = "_"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "y"))),
                x = 0.5f, y = 0.2f, width = 0.1f, height = 0.2f,
                longPress = listOf(
                    KeyActions(
                        label = KeyLabel(center = "_"),
                        actions = listOf(KeyAction(KeyActionType.STRING, "_"))
                    )
                )
            ),
            KeyData(
                label = KeyLabel(center = "u", corner = "<"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "u"))),
                x = 0.6f, y = 0.2f, width = 0.1f, height = 0.2f,
                longPress = listOf(
                    KeyActions(
                        label = KeyLabel(center = "<"),
                        actions = listOf(KeyAction(KeyActionType.STRING, "<"))
                    )
                )
            ),
            KeyData(
                label = KeyLabel(center = "i", corner = ">"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "i"))),
                x = 0.7f, y = 0.2f, width = 0.1f, height = 0.2f,
                longPress = listOf(
                    KeyActions(
                        label = KeyLabel(center = ">"),
                        actions = listOf(KeyAction(KeyActionType.STRING, ">"))
                    )
                )
            ),
            KeyData(
                label = KeyLabel(center = "o", corner = "["),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "o"))),
                x = 0.8f, y = 0.2f, width = 0.1f, height = 0.2f,
                longPress = listOf(
                    KeyActions(
                        label = KeyLabel(center = "["),
                        actions = listOf(KeyAction(KeyActionType.STRING, "["))
                    )
                )
            ),
            KeyData(
                label = KeyLabel(center = "p", corner = "]"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "p"))),
                x = 0.9f, y = 0.2f, width = 0.1f, height = 0.2f,
                longPress = listOf(
                    KeyActions(
                        label = KeyLabel(center = "]"),
                        actions = listOf(KeyAction(KeyActionType.STRING, "]"))
                    )
                )
            ),
            KeyData(
                label = KeyLabel(center = "a", corner = "!"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "a"))),
                x = 0.05f, y = 0.4f, width = 0.1f, height = 0.2f,
                longPress = listOf(
                    KeyActions(label = KeyLabel(center = "!"), actions = listOf(KeyAction(KeyActionType.STRING, "!"))),
                    KeyActions(label = KeyLabel(center = "a"), actions = listOf(KeyAction(KeyActionType.STRING, "a"))),
                    KeyActions(label = KeyLabel(center = "a"), actions = listOf(KeyAction(KeyActionType.STRING, "a"))),
                    KeyActions(label = KeyLabel(center = "a"), actions = listOf(KeyAction(KeyActionType.STRING, "a"))),
                    KeyActions(label = KeyLabel(center = "a"), actions = listOf(KeyAction(KeyActionType.STRING, "a"))),
                    KeyActions(label = KeyLabel(center = "a"), actions = listOf(KeyAction(KeyActionType.STRING, "a"))),
                    KeyActions(label = KeyLabel(center = "a"), actions = listOf(KeyAction(KeyActionType.STRING, "a"))),
                    KeyActions(label = KeyLabel(center = "a"), actions = listOf(KeyAction(KeyActionType.STRING, "a")))
                )
            ),
            KeyData(
                label = KeyLabel(center = "s", corner = "@"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "s"))),
                x = 0.15f, y = 0.4f, width = 0.1f, height = 0.2f,
                longPress = listOf(
                    KeyActions(
                        label = KeyLabel(center = "@"),
                        actions = listOf(KeyAction(KeyActionType.STRING, "@"))
                    )
                )
            ),
            KeyData(
                label = KeyLabel(center = "d", corner = "#"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "d"))),
                x = 0.25f, y = 0.4f, width = 0.1f, height = 0.2f,
                longPress = listOf(
                    KeyActions(
                        label = KeyLabel(center = "#"),
                        actions = listOf(KeyAction(KeyActionType.STRING, "#"))
                    )
                )
            ),
            KeyData(
                label = KeyLabel(center = "f", corner = "%"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "f"))),
                x = 0.35f, y = 0.4f, width = 0.1f, height = 0.2f,
                longPress = listOf(
                    KeyActions(
                        label = KeyLabel(center = "%"),
                        actions = listOf(KeyAction(KeyActionType.STRING, "%"))
                    )
                )
            ),
            KeyData(
                label = KeyLabel(center = "g", corner = "^"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "g"))),
                x = 0.45f, y = 0.4f, width = 0.1f, height = 0.2f,
                longPress = listOf(
                    KeyActions(
                        label = KeyLabel(center = "^"),
                        actions = listOf(KeyAction(KeyActionType.STRING, "^"))
                    )
                )
            ),
            KeyData(
                label = KeyLabel(center = "h", corner = "&"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "h"))),
                x = 0.55f, y = 0.4f, width = 0.1f, height = 0.2f,
                longPress = listOf(
                    KeyActions(
                        label = KeyLabel(center = "&"),
                        actions = listOf(KeyAction(KeyActionType.STRING, "&"))
                    )
                )
            ),
            KeyData(
                label = KeyLabel(center = "j", corner = "*"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "j"))),
                x = 0.65f, y = 0.4f, width = 0.1f, height = 0.2f,
                longPress = listOf(
                    KeyActions(
                        label = KeyLabel(center = "*"),
                        actions = listOf(KeyAction(KeyActionType.STRING, "*"))
                    )
                )
            ),
            KeyData(
                label = KeyLabel(center = "k", corner = "("),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "k"))),
                x = 0.75f, y = 0.4f, width = 0.1f, height = 0.2f,
                longPress = listOf(
                    KeyActions(
                        label = KeyLabel(center = "("),
                        actions = listOf(KeyAction(KeyActionType.STRING, "("))
                    )
                )
            ),
            KeyData(
                label = KeyLabel(center = "l", corner = ")"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "l"))),
                x = 0.85f, y = 0.4f, width = 0.1f, height = 0.2f,
                longPress = listOf(
                    KeyActions(
                        label = KeyLabel(center = ")"),
                        actions = listOf(KeyAction(KeyActionType.STRING, ")"))
                    )
                )
            ),
            KeyData(
                label = KeyLabel(center = "^"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.TRANSITIONTEMP, "shift"))),
                x = 0.00f, y = 0.6f, width = 0.15f, height = 0.2f
            ),
            KeyData(
                label = KeyLabel(center = "z", corner = "-"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "z"))),
                x = 0.15f, y = 0.6f, width = 0.1f, height = 0.2f,
                longPress = listOf(
                    KeyActions(
                        label = KeyLabel(center = "-"),
                        actions = listOf(KeyAction(KeyActionType.STRING, "-"))
                    )
                )
            ),
            KeyData(
                label = KeyLabel(center = "x", corner = "'"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "x"))),
                x = 0.25f, y = 0.6f, width = 0.1f, height = 0.2f,
                longPress = listOf(
                    KeyActions(
                        label = KeyLabel(center = "'"),
                        actions = listOf(KeyAction(KeyActionType.STRING, "'"))
                    )
                )
            ),
            KeyData(
                label = KeyLabel(center = "c", corner = "\""),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "c"))),
                x = 0.35f, y = 0.6f, width = 0.1f, height = 0.2f,
                longPress = listOf(
                    KeyActions(
                        label = KeyLabel(center = "\""),
                        actions = listOf(KeyAction(KeyActionType.STRING, "\""))
                    )
                )
            ),
            KeyData(
                label = KeyLabel(center = "v", corner = ":"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "v"))),
                x = 0.45f, y = 0.6f, width = 0.1f, height = 0.2f,
                longPress = listOf(
                    KeyActions(
                        label = KeyLabel(center = ":"),
                        actions = listOf(KeyAction(KeyActionType.STRING, ":"))
                    )
                )
            ),
            KeyData(
                label = KeyLabel(center = "b", corner = ";"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "b"))),
                x = 0.55f, y = 0.6f, width = 0.1f, height = 0.2f,
                longPress = listOf(
                    KeyActions(
                        label = KeyLabel(center = ";"),
                        actions = listOf(KeyAction(KeyActionType.STRING, ";"))
                    )
                )
            ),
            KeyData(
                label = KeyLabel(center = "n", corner = ","),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "n"))),
                x = 0.65f, y = 0.6f, width = 0.1f, height = 0.2f,
                longPress = listOf(
                    KeyActions(
                        label = KeyLabel(center = ","),
                        actions = listOf(KeyAction(KeyActionType.STRING, ","))
                    )
                )
            ),
            KeyData(
                label = KeyLabel(center = "m", corner = "?"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "m"))),
                x = 0.75f, y = 0.6f, width = 0.1f, height = 0.2f,
                longPress = listOf(
                    KeyActions(
                        label = KeyLabel(center = "?"),
                        actions = listOf(KeyAction(KeyActionType.STRING, "?"))
                    )
                )
            ),
            KeyData(
                label = KeyLabel(center = "<"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.MAGIC, "delete"))),
                x = 0.85f, y = 0.6f, width = 0.15f, height = 0.2f
            ),
            KeyData(
                label = KeyLabel(center = "error"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.MAGIC, "error"))),
                x = 0f, y = 0.8f, width = 0.2f, height = 0.2f
            ),
            KeyData(
                label = KeyLabel(center = " "),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, " "))),
                x = 0.2f, y = 0.8f, width = 0.5f, height = 0.2f,
                swipeLeft = KeyActions(actions = listOf(KeyAction(KeyActionType.MAGIC, "layoutprev"))),
                swipeRight = KeyActions(actions = listOf(KeyAction(KeyActionType.MAGIC, "layoutnext")))
            ),
            KeyData(
                label = KeyLabel(center = "."),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "."))),
                x = 0.7f, y = 0.8f, width = 0.1f, height = 0.2f
            ),
            KeyData(
                label = KeyLabel(center = "v"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.MAGIC, "return"))),
                x = 0.8f, y = 0.8f, width = 0.2f, height = 0.2f
            )
        ),
        "shift" to listOf(
            KeyData(
                label = KeyLabel(center = "1"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "1"))),
                x = 0.0f, y = 0f, width = 0.1f, height = 0.2f
            ),
            KeyData(
                label = KeyLabel(center = "2"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "2"))),
                x = 0.1f, y = 0f, width = 0.1f, height = 0.2f
            ),
            KeyData(
                label = KeyLabel(center = "3"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "3"))),
                x = 0.2f, y = 0f, width = 0.1f, height = 0.2f
            ),
            KeyData(
                label = KeyLabel(center = "4"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "4"))),
                x = 0.3f, y = 0f, width = 0.1f, height = 0.2f
            ),
            KeyData(
                label = KeyLabel(center = "5"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "5"))),
                x = 0.4f, y = 0f, width = 0.1f, height = 0.2f
            ),
            KeyData(
                label = KeyLabel(center = "6"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "6"))),
                x = 0.5f, y = 0f, width = 0.1f, height = 0.2f
            ),
            KeyData(
                label = KeyLabel(center = "7"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "7"))),
                x = 0.6f, y = 0f, width = 0.1f, height = 0.2f
            ),
            KeyData(
                label = KeyLabel(center = "8"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "8"))),
                x = 0.7f, y = 0f, width = 0.1f, height = 0.2f
            ),
            KeyData(
                label = KeyLabel(center = "9", corner = "{"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "9"))),
                x = 0.8f, y = 0f, width = 0.1f, height = 0.2f,
                longPress = listOf(
                    KeyActions(
                        label = KeyLabel(center = "{"),
                        actions = listOf(KeyAction(KeyActionType.STRING, "{"))
                    )
                )
            ),
            KeyData(
                label = KeyLabel(center = "0", corner = "}"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "0"))),
                x = 0.9f, y = 0f, width = 0.1f, height = 0.2f,
                longPress = listOf(
                    KeyActions(
                        label = KeyLabel(center = "}"),
                        actions = listOf(KeyAction(KeyActionType.STRING, "}"))
                    )
                )
            ),
            KeyData(
                label = KeyLabel(center = "Q", corner = "+"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "Q"))),
                x = 0.0f, y = 0.2f, width = 0.1f, height = 0.2f,
                longPress = listOf(
                    KeyActions(
                        label = KeyLabel(center = "+"),
                        actions = listOf(KeyAction(KeyActionType.STRING, "+"))
                    )
                ),
                swipeDown = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "`"))),
                swipeUp = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "```")))
            ),
            KeyData(
                label = KeyLabel(center = "W", corner = "|"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "W"))),
                x = 0.1f, y = 0.2f, width = 0.1f, height = 0.2f,
                longPress = listOf(
                    KeyActions(
                        label = KeyLabel(center = "|"),
                        actions = listOf(KeyAction(KeyActionType.STRING, "|"))
                    )
                )
            ),
            KeyData(
                label = KeyLabel(center = "E", corner = "\\"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "E"))),
                x = 0.2f, y = 0.2f, width = 0.1f, height = 0.2f,
                longPress = listOf(
                    KeyActions(
                        label = KeyLabel(center = "\\"),
                        actions = listOf(KeyAction(KeyActionType.STRING, "\\"))
                    )
                )
            ),
            KeyData(
                label = KeyLabel(center = "R", corner = "="),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "R"))),
                x = 0.3f, y = 0.2f, width = 0.1f, height = 0.2f,
                longPress = listOf(
                    KeyActions(
                        label = KeyLabel(center = "="),
                        actions = listOf(KeyAction(KeyActionType.STRING, "="))
                    )
                )
            ),
            KeyData(
                label = KeyLabel(center = "T", corner = "/"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "T"))),
                x = 0.4f, y = 0.2f, width = 0.1f, height = 0.2f,
                longPress = listOf(
                    KeyActions(
                        label = KeyLabel(center = "/"),
                        actions = listOf(KeyAction(KeyActionType.STRING, "/"))
                    )
                )
            ),
            KeyData(
                label = KeyLabel(center = "Y", corner = "_"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "Y"))),
                x = 0.5f, y = 0.2f, width = 0.1f, height = 0.2f,
                longPress = listOf(
                    KeyActions(
                        label = KeyLabel(center = "_"),
                        actions = listOf(KeyAction(KeyActionType.STRING, "_"))
                    )
                )
            ),
            KeyData(
                label = KeyLabel(center = "U", corner = "<"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "U"))),
                x = 0.6f, y = 0.2f, width = 0.1f, height = 0.2f,
                longPress = listOf(
                    KeyActions(
                        label = KeyLabel(center = "<"),
                        actions = listOf(KeyAction(KeyActionType.STRING, "<"))
                    )
                )
            ),
            KeyData(
                label = KeyLabel(center = "I", corner = ">"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "I"))),
                x = 0.7f, y = 0.2f, width = 0.1f, height = 0.2f,
                longPress = listOf(
                    KeyActions(
                        label = KeyLabel(center = ">"),
                        actions = listOf(KeyAction(KeyActionType.STRING, ">"))
                    )
                )
            ),
            KeyData(
                label = KeyLabel(center = "O", corner = "["),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "O"))),
                x = 0.8f, y = 0.2f, width = 0.1f, height = 0.2f,
                longPress = listOf(
                    KeyActions(
                        label = KeyLabel(center = "["),
                        actions = listOf(KeyAction(KeyActionType.STRING, "["))
                    )
                )
            ),
            KeyData(
                label = KeyLabel(center = "P", corner = "]"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "P"))),
                x = 0.9f, y = 0.2f, width = 0.1f, height = 0.2f,
                longPress = listOf(
                    KeyActions(
                        label = KeyLabel(center = "]"),
                        actions = listOf(KeyAction(KeyActionType.STRING, "]"))
                    )
                )
            ),
            KeyData(
                label = KeyLabel(center = "A", corner = "!"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "A"))),
                x = 0.05f, y = 0.4f, width = 0.1f, height = 0.2f,
                longPress = listOf(
                    KeyActions(label = KeyLabel(center = "!"), actions = listOf(KeyAction(KeyActionType.STRING, "!"))),
                    KeyActions(label = KeyLabel(center = "A"), actions = listOf(KeyAction(KeyActionType.STRING, "A"))),
                    KeyActions(label = KeyLabel(center = "A"), actions = listOf(KeyAction(KeyActionType.STRING, "A"))),
                    KeyActions(label = KeyLabel(center = "A"), actions = listOf(KeyAction(KeyActionType.STRING, "A"))),
                    KeyActions(label = KeyLabel(center = "A"), actions = listOf(KeyAction(KeyActionType.STRING, "A"))),
                    KeyActions(label = KeyLabel(center = "A"), actions = listOf(KeyAction(KeyActionType.STRING, "A"))),
                    KeyActions(label = KeyLabel(center = "A"), actions = listOf(KeyAction(KeyActionType.STRING, "A"))),
                    KeyActions(label = KeyLabel(center = "A"), actions = listOf(KeyAction(KeyActionType.STRING, "A"))),
                    KeyActions(label = KeyLabel(center = "A"), actions = listOf(KeyAction(KeyActionType.STRING, "A")))
                )
            ),
            KeyData(
                label = KeyLabel(center = "S", corner = "@"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "S"))),
                x = 0.15f, y = 0.4f, width = 0.1f, height = 0.2f,
                longPress = listOf(
                    KeyActions(
                        label = KeyLabel(center = "@"),
                        actions = listOf(KeyAction(KeyActionType.STRING, "@"))
                    )
                )
            ),
            KeyData(
                label = KeyLabel(center = "D", corner = "#"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "D"))),
                x = 0.25f, y = 0.4f, width = 0.1f, height = 0.2f,
                longPress = listOf(
                    KeyActions(
                        label = KeyLabel(center = "#"),
                        actions = listOf(KeyAction(KeyActionType.STRING, "#"))
                    )
                )
            ),
            KeyData(
                label = KeyLabel(center = "F", corner = "%"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "F"))),
                x = 0.35f, y = 0.4f, width = 0.1f, height = 0.2f,
                longPress = listOf(
                    KeyActions(
                        label = KeyLabel(center = "%"),
                        actions = listOf(KeyAction(KeyActionType.STRING, "%"))
                    )
                )
            ),
            KeyData(
                label = KeyLabel(center = "G", corner = "^"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "G"))),
                x = 0.45f, y = 0.4f, width = 0.1f, height = 0.2f,
                longPress = listOf(
                    KeyActions(
                        label = KeyLabel(center = "^"),
                        actions = listOf(KeyAction(KeyActionType.STRING, "^"))
                    )
                )
            ),
            KeyData(
                label = KeyLabel(center = "H", corner = "&"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "H"))),
                x = 0.55f, y = 0.4f, width = 0.1f, height = 0.2f,
                longPress = listOf(
                    KeyActions(
                        label = KeyLabel(center = "&"),
                        actions = listOf(KeyAction(KeyActionType.STRING, "&"))
                    )
                )
            ),
            KeyData(
                label = KeyLabel(center = "J", corner = "*"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "J"))),
                x = 0.65f, y = 0.4f, width = 0.1f, height = 0.2f,
                longPress = listOf(
                    KeyActions(
                        label = KeyLabel(center = "*"),
                        actions = listOf(KeyAction(KeyActionType.STRING, "*"))
                    )
                )
            ),
            KeyData(
                label = KeyLabel(center = "K", corner = "("),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "K"))),
                x = 0.75f, y = 0.4f, width = 0.1f, height = 0.2f,
                longPress = listOf(
                    KeyActions(
                        label = KeyLabel(center = "("),
                        actions = listOf(KeyAction(KeyActionType.STRING, "("))
                    )
                )
            ),
            KeyData(
                label = KeyLabel(center = "L", corner = ")"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "L"))),
                x = 0.85f, y = 0.4f, width = 0.1f, height = 0.2f,
                longPress = listOf(
                    KeyActions(
                        label = KeyLabel(center = ")"),
                        actions = listOf(KeyAction(KeyActionType.STRING, ")"))
                    )
                )
            ),
            KeyData(
                label = KeyLabel(center = "_"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.TRANSITIONPERM, "main"))),
                x = 0.00f, y = 0.6f, width = 0.15f, height = 0.2f
            ),
            KeyData(
                label = KeyLabel(center = "Z", corner = "-"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "Z"))),
                x = 0.15f, y = 0.6f, width = 0.1f, height = 0.2f,
                longPress = listOf(
                    KeyActions(
                        label = KeyLabel(center = "-"),
                        actions = listOf(KeyAction(KeyActionType.STRING, "-"))
                    )
                )
            ),
            KeyData(
                label = KeyLabel(center = "X", corner = "'"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "X"))),
                x = 0.25f, y = 0.6f, width = 0.1f, height = 0.2f,
                longPress = listOf(
                    KeyActions(
                        label = KeyLabel(center = "'"),
                        actions = listOf(KeyAction(KeyActionType.STRING, "'"))
                    )
                )
            ),
            KeyData(
                label = KeyLabel(center = "C", corner = "\""),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "C"))),
                x = 0.35f, y = 0.6f, width = 0.1f, height = 0.2f,
                longPress = listOf(
                    KeyActions(
                        label = KeyLabel(center = "\""),
                        actions = listOf(KeyAction(KeyActionType.STRING, "\""))
                    )
                )
            ),
            KeyData(
                label = KeyLabel(center = "V", corner = ":"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "V"))),
                x = 0.45f, y = 0.6f, width = 0.1f, height = 0.2f,
                longPress = listOf(
                    KeyActions(
                        label = KeyLabel(center = ":"),
                        actions = listOf(KeyAction(KeyActionType.STRING, ":"))
                    )
                )
            ),
            KeyData(
                label = KeyLabel(center = "B", corner = ";"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "B"))),
                x = 0.55f, y = 0.6f, width = 0.1f, height = 0.2f,
                longPress = listOf(
                    KeyActions(
                        label = KeyLabel(center = ";"),
                        actions = listOf(KeyAction(KeyActionType.STRING, ";"))
                    )
                )
            ),
            KeyData(
                label = KeyLabel(center = "N", corner = ","),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "N"))),
                x = 0.65f, y = 0.6f, width = 0.1f, height = 0.2f,
                longPress = listOf(
                    KeyActions(
                        label = KeyLabel(center = ","),
                        actions = listOf(KeyAction(KeyActionType.STRING, ","))
                    )
                )
            ),
            KeyData(
                label = KeyLabel(center = "M", corner = "?"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, "M"))),
                x = 0.75f, y = 0.6f, width = 0.1f, height = 0.2f,
                longPress = listOf(
                    KeyActions(
                        label = KeyLabel(center = "?"),
                        actions = listOf(KeyAction(KeyActionType.STRING, "?"))
                    )
                )
            ),
            KeyData(
                label = KeyLabel(center = "<"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.MAGIC, "delete"))),
                x = 0.85f, y = 0.6f, width = 0.15f, height = 0.2f
            ),
            KeyData(
                label = KeyLabel(center = " "),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.STRING, " "))),
                x = 0.2f, y = 0.8f, width = 0.6f, height = 0.2f,
                swipeLeft = KeyActions(actions = listOf(KeyAction(KeyActionType.MAGIC, "layoutprev"))),
                swipeRight = KeyActions(actions = listOf(KeyAction(KeyActionType.MAGIC, "layoutnext")))
            ),
            KeyData(
                label = KeyLabel(center = "v"),
                action = KeyActions(actions = listOf(KeyAction(KeyActionType.MAGIC, "return"))),
                x = 0.8f, y = 0.8f, width = 0.2f, height = 0.2f
            )
        )
    )
}