package cyfly.com.dji.cypilot3.MyMission;

import dji.common.error.DJIError;
import dji.sdk.mission.timeline.actions.MissionAction;
import dji.sdk.sdkmanager.DJISDKManager;

public class MyMissionAction {



    MissionAction action = null; //任务行动


    MissionAction getAction(){
        if (action != null)
            return action;

        return new MissionAction() {
            @Override
            protected void startListen() {

            }

            @Override
            protected void stopListen() {

            }

            @Override
            public void run() {

            }

            @Override
            public boolean isPausable() {
                return false;
            }

            @Override
            public void stop() {

            }

            @Override
            public DJIError checkValidity() {
                return null;
            }
        };
       // return DJISDKManager.getInstance()
    }



    boolean isRunning(){
        return false;
    }

}
