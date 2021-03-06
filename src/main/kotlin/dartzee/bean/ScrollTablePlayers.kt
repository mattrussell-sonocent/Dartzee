package dartzee.bean

import dartzee.core.bean.ScrollTable
import dartzee.core.util.TableUtil
import dartzee.db.PlayerEntity

fun ScrollTable.getSelectedPlayer(): PlayerEntity?
{
    val row = table.selectedRow
    return if (row == -1) null else getPlayerEntityForRow(row)
}

fun ScrollTable.getAllPlayers(): MutableList<PlayerEntity>
{
    val ret = mutableListOf<PlayerEntity>()

    for (i in 0 until table.rowCount)
    {
        val player = getPlayerEntityForRow(i)
        ret.add(player)
    }

    return ret
}


fun ScrollTable.getSelectedPlayers(): MutableList<PlayerEntity>
{
    val ret = mutableListOf<PlayerEntity>()

    val viewRows = table.selectedRows
    for (i in viewRows.indices)
    {
        val player = getPlayerEntityForRow(viewRows[i])
        ret.add(player)
    }

    return ret
}

fun ScrollTable.getPlayerEntityForRow(row: Int): PlayerEntity
{
    return table.getValueAt(row, 1) as PlayerEntity
}

fun ScrollTable.initPlayerTableModel(players: List<PlayerEntity> = listOf())
{
    val model = TableUtil.DefaultModel()
    model.addColumn("")
    model.addColumn("Player")

    this.model = model

    setRowHeight(23)
    setColumnWidths("25")

    addPlayers(players)

    setRowName("player")
    sortBy(1, false)
}

fun ScrollTable.addPlayers(players: List<PlayerEntity>) = players.forEach{ addPlayer(it) }
private fun ScrollTable.addPlayer(player: PlayerEntity)
{
    val flag = player.getFlag()
    val row = arrayOf(flag, player)

    addRow(row)
}