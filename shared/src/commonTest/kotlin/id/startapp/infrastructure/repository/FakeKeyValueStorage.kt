package id.startapp.infrastructure.repository

import id.startapp.infrastructure.storage.KeyValueStorage

/**
 * In-memory implementation of KeyValueStorage for testing.
 */
class FakeKeyValueStorage : KeyValueStorage {
    private val store = mutableMapOf<String, Any?>()

    override suspend fun putString(key: String, value: String?) {
        if (value != null) store[key] = value else store.remove(key)
    }

    override suspend fun getString(key: String): String? = store[key] as? String

    override suspend fun putInt(key: String, value: Int?) {
        if (value != null) store[key] = value else store.remove(key)
    }

    override suspend fun getInt(key: String): Int? = store[key] as? Int

    override suspend fun putBoolean(key: String, value: Boolean?) {
        if (value != null) store[key] = value else store.remove(key)
    }

    override suspend fun getBoolean(key: String): Boolean? = store[key] as? Boolean

    override suspend fun putLong(key: String, value: Long?) {
        if (value != null) store[key] = value else store.remove(key)
    }

    override suspend fun getLong(key: String): Long? = store[key] as? Long

    override suspend fun remove(key: String) { store.remove(key) }

    override suspend fun clearAll() { store.clear() }
}
