package com.wisdplat.amaptest

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import com.amap.api.location.AMapLocationClient
import com.amap.api.maps.MapsInitializer
import com.amap.api.services.core.ServiceSettings
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.models.PermissionRequest
import com.wisdplat.amaptest.ui.theme.AMapTestTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //定位隐私政策同意
        AMapLocationClient.updatePrivacyShow(applicationContext,true,true);
        AMapLocationClient.updatePrivacyAgree(applicationContext,true);
        //地图隐私政策同意
        MapsInitializer.updatePrivacyShow(applicationContext,true,true);
        MapsInitializer.updatePrivacyAgree(applicationContext,true);
        //搜索隐私政策同意
        ServiceSettings.updatePrivacyShow(applicationContext,true,true);
        ServiceSettings.updatePrivacyAgree(applicationContext,true);
        MapsInitializer.setTerrainEnable(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 1);
            }
        }
        val permissions = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        val REQUEST_CODE = 9527
        val build = PermissionRequest.Builder(this).code(REQUEST_CODE)
            .perms(permissions)
            .build()
        EasyPermissions.requestPermissions(this, build)


        setContent {
            AMapTestTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Map()
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AMapTestTheme {
    }
}