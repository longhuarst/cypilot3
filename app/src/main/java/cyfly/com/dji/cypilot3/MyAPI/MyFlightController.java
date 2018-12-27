package cyfly.com.dji.cypilot3.MyAPI;

import android.support.annotation.NonNull;

import dji.common.flightcontroller.FlightControllerState;
import dji.sdk.base.BaseProduct;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;

public class MyFlightController {


    private  MyFlightController(){

    }

    private static class MyFlightControllerHolder{
        private final static MyFlightController instance = new MyFlightController();
    }

    public static MyFlightController getInstance(){
        return MyFlightControllerHolder.instance;
    }






    FlightController getFlightController(){
        BaseProduct product = DJISDKManager.getInstance().getProduct();

        FlightController controller = ((Aircraft)product).getFlightController();

        if (controller == null)
            return null;

        controller.setStateCallback(new FlightControllerState.Callback() {
            @Override
            public void onUpdate(@NonNull FlightControllerState flightControllerState) {

            }
        });

        return controller;
    }




}
