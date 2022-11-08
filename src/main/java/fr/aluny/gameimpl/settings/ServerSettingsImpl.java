package fr.aluny.gameimpl.settings;

import fr.aluny.gameapi.settings.ServerSettings;

public class ServerSettingsImpl implements ServerSettings {

    private boolean showRanks       = true;
    private boolean allowSpectators = true;

    @Override
    public boolean doesShowRank() {
        return this.showRanks;
    }

    @Override
    public void setShowRank(boolean show) {
        this.showRanks = show;
    }

    @Override
    public boolean doesAllowSpectators() {
        return this.allowSpectators;
    }

    @Override
    public void setAllowSpectators(boolean allow) {
        this.allowSpectators = allow;
    }
}
