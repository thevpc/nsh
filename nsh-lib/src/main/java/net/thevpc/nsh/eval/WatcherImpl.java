package net.thevpc.nsh.eval;

public class WatcherImpl implements NshContext.Watcher {
    boolean stopped;
    boolean askStopped;
    int threads;

    @Override
    public void stop() {
        if (!askStopped) {
            askStopped = true;
        }
    }

    @Override
    public boolean isStopped() {
        return stopped;
    }

    public boolean isAskStopped() {
        return askStopped;
    }
}
