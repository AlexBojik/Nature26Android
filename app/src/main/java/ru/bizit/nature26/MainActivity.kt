package ru.bizit.nature26

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.RectF
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.graphics.toColorInt
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.*
import com.mapbox.geojson.Feature
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.expressions.Expression.*
import com.mapbox.mapboxsdk.style.layers.*
import com.mapbox.mapboxsdk.style.layers.Property.VISIBLE
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.style.sources.RasterSource
import com.mapbox.mapboxsdk.style.sources.TileSet
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.rxjava3.kotlin.subscribeBy
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.bizit.nature26.common.Common
import ru.bizit.nature26.interfaces.Filter
import ru.bizit.nature26.interfaces.RetrofitServices
import java.net.URI
import java.util.*
import javax.inject.Inject
import javax.inject.Named


@Module
abstract class MainActivityModule {
    @ContributesAndroidInjector
    abstract fun contributeActivityInjector(): MainActivity?
}

class MainActivity: DaggerAppCompatActivity(), PermissionsListener {
    private var mapView: MapView? = null
    private lateinit var mapboxMap: MapboxMap
    private var permissions: Array<String> = arrayOf(Manifest.permission.CAMERA)

    @Inject
    @Named("Long Live")
    lateinit var appData: AppData

    private lateinit var mService: RetrofitServices
    private var permissionsManager: PermissionsManager = PermissionsManager(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appData.newBase.onNext(appData.oldBase)
        appData.layersChanged.onNext(false)
        appData.featureLoading.onNext(false)

        Mapbox.getInstance(this, null)
        setContentView(R.layout.activity_main)

        mService = Common.retrofitService
        getBaseLayers()
        getLayers()
        getUser()

        mapView = findViewById(R.id.mapView)
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync { mapboxMap ->
            this.mapboxMap = mapboxMap
            this.mapboxMap.setStyle(Style.Builder().fromJson(appData.background)) { style ->
                this.addBaseLayer(style)

                style.addImage("tech", BitmapFactory.decodeResource(resources, R.mipmap.tech))
                style.addImage("fish", BitmapFactory.decodeResource(resources, R.mipmap.fish))
                style.addImage("san", BitmapFactory.decodeResource(resources, R.mipmap.san))
                style.addImage("pit", BitmapFactory.decodeResource(resources, R.mipmap.pit))
                style.addImage("rekr", BitmapFactory.decodeResource(resources, R.mipmap.rekr))
                style.addImage("reddozer", BitmapFactory.decodeResource(resources, R.mipmap.reddozer))
                style.addImage("greendozer", BitmapFactory.decodeResource(resources, R.mipmap.greendozer))
                style.addImage("cont", BitmapFactory.decodeResource(resources, R.mipmap.cont))
                style.addImage("dam", BitmapFactory.decodeResource(resources, R.mipmap.dam))
                style.addImage("resh", BitmapFactory.decodeResource(resources, R.mipmap.resh))
                style.addImage("tree", BitmapFactory.decodeResource(resources, R.mipmap.tree))
                style.addImage("simpletree", BitmapFactory.decodeResource(resources, R.mipmap.simpletree))
                style.addImage("duck", BitmapFactory.decodeResource(resources, R.mipmap.duck))
                style.addImage("min", BitmapFactory.decodeResource(resources, R.mipmap.min))
                style.addImage("list", BitmapFactory.decodeResource(resources, R.mipmap.list))
                style.addImage("planet", BitmapFactory.decodeResource(resources, R.mipmap.planet))
                style.addImage("hands", BitmapFactory.decodeResource(resources, R.mipmap.hands))

                val baseButton = findViewById<FloatingActionButton>(R.id.baseLayersButton)
                baseButton.setOnClickListener {
                    BaseLayersBottomSheetDialog().apply {
                        show(supportFragmentManager, BaseLayersBottomSheetDialog.TAG)
                    }
                }

                val layersButton = findViewById<FloatingActionButton>(R.id.layersButton)
                layersButton.setOnClickListener {
                    LayersBottomSheetDialog().apply {
                        show(supportFragmentManager, LayersBottomSheetDialog.TAG)
                    }
                }

                val locateButton = findViewById<FloatingActionButton>(R.id.locate)
                locateButton.setOnClickListener {
                    enableLocationComponent(style)
                }

                val shareButton = findViewById<FloatingActionButton>(R.id.share)
                shareButton.setOnClickListener {
                    if (appData.user.name.isEmpty()) {
                        val intent = Intent(this, AuthenticationActivity::class.java)
                        startActivityForResult(intent, 1)
                    } else {
                        ActivityCompat.requestPermissions(this, permissions, 200)
                    }
                }

                val warningButton = findViewById<FloatingActionButton>(R.id.warningButton)
                warningButton.setOnClickListener {
                    getNews()
                }

                val searchButton = findViewById<FloatingActionButton>(R.id.searchButton)
                val searchTextEdit = findViewById<EditText>(R.id.searchTextEdit)
                val searchView = findViewById<View>(R.id.searchView)
                searchButton.setOnClickListener {
                    searchButton.visibility = View.INVISIBLE
                    searchTextEdit.visibility = View.VISIBLE
                    searchView.visibility = View.VISIBLE
                }

                searchTextEdit.onSubmit {
                    val imm: InputMethodManager = searchTextEdit.context.getSystemService(
                        INPUT_METHOD_SERVICE
                    ) as InputMethodManager
                    getFeatures(searchTextEdit.text.toString())
                    imm.hideSoftInputFromWindow(searchTextEdit.windowToken, 0)

                    searchButton.visibility = View.VISIBLE
                    searchTextEdit.visibility = View.INVISIBLE
                    searchView.visibility = View.INVISIBLE

                }

                appData.newBase.subscribeBy(
                    onNext = { newBase -> changeBase(newBase, style) }
                )

                appData.layersChanged.subscribeBy(
                    onNext = { changed ->
                        if (changed) {
                            updateLayers(style)
                        }
                    }
                )

                appData.featureLoading.subscribeBy(
                    onNext = { loading ->
                        if (!loading && appData.loadedFeatures.isNotEmpty()) {
                            FeaturesBottomSheetDialog().apply {
                                show(supportFragmentManager, FeaturesBottomSheetDialog.TAG)
                            }
                        }
                    }
                )

                appData.featureToFly.subscribeBy {
                    appData.layers.forEach { l ->
                        l.layers?.forEach { lc ->
                            if (lc.id == it.geoObject.layerId) {
                                lc.visible = true
                            }
                        }
                    }
                    appData.layersChanged.onNext(true)

                    val type = it.geoObject.geoJson?.get("type")?.asString
                    val pos = LatLng()

                    when (type) {
                        "Point" -> {
                            val p = Point.fromJson(it.geoObject.geoJson.toString())
                            pos.latitude = p.latitude()
                            pos.longitude = p.longitude()
                        }
                        "Polygon" -> {
                            val p = Polygon.fromJson(it.geoObject.geoJson.toString()).coordinates()[0][0]
                            pos.latitude = p.latitude()
                            pos.longitude = p.longitude()
                        }
                        "MultiPolygon" -> {
                            val p = MultiPolygon.fromJson(it.geoObject.geoJson.toString()).coordinates()[0][0][0]
                            pos.latitude = p.latitude()
                            pos.longitude = p.longitude()
                        }
                    }

                    val position = CameraPosition.Builder().target(pos).zoom(15.0).build()
                    mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 1000)
                }
                enableLocationComponent(style)
            }
            this.mapboxMap.addOnMapClickListener { point ->
                val pointF = mapboxMap.projection.toScreenLocation(point)
                val rectF =
                    RectF(pointF.x - 5, pointF.y - 5, pointF.x + 5, pointF.y + 5)
                val featureList: List<Feature> = mapboxMap.queryRenderedFeatures(rectF)
                if (featureList.isNotEmpty()) {
                    appData.selectedLayers.forEach { l ->
                        mapboxMap.style?.getLayer(l)?.setProperties(fillOpacity(.7f))
//                        mapboxMap.style?.getLayer("$l-point")?.setProperties(circleRadius(6f))
                    }
                    appData.selectedLayers = mutableListOf()
                    appData.features = mutableListOf()

                    featureList.forEach { feature ->
                        val id = feature.id().toString()
                        if (id.isNotBlank()) {
                            appData.features.add(id)
                            appData.selectedLayers.add(
                                "layer" + feature.getProperty("layerId").toString()
                            )
                        }
                    }
                    appData.featureLoading.onNext(true)
                    if (appData.features.isNotEmpty()) {
                        val filter = Filter(2, appData.features.joinToString(","))
                        mService.filter(getHeaderMap(), filter).enqueue(object : Callback<MutableList<GeoObject>> {
                            override fun onFailure(
                                call: Call<MutableList<GeoObject>>,
                                t: Throwable
                            ) {
                            }

                            override fun onResponse(
                                call: Call<MutableList<GeoObject>>,
                                response: Response<MutableList<GeoObject>>
                            ) {
                                appData.loadedFeatures = response.body() as MutableList<GeoObject>
                                appData.featureLoading.onNext(false)
                            }
                        })
                    }
                    appData.selectedLayers.forEach { l ->
                        mapboxMap.style?.getLayer(l)?.setProperties(
                            fillOpacity(
                                switchCase(
                                    `in`(
                                        toString(id()),
                                        literal(appData.features.joinToString(","))
                                    ),
                                    literal(.9f),
                                    literal(.7f)
                                )
                            )
                        )
//                        mapboxMap.style?.getLayer("$l-point")?.setProperties(
//                            circleRadius(switchCase(`in`(toString(id()),
//                                literal(appData.features.joinToString(","))),
//                                literal(8f),
//                                literal(6f))))
                    }
                    return@addOnMapClickListener true
                }
                return@addOnMapClickListener false
            }
        }
    }

    private fun EditText.onSubmit(func: () -> Unit) {
        setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                func()
            }
            true
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                val code = data?.dataString
                val sharedPref = getSharedPreferences("token", Context.MODE_PRIVATE) ?: return
                with(sharedPref.edit()) {
                    putString("token", code)
                    apply()
                }
                getUser()
            }
        }
    }

    private fun getUser() {
        val sharedPref = getSharedPreferences("token", Context.MODE_PRIVATE) ?: return
        val token = sharedPref.getString("token", "")
        if (token.isNullOrEmpty()) {
            return
        }
        mService.getUser(token = token).enqueue(object : Callback<User> {
            override fun onFailure(call: Call<User>, t: Throwable) {}

            override fun onResponse(
                call: Call<User>,
                response: Response<User>
            ) {
                appData.user = response.body() as User
            }
        })
    }

    private fun updateLayers(style: Style) {
        appData.layers.forEach { layer ->
            layer.layers?.forEach { child ->
                val id = child.id.toString()
                val source = style.getSource("layer$id")
                val l = style.getLayer("layer$id")
                val lp = style.getLayer("layer$id-point")
                val lc = style.getLayer("layer$id-cluster")
                val ll = style.getLayer("layer$id-line")

                l?.setProperties(visibility(Property.NONE))
                lp?.setProperties(visibility(Property.NONE))
                lc?.setProperties(visibility(Property.NONE))
                ll?.setProperties(visibility(Property.NONE))

                if (child.visible) {
                    l?.setProperties(visibility(VISIBLE))
                    lp?.setProperties(visibility(VISIBLE))
                    lc?.setProperties(visibility(VISIBLE))
                    ll?.setProperties(visibility(VISIBLE))
                }

                if (source == null && child.visible) {
                    loadGeoJson(child, style)
                }
            }
        }
    }

    private fun loadGeoJson(layer: Layer, style: Style) {
        val geoJsonUrl = URI("http://nature.mpr26.ru/api/layers/" + layer.id.toString())
        val source = GeoJsonSource("layer" + layer.id.toString(), geoJsonUrl)
        style.addSource(source)
        drawFill(layer, style)
        drawPoints(layer, style)
    }

    private fun drawFill(layer: Layer, style: Style) {
        if (!layer.color.isNullOrEmpty()) {
            val id = "layer" + layer.id.toString()
            val fillLayer = FillLayer(id, id)
            fillLayer.setProperties(layer.color?.toUpperCase(Locale.ENGLISH)?.toColorInt()?.let {
                fillColor(it)
            })

            fillLayer.withProperties(fillOpacity(.7f))
            fillLayer.setFilter(eq(geometryType(), literal("Polygon")))
            style.addLayer(fillLayer)
        }
        if (!layer.symbol.isNullOrEmpty()) {
            val idCluster = "layer" + layer.id.toString() + "-cluster"
            val geoJsonUrl = URI("http://nature.mpr26.ru/api/cluster/" + layer.id.toString())
            val source = GeoJsonSource(idCluster, geoJsonUrl)
            style.addSource(source)

            val symbolStyle = SymbolLayer(idCluster, idCluster)
            symbolStyle.setProperties(iconImage(layer.symbol))
            symbolStyle.setProperties(iconAllowOverlap(false))
            symbolStyle.setProperties(iconSize(0.5F))
            symbolStyle.setFilter(eq(geometryType(), literal("Point")))
            style.addLayer(symbolStyle)
        }
        if (layer.lineWidth > 0 && layer.lineColor?.isNotEmpty() == true) {
            val id = "layer" + layer.id.toString()
            val lineLayer = LineLayer("$id-line", id)
            lineLayer.setProperties(layer.lineColor?.toUpperCase(Locale.ENGLISH)?.toColorInt()?.let {
                lineColor(it)
            })
            lineLayer.setProperties(lineWidth(layer.lineWidth.toFloat()))
            lineLayer.setFilter(eq(geometryType(), literal("Polygon")))
            style.addLayer(lineLayer)
        }
    }

    private fun drawPoints(layer: Layer, style: Style) {
        val id = "layer" + layer.id.toString()

        if (layer.symbol.isNullOrEmpty()) {
            if (!layer.color.isNullOrEmpty()) {
                val pointStyle = CircleLayer("$id-point", id)
                pointStyle.setProperties(
                    layer.color?.toUpperCase(Locale.ENGLISH)?.toColorInt()?.let {
                        circleColor(it)
                    })
                pointStyle.setProperties(circleRadius(6f))
                pointStyle.setProperties(circleStrokeColor(Color.WHITE))
                pointStyle.setProperties(circleStrokeWidth(1f))
                pointStyle.setFilter(eq(geometryType(), literal("Point")))
                style.addLayer(pointStyle)
            }
        } else {
            val symbolStyle = SymbolLayer("$id-point", id)
            symbolStyle.setProperties(iconImage(layer.symbol))
            symbolStyle.setProperties(iconAllowOverlap(false))
            symbolStyle.setProperties(iconSize(0.5F))
            symbolStyle.setFilter(eq(geometryType(), literal("Point")))
            style.addLayer(symbolStyle)
        }

    }

    private fun getBaseLayers() {
        mService.getBaseLayers().enqueue(object : Callback<MutableList<BaseLayer>> {
            override fun onFailure(call: Call<MutableList<BaseLayer>>, t: Throwable) {}

            override fun onResponse(
                call: Call<MutableList<BaseLayer>>,
                response: Response<MutableList<BaseLayer>>
            ) {
                appData.baseLayers = response.body() as MutableList<BaseLayer>
            }
        })
    }

    private fun getFeatures(text: String) {
        val filter = Filter(1, text)
        mService.filter(getHeaderMap(), filter).enqueue(object : Callback<MutableList<GeoObject>> {
            override fun onFailure(
                call: Call<MutableList<GeoObject>>,
                t: Throwable
            ) {}

            override fun onResponse(
                call: Call<MutableList<GeoObject>>,
                response: Response<MutableList<GeoObject>>
            ) {
                appData.loadedFeatures = response.body() as MutableList<GeoObject>
                appData.featureLoading.onNext(false)
            }
        })
    }

    private fun getNews() {
        mService.getNews().enqueue(object : Callback<MutableList<News>> {
            override fun onFailure(call: Call<MutableList<News>>, t: Throwable) {}

            override fun onResponse(
                call: Call<MutableList<News>>,
                response: Response<MutableList<News>>
            ) {
                appData.loadedNews = response.body() as MutableList<News>
                 NewsBottomSheetDialog().apply {
                    show(supportFragmentManager, NewsBottomSheetDialog.TAG)
                }
            }
        })
    }

    private fun getLayers() {
        mService.getLayers(getHeaderMap()).enqueue(object : Callback<MutableList<Layer>> {
            override fun onFailure(call: Call<MutableList<Layer>>, t: Throwable) {}

            override fun onResponse(
                call: Call<MutableList<Layer>>,
                response: Response<MutableList<Layer>>
            ) {
                appData.layers = response.body() as MutableList<Layer>
                appData.layers.forEach {
                    it.layers?.forEach { child ->
                        appData.linearLayers[child.id] = child
                    }
                }
            }
        })
    }

    private fun getHeaderMap(): Map<String, String> {
        val headerMap = mutableMapOf<String, String>()
        headerMap["Token"] = getToken()?:""
        return headerMap
    }

    private fun getToken(): String? {
        return getSharedPreferences("token", Context.MODE_PRIVATE).getString("token", "")
    }

    private fun addBaseLayer(style: Style) {
        style.addSource(
            RasterSource(
                "default", TileSet(
                    "",
                    "https://mt1.google.com/vt/lyrs=y&x={x}&y={y}&z={z}"
                ), 256
            )
        )
        style.addLayer(RasterLayer("default", "default"))
    }

    private fun changeBase(id: String, style: Style) {
        if (this.appData.oldBase !== id) {
            val current = appData.baseLayers.find { bl -> bl.name == id}
            if (current != null) {
                style.addSource(RasterSource(current.name, TileSet("", current.url), 256))
                style.addLayerBelow(RasterLayer(current.name, current.name), this.appData.oldBase)

                style.removeLayer(this.appData.oldBase)
                style.removeSource(this.appData.oldBase)
                this.appData.oldBase = id
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
        LocationService.stopService(this)
    }

//    private fun flyToMyLocation() {
//        if let center = mapView.userLocation?.coordinate {
//            let camera = MGLMapCamera(lookingAtCenter: center, altitude: 4500, pitch: 15, heading: 0)
//            mapView.fly(to: camera)
//            appData.fly = false
//        }
//    }

    @SuppressLint("MissingPermission")
    private fun enableLocationComponent(loadedMapStyle: Style) {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            val customLocationComponentOptions = LocationComponentOptions.builder(this)
                .pulseEnabled(true)
                .build()

            val locationComponentActivationOptions = LocationComponentActivationOptions.builder(
                this,
                loadedMapStyle
            )
                .locationComponentOptions(customLocationComponentOptions)
                .build()

            mapboxMap.locationComponent.apply {
                activateLocationComponent(locationComponentActivationOptions)
                isLocationComponentEnabled = true
                cameraMode = CameraMode.TRACKING
                renderMode = RenderMode.COMPASS
            }
            LocationService.startService(this)
        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager.requestLocationPermissions(this)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 200) {
            appData.lat = mapboxMap.locationComponent.lastKnownLocation?.latitude ?: 0.0
            appData.lon = mapboxMap.locationComponent.lastKnownLocation?.longitude ?: 0.0

            if (appData.lat != 0.0) {
                ShareBottomSheetDialog().apply {
                    show(supportFragmentManager, ShareBottomSheetDialog.TAG)
                }
            }
        }
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        Toast.makeText(this, "Приложению необходим доступ к геолокации", Toast.LENGTH_LONG).show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            enableLocationComponent(mapboxMap.style!!)
        } else {
            Toast.makeText(this, "Ошибка! Доступ не получен!", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}
