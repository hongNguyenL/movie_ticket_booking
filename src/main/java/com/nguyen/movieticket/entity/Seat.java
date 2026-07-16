package com.nguyen.movieticket.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "seats")
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hall_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Hall hall;

    @Column(name = "row_label", length = 10)
    private String rowLabel;

    @Column(name = "seat_number")
    private Integer seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_type", length = 20)
    @Builder.Default
    private SeatType seatType = SeatType.STANDARD;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
}
