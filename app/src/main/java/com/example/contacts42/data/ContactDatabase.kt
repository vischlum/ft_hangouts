package com.example.contacts42.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.contacts42.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@Database(entities = [Contact::class], version = 1, exportSchema = false)
abstract class ContactDatabase : RoomDatabase() {

    abstract fun contactDao(): ContactDao

    class Callback @Inject constructor(
        private val database: Provider<ContactDatabase>,
        @ApplicationScope private val applicationScope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            val dao = database.get().contactDao()

            applicationScope.launch {
                dao.insert(
                    Contact(
                        "Poulet Braisé",
                        true,
                        "07 68 53 85 32",
                        "01 22 33 44 55",
                        "poulet@brai.se",
                        "poulet@datm.fr"
                    )
                )
                dao.insert(
                    Contact(
                        "Jean Bonneau",
                        email_home = "jeannot@lap.in",
                        email_work = "jean.bonneau@herta.fr"
                    )
                )
                dao.insert(
                    Contact(
                        "Charles Magne",
                        true,
                        phone_work = "+49 241 18029 0",
                        email_home = "fast@furio.us"
                    )
                )
                dao.insert(
                    Contact(
                        "Charles Attend",
                        email_home = "slow@furio.us",
                        email_work = "presto@res.to"
                    )
                )
            }
        }
    }
}