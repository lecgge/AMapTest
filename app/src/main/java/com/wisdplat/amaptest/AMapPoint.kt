package com.wisdplat.amaptest

import com.amap.api.maps.model.LatLng
import com.amap.api.services.core.LatLonPoint

/**
 *
@author xuleyu
@Email xuleyumail@gmail.com
@create 2023-10-20 15:57
 *
 */
data class AMapPoint(var name:String,var position:LatLonPoint){

    fun getLatLon():LatLng{
        return LatLng(position.latitude,position.longitude)
    }
}