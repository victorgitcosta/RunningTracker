package com.exemplo.runningtracker.data.local

import androidx.room.TypeConverter
import com.exemplo.runningtracker.domain.model.RoutePoint
import org.json.JSONArray
import org.json.JSONObject

/** JSON sem dependência externa. O banco guarda routeCoordinates como String. */
class RouteJsonConverter {
    @TypeConverter
    fun fromPoints(points: List<RoutePoint>): String {
        val array = JSONArray()
        points.forEach { point ->
            array.put(JSONObject().put("lat", point.latitude).put("lng", point.longitude))
        }
        return array.toString()
    }

    @TypeConverter
    fun toPoints(json: String): List<RoutePoint> = runCatching {
        val array = JSONArray(json)
        (0 until array.length()).map { index ->
            val obj = array.getJSONObject(index)
            RoutePoint(obj.getDouble("lat"), obj.getDouble("lng"))
        }
    }.getOrDefault(emptyList())
}
