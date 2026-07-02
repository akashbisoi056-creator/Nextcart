package com.example.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import kotlinx.coroutines.flow.Flow

@Dao
interface MarketplaceDao {

    // --- Users ---
    @Query("SELECT * FROM users WHERE id = :id")
    fun getUserById(id: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmailDirect(email: String): UserEntity?

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    // --- Sellers ---
    @Query("SELECT * FROM sellers WHERE id = :id")
    fun getSellerById(id: String): Flow<SellerEntity?>

    @Query("SELECT * FROM sellers WHERE email = :email LIMIT 1")
    suspend fun getSellerByEmailDirect(email: String): SellerEntity?

    @Query("SELECT * FROM sellers")
    fun getAllSellers(): Flow<List<SellerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeller(seller: SellerEntity)

    // --- Products ---
    @Query("SELECT * FROM products")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    fun getProductById(id: Int): Flow<ProductEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProductById(id: Int)

    // --- Orders ---
    @Query("SELECT * FROM orders ORDER BY orderDate DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE buyerId = :buyerId ORDER BY orderDate DESC")
    fun getOrdersByBuyer(buyerId: String): Flow<List<OrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    // --- Configurations ---
    @Query("SELECT * FROM configs WHERE id = 'marketplace_config'")
    fun getConfigFlow(): Flow<ConfigEntity?>

    @Query("SELECT * FROM configs WHERE id = 'marketplace_config'")
    suspend fun getConfig(): ConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(config: ConfigEntity)
}

@Database(
    entities = [
        UserEntity::class,
        SellerEntity::class,
        ProductEntity::class,
        OrderEntity::class,
        ConfigEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun marketplaceDao(): MarketplaceDao
}
