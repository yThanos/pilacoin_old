package br.ufsm.csi.tapw.pilacoin.repository;

import br.ufsm.csi.tapw.pilacoin.model.Msgs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MsgsRepository extends JpaRepository<Msgs, Long> {
}
