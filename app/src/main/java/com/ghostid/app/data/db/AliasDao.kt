package com.ghostid.app.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AliasDao {

    // --- Alias operations ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlias(alias: AliasEntity)

    @Update
    suspend fun updateAlias(alias: AliasEntity)

    @Delete
    suspend fun deleteAlias(alias: AliasEntity)

    @Query("DELETE FROM aliases WHERE id = :aliasId")
    suspend fun deleteAliasById(aliasId: String)

    @Query("SELECT * FROM aliases ORDER BY createdAt DESC")
    fun observeAllAliases(): Flow<List<AliasEntity>>

    @Query("SELECT * FROM aliases WHERE id = :aliasId")
    suspend fun getAliasById(aliasId: String): AliasEntity?

    @Query("SELECT * FROM aliases ORDER BY createdAt DESC")
    suspend fun getAllAliases(): List<AliasEntity>

    // --- Account operations ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccounts(accounts: List<AccountEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity)

    @Query("SELECT * FROM accounts WHERE aliasId = :aliasId")
    suspend fun getAccountsForAlias(aliasId: String): List<AccountEntity>

    @Query("SELECT * FROM accounts WHERE aliasId = :aliasId")
    fun observeAccountsForAlias(aliasId: String): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts ORDER BY aliasId, platformName")
    fun observeAllAccounts(): Flow<List<AccountEntity>>

    @Query("DELETE FROM accounts WHERE aliasId = :aliasId")
    suspend fun deleteAccountsForAlias(aliasId: String)

    @Transaction
    suspend fun replaceAccountsForAlias(aliasId: String, accounts: List<AccountEntity>) {
        deleteAccountsForAlias(aliasId)
        insertAccounts(accounts)
    }

    // --- Health check ---

    @Query("""
        SELECT username FROM accounts
        GROUP BY username HAVING COUNT(*) > 1
    """)
    suspend fun findDuplicateUsernames(): List<String>
}
