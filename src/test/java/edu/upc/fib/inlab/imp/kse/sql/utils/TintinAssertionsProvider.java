package edu.upc.fib.inlab.imp.kse.sql.utils;

public class TintinAssertionsProvider {

    /* --------------------------- TPC-H related methods --------------------------- */

    public static String getTPCHAssertions() {
        return CORRECT_DATES_ASSERTION +
            AT_LEAST_ONE_ITEM_ASSERTION +
            SUPPLIER_NOT_CUSTOMER_ASSERTION +
            SUPPLIER_HAS_OTHER_SUPPLIER_ASSERTION;
    }

    public static String getTPCHCorrectDatesAssertion() {
        return CORRECT_DATES_ASSERTION;
    }

    public static String getTPCHAtLeastOneItemAssertion() {
        return AT_LEAST_ONE_ITEM_ASSERTION;
    }

    public static String getTPCHSupplierNotCustomerAssertion() {
        return SUPPLIER_NOT_CUSTOMER_ASSERTION;
    }

    public static String getTPCHSupplierHasOtherSupplierAssertion() {
        return SUPPLIER_HAS_OTHER_SUPPLIER_ASSERTION;
    }

    /* --------------------------- CV2 related methods --------------------------- */

    public static String getCV2Assertions() {
        return NATIONAL_INTERNATIONAL +
            FAST_QUALITY_EXPENSIVE +
            AT_LEAST_ONE_SERVICE;
    }

    public static String getCV2NationalInternational() {
        return NATIONAL_INTERNATIONAL;
    }

    public static String getCV2FastQualityExpensive() {
        return FAST_QUALITY_EXPENSIVE;
    }

    public static String getCV2AtLeastOneService() {
        return AT_LEAST_ONE_SERVICE;
    }


    /* --------------------------- TPC-H related assertions --------------------------- */

    private static final String CORRECT_DATES_ASSERTION = """
        CREATE ASSERTION correctDates CHECK ( NOT EXISTS (
            SELECT *
            FROM LINEITEM AS l JOIN ORDERS AS o ON (l.L_ORDERKEY = o.O_ORDERKEY)
            WHERE l.L_COMMITDATE < o.O_ORDERDATE
        ));
            
        """;

    private static final String AT_LEAST_ONE_ITEM_ASSERTION = """
        CREATE ASSERTION atLeastOneLineItem CHECK ( NOT EXISTS (
            SELECT *
            FROM ORDERS AS o
            WHERE NOT EXISTS (SELECT *
                              FROM LINEITEM AS l
                              WHERE l.L_ORDERKEY = o.O_ORDERKEY)
        ));
                
        """;

    private static final String SUPPLIER_NOT_CUSTOMER_ASSERTION = """
        CREATE ASSERTION supplierNotCustomer CHECK ( NOT EXISTS (
               SELECT *
               FROM LINEITEM AS l JOIN SUPPLIER AS s ON (l.L_SUPPKEY = s.S_SUPPKEY)
                                   JOIN ORDERS AS o ON (l.L_ORDERKEY = o.O_ORDERKEY)
                                   JOIN CUSTOMER AS c ON (o.O_CUSTKEY = c.C_CUSTKEY)
               WHERE s.S_name = c.c_name
        ));
                
        """;

    private static final String SUPPLIER_HAS_OTHER_SUPPLIER_ASSERTION = """
        CREATE ASSERTION supplierHasOtherSupplier CHECK ( NOT EXISTS (
              SELECT *
              FROM LINEITEM AS l
                      JOIN SUPPLIER AS s ON (l.L_SUPPKEY = s.S_SUPPKEY)
              WHERE NOT EXISTS (SELECT *
                                FROM SUPPLIER AS s2
                                      JOIN PARTSUPP AS ps ON (s2.S_SUPPKEY = ps.PS_SUPPKEY)
                                WHERE ps.PS_PARTKEY = l.L_PARTKEY AND s2.S_NATIONKEY <> s.S_NATIONKEY)
        ));
                
        """;

    /* --------------------------- CV2 related assertions --------------------------- */

    private static final String NATIONAL_INTERNATIONAL = """
        CREATE ASSERTION NATIONAL_INTERNATIONAL CHECK ( NOT EXISTS (
            SELECT *
            FROM NATIONAL_COMPANY AS n INNER JOIN INTERNATIONAL_COMPANY AS i ON (n.c_fk = i.c_fk)
        ));
                
        """;

    private static final String FAST_QUALITY_EXPENSIVE = """
        CREATE ASSERTION FAST_QUALITY_EXPENSIVE CHECK ( NOT EXISTS (
            SELECT *
            FROM QUALITY AS q INNER JOIN FAST AS f ON (q.s_fk = f.s_fk)
            WHERE NOT EXISTS (
                SELECT *
                FROM EXPENSIVE AS e
                WHERE e.s_fk = q.s_fk)
        ));
                
        """;

    private static final String AT_LEAST_ONE_SERVICE = """
        CREATE ASSERTION AT_LEAST_ONE_SERVICE CHECK ( NOT EXISTS (
            SELECT *
            FROM COMPANY AS c
            WHERE NOT EXISTS (
                SELECT *
                FROM SERVICE AS s
                WHERE c.c_pk = s.c_fk)
        ));
                
        """;

}
