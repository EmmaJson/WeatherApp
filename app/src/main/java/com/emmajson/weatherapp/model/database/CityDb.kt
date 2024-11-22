import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// Data class for City
data class City(
    val name: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is City) return false
        return name == other.name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}


class CityDb(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "cities.db"
        private const val DATABASE_VERSION = 1

        // Table and Column Names
        private const val TABLE_CITIES = "cities"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_LAT = "lat"
        private const val COLUMN_LON = "lon"
        private const val COLUMN_IS_FAVORITE = "isFavorite"
        private const val COLUMN_IS_LATEST = "isLatest"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create the cities table
        val createTableQuery = """
            CREATE TABLE $TABLE_CITIES (
                $COLUMN_NAME TEXT PRIMARY KEY,
                $COLUMN_LAT REAL,
                $COLUMN_LON REAL,
                $COLUMN_IS_FAVORITE INTEGER DEFAULT 0,
                $COLUMN_IS_LATEST INTEGER DEFAULT 0
            )
        """.trimIndent()
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Drop the old table if it exists and create a new one
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CITIES")
        onCreate(db)
    }

    // Add or Update a City
    fun addOrUpdateCity(name: String,isFavorite: Boolean = false, isLatest: Boolean = false) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, name)
            put(COLUMN_IS_FAVORITE, if (isFavorite) 1 else 0)
            put(COLUMN_IS_LATEST, if (isLatest) 1 else 0)
        }
        db.insertWithOnConflict(TABLE_CITIES, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    // Get Favorite Cities
    fun getFavoriteCities(): List<City> {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_CITIES,
            arrayOf(COLUMN_NAME, COLUMN_LAT, COLUMN_LON),
            "$COLUMN_IS_FAVORITE = ?",
            arrayOf("1"),
            null,
            null,
            null
        )

        val favorites = mutableListOf<City>()
        while (cursor.moveToNext()) {
            val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
            favorites.add(City(name))
        }
        cursor.close()
        return favorites
    }

    // Toggle Favorite Status of a City
    fun toggleFavoriteCity(name: String) {
        val db = this.writableDatabase
        val cursor = db.query(
            TABLE_CITIES,
            arrayOf(COLUMN_IS_FAVORITE),
            "$COLUMN_NAME = ?",
            arrayOf(name),
            null,
            null,
            null
        )

        if (cursor.moveToFirst()) {
            val currentFavorite = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_FAVORITE)) == 1
            val newFavorite = if (currentFavorite) 0 else 1
            println("CityDb.toggleFavoriteCity: Toggling $name, currentFavorite = $currentFavorite, newFavorite = $newFavorite")
            val values = ContentValues().apply {
                put(COLUMN_IS_FAVORITE, newFavorite)
            }
            db.update(TABLE_CITIES, values, "$COLUMN_NAME = ?", arrayOf(name))
        } else {
            println("CityDb.toggleFavoriteCity: City $name not found in database.")
        }
        cursor.close()
    }

    // Method to delete all cities except favorites
    fun deleteAllExceptFavorites() {
        val db = this.writableDatabase
        val rowsDeleted = db.delete(
            TABLE_CITIES,
            "$COLUMN_IS_FAVORITE = ?",
            arrayOf("0")
        )
        println("CityDb.deleteAllExceptFavorites: Deleted $rowsDeleted rows (non-favorites)")
    }
}