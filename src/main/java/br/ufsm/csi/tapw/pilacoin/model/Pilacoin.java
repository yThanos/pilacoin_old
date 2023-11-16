package br.ufsm.csi.tapw.pilacoin.model;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "pilacoin")
public class Pilacoin {
    @Id
    @Column(name = "nonce", unique = true)
    private String nonce;

    @Column(name = "status")
    private String status;
}
