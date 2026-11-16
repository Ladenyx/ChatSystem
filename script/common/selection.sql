\if :{?domain}
\else
\set domain all
\endif
SELECT :'domain' IN ('all','messaging','e2ee','spam') AS valid_domain,
    :'domain' IN ('all','messaging') AS run_messaging,
    :'domain' IN ('all','e2ee') AS run_e2ee,
    :'domain' IN ('all','spam') AS run_spam
\gset
\if :valid_domain
\else
\echo 'Invalid domain. Choose all, messaging, e2ee or spam.'
\quit 3
\endif
