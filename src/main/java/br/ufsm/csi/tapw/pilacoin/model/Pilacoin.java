package br.ufsm.csi.tapw.pilacoin.model;

import jakarta.persistence.*;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pila")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuario idDono;

    @Column(name = "nonce")
    private String nonce;
}
