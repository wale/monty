package au.id.wale.monty.entities.ridespace

import java.util.Date

data class RideSpaceDeparture(
    val name: String,
    val heading: String,
    val label: String?,
    val destination: String,
    val trips: List<RideSpaceTrip>
)

data class RideSpaceTrip(
    val tripId: String,
    val transportType: String,
    val name: String,
    val label: String,
    val subLabel: String,
    val stopId: String,
    val stopName: String,
    val arrivalTime: Date,
    val departureTime: Date,
    val arrivalLabel: String, // TODO: Change this to an enum
    val arrivalType: String, // TODO: Change this to an enum
    val capacity: String,
    val capacityLevel: Int,
    val capacityClass: String,
    val platform: Int,
    val platformCapacity: String, // TODO: Change this to an enum
    val platformCapacityLevel: Int,
    val platformCapacityClass: String,
    val destination: String,
    val localDepartureTime: String,
    val scheduledDepartureTime: Date
)