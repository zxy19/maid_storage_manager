package studio.fantasyit.maid_storage_manager.integration.kubejs.binding;

public class KJSMSMCompacted {
    private final KJSMSMTypeCasting types;
    private final KJSMSMBinding enums;
    private final KJSMSMUtilities utilities;

    public KJSMSMCompacted(KJSMSMBinding enums, KJSMSMUtilities  utilities, KJSMSMTypeCasting types){
        this.enums = enums;
        this.utilities = utilities;
        this.types = types;
    }

    public KJSMSMTypeCasting getTypes(){
        return types;
    }
    public KJSMSMBinding getEnums(){
        return enums;
    }
    public KJSMSMUtilities getUtilities(){
        return utilities;
    }
}
