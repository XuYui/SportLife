package com.sportlife.records.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `training_day_sections` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `trainingDayId` INTEGER NOT NULL,
                `name` TEXT NOT NULL,
                `bodyPart` TEXT,
                `sortOrder` INTEGER NOT NULL,
                FOREIGN KEY(`trainingDayId`) REFERENCES `training_days`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_training_day_sections_trainingDayId` ON `training_day_sections` (`trainingDayId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_training_day_sections_trainingDayId_sortOrder` ON `training_day_sections` (`trainingDayId`, `sortOrder`)")
        db.execSQL("ALTER TABLE `training_plan_exercises` ADD COLUMN `sectionId` INTEGER")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_training_plan_exercises_sectionId` ON `training_plan_exercises` (`sectionId`)")
    }
}
