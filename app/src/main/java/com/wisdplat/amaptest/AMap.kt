package com.wisdplat.amaptest

import android.content.ComponentCallbacks
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.AMap
import com.amap.api.maps.AMapOptions
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.LocationSource
import com.amap.api.maps.MapView
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.maps.model.MyLocationStyle
import com.amap.api.navi.AMapNavi
import com.amap.api.navi.model.AMapCalcRouteResult
import com.amap.api.navi.model.NaviLatLng
import com.amap.api.navi.view.RouteOverLay
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.geocoder.GeocodeResult
import com.amap.api.services.geocoder.GeocodeSearch
import com.amap.api.services.geocoder.RegeocodeQuery
import com.amap.api.services.geocoder.RegeocodeResult
import com.wisdplat.amaptest.App.Companion.context
import com.wisdplat.amaptest.utils.MapUtil


/**
 *
@author xuleyu
@Email xuleyumail@gmail.com
@create 2023-10-19 16:01
 *
 */


//声明AMapLocationClient类对象
private lateinit var mLocationClient: AMapLocationClient
//声明AMapLocationClientOption对象
private lateinit var mLocationOption: AMapLocationClientOption
//地图控制器
private lateinit var aMap: AMap
//定位样式
private val myLocationStyle = MyLocationStyle()
//位置更改监听
private var mListener: LocationSource.OnLocationChangedListener? = null
//地理编码搜索
private lateinit var geocodeSearch: GeocodeSearch
private lateinit var aMapNavi: AMapNavi

@Composable
fun Map() {
    var mStartPoint by remember { mutableStateOf(AMapPoint("", LatLonPoint(0.0, 0.0))) }
    var mEndpoint by remember { mutableStateOf(AMapPoint("", LatLonPoint(0.0, 0.0))) }


    val searchListener = object : GeocodeSearch.OnGeocodeSearchListener {
        override fun onRegeocodeSearched(p0: RegeocodeResult, p1: Int) {
            mEndpoint = AMapPoint(
                p0.regeocodeAddress.formatAddress,
                LatLonPoint(p0.regeocodeQuery.point.latitude, p0.regeocodeQuery.point.longitude)
            )
            aMap.addMarker(MarkerOptions().position(mEndpoint.getLatLon()).title("终点").snippet("DefaultMarker"))
        }

        /**
         * 地址转坐标
         *
         * @param geocodeResult
         * @param rCode
         */
        override fun onGeocodeSearched(geocodeResult: GeocodeResult, rCode: Int) {

        }

    }
    val locationListener = AMapLocationListener { aMapLocation ->
        /**
         * 接收异步返回的定位结果
         *
         * @param aMapLocation
         */
        if (aMapLocation != null) {
            if (aMapLocation.errorCode == 0) {
                val lat = "%.6f".format(aMapLocation.latitude)
                val lon = "%.6f".format(aMapLocation.longitude)
                val latLng = LatLng(lat.toDouble(), lon.toDouble())
                //设置起点
                mStartPoint =
                    AMapPoint(aMapLocation.address, MapUtil.convertToLatLonPoint(latLng))
                //显示地图定位结果
                mLocationClient.stopLocation()
                mListener?.onLocationChanged(aMapLocation)
            } else {
                //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                Log.e(
                    "AmapError",
                    "location Error, ErrCode:" + aMapLocation.errorCode + ", errInfo:" + aMapLocation.errorInfo
                )
//                        Toast.makeText(context, resources.getString(R.string.seek_failed), Toast.LENGTH_SHORT).show()
            }
        }
    }
    Box {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 5.dp, start = 5.dp)
                .background(
                    Color.White
                )
                .size(300.dp, 350.dp)
                .zIndex(1f)
        ) {
            Column {
                PositionInput(
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .background(Color.Gray, CircleShape)
                        .height(30.dp)
                        .fillMaxWidth(), Icons.Filled.LocationOn, "请输入起点", mStartPoint.name
                )
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.White)
                )
                PositionInput(
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .background(Color.Gray, CircleShape)
                        .height(30.dp)
                        .fillMaxWidth(), Icons.Filled.LocationOn, "请输入途径点", ""
                )
                PositionInput(
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .background(Color.Gray, CircleShape)
                        .height(30.dp)
                        .fillMaxWidth(), Icons.Filled.LocationOn, "请输入终点", mEndpoint.name
                )
                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = {
                        val startList = ArrayList<NaviLatLng>()
                        startList.add(NaviLatLng(mStartPoint.position.latitude, mStartPoint.position.longitude))
                        val endList = ArrayList<NaviLatLng>()
                        endList.add(NaviLatLng(mEndpoint.position.latitude, mEndpoint.position.longitude))
                        val strategy: Int = aMapNavi.strategyConvert(
                            //congestion 躲避拥堵
                            true,
                            //不走高速
                            false,
                            //避免收费
                            false,
                            //高速优先
                            true,
                            //多路径
                            true
                        )
                        aMapNavi.calculateDriveRoute(
                            startList,
                            endList,
                            null,
                            strategy
                        )
                    }) {
                        Text(text = "计算路线")
                    }
                }
            }
        }
        Box(modifier = Modifier.fillMaxSize()) {
            AMap(
                modifier = Modifier,
                locationListener = locationListener,
                searchListener = searchListener
            )
        }
    }
}


