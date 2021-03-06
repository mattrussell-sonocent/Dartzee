package dartzee.achievements.x01

import dartzee.achievements.TestAbstractAchievementRowPerGame
import dartzee.db.GameEntity
import dartzee.game.GameType
import dartzee.db.PlayerEntity
import dartzee.helper.*
import io.kotlintest.shouldBe
import org.junit.Test

class TestAchievementX01Btbf: TestAbstractAchievementRowPerGame<AchievementX01Btbf>()
{
    override fun factoryAchievement() = AchievementX01Btbf()

    override fun setUpAchievementRowForPlayerAndGame(p: PlayerEntity, g: GameEntity)
    {
        insertSuccessfulParticipant(g, p)
    }

    @Test
    fun `Should ignore data for the wrong type of game`()
    {
        val alice = insertPlayer()

        val game = insertGame(gameType = GameType.GOLF)
        insertSuccessfulParticipant(game, alice)

        factoryAchievement().populateForConversion("")

        getCountFromTable("Achievement") shouldBe 0
    }

    @Test
    fun `Should ignore rounds that arent the last one`()
    {
        val g = insertRelevantGame()
        val p = insertPlayer()

        val pt = insertParticipant(gameId = g.rowId, playerId = p.rowId, finalScore = 6)
        insertDart(pt, roundNumber = 1, startingScore = 2, score = 1, multiplier = 2)

        factoryAchievement().populateForConversion("")

        getCountFromTable("Achievement") shouldBe 0
    }

    @Test
    fun `Should ignore games that were won on a different double`()
    {
        val g = insertRelevantGame()
        val p = insertPlayer()

        val pt = insertParticipant(gameId = g.rowId, playerId = p.rowId, finalScore = 3)
        insertDart(pt, roundNumber = 1, startingScore = 4, score = 2, multiplier = 2)

        factoryAchievement().populateForConversion("")

        getCountFromTable("Achievement") shouldBe 0
    }

    @Test
    fun `Should ignore D1s that did not finish the game`()
    {
        val g = insertRelevantGame()
        val p = insertPlayer()

        val pt = insertParticipant(gameId = g.rowId, playerId = p.rowId, finalScore = 3)
        insertDart(pt, roundNumber = 1, startingScore = 4, score = 1, multiplier = 2)

        factoryAchievement().populateForConversion("")

        getCountFromTable("Achievement") shouldBe 0
    }

    @Test
    fun `Should insert a row for each double 1 achieved`()
    {
        val alice = insertPlayer(name = "Alice")

        val game = insertRelevantGame()

        insertSuccessfulParticipant(game, alice)
        insertSuccessfulParticipant(game, alice)
        insertSuccessfulParticipant(game, alice)

        factoryAchievement().populateForConversion("")

        getCountFromTable("Achievement") shouldBe 3
    }

    private fun insertSuccessfulParticipant(game: GameEntity, player: PlayerEntity)
    {
        val pt = insertParticipant(gameId = game.rowId, playerId = player.rowId, finalScore = 3)
        insertDart(pt, roundNumber = 1, startingScore = 2, score = 1, multiplier = 2)
    }
}