create table UTILISATEUR
(
    UTILISATEUR_ID INT auto_increment,
    NOM            VARCHAR(20)           not null,
    PRENOM         VARCHAR(20)           not null,
    COURRIEL       VARCHAR(255)          not null,
    ACTIF          BOOLEAN default FALSE not null,
    constraint UTILISATEUR_PK
        primary key (UTILISATEUR_ID)
);

create table LIVRE
(
    LIVRE_ID          INT auto_increment,
    TITRE             VARCHAR(50) not null,
    RESUME            TEXT        not null,
    NB_PAGE           INT         not null,
    ISBN              VARCHAR(32) not null,
    AUTEUR            VARCHAR(50) not null,
    LANGUE            VARCHAR(10),
    ANNEE_PUBLICATION YEAR,
    constraint LIVRE_PK
        primary key (LIVRE_ID)
);

create table PRET
(
    PRET_ID              INT auto_increment,
    UTILISATEUR_ID       INT                          not null,
    LIVRE_ID             INT                          not null,
    DATE_PRET            DATE    default CURRENT_DATE not null,
    DUREE_PRET           INT     default 20           not null,
    RENDU                BOOLEAN default FALSE        not null,
    NB_RELANCE           INT     default 0            not null,
    DATE_DERNIER_RELANCE DATE,
    constraint PRET_PK
        primary key (PRET_ID),
    constraint PRET_LIVRE_LIVRE_ID_FK
        foreign key (LIVRE_ID) references LIVRE (LIVRE_ID),
    constraint PRET_UTILISATEUR_UTILISATEUR_ID_FK
        foreign key (UTILISATEUR_ID) references UTILISATEUR (UTILISATEUR_ID)
);

comment on table PRET is 'Liste des prets';

comment on column PRET.DUREE_PRET is 'durée du pret en jours';
