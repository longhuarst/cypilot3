package cyfly.com.dji.cypilot3;

import dji.sdk.base.BaseProduct;
import dji.sdk.sdkmanager.BluetoothProductConnector;
import dji.sdk.sdkmanager.DJISDKManager;

public class MyDJISDKManager {




    //获取连接到手机的设备类型，只能在注册APP成功后使用
    BaseProduct getProduct(){
        return DJISDKManager.getInstance().getProduct();
    }


    //用来建立手机和DJI设备之间的蓝牙连接，蓝牙连接需要首先使用startConnectionProduct建立SDK和DJI设备之间的连接
    BluetoothProductConnector getBluetoothProductConnector(){
        return  DJISDKManager.getInstance().getBluetoothProductConnector();
    }





}
