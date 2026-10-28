package com.vcm.adoptamascota.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [PetEntity::class, LostPetEntity::class, AdoptionRequestEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun petDao(): PetDao
    abstract fun lostPetDao(): LostPetDao
    abstract fun adoptionRequestDao(): AdoptionRequestDao

    companion object {
        /** Migración 1 -> 2: agrega la tabla lost_pets SIN borrar las mascotas ya registradas. */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `lost_pets` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`name` TEXT NOT NULL, `species` TEXT NOT NULL, `lastSeen` TEXT NOT NULL, " +
                        "`description` TEXT NOT NULL, `contactPhone` TEXT NOT NULL, `imageUri` TEXT, " +
                        "`dateReported` TEXT NOT NULL, `isFound` INTEGER NOT NULL)"
                )
            }
        }

        /**
         * Migración 2 -> 3: las mascotas existentes pasan a ser "de compañía" (valor por defecto),
         * se agregan los campos de exótica y de ubicación, y se crea la tabla de solicitudes.
         */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `pets` ADD COLUMN `category` TEXT NOT NULL DEFAULT 'COMPANION'")
                db.execSQL("ALTER TABLE `pets` ADD COLUMN `exoticSpecies` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `pets` ADD COLUMN `permitNumber` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `pets` ADD COLUMN `foundPlace` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `pets` ADD COLUMN `foundLat` REAL")
                db.execSQL("ALTER TABLE `pets` ADD COLUMN `foundLng` REAL")
                db.execSQL("ALTER TABLE `pets` ADD COLUMN `lostPlace` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `pets` ADD COLUMN `lostLat` REAL")
                db.execSQL("ALTER TABLE `pets` ADD COLUMN `lostLng` REAL")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `adoption_requests` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`petId` INTEGER NOT NULL, `applicantName` TEXT NOT NULL, " +
                        "`applicantPhone` TEXT NOT NULL, `applicantEmail` TEXT NOT NULL, " +
                        "`message` TEXT NOT NULL, `permitNumber` TEXT NOT NULL, " +
                        "`status` TEXT NOT NULL, `dateRequested` TEXT NOT NULL)"
                )
            }
        }

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pet_adoption_db"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build().also { instance = it }
            }
    }
}
