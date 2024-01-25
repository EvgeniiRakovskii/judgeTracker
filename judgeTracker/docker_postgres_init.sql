CREATE TABLE IF NOT EXISTS cases
(
    id serial primary key,
    custom_name character varying(100)  NOT NULL,
    url character varying(254)  NOT NULL,
    case_number character varying(20) NOT NULL,
    number_of_column integer,
    motion_of_case character varying(8000)
    )