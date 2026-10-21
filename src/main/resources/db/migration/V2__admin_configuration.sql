create table lsh_configuration_state (
                                         id varchar(30) primary key,
                                         published_version bigint not null default 1,
                                         draft_version bigint not null default 1,
                                         published_at timestamp with time zone null,
                                         published_by varchar(200) null,
                                         entity_version bigint not null default 0,
                                         updated_at timestamp with time zone not null
);

create table lsh_deadline_rules (
                                    id varchar(100) primary key,

                                    name varchar(250) not null,
                                    description varchar(2000) not null,
                                    days integer not null,
                                    day_type varchar(30) not null,
                                    trigger_name varchar(250) not null,
                                    enabled boolean not null,

                                    draft_name varchar(250) null,
                                    draft_description varchar(2000) null,
                                    draft_days integer null,
                                    draft_day_type varchar(30) null,
                                    draft_trigger_name varchar(250) null,
                                    draft_enabled boolean null,

                                    dirty boolean not null default false,
                                    draft_updated_by varchar(200) null,
                                    draft_updated_at timestamp with time zone null,

                                    published_by varchar(200) null,
                                    published_at timestamp with time zone null,

                                    version bigint not null default 0,
                                    created_at timestamp with time zone not null,
                                    updated_at timestamp with time zone not null,

                                    constraint ck_deadline_rule_days
                                        check (days between 0 and 3650),

                                    constraint ck_deadline_rule_draft_days
                                        check (
                                            draft_days is null
                                                or draft_days between 0 and 3650
                                            ),

                                    constraint ck_deadline_rule_day_type
                                        check (
                                            day_type in (
                                                         'CALENDAR_DAYS',
                                                         'BUSINESS_DAYS'
                                                )
                                            ),

                                    constraint ck_deadline_rule_draft_day_type
                                        check (
                                            draft_day_type is null
                                                or draft_day_type in (
                                                                      'CALENDAR_DAYS',
                                                                      'BUSINESS_DAYS'
                                                )
                                            )
);

insert into lsh_configuration_state (
    id,
    published_version,
    draft_version,
    published_at,
    published_by,
    entity_version,
    updated_at
) values (
             'GLOBAL',
             1,
             1,
             current_timestamp,
             'SYSTEM',
             0,
             current_timestamp
         );

insert into lsh_deadline_rules (
    id,
    name,
    description,
    days,
    day_type,
    trigger_name,
    enabled,
    dirty,
    published_by,
    published_at,
    version,
    created_at,
    updated_at
) values
      (
          'SUMMONS_DEFEND',
          'Summons - intention to defend',
          'Deadline for delivering an intention to defend',
          10,
          'CALENDAR_DAYS',
          'receipt',
          true,
          false,
          'SYSTEM',
          current_timestamp,
          0,
          current_timestamp,
          current_timestamp
      ),
      (
          'MOTION_OPPOSE',
          'Notice of motion - intention to oppose',
          'Deadline for delivering an intention to oppose',
          5,
          'CALENDAR_DAYS',
          'receipt',
          true,
          false,
          'SYSTEM',
          current_timestamp,
          0,
          current_timestamp,
          current_timestamp
      ),
      (
          'ADDITIONAL_RESPONSE',
          'Additional response',
          'Fallback response deadline from the configured trigger',
          15,
          'CALENDAR_DAYS',
          'configured trigger',
          true,
          false,
          'SYSTEM',
          current_timestamp,
          0,
          current_timestamp,
          current_timestamp
      );

create index idx_lsh_deadline_rules_dirty
    on lsh_deadline_rules (dirty);