package cyfly.com.dji.cypilot3;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.dji.mapkit.core.maps.DJIMap;
import com.dji.mapkit.core.models.DJILatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.common.flightcontroller.ConnectionFailSafeBehavior;
import dji.common.flightcontroller.RemoteControllerFlightMode;
import dji.common.model.LocationCoordinate2D;
import dji.common.util.CommonCallbacks;
import dji.sdk.base.BaseComponent;
import dji.sdk.base.BaseProduct;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;
import dji.ux.widget.MapWidget;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();
    public static final String FLAG_CONNECTION_CHANGE = "dji_sdk_connection_change";
    private static BaseProduct mProduct;
    private Handler mHandler;
    private static boolean isAppStarted = false;//程序运行标记

    


    //    权限列表
    private static final String[] REQUIRED_PERMISSION_LIST = new String[]{
            Manifest.permission.VIBRATE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
    };
    private List<String> missingPermission = new ArrayList<>();
    private AtomicBoolean isRegistrationInProgress = new AtomicBoolean(false);
    private static final int REQUEST_PERMISSION_CODE = 12345;



    private MapWidget mapWidget;
    private ViewGroup parentView;
    private View fpvWidget;
    private boolean isMapMini = true;

    private int height;
    private int width;
    private int margin;
    private int deviceWidth;
    private int deviceHeight;


    public static boolean isStarted() {
        return isAppStarted;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        isAppStarted = true;//APP已经启动

        // When the compile and target version is higher than 22, please request the following permission at runtime to ensure the SDK works well.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkAndRequestPermissions();
        }

        //setContentView(R.layout.activity_main);

        //Initialize DJI SDK Manager
        //mHandler = new Handler(Looper.getMainLooper());


        setContentView(R.layout.activity_main); //加载界面


        height = DensityUtil.dip2px(this, 100);
        width = DensityUtil.dip2px(this, 150);
        margin = DensityUtil.dip2px(this, 12);

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        deviceHeight = displayMetrics.heightPixels;
        deviceWidth = displayMetrics.widthPixels;

        mapWidget = (MapWidget) findViewById(R.id.map_widget);
        mapWidget.initAMap(new MapWidget.OnMapReadyListener() {
            @Override
            public void onMapReady(@NonNull DJIMap map) {




                map.setOnMapClickListener(new DJIMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(DJILatLng latLng) {
                        onViewClick(mapWidget);
                    }
                });
            }
        });
        mapWidget.onCreate(savedInstanceState);




        parentView = (ViewGroup) findViewById(R.id.root_view);



        fpvWidget = findViewById(R.id.fpv_widget);
        fpvWidget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onViewClick(fpvWidget);
            }
        });


        Toast.makeText(this, "SDK Version :"+DJISDKManager.getInstance().getSDKVersion().toString(), Toast.LENGTH_SHORT).show();



        //打印SDK版本号
        Log.e("cypilot","SDK Version :"+DJISDKManager.getInstance().getSDKVersion().toString());


        //请求数据的定时器
        flightControllerInit();



        //initAMap(savedInstanceState);//初始化高德地图



        //断线重连
