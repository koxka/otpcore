insert into user (id, user_id, name, email) values (1,'1000', 'Teszt Aladár', 'teszt.aladar@otpmobil.com');
insert into user (id, user_id, name, email) values (2,'2000', 'Teszt Benedek', 'teszt.benedek@otpmobil.com');
insert into user (id, user_id, name, email) values (3,'3000', 'Teszt Cecília', 'teszt.cecilia@otpmobil.com');

insert into token (id, user_id, token) values (ENTITY_SEQ.NEXTVAL,1, 'bn6KIBcY0VZtHOvNdtjJM4QzQm56idPe');
insert into token (id, user_id, token) values (ENTITY_SEQ.NEXTVAL,1, 'QGa7r8JL7HKV7gUrKfXyjILjSqQ7VkO8');
insert into token (id, user_id, token) values (ENTITY_SEQ.NEXTVAL,1, 'af53GXfdES1J4K8x28iTYdgTUVgsjpQd');
insert into token (id, user_id, token) values (ENTITY_SEQ.NEXTVAL,2, 'iUGyHJHRTCiDH7RKqnV3rSwrXjmOsPHm');
insert into token (id, user_id, token) values (ENTITY_SEQ.NEXTVAL,2, 'IQuWHdOql21FkJjy3b3YrDe69pc');
insert into token (id, user_id, token) values (ENTITY_SEQ.NEXTVAL,3, 'Si4DZ9JosNoZVJP3Hy15FAM4wMRrKHPs');

insert into bank_card (id, user_id, card_id, card_number, cvc, name, amount, currency) values (ENTITY_SEQ.NEXTVAL,1, 'C0001', '5299706965433676', '123', 'Teszt Aladár', 1000, 'HUF');
insert into bank_card (id, user_id, card_id, card_number, cvc, name, amount, currency) values (ENTITY_SEQ.NEXTVAL,2, 'C0002', '5390508354245119', '456', 'Teszt Benedek', 2000, 'HUF');
insert into bank_card (id, user_id, card_id, card_number, cvc, name, amount, currency) values (ENTITY_SEQ.NEXTVAL,3, 'C0003', '4929088924014470', '789', 'Teszt Cecília', 3000, 'HUF');