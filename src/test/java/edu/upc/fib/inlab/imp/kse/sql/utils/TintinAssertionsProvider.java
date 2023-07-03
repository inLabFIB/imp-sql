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


    /* --------------------------- TPC-C related methods --------------------------- */

    public static String getTPCCAssertions() {
        return AT_LEAST_ONE_LINE_ITEM +
            STOCK_INFO_FOR_EACH_ITEM +
            ORDER_IMPLIES_PAYMENT +
            NEW_ORDERS_HAVE_STOCK +
            BAD_CREDITORS_CANNOT_GO_TO_DIFFERENT_DISTRICTS;
    }

    public static String getTPCCAtLeastOneLineItem() {
        return AT_LEAST_ONE_LINE_ITEM;
    }
    public static String getTPCCStockInfoForEachItem() {
        return STOCK_INFO_FOR_EACH_ITEM;
    }
    public static String getTPCCOrderImpliesPayment() {
        return ORDER_IMPLIES_PAYMENT;
    }
    public static String getTPCCNewOrdersHaveStock() {
        return NEW_ORDERS_HAVE_STOCK;
    }
    public static String getTPCCBadCreditorsCannotGoToDifferentDistricts() {
        return BAD_CREDITORS_CANNOT_GO_TO_DIFFERENT_DISTRICTS;
    }




    /* --------------------------- TPC-H related assertions --------------------------- */

    private static final String CORRECT_DATES_ASSERTION = """
        CREATE ASSERTION correctDates CHECK ( NOT ( EXISTS (
            SELECT *
            FROM LINEITEM AS l
            JOIN ORDERS AS o ON (l.L_ORDERKEY = o.O_ORDERKEY)
            WHERE l.L_COMMITDATE < o.O_ORDERDATE
        )));
            
        """;

    private static final String AT_LEAST_ONE_ITEM_ASSERTION = """
        CREATE ASSERTION atLeastOneLineItem CHECK ( NOT ( EXISTS (
            SELECT *
            FROM ORDERS AS o
            WHERE NOT ( EXISTS (
                SELECT *
                FROM LINEITEM AS l
                WHERE l.L_ORDERKEY = o.O_ORDERKEY
            ))
        )));
                
        """;

    private static final String SUPPLIER_NOT_CUSTOMER_ASSERTION = """
        CREATE ASSERTION supplierNotCustomer CHECK ( NOT ( EXISTS (
            SELECT *
            FROM LINEITEM AS l
            JOIN SUPPLIER AS s ON (l.L_SUPPKEY = s.S_SUPPKEY)
            JOIN ORDERS AS o ON (l.L_ORDERKEY = o.O_ORDERKEY)
            JOIN CUSTOMER AS c ON (o.O_CUSTKEY = c.C_CUSTKEY)
            WHERE s.S_name = c.c_name
        )));
                
        """;

    private static final String SUPPLIER_HAS_OTHER_SUPPLIER_ASSERTION = """
        CREATE ASSERTION supplierHasOtherSupplier CHECK ( NOT ( EXISTS (
            SELECT *
            FROM LINEITEM AS l
            JOIN SUPPLIER AS s ON (l.L_SUPPKEY = s.S_SUPPKEY)
            WHERE NOT ( EXISTS (
                SELECT *
                FROM SUPPLIER AS s2
                JOIN PARTSUPP AS ps ON (s2.S_SUPPKEY = ps.PS_SUPPKEY)
                WHERE ps.PS_PARTKEY = l.L_PARTKEY AND s2.S_NATIONKEY <> s.S_NATIONKEY
            ))
        )));
                
        """;

    /* --------------------------- CV2 related assertions --------------------------- */

    private static final String NATIONAL_INTERNATIONAL = """
        CREATE ASSERTION NATIONAL_INTERNATIONAL CHECK ( NOT ( EXISTS (
            SELECT *
            FROM NATIONAL_COMPANY AS n
            INNER JOIN INTERNATIONAL_COMPANY AS i ON (n.c_fk = i.c_fk)
        )));
                
        """;

    private static final String FAST_QUALITY_EXPENSIVE = """
        CREATE ASSERTION FAST_QUALITY_EXPENSIVE CHECK ( NOT ( EXISTS (
            SELECT *
            FROM QUALITY AS q
            INNER JOIN FAST AS f ON (q.s_fk = f.s_fk)
            WHERE NOT ( EXISTS (
                SELECT *
                FROM EXPENSIVE AS e
                WHERE e.s_fk = q.s_fk
                ))
        )));
                
        """;

    private static final String AT_LEAST_ONE_SERVICE = """
        CREATE ASSERTION AT_LEAST_ONE_SERVICE CHECK ( NOT ( EXISTS (
            SELECT *
            FROM COMPANY AS c
            WHERE NOT ( EXISTS (
                SELECT *
                FROM SERVICE AS s
                WHERE c.c_pk = s.c_fk
            ))
        )));
                
        """;

    /* --------------------------- TPCC related assertions --------------------------- */

    private static final String AT_LEAST_ONE_LINE_ITEM = """
        CREATE ASSERTION atLeastOneLineItem CHECK ( NOT ( EXISTS (
            SELECT *
            FROM orders AS o
            WHERE NOT ( EXISTS (
                SELECT *
                FROM order_line AS l
                WHERE l.ol_o_id = o.o_id
            ))
        )));

        """;

    private static final String STOCK_INFO_FOR_EACH_ITEM = """
        CREATE ASSERTION stockInfoForEachItem CHECK ( NOT ( EXISTS (
            SELECT *
            FROM item i, warehouse w
            WHERE NOT ( EXISTS (
                SELECT *
                FROM stock s
                WHERE s.s_i_id = i.i_id AND s.s_w_id = w.w_id
            ))
        )));

        """;

    //[This business rule is not clear to be realistic, but initial data seems to satisfy it]
    private static final String ORDER_IMPLIES_PAYMENT = """
        CREATE ASSERTION orderImpliesPayment CHECK ( NOT ( EXISTS (
            SELECT *
            FROM orders o
            WHERE NOT ( EXISTS (
                SELECT *
                FROM history h
                WHERE h.h_c_id = o.o_c_id AND h.h_c_d_id = o.o_d_id AND h.h_c_w_id = o.o_w_id
            ))
        )));

        """;

    private static final String NEW_ORDERS_HAVE_STOCK = """
        CREATE ASSERTION newOrdersHaveStock CHECK ( NOT ( EXISTS (
            SELECT *
            FROM new_order n
            JOIN order_line ol on (n.no_d_id = ol.ol_d_id AND n.no_o_id = ol.ol_o_id AND n.no_w_id = ol.ol_w_id)
            JOIN stock s on (s.s_i_id = ol.ol_i_id and s.s_w_id = ol.ol_w_id)
            WHERE s.s_quantity = 0
        )));

        """;

    private static final String BAD_CREDITORS_CANNOT_GO_TO_DIFFERENT_DISTRICTS = """
        CREATE ASSERTION badCreditorsCannotGoToDifferentDistricts CHECK ( NOT ( EXISTS (
            SELECT *
            FROM orders o1
            JOIN new_order n1 on (n1.no_d_id = o1.o_d_id and n1.no_o_id = o1.o_id and n1.no_w_id = o1.o_w_id)
            JOIN customer c on (o1.o_c_id = c.c_id and o1.o_d_id = c.c_d_id and o1.o_w_id = c.c_w_id)
            JOIN orders o2 on (o2.o_c_id = c.c_id and o2.o_d_id = c.c_d_id and o2.o_w_id = c.c_w_id)
            JOIN new_order n2 on (n2.no_d_id = o2.o_d_id and n2.no_o_id = o2.o_id and n2.no_w_id = o2.o_w_id)
            WHERE c.c_credit = 'BC' and o1.o_d_id <> o2.o_d_id
        )));

        """;

}
