package indigo

import java.util.*

val scanner = Scanner(System.`in`)
val random = Random(24)

open class Player(
    open val name: String, open val hand: MutableList<Card>, open val wonCards: Stack<Card>, open var score: Int
) {
    open fun play(numberOfCards: Int, topCard: Card?): Card? {
        println(if (numberOfCards == 0) "No cards on the table" else "\n$numberOfCards cards on the table, and the top card is $topCard")
        return null
    }
}

data class User(
    override val name: String = "Player",
    override val hand: MutableList<Card> = mutableListOf(),
    override val wonCards: Stack<Card> = Stack(),
    override var score: Int = 0
) : Player(name, hand, wonCards, score) {
    override fun play(numberOfCards: Int, topCard: Card?): Card? {
        super.play(numberOfCards, topCard)
        println("Cards in hand: ${hand.joinToString(" ") { "${hand.indexOf(it) + 1})$it" }}")
        val input = chooseCard()
        if (input == "exit") return null
        val index = input.toInt() - 1
        return hand.removeAt(index)
    }

    private fun chooseCard(): String {
        println("Choose a card to play (1-${hand.size}):")
        val input = scanner.nextLine()
        return if (input == "exit" || (input.toIntOrNull() ?: 0) in 1..hand.size) input
        else chooseCard()
    }
}

data class Computer(
    override val name: String = "Computer",
    override val hand: MutableList<Card> = mutableListOf(),
    override val wonCards: Stack<Card> = Stack(),
    override var score: Int = 0
) : Player(name, hand, wonCards, score) {
    override fun play(numberOfCards: Int, topCard: Card?): Card {
        super.play(numberOfCards, topCard)
        println(hand.joinToString(" "))

        var sameSuits = Suit.values().associateWith { mutableListOf<Card>() }
        var sameRanks = Rank.values().associateWith { mutableListOf<Card>() }

        hand.forEach { card ->
            sameSuits[card.suit]!!.add(card)
            sameRanks[card.rank]!!.add(card)
        }

        sameSuits = sameSuits.filter { it.value.isNotEmpty() }
        sameRanks = sameRanks.filter { it.value.isNotEmpty() }

        val index: Int =
            if (numberOfCards == 0) hand.indexOf((sameSuits.values.plus(sameRanks.values).find { it.size > 1 }
                ?: hand).choice()) else {
                val candidates =
                    sameSuits.filter { it.key == topCard!!.suit }.values.plus(sameRanks.filter { it.key == topCard!!.rank }.values)
                if (candidates.isNotEmpty()) {
                    hand.indexOf(
                        candidates.find { it.size > 1 }?.choice() ?: candidates.choice().choice()
                    )
                } else hand.indexOf((sameSuits.values.plus(sameRanks.values).find { it.size > 1 } ?: hand).choice())
            }
        println("Computer plays ${hand[index]}")
        return hand.removeAt(index)
    }
}

fun <E> List<E>.choice() = this[random.nextInt(this.size)]

enum class Suit(val value: Char) {
    DIAMOND('\u2666'), HEART('\u2665'), SPADE('\u2660'), CLUB('\u2663'),
}

enum class Rank(val value: String) {
    ACE("A"), TWO("2"), THREE("3"), FOUR("4"), FIVE("5"), SIX("6"), SEVEN("7"), EIGHT("8"), NINE("9"), TEN("10"), JACK("J"), QUEEN(
        "Q"
    ),
    KING("K")
}

data class Card(val rank: Rank, val suit: Suit) {
    override fun toString(): String = rank.value + suit.value
}

class IndigoCardGame {

    private val cards: List<Card>

    private val deck: Stack<Card> = Stack()
    private val table: Stack<Card> = Stack()

    private lateinit var currentPlayer: Player
    private lateinit var lastWinner: Player

    private val user = User()
    private val computer = Computer()

    init {
        Rank.values().forEach { rank ->
            Suit.values().forEach { suit ->
                deck.add(
                    Card(rank, suit)
                )
            }
        }
        cards = deck.toList()
        deck.shuffle()
    }

    fun startGame() {
        println("Indigo Card Game")

        val firstPlayer = firstPlayer()
        currentPlayer = firstPlayer
        lastWinner = currentPlayer

        print("Initial cards on the table:")
        repeat(4) { print(" ${table.push(deck.pop())}") }

        while (true) {
            if (user.wonCards.size + computer.wonCards.size + table.size == 52) {
                println(if (table.empty()) "No cards on the table" else "\n${table.size} cards on the table, and the top card is ${table.peek()}")
                if (!table.empty()) {
                    lastWinner.wonCards.addAll(table)
                    repeat(table.size) {
                        if (table.pop().rank in listOf(
                                Rank.ACE, Rank.TEN, Rank.JACK, Rank.QUEEN, Rank.KING
                            )
                        ) lastWinner.score++
                    }
                }

                if (user.wonCards.size > computer.wonCards.size) user.score += 3
                else if (user.wonCards.size < computer.wonCards.size) computer.score += 3
                else firstPlayer.score += 3

                printState()
                break
            }

            if (computer.hand.size == 0 && user.hand.size == 0) {
                dealPlayerCards(computer)
                dealPlayerCards(user)
            }

            val card = currentPlayer.play(table.size, if (table.isNotEmpty()) table.peek() else null) ?: break

            if (!table.empty()) {
                val topCard = table.peek()
                table.push(card)
                if (card.rank == topCard.rank || card.suit == topCard.suit) {
                    currentPlayer.wonCards.addAll(table)
                    repeat(table.size) {
                        if (table.pop().rank in listOf(
                                Rank.ACE, Rank.TEN, Rank.JACK, Rank.QUEEN, Rank.KING
                            )
                        ) currentPlayer.score++
                    }
                    println("${currentPlayer.name} wins cards")
                    printState()
                    lastWinner = currentPlayer
                }
            } else table.push(card)
            changeTurn()
        }
        println("Game Over")
    }

    private fun printState() {
        println("Score: ${user.name} ${user.score} - ${computer.name} ${computer.score}")
        println("Cards: ${user.name} ${user.wonCards.size} - ${computer.name} ${computer.wonCards.size}")
    }

    private fun changeTurn() {
        currentPlayer = if (currentPlayer == user) computer else user
    }

    private fun firstPlayer(): Player {
        println("Play first?")
        return when (scanner.nextLine().lowercase()) {
            "yes" -> user

            "no" -> computer

            else -> firstPlayer()
        }
    }

    private fun dealPlayerCards(player: Player) {
        repeat(6) { player.hand.add(deck.pop()) }
    }
}

fun main() {
    val game = IndigoCardGame()
    game.startGame()
}
