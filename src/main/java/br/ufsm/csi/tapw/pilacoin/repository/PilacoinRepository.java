package br.ufsm.csi.tapw.pilacoin.repository;

import br.ufsm.csi.tapw.pilacoin.model.Pilacoin;
import br.ufsm.csi.tapw.pilacoin.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PilacoinRepository extends JpaRepository<Pilacoin, String> {
}