//        IntentFilter i = new IntentFilter();
//        i.addAction(MApplication.FLAG);
    }

    private void resizeFPVWidget(int width, int height, int margin, int fpvInsertPosition) {
        RelativeLayout.LayoutParams fpvParams = (RelativeLayout.LayoutParams) fpvWidget.getLayoutParams();
        fpvParams.height = height;
        fpvParams.width = width;
        fpvParams.rightMargin = margin;
        fpvParams.bottomMargin = margin;
        fpvWidget.setLayoutParams(fpvParams);

        parentView.removeView(fpvWidget);
        parentView.addView(fpvWidget, fpvInsertPosition);
    }





    private class ResizeAnimation extends Animation {

        private View mView;
        private int mToHeight;
        private int mFromHeight;

        private int mToWidth;
        private int mFromWidth;
        private int mMargin;

        private ResizeAnimation(View v, int fromWidth, int fromHeight, int toWidth, int toHeight, int margin) {
            mToHeight = toHeight;
            mToWidth = toWidth;
            mFromHeight = fromHeight;
            mFromWidth = fromWidth;
            mView = v;
            mMargin = margin;
            setDuration(300);
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            float height = (mToHeight - mFromHeight) * interpolatedTime + mFromHeight;
            float width = (mToWidth - mFromWidth) * interpolatedTime + mFromWidth;
            RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) mView.getLayoutParams();
            p.height = (int) height;
            p.width = (int) width;
            p.rightMargin = mMargin;
            p.bottomMargin = mMargin;
            mView.requestLayout();
        }
    }



    //点击地图
    // 然后地图放大缩小
    private void onViewClick(View view) {
        if (view == mapWidget && !isMapMini) {
            resizeFPVWidget(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT, 0, 0);
            ResizeAnimation mapViewAnimation = new ResizeAnimation(mapWidget, deviceWidth, deviceHeight, width, height, margin);
            mapWidget.startAnimation(mapViewAnimation);
            isMapMini = true;
        } else if (view == mapWidget && isMapMini) {
            resizeFPVWidget(width, height, margin, 3);
            ResizeAnimation mapViewAnimation = new ResizeAnimation(mapWidget, width, height, deviceWidth, deviceHeight, 0);
            mapWidget.startAnimation(mapViewAnimation);
            isMapMini = false;
        }
    }









    @Override
    protected void onResume() {
        super.onResume();

        // Hide both the navigation bar and the status bar.
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        mapWidget.onResume();
    }



    @Override
    protected void onPause() {
        mapWidget.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mapWidget.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapWidget.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapWidget.onLowMemory();
    }





















    /**
     * Checks if there is any missing permissions, and
     * requests runtime permission if needed.
     */
    private void checkAndRequestPermissions() {
        // Check for permissions
        for (String eachPermission : REQUIRED_PERMISSION_LIST) {
            if (ContextCompat.checkSelfPermission(this, eachPermission) != PackageManager.PERMISSION_GRANTED) {
                missingPermission.add(eachPermission);
            }
        }
        // Request for missing permissions
        if (missingPermission.isEmpty()) {
            startSDKRegistration();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            showToast("Need to grant the permissions!");
            ActivityCompat.requestPermissions(this,
                    missingPermission.toArray(new String[missingPermission.size()]),
                    REQUEST_PERMISSION_CODE);
        }

    }



    /**
     * Result of runtime permission request
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Check for granted permission and remove from missing list
        if (requestCode == REQUEST_PERMISSION_CODE) {
            for (int i = grantResults.length - 1; i >= 0; i--) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    missingPermission.remove(permissions[i]);
                }
            }
        }
        // If there is enough permission, we will start the registration
        if (missingPermission.isEmpty()) {
            startSDKRegistration();
        } else {
            showToast("Missing permissions!!!");
        }
    }










    private void startSDKRegistration() {
        if (isRegistrationInProgress.compareAndSet(false, true)) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    showToast("registering, pls wait...");
                    //注册DJI APP
                    DJISDKManager.getInstance().registerApp(MainActivity.this.getApplicationContext(), new DJISDKManager.SDKManagerCallback() {
                        @Override
                        public void onRegister(DJIError djiError) {
                            if (djiError == DJISDKError.REGISTRATION_SUCCESS) {
                                showToast("Register Success");
                                DJISDKManager.getInstance().startConnectionToProduct();
                            } else {
                                showToast("Register sdk fails, please check the bundle id and network connection!");
                            }
                            Log.v(TAG, djiError.getDescription());
                        }

                        @Override
                        public void onProductDisconnect() {
                            Log.d(TAG, "onProductDisconnect");
                            showToast("Product Disconnected");
                            notifyStatusChange();

                        }
                        @Override
                        public void onProductConnect(BaseProduct baseProduct) {
                            Log.d(TAG, String.format("onProductConnect newProduct:%s", baseProduct));
                            showToast("Product Connected");
                            notifyStatusChange();

                        }
                        @Override
                        public void onComponentChange(BaseProduct.ComponentKey componentKey, BaseComponent oldComponent,
                                                      BaseComponent newComponent) {

                            if (newComponent != null) {
                                newComponent.setComponentListener(new BaseComponent.ComponentListener() {

                                    @Override
                                    public void onConnectivityChange(boolean isConnected) {
                                        Log.d(TAG, "onComponentConnectivityChanged: " + isConnected);
                                        notifyStatusChange();
                                    }
                                });
                            }
                            Log.d(TAG,
                                    String.format("onComponentChange key:%s, oldComponent:%s, newComponent:%s",
                                            componentKey,
                                            oldComponent,
                                            newComponent));

                        }
                    });
                }
            });
        }
    }





    private void notifyStatusChange() {
//        有bug
//        mHandler.removeCallbacks(updateRunnable);
//        mHandler.postDelayed(updateRunnable, 500);
    }

    private Runnable updateRunnable = new Runnable() {

        @Override
        public void run() {
            Intent intent = new Intent(FLAG_CONNECTION_CHANGE);
            sendBroadcast(intent);
        }
    };

    private void showToast(final String toastMsg) {

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_LONG).show();
            }
        });

    }











    //断线重连

//    protected BroadcastReceiver receiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//
//        }
//    };




//////////////////////////////////////////////////
//////////////////////////////////////////////////
//////////////////////////////////////////////////
//////////////////////////////////////////////////
//////////////////////////////////////////////////
//////////////////////////////////////////////////


    //实时获取飞机数据
    Timer flightControllerTimer = null;//定时器
    BaseProduct product = null;
    FlightController controller = null;



    //更新飞行控制对象
    boolean updateFlightContollerInstance(){
        if (product == null){
            product = DJISDKManager.getInstance().getProduct();
        }
        if (product !=null){
            if (controller == null){
                controller = ((Aircraft)product).getFlightController();
            }
            if (controller != null){
                return true;
            }else{
                return false;
            }
        }else{
            return false;
        }
    }



    private void flightControllerInit(){
        flightControllerTimer = new Timer();
        Log.e("cypilot3","timer start");
        flightControllerTimer.schedule(new TimerTask() {
            @Override
            public void run() {

                product = DJISDKManager.getInstance().getProduct();

                if (product != null){
                    controller = ((Aircraft)product).getFlightController();

                    if (controller != null){

                        int compassCount = controller.getCompassCount();//获取罗盘个数

                        float heading = controller.getCompass().getHeading();

                        Log.e("cypilot3","compassCounter = "+compassCount);
                        Log.e("cypilot3","heading = "+heading);
                    }
                }


                Log.e("cypilot3","timer...");
                //Toast.makeText(MainActivity,"timer",Toast.LENGTH_LONG).show();

                flightControllerInit();
            }
        },6000);
        Log.e("cypilot3","timer complete");
    }






//////////////////////////////////////////////////
//////////////////////////////////////////////////
//////////////////////////////////////////////////
//////////////////////////////////////////////////
//////////////////////////////////////////////////
//////////////////////////////////////////////////


    //地图相关操作
    //private MapWidget mapWidget = null;
    private int mapProvider;
    public static final String MAP_PROVIDER = "MapProvider";

    void SetHomeBitmap(){

    }



    //初始化高德地图
    void initAMap(@Nullable Bundle savedInstanceState){
        mapWidget = findViewById(R.id.map_widget);

        MapWidget.OnMapReadyListener onMapReadyListener = new MapWidget.OnMapReadyListener() {
            @Override
            public void onMapReady(@NonNull DJIMap djiMap) {
                djiMap.setMapType(DJIMap.MapType.Normal);//设置地图类型为正常
            }
        };
        Intent intent = getIntent();

        mapWidget.initAMap(onMapReadyListener);//直接设定为高德地图
//        mapProvider = intent.getIntExtra(MAP_PROVIDER,0);
//        switch (mapProvider){
//            case 0:
//                mapWidget.initHereMap(onMapReadyListener);
//                break;
//            case 1:
//                mapWidget.initGoogleMap(onMapReadyListener);
//                break;
//            case 2:
//                mapWidget.initAMap(onMapReadyListener);
//                break;
//            default:
//                break;
//        }
        mapWidget.onCreate(savedInstanceState);


    }













    /////////////////////////////////////////
    /////////////////////////////////////////
    /////////////////////////////////////////
    /////////////////////////////////////////
    /////////////////////////////////////////
    /////////////////////////////////////////

    //自动飞行


    //起飞
    void takeoff(){
        //if ()

        if (updateFlightContollerInstance() == false)
            return; //没有获取到飞行控制器实例


        //Starts aircraft takeoff. Takeoff is considered complete when the aircraft is hovering 1.2 meters (4 feet) above the ground. Completion block is called when aircraft crosses 0.5 meters (1.6 feet). If the motors are already on, this command cannot be executed.
        //开始起飞
        //当飞机悬停在相对地面 1.2 米处则被认为是起飞完成
        //当穿越0.5米时，Completion block将会被调用
        //如果电机已经在旋转，则这个命令将不会被执行
        controller.startTakeoff(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                //起飞结果
                Log.e("cypilot3","takeoff() --> result = "+djiError.toString());
            }
        });
    }



    //退出起飞
    void cancelTakeoff(){
        if (updateFlightContollerInstance() == false)
            return; //没有获取到飞行控制器实例


        //Stops aircraft takeoff. If called before startTakeoff is complete, the aircraft will cancel takeoff (startTakeoff completion block will return an error) and hover at the current height.
        //停止起飞,需要在起飞结束之前调用
        //飞机将会退出起飞
        //startTakeoff 的完成回调函数将会返回出错 并且飞机悬停在当前高度
        controller.cancelTakeoff(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {

            }
        });

    }


    //开始降落
    void startLanding(){
        if (updateFlightContollerInstance() == false)
            return; //没有获取到飞行控制器实例

        //Starts auto-landing of the aircraft. Returns completion block once aircraft begins to descend for auto-land.
        //开始 自动降落
        //回调函数将会在开始自动降落时调用
        controller.startLanding(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {

            }
        });
    }

    //退出降落
    void cancelLanding(){
        if (updateFlightContollerInstance() == false)
            return; //没有获取到飞行控制器实例


        //Stops auto-landing of the aircraft. If called before startLanding is complete, then the auto landing will be canceled (startLanding completion block will return an error) and the aircraft will hover at its current location.
        //停止自动降落 -- 需要在 startLanding 结束之前调用
        //然后自动降落将会被退出,startLanding 的回调函数将会返回错误
        //飞机将会悬停在当前位置
        controller.cancelLanding(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {

            }
        });
    }


    //确认降落
    void confirmLanding(){
        if (updateFlightContollerInstance() == false)
            return; //没有获取到飞行控制器实例


        //Confirms continuation of landing action. When the clearance between the aircraft and the ground is less than 0.3m, the aircraft will pause landing and wait for user's confirmation. Can use isLandingConfirmationNeeded in FlightControllerState to check if confirmation is needed. It is supported by flight controller firmware 3.2.0.0 and above.
        //确认降落行动的延续性，当飞机和地面的距离小于 30cm，飞机将会暂停降落并等待用户确认，
        // 可以检查 FlightControllerState 中的 isLandingConfirmationNeeded 参数，得知是否需要确认
        // 仅支持飞行控制器固件版本 3.2.0.0 和以上.
        controller.confirmLanding(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {

            }
        });
    }


    //返回Home点
    void startGoHome(){
        if (updateFlightContollerInstance() == false)
            return; //没有获取到飞行控制器实例

        //The aircraft will start to go home. The completion callback will return execution result once this method is invoked.
        //飞机将会开始返回 Home 点，
        // 当该方法被调用，完成回调函数将会立刻返回结果
        controller.startGoHome(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {

            }
        });
    }


    //退出返航
    void cancelGoHome(){
        if (updateFlightContollerInstance() == false)
            return; //没有获取到飞行控制器实例

        //The aircraft will stop going home and will hover in place.
        //飞机将会退出返航操作,并且悬停
        controller.cancelGoHome(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {

            }
        });
    }



    //设置返航点【使用经纬度】
    void setHomeLocation(double latitude, double longitude){
        if (updateFlightContollerInstance() == false)
            return; //没有获取到飞行控制器实例

        //第一参数 纬度  第二参数 经度
        LocationCoordinate2D homeLocation = new LocationCoordinate2D(latitude, longitude);

        //Sets the home location of the aircraft. The home location is where the aircraft returns when commanded by startGoHome, when its signal is lost or when the battery is below the lowBatteryWarning threshold. The user should be careful setting a new home point location, as sometimes the product will not be under user control when returning home. A home location is valid if it is within 30m of one of the following:
        //
        //initial take-off location
        //aircraft's current location
        //current mobile location with at least kCLLocationAccuracyNearestTenMeters accuracy level
        //current remote controller's location as shown by RC GPS.


        //设置飞机的返航点,（返航点是 startGoHome 命令，飞机返回的位置），
        //  当飞机信号丢失、电池电量低于低电量警告阈值时返回的地点。
        //  用户需要仔细设置一个新的返航点，有时候当飞机在返航时可能会不受用户控制
        //  一个有效的返航点应该是一下其中之一的30M内
        //  1. 初始的起飞点
        //  2. 飞机当前的位置
        //  3. 当前手机的位置（kCLLocationAccuracyNearestTenMeters 既当前定位精度在10M以内）
        //  4. RC-GPS显示的当前遥控器的位置
        controller.setHomeLocation(homeLocation, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {

            }
        });
    }


    //设置返航点为飞机的当前位置
    void setHomeLocationUsingAircraftCurrentLocation(){
        if (updateFlightContollerInstance() == false)
            return; //没有获取到飞行控制器实例

        //Sets the home location of the aircraft to the current location of the aircraft. See setHomeLocation for details on home point use.
        //设置返航点为飞机的当前位置
        controller.setHomeLocationUsingAircraftCurrentLocation(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {

            }
        });
    }

    //获取返航点
    void getHomeLocation(){
        if (updateFlightContollerInstance() == false)
            return; //没有获取到飞行控制器实例

        //Gets the home point of the aircraft.
        //获取当前飞机的返航点
        controller.getHomeLocation(new CommonCallbacks.CompletionCallbackWith<LocationCoordinate2D>() {
            @Override
            public void onSuccess(LocationCoordinate2D locationCoordinate2D) {
                //成功
                double latitude = locationCoordinate2D.getLatitude();//获取纬度
                double longitude = locationCoordinate2D.getLongitude();//获取经度

                Log.e("cypilot3","current home[latitude|longitude] = ["+latitude+"|"+longitude+"]");

            }

            @Override
            public void onFailure(DJIError djiError) {
                //失败

            }
        });
    }



    //设置返航点高度【单位 米】
    void setGoHomeHeightInMeters(@IntRange(from = 20, to = 500) int height){
        if (updateFlightContollerInstance() == false)
            return; //没有获取到飞行控制器实例

        //Sets the minimum altitude, relative to where the aircraft took off, at which the aircraft must be before going home. This can be useful when the user foresees obstacles in the aircraft's flight path. If the aircraft's current altitude is higher than the minimum go home altitude, it will go home at its current altitude. The valid range for the altitude is from 20m to 500m.
        //设置最小高度，相对于飞机起飞高度，飞机在返航前必须达到的高度，当飞机路径当中有障碍物时非常有用，
        // 如果当前飞机的高度比最小返航高度高，它将会在当前高度返航，
        // 高度有效范围为 20 米 ～ 500 米
        controller.setGoHomeHeightInMeters(height, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {

            }
        });
    }

    //获取返航高度
    void getGoHomeHeightInMeters(){
        if (updateFlightContollerInstance() == false)
            return; //没有获取到飞行控制器实例

        //Gets the minimum altitude (relative to the take-off location in meters) at which the aircraft must be before going home.
        //获取最小返航高度（相对于起飞高度）
        controller.getGoHomeHeightInMeters(new CommonCallbacks.CompletionCallbackWith<Integer>() {
            @Override
            public void onSuccess(Integer integer) {
                int height = integer.intValue();
                Log.e("cypilot3","GoHomeHeightInMeters = "+height);
            }

            @Override
            public void onFailure(DJIError djiError) {

            }
        });
    }


    ConnectionFailSafeBehavior cyConnectionFailSafeBehavior = null;

    void setConnectionFailSafeBehavior(){
        if (updateFlightContollerInstance() == false)
            return; //没有获取到飞行控制器实例

    //          HOVER(0),
        //    LANDING(1),
        //    GO_HOME(2),
        //    UNKNOWN(255);
        //ConnectionFailSafeBehavior behavior;

        //controller.getConnectionFailSafeBehavior();

        if (cyConnectionFailSafeBehavior == null)
            return ;

        controller.setConnectionFailSafeBehavior(cyConnectionFailSafeBehavior, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {

            }
        });
    }



    void getConnectionFailSafeBehavior(){
        if (updateFlightContollerInstance() == false)
            return; //没有获取到飞行控制器实例

        controller.getConnectionFailSafeBehavior(new CommonCallbacks.CompletionCallbackWith<ConnectionFailSafeBehavior>() {
            @Override
            public void onSuccess(ConnectionFailSafeBehavior connectionFailSafeBehavior) {
                cyConnectionFailSafeBehavior = connectionFailSafeBehavior;
            }

            @Override
            public void onFailure(DJIError djiError) {

            }
        });
    }


    //Sets the low battery warning threshold as a percentage. The percentage must be in the range of [15, 50].
    //设置低电量警告的阈值[百分比，15~50%]
    void setLowBatteryWarningThreshold(@IntRange(from = 15, to = 50) int percent){
        if (updateFlightContollerInstance() == false)
            return; //没有获取到飞行控制器实例

        controller.setLowBatteryWarningThreshold(percent, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {

            }
        });
    }



    //Gets the low battery warning threshold as a percentage.
    //获取低电量电池警告的阈值百分比
    void getLowBatteryWarningThreshold(){
        if (updateFlightContollerInstance() == false)
            return; //没有获取到飞行控制器实例

        controller.getLowBatteryWarningThreshold(new CommonCallbacks.CompletionCallbackWith<Integer>() {
            @Override
            public void onSuccess(Integer integer) {

            }

            @Override
            public void onFailure(DJIError djiError) {

            }
        });
    }



    //Sets the serious low battery warning threshold as a percentage. The minimum value is 10. The maximum value is value from getLowBatteryWarningThreshold minus 5.
    //设置严格的低电量阈值百分比
    void setSeriousLowBatteryWarningThreshold(@IntRange(from = 10, to = 45) int percent){
        if (updateFlightContollerInstance() == false)
            return; //没有获取到飞行控制器实例

        controller.setSeriousLowBatteryWarningThreshold(percent, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {

            }
        });
    }


    //获取严格的低电量阈值百分比
    void getSeriousLowBatteryWarningThreshold(){
        if (updateFlightContollerInstance() == false)
            return; //没有获取到飞行控制器实例

        controller.getLowBatteryWarningThreshold(new CommonCallbacks.CompletionCallbackWith<Integer>() {
            @Override
            public void onSuccess(Integer integer) {

            }

            @Override
            public void onFailure(DJIError djiError) {

            }
        });
    }


    //Enables/disables Smart Return-To-Home (RTH) feature. When it is enabled, aircraft will request to go home when remaining battery is only enough for completing the go-home action.
    //打开/关闭 智能返航
    void setSmartReturnToHomeEnabled(boolean enabled){
        if (updateFlightContollerInstance() == false)
            return; //没有获取到飞行控制器实例

        controller.setSmartReturnToHomeEnabled(enabled, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {

            }
        });
    }


    //Determines whether Smart Return-To-Home (RTH) feature is enabled or not. When it is enabled, aircraft will request to go home when remaining battery is only enough for completing the go-home action.
    //获取 智能返航 状态
    void getSmartReturnToHomeEnabled(){
        if (updateFlightContollerInstance() == false)
            return; //没有获取到飞行控制器实例

        controller.getSmartReturnToHomeEnabled(new CommonCallbacks.CompletionCallbackWith<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {

            }

            @Override
            public void onFailure(DJIError djiError) {

            }
        });
    }


    //Confirms or cancels the Smart Return-To-Home (RTH) request. When Smart RTH is enabled, the aircraft will request to go home when the battery is only enough for going home. Before executing the go-home action, the aircraft will wait for the confirmation from users with 10 seconds count-down. If the "confirmed" parameter is false, the request is canceled and the aircraft will not execute go-home action. Otherwise, go-home action will start. Smart RTH will be triggered only once during the same flight. Flight controller with firmware version lower than 3.0.0.0 does not support confirming the Smart RTH request. User can either cancel the request or wait for the countdown to start go-home action.
    void confirmSmartReturnToHomeRequest(boolean confirmed){
        if (updateFlightContollerInstance() == false)
            return; //没有获取到飞行控制器实例

        controller.confirmSmartReturnToHomeRequest(confirmed, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {

            }
        });
    }




    //========================  Flight Mode ==============================

    //Gets the mapping between the flight modes and the flight mode switch positions on the remote controller. Elements 0, 1, and 2 of the returned array map to POSITION_ONE, POSITION_TWO, and POSITION_THREE of the getFlightModeSwitch. The value of each Enum item represents the corresponding value of the RemoteControllerFlightMode Enum representing the flight mode.
    //The mapping is fixed for the Phantom series, Inspire series, Mavic Pro, and M100. For N3, A3, Matrice 600, and Matrice 600 Pro the mapping is firmware dependent. With firmware version 3.2.11.0 or above, the mapping can be customized in DJI Assistant 2.
    //
    void getRCSwitchFlightModeMapping(){
        if (updateFlightContollerInstance() == false)
            return; //没有获取到飞行控制器实例

        controller.getRCSwitchFlightModeMapping(new CommonCallbacks.CompletionCallbackWith<RemoteControllerFlightMode[]>() {
            @Override
            public void onSuccess(RemoteControllerFlightMode[] remoteControllerFlightModes) {

            }

            @Override
            public void onFailure(DJIError djiError) {

            }
        });
    }


    //Enables/disables multiple-flight mode. When multiple-flight mode is enabled, user can change the aircraft's mode to P/F/A/S mode by toggling the switch on the remote controller. If it is disabled, the aircraft will be in P mode.
    void setMultipleFlightModeEnabled(boolean enabled){
        if (updateFlightContollerInstance() == false)
            return; //没有获取到飞行控制器实例

        controller.setMultipleFlightModeEnabled(enabled, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {

            }
        });
    }


    //Determines whether multiple-flight mode is enabled. When multiple-flight mode is enabled, user can change the aircraft's mode to P/F/A/S mode by toggling the switch on the remote controller. If it is disabled, the aircraft will be in P mode.
    void getMultipleFlightModeEnabled(){
        if (updateFlightContollerInstance() == false)
            return; //没有获取到飞行控制器实例


        controller.getMultipleFlightModeEnabled(new CommonCallbacks.CompletionCallbackWith<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {

            }

            @Override
            public void onFailure(DJIError djiError) {

            }
        });
    }



}
