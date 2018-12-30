package cyfly.com.dji.cypilot3.MyMission;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import dji.common.error.DJIError;
import dji.sdk.mission.MissionControl;
import dji.sdk.mission.activetrack.ActiveTrackOperator;
import dji.sdk.mission.followme.FollowMeMissionOperator;
import dji.sdk.mission.hotpoint.HotpointMissionOperator;
import dji.sdk.mission.panorama.PanoramaMissionOperator;
import dji.sdk.mission.tapfly.TapFlyMissionOperator;
import dji.sdk.mission.timeline.TimelineElement;
import dji.sdk.mission.timeline.triggers.Trigger;
import dji.sdk.mission.waypoint.WaypointMissionOperator;
import dji.sdk.sdkmanager.DJISDKManager;

public class MyMissionControl {


    MissionControl control;



    //获取任务控制
    MissionControl getMissionControl(){
        control = DJISDKManager.getInstance().getMissionControl();
        return control;
    }


    //Mission Operators



    //Returns the operator for waypoint missions.
    @NonNull
    WaypointMissionOperator getWaypointMissionOperator(){
        if (control == null)
            getMissionControl();
        if (control == null)
            return null;

        return control.getWaypointMissionOperator();
    }



    //Returns the operator for Hotpoint missions.
    @NonNull
    HotpointMissionOperator getHotpointMissionOperator(){
        if (control == null)
            getMissionControl();
        if (control == null)
            return null;

        return control.getHotpointMissionOperator();

    }



    //Returns the operator for Follow Me missions.
    @NonNull
    FollowMeMissionOperator getFollowMeMissionOperator(){
        if (control == null)
            getMissionControl();
        if (control == null)
            return null;

        return control.getFollowMeMissionOperator();
    }


    //Returns the operator for ActiveTrack missions.
    @NonNull
    ActiveTrackOperator getActiveTrackOperator(){
        if (control == null)
            getMissionControl();
        if (control == null)
            return null;

        return control.getActiveTrackOperator();
    }


    //Returns the operator for TapFly missions.
    @NonNull
    TapFlyMissionOperator getTapFlyMissionOperator(){
        if (control == null)
            getMissionControl();
        if (control == null)
            return null;

        return control.getTapFlyMissionOperator();
    }




    @NonNull
    PanoramaMissionOperator getPanoramaMissionOperator() {
        if (control == null)
            getMissionControl();
        if (control == null)
            return null;

        return control.getPanoramaMissionOperator();
    }


//========================================Timeline
//========================================Timeline
//========================================Timeline


    //Adds an element to the end of the Timeline.
    DJIError scheduleElement(@NonNull TimelineElement element){
        if (control == null)
            getMissionControl();
        if (control == null)
            return null;

        return control.scheduleElement(element);
    }




    //Adds a list of elements to the end of the Timeline.
    DJIError scheduleElements(@NonNull List<TimelineElement> elements){
        if (control == null)
            getMissionControl();
        if (control == null)
            return null;

        return control.scheduleElements(elements);
    }



    //Insert an element into the Timeline at an index.
    DJIError scheduleElementAtIndex(TimelineElement element, int index){
        if (control == null)
            getMissionControl();
        if (control == null)
            return null;



        return control.scheduleElementAtIndex(element,index);
    }



    //Remove element from the Timeline. If the same element was used more than once in the Timeline, then the first one will be removed.
    void unscheduleElement(TimelineElement element){
        if (control == null)
            getMissionControl();
        if (control == null)
            return;

        control.unscheduleElement(element);
    }


    //Removes the Element at an index in the Timeline.
    void unscheduleElementAtIndex(int index){
        if (control == null)
            getMissionControl();
        if (control == null)
            return;

        control.unscheduleElementAtIndex(index);
    }



    //Returns the element at a given index in the Timeline.
    @Nullable
    TimelineElement scheduledElementAtIndex(int index){
        if (control == null)
            getMissionControl();
        if (control == null)
            return null;

        return control.scheduledElementAtIndex(index);
    }



    //Returns the number of elements within the Timeline.
    int scheduledCount(){
        if (control == null)
            getMissionControl();
        if (control == null)
            return 0;

        return control.scheduledCount();
    }



    //Clears the Timeline, removing all elements.
    void unscheduleEverything(){

        if (control == null)
            getMissionControl();
        if (control == null)
            return;


        control.unscheduleEverything();
    }




    //Set the triggers for the Timeline.
    void setTriggers(List<Trigger> triggers){
        if (control == null)
            getMissionControl();
        if (control == null)
            return;

        control.setTriggers(triggers);
    }




    //Returns the list of triggers which are dependents of the Timeline.
    List<Trigger> getTriggers(){
        if (control == null)
            getMissionControl();
        if (control == null)
            return null;

        return control.getTriggers();
    }




    //Start Timeline execution from the current element index getCurrentTimelineMarker.
    void startTimeline(){
        if (control == null)
            getMissionControl();
        if (control == null)
            return ;

        control.startTimeline();
    }



    //Pause execution of the Timeline, and current element (if pausible). If the current element is not pausible then this method will do nothing.
    void pauseTimeline(){
        if (control == null)
            getMissionControl();
        if (control == null)
            return ;

        control.pauseTimeline();
    }




    //Resume element and Timeline execution.
    void resumeTimeline(){
        if (control == null)
            getMissionControl();
        if (control == null)
            return ;

        control.resumeTimeline();
    }


    //Stops the execution of the Timeline, resets the marker index to 0. Release thread resources of timeline.
    void stopTimeline(){
        if (control == null)
            getMissionControl();
        if (control == null)
            return ;

        control.stopTimeline();
    }




    //true if the Timeline is running.
    boolean isTimelineRunning(){
        if (control == null)
            getMissionControl();
        if (control == null)
            return false;

        return control.isTimelineRunning();
    }



    //Tells the index in the timeline array that marks where the timeline is currently at. Changing this value will reposition where the timeline will resume/start work.
    int getCurrentTimelineMarker(){
        if (control == null)
            getMissionControl();
        if (control == null)
            return 0;

        return control.getCurrentTimelineMarker();

    }



    //Sets the element index within the Timeline to start execution. This value can be set only when a Timeline is stopped (not executing or paused).
    void setCurrentTimelineMarker(int currentTimelineMaker){
        if (control == null)
            getMissionControl();
        if (control == null)
            return ;

        control.setCurrentTimelineMarker(currentTimelineMaker);
    }



    //true if Timeline is paused
    boolean isTimelinePaused(){
        if (control == null)
            getMissionControl();
        if (control == null)
            return false;

        return control.isTimelinePaused();
    }

}
