package studio.fantasyit.maid_storage_manager.util;

public class BehaviorBreath {
    int breath = 5;
    public boolean breathTick(){
        if(breath > 0){
            breath--;
        }
        if(breath == 0){
            breath = 5;
            return true;
        }
        return false;
    }
}
