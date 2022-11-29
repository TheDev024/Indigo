package indigo

import java.util.*

val scanner = Scanner(System.`in`)

enum class Suit(val value: Char) {
    DIAMOND('\u2666'), HEART('\u2665'), SPADE('\u2660'), CLUB('\u2663'),
}

enum class Rank(val value: String) {
    ACE("A"), TWO("2"), THREE("3"), FOUR("4"), FIVE("5"), SIX("6"), SEVEN("7"), EIGHT("8"), NINE("9"), TEN("10"), JACK("J"), QUEEN(
        "Q"
    ),
    KING("K")
}

data class Card(
    val rank: Rank, val suit: Suit
) {
    val cardValue = "${rank.value}${suit.value}"
}

class IndigoCardGame {
    private val deck: List<Card>
    private val cards = mutableListOf<Card>()

    init {
        Suit.values().forEach { suit ->
            Rank.values().forEach { rank ->
                cards.add(Card(rank, suit))
            }
        }
        this.deck = cards.toList()
    }

    fun cmd() {
        while (true) {
            println("Choose an action (reset, shuffle, get, exit):")
            when (scanner.nextLine()) {
                "reset" -> resetDeck()

                "shuffle" -> {
                    cards.shuffle()
                    println("Card deck is shuffled.")
                }

                "get" -> getCards()

                "exit" -> break

                else -> println("Wrong action.")
            }
        }
        println("Bye")
    }

    private fun getCards() {
        println("Number of cards:")
        val n = scanner.nextLine()
        if (Regex("\\d+").matches(n) && n.toInt() in 1..52) {
            val number = n.toInt()
            if (number > cards.size) println("The remaining cards are insufficient to meet the request.") else {
                val selectedCards = cards.subList(0, number).toList()
                cards.removeAll(selectedCards)
                println(selectedCards.joinToString(" ") { it.cardValue })
            }
        } else println("Invalid number of cards.")
    }

    private fun resetDeck() {
        cards.clear()
        Suit.values().forEach { suit ->
            Rank.values().forEach { rank ->
                cards.add(Card(rank, suit))
            }
        }
        println("Card deck is reset.")
    }
}

fun main() {
    val game = IndigoCardGame()
    game.cmd()
}
