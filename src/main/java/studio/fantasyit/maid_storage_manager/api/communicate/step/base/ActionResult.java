package studio.fantasyit.maid_storage_manager.api.communicate.step.base;

public enum ActionResult {
    SUCCESS(true, true),
    CONTINUE(false, true),
    FAIL(true, false);

    final boolean end;
    final boolean success;

    ActionResult(boolean b, boolean success) {
        end = b;
        this.success = success;
    }

    public boolean isEnd() {
        return end;
    }

    public boolean isSuccess() {
        return success;
    }
}