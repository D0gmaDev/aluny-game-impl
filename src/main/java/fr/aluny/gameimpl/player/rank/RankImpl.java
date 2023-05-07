package fr.aluny.gameimpl.player.rank;

import fr.aluny.gameapi.player.rank.Rank;
import java.util.Collections;
import java.util.Set;

public final class RankImpl implements Rank {

    private final int         id;
    private final String      name;
    private final int         importanceIndex;
    private final String      prefix;
    private final String      colorCode;
    private final Set<String> permissions;
    private final boolean     superUser;

    public RankImpl(int id, String name, int importanceIndex, String prefix, String colorCode, Set<String> permissions) {
        this.id = id;
        this.name = name;
        this.importanceIndex = importanceIndex;
        this.prefix = prefix;
        this.colorCode = colorCode;
        this.permissions = permissions;
        this.superUser = permissions.contains("*");
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public int getImportanceIndex() {
        return this.importanceIndex;
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public String getColorCode() {
        return this.colorCode;
    }

    @Override
    public Set<String> getPermissions() {
        return Collections.unmodifiableSet(this.permissions);
    }

    @Override
    public boolean hasPermission(String permission) {
        return this.superUser || this.permissions.contains(permission);
    }

    @Override
    public boolean isHigherOrEqualThan(Rank other) {
        return this.importanceIndex >= other.getImportanceIndex();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || obj.getClass() != this.getClass())
            return false;

        return this.id == ((RankImpl) obj).id;
    }

}
