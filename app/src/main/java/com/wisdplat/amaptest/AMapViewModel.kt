package com.wisdplat.amaptest

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.amap.api.navi.model.AMapCalcRouteResult
import com.amap.api.navi.model.AMapNaviPath
import com.amap.api.navi.view.RouteOverLay
import kotlinx.coroutines.flow.MutableStateFlow

/**
 *
@author xuleyu
@Email xuleyumail@gmail.com
@create 2023-10-23 9:22
 *
 */
class AMapViewModel : ViewModel() {

    val routeShow = MutableStateFlow(false)
    var routes = ArrayList<MAMapNaviPath>()

    val mAMapNaviListener = object : WisplatAMapNaviListener() {
        override fun onCalculateRouteSuccess(p0: AMapCalcRouteResult?) {
            super.onCalculateRouteSuccess(p0)
            val paths = ArrayList<MAMapNaviPath>()
            for (i in 0 until p0!!.routeid.size) {
                val path = aMapNavi.naviPaths.get(p0.routeid.get(i))
                val routeOverLay = RouteOverLay(aMap, path, App.context)
                routeOverLay.showEndMarker(false)
                routeOverLay.isTrafficLine = true
                routeOverLay.addToMap()
                path?.let {
                    paths.add(MAMapNaviPath(it,routeOverLay))
                }
                routes = paths
                routeShow.value = true
            }
        }
    }
}