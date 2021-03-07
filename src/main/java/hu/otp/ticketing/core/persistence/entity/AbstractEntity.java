package hu.otp.ticketing.core.persistence.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.SequenceGenerator;

@Getter
@NoArgsConstructor
@MappedSuperclass
public class AbstractEntity {
    @Id
    @GeneratedValue(generator = "EntitySequence")
    @SequenceGenerator(name = "EntitySequence", sequenceName = "ENTITY_SEQ")
    protected Long id;
}
