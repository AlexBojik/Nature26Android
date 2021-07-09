package ru.bizit.nature26

import android.content.Context
import android.content.Intent
import android.hardware.SensorManager
import android.os.IBinder
import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import dagger.android.ContributesAndroidInjector
import dagger.android.DaggerService
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import io.reactivex.rxjava3.subjects.BehaviorSubject
import java.util.*

class Feature(
    var id: Int,
    var name: String,
    var description: String,
    var color: String?,
    var symbol: String?
) {}

class NameValue(
    var name: String,
    var value: String
) {}

class GeoObject (
    var id: Int,
    var layerId: Int,
    var name: String,
    var type: String,
//    var geoJson: Geometry,
    var description: String
)

class News (
    var id: Int,
    var start: Date,
    var description: String
) {}


class AppData() {
    var baseLayers: MutableList<BaseLayer> = mutableListOf(
        BaseLayer(1,
            "default",
            "Спутник (google)",
            "https://mt1.google.com/vt/lyrs=y&x={x}&y={y}&z={z}"
        )
    )
    val background = "{\n" +
            "  \"version\": 8,\n" +
            "  \"sources\": {},\n" +
            "  \"layers\": [\n" +
            "    {\n" +
            "      \"id\": \"background\",\n" +
            "      \"type\": \"background\",\n" +
            "      \"paint\": {\n" +
            "        \"background-color\": \"#ffffff\"\n" +
            "      }\n" +
            "    }\n" +
            "  ]\n" +
            "}\n"
    var oldBase = "default"
    var newBase: BehaviorSubject<String> = BehaviorSubject.create()
    var layersChanged: BehaviorSubject<Boolean> = BehaviorSubject.create()
    var layers: MutableList<Layer> = mutableListOf()
    var features: MutableList<String> = mutableListOf()
    var selectedLayers: MutableList<String> = mutableListOf()
    var featureLoading: BehaviorSubject<Boolean> = BehaviorSubject.create()
    var loadedFeatures: MutableList<GeoObject> = mutableListOf()
    var loadedNews: MutableList<News> = mutableListOf()
    var selectedObjects: MutableMap<Int, GeoObject> = mutableMapOf()
    var linearLayers: MutableMap<Int, Layer> = mutableMapOf()
    var user: User = User()
    var lat: Double = 0.0
    var lon: Double = 0.0
}

@Module
class AndroidModule(context: Context) {
    private val context: Context = context.applicationContext

    @Provides
    @Singleton
    internal fun provideApplicationContext() = context

    @Provides
    @Singleton
    internal fun provideSensorManager() =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
}

@Singleton
@Component(
    modules = arrayOf(
        AndroidInjectionModule::class,
        AndroidSupportInjectionModule::class,
        AndroidModule::class,
        MyModule::class,
        MainActivityModule::class,
        MyServiceModule::class,
        BaseLayersModule::class,
        LayersModule::class,
        FeaturesModule::class,
        NewsModule::class,
        ShareModule::class,
        AuthenticationActivityModule::class,
    )
)
interface MyApplicationComponent: AndroidInjector<MyApplicatoin>

@Module
class MyModule {
    @Provides
    @Singleton
    @Named("Long Live")
    fun provideLongLive() = AppData()
}

class MyService : DaggerService() {
    @Inject
    lateinit var manager: SensorManager

    override fun onBind(intent: Intent): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onCreate() {
        super.onCreate()
    }
}

@Module
abstract class MyServiceModule {
    @ContributesAndroidInjector
    abstract fun contributeServiceInjector(): MyService?
}