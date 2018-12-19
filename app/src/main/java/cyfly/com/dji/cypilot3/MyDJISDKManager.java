package cyfly.com.dji.cypilot3;

import dji.sdk.base.BaseProduct;
import dji.sdk.sdkmanager.DJISDKManager;

public class MyDJISDKManager {




    //获取连接到手机的设备类型，只能在注册APP成功后使用
    BaseProduct getProduct(){
        return DJISDKManager.getInstance().getProduct();
    }







}
