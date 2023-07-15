package fr.aluny.gameimpl.world.anchor;

import fr.aluny.gameapi.world.anchor.Anchor;
import java.util.List;
import java.util.Optional;

public interface AnchorSource {

    Optional<Anchor> findOne(String key);

    List<Anchor> findMany(String key);

    List<Anchor> findAll();

}
