package dartzee.game

enum class GameType
{
    X01,
    GOLF,
    ROUND_THE_CLOCK,
    DARTZEE;

    fun getDescription(): String =
        when (this)
        {
            X01 -> "X01"
            GOLF -> "Golf"
            ROUND_THE_CLOCK -> "Round the Clock"
            DARTZEE -> "Dartzee"
        }

    fun getDescription(gameParams: String) =
        when (this)
        {
            X01 -> gameParams
            GOLF -> "Golf - $gameParams holes"
            ROUND_THE_CLOCK -> "Round the Clock - $gameParams"
            DARTZEE -> "Dartzee"
        }
}