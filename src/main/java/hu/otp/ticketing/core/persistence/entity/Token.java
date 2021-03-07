package hu.otp.ticketing.core.persistence.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Token extends AbstractEntity {
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    private String token;
}
