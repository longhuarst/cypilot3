package cyfly.com.dji.cypilot3.MyAPI;

import dji.keysdk.KeyManager;
import dji.sdk.base.BaseProduct;
import dji.sdk.flightcontroller.FlyZoneManager;
import dji.sdk.flighthub.FlightHubManager;
import dji.sdk.mission.MissionControl;
import dji.sdk.sdkmanager.BluetoothProductConnector;
import dji.sdk.sdkmanager.DJISDKManager;
import dji.sdk.sdkmanager.LDMManager;

public class MyDJISDKManager {

    //=================SDK 管理 =================================
    //=================SDK Admin=================================






    // ================ 产品连接 ========================================
    //=================Product Connection=================================

    //获取连接到手机的设备类型，只能在注册APP成功后使用
    BaseProduct getProduct(){
        return DJISDKManager.getInstance().getProduct();
    }


    //用来建立手机和DJI设备之间的蓝牙连接，蓝牙连接需要首先使用startConnectionProduct建立SDK和DJI设备之间的连接
    BluetoothProductConnector getBluetoothProductConnector(){
        return  DJISDKManager.getInstance().getBluetoothProductConnector();
    }




    //设置蓝牙是否是唯一连接
    void setSupportOnlyForBluetoothDevice(boolean isBluetoothDevice){
        DJISDKManager.getInstance().setSupportOnlyForBluetoothDevice(isBluetoothDevice);
    }


    //开始连接SDK和DJI设备，这个方法应该在成功注册APP后被调用，
    // 之后在手机设备和DJI产品之间会有一个数据连接
    // 这个数据连接可以是USB连接、WIFI连接或者蓝牙。
    // 如果连接成功，会调用 onProductConnect 回调函数
    // 返回 true 如果连接建立成功，
    // 如果是用蓝牙连接手机和DJI产品，getBluetoothProductConnector应该被用来获取蓝牙链接，并可以处理蓝牙设备的连接。
    boolean startConnectionToProduct(){
        return DJISDKManager.getInstance().startConnectionToProduct();
    }


    //断开和DJI产品的连接
    void stopConnectionToProduct(){
        DJISDKManager.getInstance().stopConnectionToProduct();
    }





    //===========Debug and Logging=====================
    //===========调试和日志==============================


    //使用调试IP进入调试模式
    void enableBridgeModeWithBridgeAppIP(String bridgeAppIP){
        DJISDKManager.getInstance().enableBridgeModeWithBridgeAppIP(bridgeAppIP);
    }

    //获取日志路径
    String getLogPath(){
        return DJISDKManager.getInstance().getLogPath();
    }





    //============Managers==============================
    //============管理===================================

    FlyZoneManager getFlyZoneManager(){
        return DJISDKManager.getInstance().getFlyZoneManager();
    }



    //提供访问SDK key的接口
    KeyManager getKeyManager(){
        return DJISDKManager.getInstance().getKeyManager();
    }


    //提供访问MissionControl，用于管理任务
    MissionControl getMissionControl(){
        return DJISDKManager.getInstance().getMissionControl();
    }



    //提供访问FlightHubManager,这可以与DJI FlightHub 相互作用
    FlightHubManager getFlightHubManager(){
        return DJISDKManager.getInstance().getFlightHubManager();
    }




    //===============LDM =============================
    //===============本地数据模式 =============================


    //管理本地数据模式功能，本地数据模式提供开发者们选项去将SDK进入飞机模式，
    //限制其访问英特网，
    LDMManager getLDMManager(){
        return DJISDKManager.getInstance().getLDMManager();
    }


}
