create table lsh_contracts (
                               id uuid primary key,
                               reference varchar(30) not null unique,
                               title varchar(300) not null,
                               counterparty varchar(300) not null,
                               contract_type varchar(200) null,
                               effective_date date null,
                               expiry_date date null,
                               auto_renewal boolean not null default false,
                               notice_period_days integer null,
                               status varchar(40) not null,
                               uploaded_by_id varchar(200) not null,
                               uploaded_by_name varchar(200) not null,
                               extraction_confidence integer null,
                               extraction_notes varchar(4000) null,
                               version bigint not null default 0,
                               created_at timestamp with time zone not null,
                               updated_at timestamp with time zone not null,

                               constraint ck_contract_notice_period
                                   check (
                                       notice_period_days is null
                                           or notice_period_days between 0 and 3650
                                       ),

                               constraint ck_contract_confidence
                                   check (
                                       extraction_confidence is null
                                           or extraction_confidence between 0 and 100
                                       ),

                               constraint ck_contract_status
                                   check (
                                       status in (
                                                  'UPLOADED',
                                                  'EXTRACTION_PENDING',
                                                  'AWAITING_CONFIRMATION',
                                                  'ACTIVE',
                                                  'EXPIRED',
                                                  'TERMINATED'
                                           )
                                       )
);

create table lsh_contract_documents (
                                        id uuid primary key,
                                        contract_id uuid not null,
                                        file_name varchar(500) not null,
                                        content_type varchar(200) not null,
                                        size_bytes bigint not null,
                                        primary_document boolean not null,
                                        external_id varchar(500) not null,
                                        web_url varchar(2000) null,
                                        uploaded_by varchar(200) not null,
                                        uploaded_at timestamp with time zone not null,

                                        constraint fk_contract_document_contract
                                            foreign key (contract_id)
                                                references lsh_contracts (id)
                                                on delete cascade,

                                        constraint ck_contract_document_size
                                            check (
                                                size_bytes > 0
                                                    and size_bytes <= 26214400
                                                )
);

create index idx_lsh_contracts_created_at
    on lsh_contracts (created_at desc);

create index idx_lsh_contracts_expiry_date
    on lsh_contracts (expiry_date);

create index idx_lsh_contract_documents_contract
    on lsh_contract_documents (contract_id);