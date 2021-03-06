package com.hexagonkt.store.mongodb

import com.hexagonkt.helpers.filterEmpty
import com.hexagonkt.serialization.convertToMap
import com.hexagonkt.serialization.convertToObject
import com.hexagonkt.store.Mapper
import org.bson.BsonString
import java.net.URL
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class MongoDbMapper<T : Any, K : Any>(
    private val type: KClass<T>,
    private val key: KProperty1<T, K>
): Mapper<T> {

    override fun toStore(instance: T): Map<String, Any> =
        (instance.convertToMap() + ("_id" to key.get(instance)))
            .filterEmpty()
            .mapKeys { it.key.toString() }
            .mapValues { it.value as Any }

    @Suppress("UNCHECKED_CAST")
    override fun fromStore(map: Map<String, Any>): T = map.filterEmpty().convertToObject(type)

    /*
     * TODO Pick field types
     */
    override fun fromStore(property: String, value: Any): Any = when (value) {
        is BsonString -> value.value
        else -> value
    }

    override fun toStore(property: String, value: Any): Any = when (value) {
        is URL -> value.toString()
        else -> value
    }
}