@Composable
fun AMap(
    modifier: Modifier,
    locationListener: AMapLocationListener,
    searchListener: GeocodeSearch.OnGeocodeSearchListener
) {
    val context = LocalContext.current
    val aMapOptionsFactory: () -> AMapOptions = { AMapOptions() }
    val mapView = remember {
        MapView(context, aMapOptionsFactory())
    }
    aMap = mapView.map
    aMapNavi = AMapNavi.getInstance(LocalContext.current)
    mLocationClient = AMapLocationClient(context)
    initLocation(locationListener)
    initMap(LocalContext.current, searchListener)
    // 添加MapView
    AndroidView(modifier = modifier, factory = { mapView })

    MapLifecycle(
        mapView = mapView,
        onCreate = { mapView.onCreate(Bundle()) },
        onResume = {
            mapView.onResume()
            aMapNavi.addAMapNaviListener(mAMapNaviListener) },
        onPause = {
            mapView.onPause()
        },
        onDestroy = {
            mLocationClient.onDestroy()
            mapView.onDestroy()
        })
}


@Composable
private fun MapLifecycle(
    mapView: MapView,
    onCreate: () -> Unit,
    onResume: () -> Unit,
    onPause: () -> Unit,
    onDestroy: () -> Unit
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(context, lifecycle, mapView) {
        val mapLifecycleObserver = lifecycleObserver(onCreate, onResume, onPause, onDestroy)
        val callbacks = mapView.componentCallbacks()
        // 添加生命周期观察者
        lifecycle.addObserver(mapLifecycleObserver)
        // 注册ComponentCallback
        context.registerComponentCallbacks(callbacks)

        onDispose {
            // 删除生命周期观察者
            lifecycle.removeObserver(mapLifecycleObserver)
            // 取消注册ComponentCallback
            context.unregisterComponentCallbacks(callbacks)
        }
    }
}

// 管理地图生命周期
private fun lifecycleObserver(
    onCreate: () -> Unit,
    onResume: () -> Unit,
    onPause: () -> Unit,
    onDestroy: () -> Unit
): LifecycleEventObserver =
    LifecycleEventObserver { _, event ->
        when (event) {
            Lifecycle.Event.ON_CREATE -> onCreate()
            Lifecycle.Event.ON_RESUME -> onResume() // 重新绘制加载地图
            Lifecycle.Event.ON_PAUSE -> onPause()  // 暂停地图的绘制
            Lifecycle.Event.ON_DESTROY -> onDestroy() // 销毁地图
            else -> {}
        }
    }

private fun MapView.componentCallbacks(): ComponentCallbacks =
    object : ComponentCallbacks {
        // 设备配置发生改变，组件还在运行时
        override fun onConfigurationChanged(config: Configuration) {}

        // 系统运行的内存不足时，可以通过实现该方法去释放内存或不需要的资源
        override fun onLowMemory() {
            // 调用地图的onLowMemory
            this@componentCallbacks.onLowMemory()
        }
    }

@Composable
fun PositionInput(
    modifier: Modifier,
    icon: ImageVector,
    hint: String,
    test: String,
    confirm: (position: String) -> Unit = {}
) {
    var text by remember {
        mutableStateOf("")
    }
    BasicTextField(
        value = text.ifEmpty { test },
        singleLine = true,
        onValueChange = { text = it },
        decorationBox = { innerTextField ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 2.dp, horizontal = 8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = "位置搜索"
                )
                Box(
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .width(200.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (test.isEmpty() && text.isEmpty()) {
                        Text(
                            text = hint,
                            style = TextStyle(color = Color(0, 0, 0, 128))
                        )
                    }
                    innerTextField()
                }

                IconButton(
                    onClick = { confirm(text) },
                    enabled = text.isNotEmpty(),
                    modifier = Modifier.size(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Done, contentDescription = ""
                    )
                }

            }
        },
        modifier = modifier
    )
}


