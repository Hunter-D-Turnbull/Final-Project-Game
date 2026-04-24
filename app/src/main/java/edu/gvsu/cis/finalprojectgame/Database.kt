package edu.gvsu.cis.finalprojectgame

import android.app.Application
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Room

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey(autoGenerate = true) val gameId: Int = 0,
    val totalHands: Int,
    val finalPoints: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "hands",
    foreignKeys = [
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = ["gameId"],
            childColumns = ["gameOwnerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("gameOwnerId")]
)
data class HandEntity(
    @PrimaryKey(autoGenerate = true) val handId: Int = 0,
    val gameOwnerId: Int,   // links to Game
    val handNumber: Int,
    val result: String,     // "WIN", "LOSS", "PUSH"
    val pointsChange: Int
)

@Dao
interface GameDao {

    @Insert
    suspend fun insertGame(game: GameEntity): Long

    @Insert
    suspend fun insertHand(hand: HandEntity)

    @Query("SELECT * FROM games ORDER BY timestamp DESC")
    fun getAllGames(): Flow<List<GameEntity>>

    @Query("SELECT * FROM hands WHERE gameOwnerId = :gameId")
    fun getHandsForGame(gameId: Int): Flow<List<HandEntity>>

    @androidx.room.Update
    suspend fun updateGame(game: GameEntity)

    @Query("DELETE FROM games")
    suspend fun deleteAllGames()
}

@Database(
    entities = [GameEntity::class, HandEntity::class],
    version = 1
)
abstract class MyDatabase : RoomDatabase() {
    abstract fun getDao(): GameDao
}

class MyRoomApplication : Application() {

    lateinit var myDB: MyDatabase

    override fun onCreate() {
        super.onCreate()

        myDB = Room.databaseBuilder(
            applicationContext,
            MyDatabase::class.java,
            "blackjack_db"
        ).build()
    }
}