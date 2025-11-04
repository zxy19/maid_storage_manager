package studio.fantasyit.maid_storage_manager.api.communicate.step;

public enum ActionResult {
    SUCCESS(true, true, true),
    CONTINUE(false, true, true),
    SOFT_FAIL(true, false, true),
    FAIL(true, false, false);

    final boolean end;
    final boolean success;
    final boolean keepon;

    ActionResult(boolean b, boolean success, boolean keepon) {
        end = b;
        this.success = success;
        this.keepon = keepon;
    }

    public boolean isEnd() {
        return end;
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isKeepon() {
        return keepon;
    }
}