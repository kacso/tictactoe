package hr.inceptum.tictactoe.game

interface GameListener {
    /**
     * @param winner 0 - draw, 1 - Player 1, 2 - Player 2
     */
    fun onGameFinished(winner: Int)

    /**
     * Called when turn is changed for another player
     */
    fun onCurrentPlayerChanged(player: Int)

    /**
     * Called when player makes a move and UI needs to be updated
     */
    fun boardUpdated(board: Array<Int>)
}