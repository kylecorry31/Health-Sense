package com.kylecorry.healthsense.virus.domain

import com.kylecorry.trailsensecore.domain.units.Temperature

interface ITemperatureService {
    fun classifyBodyTemperature(temperature: Temperature): BodyTemperature
}