/**
 * 初始化地图
 */
private fun initMap(context: Context, listener: GeocodeSearch.OnGeocodeSearchListener) {
    //设置最小缩放等级为16 ，缩放级别范围为[3, 20]
//        aMap.minZoomLevel = 16f
    aMap.moveCamera(CameraUpdateFactory.zoomTo(16f))
    //开启室内地图
    aMap.showIndoorMap(false)
    //实例化UiSettings类对象
    val mUiSettings = aMap.uiSettings
    //隐藏缩放按钮 默认显示
    mUiSettings.isZoomControlsEnabled = true
    //显示比例尺 默认不显示
    mUiSettings.isScaleControlsEnabled = true
    // 自定义定位蓝点图标
    myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.gps_point))

    myLocationStyle.radiusFillColor(android.graphics.Color.parseColor("#00000000"))
    myLocationStyle.strokeWidth(0f)
    //设置定位蓝点的Style
    aMap.myLocationStyle = myLocationStyle
    // 设置定位监听
    aMap.setLocationSource(object : LocationSource {
        /**
         * 激活定位
         */
        override fun activate(p0: LocationSource.OnLocationChangedListener) {
            mListener = p0
            mLocationClient.startLocation()
        }

        /**
         * 停止定位
         */
        override fun deactivate() {
            mLocationClient.stopLocation()
            mLocationClient.onDestroy()
        }
    })
    // 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
    aMap.isMyLocationEnabled = true

    //地图点击监听
    aMap.setOnMapClickListener { latLng ->
        aMap.clear(true)

        //终点
        val clickPoint = MapUtil.convertToLatLonPoint(latLng)
        geocodeSearch.getFromLocationAsyn(
            RegeocodeQuery(
                clickPoint,
                500f,
                GeocodeSearch.AMAP
            )
        )
    }

    //构造 GeocodeSearch 对象
    geocodeSearch = GeocodeSearch(context)
    //设置监听
    geocodeSearch.setOnGeocodeSearchListener(listener)

}

/**
 * 初始化定位
 */
private fun initLocation(listener: AMapLocationListener) {

    //设置定位回调监听
    mLocationClient.setLocationListener(listener)
    //初始化AMapLocationClientOption对象
    mLocationOption = AMapLocationClientOption()
    //使用GPS定位模式
    mLocationOption.locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
    //获取最近3s内精度最高的一次定位结果：
    //设置setOnceLocationLatest(boolean b)接口为true，启动定位时SDK会返回最近3s内精度最高的一次定位结果。如果设置其为true，setOnceLocation(boolean b)接口也会被设置为true，反之不会，默认为false。
    mLocationOption.isOnceLocationLatest = true
    //设置是否返回地址信息（默认返回地址信息）
    mLocationOption.isNeedAddress = true
    //设置定位请求超时时间，单位是毫秒，默认30000毫秒，建议超时时间不要低于8000毫秒。
    mLocationOption.httpTimeOut = 10000
    //设置定位间隔,单位毫秒,默认为2000ms，最低1000ms。
    mLocationOption.interval = 1000
    //关闭缓存机制，高精度定位会产生缓存。
    mLocationOption.isLocationCacheEnable = false
    //给定位客户端对象设置定位参数
    mLocationClient.setLocationOption(mLocationOption)
}

private val mAMapNaviListener = object : WisplatAMapNaviListener() {
    override fun onCalculateRouteFailure(p0: AMapCalcRouteResult?) {
        super.onCalculateRouteFailure(p0)

    }

    override fun onCalculateRouteSuccess(p0: AMapCalcRouteResult?) {
        super.onCalculateRouteSuccess(p0)
        val paths = ArrayList<MAMapNaviPath>()
        aMapNavi.naviPaths.forEach { (_, path) ->
            val routeOverLay = RouteOverLay(aMap, path, context)
            routeOverLay.showEndMarker(false)
            routeOverLay.isTrafficLine = true
            routeOverLay.addToMap()
            paths.add(MAMapNaviPath(path,routeOverLay))
        }
    }
}

