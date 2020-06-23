package co.studycode.newsapp.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import co.studycode.newsapp.models.Article

@Database(
    entities = [Article::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class AppDatabase:RoomDatabase() {

   abstract fun getArticlesDao():ArticleDao
    companion object{
        @Volatile
        private var instace:AppDatabase?=null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instace?: synchronized(LOCK){
            instace?:createDatabase(context).also{
                instace =it
            }
        }

        private fun createDatabase(context: Context)=Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "article_db.db"
        ).build()

    }
}