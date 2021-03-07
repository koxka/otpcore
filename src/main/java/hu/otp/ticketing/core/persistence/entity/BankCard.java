package hu.otp.ticketing.core.persistence.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class BankCard extends AbstractEntity {
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @Column(unique = true)
    private String cardId;
    private String cardNumber;
    private String cvc;
    private String name;
    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    private Currency currency;
}
