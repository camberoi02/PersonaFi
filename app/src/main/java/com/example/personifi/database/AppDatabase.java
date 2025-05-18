package com.example.personifi.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.personifi.Budget;
import com.example.personifi.DateConverter;
import com.example.personifi.SavingsGoal;
import com.example.personifi.Transaction;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The Room database for the PersoniFi app.
 */
@Database(entities = {Transaction.class, Budget.class, SavingsGoal.class}, version = 3, exportSchema = false)
@TypeConverters({DateConverter.class})
public abstract class AppDatabase extends RoomDatabase {

    public abstract TransactionDao transactionDao();
    public abstract BudgetDao budgetDao();
    public abstract SavingsGoalDao savingsGoalDao();

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    /**
     * Get the database instance.
     */
    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "personifi_database")
                            .fallbackToDestructiveMigration()
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Callback for database creation or opening.
     */
    private static final RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

            // If you want to keep data through app restarts,
            // comment out the following block
            databaseWriteExecutor.execute(() -> {
                // Populate the database in the background.
                // If you want to start with more transactions, just add them.
                TransactionDao dao = INSTANCE.transactionDao();
                // Clear all data
                // dao.deleteAll(); // This method would need to be added to the DAO

                // Add sample data if desired here
                // Transaction transaction = new Transaction(...);
                // dao.insert(transaction);
            });
        }
    };
}